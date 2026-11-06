package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlock;
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
 * Dev-only (./gradlew runAnimationShots, 3.28.0): a flat world with two charcoal pits, one empty and one charring logs.
 * Once the loaded pit turns "active" it takes a run of screenshots a few ticks apart, to see the animated texture.
 */
public class AnimationShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private int activeAt = -1;
	private boolean created;
	private BlockPos base;
	/** -Dpe.animmachine picks the machine (3.28.1: pottery_kiln); charcoal_pit by default. */
	private final String machine = System.getProperty("pe.animmachine", "charcoal_pit");

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.animationshots")) {
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
				wc.setWorldName("animation");
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
		if (machine.equals("all")) {
			showcase(client, player);
			return;
		}
		if (machine.equals("power")) {
			powerShots(client, player);
			return;
		}
		if (worldTick == 60) {
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY(), z = base.getZ();
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set 13000");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 6, y, z - 8, x + 6, y + 5, z + 2));
			// Left: an empty machine (stopped). Right: the same machine loaded and working (active).
			boolean pit = machine.equals("charcoal_pit");
			boolean cascade = machine.equals("enrichment_cascade");
			String input = pit ? "minecraft:oak_log" : cascade ? "periodic_elements:uranium_hexafluoride" : "minecraft:clay_ball";
			String fuel = pit ? "minecraft:oak_log" : "minecraft:charcoal";
			if (cascade) {
				// Electric: a steam engine and dynamo behind the right-hand cascade, wired to it.
				cmd(client, String.format("setblock %d %d %d periodic_elements:copper_wire", x + 1, y, z - 4));
				cmd(client, String.format("setblock %d %d %d periodic_elements:copper_wire", x + 1, y, z - 5));
				cmd(client, String.format("setblock %d %d %d periodic_elements:dynamo", x + 1, y, z - 6));
				cmd(client, String.format("setblock %d %d %d periodic_elements:steam_engine", x + 1, y, z - 7));
				cmd(client, String.format("item replace block %d %d %d container.0 with minecraft:coal 64", x + 1, y, z - 7));
				cmd(client, String.format("item replace block %d %d %d container.1 with minecraft:coal 64", x + 1, y, z - 7));
				for (int i = 2; i < 9; i++) {
					cmd(client, String.format("item replace block %d %d %d container.%d with minecraft:water_bucket", x + 1, y, z - 7, i));
				}
				cmd(client, "research grant @s dynamo");
				cmd(client, "research grant @s isotope_separation");
			}
			cmd(client, String.format("setblock %d %d %d periodic_elements:%s[facing=south]", x - 1, y, z - 3, machine));
			cmd(client, String.format("setblock %d %d %d periodic_elements:%s[facing=south]", x + 1, y, z - 3, machine));
			cmd(client, String.format("item replace block %d %d %d container.0 with %s 64", x + 1, y, z - 3, input));
			if (!cascade) {
				cmd(client, String.format("item replace block %d %d %d container.1 with %s 64", x + 1, y, z - 3, fuel));
			}
			cmd(client, "research grant @s fired_pottery");
			cmd(client, "research grant @s bellows");
			var server = client.getServer();
			UUID id = player.getUuid();
			BlockPos at = new BlockPos(x + 1, y, z - 3);
			server.execute(() -> {
				if (server.getOverworld().getBlockEntity(at) instanceof com.periodicelements.machine.MachineBlockEntity be) {
					be.setOwner(id);
				}
			});
		}
		if (worldTick == 100) {
			player.setPosition(base.getX() + 0.5, base.getY() + 0.6, base.getZ() + 0.2);
			player.setYaw(180);
			player.setPitch(22);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		if (worldTick > 100 && activeAt < 0) {
			var state = client.world.getBlockState(base.add(1, 0, -3));
			if (state.contains(MachineBlock.ACTIVE) && state.get(MachineBlock.ACTIVE)) {
				activeAt = worldTick;
				PeriodicElements.LOGGER.info("[animationshots] machine active after {} ticks", worldTick - 60);
			} else if (worldTick > 6000) {
				PeriodicElements.LOGGER.error("[animationshots] machine never turned active");
				client.scheduleStop();
			}
		}
		if (activeAt > 0) {
			int k = worldTick - activeAt - 40;
			if (k >= 0 && k < 48 && k % 2 == 0) {
				shot(client, String.format("%s_%02d", machine, k / 2));
			}
			if (k == 60) {
				PeriodicElements.LOGGER.info("[animationshots] done");
				client.scheduleStop();
			}
		}
	}

	/**
	 * PE_ANIM=all (3.33.0): one wall per tech level, a row per machine and a column per look, every block set to
	 * active=true with its mode. The world's ticks are frozen so the machines don't switch themselves back off.
	 */
	private void showcase(MinecraftClient client, net.minecraft.client.network.ClientPlayerEntity player) {
		var machines = com.periodicelements.machine.Reactions.machines();
		int levels = 8;
		if (worldTick == 60) {
			base = player.getBlockPos();
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set noon");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "tick freeze");
			int[] rows = new int[levels];
			for (var spec : machines) {
				int level = Math.max(1, spec.level());
				int row = rows[level]++;
				int looks = com.periodicelements.machine.MachineAnimations.looks(spec.id());
				int x0 = base.getX() + (level - 1) * 30, y = base.getY() + row, z = base.getZ() - 8;
				cmd(client, String.format("setblock %d %d %d periodic_elements:%s[facing=south]", x0 - 2, y, z, spec.id()));
				for (int m = 0; m < looks; m++) {
					cmd(client, String.format("setblock %d %d %d periodic_elements:%s[facing=south,active=true,mode=%d]",
							x0 + m, y, z, spec.id(), m));
				}
			}
		}
		int perLevel = 60;
		int k = worldTick - 120;
		if (k >= 0 && k < levels * perLevel) {
			int level = k / perLevel + 1;
			if (level >= levels) {
				return;
			}
			int at = k % perLevel;
			if (at == 0) {
				int rows = 0;
				for (var spec : machines) {
					if (Math.max(1, spec.level()) == level) {
						rows++;
					}
				}
				double cx = base.getX() + (level - 1) * 30 + 5.5;
				player.setPosition(cx, base.getY() + rows / 2.0, base.getZ() - 8 + 6 + rows * 0.55);
				player.setYaw(180);
				player.setPitch(5);
				player.getAbilities().flying = true;
				client.options.hudHidden = true;
			}
			if (at == 40 || at == 44) {
				shot(client, String.format("level%d_%d", level, at));
			}
		}
		if (k == levels * perLevel) {
			PeriodicElements.LOGGER.info("[animationshots] done");
			client.scheduleStop();
		}
	}

	/**
	 * PE_ANIM=power (3.41.0): a steam engine (grey smoke) and a diesel generator (black smoke) running, the diesel
	 * charging a battery; then, with ticks frozen, five batteries showing each colour of the charge bar.
	 */
	private void powerShots(MinecraftClient client, net.minecraft.client.network.ClientPlayerEntity player) {
		if (worldTick == 60) {
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY(), z = base.getZ() - 4;
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set noon");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, String.format("setblock %d %d %d periodic_elements:steam_engine[facing=south]", x - 2, y, z));
			cmd(client, String.format("item replace block %d %d %d container.0 with minecraft:coal 64", x - 2, y, z));
			for (int i = 2; i < 9; i++) {
				cmd(client, String.format("item replace block %d %d %d container.%d with minecraft:water_bucket", x - 2, y, z, i));
			}
			// The engine only runs while a dynamo next to it wants current; a battery behind drains the dynamo.
			cmd(client, String.format("setblock %d %d %d periodic_elements:dynamo", x - 2, y, z - 1));
			cmd(client, String.format("setblock %d %d %d periodic_elements:lithium_ion_battery", x - 2, y, z - 2));
			cmd(client, String.format("setblock %d %d %d periodic_elements:diesel_generator[facing=south]", x + 1, y, z));
			cmd(client, String.format("item replace block %d %d %d container.0 with periodic_elements:diesel 64", x + 1, y, z));
			cmd(client, String.format("setblock %d %d %d periodic_elements:lithium_ion_battery[facing=south]", x + 2, y, z));
		}
		if (worldTick == 100) {
			player.setPosition(base.getX() + 0.5, base.getY() + 1.2, base.getZ() + 1.5);
			player.setYaw(180);
			player.setPitch(8);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		if (worldTick == 300) {
			shot(client, "power_smoke");
		}
		if (worldTick == 310) {
			cmd(client, "tick freeze");
			cmd(client, "kill @e[type=!player]");
			int x = base.getX(), y = base.getY() + 3, z = base.getZ() - 4;
			int[][] states = {{0, 0}, {2, 1}, {5, 2}, {9, 3}, {12, 4}};
			for (int i = 0; i < states.length; i++) {
				cmd(client, String.format("setblock %d %d %d periodic_elements:lithium_ion_battery[facing=south,level=%d,band=%d]",
						x - 2 + i, y, z, states[i][0], states[i][1]));
			}
		}
		if (worldTick == 330) {
			player.setPosition(base.getX() + 0.5, base.getY() + 3.6, base.getZ() + 0.5);
			player.setPitch(15);
		}
		if (worldTick == 360) {
			shot(client, "battery_bands");
		}
		if (worldTick == 370) {
			PeriodicElements.LOGGER.info("[animationshots] done");
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
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(),
				text -> { });
	}
}
