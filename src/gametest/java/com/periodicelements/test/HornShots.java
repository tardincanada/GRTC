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
 * Dev-only (./gradlew runHornShots, 3.53.4): each animal with horns, antlers or tusks next to its baby, in a flat
 * world at noon, to check that babies show none.
 */
public class HornShots implements ClientModInitializer {
	private static final String[][] ROWS = {{"reindeer", "red_deer", "moose"}, {"yak", "wild_boar", "walrus"}};
	private int tick = -1, worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (Boolean.getBoolean("pe.hornshots")) ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		if (client.world == null) {
			if (client.getOverlay() != null || client.currentScreen == null) return;
			tick++;
			if (!created && tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("horns");
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
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 20, y, z - 20, x + 20, y + 10, z + 50));
			for (int r = 0; r < ROWS.length; r++) {
				int rz = z + r * 30;
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:snow_block", x - 12, y - 1, rz - 4, x + 12, y - 1, rz + 3));
				for (int i = 0; i < ROWS[r].length; i++) {
					double ax = x - 6.5 + i * 6.5;
					summon(client, ROWS[r][i], ax, y, rz + 0.5, 90, false);
					summon(client, ROWS[r][i], ax + 2.2, y, rz + 0.5, 90, true);
				}
			}
		}
		int x = base == null ? 0 : base.getX(), y = base == null ? 0 : base.getY(), z = base == null ? 0 : base.getZ();
		switch (worldTick) {
			case 70 -> {
				look(player, x - 3.0, y + 2.0, z + 11.0, 180, 8);
				player.getAbilities().flying = true;
				client.options.hudHidden = true;
			}
			case 160 -> shot(client, "chifres_cervos");
			case 165 -> look(player, x - 5.5, y + 1.6, z + 5.0, 180, 10);
			case 200 -> shot(client, "chifres_rena_perto");
			case 205 -> look(player, x - 3.0, y + 2.0, z + 41.0, 180, 8);
			case 250 -> shot(client, "chifres_iaque_javali_morsa");
			case 260 -> {
				PeriodicElements.LOGGER.info("[hornshots] done");
				client.scheduleStop();
			}
			default -> { }
		}
	}

	private static void look(net.minecraft.client.network.ClientPlayerEntity p, double x, double y, double z, float yaw, float pitch) {
		p.setPosition(x, y, z);
		p.setYaw(yaw);
		p.setPitch(pitch);
	}

	private static void summon(MinecraftClient client, String id, double x, double y, double z, float yaw, boolean baby) {
		cmd(client, String.format(java.util.Locale.ROOT, "summon periodic_elements:%s %.2f %.2f %.2f {NoAI:1b,PersistenceRequired:1b,Rotation:[%.1ff,0f]%s}",
				id, x, y, z, yaw, baby ? ",Age:-24000" : ""));
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
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(), text -> { });
	}
}
