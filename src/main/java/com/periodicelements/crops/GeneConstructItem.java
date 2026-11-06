package com.periodicelements.crops;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** A transgene ready for the Gene Gun (3.37.0): Bt, EPSPS, golden rice or DREB. */
public class GeneConstructItem extends Item {
	private final int transgene;

	public GeneConstructItem(int transgene, Settings settings) {
		super(settings);
		this.transgene = transgene;
	}

	public int transgene() {
		return transgene;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
