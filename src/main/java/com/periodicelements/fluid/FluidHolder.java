package com.periodicelements.fluid;

import com.periodicelements.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 3.38.0: something that stores one liquid or gas and that pipes, pumps and machines can fill and drain: the
 * Storage Tank and the multiblock tank's controller. A fluid is named by its flask item
 * ("periodic_elements:chlorine"), as everywhere else in the mod.
 */
public interface FluidHolder {
	/** The fluid held, or null when empty. */
	@Nullable
	Item fluid();

	double units();

	/** How many units of this fluid it can hold in all (0 if it cannot hold it now). */
	double capacity(Item fluid);

	/** One flask of what it holds, stamped with its isotope ratio (empty stack when empty). */
	ItemStack sample();

	/** Puts in up to {@code units} of {@code fluid} at isotope ratio {@code ratio} (NaN: natural); returns what went in. */
	double fill(Item fluid, double units, double ratio, boolean simulate);

	/** Takes out up to {@code units}; returns what came out. */
	double drain(double units, boolean simulate);

	/** Is this holder still in the world? */
	boolean gone();

	static boolean isGas(Item fluid) {
		return Fluids.cylinderOf(fluid) != null;
	}

	/** Room for more of this fluid. */
	default double room(Item fluid) {
		Item held = fluid();
		if (held != null && held != fluid) {
			return 0;
		}
		return Math.max(0, capacity(fluid) - units());
	}

	/** Fraction in use, 0 to 1 (0 when it holds nothing or cannot hold anything). */
	default double fraction() {
		Item held = fluid();
		if (held == null) {
			return 0;
		}
		double cap = capacity(held);
		return cap <= 0 ? 1 : Math.min(1, units() / cap);
	}
}
