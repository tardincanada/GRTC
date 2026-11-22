package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
 * Dev-only (./gradlew runFusionShots, 3.62.0): the Space Telescope's forecast in chat, a meteorite streaking down at
 * night, the impact, the crater by day with the chat line that marks it on the Atlas, and the three kinds side by side.
 */
public class FusionShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.fusionshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private static String part(int dx, int dy, int dz, boolean litVessels) {
		int ring = Math.max(Math.abs(dx), Math.abs(dz));
		if (ring == 0) {
			return "periodic_elements:central_solenoid";
		}
		if (ring == 1) {
			return dy == 0 ? "periodic_elements:vacuum_vessel" + (litVessels ? "[lit=true]" : "") : "periodic_elements:lithium_blanket";
		}
		return "periodic_elements:toroidal_field_coil";
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
				wc.setWorldName("fusion");
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
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY(), z = base.getZ();
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set 6000");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			// the working ring: centre 8 north of the player, middle layer one above the ground
			for (int dy = -1; dy <= 1; dy++) {
				for (int dx = -2; dx <= 2; dx++) {
					for (int dz = -2; dz <= 2; dz++) {
						cmd(client, String.format("setblock %d %d %d %s", x + dx, y + 1 + dy, z - 8 + dz, part(dx, dy, dz, false)));
						// the cut-away: the middle layer alone, 8 east, its vessels lit
						if (dy == 0) {
							cmd(client, String.format("setblock %d %d %d %s", x + 8 + dx, y, z - 8 + dz, part(dx, 0, dz, true)));
						}
					}
				}
			}
			cmd(client, String.format("setblock %d %d %d periodic_elements:tokamak[facing=south]", x, y + 1, z - 5));
			cmd(client, String.format("setblock %d %d %d periodic_elements:creative_generator", x, y, z - 5));
			server.execute(() -> ResearchState.get(server).grant(server, id, Research.all().stream().map(Research.Project::id).toList()));
		}
		if (worldTick == 70) {
			BlockPos t = base.add(0, 1, -5);
			server.execute(() -> {
				if (server.getOverworld().getBlockEntity(t) instanceof PowerBlockEntity be) {
					be.setStack(0, stack("deuterium", 16));
					be.setStack(1, stack("tritium_vial", 4));
					be.setStack(2, stack("lithium", 16));
					be.setStack(3, stack("helium", 4));
				}
			});
			player.setPosition(base.getX() + 9.5, base.getY() + 5.0, base.getZ() + 3.5);
			player.setYaw(160);
			player.setPitch(28);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		if (worldTick == 260) {   // lit by now (100 ticks of ignition)
			shot(client, "tokamak_day");
			cmd(client, "time set 18000");
		}
		if (worldTick == 290) {
			shot(client, "tokamak_night");
		}
		if (worldTick == 295) {   // close on the cut-away layer
			player.setPosition(base.getX() + 8.5, base.getY() + 4.5, base.getZ() - 3.0);
			player.setYaw(180);
			player.setPitch(50);
		}
		if (worldTick == 320) {
			shot(client, "tokamak_cutaway");
			cmd(client, "time set 6000");
		}
		if (worldTick == 345) {
			shot(client, "tokamak_cutaway_day");
		}
		if (worldTick == 350) {
			PeriodicElements.LOGGER.info("[fusionshots] done");
			client.scheduleStop();
		}
	}

	private static ItemStack stack(String id, int n) {
		return new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", id)), n);
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
