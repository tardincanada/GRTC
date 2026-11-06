package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.worldgen.GeoWorld;
import com.periodicelements.worldgen.Geology;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.WorldPresetTags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;

/** The Geological world type (2.6.0): registered, varied, and its terrain density agrees with its map. */
public class GeologyTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void geologicalPresetIsListed(TestContext ctx) {
		var presets = ctx.getWorld().getRegistryManager().get(RegistryKeys.WORLD_PRESET);
		var entry = presets.getEntry(Identifier.of(PeriodicElements.MOD_ID, "geological"));
		expect(ctx, entry.isPresent(), "world preset periodic_elements:geological missing");
		expect(ctx, entry.get().isIn(WorldPresetTags.NORMAL), "geological preset is not in the world type list");
		expect(ctx, ctx.getWorld().getRegistryManager().get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).contains(GeoWorld.SETTINGS),
				"noise settings periodic_elements:geological missing");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void geologyMapIsVaried(TestContext ctx) {
		Geology g = Geology.of(20261103L);
		int[] provinces = new int[Geology.Province.values().length];
		int land = 0, mountains = 0, domes = 0, necks = 0, outcrops = 0, n = 0;
		double max = 0;
		for (int x = -6000; x < 6000; x += 30) {
			for (int z = -6000; z < 6000; z += 30) {
				Geology.Column c = g.column(x, z);
				n++;
				provinces[c.province.ordinal()]++;
				max = Math.max(max, c.height);
				if (c.height > Geology.SEA) {
					land++;
					if (g.outcropZone(x, z)) outcrops++;
				}
				if (c.mountain > 0.6) mountains++;
				if (c.dome) domes++;
				if (c.neck) necks++;
			}
		}
		for (Geology.Province p : Geology.Province.values()) {
			expect(ctx, provinces[p.ordinal()] > n / 20, "too little " + p + ": " + provinces[p.ordinal()] + "/" + n);
		}
		double landShare = land / (double) n, outcropShare = outcrops / (double) land;
		expect(ctx, landShare > 0.45 && landShare < 0.85, "land share " + landShare);
		expect(ctx, max < 310 && max > 170, "max height " + max);
		expect(ctx, mountains > 0 && domes > 0 && necks > 0, "missing landforms m=" + mountains + " d=" + domes + " n=" + necks);
		expect(ctx, outcropShare > 0.01 && outcropShare < 0.08, "outcrops are meant to be rare: " + outcropShare);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void terrainFollowsTheMap(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		NoiseConfig config = NoiseConfig.create(rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).get(GeoWorld.SETTINGS),
				rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 42L);
		Geology g = GeoWorld.geology(config);
		DensityFunction initial = config.getNoiseRouter().initialDensityWithoutJaggedness();
		for (int i = 0; i < 40; i++) {
			int x = i * 517 - 9000, z = i * -389 + 4000;
			int h = (int) Math.floor(g.column(x, z).height);
			double below = initial.sample(new DensityFunction.UnblendedNoisePos(x, h - 2, z));
			double above = initial.sample(new DensityFunction.UnblendedNoisePos(x, h + 3, z));
			expect(ctx, below > 0.390625 && above < 0.390625,
					"surface at " + x + "," + z + " should be near y=" + h + " (densities " + below + ", " + above + ")");
		}
		ctx.complete();
	}

	private static final int SEA_TOP = 62; // highest water block

	/** Coastal plains sit a few blocks above the water, not flush with it, in the real generator. */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void coastalPlainsStayAboveTheSea(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		var settings = rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getEntry(GeoWorld.SETTINGS).orElseThrow();
		var preset = rm.get(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getEntry(net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElseThrow();
		var gen = new net.minecraft.world.gen.chunk.NoiseChunkGenerator(net.minecraft.world.biome.source.MultiNoiseBiomeSource.create(preset), settings);
		long seed = 4242L;
		NoiseConfig config = NoiseConfig.create(settings.value(), rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), seed);
		Geology g = GeoWorld.geology(config);
		var view = net.minecraft.world.HeightLimitView.create(-64, 384);
		// The coastal plain proper (above the beach) must stand at least one block clear of the water.
		int n = 0, low = 0;
		double from = Geology.SEA + 3, to = from + 4;
		for (int x = -3000; x < 3000; x += 37) for (int z = -3000; z < 3000; z += 37) {
			var c = g.column(x, z);
			if (c.macro < from || c.macro >= to) continue;
			int floor = gen.getHeight(x, z, net.minecraft.world.Heightmap.Type.OCEAN_FLOOR_WG, view, config);
			n++;
			if (floor - 1 < SEA_TOP + 2) low++; // top block level with or one above the water
		}
		PeriodicElements.LOGGER.info("[coast] plain columns {}, low {}", n, low);
		expect(ctx, n > 1000, "too few coastal plain columns: " + n);
		expect(ctx, low < n * 0.05, "coastal plain at water level in " + low + " of " + n + " columns");
		ctx.complete();
	}

	/** Open ground below sea level is flooded, so shallow sea floors never stay dry; covered hollows are left alone. */
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void dryGroundBelowTheSeaIsFlooded(TestContext ctx) {
		var world = ctx.getWorld();
		var chunk = new net.minecraft.world.chunk.ProtoChunk(new net.minecraft.util.math.ChunkPos(0, 0),
				net.minecraft.world.chunk.UpgradeData.NO_UPGRADE_DATA, world, world.getRegistryManager().get(RegistryKeys.BIOME), null);
		var pos = new net.minecraft.util.math.BlockPos.Mutable();
		for (int y = 40; y <= 58; y++) chunk.setBlockState(pos.set(0, y, 0), net.minecraft.block.Blocks.STONE.getDefaultState(), false);
		chunk.setBlockState(pos.set(0, 59, 0), net.minecraft.block.Blocks.GRASS_BLOCK.getDefaultState(), false);
		for (int y = 40; y <= 70; y++) chunk.setBlockState(pos.set(1, y, 0), net.minecraft.block.Blocks.STONE.getDefaultState(), false);
		for (int y = 50; y <= 55; y++) chunk.setBlockState(pos.set(1, y, 0), net.minecraft.block.Blocks.AIR.getDefaultState(), false);
		net.minecraft.world.Heightmap.populateHeightmaps(chunk, java.util.EnumSet.of(net.minecraft.world.Heightmap.Type.WORLD_SURFACE_WG));
		GeoWorld.flood(chunk, new net.minecraft.util.math.BlockPos.Mutable(), 0, 0, 0, 0);
		GeoWorld.flood(chunk, new net.minecraft.util.math.BlockPos.Mutable(), 1, 1, 0, 0);
		for (int y = 60; y < Geology.SEA; y++) {
			expect(ctx, chunk.getBlockState(pos.set(0, y, 0)).isOf(net.minecraft.block.Blocks.WATER), "no water at y=" + y);
		}
		expect(ctx, chunk.getBlockState(pos.set(0, Geology.SEA, 0)).isAir(), "water above sea level");
		expect(ctx, chunk.getBlockState(pos.set(0, 59, 0)).isOf(net.minecraft.block.Blocks.DIRT), "grass left under water");
		expect(ctx, chunk.getBlockState(pos.set(1, 52, 0)).isAir(), "a covered hollow was flooded");
		ctx.complete();
	}

	/** No thin lines of land at the water line out at sea: sea floor away from the beach stays clear of the surface. */
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void seaFloorKeepsClearOfTheWaterLine(TestContext ctx) {
		Geology g = Geology.of(123321L);
		int n = 0, flush = 0;
		for (int x = -6000; x < 6000; x += 23) for (int z = -6000; z < 6000; z += 23) {
			var c = g.column(x, z);
			if (c.macro >= Geology.SEA - 3) continue; // land, beach and shore
			n++;
			if (c.height > Geology.SEA - 1.5 && c.height < Geology.SEA + 1) flush++;
		}
		expect(ctx, n > 10000, "too few sea columns: " + n);
		expect(ctx, flush < n * 0.001, "sea floor at the water line in " + flush + " of " + n + " columns");
		ctx.complete();
	}

	/**
	 * In the real generator (aquifers included), shallow sea floor stays under water: aquifer barrier walls once
	 * stood up to the water line there, seen as thin lines of land across the sea (seed 123321, near -1108 -812).
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void shallowSeaHasNoWallsAtTheWaterLine(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		var settings = rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getEntry(GeoWorld.SETTINGS).orElseThrow();
		var preset = rm.get(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getEntry(net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElseThrow();
		var gen = new net.minecraft.world.gen.chunk.NoiseChunkGenerator(net.minecraft.world.biome.source.MultiNoiseBiomeSource.create(preset), settings);
		NoiseConfig config = NoiseConfig.create(settings.value(), rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 123321L);
		Geology g = GeoWorld.geology(config);
		var view = net.minecraft.world.HeightLimitView.create(-64, 384);
		int n = 0, walls = 0;
		for (int x = -1400; x < -900; x += 3) for (int z = -1000; z < -600; z += 3) {
			if (g.column(x, z).height >= Geology.SEA - 2) continue;
			n++;
			if (gen.getHeight(x, z, net.minecraft.world.Heightmap.Type.OCEAN_FLOOR_WG, view, config) > SEA_TOP) walls++;
		}
		PeriodicElements.LOGGER.info("[coast] shallow sea columns {}, solid at the water line {}", n, walls);
		expect(ctx, n > 2000, "too few sea columns: " + n);
		expect(ctx, walls < n * 0.002, "solid at the water line over the sea floor in " + walls + " of " + n + " columns");
		ctx.complete();
	}

	private static final java.util.Set<String> SNOWY = java.util.Set.of("snowy_slopes", "frozen_peaks", "jagged_peaks", "grove",
			"snowy_plains", "ice_spikes", "snowy_taiga", "snowy_beach", "frozen_river", "frozen_ocean", "deep_frozen_ocean");
	private static final java.util.Set<String> TROPICAL = java.util.Set.of("jungle", "bamboo_jungle", "sparse_jungle", "savanna",
			"savanna_plateau", "windswept_savanna", "mangrove_swamp", "desert", "badlands", "wooded_badlands", "eroded_badlands");

	/** Tropical and hot biomes never border snowy ones in the Geological world (Guilherme, 2026-11-03). */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 600)
	public void tropicsNeverBorderSnow(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		var preset = rm.get(RegistryKeys.WORLD_PRESET).get(Identifier.of(PeriodicElements.MOD_ID, "geological"));
		var source = preset.getOverworld().orElseThrow().chunkGenerator().getBiomeSource();
		var settings = rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).get(GeoWorld.SETTINGS);
		NoiseConfig config = NoiseConfig.create(settings, rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 123321L);
		Geology g = GeoWorld.geology(config);
		var sampler = config.getMultiNoiseSampler();
		int step = 48, size = 160;
		String[][] grid = new String[size][size];
		int temperateSnow = 0;
		GeoWorld.setActive(true);
		try {
		for (int i = 0; i < size; i++) for (int k = 0; k < size; k++) {
			int x = (i - size / 2) * step, z = (k - size / 2) * step;
			int y = (int) Math.max(Geology.SEA, g.column(x, z).height);
			grid[i][k] = source.getBiome(x >> 2, y >> 2, z >> 2, sampler).getKey().orElseThrow().getValue().getPath();
			float t = net.minecraft.world.biome.source.util.MultiNoiseUtil.toFloat(sampler.sample(x >> 2, y >> 2, z >> 2).temperatureNoise());
			if (t >= -0.15F && t < 0.2F && java.util.Set.of("snowy_slopes", "frozen_peaks", "jagged_peaks", "grove").contains(grid[i][k])) temperateSnow++;
		}
		} finally {
			GeoWorld.setActive(false);
		}
		int borders = 0;
		for (int i = 0; i + 1 < size; i++) for (int k = 0; k + 1 < size; k++) {
			for (String other : new String[]{grid[i + 1][k], grid[i][k + 1]}) {
				String a = grid[i][k];
				if (SNOWY.contains(a) && TROPICAL.contains(other) || TROPICAL.contains(a) && SNOWY.contains(other)) borders++;
			}
		}
		PeriodicElements.LOGGER.info("[climate] tropical/snowy borders {}, snowy mountains in the temperate band {}", borders, temperateSnow);
		expect(ctx, temperateSnow == 0, "snowy mountain biomes in the temperate band at " + temperateSnow + " points");
		expect(ctx, borders == 0, "tropical biomes border snowy ones in " + borders + " places");
		ctx.complete();
	}

	/** In the Geological world, biomes start cooling with height SNOW_LINE_RAISE blocks higher. */
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void snowLineIsHigher(TestContext ctx) {
		var biome = ctx.getWorld().getRegistryManager().get(RegistryKeys.BIOME).get(net.minecraft.world.biome.BiomeKeys.WINDSWEPT_HILLS);
		// windswept hills (0.2) snow above about y=120 in vanilla
		var high = new net.minecraft.util.math.BlockPos(1000, 130, 1000);
		try {
			GeoWorld.setActive(false);
			expect(ctx, !biome.doesNotSnow(high), "vanilla: no snow at y=130 in windswept hills");
			GeoWorld.setActive(true);
			var high2 = new net.minecraft.util.math.BlockPos(2000, 130, 2000);
			expect(ctx, biome.doesNotSnow(high2), "geological: snow already at y=130 in windswept hills");
			var higher = new net.minecraft.util.math.BlockPos(3000, 150, 3000);
			expect(ctx, !biome.doesNotSnow(higher), "geological: no snow at y=150 in windswept hills");
		} finally {
			GeoWorld.setActive(false);
		}
		ctx.complete();
	}

	/** Rivers (stage 2a): the same wherever exploring starts, water never runs uphill, and they reach the sea. */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 1200)
	public void riversAreDeterministicAndRunDownhill(TestContext ctx) {
		Geology a = Geology.fresh(123321L), b = Geology.fresh(123321L);
		int[][] points = new int[60][];
		for (int n = 0; n < points.length; n++) points[n] = new int[]{-3000 + (n * 997) % 6000, -3000 + (n * 1531) % 6000};
		int rivers = 0, mouths = 0;
		for (int[] p : points) a.column(p[0], p[1]);
		for (int n = points.length - 1; n >= 0; n--) b.column(points[n][0] + 3, points[n][1] - 5);
		for (int x = -3000; x < 3000; x += 7) {
			for (int z = 0; z < 60; z += 7) {
				var ca = a.column(x, z);
				var cb = b.column(x, z);
				expect(ctx, ca.height == cb.height && ca.riverWater == cb.riverWater,
						"river map depends on exploring order at " + x + "," + z + ": " + ca.height + " vs " + cb.height);
				if (ca.river) rivers++;
				if (ca.river && ca.riverWater <= Geology.SEA) mouths++;
			}
		}
		expect(ctx, a.riversRunDownhill() && b.riversRunDownhill(), "a river runs uphill");
		PeriodicElements.LOGGER.info("[rivers] river columns {}, at sea level {}", rivers, mouths);
		expect(ctx, rivers > 20, "too few river columns: " + rivers);
		ctx.complete();
	}

	/**
	 * Stage 2b karst, in the real generator: caves along the calcite layers of the basins, flooded below the sea's
	 * level and dry above it, vanilla's caves filled in the sediments, roofs kept under the ground.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 1200)
	public void karstCavesFollowTheLimestone(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		var settings = rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getEntry(GeoWorld.SETTINGS).orElseThrow();
		var preset = rm.get(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getEntry(net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElseThrow();
		var gen = new net.minecraft.world.gen.chunk.NoiseChunkGenerator(net.minecraft.world.biome.source.MultiNoiseBiomeSource.create(preset), settings);
		NoiseConfig config = NoiseConfig.create(settings.value(), rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 123321L);
		Geology g = GeoWorld.geology(config);
		var view = net.minecraft.world.HeightLimitView.create(-64, 384);
		int columns = 0, cave = 0, wetLow = 0, low = 0, dryHigh = 0, high = 0, calcite = 0, sediment = 0, filled = 0, roof = 0;
		for (int x = -4000; x < 4000 && columns < 400; x += 37) for (int z = -4000; z < 4000 && columns < 400; z += 41) {
			if (!g.karstAt(x, z)) continue;
			columns++;
			var col = gen.getColumnSample(x, z, view, config);
			double floor = g.sedimentFloor(x, z), h = g.column(x, z).height;
			for (int y = (int) Math.ceil(floor) + 1; y < h; y++) {
				double k = g.caves(x, y, z);
				var state = col.getState(y);
				if (k < -0.3) {
					cave++;
					if (g.rock(x, y, z) == Geology.Rock.CALCITE) calcite++;
					if (y >= h - 7) roof++;
					if (y <= Geology.SEA - 3) { low++; if (state.getFluidState().isOf(net.minecraft.fluid.Fluids.WATER)) wetLow++; }
					if (y >= Geology.SEA + 1) { high++; if (state.isAir()) dryHigh++; }
				} else if (k > 0.5 && y > floor + 3 && y < h - 2) {
					sediment++;
					if (!state.isAir() && state.getFluidState().isEmpty()) filled++;
				}
			}
		}
		PeriodicElements.LOGGER.info("[karst] columns {}, cave blocks {} (calcite {}), low {} wet {}, high {} dry {}, sediment {} solid {}, near roof {}",
				columns, cave, calcite, low, wetLow, high, dryHigh, sediment, filled, roof);
		expect(ctx, columns > 100, "too few karst columns: " + columns);
		expect(ctx, cave > columns * 2, "too few karst cave blocks: " + cave);
		expect(ctx, calcite > cave * 0.4, "karst caves leave the limestone: " + calcite + " of " + cave);
		expect(ctx, wetLow > low * 0.9, "karst caves below the sea's level are not flooded: " + wetLow + " of " + low);
		expect(ctx, dryHigh > high * 0.9, "karst caves above the sea's level are not dry: " + dryHigh + " of " + high);
		expect(ctx, filled > sediment * 0.995, "vanilla caves left in the basin sediments: " + (sediment - filled) + " of " + sediment);
		ctx.complete();
	}

	/**
	 * Stage 2c, in the real generator: lava tubes in the basalt flows (dry, in basalt), vanilla's caves filled in
	 * the basalt, and granite with far fewer caves than other rock.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 1200)
	public void cavesFollowTheRock(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		var settings = rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getEntry(GeoWorld.SETTINGS).orElseThrow();
		var preset = rm.get(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getEntry(net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElseThrow();
		var gen = new net.minecraft.world.gen.chunk.NoiseChunkGenerator(net.minecraft.world.biome.source.MultiNoiseBiomeSource.create(preset), settings);
		NoiseConfig config = NoiseConfig.create(settings.value(), rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 123321L);
		Geology g = GeoWorld.geology(config);
		var view = net.minecraft.world.HeightLimitView.create(-64, 384);
		java.util.List<Double> macros = new java.util.ArrayList<>();
		int basaltCols = 0, tube = 0, tubeAir = 0, tubeBasalt = 0, flows = 0, flowsSolid = 0;
		int granite = 0, graniteAir = 0, other = 0, otherAir = 0;
		for (int x = -5000; x < 5000; x += 37) for (int z = -5000; z < 5000; z += 41) {
			var c = g.column(x, z);
			boolean basalt = g.basaltAt(x, z);
			boolean craton = c.province == Geology.Province.CRATON && c.mountain < 0.3 && c.height > Geology.SEA + 2;
			if (!basalt && !craton) continue;
			if (basalt && basaltCols >= 2000 || craton && granite + other > 400000) continue;
			var col = gen.getColumnSample(x, z, view, config);
			if (basalt) {
				basaltCols++;
				macros.add(c.macro);
				double floor = g.sedimentFloor(x, z);
				for (int y = (int) Math.ceil(floor) + 3; y < c.height - 2; y++) {
					double k = g.caves(x, y, z);
					var state = col.getState(y);
					if (k < -0.3) {
						tube++;
						if (state.isAir()) tubeAir++;
						if (g.rock(x, y, z) == Geology.Rock.BASALT || g.rock(x, y, z) == Geology.Rock.TUFF) tubeBasalt++;
					} else if (k > 0.5) {
						flows++;
						if (!state.isAir() && state.getFluidState().isEmpty()) flowsSolid++;
					}
				}
			} else {
				for (int y = 1; y < c.height - 10; y++) {
					boolean air = col.getState(y).isAir() || !col.getState(y).getFluidState().isEmpty();
					if (g.rock(x, y, z) == Geology.Rock.GRANITE) { granite++; if (air) graniteAir++; }
					else { other++; if (air) otherAir++; }
				}
			}
		}
		java.util.Collections.sort(macros);
		if (!macros.isEmpty()) PeriodicElements.LOGGER.info("[caves] basalt macro p10 {} p50 {} p90 {}", macros.get(macros.size() / 10),
				macros.get(macros.size() / 2), macros.get(macros.size() * 9 / 10));
		double gf = graniteAir / (double) Math.max(1, granite), of = otherAir / (double) Math.max(1, other);
		PeriodicElements.LOGGER.info("[caves] basalt columns {}, tube blocks {} (air {}, basalt {}), flows {} solid {}; granite {} open {}%, other {} open {}%",
				basaltCols, tube, tubeAir, tubeBasalt, flows, flowsSolid, granite, String.format("%.2f", gf * 100), other, String.format("%.2f", of * 100));
		expect(ctx, basaltCols > 100, "too few basalt columns: " + basaltCols);
		expect(ctx, tube > basaltCols / 4, "too few lava tube blocks: " + tube);
		expect(ctx, tubeAir > tube * 0.97, "lava tubes not open: " + tubeAir + " of " + tube);
		expect(ctx, tubeBasalt > tube * 0.9, "lava tubes leave the basalt: " + tubeBasalt + " of " + tube);
		expect(ctx, flowsSolid > flows * 0.995, "vanilla caves left in the basalt: " + (flows - flowsSolid) + " of " + flows);
		expect(ctx, granite > 10000 && other > 10000, "too little rock sampled: " + granite + " / " + other);
		expect(ctx, gf < of * 0.5, "granite is not less cavernous: " + gf + " vs " + of);
		ctx.complete();
	}
}
