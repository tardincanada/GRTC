package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.ScreenshotRecorder;

/** Dev-only (-Dpe.geooptionsshots=true with runGuideShots' setup): the world screen and the Geological "Customize" screen (3.49.0). */
public class GeoOptionsScreenshots implements ClientModInitializer {
	private int tick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.geooptionsshots")) return;
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		if (tick < 0 && (client.getOverlay() != null || client.currentScreen == null)) return;
		tick++;
		switch (tick) {
			case 20 -> CreateWorldScreen.create(client, client.currentScreen);
			case 80 -> client.currentScreen.children().stream().filter(c -> c instanceof net.minecraft.client.gui.widget.TabNavigationWidget)
					.findFirst().ifPresent(t -> ((net.minecraft.client.gui.widget.TabNavigationWidget) t).selectTab(1, false));
			case 100 -> {
				if (client.currentScreen instanceof CreateWorldScreen s) {
					var p = s.getWorldCreator().getLevelScreenProvider();
					PeriodicElements.LOGGER.info("[geooptions] world type {} provider {}", s.getWorldCreator().getWorldType().getName().getString(), p);
				}
				shot(client, "geo_world_tab");
			}
			case 110 -> press(client, "Customize|Personalizar");
			case 130 -> shot(client, "geo_options");
			case 140 -> client.scheduleStop();
			default -> {
			}
		}
	}

	private static void press(MinecraftClient client, String label) {
		var names = java.util.List.of(label.split("\\|"));
		client.currentScreen.children().stream().filter(c -> c instanceof ClickableWidget b && names.stream().anyMatch(n -> b.getMessage().getString().startsWith(n)))
				.findFirst().map(c -> (ClickableWidget) c).ifPresentOrElse(b -> b.onClick(b.getX() + 2, b.getY() + 2),
						() -> PeriodicElements.LOGGER.warn("[geooptions] no button {}; have {}", label,
								client.currentScreen.children().stream().filter(c -> c instanceof ClickableWidget)
										.map(c -> ((ClickableWidget) c).getMessage().getString()).toList()));
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(),
				text -> PeriodicElements.LOGGER.info("[geooptions] {}", text.getString()));
	}
}
