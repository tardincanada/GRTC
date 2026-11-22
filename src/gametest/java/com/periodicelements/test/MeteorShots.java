package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.space.MeteorEntity;
import com.periodicelements.space.Meteorites;
import com.periodicelements.space.SpaceState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runMeteorShots, 3.62.0): the Space Telescope's forecast in chat, a meteorite streaking down at
 * night, the impact, the crater by day with the chat line that marks it on the Atlas, and the three kinds side by side.
 */
public class MeteorShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;
	private int groundY;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.meteorshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private BlockPos target() {
		return new BlockPos(base.getX(), groundY, base.getZ() - 50);
	}

	private void step(MinecraftClient client) {
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) {
				return;
			}
			tick++;
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("meteors");
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
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY(), z = base.getZ();
			groundY = y + 6;
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set 6000");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			// a low stone hill for the crater to dig into (the flat world is only four blocks deep)
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:stone", x - 16, y, z - 66, x + 16, y + 5, z - 34));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:dirt", x - 16, y + 5, z - 66, x + 16, y + 5, z - 34));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:grass_block", x - 16, y + 6, z - 66, x + 16, y + 6, z - 34));
			// the three kinds side by side, near the start
			cmd(client, String.format("setblock %d %d %d periodic_elements:chondrite", x - 2, y, z - 4));
			cmd(client, String.format("setblock %d %d %d periodic_elements:iron_meteorite", x, y, z - 4));
			cmd(client, String.format("setblock %d %d %d periodic_elements:carbonaceous_chondrite", x + 2, y, z - 4));
			server.execute(() -> {
				ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList());
				SpaceState.get(server).add(id, "telescope");
			});
		}
		if (worldTick == 70) {
			player.setPosition(base.getX() + 0.5, base.getY() + 1.6, base.getZ() + 0.5);
			player.setYaw(180);
			player.setPitch(35);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		if (worldTick == 100) {
			shot(client, "meteorites");
		}
		if (worldTick == 105) {   // the telescope's evening forecast, in chat
			cmd(client, "time set 13000");
			client.options.hudHidden = false;
			player.setPitch(-10);
			BlockPos t = target();
			server.execute(() -> Meteorites.schedule(server.getOverworld(), t.getX(), t.getZ(), server.getOverworld().getTimeOfDay() + 5000));
		}
		if (worldTick == 130) {
			shot(client, "forecast");
			client.options.hudHidden = true;
		}
		if (worldTick == 135) {   // night: the fall, seen from where the player stands
			cmd(client, "time set 18000");
			player.setPosition(base.getX() + 0.5, base.getY() + 2.0, base.getZ() + 6.5);
			player.setYaw(180);
			player.setPitch(-52);
			BlockPos t = target();
			server.execute(() -> {
				var st = SpaceState.get(server);
				st.fallAt = -1;
				st.markDirty();
				MeteorEntity.send(server.getOverworld(), t.getX(), t.getY(), t.getZ(), Meteorites.IRON_METEORITE, 0, -1, false);
			});
		}
		if (worldTick == 160) {   // the meteor comes in steeply: look high, then follow it down
			player.setPitch(-52);
			shot(client, "streak_high");
		}
		if (worldTick == 180) {
			player.setPitch(-42);
			shot(client, "streak_low");
		}
		if (worldTick == 210) {
			player.setPitch(-12);
			shot(client, "streak_last");
		}
		if (worldTick == 217) {
			player.setPitch(-4);
			shot(client, "impact");
		}
		if (worldTick == 240) {   // morning: the crater, and the chat line that marked it
			cmd(client, "time set 1000");
			client.options.hudHidden = false;
			BlockPos t = target();
			player.setPosition(t.getX() + 0.5, groundY + 6.0, t.getZ() + 9.5);
			player.setYaw(180);
			player.setPitch(38);
		}
		if (worldTick == 265) {
			shot(client, "crater");
		}
		if (worldTick == 270) {
			client.options.hudHidden = true;
			BlockPos t = target();
			player.setPosition(t.getX() + 0.5, groundY + 2.5, t.getZ() + 4.5);
			player.setPitch(48);
		}
		if (worldTick == 290) {
			shot(client, "crater_close");
		}
		if (worldTick == 300) {
			PeriodicElements.LOGGER.info("[meteorshots] done");
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
