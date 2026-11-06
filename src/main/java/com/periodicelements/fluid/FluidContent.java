package com.periodicelements.fluid;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.DoubleSupplier;

/**
 * 3.38.0: tanks and pipes. The multiblock tank (Tank Plate, Tank Controller, Tank Valve, Tank Gauge, Tank Compressor),
 * Fluid Pipe and Fluid Pump. Gas Mains (level 3) unlocks them; the Tank Compressor needs Gas Compression (level 4).
 * Assets, recipes and text come from tools/tanks.py.
 */
public final class FluidContent {
	public static Block TANK_WALL;
	public static Block TANK_CONTROLLER;
	public static Block TANK_VALVE;
	public static Block TANK_GAUGE;
	public static Block TANK_COMPRESSOR;
	public static Block FLUID_PIPE;
	public static Block FLUID_PUMP;
	public static BlockEntityType<TankControllerBlockEntity> CONTROLLER_ENTITY;
	public static BlockEntityType<TankGaugeBlockEntity> GAUGE_ENTITY;
	public static BlockEntityType<FluidPumpBlock.Entity> PUMP_ENTITY;

	private FluidContent() {
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

	private static AbstractBlock.Settings metal() {
		return AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL);
	}

	public static void register(List<Item> items) {
		TANK_WALL = gated("tank_wall", new TankBlocks.Part(metal()), "gas_mains", items);
		TANK_CONTROLLER = gated("tank_controller", new TankBlocks.Controller(metal()), "gas_mains", items);
		TANK_VALVE = gated("tank_valve", new TankBlocks.Valve(metal()), "gas_mains", items);
		TANK_GAUGE = gated("tank_gauge", new TankBlocks.Gauge(metal()), "gas_mains", items);
		TANK_COMPRESSOR = gated("tank_compressor", new TankBlocks.Part(metal()), "gas_compression", items);
		FLUID_PIPE = gated("fluid_pipe", new FluidPipeBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(1.5f, 6.0f).requiresTool().nonOpaque().sounds(BlockSoundGroup.METAL)), "gas_mains", items);
		FLUID_PUMP = gated("fluid_pump", new FluidPumpBlock(metal()), "gas_mains", items);
		CONTROLLER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("tank_controller"),
				BlockEntityType.Builder.create(TankControllerBlockEntity::new, TANK_CONTROLLER).build(null));
		GAUGE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("tank_gauge"),
				BlockEntityType.Builder.create(TankGaugeBlockEntity::new, TANK_GAUGE).build(null));
		PUMP_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("fluid_pump"),
				BlockEntityType.Builder.create(FluidPumpBlock.Entity::new, FLUID_PUMP).build(null));
	}

	/**
	 * The relief valve of a tank (sneak-use with an empty hand): a gas goes into the air with a hiss; a liquid stays.
	 * {@code vent} empties the tank and returns the units let out.
	 */
	public static void vent(PlayerEntity player, @Nullable Item fluid, DoubleSupplier vent) {
		String k = "gui.periodic_elements.big_tank.";
		if (fluid == null) {
			player.sendMessage(Text.translatable(k + "vent_empty"), true);
			return;
		}
		if (!FluidHolder.isGas(fluid)) {
			player.sendMessage(Text.translatable(k + "vent_liquid", Text.translatable(fluid.getTranslationKey())), true);
			return;
		}
		double out = vent.getAsDouble();
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.6f, 1.6f);
		player.sendMessage(Text.translatable(k + "vented", String.format(Locale.ROOT, "%,.0f", out),
				Text.translatable(fluid.getTranslationKey())), true);
	}
}
