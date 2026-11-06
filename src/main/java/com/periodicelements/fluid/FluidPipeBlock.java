package com.periodicelements.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import java.util.EnumMap;
import java.util.Map;

/** 3.38.0: a length of Fluid Pipe; it bends to join the pipes, pumps, tanks, valves and machines round it. */
public class FluidPipeBlock extends Block {
	public static final Map<Direction, BooleanProperty> SIDES = new EnumMap<>(Map.of(
			Direction.NORTH, Properties.NORTH, Direction.SOUTH, Properties.SOUTH, Direction.EAST, Properties.EAST,
			Direction.WEST, Properties.WEST, Direction.UP, Properties.UP, Direction.DOWN, Properties.DOWN));
	private static final VoxelShape CORE = Block.createCuboidShape(5, 5, 5, 11, 11, 11);
	private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Map.of(
			Direction.NORTH, Block.createCuboidShape(5, 5, 0, 11, 11, 5),
			Direction.SOUTH, Block.createCuboidShape(5, 5, 11, 11, 11, 16),
			Direction.WEST, Block.createCuboidShape(0, 5, 5, 5, 11, 11),
			Direction.EAST, Block.createCuboidShape(11, 5, 5, 16, 11, 11),
			Direction.DOWN, Block.createCuboidShape(5, 0, 5, 11, 5, 11),
			Direction.UP, Block.createCuboidShape(5, 11, 5, 11, 16, 11)));

	public FluidPipeBlock(Settings settings) {
		super(settings);
		BlockState s = getStateManager().getDefaultState();
		for (BooleanProperty p : SIDES.values()) {
			s = s.with(p, false);
		}
		setDefaultState(s);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		SIDES.values().forEach(builder::add);
	}

	public BlockState joined(BlockView world, BlockPos pos) {
		BlockState s = getDefaultState();
		for (var e : SIDES.entrySet()) {
			s = s.with(e.getValue(), FluidNet.joins(world.getBlockState(pos.offset(e.getKey()))));
		}
		return s;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return joined(ctx.getWorld(), ctx.getBlockPos());
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world,
												   BlockPos pos, BlockPos neighborPos) {
		return state.with(SIDES.get(direction), FluidNet.joins(neighborState));
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		VoxelShape shape = CORE;
		for (var e : SIDES.entrySet()) {
			if (state.get(e.getValue())) {
				shape = VoxelShapes.union(shape, ARMS.get(e.getKey()));
			}
		}
		return shape;
	}
}
