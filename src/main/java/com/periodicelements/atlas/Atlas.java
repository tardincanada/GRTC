package com.periodicelements.atlas;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Field Atlas: a map that draws the land its carrier walks past and gains features with research (map plan, stage 1:
 * levels 1 to 3). Unlocks: item:field_atlas (Surveying), map:compass, map:latitude, map:copy, map:longitude,
 * map:contours, map:geology, item:survey_balloon; item:vanilla_map and item:vanilla_compass gate the vanilla recipes.
 */
public final class Atlas {
	public static Item FIELD_ATLAS, ATLAS_COPY, SURVEY_BALLOON, CORE_DRILL, MAGNETOMETER;
	public static net.minecraft.block.Block RADIO_TRANSMITTER;
	// Stage 3 (levels 6 and 7).
	public static Item HANDHELD_RADAR, GPS_RECEIVER, SATELLITE;
	public static net.minecraft.block.Block RADAR_STATION, TRACKING_STATION, LAUNCH_PAD;
	public static net.minecraft.block.entity.BlockEntityType<StationBlockEntity> STATION_ENTITY;

	/** Drawing reach around the carrier, in blocks: a sketch by eye, then with a quadrant. */
	public static final int REACH = 24, REACH_QUADRANT = 48, BALLOON_REACH = 128;
	public static final int MARKERS = 8, MARKERS_THEODOLITE = 32;
	private static final int TICKS = 20;

	private static final Map<UUID, long[]> LAST = new HashMap<>();

	private Atlas() {
	}

	public static boolean has(Set<String> known, String feature) {
		return Research.allows(known, "map:" + feature);
	}

	public static boolean canDraw(Set<String> known) {
		return Research.allows(known, "item:field_atlas");
	}

	public static boolean knows(PlayerEntity player, String feature) {
		return has(ResearchAccess.known(player), feature);
	}

	public static int reach(Set<String> known) {
		return has(known, "latitude") ? REACH_QUADRANT : REACH;
	}

	public static int markerLimit(Set<String> known) {
		return has(known, "contours") ? MARKERS_THEODOLITE : MARKERS;
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	public static void register(List<Item> items) {
		FIELD_ATLAS = Registry.register(Registries.ITEM, id("field_atlas"), new FieldAtlasItem(new Item.Settings().maxCount(1)));
		ATLAS_COPY = Registry.register(Registries.ITEM, id("atlas_copy"), new AtlasCopyItem(new Item.Settings().maxCount(1)));
		SURVEY_BALLOON = Registry.register(Registries.ITEM, id("survey_balloon"), new SurveyBalloonItem(new Item.Settings().maxCount(16)));
		items.add(FIELD_ATLAS);
		items.add(ATLAS_COPY);
		items.add(SURVEY_BALLOON);
		CORE_DRILL = Registry.register(Registries.ITEM, id("core_drill"), new CoreDrillItem(new Item.Settings().maxDamage(64)));
		MAGNETOMETER = Registry.register(Registries.ITEM, id("prospecting_magnetometer"), new MagnetometerItem(new Item.Settings().maxCount(1)));
		items.add(CORE_DRILL);
		items.add(MAGNETOMETER);
		RADIO_TRANSMITTER = Registry.register(Registries.BLOCK, id("radio_transmitter"), new RadioTransmitterBlock(
				net.minecraft.block.AbstractBlock.Settings.create().mapColor(net.minecraft.block.MapColor.IRON_GRAY).strength(2.5f)
						.requiresTool().sounds(net.minecraft.sound.BlockSoundGroup.METAL)));
		items.add(Registry.register(Registries.ITEM, id("radio_transmitter"), new com.periodicelements.research.GatedBlockItem(
				RADIO_TRANSMITTER, new Item.Settings(), known -> Research.allows(known, "item:radio_transmitter"),
				Research.get("radio_direction_finding").name())));
		HANDHELD_RADAR = Registry.register(Registries.ITEM, id("handheld_radar"), new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		GPS_RECEIVER = Registry.register(Registries.ITEM, id("gps_receiver"), new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		SATELLITE = Registry.register(Registries.ITEM, id("satellite"), new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		items.add(HANDHELD_RADAR);
		items.add(GPS_RECEIVER);
		items.add(SATELLITE);
		var metal = net.minecraft.block.AbstractBlock.Settings.create().mapColor(net.minecraft.block.MapColor.IRON_GRAY).strength(3f)
				.requiresTool().sounds(net.minecraft.sound.BlockSoundGroup.METAL);
		RADAR_STATION = gatedBlock(items, "radar_station", new StationBlock(metal, StationBlock.Kind.RADAR), "radar");
		TRACKING_STATION = gatedBlock(items, "tracking_station", new StationBlock(metal, StationBlock.Kind.TRACKING), "satellite");
		LAUNCH_PAD = gatedBlock(items, "launch_pad", new LaunchPadBlock(net.minecraft.block.AbstractBlock.Settings.create()
				.mapColor(net.minecraft.block.MapColor.STONE_GRAY).strength(3f).requiresTool()), "satellite");
		STATION_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("atlas_station"),
				net.minecraft.block.entity.BlockEntityType.Builder.create(StationBlockEntity::new, RADAR_STATION, TRACKING_STATION).build(null));
	}

	private static net.minecraft.block.Block gatedBlock(List<Item> items, String name, net.minecraft.block.Block block, String project) {
		net.minecraft.block.Block b = Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new com.periodicelements.research.GatedBlockItem(
				b, new Item.Settings(), known -> Research.allows(known, "item:" + name), Research.get(project).name())));
		return b;
	}

	/** Packets and the drawing tick; called from the main initializer. */
	public static void init() {
		PayloadTypeRegistry.playC2S().register(AtlasPayloads.Request.ID, AtlasPayloads.Request.CODEC);
		PayloadTypeRegistry.playC2S().register(AtlasPayloads.EditMarker.ID, AtlasPayloads.EditMarker.CODEC);
		PayloadTypeRegistry.playS2C().register(AtlasPayloads.Tile.ID, AtlasPayloads.Tile.CODEC);
		PayloadTypeRegistry.playS2C().register(AtlasPayloads.Markers.ID, AtlasPayloads.Markers.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(AtlasPayloads.Request.ID, (p, ctx) -> answer(ctx.player(), p));
		ServerPlayNetworking.registerGlobalReceiver(AtlasPayloads.EditMarker.ID, (p, ctx) -> editMarker(ctx.player(), p));
		PayloadTypeRegistry.playS2C().register(AtlasPayloads.Overlay.ID, AtlasPayloads.Overlay.CODEC);
		Prospecting.register();
		Satellites.init();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % TICKS == 0) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					tickPlayer(player);
				}
			}
		});
	}

	public static boolean carriesAtlas(PlayerEntity player) {
		var inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(FIELD_ATLAS)) return true;
		}
		return false;
	}

	/** Forgets where a player was last drawn, so the next tick draws again (tests). */
	public static void forget(UUID player) {
		LAST.remove(player);
	}

	public static void tickPlayer(ServerPlayerEntity player) {
		if (player.getWorld().getRegistryKey() != World.OVERWORLD || player.isSpectator() || !carriesAtlas(player)) {
			return;
		}
		Set<String> known = ResearchState.get(player.getServer()).known(player.getUuid());
		if (!canDraw(known)) {
			return;
		}
		int x = player.getBlockX(), z = player.getBlockZ();
		long now = player.getServer().getTicks();
		long[] last = LAST.get(player.getUuid());
		if (last != null && Math.abs(last[0] - x) < 4 && Math.abs(last[1] - z) < 4 && now - last[2] < 200) {
			return;
		}
		LAST.put(player.getUuid(), new long[]{x, z, now});
		if (has(known, "geiger_log") && player.getInventory().contains(new ItemStack(com.periodicelements.ModContent.GEIGER_COUNTER))) {
			Prospecting.logRadiation(player);
			if (has(known, "isotopes")) Prospecting.aerial(player);
		}
		record(player.getServerWorld(), AtlasData.of(player.getServer(), player.getUuid()), x, z, reach(known),
				has(known, "geology"));
	}

	/**
	 * Draws every loaded column within {@code r} of (cx, cz): its top colour and height, and with {@code rocks} the
	 * first rock under it. Returns how many columns changed.
	 */
	public static int record(ServerWorld world, AtlasData data, int cx, int cz, int r, boolean rocks) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int bottom = world.getBottomY();
		int changed = 0;
		Map<Long, WorldChunk> chunks = new HashMap<>();
		for (int dz = -r; dz <= r; dz++) {
			for (int dx = -r; dx <= r; dx++) {
				if (dx * dx + dz * dz > r * r) continue;
				int x = cx + dx, z = cz + dz;
				long ck = ((long) (x >> 4) << 32) | ((z >> 4) & 0xFFFFFFFFL);
				WorldChunk chunk = chunks.computeIfAbsent(ck, k -> world.getChunkManager().getWorldChunk(x >> 4, z >> 4));
				if (chunk == null) continue;
				int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 15, z & 15);
				MapColor color = MapColor.CLEAR;
				BlockState state = null;
				for (int k = 0; k < 10 && y >= bottom; k++, y--) {
					state = chunk.getBlockState(pos.set(x, y, z));
					color = state.getMapColor(world, pos);
					if (color != MapColor.CLEAR) break;
				}
				if (color == MapColor.CLEAR || y < bottom) continue;
				AtlasTile tile = data.tile(x, z, true);
				int i = AtlasTile.index(x, z);
				boolean diff = tile.color[i] != (byte) color.id || tile.height[i] != (short) y;
				if (diff) {
					tile.color[i] = (byte) color.id;
					tile.height[i] = (short) y;
				}
				if (rocks && tile.rock[i] == Rocks.UNKNOWN) {
					int rock = Rocks.UNKNOWN;
					boolean wet = !state.getFluidState().isEmpty();
					for (int d = 0; d <= Rocks.DEPTH && y - d >= bottom; d++) {
						BlockState s = chunk.getBlockState(pos.set(x, y - d, z));
						rock = Rocks.of(s.getBlock());
						if (rock != Rocks.UNKNOWN) break;
						wet |= !s.getFluidState().isEmpty();
					}
					if (rock == Rocks.UNKNOWN && !wet) rock = Rocks.SOIL;
					if (rock != Rocks.UNKNOWN) {
						tile.rock[i] = (byte) rock;
						diff = true;
					}
				}
				if (diff) {
					changed++;
					tile.rev++;
				}
			}
		}
		if (changed > 0) data.markDirty();
		return changed;
	}

	private static void answer(ServerPlayerEntity player, AtlasPayloads.Request req) {
		AtlasData data = AtlasData.of(player.getServer(), player.getUuid());
		int n = Math.min(Math.min(req.keys().size(), req.revs().size()), 96);
		for (int i = 0; i < n; i++) {
			AtlasTile t = data.tiles.get(req.keys().get(i));
			if (t != null && t.rev != req.revs().get(i)) {
				ServerPlayNetworking.send(player, new AtlasPayloads.Tile(t.key(), t.rev, t.color.clone(), t.heightBytes(), t.rock.clone()));
			}
		}
		if (req.overlayRev() != data.overlayRev) {
			sendOverlay(player, data, req.keys());
		}
		sendMarkers(player, data);
	}

	/** The overlay layers of every chunk inside the requested tiles (a tile holds 8 x 8 chunks). */
	static void sendOverlay(ServerPlayerEntity player, AtlasData data, List<Long> tiles) {
		java.util.Set<Long> want = new java.util.HashSet<>(tiles);
		List<AtlasPayloads.ChunkEntry> out = new java.util.ArrayList<>();
		for (var e : data.chunks.entrySet()) {
			int cx = (int) (e.getKey() >> 32), cz = (int) (long) e.getKey();
			if (!want.contains(AtlasTile.key(cx >> 3, cz >> 3)) || out.size() >= 20000) continue;
			AtlasData.ChunkInfo c = e.getValue();
			out.add(new AtlasPayloads.ChunkEntry(e.getKey(), c.rad, c.magnetic, c.dense, Prospecting.oreList(c), c.cored,
					new java.util.HashMap<>(c.isotopes)));
		}
		ServerPlayNetworking.send(player, new AtlasPayloads.Overlay(data.overlayRev, out));
	}

	static void sendMarkers(ServerPlayerEntity player, AtlasData data) {
		Set<String> known = ResearchState.get(player.getServer()).known(player.getUuid());
		ServerPlayNetworking.send(player, new AtlasPayloads.Markers(List.copyOf(data.markers), markerLimit(known), shared(player, known)));
	}

	/** Markers shared by radio: from every transmitter within range whose owner is someone else. */
	public static List<AtlasData.Marker> shared(ServerPlayerEntity player, Set<String> known) {
		List<AtlasData.Marker> out = new java.util.ArrayList<>();
		if (!has(known, "radio") || player.getWorld().getRegistryKey() != World.OVERWORLD) return out;
		MinecraftServer server = player.getServer();
		java.util.Set<UUID> owners = new java.util.LinkedHashSet<>();
		RadioState.get(server).transmitters.forEach((pos, owner) -> {
			if (pos.getSquaredDistance(player.getPos()) <= (double) RadioState.RANGE * RadioState.RANGE) {
				if (!owner.equals(player.getUuid())) {
					owners.add(owner);
					out.add(new AtlasData.Marker(ownerName(server, owner), pos.getX(), pos.getZ()));
				}
			}
		});
		for (UUID o : owners) {
			String who = ownerName(server, o);
			for (AtlasData.Marker m : AtlasData.of(server, o).markers) {
				// 3.51.1: put together on the client, in the reader's language (not the server's)
				out.add(new AtlasData.Marker(SHARED + m.name() + "\n" + who, m.x(), m.z()));
			}
		}
		return out;
	}

	/** Prefix of a marker shared by another player: "#shared:" name "\n" owner. */
	public static final String SHARED = "#shared:";

	private static String ownerName(MinecraftServer server, UUID id) {
		ServerPlayerEntity online = server.getPlayerManager().getPlayer(id);
		if (online != null) return online.getName().getString();
		return server.getUserCache() == null ? "?" : server.getUserCache().getByUuid(id).map(com.mojang.authlib.GameProfile::getName).orElse("?");
	}

	public static void editMarker(ServerPlayerEntity player, AtlasPayloads.EditMarker edit) {
		Set<String> known = ResearchState.get(player.getServer()).known(player.getUuid());
		if (!canDraw(known)) return;
		AtlasData data = AtlasData.of(player.getServer(), player.getUuid());
		if (edit.add()) {
			if (countMarkers(data.markers) >= markerLimit(known)) {
				player.sendMessage(net.minecraft.text.Text.translatable("gui.periodic_elements.atlas.marker.full"), true);
			} else {
				String name = edit.name().strip();
				data.markers.add(new AtlasData.Marker(name.substring(0, Math.min(32, name.length())), edit.x(), edit.z()));
			}
		} else {
			AtlasData.Marker best = null;
			double bestD = 64;
			for (AtlasData.Marker m : data.markers) {
				double d = Math.hypot(m.x() - edit.x(), m.z() - edit.z());
				if (d < bestD) {
					bestD = d;
					best = m;
				}
			}
			if (best != null) data.markers.remove(best);
		}
		data.markDirty();
		sendMarkers(player, data);
	}

	/** Markers that count toward the limit: all but the GPS death marker. */
	public static long countMarkers(List<AtlasData.Marker> markers) {
		return markers.stream().filter(m -> !m.name().equals(Satellites.DEATH)).count();
	}

	/** Whether a crafted vanilla item is held back by research (the map and the compass). */
	public static String vanillaGate(ItemStack stack) {
		if (stack.isOf(net.minecraft.item.Items.MAP)) return "item:vanilla_map";
		if (stack.isOf(net.minecraft.item.Items.COMPASS)) return "item:vanilla_compass";
		return null;
	}

	public static MinecraftServer serverOf(PlayerEntity p) {
		return p instanceof ServerPlayerEntity sp ? sp.getServer() : null;
	}
}
