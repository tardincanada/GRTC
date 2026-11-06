package com.periodicelements.machine;

import com.periodicelements.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class MachineScreenHandler extends ScreenHandler {
	public static final int PLAYER_INV_Y = 140;
	/** Item x of the side panel's slots (to the right of the main panel). */
	public static final int SIDE_X = 184;

	private final Inventory inventory;
	private final PropertyDelegate properties;

	/** Client side. */
	public MachineScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(MachineBlockEntity.SIZE) {
			@Override
			public boolean isValid(int slot, ItemStack stack) {
				return switch (slot) {
					case MachineBlockEntity.FLASKS -> stack.isOf(com.periodicelements.Fluids.GLASS_FLASK);
					case MachineBlockEntity.CYLINDERS -> stack.isOf(com.periodicelements.Fluids.STEEL_CYLINDER);
					case MachineBlockEntity.UPGRADE_FIRST, MachineBlockEntity.UPGRADE_LAST -> Upgrades.isUpgrade(stack);
					default -> slot <= MachineBlockEntity.FUEL || slot == MachineBlockEntity.REAGENT;
				};
			}
		}, new ArrayPropertyDelegate(MachineBlockEntity.P_COUNT));
	}

	public MachineScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
		super(ModContent.MACHINE_SCREEN, syncId);
		checkSize(inventory, MachineBlockEntity.SIZE);
		this.inventory = inventory;
		this.properties = properties;
		inventory.onOpen(playerInventory.player);

		addSlot(new FilteredSlot(inventory, MachineBlockEntity.INPUT, 8, 18));
		addSlot(new FilteredSlot(inventory, MachineBlockEntity.FUEL, 8, 54));
		for (int i = 0; i < 3; i++) {
			addSlot(new OutputSlot(inventory, MachineBlockEntity.PRODUCT_FIRST + i, 116 + i * 18, 18));
		}
		addSlot(new OutputSlot(inventory, MachineBlockEntity.RESIDUE, 152, 54));
		addSlot(new ReagentSlot(inventory, MachineBlockEntity.REAGENT, 116, 40));
		// Side panel: empty containers, then the upgrade slots.
		addSlot(new FilteredSlot(inventory, MachineBlockEntity.FLASKS, SIDE_X, 18));
		addSlot(new FilteredSlot(inventory, MachineBlockEntity.CYLINDERS, SIDE_X, 36));
		addSlot(new UpgradeSlot(inventory, MachineBlockEntity.UPGRADE_FIRST, SIDE_X, 62));
		addSlot(new UpgradeSlot(inventory, MachineBlockEntity.UPGRADE_LAST, SIDE_X, 80));

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, PLAYER_INV_Y + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(playerInventory, col, 8 + col * 18, PLAYER_INV_Y + 58));
		}
		addProperties(properties);
	}

	public int property(int index) {
		return properties.get(index);
	}

	/** 3.29.0: the machine screen's relief valve button. */
	public static final int TOGGLE_VENT = 0;

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == TOGGLE_VENT && inventory instanceof MachineBlockEntity be) {
			be.setVenting(!be.venting());
			return true;
		}
		return false;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasStack()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getStack();
		ItemStack original = stack.copy();
		int machineSlots = MachineBlockEntity.SIZE;
		if (index < machineSlots) {
			if (!insertItem(stack, machineSlots, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else {
			boolean moved = false;
			if (slots.get(MachineBlockEntity.INPUT).canInsert(stack)) {
				moved = insertItem(stack, MachineBlockEntity.INPUT, MachineBlockEntity.INPUT + 1, false);
			}
			if (!moved && slots.get(MachineBlockEntity.FUEL).canInsert(stack)) {
				moved = insertItem(stack, MachineBlockEntity.FUEL, MachineBlockEntity.FUEL + 1, false);
			}
			if (!moved && slots.get(MachineBlockEntity.REAGENT).canInsert(stack) && slots.get(MachineBlockEntity.REAGENT).isEnabled()) {
				moved = insertItem(stack, MachineBlockEntity.REAGENT, MachineBlockEntity.REAGENT + 1, false);
			}
			for (int i = MachineBlockEntity.FLASKS; i <= MachineBlockEntity.UPGRADE_LAST && !moved; i++) {
				if (slots.get(i).canInsert(stack)) {
					moved = insertItem(stack, i, i + 1, false);
				}
			}
			if (!moved) {
				return ItemStack.EMPTY;
			}
		}
		if (stack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}
		return original;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		inventory.onClose(player);
	}

	private static class FilteredSlot extends Slot {
		FilteredSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return inventory.isValid(getIndex(), stack);
		}
	}

	/** Shown only in machines that have reactions with a second ingredient. */
	private class ReagentSlot extends FilteredSlot {
		ReagentSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean isEnabled() {
			Reactions.MachineSpec spec = Reactions.machine(property(MachineBlockEntity.P_MACHINE));
			return spec != null && Reactions.forMachine(spec.id()).stream().anyMatch(r -> !r.reagents().isEmpty());
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return isEnabled() && super.canInsert(stack);
		}
	}

	private static class UpgradeSlot extends FilteredSlot {
		UpgradeSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}
	}

	private static class OutputSlot extends Slot {
		OutputSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}
	}
}
