package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.space.LifeSupportBlockEntity;
import com.periodicelements.space.Moon;
import com.periodicelements.space.MoonChunkGenerator;
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
 * Dev-only (./gradlew runMoonShots, 3.64.0): the lunar landscape, the Earth in the black sky, a sealed base with its
 * Life Support lit and a space suit beside it, and an icy crater floor near the pole.
 */
public class MoonShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private volatile int baseY = Integer.MIN_VALUE;
	private volatile int[] ice;
	private volatile int highest;
	private volatile int[] skylight;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.moonshots")) {
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
				PeriodicElements.LOGGER.info("[moonshots] waiting on screen {}", client.currentScreen.getClass().getName());
			}
			// a modded dimension may ask to confirm experimental settings: go on
			if (tick % 40 == 30 && client.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmScreen confirm) {
				PeriodicElements.LOGGER.info("[moonshots] confirm screen: {}", confirm.getTitle().getString());
				confirm.children().stream().filter(c -> c instanceof ButtonWidget).findFirst()
						.map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("moon");
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
			server.execute(() -> ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList()));
			cmd(client, "execute in periodic_elements:moon run tp @s 0.5 160 0.5");
			server.execute(() -> {
				ServerWorld moon = server.getWorld(Moon.MOON);
				baseY = Moon.surface(moon, 0, 0) - 1;
				int top = 0;
				for (int dx = -60; dx <= 20; dx += 10) {
					for (int dz = -80; dz <= 20; dz += 10) {
						top = Math.max(top, Moon.surface(moon, dx, dz));
					}
				}
				highest = top;
				// an icy crater floor near the north pole for the last shot
				var t = MoonChunkGenerator.terrain(moon.getChunkManager().getNoiseConfig());
				boolean[] f = new boolean[1];
				for (int z = 1100; z < 1700 && ice == null; z += 4) {
					for (int x = -400; x < 400 && ice == null; x += 4) {
						f[0] = false;
						t.height(x, z, f);
						if (f[0]) {
							ice = new int[]{x, z};
						}
					}
				}
				// 3.65.0: a skylight into a lava tube
				for (int x = -1500; x < 1500 && skylight == null; x += 3) {
					for (int z = -1500; z < 1500 && skylight == null; z += 3) {
						int ground = t.height(x, z, null);
						int[] tb = t.tube(x, z, ground);
						if (tb != null && tb[1] == ground && t.mare(x, z) > 0.8 && t.tube(x + 3, z, t.height(x + 3, z, null)) != null
								&& t.tube(x - 3, z, t.height(x - 3, z, null)) != null && t.tube(x, z + 3, t.height(x, z + 3, null)) != null
								&& t.tube(x, z - 3, t.height(x, z - 3, null)) != null) {
							int yaw = 0, bestRun = -1;
							for (int a = 0; a < 360; a += 15) {   // look the way the tube runs on furthest
								int run = 0;
								for (int d = 4; d <= 30; d += 2) {
									int px = x - (int) Math.round(d * Math.sin(Math.toRadians(a)));
									int pz = z + (int) Math.round(d * Math.cos(Math.toRadians(a)));
									int[] o = t.tube(px, pz, t.height(px, pz, null));
									if (o == null) break;
									run = d;
								}
								if (run > bestRun) {
									bestRun = run;
									yaw = a;
								}
							}
							skylight = new int[]{x, z, tb[0], yaw};
						}
					}
				}
			});
		}
		if (worldTick == 120 && baseY != Integer.MIN_VALUE) {
			int y = baseY;
			client.options.hudHidden = true;
			// a sealed glass room on the ground, Life Support inside, a suit on a stand outside
			cmd(client, String.format("fill -4 %d -4 8 %d 4 periodic_elements:anorthosite", y - 2, y));
			cmd(client, String.format("fill -4 %d -4 8 %d 4 minecraft:air", y + 1, y + 12));
			cmd(client, String.format("fill -3 %d -3 3 %d 3 minecraft:glass hollow", y + 1, y + 4));
			cmd(client, String.format("setblock 0 %d 0 periodic_elements:life_support", y + 2));
			cmd(client, String.format("setblock 2 %d 2 minecraft:white_bed", y + 2));
			cmd(client, String.format("setblock -2 %d 2 minecraft:crafting_table", y + 2));
			cmd(client, String.format("summon minecraft:armor_stand 5.5 %d 0.5 {Rotation:[90f,0f],ArmorItems:["
					+ "{id:\"periodic_elements:space_suit_boots\",count:1},{id:\"periodic_elements:space_suit_leggings\",count:1},"
					+ "{id:\"periodic_elements:space_suit_chestplate\",count:1},{id:\"periodic_elements:space_suit_helmet\",count:1}]}", y + 1));
			int camY = Math.min(250, Math.max(y + 40, highest + 12));
			cmd(client, String.format("tp @s -40 %d -60 -35 22", camY));
		}
		if (worldTick == 130) {
			server.execute(() -> {
				ServerWorld moon = server.getWorld(Moon.MOON);
				if (moon.getBlockEntity(new BlockPos(0, baseY + 2, 0)) instanceof LifeSupportBlockEntity be) {
					be.addCylinders(8);
				}
			});
		}
		if (worldTick == 220) {
			shot(client, "moon_landscape");
			cmd(client, String.format("tp @s 0.5 %d -6.5 180 -52", baseY + 3));
		}
		if (worldTick == 250) {
			shot(client, "moon_earth");
			cmd(client, String.format("tp @s 9.5 %d -7.5 50 12", baseY + 4));
		}
		if (worldTick == 280) {
			shot(client, "moon_base");
			int[] at = ice;
			if (at != null) {
				cmd(client, String.format("tp @s %d 150 %d 0 90", at[0], at[1]));
			}
		}
		if (worldTick == 300 && ice != null) {   // the chunks there exist now: drop to just above the floor
			int[] at = ice;
			server.execute(() -> {
				ServerWorld moon = server.getWorld(Moon.MOON);
				int y = Moon.surface(moon, at[0], at[1]);
				cmd(client, String.format("tp @s %d %d %d 0 50", at[0], y + 12, at[1] - 10));
			});
		}
		if (worldTick == 400) {
			shot(client, "moon_ice");
		}
		if (worldTick == 405) {
			cmd(client, String.format("tp @s 0.5 %d -6.5 -90 -60", baseY + 3));
		}
		if (worldTick == 430) {
			shot(client, "moon_sun");
		}
		if (worldTick == 436) {
			cmd(client, "effect give @s minecraft:night_vision 600 0 true");
		}
		if (worldTick == 440 && skylight != null) {   // stand on the tube floor under the skylight, looking along it
			int[] at = skylight;
			cmd(client, String.format("tp @s %d %d %d %d 5", at[0], at[2] + 1, at[1], at[3]));
		}
		if (worldTick == 530 && skylight != null) {
			int[] at = skylight;
			server.execute(() -> {
				ServerWorld moon = server.getWorld(Moon.MOON);
				StringBuilder b = new StringBuilder();
				for (int y = at[2] - 2; y <= at[2] + 8; y++) {
					b.append(y).append('=').append(moon.getBlockState(new BlockPos(at[0], y, at[1])).getBlock().getTranslationKey()).append(' ');
				}
				var pl = server.getPlayerManager().getPlayerList().get(0);
				PeriodicElements.LOGGER.info("[moonshots] tube at {} {} floor {} yaw {}: {} player {} {}", at[0], at[1], at[2], at[3], b,
						pl.getPos(), pl.getWorld().getRegistryKey().getValue());
			});
		}
		if (worldTick == 540 && skylight != null) {
			shot(client, "moon_lava_tube");
			int[] at = skylight;
			cmd(client, String.format("tp @s %d.5 %d %d.5 0 90", at[0], Math.min(250, at[2] + 32), at[1]));
		}
		if (worldTick == 600 && skylight != null) {
			shot(client, "moon_skylight");
		}
		if (worldTick == 610) {
			PeriodicElements.LOGGER.info("[moonshots] done");
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
