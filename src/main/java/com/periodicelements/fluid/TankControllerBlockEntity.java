package com.periodicelements.fluid;

import com.periodicelements.Fluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 3.38.0: the Tank Controller holds what the whole multiblock tank holds. It measures its tank once a second
 * ({@link TankStructure}); hoppers, pipes and players reach it through it or any Tank Valve in the shell. Like the
 * Storage Tank it works containers from its input slot: filled flasks and cylinders are poured in, empty flasks come
 * out filled, and so do empty cylinders when the tank holds a gas and has a Tank Compressor.
 */
public class TankControllerBlockEntity extends BlockEntity implements SidedInventory, FluidHolder {
	public static final int IN = 0;
	public static final int OUT = 1;
	public static final double FLASK_UNITS = 1000.0 / 9.0;
	private static final int MEASURE_TICKS = 20;

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
	private final FluidStore store = new FluidStore();
	@Nullable
	private TankStructure.Result structure;
	private int timer;

	public TankControllerBlockEntity(BlockPos pos, BlockState state) {
		super(FluidContent.CONTROLLER_ENTITY, pos, state);
	}

	@Nullable
	public TankStructure.Result structure() {
		return structure;
	}

	public boolean valid() {
		return structure != null && structure.valid();
	}

	/** Measures the tank now (tests, and the first tick after loading). */
	public void measure() {
		if (world == null) {
			return;
		}
		structure = TankStructure.detect(world, pos);
		// Every shell block knows its controller: valves pass things through, plates take a gauge (3.38.0).
		List<BlockPos> parts = new ArrayList<>(structure.shell());
		parts.remove(pos);
		TankStructure.bind(world, pos, parts);
		// A smaller tank, or one that lost its compressor, lets the gas it can no longer hold out of its valve.
		if (structure.valid() && store.fluid != null && FluidHolder.isGas(store.fluid)) {
			double cap = capacity(store.fluid);
			if (store.units > cap) {
				store.take(store.units - cap);
				markDirty();
			}
		}
	}

	public void tick() {
		if (timer-- <= 0) {
			timer = MEASURE_TICKS;
			measure();
		}
		ItemStack in = items.get(IN);
		if (in.isEmpty()) {
			return;
		}
		ItemStack filled = drawFor(in);
		if (filled != null) {
			if (fits(filled)) {
				double per = Fluids.isCylinder(filled.getItem()) ? Fluids.cylinderUnits() : FLASK_UNITS;
				store.take(per);
				in.decrement(1);
				putOut(filled);
				markDirty();
			}
		} else if (canPour(in)) {
			Item rem = in.getItem().getRecipeRemainder();
			if (rem == null || fits(new ItemStack(rem))) {
				ItemStack empty = pour(in);
				if (!empty.isEmpty()) {
					putOut(empty);
				}
			}
		}
	}

	// ---------------------------------------------------------------- containers

	/** The flask or cylinder item a filled container holds, or null. */
	@Nullable
	static Item contentOf(ItemStack stack) {
		Item gas = Fluids.gasOf(stack.getItem());
		if (gas != null) {
			return gas;
		}
		return Fluids.inFlask(stack.getItem()) ? stack.getItem() : null;
	}

	static double unitsOf(ItemStack stack) {
		return Fluids.isCylinder(stack.getItem()) ? Fluids.cylinderUnits() : FLASK_UNITS;
	}

	public boolean canPour(ItemStack stack) {
		Item content = contentOf(stack);
		return content != null && room(content) >= unitsOf(stack) - 1e-6;
	}

	/** Pours one filled container in and returns its empty. */
	public ItemStack pour(ItemStack stack) {
		Item content = contentOf(stack);
		store.add(content, unitsOf(stack), FluidStore.ratioOf(stack, content));
		Item rem = stack.getItem().getRecipeRemainder();
		stack.decrement(1);
		markDirty();
		return rem == null ? ItemStack.EMPTY : new ItemStack(rem);
	}

	/** What an empty container comes out as when filled here now, or null if it cannot be filled. */
	@Nullable
	public ItemStack drawFor(ItemStack empty) {
		if (store.fluid == null) {
			return null;
		}
		if (empty.isOf(Fluids.GLASS_FLASK) && store.units >= FLASK_UNITS - 1e-6) {
			return store.sample();
		}
		Item cyl = Fluids.cylinderOf(store.fluid);
		if (empty.isOf(Fluids.STEEL_CYLINDER) && cyl != null && valid() && structure.compressed()
				&& store.units >= Fluids.cylinderUnits() - 1e-6) {
			ItemStack out = new ItemStack(cyl);
			String el = FluidStore.trackedElement(store.fluid);
			if (el != null && !Double.isNaN(store.ratio)) {
				com.periodicelements.radiation.Isotopes.stamp(out, el, store.ratio);
			}
			return out;
		}
		return null;
	}

	/** Fills one empty container a player holds (the caller takes the empty). */
	public ItemStack draw(ItemStack empty) {
		ItemStack out = drawFor(empty);
		if (out == null) {
			return ItemStack.EMPTY;
		}
		store.take(Fluids.isCylinder(out.getItem()) ? Fluids.cylinderUnits() : FLASK_UNITS);
		markDirty();
		return out;
	}

	/** Releases a gas into the air; returns the units let out. */
	public double vent() {
		if (store.fluid == null || !FluidHolder.isGas(store.fluid)) {
			return 0;
		}
		double out = store.units;
		store.clear();
		markDirty();
		return out;
	}

	private static String n(double x) {
		return String.format(Locale.ROOT, "%,.0f", x);
	}

	/** One line for the action bar: the tank's size and what it holds, or what is wrong with it. */
	public Text describe() {
		TankStructure.Result s = structure;
		String k = "gui.periodic_elements.big_tank.";
		if (s == null || !s.valid()) {
			TankStructure.Problem p = s == null ? TankStructure.Problem.OPEN : s.problem();
			Text problem = switch (p) {
				case MISSING -> Text.translatable(k + "missing", Text.translatable(world.getBlockState(s.missing()).getBlock().getTranslationKey()),
						s.missing().getX(), s.missing().getY(), s.missing().getZ());
				case NOT_BOX -> Text.translatable(k + "not_box");
				case TWO_CONTROLLERS -> Text.translatable(k + "two_controllers");
				case TWO_GAUGES -> Text.translatable(k + "two_gauges");
				default -> Text.translatable(k + "open", TankStructure.MAX_SIDE);
			};
			if (store.fluid == null) {
				return problem;
			}
			return Text.translatable(k + "broken", problem, n(store.units), Text.translatable(store.fluid.getTranslationKey()));
		}
		String size = s.dx() + "x" + s.dy() + "x" + s.dz();
		if (store.fluid == null) {
			return Text.translatable(k + (s.compressed() ? "empty.compressed" : "empty"), size, s.volume(),
					n(s.capacity(false)), n(s.capacity(true)));
		}
		double cap = capacity(store.fluid);
		return Text.translatable(k + "holds", size, n(store.units), n(cap), Text.translatable(store.fluid.getTranslationKey()),
				String.format(Locale.ROOT, "%.1f", cap <= 0 ? 0 : 100 * store.units / cap));
	}

	public int comparatorOutput() {
		return store.fluid == null ? 0 : Math.max(1, (int) Math.floor(14 * fraction()) + 1);
	}

	private boolean fits(ItemStack stack) {
		ItemStack out = items.get(OUT);
		return out.isEmpty() || (ItemStack.areItemsAndComponentsEqual(out, stack) && out.getCount() < out.getMaxCount());
	}

	private void putOut(ItemStack stack) {
		ItemStack out = items.get(OUT);
		if (out.isEmpty()) {
			items.set(OUT, stack.copyWithCount(1));
		} else {
			out.increment(1);
		}
	}

	// ---------------------------------------------------------------- FluidHolder

	@Nullable
	@Override
	public Item fluid() {
		return store.fluid;
	}

	@Override
	public double units() {
		return store.units;
	}

	@Override
	public double capacity(Item what) {
		return valid() ? structure.capacity(FluidHolder.isGas(what)) : 0;
	}

	@Override
	public ItemStack sample() {
		return store.sample();
	}

	@Override
	public double fill(Item what, double add, double r, boolean simulate) {
		double in = Math.min(add, room(what));
		if (in <= 1e-9) {
			return 0;
		}
		if (!simulate) {
			store.add(what, in, r);
			markDirty();
		}
		return in;
	}

	@Override
	public double drain(double want, boolean simulate) {
		double out = Math.max(0, Math.min(want, store.units));
		if (!simulate && out > 0) {
			store.take(out);
			markDirty();
		}
		return out;
	}

	@Override
	public boolean gone() {
		return isRemoved();
	}

	@Override
	public void markRemoved() {
		super.markRemoved();
		if (world != null) {
			TankStructure.bind(world, pos, List.of());
		}
	}

	// ---------------------------------------------------------------- persistence

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		store.write(nbt);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		store.read(nbt);
	}

	// ---------------------------------------------------------------- inventory

	@Override
	public int size() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack s = Inventories.splitStack(items, slot, amount);
		if (!s.isEmpty()) {
			markDirty();
		}
		return s;
	}

	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(items, slot);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		items.set(slot, stack);
		stack.capCount(getMaxCount(stack));
		markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return net.minecraft.inventory.Inventory.canPlayerUse(this, player);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return slot == IN && (Fluids.isEmptyContainer(stack) || contentOf(stack) != null);
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return side == Direction.DOWN ? new int[]{OUT} : new int[]{IN};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return isValid(slot, stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot == OUT;
	}
}
