package com.periodicelements.logistics;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * What a belt carries (3.40.0): one stack of up to the tier's capacity, and how far along the block it has come. The
 * load moves on when it reaches the front; if nothing in front takes it, it waits there.
 */
public class BeltBlockEntity extends BlockEntity implements SidedInventory {
	private static final int[] SLOT = {0};
	private ItemStack stack = ItemStack.EMPTY;
	/** Ticks since the load came onto this belt (it leaves once this reaches the tier's interval). */
	private int progress;
	private int pickupTimer;

	public BeltBlockEntity(BlockPos pos, BlockState state) {
		super(Logistics.BELT_ENTITY, pos, state);
	}

	public Logistics.BeltTier tier() {
		return getCachedState().getBlock() instanceof BeltBlock b ? b.tier() : Logistics.CANVAS;
	}

	public Direction facing() {
		return getCachedState().get(BeltBlock.FACING);
	}

	/** How far along the block the load is, 0 (entered) to 1 (at the front), for drawing. */
	public float progress(float tickDelta) {
		return Math.min(1f, (progress + tickDelta) / tier().interval());
	}

	void clientTick() {
		if (!stack.isEmpty() && progress < tier().interval()) {
			progress++;
		}
	}

	void serverTick(ServerWorld world, BlockPos pos, BlockState state) {
		Logistics.BeltTier tier = tier();
		if (stack.getCount() < tier.capacity() && ++pickupTimer >= tier.interval()) {
			pickupTimer = 0;
			pickUp(world, pos, tier);
		}
		if (stack.isEmpty()) {
			return;
		}
		if (progress < tier.interval()) {
			progress++;
			return;
		}
		Direction d = state.get(BeltBlock.FACING);
		BlockPos front = pos.offset(d);
		if (world.getBlockEntity(front) instanceof BeltBlockEntity next && next.facing() == d.getOpposite()) {
			return; // two belts facing each other: nothing passes
		}
		int before = stack.getCount();
		Transfer.push(world, stack, stack.getCount(), front, d);
		if (stack.getCount() != before) {
			if (stack.isEmpty()) {
				stack = ItemStack.EMPTY;
			}
			changed();
		}
	}

	/** Items lying on the belt go onto it. */
	private void pickUp(ServerWorld world, BlockPos pos, Logistics.BeltTier tier) {
		Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.6, pos.getZ() + 1);
		for (ItemEntity e : world.getEntitiesByClass(ItemEntity.class, box, ItemEntity::isAlive)) {
			ItemStack s = e.getStack();
			if (!stack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(stack, s)) {
				continue;
			}
			int room = Math.min(tier.capacity(), s.getMaxCount()) - stack.getCount();
			int take = Math.min(room, s.getCount());
			if (take <= 0) {
				continue;
			}
			if (stack.isEmpty()) {
				stack = s.copyWithCount(take);
				progress = 0;
			} else {
				stack.increment(take);
			}
			s.decrement(take);
			if (s.isEmpty()) {
				e.discard();
			} else {
				e.setStack(s);
			}
			changed();
		}
	}

	private void changed() {
		markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	// Inventory: one place, holding up to the tier's capacity; filled from anywhere but the front, never drained.

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		return stack;
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack out = stack.split(amount);
		if (stack.isEmpty()) {
			stack = ItemStack.EMPTY;
		}
		changed();
		return out;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack out = stack;
		stack = ItemStack.EMPTY;
		changed();
		return out;
	}

	@Override
	public void setStack(int slot, ItemStack s) {
		if (stack.isEmpty()) {
			progress = 0;
		}
		stack = s;
		changed();
	}

	@Override
	public int getMaxCountPerStack() {
		return tier().capacity();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return false;
	}

	@Override
	public void clear() {
		stack = ItemStack.EMPTY;
		changed();
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return SLOT;
	}

	@Override
	public boolean canInsert(int slot, ItemStack s, @Nullable Direction dir) {
		return dir != facing() && (stack.isEmpty() || ItemStack.areItemsAndComponentsEqual(stack, s));
	}

	@Override
	public boolean canExtract(int slot, ItemStack s, Direction dir) {
		return false; // a hopper under a belt must not drain it: the Belt Sorter sends items down on purpose
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		stack = nbt.contains("Load") ? ItemStack.fromNbtOrEmpty(lookup, nbt.getCompound("Load")) : ItemStack.EMPTY;
		progress = nbt.getInt("Progress");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!stack.isEmpty()) {
			nbt.put("Load", stack.encode(lookup));
		}
		nbt.putInt("Progress", progress);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
		return createNbt(lookup);
	}

}
