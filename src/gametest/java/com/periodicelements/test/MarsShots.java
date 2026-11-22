package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.space.LifeSupportBlockEntity;
import com.periodicelements.space.Mars;
import com.periodicelements.space.MarsChunkGenerator;
import com.periodicelements.space.Moon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runMarsShots, 3.66.0): a giant volcano under the butterscotch sky, the blue sunset, a canyon, the
 * polar cap, a dust storm, and a greenhouse of washed soil kept in air by Life Support.
 */
public class MarsShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private volatile int baseY = Integer.MIN_VALUE;
	private volatile int[] volcano, canyon, cap;
	private volatile long stormTime = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.marsshots")) {
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
			if (tick % 200 == 0) {
				PeriodicElements.LOGGER.info("[marsshots] waiting on screen {}", client.currentScreen.getClass().getName());
			}
			// a modded dimension may ask to confirm experimental settings: go on
			if (tick % 40 == 30 && client.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmScreen confirm) {
				PeriodicElements.LOGGER.info("[marsshots] confirm screen: {}", confirm.getTitle().getString());
				confirm.children().stream().filter(c -> c instanceof ButtonWidget).findFirst()
						.map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("mars");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(WorldPresets.FLAT))
						.findFirst().ifPresent(wc::setWorldType);
				screen.children().stream().filter(c -> c instanceof ButtonWidget b && b.getMessage().getString().equals(
								net.minecraft.text.Text.translatable("selectWorld.create").getString()))
						.findFirst().map(c -> (ButtonWidget) c).ifPresent(b -> {
							created = true;
							b.onPress();
						});
			}
			return;
		}
		if (client.player == null || client.getOverlay() != null) {
			return;   // wait while the language reload runs, so no shot catches the loading screen
		}
		worldTick++;
		var player = client.player;
		var server = client.getServer();
		UUID id = player.getUuid();
		String lang = System.getProperty("pe.lang", "en_us");
		if (worldTick == 20 && !client.options.language.equals(lang)) {
			client.options.language = lang;
			client.getLanguageManager().setLanguage(lang);
			client.reloadResources();
		}
		if (worldTick == 60) {
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "time set 3500");
			server.execute(() -> ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList()));
			cmd(client, "execute in periodic_elements:mars run tp @s 0.5 200 0.5");
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				int top = 0;
				for (int dx = -20; dx <= 30; dx += 5) {
					for (int dz = -20; dz <= 20; dz += 5) {
						top = Math.max(top, Moon.surface(mars, dx, dz));
					}
				}
				baseY = top;
				var t = MarsChunkGenerator.terrain(mars.getChunkManager().getNoiseConfig());
				// the nearest big volcano
				double best = Double.MAX_VALUE;
				for (int cx = -3; cx <= 3; cx++) {
					for (int cz = -1; cz <= 1; cz++) {
						var v = t.volcano(cx, cz);
						if (v != null && v.h() > 95 && v.h() < 130 && Math.hypot(v.x(), v.z()) < best) {
							best = Math.hypot(v.x(), v.z());
							volcano = new int[]{(int) v.x(), (int) v.z(), (int) v.r()};
						}
					}
				}
				// a deep canyon, and the rim beside it
				for (int x = -6000; x < 6000 && canyon == null; x += 8) {
					for (int z = -1200; z < 1200 && canyon == null; z += 8) {
						if (t.canyon(x, z) >= 1 && t.column(x, z).top() < 48) {
							for (int d = 8; d < 300; d += 4) {
								if (t.canyon(x + d, z) == 0) {
									if (d >= 48) {
										canyon = new int[]{x, z, x + d + 3};
									}
									break;
								}
							}
						}
					}
				}
				for (int z = 2000; z < 2600 && cap == null; z += 8) {
					var c = t.column(0, z);
					if (c.frost()) {
						cap = new int[]{0, z};
					}
				}
				for (long d = 0; d < 3000 && stormTime < 0; d += 3) {
					long at = d * 24000L + 36000L;
					if (Mars.storm(at) > 0.99f) {
						stormTime = at;
					}
				}
				PeriodicElements.LOGGER.info("[marsshots] volcano {} canyon {} cap {} storm {}", volcano == null ? null : volcano[0] + "," + volcano[1],
						canyon == null ? null : canyon[0] + "," + canyon[1], cap == null ? null : cap[1], stormTime);
			});
		}
		if (worldTick == 120 && baseY != Integer.MIN_VALUE) {
			int y = baseY;
			client.options.hudHidden = true;
			// a glass greenhouse on the plain: washed soil tilled and sown, Life Support keeping it in air
			client.options.getViewDistance().setValue(20);
			cmd(client, String.format("fill -20 %d -20 30 %d 20 periodic_elements:martian_regolith", y - 12, y));
			cmd(client, String.format("fill -20 %d -20 30 %d 20 minecraft:air", y + 1, y + 15));
			cmd(client, String.format("fill -20 %d -20 30 %d 20 minecraft:air", y + 16, y + 30));
			cmd(client, String.format("fill -4 %d -4 4 %d 4 minecraft:glass hollow", y, y + 4));
			cmd(client, String.format("fill -3 %d -3 3 %d 3 periodic_elements:washed_martian_soil", y, y));
			cmd(client, String.format("fill -3 %d -3 3 %d -1 minecraft:farmland[moisture=7]", y, y));
			cmd(client, String.format("fill -3 %d -3 3 %d -1 minecraft:wheat[age=7]", y + 1, y + 1));
			cmd(client, String.format("fill -3 %d 1 -1 %d 3 minecraft:farmland[moisture=7]", y, y));
			cmd(client, String.format("fill -3 %d 1 -1 %d 3 minecraft:potatoes[age=5]", y + 1, y + 1));
			cmd(client, String.format("setblock 2 %d 2 periodic_elements:life_support", y + 1));
			cmd(client, String.format("setblock 0 %d 0 minecraft:water", y));
			cmd(client, String.format("summon minecraft:armor_stand 6.5 %d 0.5 {Rotation:[90f,0f],ArmorItems:["
					+ "{id:\"periodic_elements:space_suit_boots\",count:1},{id:\"periodic_elements:space_suit_leggings\",count:1},"
					+ "{id:\"periodic_elements:space_suit_chestplate\",count:1},{id:\"periodic_elements:space_suit_helmet\",count:1}]}", y + 1));
			int[] v = volcano;
			if (v != null) {   // stand off the volcano's foot, looking at it
				int px = v[0], pz = v[1] + (int) (v[2] * 0.25);   // south of the summit, looking south down the flank
				cmd(client, String.format("tp @s %d 250 %d 0 0", px, pz));
			}
		}
		if (worldTick == 130) {
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				if (mars.getBlockEntity(new BlockPos(2, baseY + 1, 2)) instanceof LifeSupportBlockEntity be) {
					be.addCylinders(8);
				}
			});
		}
		if (worldTick == 160 && volcano != null) {
			int[] v = volcano;
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				var pl = server.getPlayerManager().getPlayer(id);
				int ground = Moon.surface(mars, pl.getBlockX(), pl.getBlockZ());
				cmd(client, String.format("tp @s ~ %d ~ 0 22", Math.min(254, ground + 14)));
			});
		}
		if (worldTick == 300) {
			shot(client, "mars_volcano");
			int[] v = volcano;
			if (v != null) {   // over the summit caldera, looking down into it
				cmd(client, String.format("tp @s %d 250 %d 0 60", v[0], v[1] - (int) (v[2] * 0.12)));
			}
		}
		if (worldTick == 315 && volcano != null) {
			int[] v = volcano;
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				int ground = Moon.surface(mars, v[0], v[1] - (int) (v[2] * 0.12));
				cmd(client, String.format("tp @s %d %d %d 0 38", v[0], Math.min(254, ground + 34), v[1] - (int) (v[2] * 0.12)));
			});
		}
		if (worldTick == 410) {
			shot(client, "mars_caldera");
			cmd(client, String.format("tp @s 14.5 %d 0.5 90 18", baseY + 6));
		}
		if (worldTick == 450) {
			shot(client, "mars_greenhouse");
			cmd(client, "time set 11900");
			cmd(client, String.format("tp @s 30.5 %d 0.5 90 -8", baseY + 12));
		}
		if (worldTick == 500) {
			shot(client, "mars_sunset");
			cmd(client, "time set 6000");
			int[] c = canyon;
			if (c != null) {
				cmd(client, String.format("tp @s %d 150 %d 90 30", c[2], c[1]));
			}
		}
		if (worldTick == 520 && canyon != null) {
			int[] c = canyon;
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				int ground = Moon.surface(mars, c[2], c[1]);
				cmd(client, String.format("tp @s %d %d %d 90 28", c[2] + 2, ground + 26, c[1]));
			});
		}
		if (worldTick == 620) {
			if (canyon != null) {
				shot(client, "mars_canyon");
			}
			int[] p = cap;
			if (p != null) {
				cmd(client, String.format("tp @s %d 200 %d 180 20", p[0], p[1]));
			}
		}
		if (worldTick == 640 && cap != null) {
			int[] p = cap;
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				int ground = Moon.surface(mars, p[0], p[1] - 60);
				cmd(client, String.format("tp @s %d %d %d 0 12", p[0], ground + 10, p[1] - 60));
			});
		}
		if (worldTick == 740) {
			if (cap != null) {
				shot(client, "mars_polar_cap");
			}
			if (stormTime >= 0) {
				cmd(client, "time set " + stormTime);
			}
			cmd(client, String.format("tp @s 14.5 %d 4.5 120 5", baseY + 3));
		}
		if (worldTick == 820) {
			shot(client, "mars_storm");
			cmd(client, "time set 18000");
			cmd(client, String.format("tp @s 0.5 %d -10.5 0 -60", baseY + 8));
		}
		if (worldTick == 860) {
			shot(client, "mars_night");
		}
		if (worldTick == 870) {
			PeriodicElements.LOGGER.info("[marsshots] done");
			client.scheduleStop();
		}
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
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> { });
	}
}
