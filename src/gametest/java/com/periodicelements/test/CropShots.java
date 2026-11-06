package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
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
 * Dev-only (./gradlew runCropShots, seed -Dpe.treeseed): screenshots the shrubs where they grow wild, a sky garden of
 * every species and a heated greenhouse on the snow, to run/screenshots.
 */
public class CropShots implements ClientModInitializer {
	// -Dpe.cropbiomes=a,b,c picks the wild biomes.
	private static final String[] BIOMES = java.util.Arrays.stream(System.getProperty("pe.cropbiomes",
			"savanna,plains,swamp").split(",")).filter(b -> !b.isBlank()).toArray(String[]::new);
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
		if (!Boolean.getBoolean("pe.cropshots")) {
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
				wc.setWorldName("crops");
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
		List<String> crops = new ArrayList<>(com.periodicelements.crops.Crops.BLOCKS.keySet());
		if (i < 2) {
			// every crop in rows of four stages (age 0, 3, 5, 7) on farmland, with water between the rows
			int x0 = 0, z0 = 0;
			if (p == 0) {
				if (i == 0) {
					tp(client, x0 + 14, ARB_Y + 14, z0 - 9, 20, 45);
				} else {
					tp(client, x0 + 3.5, ARB_Y + 3.2, z0 - 3.0, -20, 30);
				}
			}
			if (p == 80 && i == 0) {
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x0 - 6, ARB_Y, z0 - 6,
						x0 + 40, ARB_Y, z0 + 24));
				for (int k = 0; k < crops.size(); k++) {
					int cx = x0 + (k % 5) * 6, cz = z0 + (k / 5) * 4;
					int[] ages = {0, 3, 5, 7};
					for (int a = 0; a < 4; a++) {
						cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:farmland[moisture=7]", cx + a, ARB_Y, cz));
						cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:%s_crop[age=%d]", cx + a, ARB_Y + 1, cz,
								crops.get(k), ages[a]));
					}
					cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:water", cx, ARB_Y, cz + 1, cx + 3, ARB_Y, cz + 1));
				}
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:feeding_trough", x0 + 30, ARB_Y + 1, z0 + 2));
			}
			if (p == WAIT - 5) {
				shot(client, i == 0 ? "crops_field" : "crops_field_close");
			}
			return;
		}
		if (i == 2) {
			// 3.37.0: the breeding lab (silo, irradiator, gamma garden, sequencer, thermocycler) beside a wheat field
			int x0 = 0, z0 = 40;
			if (p == 0) {
				tp(client, x0 + 5.5, ARB_Y + 3.6, z0 - 4.5, 0, 26);
			}
			if (p == 80) {
				cmd(client, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:grass_block", x0 - 8, ARB_Y, z0 - 8,
						x0 + 20, ARB_Y, z0 + 16));
				String[] lab = {"silo", "seed_irradiator", "dna_sequencer", "thermocycler"};
				for (int k = 0; k < lab.length; k++) {
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:%s", x0 + 1 + 3 * k, ARB_Y + 1, z0, lab[k]));
					cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:hopper[facing=down]", x0 + 1 + 3 * k, ARB_Y + 2, z0));
				}
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:silo", x0 + 1, ARB_Y + 3, z0));
				for (int dx = 0; dx < 11; dx++) {
					for (int dz = 3; dz < 10; dz++) {
						cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:farmland[moisture=7]", x0 + dx, ARB_Y, z0 + dz));
						cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:wheat[age=%d]", x0 + dx, ARB_Y + 1, z0 + dz, 4 + (dx + dz) % 4));
					}
				}
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d minecraft:air", x0 + 5, ARB_Y + 1, z0 + 6));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:gamma_garden", x0 + 5, ARB_Y + 1, z0 + 6));
				cmd(client, String.format(Locale.ROOT, "setblock %d %d %d periodic_elements:feeding_trough", x0 + 14, ARB_Y + 1, z0 + 1));
			}
			if (p == WAIT - 5) {
				shot(client, "crops_breeding_lab");
			}
			return;
		}
		PeriodicElements.LOGGER.info("[cropshots] done");
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
						.filter(n -> n.startsWith("periodic_elements:crop/")).forEach(ours::add));
				PeriodicElements.LOGGER.info("[cropshots] {} features: {}", biome, ours);
			}).join();
			var found = server.submit(() -> server.getOverworld().locateBiome(
					b -> b.matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(biome))),
					new BlockPos(0, 100, 0), 6400, 32, 64)).join();
			if (found == null) {
				PeriodicElements.LOGGER.info("[cropshots] no {}", biome);
				return;
			}
			BlockPos b = heart(server.getOverworld(), found.getFirst(), biome);
			origin = b;
			tp(client, b.getX(), 160, b.getZ(), 0, 90);
			PeriodicElements.LOGGER.info("[cropshots] {} at {}", biome, b);
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
			shot(client, "crops_wild_" + biome);
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
		PeriodicElements.LOGGER.info("[cropshots] {} heart: {}/81 samples", biome, best);
		return new BlockPos(near.getX() + (bi - n / 2) * 24, 64, near.getZ() + (bj - n / 2) * 24);
	}

	/** The top of the column (within 64 blocks) with the most new-species logs around it in the biome, or null. */
	private static BlockPos densest(net.minecraft.server.world.ServerWorld world, BlockPos c, String biome) {
		Set<Block> logs = new HashSet<>(com.periodicelements.crops.Crops.BLOCKS.values());
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
						kinds.merge(net.minecraft.registry.Registries.BLOCK.getId(b).getPath(), 1, Integer::sum);
						break;
					}
				}
			}
		}
		PeriodicElements.LOGGER.info("[cropshots] {}: {} log columns, species {}, {} surface columns of the biome", biome, found.size(), kinds, cols[0]);
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
				text -> PeriodicElements.LOGGER.info("[cropshots] {}", text.getString()));
	}
}
