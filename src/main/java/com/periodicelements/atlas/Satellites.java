package com.periodicelements.atlas;

import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Map plan, stage 3 (levels 6 and 7): radar contacts, satellite mapping from Tracking Stations, and the GPS death
 * marker. Unlocks: map:radar, map:minimap (Radar), map:isotopes (gamma spectrometry), map:satellite, map:landsat,
 * map:gps.
 */
public final class Satellites {
	/** Radar reach: a station, and the handheld set. */
	public static final int STATION_RANGE = 96, HANDHELD_RANGE = 48;
	/**
	 * Tracking: a spiral of 8 x 8 cells reaching 512 blocks out (1024 across), one cell a tick per satellite (up to 4):
	 * 16,641 cells, under a day's 24,000 ticks with one satellite. At most {@link #BUDGET_NANOS} a tick go to it.
	 */
	public static final int CELL = 8, RINGS = 64, CELLS = (2 * RINGS + 1) * (2 * RINGS + 1), PER_TICK = 1, MAX_SATELLITES = 4;
	public static final long BUDGET_NANOS = 3_000_000L;
	public static final String DEATH = "#death";
	private static final Set<UUID> HAD_CONTACTS = new HashSet<>();

	private Satellites() {
	}

	public static void init() {
		PayloadTypeRegistry.playS2C().register(AtlasPayloads.Contacts.ID, AtlasPayloads.Contacts.CODEC);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			track(server, 1);
			if (server.getTicks() % 10 == 0) {
				for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) radar(p);
			}
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity p) died(p);
		});
	}

	static boolean carries(PlayerEntity p, Item item) {
		var inv = p.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(item)) return true;
		}
		return false;
	}

	// --- Radar -------------------------------------------------------------------------------------------------

	/** Everything the player's radars see now: a handheld set round the player, and their powered stations. */
	public static List<AtlasPayloads.Contact> contacts(ServerPlayerEntity p) {
		List<AtlasPayloads.Contact> out = new ArrayList<>();
		if (p.getWorld().getRegistryKey() != World.OVERWORLD || !Atlas.knows(p, "radar")) return out;
		ServerWorld w = p.getServerWorld();
		List<Box> boxes = new ArrayList<>();
		if (carries(p, Atlas.HANDHELD_RADAR)) boxes.add(new Box(p.getBlockPos()).expand(HANDHELD_RANGE));
		StationState.get(p.getServer()).stations.forEach((pos, s) -> {
			if (s.kind == StationBlock.Kind.RADAR && s.powered && s.owner.equals(p.getUuid()) && w.isChunkLoaded(pos)) {
				boxes.add(new Box(pos).expand(STATION_RANGE));
			}
		});
		Set<Integer> seen = new HashSet<>();
		for (Box b : boxes) {
			double r = (b.maxX - b.minX) / 2;
			double cx = (b.minX + b.maxX) / 2, cz = (b.minZ + b.maxZ) / 2;
			for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, b, e -> e.isAlive() && e != p && !e.isSpectator())) {
				if (Math.pow(e.getX() - cx, 2) + Math.pow(e.getZ() - cz, 2) > r * r || !seen.add(e.getId())) continue;
				int kind = e instanceof PlayerEntity ? 0 : e instanceof Monster ? 1 : 2;
				out.add(new AtlasPayloads.Contact(e.getBlockX(), e.getBlockZ(), kind));
				if (out.size() >= 256) return out;
			}
		}
		return out;
	}

	private static void radar(ServerPlayerEntity p) {
		List<AtlasPayloads.Contact> c = contacts(p);
		if (c.isEmpty() && !HAD_CONTACTS.contains(p.getUuid())) return;
		if (c.isEmpty()) HAD_CONTACTS.remove(p.getUuid());
		else HAD_CONTACTS.add(p.getUuid());
		if (ServerPlayNetworking.canSend(p, AtlasPayloads.Contacts.ID)) {
			ServerPlayNetworking.send(p, new AtlasPayloads.Contacts(c));
		}
	}

	// --- Satellites and tracking stations -------------------------------------------------------------------------

	/** The n-th cell of a square spiral round (0, 0): rings of growing size, so the land near the station comes first. */
	public static int[] spiral(int n) {
		if (n == 0) return new int[]{0, 0};
		int k = (int) Math.ceil((Math.sqrt(n + 1) - 1) / 2);
		int t = 2 * k + 1;
		int m = t * t;
		t -= 1;
		if (n + 1 >= m - t) return new int[]{k - (m - n - 1), -k};
		m -= t;
		if (n + 1 >= m - t) return new int[]{-k, -k + (m - n - 1)};
		m -= t;
		if (n + 1 >= m - t) return new int[]{-k + (m - n - 1), k};
		return new int[]{k, k - (m - n - 1 - t)};
	}

	/** Runs {@code ticks} ticks of every Tracking Station that has power and an owner with a satellite up. */
	public static void track(MinecraftServer server, int ticks) {
		StationState st = StationState.get(server);
		if (st.stations.isEmpty() || st.satellites.isEmpty()) return;
		ServerWorld w = server.getOverworld();
		boolean dirty = false;
		long start = System.nanoTime();
		for (var e : st.stations.entrySet()) {
			StationState.Station s = e.getValue();
			int sats = Math.min(MAX_SATELLITES, st.satellites(s.owner));
			if (s.kind != StationBlock.Kind.TRACKING || !s.powered || sats == 0) continue;
			Set<String> known = ResearchState.get(server).known(s.owner);
			if (!Atlas.has(known, "satellite")) continue;
			AtlasData data = AtlasData.of(server, s.owner);
			boolean landsat = Atlas.has(known, "landsat");
			for (int k = 0; k < PER_TICK * sats * ticks; k++) {
				if (ticks == 1 && k > 0 && System.nanoTime() - start > BUDGET_NANOS) break;
				int[] c = spiral(s.progress);
				survey(w, data, e.getKey().getX() + c[0] * CELL, e.getKey().getZ() + c[1] * CELL, landsat);
				s.progress = (s.progress + 1) % CELLS;
			}
			dirty = true;
		}
		if (dirty) st.markDirty();
	}

	/**
	 * What a satellite sees of one 4 x 4 cell. Loaded land is drawn as the Atlas would draw it on foot (and with the
	 * multispectral scanner its rock and the ores near the surface); land nobody has loaded is estimated from the
	 * world's own generator: its height, and a colour from its biome. Real drawings are never overwritten by estimates.
	 */
	public static void survey(ServerWorld w, AtlasData data, int x0, int z0, boolean landsat) {
		x0 = Math.floorDiv(x0, CELL) * CELL;
		z0 = Math.floorDiv(z0, CELL) * CELL;
		WorldChunk chunk = w.getChunkManager().getWorldChunk(x0 >> 4, z0 >> 4);
		if (chunk != null) {
			for (int dz = 0; dz < CELL; dz++) {
				for (int dx = 0; dx < CELL; dx++) {
					Atlas.record(w, data, x0 + dx, z0 + dz, 0, landsat);
				}
			}
			if (landsat && (x0 & 15) == 0 && (z0 & 15) == 0) shallowOres(w, data, chunk);
			return;
		}
		AtlasTile tile = data.tile(x0, z0, true);
		boolean blank = false;
		for (int dz = 0; dz < CELL && !blank; dz++) {
			for (int dx = 0; dx < CELL; dx++) {
				if (tile.color[AtlasTile.index(x0 + dx, z0 + dz)] == 0) {
					blank = true;
					break;
				}
			}
		}
		if (!blank) return;
		ChunkGenerator gen = w.getChunkManager().getChunkGenerator();
		NoiseConfig nc = w.getChunkManager().getNoiseConfig();
		int cx = x0 + CELL / 2, cz = z0 + CELL / 2;
		int top = groundAt(w, nc, cx, cz);
		boolean water = top < w.getSeaLevel() - 1;
		if (water) top = w.getSeaLevel() - 1;
		RegistryEntry<Biome> biome = gen.getBiomeSource().getBiome(BiomeCoords.fromBlock(cx), BiomeCoords.fromBlock(top),
				BiomeCoords.fromBlock(cz), nc.getMultiNoiseSampler());
		MapColor color = water ? MapColor.WATER_BLUE : estimate(biome, top, w.getSeaLevel());
		int changed = 0;
		for (int dz = 0; dz < CELL; dz++) {
			for (int dx = 0; dx < CELL; dx++) {
				int i = AtlasTile.index(x0 + dx, z0 + dz);
				if (tile.color[i] != 0) continue;
				tile.color[i] = (byte) color.id;
				tile.height[i] = (short) top;
				changed++;
			}
		}
		if (changed > 0) {
			tile.rev++;
			data.markDirty();
		}
	}

	/**
	 * The ground's height at (x, z) as the world's noise would make it, without building the chunk: the final density
	 * read top down every 8 blocks, then block by block. Far cheaper than the generator's own column sample; caves
	 * and overhangs near the top can fool it, which a satellite estimate can live with.
	 */
	static int groundAt(ServerWorld w, NoiseConfig nc, int x, int z) {
		net.minecraft.world.gen.densityfunction.DensityFunction f = nc.getNoiseRouter().finalDensity();
		int bottom = w.getBottomY(), topY = w.getTopY() - 1;
		int y = Math.min(topY, 200);
		if (f.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(x, y, z)) > 0) {
			while (y < topY && f.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(x, y + 8, z)) > 0) y += 8;
			y = Math.min(topY, y + 8);
		}
		while (y > bottom && f.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(x, y, z)) <= 0) y -= 8;
		for (int k = 0; k < 8 && y + 1 <= topY
				&& f.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(x, y + 1, z)) > 0; k++) {
			y++;
		}
		return y;
	}

	/** The colour a biome shows from orbit. */
	static MapColor estimate(RegistryEntry<Biome> b, int top, int sea) {
		if (b.isIn(BiomeTags.IS_OCEAN) || b.isIn(BiomeTags.IS_RIVER)) return top < sea ? MapColor.WATER_BLUE : MapColor.PALE_YELLOW;
		if (b.isIn(BiomeTags.IS_BEACH) || b.matchesKey(BiomeKeys.DESERT)) return MapColor.PALE_YELLOW;
		if (b.isIn(BiomeTags.IS_BADLANDS)) return MapColor.ORANGE;
		if (b.value().getTemperature() < 0.15f) return MapColor.WHITE;
		if (top > 150) return MapColor.STONE_GRAY;
		if (b.isIn(BiomeTags.IS_JUNGLE) || b.isIn(BiomeTags.IS_FOREST) || b.isIn(BiomeTags.IS_TAIGA)
				|| b.matchesKey(BiomeKeys.DARK_FOREST) || b.matchesKey(BiomeKeys.SWAMP) || b.matchesKey(BiomeKeys.MANGROVE_SWAMP)) {
			return MapColor.DARK_GREEN;
		}
		if (b.matchesKey(BiomeKeys.SAVANNA) || b.matchesKey(BiomeKeys.SAVANNA_PLATEAU)) return MapColor.TERRACOTTA_YELLOW;
		return MapColor.PALE_GREEN;
	}

	/** The multispectral scanner: ores within 8 blocks of the surface, all over a loaded chunk. */
	public static void shallowOres(ServerWorld w, AtlasData data, WorldChunk chunk) {
		BlockPos.Mutable m = new BlockPos.Mutable();
		Map<String, Integer> found = new java.util.TreeMap<>();
		int bx = chunk.getPos().getStartX(), bz = chunk.getPos().getStartZ();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int top = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				for (int y = top - 1; y >= top - 8 && y >= w.getBottomY(); y--) {
					BlockState s = chunk.getBlockState(m.set(bx + x, y, bz + z));
					if (Prospecting.isOre(s)) found.merge(Prospecting.oreId(s.getBlock()), 1, Integer::sum);
				}
			}
		}
		if (found.isEmpty()) return;
		AtlasData.ChunkInfo c = data.chunk(bx, bz);
		boolean ch = false;
		for (var e : found.entrySet()) {
			Integer old = c.ores.get(e.getKey());
			if (old == null || old < e.getValue()) {
				c.ores.put(e.getKey(), e.getValue());
				ch = true;
			}
		}
		if (ch) data.overlayChanged();
	}

	/** What a station says when used: whether it has power, and for a tracking station how far it has mapped. */
	public static Text describe(ServerWorld w, BlockPos pos, StationBlock.Kind kind) {
		boolean powered = w.getBlockEntity(pos) instanceof StationBlockEntity be && be.powered();
		if (!powered) return Text.translatable("gui.periodic_elements.atlas.station.no_power");
		if (kind == StationBlock.Kind.RADAR) {
			return Text.translatable("gui.periodic_elements.atlas.radar.on", STATION_RANGE);
		}
		StationState st = StationState.get(w.getServer());
		StationState.Station s = st.stations.get(pos);
		if (s == null || st.satellites(s.owner) == 0) return Text.translatable("gui.periodic_elements.atlas.tracking.none");
		int ring = (int) Math.ceil((Math.sqrt(s.progress + 1) - 1) / 2);
		return Text.translatable("gui.periodic_elements.atlas.tracking.on", st.satellites(s.owner), ring * CELL);
	}

	// --- GPS ---------------------------------------------------------------------------------------------------

	/** With GPS, where a player dies goes on their Atlas as a marker (one at a time; it does not count toward the limit). */
	public static void died(ServerPlayerEntity p) {
		if (p.getWorld().getRegistryKey() != World.OVERWORLD || !Atlas.knows(p, "gps")) return;
		AtlasData d = AtlasData.of(p.getServer(), p.getUuid());
		d.markers.removeIf(m -> m.name().equals(DEATH));
		d.markers.add(new AtlasData.Marker(DEATH, p.getBlockX(), p.getBlockZ()));
		d.markDirty();
	}
}
