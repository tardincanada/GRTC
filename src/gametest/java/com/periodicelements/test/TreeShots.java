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
 * Dev-only (./gradlew runTreeShots): makes a normal world (seed -Dpe.treeseed, default 4242), screenshots the new trees
 * where they grow wild in each stage-1 biome, then builds an arboretum in the sky with every species placed by
 * /place feature and screenshots it row by row, to run/screenshots.
 */
public class TreeShots implements ClientModInitializer {
	// -Dpe.treebiomes=a,b,c picks the wild biomes; -Dpe.treefrom=N starts the arboretum at species N (stage 2 = 52).
	private static final String[] BIOMES = System.getProperty("pe.treebiomes", "jungle,bamboo_jungle,savanna,savanna_plateau,"
			+ "desert,badlands,wooded_badlands,mangrove_swamp,beach").split(",");
	private static final int FROM = Integer.getInteger("pe.treefrom", 0);
	private static final int WAIT = 300, ROW = 5, GAP = 10, ARB_Y = 200;

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
		if (!Boolean.getBoolean("pe.treeshots")) {
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
				wc.setWorldName("trees");
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
		int rows = (Trees.ALL.size() - FROM + ROW - 1) / ROW;
		if (i < rows) {
			// each row is built on its own patch of sky, far from the others, just before its shot
			int x0 = 0, z0 = i * 64;
			double cx = x0 + (ROW - 1) * GAP / 2.0;
			if (p == 0) {
				tp(client, cx, ARB_Y + 9, z0 - 20, 0, 12);
			}
			if (p == 80) {
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x0 - 8, ARB_Y, z0 - 8,
						x0 + (ROW - 1) * GAP + 8, ARB_Y, z0 + 10));
				for (int k = FROM + i * ROW; k < Math.min(Trees.ALL.size(), FROM + (i + 1) * ROW); k++) {
					cmd(client, String.format(Locale.ROOT, "place feature periodic_elements:tree/%s %d %d %d", Trees.ALL.get(k).id(),
							x0 + ((k - FROM) % ROW) * GAP, ARB_Y + 1, z0));
				}
			}
			if (p == WAIT - 5) {
				shot(client, "trees_arboretum_" + (i + 1));
			}
			return;
		}
		PeriodicElements.LOGGER.info("[treeshots] done");
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
						.filter(n -> n.startsWith("periodic_elements:tree/")).forEach(ours::add));
				PeriodicElements.LOGGER.info("[treeshots] {} features: {}", biome, ours);
			}).join();
			var found = server.submit(() -> server.getOverworld().locateBiome(
					b -> b.matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome))),
					new BlockPos(0, 100, 0), 6400, 32, 64)).join();
			if (found == null) {
				PeriodicElements.LOGGER.info("[treeshots] no {}", biome);
				return;
			}
			BlockPos b = heart(server.getOverworld(), found.getFirst(), biome);
			origin = b;
			tp(client, b.getX(), 160, b.getZ(), 0, 90);
			PeriodicElements.LOGGER.info("[treeshots] {} at {}", biome, b);
		}
		if (p == 160) {
			BlockPos best = server.submit(() -> densest(server.getOverworld(), origin, biome)).join();
			if (best != null) {
				double d = 22;
				// from (-0.7d, +0.7d) the stand lies toward +x -z, which is yaw -135
				tp(client, best.getX() - d * 0.7, best.getY() + 12, best.getZ() + d * 0.7, -135, 22);
			}
		}
		if (p == WAIT - 5) {
			shot(client, "trees_wild_" + biome);
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
		PeriodicElements.LOGGER.info("[treeshots] {} heart: {}/81 samples", biome, best);
		return new BlockPos(near.getX() + (bi - n / 2) * 24, 64, near.getZ() + (bj - n / 2) * 24);
	}

	/** The top of the column (within 64 blocks) with the most new-species logs around it in the biome, or null. */
	private static BlockPos densest(net.minecraft.server.world.ServerWorld world, BlockPos c, String biome) {
		Set<Block> logs = new HashSet<>();
		Trees.ALL.forEach(s -> logs.add(s.log()));
		logs.add(com.periodicelements.farm.Farm.RUBBER_LOG);
		List<BlockPos> found = new ArrayList<>();
		java.util.Map<String, Integer> kinds = new java.util.TreeMap<>();
		int[] cols = {0};
		for (int x = -72; x <= 72; x += 1) {
			for (int z = -72; z <= 72; z += 1) {
				int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING, c.getX() + x, c.getZ() + z);
				if (world.getBiome(new BlockPos(c.getX() + x, top, c.getZ() + z)).matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome)))) {
					cols[0]++;
				}
				for (int y = top; y > top - 40; y--) {
					BlockPos pos = new BlockPos(c.getX() + x, y, c.getZ() + z);
					Block b = world.getBlockState(pos).getBlock();
					if (logs.contains(b) && world.getBiome(pos).matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome)))) {
						found.add(pos);
						kinds.merge(b == com.periodicelements.farm.Farm.RUBBER_LOG ? "rubber" : Trees.ofLog(b).id(), 1, Integer::sum);
						break;
					}
				}
			}
		}
		PeriodicElements.LOGGER.info("[treeshots] {}: {} log columns, species {}, {} surface columns of the biome", biome, found.size(), kinds, cols[0]);
		BlockPos best = null;
		int bestN = 0;
		for (BlockPos a : found) {
			int n = 0;
			for (BlockPos b : found) {
				if (a.getSquaredDistance(b) < 20 * 20) {
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
				text -> PeriodicElements.LOGGER.info("[treeshots] {}", text.getString()));
	}
}
