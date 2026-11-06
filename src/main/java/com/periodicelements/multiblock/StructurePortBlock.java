package com.periodicelements.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

/**
 * A Structure Port in the shell of a multiblock: hoppers and other machines reach the controller's inventory through
 * it, by the same sides as the machine itself (top: input; bottom: products; sides: fuel, reagent, flasks, cylinders).
 */
public class StructurePortBlock extends AnimatedPartBlock implements InventoryProvider {
	public StructurePortBlock(Settings settings) {
		super(settings);
	}

	@Override
	public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
		BlockPos controller = Structures.controllerOf(world, pos);
		return controller != null && world.getBlockEntity(controller) instanceof SidedInventory inv ? inv : null;
	}
}
