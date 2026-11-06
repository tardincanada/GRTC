package com.periodicelements.farm;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/** Tolerant Seeds (2.0.0): plant wheat on farmland that Field Clear spares and that grows a tenth faster. */
public class TolerantSeedsItem extends Item {
	public static final double EXTRA_GROWTH = 0.1;

	public TolerantSeedsItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		BlockPos farmland = ctx.getBlockPos();
		BlockPos crop = farmland.up();
		if (!ctx.getWorld().getBlockState(farmland).isOf(Blocks.FARMLAND) || !ctx.getWorld().getBlockState(crop).isAir()) {
			return ActionResult.PASS;
		}
		if (ctx.getWorld() instanceof ServerWorld world) {
			world.setBlockState(crop, Blocks.WHEAT.getDefaultState());
			FieldState.of(world).plantTolerant(crop);
			// 3.37.0: the wheat carries the EPSPS transgene and passes it to its seeds.
			com.periodicelements.crops.CropGenomes.of(world).put(crop, com.periodicelements.crops.CropGenomes.of(
					com.periodicelements.crops.Genome.withTransgene(com.periodicelements.crops.Genome.DEFAULT,
							com.periodicelements.crops.Genome.EPSPS), 0));
			world.playSound(null, crop, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1f, 1f);
			if (ctx.getPlayer() != null) {
				ctx.getStack().decrementUnlessCreative(1, ctx.getPlayer());
				ctx.getPlayer().sendMessage(Text.translatable("gui.periodic_elements.tolerant"), true);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
