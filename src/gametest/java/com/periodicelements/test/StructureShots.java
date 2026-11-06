package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.multiblock.Structures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runStructureShots, 3.3.0): a flat creative world with a 3x3x6 blast furnace stack and a 4-oven
 * coke battery, photographed whole, then with a block knocked out and the Field Guide in hand (red sparks).
 */
public class StructureShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.structureshots")) {
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
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 10 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("structures");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(WorldPresets.FLAT))
						.findFirst().ifPresent(wc::setWorldType);
				press(client, "Create New World");
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		var player = client.player;
		switch (worldTick) {
			case 60 -> {
				base = player.getBlockPos();
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "time set 1000");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "gamerule doWeatherCycle false");
				cmd(client, "gamerule doMobSpawning false");
				cmd(client, "difficulty peaceful");
				cmd(client, "kill @e[type=!player]");
				var server = client.getServer();
				BlockPos b = base;
				server.execute(() -> {
					var w = server.getOverworld();
					// Controllers face south, towards the camera; the floor sits on the ground.
					Structures.furnace(3, 6).build(w, b.add(-9, 1, -9), Direction.SOUTH);
					Structures.battery(4).build(w, b.add(1, 1, -9), Direction.SOUTH);
				});
			}
			case 100 -> {
				player.setPosition(base.getX() - 3.5, base.getY() + 6.5, base.getZ() + 10.5);
				player.setYaw(180);
				player.setPitch(16);
				player.getAbilities().flying = true;
			}
			case 150 -> {
				client.inGameHud.getChatHud().clear(false);
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
				client.options.hudHidden = true;
			}
			case 170 -> shot(client, "structures_world");
			case 175 -> {
				client.options.hudHidden = false;
				cmd(client, String.format("setblock %d %d %d minecraft:air", base.getX() - 10, base.getY() + 5, base.getZ() - 9));
				player.setPosition(base.getX() - 6.5, base.getY() + 6.5, base.getZ() - 1.5);
				player.setYaw(160);
				player.setPitch(20);
				cmd(client, "item replace entity @s weapon.mainhand with periodic_elements:field_guide");
			}
			case 230 -> shot(client, "structures_missing");
			// 3.8.0: a lead chamber with its Gay-Lussac tower and a 3x3 distillation column, then the outline.
			case 235 -> {
				client.options.hudHidden = true;
				var server = client.getServer();
				BlockPos b = base;
				server.execute(() -> {
					var w = server.getOverworld();
					Structures.chamber(5, 3, 3, true).build(w, b.add(20, 1, -9), Direction.SOUTH);
					Structures.column(3, 12).build(w, b.add(30, 1, -8), Direction.SOUTH);
					// A chamber half built: the outline shows the rest.
					Structures.Template t = Structures.chamber(3, 3, 3, false);
					BlockPos c = b.add(44, 1, -6);
					t.build(w, c, Direction.SOUTH);
					t.parts().forEach((rel, part) -> {
						if (rel.getY() >= 2 && part != Structures.Part.CONTROLLER) {
							w.setBlockState(t.toWorld(rel, c, Direction.SOUTH), net.minecraft.block.Blocks.AIR.getDefaultState());
						}
					});
				});
				player.setPosition(base.getX() + 27.5, base.getY() + 8.5, base.getZ() + 10.5);
				player.setYaw(180);
				player.setPitch(18);
			}
			case 300 -> shot(client, "structures_stage2");
			case 305 -> {
				BlockPos c = base.add(44, 1, -6);
				com.periodicelements.multiblock.Multiblock.outline.toggle(Structures.Kind.LEAD_CHAMBER, c, Direction.SOUTH);
				player.setPosition(base.getX() + 47.5, base.getY() + 5.5, base.getZ() + 2.5);
				player.setYaw(160);
				player.setPitch(25);
			}
			case 350 -> shot(client, "structures_outline");
			// 3.13.0: a 32-stage enrichment cascade and a 5x5x5 reactor with water against it.
			case 355 -> {
				var server = client.getServer();
				BlockPos b = base;
				server.execute(() -> {
					var w = server.getOverworld();
					Structures.cascade(2, 2, 8).build(w, b.add(60, 1, -4), Direction.SOUTH);
					Structures.reactor(5, 5, 5).build(w, b.add(70, 1, -4), Direction.SOUTH);
					for (int z = -10; z <= -4; z++) {
						w.setBlockState(b.add(71, 1, z), net.minecraft.block.Blocks.WATER.getDefaultState());
					}
				});
				player.setPosition(base.getX() + 66.5, base.getY() + 8.5, base.getZ() + 9.5);
				player.setYaw(180);
				player.setPitch(22);
			}
			case 420 -> shot(client, "structures_stage3");
			case 425 -> {
				BlockPos c = base.add(80, 1, 4);
				var server = client.getServer();
				server.execute(() -> server.getOverworld().setBlockState(c, Structures.Kind.NUCLEAR_REACTOR.controllerBlock()
						.getDefaultState().with(com.periodicelements.machine.PowerBlock.FACING, Direction.SOUTH)));
			}
			case 435 -> {
				com.periodicelements.multiblock.Multiblock.outline.toggle(Structures.Kind.NUCLEAR_REACTOR, base.add(80, 1, 4), Direction.SOUTH);
				player.setPosition(base.getX() + 82.5, base.getY() + 7.5, base.getZ() + 10.5);
				player.setYaw(165);
				player.setPitch(30);
			}
			case 480 -> shot(client, "structures_reactor_outline");
			case 490 -> {
				PeriodicElements.LOGGER.info("[structureshots] done");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void press(MinecraftClient client, String label) {
		client.currentScreen.children().stream().filter(c -> c instanceof ButtonWidget b && b.getMessage().getString().equals(label))
				.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
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
				text -> PeriodicElements.LOGGER.info("[structureshots] {}", text.getString()));
	}
}
