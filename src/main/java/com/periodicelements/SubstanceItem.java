package com.periodicelements;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;

/** A substance item. Its tooltip shows the formula, complexity tier, where it is used, and its state. */
public class SubstanceItem extends Item {
	private final Substance substance;

	public SubstanceItem(Substance substance, Settings settings) {
		super(settings);
		this.substance = substance;
	}

	public Substance getSubstance() {
		return substance;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		String ns = PeriodicElements.MOD_ID;
		tooltip.add(Text.translatable("tooltip." + ns + ".formula", substance.formula())
				.styled(s -> s.withColor(TextColor.fromRgb(substance.color()))));
		tooltip.add(Text.translatable("tooltip." + ns + ".tier", substance.tier(),
				Text.translatable("tier." + ns + "." + substance.tier())).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("sector." + ns + "." + substance.sector()).formatted(Formatting.GOLD));
		tooltip.add(Text.translatable(getTranslationKey() + ".use").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip." + ns + ".state",
				Text.translatable("state." + ns + "." + substance.state())).formatted(Formatting.DARK_GRAY));
		if (Fluids.isFluid(substance.id())) {
			tooltip.add(Text.translatable("tooltip." + ns + ".in_flask").formatted(Formatting.DARK_GRAY));
		}
		tooltip.add(Text.translatable("tooltip." + ns + ".level", substance.level(),
				Text.translatable("level." + ns + "." + substance.level())).formatted(Formatting.AQUA));
		super.appendTooltip(stack, context, tooltip, type);
	}
}
