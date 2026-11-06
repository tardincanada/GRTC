package com.periodicelements;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * An ionisation smoke detector: americium-241 keeps a little current flowing through the air in its chamber, and
 * smoke stops it. In the game it checks once a second for fire or a lit campfire within {@link #RANGE} blocks, then
 * gives a full redstone signal and beeps.
 */
public class SmokeDetectorBlock extends Block {
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final int RANGE = 4;
	private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 2, 12);

	public SmokeDetectorBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(this)) {
			world.scheduleBlockTick(pos, this, 20);
		}
	}

	/** Is fire or a lit campfire burning within range? */
	public static boolean smoke(World world, BlockPos pos) {
		for (BlockPos p : BlockPos.iterate(pos.add(-RANGE, -RANGE, -RANGE), pos.add(RANGE, RANGE, RANGE))) {
			BlockState s = world.getBlockState(p);
			if (s.isIn(BlockTags.FIRE) || (s.getBlock() instanceof CampfireBlock && s.get(CampfireBlock.LIT))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean alarm = smoke(world, pos);
		if (alarm != state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, alarm), Block.NOTIFY_ALL);
			world.updateNeighborsAlways(pos, this);
			world.updateNeighborsAlways(pos.down(), this);
		}
		if (alarm) {
			world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1.0f, 2.0f);
		}
		world.scheduleBlockTick(pos, this, 20);
	}

	@Override
	protected boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(POWERED) ? 15 : 0;
	}

	@Override
	protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(POWERED) && direction == Direction.UP ? 15 : 0;
	}
}
