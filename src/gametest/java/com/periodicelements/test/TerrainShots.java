package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.worldgen.GeoWorld;
import com.periodicelements.worldgen.Geology;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Dev-only (./gradlew runTerrainShots): makes a new Geological world (seed from -Dpe.terrainseed, default 4242),
 * finds a granite dome, a mesa, a basalt plateau and neck, a mountain belt and an outcrop patch, and screenshots
 * each from the air to run/screenshots. Also screenshots the Create World screen with the world type shown.
 */
public class TerrainShots implements ClientModInitializer {
	private static final RegistryKey<net.minecraft.world.gen.WorldPreset> PRESET = RegistryKey.of(RegistryKeys.WORLD_PRESET,
			Identifier.of(PeriodicElements.MOD_ID, "geological"));
	private static final int WAIT = Integer.getInteger("pe.terrainwait", 600);

	private record Spot(String name, int x, int z, double h, double[] cam) {
		Spot(String name, int x, int z, double h) {
			this(name, x, z, h, null);
		}
	}

	private int tick = -1;
	private int worldTick = -1;
	private boolean createShot;
	private int pressedAt = -1;
	private final List<Spot> spots = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.terrainshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) {
				return;
			}
			tick++;
			if (pressedAt >= 0 && tick - pressedAt < 1200) {
				return; // the world is being created
			}
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 10 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				if (!createShot) {
					PeriodicElements.LOGGER.info("[terrainshots] default world type {}", wc.getWorldType().preset() == null ? null
							: wc.getWorldType().preset().getKey().map(k -> k.getValue().toString()).orElse("?"));
				}
				wc.setWorldName("geological");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.setSeed(System.getProperty("pe.terrainseed", "4242"));
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(PRESET))
						.findFirst().ifPresent(wc::setWorldType);
				if (!createShot) {
					createShot = true;
					return;
				}
				if (tick % 40 == 0) {
					shot(client, "terrain_create_world");
					press(client, net.minecraft.text.Text.translatable("selectWorld.create").getString());
					pressedAt = tick;
				}
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		if (worldTick == 40) {
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "gamerule periodicRadiation off");
			cmd(client, "time set 6000");
			cmd(client, "weather clear");
			cmd(client, "effect give @s minecraft:night_vision infinite 0 true");
			client.options.getViewDistance().setValue(20);
			findSpots(client);
		}
		if (worldTick == 50) {
			client.setScreen(null);
			client.options.hudHidden = true;
			client.getToastManager().clear();
			client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
			cmd(client, "gamemode spectator");
		}
		if (worldTick < 60) {
			return;
		}
		int i = (worldTick - 60) / WAIT, phase = (worldTick - 60) % WAIT;
		if (i >= spots.size()) {
			PeriodicElements.LOGGER.info("[terrainshots] done");
			client.scheduleStop();
			return;
		}
		Spot s = spots.get(i);
		if (phase == 200 && s.cam() != null && s.cam().length >= 11) { // a cut: fill x1 y1 z1 x2 y2 z2 with air, to look inside the rock
			double[] c = s.cam();
			cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d air%s", (int) c[5], (int) c[6], (int) c[7], (int) c[8], (int) c[9], (int) c[10],
					c.length >= 12 && c[11] == 1 ? " replace water" : ""));
		}
		if (phase == WAIT / 2 && s.cam() != null) {
			client.worldRenderer.reload();
		}
		if (phase == 0 && s.cam() != null) {
			double[] c = s.cam();
			client.options.getViewDistance().setValue(8);
			cmd(client, String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", c[0], c[1], c[2], c[3], c[4]));
			PeriodicElements.LOGGER.info("[terrainshots] {} at {} {} {}", s.name(), c[0], c[1], c[2]);
		} else if (phase == 0) {
			client.options.getViewDistance().setValue(20);
			boolean top = s.name().equals("outcrop") || s.name().startsWith("river") || s.name().equals("doline") || s.name().equals("skylight");
			boolean far = s.name().equals("panorama");
			boolean close = s.name().equals("doline") || s.name().equals("skylight");
			double dist = close ? 12 : top ? 25 : far ? 300 : 70 + s.h() * 0.3;
			double cx = s.x() - dist * 0.7, cz = s.z() + dist * 0.7, cy = Math.max(s.h(), 70) + (close ? 34 : top ? 70 : far ? 90 : 28 + s.h() * 0.1);
			double dx = s.x() - cx, dz = s.z() - cz, dy = Math.max(s.h() * 0.6, 64) - cy;
			float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
			float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
			cmd(client, String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", cx, cy, cz, yaw, pitch));
			PeriodicElements.LOGGER.info("[terrainshots] {} at {} {} h={}", s.name(), s.x(), s.z(), (int) s.h());
		}
		if (phase == WAIT - 5) {
			PeriodicElements.LOGGER.info("[terrainshots] screen={} pos={} chunks={}", client.currentScreen, client.player.getBlockPos(),
					client.worldRenderer.getCompletedChunkCount());
			shot(client, "terrain_" + s.name());
		}
	}

	/** Searches outward from spawn for one column of each kind. */
	private void findSpots(MinecraftClient client) {
		var world = client.getServer().getOverworld();
		Geology g = GeoWorld.geology(world.getChunkManager().getNoiseConfig());
		String camAt = System.getProperty("pe.terraincam");
		if (camAt != null) {
			String[] cams = camAt.split(";");
			for (int n = 0; n < cams.length; n++) {
				double[] c = java.util.Arrays.stream(cams[n].split(",")).mapToDouble(Double::parseDouble).toArray();
				spots.add(new Spot(n == 0 ? "cam" : "cam" + n, (int) c[0], (int) c[2], c[1], c));
			}
		}
		String at = System.getProperty("pe.terrainat");
		if (at != null) {
			String[] xz = at.split(",");
			int ax = Integer.parseInt(xz[0]), az = Integer.parseInt(xz[1]);
			for (int d = -16; d <= 16; d += 4) {
				var c = g.column(ax + d, az);
				PeriodicElements.LOGGER.info("[terrainshots] probe {} {} macro {} h {} ocean {} prov {}", ax + d, az, c.macro, c.height, c.ocean, c.province);
			}
			spots.add(new Spot("at", ax, az, g.column(ax, az).height));
		}
		find(g, "granite_dome", (x, z) -> g.column(x, z).dome && g.column(x, z).height > 115);
		find(g, "mesa", (x, z) -> g.column(x, z).province == Geology.Province.BASIN && g.column(x, z).plateau > 0.9
				&& g.column(x, z).mountain < 0.1 && g.column(x + 40, z).plateau < 0.1);
		find(g, "basalt_plateau", (x, z) -> g.column(x, z).province == Geology.Province.VOLCANIC && g.column(x, z).plateau > 0.9
				&& g.column(x, z).mountain < 0.1 && g.column(x + 50, z).plateau < 0.3);
		find(g, "basalt_neck", (x, z) -> g.column(x, z).neck && g.column(x, z).height > 120);
		find(g, "mountains", (x, z) -> g.column(x, z).mountain > 0.85 && g.column(x, z).height > 170);
		find(g, "outcrop", (x, z) -> g.outcropZone(x, z) && g.outcropZone(x + 20, z) && g.outcropZone(x - 20, z)
				&& g.column(x, z).height > 66 && g.column(x, z).mountain < 0.1);
		find(g, "upland_hills", (x, z) -> g.column(x, z).province == Geology.Province.CRATON && g.column(x, z).macro > 95
				&& g.column(x, z).mountain < 0.05 && !g.column(x, z).dome);
		find(g, "panorama", (x, z) -> g.column(x, z).mountain > 0.6 && g.column(x, z).height > 150
				&& g.column(x - 210, z + 210).height < 110);
		find(g, "ocean_shallows", (x, z) -> Math.hypot(x, z) > 1500 && g.column(x, z).ocean > 0.7 && g.column(x, z).height > 60);
		find(g, "deep_ocean", (x, z) -> Math.hypot(x, z) > 1500 && g.column(x, z).ocean > 0.95);
		find(g, "river", (x, z) -> g.column(x, z).river && g.column(x, z).riverWater > Geology.SEA + 6
				&& g.column(x + 6, z).river && g.column(x, z + 6).river);
		volcano(g, "volcano_active", true);
		volcano(g, "volcano", false);
		find(g, "river_placer", (x, z) -> g.column(x, z).river && g.column(x, z).riverWater > Geology.SEA + 2
				&& g.column(x + 4, z).river && com.periodicelements.worldgen.Deposits.nearestWith(g, x, z, "gold") < 90);
		find(g, "river_mouth", (x, z) -> g.column(x, z).river && g.column(x, z).riverWater <= Geology.SEA + 2
				&& g.column(x + 4, z).river && g.column(x, z + 4).river);
		find(g, "doline", (x, z) -> g.karstAt(x, z) && g.collapseShaft(x, z));
		// 3.42.0: soils by rock
		find(g, "terra_roxa", (x, z) -> soil(g, x, z, com.periodicelements.GeoRocks.TERRA_ROXA) && soil(g, x + 16, z, com.periodicelements.GeoRocks.TERRA_ROXA)
				&& soil(g, x, z + 16, com.periodicelements.GeoRocks.TERRA_ROXA));
		find(g, "chalky_soil", (x, z) -> soil(g, x, z, com.periodicelements.GeoRocks.CHALKY_SOIL) && soil(g, x + 16, z, com.periodicelements.GeoRocks.CHALKY_SOIL)
				&& soil(g, x, z + 16, com.periodicelements.GeoRocks.CHALKY_SOIL));
		cave(g, "karst_cave", 64, false);
		cave(g, "flooded_cave", 50, true);
		tube(g);
		find(g, "skylight", (x, z) -> g.basaltAt(x, z) && g.caves(x, (int) g.column(x, z).height, z) < -0.5
				&& g.caves(x, (int) g.column(x, z).height - 8, z) < -0.3);
		find(g, "coast", (x, z) -> g.column(x, z).height > 63 && g.column(x, z).height < 66 && g.column(x + 60, z).height < 50);
	}

	/** A karst chamber: dry ones from {@code y0} up, or one the water line crosses; the camera is put inside. */
	private void cave(Geology g, String name, int y0, boolean flooded) {
		String only = System.getProperty("pe.terrainonly");
		if (only != null && !java.util.Arrays.asList(only.split(",")).contains(name)) return;
		for (int r = 0; r <= 6000; r += 24) {
			int steps = Math.max(1, (int) (2 * Math.PI * r / 24));
			for (int k = 0; k < steps; k++) {
				double a = 2 * Math.PI * k / steps;
				int x = (int) (r * Math.cos(a)), z = (int) (r * Math.sin(a));
				if (!g.karstAt(x, z)) continue;
				double h = g.column(x, z).height;
				for (int y = y0; y < (flooded ? Geology.SEA - 4 : h - 9); y++) {
					boolean open = g.caves(x, y, z) < -1.0 && g.caves(x, y + 3, z) < -0.3 && g.caves(x + 8, y, z) < -0.3
							&& g.caves(x - 8, y, z) < -0.3 && g.caves(x - 7, y + 1, z) < -0.3;
					if (!open) continue;
					double cy = flooded ? y - 1.0 : y + 0.5; // feet: the eye is 1.62 higher
					spots.add(new Spot(name, x, z, h, new double[]{x - (flooded ? 6.5 : 7.5), cy, z + 0.5, -90, flooded ? 5 : 8}));
					PeriodicElements.LOGGER.info("[terrainshots] found {} at {} {} {}", name, x, y, z);
					return;
				}
			}
		}
		PeriodicElements.LOGGER.info("[terrainshots] no {}", name);
	}

	/** A volcanic cone (4b): the camera stands off its flank, a little above the rim, looking at the crater. */
	private void volcano(Geology g, String name, boolean active) {
		String only = System.getProperty("pe.terrainonly");
		if (only != null && !java.util.Arrays.asList(only.split(",")).contains(name)) return;
		double[] best = null;
		for (int i = -8; i <= 8; i++) for (int j = -8; j <= 8; j++) {
			double[] c = g.cone(i, j);
			if (c == null || (c[4] == 1) != active) continue;
			if (best == null || Math.hypot(c[0], c[1]) < Math.hypot(best[0], best[1])) best = c;
		}
		if (best == null) {
			PeriodicElements.LOGGER.info("[terrainshots] no {}", name);
			return;
		}
		double cx = best[0] + 38, cz = best[1] + 38, cy = best[2] + 40;
		float yaw = (float) Math.toDegrees(Math.atan2(-(best[0] - cx), best[1] - cz));
		float pitch = (float) -Math.toDegrees(Math.atan2(best[2] - 20 - cy, Math.hypot(best[0] - cx, best[1] - cz)));
		spots.add(new Spot(name, (int) best[0], (int) best[1], best[2], new double[]{cx, cy, cz, yaw, pitch}));
		PeriodicElements.LOGGER.info("[terrainshots] found {} at {} {} rim {}", name, (int) best[0], (int) best[1], (int) best[2]);
		reliefMap(g, name, (int) best[0], (int) best[1], 520);
	}

	/**
	 * 3.42.1: a shaded relief map (north up, 1 pixel = 2 blocks) of the ground around a spot, from the generator's
	 * heights: shows the outline of a whole volcano, which no camera in the game can take in at once.
	 */
	private static void reliefMap(Geology g, String name, int cx, int cz, int half) {
		int step = 2, n = 2 * half / step;
		double[][] h = new double[n][n];
		for (int a = 0; a < n; a++) for (int b = 0; b < n; b++) {
			Geology.Column c = g.column(cx - half + a * step, cz - half + b * step);
			h[a][b] = c.craterLake != Integer.MIN_VALUE ? -1000 - (c.lava ? 1 : 0) : c.height;
		}
		var img = new java.awt.image.BufferedImage(n, n, java.awt.image.BufferedImage.TYPE_INT_RGB);
		for (int a = 0; a < n; a++) for (int b = 0; b < n; b++) {
			int rgb;
			if (h[a][b] <= -1000) rgb = h[a][b] < -1000.5 ? 0xE0501A : 0x3A6EC8;
			else if (h[a][b] < Geology.SEA) rgb = 0x2E5AA8;
			else {
				double gx = h[Math.min(n - 1, a + 1)][b] - h[Math.max(0, a - 1)][b];
				double gz = h[a][Math.min(n - 1, b + 1)] - h[a][Math.max(0, b - 1)];
				double shade = Math.max(0.25, Math.min(1.2, 0.8 + (-gx - gz) * 0.12));
				double t = Math.max(0, Math.min(1, (h[a][b] - Geology.SEA) / 180));
				int r = (int) ((90 + 120 * t) * shade), gg = (int) ((140 + 40 * t) * shade), bl = (int) ((70 + 120 * t) * shade);
				rgb = (Math.min(255, r) << 16) | (Math.min(255, gg) << 8) | Math.min(255, bl);
			}
			img.setRGB(a, b, rgb);
		}
		try {
			java.nio.file.Path out = java.nio.file.Path.of("screenshots", "pe_terrain_" + name + "_map.png");
			java.nio.file.Files.createDirectories(out.getParent());
			javax.imageio.ImageIO.write(img, "png", out.toFile());
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** A lava tube: the camera stands in it, looking along it. */
	private void tube(Geology g) {
		String only = System.getProperty("pe.terrainonly");
		if (only != null && !java.util.Arrays.asList(only.split(",")).contains("lava_tube")) return;
		for (int r = 0; r <= 6000; r += 16) {
			int steps = Math.max(1, (int) (2 * Math.PI * r / 16));
			for (int k = 0; k < steps; k++) {
				double a = 2 * Math.PI * k / steps;
				int x = (int) (r * Math.cos(a)), z = (int) (r * Math.sin(a));
				if (!g.basaltAt(x, z)) continue;
				double h = g.column(x, z).height;
				for (int y = Geology.SEA; y < h - 4; y++) {
					if (!(g.caves(x, y, z) < -0.4 && g.caves(x, y + 2, z) < -0.4 && g.caves(x, y - 1, z) > 0)) continue;
					for (int d = 0; d < 8; d++) {
						double ang = Math.PI * d / 4;
						int fx = (int) Math.round(x + 10 * Math.cos(ang)), fz = (int) Math.round(z + 10 * Math.sin(ang));
						int bx = (int) Math.round(x - 10 * Math.cos(ang)), bz = (int) Math.round(z - 10 * Math.sin(ang));
						if (g.caves(fx, y + 1, fz) < -0.4 && g.caves(bx, y + 1, bz) < -0.4) {
							float yaw = (float) Math.toDegrees(Math.atan2(-Math.cos(ang), Math.sin(ang)));
							spots.add(new Spot("lava_tube", x, z, h, new double[]{x + 0.5, y, z + 0.5, yaw, 5}));
							PeriodicElements.LOGGER.info("[terrainshots] found lava_tube at {} {} {}", x, y, z);
							return;
						}
					}
				}
			}
		}
		PeriodicElements.LOGGER.info("[terrainshots] no lava_tube");
	}

	private static boolean soil(Geology g, int x, int z, net.minecraft.block.Block block) {
		Geology.Column c = g.column(x, z);
		if (c.height < Geology.SEA + 4 || c.river) return false;
		net.minecraft.block.BlockState s = com.periodicelements.worldgen.GeoWorld.soilFor(g, c, x, (int) c.height, z);
		return s != null && s.isOf(block);
	}

	private void find(Geology g, String name, BiPredicate<Integer, Integer> test) {
		String only = System.getProperty("pe.terrainonly");
		if (only != null && !java.util.Arrays.asList(only.split(",")).contains(name)) return;
		for (int r = 0; r <= 6000; r += 48) {
			int steps = Math.max(1, (int) (2 * Math.PI * r / 48));
			for (int k = 0; k < steps; k++) {
				double a = 2 * Math.PI * k / steps;
				int x = (int) (r * Math.cos(a)), z = (int) (r * Math.sin(a));
				if (test.test(x, z)) {
					double h = g.column(x, z).height;
					if (name.endsWith("soil") || name.equals("terra_roxa")) {
						// a trench cut away south of the spot shows the soil profile on its north wall
						spots.add(new Spot(name, x, z, h, new double[]{x + 12.5, h + 4, z + 14, 180, 22, x, h - 10, z + 1, x + 24, h + 30, z + 30}));
					} else spots.add(new Spot(name, x, z, h));
					PeriodicElements.LOGGER.info("[terrainshots] found {} at {} {}", name, x, z);
					return;
				}
			}
		}
		PeriodicElements.LOGGER.info("[terrainshots] no {}", name);
	}

	private static void press(MinecraftClient client, String label) {
		client.currentScreen.children().stream().filter(c -> c instanceof ButtonWidget b && b.getMessage().getString().equals(label))
				.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
	}

	private static void cmd(MinecraftClient client, String command) {
		var server = client.getServer();
		UUID id = client.player.getUuid();
		server.execute(() -> {
			var player = server.getPlayerManager().getPlayer(id);
			server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(4), command);
		});
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(),
				text -> PeriodicElements.LOGGER.info("[terrainshots] {}", text.getString()));
	}
}
