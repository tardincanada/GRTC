package com.periodicelements.fluid;

import com.periodicelements.radiation.Isotopes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** 3.38.0: one fluid, its units and its isotope ratio, with the mixing and saving every holder needs. */
public final class FluidStore {
	@Nullable
	public Item fluid;
	public double units;
	/** Isotope ratio (atom %) of a tracked element held (hydrogen), or NaN. */
	public double ratio = Double.NaN;

	@Nullable
	public static String trackedElement(Item fluid) {
		String el = Isotopes.elementOf(Registries.ITEM.getId(fluid).getPath());
		return el != null && Isotopes.tracked(el) != null ? el : null;
	}

	/** Adds units at a ratio (NaN: natural). The caller has checked it is the same fluid and fits. */
	public void add(Item what, double add, double r) {
		if (add <= 0) {
			return;
		}
		String el = trackedElement(what);
		if (el != null) {
			if (Double.isNaN(r)) {
				r = Isotopes.tracked(el).natural();
			}
			double old = Double.isNaN(ratio) || fluid == null ? r : ratio;
			ratio = (old * units + r * add) / (units + add);
		}
		fluid = what;
		units += add;
	}

	public double take(double want) {
		double out = Math.min(want, units);
		units -= out;
		if (units < 1e-3) {
			clear();
		}
		return out;
	}

	public void clear() {
		units = 0;
		fluid = null;
		ratio = Double.NaN;
	}

	public ItemStack sample() {
		if (fluid == null) {
			return ItemStack.EMPTY;
		}
		ItemStack out = new ItemStack(fluid);
		String el = trackedElement(fluid);
		if (el != null && !Double.isNaN(ratio)) {
			Isotopes.stamp(out, el, ratio);
		}
		return out;
	}

	/** The ratio a stack carries for this fluid's tracked element, or NaN. */
	public static double ratioOf(ItemStack stack, Item fluid) {
		String el = trackedElement(fluid);
		return el == null ? Double.NaN : Isotopes.ratio(stack, el);
	}

	public void write(NbtCompound nbt) {
		if (fluid != null) {
			nbt.putString("Fluid", Registries.ITEM.getId(fluid).toString());
			nbt.putDouble("Units", units);
			if (!Double.isNaN(ratio)) {
				nbt.putDouble("Ratio", ratio);
			}
		}
	}

	public void read(NbtCompound nbt) {
		fluid = nbt.contains("Fluid") ? Registries.ITEM.get(Identifier.of(nbt.getString("Fluid"))) : null;
		units = fluid == null ? 0 : nbt.getDouble("Units");
		ratio = nbt.contains("Ratio") ? nbt.getDouble("Ratio") : Double.NaN;
	}
}
