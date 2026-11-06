package com.periodicelements.logistics;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/** An open larger chest (3.40.0): six rows as wide as the tier, the player's pack centred below. */
public class CrateScreenHandler extends ScreenHandler {
	public static final int ROWS = 6;
	private final Inventory inventory;
	private final int columns;

	/** Client side: the chest's width comes from the server. */
	public CrateScreenHandler(int syncId, PlayerInventory playerInv, Integer columns) {
		this(syncId, playerInv, new SimpleInventory(columns * ROWS), columns);
	}

	public CrateScreenHandler(int syncId, PlayerInventory playerInv, Inventory inventory, int columns) {
		super(Logistics.CHEST_SCREEN, syncId);
		this.inventory = inventory;
		this.columns = columns;
		checkSize(inventory, columns * ROWS);
		inventory.onOpen(playerInv.player);
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < columns; c++) {
				addSlot(new Slot(inventory, c + r * columns, 8 + c * 18, 18 + r * 18));
			}
		}
		int px = (width(columns) - 176) / 2 + 8;
		int py = 18 + ROWS * 18 + 13;
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 9; c++) {
				addSlot(new Slot(playerInv, c + r * 9 + 9, px + c * 18, py + r * 18));
			}
		}
		for (int c = 0; c < 9; c++) {
			addSlot(new Slot(playerInv, c, px + c * 18, py + 58));
		}
	}

	/** Screen width for a chest this many columns wide. */
	public static int width(int columns) {
		return Math.max(176, columns * 18 + 14);
	}

	public int columns() {
		return columns;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack moved = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot.hasStack()) {
			ItemStack stack = slot.getStack();
			moved = stack.copy();
			int chest = columns * ROWS;
			if (index < chest) {
				if (!insertItem(stack, chest, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!insertItem(stack, 0, chest, false)) {
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
		return inventory.canPlayerUse(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		inventory.onClose(player);
	}
}
