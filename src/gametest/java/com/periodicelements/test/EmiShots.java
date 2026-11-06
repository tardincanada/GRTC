package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.ProgressBook;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew -PwithEmi runEmiShots, 3.25.0): a flat world at tech level 2, then EMI's recipe screen for a
 * level-1 reaction, a level-2 reaction (still to research), a level-6 one (hidden), a machine's whole list, and two
 * compositions; screenshots to run/screenshots. Language from -Dpe.lang (default en_us). Needs EMI on the classpath.
 */
public class EmiShots implements ClientModInitializer {
	private static final String NS = PeriodicElements.MOD_ID;
	private int tick = -1;
	private int worldTick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.emishots")) {
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
			if (tick % 40 == 20 && !(client.currentScreen instanceof CreateWorldScreen)) {
				CreateWorldScreen.create(client, new TitleScreen());
			}
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("emi");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(WorldPresets.FLAT))
						.findFirst().ifPresent(wc::setWorldType);
				screen.children().stream().filter(c -> c instanceof ButtonWidget b && b.getMessage().getString().equals(
						net.minecraft.text.Text.translatable("selectWorld.create").getString()))
						.findFirst().map(c -> (ButtonWidget) c).ifPresent(ButtonWidget::onPress);
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		switch (worldTick) {
			case 40 -> {
				cmd(client, "gamerule sendCommandFeedback false");
				cmd(client, "gamerule doDaylightCycle false");
				cmd(client, "time set 6000");
				var server = client.getServer();
				UUID id = client.player.getUuid();
				server.execute(() -> ProgressBook.setLevel(server, id, 1, true));
				client.getToastManager().clear();
				client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
			}
			case 200 -> {
				org.lwjgl.glfw.GLFW.glfwSetCursorPos(client.getWindow().getHandle(), 2, 2);
				client.setScreen(new InventoryScreen(client.player));
			}
			case 220 -> shot(client, "emi_inventory");
			case 230 -> Emi.recipe("/lime_kiln/limestone_calcination");
			case 260 -> shot(client, "emi_level1_known");
			case 270 -> Emi.recipe("/alembic/oil_of_vitriol");
			case 300 -> shot(client, "emi_level2_to_research");
			case 310 -> Emi.recipe("/reduction_crucible/europium_reduction");
			case 340 -> shot(client, "emi_level6_hidden");
			case 350 -> Emi.workstation("lime_kiln");
			case 380 -> shot(client, "emi_lime_kiln_list");
			case 390 -> Emi.recipe("/composition/minecraft/oak_log");
			case 420 -> shot(client, "emi_composition_oak_log");
			case 430 -> Emi.recipe("/composition/minecraft/granite");
			case 460 -> shot(client, "emi_composition_granite");
			case 470 -> Emi.uses(net.minecraft.item.Items.OAK_LOG);
			case 500 -> shot(client, "emi_uses_oak_log");
			case 510 -> client.scheduleStop();
			default -> {
			}
		}
	}

	/** Every EMI call, kept out of the outer class so it loads without EMI on the classpath. */
	private static final class Emi {
		static void recipe(String path) {
			EmiRecipe r = EmiApi.getRecipeManager().getRecipe(Identifier.of(NS, path));
			if (r == null) {
				PeriodicElements.LOGGER.warn("[emishots] no recipe {}", path);
				return;
			}
			EmiApi.displayRecipe(r);
		}

		static void workstation(String machine) {
			EmiApi.getRecipeManager().getCategories().stream().filter(c -> c.getId().equals(Identifier.of(NS, machine)))
					.findFirst().ifPresent(EmiApi::displayRecipeCategory);
		}

		static void uses(net.minecraft.item.Item item) {
			EmiApi.displayUses(EmiStack.of(item));
		}
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[emishots] {}", text.getString()));
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
