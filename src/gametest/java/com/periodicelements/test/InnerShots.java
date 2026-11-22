package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.space.Mercury;
import com.periodicelements.space.Moon;
import com.periodicelements.space.Venus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runInnerShots, 3.68.0): Mercury's dawn band with the Sun on the horizon, the scorched day side and
 * the night side under the stars; Venus's Aerostat Deck over the acid clouds, the clouds from inside, and dusk.
 */
public class InnerShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private volatile int dawnX = Integer.MIN_VALUE, ground = 0;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.innershots")) {
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
			if (tick % 40 == 30 && client.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmScreen confirm) {
				confirm.children().stream().filter(c -> c instanceof ButtonWidget).findFirst()
						.map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("inner");
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
			return;
		}
		worldTick++;
		var server = client.getServer();
		UUID id = client.player.getUuid();
		String lang = System.getProperty("pe.lang", "en_us");
		if (worldTick == 20 && !client.options.language.equals(lang)) {
			client.options.language = lang;
			client.getLanguageManager().setLanguage(lang);
			client.reloadResources();
		}
		if (worldTick == 60) {
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "gamerule doDaylightCycle false");
			server.execute(() -> ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList()));
			cmd(client, "execute in periodic_elements:mercury run tp @s 0.5 200 0.5");
			client.options.getViewDistance().setValue(16);
			client.options.hudHidden = true;
		}
		if (worldTick == 80) {
			server.execute(() -> {
				ServerWorld mercury = server.getWorld(Mercury.MERCURY);
				// a fixed game time far from 0, so the dawn line sits a long way off the spawn
				int x = (int) Math.floor(Mercury.dawnLine(Mercury.clock(mercury))) + 14;
				dawnX = x;
				ground = Moon.surface(mercury, x, 0);
				fly(server, id);
				PeriodicElements.LOGGER.info("[innershots] Mercury dawn line at x {} ground {}", x - 14, ground);
			});
		}
		if (worldTick == 100 && dawnX != Integer.MIN_VALUE) {
			int x = dawnX, y = ground;
			// a camp in the dawn band: a Thermal Suit on a stand, solar panels facing the low Sun, a Magnetic Shield
			cmd(client, String.format("execute in periodic_elements:mercury run fill %d %d -6 %d %d 6 periodic_elements:mercurian_regolith", x - 6, y - 3, x + 6, y));
			cmd(client, String.format("execute in periodic_elements:mercury run fill %d %d -6 %d %d 6 minecraft:air", x - 6, y + 1, x + 6, y + 12));
			cmd(client, String.format("execute in periodic_elements:mercury run fill %d %d -3 %d %d -1 periodic_elements:solar_panel", x + 2, y + 1, x + 4, y + 1));
			cmd(client, String.format("execute in periodic_elements:mercury run setblock %d %d 3 periodic_elements:magnetic_shield", x + 3, y + 1));
			cmd(client, String.format("execute in periodic_elements:mercury run setblock %d %d 2 periodic_elements:mercurian_hollows", x + 1, y));
			cmd(client, String.format("execute in periodic_elements:mercury run summon minecraft:armor_stand %d.5 %d %d.5 {Rotation:[-90f,0f],ArmorItems:["
					+ "{id:\"periodic_elements:thermal_suit_boots\",count:1},{id:\"periodic_elements:thermal_suit_leggings\",count:1},"
					+ "{id:\"periodic_elements:thermal_suit_chestplate\",count:1},{id:\"periodic_elements:thermal_suit_helmet\",count:1}]}", x + 1, y + 1, 0));
			cmd(client, String.format("execute in periodic_elements:mercury run tp @s %d.5 %d 1.5 -100 4", x - 4, y + 3));
		}
		if (worldTick == 200) {
			shot(client, "mercury_dawn");
			cmd(client, String.format("execute in periodic_elements:mercury run tp @s %d.5 %d 0.5 -90 -12", dawnX + 160, ground + 20));
		}
		if (worldTick == 260) {
			server.execute(() -> {
				ServerWorld mercury = server.getWorld(Mercury.MERCURY);
				int g = Moon.surface(mercury, dawnX + 160, 0);
				cmd(client, String.format("execute in periodic_elements:mercury run tp @s %d.5 %d 0.5 -60 10", dawnX + 160, g + 6));
			});
		}
		if (worldTick == 340) {
			shot(client, "mercury_day");
			cmd(client, String.format("execute in periodic_elements:mercury run tp @s %d.5 %d 0.5 90 -15", dawnX - 200, ground + 20));
		}
		if (worldTick == 400) {
			server.execute(() -> {
				ServerWorld mercury = server.getWorld(Mercury.MERCURY);
				int g = Moon.surface(mercury, dawnX - 200, 0);
				cmd(client, String.format("execute in periodic_elements:mercury run tp @s %d.5 %d 0.5 90 -20", dawnX - 200, g + 4));
			});
		}
		if (worldTick == 480) {
			shot(client, "mercury_night");
			cmd(client, "execute in periodic_elements:venus run time set 2500");
			cmd(client, String.format("execute in periodic_elements:venus run tp @s 5.5 %d 12.5 160 18", Venus.DECK_Y + 6));
		}
		if (worldTick == 485) {
			fly(server, id);
		}
		if (worldTick == 500) {
			server.execute(() -> {
				ServerWorld venus = server.getWorld(Venus.VENUS);
				Venus.deck(venus, 0, 0);
				Venus.deck(venus, 9, 0);
			});
		}
		if (worldTick == 520) {
			int y = Venus.DECK_Y + 1;
			// a habitat on the deck: a glass dome kept in air by Life Support, solar panels, a suit on a stand
			cmd(client, String.format("execute in periodic_elements:venus run fill -2 %d -2 2 %d 2 minecraft:glass hollow", y, y + 3));
			cmd(client, String.format("execute in periodic_elements:venus run fill -1 %d -1 1 %d 1 minecraft:air", y, y + 2));
			cmd(client, String.format("execute in periodic_elements:venus run setblock 0 %d 0 periodic_elements:life_support", y));
			cmd(client, String.format("execute in periodic_elements:venus run fill 7 %d -2 11 %d 2 periodic_elements:solar_panel", y, y));
			cmd(client, String.format("execute in periodic_elements:venus run summon minecraft:armor_stand 4.5 %d 3.5 {Rotation:[200f,0f],ArmorItems:["
					+ "{id:\"periodic_elements:thermal_suit_boots\",count:1},{id:\"periodic_elements:thermal_suit_leggings\",count:1},"
					+ "{id:\"periodic_elements:thermal_suit_chestplate\",count:1},{id:\"periodic_elements:thermal_suit_helmet\",count:1}]}", y));
			cmd(client, String.format("execute in periodic_elements:venus run tp @s 5.5 %d 12.5 160 18", y + 5));
		}
		if (worldTick == 620) {
			shot(client, "venus_deck");
			cmd(client, String.format("execute in periodic_elements:venus run tp @s 0.5 %d 30.5 180 6", Venus.CLOUD_TOP + 4));
		}
		if (worldTick == 680) {
			shot(client, "venus_clouds");
			cmd(client, "execute in periodic_elements:venus run time set 12300");
			cmd(client, String.format("execute in periodic_elements:venus run tp @s -6.5 %d 6.5 -120 2", Venus.DECK_Y + 3));
		}
		if (worldTick == 760) {
			shot(client, "venus_dusk");
		}
		if (worldTick == 770) {
			PeriodicElements.LOGGER.info("[innershots] done");
			client.scheduleStop();
		}
	}

	private static void fly(net.minecraft.server.MinecraftServer server, UUID id) {
		server.execute(() -> {
			var pl = server.getPlayerManager().getPlayer(id);
			pl.getAbilities().flying = true;
			pl.sendAbilitiesUpdate();
		});
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
