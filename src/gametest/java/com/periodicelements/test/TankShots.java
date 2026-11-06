package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.FieldGuideScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runTankShots, 3.38.0): a flat world with a multiblock tank (gauge, valves, compressor), a
 * Pneumatic Trough piping its gases into it and a pump filling a Storage Tank from it; then the Field Guide's page.
 * Language from -Dpe.lang.
 */
public class TankShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.tankshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		String lang = System.getProperty("pe.lang", "en_us");
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) {
				return;
			}
			tick++;
			if (tick == 5 && !client.options.language.equals(lang)) {
				client.options.language = lang;
				client.getLanguageManager().setLanguage(lang);
				client.reloadResources();
				return;
			}
			if (!created && tick > 30 && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 30 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("tanks");
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
		if (worldTick == 60) {
			base = player.getBlockPos();
			build(client);
		}
		if (worldTick == 70) {
			var server = client.getServer();
			UUID id = player.getUuid();
			BlockPos trough = base.add(-7, 0, -8), controller = base.add(-1, 1, -6), store = base.add(7, 0, -8);
			server.execute(() -> {
				var w = server.getOverworld();
				if (w.getBlockEntity(trough) instanceof com.periodicelements.machine.MachineBlockEntity be) {
					be.setOwner(id);
				}
				if (w.getBlockEntity(controller) instanceof com.periodicelements.fluid.TankControllerBlockEntity c) {
					c.measure();
					c.fill(Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, "oxygen")), 500_000, Double.NaN, false);
				}
			});
		}
		if (worldTick == 96) {
			// A redstone block on the pump stops it, so the Storage Tank is shown part full.
			cmd(client, String.format("setblock %d %d %d minecraft:redstone_block", base.getX() + 5, base.getY() + 1, base.getZ() - 8));
		}
		if (worldTick == 100) {
			view(client, base.getX() + 0.5, base.getY() + 4.2, base.getZ() + 2.5, 22);
		}
		if (worldTick == 240) {
			shot(client, "tank_overview");
		}
		if (worldTick == 250) {
			view(client, base.getX() + 0.5, base.getY() + 0.9, base.getZ() - 3.6, 0);
		}
		if (worldTick == 280) {
			shot(client, "tank_gauge");
		}
		if (worldTick == 290) {
			view(client, base.getX() + 6.5, base.getY() + 0.3, base.getZ() - 4.2, 18);
		}
		if (worldTick == 320) {
			shot(client, "tank_storage_gauge");
		}
		if (worldTick == 330) {
			view(client, base.getX() - 4.5, base.getY() + 1.2, base.getZ() - 2.5, 22);
		}
		if (worldTick == 360) {
			shot(client, "tank_pipes");
		}
		if (worldTick == 370) {
			client.options.hudHidden = false;
			FieldGuideScreen.revealOverride = 7;
			client.setScreen(new FieldGuideScreen());
		}
		if (worldTick == 380) {
			((FieldGuideScreen) client.currentScreen).typeSearch(lang.equals("pt_br") ? "mostrador de tanque na casca" : "tank gauge in the shell");
		}
		if (worldTick == 395) {
			((FieldGuideScreen) client.currentScreen).openFirstResult();
		}
		if (worldTick == 415) {
			shot(client, "tank_guide");
		}
		if (worldTick == 420) {
			client.setScreen(new FieldGuideScreen());
		}
		if (worldTick == 425) {
			((FieldGuideScreen) client.currentScreen).typeSearch(lang.equals("pt_br") ? "uma linha pode ter" : "a run may be");
		}
		if (worldTick == 440) {
			((FieldGuideScreen) client.currentScreen).openFirstResult();
		}
		if (worldTick == 460) {
			shot(client, "tank_guide_pipes");
		}
		if (worldTick == 470) {
			PeriodicElements.LOGGER.info("[tankshots] done");
			client.scheduleStop();
		}
	}

	private static void view(MinecraftClient client, double x, double y, double z, float pitch) {
		var player = client.player;
		player.getAbilities().flying = true;
		player.setPosition(x, y, z);
		player.setYaw(180);
		player.setPitch(pitch);
		client.options.hudHidden = true;
	}

	private void build(MinecraftClient client) {
		int x = base.getX(), y = base.getY(), z = base.getZ();
		cmd(client, "gamerule sendCommandFeedback false");
		cmd(client, "time set noon");
		cmd(client, "gamerule doDaylightCycle false");
		cmd(client, "gamerule doWeatherCycle false");
		cmd(client, "gamerule doMobSpawning false");
		cmd(client, "kill @e[type=!player]");
		cmd(client, String.format("fill %d %d %d %d %d %d air", x - 10, y, z - 14, x + 10, y + 8, z + 3));
		// The tank: 5x5x5 of Tank Plates, inside 3x3x3, x-2..x+2, z-10..z-6.
		cmd(client, String.format("fill %d %d %d %d %d %d periodic_elements:tank_wall hollow", x - 2, y, z - 10, x + 2, y + 4, z - 6));
		cmd(client, String.format("setblock %d %d %d periodic_elements:tank_controller", x - 1, y + 1, z - 6));
		cmd(client, String.format("setblock %d %d %d periodic_elements:tank_gauge[facing=south]", x, y + 2, z - 6));
		cmd(client, String.format("setblock %d %d %d periodic_elements:tank_compressor", x, y + 4, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:tank_valve", x - 2, y + 1, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:tank_valve", x + 2, y + 1, z - 8));
		// Left: a Pneumatic Trough splitting saltpetre, its gases down a pipe into the tank's west valve.
		cmd(client, String.format("setblock %d %d %d periodic_elements:pneumatic_trough[facing=south]", x - 7, y, z - 8));
		cmd(client, String.format("item replace block %d %d %d container.0 with periodic_elements:potassium_nitrate 64", x - 7, y, z - 8));
		cmd(client, String.format("item replace block %d %d %d container.1 with minecraft:charcoal 64", x - 7, y, z - 8));
		cmd(client, "research grant @s pneumatic_chemistry");
		for (int px = x - 6; px <= x - 3; px++) {
			cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pipe", px, y, z - 8));
		}
		cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pipe", x - 3, y + 1, z - 8));
		// Right: from the east valve a pipe to a pump, which fills a Storage Tank with a gauge on its front.
		cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pipe", x + 3, y + 1, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pipe", x + 3, y, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pipe", x + 4, y, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pump[facing=east]", x + 5, y, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:fluid_pipe", x + 6, y, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:storage_tank", x + 7, y, z - 8));
		cmd(client, String.format("setblock %d %d %d periodic_elements:tank_gauge[facing=south]", x + 7, y, z - 7));
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
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[tankshots] {}", text.getString()));
	}
}
