package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/** A tallow candle: bright (light 12) and smoky, and it burns away three game days after it is placed. */
public class TallowCandleBlock extends Block {
	public static final MapCodec<TallowCandleBlock> CODEC = createCodec(TallowCandleBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(6, 0, 6, 10, 9, 10);

	public TallowCandleBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(this)) {
			world.scheduleBlockTick(pos, this, Farm.candleTicks());
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.removeBlock(pos, false); // burnt down: nothing is left
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world,
												   BlockPos pos, BlockPos neighborPos) {
		return direction == Direction.DOWN && !canPlaceAt(state, world, pos) ? net.minecraft.block.Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		double x = pos.getX() + 0.5, y = pos.getY() + 0.68, z = pos.getZ() + 0.5;
		world.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0, 0, 0);
		if (random.nextInt(3) == 0) {
			world.addParticle(ParticleTypes.SMOKE, x, y + 0.1, z, 0, 0.02, 0); // tallow smokes
		}
	}
}
