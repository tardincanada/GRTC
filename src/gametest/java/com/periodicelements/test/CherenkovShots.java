package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runCherenkovShots): makes a new flat creative world from the title screen, lays out radioactive
 * things in air and in a pool at night, and screenshots the Cherenkov glow (night, day, inventory) to run/screenshots.
 */
public class CherenkovShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.cherenkovshots")) {
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
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				PeriodicElements.LOGGER.info("[cherenkovshots] screen {}", client.currentScreen.getClass().getName());
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 10 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("cherenkov");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(WorldPresets.FLAT))
						.findFirst().ifPresent(wc::setWorldType);
				PeriodicElements.LOGGER.info("[cherenkovshots] creating world");
				press(client, "Create New World");
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		var player = client.player;
		switch (worldTick) {
			case 60 -> {
				base = player.getBlockPos();
				int x = base.getX(), y = base.getY(), z = base.getZ();
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "time set midnight");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "gamerule doWeatherCycle false");
				cmd(client, "gamerule doMobSpawning false");
				cmd(client, "difficulty peaceful");
				cmd(client, "kill @e[type=!player]");
				cmd(client, String.format("fill %d %d %d %d %d %d air", x - 8, y, z - 12, x + 8, y + 6, z + 2));
				// A pool on the right: water two deep, pitchblende ore at the bottom, items in the water.
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:stone_bricks", x + 1, y - 3, z - 9, x + 6, y - 1, z - 4));
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:water", x + 2, y - 2, z - 8, x + 5, y - 1, z - 5));
				cmd(client, String.format("setblock %d %d %d periodic_elements:pitchblende_ore", x + 4, y - 3, z - 6));
				cmd(client, String.format("setblock %d %d %d periodic_elements:pitchblende_ore", x + 3, y - 3, z - 7));
				item(client, "polonium", x + 2.6, y - 1.6, z - 5.6);
				item(client, "americium", x + 4.5, y - 1.6, z - 7.5);
				item(client, "radium", x + 3.5, y - 1.6, z - 5.4);
				item(client, "uranium", x + 5.3, y - 1.6, z - 6.0);
				// In air on the left, on a stone slab shelf: weakest to strongest; uranium shows nothing.
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:smooth_stone_slab", x - 6, y, z - 7, x - 1, y, z - 7));
				String[] air = {"uranium", "pitchblende_ore", "radium", "fission_products", "einsteinium", "californium"};
				for (int i = 0; i < air.length; i++) {
					item(client, air[i], x - 6 + i + 0.5, y + 0.6, z - 6.5);
				}
				cmd(client, "give @s periodic_elements:radium 3");
				cmd(client, "give @s periodic_elements:californium 1");
				cmd(client, "give @s periodic_elements:uranium 16");
				cmd(client, "give @s periodic_elements:spent_fuel_rod 1");
				cmd(client, "give @s periodic_elements:pitchblende_ore 32");
			}
			case 100 -> {
				player.setPosition(base.getX() + 0.5, base.getY() + 1.2, base.getZ() + 0.5);
				player.setYaw(180);
				player.setPitch(35);
				player.getAbilities().flying = true;
			}
			case 150 -> {
				client.inGameHud.getChatHud().clear(false);
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
			}
			case 160 -> shot(client, "cherenkov_night");
			case 165 -> cmd(client, "time set noon");
			case 200 -> shot(client, "cherenkov_day");
			case 205 -> cmd(client, "time set midnight");
			case 230 -> {
				cmd(client, "gamerule periodicRadiation off");
				cmd(client, "effect clear @s");
				cmd(client, "gamemode survival");
			}
			case 245 -> client.inGameHud.getChatHud().clear(false);
			case 250 -> client.setScreen(new InventoryScreen(player));
			case 270 -> shot(client, "cherenkov_inventory");
			case 280 -> {
				PeriodicElements.LOGGER.info("[cherenkovshots] done");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void item(MinecraftClient client, String id, double x, double y, double z) {
		cmd(client, String.format(java.util.Locale.ROOT,
				"summon item %.2f %.2f %.2f {Item:{id:\"periodic_elements:%s\",count:1},PickupDelay:32767,Age:-32768,NoGravity:1b,Motion:[0.0,0.0,0.0]}",
				x, y, z, id));
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
				text -> PeriodicElements.LOGGER.info("[cherenkovshots] {}", text.getString()));
	}
}
