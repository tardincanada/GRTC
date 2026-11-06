package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * Dev-only (./gradlew runScreenshots, needs -Dpe.screenshots=true): once a world is open, sets up a lime kiln
 * and a research station next to the player, fast-forwards time, opens each screen and saves a screenshot
 * to run/screenshots, then quits.
 */
public class ScreenshotTour implements ClientModInitializer {
	private int tick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.screenshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private static String uuidArray(UUID id) {
		long m = id.getMostSignificantBits(), l = id.getLeastSignificantBits();
		return "[I;" + (int) (m >> 32) + "," + (int) m + "," + (int) (l >> 32) + "," + (int) l + "]";
	}

	private void step(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}
		tick++;
		var player = client.player;
		BlockPos base = player.getBlockPos();
		BlockPos kiln = base.north(2);
		BlockPos station = base.north(2).west(2);
		switch (tick) {
			case 40 -> {
				cmd(client, "gamemode creative");
				cmd(client, "time set noon");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "research grant @s level:1");
				cmd(client, "research grant @s writing");
				cmd(client, "research grant @s fired_pottery");
				String owner = uuidArray(player.getUuid());
				cmd(client, String.format("fill %d %d %d %d %d %d air", base.getX() - 4, base.getY(), base.getZ() - 2,
						base.getX() + 4, base.getY(), base.getZ() - 2));
				cmd(client, String.format("setblock %d %d %d periodic_elements:lime_kiln[facing=south]{Owner:%s,Items:[{Slot:0b,id:\"minecraft:calcite\",count:6},{Slot:1b,id:\"minecraft:charcoal\",count:24}]}",
						kiln.getX(), kiln.getY(), kiln.getZ(), owner));
				cmd(client, String.format("setblock %d %d %d periodic_elements:potters_workshop[facing=south]{Items:[{Slot:0b,id:\"minecraft:clay_ball\",count:8},{Slot:1b,id:\"minecraft:charcoal\",count:4},{Slot:3b,id:\"minecraft:leather\",count:5},{Slot:4b,id:\"minecraft:paper\",count:6},{Slot:5b,id:\"periodic_elements:carbon_ink\",count:3},{Slot:6b,id:\"minecraft:oak_planks\",count:8}]}",
						station.getX(), station.getY(), station.getZ()));
				cmd(client, String.format("setblock %d %d %d periodic_elements:bloomery[facing=south]", base.north(2).east(2).getX(), base.getY(), base.getZ() - 2));
				cmd(client, String.format("setblock %d %d %d periodic_elements:smelting_furnace[facing=south]", base.north(2).east(3).getX(), base.getY(), base.getZ() - 2));
				cmd(client, String.format("setblock %d %d %d periodic_elements:charcoal_pit[facing=south]", base.north(2).west(3).getX(), base.getY(), base.getZ() - 2));
				// The new ores, in a wall south of the player.
				String[] ores = {"cassiterite_ore", "deepslate_cassiterite_ore", "galena_ore", "deepslate_galena_ore",
						"sphalerite_ore", "deepslate_sphalerite_ore", "nether_sulfur_ore", "deepslate_sulfur_ore", "saltpetre_crust"};
				cmd(client, String.format("fill %d %d %d %d %d %d air", base.getX() - 5, base.getY(), base.getZ() + 1,
						base.getX() + 5, base.getY() + 2, base.getZ() + 3));
				for (int i = 0; i < ores.length; i++) {
					cmd(client, String.format("setblock %d %d %d periodic_elements:%s", base.getX() + 4 - i, base.getY(),
							base.getZ() + 3, ores[i]));
				}
			}
			case 60 -> cmd(client, "tick sprint 2400");
			case 200 -> {
				player.setYaw(180);
				player.setPitch(20);
				player.setPosition(Vec3d.ofBottomCenter(base));
			}
			case 220 -> shot(client, "world");
			case 230 -> use(client, kiln);
			case 270 -> shot(client, "lime_kiln");
			case 280 -> player.closeHandledScreen();
			case 290 -> use(client, station);
			case 310 -> {
				var screen = client.currentScreen;
				if (screen != null) {
					int sx = (screen.width - 256) / 2, sy = (screen.height - 222) / 2;
					screen.mouseClicked(sx + 66 + 20, sy + 18 + 4 + 12 * 5, 0); // Bellows
					cursor(client, sx + 66 + 20, sy + 18 + 4 + 12);
				}
			}
			case 330 -> shot(client, "research_station");
			case 335 -> player.closeHandledScreen();
			case 340 -> {
				player.setYaw(0);
				player.setPitch(15);
				player.setPosition(Vec3d.ofBottomCenter(base.north(1)));
			}
			case 360 -> shot(client, "ores");
			case 380 -> {
				PeriodicElements.LOGGER.info("[screenshots] done");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void cmd_unused() {
	}

	private static void cursor(MinecraftClient client, double guiX, double guiY) {
		double scale = client.getWindow().getScaleFactor();
		org.lwjgl.glfw.GLFW.glfwSetCursorPos(client.getWindow().getHandle(), guiX * scale, guiY * scale);
	}

	/** Runs a command as the player with operator rights on the integrated server (cheats may be off). */
	private static void cmd(MinecraftClient client, String command) {
		var server = client.getServer();
		UUID id = client.player.getUuid();
		server.execute(() -> {
			var player = server.getPlayerManager().getPlayer(id);
			server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(4), command);
		});
	}

	private static void use(MinecraftClient client, BlockPos pos) {
		client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(pos), Direction.SOUTH, pos, false));
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(),
				text -> PeriodicElements.LOGGER.info("[screenshots] {}", text.getString()));
	}
}
