package com.periodicelements.fauna;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Sericulture Tray: stage 0 empty, 1 silkworms (hungry), 2 fed with white mulberry leaves (spinning), 3 cocoons. Used
 * empty-handed at stage 3 it gives 2 or 3 cocoons and keeps a brood of worms (stage 1).
 */
public class SericultureTrayBlock extends Block {
	public static final MapCodec<SericultureTrayBlock> CODEC = createCodec(SericultureTrayBlock::new);
	public static final IntProperty STAGE = IntProperty.of("stage", 0, 3);
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 6, 16);

	public SericultureTrayBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(STAGE, 0));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	static boolean is(ItemStack stack, String path) {
		return Registries.ITEM.getId(stack.getItem()).getPath().equals(path);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		int st = state.get(STAGE);
		boolean worm = st == 0 && is(stack, "silkworm");
		boolean leaves = st == 1 && is(stack, "white_mulberry_leaves");
		if (!worm && !leaves) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!world.isClient) {
			world.setBlockState(pos, state.with(STAGE, st + 1));
			if (!player.getAbilities().creativeMode) stack.decrement(1);
			world.playSound(null, pos, SoundEvents.BLOCK_AZALEA_LEAVES_PLACE, SoundCategory.BLOCKS, 0.8f, 1.2f);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(STAGE) != 3) return ActionResult.PASS;
		if (!world.isClient) {
			Item cocoon = Fauna.item("silk_cocoon");
			if (cocoon != null) Block.dropStack(world, pos.up(), new ItemStack(cocoon, 2 + world.random.nextInt(2)));
			world.setBlockState(pos, state.with(STAGE, 1));
			world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 0.6f, 1.2f);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(STAGE) == 2 && random.nextInt(3) == 0) world.setBlockState(pos, state.with(STAGE, 3));
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && state.get(STAGE) > 0 && Fauna.item("silkworm") != null) {
			Block.dropStack(world, pos, new ItemStack(Fauna.item("silkworm")));
		}
		return super.onBreak(world, pos, state, player);
	}
}
