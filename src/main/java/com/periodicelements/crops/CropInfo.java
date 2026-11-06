package com.periodicelements.crops;

import net.minecraft.item.Item;

import java.util.List;
import java.util.function.Supplier;

/**
 * What the genetics need to know about a crop (mod crop or vanilla wheat, carrots, potatoes, beetroots).
 *
 * @param climates the climates it grows at full speed in (warm, temperate, cold), or null for vanilla crops (any)
 * @param outcross the share of its flowers pollinated by a neighbour (the rest pollinate themselves)
 * @param cloned    grown from a cutting or tuber: the offspring is a copy of the parent
 * @param perennial for coffee and alfalfa, the age it grows back from after picking; null otherwise
 */
public record CropInfo(String id, double outcross, boolean cloned, List<String> climates, List<String> biomes,
		boolean arid, boolean water, boolean legume, Integer perennial, Supplier<Item> seed, Supplier<Item> product) {
	/** Wheat and rice: the crops with a semi-dwarf gene (3.37.0). */
	public boolean stature() {
		return id.equals("wheat") || id.equals("rice");
	}

	public boolean cold() {
		return climates != null && climates.contains("cold");
	}
}
