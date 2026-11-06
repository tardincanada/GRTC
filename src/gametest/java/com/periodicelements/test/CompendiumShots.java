package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.CompendiumScreen;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Locale;
import java.util.UUID;

/**
 * Dev-only (./gradlew runCompendiumShots): makes a normal world (seed 4242), stands in a forest before a European beech
 * with the Tree Compendium in the hotbar and screenshots the corner of the screen and the book, first knowing only that
 * tree, then with every entry and the three compendium research projects, to run/screenshots.
 */
public class CompendiumShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private BlockPos spot;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.compendiumshots")) {
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
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("compendium");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.setSeed(System.getProperty("pe.compseed", "4242"));
				press(client, net.minecraft.text.Text.translatable("selectWorld.create").getString());
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		var server = client.getServer();
		switch (worldTick) {
			case 40 -> {
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "gamerule doWeatherCycle false");
				cmd(client, "gamerule doMobSpawning false");
				cmd(client, "gamerule periodicRadiation off");
				cmd(client, "time set 6000");
				cmd(client, "weather clear");
				cmd(client, "compendium forget");
				client.options.getViewDistance().setValue(10);
				client.setScreen(null);
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
				var found = server.submit(() -> server.getOverworld().locateBiome(
						b -> b.matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla("forest"))),
						new BlockPos(0, 100, 0), 6400, 32, 64)).join();
				spot = found == null ? new BlockPos(0, 64, 0) : server.submit(() -> inside(server.getOverworld(), found.getFirst())).join();
				cmd(client, "gamemode spectator");
				tp(client, spot.getX(), 160, spot.getZ(), 0, 90);
				PeriodicElements.LOGGER.info("[compshots] forest at {}", spot);
			}
			case 160 -> {
				int y = server.submit(() -> server.getOverworld().getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spot.getX(), spot.getZ())).join() - 1;
				spot = new BlockPos(spot.getX(), y, spot.getZ());
				int x = spot.getX(), z = spot.getZ();
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", x - 9, y + 1, z - 9, x + 9, y + 30, z + 9));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x - 9, y, z - 9, x + 9, y, z + 9));
				cmd(client, String.format(Locale.ROOT, "place feature periodic_elements:tree/beech %d %d %d", x, y + 1, z));
				cmd(client, "gamemode creative");
				cmd(client, "item replace entity @s hotbar.0 with periodic_elements:tree_compendium");
				cmd(client, "item replace entity @s hotbar.1 with periodic_elements:beech_sapling");
				cmd(client, "item replace entity @s hotbar.2 with minecraft:book");
				client.player.getInventory().selectedSlot = 0;
				tp(client, x + 0.5, y + 1, z - 4.5, 0, -2);
			}
			case 255, 375 -> client.inGameHud.getChatHud().clear(false);
			case 260 -> shot(client, "compendium_hud_first_tree");
			case 270 -> {
				CompendiumScreen.startBiome = "forest";
				CompendiumScreen.startTree = null;
				client.setScreen(new CompendiumScreen());
			}
			case 300 -> shot(client, "compendium_book_first_tree");
			case 310 -> {
				client.setScreen(null);
				cmd(client, "research grant @s species_plantarum");
				cmd(client, "research grant @s plant_geography");
				cmd(client, "research grant @s red_list");
				cmd(client, "compendium reveal");
			}
			case 380 -> shot(client, "compendium_hud_research");
			case 390 -> {
				CompendiumScreen.startBiome = null;
				CompendiumScreen.startTree = null;
				client.setScreen(new CompendiumScreen());
			}
			case 420 -> shot(client, "compendium_book_home");
			case 430 -> open(client, "forest", null);
			case 460 -> shot(client, "compendium_book_forest");
			case 470 -> open(client, "forest", "beech");
			case 500 -> shot(client, "compendium_book_beech");
			case 510 -> open(client, "savanna_plateau", "araucaria");
			case 540 -> shot(client, "compendium_book_araucaria");
			case 550 -> open(client, "jungle", "brazil_nut");
			case 580 -> shot(client, "compendium_book_brazil_nut");
			case 590 -> {
				client.setScreen(null);
				int x = spot.getX(), y = spot.getY(), z = spot.getZ();
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:cornflower", x - 2, y + 1, z - 4, x + 2, y + 1, z - 2));
				cmd(client, "item replace entity @s hotbar.0 with periodic_elements:plant_compendium");
				client.player.getInventory().selectedSlot = 0;
				tp(client, x + 0.5, y + 1, z - 6.5, 0, 20);
			}
			case 650 -> client.inGameHud.getChatHud().clear(false);
			case 660 -> shot(client, "plant_hud_cornflower");
			case 670 -> open(client, "plant", null, null);
			case 700 -> shot(client, "plant_book_home");
			case 710 -> open(client, "plant", "flower_forest", null);
			case 740 -> shot(client, "plant_book_flower_forest");
			case 750 -> open(client, "plant", "flower_forest", "cornflower");
			case 780 -> shot(client, "plant_book_cornflower");
			case 790 -> open(client, "plant", "jungle", "cocoa");
			case 820 -> shot(client, "plant_book_cocoa");
			case 830 -> open(client, "plant", "cold_ocean", "kelp");
			case 860 -> shot(client, "plant_book_kelp");
			case 870 -> open(client, "plant", "warm_ocean", "torchflower");
			case 900 -> shot(client, "plant_book_torchflower");
			// 3.15.0 Mineral Compendium: a small wall of rocks and ores, an unknown rock, the lens, then every layer.
			case 910 -> {
				client.setScreen(null);
				cmd(client, "compendium forget");
				int x = spot.getX(), y = spot.getY(), z = spot.getZ();
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", x - 9, y + 1, z - 9, x + 9, y + 30, z + 9));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:granite", x - 3, y + 1, z - 1, x + 3, y + 3, z - 1));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:basalt", x - 3, y + 1, z - 1, x - 2, y + 3, z - 1));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:iron_ore", x + 2, y + 2, z - 1));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:pitchblende_ore", x + 3, y + 1, z - 1));
				cmd(client, "item replace entity @s hotbar.0 with periodic_elements:mineral_compendium");
				cmd(client, "item replace entity @s hotbar.1 with periodic_elements:magnifying_lens");
				cmd(client, "item replace entity @s hotbar.2 with minecraft:granite");
				client.player.getInventory().selectedSlot = 0;
				tp(client, x + 0.5, y + 1, z - 4.5, 0, -80);
			}
			case 960 -> tp(client, spot.getX() + 0.5, spot.getY() + 1, spot.getZ() - 4.5, 0, 8);
			case 966 -> {
				client.inGameHud.getChatHud().clear(false);
				client.getToastManager().clear();
			}
			case 968 -> shot(client, "mineral_hud_unknown");
			case 1010 -> {
				client.inGameHud.getChatHud().clear(false);
				client.getToastManager().clear();
			}
			case 1012 -> shot(client, "mineral_hud_granite");
			case 1014 -> client.player.getInventory().selectedSlot = 1;
			case 1030 -> shot(client, "mineral_hud_analysing");
			case 1076 -> client.getToastManager().clear();
			case 1080 -> shot(client, "mineral_hud_analysed");
			case 1090 -> open(client, "mineral", "rocks", "granite");
			case 1120 -> shot(client, "mineral_book_granite_analysed");
			case 1130 -> {
				client.setScreen(null);
				cmd(client, "research grant @s all");
				cmd(client, "compendium reveal");
				client.player.getInventory().selectedSlot = 0;
			}
			case 1190 -> {
				client.inGameHud.getChatHud().clear(false);
				client.getToastManager().clear();
				client.setScreen(null);
			}
			case 1200 -> shot(client, "mineral_hud_formation");
			case 1210 -> open(client, "mineral", null, null);
			case 1240 -> shot(client, "mineral_book_home");
			case 1250 -> open(client, "mineral", "igneous", null);
			case 1280 -> shot(client, "mineral_book_igneous");
			case 1290 -> open(client, "mineral", "igneous", "granite");
			case 1320 -> shot(client, "mineral_book_granite");
			case 1330 -> open(client, "mineral", "ores", "pitchblende");
			case 1360 -> shot(client, "mineral_book_pitchblende");
			case 1370 -> open(client, "mineral", "ores", "iron");
			case 1400 -> shot(client, "mineral_book_iron");
			case 1410 -> {
				client.setScreen(null);
				client.player.getInventory().selectedSlot = 2;
			}
			case 1420 -> {
				var stack = client.player.getInventory().getStack(2);
				var lines = stack.getTooltip(net.minecraft.item.Item.TooltipContext.DEFAULT, client.player,
						net.minecraft.item.tooltip.TooltipType.BASIC);
				PeriodicElements.LOGGER.info("[compshots] granite tooltip: {}", lines.stream().map(t -> t.getString()).toList());
			}
			case 1430 -> {
				PeriodicElements.LOGGER.info("[compshots] done");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	/** A point near {@code near} with forest all around it (biome source sampled every 6 blocks within 18), so the shot is not on an edge. */
	private static BlockPos inside(net.minecraft.server.world.ServerWorld world, BlockPos near) {
		var cm = world.getChunkManager();
		var src = cm.getChunkGenerator().getBiomeSource();
		var sampler = cm.getNoiseConfig().getMultiNoiseSampler();
		var key = RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla("forest"));
		for (int r = 0; r <= 400; r += 8) {
			for (int dx = -r; dx <= r; dx += 8) {
				for (int dz = -r; dz <= r; dz += 8) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
					int x = near.getX() + dx, z = near.getZ() + dz;
					boolean all = true;
					for (int a = -18; a <= 18 && all; a += 6) {
						for (int b = -18; b <= 18 && all; b += 6) {
							all = src.getBiome((x + a) >> 2, 72 >> 2, (z + b) >> 2, sampler).matchesKey(key);
						}
					}
					if (all) return new BlockPos(x, 64, z);
				}
			}
		}
		return near;
	}

	private static void open(MinecraftClient client, String biome, String tree) {
		open(client, "tree", biome, tree);
	}

	private static void open(MinecraftClient client, String kind, String biome, String tree) {
		CompendiumScreen.startBiome = biome;
		CompendiumScreen.startTree = tree;
		CompendiumScreen s = new CompendiumScreen(kind);
		client.setScreen(s);
		if (biome != null) s.showBiomeInList(biome);
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(),
				text -> PeriodicElements.LOGGER.info("[compshots] {}", text.getString()));
		PeriodicElements.LOGGER.info("[compshots] {}", name);
	}

	private static void tp(MinecraftClient client, double x, double y, double z, float yaw, float pitch) {
		cmd(client, String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", x, y, z, yaw, pitch));
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
}
