package com.periodicelements.test;

import com.periodicelements.worldgen.GeoOptions;
import com.periodicelements.worldgen.GeoWorld;
import com.periodicelements.worldgen.Geology;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** 3.49.0: the Geological world's options (biome size, provinces, volcanoes, ocean, rivers). */
public class GeoOptionsTests implements FabricGameTest {
	private static final long SEED = 123321L;

	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) ctx.throwGameTestException(message);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void optionsSurviveTheWorldFile(TestContext ctx) {
		GeoOptions o = GeoOptions.DEFAULT.with(GeoOptions.BiomeSize.HUGE, GeoOptions.Size.SMALL, GeoOptions.Amount.MANY,
				GeoOptions.Amount.FEW, GeoOptions.Amount.MANY);
		expect(ctx, GeoOptions.fromJson(GeoOptions.toJson(o)).equals(o), "options did not survive json");
		expect(ctx, GeoOptions.fromJson(GeoOptions.toJson(GeoOptions.LEGACY)).equals(GeoOptions.LEGACY), "legacy did not survive json");
		ctx.complete();
	}

	/** Normal options and worlds from before 3.49 have the very same terrain as 3.48 had. */
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void normalOptionsKeepTheTerrain(TestContext ctx) {
		Geology a = Geology.fresh(SEED, GeoOptions.DEFAULT), b = Geology.fresh(SEED, GeoOptions.LEGACY);
		for (int i = 0; i < 400; i++) {
			int x = i * 211 - 40000, z = i * -173 + 30000;
			expect(ctx, a.column(x, z).height == b.column(x, z).height, "height differs at " + x + "," + z);
		}
		ctx.complete();
	}

	private static double[] survey(Geology g) {
		int ocean = 0, cones = 0, rivers = 0, n = 0;
		for (int x = -5000; x < 5000; x += 40) {
			for (int z = -5000; z < 5000; z += 40) {
				Geology.Column c = g.column(x, z);
				n++;
				if (c.province == Geology.Province.OCEAN) ocean++;
				if (c.cone) cones++;
			}
		}
		for (int x = -3000; x < 3000; x += 4) { // a line across: how many river channels it crosses
			boolean was = false;
			for (int k = 0; k < 3; k++) {
				boolean r = g.column(x, -1500 + k * 1500).river;
				if (r && !was) rivers++;
				was = r;
			}
		}
		return new double[]{ocean / (double) n, cones / (double) n, rivers};
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 2400)
	public void optionsChangeTheMap(TestContext ctx) {
		GeoOptions.Amount F = GeoOptions.Amount.FEW, N = GeoOptions.Amount.NORMAL, M = GeoOptions.Amount.MANY;
		GeoOptions.BiomeSize B = GeoOptions.BiomeSize.NORMAL;
		GeoOptions.Size S = GeoOptions.Size.NORMAL;
		Geology base = Geology.fresh(SEED, GeoOptions.DEFAULT);
		double[] mid = survey(base);
		double[] oFew = survey(Geology.fresh(SEED, GeoOptions.DEFAULT.with(B, S, N, F, N))), oMany = survey(Geology.fresh(SEED, GeoOptions.DEFAULT.with(B, S, N, M, N)));
		double[] vFew = survey(Geology.fresh(SEED, GeoOptions.DEFAULT.with(B, S, F, N, N))), vMany = survey(Geology.fresh(SEED, GeoOptions.DEFAULT.with(B, S, M, N, N)));
		double[] rFew = survey(Geology.fresh(SEED, GeoOptions.DEFAULT.with(B, S, N, N, F))), rMany = survey(Geology.fresh(SEED, GeoOptions.DEFAULT.with(B, S, N, N, M)));
		String all = "ocean " + oFew[0] + "/" + mid[0] + "/" + oMany[0] + ", cones " + vFew[1] + "/" + mid[1] + "/" + vMany[1]
				+ ", rivers " + rFew[2] + "/" + mid[2] + "/" + rMany[2];
		ctx.getWorld().getServer().sendMessage(net.minecraft.text.Text.literal("options survey: " + all));
		expect(ctx, oFew[0] < mid[0] && mid[0] < oMany[0], "ocean share should grow: " + all);
		expect(ctx, vFew[1] < mid[1] && mid[1] < vMany[1], "volcanoes should grow: " + all);
		expect(ctx, rFew[2] < mid[2] && mid[2] < rMany[2], "rivers should grow: " + all);
		ctx.complete();
	}

	/** Small and large provinces: the ground never jumps between neighbouring columns (the blends stay whole). */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 2400)
	public void provinceSizesStayContinuous(TestContext ctx) {
		for (GeoOptions.Size size : GeoOptions.Size.values()) {
			Geology g = Geology.fresh(SEED, GeoOptions.DEFAULT.with(GeoOptions.BiomeSize.NORMAL, size,
					GeoOptions.Amount.NORMAL, GeoOptions.Amount.NORMAL, GeoOptions.Amount.NORMAL));
			int provinces = 0;
			Geology.Province last = null;
			for (int x = -8000; x < 8000; x++) {
				Geology.Column a = g.computeBaseForTest(x, 777), b = g.computeBaseForTest(x + 1, 777);
				expect(ctx, Math.abs(a.macro - b.macro) < 1.5, size + ": macro jumps " + a.macro + " -> " + b.macro + " at x=" + x);
				if (a.province != last) provinces++;
				last = a.province;
			}
			ctx.getWorld().getServer().sendMessage(net.minecraft.text.Text.literal(size + " province changes on the line: " + provinces));
		}
		ctx.complete();
	}

	/** Biome size: the climate noise scale follows the option, and biome patches get bigger with it. */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 2400)
	public void biomeSizeFollowsTheOption(TestContext ctx) {
		var rm = ctx.getWorld().getRegistryManager();
		var source = MultiNoiseBiomeSource.create(rm.get(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.entryOf(MultiNoiseBiomeSourceParameterLists.OVERWORLD));
		Map<GeoOptions.BiomeSize, Integer> changes = new HashMap<>();
		boolean dump = Boolean.getBoolean("pe.biomemaps");
		try {
			GeoWorld.setActive(true);
			for (GeoOptions o : new GeoOptions[]{GeoOptions.LEGACY,
					GeoOptions.DEFAULT.with(GeoOptions.BiomeSize.SMALL, GeoOptions.Size.NORMAL, GeoOptions.Amount.NORMAL, GeoOptions.Amount.NORMAL, GeoOptions.Amount.NORMAL),
					GeoOptions.DEFAULT,
					GeoOptions.DEFAULT.with(GeoOptions.BiomeSize.HUGE, GeoOptions.Size.NORMAL, GeoOptions.Amount.NORMAL, GeoOptions.Amount.NORMAL, GeoOptions.Amount.NORMAL)}) {
				GeoOptions.setCurrent(o);
				NoiseConfig config = NoiseConfig.create(rm.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).get(GeoWorld.SETTINGS),
						rm.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), SEED);
				Geology g = GeoWorld.geology(config);
				var sampler = config.getMultiNoiseSampler();
				int step = dump ? 16 : 32, half = dump ? 4096 : 2048;
				StringBuilder out = new StringBuilder();
				int count = 0;
				for (int z = -half; z < half; z += step) {
					String prev = null;
					for (int x = -half; x < half; x += step) {
						int y = (int) g.column(x, z).height;
						RegistryEntry<Biome> b = source.getBiome(x >> 2, y >> 2, z >> 2, sampler);
						String id = b.getKey().map(k -> k.getValue().getPath()).orElse("?");
						if (prev != null && !prev.equals(id)) count++;
						prev = id;
						if (dump) out.append(x == -half ? "" : " ").append(id);
					}
					if (dump) out.append('\n');
				}
				String name = o.legacy() ? "legacy" : o.biomes().name().toLowerCase();
				if (!o.legacy()) changes.put(o.biomes(), count);
				else changes.put(GeoOptions.BiomeSize.LARGE, count);
				if (dump) {
					Path f = Path.of(System.getProperty("pe.biomemapdir", "."), "biomes_" + name + ".txt");
					Files.createDirectories(f.getParent());
					Files.writeString(f, out.toString());
				}
				// the climate scale itself
				DensityFunction t = config.getNoiseRouter().temperature();
				expect(ctx, t instanceof com.periodicelements.worldgen.GeoClimate || t.toString().contains("GeoClimate"),
						"temperature is not periodic_elements:climate: " + t);
			}
		} catch (java.io.IOException e) {
			ctx.throwGameTestException(e.toString());
		} finally {
			GeoOptions.setCurrent(GeoOptions.DEFAULT);
			GeoWorld.setActive(false);
		}
		ctx.getWorld().getServer().sendMessage(net.minecraft.text.Text.literal("biome changes: " + changes));
		int small = changes.get(GeoOptions.BiomeSize.SMALL), normal = changes.get(GeoOptions.BiomeSize.NORMAL),
				legacy = changes.get(GeoOptions.BiomeSize.LARGE), huge = changes.get(GeoOptions.BiomeSize.HUGE);
		expect(ctx, small > normal && normal > legacy && legacy > huge, "biome changes should fall as biomes grow: " + changes);
		ctx.complete();
	}
}
