package com.periodicelements.explore;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * An open backpack: 3 or 6 rows of 9 places over the player's pack. The backpack being used cannot be moved while it
 * is open, and every change is written back into it at once.
 */
public class BackpackScreenHandler extends ScreenHandler {
	private final PlayerInventory playerInventory;
	/** Where the open backpack is: 0-35 in the pack, or PlayerInventory.OFF_HAND_SLOT. */
	private final int packSlot;
	private final int rows;
	private final SimpleInventory inventory;
	private int lockedSlotId = -1;

	public BackpackScreenHandler(int syncId, PlayerInventory inv, Integer packSlot) {
		super(Explore.BACKPACK_SCREEN, syncId);
		this.playerInventory = inv;
		this.packSlot = packSlot;
		ItemStack pack = inv.getStack(packSlot);
		this.rows = pack.getItem() instanceof BackpackItem b ? b.rows() : 3;
		this.inventory = new SimpleInventory(rows * 9) {
			@Override
			public boolean isValid(int slot, ItemStack stack) {
				return BackpackItem.accepts(stack);
			}
		};
		if (pack.getItem() instanceof BackpackItem) {
			var list = BackpackItem.contents(pack);
			for (int i = 0; i < list.size(); i++) {
				inventory.setStack(i, list.get(i));
			}
		}
		inventory.addListener(i -> save());
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18) {
					@Override
					public boolean canInsert(ItemStack stack) {
						return BackpackItem.accepts(stack);
					}
				});
			}
		}
		int y = 18 + rows * 18 + 13;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addLocked(new Slot(inv, col + row * 9 + 9, 8 + col * 18, y + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addLocked(new Slot(inv, col, 8 + col * 18, y + 58));
		}
	}

	public int rows() {
		return rows;
	}

	private void addLocked(Slot plain) {
		int index = plain.getIndex();
		Slot s = addSlot(new Slot(plain.inventory, index, plain.x, plain.y) {
			@Override
			public boolean canTakeItems(PlayerEntity player) {
				return getIndex() != packSlot && super.canTakeItems(player);
			}
		});
		if (index == packSlot) {
			lockedSlotId = s.id;
		}
	}

	private void save() {
		ItemStack pack = playerInventory.getStack(packSlot);
		if (pack.getItem() instanceof BackpackItem) {
			BackpackItem.store(pack, inventory.getHeldStacks());
		}
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		if (slotIndex == lockedSlotId && lockedSlotId >= 0) {
			return;
		}
		if (actionType == SlotActionType.SWAP && (button == packSlot || (button == 40 && packSlot == PlayerInventory.OFF_HAND_SLOT))) {
			return;
		}
		super.onSlotClick(slotIndex, button, actionType, player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasStack() || index == lockedSlotId) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getStack();
		ItemStack copy = stack.copy();
		int n = rows * 9;
		if (index < n) {
			if (!insertItem(stack, n, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!BackpackItem.accepts(stack) || !insertItem(stack, 0, n, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}
		return copy;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return playerInventory.getStack(packSlot).getItem() instanceof BackpackItem;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		save();
	}
}
