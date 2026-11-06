package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.ProgressBookScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runProgressBookShots, 3.20.0): makes a flat creative world, opens the Book of Progress, picks
 * level 4 and screenshots the page before and after, then the item's tooltip in the creative tab, to run/screenshots.
 * Language from -Dpe.lang (default en_us).
 */
public class ProgressBookShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.progressshots")) {
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
				wc.setWorldName("progress");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
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
		switch (worldTick) {
			case 40 -> {
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "time set 6000");
				cmd(client, "research revoke @s all");
				cmd(client, "item replace entity @s hotbar.0 with periodic_elements:progress_book");
				client.player.getInventory().selectedSlot = 0;
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
			}
			case 80 -> client.setScreen(new ProgressBookScreen());
			case 100 -> shot(client, "progress_book_level0");
			case 110 -> client.currentScreen.children().stream().filter(c -> c instanceof ButtonWidget).skip(4)
					.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			case 140 -> shot(client, "progress_book_level4");
			case 150 -> client.setScreen(null);
			case 170 -> shot(client, "progress_book_chat");
			case 180 -> client.scheduleStop();
			default -> {
			}
		}
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[progressshots] {}", text.getString()));
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
