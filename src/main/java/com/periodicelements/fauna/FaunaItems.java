package com.periodicelements.fauna;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

/** The fauna plan's items with behaviour of their own. */
public final class FaunaItems {
	private FaunaItems() {
	}

	static void desc(String key, List<Text> tooltip) {
		tooltip.add(Text.translatable(key + ".desc").formatted(Formatting.GRAY));
	}

	/** An item whose tooltip is its ".desc" lang line. */
	public static class Described extends Item {
		public Described(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			desc(getTranslationKey(), tooltip);
		}
	}

	public static class DescribedBlock extends BlockItem {
		public DescribedBlock(Block block, Settings settings) {
			super(block, settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			desc(getBlock().getTranslationKey(), tooltip);
		}
	}

	/** Manure and humus: used on a plant like bone meal. */
	public static class Fertilizer extends BoneMealItem {
		public Fertilizer(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			desc(getTranslationKey(), tooltip);
		}
	}

	/** Drunk, it cures poison (scorpion, Gila monster, frog, jelly). */
	public static class Antivenom extends Described {
		public Antivenom(Settings settings) {
			super(settings);
		}

		@Override
		public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
			if (!world.isClient) user.removeStatusEffect(StatusEffects.POISON);
			if (user instanceof PlayerEntity p && p.getAbilities().creativeMode) return stack;
			stack.decrement(1);
			if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
			if (user instanceof PlayerEntity p && !p.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE))) {
				p.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
			}
			return stack;
		}

		@Override
		public int getMaxUseTime(ItemStack stack, LivingEntity user) {
			return 32;
		}

		@Override
		public UseAction getUseAction(ItemStack stack) {
			return UseAction.DRINK;
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			return ItemUsage.consumeHeldItem(world, user, hand);
		}
	}

	/** Cochineal or sticklac: used on the side of a cactus (or jungle log) it starts a colony there. */
	public static class ColonyStarter extends Described {
		private final String colony;
		private final Block support;

		public ColonyStarter(Settings settings, String colony, Block support) {
			super(settings);
			this.colony = colony;
			this.support = support;
		}

		@Override
		public ActionResult useOnBlock(ItemUsageContext ctx) {
			World world = ctx.getWorld();
			Direction side = ctx.getSide();
			BlockState on = world.getBlockState(ctx.getBlockPos());
			BlockPos at = ctx.getBlockPos().offset(side);
			Block block = Fauna.block(colony);
			if (block == null || !on.isOf(support) || side.getAxis().isVertical() || !world.getBlockState(at).isReplaceable()) {
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				world.setBlockState(at, block.getDefaultState().with(ColonyBlock.FACING, side));
				world.playSound(null, at, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 0.6f, 1.4f);
				if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().creativeMode) ctx.getStack().decrement(1);
			}
			return ActionResult.success(world.isClient);
		}
	}
}
