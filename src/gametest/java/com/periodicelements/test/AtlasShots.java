package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.client.AtlasScreen;
import com.periodicelements.research.ResearchState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

/**
 * Dev-only (./gradlew runAtlasShots): makes a Geological world, draws 150 blocks round spawn on the Atlas and
 * screenshots the map as it looks at each stage of research, plus the compass bar, to run/screenshots.
 */
public class AtlasShots implements ClientModInitializer {
	private static final RegistryKey<net.minecraft.world.gen.WorldPreset> PRESET = RegistryKey.of(RegistryKeys.WORLD_PRESET,
			Identifier.of(PeriodicElements.MOD_ID, "geological"));
	private static final String[][] STAGES = {
			{"atlas_1_sketch", "surveying"},
			{"atlas_2_compass", "iron_tools", "magnetic_compass", "astrolabe", "portolan"},
			{"atlas_3_theodolite", "chronometer", "triangulation", "hot_air_balloon"},
			{"atlas_3_rocks", "geological_map"},
			{"atlas_4_ores", "bessemer", "core_drilling"},
			{"atlas_4_radiation", "radioactivity", "radiation_mapping"},
			{"atlas_5_anomalies", "dynamo", "magnetic_prospecting", "geiger_survey", "radio_direction_finding"},
			{"atlas_hud"},
			{"atlas_6_isotopes", "crystal_growth", "gamma_spectrometry"},
			{"atlas_6_radar", "radar"},
			{"atlas_6_minimap"},
			{"atlas_7_satellite", "solar_cell", "satellite", "integrated_circuits", "landsat"},
			{"atlas_7_gps", "gps"},
	};
	private static final int START = 540, EACH = 120;

	private int tick = -1, worldTick = -1;
	private volatile boolean ready, waiting;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.atlasshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) return;
			tick++;
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 60 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("atlas");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.setSeed(System.getProperty("pe.terrainseed", "1789"));
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(PRESET))
						.findFirst().ifPresent(wc::setWorldType);
				screen.children().stream().filter(c -> c instanceof ButtonWidget b
								&& b.getMessage().getString().equals(net.minecraft.text.Text.translatable("selectWorld.create").getString()))
						.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			return;
		}
		if (client.player == null) return;
		worldTick++;
		var server = client.getServer();
		UUID id = client.player.getUuid();
		if (worldTick == 40) {
			client.options.getViewDistance().setValue(16);
			server.execute(() -> {
				var p = server.getPlayerManager().getPlayer(id);
				for (String c : List.of("gamerule doDaylightCycle false", "gamerule doMobSpawning false",
						"gamerule periodicRadiation off", "time set 6000", "weather clear", "gamemode survival")) {
					server.getCommandManager().executeWithPrefix(p.getCommandSource().withLevel(4), c);
				}
				p.getAbilities().allowFlying = true;
				p.getInventory().setStack(p.getInventory().selectedSlot, new ItemStack(Atlas.FIELD_ATLAS));
			});
		}
		if (worldTick == 500) {
			server.execute(() -> {
				var p = server.getPlayerManager().getPlayer(id);
				int n = Atlas.record(p.getServerWorld(), AtlasData.of(server, id), p.getBlockX(), p.getBlockZ(), 150, true);
				AtlasData.of(server, id).markers.add(new AtlasData.Marker("Camp", p.getBlockX() + 40, p.getBlockZ() - 25));
				PeriodicElements.LOGGER.info("[atlasshots] drew {} columns", n);
				// Stage 2 layers: cores on a grid, a radiation reading per chunk, magnetometer surveys round spawn.
				var w = p.getServerWorld();
				AtlasData d = AtlasData.of(server, id);
				int x0 = p.getBlockX(), z0 = p.getBlockZ();
				// Three buried pockets of pitchblende so the radiation layer has something to show.
				var pitch = net.minecraft.registry.Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "pitchblende_ore")).getDefaultState();
				for (int[] at : new int[][]{{-64, 32}, {48, 64}, {64, -64}, {-32, -96}}) {
					int bx = x0 + at[0], bz = z0 + at[1], by = w.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, bx, bz) - 3;
					for (int a = -2; a <= 2; a++) for (int b = -2; b <= 2; b++) for (int c = 0; c < 2; c++) {
						w.setBlockState(new net.minecraft.util.math.BlockPos(bx + a, by - c, bz + b), pitch);
					}
				}
				double px = p.getX(), py = p.getY(), pz = p.getZ();
				for (int dx = -128; dx <= 128; dx += 16) {
					for (int dz = -128; dz <= 128; dz += 16) {
						int x = x0 + dx, z = z0 + dz;
						int top = w.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);
						if (Math.floorMod(dx + dz, 32) == 0) {
							com.periodicelements.atlas.Prospecting.core(p, new net.minecraft.util.math.BlockPos(x, top - 1, z));
						}
						double rate = com.periodicelements.radiation.Radiation.surroundings(w, new net.minecraft.util.math.Vec3d(x + 0.5, top + 1, z + 0.5));
						d.chunk(x, z).rad = (float) (com.periodicelements.atlas.Prospecting.BACKGROUND + rate);
					}
				}
				for (int dx = -96; dx <= 96; dx += 96) {
					for (int dz = -96; dz <= 96; dz += 96) {
						p.setPosition(x0 + dx, py, z0 + dz);
						com.periodicelements.atlas.Prospecting.survey(p);
					}
				}
				p.setPosition(px, py, pz);
				d.overlayChanged();
				ready = true;
			});
		}
		if (worldTick < START) return;
		if (!ready) {
			worldTick = START - 1;
			return;
		}
		int i = (worldTick - START) / EACH, phase = (worldTick - START) % EACH;
		if (i >= STAGES.length) {
			PeriodicElements.LOGGER.info("[atlasshots] done");
			client.scheduleStop();
			return;
		}
		String[] stage = STAGES[i];
		if (waiting && phase > 0) {
			worldTick--;
			return;
		}
		boolean hud = stage[0].equals("atlas_hud") || stage[0].equals("atlas_6_minimap") || stage[0].equals("atlas_7_gps");
		if (phase == 0) {
			List<String> grant = List.of(stage).subList(1, stage.length);
			waiting = true;
			server.execute(() -> {
				ResearchState.get(server).grant(server, id, grant);
				stageSetup(server, server.getPlayerManager().getPlayer(id), stage[0]);
				waiting = false;
			});
			client.setScreen(null);
			if (stage[0].equals("atlas_7_gps")) {
				com.periodicelements.client.Minimap.show();
				AtlasData.of(server, id).markers.stream().filter(m -> m.name().equals("Camp")).findFirst()
						.ifPresent(com.periodicelements.client.AtlasClient::setRoute);
			}
		}
		if (phase == 20 && !hud) {
			com.periodicelements.client.AtlasClient.Layer layer = switch (stage[0]) {
				case "atlas_3_rocks" -> com.periodicelements.client.AtlasClient.Layer.GEOLOGY;
				case "atlas_4_ores" -> com.periodicelements.client.AtlasClient.Layer.ORES;
				case "atlas_4_radiation" -> com.periodicelements.client.AtlasClient.Layer.RADIATION;
				case "atlas_5_anomalies" -> com.periodicelements.client.AtlasClient.Layer.ANOMALIES;
				case "atlas_6_isotopes" -> com.periodicelements.client.AtlasClient.Layer.ISOTOPES;
				default -> com.periodicelements.client.AtlasClient.Layer.TERRAIN;
			};
			int zoom = stage[0].startsWith("atlas_1") || stage[0].startsWith("atlas_2") ? 1
					: stage[0].equals("atlas_6_radar") ? 3 : stage[0].equals("atlas_7_satellite") ? 0 : 2;
			AtlasScreen.preset(layer, zoom);
			client.setScreen(new AtlasScreen());
		}
		if (phase == EACH - 10) {
			ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + stage[0] + ".png", client.getFramebuffer(),
					text -> PeriodicElements.LOGGER.info("[atlasshots] {}", text.getString()));
		}
	}

	/** Server side of a stage 3 shot: what the research needs to show (on the server thread). */
	private static void stageSetup(net.minecraft.server.MinecraftServer server, net.minecraft.server.network.ServerPlayerEntity p, String stage) {
		if (p == null) return;
		var w = p.getServerWorld();
		AtlasData d = AtlasData.of(server, p.getUuid());
		int x0 = p.getBlockX(), z0 = p.getBlockZ();
		switch (stage) {
			case "atlas_6_isotopes" -> {
				for (int dx = -128; dx <= 128; dx += 16) {
					for (int dz = -128; dz <= 128; dz += 16) {
						int x = x0 + dx, z = z0 + dz;
						int top = w.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);
						com.periodicelements.atlas.Prospecting.reading(w, d, new net.minecraft.util.math.Vec3d(x + 0.5, top + 1, z + 0.5), true);
					}
				}
				d.overlayChanged();
			}
			case "atlas_6_radar" -> {
				p.getInventory().insertStack(new ItemStack(Atlas.HANDHELD_RADAR));
				var types = List.of(net.minecraft.entity.EntityType.COW, net.minecraft.entity.EntityType.SHEEP,
						net.minecraft.entity.EntityType.SPIDER, net.minecraft.entity.EntityType.PIG, net.minecraft.entity.EntityType.SPIDER);
				for (int k = 0; k < 14; k++) {
					double a = k * 2.4, r = 10 + k * 2.5;
					int x = x0 + (int) (Math.cos(a) * r), z = z0 + (int) (Math.sin(a) * r);
					int y = w.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
					var e = types.get(k % types.size()).create(w);
					if (e == null) continue;
					e.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0, 0);
					if (e instanceof net.minecraft.entity.mob.MobEntity m) m.setAiDisabled(true);
					w.spawnEntity(e);
				}
			}
			case "atlas_7_satellite" -> {
				var st = com.periodicelements.atlas.StationState.get(server);
				var station = st.add(p.getBlockPos(), com.periodicelements.atlas.StationBlock.Kind.TRACKING, p.getUuid());
				station.powered = true;
				st.satellites.put(p.getUuid(), 1);
				long t0 = System.nanoTime();
				com.periodicelements.atlas.Satellites.track(server, com.periodicelements.atlas.Satellites.CELLS / com.periodicelements.atlas.Satellites.PER_TICK + 1);
				station.powered = false;
				PeriodicElements.LOGGER.info("[atlasshots] satellite pass done: {} ms a cell",
						(System.nanoTime() - t0) / 1e6 / com.periodicelements.atlas.Satellites.CELLS);
			}
			case "atlas_7_gps" -> {
				p.getInventory().insertStack(new ItemStack(Atlas.GPS_RECEIVER));
				d.markers.add(new AtlasData.Marker(com.periodicelements.atlas.Satellites.DEATH, x0 - 22, z0 + 16));
			}
			default -> {
			}
		}
	}
}
