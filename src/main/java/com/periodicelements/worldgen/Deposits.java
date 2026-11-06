package com.periodicelements.worldgen;

import com.google.gson.JsonElement;
import com.periodicelements.ElementTable;
import com.periodicelements.GeoOres;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stage 3a of the Geological world: ores in deposits bound to the rock, in place of scattered veins.
 *
 * <p>The map is cut into regions of {@link #REGION} blocks. Each region takes the setting that most of it lies in
 * (craton, mountain belt, basin or volcanic province) and gets one big deposit of every kind that setting has, with
 * small pods ("satellites") scattered around it. Every setting has an iron, a copper and a coal kind, so those three
 * are never more than a region or so away. Ores go only into their host rocks: the ore block is the variant for the
 * rock it replaces ({@link GeoOres}). Deposits that come close to the surface leave discreet hints on the ground.
 *
 * <p>Everything is a pure function of the seed and the position, and each chunk writes only inside itself, so
 * chunks can be generated in any order and on any thread.
 */
public final class Deposits {
	static final int REGION = 256;
	static final int MARGIN = 32;

	private static final Set<Identifier> REPLACED = new HashSet<>();

	static {
		for (JsonElement e : ElementTable.readObject("/periodic_elements/geo_ores.json").getAsJsonArray("disabledFeatures")) {
			REPLACED.add(Identifier.of(e.getAsString()));
		}
	}

	private Deposits() {
	}

	/** True for the vanilla and mod veins that the deposits replace in the Geological world. */
	public static boolean replacesVein(Identifier placedFeature) {
		return REPLACED.contains(placedFeature);
	}

	/** Minerals of the deposits a region holds (tests and the Atlas). Empty for open sea. */
	public static Set<String> mineralsIn(Geology g, int rx, int rz) {
		Set<String> out = new java.util.TreeSet<>();
		for (Deposit d : region(g, rx, rz)) {
			for (Zone z : d.zones) out.addAll(List.of(z.minerals));
		}
		return out;
	}

	/**
	 * Estimated ore blocks per chunk, by mineral, over rx0..rx1 x rz0..rz1 regions, without generating chunks: the
	 * host is taken from the geology map (caves and soil ignored). Columns are sampled every {@code step} blocks.
	 * Used to balance the deposits against the default world (stage 3c starts from it).
	 */
	public static java.util.Map<String, Double> survey(Geology g, int rx0, int rz0, int rx1, int rz1, int step) {
		java.util.Map<String, Double> out = new java.util.TreeMap<>();
		long x0 = (long) rx0 * REGION, x1 = (long) (rx1 + 1) * REGION, z0 = (long) rz0 * REGION, z1 = (long) (rz1 + 1) * REGION;
		int land = 0;
		for (int rx = rx0; rx <= rx1; rx++) {
			for (int rz = rz0; rz <= rz1; rz++) {
				if (!region(g, rx, rz).isEmpty()) land++;
				for (Deposit d : region(g, rx, rz)) {
					Survey s = new Survey(g, out, step * step);
					for (int x = d.minX - Math.floorMod(d.minX, step); x <= d.maxX; x += step) {
						for (int z = d.minZ - Math.floorMod(d.minZ, step); z <= d.maxZ; z += step) {
							if (x < x0 || x >= x1 || z < z0 || z >= z1) continue;
							s.column(d, x, z);
						}
					}
				}
			}
		}
		double chunks = Math.max(1, land) * (REGION / 16.0) * (REGION / 16.0); // per chunk of land regions
		out.put("~land_regions", (double) land);
		out.replaceAll((k, v) -> k.startsWith("~") ? v : v / chunks);
		return out;
	}

	/** The survey's stand-in for a chunk: hosts from the map, ores counted instead of placed. */
	private static final class Survey {
		final Geology g;
		final java.util.Map<String, Double> out;
		final double weight;

		Survey(Geology g, java.util.Map<String, Double> out, double weight) {
			this.g = g;
			this.out = out;
			this.weight = weight;
		}

		void column(Deposit d, int x, int z) {
			Geology.Column c = g.column(x, z);
			int top = (int) Math.ceil(c.height) - 4;
			forEachOre(g, d, x, z, -63, Math.min(top, 319), top, (y, mineral) -> {
				if (mineral == null) return;
				BlockState host = d.rock != null ? d.rock : y < 0 ? Blocks.DEEPSLATE.getDefaultState() : rockState(g.rock(x, y, z));
				if (GeoOres.ore(mineral, host) != null) out.merge(mineral, weight, Double::sum);
			});
		}
	}

	private static BlockState rockState(Geology.Rock r) {
		return GeoWorld.rockState(r);
	}

	/** Region size in blocks. */
	public static int regionSize() {
		return REGION;
	}

	// ---- deposit kinds --------------------------------------------------------------------------------

	enum Setting { CRATON, MOUNTAIN, BASIN, VOLCANIC }

	enum Shape {
		/** Ellipsoid, richest at the centre (porphyry, skarn, greenstone). */
		BODY,
		/** Small ellipsoid: satellites and pockets. */
		POD,
		/** Thin steep slab (pegmatite, quartz vein). */
		DIKE,
		/** Thick folded layer at depth (banded iron formation). */
		FOLDED_LAYER,
		/** Part of one basin layer: follows the strata, so it stays in the same rock. */
		BASIN_STRATUM,
		/** Part of one folded band of a mountain belt. */
		FOLDED_STRATUM,
		/** A flat level between basalt flows. */
		FLAT,
		/** A vertical pipe (kimberlite, fumarole chimney): radius ra, from minY to maxY. */
		PIPE,
		/** Laterite: a crust 2-6 blocks thick under the soil (handled per column). */
		CRUST
	}

	/** Minerals of one zone of a deposit (zones go outwards from the centre, up to rMax). */
	record Zone(double rMax, String[] minerals, double[] weights) {
	}

	private static Zone zone(double rMax, Object... mw) {
		String[] m = new String[mw.length / 2];
		double[] w = new double[mw.length / 2];
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			m[i] = (String) mw[2 * i];
			w[i] = ((Number) mw[2 * i + 1]).doubleValue();
			sum += w[i];
		}
		for (int i = 0; i < w.length; i++) w[i] /= sum;
		return new Zone(rMax, m, w);
	}

	static final Zone[] PEGMATITE = {zone(1, "spodumene", 21, "cassiterite", 40, "coltan", 18, "pitchblende", 30, "fluorite", 4)};
	static final Zone[] QUARTZ = {zone(1, "gold", 13, "wolframite", 34, "cassiterite", 46, "arsenopyrite", 12, "stibnite", 7)};
	static final Zone[] BIF = {zone(1, "iron", 1)};
	static final Zone[] GREENSTONE = {zone(0.6, "copper", 92, "gold", 8), zone(1, "copper", 70, "gold", 10, "arsenopyrite", 10, "cobaltite", 20)};
	static final Zone[] COAL = {zone(1, "coal", 1)};
	static final Zone[] PORPHYRY = {zone(0.4, "copper", 30, "molybdenite", 70), zone(0.8, "copper", 92, "gold", 8),
			zone(1, "gold", 35, "arsenopyrite", 35, "stibnite", 10, "molybdenite", 20)};
	static final Zone[] SKARN = {zone(0.7, "iron", 72, "copper", 8, "cobaltite", 22), zone(1, "iron", 40, "copper", 18, "cobaltite", 45)};
	static final Zone[] OIL_SHALE = {zone(1, "oil_shale", 1)};
	static final Zone[] IRON = {zone(1, "iron", 1)};
	static final Zone[] COPPER = {zone(1, "copper", 1)};
	static final Zone[] EVAPORITE = {zone(1, "saltpetre", 40, "borax", 11, "sulfur", 49)};
	static final Zone[] MAGMATIC = {zone(1, "chromite", 70, "pentlandite", 30)};
	static final Zone[] SULFUR = {zone(1, "sulfur", 1)};
	static final Zone[] KIMBERLITE = {zone(1, "diamond", 1)};
	static final Zone[] LATERITE = {zone(1, "bauxite", 75, "pyrolusite", 25)};
	static final Zone[] MVT = {zone(1, "galena", 33, "sphalerite", 23.6, "barite", 11.5, "fluorite", 17, "vanadinite", 8.7)};

	/** Gossan colours: red over iron and sulphides, white over pegmatites; none over coal and shale. */
	private static final BlockState RED = Blocks.RED_TERRACOTTA.getDefaultState();
	private static final BlockState WHITE = Blocks.CALCITE.getDefaultState();

	static final class Deposit {
		Shape shape;
		Zone[] zones;
		double cx, cy, cz;
		/** half axes: along, vertical, across (DIKE: length/2, height/2, thickness/2; strata: footprint along/across) */
		double ra, ry, rc;
		/** unit horizontal direction of the along axis */
		double ax, az;
		/** strata: band index and the part of the 5-block band taken [f0, f1); FLAT: the levels y0..y1 */
		int band;
		double f0, f1;
		/** FOLDED_LAYER: fold amplitude and wavelength */
		double amp, wave;
		double fill;
		/** fraction of strata cells that hold ore (patchy layers), 1 for continuous */
		double patch = 1;
		BlockState hint;
		/** if set, the deposit's own rock: every host block inside it becomes this (kimberlite, laterite) */
		BlockState rock;
		long seed;
		int minX, maxX, minY, maxY, minZ, maxZ;

		void bounds(int yLo, int yHi) {
			double r = Math.max(ra, rc) + 1;
			minX = (int) Math.floor(cx - r);
			maxX = (int) Math.ceil(cx + r);
			minZ = (int) Math.floor(cz - r);
			maxZ = (int) Math.ceil(cz + r);
			minY = yLo;
			maxY = yHi;
		}
	}

	// ---- climate (3b) -----------------------------------------------------------------------------------

	/** Vanilla climate noise at a column: temperature and humidity, -1..1 (the values biomes are picked by). */
	public interface Climate {
		double temperature(int x, int z);

		double humidity(int x, int z);
	}

	private static final ConcurrentHashMap<Long, Climate> CLIMATES = new ConcurrentHashMap<>();

	/** Makes the deposits of this map see the climate of the world (call before its regions are built). */
	public static void useClimate(Geology g, NoiseConfig config) {
		CLIMATES.computeIfAbsent(g.seed, k -> {
			// 3.45.0: sample the two climate noises alone, not the whole multi-noise point: its continentalness is the
			// Geological terrain, which (through the rivers) asks for the climate.
			var temperature = config.getNoiseRouter().temperature();
			var humidity = config.getNoiseRouter().vegetation();
			return new Climate() {
				public double temperature(int x, int z) {
					return clamp(temperature.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(
							(x >> 2) << 2, 80, (z >> 2) << 2)));
				}

				public double humidity(int x, int z) {
					return clamp(humidity.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(
							(x >> 2) << 2, 80, (z >> 2) << 2)));
				}
			};
		});
	}

	/** The climate value as biomes see it (MultiNoiseUtil.toFloat(toLong(v)) rounds to 1/10000). */
	private static double clamp(double v) {
		return net.minecraft.world.biome.source.util.MultiNoiseUtil.toFloat(net.minecraft.world.biome.source.util.MultiNoiseUtil.toLong((float) v));
	}

	/** Hot or warm and dry: deserts, badlands, dry savanna (evaporites). */
	static boolean dry(Geology g, int x, int z) {
		Climate c = CLIMATES.get(g.seed);
		return c != null && c.temperature(x, z) > 0.2 && c.humidity(x, z) < -0.1;
	}

	/** 3.45.0: hot and arid, the deserts and badlands (vanilla's hottest temperature band): no river rises here. */
	static boolean arid(Geology g, int x, int z) {
		Climate c = CLIMATES.get(g.seed);
		return c != null && c.temperature(x, z) > 0.55;
	}

	/** Hot or warm and wet: jungles and wet savanna (laterite). */
	static boolean humidTropics(Geology g, int x, int z) {
		Climate c = CLIMATES.get(g.seed);
		return c != null && c.temperature(x, z) > 0.2 && c.humidity(x, z) > 0.1;
	}

	// ---- regions --------------------------------------------------------------------------------------

	private record Key(long seed, int rx, int rz) {
	}

	private static final ConcurrentHashMap<Key, List<Deposit>> CACHE = new ConcurrentHashMap<>();

	static List<Deposit> region(Geology g, int rx, int rz) {
		if (CACHE.size() > 4096) CACHE.clear();
		return CACHE.computeIfAbsent(new Key(g.seed, rx, rz), k -> build(g, rx, rz));
	}

	/** One line per deposit of the regions in [r0, r1]: minerals, shape, centre and half axes (for finding photo spots). */
	public static List<String> describe(Geology g, int r0, int r1) {
		List<String> out = new java.util.ArrayList<>();
		for (int rx = r0; rx <= r1; rx++) for (int rz = r0; rz <= r1; rz++) {
			for (Deposit d : region(g, rx, rz)) {
				StringBuilder m = new StringBuilder();
				for (Zone z : d.zones) m.append(String.join("+", z.minerals())).append('/');
				out.add(String.format(java.util.Locale.ROOT, "%s %s %.0f %.0f %.0f %.0f %.0f %.0f", m, d.shape, d.cx, d.cy, d.cz, d.ra, d.ry, d.rc));
			}
		}
		return out;
	}

	static Setting setting(Geology.Column c) {
		if (c.province == Geology.Province.OCEAN || c.height < Geology.SEA - 2) return null;
		if (c.mountain > 0.45) return Setting.MOUNTAIN;
		return switch (c.province) {
			case CRATON -> Setting.CRATON;
			case BASIN -> Setting.BASIN;
			default -> Setting.VOLCANIC;
		};
	}

	private record Spot(int x, int z, Setting s) {
	}

	static List<Deposit> build(Geology g, int rx, int rz) {
		Random rnd = new Random(g.seed * 0x9E3779B97F4A7C15L ^ (rx * 341873128712L + rz * 132897987541L) ^ 0x3A17L);
		List<Spot> spots = new ArrayList<>();
		int[] count = new int[Setting.values().length];
		int span = REGION - 2 * MARGIN;
		for (int i = 0; i < 24; i++) {
			int x = rx * REGION + MARGIN + rnd.nextInt(span), z = rz * REGION + MARGIN + rnd.nextInt(span);
			Setting s = setting(g.column(x, z));
			if (s == null) continue;
			spots.add(new Spot(x, z, s));
			count[s.ordinal()]++;
		}
		List<Deposit> out = new ArrayList<>();
		if (spots.size() < 4) return out; // open sea
		Setting main = Setting.values()[0];
		for (Setting s : Setting.values()) if (count[s.ordinal()] > count[main.ordinal()]) main = s;
		final Setting dominant = main;
		List<Spot> here = spots.stream().filter(p -> p.s == dominant).toList();
		int next = rnd.nextInt(here.size());
		for (Kind kind : KINDS.get(dominant)) {
			Spot p = here.get(next++ % here.size());
			int x = p.x + rnd.nextInt(41) - 20, z = p.z + rnd.nextInt(41) - 20;
			List<Deposit> made = kind.make(g, rnd, x, z);
			double k = SCALE.getOrDefault(dominant + "/" + kind.name, SCALE.getOrDefault(kind.name, 1.0));
			for (Deposit d : made) scale(d, k);
			out.addAll(made);
			if (!made.isEmpty()) satellites(g, rnd, made.get(0), kind, out);
		}
		return out;
	}

	/**
	 * Footprint scale of each kind (setting/kind or kind), tuned with {@link #survey} so that each mineral's total
	 * per area comes close to the default world's (measured 2026-11-04, seed 123321).
	 */
	static final java.util.Map<String, Double> SCALE = new java.util.HashMap<>();

	/** Widens a deposit's footprint by k (k squared in area), but no wider than 100 blocks. */
	static void scale(Deposit d, double k) {
		if (k == 1) return;
		double cap = d.shape == Shape.POD ? 8 : 50;
		d.ra = Math.min(cap, d.ra * k);
		if (d.shape != Shape.DIKE) d.rc = Math.min(cap, d.rc * k);
		d.bounds(d.minY, d.maxY);
	}

	interface Maker {
		List<Deposit> make(Geology g, Random rnd, int x, int z);
	}

	/** One kind of deposit: how to make the big one, its satellites' minerals and how many satellites. */
	record Kind(String name, Maker maker, Zone[] satelliteZones, int satMin, int satMax) {
		List<Deposit> make(Geology g, Random rnd, int x, int z) {
			return maker.make(g, rnd, x, z);
		}
	}

	private static final java.util.Map<Setting, List<Kind>> KINDS = new java.util.EnumMap<>(Setting.class);

	static {
		KINDS.put(Setting.CRATON, List.of(
				new Kind("banded_iron", Deposits::bandedIron, BIF, 10, 16),
				new Kind("pegmatite", (g, r, x, z) -> dikeField(g, r, x, z, PEGMATITE, 10, 14, WHITE), PEGMATITE, 8, 12),
				new Kind("pegmatite", (g, r, x, z) -> dikeField(g, r, x, z, PEGMATITE, 10, 14, WHITE), PEGMATITE, 8, 12),
				new Kind("quartz_gold", (g, r, x, z) -> dikeField(g, r, x, z, QUARTZ, 6, 9, RED), QUARTZ, 6, 10),
				new Kind("greenstone_copper", (g, r, x, z) -> body(g, r, x, z, GREENSTONE, 40, 50, 22, 30, 0.55, 20, 45, RED), COPPER, 12, 18),
				new Kind("coal_pockets", Deposits::coalPockets, COAL, 0, 0),
				new Kind("coal_pockets", Deposits::coalPockets, COAL, 0, 0),
				new Kind("kimberlite", Deposits::kimberlite, KIMBERLITE, 0, 0),
				new Kind("kimberlite", Deposits::kimberlite, KIMBERLITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0)));
		KINDS.put(Setting.MOUNTAIN, List.of(
				new Kind("porphyry", (g, r, x, z) -> body(g, r, x, z, PORPHYRY, 40, 50, 28, 36, 0.55, 25, 60, RED), COPPER, 12, 18),
				new Kind("skarn", (g, r, x, z) -> body(g, r, x, z, SKARN, 28, 36, 18, 24, 0.65, 20, 45, RED), IRON, 10, 16),
				new Kind("quartz_veins", (g, r, x, z) -> dikeField(g, r, x, z, QUARTZ, 8, 12, RED), QUARTZ, 6, 10),
				new Kind("pegmatite", (g, r, x, z) -> dikeField(g, r, x, z, PEGMATITE, 10, 14, WHITE), PEGMATITE, 8, 12),
				new Kind("folded_coal", Deposits::foldedCoal, COAL, 10, 16),
				new Kind("folded_coal", Deposits::foldedCoal, COAL, 10, 16),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0)));
		KINDS.put(Setting.BASIN, List.of(
				new Kind("coal_seams", (g, r, x, z) -> basinSeams(g, r, x, z, COAL, new int[]{1, 4}, 0, 4.5, 5, 0.9, 1, null), COAL, 10, 16),
				new Kind("coal_seams", (g, r, x, z) -> basinSeams(g, r, x, z, COAL, new int[]{1, 4}, 0, 4.5, 5, 0.9, 1, null), COAL, 10, 16),
				new Kind("coal_seams", (g, r, x, z) -> basinSeams(g, r, x, z, COAL, new int[]{1, 4}, 0, 4.5, 5, 0.9, 1, null), COAL, 10, 16),
				new Kind("oil_shale", (g, r, x, z) -> basinSeams(g, r, x, z, OIL_SHALE, new int[]{4, 1}, 0, 5, 2, 0.75, 1, null), OIL_SHALE, 0, 0),
				new Kind("oil_shale", (g, r, x, z) -> basinSeams(g, r, x, z, OIL_SHALE, new int[]{4, 1}, 0, 5, 2, 0.75, 1, null), OIL_SHALE, 0, 0),
				new Kind("oil_shale", (g, r, x, z) -> basinSeams(g, r, x, z, OIL_SHALE, new int[]{4, 1}, 0, 5, 2, 0.75, 1, null), OIL_SHALE, 0, 0),
				new Kind("oolitic_iron", (g, r, x, z) -> basinSeams(g, r, x, z, IRON, new int[]{0, 3, 2}, 0.5, 4.5, 3, 0.65, 1, RED), IRON, 10, 16),
				new Kind("red_bed_copper", (g, r, x, z) -> basinSeams(g, r, x, z, COPPER, new int[]{0, 1, 3}, 0.5, 3.5, 4, 0.75, 0.6, RED), COPPER, 10, 16),
				new Kind("mvt", (g, r, x, z) -> basinSeams(g, r, x, z, MVT, new int[]{2, 5}, 0, 5, 3, 0.8, 0.85, RED), MVT, 10, 16),
				new Kind("mvt", (g, r, x, z) -> basinSeams(g, r, x, z, MVT, new int[]{2, 5}, 0, 5, 3, 0.8, 0.85, RED), MVT, 10, 16),
				new Kind("mvt", (g, r, x, z) -> basinSeams(g, r, x, z, MVT, new int[]{2, 5}, 0, 5, 3, 0.8, 0.85, RED), MVT, 10, 16),
				new Kind("evaporites", Deposits::evaporites, EVAPORITE, 6, 10),
				new Kind("evaporites", Deposits::evaporites, EVAPORITE, 6, 10),
				new Kind("evaporites", Deposits::evaporites, EVAPORITE, 6, 10),
				new Kind("evaporites", Deposits::evaporites, EVAPORITE, 6, 10),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0)));
		KINDS.put(Setting.VOLCANIC, List.of(
				new Kind("porphyry", Deposits::deepPorphyry, COPPER, 0, 0),
				new Kind("magnetite", (g, r, x, z) -> flows(g, r, x, z, IRON, 1, 4, 3, 0.65, 1, RED), IRON, 10, 16),
				new Kind("native_copper", (g, r, x, z) -> flows(g, r, x, z, COPPER, -3, -1, 4, 0.7, 0.7, RED), COPPER, 10, 16),
				new Kind("lignite", (g, r, x, z) -> flows(g, r, x, z, COAL, -1, 1, 4, 0.9, 1, null), COAL, 10, 16),
				new Kind("lignite", (g, r, x, z) -> flows(g, r, x, z, COAL, -1, 1, 4, 0.9, 1, null), COAL, 10, 16),
				new Kind("magmatic", Deposits::magmatic, MAGMATIC, 8, 12),
				new Kind("magmatic", Deposits::magmatic, MAGMATIC, 8, 12),
				new Kind("magmatic", Deposits::magmatic, MAGMATIC, 8, 12),
				new Kind("fumarole", Deposits::fumarole, SULFUR, 0, 0),
				new Kind("fumarole", Deposits::fumarole, SULFUR, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0),
				new Kind("laterite", Deposits::laterite, LATERITE, 0, 0)));
	}

	// ---- makers ---------------------------------------------------------------------------------------

	private static double surface(Geology g, double x, double z) {
		return g.column((int) Math.floor(x), (int) Math.floor(z)).height;
	}

	private static Deposit base(Shape s, Zone[] zones, Random rnd, double x, double y, double z) {
		Deposit d = new Deposit();
		d.shape = s;
		d.zones = zones;
		d.cx = x;
		d.cy = y;
		d.cz = z;
		double a = rnd.nextDouble() * Math.PI;
		d.ax = Math.cos(a);
		d.az = Math.sin(a);
		d.seed = rnd.nextLong();
		return d;
	}

	/** Banded iron: a folded layer 5-7 thick and 70-100 across, at depth in the craton. */
	static List<Deposit> bandedIron(Geology g, Random rnd, int x, int z) {
		double top = surface(g, x, z);
		Deposit d = base(Shape.FOLDED_LAYER, BIF, rnd, x, Math.min(top - 30, 8 + rnd.nextInt(30)), z);
		d.ra = 44 + rnd.nextInt(7);
		d.rc = 36 + rnd.nextInt(9);
		d.ry = 5.5 + 1.5 * rnd.nextDouble();
		d.amp = 6 + rnd.nextInt(8);
		d.wave = 30 + rnd.nextInt(30);
		d.fill = 0.6;
		d.hint = RED;
		d.bounds((int) Math.floor(d.cy - d.amp - d.ry - 1), (int) Math.ceil(d.cy + d.amp + d.ry + 1));
		return List.of(d);
	}

	/** A field of steep dikes around a point (pegmatites or quartz veins). */
	static List<Deposit> dikeField(Geology g, Random rnd, int x, int z, Zone[] zones, int nMin, int nMax, BlockState hint) {
		List<Deposit> out = new ArrayList<>();
		int n = nMin + rnd.nextInt(nMax - nMin + 1);
		double dir = rnd.nextDouble() * Math.PI;
		for (int i = 0; i < n; i++) {
			double px = x + rnd.nextGaussian() * 28, pz = z + rnd.nextGaussian() * 28;
			double top = surface(g, px, pz);
			Deposit d = base(Shape.DIKE, zones, rnd, px, top - 10 - rnd.nextInt(40), pz);
			double a = dir + rnd.nextGaussian() * 0.3; // dikes of one field run roughly parallel
			d.ax = Math.cos(a);
			d.az = Math.sin(a);
			d.ra = 15 + rnd.nextInt(16);
			d.ry = 12 + rnd.nextInt(11);
			d.rc = 1.5 + rnd.nextDouble();
			d.fill = 0.55;
			d.hint = hint;
			d.bounds((int) Math.floor(d.cy - d.ry), (int) Math.ceil(d.cy + d.ry));
			out.add(d);
		}
		return out;
	}

	/** An ellipsoid body: half axes along ra, across rc, vertical ry; centre dMin..dMax below the surface. */
	static List<Deposit> body(Geology g, Random rnd, int x, int z, Zone[] zones, int raMin, int raMax, int ryMin, int ryMax,
			double fill, int dMin, int dMax, BlockState hint) {
		double top = surface(g, x, z);
		Deposit d = base(Shape.BODY, zones, rnd, x, top - dMin - rnd.nextInt(dMax - dMin + 1), z);
		d.ra = raMin + rnd.nextInt(raMax - raMin + 1);
		d.rc = d.ra * (0.7 + 0.3 * rnd.nextDouble());
		d.ry = ryMin + rnd.nextInt(ryMax - ryMin + 1);
		d.fill = fill;
		d.hint = hint;
		d.bounds((int) Math.floor(d.cy - d.ry), (int) Math.ceil(d.cy + d.ry));
		return List.of(d);
	}

	/** Coal in the craton: a cluster of pockets in stone (the craton's only coal, small). */
	static List<Deposit> coalPockets(Geology g, Random rnd, int x, int z) {
		List<Deposit> out = new ArrayList<>();
		int n = 30 + rnd.nextInt(12);
		for (int i = 0; i < n; i++) {
			double px = x + rnd.nextGaussian() * 30, pz = z + rnd.nextGaussian() * 30;
			out.add(pod(g, rnd, px, pz, COAL, surface(g, px, pz) - 6 - rnd.nextInt(40), 4 + rnd.nextDouble() * 2.5, null));
		}
		return out;
	}

	static Deposit pod(Geology g, Random rnd, double x, double z, Zone[] zones, double y, double r, BlockState hint) {
		Deposit d = base(Shape.POD, zones, rnd, x, y, z);
		d.ra = r * (0.8 + 0.5 * rnd.nextDouble());
		d.rc = r * (0.6 + 0.4 * rnd.nextDouble());
		d.ry = r * (0.5 + 0.4 * rnd.nextDouble());
		d.fill = 0.75;
		d.hint = hint;
		d.bounds((int) Math.floor(d.cy - d.ry), (int) Math.ceil(d.cy + d.ry));
		return d;
	}

	/** Seams in the basin layers: bands are SEDIMENTS indices mod 6 (0, 3 sandstone; 1, 4 terracotta; 2, 5 calcite). */
	static List<Deposit> basinSeams(Geology g, Random rnd, int x, int z, Zone[] zones, int[] residues, double f0, double f1,
			int seams, double fill, double patch, BlockState hint) {
		List<Deposit> out = new ArrayList<>();
		double top = surface(g, x, z), floor = g.sedimentFloor(x, z), shift = g.layerShift(x, z);
		if (top - floor < 12) return out;
		int lo = (int) Math.ceil((floor + 2 + shift) / 5), hi = (int) Math.floor((top - 6 + shift) / 5);
		List<Integer> bands = new ArrayList<>();
		for (int k = lo; k <= hi; k++) {
			for (int r : residues) if (Math.floorMod(k, 6) == r) bands.add(k);
		}
		if (bands.isEmpty()) return out;
		java.util.Collections.shuffle(bands, rnd);
		for (int i = 0; i < Math.min(seams, bands.size()); i++) {
			Deposit d = base(Shape.BASIN_STRATUM, zones, rnd, x + rnd.nextInt(31) - 15, bands.get(i) * 5, z + rnd.nextInt(31) - 15);
			d.band = bands.get(i);
			d.f0 = f0;
			d.f1 = f1;
			d.ra = 44 + rnd.nextInt(7);
			d.rc = 36 + rnd.nextInt(9);
			d.fill = fill;
			d.patch = patch;
			d.hint = hint;
			d.bounds(d.band * 5 - 8, d.band * 5 + 13);
			out.add(d);
		}
		return out;
	}

	/** Coal in the tuff band of a mountain belt's folds (FOLDED index 4). */
	static List<Deposit> foldedCoal(Geology g, Random rnd, int x, int z) {
		List<Deposit> out = new ArrayList<>();
		double top = surface(g, x, z), off = g.foldOffset(x, z);
		int lo = (int) Math.ceil((top - 60 + off) / 5), hi = (int) Math.floor((top - 4 + off) / 5);
		List<Integer> bands = new ArrayList<>();
		for (int k = lo; k <= hi; k++) if (Math.floorMod(k, 6) == 4) bands.add(k);
		java.util.Collections.shuffle(bands, rnd);
		for (int i = 0; i < Math.min(3, bands.size()); i++) {
			Deposit d = base(Shape.FOLDED_STRATUM, COAL, rnd, x, bands.get(i) * 5 - off, z);
			d.band = bands.get(i);
			d.f0 = 0;
			d.f1 = 5;
			d.ra = 44 + rnd.nextInt(7);
			d.rc = 36 + rnd.nextInt(9);
			d.fill = 0.85;
			d.bounds(Integer.MIN_VALUE, Integer.MAX_VALUE); // per column
			out.add(d);
		}
		return out;
	}

	/** Levels between basalt flows: tuff layers lie at y = 70 + 8k; dy0..dy1 are offsets from a tuff layer. */
	static List<Deposit> flows(Geology g, Random rnd, int x, int z, Zone[] zones, int dy0, int dy1, int levels, double fill,
			double patch, BlockState hint) {
		List<Deposit> out = new ArrayList<>();
		double top = surface(g, x, z), floor = g.sedimentFloor(x, z);
		List<Integer> tuffs = new ArrayList<>();
		for (int t = 70 + 8 * (int) Math.ceil((floor + 4 - 70) / 8.0); t <= top - 5; t += 8) tuffs.add(t);
		java.util.Collections.shuffle(tuffs, rnd);
		for (int i = 0; i < Math.min(levels, tuffs.size()); i++) {
			int t = tuffs.get(i);
			Deposit d = base(Shape.FLAT, zones, rnd, x + rnd.nextInt(31) - 15, t, z + rnd.nextInt(31) - 15);
			d.f0 = t + dy0;
			d.f1 = t + dy1;
			d.ra = 44 + rnd.nextInt(7);
			d.rc = 36 + rnd.nextInt(9);
			d.fill = fill;
			d.patch = patch;
			d.hint = hint;
			d.bounds(t + dy0, t + dy1);
			out.add(d);
		}
		return out;
	}

	/** Under the basalt plateaus the porphyry sits in the granite below the flows, its top reaching into them. */
	static List<Deposit> deepPorphyry(Geology g, Random rnd, int x, int z) {
		double floor = g.sedimentFloor(x, z);
		Deposit d = base(Shape.BODY, PORPHYRY, rnd, x, floor - 6 - rnd.nextInt(14), z);
		d.ra = 40 + rnd.nextInt(11);
		d.rc = d.ra * (0.7 + 0.3 * rnd.nextDouble());
		d.ry = 25 + rnd.nextInt(6);
		d.fill = 0.55;
		d.hint = RED;
		d.bounds((int) Math.floor(d.cy - d.ry), (int) Math.ceil(d.cy + d.ry));
		return List.of(d);
	}

	/** Evaporites under dry basins: saltpetre, borax and sulphur in the top layers (none where the climate is wet). */
	static List<Deposit> evaporites(Geology g, Random rnd, int x, int z) {
		if (!dry(g, x, z)) return List.of();
		List<Deposit> out = new ArrayList<>();
		double top = surface(g, x, z), shift = g.layerShift(x, z);
		int hi = (int) Math.floor((top - 3 + shift) / 5), lo = Math.max(hi - 6, (int) Math.ceil((g.sedimentFloor(x, z) + 2 + shift) / 5));
		for (int k = hi; k >= lo && out.size() < 5; k--) {
			Deposit d = base(Shape.BASIN_STRATUM, EVAPORITE, rnd, x + rnd.nextInt(31) - 15, k * 5, z + rnd.nextInt(31) - 15);
			d.band = k;
			d.f0 = 0;
			d.f1 = 5;
			d.ra = 44 + rnd.nextInt(7);
			d.rc = 36 + rnd.nextInt(9);
			d.fill = 0.95;
			d.bounds(k * 5 - 8, k * 5 + 13);
			out.add(d);
		}
		return out;
	}

	/** Chromite and pentlandite in thin layers at the base of the basalt flows (until stage 4 brings peridotite). */
	static List<Deposit> magmatic(Geology g, Random rnd, int x, int z) {
		double floor = g.sedimentFloor(x, z);
		Deposit d = base(Shape.FLAT, MAGMATIC, rnd, x, floor, z);
		// 4a: in the peridotite under the basalt (GeoWorld puts 14 blocks of it below the flows)
		d.f0 = Math.floor(floor) - 9;
		d.f1 = Math.floor(floor) - 1;
		d.ra = 44 + rnd.nextInt(7);
		d.rc = 36 + rnd.nextInt(9);
		d.fill = 0.75;
		d.bounds((int) d.f0, (int) d.f1);
		return List.of(d);
	}

	/** Sulphur in the chimney of a volcanic neck near the spot, up to the surface (none without a neck). */
	static List<Deposit> fumarole(Geology g, Random rnd, int x, int z) {
		for (int r = 0; r <= 120; r += 8) {
			for (int a = 0; a < Math.max(1, r / 4); a++) {
				double t = 2 * Math.PI * a / Math.max(1, r / 4);
				int px = x + (int) (r * Math.cos(t)), pz = z + (int) (r * Math.sin(t));
				Geology.Column cc = g.column(px, pz);
				if (!cc.neck && cc.craterLake == Integer.MIN_VALUE) continue; // a neck, or a crater (4b)
				double top = surface(g, px, pz);
				Deposit d = base(Shape.PIPE, SULFUR, rnd, px, top, pz);
				d.ra = d.rc = 6 + rnd.nextInt(4);
				d.fill = 0.7;
				d.bounds((int) top - 60, (int) Math.ceil(top) + 2);
				return List.of(d);
			}
		}
		return List.of();
	}

	/** A kimberlite pipe: a narrow column of tuff (standing in for kimberlite breccia) with diamonds, from the deep up to near the surface. */
	static List<Deposit> kimberlite(Geology g, Random rnd, int x, int z) {
		double top = surface(g, x, z);
		Deposit d = base(Shape.PIPE, KIMBERLITE, rnd, x, top, z);
		d.ra = d.rc = 4 + rnd.nextInt(3);
		d.fill = 0.25;
		d.rock = com.periodicelements.GeoRocks.KIMBERLITE.getDefaultState();
		d.bounds(-60, (int) top - 4);
		return List.of(d);
	}

	/** Laterite: a red crust under the soil of the hot, wet tropics, with bauxite and manganese (none elsewhere). */
	static List<Deposit> laterite(Geology g, Random rnd, int x, int z) {
		if (!humidTropics(g, x, z)) return List.of();
		Deposit d = base(Shape.CRUST, LATERITE, rnd, x, surface(g, x, z), z);
		d.ra = 44 + rnd.nextInt(7);
		d.rc = 36 + rnd.nextInt(9);
		d.fill = 0.5;
		d.rock = com.periodicelements.GeoRocks.LATERITE.getDefaultState();
		d.bounds(-64, 320);
		return List.of(d);
	}

	/** Small pods around a big deposit, some reaching just under the ground (outcrops). */
	static void satellites(Geology g, Random rnd, Deposit main, Kind kind, List<Deposit> out) {
		if (kind.satMax == 0) return;
		int n = kind.satMin + rnd.nextInt(kind.satMax - kind.satMin + 1);
		for (int i = 0; i < n; i++) {
			double a = rnd.nextDouble() * 2 * Math.PI, dist = 30 + rnd.nextDouble() * 90;
			double x = main.cx + Math.cos(a) * dist, z = main.cz + Math.sin(a) * dist;
			double top = surface(g, x, z);
			double y = rnd.nextBoolean() ? top - 3 - rnd.nextInt(10) : Math.min(top - 4, main.cy + rnd.nextGaussian() * 12);
			out.add(pod(g, rnd, x, z, kind.satelliteZones, y, 3 + rnd.nextDouble() * 2.5, main.hint));
		}
	}

	// ---- placing --------------------------------------------------------------------------------------

	/** Called after the carvers: puts the deposits that reach into this chunk, and their hints. */
	public static void place(Chunk chunk, NoiseConfig config) {
		Geology g = GeoWorld.geology(config);
		useClimate(g, config);
		ChunkPos cp = chunk.getPos();
		int x0 = cp.getStartX(), z0 = cp.getStartZ(), x1 = x0 + 15, z1 = z0 + 15;
		int rx = Math.floorDiv(x0, REGION), rz = Math.floorDiv(z0, REGION);
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int bottom = chunk.getBottomY(), topY = chunk.getTopY() - 1;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				for (Deposit d : region(g, rx + dx, rz + dz)) {
					if (d.maxX < x0 || d.minX > x1 || d.maxZ < z0 || d.minZ > z1) continue;
					fill(chunk, g, d, Math.max(x0, d.minX), Math.min(x1, d.maxX), Math.max(z0, d.minZ), Math.min(z1, d.maxZ),
							bottom, topY, pos);
				}
			}
		}
		placers(chunk, g, x0, z0, rx, rz, pos);
	}

	// ---- placers (3b) ---------------------------------------------------------------------------------

	/** Heavy minerals that rivers carry from their deposits and leave in the gravel of the bed. */
	static final String[] PLACER = {"gold", "cassiterite", "coltan", "diamond"};
	/** Chance that a bed block right next to a source turns to ore gravel; it falls off downstream (here: with distance). */
	static final double PLACER_RATE = 0.10, PLACER_FADE = 140, PLACER_REACH = 320;

	private static void placers(Chunk chunk, Geology g, int x0, int z0, int rx, int rz, BlockPos.Mutable pos) {
		double mx = x0 + 8, mz = z0 + 8;
		double[] p = new double[PLACER.length];
		boolean any = false;
		for (int i = 0; i < PLACER.length; i++) {
			double best = PLACER_REACH;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					for (Deposit d : region(g, rx + dx, rz + dz)) {
						if (!holds(d, PLACER[i])) continue;
						best = Math.min(best, Math.hypot(d.cx - mx, d.cz - mz));
					}
				}
			}
			if (best < PLACER_REACH) {
				p[i] = PLACER_RATE * Math.exp(-best / PLACER_FADE);
				any = true;
			}
		}
		if (!any) return;
		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				int x = x0 + lx, z = z0 + lz;
				Geology.Column col = g.column(x, z);
				if (!col.river || col.riverWater == Integer.MIN_VALUE) continue;
				int top = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, lx, lz);
				double u = unit(g.seed + 11, x, 0, z);
				for (int i = 0; i < PLACER.length; i++) {
					u -= p[i];
					if (u >= 0) continue;
					for (int y = top; y > top - 2; y--) {
						BlockState bed = chunk.getBlockState(pos.set(x, y, z));
						if (!bed.isOf(Blocks.GRAVEL) && !bed.isOf(Blocks.SAND)) break;
						BlockState ore = GeoOres.ore(PLACER[i], Blocks.GRAVEL.getDefaultState());
						if (ore != null) chunk.setBlockState(pos, ore, false);
					}
					break;
				}
			}
		}
	}

	/** Distance from (x, z) to the nearest deposit centre holding the mineral, in this and the 8 regions around (for photo spots). */
	public static double nearestWith(Geology g, int x, int z, String mineral) {
		int rx = Math.floorDiv(x, REGION), rz = Math.floorDiv(z, REGION);
		double best = Double.MAX_VALUE;
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			for (Deposit d : region(g, rx + dx, rz + dz)) if (holds(d, mineral)) best = Math.min(best, Math.hypot(d.cx - x, d.cz - z));
		}
		return best;
	}

	private static boolean holds(Deposit d, String mineral) {
		for (Zone z : d.zones) {
			for (String m : z.minerals) if (m.equals(mineral)) return true;
		}
		return false;
	}

	private static void fill(Chunk chunk, Geology g, Deposit d, int xa, int xb, int za, int zb, int bottom, int topY,
			BlockPos.Mutable pos) {
		for (int x = xa; x <= xb; x++) {
			for (int z = za; z <= zb; z++) {
				final int fx = x, fz = z;
				int crustTop = d.shape == Shape.CRUST ? belowSoil(chunk, x, z, pos) : 0;
				double rh2 = forEachOre(g, d, x, z, bottom + 1, topY, crustTop, (y, mineral) -> {
					pos.set(fx, y, fz);
					BlockState host = chunk.getBlockState(pos);
					if (!GeoOres.host(host)) return;
					if (d.rock != null) {
						BlockState ore = mineral == null ? null : GeoOres.ore(mineral, d.rock);
						chunk.setBlockState(pos, ore != null ? ore : d.rock, false);
						return;
					}
					BlockState ore = GeoOres.ore(mineral, host);
					if (ore != null) chunk.setBlockState(pos, ore, false);
				});
				if (d.hint != null && rh2 < 1) hint(chunk, g, d, x, z, rh2, pos);
			}
		}
	}

	/** Receives each ore block of a column; mineral is null for a block of the deposit's own rock without ore. */
	interface OreSink {
		void ore(int y, String mineral);
	}

	private static final Set<Block> SOIL = Set.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
			Blocks.ROOTED_DIRT, Blocks.MUD, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SNOW_BLOCK, Blocks.CLAY);

	/** The first block under the soil of a column (the top of the rock), or the ground itself under water. */
	private static int belowSoil(Chunk chunk, int x, int z, BlockPos.Mutable pos) {
		int y = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x & 15, z & 15);
		int stop = y - 8;
		while (y > stop && SOIL.contains(chunk.getBlockState(pos.set(x, y, z)).getBlock())) y--;
		return y;
	}

	/** Every block of a deposit's column (x, z) between yMin and yMax that holds ore; returns the footprint's r squared. */
	static double forEachOre(Geology g, Deposit d, int x, int z, int yMin, int yMax, int crustTop, OreSink sink) {
		double ox = x + 0.5 - d.cx, oz = z + 0.5 - d.cz;
		double along = ox * d.ax + oz * d.az, across = -ox * d.az + oz * d.ax;
		double rh2 = (along / d.ra) * (along / d.ra) + (across / d.rc) * (across / d.rc);
		int ylo, yhi;
		double colOff = 0;
		switch (d.shape) {
			case DIKE -> {
				if (Math.abs(along) > d.ra || Math.abs(across) > d.rc * (0.6 + 0.8 * unit(d.seed, x >> 2, 0, z >> 2))) return 9;
				ylo = d.minY;
				yhi = d.maxY;
				rh2 = (along / d.ra) * (along / d.ra);
			}
			case FOLDED_LAYER -> {
				if (rh2 >= 1) return rh2;
				double mid = d.cy + d.amp * Math.sin(2 * Math.PI * along / d.wave) + 2 * Math.sin(across / 9.0);
				ylo = (int) Math.floor(mid - d.ry);
				yhi = (int) Math.ceil(mid + d.ry);
				colOff = mid;
			}
			case BASIN_STRATUM -> {
				if (rh2 >= 1 || !patchy(d, x, z)) return 9;
				double shift = g.layerShift(x, z);
				ylo = (int) Math.ceil(d.band * 5 + d.f0 - shift);
				yhi = (int) Math.ceil(d.band * 5 + d.f1 - shift) - 1;
			}
			case FOLDED_STRATUM -> {
				if (rh2 >= 1 || !patchy(d, x, z)) return 9;
				double off = g.foldOffset(x, z);
				ylo = (int) Math.ceil(d.band * 5 + d.f0 - off);
				yhi = (int) Math.ceil(d.band * 5 + d.f1 - off) - 1;
			}
			case FLAT -> {
				if (rh2 >= 1 || !patchy(d, x, z)) return 9;
				ylo = (int) d.f0;
				yhi = (int) d.f1;
			}
			case PIPE -> {
				if (rh2 >= 1) return rh2;
				ylo = d.minY;
				yhi = d.maxY;
			}
			case CRUST -> {
				if (rh2 >= 1) return rh2;
				if (g.column(x, z).cone && g.column(x, z).coneFrac > 0.35) return 1; // no red earth on the bare upper flanks of a volcano (4b); the wide lower apron weathers like the plain (3.44)
				double t = 3.6 + 6 * (1 - rh2) * (0.6 + 0.4 * unit(d.seed + 7, Math.floorDiv(x, 4), 0, Math.floorDiv(z, 4)));
				yhi = crustTop;
				ylo = crustTop - (int) t + 1;
			}
			default -> {
				if (rh2 >= 1) return rh2;
				double h = d.ry * Math.sqrt(1 - rh2);
				ylo = (int) Math.floor(d.cy - h);
				yhi = (int) Math.ceil(d.cy + h);
			}
		}
		ylo = Math.max(ylo, yMin);
		yhi = Math.min(yhi, yMax);
		for (int y = ylo; y <= yhi; y++) {
			double r;
			switch (d.shape) {
				case BODY, POD -> {
					double dy = (y + 0.5 - d.cy) / d.ry;
					r = Math.sqrt(rh2 + dy * dy);
				}
				case DIKE -> {
					double dy = (y + 0.5 - d.cy) / d.ry, da = along / d.ra;
					r = Math.sqrt(da * da + dy * dy);
				}
				case FOLDED_LAYER -> r = Math.max(Math.sqrt(rh2), Math.abs(y + 0.5 - colOff) / (d.ry + 0.5));
				default -> r = Math.sqrt(rh2);
			}
			if (r >= 1) continue;
			if (d.shape == Shape.PIPE || d.shape == Shape.CRUST) {
				String m = unit(d.seed + 2, x, y, z) < d.fill ? kept(d, mineral(d, r, x, y, z), x, y, z) : null;
				if (m != null || d.rock != null) sink.ore(y, m);
				continue;
			}
			double p = switch (d.shape) {
				case BODY -> d.fill * (1.25 - r * r);
				case POD -> d.fill * (1 - 0.6 * r * r);
				default -> d.fill * (1 - Math.pow(r, 6));
			};
			// ore comes in clumps: a coarse 2-block cell first, then the block itself
			double c = 0.65;
			if (unit(d.seed + 1, x >> 1, y >> 1, z >> 1) >= c || unit(d.seed + 2, x, y, z) >= p / c) continue;
			String m = kept(d, mineral(d, r, x, y, z), x, y, z);
			if (m != null || d.rock != null) sink.ore(y, m);
		}
		return rh2;
	}

	/**
	 * Stage 3c: the share of each mineral's chosen blocks that stays ore (the rest stays rock). Tuned with
	 * {@link #survey} so each total sits halfway (geometric mean) between the default world and geological reality.
	 */
	static final java.util.Map<String, Double> KEEP = java.util.Map.ofEntries(
			java.util.Map.entry("gold", 0.30), java.util.Map.entry("arsenopyrite", 0.78), java.util.Map.entry("cassiterite", 0.71),
			java.util.Map.entry("coltan", 0.76), java.util.Map.entry("molybdenite", 1.0), java.util.Map.entry("pitchblende", 0.75),
			java.util.Map.entry("spodumene", 0.75), java.util.Map.entry("fluorite", 0.78), java.util.Map.entry("copper", 0.79),
			java.util.Map.entry("barite", 0.80), java.util.Map.entry("vanadinite", 0.82), java.util.Map.entry("galena", 0.88),
			java.util.Map.entry("stibnite", 1.0), java.util.Map.entry("wolframite", 0.91),
			java.util.Map.entry("borax", 0.75), java.util.Map.entry("cobaltite", 0.87), java.util.Map.entry("pyrolusite", 0.9),
			java.util.Map.entry("chromite", 0.57), java.util.Map.entry("pentlandite", 0.56));

	/** Patchy layers: ore only in some 8x8 cells of the footprint. */
	private static boolean patchy(Deposit d, int x, int z) {
		return d.patch >= 1 || unit(d.seed + 3, Math.floorDiv(x, 8), 0, Math.floorDiv(z, 8)) < d.patch;
	}

	private static String kept(Deposit d, String m, int x, int y, int z) {
		Double k = KEEP.get(m);
		return k == null || unit(d.seed + 9, x, y, z) < k ? m : null;
	}

	private static String mineral(Deposit d, double r, int x, int y, int z) {
		Zone zone = d.zones[d.zones.length - 1];
		for (Zone zz : d.zones) {
			if (r <= zz.rMax) {
				zone = zz;
				break;
			}
		}
		if (zone.minerals.length == 1) return zone.minerals[0];
		// minerals come in clumps too: pick by a 3-block cell
		double u = unit(d.seed + 4, Math.floorDiv(x, 3), Math.floorDiv(y, 3), Math.floorDiv(z, 3));
		for (int i = 0; i < zone.minerals.length; i++) {
			u -= zone.weights[i];
			if (u < 0) return zone.minerals[i];
		}
		return zone.minerals[zone.minerals.length - 1];
	}

	/** A deposit whose top is close under the ground stains a few patches of the surface above it. */
	static final int HINT_DEPTH = 12;
	private static final Set<Block> GROUND = Set.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
			Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
			Blocks.TUFF, Blocks.BASALT, Blocks.SMOOTH_BASALT, Blocks.SANDSTONE, Blocks.TERRACOTTA, Blocks.BROWN_TERRACOTTA,
			Blocks.CALCITE);

	private static void hint(Chunk chunk, Geology g, Deposit d, int x, int z, double rh2, BlockPos.Mutable pos) {
		if (rh2 > 0.5) return;
		int lx = x & 15, lz = z & 15;
		int top = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, lx, lz);
		if (top != chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, lx, lz)) return; // under water
		int depositTop = switch (d.shape) {
			case BASIN_STRATUM -> (int) Math.ceil(d.band * 5 + d.f1 - g.layerShift(x, z));
			case FLAT -> (int) d.f1;
			default -> d.maxY;
		};
		if (top - depositTop > HINT_DEPTH) return;
		// discreet: some 3x3 patches, each partly stained
		if (unit(d.seed + 5, Math.floorDiv(x, 3), 0, Math.floorDiv(z, 3)) > 0.12) return;
		if (unit(d.seed + 6, x, 0, z) > 0.5) return;
		pos.set(x, top, z);
		if (GROUND.contains(chunk.getBlockState(pos).getBlock())) chunk.setBlockState(pos, d.hint, false);
	}

	/** A hash of a cell, uniform in [0, 1). */
	static double unit(long seed, int x, int y, int z) {
		long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
		h ^= h >>> 33;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 33;
		h *= 0xC4CEB9FE1A85EC53L;
		h ^= h >>> 33;
		return (h >>> 11) * 0x1.0p-53;
	}
}
