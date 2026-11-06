package com.periodicelements.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.BlockComposition;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactions, machines and fuels from {@code reactions.json} and {@code machines.json}
 * (generated from data/reactions.csv, data/machines.csv and data/fuels.csv).
 *
 * <p>Units: 1 unit is 1 gram, so a block (1000 units) is 1 kg. Energy is in kJ and power in kJ per tick.
 * A reaction's amounts and energy are per 100 units of its feed.
 */
public final class Reactions {
	/** One unit of an element or substance item. Same size as an ingot or dust: 9 make a block. */
	public static final double ITEM_UNITS = 1000.0 / 9.0;
	public static final double AMBIENT_C = 20.0;
	/** Extra flame temperature from forced air (the Bellows project). */
	public static final double BELLOWS_BONUS_C = 250.0;

	/**
	 * A product per 100 units of feed. {@code id} is a substance or element id, or "item:minecraft:charcoal".
	 * Vented products escape as gas; slag products join the residue unless a later reaction uses them.
	 */
	public record Product(String id, double units, boolean vented, boolean slag) {
	}

	/** A second ingredient a reaction consumes, in units per 100 units of feed (tin for bronze, water for acids). */
	public record Reagent(String id, double units) {
	}

	public record Reaction(String id, int index, int level, List<String> machines, String project, String feed,
						   List<Product> products, double carbon, double tMin, double tMax, double pressure,
						   double energy, String catalyst, List<Reagent> reagents, Map<String, Double> stamp,
						   double sepAlpha, double sepFraction, Map<String, Double> minRatio) {
		/** Is this a Calutron pass that splits its feed into enriched and depleted metal? */
		public boolean separates() {
			return sepAlpha > 0;
		}

		public Text name() {
			return Text.translatable("reaction." + PeriodicElements.MOD_ID + "." + id);
		}

		public boolean produces(String id) {
			for (Product p : products) {
				if (!p.vented() && !p.slag() && p.id().equals(id)) {
					return true;
				}
			}
			return false;
		}
	}

	public record MachineSpec(String id, int index, int level, String project, double maxC, double firePower,
							  double loss, double heatCapacity, int throughput, boolean bellows, String residue,
							  boolean electric, Map<String, Double> intake) {
		/** Machines that fill themselves from the air (the Air Liquefier). */
		public boolean breathes() {
			return !intake.isEmpty();
		}
	}

	/** How many Copper Wire blocks current can travel through from a pile or dynamo to a machine. */
	private static int wireReach = 64;

	public static int wireReach() {
		return wireReach;
	}

	/** How much of the reach one block of Insulated Cable uses (a quarter: it carries current four times as far). */
	private static double cableCost = 0.25;

	public static double cableCost() {
		return cableCost;
	}

	/** A Voltaic Pile ("electric") or Steam Engine ("shaft"), from machines.json "power". */
	public record PowerSpec(String id, int level, String project, String kind, JsonObject settings) {
		public double num(String key) {
			return settings.get(key).getAsDouble();
		}

		public String str(String key) {
			return settings.get(key).getAsString();
		}
	}

	private static List<PowerSpec> powerList = List.of();

	public static List<PowerSpec> power() {
		return powerList;
	}

	public static PowerSpec power(String id) {
		for (PowerSpec p : powerList) {
			if (p.id().equals(id)) {
				return p;
			}
		}
		return null;
	}

	public record Fuel(String fuel, double energy, double flame) {
		boolean matches(ItemStack stack) {
			if (fuel.startsWith("#")) {
				return stack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(fuel.substring(1))));
			}
			return Registries.ITEM.getId(stack.getItem()).toString().equals(fuel);
		}
	}

	private static List<Reaction> reactions = List.of();
	private static Map<String, MachineSpec> machines = Map.of();
	private static List<MachineSpec> machineList = List.of();
	private static List<Fuel> fuels = List.of();

	private Reactions() {
	}

	public static void load() {
		List<Reaction> list = new ArrayList<>();
		for (JsonElement e : ElementTable.readArray("/periodic_elements/reactions.json")) {
			JsonObject o = e.getAsJsonObject();
			List<String> ms = new ArrayList<>();
			o.getAsJsonArray("machines").forEach(x -> ms.add(x.getAsString()));
			List<Product> products = new ArrayList<>();
			for (JsonElement p : o.getAsJsonArray("products")) {
				JsonObject po = p.getAsJsonObject();
				products.add(new Product(po.get("id").getAsString(), po.get("units").getAsDouble(),
						po.get("vented").getAsBoolean(), po.get("slag").getAsBoolean()));
			}
			List<Reagent> reagents = new ArrayList<>();
			if (o.has("reagents")) {
				for (JsonElement p : o.getAsJsonArray("reagents")) {
					JsonObject po = p.getAsJsonObject();
					reagents.add(new Reagent(po.get("id").getAsString(), po.get("units").getAsDouble()));
				}
			}
			list.add(new Reaction(o.get("id").getAsString(), list.size(), o.get("level").getAsInt(), List.copyOf(ms),
					o.get("project").isJsonNull() ? null : o.get("project").getAsString(), o.get("feed").getAsString(),
					List.copyOf(products), o.get("carbon").getAsDouble(), o.get("tMin").getAsDouble(),
					o.get("tMax").getAsDouble(), o.get("pressure").getAsDouble(), o.get("energy").getAsDouble(),
					o.get("catalyst").getAsString(), List.copyOf(reagents), doubles(o, "stamp"),
					o.has("separate") ? o.getAsJsonObject("separate").get("alpha").getAsDouble() : 0,
					o.has("separate") ? o.getAsJsonObject("separate").get("fraction").getAsDouble() : 0,
					doubles(o, "minRatio")));
		}
		reactions = Collections.unmodifiableList(list);

		JsonObject root = ElementTable.readObject("/periodic_elements/machines.json");
		Map<String, MachineSpec> map = new LinkedHashMap<>();
		List<MachineSpec> mlist = new ArrayList<>();
		for (JsonElement e : root.getAsJsonArray("machines")) {
			JsonObject o = e.getAsJsonObject();
			MachineSpec m = new MachineSpec(o.get("id").getAsString(), mlist.size(), o.get("level").getAsInt(),
					o.get("project").isJsonNull() ? null : o.get("project").getAsString(), o.get("maxC").getAsDouble(),
					o.get("firePower").getAsDouble(), o.get("loss").getAsDouble(), o.get("heatCapacity").getAsDouble(),
					o.get("throughput").getAsInt(), o.get("bellows").getAsBoolean(), o.get("residue").getAsString(),
					o.has("electric") && o.get("electric").getAsBoolean(), intake(o));
			map.put(m.id(), m);
			mlist.add(m);
		}
		machines = Collections.unmodifiableMap(map);
		if (root.has("wireReach")) {
			wireReach = root.get("wireReach").getAsInt();
		}
		if (root.has("cableCost")) {
			cableCost = root.get("cableCost").getAsDouble();
		}
		machineList = Collections.unmodifiableList(mlist);
		List<PowerSpec> plist = new ArrayList<>();
		if (root.has("power")) {
			for (JsonElement e : root.getAsJsonArray("power")) {
				JsonObject o = e.getAsJsonObject();
				plist.add(new PowerSpec(o.get("id").getAsString(), o.get("level").getAsInt(),
						o.get("project").getAsString(), o.get("kind").getAsString(), o));
			}
		}
		powerList = Collections.unmodifiableList(plist);
		List<Fuel> flist = new ArrayList<>();
		for (JsonElement e : root.getAsJsonArray("fuels")) {
			JsonObject o = e.getAsJsonObject();
			flist.add(new Fuel(o.get("fuel").getAsString(), o.get("energy").getAsDouble(), o.get("flame").getAsDouble()));
		}
		fuels = Collections.unmodifiableList(flist);
	}

	private static Map<String, Double> doubles(JsonObject o, String key) {
		Map<String, Double> out = new LinkedHashMap<>();
		if (o.has(key)) {
			for (var e : o.getAsJsonObject(key).entrySet()) {
				out.put(e.getKey(), e.getValue().getAsDouble());
			}
		}
		return Collections.unmodifiableMap(out);
	}

	private static Map<String, Double> intake(JsonObject o) {
		Map<String, Double> out = new LinkedHashMap<>();
		if (o.has("intake")) {
			for (var e : o.getAsJsonObject("intake").entrySet()) {
				out.put(e.getKey(), e.getValue().getAsDouble());
			}
		}
		return Collections.unmodifiableMap(out);
	}

	public static List<Reaction> all() {
		return reactions;
	}

	public static Reaction get(int index) {
		return index >= 0 && index < reactions.size() ? reactions.get(index) : null;
	}

	public static MachineSpec machine(String id) {
		return machines.get(id);
	}

	public static MachineSpec machine(int index) {
		return index >= 0 && index < machineList.size() ? machineList.get(index) : null;
	}

	public static List<MachineSpec> machines() {
		return machineList;
	}

	/** Reactions this machine can run, in table order (earlier reactions feed later ones). */
	public static List<Reaction> forMachine(String machine) {
		List<Reaction> out = new ArrayList<>();
		for (Reaction r : reactions) {
			if (r.machines().contains(machine)) {
				out.add(r);
			}
		}
		return out;
	}

	public static Fuel fuel(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		for (Fuel f : fuels) {
			if (f.matches(stack)) {
				return f;
			}
		}
		// A cylinder of a fuel gas burns like nine flasks of it.
		net.minecraft.item.Item gas = com.periodicelements.Fluids.gasOf(stack.getItem());
		if (gas != null) {
			Fuel f = fuel(new ItemStack(gas));
			if (f != null) {
				return new Fuel(f.fuel(), f.energy() * com.periodicelements.Fluids.cylinderUnits() / (1000.0 / 9.0), f.flame());
			}
		}
		return null;
	}

	/** Units of free carbon one item supplies as a reducing agent (charcoal: 100, coal: 89). */
	public static double carbonIn(ItemStack stack) {
		BlockComposition.Entry entry = BlockComposition.of(Registries.ITEM.getId(stack.getItem()).toString());
		if (entry == null) {
			return 0;
		}
		for (BlockComposition.Part p : entry.parts()) {
			if (p.id().equals("carbon")) {
				return p.units();
			}
		}
		return 0;
	}

	/** The item a product or residue id comes out as. */
	public static Item outputItem(String id) {
		id = com.periodicelements.radiation.Isotopes.baseId(id);
		if (id.startsWith("item:")) {
			return Registries.ITEM.get(Identifier.of(id.substring(5)));
		}
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	/** How many units make one output item. */
	public static double unitsPerItem(String id) {
		id = com.periodicelements.radiation.Isotopes.baseId(id);
		if (id.startsWith("item:")) {
			BlockComposition.Entry entry = BlockComposition.of(id.substring(5));
			return entry != null ? entry.amount() : ITEM_UNITS;
		}
		return ITEM_UNITS;
	}
}
