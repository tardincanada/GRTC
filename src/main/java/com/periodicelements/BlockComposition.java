package com.periodicelements;

import java.util.List;
import java.util.Map;

/**
 * What vanilla blocks and items break down into, generated from data/compositions.csv and
 * data/rarity.csv. 1 block = 1000 units, 1 ingot or gem = 111.1 units, 1 bucket = 1000 units.
 * Units already include the rarity correction; the removed share is listed as "gangue".
 */
public final class BlockComposition {
	/** A substance or element id from this mod (or "gangue") and how many units of it. */
	public record Part(String id, double units) {
	}

	/** Total units of the block or item, and its parts, largest first. */
	public record Entry(double amount, List<Part> parts) {
	}

	public static final String GANGUE = "gangue";
	/** Gangue separates into the composition of ordinary stone. */
	public static final String GANGUE_SOURCE = "minecraft:stone";

	private static Map<String, Entry> compositions = Map.of();

	private BlockComposition() {
	}

	static void load() {
		compositions = ElementTable.loadCompositions();
	}

	/** Composition of a block or item id such as "minecraft:andesite", or null if it has none. */
	public static Entry of(String id) {
		return compositions.get(id);
	}

	/** Every composition by block or item id (read-only), for recipe viewers. */
	public static Map<String, Entry> all() {
		return java.util.Collections.unmodifiableMap(compositions);
	}

	public static int size() {
		return compositions.size();
	}
}
