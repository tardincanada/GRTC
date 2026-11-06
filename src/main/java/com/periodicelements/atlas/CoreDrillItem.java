package com.periodicelements.atlas;

import com.periodicelements.ModContent;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/** The Diamond Core Drill (1863): use it on the ground for a core of every rock and ore down to the bottom. */
public class CoreDrillItem extends ModContent.TippedItem {
	public static final int COOLDOWN = 60;

	public CoreDrillItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		if (ctx.getWorld().isClient || !(ctx.getPlayer() instanceof ServerPlayerEntity p)) {
			return ActionResult.SUCCESS;
		}
		return drill(p, ctx.getStack(), ctx.getBlockPos()) ? ActionResult.CONSUME : ActionResult.FAIL;
	}

	/** Takes a core at {@code pos}; returns false if the player may not. */
	public static boolean drill(ServerPlayerEntity p, ItemStack stack, net.minecraft.util.math.BlockPos pos) {
		var known = ResearchAccess.known(p);
		if (!Atlas.has(known, "ores") || !Atlas.canDraw(known)) {
			p.sendMessage(Text.translatable("gui.periodic_elements.atlas.core.needs").formatted(Formatting.RED), true);
			return false;
		}
		if (p.getWorld().getRegistryKey() != World.OVERWORLD) return false;
		p.sendMessage(Prospecting.core(p, pos), false);
		p.getItemCooldownManager().set(stack.getItem(), COOLDOWN);
		stack.damage(1, p, EquipmentSlot.MAINHAND);
		p.getWorld().playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1f, 0.6f);
		return true;
	}
}
