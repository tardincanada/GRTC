package com.periodicelements.machine;

import com.periodicelements.Fluids;
import com.periodicelements.ModContent;
import com.periodicelements.radiation.Isotopes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Holds up to {@link Fluids#tankUnits()} units of one liquid or gas. Filled flasks and cylinders poured in give
 * their empties back; empty flasks drawn from it come out filled. By hand (use the tank holding the container)
 * or by hopper: containers go into the input slot from the top or sides, and what comes out leaves from below.
 */
public class StorageTankBlockEntity extends BlockEntity implements SidedInventory, com.periodicelements.fluid.FluidHolder {
	public static final int IN = 0;
	public static final int OUT = 1;
	private static final double FLASK_UNITS = 1000.0 / 9.0;

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
	/** The flask item of the fluid held ("periodic_elements:chlorine"), or null when empty. */
	@Nullable
	private Item fluid;
	private double units;
	/** Isotope ratio (atom %) of a tracked element held (hydrogen), or NaN. */
	private double ratio = Double.NaN;

	public StorageTankBlockEntity(BlockPos pos, BlockState state) {
		super(ModContent.TANK_ENTITY, pos, state);
	}

	@Nullable
	public Item fluid() {
		return fluid;
	}

	public double units() {
		return units;
	}

	/** The flask item a filled container holds, or null if it holds no liquid or gas. */
	@Nullable
	private static Item contentOf(ItemStack stack) {
		Item gas = Fluids.gasOf(stack.getItem());
		if (gas != null) {
			return gas;
		}
		return Fluids.inFlask(stack.getItem()) ? stack.getItem() : null;
	}

	private static double unitsOf(ItemStack stack) {
		return Fluids.isCylinder(stack.getItem()) ? Fluids.cylinderUnits() : FLASK_UNITS;
	}

	/** Can one of this filled container be poured in? */
	public boolean canPour(ItemStack stack) {
		Item content = contentOf(stack);
		return content != null && (fluid == null || fluid == content) && units + unitsOf(stack) <= Fluids.tankUnits() + 1e-6;
	}

	/** Pours one filled container in and returns its empty. */
	public ItemStack pour(ItemStack stack) {
		Item content = contentOf(stack);
		double add = unitsOf(stack);
		String el = Isotopes.elementOf(Registries.ITEM.getId(content).getPath());
		if (el != null && Isotopes.tracked(el) != null) {
			double r = Isotopes.ratio(stack, el);
			double old = Double.isNaN(ratio) ? r : ratio;
			ratio = (old * units + r * add) / (units + add);
		}
		fluid = content;
		units += add;
		Item rem = stack.getItem().getRecipeRemainder();
		stack.decrement(1);
		markDirty();
		return rem == null ? ItemStack.EMPTY : new ItemStack(rem);
	}

	public boolean canDraw() {
		return fluid != null && units >= FLASK_UNITS - 1e-6;
	}

	/** Fills one flask from the tank (the caller takes the empty flask). */
	public ItemStack draw() {
		ItemStack out = new ItemStack(fluid);
		String el = Isotopes.elementOf(Registries.ITEM.getId(fluid).getPath());
		if (el != null && Isotopes.tracked(el) != null && !Double.isNaN(ratio)) {
			Isotopes.stamp(out, el, ratio);
		}
		units -= FLASK_UNITS;
		if (units < 1e-3) {
			units = 0;
			fluid = null;
			ratio = Double.NaN;
		}
		markDirty();
		return out;
	}

	public Text describe() {
		if (fluid == null) {
			return Text.translatable("gui.periodic_elements.tank.empty", String.format(java.util.Locale.ROOT, "%,d", (int) Fluids.tankUnits()));
		}
		return Text.translatable("gui.periodic_elements.tank.holds", String.format(java.util.Locale.ROOT, "%,.0f", units),
				String.format(java.util.Locale.ROOT, "%,d", (int) Fluids.tankUnits()), Text.translatable(fluid.getTranslationKey()));
	}

	public int comparatorOutput() {
		return fluid == null ? 0 : Math.max(1, (int) Math.floor(14 * units / Fluids.tankUnits()) + 1);
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

	/** Works one container from the input slot each tick. */
	public void tick() {
		ItemStack in = items.get(IN);
		if (in.isEmpty()) {
			return;
		}
		if (in.isOf(Fluids.GLASS_FLASK)) {
			if (canDraw()) {
				ItemStack filled = new ItemStack(fluid);
				String el = Isotopes.elementOf(Registries.ITEM.getId(fluid).getPath());
				if (el != null && Isotopes.tracked(el) != null && !Double.isNaN(ratio)) {
					Isotopes.stamp(filled, el, ratio);
				}
				if (fits(filled)) {
					in.decrement(1);
					putOut(draw());
				}
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

	// ---------------------------------------------------------------- 3.38.0: pipes, pumps and the relief valve

	@Override
	public double capacity(Item what) {
		return Fluids.tankUnits();
	}

	@Override
	public ItemStack sample() {
		if (fluid == null) {
			return ItemStack.EMPTY;
		}
		ItemStack out = new ItemStack(fluid);
		String el = Isotopes.elementOf(Registries.ITEM.getId(fluid).getPath());
		if (el != null && Isotopes.tracked(el) != null && !Double.isNaN(ratio)) {
			Isotopes.stamp(out, el, ratio);
		}
		return out;
	}

	@Override
	public double fill(Item what, double add, double r, boolean simulate) {
		if (fluid != null && fluid != what) {
			return 0;
		}
		double in = Math.max(0, Math.min(add, Fluids.tankUnits() - units));
		if (in <= 1e-9 || simulate) {
			return in;
		}
		String el = com.periodicelements.fluid.FluidStore.trackedElement(what);
		if (el != null) {
			double rr = Double.isNaN(r) ? Isotopes.tracked(el).natural() : r;
			double old = Double.isNaN(ratio) || fluid == null ? rr : ratio;
			ratio = (old * units + rr * in) / (units + in);
		}
		fluid = what;
		units += in;
		markDirty();
		return in;
	}

	@Override
	public double drain(double want, boolean simulate) {
		double out = Math.max(0, Math.min(want, units));
		if (simulate || out <= 0) {
			return out;
		}
		units -= out;
		if (units < 1e-3) {
			units = 0;
			fluid = null;
			ratio = Double.NaN;
		}
		markDirty();
		return out;
	}

	@Override
	public boolean gone() {
		return isRemoved();
	}

	/** Releases a gas into the air; returns the units let out (0 for a liquid or an empty tank). */
	public double vent() {
		if (fluid == null || !com.periodicelements.fluid.FluidHolder.isGas(fluid)) {
			return 0;
		}
		return drain(units, false);
	}

	// ---------------------------------------------------------------- persistence

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		if (fluid != null) {
			nbt.putString("Fluid", Registries.ITEM.getId(fluid).toString());
			nbt.putDouble("Units", units);
			if (!Double.isNaN(ratio)) {
				nbt.putDouble("Ratio", ratio);
			}
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		fluid = nbt.contains("Fluid") ? Registries.ITEM.get(Identifier.of(nbt.getString("Fluid"))) : null;
		units = fluid == null ? 0 : nbt.getDouble("Units");
		ratio = nbt.contains("Ratio") ? nbt.getDouble("Ratio") : Double.NaN;
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
		return slot == IN && (stack.isOf(Fluids.GLASS_FLASK) || contentOf(stack) != null);
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
