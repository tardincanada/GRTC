package com.periodicelements.space;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3.66.0: the ground of Mars. Smooth northern lowlands and old cratered southern highlands (the dichotomy), giant shield
 * volcanoes, long canyons with layered sulfate walls, dry river beds with hematite on their floors, all under red
 * regolith over basalt; ice a few blocks down past {@link #ICE_LATITUDE}, and polar caps of water ice capped with frozen
 * carbon dioxide past {@link #CAP}.
 */
public class MarsChunkGenerator extends ChunkGenerator {
	public static final MapCodec<MarsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, MarsChunkGenerator::new));

	public static final int MIN_Y = 0, HEIGHT = 256;
	/** Distance from z = 0 beyond which ice lies under the soil. */
	public static final int ICE_LATITUDE = 800;
	/** Distance from z = 0 beyond which the polar caps begin. */
	public static final int CAP = 1600;
	/** Volcano cells: one shield volcano at most per cell this wide. */
	public static final int VOLCANO_CELL = 2400;

	private static final Map<Long, Terrain> TERRAINS = new ConcurrentHashMap<>();

	public MarsChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	/** What the ground of one column is like. */
	public enum Ground { PLAIN, HIGHLAND, VOLCANO, CANYON, RIVERBED, CAP }

	/** One column: the top block's y, what it is, and how deep its dust lies. */
	public record Column(int top, Ground ground, double volcano, boolean ice, boolean frost) {
	}

	/** The height field of one world seed. */
	public static final class Terrain {
		private final SimplexNoiseSampler dichotomy, hills, rough, canyon, canyonMask, river, riverMask, flow;
		private final long seed;

		public Terrain(long seed) {
			this.seed = seed;
			dichotomy = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x44494348L));
			hills = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x48494C4DL));
			rough = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x524F5548L));
			canyon = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x43414E59L));
			canyonMask = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x434D534BL));
			river = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x52495645L));
			riverMask = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x524D534BL));
			flow = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x464C4F57L));
		}

		private static double fbm(SimplexNoiseSampler n, double x, double z, int octaves) {
			double sum = 0, amp = 1, norm = 0;
			for (int o = 0; o < octaves; o++) {
				sum += amp * n.sample(x, z);
				norm += amp;
				amp *= 0.5;
				x *= 2;
				z *= 2;
			}
			return sum / norm;
		}

		/** 0 in the northern lowlands to 1 in the southern highlands, with a ragged border near z = 0. */
		public double highland(int x, int z) {
			double edge = z + 500 * fbm(dichotomy, x / 1500.0, z / 1500.0, 3);
			return MathHelper.clamp(0.5 + edge / 700.0, 0, 1);
		}

		private long cellSeed(int cx, int cz, int layer) {
			long h = seed * 0x9E3779B97F4A7C15L + cx * 341873128712L + cz * 132897987541L + layer * 0x632BE59BD9B4E019L;
			h ^= h >>> 29;
			h *= 0xBF58476D1CE4E5B9L;
			return h ^ (h >>> 32);
		}

		/** One shield volcano: centre, radius, height of the summit above the plain. */
		public record Volcano(double x, double z, double r, double h) {
		}

		/** The volcano of a cell, or null; never too near the poles. */
		public Volcano volcano(int cx, int cz) {
			java.util.Random r = new java.util.Random(cellSeed(cx, cz, 7));
			if (r.nextDouble() > 0.55) {
				return null;
			}
			double x = (cx + 0.25 + 0.5 * r.nextDouble()) * VOLCANO_CELL, z = (cz + 0.25 + 0.5 * r.nextDouble()) * VOLCANO_CELL;
			if (Math.abs(z) > CAP - 500) {
				return null;
			}
			double u = r.nextDouble();
			return new Volcano(x, z, 260 + 260 * u, 70 + 90 * u);
		}

		/** The volcano nearest (x, z) (cell and its neighbours), or null. */
		public Volcano nearestVolcano(int x, int z) {
			int cx0 = Math.floorDiv(x, VOLCANO_CELL), cz0 = Math.floorDiv(z, VOLCANO_CELL);
			Volcano best = null;
			double bestD = Double.MAX_VALUE;
			for (int cx = cx0 - 1; cx <= cx0 + 1; cx++) {
				for (int cz = cz0 - 1; cz <= cz0 + 1; cz++) {
					Volcano v = volcano(cx, cz);
					if (v == null) {
						continue;
					}
					double d = Math.hypot(x - v.x, z - v.z) / v.r;
					if (d < bestD) {
						bestD = d;
						best = v;
					}
				}
			}
			return best;
		}

		/** Height a shield volcano adds at (x, z): broad gentle flanks, a steep scarp at the foot, a summit caldera. */
		public double volcanoHeight(int x, int z) {
			Volcano v = nearestVolcano(x, z);
			if (v == null) {
				return 0;
			}
			double wob = 1 + 0.12 * flow.sample(x / 120.0, z / 120.0);   // lava flows make the outline ragged
			double d = Math.hypot(x - v.x, z - v.z) / (v.r * wob);
			if (d >= 1.08) {
				return 0;
			}
			double h = d < 1 ? v.h * Math.pow(1 - d, 1.4) : 0;
			if (d > 0.9 && d < 1.08) {   // Olympus Mons ends in cliffs
				h += 10 * Math.max(0, 1 - Math.abs(d - 0.96) / 0.12);
			}
			double cal = 0.07;
			if (d < cal) {
				h -= v.h * 0.12 * (1 - (d / cal) * (d / cal));   // the caldera
			}
			return h;
		}

		/** How far into a canyon this column is: 0 outside, 1 on the floor. */
		public double canyon(int x, int z) {
			if (canyonMask.sample(x / 3000.0, z / 3000.0) < 0.15) {
				return 0;
			}
			double a = Math.abs(fbm(canyon, x / 1400.0, z / 4200.0, 2));
			double w = 0.035;
			return a >= w ? 0 : MathHelper.clamp((w - a) / (w * 0.5), 0, 1);
		}

		/** How far into a dry river bed this column is: 0 outside, 1 in the channel. */
		public double riverbed(int x, int z) {
			if (riverMask.sample(x / 1200.0, z / 1200.0) < -0.1) {
				return 0;
			}
			double a = Math.abs(river.sample(x / 420.0, z / 420.0));
			double w = 0.022;
			return a >= w ? 0 : (w - a) / w;
		}

		private static final int[][] LAYERS = {{200, 90}, {56, 30}};
		private static final double[][] RADII = {{20, 70}, {6, 18}};
		private static final double[] CHANCE = {0.35, 0.45};

		/** Craters: fewer and softer than the Moon's, worn down by wind and filled by dust; more on the highlands. */
		double craters(int x, int z, double high) {
			double dh = 0;
			for (int l = 0; l < LAYERS.length; l++) {
				int cell = LAYERS[l][0];
				int cx0 = Math.floorDiv(x, cell), cz0 = Math.floorDiv(z, cell);
				int reach = (int) Math.ceil(RADII[l][1] * 1.5 / cell);
				for (int cx = cx0 - reach; cx <= cx0 + reach; cx++) {
					for (int cz = cz0 - reach; cz <= cz0 + reach; cz++) {
						java.util.Random r = new java.util.Random(cellSeed(cx, cz, l));
						if (r.nextDouble() > CHANCE[l] * (0.35 + 0.65 * high)) {
							continue;
						}
						double u = r.nextDouble();
						double radius = RADII[l][0] + (RADII[l][1] - RADII[l][0]) * u * u * u;
						double ccx = (cx + 0.15 + 0.7 * r.nextDouble()) * cell, ccz = (cz + 0.15 + 0.7 * r.nextDouble()) * cell;
						double d = Math.hypot(x - ccx, z - ccz) / radius;
						if (d > 1.6) {
							continue;
						}
						double depth = Math.min(radius * 0.22, 16);
						if (d < 1) {
							dh -= depth * (1 - d * d);
						}
						dh += depth * 0.3 * Math.exp(-((d - 1) / 0.25) * ((d - 1) / 0.25));
					}
				}
			}
			return dh;
		}

		/** The column at (x, z). */
		public Column column(int x, int z) {
			double hi = highland(x, z);
			double s = hi * hi * (3 - 2 * hi);
			double low = 64 + 3 * fbm(hills, x / 500.0, z / 500.0, 3) + 0.8 * rough.sample(x / 40.0, z / 40.0);
			double high = 86 + 12 * fbm(hills, x / 380.0 + 70, z / 380.0, 4) + 3 * rough.sample(x / 45.0, z / 45.0);
			double h = low * (1 - s) + high * s;
			double vh = volcanoHeight(x, z);
			h += vh;
			double cy = canyon(x, z);
			double rb = cy > 0 ? 0 : riverbed(x, z);
			if (vh < 2) {
				h += craters(x, z, s);
			}
			if (cy > 0) {
				double floor = Math.min(h, 40 + 3 * rough.sample(x / 60.0, z / 60.0));
				double t = cy * cy * (3 - 2 * cy);
				h = h * (1 - t) + floor * t;
			} else if (rb > 0 && vh < 2) {
				h -= 5 * Math.sqrt(rb);
			}
			int lat = Math.abs(z);
			if (lat > CAP) {   // the polar caps: layered ice domes kilometres thick, a few dozen blocks here
				h += Math.min(28, (lat - CAP) * 0.05) * (0.8 + 0.2 * rough.sample(x / 90.0, z / 90.0));
			}
			int top = MathHelper.clamp((int) Math.round(h), 8, HEIGHT - 8);
			Ground g = lat > CAP ? Ground.CAP : cy > 0.15 ? Ground.CANYON : rb > 0.3 && vh < 2 ? Ground.RIVERBED
					: vh > 3 ? Ground.VOLCANO : s > 0.5 ? Ground.HIGHLAND : Ground.PLAIN;
			boolean ice = lat > ICE_LATITUDE;
			boolean frost = lat > CAP + 300 && rough.sample(x / 30.0, z / 30.0) > -0.3;
			return new Column(top, g, vh, ice, frost);
		}
	}

	public static Terrain terrain(NoiseConfig config) {
		long seed = config.getOrCreateRandomDeriver(Moon.id("mars")).split("terrain").nextLong();
		return TERRAINS.computeIfAbsent(seed, Terrain::new);
	}

	/** Hematite concretions ("blueberries") are scattered through the sulfate beds: one block in a few. */
	private static boolean berry(int x, int y, int z) {
		long h = x * 3129871L ^ z * 116129781L ^ y * 0x5DEECE66DL;
		h = h * h * 42317861L + h * 11L;
		return ((h >> 16) & 7) == 0;
	}

	/** The block at height y of column {@code c} at (x, z). */
	public static BlockState block(int x, int y, int z, Column c) {
		int top = c.top();
		if (y == MIN_Y) {
			return Blocks.BEDROCK.getDefaultState();
		}
		if (y > top) {
			return Blocks.AIR.getDefaultState();
		}
		int depth = top - y;
		switch (c.ground()) {
			case CAP -> {
				if (c.frost() && depth == 0) {
					return Mars.DRY_ICE.getDefaultState();
				}
				if (depth < 14) {
					// layered deposits: bands of dusty ice between clean ice
					return (y % 5 == 0 ? Mars.MARTIAN_REGOLITH : Mars.MARTIAN_ICE).getDefaultState();
				}
			}
			case VOLCANO -> {
				if (depth < 1 && c.volcano() < 40) {
					return Mars.MARTIAN_REGOLITH.getDefaultState();
				}
				return Mars.MARTIAN_BASALT.getDefaultState();   // fresh lava flows, little dust on the high flanks
			}
			case CANYON -> {
				if (depth < 1) {
					return Mars.MARTIAN_REGOLITH.getDefaultState();
				}
				// the canyon floor and walls bare the layered sulfate beds of an old sea or lake
				if (y < 74) {
					return (berry(x, y, z) ? Mars.HEMATITE_CONCRETION : (y / 3) % 3 == 0 ? Mars.MARTIAN_BASALT : Mars.MARTIAN_SULFATE)
							.getDefaultState();
				}
			}
			case RIVERBED -> {
				if (depth < 3) {
					return (berry(x, y, z) ? Mars.HEMATITE_CONCRETION : Mars.MARTIAN_SULFATE).getDefaultState();
				}
			}
			default -> {
			}
		}
		if (depth < 3) {
			return Mars.MARTIAN_REGOLITH.getDefaultState();
		}
		if (c.ice() && depth < 7) {
			return Mars.MARTIAN_ICE.getDefaultState();   // ground ice, as Phoenix found under a few centimetres of soil
		}
		return Mars.MARTIAN_BASALT.getDefaultState();
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structures, Chunk chunk) {
		Terrain t = terrain(noiseConfig);
		ChunkPos cp = chunk.getPos();
		Heightmap ocean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
		Heightmap surface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = cp.getStartX() + dx, z = cp.getStartZ() + dz;
				Column c = t.column(x, z);
				for (int y = MIN_Y; y <= c.top(); y++) {
					BlockState s = block(x, y, z, c);
					chunk.setBlockState(p.set(dx, y, dz), s, false);
					ocean.trackUpdate(dx, y, dz, s);
					surface.trackUpdate(dx, y, dz, s);
				}
			}
		}
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public void carve(ChunkRegion region, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structures,
					  Chunk chunk, GenerationStep.Carver carverStep) {
	}

	@Override
	public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
	}

	@Override
	public void populateEntities(ChunkRegion region) {
	}

	@Override
	public int getWorldHeight() {
		return HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return MIN_Y;
	}

	@Override
	public int getMinimumY() {
		return MIN_Y;
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type type, HeightLimitView world, NoiseConfig noiseConfig) {
		return terrain(noiseConfig).column(x, z).top() + 1;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		Column c = terrain(noiseConfig).column(x, z);
		BlockState[] col = new BlockState[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			col[y] = block(x, MIN_Y + y, z, c);
		}
		return new VerticalBlockSample(MIN_Y, col);
	}

	@Override
	public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
		Column c = terrain(noiseConfig).column(pos.getX(), pos.getZ());
		text.add(String.format(Locale.ROOT, "Mars: %s, ground %d, volcano %.0f", c.ground(), c.top(), c.volcano()));
	}
}
