package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.creative.CreativeTabs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPresets;

import java.util.List;
import java.util.UUID;

/**
 * Dev-only (./gradlew runCreativeShots, 3.51.0): a flat creative world; screenshots every creative tab of the mod,
 * then the creative test blocks at work. Language from -Dpe.lang (default en_us). Files in run/screenshots.
 */
public class CreativeShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private List<String> tabs;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.creativeshots")) {
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
				wc.setWorldName("creative");
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
		if (tabs == null) {
			tabs = CreativeTabs.tabs();
		}
		if (worldTick == 40) {
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "time set 6000");
			client.getToastManager().clear();
			client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
		}
		// every tab: open at 60 + 20 i, shoot 10 ticks later
		int t = worldTick - 60;
		if (t >= 0 && t < tabs.size() * 20) {
			int i = t / 20;
			if (t % 20 == 0) {
				open(client, tabs.get(i));
			} else if (t % 20 == 8) {
				parkMouse(client);
			} else if (t % 20 == 14) {
				shot(client, "tab_" + String.format("%02d", i + 1) + "_" + tabs.get(i));
			}
			return;
		}
		int s = t - tabs.size() * 20;
		switch (s) {
			case 0 -> {
				client.setScreen(null);
				scene(client);
			}
			case 80 -> shot(client, "creative_blocks");
			case 100 -> client.scheduleStop();
			default -> {
			}
		}
	}

	/** The five blocks in a row with what they feed, and a blast furnace from the Structure Builder. */
	private static void scene(MinecraftClient client) {
		cmd(client, "fill ~-12 ~ ~-4 ~12 ~10 ~16 air");
		cmd(client, "tp @s ~ ~ ~ 180 25");
		String[] blocks = {"creative_generator", "creative_fluid_source", "creative_item_source", "creative_void", "creative_accelerator"};
		for (int i = 0; i < blocks.length; i++) {
			int x = 6 - 3 * i;
			cmd(client, "setblock ~" + x + " ~ ~-5 periodic_elements:" + blocks[i]);
		}
		cmd(client, "setblock ~6 ~ ~-6 periodic_elements:copper_wire");
		cmd(client, "setblock ~6 ~ ~-7 periodic_elements:electrolysis_cell[facing=south]");
		cmd(client, "setblock ~3 ~ ~-6 periodic_elements:fluid_pipe");
		cmd(client, "setblock ~3 ~ ~-7 periodic_elements:storage_tank");
		cmd(client, "setblock ~0 ~1 ~-5 minecraft:hopper");
		cmd(client, "setblock ~-3 ~1 ~-5 minecraft:hopper");
		cmd(client, "setblock ~-6 ~ ~-6 minecraft:farmland");
		cmd(client, "setblock ~-6 ~1 ~-6 minecraft:wheat");
		cmd(client, "setblock ~-7 ~ ~-6 minecraft:farmland");
		cmd(client, "setblock ~-7 ~1 ~-6 minecraft:carrots");
		cmd(client, "summon minecraft:cow ~-5 ~ ~-7 {Age:-24000}");
	}

	private static void open(MinecraftClient client, String tab) {
		ItemGroup group = Registries.ITEM_GROUP.get(RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(PeriodicElements.MOD_ID, tab)));
		if (!(client.currentScreen instanceof CreativeInventoryScreen)) {
			client.setScreen(new CreativeInventoryScreen(client.player, client.player.networkHandler.getEnabledFeatures(), true));
		}
		CreativeInventoryScreen screen = (CreativeInventoryScreen) client.currentScreen;
		FabricCreativeInventoryScreen fabric = (FabricCreativeInventoryScreen) screen;
		fabric.switchToPage(fabric.getPage(group));
		try {
			var m = CreativeInventoryScreen.class.getDeclaredMethod("setSelectedTab", ItemGroup.class);
			m.setAccessible(true);
			m.invoke(screen, group);
		} catch (ReflectiveOperationException e) {
			PeriodicElements.LOGGER.error("[creativeshots] cannot select tab", e);
		}
	}

	private static void shot(MinecraftClient client, String name) {
		// keep the mouse off the item grid (no tooltip) and the chat empty
		parkMouse(client);
		client.inGameHud.getChatHud().clear(false);
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[creativeshots] {}", text.getString()));
	}

	/** Puts the mouse in the bottom-left corner (xvfb ignores GLFW's cursor moves, so set the fields). */
	private static void parkMouse(MinecraftClient client) {
		try {
			for (String f : new String[]{"x", "y"}) {
				var field = net.minecraft.client.Mouse.class.getDeclaredField(f);
				field.setAccessible(true);
				field.setDouble(client.mouse, f.equals("x") ? 2 : client.getWindow().getHeight() - 2);
			}
		} catch (ReflectiveOperationException e) {
			PeriodicElements.LOGGER.error("[creativeshots] cannot move the mouse", e);
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
}
