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
 * 3.64.0: the Moon's ground. Dark maria of basalt in low basins, bright highlands of anorthosite above them, all under a
 * few blocks of grey regolith, and craters of every size: bowls with raised rims, the big ones with ice on their floors
 * near the poles (more than {@link #POLE} blocks north or south), where the sun never reaches.
 */
public class MoonChunkGenerator extends ChunkGenerator {
	public static final MapCodec<MoonChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, MoonChunkGenerator::new));

	public static final int MIN_Y = 0, HEIGHT = 256;
	/** Distance from z = 0 beyond which the floors of big craters hold ice. */
	public static final int POLE = 1000;

	private static final Map<Long, Terrain> TERRAINS = new ConcurrentHashMap<>();

	public MoonChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	/** The height field of one world seed: maria, highlands and craters. */
	public static final class Terrain {
		private final SimplexNoiseSampler mare, hills, rough, tube, skylight, kreep;
		private final long seed;

		public Terrain(long seed) {
			this.seed = seed;
			mare = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x4D41524EL));
			hills = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x48494C4CL));
			rough = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x524F5547L));
			tube = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x54554245L));
			skylight = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x534B594CL));
			kreep = new SimplexNoiseSampler(new CheckedRandom(seed ^ 0x4B524545L));
		}

		/**
		 * 3.65.0: KREEP, the potassium, rare-earth and phosphorus rich basalt of the Procellarum region (and its
		 * thorium, which Lunar Prospector's gamma-ray spectrometer mapped in 1998): some maria are made of it.
		 */
		public boolean kreep(int x, int z) {
			return mare(x, z) > 0.5 && fbm(kreep, x / 1600.0, z / 1600.0, 2) > 0.3;
		}

		/**
		 * 3.65.0: lava tubes under the maria, the drained channels of old lava flows: winding tunnels about 8 blocks wide
		 * and 6 high, a dozen blocks under the ground, with here and there a skylight where the roof fell in. Returns
		 * the lowest and highest air block of the tube in this column, or null; the top is the ground's when the roof
		 * is open.
		 */
		private static final double TUBE_WIDTH = 0.07;

		/** Is this column in the basalt round a lava tube (the flow that built it), where the tube's walls are? */
		public boolean tubeRock(int x, int z) {
			return mare(x, z) >= 0.55 && Math.abs(tube.sample(x / 260.0, z / 260.0)) < TUBE_WIDTH * 1.8;
		}

		public int[] tube(int x, int z, int top) {
			if (mare(x, z) < 0.6) {
				return null;
			}
			double a = Math.abs(tube.sample(x / 260.0, z / 260.0)), w = TUBE_WIDTH;
			if (a >= w) {
				return null;
			}
			double r = a / w;
			int c = 50 + (int) Math.round(3 * hills.sample(x / 300.0 + 90, z / 300.0));
			double h = 4.5 * Math.sqrt(1 - r * r);
			int lo = c - (int) Math.round(h * 0.5), hi = c + (int) Math.round(h);
			if (r < 0.8 && skylight.sample(x / 60.0, z / 60.0) > 0.62) {
				return new int[]{lo, top};   // a skylight: the roof has fallen in
			}
			hi = Math.min(hi, top - 4);
			return hi >= lo ? new int[]{lo, hi} : null;
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

		/** How much of a mare this column is, 0 (highland) to 1 (mare). */
		public double mare(int x, int z) {
			double m = fbm(mare, x / 1100.0, z / 1100.0, 3);
			return MathHelper.clamp((m - 0.02) / 0.22, 0, 1);
		}

		private long cellSeed(int cx, int cz, int layer) {
			long h = seed * 0x9E3779B97F4A7C15L + cx * 341873128712L + cz * 132897987541L + layer * 0x632BE59BD9B4E019L;
			h ^= h >>> 29;
			h *= 0xBF58476D1CE4E5B9L;
			return h ^ (h >>> 32);
		}

		/** One crater: centre, radius. */
		record Crater(double x, double z, double r) {
		}

		/** The craters of a cell on a layer (cell size, chance, radius range). */
		private Crater crater(int cx, int cz, int layer, int cell, double chance, double rMin, double rMax) {
			java.util.Random r = new java.util.Random(cellSeed(cx, cz, layer));
			if (r.nextDouble() > chance) {
				return null;
			}
			double u = r.nextDouble();
			double radius = rMin + (rMax - rMin) * u * u * u;
			return new Crater((cx + 0.15 + 0.7 * r.nextDouble()) * cell, (cz + 0.15 + 0.7 * r.nextDouble()) * cell, radius);
		}

		private static final int[][] LAYERS = {{160, 70}, {48, 24}, {16, 6}};
		private static final double[][] RADII = {{18, 60}, {6, 18}, {2, 5}};
		private static final double[] CHANCE = {0.55, 0.6, 0.5};

		/** Height offset of the craters at (x, z), and through {@code floor} whether it is the floor of a big crater. */
		double craters(int x, int z, boolean[] iceFloor) {
			double dh = 0;
			for (int l = 0; l < LAYERS.length; l++) {
				int cell = LAYERS[l][0];
				int cx0 = Math.floorDiv(x, cell), cz0 = Math.floorDiv(z, cell);
				int reach = (int) Math.ceil(RADII[l][1] * 1.5 / cell);
				for (int cx = cx0 - reach; cx <= cx0 + reach; cx++) {
					for (int cz = cz0 - reach; cz <= cz0 + reach; cz++) {
						Crater c = crater(cx, cz, l, cell, CHANCE[l], RADII[l][0], RADII[l][1]);
						if (c == null) {
							continue;
						}
						double d = Math.sqrt((x - c.x) * (x - c.x) + (z - c.z) * (z - c.z)) / c.r;
						if (d > 1.6) {
							continue;
						}
						double depth = Math.min(c.r * 0.32, 22);
						if (d < 1) {
							dh -= depth * (1 - d * d);
							if (iceFloor != null && d < 0.5 && c.r >= 12 && Math.abs(z) > POLE) {
								iceFloor[0] = true;
							}
						}
						dh += depth * 0.35 * Math.exp(-((d - 1) / 0.22) * ((d - 1) / 0.22));
					}
				}
			}
			return dh;
		}

		/** The top block's y at (x, z). */
		public int height(int x, int z, boolean[] iceFloor) {
			double m = mare(x, z);
			double s = m * m * (3 - 2 * m);
			double high = 84 + 16 * fbm(hills, x / 420.0, z / 420.0, 4) + 4 * rough.sample(x / 50.0, z / 50.0);
			double low = 66 + 2.5 * fbm(hills, x / 260.0 + 40, z / 260.0, 3) + 0.8 * rough.sample(x / 30.0, z / 30.0);
			double h = high * (1 - s) + low * s + craters(x, z, iceFloor);
			return MathHelper.clamp((int) Math.round(h), 8, HEIGHT - 20);
		}
	}

	public static Terrain terrain(NoiseConfig config) {
		long seed = config.getOrCreateRandomDeriver(Moon.id("moon")).split("terrain").nextLong();
		return TERRAINS.computeIfAbsent(seed, Terrain::new);
	}

	/** The block at height y of a column whose top is {@code top}, KREEP basalt for the maria where {@code kreep}. */
	public static BlockState block(int y, int top, double mare, boolean ice, boolean kreep, int[] tube) {
		if (tube != null && y >= tube[0] && y <= tube[1] && y > MIN_Y) {
			return Blocks.AIR.getDefaultState();
		}
		BlockState s = block(y, top, mare, ice);
		return kreep && s.isOf(Moon.MARE_BASALT) ? Moon.KREEP_BASALT.getDefaultState() : s;
	}

	/** The block at height y of a column whose top is {@code top}. */
	public static BlockState block(int y, int top, double mare, boolean ice) {
		if (y == MIN_Y) {
			return Blocks.BEDROCK.getDefaultState();
		}
		if (y > top) {
			return Blocks.AIR.getDefaultState();
		}
		if (ice && y > top - 2) {
			return Moon.LUNAR_ICE.getDefaultState();
		}
		if (y > top - 3) {
			return Moon.LUNAR_REGOLITH.getDefaultState();
		}
		// maria are basalt flows a dozen blocks deep over the anorthosite crust
		if (mare > 0.5 && y > top - 3 - 12 * mare) {
			return Moon.MARE_BASALT.getDefaultState();
		}
		return Moon.ANORTHOSITE.getDefaultState();
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structures, Chunk chunk) {
		Terrain t = terrain(noiseConfig);
		ChunkPos cp = chunk.getPos();
		Heightmap ocean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
		Heightmap surface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
		BlockPos.Mutable p = new BlockPos.Mutable();
		boolean[] ice = new boolean[1];
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = cp.getStartX() + dx, z = cp.getStartZ() + dz;
				ice[0] = false;
				int top = t.height(x, z, ice);
				double mare = t.mare(x, z);
				boolean kreep = t.kreep(x, z);
				int[] tube = t.tube(x, z, top);
				boolean lining = t.tubeRock(x, z);
				for (int y = MIN_Y; y <= top; y++) {
					BlockState s = block(y, top, mare, ice[0], kreep, tube);
					if (s.isAir()) {
						continue;   // a lava tube
					}
					if (lining && y >= 40 && y < top - 3 && s.isOf(Moon.ANORTHOSITE)) {
						s = (kreep ? Moon.KREEP_BASALT : Moon.MARE_BASALT).getDefaultState();   // the old flow round the tube
					}
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
		Terrain t = terrain(noiseConfig);
		int top = t.height(x, z, null);
		int[] tube = t.tube(x, z, top);
		return (tube != null && tube[1] == top ? tube[0] - 1 : top) + 1;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		Terrain t = terrain(noiseConfig);
		boolean[] ice = new boolean[1];
		int top = t.height(x, z, ice);
		double mare = t.mare(x, z);
		boolean kreep = t.kreep(x, z);
		int[] tube = t.tube(x, z, top);
		BlockState[] col = new BlockState[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			col[y] = block(MIN_Y + y, top, mare, ice[0], kreep, tube);
		}
		return new VerticalBlockSample(MIN_Y, col);
	}

	@Override
	public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
		Terrain t = terrain(noiseConfig);
		text.add(String.format(Locale.ROOT, "Moon: mare %.2f, ground %d", t.mare(pos.getX(), pos.getZ()),
				t.height(pos.getX(), pos.getZ(), null)));
	}
}
