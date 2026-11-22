package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.atlas.Atlas;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.space.Space;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.List;
import java.util.UUID;

/**
 * Dev-only (./gradlew runSpaceShots, 3.61.0): a flat world with two rockets on their pads, the Launch Console's report,
 * lift-off, stage separation high up, and the reusable first stage coming down and standing on its pad again.
 */
public class SpaceShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.spaceshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private BlockPos padA() {
		return base.add(-3, 0, -12);
	}

	private BlockPos padB() {
		return base.add(4, 0, -12);
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
				wc.setWorldName("space");
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
		if (client.player == null) {
			return;
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
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set 6000");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 30, y, z - 30, x + 30, y + 20, z + 30));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:smooth_stone", x - 8, y - 1, z - 17, x + 9, y - 1, z - 7));
			BlockPos a = padA(), b = padB();
			server.execute(() -> {
				var w = server.getOverworld();
				ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList());
				SpaceTests.build(w, a.down(), new ItemStack(Atlas.SATELLITE), false, Space.KEROLOX_ENGINE, 4, Space.KEROLOX_ENGINE, 2);
				SpaceTests.build(w, b.down(), new ItemStack(Space.WEATHER_SATELLITE), true, Space.KEROLOX_ENGINE, 5,
						Space.HYPERGOLIC_ENGINE, 2);
				w.setBlockState(base.add(0, 0, -6), Space.LAUNCH_CONSOLE.getDefaultState());
			});
		}
		if (worldTick == 70) {
			player.setPosition(base.getX() + 0.5, base.getY() + 3.0, base.getZ() + 9.5);
			player.setYaw(180);
			player.setPitch(-12);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		if (worldTick == 140) {
			shot(client, "rockets_on_pads");
		}
		if (worldTick == 150) {   // the console's report for the reusable rocket, in chat
			client.options.hudHidden = false;
			player.setPosition(base.getX() + 0.5, base.getY(), base.getZ() - 3.5);
			player.setYaw(180);
			player.setPitch(10);
			server.execute(() -> {
				var w = server.getOverworld();
				var sp = server.getPlayerManager().getPlayer(id);
				var r = com.periodicelements.space.Rocket.read(w, padB().down());
				r.report().forEach(t -> sp.sendMessage(t, false));
				var one = com.periodicelements.space.Rocket.read(w, padA().down());
				one.report().forEach(t -> sp.sendMessage(t, false));
			});
		}
		if (worldTick == 175) {
			shot(client, "console_report");
			client.options.hudHidden = true;
		}
		// lift-off of the expendable rocket, seen from the side
		if (worldTick == 180) {
			player.setPosition(base.getX() + 18.5, base.getY() + 2.0, base.getZ() + 4.5);
			player.setYaw(135);
			player.setPitch(-15);
			server.execute(() -> Space.launch(server.getOverworld(), padA().down(), id));
		}
		if (worldTick == 205) {
			shot(client, "liftoff");
		}
		if (worldTick == 225) {
			shot(client, "climb");
		}
		if (worldTick == 262) {
			player.setPitch(-62);
		}
		if (worldTick == 268) {
			shot(client, "separation");
		}
		if (worldTick == 285) {
			shot(client, "separation_after");
		}
		// the reusable one: up, back, down
		if (worldTick == 360) {
			player.setPosition(base.getX() + 16.5, base.getY() + 3.0, base.getZ() + 6.5);
			player.setYaw(140);
			player.setPitch(-20);
			server.execute(() -> Space.launch(server.getOverworld(), padB().down(), id));
		}
		for (int k : List.of(560, 580, 600, 615, 630, 645, 660)) {
			if (worldTick == k) {
				player.setPitch(-12);
				shot(client, "landing_" + k);
			}
		}
		if (worldTick == 760) {
			player.setPosition(base.getX() + 10.5, base.getY() + 3.0, base.getZ() + 2.5);
			player.setYaw(145);
			player.setPitch(-5);
		}
		if (worldTick == 780) {
			shot(client, "landed");
		}
		if (worldTick == 790) {
			PeriodicElements.LOGGER.info("[spaceshots] done");
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
