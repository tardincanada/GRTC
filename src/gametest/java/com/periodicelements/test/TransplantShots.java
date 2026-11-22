package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
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
 * Dev-only (PE_LANG=pt_br ./gradlew runTransplantShots, 3.54.0): a mahogany sapling on the plains and guarana in an
 * unheated greenhouse, looked at, to show the warning under the crosshair. Logs what the HUD reports.
 */
public class TransplantShots implements ClientModInitializer {
	private int tick = -1, worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (Boolean.getBoolean("pe.transplantshots")) ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		String lang = System.getProperty("pe.lang", "en_us");
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) return;
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
				wc.setWorldName("transplant");
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
		if (client.player == null) return;
		worldTick++;
		var player = client.player;
		if (worldTick == 10) client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
		if (worldTick == 60) {
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY(), z = base.getZ();
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set 6000");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			cmd(client, "clear @s");
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 10, y, z - 10, x + 15, y + 10, z + 15));
			cmd(client, String.format("setblock %d %d %d periodic_elements:damask_rose_bush[age=1]", x, y, z + 3));
			cmd(client, String.format("setblock %d %d %d periodic_elements:damask_rose_bush[age=3]", x + 3, y, z + 3));
			cmd(client, "item replace entity @s hotbar.0 with minecraft:shears");
			cmd(client, "item replace entity @s hotbar.1 with minecraft:iron_shovel");
		}
		int x = base == null ? 0 : base.getX(), y = base == null ? 0 : base.getY(), z = base == null ? 0 : base.getZ();
		switch (worldTick) {
			case 70 -> {
				player.getAbilities().flying = false;
				player.getInventory().selectedSlot = 0;
				look(player, x + 0.5, y, z + 0.5, 0, 26);
			}
			case 100 -> {
				if (client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult h) {
					client.interactionManager.interactBlock(player, net.minecraft.util.Hand.MAIN_HAND, h);
				}
			}
			case 102 -> client.inGameHud.getChatHud().clear(false);
			case 108 -> shot(client, "tesoura_jovem_demais");
			case 120 -> {
				player.getInventory().selectedSlot = 1;
				look(player, x + 3.5, y, z + 0.5, 0, 26);
			}
			case 150 -> serverSide(client, sp -> {
				BlockPos p = new BlockPos(x + 3, y, z + 3);
				var state = sp.getServerWorld().getBlockState(p);
				PeriodicElements.LOGGER.info("[transplant] before {}", state);
				com.periodicelements.shrubs.ShrubBlock.uproot(state, sp.getServerWorld(), p, sp, net.minecraft.util.Hand.MAIN_HAND);
			});
			case 165 -> {
				for (int i = 0; i < 9; i++) {
					if (player.getInventory().getStack(i).contains(net.minecraft.component.DataComponentTypes.BLOCK_STATE)) {
						player.getInventory().selectedSlot = i;
						PeriodicElements.LOGGER.info("[transplant] root ball {} {}", player.getInventory().getStack(i).getName().getString(),
								player.getInventory().getStack(i).get(net.minecraft.component.DataComponentTypes.BLOCK_STATE));
					}
				}
			}
			case 185 -> shot(client, "torrao_na_mao");
			case 200 -> serverSide(client, sp -> {
				BlockPos floor = new BlockPos(x + 3, y - 1, z + 6);
				net.minecraft.item.ItemStack ball = sp.getMainHandStack().copy();
				sp.interactionManager.interactBlock(sp, sp.getServerWorld(), ball, net.minecraft.util.Hand.MAIN_HAND,
						new net.minecraft.util.hit.BlockHitResult(net.minecraft.util.math.Vec3d.ofCenter(floor).add(0, 0.5, 0),
								net.minecraft.util.math.Direction.UP, floor, false));
				PeriodicElements.LOGGER.info("[transplant] replanted {}", sp.getServerWorld().getBlockState(floor.up()));
			});
			case 215 -> {
				player.getInventory().selectedSlot = 1;
				look(player, x + 3.5, y, z + 3.5, 0, 26);
			}
			case 250 -> shot(client, "replantada_madura");
			case 260 -> {
				PeriodicElements.LOGGER.info("[transplant] done");
				client.scheduleStop();
			}
			default -> { }
		}
	}

	private static void serverSide(MinecraftClient client, java.util.function.Consumer<net.minecraft.server.network.ServerPlayerEntity> action) {
		var server = client.getServer();
		UUID id = client.player.getUuid();
		server.execute(() -> action.accept(server.getPlayerManager().getPlayer(id)));
	}

	private static void look(net.minecraft.client.network.ClientPlayerEntity p, double x, double y, double z, float yaw, float pitch) {
		p.setPosition(x, y, z);
		p.setYaw(yaw);
		p.setPitch(pitch);
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
		client.inGameHud.getChatHud().clear(false);
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(), text -> { });
	}
}
