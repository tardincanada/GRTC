package com.periodicelements.atlas;

import com.periodicelements.ModContent;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Set;
import java.util.UUID;

/**
 * Use: opens the map (client). Sneak and use with paper in the other hand (Portolan Charts): makes an Atlas Copy of
 * everything drawn so far, for another player.
 */
public class FieldAtlasItem extends ModContent.TippedItem {
	/** Set by the client initializer; does nothing on a dedicated server. */
	public static Runnable open = () -> {
	};

	public FieldAtlasItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		Set<String> known = ResearchAccess.known(user);
		if (!Atlas.canDraw(known)) {
			if (!world.isClient) {
				user.sendMessage(Text.translatable("gui.periodic_elements.atlas.needs").formatted(Formatting.RED), true);
			}
			return TypedActionResult.fail(stack);
		}
		ItemStack other = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if (user.isSneaking()) {
			if (!world.isClient && user instanceof ServerPlayerEntity sp) {
				makeCopy(sp, known, other);
			}
			return TypedActionResult.success(stack, world.isClient);
		}
		if (world.isClient) {
			open.run();
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	/** Makes an Atlas Copy from the paper in {@code paper}; returns the copy, or empty if it could not. */
	public static ItemStack makeCopy(ServerPlayerEntity player, Set<String> known, ItemStack paper) {
		if (!Atlas.has(known, "copy")) {
			player.sendMessage(Text.translatable("gui.periodic_elements.atlas.copy.needs").formatted(Formatting.RED), true);
			return ItemStack.EMPTY;
		}
		if (!paper.isOf(Items.PAPER)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.atlas.copy.paper").formatted(Formatting.YELLOW), true);
			return ItemStack.EMPTY;
		}
		AtlasData mine = AtlasData.of(player.getServer(), player.getUuid());
		UUID id = UUID.randomUUID();
		AtlasData copy = AtlasData.copy(player.getServer(), id);
		copy.mergeFrom(mine, Integer.MAX_VALUE);
		if (!player.getAbilities().creativeMode) {
			paper.decrement(1);
		}
		ItemStack out = new ItemStack(Atlas.ATLAS_COPY);
		NbtCompound tag = new NbtCompound();
		tag.putUuid("copy", id);
		out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
		ItemStack made = out.copy();
		if (!player.getInventory().insertStack(out)) {
			player.dropItem(out, false);
		}
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.PLAYERS, 1f, 1f);
		player.sendMessage(Text.translatable("gui.periodic_elements.atlas.copied", copy.tiles.size()), true);
		return made;
	}
}
