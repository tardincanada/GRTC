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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3.68.0: Mercury's ground. The Moon's height field (MoonChunkGenerator.Terrain, from Mercury's own seed): smooth
 * volcanic plains in the basins and old cratered terrain round them, all of a crust darkened by graphite. Bright
 * hollows lie in some plains where sulfur and other volatiles boiled off (MESSENGER, 2011), and ice fills the floors of
 * the big polar craters, as on the Moon.
 */
public class MercuryChunkGenerator extends ChunkGenerator {
	public static final MapCodec<MercuryChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, MercuryChunkGenerator::new));

	public static final int MIN_Y = 0, HEIGHT = 256;
	private static final Map<Long, MoonChunkGenerator.Terrain> TERRAINS = new ConcurrentHashMap<>();

	public MercuryChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	public static MoonChunkGenerator.Terrain terrain(NoiseConfig config) {
		long seed = config.getOrCreateRandomDeriver(Moon.id("mercury")).split("terrain").nextLong();
		return TERRAINS.computeIfAbsent(seed, MoonChunkGenerator.Terrain::new);
	}

	/** The block at height y of a column whose top is {@code top}; hollows where the sulfur-rich plains lie. */
	public static BlockState block(int y, int top, boolean ice, boolean hollows) {
		if (y == MIN_Y) {
			return Blocks.BEDROCK.getDefaultState();
		}
		if (y > top) {
			return Blocks.AIR.getDefaultState();
		}
		if (ice && y > top - 2) {
			return Moon.LUNAR_ICE.getDefaultState();
		}
		if (hollows && y > top - 2) {
			return Mercury.MERCURIAN_HOLLOWS.getDefaultState();
		}
		if (y > top - 3) {
			return Mercury.MERCURIAN_REGOLITH.getDefaultState();
		}
		return Mercury.MERCURIAN_CRUST.getDefaultState();
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structures, Chunk chunk) {
		MoonChunkGenerator.Terrain t = terrain(noiseConfig);
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
				boolean hollows = t.kreep(x, z);
				for (int y = MIN_Y; y <= top; y++) {
					BlockState s = block(y, top, ice[0], hollows);
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
		return terrain(noiseConfig).height(x, z, null) + 1;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		MoonChunkGenerator.Terrain t = terrain(noiseConfig);
		boolean[] ice = new boolean[1];
		int top = t.height(x, z, ice);
		boolean hollows = t.kreep(x, z);
		BlockState[] col = new BlockState[HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			col[y] = block(MIN_Y + y, top, ice[0], hollows);
		}
		return new VerticalBlockSample(MIN_Y, col);
	}

	@Override
	public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
		text.add(String.format(Locale.ROOT, "Mercury: ground %d", terrain(noiseConfig).height(pos.getX(), pos.getZ(), null)));
	}
}
