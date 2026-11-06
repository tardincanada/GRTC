package com.periodicelements.explore;

import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashSet;
import java.util.Set;

/** Once a second a street lamp takes a second's power from its wire network; with all of it, it is lit. */
public class StreetLampBlockEntity extends BlockEntity {
	public static final int PERIOD = 20;
	/** How far down a post the lamp looks for its foot. */
	public static final int MAX_POST = 16;

	public StreetLampBlockEntity(BlockPos pos, BlockState state) {
		super(Explore.LAMP_ENTITY, pos, state);
	}

	/** Is this a block a lamp stands on: a fence, wall, bars or pane, or a lamp post block? */
	static boolean isPost(BlockState s) {
		return s.getBlock() instanceof FenceBlock || s.getBlock() instanceof WallBlock || s.getBlock() instanceof PaneBlock;
	}

	/** The lamp and the foot of its post: where copper wire may touch it. */
	static Set<BlockPos> feedPoints(ServerWorld world, BlockPos pos) {
		Set<BlockPos> out = new LinkedHashSet<>();
		out.add(pos);
		BlockPos at = pos.down();
		int n = 0;
		while (n < MAX_POST && isPost(world.getBlockState(at))) {
			out.add(at);
			at = at.down();
			n++;
		}
		return out;
	}

	public void tick(ServerWorld world, BlockPos pos, BlockState state) {
		if ((world.getTime() + pos.asLong()) % PERIOD != 0) {
			return;
		}
		StreetLampBlock.Kind kind = ((StreetLampBlock) state.getBlock()).kind();
		double want = kind.kJPerTick() * PERIOD;
		double got = 0;
		Set<PowerBlockEntity> seen = new java.util.HashSet<>();
		outer:
		for (BlockPos feed : feedPoints(world, pos)) {
			for (PowerBlockEntity src : MachineBlockEntity.powerSources(world, feed)) {
				if (!seen.add(src)) {
					continue;
				}
				got += src.draw(want - got);
				if (got >= want - 1e-9) {
					break outer;
				}
			}
		}
		boolean lit = got >= want - 1e-9;
		if (state.get(StreetLampBlock.LIT) != lit) {
			world.setBlockState(pos, state.with(StreetLampBlock.LIT, lit));
		}
		if (lit) {
			Explore.LitLamps.set(world, pos, kind.radius());
		} else {
			Explore.LitLamps.remove(world, pos);
		}
	}

	@Override
	public void markRemoved() {
		if (world != null && !world.isClient) {
			Explore.LitLamps.remove(world, pos);
		}
		super.markRemoved();
	}
}
