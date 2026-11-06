package com.periodicelements.logistics;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Moves many items at once into an inventory (3.40.0). Vanilla's hopper transfer moves one item and ignores an
 * inventory's own stack limit, which belts rely on to hold only their capacity, so belts, sorters and the large
 * hoppers use this instead.
 */
public final class Transfer {
	private Transfer() {
	}

	/** The inventory at {@code target} (a chest, barrel, hopper, machine, belt...), or null. */
	public static Inventory at(ServerWorld world, BlockPos target) {
		return HopperBlockEntity.getInventoryAt(world, target);
	}

	/**
	 * Puts as much of {@code stack} as fits into {@code to}, entering by {@code side} (the face of {@code to} the items
	 * come through). Shrinks {@code stack}; returns how many went in.
	 */
	public static int insert(Inventory to, ItemStack stack, Direction side) {
		if (stack.isEmpty()) {
			return 0;
		}
		int before = stack.getCount();
		int[] slots = to instanceof SidedInventory sided ? sided.getAvailableSlots(side) : null;
		int n = slots != null ? slots.length : to.size();
		// Top up stacks of the same item first, then empty places.
		for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
			for (int k = 0; k < n && !stack.isEmpty(); k++) {
				int slot = slots != null ? slots[k] : k;
				if (!to.isValid(slot, stack) || to instanceof SidedInventory sided && !sided.canInsert(slot, stack, side)) {
					continue;
				}
				ItemStack in = to.getStack(slot);
				int limit = Math.min(to.getMaxCount(stack), stack.getMaxCount());
				if (pass == 0 && !in.isEmpty() && ItemStack.areItemsAndComponentsEqual(in, stack) && in.getCount() < limit) {
					int move = Math.min(stack.getCount(), limit - in.getCount());
					in.increment(move);
					stack.decrement(move);
				} else if (pass == 1 && in.isEmpty()) {
					int move = Math.min(stack.getCount(), limit);
					to.setStack(slot, stack.split(move));
				}
			}
		}
		int moved = before - stack.getCount();
		if (moved > 0) {
			to.markDirty();
		}
		return moved;
	}

	/** Moves up to {@code max} items of {@code stack} into the inventory at {@code target}; returns how many moved. */
	public static int push(ServerWorld world, ItemStack stack, int max, BlockPos target, Direction towards) {
		Inventory to = at(world, target);
		if (to == null || stack.isEmpty()) {
			return 0;
		}
		ItemStack part = stack.copyWithCount(Math.min(max, stack.getCount()));
		int moved = insert(to, part, towards.getOpposite());
		stack.decrement(moved);
		return moved;
	}
}
