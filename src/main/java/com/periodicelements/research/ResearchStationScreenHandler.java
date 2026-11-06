package com.periodicelements.research;

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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * Research station menu. Buttons go through vanilla's button-click packet: id N starts project N,
 * {@link #WRITE_TREATISE} writes a treatise.
 */
public class ResearchStationScreenHandler extends ScreenHandler {
	public static final int WRITE_TREATISE = 10_000;
	public static final int SLOTS_X = 8, SLOTS_Y = 18;
	public static final int PLAYER_INV_Y = 140;

	private final Inventory inventory;
	private final PropertyDelegate properties;
	@Nullable
	private final ResearchStationBlockEntity station;

	public ResearchStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(ResearchStationBlockEntity.SIZE),
				new ArrayPropertyDelegate(ResearchStationBlockEntity.P_COUNT), null);
	}

	public ResearchStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
										PropertyDelegate properties, @Nullable ResearchStationBlockEntity station) {
		super(ModContent.STATION_SCREEN, syncId);
		checkSize(inventory, ResearchStationBlockEntity.SIZE);
		this.inventory = inventory;
		this.properties = properties;
		this.station = station;
		inventory.onOpen(playerInventory.player);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				addSlot(new Slot(inventory, col + row * 3, SLOTS_X + col * 18, SLOTS_Y + row * 18));
			}
		}
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

	public Inventory inventory() {
		return inventory;
	}

	public int property(int index) {
		return properties.get(index);
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (station == null || !(player instanceof ServerPlayerEntity sp)) {
			return true;
		}
		String refusal;
		if (id == WRITE_TREATISE) {
			refusal = station.writeTreatise(sp);
			if (refusal == null) {
				sp.sendMessage(Text.translatable("message.periodic_elements.treatise.written").formatted(Formatting.GREEN), true);
			}
		} else {
			Research.Project p = Research.get(id);
			if (p == null) {
				return false;
			}
			refusal = station.startResearch(sp, p);
			if (refusal == null) {
				sp.sendMessage(Text.translatable("message.periodic_elements.research.started", p.name()), true);
			}
		}
		if (refusal != null) {
			sp.sendMessage(Text.translatable(refusal).formatted(Formatting.RED), true);
		}
		sendContentUpdates();
		return refusal == null;
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
		int n = ResearchStationBlockEntity.SIZE;
		if (index < n ? !insertItem(stack, n, slots.size(), true) : !insertItem(stack, 0, n, false)) {
			return ItemStack.EMPTY;
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
}
