package com.periodicelements.samplecase;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * The open case: 6 rows of 9 places that take only samples, over the player's pack. The case being used cannot be
 * moved while open (its place in the pack is locked), and every change is written back into it at once.
 */
public class SampleCaseScreenHandler extends ScreenHandler {
	private final PlayerInventory playerInventory;
	/** Where the open case is in the pack: 0-8 hotbar, or PlayerInventory.OFF_HAND_SLOT. */
	private final int caseSlot;
	private final SimpleInventory inventory;
	private int lockedSlotId = -1;

	public SampleCaseScreenHandler(int syncId, PlayerInventory inv, Integer caseSlot) {
		super(SampleCase.SCREEN, syncId);
		this.playerInventory = inv;
		this.caseSlot = caseSlot;
		this.inventory = new SimpleInventory(SampleCase.SLOTS) {
			@Override
			public boolean isValid(int slot, ItemStack stack) {
				return SampleCase.accepts(stack);
			}
		};
		ItemStack caseStack = inv.getStack(caseSlot);
		if (caseStack.isOf(SampleCase.CASE)) {
			var list = SampleCase.contents(caseStack);
			for (int i = 0; i < list.size(); i++) {
				inventory.setStack(i, list.get(i));
			}
		}
		inventory.addListener(i -> save());
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18) {
					@Override
					public boolean canInsert(ItemStack stack) {
						return SampleCase.accepts(stack);
					}
				});
			}
		}
		int y = 18 + 6 * 18 + 13;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, y + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			Slot s = addSlot(new Slot(inv, col, 8 + col * 18, y + 58) {
				@Override
				public boolean canTakeItems(PlayerEntity player) {
					return getIndex() != SampleCaseScreenHandler.this.caseSlot && super.canTakeItems(player);
				}
			});
			if (col == caseSlot) {
				lockedSlotId = s.id;
			}
		}
	}

	private void save() {
		ItemStack caseStack = playerInventory.getStack(caseSlot);
		if (caseStack.isOf(SampleCase.CASE)) {
			SampleCase.store(caseStack, inventory.getHeldStacks());
		}
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		// The open case stays put: no clicking it, no number-key swap into its hotbar place, no off-hand swap of it.
		if (slotIndex == lockedSlotId && lockedSlotId >= 0) {
			return;
		}
		if (actionType == SlotActionType.SWAP && (button == caseSlot || (button == 40 && caseSlot == PlayerInventory.OFF_HAND_SLOT))) {
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
		if (index < SampleCase.SLOTS) {
			if (!insertItem(stack, SampleCase.SLOTS, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!SampleCase.accepts(stack) || !insertItem(stack, 0, SampleCase.SLOTS, false)) {
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
		return playerInventory.getStack(caseSlot).isOf(SampleCase.CASE);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		save();
	}
}
