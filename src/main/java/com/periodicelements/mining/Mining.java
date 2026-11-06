package com.periodicelements.mining;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Mining tools (2.1.0, the approved mining tools plan): the Blasting Charge (L2), Pneumatic Rock Drill (L4), Electric
 * Rotary Hammer and Chainsaw (L5), Heavy Mining Drill (L6), four drill bits, the Lead-Acid and Lithium-Ion packs and the
 * Charging Bench. Sledgehammers and Excavators are gear (Gear.java); the Air Compressor is a machine. Assets come from
 * tools/mining.py.
 */
public final class Mining {
	public static Block CHARGING_BENCH;
	public static Block BLASTING_CHARGE;
	public static BlockEntityType<ChargingBenchBlockEntity> BENCH_ENTITY;
	public static Item PNEUMATIC_ROCK_DRILL;
	public static Item ROTARY_HAMMER;
	public static Item HEAVY_MINING_DRILL;
	public static Item CHAINSAW;
	public static Item TUNGSTEN_STEEL_BIT;
	public static Item HIGH_SPEED_STEEL_BIT;
	public static Item CEMENTED_CARBIDE_BIT;
	public static Item SYNTHETIC_DIAMOND_BIT;
	public static Item LEAD_ACID_PACK;
	public static Item LI_ION_PACK;

	private Mining() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	private static Block gated(String name, Block block, String project, List<Item> items) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new GatedBlockItem(block, new Item.Settings(),
				known -> Research.allows(known, "item:" + name), Research.get(project).name())));
		return block;
	}

	private static Item item(String name, Item item, List<Item> items) {
		items.add(Registry.register(Registries.ITEM, id(name), item));
		return item;
	}

	private static Item drill(String name, DrillItem.Kind kind, List<Item> items) {
		Item.Settings s = new Item.Settings();
		if (kind == DrillItem.Kind.CHAINSAW) {
			s = s.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, DrillItem.modeModifiers(DrillItem.Mode.TREE));
		}
		return item(name, new DrillItem(s, kind, name), items);
	}

	public static void register(List<Item> items) {
		BLASTING_CHARGE = gated("blasting_charge", new BlastingChargeBlock(AbstractBlock.Settings.create().mapColor(MapColor.BROWN)
				.strength(0.5f).sounds(BlockSoundGroup.GRASS)), "rock_blasting", items);
		CHARGING_BENCH = gated("charging_bench", new ChargingBenchBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(2.5f).requiresTool().sounds(BlockSoundGroup.METAL)), "battery_pack", items);
		BENCH_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("charging_bench"),
				BlockEntityType.Builder.create(ChargingBenchBlockEntity::new, CHARGING_BENCH).build(null));

		PNEUMATIC_ROCK_DRILL = drill("pneumatic_rock_drill", DrillItem.Kind.PNEUMATIC, items);
		ROTARY_HAMMER = drill("rotary_hammer", DrillItem.Kind.ROTARY, items);
		HEAVY_MINING_DRILL = drill("heavy_mining_drill", DrillItem.Kind.HEAVY, items);
		CHAINSAW = drill("chainsaw", DrillItem.Kind.CHAINSAW, items);
		TUNGSTEN_STEEL_BIT = item("tungsten_steel_bit", new DrillBitItem(new Item.Settings().maxDamage(1300), 8.5f,
				BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 10), items);
		HIGH_SPEED_STEEL_BIT = item("high_speed_steel_bit", new DrillBitItem(new Item.Settings().maxDamage(1600), 10f,
				BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 10), items);
		CEMENTED_CARBIDE_BIT = item("cemented_carbide_bit", new DrillBitItem(new Item.Settings().maxDamage(1200), 12f,
				BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 8), items);
		SYNTHETIC_DIAMOND_BIT = item("synthetic_diamond_bit", new DrillBitItem(new Item.Settings().maxDamage(2500), 13f,
				BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 10), items);
		LEAD_ACID_PACK = item("lead_acid_pack", new BatteryPackItem(new Item.Settings(), 1500, false), items);
		LI_ION_PACK = item("li_ion_pack", new BatteryPackItem(new Item.Settings(), 6000, true), items);
		AreaMining.register();
	}
}
