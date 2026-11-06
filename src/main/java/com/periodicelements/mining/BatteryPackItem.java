package com.periodicelements.mining;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/** A battery pack carried in the inventory: the drills and the chainsaw draw on it; a Charging Bench fills it. */
public class BatteryPackItem extends Item {
	private final double capacity;
	private final boolean light;

	public BatteryPackItem(Settings settings, double capacity, boolean light) {
		super(settings.maxCount(1));
		this.capacity = capacity;
		this.light = light;
	}

	public double capacity() {
		return capacity;
	}

	/** Lithium-ion: light enough that the Heavy Mining Drill no longer slows you. */
	public boolean light() {
		return light;
	}

	public static double energy(ItemStack stack) {
		NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
		return nbt == null ? 0 : nbt.copyNbt().getDouble("Energy");
	}

	public static void setEnergy(ItemStack stack, double kJ) {
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, n -> n.putDouble("Energy", Math.max(0, kJ)));
	}

	/** Takes up to {@code kJ} from the pack; returns what it gave. */
	public static double draw(ItemStack stack, double kJ) {
		double have = energy(stack);
		double given = Math.min(have, kJ);
		if (given > 0) {
			setEnergy(stack, have - given);
		}
		return given;
	}

	/** Puts up to {@code kJ} into the pack; returns what it took. */
	public double fill(ItemStack stack, double kJ) {
		double have = energy(stack);
		double taken = Math.min(capacity - have, kJ);
		if (taken > 0) {
			setEnergy(stack, have + taken);
		}
		return Math.max(0, taken);
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		return (int) Math.round(13 * MathHelper.clamp(energy(stack) / capacity, 0, 1));
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return 0x50C878;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("gui.periodic_elements.pack.energy", (int) energy(stack), (int) capacity).formatted(Formatting.GREEN));
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
