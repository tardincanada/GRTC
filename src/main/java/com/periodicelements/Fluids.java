package com.periodicelements;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Containers for liquids and gases (data from fluids.json, written by tools/fluids.py).
 *
 * <p>Every liquid or gas item is held in a Glass Flask (111.1 units): its recipe remainder is the empty flask,
 * so crafting, machines and furnaces give the flask back. Each gas also has a "&lt;gas&gt;_cylinder" item of
 * 1000 units whose remainder is the empty Steel Cylinder; only a machine with a Compressor makes them.
 */
public final class Fluids {
	public static Item GLASS_FLASK;
	public static Item STEEL_CYLINDER;
	private static final Set<String> LIQUIDS = new HashSet<>();
	private static final Set<String> GASES = new HashSet<>();
	private static final Map<Item, Item> CYLINDER_OF = new HashMap<>();
	private static final Map<Item, Item> GAS_OF = new HashMap<>();
	private static double cylinderUnits = 1000;
	private static double tankUnits = 16000;

	private Fluids() {
	}

	/** Registers the empty containers; call before the elements and substances that use them. */
	static void registerContainers() {
		JsonObject root = ElementTable.readObject("/periodic_elements/fluids.json");
		root.getAsJsonArray("liquids").forEach(e -> LIQUIDS.add(e.getAsString()));
		root.getAsJsonArray("gases").forEach(e -> GASES.add(e.getAsString()));
		cylinderUnits = root.get("cylinderUnits").getAsDouble();
		tankUnits = root.get("tankUnits").getAsDouble();
		GLASS_FLASK = Registry.register(Registries.ITEM, id(root.get("flask").getAsString()),
				new ModContent.TippedItem(new Item.Settings()));
		STEEL_CYLINDER = Registry.register(Registries.ITEM, id(root.get("cylinder").getAsString()),
				new ModContent.TippedItem(new Item.Settings().maxCount(16)));
	}

	/** Item settings for an element or substance of this id: liquids and gases come in a flask. */
	public static Item.Settings settings(String chemId) {
		Item.Settings s = new Item.Settings();
		return isFluid(chemId) ? s.recipeRemainder(GLASS_FLASK) : s;
	}

	/** Registers one cylinder item per gas; call after the elements and substances. */
	static void registerCylinders() {
		for (String gas : GASES) {
			Item gasItem = Registries.ITEM.get(id(gas));
			Item cyl = Registry.register(Registries.ITEM, id(gas + "_cylinder"),
					new CylinderItem(new Item.Settings().maxCount(16).recipeRemainder(STEEL_CYLINDER)));
			CYLINDER_OF.put(gasItem, cyl);
			GAS_OF.put(cyl, gasItem);
		}
	}

	public static boolean isFluid(String chemId) {
		return LIQUIDS.contains(chemId) || GASES.contains(chemId);
	}

	public static boolean isGas(String chemId) {
		return GASES.contains(chemId);
	}

	/** Is this item a liquid or gas that comes in a Glass Flask? */
	public static boolean inFlask(Item item) {
		Identifier key = Registries.ITEM.getId(item);
		return key.getNamespace().equals(PeriodicElements.MOD_ID) && isFluid(key.getPath());
	}

	/** The cylinder item for a gas item, or null if it is not a gas. */
	@Nullable
	public static Item cylinderOf(Item gas) {
		return CYLINDER_OF.get(gas);
	}

	/** The gas (flask) item a cylinder item holds, or null if it is not a cylinder. */
	@Nullable
	public static Item gasOf(Item cylinder) {
		return GAS_OF.get(cylinder);
	}

	public static boolean isCylinder(Item item) {
		return GAS_OF.containsKey(item);
	}

	public static double cylinderUnits() {
		return cylinderUnits;
	}

	public static double tankUnits() {
		return tankUnits;
	}

	/** Is this an empty container (flask or cylinder)? */
	public static boolean isEmptyContainer(ItemStack stack) {
		return stack.isOf(GLASS_FLASK) || stack.isOf(STEEL_CYLINDER);
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** A cylinder of compressed gas. */
	public static class CylinderItem extends Item {
		CylinderItem(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			tooltip.add(Text.translatable("tooltip." + PeriodicElements.MOD_ID + ".in_cylinder",
					String.valueOf((int) cylinderUnits)).formatted(Formatting.GRAY));
		}
	}
}
