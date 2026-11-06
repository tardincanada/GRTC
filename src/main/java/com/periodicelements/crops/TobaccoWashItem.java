package com.periodicelements.crops;

import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/** Tobacco Wash (3.23.0): nicotine soaked out of tobacco leaves; used on a crop, it clears caterpillars 2 blocks around. */
public class TobaccoWashItem extends Item {
	public static final String UNLOCK = "item:tobacco_wash";
	public static final int RADIUS = 2;

	public TobaccoWashItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		BlockPos pos = context.getBlockPos();
		if (Genetics.info(context.getWorld().getBlockState(pos).getBlock()) == null) {
			return ActionResult.PASS;
		}
		PlayerEntity player = context.getPlayer();
		if (context.getWorld() instanceof ServerWorld sw && player != null) {
			if (!player.isCreative() && !Research.allows(ResearchAccess.known(player), UNLOCK)) {
				player.sendMessage(Text.translatable("gui.periodic_elements.wash.needs"), true);
				return ActionResult.FAIL;
			}
			int n = Genetics.curePests(sw, pos, RADIUS);
			sw.spawnParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 30, 1.5, 0.3, 1.5, 0);
			sw.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1f, 1f);
			context.getStack().damage(1, player, net.minecraft.entity.LivingEntity.getSlotForHand(context.getHand()));
			player.sendMessage(Text.translatable("gui.periodic_elements.wash.cured", n), true);
		}
		return ActionResult.success(context.getWorld().isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
