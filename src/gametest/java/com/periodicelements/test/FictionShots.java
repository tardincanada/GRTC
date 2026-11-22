package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.space.Mars;
import com.periodicelements.space.Moon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runFictionShots, 3.67.0): the materials of science fiction at a base on Mars: the Frontier Science
 * Laboratory, an Annihilation Reactor running, a Magnetic Shield, the Thermal Suit, the three new engines, and a rocket
 * with a Fusion Drive over a chemical stage; by day and by night.
 */
public class FictionShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private volatile int baseY = Integer.MIN_VALUE;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.fictionshots")) {
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
			if (tick % 200 == 0) {
				PeriodicElements.LOGGER.info("[fictionshots] waiting on screen {}", client.currentScreen.getClass().getName());
			}
			// a modded dimension may ask to confirm experimental settings: go on
			if (tick % 40 == 30 && client.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmScreen confirm) {
				PeriodicElements.LOGGER.info("[fictionshots] confirm screen: {}", confirm.getTitle().getString());
				confirm.children().stream().filter(c -> c instanceof ButtonWidget).findFirst()
						.map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("fiction");
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
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "time set 3500");
			server.execute(() -> ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList()));
			cmd(client, "execute in periodic_elements:mars run tp @s 0.5 200 0.5");
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				int top = 0;
				for (int dx = -20; dx <= 30; dx += 5) {
					for (int dz = -20; dz <= 20; dz += 5) {
						top = Math.max(top, Moon.surface(mars, dx, dz));
					}
				}
				baseY = top;
			});
		}
		if (worldTick == 120 && baseY != Integer.MIN_VALUE) {
			int y = baseY;
			client.options.hudHidden = true;
			client.options.getViewDistance().setValue(16);
			cmd(client, String.format("fill -20 %d -20 30 %d 20 periodic_elements:martian_regolith", y - 12, y));
			cmd(client, String.format("fill -20 %d -20 30 %d 20 minecraft:air", y + 1, y + 15));
			cmd(client, String.format("fill -20 %d -20 30 %d 20 minecraft:air", y + 16, y + 40));
			// the lab floor, the station, the reactor and the shield
			cmd(client, String.format("fill -6 %d -5 6 %d 5 minecraft:smooth_stone", y, y));
			cmd(client, String.format("setblock -3 %d -3 periodic_elements:frontier_laboratory[facing=south]", y + 1));
			cmd(client, String.format("setblock 0 %d -3 periodic_elements:annihilation_reactor[facing=south]", y + 1));
			cmd(client, String.format("setblock 3 %d -3 periodic_elements:magnetic_shield", y + 1));
			// the three engines on show
			cmd(client, String.format("setblock -3 %d 2 periodic_elements:metallic_hydrogen_engine", y + 1));
			cmd(client, String.format("setblock 0 %d 2 periodic_elements:antimatter_engine", y + 1));
			cmd(client, String.format("setblock 3 %d 2 periodic_elements:fusion_drive", y + 1));
			cmd(client, String.format("summon minecraft:armor_stand 6.5 %d 0.5 {Rotation:[90f,0f],ArmorItems:["
					+ "{id:\"periodic_elements:thermal_suit_boots\",count:1},{id:\"periodic_elements:thermal_suit_leggings\",count:1},"
					+ "{id:\"periodic_elements:thermal_suit_chestplate\",count:1},{id:\"periodic_elements:thermal_suit_helmet\",count:1}]}", y + 1));
			// a rocket: a kerosene stage lifting a Fusion Drive stage
			int rx = 14;
			cmd(client, String.format("setblock %d %d 0 periodic_elements:launch_pad", rx, y));
			cmd(client, String.format("setblock %d %d 0 periodic_elements:kerolox_engine", rx, y + 1));
			cmd(client, String.format("fill %d %d 0 %d %d 0 periodic_elements:propellant_tank", rx, y + 2, rx, y + 5));
			cmd(client, String.format("setblock %d %d 0 periodic_elements:fusion_drive", rx, y + 6));
			cmd(client, String.format("fill %d %d 0 %d %d 0 periodic_elements:propellant_tank", rx, y + 7, rx, y + 8));
			cmd(client, String.format("setblock %d %d 0 periodic_elements:payload_fairing", rx, y + 9));
			cmd(client, String.format("tp @s 0.5 %d 11.5 180 22", y + 5));
		}
		if (worldTick == 130) {
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				if (mars.getBlockEntity(new BlockPos(0, baseY + 1, -3)) instanceof PowerBlockEntity be) {
					be.setStack(0, new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", "antihydrogen")), 4));
					be.setStack(1, new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", "hydrogen")), 4));
				}
			});
		}
		if (worldTick == 250) {
			server.execute(() -> {
				ServerWorld mars = server.getWorld(Mars.MARS);
				var be = mars.getBlockEntity(new BlockPos(0, baseY + 1, -3));
				PeriodicElements.LOGGER.info("[fictionshots] reactor {} {}", be, be instanceof PowerBlockEntity p
						? p.stored() + " " + p.getStack(0) + " " + p.getStack(1) + " " + mars.getBlockState(new BlockPos(0, baseY + 1, -3)) : "");
			});
		}
		if (worldTick == 260) {
			shot(client, "fiction_base");
			cmd(client, String.format("tp @s 4.5 %d 4.5 135 30", baseY + 4));
		}
		if (worldTick == 300) {
			shot(client, "fiction_engines");
			cmd(client, String.format("tp @s 9.5 %d 6.5 -125 -15", baseY + 3));
		}
		if (worldTick == 340) {
			shot(client, "fiction_rocket");
			cmd(client, "time set 18000");
			cmd(client, String.format("tp @s 1.5 %d 1.5 180 28", baseY + 3));
		}
		if (worldTick == 400) {
			shot(client, "fiction_night");
		}
		if (worldTick == 410) {
			PeriodicElements.LOGGER.info("[fictionshots] done");
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
