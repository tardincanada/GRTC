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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 3.38.0: the multiblock tank. A closed box of tank blocks (Tank Plate, Tank Valve, Tank Gauge, Tank Compressor and one
 * Tank Controller) round an empty inside of any size from 1x1x1 to {@link #MAX_SIDE} on a side. Every block of the inside
 * holds {@link #UNITS_PER_BLOCK} units of a liquid or gas, or {@link #GAS_UNITS_PER_BLOCK} of gas with a Tank
 * Compressor in the shell. The shell is the whole box round the inside, edges and corners too.
 */
public final class TankStructure {
	public static final int MAX_SIDE = 14;
	public static final double UNITS_PER_BLOCK = 16_000;
	public static final double GAS_UNITS_PER_BLOCK = 50_000;

	public enum Problem { NONE, OPEN, NOT_BOX, TOO_BIG, MISSING, TWO_CONTROLLERS, TWO_GAUGES }

	/** What the controller found: the inside's size, its parts, or the first problem. */
	public record Result(Problem problem, int dx, int dy, int dz, int compressors, List<BlockPos> valves, List<BlockPos> gauges,
						 @Nullable BlockPos missing, List<BlockPos> shell) {
		Result(Problem problem, int dx, int dy, int dz, int compressors, List<BlockPos> valves, List<BlockPos> gauges, @Nullable BlockPos missing) {
			this(problem, dx, dy, dz, compressors, valves, gauges, missing, List.of());
		}

		public boolean valid() {
			return problem == Problem.NONE;
		}

		public int volume() {
			return valid() ? dx * dy * dz : 0;
		}

		public boolean compressed() {
			return compressors > 0;
		}

		/** Units of this fluid the tank holds. */
		public double capacity(boolean gas) {
			return volume() * (gas && compressed() ? GAS_UNITS_PER_BLOCK : UNITS_PER_BLOCK);
		}
	}

	private static final Result OPEN = new Result(Problem.OPEN, 0, 0, 0, 0, List.of(), List.of(), null);

	private TankStructure() {
	}

	/** Is this block a part of a tank shell? */
	public static boolean isPart(BlockState s) {
		Block b = s.getBlock();
		return b == FluidContent.TANK_WALL || b == FluidContent.TANK_VALVE || b == FluidContent.TANK_GAUGE
				|| b == FluidContent.TANK_COMPRESSOR || b == FluidContent.TANK_CONTROLLER;
	}

	/** Finds the tank the controller at {@code c} stands in the shell of. */
	public static Result detect(World w, BlockPos c) {
		Result best = OPEN;
		for (Direction d : Direction.values()) {
			BlockPos start = c.offset(d);
			if (!w.isChunkLoaded(start) || !w.getBlockState(start).isAir()) {
				continue;
			}
			Result r = measure(w, c, start);
			if (r.valid()) {
				return r;
			}
			if (rank(r.problem()) > rank(best.problem())) {
				best = r;
			}
		}
		return best;
	}

	/** Which problem to report when no side gives a tank: the one nearest to a finished tank. */
	private static int rank(Problem p) {
		return switch (p) {
			case MISSING, TWO_CONTROLLERS, TWO_GAUGES -> 3;
			case NOT_BOX -> 2;
			case TOO_BIG -> 1;
			default -> 0;
		};
	}

	private static Result measure(World w, BlockPos c, BlockPos start) {
		int limit = MAX_SIDE * MAX_SIDE * MAX_SIDE;
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		seen.add(start);
		queue.add(start);
		int x0 = start.getX(), x1 = x0, y0 = start.getY(), y1 = y0, z0 = start.getZ(), z1 = z0;
		while (!queue.isEmpty()) {
			BlockPos p = queue.poll();
			x0 = Math.min(x0, p.getX());
			x1 = Math.max(x1, p.getX());
			y0 = Math.min(y0, p.getY());
			y1 = Math.max(y1, p.getY());
			z0 = Math.min(z0, p.getZ());
			z1 = Math.max(z1, p.getZ());
			if (seen.size() > limit || x1 - x0 >= MAX_SIDE || y1 - y0 >= MAX_SIDE || z1 - z0 >= MAX_SIDE) {
				return new Result(Problem.TOO_BIG, 0, 0, 0, 0, List.of(), List.of(), null);
			}
			for (Direction d : Direction.values()) {
				BlockPos n = p.offset(d);
				if (seen.contains(n)) {
					continue;
				}
				if (!w.isChunkLoaded(n)) {
					return OPEN;
				}
				if (w.getBlockState(n).isAir()) {
					seen.add(n.toImmutable());
					queue.add(n.toImmutable());
				}
			}
		}
		int dx = x1 - x0 + 1, dy = y1 - y0 + 1, dz = z1 - z0 + 1;
		if (seen.size() != dx * dy * dz) {
			return new Result(Problem.NOT_BOX, dx, dy, dz, 0, List.of(), List.of(), null);
		}
		int compressors = 0, controllers = 0;
		List<BlockPos> valves = new ArrayList<>(), gauges = new ArrayList<>(), shell = new ArrayList<>();
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int x = x0 - 1; x <= x1 + 1; x++) {
			for (int y = y0 - 1; y <= y1 + 1; y++) {
				for (int z = z0 - 1; z <= z1 + 1; z++) {
					boolean inside = x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1;
					if (inside) {
						continue;
					}
					p.set(x, y, z);
					BlockState s = w.getBlockState(p);
					if (!isPart(s)) {
						return new Result(Problem.MISSING, dx, dy, dz, 0, List.of(), List.of(), p.toImmutable());
					}
					Block b = s.getBlock();
					shell.add(p.toImmutable());
					if (b == FluidContent.TANK_COMPRESSOR) {
						compressors++;
					} else if (b == FluidContent.TANK_VALVE) {
						valves.add(p.toImmutable());
					} else if (b == FluidContent.TANK_GAUGE) {
						gauges.add(p.toImmutable());
					} else if (b == FluidContent.TANK_CONTROLLER) {
						controllers++;
					}
				}
			}
		}
		if (controllers > 1) {
			return new Result(Problem.TWO_CONTROLLERS, dx, dy, dz, 0, List.of(), List.of(), null);
		}
		if (gauges.size() > 1) {
			return new Result(Problem.TWO_GAUGES, dx, dy, dz, 0, List.of(), List.of(), null);
		}
		return new Result(Problem.NONE, dx, dy, dz, compressors, valves, gauges, null, shell);
	}

	// ---- Which controller a valve or gauge belongs to (rebuilt by each controller as it measures).

	private static final Map<WorldAccess, Map<BlockPos, BlockPos>> PARTS = new WeakHashMap<>();

	@Nullable
	public static synchronized BlockPos controllerOf(WorldAccess world, BlockPos part) {
		Map<BlockPos, BlockPos> m = PARTS.get(world);
		return m == null ? null : m.get(part);
	}

	/** Replaces the parts this controller owns. */
	static synchronized void bind(WorldAccess world, BlockPos controller, List<BlockPos> parts) {
		Map<BlockPos, BlockPos> m = PARTS.computeIfAbsent(world, w -> new HashMap<>());
		m.values().removeIf(controller::equals);
		for (BlockPos part : parts) {
			m.put(part, controller);
		}
	}
}
