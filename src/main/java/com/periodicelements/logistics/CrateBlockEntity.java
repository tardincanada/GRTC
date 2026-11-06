package com.periodicelements.logistics;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

/** A larger chest's places (3.40.0): six rows of the tier's width. */
public class CrateBlockEntity extends LootableContainerBlockEntity implements ExtendedScreenHandlerFactory<Integer> {
	private DefaultedList<ItemStack> items;

	public CrateBlockEntity(BlockPos pos, BlockState state) {
		super(Logistics.CHEST_ENTITY, pos, state);
		items = DefaultedList.ofSize(tier(state).slots(), ItemStack.EMPTY);
	}

	private static Logistics.ChestTier tier(BlockState state) {
		return state.getBlock() instanceof CrateBlock b ? b.tier() : Logistics.IRON_CHEST;
	}

	public int columns() {
		return tier(getCachedState()).columns();
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	protected DefaultedList<ItemStack> getHeldStacks() {
		return items;
	}

	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> list) {
		items = list;
	}

	@Override
	protected Text getContainerName() {
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory inv) {
		return new CrateScreenHandler(syncId, inv, this, columns());
	}

	@Override
	public Integer getScreenOpeningData(ServerPlayerEntity player) {
		return columns();
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items = DefaultedList.ofSize(size(), ItemStack.EMPTY);
		if (!readLootTable(nbt)) {
			Inventories.readNbt(nbt, items, lookup);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!writeLootTable(nbt)) {
			Inventories.writeNbt(nbt, items, lookup);
		}
	}
}
