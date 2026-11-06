package com.periodicelements.logistics;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Moving and keeping items (3.40.0): conveyor belts in three tiers, two large hoppers to keep up with them, a Belt
 * Sorter that turns chosen items aside or down into a hopper, and three larger chests. Assets from tools/logistics.py.
 */
public final class Logistics {
	/** A belt tier: it moves its load one block every {@code interval} ticks and carries up to {@code capacity} items. */
	public record BeltTier(String id, int interval, int capacity, String project, double animSpeed) {
		public double blocksPerSecond() {
			return 20.0 / interval;
		}
	}

	/** A hopper tier: {@code slots} places, moves up to {@code perCycle} items in and out every {@code period} ticks. */
	public record HopperTier(String id, int slots, int perCycle, int period, String project) {
	}

	/** A chest tier: {@code columns} by six rows. */
	public record ChestTier(String id, int columns) {
		public int slots() {
			return columns * 6;
		}
	}

	public static final BeltTier CANVAS = new BeltTier("canvas_belt", 10, 8, "automatic_mill", 0.1);
	public static final BeltTier RUBBER = new BeltTier("rubber_belt", 5, 16, "belt_conveyor", 0.2);
	public static final BeltTier ROLLER = new BeltTier("roller_conveyor", 2, 32, "assembly_line", 0.5);
	public static final List<BeltTier> BELT_TIERS = List.of(CANVAS, RUBBER, ROLLER);

	public static final HopperTier STEEL_HOPPER = new HopperTier("steel_hopper", 9, 16, 4, "belt_conveyor");
	public static final HopperTier STAINLESS_HOPPER = new HopperTier("stainless_hopper", 18, 64, 4, "assembly_line");
	public static final List<HopperTier> HOPPER_TIERS = List.of(STEEL_HOPPER, STAINLESS_HOPPER);

	public static final ChestTier IRON_CHEST = new ChestTier("iron_chest", 9);
	public static final ChestTier STEEL_CHEST = new ChestTier("steel_chest", 13);
	public static final ChestTier STAINLESS_CHEST = new ChestTier("stainless_chest", 18);
	public static final List<ChestTier> CHEST_TIERS = List.of(IRON_CHEST, STEEL_CHEST, STAINLESS_CHEST);

	public static final String SORTER_PROJECT = "belt_conveyor";
	/** How many items a sorter holds, and how often (ticks) it passes them on. */
	public static final int SORTER_CAPACITY = 64;
	public static final int SORTER_INTERVAL = 2;

	public static final List<BeltBlock> BELTS = new ArrayList<>();
	public static final List<BigHopperBlock> HOPPERS = new ArrayList<>();
	public static final List<CrateBlock> CHESTS = new ArrayList<>();
	public static Block SORTER;

	public static BlockEntityType<BeltBlockEntity> BELT_ENTITY;
	public static BlockEntityType<BigHopperBlockEntity> HOPPER_ENTITY;
	public static BlockEntityType<CrateBlockEntity> CHEST_ENTITY;
	public static BlockEntityType<SorterBlockEntity> SORTER_ENTITY;
	public static ScreenHandlerType<CrateScreenHandler> CHEST_SCREEN;
	public static ScreenHandlerType<SorterScreenHandler> SORTER_SCREEN;

	private Logistics() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	private static <B extends Block> B gated(String name, B block, String project, List<Item> items) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new GatedBlockItem(block, new Item.Settings(),
				known -> Research.allows(known, "item:" + name), Research.get(project).name())));
		return block;
	}

	/** A block item that needs no research, with its description line. */
	private static <B extends Block> B plain(String name, B block, List<Item> items) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new BlockItem(block, new Item.Settings()) {
			@Override
			public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
				tooltip.add(Text.translatable(getBlock().getTranslationKey() + ".desc").formatted(Formatting.GRAY));
				super.appendTooltip(stack, context, tooltip, type);
			}
		}));
		return block;
	}

	public static void register(List<Item> items) {
		for (BeltTier t : BELT_TIERS) {
			BlockSoundGroup sound = t == CANVAS ? BlockSoundGroup.WOOL : BlockSoundGroup.METAL;
			BELTS.add(gated(t.id(), new BeltBlock(AbstractBlock.Settings.create().mapColor(t == ROLLER ? MapColor.IRON_GRAY
					: t == RUBBER ? MapColor.BLACK : MapColor.BROWN).strength(1.0f).sounds(sound).nonOpaque(), t), t.project(), items));
		}
		SORTER = gated("belt_sorter", new SorterBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(2.5f).sounds(BlockSoundGroup.METAL)), SORTER_PROJECT, items);
		for (HopperTier t : HOPPER_TIERS) {
			HOPPERS.add(gated(t.id(), new BigHopperBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY)
					.requiresTool().strength(3.0f, 4.8f).sounds(BlockSoundGroup.METAL).nonOpaque(), t), t.project(), items));
		}
		for (ChestTier t : CHEST_TIERS) {
			CHESTS.add(plain(t.id(), new CrateBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
					.strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL), t), items));
		}
		BELT_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("belt"),
				BlockEntityType.Builder.create(BeltBlockEntity::new, BELTS.toArray(Block[]::new)).build(null));
		HOPPER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("big_hopper"),
				BlockEntityType.Builder.create(BigHopperBlockEntity::new, HOPPERS.toArray(Block[]::new)).build(null));
		CHEST_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("big_chest"),
				BlockEntityType.Builder.create(CrateBlockEntity::new, CHESTS.toArray(Block[]::new)).build(null));
		SORTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("belt_sorter"),
				BlockEntityType.Builder.create(SorterBlockEntity::new, SORTER).build(null));
		CHEST_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("big_chest"),
				new ExtendedScreenHandlerType<>(CrateScreenHandler::new, PacketCodecs.VAR_INT.cast()));
		SORTER_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("belt_sorter"),
				new ScreenHandlerType<>(SorterScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES));
	}
}
