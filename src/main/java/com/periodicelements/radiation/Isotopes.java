package com.periodicelements.radiation;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.periodicelements.ElementItem;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Isotope ratios (1.3.0). A few elements' items carry the atom % of one isotope as a data component, because the
 * game has processes that change it (Calutron enrichment, the reactor, electrolysis). An item without the component
 * has the natural ratio, so ordinary items still stack. Ratios are rounded to a fixed step per element so that
 * items from the same process stack too.
 *
 * <p>Machines keep ratios through their output buffers by keying the buffer "id#ratio" (see {@link #key}).
 */
public final class Isotopes {
	public record Tracked(String element, String isotope, double natural, double step, String changedBy) {
		public int decimals() {
			return Math.max(0, (int) Math.ceil(-Math.log10(step) - 1e-9));
		}

		public double round(double ratio) {
			return Math.max(0, Math.min(100, Math.round(ratio / step) * step));
		}

		public boolean isNatural(double ratio) {
			return Math.abs(ratio - natural) < step / 2;
		}

		public String format(double ratio) {
			return String.format(Locale.ROOT, "%." + decimals() + "f", round(ratio));
		}
	}

	public static ComponentType<Double> RATIO;
	private static final Map<String, Tracked> TRACKED = new HashMap<>();
	private static final Map<String, Double> IRRADIATION = new HashMap<>();
	/** Reactor targets that change only when enriched enough: item id -> (element, minimum %, product id). */
	public record Conditional(String element, double min, String product) {
	}

	private static final Map<String, Conditional> IRRADIATION_IF = new HashMap<>();
	private static final Map<String, String> NATURAL = new HashMap<>();
	private static final String ITEM_PREFIX = "item:" + PeriodicElements.MOD_ID + ":";

	private Isotopes() {
	}

	public static void register() {
		RATIO = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(PeriodicElements.MOD_ID, "isotope_ratio"),
				ComponentType.<Double>builder().codec(Codec.DOUBLE).packetCodec(PacketCodecs.DOUBLE).build());
	}

	public static void load() {
		JsonObject root = ElementTable.readObject("/periodic_elements/isotopes.json");
		TRACKED.clear();
		for (var e : root.getAsJsonObject("tracked").entrySet()) {
			JsonObject o = e.getValue().getAsJsonObject();
			TRACKED.put(e.getKey(), new Tracked(e.getKey(), o.get("isotope").getAsString(), o.get("natural").getAsDouble(),
					o.get("step").getAsDouble(), o.get("changedBy").getAsString()));
		}
		IRRADIATION.clear();
		root.getAsJsonObject("irradiation").entrySet().forEach(e -> IRRADIATION.put(e.getKey(), e.getValue().getAsDouble()));
		IRRADIATION_IF.clear();
		if (root.has("irradiationIf")) {
			for (var e : root.getAsJsonObject("irradiationIf").entrySet()) {
				JsonObject o = e.getValue().getAsJsonObject();
				IRRADIATION_IF.put(e.getKey(), new Conditional(o.get("element").getAsString(), o.get("min").getAsDouble(),
						o.get("product").getAsString()));
			}
		}
		NATURAL.clear();
		JsonObject decay = ElementTable.readObject("/periodic_elements/decay.json");
		if (decay.has("natural")) {
			decay.getAsJsonObject("natural").entrySet().forEach(e -> NATURAL.put(e.getKey(), e.getValue().getAsString()));
		}
	}

	@Nullable
	public static Tracked tracked(@Nullable String element) {
		return element == null ? null : TRACKED.get(element);
	}

	/** "92.2% Si-28, 4.7% Si-29, 3.1% Si-30" for a stable element, or null. */
	@Nullable
	public static String natural(String element) {
		return NATURAL.get(element);
	}

	/** The ratio a reactor-made item gets, or null. */
	@Nullable
	public static Double irradiated(String itemId) {
		return IRRADIATION.get(itemId);
	}

	/** What a reactor makes of this stack because of its enrichment (lithium-6 into tritium), or null. */
	@Nullable
	public static String irradiatedIf(ItemStack stack) {
		Conditional c = IRRADIATION_IF.get(Registries.ITEM.getId(stack.getItem()).toString());
		return c != null && ratio(stack, c.element()) >= c.min() - 1e-9 ? c.product() : null;
	}

	/** The id without its "#ratio" suffix. */
	public static String baseId(String key) {
		int i = key.indexOf('#');
		return i < 0 ? key : key.substring(0, i);
	}

	/** The tracked element a charge or buffer id stands for ("uranium", "item:periodic_elements:uranium"), or null. */
	@Nullable
	public static String elementOf(String id) {
		String base = baseId(id);
		if (base.startsWith(ITEM_PREFIX)) {
			base = base.substring(ITEM_PREFIX.length());
		}
		return TRACKED.containsKey(base) ? base : null;
	}

	/** The buffer key for this id at this ratio: the plain id when the ratio is natural or the id isn't tracked. */
	public static String key(String id, double ratio) {
		Tracked t = tracked(elementOf(id));
		if (t == null || t.isNatural(ratio)) {
			return baseId(id);
		}
		return baseId(id) + "#" + t.format(ratio);
	}

	/** The ratio a buffer key carries (natural for a plain id). */
	public static double ratioOfKey(String key) {
		Tracked t = tracked(elementOf(key));
		int i = key.indexOf('#');
		if (i >= 0) {
			return Double.parseDouble(key.substring(i + 1));
		}
		return t == null ? Double.NaN : t.natural();
	}

	/** The ratio of a tracked element in this stack: its component if it is that element's item, else natural. */
	public static double ratio(ItemStack stack, String element) {
		Tracked t = tracked(element);
		if (t == null) {
			return Double.NaN;
		}
		if (stack.getItem() instanceof ElementItem e && e.getElement().id().equals(element)) {
			Double r = stack.get(RATIO);
			if (r != null) {
				return r;
			}
		}
		return t.natural();
	}

	/** Gives the stack this ratio (natural removes the component, so it stacks with ordinary items). */
	public static ItemStack stamp(ItemStack stack, String element, double ratio) {
		Tracked t = tracked(element);
		if (t == null || t.isNatural(ratio)) {
			stack.remove(RATIO);
		} else {
			stack.set(RATIO, t.round(ratio));
		}
		return stack;
	}

	/**
	 * One separation pass on feed of ratio {@code r}: the cut {@code fraction} has its odds multiplied by
	 * {@code alpha}; the rest is depleted so that the isotope is conserved. Returns {enriched, depleted}.
	 */
	public static double[] separate(double r, double alpha, double fraction) {
		double odds = r / Math.max(1e-12, 100 - r);
		double rp = Math.min(99.99, 100 * odds * alpha / (1 + odds * alpha));
		double f = Math.min(fraction, rp > 0 ? r / rp : fraction);
		double rt = Math.max(0, (r - f * rp) / (1 - f));
		return new double[]{rp, rt};
	}

	/** "natural", "enriched", "depleted", or for plutonium its grade. */
	public static String label(Tracked t, double ratio) {
		if (t.element().equals("plutonium")) {
			return ratio < 7 ? "weapons_grade" : ratio < 18 ? "fuel_grade" : "reactor_grade";
		}
		return t.isNatural(ratio) ? "natural" : ratio > t.natural() ? "enriched" : "depleted";
	}
}
