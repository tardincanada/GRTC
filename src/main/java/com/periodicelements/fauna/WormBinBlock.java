package com.periodicelements.fauna;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Worm Bin: stage 0 empty, 1 earthworms at work, 2 humus ready. FOOD (0-4, not drawn) is how much leaf litter,
 * plants or manure is left to eat; each portion eaten may finish a batch. Used empty-handed when ready: 2 humus.
 */
public class WormBinBlock extends Block {
	public static final MapCodec<WormBinBlock> CODEC = createCodec(WormBinBlock::new);
	public static final IntProperty STAGE = IntProperty.of("stage", 0, 2);
	public static final IntProperty FOOD = IntProperty.of("food", 0, 4);

	public WormBinBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(STAGE, 0).with(FOOD, 0));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(STAGE, FOOD);
	}

	/** Leaves, saplings, flowers, crops' produce and manure feed the worms. */
	static boolean feed(ItemStack stack) {
		if (SericultureTrayBlock.is(stack, "manure") || stack.isIn(ItemTags.LEAVES) || stack.isIn(ItemTags.SAPLINGS)
				|| stack.isIn(ItemTags.FLOWERS) || stack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
			return true;
		}
		return stack.contains(net.minecraft.component.DataComponentTypes.FOOD);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		int st = state.get(STAGE), food = state.get(FOOD);
		BlockState next;
		if (st == 0 && SericultureTrayBlock.is(stack, "earthworm")) {
			next = state.with(STAGE, 1);
		} else if (st == 1 && food < 4 && feed(stack)) {
			next = state.with(FOOD, food + 1);
		} else {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			world.setBlockState(pos, next);
			if (!player.getAbilities().creativeMode) stack.decrement(1);
			world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 0.8f, 1f);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(STAGE) != 2) return ActionResult.PASS;
		if (!world.isClient) {
			Item humus = Fauna.item("humus");
			if (humus != null) Block.dropStack(world, pos.up(), new ItemStack(humus, 2));
			world.setBlockState(pos, state.with(STAGE, 1));
			world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1f, 1f);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(STAGE) == 1 && state.get(FOOD) > 0 && random.nextInt(2) == 0) {
			BlockState next = state.with(FOOD, state.get(FOOD) - 1);
			if (random.nextInt(2) == 0) next = next.with(STAGE, 2);
			world.setBlockState(pos, next);
		}
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && state.get(STAGE) > 0 && Fauna.item("earthworm") != null) {
			Block.dropStack(world, pos, new ItemStack(Fauna.item("earthworm")));
		}
		return super.onBreak(world, pos, state, player);
	}
}
