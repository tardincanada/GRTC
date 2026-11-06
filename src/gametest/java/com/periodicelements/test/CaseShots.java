package com.periodicelements.test;

import com.periodicelements.ElementItem;
import com.periodicelements.PeriodicElements;
import com.periodicelements.SubstanceItem;
import com.periodicelements.atlas.Atlas;
import com.periodicelements.client.ClientConfig;
import com.periodicelements.client.ConfigScreen;
import com.periodicelements.research.ProgressBook;
import com.periodicelements.samplecase.SampleCase;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.world.gen.WorldPresets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dev-only (./gradlew -PwithModMenu runCaseShots, 3.31.0): a flat world, the Sample Case full of samples (screen and
 * tooltip), the settings screen, and the minimap moved to the bottom-right corner; screenshots to run/screenshots.
 */
public class CaseShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private ClientConfig.Corner oldCorner;
	private int oldSize;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.caseshots")) {
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
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("case");
				wc.setGameMode(WorldCreator.Mode.SURVIVAL);
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
		var server = client.getServer();
		UUID id = client.player.getUuid();
		switch (worldTick) {
			case 40 -> {
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "time set 6000");
				server.execute(() -> ProgressBook.setLevel(server, id, 7, true));
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
				server.execute(() -> {
					var sp = server.getPlayerManager().getPlayer(id);
					sp.getInventory().clear();
					ItemStack c = new ItemStack(SampleCase.CASE);
					List<ItemStack> list = new ArrayList<>();
					List<Item> els = Registries.ITEM.stream().filter(i -> i instanceof ElementItem).limit(24).toList();
					List<Item> subs = Registries.ITEM.stream().filter(i -> i instanceof SubstanceItem).limit(14).toList();
					int n = 0;
					for (Item i : els) list.add(new ItemStack(i, 1 + (n++ * 7) % 64));
					for (Item i : subs) list.add(new ItemStack(i, 1 + (n++ * 11) % 32));
					SampleCase.store(c, list);
					sp.getInventory().setStack(0, c);
					sp.getInventory().setStack(1, new ItemStack(Atlas.GPS_RECEIVER));
					sp.getInventory().selectedSlot = 0;
				});
			}
			case 100 -> server.execute(() -> {
				var sp = server.getPlayerManager().getPlayer(id);
				SampleCase.CASE.use(sp.getWorld(), sp, Hand.MAIN_HAND);
			});
			case 130 -> mouse(client, 2, 2);
			case 140 -> shot(client, "case_screen");
			case 145 -> {
				if (client.currentScreen instanceof HandledScreen<?> hs) {
					// The case itself sits in the first hotbar place, at the bottom of the screen.
					int x = (hs.width - 176) / 2 + 8 + 8, y = (hs.height - (114 + 6 * 18)) / 2 + 114 + 6 * 18 - 24 + 8;
					double scale = client.getWindow().getScaleFactor();
					mouse(client, x * scale, y * scale);
				}
			}
			case 160 -> shot(client, "case_tooltip");
			case 170 -> client.setScreen(new ConfigScreen(null));
			case 190 -> shot(client, "config_screen");
			case 200 -> {
				client.setScreen(null);
				ClientConfig cfg = ClientConfig.get();
				oldCorner = cfg.minimapCorner;
				oldSize = cfg.minimapSize;
				cfg.minimapCorner = ClientConfig.Corner.BOTTOM_RIGHT;
				cfg.minimapSize = 128;
				server.execute(() -> {
					var p = server.getPlayerManager().getPlayer(id);
					com.periodicelements.atlas.Atlas.record(p.getServerWorld(),
							com.periodicelements.atlas.AtlasData.of(server, id), p.getBlockX(), p.getBlockZ(), 100, true);
				});
			}
			case 600 -> shot(client, "minimap_corner");
			case 610 -> {
				ClientConfig cfg = ClientConfig.get();
				cfg.minimapCorner = oldCorner;
				cfg.minimapSize = oldSize;
				cfg.save();
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void mouse(MinecraftClient client, double x, double y) {
		try {
			var m = net.minecraft.client.Mouse.class.getDeclaredMethod("onCursorPos", long.class, double.class, double.class);
			m.setAccessible(true);
			m.invoke(client.mouse, client.getWindow().getHandle(), x, y);
		} catch (ReflectiveOperationException e) {
			PeriodicElements.LOGGER.warn("[caseshots] could not move the mouse", e);
		}
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[caseshots] {}", text.getString()));
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
