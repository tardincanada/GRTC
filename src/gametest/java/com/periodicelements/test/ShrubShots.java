package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.trees.Trees;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Dev-only (./gradlew runShrubShots, seed -Dpe.treeseed): screenshots the shrubs where they grow wild, a sky garden of
 * every species and a heated greenhouse on the snow, to run/screenshots.
 */
public class ShrubShots implements ClientModInitializer {
	// -Dpe.shrubbiomes=a,b,c picks the wild biomes.
	private static final String[] BIOMES = System.getProperty("pe.shrubbiomes", "jungle,savanna,"
			+ "desert,badlands,mangrove_swamp,beach").split(",");
	private static final int WAIT = 300, GAP = 10, ARB_Y = 200;
	// 3.22.0: -Dpe.shrubfrom=N puts only species N onwards in the garden, -Dpe.shrubrow the garden's row length.
	private static final int FROM = Integer.getInteger("pe.shrubfrom", 0), ROW = Integer.getInteger("pe.shrubrow", 5);

	private record Shot(String name, double x, double y, double z, float yaw, float pitch) {
	}

	private int tick = -1;
	private int worldTick = -1;
	private int index;
	private int phase;
	private final List<Shot> shots = new ArrayList<>();
	private BlockPos origin = BlockPos.ORIGIN;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("pe.shrubshots")) {
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
			if (tick > 20 && tick % 40 == 0 && client.currentScreen instanceof CreateWorldScreen screen) {
				WorldCreator wc = screen.getWorldCreator();
				wc.setWorldName("shrubs");
				wc.setGameMode(WorldCreator.Mode.CREATIVE);
				wc.setCheatsEnabled(true);
				wc.setSeed(System.getProperty("pe.treeseed", "4242"));
				press(client, net.minecraft.text.Text.translatable("selectWorld.create").getString());
			}
			return;
		}
		if (client.player == null) {
			return;
		}
		worldTick++;
		if (worldTick == 40) {
			cmd(client, "gamerule sendCommandFeedback false");
			cmd(client, "gamerule doDaylightCycle false");
			cmd(client, "gamerule doWeatherCycle false");
			cmd(client, "gamerule doMobSpawning false");
			cmd(client, "gamerule periodicRadiation off");
			cmd(client, "time set 6000");
			cmd(client, "weather clear");
			client.options.getViewDistance().setValue(16);
		}
		if (worldTick == 50) {
			client.setScreen(null);
			client.options.hudHidden = true;
			client.getToastManager().clear();
			client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
			cmd(client, "gamemode spectator");
		}
		if (worldTick < 60) {
			return;
		}
		int t = worldTick - 60;
		int i = t / WAIT, p = t % WAIT;
		if (i < BIOMES.length) {
			wild(client, BIOMES[i], p);
			return;
		}
		i -= BIOMES.length;
		int all = com.periodicelements.shrubs.Shrubs.ALL.size() - FROM;
		int rows = (all + ROW - 1) / ROW;
		if (i < 1) {
			// every species, ripe, in a garden in the sky: ROW per row, rows 3 blocks apart
			int x0 = 0, z0 = 0;
			if (p == 0) {
				tp(client, x0 + (ROW - 1) * 1.5, ARB_Y + 4.5, z0 - 3.5, 0, 38);
			}
			if (p == 80) {
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x0 - 6, ARB_Y, z0 - 6,
						x0 + ROW * 3 + 6, ARB_Y, z0 + rows * 3 + 6));
				for (int k = 0; k < all; k++) {
					var b = com.periodicelements.shrubs.Shrubs.ALL.get(FROM + k);
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:%s_bush[age=3]", x0 + (k % ROW) * 3,
							ARB_Y + 1, z0 + (k / ROW) * 3, b.id));
				}
			}
			if (p == WAIT - 5) {
				shot(client, "shrubs_garden");
			}
			return;
		}
		if (i < 2) {
			// a heated greenhouse on the snow: glass, an oak door, a heater, guarana and tea inside
			int x0 = 200, z0 = 0, y0 = ARB_Y;
			if (p == 0) {
				tp(client, x0 + 1.3, y0 + 3.2, z0 + 1.3, -45, 32);
			}
			if (p == 80) {
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:snow_block", x0 - 8, y0, z0 - 10, x0 + 14, y0, z0 + 14));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:glass", x0, y0 + 1, z0, x0 + 7, y0 + 5, z0 + 7));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", x0 + 1, y0 + 1, z0 + 1, x0 + 6, y0 + 4, z0 + 6));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x0 + 1, y0, z0 + 1, x0 + 6, y0, z0 + 6));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:oak_door[half=lower,facing=south]", x0 + 3, y0 + 1, z0));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:oak_door[half=upper,facing=south]", x0 + 3, y0 + 2, z0));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:greenhouse_heater[facing=west,lit=true]", x0 + 7, y0 + 1, z0 + 3));
				String[] in = {"guarana", "tea", "jasmine", "malagueta", "annatto", "henna"};
				for (int k = 0; k < in.length; k++) {
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:%s_bush[age=3]", x0 + 2 + (k % 3) * 2,
							y0 + 1, z0 + 2 + (k / 3) * 3, in[k]));
				}
			}
			if (p == WAIT - 60) {
				shot(client, "shrubs_greenhouse_inside");
				tp(client, x0 + 3.5, y0 + 4, z0 - 5, 0, 22);
			}
			if (p == WAIT - 5) {
				shot(client, "shrubs_greenhouse");
			}
			return;
		}
		if (i < 3) {
			// 3.22.0: a cooled greenhouse on the desert sand, with a Greenhouse Cooler and arctic and alpine shrubs inside,
			// and hydrangeas pink and blue (lime and aluminium) beside it
			int x0 = 400, z0 = 0, y0 = ARB_Y;
			if (p == 0) {
				tp(client, x0 + 1.3, y0 + 3.2, z0 + 1.3, -45, 32);
			}
			if (p == 80) {
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:sand", x0 - 8, y0, z0 - 10, x0 + 14, y0, z0 + 14));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:glass", x0, y0 + 1, z0, x0 + 7, y0 + 5, z0 + 7));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", x0 + 1, y0 + 1, z0 + 1, x0 + 6, y0 + 4, z0 + 6));
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x0 + 1, y0, z0 + 1, x0 + 6, y0, z0 + 6));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:oak_door[half=lower,facing=south]", x0 + 3, y0 + 1, z0));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:oak_door[half=upper,facing=south]", x0 + 3, y0 + 2, z0));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:greenhouse_cooler[facing=west,lit=true]", x0 + 7, y0 + 1, z0 + 3));
				String[] in = {"cloudberry", "crowberry", "dwarf_birch", "mountain_avens", "dwarf_willow", "arctic_bell_heather"};
				for (int k = 0; k < in.length; k++) {
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:%s_bush[age=3]", x0 + 2 + (k % 3) * 2,
							y0 + 1, z0 + 2 + (k / 3) * 3, in[k]));
				}
				for (int k = 0; k < 4; k++) {
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:grass_block", x0 - 4 + k * 2, y0, z0 - 4));
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:hydrangea_bush[age=3,blue=%s]",
							x0 - 4 + k * 2, y0 + 1, z0 - 4, k % 2 == 1));
				}
			}
			if (p == WAIT - 60) {
				shot(client, "shrubs_cold_greenhouse_inside");
				tp(client, x0 + 3.5, y0 + 4, z0 - 9, 0, 22);
			}
			if (p == WAIT - 5) {
				shot(client, "shrubs_cold_greenhouse");
			}
			return;
		}
		PeriodicElements.LOGGER.info("[shrubshots] done");
		client.scheduleStop();
	}

	/** Goes to the nearest patch of a biome, waits for it to load, then frames the thickest stand of new trees. */
	private void wild(MinecraftClient client, String biome, int p) {
		var server = client.getServer();
		if (p == 0) {
			server.submit(() -> {
				var reg = server.getRegistryManager().get(RegistryKeys.BIOME);
				var bio = reg.get(Identifier.ofVanilla(biome));
				List<String> ours = new ArrayList<>();
				bio.getGenerationSettings().getFeatures().forEach(list -> list.stream()
						.map(e -> e.getKey().map(k -> k.getValue().toString()).orElse("?"))
						.filter(n -> n.startsWith("periodic_elements:shrub/")).forEach(ours::add));
				PeriodicElements.LOGGER.info("[shrubshots] {} features: {}", biome, ours);
			}).join();
			var found = server.submit(() -> server.getOverworld().locateBiome(
					b -> b.matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome))),
					new BlockPos(0, 100, 0), 6400, 32, 64)).join();
			if (found == null) {
				PeriodicElements.LOGGER.info("[shrubshots] no {}", biome);
				return;
			}
			BlockPos b = heart(server.getOverworld(), found.getFirst(), biome);
			origin = b;
			tp(client, b.getX(), 160, b.getZ(), 0, 90);
			PeriodicElements.LOGGER.info("[shrubshots] {} at {}", biome, b);
		}
		if (p == 160) {
			BlockPos best = server.submit(() -> densest(server.getOverworld(), origin, biome)).join();
			if (best != null) {
				double d = 5;
				// from (-0.7d, +0.7d) the stand lies toward +x -z, which is yaw -135
				tp(client, best.getX() - d * 0.7, best.getY() + 2.5, best.getZ() + d * 0.7, -135, 24);
			}
		}
		if (p == WAIT - 5) {
			shot(client, "shrubs_wild_" + biome);
		}
	}

	/** The middle of the biggest patch of the biome within 1500 blocks (biome source sampled at y 96, 140 in the mountains, every 24 blocks). */
	private static BlockPos heart(net.minecraft.server.world.ServerWorld world, BlockPos near, String biome) {
		var cm = world.getChunkManager();
		var src = cm.getChunkGenerator().getBiomeSource();
		var sampler = cm.getNoiseConfig().getMultiNoiseSampler();
		var key = RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome));
		int n = 129;
		int y = biome.endsWith("_peaks") ? 200 : biome.equals("snowy_slopes") ? 170
				: Set.of("cherry_grove", "meadow", "grove", "windswept_hills").contains(biome) ? 140 : 96;
		boolean[][] in = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int x = near.getX() + (i - n / 2) * 24, z = near.getZ() + (j - n / 2) * 24;
				for (int yy = y - 40; yy <= y + 40 && !in[i][j]; yy += 20) {
					in[i][j] = src.getBiome(x >> 2, yy >> 2, z >> 2, sampler).matchesKey(key);
				}
			}
		}
		int best = -1, bi = n / 2, bj = n / 2;
		for (int i = 4; i < n - 4; i++) {
			for (int j = 4; j < n - 4; j++) {
				int c = 0;
				for (int a = -4; a <= 4; a++) {
					for (int b = -4; b <= 4; b++) {
						c += in[i + a][j + b] ? 1 : 0;
					}
				}
				if (c > best) {
					best = c;
					bi = i;
					bj = j;
				}
			}
		}
		PeriodicElements.LOGGER.info("[shrubshots] {} heart: {}/81 samples", biome, best);
		return new BlockPos(near.getX() + (bi - n / 2) * 24, 64, near.getZ() + (bj - n / 2) * 24);
	}

	/** The top of the column (within 64 blocks) with the most new-species logs around it in the biome, or null. */
	private static BlockPos densest(net.minecraft.server.world.ServerWorld world, BlockPos c, String biome) {
		Set<Block> logs = new HashSet<>(com.periodicelements.shrubs.Shrubs.ALL);
		List<BlockPos> found = new ArrayList<>();
		java.util.Map<String, Integer> kinds = new java.util.TreeMap<>();
		int[] cols = {0};
		for (int x = -72; x <= 72; x += 1) {
			for (int z = -72; z <= 72; z += 1) {
				int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING, c.getX() + x, c.getZ() + z);
				if (world.getBiome(new BlockPos(c.getX() + x, top, c.getZ() + z)).matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome)))) {
					cols[0]++;
				}
				for (int y = top + 1; y > top - 40; y--) {
					BlockPos pos = new BlockPos(c.getX() + x, y, c.getZ() + z);
					Block b = world.getBlockState(pos).getBlock();
					if (logs.contains(b) && world.getBiome(pos).matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome)))) {
						found.add(pos);
						kinds.merge(((com.periodicelements.shrubs.ShrubBlock) b).id, 1, Integer::sum);
						break;
					}
				}
			}
		}
		PeriodicElements.LOGGER.info("[shrubshots] {}: {} log columns, species {}, {} surface columns of the biome", biome, found.size(), kinds, cols[0]);
		BlockPos best = null;
		int bestN = 0;
		for (BlockPos a : found) {
			int n = 0;
			for (BlockPos b : found) {
				if (a.getSquaredDistance(b) < 8 * 8) {
					n++;
				}
			}
			if (n > bestN) {
				bestN = n;
				best = a;
			}
		}
		return best;
	}

	private static void tp(MinecraftClient client, double x, double y, double z, float yaw, float pitch) {
		cmd(client, String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", x, y, z, yaw, pitch));
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
				text -> PeriodicElements.LOGGER.info("[shrubshots] {}", text.getString()));
	}
}
