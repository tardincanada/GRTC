package com.periodicelements.machine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/** A junction of copper cable: carries current from piles and dynamos to electric machines it touches. */
public class CopperWireBlock extends Block {
	private static final VoxelShape SHAPE = VoxelShapes.union(
			Block.createCuboidShape(5, 5, 0, 11, 11, 16),
			Block.createCuboidShape(0, 5, 5, 16, 11, 11),
			Block.createCuboidShape(5, 0, 5, 11, 16, 11));

	public CopperWireBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
}
