package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.client.FieldGuideScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import com.periodicelements.client.GuideBookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;

/** Dev-only (./gradlew runGuideShots): opens the Field Guide from the title screen and screenshots it. */
public class GuideScreenshots implements ClientModInitializer {
	private int tick = -1;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.guideshots")) {
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(this::step);
	}

	private void step(MinecraftClient client) {
		if (tick < 0 && (client.getOverlay() != null || client.currentScreen == null)) {
			return; // still loading resources
		}
		tick++;
		switch (tick) {
			// A new player (tech level 0): only Getting Started, level 1 and what belongs to it.
			case 20 -> {
				FieldGuideScreen.revealOverride = null;
				client.setScreen(new FieldGuideScreen());
			}
			case 40 -> shot(client, "guide_contents_start");
			case 45 -> press(client, "Getting Started|Primeiros Passos|Первые шаги");
			case 60 -> shot(client, "guide_start_p1");
			// Everything open (as at level 7), for the remaining shots.
			case 70 -> {
				FieldGuideScreen.revealOverride = 7;
				client.setScreen(new FieldGuideScreen());
			}
			case 85 -> shot(client, "guide_contents");
			case 90 -> press(client, "Level 4|Nível 4|Уровень 4");
			case 100 -> press(client, "Research|Pesquisa|Исследования");
			case 115 -> shot(client, "guide_level4_p1");
			case 120 -> ((GuideBookScreen) client.currentScreen).setSpread(1);
			case 135 -> shot(client, "guide_level4_p2");
			case 140 -> client.setScreen(new FieldGuideScreen());
			case 150 -> press(client, "Mining Tools|Mineração|Горное дело");
			case 165 -> shot(client, "guide_mining");
			case 170 -> client.setScreen(new FieldGuideScreen());
			case 180 -> press(client, "Life Sciences|Ciências da Vida|Науки о жизни");
			case 195 -> shot(client, "guide_life");
			case 200 -> client.setScreen(machineScreen());
			case 215 -> shot(client, "machine_side_panel");
			// 3.3.0: recipe grids, and the Structures chapter with its layer and 3D views.
			case 220 -> client.setScreen(new FieldGuideScreen());
			case 230 -> press(client, "Level 2|Nível 2|Уровень 2");
			case 240 -> press(client, "Machines|Máquinas|Машины");
			case 255 -> shot(client, "guide_level2_machines");
			case 260 -> client.setScreen(new FieldGuideScreen());
			case 270 -> press(client, "Structures|Estruturas|Конструкции");
			case 285 -> shot(client, "guide_structures_p1");
			case 290 -> ((GuideBookScreen) client.currentScreen).setSpread(1);
			case 305 -> shot(client, "guide_structures_p2");
			case 310 -> ((GuideBookScreen) client.currentScreen).setSpread(2);
			case 325 -> shot(client, "guide_structures_p3");
			case 330 -> ((GuideBookScreen) client.currentScreen).setSpread(3);
			case 345 -> shot(client, "guide_structures_p4");
			case 350 -> client.setScreen(furnaceScreen());
			case 365 -> shot(client, "machine_structure");
			// 3.8.0: the lead chamber and distillation column pages.
			case 370 -> client.setScreen(new FieldGuideScreen());
			case 380 -> press(client, "Structures|Estruturas|Конструкции");
			case 390 -> ((GuideBookScreen) client.currentScreen).setSpread(4);
			case 405 -> shot(client, "guide_structures_p5");
			case 410 -> ((GuideBookScreen) client.currentScreen).setSpread(5);
			case 425 -> shot(client, "guide_structures_p6");
			case 430 -> ((GuideBookScreen) client.currentScreen).setSpread(6);
			case 445 -> shot(client, "guide_structures_p7");
			case 450 -> ((GuideBookScreen) client.currentScreen).setSpread(7);
			case 465 -> shot(client, "guide_structures_p8");
			// 3.13.0: the enrichment cascade and reactor pages at the end of the chapter.
			case 470 -> ((GuideBookScreen) client.currentScreen).setSpread(11);
			case 485 -> shot(client, "guide_structures_p12");
			case 490 -> ((GuideBookScreen) client.currentScreen).setSpread(12);
			case 505 -> shot(client, "guide_structures_p13");
			case 510 -> ((GuideBookScreen) client.currentScreen).setSpread(13);
			case 525 -> shot(client, "guide_structures_p14");
			case 530 -> ((GuideBookScreen) client.currentScreen).setSpread(14);
			case 545 -> shot(client, "guide_structures_p15");
			case 550 -> ((GuideBookScreen) client.currentScreen).setSpread(15);
			case 565 -> shot(client, "guide_structures_p16");
			case 575 -> {
				PeriodicElements.LOGGER.info("[guideshots] done");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	/** A Pneumatic Trough screen with flasks, cylinders and a Compressor fitted (no world needed). */
	private static net.minecraft.client.gui.screen.Screen machineScreen() {
		var inv = new net.minecraft.inventory.SimpleInventory(com.periodicelements.machine.MachineBlockEntity.SIZE);
		var props = new net.minecraft.screen.ArrayPropertyDelegate(com.periodicelements.machine.MachineBlockEntity.P_COUNT);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_MACHINE,
				com.periodicelements.machine.Reactions.machine("pneumatic_trough").index());
		props.set(com.periodicelements.machine.MachineBlockEntity.P_TEMP, 420);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_STATUS,
				com.periodicelements.machine.MachineBlockEntity.Status.NEEDS_FLASKS.ordinal());
		inv.setStack(0, new net.minecraft.item.ItemStack(item("potassium_nitrate"), 18));
		inv.setStack(1, new net.minecraft.item.ItemStack(net.minecraft.item.Items.CHARCOAL, 20));
		inv.setStack(2, new net.minecraft.item.ItemStack(item("oxygen"), 3));
		inv.setStack(3, new net.minecraft.item.ItemStack(item("oxygen_cylinder"), 1));
		inv.setStack(4, new net.minecraft.item.ItemStack(item("potassium_oxide"), 4));
		inv.setStack(com.periodicelements.machine.MachineBlockEntity.CYLINDERS, new net.minecraft.item.ItemStack(item("steel_cylinder"), 3));
		inv.setStack(com.periodicelements.machine.MachineBlockEntity.UPGRADE_FIRST, new net.minecraft.item.ItemStack(item("compressor")));
		inv.setStack(com.periodicelements.machine.MachineBlockEntity.UPGRADE_LAST, new net.minecraft.item.ItemStack(item("refractory_lining")));
		props.set(com.periodicelements.machine.MachineBlockEntity.P_LIMIT, 1050);
		var playerInv = new net.minecraft.entity.player.PlayerInventory(null);
		var handler = new com.periodicelements.machine.MachineScreenHandler(0, playerInv, inv, props);
		var inner = new com.periodicelements.client.MachineScreen(handler, playerInv,
				net.minecraft.text.Text.translatable("block.periodic_elements.pneumatic_trough"));
		// A handled screen closes itself without a player, so draw it from a plain screen that never ticks it.
		return new net.minecraft.client.gui.screen.Screen(net.minecraft.text.Text.empty()) {
			@Override
			protected void init() {
				inner.init(client, width, height);
			}

			@Override
			public void render(net.minecraft.client.gui.DrawContext ctx, int mouseX, int mouseY, float delta) {
				inner.render(ctx, mouseX, mouseY, delta);
			}
		};
	}

	/** 3.3.0: a Charcoal Blast Furnace controlling a 3x3x10 stack, smelting. */
	private static net.minecraft.client.gui.screen.Screen furnaceScreen() {
		var inv = new net.minecraft.inventory.SimpleInventory(com.periodicelements.machine.MachineBlockEntity.SIZE);
		var props = new net.minecraft.screen.ArrayPropertyDelegate(com.periodicelements.machine.MachineBlockEntity.P_COUNT);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_MACHINE,
				com.periodicelements.machine.Reactions.machine("charcoal_blast_furnace").index());
		props.set(com.periodicelements.machine.MachineBlockEntity.P_TEMP, 1386);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_LIMIT, 1550);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_STATUS, com.periodicelements.machine.MachineBlockEntity.Status.WORKING.ordinal());
		props.set(com.periodicelements.machine.MachineBlockEntity.P_ACTIVE, -1);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_STATUS_REACTION, -1);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_CARBON, 84);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_S_KIND, 1);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_S_A, 3);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_S_H, 10);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_S_SPEED, 1800);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_S_LOSS, 55);
		props.set(com.periodicelements.machine.MachineBlockEntity.P_PROGRESS, 400);
		inv.setStack(0, new net.minecraft.item.ItemStack(net.minecraft.item.Items.RAW_IRON, 64));
		inv.setStack(1, new net.minecraft.item.ItemStack(net.minecraft.item.Items.CHARCOAL, 40));
		inv.setStack(2, new net.minecraft.item.ItemStack(item("cast_iron_ingot"), 23));
		inv.setStack(5, new net.minecraft.item.ItemStack(item("slag"), 9));
		var playerInv = new net.minecraft.entity.player.PlayerInventory(null);
		var handler = new com.periodicelements.machine.MachineScreenHandler(0, playerInv, inv, props);
		var inner = new com.periodicelements.client.MachineScreen(handler, playerInv,
				net.minecraft.text.Text.translatable("block.periodic_elements.charcoal_blast_furnace"));
		return new net.minecraft.client.gui.screen.Screen(net.minecraft.text.Text.empty()) {
			@Override
			protected void init() {
				inner.init(client, width, height);
			}

			@Override
			public void render(net.minecraft.client.gui.DrawContext ctx, int mouseX, int mouseY, float delta) {
				inner.render(ctx, mouseX, mouseY, delta);
			}
		};
	}

	private static net.minecraft.item.Item item(String id) {
		return net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void press(MinecraftClient client, String label) {
		client.currentScreen.children().stream().filter(c -> c instanceof ButtonWidget b && java.util.List.of(label.split("\\|")).contains(b.getMessage().getString()))
				.findFirst().map(c -> (ButtonWidget) c).orElseThrow(() -> new IllegalStateException("no button " + label)).onPress();
	}

	private static void shot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + ".png", client.getFramebuffer(),
				text -> PeriodicElements.LOGGER.info("[guideshots] {}", text.getString()));
	}
}
