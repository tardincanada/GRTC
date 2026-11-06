package com.periodicelements;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;

/** An element item. Its tooltip shows the symbol, atomic number, category, and state. */
public class ElementItem extends Item {
	private final Element element;

	public ElementItem(Element element, Settings settings) {
		super(settings);
		this.element = element;
	}

	public Element getElement() {
		return element;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		String ns = PeriodicElements.MOD_ID;
		tooltip.add(Text.translatable("tooltip." + ns + ".symbol", element.symbol())
				.styled(s -> s.withColor(TextColor.fromRgb(element.color()))));
		tooltip.add(Text.translatable("tooltip." + ns + ".number", element.number()).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("category." + ns + "." + element.category()).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip." + ns + ".state",
				Text.translatable("state." + ns + "." + element.state())).formatted(Formatting.DARK_GRAY));
		if (Fluids.isFluid(element.id())) {
			tooltip.add(Text.translatable("tooltip." + ns + ".in_flask").formatted(Formatting.DARK_GRAY));
		}
		tooltip.add(Text.translatable("tooltip." + ns + ".level", element.level(),
				Text.translatable("level." + ns + "." + element.level())).formatted(Formatting.AQUA));
		tooltip.add((element.isAncient()
				? Text.translatable("tooltip." + ns + ".discovered.ancient")
				: Text.translatable("tooltip." + ns + ".discovered", element.discovered())).formatted(Formatting.DARK_GRAY));
		String isotope = com.periodicelements.radiation.Decay.isotope(element.id());
		if (isotope != null) {
			tooltip.add(Text.translatable("tooltip." + ns + ".isotope", isotope).formatted(Formatting.DARK_GREEN));
		} else if (com.periodicelements.radiation.Isotopes.natural(element.id()) != null) {
			tooltip.add(Text.translatable("tooltip." + ns + ".natural_isotopes",
					com.periodicelements.radiation.Isotopes.natural(element.id())).formatted(Formatting.DARK_GREEN));
		}
		var tracked = com.periodicelements.radiation.Isotopes.tracked(element.id());
		if (tracked != null) {
			double r = com.periodicelements.radiation.Isotopes.ratio(stack, element.id());
			tooltip.add(Text.translatable("tooltip." + ns + ".isotope_ratio", tracked.isotope(), tracked.format(r),
					Text.translatable("isotope." + ns + "." + com.periodicelements.radiation.Isotopes.label(tracked, r)))
					.formatted(Formatting.GREEN));
		}
		super.appendTooltip(stack, context, tooltip, type);
	}
}
