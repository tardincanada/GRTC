package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.CompendiumScreen;
import com.periodicelements.compendium.CompendiumData;
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
 * Dev-only (./gradlew runFaunaShots, 3.50.0): a flat world at noon with the stage-1 animals standing still, tropical ones
 * by a pond and dry-land ones on sand, close-ups of the small ones, the cochineal and lac colonies, the worm bin and the
 * sericulture tray, then the Animal Compendium's corner of the screen and its book.
 */
public class FaunaShots implements ClientModInitializer {
	private static final int STAGE = Integer.getInteger("pe.faunastage", 1);
	/** Each stage's two scenes: the first by a pond (water animals go in it), the second on the stage's own ground. */
	private static final String[][][] SCENES = {
			{{"capybara", "tapir", "toco_toucan", "sloth", "golden_poison_frog", "caiman"},
					{"ostrich", "zebra", "gila_monster", "deathstalker", "desert_locust"}},
			{{"eurasian_beaver", "mallard", "sturgeon", "american_alligator", "peruvian_booby"},
					{"red_deer", "wild_boar", "pheasant", "hedgehog"}},
			{{"moose", "brown_bear", "eurasian_lynx", "stoat"},
					{"reindeer", "yak", "alpaca", "alpine_marmot"}},
			{{"ringed_seal", "walrus", "green_crab", "horseshoe_crab"},
					{"sperm_whale", "greenland_shark", "cuttlefish", "crystal_jelly", "murex", "atlantic_herring"}},
	};
	private static final String[] TROPICAL = SCENES[STAGE - 1][0];
	private static final String[] ARID = SCENES[STAGE - 1][1];
	private static final String GROUND = STAGE == 1 ? "minecraft:sand" : STAGE == 2 ? "minecraft:grass_block" : STAGE == 3 ? "minecraft:snow_block" : "minecraft:sand";
	private static final String TAG = STAGE == 1 ? "" : "s" + STAGE + "_";
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.faunashots")) {
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
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("fauna");
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
		String lang = System.getProperty("pe.lang", "en_us");
		if (worldTick == 10) {
			client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
		}
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
			cmd(client, "compendium forget");
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 30, y, z - 30, x + 30, y + 10, z + 60));
			// Tropical scene: a pond on the left, jungle bushes behind.
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:water", x - 10, y - 1, z - 3, x - 3, y - 1, z + 2));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:jungle_leaves[persistent=true]", x - 12, y, z - 6, x + 12, y + 2, z - 5));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:bamboo", x + 6, y, z - 4, x + 9, y + 3, z - 4));
			int inPond = 0, onLand = 0;
			for (String id : TROPICAL) {
				var sp = com.periodicelements.fauna.Fauna.SPECIES.get(id);
				boolean wet = sp != null && !sp.kind().equals("land");
				if (wet && inPond < 3) {
					summon(client, id, x - 8.5 + inPond++ * 2.6, y - (sp.kind().equals("water") ? 0.6 : 0.4), z + 0.5, 20 - inPond * 8);
				} else {
					summon(client, id, x - 0.5 + onLand++ * 3.0, y, z + 0.5, 10 - onLand * 8);
				}
			}
			// Dry scene: sand, a few cacti with cochineal, dead bushes.
			int dz = z + 40;
			cmd(client, String.format("fill %d %d %d %d %d %d %s", x - 14, y - 1, dz - 8, x + 14, y - 1, dz + 8, GROUND));
			if (STAGE == 2) {
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:oak_leaves[persistent=true]", x - 12, y, dz - 7, x + 12, y + 2, dz - 6));
			} else if (STAGE == 3) {
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:spruce_leaves[persistent=true]", x - 12, y, dz - 7, x + 12, y + 3, dz - 6));
			}
			if (STAGE == 4) {
				// a sea basin three blocks deep (the flat world ends at bedrock four blocks down), clear water over sand
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:sand", x - 14, y - 4, dz - 8, x + 14, y - 4, dz + 8));
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:water", x - 14, y - 3, dz - 8, x + 14, y - 1, dz + 8));
				for (int i = 0; i < ARID.length; i++) {
					summon(client, ARID[i], x - 9.5 + i * 3.8, y - (i == 0 ? 3.0 : 2.2), dz + 0.5 + (i >= 2 ? 3 : 0), 25 - i * 10);
				}
				for (int i = 0; i < 4; i++) {
					cmd(client, String.format("setblock %d %d %d periodic_elements:pearl_oyster[waterlogged=true]", x + 2 + i, y - 3, dz + 5));
				}
			} else {
				for (int i = 0; i < ARID.length; i++) {
					summon(client, ARID[i], x - 7.5 + i * 3.6, y, dz + 0.5 + (i >= 2 ? 3 : 0), 25 - i * 10);
				}
			}
			if (STAGE == 1) {
				cmd(client, String.format("setblock %d %d %d minecraft:dead_bush", x - 10, y, dz - 3));
				cmd(client, String.format("setblock %d %d %d minecraft:dead_bush", x + 9, y, dz - 2));
			}
			// Colonies, the worm bin and the sericulture tray, in a row behind the camera of the first scene.
			int cz = z + 20;
			if (STAGE == 2) {
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:stone", x - 5, y - 1, cz - 1, x + 6, y - 1, cz + 2));
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:water", x - 5, y - 1, cz + 1, x - 1, y - 1, cz + 1));
				cmd(client, String.format("fill %d %d %d %d %d %d periodic_elements:beaver_dam", x - 5, y - 1, cz, x - 1, y, cz));
				cmd(client, String.format("setblock %d %d %d minecraft:water", x - 3, y, cz - 1));
				for (int i = 0; i < 4; i++) {
					cmd(client, String.format("setblock %d %d %d periodic_elements:guano[layers=%d]", x + 2 + i, y, cz + 1, i + 1));
				}
				cmd(client, String.format("summon periodic_elements:peruvian_booby %d %d %d {NoAI:1b,PersistenceRequired:1b,Rotation:[20f,0f]}", x + 4, y, cz));
			} else if (STAGE == 3) {
				for (int i = 0; i < 5; i++) {
					cmd(client, String.format("setblock %d %d %d periodic_elements:reindeer_lichen", x - 3 + i * 2, y, cz + 1));
				}
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:snow_block", x - 5, y - 1, cz - 1, x + 6, y - 1, cz + 2));
			}
			if (STAGE == 1) {
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:sand", x - 4, y - 1, cz - 2, x - 2, y - 1, cz + 2));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:cactus", x - 3, y, cz, x - 3, y + 2, cz));
			for (int h = 0; h < 3; h++) {
				cmd(client, String.format("setblock %d %d %d periodic_elements:cochineal_colony[facing=south,age=%d]", x - 3, y + h, cz + 1, 2 - h % 2));
			}
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:jungle_log", x, y, cz, x, y + 3, cz));
			for (int h = 0; h < 3; h++) {
				cmd(client, String.format("setblock %d %d %d periodic_elements:lac_colony[facing=south,age=%d]", x, y + h, cz + 1, 2 - h % 2));
			}
			cmd(client, String.format("setblock %d %d %d periodic_elements:worm_bin[stage=2]", x + 3, y, cz + 1));
			cmd(client, String.format("setblock %d %d %d periodic_elements:sericulture_tray[stage=3]", x + 5, y, cz + 1));
			}
		}
		int x = base == null ? 0 : base.getX(), y = base == null ? 0 : base.getY(), z = base == null ? 0 : base.getZ();
		switch (worldTick) {
			case 70 -> {
				look(player, x + 0.5, y + 3.0, z + 12.5, 180, 12);
				player.getAbilities().flying = true;
				client.options.hudHidden = true;
			}
			case 160 -> shot(client, "fauna_tropical");
			case 165 -> look(player, x - 4.0, y + 1.0, z + 4.0, 180, 22);
			case 200 -> shot(client, "fauna_tropical_close");
			case 205 -> look(player, x + 0.5, y + 3.5, z + 54.5, 180, 12);
			case 250 -> shot(client, "fauna_dry");
			case 255 -> look(player, x + 1.5, y + 1.2, z + 47.5, 180, 25);
			case 290 -> shot(client, "fauna_dry_small");
			case 295 -> look(player, x + 1.5, y + 2.0, z + 26.0, 180, 18);
			case 330 -> shot(client, "fauna_colonies_bins");
			// The Animal Compendium in the hotbar, looking at the capybara.
			case 335 -> {
				client.options.hudHidden = false;
				cmd(client, "item replace entity @s hotbar.0 with periodic_elements:animal_compendium");
				player.getInventory().selectedSlot = 0;
				var first = com.periodicelements.fauna.Fauna.SPECIES.get(TROPICAL[0]);
				look(player, first != null && !first.kind().equals("land") ? x - 8.5 : x - 0.5, y + 1.0, z + 6.0, 180, 26);
			}
			case 420 -> shot(client, "fauna_compendium_hud");
			case 425 -> {
				CompendiumScreen.startBiome = STAGE == 1 ? "jungle" : STAGE == 2 ? "river" : STAGE == 3 ? "taiga" : "frozen_ocean";
				CompendiumScreen.startTree = TROPICAL[0];
				client.setScreen(new CompendiumScreen(CompendiumData.ANIMAL));
			}
			case 460 -> shot(client, "fauna_compendium_first");
			case 465 -> {
				client.setScreen(null);
				cmd(client, "compendium reveal");
			}
			case 500 -> {
				CompendiumScreen.startBiome = STAGE == 1 ? "jungle" : STAGE == 2 ? "river" : STAGE == 3 ? "taiga" : "frozen_ocean";
				CompendiumScreen.startTree = TROPICAL[0];
				client.setScreen(new CompendiumScreen(CompendiumData.ANIMAL));
			}
			case 540 -> shot(client, "fauna_compendium_capybara");
			case 545 -> {
				CompendiumScreen.startBiome = STAGE == 1 ? "desert" : STAGE == 2 ? "forest" : STAGE == 3 ? "snowy_plains" : "deep_ocean";
				CompendiumScreen.startTree = null;
				client.setScreen(new CompendiumScreen(CompendiumData.ANIMAL));
			}
			case 580 -> shot(client, "fauna_compendium_desert");
			case 590 -> {
				PeriodicElements.LOGGER.info("[faunashots] done");
				client.scheduleStop();
			}
			default -> { }
		}
	}

	private static void look(net.minecraft.client.network.ClientPlayerEntity p, double x, double y, double z, float yaw, float pitch) {
		p.setPosition(x, y, z);
		p.setYaw(yaw);
		p.setPitch(pitch);
	}

	private static void summon(MinecraftClient client, String id, double x, double y, double z, float yaw) {
		cmd(client, String.format(java.util.Locale.ROOT, "summon periodic_elements:%s %.2f %.2f %.2f {NoAI:1b,PersistenceRequired:1b,Rotation:[%.1ff,0f]}",
				id, x, y, z, yaw));
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
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + TAG + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> { });
	}
}
