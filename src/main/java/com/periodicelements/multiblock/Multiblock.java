package com.periodicelements.multiblock;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Multiblock structures (3.3.0, stage 1 of the multiblock plan): the shell blocks and the Structure Port. A machine
 * placed in the wall of a valid structure becomes its controller; see {@link Structures}.
 */
public final class Multiblock {
	public static Block REFRACTORY_BRICK;
	public static Block TUYERE;
	public static Block SILICA_BRICK;
	public static Block STRUCTURE_PORT;
	/** 3.8.0: lead chamber and distillation column. */
	public static Block LEAD_SHEET;
	public static Block GAY_LUSSAC_TOWER;
	public static Block DISTILLATION_TRAY;
	/** 3.13.0: enrichment cascade and nuclear reactor. */
	public static Block DIFFUSER_STAGE;
	public static Block GRAPHITE_BLOCK;
	public static Block FUEL_CHANNEL;
	public static Block REACTOR_SHIELDING;

	/** Set by the client: shows (or hides) the smallest structure round a controller. Does nothing on a server. */
	public interface Outline {
		void toggle(Structures.Kind kind, net.minecraft.util.math.BlockPos pos, net.minecraft.util.math.Direction facing);
	}

	public static Outline outline = (kind, pos, facing) -> {
	};

	private Multiblock() {
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

	private static AbstractBlock.Settings brick(MapColor color) {
		return AbstractBlock.Settings.create().mapColor(color).strength(2.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.STONE);
	}

	public static void register(List<Item> items) {
		REFRACTORY_BRICK = gated("refractory_brick", new AnimatedPartBlock(brick(MapColor.PALE_YELLOW)), "blast_furnace", items);
		TUYERE = gated("tuyere", new AnimatedPartBlock(brick(MapColor.PALE_YELLOW)), "blast_furnace", items);
		STRUCTURE_PORT = gated("structure_port", new StructurePortBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(2.5f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "blast_furnace", items);
		SILICA_BRICK = gated("silica_brick", new AnimatedPartBlock(brick(MapColor.WHITE_GRAY)), "coking", items);
		LEAD_SHEET = gated("lead_sheet", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY)
				.strength(1.5f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "lead_chamber", items);
		GAY_LUSSAC_TOWER = gated("gay_lussac_tower", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY)
				.strength(1.5f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "lead_chamber", items);
		DISTILLATION_TRAY = gated("distillation_tray", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(3.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "oil_refining", items);
		DIFFUSER_STAGE = gated("diffuser_stage", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(3.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "isotope_separation", items);
		GRAPHITE_BLOCK = gated("graphite_block", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK)
				.strength(2.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.STONE)), "nuclear_reactor", items);
		FUEL_CHANNEL = gated("fuel_channel", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK)
				.strength(2.5f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "nuclear_reactor", items);
		REACTOR_SHIELDING = gated("reactor_shielding", new AnimatedPartBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(5.0f, 1200.0f).requiresTool().sounds(BlockSoundGroup.METAL)), "nuclear_reactor", items);
		// 3.8.0: sneak-use a controller with the Field Guide to see the smallest structure drawn round it.
		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (!player.isSneaking() || !(player.getStackInHand(hand).getItem() instanceof com.periodicelements.FieldGuideItem)) {
				return net.minecraft.util.ActionResult.PASS;
			}
			var state = world.getBlockState(hit.getBlockPos());
			String machine = state.getBlock() instanceof com.periodicelements.machine.MachineBlock mb ? mb.machineId()
					: state.getBlock() instanceof com.periodicelements.machine.PowerBlock pb ? pb.powerId() : null;
			Structures.Kind kind = machine == null ? null : Structures.forMachine(machine);
			if (kind == null) {
				return net.minecraft.util.ActionResult.PASS;
			}
			if (world.isClient) {
				outline.toggle(kind, hit.getBlockPos(), state.get(net.minecraft.state.property.Properties.HORIZONTAL_FACING));
			}
			return net.minecraft.util.ActionResult.SUCCESS;
		});
	}
}
