package com.periodicelements.trees;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** A sapling item whose tooltip names the biomes it grows in. */
public class BiomeSaplingItem extends BlockItem {
	public BiomeSaplingItem(BiomeSaplingBlock block, Settings settings) {
		super(block, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		tooltip.add(Text.translatable("gui.periodic_elements.sapling.biomes", ((BiomeSaplingBlock) getBlock()).biomeNames())
				.formatted(Formatting.DARK_GREEN));
	}
}
