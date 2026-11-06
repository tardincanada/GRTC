package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.FieldGuideScreen;
import com.periodicelements.machine.MachineBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Dev-only (./gradlew runValveShots, 3.29.0): an Electrolysis Cell screen with the relief valve open and shut (mouse on
 * the valve, so its tooltip shows), then the Field Guide's paragraph on it. No world needed. Language from -Dpe.lang.
 */
public class ValveShots implements ClientModInitializer {
	private int tick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.valveshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		if (client.getOverlay() != null || (tick < 0 && client.currentScreen == null)) {
			return;
		}
		tick++;
		String lang = System.getProperty("pe.lang", "en_us");
		switch (tick) {
			case 5 -> {
				if (!client.options.language.equals(lang)) {
					client.options.language = lang;
					client.getLanguageManager().setLanguage(lang);
					client.reloadResources();
				}
			}
			case 40 -> client.setScreen(cell(true, false));
			case 50 -> shot(client, "valve_open");
			case 55 -> client.setScreen(cell(false, false));
			case 65 -> shot(client, "valve_shut");
			case 70 -> client.setScreen(cell(true, true));
			case 80 -> shot(client, "valve_open_tooltip");
			case 85 -> client.setScreen(cell(false, true));
			case 105 -> shot(client, "valve_shut_tooltip");
			case 110 -> {
				FieldGuideScreen.revealOverride = 7;
				client.setScreen(new FieldGuideScreen());
			}
			case 115 -> ((FieldGuideScreen) client.currentScreen).typeSearch(lang.equals("pt_br") ? "valvula de alivio" : "relief valve");
			case 130 -> shot(client, "valve_guide_results");
			case 135 -> ((FieldGuideScreen) client.currentScreen).openFirstResult();
			case 155 -> shot(client, "valve_guide");
			case 165 -> client.scheduleStop();
			default -> {
			}
		}
	}

	private static Item item(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	/** An Electrolysis Cell splitting andesite's lime, out of flasks, with the mouse on the valve. */
	private static Screen cell(boolean open, boolean hover) {
		var inv = new net.minecraft.inventory.SimpleInventory(MachineBlockEntity.SIZE);
		var props = new net.minecraft.screen.ArrayPropertyDelegate(MachineBlockEntity.P_COUNT);
		props.set(MachineBlockEntity.P_MACHINE, com.periodicelements.machine.Reactions.machine("electrolysis_cell").index());
		props.set(MachineBlockEntity.P_TEMP, 60);
		props.set(MachineBlockEntity.P_PROGRESS, 450);
		props.set(MachineBlockEntity.P_VENT, open ? 1 : 0);
		props.set(MachineBlockEntity.P_STATUS, (open ? MachineBlockEntity.Status.WORKING : MachineBlockEntity.Status.NEEDS_FLASKS).ordinal());
		var all = com.periodicelements.machine.Reactions.all();
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).id().equals("lime_electrolysis")) {
				props.set(MachineBlockEntity.P_ACTIVE, i);
			}
		}
		inv.setStack(0, new ItemStack(net.minecraft.item.Items.ANDESITE, 40));
		inv.setStack(2, new ItemStack(item("calcium"), 7));
		inv.setStack(MachineBlockEntity.RESIDUE, new ItemStack(item("slag"), 12));
		var playerInv = new net.minecraft.entity.player.PlayerInventory(null);
		var handler = new com.periodicelements.machine.MachineScreenHandler(0, playerInv, inv, props);
		var inner = new com.periodicelements.client.MachineScreen(handler, playerInv,
				net.minecraft.text.Text.translatable("block.periodic_elements.electrolysis_cell"));
		return new Screen(net.minecraft.text.Text.empty()) {
			@Override
			protected void init() {
				inner.init(client, width, height);
			}

			@Override
			public void render(net.minecraft.client.gui.DrawContext ctx, int mouseX, int mouseY, float delta) {
				int x = (width - 176) / 2, y = (height - (com.periodicelements.machine.MachineScreenHandler.PLAYER_INV_Y + 82)) / 2;
				inner.render(ctx, hover ? x + com.periodicelements.machine.MachineScreenHandler.SIDE_X + 8 : 0, hover ? y + 111 : 0, delta);
			}
		};
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> PeriodicElements.LOGGER.info("[valveshots] {}", text.getString()));
	}
}
