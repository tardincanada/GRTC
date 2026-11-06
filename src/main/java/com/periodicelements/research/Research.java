package com.periodicelements.research;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The research tree from {@code periodic_elements/research.json} (generated from data/research.csv).
 * Research is per player: a player's tech level is the highest level whose key projects, and the key
 * projects of every level below it, are all finished. Level 0 needs no research.
 */
public final class Research {
	/** A sample: one item id, or several joined by "|" when any of them will do. */
	public record Sample(String item, int count) {
		/** The first choice, shown in the station's list. */
		public Item asItem() {
			return Registries.ITEM.get(Identifier.of(item.split("\\|")[0]));
		}

		public boolean matches(net.minecraft.item.ItemStack stack) {
			for (String id : item.split("\\|")) {
				if (stack.isOf(Registries.ITEM.get(Identifier.of(id)))) {
					return true;
				}
			}
			return false;
		}
	}

	public record Project(String id, int index, int level, boolean key, String category, String date,
						  List<String> requires, List<Sample> samples, List<String> unlocks) {
		public Text name() {
			return Text.translatable("research." + PeriodicElements.MOD_ID + "." + id);
		}

		public Text description() {
			return Text.translatable("research." + PeriodicElements.MOD_ID + "." + id + ".desc");
		}
	}

	public static final int MAX_LEVEL = 7;

	private static List<Project> projects = List.of();
	private static Map<String, Project> byId = Map.of();
	/** "machine:lime_kiln", "reaction:limestone_calcination", "upgrade:forced_air" ... to the project that unlocks it. */
	private static Map<String, String> unlockedBy = Map.of();

	private Research() {
	}

	public static void load() {
		List<Project> list = new ArrayList<>();
		for (JsonElement e : ElementTable.readArray("/periodic_elements/research.json")) {
			JsonObject o = e.getAsJsonObject();
			List<String> requires = new ArrayList<>();
			o.getAsJsonArray("requires").forEach(x -> requires.add(x.getAsString()));
			List<Sample> samples = new ArrayList<>();
			o.getAsJsonArray("samples").forEach(x -> samples.add(new Sample(
					x.getAsJsonObject().get("item").getAsString(), x.getAsJsonObject().get("count").getAsInt())));
			List<String> unlocks = new ArrayList<>();
			o.getAsJsonArray("unlocks").forEach(x -> unlocks.add(x.getAsString()));
			list.add(new Project(o.get("id").getAsString(), list.size(), o.get("level").getAsInt(),
					o.get("key").getAsBoolean(), o.get("category").getAsString(), o.get("date").getAsString(),
					List.copyOf(requires), List.copyOf(samples), List.copyOf(unlocks)));
		}
		Map<String, Project> ids = new LinkedHashMap<>();
		Map<String, String> unlocks = new HashMap<>();
		for (Project p : list) {
			ids.put(p.id(), p);
			for (String u : p.unlocks()) {
				unlocks.put(u, p.id());
			}
		}
		projects = Collections.unmodifiableList(list);
		byId = Collections.unmodifiableMap(ids);
		unlockedBy = Collections.unmodifiableMap(unlocks);
	}

	public static List<Project> all() {
		return projects;
	}

	public static Project get(String id) {
		return byId.get(id);
	}

	public static Project get(int index) {
		return index >= 0 && index < projects.size() ? projects.get(index) : null;
	}

	/** The project that unlocks e.g. "machine:bloomery", or null if nothing needs to be researched for it. */
	public static String unlockedBy(String unlock) {
		return unlockedBy.get(unlock);
	}

	/** Whether a player who knows {@code known} may use the thing named by {@code unlock}. */
	public static boolean allows(Set<String> known, String unlock) {
		String project = unlockedBy(unlock);
		return project == null || known.contains(project);
	}

	/** Highest tech level reached: every key project of levels 1..L is done. */
	public static int techLevel(Collection<String> known) {
		int level = 0;
		for (int l = 1; l <= MAX_LEVEL; l++) {
			boolean any = false;
			boolean all = true;
			for (Project p : projects) {
				if (p.level() == l && p.key()) {
					any = true;
					all &= known.contains(p.id());
				}
			}
			if (!any || !all) {
				break;
			}
			level = l;
		}
		return level;
	}

	public enum Status { DONE, AVAILABLE, NEEDS_PROJECTS, NEEDS_LEVEL }

	/** Can this player start the project now (ignoring samples)? */
	public static Status status(Project p, Collection<String> known) {
		if (known.contains(p.id())) {
			return Status.DONE;
		}
		if (techLevel(known) < p.level() - 1) {
			return Status.NEEDS_LEVEL;
		}
		for (String r : p.requires()) {
			if (!known.contains(r)) {
				return Status.NEEDS_PROJECTS;
			}
		}
		return Status.AVAILABLE;
	}

	/** Research time in ticks: 20 seconds per level. */
	public static int duration(Project p) {
		return 400 * p.level();
	}
}
