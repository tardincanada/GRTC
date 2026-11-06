package com.periodicelements.crops;

import com.periodicelements.radiation.Radiation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Gamma Garden (3.37.0, level 6): a cobalt-60 source on a lead post. Crops within {@link Genetics#GAMMA_RADIUS}
 * blocks pass on twenty times as many mutations; people near it take a dose like near a leaking reactor.
 */
public class GammaGardenBlock extends Block {
	/** Sv/h at one block, falling off with the square of the distance. */
	public static final double DOSE = 0.5;
	public static final int PERIOD = 100;

	public GammaGardenBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(this)) {
			world.scheduleBlockTick(pos, this, 1);
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		Radiation.leak(world, pos, DOSE, PERIOD * 2);
		world.spawnParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0);
		world.scheduleBlockTick(pos, this, PERIOD);
	}
}
