package com.periodicelements;

import com.periodicelements.machine.MachineBlock;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.MachineScreenHandler;
import com.periodicelements.machine.PowerBlock;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchStationBlock;
import com.periodicelements.research.ResearchStationBlockEntity;
import com.periodicelements.research.ResearchStationScreenHandler;
import com.periodicelements.research.TreatiseContents;
import com.periodicelements.research.TreatiseItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Research stations, machines, treatises and their supporting items. */
public final class ModContent {
	public static final List<Block> STATIONS = new ArrayList<>();
	public static final List<Block> MACHINES = new ArrayList<>();
	public static final List<Item> ITEMS = new ArrayList<>();

	public static ComponentType<TreatiseContents> TREATISE_CONTENTS;
	public static Item TREATISE_MANUSCRIPT;
	public static Item TREATISE_PRINTED;
	public static Item SEAWATER_BUCKET;
	public static Item LEAVENED_BREAD;
	public static Item MAGNIFYING_LENS;
	public static Item ELECTROSCOPE;
	public static Item GEIGER_COUNTER;
	public static Block COPPER_WIRE;
	public static Block INSULATED_CABLE;
	public static Block ASPHALT;
	public static BlockEntityType<MachineBlockEntity> MACHINE_ENTITY;
	public static BlockEntityType<PowerBlockEntity> POWER_ENTITY;
	public static final List<Block> POWER_BLOCKS = new ArrayList<>();
	public static BlockEntityType<ResearchStationBlockEntity> STATION_ENTITY;
	public static BlockEntityType<com.periodicelements.machine.StorageTankBlockEntity> TANK_ENTITY;
	public static Block STORAGE_TANK;
	public static ScreenHandlerType<MachineScreenHandler> MACHINE_SCREEN;
	public static ScreenHandlerType<ResearchStationScreenHandler> STATION_SCREEN;

	private static final String[][] STATION_DEFS = {{"potters_workshop", "1"}, {"alchemists_laboratory", "2"},
			{"natural_philosophers_laboratory", "3"}, {"industrial_laboratory", "4"},
			{"university_laboratory", "5"}, {"national_laboratory", "6"}, {"international_research_centre", "7"}};

	private ModContent() {
	}

	/** An item whose tooltip is its ".tip" lang line. */
	public static class TippedItem extends Item {
		public TippedItem(Settings settings) {
			super(settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		}
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	static void register() {
		TREATISE_CONTENTS = Registry.register(Registries.DATA_COMPONENT_TYPE, id("treatise"),
				ComponentType.<TreatiseContents>builder().codec(TreatiseContents.CODEC)
						.packetCodec(TreatiseContents.PACKET_CODEC).cache().build());

		for (String[] def : STATION_DEFS) {
			int level = Integer.parseInt(def[1]);
			Block block = Registry.register(Registries.BLOCK, id(def[0]), new ResearchStationBlock(level,
					AbstractBlock.Settings.create().mapColor(MapColor.BROWN).strength(2.5f).sounds(BlockSoundGroup.WOOD)));
			Text req = level <= 1 ? Text.translatable("tooltip.periodic_elements.requires.nothing")
					: Text.translatable("tooltip.periodic_elements.requires.level", level - 1,
					Text.translatable("level.periodic_elements." + (level - 1)));
			ITEMS.add(Registry.register(Registries.ITEM, id(def[0]), new GatedBlockItem(block, new Item.Settings(),
					known -> Research.techLevel(known) >= level - 1, req)));
			STATIONS.add(block);
		}

		for (Reactions.MachineSpec spec : Reactions.machines()) {
			boolean pit = spec.id().equals("charcoal_pit");
			Block block = Registry.register(Registries.BLOCK, id(spec.id()), new MachineBlock(spec.id(),
					AbstractBlock.Settings.create().mapColor(pit ? MapColor.DIRT_BROWN : MapColor.STONE_GRAY)
							.strength(pit ? 1.0f : 3.0f).requiresTool()
							.sounds(pit ? BlockSoundGroup.GRAVEL : BlockSoundGroup.STONE)
							.luminance(state -> state.get(Properties.LIT) ? 13 : 0)));
			Text req = spec.project() == null ? Text.translatable("tooltip.periodic_elements.requires.nothing")
					: Research.get(spec.project()).name();
			String unlock = "machine:" + spec.id();
			ITEMS.add(Registry.register(Registries.ITEM, id(spec.id()), new GatedBlockItem(block, new Item.Settings(),
					known -> Research.allows(known, unlock), req)));
			MACHINES.add(block);
		}

		for (Reactions.PowerSpec spec : Reactions.power()) {
			AbstractBlock.Settings powerSettings = AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3.0f).requiresTool()
					.sounds(BlockSoundGroup.METAL).luminance(state -> state.get(Properties.LIT) ? 7 : 0);
			// 3.41.0: the battery shows its charge (BatteryBlock) instead of running/stopped.
			Block block = Registry.register(Registries.BLOCK, id(spec.id()), spec.kind().equals("battery")
					? new com.periodicelements.machine.BatteryBlock(spec.id(), powerSettings)
					: new PowerBlock(spec.id(), powerSettings));
			String unlock = "machine:" + spec.id();
			ITEMS.add(Registry.register(Registries.ITEM, id(spec.id()), new GatedBlockItem(block, new Item.Settings(),
					known -> Research.allows(known, unlock), Research.get(spec.project()).name())));
			POWER_BLOCKS.add(block);
		}

		COPPER_WIRE = Registry.register(Registries.BLOCK, id("copper_wire"), new com.periodicelements.machine.CopperWireBlock(
				AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).strength(1.0f).nonOpaque()
						.sounds(BlockSoundGroup.COPPER)));
		ITEMS.add(Registry.register(Registries.ITEM, id("copper_wire"), new GatedBlockItem(COPPER_WIRE, new Item.Settings(),
				known -> Research.allows(known, "item:copper_wire"), Research.get("dynamo").name())));

		INSULATED_CABLE = Registry.register(Registries.BLOCK, id("insulated_cable"), new com.periodicelements.machine.CopperWireBlock(
				AbstractBlock.Settings.create().mapColor(MapColor.GRAY).strength(1.0f).nonOpaque().sounds(BlockSoundGroup.WOOL)));
		ITEMS.add(Registry.register(Registries.ITEM, id("insulated_cable"), new GatedBlockItem(INSULATED_CABLE, new Item.Settings(),
				known -> Research.allows(known, "item:insulated_cable"), Research.get("pvc").name())));
		// Velocity multiplier 1.1 per tick: with ground friction that is about 25% faster walking, riding and running.
		ASPHALT = Registry.register(Registries.BLOCK, id("asphalt"), new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.BLACK).strength(1.5f, 6.0f).requiresTool().velocityMultiplier(1.1f).sounds(BlockSoundGroup.STONE)));
		ITEMS.add(Registry.register(Registries.ITEM, id("asphalt"), new GatedBlockItem(ASPHALT, new Item.Settings(),
				known -> Research.allows(known, "item:asphalt"), Research.get("oil_refining").name())));
		Block tritium = Registry.register(Registries.BLOCK, id("tritium_light"), new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.PALE_GREEN).strength(0.3f).sounds(BlockSoundGroup.GLASS).luminance(state -> 12)));
		ITEMS.add(Registry.register(Registries.ITEM, id("tritium_light"), new GatedBlockItem(tritium, new Item.Settings(),
				known -> Research.allows(known, "item:tritium_light"), Research.get("tritium").name())));
		STORAGE_TANK = Registry.register(Registries.BLOCK, id("storage_tank"), new com.periodicelements.machine.StorageTankBlock(
				AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(2.5f).requiresTool().sounds(BlockSoundGroup.METAL)));
		ITEMS.add(Registry.register(Registries.ITEM, id("storage_tank"), new GatedBlockItem(STORAGE_TANK, new Item.Settings(),
				known -> Research.allows(known, "item:storage_tank"), Research.get("pneumatic_chemistry").name())));
		ITEMS.add(Fluids.GLASS_FLASK);
		ITEMS.add(Fluids.STEEL_CYLINDER);
		ITEMS.add(Registry.register(Registries.ITEM, id("field_guide"), new FieldGuideItem(new Item.Settings().maxCount(1))));
		ITEMS.add(Registry.register(Registries.ITEM, id("parachute"), new ParachuteItem(new Item.Settings().maxCount(1))));

		Block smoke = Registry.register(Registries.BLOCK, id("smoke_detector"), new SmokeDetectorBlock(
				AbstractBlock.Settings.create().mapColor(MapColor.WHITE).strength(0.5f).nonOpaque().sounds(BlockSoundGroup.STONE)));
		ITEMS.add(Registry.register(Registries.ITEM, id("smoke_detector"), new GatedBlockItem(smoke, new Item.Settings(),
				known -> Research.allows(known, "item:smoke_detector"), Research.get("smoke_detector").name())));
		ITEMS.add(Registry.register(Registries.ITEM, id("fibre_communicator"), new ResearchedToolItem(
				new Item.Settings().maxCount(1), ResearchedToolItem.Kind.COMMUNICATOR, "optical_fibre")));
		ITEMS.add(Registry.register(Registries.ITEM, id("element_analyzer"), new ResearchedToolItem(
				new Item.Settings().maxCount(1), ResearchedToolItem.Kind.ANALYZER, "element_analyzer")));

		TREATISE_MANUSCRIPT = Registry.register(Registries.ITEM, id("treatise_manuscript"),
				new TreatiseItem(new Item.Settings().maxCount(1)));
		ITEMS.add(TREATISE_MANUSCRIPT);
		TREATISE_PRINTED = Registry.register(Registries.ITEM, id("treatise_printed"),
				new TreatiseItem(new Item.Settings().maxCount(1)));
		ITEMS.add(TREATISE_PRINTED);
		// 3.45.0: the seawater bucket holds the salt water fluid of the sea (see water.Water).
		com.periodicelements.water.Water.register(ITEMS);
		SEAWATER_BUCKET = com.periodicelements.water.Water.SALT_WATER_BUCKET;
		LEAVENED_BREAD = Registry.register(Registries.ITEM, id("leavened_bread"), new TippedItem(new Item.Settings()
				.food(new FoodComponent.Builder().nutrition(7).saturationModifier(0.7f).build())));
		ITEMS.add(LEAVENED_BREAD);
		MAGNIFYING_LENS = Registry.register(Registries.ITEM, id("magnifying_lens"),
				new TippedItem(new Item.Settings().maxCount(1)));
		ITEMS.add(MAGNIFYING_LENS);
		ELECTROSCOPE = Registry.register(Registries.ITEM, id("electroscope"),
				new com.periodicelements.radiation.ElectroscopeItem(new Item.Settings().maxCount(1), "electroscope", "radioactivity", 20));
		ITEMS.add(ELECTROSCOPE);
		GEIGER_COUNTER = Registry.register(Registries.ITEM, id("geiger_counter"),
				new com.periodicelements.radiation.ElectroscopeItem(new Item.Settings().maxCount(1), "geiger_counter", "geiger_counter", 0));
		ITEMS.add(GEIGER_COUNTER);
		ITEMS.add(Registry.register(Registries.ITEM, id("tungsten_carbide_arrow"),
				new PenetratorArrowItem(new Item.Settings(), 1.5, false)));
		ITEMS.add(Registry.register(Registries.ITEM, id("depleted_uranium_arrow"),
				new PenetratorArrowItem(new Item.Settings(), 1.75, true)));
		java.util.List<String> plainItems = new java.util.ArrayList<>();
		ElementTable.readObject("/periodic_elements/machines.json").getAsJsonArray("plainItems")
				.forEach(e -> plainItems.add(e.getAsString()));
		for (String plain : plainItems) {
			ITEMS.add(Registry.register(Registries.ITEM, id(plain), new TippedItem(new Item.Settings())));
		}

		OreContent.register(ITEMS);
		GeoRocks.register(ITEMS);
		GeoOres.register(ITEMS);
		Gear.register(ITEMS);
		com.periodicelements.farm.Farm.register(ITEMS);
		com.periodicelements.trees.Trees.register(ITEMS);
		com.periodicelements.shrubs.Shrubs.register(ITEMS);
		com.periodicelements.crops.Crops.register(ITEMS);
		com.periodicelements.mining.Mining.register(ITEMS);
		com.periodicelements.atlas.Atlas.register(ITEMS);
		com.periodicelements.multiblock.Multiblock.register(ITEMS);
		com.periodicelements.fluid.FluidContent.register(ITEMS);
		com.periodicelements.compendium.Compendium.register(ITEMS);
		com.periodicelements.research.ProgressBook.register(ITEMS);
		com.periodicelements.creative.CreativeItems.register();
		com.periodicelements.samplecase.SampleCase.register(ITEMS);
		com.periodicelements.logistics.Logistics.register(ITEMS);
		com.periodicelements.explore.Explore.register(ITEMS);
		com.periodicelements.fauna.Fauna.register(ITEMS);

		MACHINE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("machine"),
				BlockEntityType.Builder.create(MachineBlockEntity::new, MACHINES.toArray(Block[]::new)).build(null));
		POWER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("power"),
				BlockEntityType.Builder.create(PowerBlockEntity::new, POWER_BLOCKS.toArray(Block[]::new)).build(null));
		STATION_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("research_station"),
				BlockEntityType.Builder.create(ResearchStationBlockEntity::new, STATIONS.toArray(Block[]::new)).build(null));
		TANK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("storage_tank"),
				BlockEntityType.Builder.create(com.periodicelements.machine.StorageTankBlockEntity::new, STORAGE_TANK).build(null));
		MACHINE_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("machine"),
				new ScreenHandlerType<>(MachineScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
		STATION_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("research_station"),
				new ScreenHandlerType<>(ResearchStationScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	}
}
