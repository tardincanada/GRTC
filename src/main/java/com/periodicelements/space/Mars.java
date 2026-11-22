package com.periodicelements.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 9, stage 5 (3.66.0): Mars. Its dimension (MarsChunkGenerator), rocks and ice, gravity of 0.38, thin air of carbon
 * dioxide that nobody can breathe and nothing burns in, dust storms, and the soil that must be washed of perchlorate
 * before anything grows in it. The crewed flight is Moon.arrive and Rocket; the factories of oxygen and methane are
 * reactions of the existing machines (data/reactions.csv).
 */
public final class Mars {
	public static final RegistryKey<World> MARS = RegistryKey.of(RegistryKeys.WORLD, Moon.id("mars"));

	/** Martian gravity is 3.72 m/s², 0.38 of the Earth's. */
	public static final double GRAVITY = 0.38;
	/** Sunlight on Mars is 43 % of the Earth's (1.52 times as far from the Sun). */
	public static final double SUNLIGHT = 0.43;
	/** A dust storm at its worst lets this much of the sunlight through (Opportunity's last storm, 2018: under 1 %). */
	public static final double STORM_SUNLIGHT = 0.1;
	/** The air is a hundredth as dense as the Earth's: a wind turbine gets this share of its power. */
	public static final double WIND = 0.15;
	/** What the Air Liquefier draws from Martian air, per load (Curiosity's SAM, 2013: 95 % CO2, 2.6 % N2, 1.9 % Ar). */
	public static final Map<String, Double> AIR = new LinkedHashMap<>();

	static {
		AIR.put("carbon_dioxide", 951.0);
		AIR.put("nitrogen", 26.0);
		AIR.put("argon", 19.0);
		AIR.put("oxygen", 2.0);
		AIR.put("carbon_monoxide", 2.0);
	}

	public static Block MARTIAN_REGOLITH, MARTIAN_BASALT, MARTIAN_SULFATE, HEMATITE_CONCRETION, MARTIAN_ICE, DRY_ICE, WASHED_SOIL;
	public static Item MARS_CAPSULE;

	private Mars() {
	}

	public static boolean is(World world) {
		return world != null && world.getRegistryKey() == MARS;
	}

	private static AbstractBlock.Settings rock(MapColor colour, float hardness) {
		return AbstractBlock.Settings.create().mapColor(colour).strength(hardness, 6f).requiresTool().sounds(BlockSoundGroup.STONE);
	}

	static void register(List<Item> items) {
		MARTIAN_REGOLITH = Moon.rockBlock(items, "martian_regolith", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.ORANGE).strength(0.6f).sounds(BlockSoundGroup.SAND)));
		MARTIAN_BASALT = Moon.rockBlock(items, "martian_basalt", new Block(rock(MapColor.TERRACOTTA_BROWN, 1.8f)));
		MARTIAN_SULFATE = Moon.rockBlock(items, "martian_sulfate", new Block(rock(MapColor.TERRACOTTA_YELLOW, 1.0f)));
		HEMATITE_CONCRETION = Moon.rockBlock(items, "hematite_concretion", new Block(rock(MapColor.TERRACOTTA_BLUE, 2.2f)));
		MARTIAN_ICE = Moon.rockBlock(items, "martian_ice", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE).strength(0.8f).sounds(BlockSoundGroup.GLASS).slipperiness(0.98f)));
		DRY_ICE = Moon.rockBlock(items, "dry_ice", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE).strength(0.5f).sounds(BlockSoundGroup.SNOW)));
		WASHED_SOIL = Space.gatedBlock(items, "washed_martian_soil", "martian_greenhouse", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.DIRT_BROWN).strength(0.5f).sounds(BlockSoundGroup.GRAVEL)));
		MARS_CAPSULE = Space.item(items, "mars_capsule", "mars_transit",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		// a hoe turns washed soil into farmland, like dirt
		TillableBlockRegistry.register(WASHED_SOIL, HoeItem::canTillFarmland, Blocks.FARMLAND.getDefaultState());
		net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.CHUNK_GENERATOR, Moon.id("mars"),
				MarsChunkGenerator.CODEC);
		// a storm rising is told to everyone on Mars
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (is(world) && world.getTime() % 20 == 0) {
				long t = world.getTimeOfDay();
				if (storm(t) >= 0.05f && storm(t - 20) < 0.05f) {
					for (ServerPlayerEntity p : world.getPlayers()) {
						p.sendMessage(Text.translatable("message.periodic_elements.mars.storm").formatted(Formatting.GOLD), false);
					}
				}
			}
		});
	}

	/**
	 * How strong a dust storm is now, 0 to 1. Storms come by the time of day alone, so the client works it out too: each
	 * span of three days has one chance in four of a storm that builds for a day, rages, and settles.
	 */
	public static float storm(long timeOfDay) {
		long span = Math.floorDiv(timeOfDay, 72000L);
		long h = span * 0x9E3779B97F4A7C15L + 0x2545F4914F6CDD1DL;
		h ^= h >>> 31;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 29;
		if (Math.floorMod(h, 100L) >= 25) {
			return 0;
		}
		double t = Math.floorMod(timeOfDay, 72000L) / 72000.0;   // 0 to 1 over the three days
		return (float) MathHelper.clamp(Math.sin(Math.PI * t) * 1.6, 0, 1);
	}

	public static float storm(World world) {
		return is(world) ? storm(world.getTimeOfDay()) : 0;
	}

	/** Share of the Earth's sunlight a solar panel gets in {@code world}: less on Mars, far less in a dust storm. */
	public static double sunlight(World world) {
		if (!is(world)) {
			return 1;
		}
		return SUNLIGHT * (1 - (1 - STORM_SUNLIGHT) * storm(world));
	}
}
