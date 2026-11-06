package com.periodicelements.radiation;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;

/**
 * How bright the blue Cherenkov glow of a radioactive thing is, from 0 (none) to 1.
 *
 * <p>The glow follows the dose rate of one item or block at 1 m, that is how concentrated the activity is, not how much
 * of it there is: a stack of pitchblende glows no more than one ore. In air only the strongest emitters glow (from
 * {@code airThreshold}, radium and up); in or beside water the threshold drops to {@code waterThreshold} and the glow is
 * brighter. Each tenfold step above the threshold adds a share of the brightness, full at {@code decades} steps.
 * Running reactors and accelerators glow as an item of the dose rate given in {@code machines}.
 */
public final class Cherenkov {
	/** In air the glow is this fraction of what it is in water. */
	public static final double AIR_FACTOR = 0.75;

	private Cherenkov() {
	}

	private static JsonObject cfg() {
		return Radioactivity.section("cherenkov");
	}

	private static double num(String key, double fallback) {
		JsonObject c = cfg();
		return c.has(key) ? c.get(key).getAsDouble() : fallback;
	}

	/** Brightness 0..1 of something whose one unit gives {@code rate} Sv/h at 1 m, in water or in air. */
	public static double strength(double rate, boolean wet) {
		double threshold = wet ? num("waterThreshold", 1e-4) : num("airThreshold", 0.1);
		if (!(rate >= threshold)) {
			return 0;
		}
		double decades = num("decades", 6);
		double s = 0.4 + 0.6 * Math.min(1, Math.log10(rate / threshold) / decades);
		return wet ? s : s * AIR_FACTOR;
	}

	/** Sv/h at 1 m this block glows as: its own radioactivity, or a running machine's. */
	public static double rate(BlockState state) {
		Block block = state.getBlock();
		double own = Radioactivity.of(block);
		JsonObject machines = cfg().has("machines") ? cfg().getAsJsonObject("machines") : new JsonObject();
		String path = Registries.BLOCK.getId(block).getPath();
		if (machines.has(path)) {
			boolean needsRunning = !path.equals("radioisotope_generator");
			if (!needsRunning || (state.contains(Properties.LIT) && state.get(Properties.LIT))) {
				own = Math.max(own, machines.get(path).getAsDouble());
			}
		}
		return own;
	}

	/** Blue as 0xRRGGBB. */
	public static int color() {
		JsonObject c = cfg();
		if (!c.has("color")) {
			return 0x468CFF;
		}
		var a = c.getAsJsonArray("color");
		return (a.get(0).getAsInt() << 16) | (a.get(1).getAsInt() << 8) | a.get(2).getAsInt();
	}
}
