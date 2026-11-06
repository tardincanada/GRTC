package com.periodicelements.logistics;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * The Belt Sorter's screen (3.40.1): its nine filter places in the dispenser's grid, and the mode, "required" (items
 * like the filter's are turned aside) or "forbidden" (items like the filter's go straight on, all others are turned
 * aside). Button 0 switches the mode.
 */
public class SorterScreenHandler extends ScreenHandler {
	private final Inventory filter;
	private final PropertyDelegate mode;
	private final SorterBlockEntity sorter;

	/** Client side. */
	public SorterScreenHandler(int syncId, PlayerInventory playerInv) {
		this(syncId, playerInv, new SimpleInventory(9) {
			@Override
			public int getMaxCountPerStack() {
				return 1;
			}
		}, new ArrayPropertyDelegate(1), null);
	}

	public SorterScreenHandler(int syncId, PlayerInventory playerInv, Inventory filter, PropertyDelegate mode, SorterBlockEntity sorter) {
		super(Logistics.SORTER_SCREEN, syncId);
		this.filter = filter;
		this.mode = mode;
		this.sorter = sorter;
		checkSize(filter, 9);
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				addSlot(new Slot(filter, c + r * 3, 62 + c * 18, 17 + r * 18));
			}
		}
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 9; c++) {
				addSlot(new Slot(playerInv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
			}
		}
		for (int c = 0; c < 9; c++) {
			addSlot(new Slot(playerInv, c, 8 + c * 18, 142));
		}
		addProperties(mode);
	}

	/** True in "forbidden" mode. */
	public boolean forbidden() {
		return mode.get(0) != 0;
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == 0 && sorter != null) {
			sorter.setForbidden(!sorter.forbidden());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack moved = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot.hasStack()) {
			ItemStack stack = slot.getStack();
			moved = stack.copy();
			if (index < 9) {
				if (!insertItem(stack, 9, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!insertItem(stack, 0, 9, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}
		return moved;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return sorter == null || Inventory.canPlayerUse(sorter, player);
	}
}
