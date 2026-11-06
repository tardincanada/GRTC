package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.logistics.SorterBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runLogisticsShots, 3.40.0): a flat world with a belt line. A chest of cobblestone and raw iron
 * over a stainless hopper feeds canvas belts, then rubber belts, then a Belt Sorter that drops the iron through a steel
 * hopper into a steel chest below, while the cobblestone rides a roller conveyor into a stainless chest. Takes a run of
 * frames for a GIF, then the chest and sorter screens.
 */
public class LogisticsShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.logisticsshots")) {
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
				wc.setWorldName("logistics");
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
		if (worldTick == 20 && !client.options.language.equals(lang)) {
			client.options.language = lang;
			client.getLanguageManager().setLanguage(lang);
			client.reloadResources();
		}
		if (worldTick == 60) {
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY() + 1, z = base.getZ() - 5;
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set noon");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 9, y - 1, z - 3, x + 9, y + 4, z + 6));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:smooth_stone", x - 9, y - 2, z - 3, x + 9, y - 2, z + 6));
			// Feed: an iron chest over a stainless hopper pointing east.
			cmd(client, String.format("setblock %d %d %d periodic_elements:iron_chest[facing=south]", x - 8, y + 1, z));
			cmd(client, String.format("setblock %d %d %d periodic_elements:stainless_hopper[facing=east]", x - 8, y, z));
			for (int i = -7; i <= -5; i++) {
				cmd(client, String.format("setblock %d %d %d periodic_elements:canvas_belt[facing=east]", x + i, y, z));
			}
			for (int i = -4; i <= -1; i++) {
				cmd(client, String.format("setblock %d %d %d periodic_elements:rubber_belt[facing=east]", x + i, y, z));
			}
			cmd(client, String.format("setblock %d %d %d periodic_elements:belt_sorter[facing=east]", x, y, z));
			cmd(client, String.format("setblock %d %d %d periodic_elements:steel_hopper[facing=down]", x, y - 1, z));
			cmd(client, String.format("setblock %d %d %d periodic_elements:steel_chest[facing=south]", x, y - 2, z));
			for (int i = 1; i <= 6; i++) {
				cmd(client, String.format("setblock %d %d %d periodic_elements:roller_conveyor[facing=east]", x + i, y, z));
			}
			cmd(client, String.format("setblock %d %d %d periodic_elements:stainless_chest[facing=south]", x + 7, y, z));
			var server = client.getServer();
			BlockPos chest = new BlockPos(x - 8, y + 1, z), sorter = new BlockPos(x, y, z);
			server.execute(() -> {
				var w = server.getOverworld();
				if (w.getBlockEntity(chest) instanceof Inventory inv) {
					for (int i = 0; i < inv.size(); i++) {
						inv.setStack(i, new ItemStack(i % 3 == 1 ? Items.RAW_IRON : Items.COBBLESTONE, 64));
					}
				}
				if (w.getBlockEntity(sorter) instanceof SorterBlockEntity s) {
					s.filter().setStack(0, new ItemStack(Items.RAW_IRON));
					s.filter().setStack(4, new ItemStack(Items.IRON_INGOT));
				}
			});
		}
		if (worldTick == 70) {
			player.setPosition(base.getX() - 0.5, base.getY() + 3.2, base.getZ() + 1.5);
			player.setYaw(180);
			player.setPitch(30);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		int k = worldTick - 200;
		if (k >= 0 && k < 60 && k % 2 == 0) {
			shot(client, String.format("belts_%02d", k / 2));
		}
		if (worldTick == 300) {
			player.setPosition(base.getX() + 4.5, base.getY() + 1.4, base.getZ() - 2.5);
			player.setYaw(135);
			player.setPitch(28);
		}
		if (worldTick == 330) {
			shot(client, "sorter_close");
		}
		if (worldTick == 900) {
			client.options.hudHidden = false;
			open(client, base.add(0, -1, -5));
		}
		if (worldTick == 920) {
			shot(client, "steel_chest_screen");
		}
		if (worldTick == 925) {
			client.player.closeHandledScreen();
			open(client, base.add(7, 1, -5));
		}
		if (worldTick == 945) {
			shot(client, "stainless_chest_screen");
		}
		if (worldTick == 950) {
			client.player.closeHandledScreen();
			open(client, base.add(0, 1, -5));
		}
		if (worldTick == 970) {
			shot(client, "sorter_screen");
		}
		if (worldTick == 971) {
			client.interactionManager.clickButton(client.player.currentScreenHandler.syncId, 0);
		}
		if (worldTick == 974 + 0) {
			shot(client, "sorter_screen_forbidden");
		}
		if (worldTick == 975) {
			client.player.closeHandledScreen();
			player.setPosition(base.getX() - 7.5, base.getY() + 1.4, base.getZ() - 2.5);
			open(client, base.add(-8, 2, -5));
		}
		if (worldTick == 995) {
			shot(client, "iron_chest_screen");
		}
		if (worldTick == 1000) {
			client.player.closeHandledScreen();
			player.setPosition(base.getX() + 0.5, base.getY() + 1.4, base.getZ() - 2.5);
			open(client, base.add(0, 0, -5));
		}
		if (worldTick == 1020) {
			shot(client, "steel_hopper_screen");
		}
		if (worldTick == 1030) {
			PeriodicElements.LOGGER.info("[logisticsshots] done");
			client.scheduleStop();
		}
	}

	private static void open(MinecraftClient client, BlockPos pos) {
		var server = client.getServer();
		UUID id = client.player.getUuid();
		server.execute(() -> {
			var player = server.getPlayerManager().getPlayer(id);
			if (player.getServerWorld().getBlockEntity(pos) instanceof NamedScreenHandlerFactory f) {
				player.openHandledScreen(f);
			}
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
