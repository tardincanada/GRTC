package com.periodicelements.space;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3.68.0: Venus, 50 km up: nothing but sky. Below the Aerostat Deck lie the clouds of sulfuric acid, in banks and
 * gaps between y 20 and 64; under them the void stands for the 50 km fall to ground at 465 °C, which nothing survives.
 */
public class VenusChunkGenerator extends ChunkGenerator {
	public static final MapCodec<VenusChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, VenusChunkGenerator::new));

	public static final int MIN_Y = 0, HEIGHT = 256;
	private static final Map<Long, Clouds> CLOUDS = new ConcurrentHashMap<>();

	public VenusChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	/** The cloud field: banks stretched east to west by the winds that circle Venus in four days. */
	public static final class Clouds {
		private final SimplexNoiseSampler big, small;

		public Clouds(long seed) {
			Random r = Random.create(seed);
			big = new SimplexNoiseSampler(r);
			small = new SimplexNoiseSampler(r);
		}

		public boolean cloud(int x, int y, int z) {
			if (y < Venus.CLOUD_BOTTOM || y > Venus.CLOUD_TOP) {
				return false;
			}
			double mid = (Venus.CLOUD_BOTTOM + Venus.CLOUD_TOP) / 2.0, half = (Venus.CLOUD_TOP - Venus.CLOUD_BOTTOM) / 2.0;
			double edge = Math.abs(y - mid) / half;   // 0 in the middle of the layer, 1 at its top and bottom
			double n = big.sample(x / 96.0, y / 18.0, z / 40.0) * 0.7 + small.sample(x / 20.0, y / 7.0, z / 12.0) * 0.3;
			return n > -0.15 + 0.75 * edge * edge;
		}
	}

	public static Clouds clouds(NoiseConfig config) {
		long seed = config.getOrCreateRandomDeriver(Moon.id("venus")).split("clouds").nextLong();
		return CLOUDS.computeIfAbsent(seed, Clouds::new);
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structures, Chunk chunk) {
		Clouds c = clouds(noiseConfig);
		ChunkPos cp = chunk.getPos();
		Heightmap surface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
		BlockPos.Mutable p = new BlockPos.Mutable();
		BlockState cloud = Venus.VENUS_CLOUD.getDefaultState();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = cp.getStartX() + dx, z = cp.getStartZ() + dz;
				for (int y = Venus.CLOUD_BOTTOM; y <= Venus.CLOUD_TOP; y++) {
					if (c.cloud(x, y, z)) {
						chunk.setBlockState(p.set(dx, y, dz), cloud, false);
						surface.trackUpdate(dx, y, dz, cloud);
					}
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
		return MIN_Y;   // no ground anywhere: the clouds hold nothing up
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		Clouds c = clouds(noiseConfig);
		BlockState[] col = new BlockState[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			col[y] = c.cloud(x, MIN_Y + y, z) ? Venus.VENUS_CLOUD.getDefaultState() : Blocks.AIR.getDefaultState();
		}
		return new VerticalBlockSample(MIN_Y, col);
	}

	@Override
	public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
		text.add(String.format(Locale.ROOT, "Venus: %s", clouds(noiseConfig).cloud(pos.getX(), pos.getY(), pos.getZ()) ? "in the clouds" : "clear air"));
	}
}
