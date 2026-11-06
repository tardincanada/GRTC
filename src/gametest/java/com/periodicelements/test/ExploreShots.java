package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.explore.Explore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldPresets;

import java.util.UUID;

/**
 * Dev-only (./gradlew runExploreShots, 3.47.0): a flat world at midnight with a street of Sodium and LED lamps on fence
 * posts fed by batteries, a tent, the player wearing the cap lamp, then the night vision goggles with outlined zombies,
 * and the open backpack.
 */
public class ExploreShots implements ClientModInitializer {
	private int tick = -1;
	private int worldTick = -1;
	private boolean created;
	private BlockPos base;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.exploreshots")) {
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
				wc.setWorldName("explore");
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
		String lang = System.getProperty("pe.lang", "en_us");
		if (worldTick == 20 && !client.options.language.equals(lang)) {
			client.options.language = lang;
			client.getLanguageManager().setLanguage(lang);
			client.reloadResources();
		}
		if (worldTick == 60) {
			base = player.getBlockPos();
			int x = base.getX(), y = base.getY(), z = base.getZ();
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "time set 18000");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "kill @e[type=!player]");
			cmd(client, String.format("fill %d %d %d %d %d %d air", x - 30, y, z - 30, x + 30, y + 10, z + 30));
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:gravel", x - 30, y - 1, z - 2, x + 30, y - 1, z + 2));
			for (int i = -24; i <= 24; i += 12) {
				String lamp = i <= 0 ? "periodic_elements:sodium_lamp" : "periodic_elements:led_lamp";
				int px = x + i, pz = z - 3;
				cmd(client, String.format("fill %d %d %d %d %d %d minecraft:spruce_fence", px, y, pz, px, y + 3, pz));
				cmd(client, String.format("setblock %d %d %d %s", px, y + 4, pz, lamp));
				cmd(client, String.format("setblock %d %d %d periodic_elements:lithium_ion_battery", px, y, pz - 1));
			}
			cmd(client, String.format("setblock %d %d %d periodic_elements:tent[facing=north]", x + 4, y, z + 5));
			cmd(client, String.format("setblock %d %d %d periodic_elements:tent[facing=north]", x + 6, y, z + 6));
			cmd(client, String.format("setblock %d %d %d minecraft:campfire", x + 5, y, z + 8));
			var server = client.getServer();
			server.execute(() -> {
				var w = server.getOverworld();
				for (int i = -24; i <= 24; i += 12) {
					var be = w.getBlockEntity(new BlockPos(x + i, y, z - 4));
					if (be != null) {
						NbtCompound nbt = new NbtCompound();
						nbt.putDouble("Stored", 20000);
						be.read(nbt, w.getRegistryManager());
					}
				}
			});
		}
		if (worldTick == 70) {
			player.setPosition(base.getX() + 0.5, base.getY() + 6.0, base.getZ() + 14.5);
			player.setYaw(180);
			player.setPitch(18);
			player.getAbilities().flying = true;
			client.options.hudHidden = true;
		}
		if (worldTick == 160) {
			shot(client, "lamps_street");
		}
		if (worldTick == 170) {
			player.setPosition(base.getX() + 5.5, base.getY() + 2.2, base.getZ() + 12.5);
			player.setYaw(180);
			player.setPitch(15);
		}
		if (worldTick == 200) {
			shot(client, "tent");
		}
		// Cap lamp: walk away from the lamps in the dark.
		if (worldTick == 210) {
			equip(client, new ItemStack(Explore.CAP_LAMP));
			player.setPosition(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 30.5);
			player.setYaw(0);
			player.setPitch(12);
			cmd(client, String.format("fill %d %d %d %d %d %d minecraft:oak_log", base.getX() - 4, base.getY(), base.getZ() + 42,
					base.getX() + 4, base.getY() + 4, base.getZ() + 42));
		}
		if (worldTick == 250) {
			shot(client, "cap_lamp");
		}
		if (worldTick == 255) {
			equip(client, new ItemStack(Explore.SPOTLIGHT_HELMET));
		}
		if (worldTick == 300) {
			shot(client, "led_spotlight");
		}
		// Goggles: zombies standing still in the dark, one behind a wall.
		if (worldTick == 305) {
			equip(client, new ItemStack(Items.AIR));
			int zx = base.getX(), zy = base.getY(), zz = base.getZ() + 47;
			for (int i = -2; i <= 2; i += 2) {
				cmd(client, String.format("summon minecraft:zombie %d %d %d {NoAI:1b,PersistenceRequired:1b}", zx + i * 2, zy, zz + Math.abs(i)));
			}
			cmd(client, String.format("summon minecraft:skeleton %d %d %d {NoAI:1b,PersistenceRequired:1b}", zx + 1, zy, zz + 6));
			cmd(client, String.format("summon minecraft:pig %d %d %d {NoAI:1b}", zx - 3, zy, zz - 3));
			player.setPosition(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 34.5);
			player.setYaw(0);
			player.setPitch(4);
		}
		if (worldTick == 340) {
			shot(client, "goggles_off");
			equip(client, new ItemStack(Explore.GOGGLES));
		}
		if (worldTick == 380) {
			shot(client, "goggles_on");
		}
		if (worldTick == 390) {
			client.options.hudHidden = false;
			var server = client.getServer();
			UUID id = player.getUuid();
			server.execute(() -> {
				var sp = server.getPlayerManager().getPlayer(id);
				ItemStack pack = new ItemStack(Explore.LEATHER_BACKPACK);
				sp.getInventory().setStack(0, pack);
				sp.getInventory().setStack(1, new ItemStack(Explore.COMPASS));
				sp.getInventory().setStack(2, new ItemStack(Explore.ROPE_NET));
				sp.getInventory().setStack(3, new ItemStack(Explore.STEEL_CAGE));
				sp.getInventory().setStack(4, new ItemStack(Explore.TENT));
				sp.getInventory().selectedSlot = 0;
				com.periodicelements.explore.BackpackItem.store(pack, java.util.List.of(new ItemStack(Items.OAK_SAPLING, 12),
						new ItemStack(Items.WHEAT_SEEDS, 30), new ItemStack(Items.POPPY, 5), new ItemStack(Items.RAW_COPPER, 21),
						new ItemStack(Items.RAW_IRON, 9), new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.BREAD, 16),
						new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.TORCH, 48)));
			});
		}
		if (worldTick == 400) {
			var server = client.getServer();
			UUID id = player.getUuid();
			server.execute(() -> com.periodicelements.explore.BackpackItem.open(server.getPlayerManager().getPlayer(id), 0));
		}
		if (worldTick == 420) {
			shot(client, "backpack_screen");
		}
		if (worldTick == 430) {
			PeriodicElements.LOGGER.info("[exploreshots] done");
			client.scheduleStop();
		}
	}

	private static void equip(MinecraftClient client, ItemStack stack) {
		var server = client.getServer();
		UUID id = client.player.getUuid();
		server.execute(() -> server.getPlayerManager().getPlayer(id).equipStack(EquipmentSlot.HEAD, stack));
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
		ScreenshotRecorder.saveScreenshot(client.runDirectory, "pe_" + name + "_" + client.options.language + ".png",
				client.getFramebuffer(), text -> { });
	}
}
