package com.periodicelements.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 3.38.0: Fluid Pipes join machines to tanks. A machine sends the liquids and gases it makes down its pipes into any
 * tank they reach (before it fills flasks) and draws the liquids and gases it can use from them. A Fluid Pump moves
 * fluid from the tanks behind it to the tanks in front. Pipes hold nothing themselves: the fluid goes straight from
 * holder to holder, and a pipe run may be up to {@link #MAX_PIPES} pipes.
 */
public final class FluidNet {
	public static final int MAX_PIPES = 1024;

	private FluidNet() {
	}

	/** Does a pipe join up with this block? */
	public static boolean joins(BlockState s) {
		Block b = s.getBlock();
		return b instanceof FluidPipeBlock || b instanceof FluidPumpBlock || b instanceof TankBlocks.Valve
				|| b instanceof TankBlocks.Controller || b instanceof com.periodicelements.machine.StorageTankBlock
				|| b instanceof com.periodicelements.machine.MachineBlock;
	}

	/** The holder at this block: a Storage Tank, a Tank Controller, or the controller behind a Tank Valve. */
	@Nullable
	public static FluidHolder holderAt(WorldAccess w, BlockPos p) {
		if (w.getBlockEntity(p) instanceof FluidHolder h) {
			return h;
		}
		if (w.getBlockState(p).getBlock() instanceof TankBlocks.Valve) {
			return TankBlocks.controller(w, p);
		}
		return null;
	}

	/** Every holder touching {@code origin} on these sides, directly or through the pipes that start there. */
	public static List<FluidHolder> reach(World w, BlockPos origin, Direction... sides) {
		Map<FluidHolder, Boolean> found = new IdentityHashMap<>();
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		seen.add(origin);
		for (Direction d : sides) {
			BlockPos n = origin.offset(d);
			if (!w.isChunkLoaded(n) || !seen.add(n)) {
				continue;
			}
			FluidHolder h = holderAt(w, n);
			if (h != null) {
				found.put(h, true);
			} else if (w.getBlockState(n).getBlock() instanceof FluidPipeBlock) {
				queue.add(n);
			}
		}
		int pipes = 0;
		while (!queue.isEmpty() && pipes++ < MAX_PIPES) {
			BlockPos p = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos n = p.offset(d);
				if (!w.isChunkLoaded(n) || !seen.add(n)) {
					continue;
				}
				if (w.getBlockState(n).getBlock() instanceof FluidPipeBlock) {
					queue.add(n);
					continue;
				}
				FluidHolder h = holderAt(w, n);
				if (h != null) {
					found.put(h, true);
				}
			}
		}
		return new ArrayList<>(found.keySet());
	}

	/** Sends up to {@code units} of a fluid into the holders; returns what went in. */
	public static double send(List<FluidHolder> to, net.minecraft.item.Item fluid, double units, double ratio) {
		double sent = 0;
		// Holders that already hold this fluid first, then empty ones.
		for (int pass = 0; pass < 2 && sent < units - 1e-9; pass++) {
			for (FluidHolder h : to) {
				if (h.gone() || (pass == 0) != (h.fluid() == fluid)) {
					continue;
				}
				sent += h.fill(fluid, units - sent, ratio, false);
				if (sent >= units - 1e-9) {
					break;
				}
			}
		}
		return sent;
	}
}
