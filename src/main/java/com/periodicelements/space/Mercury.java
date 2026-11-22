package com.periodicelements.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Level 10, stage 7 (3.68.0), FICTION in the getting there: Mercury. A day there lasts 176 Earth days, so the line
 * between night and day (the terminator) crawls across the ground: east of it the Sun is up and the rock is at 430 °C,
 * west of it is a night at -180 °C. People live in the dawn band and move with it. Its crust is dark with graphite,
 * with bright hollows where sulfur boils off, and ice lies in the shade of polar craters (MercuryChunkGenerator).
 */
public final class Mercury {
	public static final RegistryKey<World> MERCURY = RegistryKey.of(RegistryKeys.WORLD, Moon.id("mercury"));

	/** Mercury's gravity is 3.7 m/s², 0.38 of the Earth's (as Mars's: it is small but dense, with a big iron core). */
	public static final double GRAVITY = 0.38;
	/** The dawn line moves west one block in this many ticks (a minute). */
	public static final int TICKS_PER_BLOCK = 1200;
	/** Degrees per block from the dawn line: 20 °C on it, 430 °C on the day side, -180 °C on the night side. */
	public static final double DEGREES_PER_BLOCK = 2.05;
	/** Hotter or colder than this, only the Thermal Suit keeps the wearer alive. */
	public static final double SUIT_HOT = 150, SUIT_COLD = -120;
	/** Sunlight on the day side: Mercury is 0.39 times as far from the Sun, so it gets 6.7 times the Earth's. */
	public static final double SUNLIGHT = 6.7;
	/** Cosmic rays and the solar wind with no air at all, close to the Sun. */
	public static final double COSMIC = 1.5;

	public static Block MERCURIAN_REGOLITH, MERCURIAN_CRUST, MERCURIAN_HOLLOWS;
	public static Item MERCURY_CAPSULE;

	private Mercury() {
	}

	public static boolean is(World world) {
		return world != null && world.getRegistryKey() == MERCURY;
	}

	/**
	 * 3.68.1: the clock the dawn line runs by, the world's time of day (as /time set and Iris's worldDay and worldTime
	 * see it, so the Fiat Lux shader lights the same line), not the total game time.
	 */
	public static long clock(World world) {
		return world.getTimeOfDay();
	}

	/** The x of the dawn line at time of day {@code time}: it crawls west, so a place first lies in night, then in day. */
	public static double dawnLine(long time) {
		return -(double) time / TICKS_PER_BLOCK;
	}

	/** The temperature of the ground at x, °C. */
	public static double temperature(long time, double x) {
		return MathHelper.clamp(20 + (x - dawnLine(time)) * DEGREES_PER_BLOCK, -180, 430);
	}

	/** Share of the Earth's sunlight a solar panel at x gets: none at night, 6.7 times in full day. */
	public static double sunlight(long time, double x) {
		double d = x - dawnLine(time);
		return SUNLIGHT * MathHelper.clamp(d / 150.0, 0, 1);
	}

	static void register(List<Item> items) {
		MERCURIAN_REGOLITH = Moon.rockBlock(items, "mercurian_regolith", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.BLACK).strength(0.6f).sounds(BlockSoundGroup.SAND)));
		MERCURIAN_CRUST = Moon.rockBlock(items, "mercurian_crust", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.DEEPSLATE_GRAY).strength(2f, 6f).requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));
		MERCURIAN_HOLLOWS = Moon.rockBlock(items, "mercurian_hollows", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.LIGHT_BLUE_GRAY).strength(0.8f).sounds(BlockSoundGroup.TUFF)));
		MERCURY_CAPSULE = Space.item(items, "mercury_capsule", "mercury_landing",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		Registry.register(Registries.CHUNK_GENERATOR, Moon.id("mercury"), MercuryChunkGenerator.CODEC);

		// the heat of the day side and the cold of the night side, once a second; the action bar shows the temperature
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (!is(world) || world.getTime() % 20 != 0) {
				return;
			}
			for (ServerPlayerEntity p : world.getPlayers()) {
				if (p.isCreative() || p.isSpectator()) {
					continue;
				}
				expose(world, p);
			}
		});
	}

	/** One second of Mercury's heat or cold on {@code p}. */
	public static void expose(ServerWorld world, ServerPlayerEntity p) {
		double t = temperature(clock(world), p.getX());
		boolean thermal = Fiction.wearingThermalSuit(p);
		boolean shade = Vacuum.cover(world, p.getBlockPos().up()) > 0;   // a roof keeps the Sun off, not the hot ground
		if (t > SUIT_HOT && !thermal) {
			p.damage(world.getDamageSources().hotFloor(), shade ? 1f : 3f);
			p.setOnFireFor(2);
		} else if (t < SUIT_COLD && !thermal) {
			p.setFrozenTicks(Math.min(p.getMinFreezeDamageTicks() + 40, p.getFrozenTicks() + 60));
		}
		int east = (int) Math.round(p.getX() - dawnLine(clock(world)));
		Formatting f = t > SUIT_HOT || t < SUIT_COLD ? (thermal ? Formatting.YELLOW : Formatting.RED) : Formatting.GREEN;
		p.sendMessage(Text.translatable("message.periodic_elements.mercury.temperature", Math.round(t),
				Text.translatable(east >= 0 ? "message.periodic_elements.mercury.east" : "message.periodic_elements.mercury.west",
						Math.abs(east))).formatted(f), true);
	}
}
