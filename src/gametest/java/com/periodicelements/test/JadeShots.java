package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.ProgressBook;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Dev-only (./gradlew -PwithEmi runJadeShots, 3.25.0): a flat world at tech level 2, then EMI's recipe screen for a
 * level-1 reaction, a level-2 reaction (still to research), a level-6 one (hidden), a machine's whole list, and two
 * compositions; screenshots to run/screenshots. Language from -Dpe.lang (default en_us). Needs EMI on the classpath.
 */
public class JadeShots implements ClientModInitializer {
	private static final String NS = PeriodicElements.MOD_ID;
	private int tick = -1;
	private int worldTick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.jadeshots")) {
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
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("jade");
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
		if (base == null) {
			base = player.getBlockPos();
		}
		BlockPos target = base.north(2);
		switch (worldTick) {
			case 40 -> {
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "time set 6000");
				var server = client.getServer();
				UUID id = player.getUuid();
				server.execute(() -> ProgressBook.setLevel(server, id, 4, true));
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
				cmd(client, String.format("fill %d %d %d %d %d %d air", base.getX() - 3, base.getY(), base.getZ() - 4,
						base.getX() + 3, base.getY() + 3, base.getZ() - 1));
				place(client, target, String.format("periodic_elements:lime_kiln[facing=south]{Owner:%s,Items:[{Slot:0b,id:\"minecraft:calcite\",count:32},{Slot:1b,id:\"minecraft:charcoal\",count:32}]}",
						uuidArray(id)));
			}
			case 60 -> cmd(client, "tick sprint 1600");
			case 200 -> {
				client.setScreen(null);
				player.setYaw(180);
				player.setPitch(30);
				player.setPosition(Vec3d.ofBottomCenter(base));
				player.getInventory().selectedSlot = 8;
			}
			case 240 -> shot(client, "jade_lime_kiln");
			case 250 -> place(client, target, "periodic_elements:voltaic_pile");
			case 290 -> shot(client, "jade_voltaic_pile");
			case 300 -> place(client, target, "periodic_elements:storage_tank");
			case 340 -> shot(client, "jade_storage_tank");
			case 350 -> place(client, target, "minecraft:granite");
			case 390 -> shot(client, "jade_granite_not_analysed");
			case 400 -> place(client, target, "minecraft:bricks");
			case 440 -> shot(client, "jade_bricks");
			case 450 -> place(client, target, "minecraft:glass");
			case 490 -> shot(client, "jade_glass");
			case 500 -> client.scheduleStop();
			default -> {
			}
		}
	}

	private BlockPos base;
	private boolean created;

	private static void place(MinecraftClient client, BlockPos pos, String block) {
		cmd(client, String.format("setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), block));
	}

	private static String uuidArray(UUID id) {
		long m = id.getMostSignificantBits(), l = id.getLeastSignificantBits();
		return "[I;" + (int) (m >> 32) + "," + (int) m + "," + (int) (l >> 32) + "," + (int) l + "]";
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[jadeshots] {}", text.getString()));
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
