package com.periodicelements.radiation;

import com.google.gson.JsonObject;
import com.periodicelements.BlockComposition;
import com.periodicelements.ElementTable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How strongly things emit gamma rays: the dose rate at 1 m from one item or block, in Sv per hour.
 *
 * <p>Per-gram rates for each radioactive element come from {@code radiation.json}; compounds count by the mass
 * fraction of their elements (from {@code lens.json}). One unit is one gram, so a radium item (111 g) gives
 * about 0.9 Sv/h at 1 m, as 111 g of real radium would.
 */
public final class Radioactivity {
	private static Map<String, Double> gamma = Map.of();
	private static Map<String, Map<String, Double>> fractions = Map.of();
	private static JsonObject settings = new JsonObject();
	private static final Map<String, Double> CACHE = new ConcurrentHashMap<>();

	private Radioactivity() {
	}

	public static void load() {
		JsonObject root = ElementTable.readObject("/periodic_elements/radiation.json");
		Map<String, Double> g = new HashMap<>();
		root.getAsJsonObject("gamma").entrySet().forEach(e -> g.put(e.getKey(), e.getValue().getAsDouble()));
		gamma = g;
		settings = root;
		Map<String, Map<String, Double>> f = new HashMap<>();
		JsonObject subs = ElementTable.readObject("/periodic_elements/lens.json").getAsJsonObject("substances");
		for (var e : subs.entrySet()) {
			Map<String, Double> m = new HashMap<>();
			e.getValue().getAsJsonObject().entrySet().forEach(x -> m.put(x.getKey(), x.getValue().getAsDouble()));
			f.put(e.getKey(), m);
		}
		fractions = f;
		CACHE.clear();
		SOURCES.clear();
	}

	/** Lead walls and water: what is stored in these blocks doesn't count toward the dose around them. */
	public static boolean shielded(Block block) {
		if (!settings.has("shielded")) {
			return false;
		}
		String id = Registries.BLOCK.getId(block).toString();
		for (var e : settings.getAsJsonArray("shielded")) {
			if (e.getAsString().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/** A section of {@code radiation.json}, such as "cherenkov". */
	public static JsonObject section(String key) {
		return settings.has(key) ? settings.getAsJsonObject(key) : new JsonObject();
	}

	public static double setting(String key) {
		return settings.get(key).getAsDouble();
	}

	/** Units of each element in one of this item or block (by registry id), largest first; empty if unknown. */
	public static java.util.LinkedHashMap<String, Double> elementsOf(String id) {
		Map<String, Double> sum = new HashMap<>();
		BlockComposition.Entry entry = BlockComposition.of(id);
		if (entry != null) {
			for (BlockComposition.Part p : entry.parts()) {
				Map<String, Double> f = fractions.get(p.id());
				if (f == null) {
					sum.merge(p.id(), p.units(), Double::sum);
				} else {
					f.forEach((el, x) -> sum.merge(el, x * p.units(), Double::sum));
				}
			}
		}
		java.util.LinkedHashMap<String, Double> out = new java.util.LinkedHashMap<>();
		sum.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
				.forEach(e -> out.put(e.getKey(), e.getValue()));
		return out;
	}

	/** Sv/h at 1 m per gram of this element or substance. */
	public static double perGram(String id) {
		Double direct = gamma.get(id);
		if (direct != null) {
			return direct;
		}
		Map<String, Double> f = fractions.get(id);
		if (f == null) {
			return 0;
		}
		double sum = 0;
		for (var e : f.entrySet()) {
			sum += e.getValue() * gamma.getOrDefault(e.getKey(), 0.0);
		}
		return sum;
	}

	/** Sv/h at 1 m from one of this item or block (by registry id). */
	public static double of(String id) {
		return CACHE.computeIfAbsent(id, k -> {
			BlockComposition.Entry entry = BlockComposition.of(k);
			if (entry == null) {
				return 0.0;
			}
			double sum = 0;
			for (BlockComposition.Part p : entry.parts()) {
				sum += p.units() * perGram(p.id());
			}
			return sum;
		});
	}

	private static final Map<String, Map<String, Double>> SOURCES = new ConcurrentHashMap<>();

	/**
	 * Sv/h at 1 m from one of this item or block (by registry id), split by the radioactive element that gives it
	 * (fission products count as one source); empty when it is not radioactive. Gamma spectrometry reads this.
	 */
	public static Map<String, Double> sources(String id) {
		return SOURCES.computeIfAbsent(id, k -> {
			Map<String, Double> out = new HashMap<>();
			BlockComposition.Entry entry = BlockComposition.of(k);
			if (entry == null) {
				return out;
			}
			for (BlockComposition.Part p : entry.parts()) {
				Double direct = gamma.get(p.id());
				if (direct != null) {
					out.merge(p.id(), p.units() * direct, Double::sum);
					continue;
				}
				Map<String, Double> f = fractions.get(p.id());
				if (f == null) continue;
				for (var e : f.entrySet()) {
					double g = gamma.getOrDefault(e.getKey(), 0.0);
					if (g > 0) out.merge(e.getKey(), p.units() * e.getValue() * g, Double::sum);
				}
			}
			out.values().removeIf(v -> v <= 0);
			return out;
		});
	}

	/** Sv/h at 1 m from the fission products in one stack: the part iodide and Prussian blue act on. */
	public static double fissionOf(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		BlockComposition.Entry entry = BlockComposition.of(Registries.ITEM.getId(stack.getItem()).toString());
		if (entry == null) {
			return 0;
		}
		double sum = 0;
		for (BlockComposition.Part p : entry.parts()) {
			if (p.id().equals("fission_products")) {
				sum += p.units() * perGram(p.id());
			}
		}
		return sum * stack.getCount();
	}

	public static double of(Item item) {
		return of(Registries.ITEM.getId(item).toString());
	}

	public static double of(Block block) {
		return of(Registries.BLOCK.getId(block).toString());
	}

	public static double of(ItemStack stack) {
		return stack.isEmpty() ? 0 : of(stack.getItem()) * stack.getCount();
	}

	/** "3.2 mSv/h" style text for a dose rate in Sv/h. */
	public static String formatRate(double svPerHour) {
		return format(svPerHour) + "/h";
	}

	/** "3.2 mSv" style text for a dose in Sv. */
	public static String format(double sv) {
		if (sv >= 1) {
			return String.format(java.util.Locale.ROOT, "%.2f Sv", sv);
		}
		if (sv >= 1e-3) {
			return String.format(java.util.Locale.ROOT, "%.1f mSv", sv * 1e3);
		}
		if (sv >= 1e-6) {
			return String.format(java.util.Locale.ROOT, "%.1f µSv", sv * 1e6);
		}
		return String.format(java.util.Locale.ROOT, "%.2f µSv", sv * 1e6);
	}
}
