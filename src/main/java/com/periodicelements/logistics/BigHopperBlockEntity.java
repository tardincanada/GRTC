package com.periodicelements.logistics;

import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** A large hopper's places and timer (3.40.0). */
public class BigHopperBlockEntity extends LootableContainerBlockEntity implements Hopper {
	private DefaultedList<ItemStack> items;
	private int cooldown;
	/** The place the next push starts from, so one kind of item cannot hold the spout forever. */
	private int nextSlot;

	public BigHopperBlockEntity(BlockPos pos, BlockState state) {
		super(Logistics.HOPPER_ENTITY, pos, state);
		items = DefaultedList.ofSize(tier(state).slots(), ItemStack.EMPTY);
	}

	private static Logistics.HopperTier tier(BlockState state) {
		return state.getBlock() instanceof BigHopperBlock b ? b.tier() : Logistics.STEEL_HOPPER;
	}

	public Logistics.HopperTier tier() {
		return tier(getCachedState());
	}

	void serverTick(ServerWorld world, BlockPos pos, BlockState state) {
		if (--cooldown > 0) {
			return;
		}
		cooldown = 0;
		if (!state.get(HopperBlock.ENABLED)) {
			return;
		}
		Logistics.HopperTier tier = tier(state);
		boolean moved = false;
		// Out: up to perCycle items into whatever the spout points at.
		Direction out = state.get(HopperBlock.FACING);
		int budget = tier.perCycle();
		int size = items.size();
		for (int k = 0; k < size && budget > 0; k++) {
			int i = (nextSlot + k) % size;
			ItemStack s = items.get(i);
			if (!s.isEmpty()) {
				int n = Transfer.push(world, s, budget, pos.offset(out), out);
				budget -= n;
				if (n > 0) {
					moved = true;
					nextSlot = (i + 1) % size;
				}
			}
		}
		// In: up to perCycle items from the inventory above, or items lying on top.
		for (int n = 0; n < tier.perCycle(); n++) {
			if (!HopperBlockEntity.extract(world, this)) {
				break;
			}
			moved = true;
		}
		if (moved) {
			cooldown = tier.period();
			markDirty();
		}
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
		return items.size() == 9 ? new Generic3x3ContainerScreenHandler(syncId, inv, this)
				: new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, inv, this, 2);
	}

	@Override
	public double getHopperX() {
		return pos.getX() + 0.5;
	}

	@Override
	public double getHopperY() {
		return pos.getY() + 0.5;
	}

	@Override
	public double getHopperZ() {
		return pos.getZ() + 0.5;
	}

	@Override
	public boolean canBlockFromAbove() {
		return true;
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items = DefaultedList.ofSize(size(), ItemStack.EMPTY);
		if (!readLootTable(nbt)) {
			Inventories.readNbt(nbt, items, lookup);
		}
		cooldown = nbt.getInt("Cooldown");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!writeLootTable(nbt)) {
			Inventories.writeNbt(nbt, items, lookup);
		}
		nbt.putInt("Cooldown", cooldown);
	}
}
