package com.periodicelements.fauna;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** 3.50.0 (fauna stage 2): seabird droppings in layers, like snow that never melts. Boobies on the shore add layers. */
public class GuanoBlock extends SnowBlock {
	public GuanoBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected boolean hasRandomTicks(BlockState state) {
		return false;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
	}

	/** Adds one layer at pos (a new block on bare ground, or one more on a guano block). True if it did. */
	public static boolean addLayer(ServerWorld world, BlockPos pos) {
		var block = Fauna.block("guano");
		if (!(block instanceof GuanoBlock g)) return false;
		BlockState here = world.getBlockState(pos);
		if (here.isOf(g)) {
			int n = here.get(LAYERS);
			if (n >= 4) return false;  // the colony keeps it from piling higher than half a block
			world.setBlockState(pos, here.with(LAYERS, n + 1));
			return true;
		}
		if (here.isAir() && g.getDefaultState().canPlaceAt(world, pos)) {
			world.setBlockState(pos, g.getDefaultState());
			return true;
		}
		return false;
	}
}
