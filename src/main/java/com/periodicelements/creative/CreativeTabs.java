package com.periodicelements.creative;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 3.51.0: the creative inventory tabs. Every item of the mod goes to exactly one tab, read from
 * {@code periodic_elements/creative_tabs.json} (written by tools/creative_tabs.py). Substances are split by their
 * state at SATP (25 C, 1 atm). Inside a tab the items keep the order they were registered in.
 */
public final class CreativeTabs {
	/** Tab of an item the json does not name (a test checks there are none). */
	public static final String FALLBACK = "materials";
	private static final Map<String, String> ITEM_TAB = new HashMap<>();
	private static final Map<String, String> ICONS = new LinkedHashMap<>();

	private CreativeTabs() {
	}

	public static void register() {
		load();
		for (Map.Entry<String, String> tab : ICONS.entrySet()) {
			String id = tab.getKey();
			String icon = tab.getValue();
			Registry.register(Registries.ITEM_GROUP, Identifier.of(PeriodicElements.MOD_ID, id), FabricItemGroup.builder()
					.icon(() -> {
						Item i = Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, icon));
						return new ItemStack(i == Items.AIR ? Items.BARRIER : i);
					})
					.displayName(Text.translatable("itemGroup." + PeriodicElements.MOD_ID + "." + id))
					.entries((context, entries) -> items(id).forEach(entries::add))
					.build());
		}
	}

	/** Tab ids in display order. */
	public static List<String> tabs() {
		return List.copyOf(ICONS.keySet());
	}

	/** The tab an item of the mod goes to. */
	public static String tabOf(Item item) {
		return ITEM_TAB.getOrDefault(Registries.ITEM.getId(item).getPath(), FALLBACK);
	}

	/** Items of the mod the json leaves out. */
	public static List<Identifier> unplaced() {
		List<Identifier> out = new ArrayList<>();
		for (Item item : Registries.ITEM) {
			Identifier id = Registries.ITEM.getId(item);
			if (id.getNamespace().equals(PeriodicElements.MOD_ID) && !ITEM_TAB.containsKey(id.getPath())) {
				out.add(id);
			}
		}
		return out;
	}

	/** The items of a tab, in registration order. */
	public static List<Item> items(String tab) {
		List<Item> out = new ArrayList<>();
		for (Item item : Registries.ITEM) {
			Identifier id = Registries.ITEM.getId(item);
			if (id.getNamespace().equals(PeriodicElements.MOD_ID) && tabOf(item).equals(tab)) {
				out.add(item);
			}
		}
		return out;
	}

	private static void load() {
		try (InputStream in = CreativeTabs.class.getResourceAsStream("/periodic_elements/creative_tabs.json")) {
			if (in == null) {
				throw new IllegalStateException("periodic_elements/creative_tabs.json is missing");
			}
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (var e : root.getAsJsonArray("tabs")) {
				JsonObject t = e.getAsJsonObject();
				ICONS.put(t.get("id").getAsString(), t.get("icon").getAsString());
			}
			for (var e : root.getAsJsonObject("items").entrySet()) {
				ITEM_TAB.put(e.getKey(), e.getValue().getAsString());
			}
		} catch (java.io.IOException e) {
			throw new IllegalStateException(e);
		}
		int missing = unplacedCount();
		if (missing > 0) {
			PeriodicElements.LOGGER.warn("{} items have no creative tab and go to '{}'", missing, FALLBACK);
		}
	}

	private static int unplacedCount() {
		return unplaced().size();
	}
}
