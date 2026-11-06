package com.periodicelements.shrubs;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * The item that plants a shrub: a cutting (named "&lt;shrub&gt; Cutting"), or for shrubs grown from seed the product
 * itself. The tooltip names the biomes it grows in and the greenhouse that will do instead.
 */
public class ShrubCuttingItem extends BlockItem {
	private final String key;

	public ShrubCuttingItem(ShrubBlock block, String key, Settings settings) {
		super(block, settings);
		this.key = key;
	}

	@Override
	public String getTranslationKey() {
		return key;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		ShrubBlock b = (ShrubBlock) getBlock();
		tooltip.add(Text.translatable("gui.periodic_elements.shrub.biomes", b.biomeNames()).formatted(Formatting.DARK_GREEN));
		tooltip.add(Text.translatable("gui.periodic_elements.shrub.greenhouse_" + b.climate)
				.formatted(Formatting.DARK_GREEN));
	}
}
