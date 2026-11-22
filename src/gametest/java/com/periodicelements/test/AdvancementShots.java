package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.FieldGuideScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (PE_LANG=pt_br ./gradlew runAdvancementShots, 3.60.0): a new flat survival world; screenshots the
 * starter Field Guide in the inventory, its "First steps" page, and the Elementar advancement tab at level 0, after
 * Fired Pottery and Copper Smelting, and at level 2. Saved to run/screenshots.
 */
public class AdvancementShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.advancementshots")) {
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
			if (tick == 5) {
				String lang = System.getProperty("pe.lang", "en_us");
				if (!client.options.language.equals(lang)) {
					client.options.language = lang;
					client.getLanguageManager().setLanguage(lang);
					client.reloadResources();
				}
			}
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("advancements");
				wc.setGameMode(WorldCreator.Mode.SURVIVAL);
				wc.setCheatsEnabled(true);
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(WorldPresets.FLAT))
						.findFirst().ifPresent(wc::setWorldType);
				screen.children().stream().filter(c -> c instanceof ButtonWidget b && b.getMessage().getString().equals(
						net.minecraft.text.Text.translatable("selectWorld.create").getString()))
						.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		boolean pt = client.options.language.equals("pt_br");
		switch (worldTick) {
			case 40 -> {
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "time set 6000");
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
			}
			case 60 -> client.setScreen(new InventoryScreen(client.player));
			case 80 -> shot(client, "starter_inventory");
			case 90 -> {
				FieldGuideScreen.revealOverride = 0;
				client.setScreen(new FieldGuideScreen());
			}
			case 100 -> ((FieldGuideScreen) client.currentScreen).typeSearch(pt ? "Primeiros passos" : "First steps");
			case 115 -> ((FieldGuideScreen) client.currentScreen).openFirstResult();
			case 135 -> shot(client, "starter_first_steps");
			case 140 -> {
				FieldGuideScreen.revealOverride = null;
				openTab(client);
			}
			case 170 -> shot(client, "tab_level0");
			case 175 -> {
				client.setScreen(null);
				cmd(client, "research grant @s fired_pottery");
				cmd(client, "research grant @s copper_smelting");
				cmd(client, "give @s periodic_elements:charcoal_pit");
				cmd(client, "give @s minecraft:charcoal 4");
				cmd(client, "give @s periodic_elements:potters_workshop");
			}
			case 215 -> shot(client, "toast");
			case 220 -> {
				client.getToastManager().clear();
				openTab(client);
			}
			case 250 -> shot(client, "tab_copper");
			case 255 -> {
				client.setScreen(null);
				cmd(client, "research grant @s level:2");
				cmd(client, "give @s periodic_elements:alchemists_laboratory");
			}
			case 300 -> {
				client.getToastManager().clear();
				openTab(client);
			}
			case 330 -> shot(client, "tab_level2");
			case 335 -> {
				if (client.currentScreen != null) {
					client.currentScreen.mouseDragged(300, 200, 0, -260, -40);
				}
			}
			case 350 -> shot(client, "tab_level2_right");
			case 360 -> {
				PeriodicElements.LOGGER.info("[advancementshots] done");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void openTab(MinecraftClient client) {
		var handler = client.player.networkHandler.getAdvancementHandler();
		var root = handler.getManager().get(Identifier.of(PeriodicElements.MOD_ID, "elementar/root"));
		client.setScreen(new AdvancementsScreen(handler));
		if (root != null) {
			handler.selectTab(root.getAdvancementEntry(), true);
		}
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_adv_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[advancementshots] {}", text.getString()));
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
