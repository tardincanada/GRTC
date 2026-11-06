package com.periodicelements.atlas;

import com.periodicelements.ModContent;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** The Prospecting Magnetometer (1921): reads the anomalies of the 7 x 7 chunks around you onto the Atlas. */
public class MagnetometerItem extends ModContent.TippedItem {
	public static final int COOLDOWN = 100;

	public MagnetometerItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient || !(user instanceof ServerPlayerEntity p)) {
			return TypedActionResult.success(stack, world.isClient);
		}
		return read(p, stack) ? TypedActionResult.success(stack) : TypedActionResult.fail(stack);
	}

	public static boolean read(ServerPlayerEntity p, ItemStack stack) {
		var known = ResearchAccess.known(p);
		if (!Atlas.has(known, "anomalies") || !Atlas.canDraw(known)) {
			p.sendMessage(Text.translatable("gui.periodic_elements.atlas.magnetometer.needs").formatted(Formatting.RED), true);
			return false;
		}
		if (p.getWorld().getRegistryKey() != World.OVERWORLD) return false;
		int[] r = Prospecting.survey(p);
		p.sendMessage(Text.translatable("gui.periodic_elements.atlas.magnetometer", r[2], Prospecting.strength(r[0]),
				Prospecting.strength(r[1])), true);
		p.getItemCooldownManager().set(stack.getItem(), COOLDOWN);
		return true;
	}
}
