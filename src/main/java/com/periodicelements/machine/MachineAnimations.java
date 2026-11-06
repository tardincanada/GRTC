package com.periodicelements.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;

import java.util.HashMap;
import java.util.Map;

/**
 * Which animation each machine shows for each process (3.33.0). {@code animations.json} is written by
 * tools/machines.py: machine id -> reaction id -> mode, the value of {@link MachineBlock#MODE} while that reaction
 * runs. Mode 0 is the machine's main animation; processes that look different (a gas instead of a liquid, another
 * colour of flame or melt) get their own mode and their own animated texture strip.
 */
public final class MachineAnimations {
	private static final Map<String, Map<String, Integer>> MODES = new HashMap<>();
	private static boolean loaded;

	private MachineAnimations() {
	}

	private static synchronized void load() {
		if (loaded) {
			return;
		}
		loaded = true;
		JsonObject root = ElementTable.readObject("/periodic_elements/animations.json");
		for (Map.Entry<String, JsonElement> m : root.entrySet()) {
			Map<String, Integer> byReaction = new HashMap<>();
			for (Map.Entry<String, JsonElement> r : m.getValue().getAsJsonObject().entrySet()) {
				byReaction.put(r.getKey(), r.getValue().getAsInt());
			}
			MODES.put(m.getKey(), byReaction);
		}
	}

	/** How many looks a machine has (at least 1). */
	public static int looks(String machine) {
		load();
		Map<String, Integer> m = MODES.get(machine);
		return m == null || m.isEmpty() ? 1 : m.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
	}

	/** The animation mode for a reaction in a machine; 0 if the machine has no special look for it. */
	public static int mode(String machine, String reaction) {
		load();
		Map<String, Integer> m = MODES.get(machine);
		Integer mode = m == null ? null : m.get(reaction);
		return mode == null ? 0 : Math.max(0, Math.min(MachineBlock.MAX_MODE, mode));
	}
}
