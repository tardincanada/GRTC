package com.periodicelements.logistics;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * The Belt Sorter's contents (3.40.0): what passes through (one stack, up to {@link Logistics#SORTER_CAPACITY}) and
 * the nine filter places (one item each, a sample of what to turn aside). In "required" mode (the default) items like the filter's are turned aside and an empty filter lets everything
 * straight on; in "forbidden" mode items like the filter's go straight on and all others are turned aside.
 */
public class SorterBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
	private static final int[] SLOT = {0};
	private ItemStack stack = ItemStack.EMPTY;
	private int timer;
	private final SimpleInventory filter = new SimpleInventory(9) {
		@Override
		public int getMaxCountPerStack() {
			return 1;
		}

		@Override
		public void markDirty() {
			super.markDirty();
			SorterBlockEntity.this.markDirty();
		}
	};

	public SorterBlockEntity(BlockPos pos, BlockState state) {
		super(Logistics.SORTER_ENTITY, pos, state);
	}

	private final PropertyDelegate modeProperty = new PropertyDelegate() {
		@Override
		public int get(int index) {
			return forbidden() ? 1 : 0;
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int size() {
			return 1;
		}
	};

	/** "Forbidden" mode (3.40.1): the filter lists what goes straight on, and everything else is turned aside. */
	public boolean forbidden() {
		return getCachedState().get(SorterBlock.FORBIDDEN);
	}

	public void setForbidden(boolean value) {
		if (world != null && forbidden() != value) {
			world.setBlockState(pos, getCachedState().with(SorterBlock.FORBIDDEN, value));
		}
	}

	/** Is this item one to turn aside, given the filter and the mode? */
	public boolean turnsAside(ItemStack s) {
		return forbidden() ? !matches(s) : matches(s);
	}

	public SimpleInventory filter() {
		return filter;
	}

	public Direction facing() {
		return getCachedState().get(SorterBlock.FACING);
	}

	/** Is this kind of item one the filter turns aside? */
	public boolean matches(ItemStack s) {
		for (int i = 0; i < filter.size(); i++) {
			if (!filter.getStack(i).isEmpty() && ItemStack.areItemsEqual(filter.getStack(i), s)) {
				return true;
			}
		}
		return false;
	}

	void serverTick(ServerWorld world, BlockPos pos, BlockState state) {
		if (stack.isEmpty() || ++timer < Logistics.SORTER_INTERVAL) {
			return;
		}
		timer = 0;
		Direction f = state.get(SorterBlock.FACING);
		int before = stack.getCount();
		if (turnsAside(stack)) {
			// Down into a hopper or chest first, else out of the left, then the right side.
			for (Direction d : new Direction[]{Direction.DOWN, f.rotateYCounterclockwise(), f.rotateYClockwise()}) {
				if (stack.isEmpty()) {
					break;
				}
				BlockPos out = pos.offset(d);
				if (world.getBlockEntity(out) instanceof BeltBlockEntity belt && belt.facing() == d.getOpposite()) {
					continue; // a belt that feeds the sorter is an input, not an exit
				}
				Transfer.push(world, stack, stack.getCount(), out, d);
			}
		} else {
			BlockPos front = pos.offset(f);
			if (!(world.getBlockEntity(front) instanceof BeltBlockEntity belt && belt.facing() == f.getOpposite())) {
				Transfer.push(world, stack, stack.getCount(), front, f);
			}
		}
		if (stack.getCount() != before) {
			if (stack.isEmpty()) {
				stack = ItemStack.EMPTY;
			}
			markDirty();
		}
	}

	// Inventory: what passes through. Filled from behind and the sides, never drained by hoppers.

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
		markDirty();
		return out;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack out = stack;
		stack = ItemStack.EMPTY;
		markDirty();
		return out;
	}

	@Override
	public void setStack(int slot, ItemStack s) {
		stack = s;
		markDirty();
	}

	@Override
	public int getMaxCountPerStack() {
		return Logistics.SORTER_CAPACITY;
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return false;
	}

	@Override
	public void clear() {
		stack = ItemStack.EMPTY;
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return SLOT;
	}

	@Override
	public boolean canInsert(int slot, ItemStack s, @Nullable Direction dir) {
		return dir != facing() && dir != Direction.DOWN && (stack.isEmpty() || ItemStack.areItemsAndComponentsEqual(stack, s));
	}

	@Override
	public boolean canExtract(int slot, ItemStack s, Direction dir) {
		return false;
	}

	// The filter screen: the dispenser's 3 x 3 grid and a mode button.

	@Override
	public Text getDisplayName() {
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new SorterScreenHandler(syncId, inv, filter, modeProperty, this);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		stack = nbt.contains("Load") ? ItemStack.fromNbtOrEmpty(lookup, nbt.getCompound("Load")) : ItemStack.EMPTY;
		DefaultedList<ItemStack> list = DefaultedList.ofSize(9, ItemStack.EMPTY);
		NbtCompound f = nbt.getCompound("Filter");
		Inventories.readNbt(f, list, lookup);
		for (int i = 0; i < 9; i++) {
			filter.setStack(i, list.get(i));
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!stack.isEmpty()) {
			nbt.put("Load", stack.encode(lookup));
		}
		DefaultedList<ItemStack> list = DefaultedList.ofSize(9, ItemStack.EMPTY);
		for (int i = 0; i < 9; i++) {
			list.set(i, filter.getStack(i));
		}
		NbtCompound f = new NbtCompound();
		Inventories.writeNbt(f, list, lookup);
		nbt.put("Filter", f);
	}
}
