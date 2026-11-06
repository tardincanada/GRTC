package com.periodicelements.worldgen;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The geological map of the "Geological" world type: provinces (ocean, granite shield, sedimentary basin,
 * volcanic province) on large warped Voronoi cells, mountain belts along the borders between them, and the
 * relief each rock makes. Everything is a pure function of the seed and the block position, so the density
 * functions (terrain, biome climate) and the surface painter always agree.
 */
public final class Geology {
	public enum Province { OCEAN, CRATON, BASIN, VOLCANIC }

	/** Everything known about one column. */
	public static final class Column {
		public double height;       // surface height in blocks
		public Province province;   // nearest cell's province
		public double mountain;     // 0..1 mountain-belt strength
		public double ocean;        // 0..1 weight of ocean cells
		public double plateau;      // 0..1 how much of the height is mesa / trap plateau
		public double macro;        // the broad, gentle part of the height
		public boolean dome;        // inside a granite dome (inselberg)
		public boolean neck;        // inside a basalt volcanic neck
		public int riverWater = Integer.MIN_VALUE; // top water block of a river or pond in this column, if any
		public boolean river;       // inside a river channel (biome: river)
		// stage 4b (3.42.0): volcanic cones
		public boolean cone;        // on a volcanic cone (its rock is andesite and tuff in layers)
		public double coneFrac;     // 0 at the foot of the cone, 1 at the crater rim
		public double coneBase;     // the level the cone stands on
		public int craterLake = Integer.MIN_VALUE; // top block of the lake in a crater, if any
		public boolean lava;        // the crater lake is lava (an active volcano)
		// basin sediments, filled in on first use (karst, rocks)
		boolean sedReady;
		double sedFloor, layerShift;
		Doline doline;
	}

	/** A doline (sinkhole) near a column: a round hollow; a collapse doline also has a shaft down to a cave. */
	static final class Doline {
		double cx, cz, r, depth, dist;
		boolean collapse;
	}

	/** Province cell size and blends for the default size; 3.49.0 scales them with {@link GeoOptions#provinces()}. */
	static final int DEFAULT_CELL = 1100;
	static final double BLEND = 70;
	private final int CELL;
	private final double BELT, BLEND_MACRO, REACH, WARP;
	public static final int SEA = 63;
	/** Blocks by which snow starts higher than in vanilla (Guilherme, 2026-11-03). */
	public static final int SNOW_LINE_RAISE = 20;

	private record Key(long seed, GeoOptions options) {
	}

	private static final ConcurrentHashMap<Key, Geology> CACHE = new ConcurrentHashMap<>();

	/** The geology of a seed with the options of the world being played ({@link GeoOptions#current()}). */
	public static Geology of(long seed) {
		return of(seed, GeoOptions.current());
	}

	public static Geology of(long seed, GeoOptions options) {
		return CACHE.computeIfAbsent(new Key(seed, options), k -> new Geology(k.seed, k.options));
	}

	/** A fresh, uncached instance (tests: the map must not depend on the order it is explored in). */
	public static Geology fresh(long seed) {
		return new Geology(seed, GeoOptions.current());
	}

	public static Geology fresh(long seed, GeoOptions options) {
		return new Geology(seed, options);
	}

	public final GeoOptions options;

	/** Rivers: true if every traced river node's water level is at or above the next one downstream. */
	public boolean riversRunDownhill() {
		return rivers.runsDownhill();
	}

	public final long seed;
	private final SimplexNoiseSampler warpX, warpZ, relief, detail, mesa, mesaH, ridge, trap, outcrop, rock3d, fold, soil, continent, riverX, riverZ, karstA, karstB, tubeA, tubeB, tubeMask, climateVar;
	private final ConcurrentHashMap<Long, Province> provinces = new ConcurrentHashMap<>();
	final Rivers rivers;
	private final ThreadLocal<long[]> keys = ThreadLocal.withInitial(() -> {
		long[] k = new long[4096];
		java.util.Arrays.fill(k, Long.MIN_VALUE);
		return k;
	});
	private final ThreadLocal<Column[]> cols = ThreadLocal.withInitial(() -> new Column[4096]);

	private Geology(long seed, GeoOptions options) {
		this.seed = seed;
		this.options = options;
		CELL = options.provinces().cell;
		double s = CELL / (double) DEFAULT_CELL;
		// every cell outside the 5x5 window must stay past REACH, and a belt within it (see computeBase)
		REACH = 500 * s;
		BELT = 270 * Math.min(1, s);
		BLEND_MACRO = 260 * Math.min(1, s);
		WARP = 220 * Math.min(1, s);
		Random r = Random.create(seed);
		warpX = new SimplexNoiseSampler(r);
		warpZ = new SimplexNoiseSampler(r);
		relief = new SimplexNoiseSampler(r);
		detail = new SimplexNoiseSampler(r);
		mesa = new SimplexNoiseSampler(r);
		mesaH = new SimplexNoiseSampler(r);
		ridge = new SimplexNoiseSampler(r);
		trap = new SimplexNoiseSampler(r);
		outcrop = new SimplexNoiseSampler(r);
		rock3d = new SimplexNoiseSampler(r);
		fold = new SimplexNoiseSampler(r);
		soil = new SimplexNoiseSampler(r);
		continent = new SimplexNoiseSampler(r);
		riverX = new SimplexNoiseSampler(r);
		riverZ = new SimplexNoiseSampler(r);
		karstA = new SimplexNoiseSampler(r);
		karstB = new SimplexNoiseSampler(r);
		tubeA = new SimplexNoiseSampler(r);
		tubeB = new SimplexNoiseSampler(r);
		tubeMask = new SimplexNoiseSampler(r);
		climateVar = new SimplexNoiseSampler(Random.create(seed ^ 0x3490L));
		rivers = new Rivers(this);
	}

	/** The slow noise that places the oceans (low = ocean). */
	double continentNoise(double x, double z) {
		return fbm(continent, x, z, 4200, 3);
	}

	SimplexNoiseSampler riverWarpX() {
		return riverX;
	}

	SimplexNoiseSampler riverWarpZ() {
		return riverZ;
	}

	// ---- noise helpers -------------------------------------------------------------------------------

	static double fbm(SimplexNoiseSampler n, double x, double z, double scale, int octaves) {
		double sum = 0, amp = 1, norm = 0, f = 1 / scale;
		for (int i = 0; i < octaves; i++) {
			sum += amp * n.sample(x * f + i * 31.7, z * f - i * 17.3);
			norm += amp;
			amp *= 0.5;
			f *= 2;
		}
		return sum / norm;
	}

	long hash(long a, long b, long salt) {
		long h = seed ^ salt * 0x9E3779B97F4A7C15L;
		h ^= a * 0xC2B2AE3D27D4EB4FL;
		h = Long.rotateLeft(h, 31) * 0x165667B19E3779F9L;
		h ^= b * 0x27D4EB2F165667C5L;
		h = Long.rotateLeft(h, 27) * 0x9E3779B97F4A7C15L;
		h ^= h >>> 33;
		return h;
	}

	double unit(long a, long b, long salt) {
		return (hash(a, b, salt) >>> 11) * 0x1.0p-53;
	}

	/** Polynomial smooth minimum, blending over about k blocks. */
	static double smoothMin(double a, double b, double k) {
		double h = MathHelper.clamp(0.5 + 0.5 * (b - a) / k, 0, 1);
		return b + (a - b) * h - k * h * (1 - h);
	}

	static double smooth(double e0, double e1, double x) {
		double t = MathHelper.clamp((x - e0) / (e1 - e0), 0, 1);
		return t * t * (3 - 2 * t);
	}

	/** Terraces with flat treads and steep risers of the given step. */
	static double terrace(double h, double step) {
		double k = Math.floor(h / step);
		double f = h / step - k;
		return step * (k + smooth(0.78, 1.0, f));
	}

	// ---- provinces -----------------------------------------------------------------------------------

	/** Oceans and continents come from a slow noise at the cell centres, so oceans join up into seas. */
	Province provinceOf(long i, long j) {
		return provinces.computeIfAbsent((i << 32) ^ (j & 0xffffffffL), k -> computeProvince(i, j));
	}

	private Province computeProvince(long i, long j) {
		double cx = cellX(i, j), cz = cellZ(i, j);
		if (fbm(continent, cx, cz, 4200, 3) + 0.25 * (unit(i, j, 1) - 0.5) < options.oceanLevel()) return Province.OCEAN;
		double u = unit(i, j, 5);
		if (u < 0.36) return Province.CRATON;
		if (u < 0.70) return Province.BASIN;
		return Province.VOLCANIC;
	}

	double cellX(long i, long j) {
		return (i + 0.15 + 0.7 * unit(i, j, 2)) * CELL;
	}

	double cellZ(long i, long j) {
		return (j + 0.15 + 0.7 * unit(i, j, 3)) * CELL;
	}

	/** How strongly the border between two cells is pushed up into a mountain belt (0 = no mountains). */
	double beltStrength(long i1, long j1, Province p1, long i2, long j2, Province p2) {
		boolean o1 = p1 == Province.OCEAN, o2 = p2 == Province.OCEAN;
		if (o1 && o2) return 0;
		long a = Math.min(i1 * 7919 + j1, i2 * 7919 + j2), b = Math.max(i1 * 7919 + j1, i2 * 7919 + j2);
		double u = unit(a, b, 4);
		double chance = (o1 || o2) ? 0.25 : 0.65;
		return u < chance ? 0.55 + 0.45 * (u / chance) : 0;
	}

	// ---- columns -------------------------------------------------------------------------------------

	public Column column(int x, int z) {
		long key = ((long) x << 32) ^ (z & 0xffffffffL);
		int slot = (int) ((x * 73856093L ^ z * 19349663L) & 4095);
		long[] k = keys.get();
		Column[] c = cols.get();
		if (k[slot] == key && c[slot] != null) return c[slot];
		Column col = compute(x, z);
		k[slot] = key;
		c[slot] = col;
		return col;
	}

	/**
	 * Height = MACRO + MICRO. The macro relief is broad and gentle (continents, shelves, uplands, the broad swell of a
	 * mountain belt, blended over hundreds of blocks); the micro relief (hills, domes, mesas, traps, peaks) sits on
	 * top of it, so a hill or a cliff can stand at any altitude.
	 */
	private Column compute(int x, int z) {
		Column col = computeBase(x, z, true);
		applyRivers(col, x, z);
		applyCones(col, x, z);
		return col;
	}

	/** Tests: {@link #computeBase(int, int)}. */
	public Column computeBaseForTest(int x, int z) {
		return computeBase(x, z, false);
	}

	/** The column before rivers; without {@code full}, only the macro relief and what it needs are right. */
	Column computeBase(int x, int z) {
		return computeBase(x, z, false);
	}

	private Column computeBase(int x, int z, boolean full) {
		double wx = x + WARP * fbm(warpX, x, z, 700, 3);
		double wz = z + WARP * fbm(warpZ, x, z, 700, 3);
		long ci = (long) Math.floor(wx / CELL), cj = (long) Math.floor(wz / CELL);

		long[] is = new long[25], js = new long[25];
		double[] px = new double[25], pz = new double[25], d2 = new double[25];
		int best = 0, n = 0;
		for (long di = -2; di <= 2; di++) {
			for (long dj = -2; dj <= 2; dj++) {
				long i = ci + di, j = cj + dj;
				is[n] = i;
				js[n] = j;
				px[n] = cellX(i, j);
				pz[n] = cellZ(i, j);
				double dx = wx - px[n], dz = wz - pz[n];
				d2[n] = dx * dx + dz * dz;
				if (d2[n] < d2[best]) best = n;
				n++;
			}
		}
		Province p1 = provinceOf(is[best], js[best]);
		double[] wMicro = new double[4], wMacro = new double[4];
		double totalMicro = 0, totalMacro = 0, mountain = 0;
		for (int m = 0; m < 25; m++) {
			Province pm = provinceOf(is[m], js[m]);
			double edge = 0;
			if (m != best) {
				// half the difference of distances: about the distance to the border, and continuous everywhere
				edge = 0.5 * (Math.sqrt(d2[m]) - Math.sqrt(d2[best]));
			}
			// weights reach exactly 0 at REACH, which every cell outside the 5x5 window exceeds, so nothing jumps
			double a = Math.max(0, Math.exp(-edge / BLEND) - Math.exp(-REACH / BLEND));
			double b = Math.max(0, Math.exp(-edge / BLEND_MACRO) - Math.exp(-REACH / BLEND_MACRO));
			wMicro[pm.ordinal()] += a;
			wMacro[pm.ordinal()] += b;
			totalMicro += a;
			totalMacro += b;
		}

		// Mountain belts along the border of each pair of near cells. Written only in distances, symmetric in the
		// pair, and fading with how far the pair is from being nearest, so it is continuous across every border.
		double dBest = Math.sqrt(d2[best]);
		int[] near = new int[25];
		int nn = 0;
		for (int m = 0; m < 25; m++) {
			near[nn++] = m; // all 25: with BELT <= 270 a cell outside the window can never reach a belt
		}
		for (int a = 0; a < nn; a++) {
			for (int b = a + 1; b < nn; b++) {
				int ia = near[a], ib = near[b];
				double belt = beltStrength(is[ia], js[ia], provinceOf(is[ia], js[ia]), is[ib], js[ib], provinceOf(is[ib], js[ib]));
				if (belt <= 0) continue;
				double da = Math.sqrt(d2[ia]), db = Math.sqrt(d2[ib]);
				double t = MathHelper.clamp(1 - 0.5 * Math.abs(da - db) / BELT, 0, 1);
				double g = MathHelper.clamp(1 - (Math.min(da, db) - dBest) / BELT, 0, 1);
				mountain = Math.max(mountain, belt * t * t * (3 - 2 * t) * g);
			}
		}

		Column col = new Column();
		col.province = p1;
		col.mountain = mountain;
		col.ocean = wMacro[Province.OCEAN.ordinal()] / totalMacro;

		// MACRO: province base levels blended broadly, a slow continental swell, and the broad uplift of belts.
		double macro = 0;
		for (Province p : Province.values()) {
			macro += wMacro[p.ordinal()] / totalMacro * baseLevel(p);
		}
		double land = 1 - col.ocean;
		macro += land * 26 * fbm(relief, x, z, 1800, 3) + 6 * fbm(relief, x - 7000, z, 600, 2);
		macro += mountain * (60 + 30 * fbm(relief, x + 5000, z, 1200, 2));
		// Coastal plains: the first COAST_RISE blocks of land above the sea rise COAST_SLOPE times as fast, and the
		// last SHELF blocks under it too, so a wide flat strip lies between the water and the hills.
		macro = coastalPlain(macro);
		col.macro = macro;
		if (!full) return col;
		double inland = smooth(SEA + BANK, SEA + BANK + COAST_RISE * COAST_SLOPE + 14, macro); // 0 on the plain, 1 past it

		// MICRO: rounded hills everywhere on land, each province's own landforms, and peaks on the belts.
		double hills = 9 * Math.pow(0.5 + 0.5 * fbm(detail, x, z, 230, 3), 1.6) + 2.5 * fbm(detail, x + 900, z, 55, 2);
		double micro = land * hills * (0.12 + 0.88 * inland), plateau = 0;
		for (Province p : Province.values()) {
			double w = wMicro[p.ordinal()] / totalMicro;
			if (w < 1e-4) continue;
			double[] out = provinceMicro(p, x, z, col, p == p1);
			micro += w * out[0] * (p == Province.OCEAN ? 1 : 0.2 + 0.8 * inland);
			plateau += w * out[1];
		}
		if (mountain > 0) {
			double r = 1 - Math.abs(fbm(ridge, x, z, 340, 4));
			r = r * r * (3 - 2 * r);
			micro += mountain * 80 * r * (0.6 + 0.4 * fbm(relief, x - 3000, z, 700, 2));
		}
		col.plateau = plateau;
		// Under the sea, small relief may not reach the water line: crests that touched it drew thin lines of
		// land at sea level across shallow seas. Only the beach (macro near the shore) and big seamounts get out.
		double cap = Math.max(SEA - 3, macro + 1) + 400 * smooth(SEA, SEA + BANK, macro);
		col.height = MathHelper.clamp(smoothMin(macro + micro, cap, 3), -40, 300);
		return col;
	}

	// ---- volcanic cones (stage 4b) ------------------------------------------------------------------------

	static final int CONE_GRID = 700;

	/**
	 * A stratovolcano: centre, mean radius, height over its base, crater radius and depth, whether lava fills it, and its
	 * shape: stretched along an axis (elong, rot) and lobed (three harmonics of the radius by direction), so no two
	 * cones are round and alike (3.42.1).
	 */
	record Cone(double cx, double cz, double r, double base, double amp, double rc, double depth, boolean active,
			double elong, double rot, double a1, double p1, double a2, double p2, double a3, double p3) {
		/** Height of the cone over its base at relative distance d (0 = centre, 1 = foot): a wide, gentle apron and a steep top. */
		double profile(double d) {
			double u = 1 - d;
			return amp * (0.62 * Math.pow(u, 2.4) + 0.38 * Math.pow(u, 1.15));
		}

		double rim() {
			return base + profile(rc / r);
		}
	}

	private static final Cone NO_CONE = new Cone(0, 0, 0, 0, 0, 0, 0, false, 1, 0, 0, 0, 0, 0, 0, 0);
	private final ConcurrentHashMap<Long, Cone> cones = new ConcurrentHashMap<>();

	/** The cone of a grid cell, or null: on land in the volcanic province, or in the fringe of a mountain belt. */
	Cone coneOf(long i, long j) {
		Cone c = cones.computeIfAbsent(i * 1_000_003L + j, k -> {
			if (unit(i, j, 61) > options.coneChance()) return NO_CONE;
			double cx = (i + 0.3 + 0.4 * unit(i, j, 62)) * CONE_GRID, cz = (j + 0.3 + 0.4 * unit(i, j, 63)) * CONE_GRID;
			Column at = computeBase((int) cx, (int) cz, false);
			boolean volcanic = at.province == Province.VOLCANIC && at.mountain < 0.3;
			boolean arc = at.mountain > 0.15 && at.mountain < 0.45 && unit(i, j, 64) < 0.5;
			if (!(volcanic || arc) || at.ocean > 0.25 || at.macro < SEA + 8) return NO_CONE;
			double r = 170 + 100 * unit(i, j, 65);
			double amp = 65 + 80 * unit(i, j, 66);
			amp = Math.min(amp, 250 - at.macro);
			double tau = 2 * Math.PI;
			return new Cone(cx, cz, r, at.macro, amp, 14 + 14 * unit(i, j, 67), 14 + 12 * unit(i, j, 68),
					unit(i, j, 69) < 0.3, 0.65 + 0.25 * unit(i, j, 70), tau * unit(i, j, 71),
					0.10 + 0.14 * unit(i, j, 72), tau * unit(i, j, 73), 0.06 + 0.10 * unit(i, j, 74), tau * unit(i, j, 75),
					0.03 + 0.07 * unit(i, j, 76), tau * unit(i, j, 77));
		});
		return c == NO_CONE ? null : c;
	}

	/**
	 * Raises a cone over the terrain and cuts its crater; a crater holds a lake of water, or of lava if active.
	 * Near the summit the cone is round (the crater stays whole); further down the outline is stretched, lobed and
	 * warped, and the flanks are cut by radial gullies (barrancos) of uneven depth.
	 */
	private void applyCones(Column col, int x, int z) {
		long gi = Math.floorDiv(x, CONE_GRID), gj = Math.floorDiv(z, CONE_GRID);
		for (long i = gi - 1; i <= gi + 1; i++) {
			for (long j = gj - 1; j <= gj + 1; j++) {
				Cone c = coneOf(i, j);
				if (c == null) continue;
				double dx0 = x - c.cx, dz0 = z - c.cz;
				double d0 = Math.hypot(dx0, dz0) / c.r;
				if (d0 >= 2.6) continue;
				double rcRel = c.rc / c.r;
				double rim = c.rim();
				double floor = rim - c.depth;
				if (d0 < rcRel) {
					double t = d0 / rcRel;
					double h = floor + (rim - floor) * t * t * t * t;
					col.height = h;
					int lake = (int) Math.floor(floor + c.depth * 0.45);
					if (lake > h) {
						col.craterLake = lake;
						col.lava = c.active;
					}
					mark(col, c, 1);
					continue;
				}
				// shape grows in from the rim: 0 at the crater, full from three crater radii out
				double s = smooth(rcRel, rcRel * 3 + 0.1, d0);
				double dx = dx0 + s * 30 * fbm(detail, x + 11000, z, 110, 2);
				double dz = dz0 + s * 30 * fbm(detail, x, z + 11000, 110, 2);
				double cos = Math.cos(c.rot), sin = Math.sin(c.rot);
				double u = dx * cos + dz * sin, v = (-dx * sin + dz * cos) / (1 + s * (1 / c.elong - 1));
				double th = Math.atan2(v, u);
				double lobes = 1 + s * (c.a1 * Math.sin(th + c.p1) + c.a2 * Math.sin(2 * th + c.p2) + c.a3 * Math.sin(3 * th + c.p3));
				double d = Math.hypot(u, v) / (c.r * lobes);
				if (d >= 1) continue;
				// the foot settles onto the ground around it, so there is no step where the cone ends
				double h = c.profile(d) + MathHelper.lerp(smooth(0.5, 1.0, d), c.base, col.height);
				// gullies: deepest at mid-slope, fading at the rim and at the foot
				double g = Math.pow(0.5 + 0.5 * Math.sin(11 * th + 3.5 * fbm(detail, x + 12000, z, 160, 2)), 3);
				h -= g * c.amp * 0.22 * Math.min(d - rcRel, 1 - d) * s;
				if (h <= col.height) continue;
				col.height = smoothMax(col.height, h, 6);
				mark(col, c, Math.min(1, (1 - d) / (1 - rcRel)));
			}
		}
	}

	private static void mark(Column col, Cone c, double frac) {
		col.cone = true;
		col.coneFrac = frac;
		col.coneBase = c.base;
		col.river = false;
		col.riverWater = Integer.MIN_VALUE;
	}

	/** {centre x, centre z, rim height, crater radius, 1 if active, mean radius} of the cone of a grid cell, or null (photos, tests). */
	public double[] cone(long i, long j) {
		Cone c = coneOf(i, j);
		if (c == null) return null;
		return new double[]{c.cx, c.cz, c.rim(), c.rc, c.active ? 1 : 0, c.r};
	}

	private static double smoothMax(double a, double b, double k) {
		return -smoothMin(-a, -b, k);
	}

	/**
	 * Cuts the river valleys and channels into a column: banks rise gently from the water over VALLEY blocks, the
	 * channel is a rounded trough under the water level. Water above sea level is placed by the surface painter.
	 */
	private void applyRivers(Column col, int x, int z) {
		if (col.macro < SEA - 2) return;
		Rivers.Hit hit = rivers.near(x, z);
		if (hit == null) return;
		double h = col.height;
		double out = Math.max(0, hit.dist - hit.halfWidth);
		double bank = hit.level + 1.2 + out * 0.45 + out * out * 0.012;
		h = smoothMin(h, bank, 4);
		if (hit.dist < hit.halfWidth) {
			double f = hit.dist / hit.halfWidth;
			double bed = hit.level - 0.5 - hit.depth * (1 - f * f);
			h = Math.min(h, bed);
			col.river = true;
			col.riverWater = (int) Math.floor(hit.level);
		}
		col.height = h;
	}

	static final double COAST_RISE = 20, COAST_SLOPE = 0.25, CATCH_UP = 52, SHELF = 14, SHELF_SLOPE = 0.35, SHELF_CATCH_UP = 32;
	/** The plain starts BANK blocks above the water; the last BEACH blocks of shelf climb to it as a short beach. */
	static final double BANK = 3, BEACH = 4, BEACH_DEPTH = 3;

	/**
	 * Squeezes the macro height near sea level into a gentle coastal plain and a shallow shelf. The plain starts
	 * BANK blocks above the water (the surface block sits about one under the column height, so a plain right at
	 * sea level would read as flooded), and a short beach climbs to it from the shelf. Continuous, and the same as
	 * before beyond CATCH_UP blocks above / SHELF_CATCH_UP below the sea, so inland heights don't change.
	 */
	static double coastalPlain(double h) {
		double m = h - SEA;
		if (m >= CATCH_UP || m <= -SHELF_CATCH_UP) return h;
		if (m >= COAST_RISE) {
			double low = BANK + COAST_RISE * COAST_SLOPE;
			return SEA + low + (m - COAST_RISE) * (CATCH_UP - low) / (CATCH_UP - COAST_RISE);
		}
		if (m >= 0) return SEA + BANK + m * COAST_SLOPE;
		if (m >= -BEACH) return SEA + BANK - (BANK + BEACH_DEPTH) * (-m / BEACH);
		if (m >= -SHELF) return SEA - BEACH_DEPTH + (m + BEACH) * SHELF_SLOPE;
		double low = BEACH_DEPTH + (SHELF - BEACH) * SHELF_SLOPE;
		return SEA - low + (m + SHELF) * (SHELF_CATCH_UP - low) / (SHELF_CATCH_UP - SHELF);
	}

	/** Macro level of each province: deep sea floor, low basins, moderate shields, high volcanic uplands. */
	private static double baseLevel(Province p) {
		return switch (p) {
			case OCEAN -> 30;
			case BASIN -> 72;
			case CRATON -> 80;
			case VOLCANIC -> 92;
		};
	}

	/** {height, plateau fraction} of a province's micro relief at a column. */
	private double[] provinceMicro(Province p, int x, int z, Column col, boolean dominant) {
		switch (p) {
			case OCEAN:
				return new double[]{6 * fbm(relief, x, z, 180, 3), 0};
			case BASIN: {
				// chapadas: flat tops and cliffs (paredoes) with the layers showing
				double m = fbm(mesa, x, z, 320, 4);
				double plate = smooth(0.30, 0.36, m);
				double top = plate * (14 + 12 * (0.5 + 0.5 * fbm(mesaH, x, z, 500, 2)));
				Doline d = dolineAt(x, z);
				double hollow = d == null ? 0 : d.depth * Math.pow(1 - (d.dist / d.r) * (d.dist / d.r), 2);
				return new double[]{terrace(top, 6) - hollow, plate};
			}
			case VOLCANIC: {
				double v = Math.max(0, smooth(-0.05, 0.3, fbm(trap, x, z, 450, 3)) * 24 + 6 * fbm(detail, x, z, 200, 2));
				double neck = spire(x, z, 240, 0.16, 10, 18, 40, 80, 11);
				if (neck > 0 && dominant) col.neck = true;
				return new double[]{terrace(v, 8) + neck, smooth(4, 12, v)};
			}
			case CRATON:
			default: {
				double dome = domes(x, z);
				if (dome > 2 && dominant) col.dome = true;
				return new double[]{dome, 0};
			}
		}
	}

	/** Granite domes (inselbergs, like Sugarloaf Mountain): rounded tops, near-vertical flanks. */
	private double domes(int x, int z) {
		final int grid = 200;
		long gi = Math.floorDiv(x, grid), gj = Math.floorDiv(z, grid);
		double best = 0;
		for (long i = gi - 1; i <= gi + 1; i++) {
			for (long j = gj - 1; j <= gj + 1; j++) {
				if (unit(i, j, 21) > 0.11) continue;
				double cx = (i + 0.2 + 0.6 * unit(i, j, 22)) * grid, cz = (j + 0.2 + 0.6 * unit(i, j, 23)) * grid;
				double ang = unit(i, j, 24) * Math.PI;
				double stretch = 1 + 0.7 * unit(i, j, 25);
				double r0 = 30 + 34 * unit(i, j, 26), amp = 30 + 45 * unit(i, j, 27);
				double shape = unit(i, j, 28) < 0.3 ? 0.5 : 1.4; // a few with sheer walls, most rounded
				double dx = x - cx, dz = z - cz;
				double a = dx * Math.cos(ang) + dz * Math.sin(ang), b = -dx * Math.sin(ang) + dz * Math.cos(ang);
				double q = (a * a) / (r0 * r0 * stretch * stretch) + (b * b) / (r0 * r0);
				if (q < 1) best = Math.max(best, amp * Math.pow(1 - q, shape));
			}
		}
		return best;
	}

	/** Narrow steep spires (volcanic necks). */
	private double spire(int x, int z, int grid, double chance, double rMin, double rMax, double aMin, double aMax, long salt) {
		long gi = Math.floorDiv(x, grid), gj = Math.floorDiv(z, grid);
		double best = 0;
		for (long i = gi - 1; i <= gi + 1; i++) {
			for (long j = gj - 1; j <= gj + 1; j++) {
				if (unit(i, j, salt) > chance) continue;
				double cx = (i + 0.2 + 0.6 * unit(i, j, salt + 1)) * grid, cz = (j + 0.2 + 0.6 * unit(i, j, salt + 2)) * grid;
				double r0 = rMin + (rMax - rMin) * unit(i, j, salt + 3), amp = aMin + (aMax - aMin) * unit(i, j, salt + 4);
				double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)) / r0;
				if (d < 1) best = Math.max(best, amp * Math.pow(1 - d, 0.7));
			}
		}
		return best;
	}

	// ---- biome climate (vanilla multi-noise parameters) ----------------------------------------------

	public double continentalness(int x, int z) {
		Column c = column(x, z);
		if (c.height < SEA - 1) return Math.max(-1.0, -0.19 - (SEA - 1 - c.height) / 20 * 0.8);
		if (c.ocean > 0.22 && c.height < SEA + 4 && c.mountain < 0.2) return -0.15;
		return -0.11 + 0.9 * MathHelper.clamp((0.5 - c.ocean) / 0.5, 0, 1);
	}

	public double erosion(int x, int z) {
		Column c = column(x, z);
		double e = switch (c.province) {
			case BASIN -> 0.5;
			case CRATON -> c.dome ? -0.3 : 0.25;
			case VOLCANIC -> 0.1;
			case OCEAN -> 0.2;
		};
		if (c.plateau > 0.5) e = Math.min(e, -0.28);
		// 3.49.0: erosion also wanders inside a province, so one province holds more than one row of the biome table
		if (!options.legacy() && c.province != Province.OCEAN) e += 0.32 * fbm(climateVar, x, z, 380 * options.biomes().temperature, 2);
		return MathHelper.clamp(e - 1.35 * c.mountain, -0.95, 0.9);
	}

	public double weirdness(int x, int z) {
		Column c = column(x, z);
		// SNOW_LINE_RAISE: the high slopes and peaks (snowy biomes in the cold bands) start that much higher.
		double pv = MathHelper.clamp(-0.6 + 1.6 * (c.height - 64 - SNOW_LINE_RAISE) / 130, -0.6, 1.0);
		double w = (pv + 1) / 3;
		if (c.river) return 0; // river valley: the river biome
		// 3.49.0: the variant biomes (sunflower plains, flower forest...) come in patches about half as wide as before
		double scale = options.legacy() ? 400 : 220 * options.biomes().temperature;
		return fbm(soil, x + 9000, z, scale, 2) < 0 ? -w : w;
	}

	/** Vanilla-style offset so that the biome "depth" parameter is 0 at the surface. */
	public double offset(int x, int z) {
		return (column(x, z).height + 64) / 128 - 1.5;
	}

	// ---- rocks ---------------------------------------------------------------------------------------

	public enum Rock { STONE, GRANITE, DIORITE, ANDESITE, TUFF, BASALT, SMOOTH_BASALT, SANDSTONE, TERRACOTTA, BROWN_TERRACOTTA, CALCITE,
		/** stage 4a (3.42.0): the mod's own rocks */
		SHALE, GNEISS, PERIDOTITE }

	private static final Rock[] SEDIMENTS = {Rock.SANDSTONE, Rock.TERRACOTTA, Rock.CALCITE, Rock.SANDSTONE, Rock.SHALE, Rock.CALCITE};
	private static final Rock[] FOLDED = {Rock.ANDESITE, Rock.STONE, Rock.DIORITE, Rock.ANDESITE, Rock.TUFF, Rock.STONE};

	/** The rock at a position, below the soil. */
	public Rock rock(int x, int y, int z) {
		Column c = column(x, z);
		if (c.neck && c.province == Province.VOLCANIC) return Rock.BASALT;
		// a stratovolcano: layers of andesite lava and tuff (ash) down to a little under the ground it stands on
		if (c.cone && y > c.coneBase - 6) return Math.floorMod(y, 7) < 2 ? Rock.TUFF : Rock.ANDESITE;
		if (c.mountain > 0.45) {
			double v = y + foldOffset(x, z);
			int idx = Math.floorMod((int) Math.floor(v / 5), FOLDED.length);
			// ophiolites: rare slivers of mantle peridotite pushed up in the belt
			if (rock3d.sample(x / 28.0 + 300, y / 20.0, z / 28.0 + 300) > 0.62) return Rock.PERIDOTITE;
			// the deep core of the belt: granite, and gneiss below it
			if (c.height - y > 18 && rock3d.sample(x / 50.0, y / 40.0, z / 50.0) > 0.25) return c.height - y > 45 ? Rock.GNEISS : Rock.GRANITE;
			return FOLDED[idx];
		}
		switch (c.province) {
			case OCEAN:
				return y > 12 ? Rock.SMOOTH_BASALT : Rock.STONE;
			case BASIN: {
				sediments(c, x, z);
				if (y < c.sedFloor) return Rock.STONE;
				int idx = Math.floorMod((int) Math.floor((y + c.layerShift) / 5), SEDIMENTS.length);
				return SEDIMENTS[idx];
			}
			case VOLCANIC: {
				if (c.neck) return Rock.BASALT;
				sediments(c, x, z);
				if (y < c.sedFloor) return y >= c.sedFloor - 14 ? Rock.PERIDOTITE : Rock.GRANITE;
				return Math.floorMod(y - 70, 8) == 0 ? Rock.TUFF : Rock.BASALT;
			}
			case CRATON:
			default: {
				if (c.dome) return Rock.GRANITE;
				double g = rock3d.sample(x / 70.0, y / 50.0, z / 70.0);
				if (g > -0.15) return Rock.GRANITE;
				if (g < -0.55) return Rock.DIORITE;
				return Rock.GNEISS; // the old basement of the shield
			}
		}
	}

	// ---- karst (stage 2b) ------------------------------------------------------------------------------

	/** Calcite comes every KARST_BAND blocks in the basin layers (SEDIMENTS indices 2 and 5, 5 blocks each). */
	static final int KARST_BAND = 15;
	static final double KARST_MID = 12.5;
	static final int DOLINE_GRID = 56;
	/** Cave roofs stay at least this far under the ground (only collapse dolines open them). */
	static final int KARST_ROOF = 7;

	/** Bottom of the basin sediments or of the basalt flows, and the fold of the basin layers. */
	private void sediments(Column c, int x, int z) {
		if (c.sedReady) return;
		c.sedFloor = c.province == Province.VOLCANIC ? 46 + 4 * fbm(detail, x + 600, z, 120, 2)
				: 38 + 6 * fbm(detail, x + 300, z, 120, 2);
		c.layerShift = 2 * fbm(fold, x, z, 90, 2);
		c.sedReady = true;
	}

	/** Bottom of the basin sediments at a column (below it, plain stone). */
	public double sedimentFloor(int x, int z) {
		Column c = column(x, z);
		sediments(c, x, z);
		return c.sedFloor;
	}

	/** True inside the shaft of a collapse doline. */
	public boolean collapseShaft(int x, int z) {
		Doline d = dolineAt(x, z);
		return d != null && d.collapse && d.dist < 2.5 + 0.2 * d.r;
	}

	public boolean basaltAt(int x, int z) {
		return basaltColumn(column(x, z));
	}

	public boolean karstAt(int x, int z) {
		return karstColumn(column(x, z));
	}

	/** Where there is karst: basin sediments under dry land, away from the mountain belts. */
	static boolean karstColumn(Column c) {
		return c.province == Province.BASIN && c.mountain < 0.3 && c.ocean < 0.5 && c.height >= SEA + 2;
	}

	/** The doline whose hollow holds this column, or null. */
	Doline dolineAt(int x, int z) {
		long gi = Math.floorDiv(x, DOLINE_GRID), gj = Math.floorDiv(z, DOLINE_GRID);
		Doline best = null;
		for (long i = gi - 1; i <= gi + 1; i++) {
			for (long j = gj - 1; j <= gj + 1; j++) {
				if (unit(i, j, 41) > 0.45) continue;
				double cx = (i + 0.25 + 0.5 * unit(i, j, 42)) * DOLINE_GRID, cz = (j + 0.25 + 0.5 * unit(i, j, 43)) * DOLINE_GRID;
				double r = 7 + 9 * unit(i, j, 44);
				double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
				if (d >= r || (best != null && d / r >= best.dist / best.r)) continue;
				if (best == null) best = new Doline();
				best.cx = cx;
				best.cz = cz;
				best.r = r;
				best.dist = d;
				best.depth = 2.5 + r * (0.15 + 0.3 * unit(i, j, 45));
				best.collapse = unit(i, j, 46) < 0.35;
			}
		}
		return best;
	}

	/** How open a cave level is at a column: above 0 inside a chamber or a tunnel, up to 1. */
	private double caveShape(int x, int z, int level) {
		double ox = 1733.0 * level, oz = -911.0 * level;
		double chamber = (fbm(karstA, x + ox, z + oz, 70, 3) - 0.30) / 0.25;
		double tunnel = (0.07 - Math.abs(fbm(karstB, x + ox, z + oz, 110, 2))) / 0.07 * 0.45;
		return Math.min(1, Math.max(chamber, tunnel));
	}

	/**
	 * Karst caves, as a density (below 0 inside): wide, low caves along the calcite layers of the basins, flat
	 * floored, with a big chamber under every collapse doline and the doline's shaft opening it to the sky.
	 */
	private double karst(Column c, int x, int y, int z) {
		if (y > c.height + 2) return 4;
		sediments(c, x, z);
		if (y < c.sedFloor - 1) return 4;
		double v = y + c.layerShift;
		int level = (int) Math.round((v - KARST_MID) / KARST_BAND);
		double dy = v - (level * KARST_BAND + KARST_MID);
		double s = caveShape(x, z, level);
		double shaft = 4;
		Doline d = c.doline != null ? c.doline : (c.doline = dolineAt(x, z));
		if (d != null && d.collapse) {
			double floorY = column((int) Math.floor(d.cx), (int) Math.floor(d.cz)).height;
			int bottom = (int) Math.floor((floorY - 12 + c.layerShift - KARST_MID) / KARST_BAND);
			if (level == bottom) s = Math.max(s, 1 - d.dist / (d.r * 1.3));
			double bottomY = bottom * KARST_BAND + KARST_MID - c.layerShift;
			if (y >= bottomY - 1) shaft = (d.dist - (2.5 + 0.2 * d.r)) / 3;
		}
		double cave = 4;
		if (s > 0) {
			double h = 1 + 5.5 * s;
			cave = ((dy < 0 ? -dy * 1.6 : dy) - h) / 4;
		}
		cave = Math.max(cave, (y - (c.height - KARST_ROOF)) / 3);
		cave = Math.max(cave, (c.sedFloor - y) / 3);
		return MathHelper.clamp(Math.min(cave, shaft), -3, 4);
	}

	/** Where there are basalt flows with lava tubes: volcanic uplands on land, away from necks and mountain belts. */
	static boolean basaltColumn(Column c) {
		return c.province == Province.VOLCANIC && !c.neck && c.mountain < 0.3 && c.ocean < 0.5 && c.height >= SEA + 2;
	}

	/**
	 * Lava tubes (stage 2c): long winding round tunnels in the basalt flows, about 6 to 9 blocks under the old
	 * surface of the flows (the macro relief), in two levels, here and there open to the sky by a skylight.
	 */
	private double lavaTubes(Column c, int x, int y, int z) {
		if (y > c.height + 2 || y < c.sedFloor - 1 || fbm(tubeMask, x, z, 700, 2) < -0.3) return 4;
		double best = 4;
		for (int level = 0; level < 2; level++) {
			double ox = 977.0 * level, oz = 1531.0 * level;
			double path = Math.abs(fbm(tubeA, x + ox, z + oz, 150, 2));
			final double w = 0.06;
			if (path >= w) continue;
			double r = 2.2 + 1.3 * (0.5 + 0.5 * fbm(tubeB, x + ox, z + oz, 60, 2));
			double yc = Math.min(c.macro, c.height) - 6 - 9 * level + 1.5 * fbm(tubeB, x - 500 + ox, z, 200, 2);
			if (yc - r < c.sedFloor || yc - r < SEA + 1) continue; // dry tubes only: above the water
			double side = path / w * r, dy = y - yc;
			double d = (Math.sqrt(side * side + (dy < 0 ? dy * 1.5 : dy) * (dy < 0 ? dy * 1.5 : dy)) - r) / 2;
			boolean skylight = level == 0 && fbm(tubeB, x + 4000, z, 14, 1) > 0.62;
			if (!skylight) d = Math.max(d, (y - (c.height - 3)) / 2);
			else if (y > yc) d = Math.min(d, (side - r * 0.8) / 2);
			best = Math.min(best, d);
		}
		return best;
	}

	/** Caves made by the world type, as a density (below 0 inside): karst in the basins, lava tubes in basalt. */
	public double caves(int x, int y, int z) {
		Column c = column(x, z);
		if (karstColumn(c)) return karst(c, x, y, z);
		if (basaltColumn(c)) {
			sediments(c, x, z);
			return MathHelper.clamp(lavaTubes(c, x, y, z), -3, 4);
		}
		return 4;
	}

	/**
	 * Fills vanilla's caves (1 where the ground is kept solid, -1 elsewhere): all of them in the basin sediments
	 * (they have karst instead) and in the basalt flows (lava tubes instead), and most of them in granite.
	 */
	public double caveFill(int x, int y, int z) {
		Column c = column(x, z);
		if (y >= c.height - 1 || y < 0) return -1;
		boolean basin = karstColumn(c), basalt = basaltColumn(c);
		if (basin || basalt) {
			sediments(c, x, z);
			if (y > c.sedFloor + 2) return 1;
			if (basin) return -1;
			return fewerCaves(x, y, z); // granite under the basalt
		}
		if (c.province == Province.CRATON && c.mountain <= 0.45 && c.height >= SEA + 2 && rock(x, y, z) == Rock.GRANITE) {
			return fewerCaves(x, y, z);
		}
		return -1;
	}

	/** Granite keeps about a fifth of vanilla's caves: they are filled in big rounded patches. */
	private double fewerCaves(int x, int y, int z) {
		return tubeMask.sample(x / 60.0, y / 40.0, z / 60.0) > -0.45 ? 1 : -1;
	}

	/** Aquifer floodedness: 2 makes our caves take the sea's level (flooded below it, dry above), -1 leaves vanilla's. */
	public double caveFlood(int x, int y, int z) {
		Column c = column(x, z);
		if (!(karstColumn(c) || basaltColumn(c)) || y > c.height) return -1;
		sediments(c, x, z);
		return y >= c.sedFloor - 4 ? 2 : -1;
	}

	/** True where the soil is missing and the rock comes to the surface: rare patches about 80 blocks across. */
	public boolean outcropZone(int x, int z) {
		double v = fbm(outcrop, x, z, 140, 3) + 0.12 * fbm(detail, x + 1700, z, 14, 2);
		return v > 0.5;
	}

	/** Patchy coarse soil (over granite). */
	public boolean coarseSoil(int x, int z) {
		return fbm(soil, x, z, 45, 2) > 0.45;
	}

	// ---- strata for the deposits (stage 3a) ----------------------------------------------------------

	/** Basin layers: the rock at y is SEDIMENTS[floor((y + layerShift) / 5) mod 6]. */
	public double layerShift(int x, int z) {
		Column c = column(x, z);
		sediments(c, x, z);
		return c.layerShift;
	}

	/** Mountain belts: the folded band at y is FOLDED[floor((y + foldOffset) / 5) mod 6]. */
	public double foldOffset(int x, int z) {
		return 30 * fbm(fold, x, z, 110, 3) + 0.12 * (x + z);
	}
}
