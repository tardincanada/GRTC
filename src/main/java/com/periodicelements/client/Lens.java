package com.periodicelements.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.BlockComposition;
import com.periodicelements.ElementTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the Magnifying Lens shows: a block broken down into its elements by mass, naming only the elements the
 * player knows; the rest are summed as "other". An element is known from the start (level 0), or once the player
 * knows a research project whose reaction gives it (from lens.json, written by tools/generate.py).
 */
public final class Lens {
	public record Line(String element, double units) {
	}

	public record View(List<Line> known, double other, double total) {
	}

	private static Set<String> always = Set.of();
	private static Map<String, List<String>> sources = Map.of();
	private static Map<String, Map<String, Double>> substances = Map.of();

	private Lens() {
	}

	public static void load() {
		JsonObject root = ElementTable.readObject("/periodic_elements/lens.json");
		Set<String> a = new HashSet<>();
		root.getAsJsonArray("alwaysKnown").forEach(e -> a.add(e.getAsString()));
		Map<String, List<String>> s = new HashMap<>();
		for (var e : root.getAsJsonObject("sources").entrySet()) {
			List<String> projects = new ArrayList<>();
			e.getValue().getAsJsonArray().forEach(p -> projects.add(p.getAsString()));
			s.put(e.getKey(), projects);
		}
		Map<String, Map<String, Double>> f = new HashMap<>();
		for (var e : root.getAsJsonObject("substances").entrySet()) {
			Map<String, Double> m = new HashMap<>();
			for (Map.Entry<String, JsonElement> x : e.getValue().getAsJsonObject().entrySet()) {
				m.put(x.getKey(), x.getValue().getAsDouble());
			}
			f.put(e.getKey(), m);
		}
		always = a;
		sources = s;
		substances = f;
	}

	public static boolean knows(String element, Set<String> known) {
		if (always.contains(element)) {
			return true;
		}
		for (String p : sources.getOrDefault(element, List.of())) {
			if (known.contains(p)) {
				return true;
			}
		}
		return false;
	}

	/** Whether the player can name a substance or element: every element in its formula is known (EMI, 3.25.0). */
	public static boolean knowsPart(String id, Set<String> known) {
		Map<String, Double> fractions = substances.get(id);
		if (fractions == null) {
			return knows(id, known);
		}
		for (String element : fractions.keySet()) {
			if (!knows(element, known)) {
				return false;
			}
		}
		return true;
	}

	/** Elements of the entry by mass; known ones by name (largest first), the rest summed into {@code other}. */
	public static View view(BlockComposition.Entry entry, Set<String> known) {
		Map<String, Double> elements = new HashMap<>();
		double other = 0;
		for (BlockComposition.Part part : entry.parts()) {
			Map<String, Double> fractions = substances.get(part.id());
			if (fractions == null) {
				fractions = Map.of(part.id(), 1.0); // an element, or something with no formula
			}
			for (var f : fractions.entrySet()) {
				double units = part.units() * f.getValue();
				if (knows(f.getKey(), known)) {
					elements.merge(f.getKey(), units, Double::sum);
				} else {
					other += units;
				}
			}
		}
		List<Line> lines = new ArrayList<>();
		elements.forEach((k, v) -> lines.add(new Line(k, v)));
		lines.sort((x, y) -> Double.compare(y.units(), x.units()));
		return new View(lines, other, entry.amount());
	}

	/** For research checks elsewhere: the projects that reveal an element. */
	public static List<String> sources(String element) {
		return sources.getOrDefault(element, List.of());
	}
}
