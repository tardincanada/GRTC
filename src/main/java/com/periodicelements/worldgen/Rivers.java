package com.periodicelements.worldgen;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Rivers of the Geological world (stage 2a), from a flow field on a grid of STEP blocks. Every grid point drains to
 * the neighbour down the steepest slope of the potential (the macro relief tilted towards the oceans). A grid
 * point's catchment is itself plus every grid point that drains into it; where the catchment is at least
 * RIVER_AREA points there is a river, wider and deeper the bigger it is. A river ends in the sea, or in a pond
 * where the potential has a hollow. Desert and badlands grid points add nothing to a catchment (3.45.0), so no river
 * rises in them, though one from wetter land runs on through. Everything is a pure function of the grid point (memoised), so the rivers do
 * not depend on the order the world is explored in.
 */
final class Rivers {
	static final int STEP = 16;
	/** 3.49.0: from the world's options ({@link GeoOptions#riverArea()}); 160 by default. */
	final int RIVER_AREA;
	static final double TILT = 160;
	static final double VALLEY = 34;
	static final double MEANDER = 18;

	/** What a column needs to know about the nearest river. */
	static final class Hit {
		double dist = Double.MAX_VALUE, level, halfWidth, depth;
		boolean pond;
	}

	private final Geology g;
	private final ConcurrentHashMap<Long, Float> macro = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Long> down = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Integer> area = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Float> level = new ConcurrentHashMap<>();

	Rivers(Geology g) {
		this.g = g;
		RIVER_AREA = g.options.riverArea();
	}

	static long key(int x, int z) {
		return ((long) x << 32) ^ (z & 0xffffffffL);
	}

	static int kx(long k) {
		return (int) (k >> 32);
	}

	static int kz(long k) {
		return (int) k;
	}

	double macroAt(int x, int z) {
		return macro.computeIfAbsent(key(x, z), k -> (float) g.computeBase(x, z).macro);
	}

	/**
	 * What rivers run down: the macro relief tilted towards the oceans (the slow continent noise that places
	 * them), so flat basins drain to the sea instead of into countless shallow hollows.
	 */
	double potential(int x, int z) {
		return macroAt(x, z) + TILT * g.continentNoise(x, z);
	}

	/** The grid point this one drains to; itself in the sea or in a hollow. */
	long down(int x, int z) {
		long k = key(x, z);
		Long d = down.get(k);
		if (d != null) return d;
		long result = k;
		if (macroAt(x, z) >= Geology.SEA) {
			double p = potential(x, z), best = 0;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0) continue;
					int nx = x + dx * STEP, nz = z + dz * STEP;
					double slope = (p - potential(nx, nz)) / (STEP * Math.sqrt(dx * dx + dz * dz));
					if (slope > best) {
						best = slope;
						result = key(nx, nz);
					}
				}
			}
		}
		down.put(k, result);
		return result;
	}

	/** Grid points in the catchment of this one (itself included); 0 in the sea. */
	int area(int x, int z) {
		long k0 = key(x, z);
		Integer known = area.get(k0);
		if (known != null) return known;
		// iterative post-order over the upstream tree (it can hold tens of thousands of points)
		LongArrayList stack = new LongArrayList();
		stack.add(k0);
		while (!stack.isEmpty()) {
			long k = stack.getLong(stack.size() - 1);
			if (area.containsKey(k)) {
				stack.removeLong(stack.size() - 1);
				continue;
			}
			int cx = kx(k), cz = kz(k);
			if (macroAt(cx, cz) < Geology.SEA) {
				level.put(k, (float) (macroAt(cx, cz) - 1.5));
				area.put(k, 0);
				stack.removeLong(stack.size() - 1);
				continue;
			}
			// 3.45.0: rain falls outside the deserts only, so a river rises in wetter land; it can still cross a desert
			int sum = Deposits.arid(g, cx, cz) ? 0 : 1;
			float low = (float) (macroAt(cx, cz) - 1.5);
			boolean ready = true;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0) continue;
					int nx = cx + dx * STEP, nz = cz + dz * STEP;
					if (down(nx, nz) != k) continue;
					Integer a = area.get(key(nx, nz));
					if (a == null) {
						stack.add(key(nx, nz));
						ready = false;
					} else {
						sum += a;
						low = Math.min(low, level.get(key(nx, nz)));
					}
				}
			}
			if (ready) {
				level.put(k, low);
				area.put(k, sum);
				stack.removeLong(stack.size() - 1);
			}
		}
		return area.get(k0);
	}

	/**
	 * Water level at a grid point: never above any level upstream of it, so water never runs uphill (where a
	 * river crosses higher ground it cuts a gorge to the level it already has).
	 */
	float level(int x, int z) {
		area(x, z);
		return level.get(key(x, z));
	}

	static double halfWidth(int area) {
		return Math.min(14, 1.4 + 1.25 * Math.sqrt(area / 160.0));
	}

	static double depth(int area) {
		return Math.min(7, 1.5 + 0.6 * Math.sqrt(area / 160.0));
	}

	/** The nearest river segment to a column, or null when none is within its valley. */
	Hit near(int x, int z) {
		double qx = x + MEANDER * Geology.fbm(g.riverWarpX(), x, z, 140, 2);
		double qz = z + MEANDER * Geology.fbm(g.riverWarpZ(), x, z, 140, 2);
		int gx = Math.floorDiv((int) Math.round(qx), STEP) * STEP, gz = Math.floorDiv((int) Math.round(qz), STEP) * STEP;
		int r = (int) Math.ceil((14 + VALLEY + STEP) / STEP);
		Hit best = null;
		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				int ax = gx + dx * STEP, az = gz + dz * STEP;
				if (macroAt(ax, az) < Geology.SEA) continue;
				int a = area(ax, az);
				if (a < RIVER_AREA) continue;
				long d = down(ax, az);
				boolean pond = d == key(ax, az);
				int bx = kx(d), bz = kz(d);
				double vx = bx - ax, vz = bz - az;
				double len2 = vx * vx + vz * vz;
				double t = len2 == 0 ? 0 : MathHelper.clamp(((qx - ax) * vx + (qz - az) * vz) / len2, 0, 1);
				double px = ax + t * vx - qx, pz = az + t * vz - qz;
				double dist = Math.sqrt(px * px + pz * pz);
				double hw = halfWidth(a) + (pond ? 4 + 3 * Math.sqrt(a / 160.0) : 0);
				if (dist > hw + VALLEY) continue;
				// compare by how far inside the valley edge, so a wide river wins over a nearby brook
				if (best == null || dist - hw < best.dist - best.halfWidth) {
					if (best == null) best = new Hit();
					best.dist = dist;
					best.halfWidth = hw;
					best.depth = depth(a);
					float la = level(ax, az);
					float lb = macroAt(bx, bz) < Geology.SEA ? Math.min(la, Geology.SEA - 1) : level(bx, bz);
					best.level = la + t * (lb - la);
					best.pond = pond;
				}
			}
		}
		return best;
	}

	boolean runsDownhill() {
		for (var e : level.entrySet()) {
			long k = e.getKey();
			long d = down.getOrDefault(k, k);
			if (d == k) continue;
			Float next = level.get(d);
			if (next != null && area.containsKey(d) && next > e.getValue() + 1e-4) return false;
		}
		return true;
	}
}
