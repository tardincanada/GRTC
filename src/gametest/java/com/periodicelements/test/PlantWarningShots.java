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
 * Dev-only (PE_LANG=pt_br ./gradlew runPlantWarningShots, 3.54.0): a mahogany sapling on the plains and guarana in an
 * unheated greenhouse, looked at, to show the warning under the crosshair. Logs what the HUD reports.
 */
public class PlantWarningShots implements ClientModInitializer {
	private int tick = -1, worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (Boolean.getBoolean("pe.plantwarningshots")) ClientTickEvents.END_CLIENT_TICK.register(this::step);
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
				wc.setWorldName("plantwarning");
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
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 10, y, z - 10, x + 25, y + 10, z + 25));
			cmd(client, String.format("setblock %d %d %d periodic_elements:mahogany_sapling", x, y, z + 3));
			// An unheated greenhouse: glass all round, guarana (a jungle shrub that wants warmth) inside on grass.
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:glass hollow", x + 10, y - 1, z + 10, x + 16, y + 4, z + 16));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:grass_block", x + 11, y - 1, z + 11, x + 15, y - 1, z + 15));
			cmd(client, String.format("setblock %d %d %d periodic_elements:guarana_bush", x + 13, y, z + 14));
		}
		int x = base == null ? 0 : base.getX(), y = base == null ? 0 : base.getY(), z = base == null ? 0 : base.getZ();
		switch (worldTick) {
			case 70 -> {
				player.getAbilities().flying = false;
				look(player, x + 0.5, y, z + 0.5, 0, 26);
			}
			case 140 -> log(client, "sapling");
			case 110 -> cmd(client, "kill @e[type=!player]");
			case 145 -> client.inGameHud.getChatHud().clear(false);
			case 150 -> shot(client, "aviso_muda_fora_do_bioma");
			case 160 -> look(player, x + 13.5, y, z + 11.5, 0, 26);
			case 230 -> log(client, "greenhouse");
			case 200 -> cmd(client, "kill @e[type=!player]");
			case 235 -> client.inGameHud.getChatHud().clear(false);
			case 240 -> shot(client, "aviso_estufa_errada");
			case 250 -> {
				PeriodicElements.LOGGER.info("[plantwarning] done");
				client.scheduleStop();
			}
			default -> { }
		}
	}

	private static void log(MinecraftClient client, String what) {
		PeriodicElements.LOGGER.info("[plantwarning] {}: target {} -> {}", what, client.crosshairTarget instanceof
				net.minecraft.util.hit.BlockHitResult h ? client.world.getBlockState(h.getBlockPos()) : client.crosshairTarget,
				com.periodicelements.client.PlantWarningHud.current());
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
