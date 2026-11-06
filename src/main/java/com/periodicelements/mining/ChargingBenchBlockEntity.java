package com.periodicelements.mining;

import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Holds one battery pack and fills it at up to {@link #RATE} kJ a tick from the piles, dynamos and batteries on its
 * wire network (as an electric machine draws). Hoppers can put packs in and take them out.
 */
public class ChargingBenchBlockEntity extends BlockEntity implements SidedInventory {
	public static final double RATE = 5;
	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
	private double lastDraw;

	public ChargingBenchBlockEntity(BlockPos pos, BlockState state) {
		super(Mining.BENCH_ENTITY, pos, state);
	}

	public double lastDraw() {
		return lastDraw;
	}

	public void tick(ServerWorld world, BlockPos pos) {
		lastDraw = 0;
		ItemStack pack = items.get(0);
		if (!(pack.getItem() instanceof BatteryPackItem p)) {
			return;
		}
		double want = Math.min(RATE, p.capacity() - BatteryPackItem.energy(pack));
		if (want <= 1e-9) {
			return;
		}
		double got = 0;
		for (PowerBlockEntity pile : MachineBlockEntity.powerSources(world, pos)) {
			got += pile.draw(want - got);
			if (got >= want - 1e-9) {
				break;
			}
		}
		if (got > 0) {
			p.fill(pack, got);
			lastDraw = got;
			markDirty();
		}
	}

	public Text describe() {
		ItemStack pack = items.get(0);
		if (!(pack.getItem() instanceof BatteryPackItem p)) {
			return Text.translatable("gui.periodic_elements.bench.empty");
		}
		return Text.translatable(lastDraw > 0 || BatteryPackItem.energy(pack) >= p.capacity() - 1e-6
						? "gui.periodic_elements.bench.charging" : "gui.periodic_elements.bench.no_power",
				pack.getName(), (int) BatteryPackItem.energy(pack), (int) p.capacity());
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return stack.getItem() instanceof BatteryPackItem && items.get(0).isEmpty();
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		// Hoppers take only a full pack.
		return stack.getItem() instanceof BatteryPackItem p && BatteryPackItem.energy(stack) >= p.capacity() - 1e-6;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return items.get(0).isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack s = Inventories.splitStack(items, slot, amount);
		markDirty();
		return s;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack s = Inventories.removeStack(items, slot);
		markDirty();
		return s;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		items.set(slot, stack);
		markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void clear() {
		items.clear();
	}
}
