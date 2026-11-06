package com.periodicelements.worldgen;

import com.periodicelements.PeriodicElements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.EnumMap;
import java.util.Set;

/**
 * The "Geological" world type (world preset {@code periodic_elements:geological}). Terrain and biome climate come
 * from {@link GeoDensity}; after the vanilla surface rules run, {@link #paint} turns plain stone into the
 * province's rocks and lays the rock bare on cliffs and in rare outcrop patches.
 */
public final class GeoWorld {
	public static final RegistryKey<ChunkGeneratorSettings> SETTINGS =
			RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.of(PeriodicElements.MOD_ID, "geological"));

	private static final EnumMap<Geology.Rock, BlockState> ROCKS = new EnumMap<>(Geology.Rock.class);
	private static final Set<Block> SOIL = Set.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
			Blocks.ROOTED_DIRT, Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SNOW_BLOCK,
			Blocks.POWDER_SNOW, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MUD, Blocks.PACKED_ICE);

	private GeoWorld() {
	}

	public static void register() {
		Registry.register(Registries.DENSITY_FUNCTION_TYPE, Identifier.of(PeriodicElements.MOD_ID, "geology"), GeoDensity.CODEC);
		Registry.register(Registries.DENSITY_FUNCTION_TYPE, Identifier.of(PeriodicElements.MOD_ID, "climate"), GeoClimate.CODEC);
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents.LOAD.register((server, world) -> {
			if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
				active = world.getChunkManager().getChunkGenerator() instanceof net.minecraft.world.gen.chunk.NoiseChunkGenerator noise
						&& noise.matchesSettings(SETTINGS);
				// 3.49.0: the world's options, before any chunk (the spawn search included) is made
				GeoOptions.setCurrent(active ? GeoOptions.loadFor(server.getSavePath(net.minecraft.util.WorldSavePath.ROOT),
						!server.getSaveProperties().getMainWorldProperties().isInitialized()) : GeoOptions.DEFAULT);
			}
		});
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			active = false;
			GeoOptions.setCurrent(GeoOptions.DEFAULT);
		});
		ROCKS.put(Geology.Rock.STONE, Blocks.STONE.getDefaultState());
		ROCKS.put(Geology.Rock.GRANITE, Blocks.GRANITE.getDefaultState());
		ROCKS.put(Geology.Rock.DIORITE, Blocks.DIORITE.getDefaultState());
		ROCKS.put(Geology.Rock.ANDESITE, Blocks.ANDESITE.getDefaultState());
		ROCKS.put(Geology.Rock.TUFF, Blocks.TUFF.getDefaultState());
		ROCKS.put(Geology.Rock.BASALT, Blocks.BASALT.getDefaultState());
		ROCKS.put(Geology.Rock.SMOOTH_BASALT, Blocks.SMOOTH_BASALT.getDefaultState());
		ROCKS.put(Geology.Rock.SANDSTONE, Blocks.SANDSTONE.getDefaultState());
		ROCKS.put(Geology.Rock.TERRACOTTA, Blocks.TERRACOTTA.getDefaultState());
		ROCKS.put(Geology.Rock.BROWN_TERRACOTTA, Blocks.BROWN_TERRACOTTA.getDefaultState());
		ROCKS.put(Geology.Rock.CALCITE, Blocks.CALCITE.getDefaultState());
		ROCKS.put(Geology.Rock.SHALE, com.periodicelements.GeoRocks.SHALE.getDefaultState());
		ROCKS.put(Geology.Rock.GNEISS, com.periodicelements.GeoRocks.GNEISS.getDefaultState());
		ROCKS.put(Geology.Rock.PERIDOTITE, com.periodicelements.GeoRocks.PERIDOTITE.getDefaultState());
	}

	/** The block a rock of the geology stands for. */
	public static BlockState rockState(Geology.Rock r) {
		return ROCKS.get(r);
	}

	public static Geology geology(NoiseConfig config) {
		return Geology.of(GeoDensity.seedOf(config.getOrCreateSampler(GeoDensity.SEED_KEY)));
	}

	/**
	 * 3.45.0: whether a column of the Geological world is a river channel or river pond (fresh water even where the
	 * biome picked for the low valley is an ocean). Within 2 blocks of the channel counts, for the banks' water.
	 */
	public static boolean riverColumn(NoiseConfig config, int x, int z) {
		Geology g = geology(config);
		for (int dx = -2; dx <= 2; dx += 2) {
			for (int dz = -2; dz <= 2; dz += 2) {
				if (g.column(x + dx, z + dz).river) return true;
			}
		}
		return false;
	}

	/** 3.45.0: makes the climate known to this world's geology before any terrain is shaped (rivers need it). */
	public static void prepareClimate(NoiseConfig config, net.minecraft.world.gen.chunk.ChunkGeneratorSettings settings) {
		try {
			Deposits.useClimate(geology(config), config);
		} catch (RuntimeException e) {
			// no geology noise in this world's registries: nothing to prepare
		}
	}

	/** Steepest drop of the terrain around a column, in blocks per block. */
	static double slope(Geology g, int x, int z) {
		double hx = Math.abs(g.column(x + 2, z).height - g.column(x - 2, z).height);
		double hz = Math.abs(g.column(x, z + 2).height - g.column(x, z - 2).height);
		return Math.max(hx, hz) / 4.0;
	}

	/**
	 * True while a server runs a Geological overworld. Biome temperature has no world at hand, so the flag is set
	 * when that world loads; a client joining a remote server keeps vanilla's value for its snowfall rendering.
	 */
	private static volatile boolean active;

	public static int coolingStart(int vanilla) {
		return active ? vanilla + Geology.SNOW_LINE_RAISE : vanilla;
	}

	public static boolean active() {
		return active;
	}

	/** Snowy mountain biomes and their snow-free stand-ins in the temperate band (vanilla temperature -0.15..0.2). */
	private static final java.util.Map<net.minecraft.registry.RegistryKey<net.minecraft.world.biome.Biome>,
			net.minecraft.registry.RegistryKey<net.minecraft.world.biome.Biome>> SNOW_FREE = java.util.Map.of(
			net.minecraft.world.biome.BiomeKeys.FROZEN_PEAKS, net.minecraft.world.biome.BiomeKeys.STONY_PEAKS,
			net.minecraft.world.biome.BiomeKeys.JAGGED_PEAKS, net.minecraft.world.biome.BiomeKeys.STONY_PEAKS,
			net.minecraft.world.biome.BiomeKeys.SNOWY_SLOPES, net.minecraft.world.biome.BiomeKeys.WINDSWEPT_HILLS,
			net.minecraft.world.biome.BiomeKeys.GROVE, net.minecraft.world.biome.BiomeKeys.WINDSWEPT_FOREST);
	private static final long TEMPERATE_MIN = net.minecraft.world.biome.source.util.MultiNoiseUtil.toLong(-0.15F),
			TEMPERATE_MAX = net.minecraft.world.biome.source.util.MultiNoiseUtil.toLong(0.2F);
	private static final java.util.Map<net.minecraft.world.biome.source.BiomeSource,
			java.util.Map<net.minecraft.registry.RegistryKey<net.minecraft.world.biome.Biome>, RegistryEntry<net.minecraft.world.biome.Biome>>>
			STAND_INS = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

	/** The snow-free stand-in for a snowy mountain biome picked in the temperate band, or null to keep it. */
	public static RegistryEntry<net.minecraft.world.biome.Biome> snowFree(net.minecraft.world.biome.source.BiomeSource source,
			RegistryEntry<net.minecraft.world.biome.Biome> biome, long temperature) {
		if (temperature < TEMPERATE_MIN || temperature >= TEMPERATE_MAX) return null;
		var key = biome.getKey().orElse(null);
		if (key == null || !SNOW_FREE.containsKey(key)) return null;
		var byKey = STAND_INS.computeIfAbsent(source, s -> {
			java.util.Map<net.minecraft.registry.RegistryKey<net.minecraft.world.biome.Biome>, RegistryEntry<net.minecraft.world.biome.Biome>> m = new java.util.HashMap<>();
			for (var entry : s.getBiomes()) entry.getKey().ifPresent(k -> m.put(k, entry));
			return m;
		});
		return byKey.get(SNOW_FREE.get(key));
	}

	/** For tests only. */
	public static void setActive(boolean value) {
		active = value;
	}

	public static void paint(Chunk chunk, NoiseConfig config) {
		Geology g = geology(config);
		ChunkPos cp = chunk.getPos();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int bottom = chunk.getBottomY();
		Deposits.useClimate(g, config);
		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				int x = cp.getStartX() + lx, z = cp.getStartZ() + lz;
				int top = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, lx, lz);
				for (int y = bottom; y <= top; y++) {
					pos.set(x, y, z);
					if (chunk.getBlockState(pos).isOf(Blocks.STONE)) {
						BlockState rock = ROCKS.get(g.rock(x, y, z));
						if (!rock.isOf(Blocks.STONE)) chunk.setBlockState(pos, rock, false);
					}
					// soil the surface rules laid on the floor of a cave close under the ground (lava tubes) is rock
					else if (y < top - 6 && SOIL.contains(chunk.getBlockState(pos).getBlock())) {
						chunk.setBlockState(pos, ROCKS.get(g.rock(x, y, z)), false);
					}
				}
				clearAboveSeaFloor(chunk, pos, x, z, g.column(x, z).height);
				flood(chunk, pos, x, lx, z, lz);
				fillRiver(chunk, pos, g, x, lx, z, lz);
				fillCrater(chunk, pos, g, x, z);
				if (top <= Geology.SEA) continue;
				Geology.Column col = g.column(x, z);
				boolean bare = col.cone && col.coneFrac > 0.55 || col.neck || col.dome && slope(g, x, z) > 0.8 || slope(g, x, z) > 1.25 || g.outcropZone(x, z);
				if (bare) {
					for (int d = 0; d < 6; d++) {
						int y = top - d;
						pos.set(x, y, z);
						if (SOIL.contains(chunk.getBlockState(pos).getBlock())) {
							chunk.setBlockState(pos, ROCKS.get(g.rock(x, y, z)), false);
						}
					}
					// loose ash and cinders near the top of a cone
					if (col.cone && col.coneFrac > 0.75 && col.craterLake == Integer.MIN_VALUE
							&& Geology.fbm(g.riverWarpX(), x + 9000, z, 9, 1) > 0.1) {
						pos.set(x, top, z);
						if (!chunk.getBlockState(pos).isOf(Blocks.WATER)) chunk.setBlockState(pos, Blocks.GRAVEL.getDefaultState(), false);
					}
				} else if (soilFor(g, col, x, top, z) != null) {
					// 4c: the subsoil (and bare dirt) takes the colour of the rock it weathered from
					BlockState soil = soilFor(g, col, x, top, z);
					for (int y = top - 5; y <= top; y++) {
						pos.set(x, y, z);
						if (chunk.getBlockState(pos).isOf(Blocks.DIRT)) chunk.setBlockState(pos, soil, false);
					}
				} else if (g.column(x, z).province == Geology.Province.CRATON && g.coarseSoil(x, z)) {
					pos.set(x, top, z);
					if (chunk.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) {
						chunk.setBlockState(pos, Blocks.COARSE_DIRT.getDefaultState(), false);
					}
				}
			}
		}
		// Sea-floor features (sand and gravel disks) are placed on these heightmaps: keep them on the cleared floor.
		Heightmap.populateHeightmaps(chunk, java.util.EnumSet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG));
	}

	/**
	 * Puts the water into a river channel above sea level, on a bed of gravel and sand, and keeps it in. Runs after
	 * the surface and again after the carvers: a cave that crossed the channel (a noise cave under the bed, or a
	 * carver cutting through the water afterwards) drained it, and the river stopped there, falling into the cave.
	 * Now the bed is sealed with a few blocks of rock over such a cave, the channel is filled again, and the banks
	 * next to the channel are closed up to the water line.
	 */
	static void fillRiver(Chunk chunk, BlockPos.Mutable pos, Geology g, int x, int lx, int z, int lz) {
		Geology.Column col = g.column(x, z);
		if (col.riverWater == Integer.MIN_VALUE) {
			sealBank(chunk, pos, g, x, z, col);
			return;
		}
		int ground = (int) Math.ceil(col.height) - 1; // the bed, where the terrain puts it
		int top = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, lx, lz);
		if (top < ground - 2 || top > ground + 1) top = ground; // a cave opened the bed, or a carver left a hole
		if (top >= col.riverWater) return;
		for (int y = top - RIVER_SEAL; y < top; y++) {
			pos.set(x, y, z);
			if (open(chunk.getBlockState(pos))) chunk.setBlockState(pos, ROCKS.get(g.rock(x, y, z)), false);
		}
		pos.set(x, top, z);
		BlockState floor = chunk.getBlockState(pos);
		if (SOIL.contains(floor.getBlock()) || open(floor)) {
			boolean sand = Geology.fbm(g.riverWarpX(), x + 4000, z, 24, 1) > 0.25;
			chunk.setBlockState(pos, (sand ? Blocks.SAND : Blocks.GRAVEL).getDefaultState(), false);
		}
		for (int y = top + 1; y <= col.riverWater; y++) {
			pos.set(x, y, z);
			BlockState state = chunk.getBlockState(pos);
			if (state.isAir() || !state.getFluidState().isEmpty() || state.isReplaceable()) {
				chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
			}
		}
	}

	/** Blocks of rock kept between a river bed and a cave under it. */
	static final int RIVER_SEAL = 3;

	private static boolean open(BlockState state) {
		return state.isAir() || !state.getFluidState().isEmpty();
	}

	/**
	 * Next to a river channel, closes caves cut into the bank under the river's water line (up to the ground), so
	 * the water does not run out sideways.
	 */
	private static void sealBank(Chunk chunk, BlockPos.Mutable pos, Geology g, int x, int z, Geology.Column col) {
		int water = Integer.MIN_VALUE;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx == 0 && dz == 0) continue;
				water = Math.max(water, g.column(x + dx, z + dz).riverWater);
			}
		}
		if (water == Integer.MIN_VALUE) return;
		int ground = (int) Math.ceil(col.height) - 1;
		for (int y = Math.max(ground - RIVER_SEAL, chunk.getBottomY()); y <= Math.min(ground, water); y++) {
			pos.set(x, y, z);
			if (open(chunk.getBlockState(pos))) chunk.setBlockState(pos, ROCKS.get(g.rock(x, y, z)), false);
		}
	}

	/**
	 * A lake or pond at the surface whose floor a carver cut open hung over the cave: closes the cave under it with
	 * a few blocks of rock.
	 */
	private static void sealUnderWater(Chunk chunk, BlockPos.Mutable pos, Geology g, int x, int lx, int z, int lz) {
		int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, lx, lz) + 1;
		int bottom = chunk.getBottomY();
		do {
			y--;
			pos.set(x, y, z);
		} while (y > bottom && chunk.getBlockState(pos).isAir());
		if (!chunk.getBlockState(pos).isOf(Blocks.WATER)) return;
		while (y > bottom && chunk.getBlockState(pos.set(x, y - 1, z)).isOf(Blocks.WATER)) y--;
		if (y < 30) return; // deep water belongs to the sea or to flooded caves
		for (int d = 1; d <= RIVER_SEAL; d++) {
			pos.set(x, y - d, z);
			if (open(chunk.getBlockState(pos))) chunk.setBlockState(pos, ROCKS.get(g.rock(x, y - d, z)), false);
		}
	}

	/** After the carvers: puts back river water and banks that a carved cave cut through. */
	public static void repairRivers(Chunk chunk, NoiseConfig config) {
		Geology g = geology(config);
		ChunkPos cp = chunk.getPos();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				int x = cp.getStartX() + lx, z = cp.getStartZ() + lz;
				fillRiver(chunk, pos, g, x, lx, z, lz);
				sealUnderWater(chunk, pos, g, x, lx, z, lz);
				fillCrater(chunk, pos, g, x, z);
			}
		}
	}

	/** The soil of a column by its rock and climate (4c): terra roxa on basalt and in the wet tropics, chalky soil on limestone. */
	public static BlockState soilFor(Geology g, Geology.Column col, int x, int top, int z) {
		if (com.periodicelements.GeoRocks.TERRA_ROXA == null || col.cone || col.neck) return null;
		if (col.province == Geology.Province.VOLCANIC && col.mountain < 0.45 || Deposits.humidTropics(g, x, z)) {
			return com.periodicelements.GeoRocks.TERRA_ROXA.getDefaultState();
		}
		if (col.province == Geology.Province.BASIN && col.mountain < 0.45 && g.rock(x, top - 4, z) == Geology.Rock.CALCITE) {
			return com.periodicelements.GeoRocks.CHALKY_SOIL.getDefaultState();
		}
		return null;
	}

	/**
	 * Fills the lake of a volcanic crater: water in an extinct volcano, lava over a floor of magma in an active one.
	 * Runs after the surface and again after the carvers (a cave must not drain it).
	 */
	static void fillCrater(Chunk chunk, BlockPos.Mutable pos, Geology g, int x, int z) {
		Geology.Column col = g.column(x, z);
		if (col.craterLake == Integer.MIN_VALUE) return;
		int ground = (int) Math.ceil(col.height) - 1;
		for (int y = ground - 3; y <= ground; y++) {
			pos.set(x, y, z);
			BlockState s = chunk.getBlockState(pos);
			if (open(s) || SOIL.contains(s.getBlock())) {
				chunk.setBlockState(pos, col.lava && y == ground ? Blocks.MAGMA_BLOCK.getDefaultState()
						: ROCKS.get(g.rock(x, y, z)), false);
			} else if (col.lava && y == ground) {
				chunk.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), false);
			}
		}
		BlockState fluid = (col.lava ? Blocks.LAVA : Blocks.WATER).getDefaultState();
		for (int y = ground + 1; y <= col.craterLake; y++) {
			pos.set(x, y, z);
			chunk.setBlockState(pos, fluid, false);
		}
	}

	/**
	 * Fills everything above the sea floor and under the water line with water. Vanilla aquifers wall off a hollow
	 * whose local water level differs from the sea with barrier walls; once the hollow is flooded those walls
	 * stood as thin meandering lines of land at sea level. The terrain itself never rises above its column height.
	 */
	public static void clearAboveSeaFloor(Chunk chunk, BlockPos.Mutable pos, int x, int z, double height) {
		if (height >= Geology.SEA - 1) return;
		for (int y = Geology.SEA - 1; y > Math.ceil(height); y--) {
			pos.set(x, y, z);
			BlockState state = chunk.getBlockState(pos);
			if (!state.isOf(Blocks.WATER)) chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
		}
	}

	/**
	 * Fills open ground below sea level with water. Vanilla aquifers give shallow sea-floor hollows next to land a
	 * local water level of their own, often lower, which left dry pits under the sea along the gentle coasts.
	 */
	public static void flood(Chunk chunk, BlockPos.Mutable pos, int x, int lx, int z, int lz) {
		int surface = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, lx, lz);
		if (surface >= Geology.SEA - 1) return;
		pos.set(x, surface, z);
		if (chunk.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) chunk.setBlockState(pos, Blocks.DIRT.getDefaultState(), false);
		for (int y = surface + 1; y < Geology.SEA; y++) {
			pos.set(x, y, z);
			if (chunk.getBlockState(pos).isAir()) chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
		}
	}
}
