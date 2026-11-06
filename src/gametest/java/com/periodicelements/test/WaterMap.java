package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.water.Water;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.WorldChunk;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 3.45.0: with -Dpe.watermap=true (./gradlew runWaterMap), generates the chunks around the spawn and draws what water
 * is where: salt water dark blue, fresh water cyan, brine white, deserts and badlands sand-coloured, other land green.
 * Writes watermap.png and watermap.txt (counts, brine springs) to the run folder and stops the server.
 */
public class WaterMap implements ModInitializer {
	@Override
	public void onInitialize() {
		if (!Boolean.getBoolean("pe.watermap")) return;
		ServerLifecycleEvents.SERVER_STARTED.register(WaterMap::draw);
	}

	private static void draw(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		int half = Integer.getInteger("pe.watermap.half", 256);
		BlockPos spawn = world.getSpawnPos();
		String at = System.getProperty("pe.watermap.at", "");
		if (!at.isEmpty()) spawn = new BlockPos(Integer.parseInt(at.split(",")[0]), 0, Integer.parseInt(at.split(",")[1]));
		int cx0 = (spawn.getX() - half) >> 4, cz0 = (spawn.getZ() - half) >> 4, n = (2 * half) >> 4;
		BufferedImage img = new BufferedImage(n * 16, n * 16, BufferedImage.TYPE_INT_RGB);
		long plants = 0, salt = 0, fresh = 0, brine = 0, freshInSea = 0, saltInland = 0, freshInDesert = 0;
		List<String> springs = new ArrayList<>();
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				WorldChunk chunk = world.getChunk(cx0 + i, cz0 + j);
				ChunkPos cp = chunk.getPos();
				if (Water.hasBrineSpring(world.getSeed(), cp)) {
					BlockPos c = new BlockPos(cp.getStartX() + 8, 0, cp.getStartZ() + 8);
					int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, 8, 8);
					boolean made = Water.isBrine(chunk.getFluidState(c.withY(y)).getFluid());
					springs.add(c.getX() + " " + y + " " + c.getZ() + (made ? " brine spring" : " (chunk drawn, no room for a spring)"));
				}
				for (int lx = 0; lx < 16; lx++) {
					for (int lz = 0; lz < 16; lz++) {
						int x = cp.getStartX() + lx, z = cp.getStartZ() + lz;
						int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lx, lz);
						FluidState f = chunk.getFluidState(new BlockPos(x, y, z));
						RegistryEntry<Biome> biome = world.getBiome(new BlockPos(x, world.getSeaLevel(), z));
						boolean sea = Water.isSea(biome);
						boolean desert = biome.matchesKey(BiomeKeys.DESERT) || biome.isIn(BiomeTags.IS_BADLANDS);
						Fluid fl = f.getFluid();
						int rgb;
						if (!(chunk.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof net.minecraft.block.FluidBlock) && !f.isEmpty()) {
							plants++; // kelp, seagrass and other waterlogged blocks hold vanilla (fresh) water
							img.setRGB(i * 16 + lx, j * 16 + lz, sea ? 0x1E3C96 : 0x3CE6FF);
							continue;
						}
						if (Water.isSaltWater(fl)) {
							salt++;
							if (!sea) saltInland++;
							rgb = sea ? 0x1E3C96 : 0xD020D0; // magenta: salt water outside the sea's biomes
						} else if (Water.isFresh(fl)) {
							fresh++;
							if (sea) freshInSea++;
							if (desert) freshInDesert++;
							rgb = 0x3CE6FF;
						} else if (Water.isBrine(fl)) {
							brine++;
							rgb = 0xFFFFFF;
						} else if (!f.isEmpty()) {
							rgb = 0xFF6000; // lava
						} else {
							int shade = Math.max(0, Math.min(60, (y - world.getSeaLevel()) / 2));
							rgb = desert ? rgb(220 - shade, 196 - shade, 130 - shade) : rgb(70 + shade, 120 + shade, 60 + shade);
						}
						img.setRGB(i * 16 + lx, j * 16 + lz, rgb);
					}
				}
			}
		}
		try {
			ImageIO.write(img, "png", new File("watermap.png"));
			try (PrintWriter w = new PrintWriter("watermap.txt")) {
				w.printf("area %d x %d blocks around spawn %s, %d s%n", 2 * half, 2 * half, spawn.toShortString(),
						(System.currentTimeMillis() - t0) / 1000);
				w.printf("top water columns: salt %d, fresh %d, brine %d%n", salt, fresh, brine);
				w.printf("waterlogged blocks on top (kelp, seagrass; not counted): %d%n", plants);
				w.printf("fresh water in sea biomes %d, salt water outside sea biomes %d, fresh water in deserts/badlands %d%n",
						freshInSea, saltInland, freshInDesert);
				for (String s : springs) w.println(s);
			}
			PeriodicElements.LOGGER.info("[watermap] salt {} fresh {} brine {} freshInSea {} saltInland {} freshInDesert {} springs {}",
					salt, fresh, brine, freshInSea, saltInland, freshInDesert, springs);
		} catch (Exception e) {
			PeriodicElements.LOGGER.error("[watermap] failed", e);
		}
		server.stop(false);
	}

	private static int rgb(int r, int g, int b) {
		return (r << 16) | (g << 8) | b;
	}
}
