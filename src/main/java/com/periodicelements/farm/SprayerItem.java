package com.periodicelements.farm;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * The 2.0.0 sprayers, used on a block: the DDT Duster (insects on the ground take harm for three days; chickens near
 * lay half as often), Weed Killer (2,4-D: broad-leaved plants die, grass and crops live), Pre-Emergent (atrazine: no
 * grass spreads onto the ground for ten days) and Field Clear (glyphosate: every plant dies but tolerant crops).
 */
public class SprayerItem extends Item {
	public enum Kind { DDT, WEED_KILLER, PRE_EMERGENT, FIELD_CLEAR }

	public static final int RADIUS = 4;
	public static final long DDT_TICKS = 72000;
	public static final long PRE_EMERGENT_TICKS = 240000;
	private final Kind kind;

	public SprayerItem(Settings settings, Kind kind) {
		super(settings);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	/** Broad-leaved plants: flowers, ferns, vines, leaves, bushes and saplings, the targets of 2,4-D. */
	public static boolean broadLeaved(BlockState s) {
		return s.isIn(BlockTags.FLOWERS) || s.isIn(BlockTags.LEAVES) || s.isOf(Blocks.FERN) || s.isOf(Blocks.LARGE_FERN)
				|| s.getBlock() instanceof VineBlock || s.isOf(Blocks.DEAD_BUSH) || s.getBlock() instanceof SaplingBlock
				|| s.isOf(Blocks.SWEET_BERRY_BUSH) || s.isOf(Blocks.GLOW_LICHEN) || s.isOf(Blocks.CAVE_VINES)
				|| s.isOf(Blocks.CAVE_VINES_PLANT) || s.isOf(Blocks.MOSS_CARPET) || s.isOf(Blocks.LILY_PAD);
	}

	/** Every green plant, for glyphosate: the broad-leaved ones, grasses and crops. */
	public static boolean anyPlant(BlockState s) {
		return broadLeaved(s) || s.getBlock() instanceof PlantBlock || s.isOf(Blocks.SHORT_GRASS) || s.isOf(Blocks.TALL_GRASS);
	}

	/** Kills plants round the centre; returns how many. Field Clear spares crops planted from Tolerant Seeds. */
	public static int kill(ServerWorld world, BlockPos center, Kind kind) {
		FieldState fields = FieldState.of(world);
		int n = 0;
		for (BlockPos p : BlockPos.iterate(center.add(-RADIUS, -RADIUS, -RADIUS), center.add(RADIUS, RADIUS, RADIUS))) {
			BlockState s = world.getBlockState(p);
			boolean dies = kind == Kind.WEED_KILLER ? broadLeaved(s) && !(s.getBlock() instanceof CropBlock)
					: anyPlant(s) && !(s.getBlock() instanceof CropBlock && (fields.tolerant(p)
							|| com.periodicelements.crops.Crops.glyphosateTolerant(world, p)));
			if (dies) {
				world.breakBlock(p, false);
				fields.forget(p);
				n++;
			}
		}
		return n;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		if (!(ctx.getWorld() instanceof ServerWorld world)) {
			return ActionResult.SUCCESS;
		}
		BlockPos pos = ctx.getBlockPos();
		long now = world.getTime();
		switch (kind) {
			case DDT -> {
				FieldState.of(world).add(FieldState.Kind.DDT, pos.up(), RADIUS, now + DDT_TICKS);
				msg(ctx, Text.translatable("gui.periodic_elements.dusted"));
			}
			case PRE_EMERGENT -> {
				FieldState.of(world).add(FieldState.Kind.PRE_EMERGENT, pos, RADIUS, now + PRE_EMERGENT_TICKS);
				msg(ctx, Text.translatable("gui.periodic_elements.pre_emergent"));
			}
			case WEED_KILLER -> msg(ctx, Text.translatable("gui.periodic_elements.weeded", kill(world, pos, kind)));
			case FIELD_CLEAR -> msg(ctx, Text.translatable("gui.periodic_elements.field_clear", kill(world, pos, kind)));
		}
		world.spawnParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 1.5, 0.3, 1.5, 0.01);
		world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.4f, 2f);
		if (ctx.getPlayer() != null) {
			ctx.getStack().damage(1, ctx.getPlayer(), LivingEntity.getSlotForHand(ctx.getHand()));
		}
		return ActionResult.SUCCESS;
	}

	private static void msg(ItemUsageContext ctx, Text t) {
		if (ctx.getPlayer() != null) {
			ctx.getPlayer().sendMessage(t, true);
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
