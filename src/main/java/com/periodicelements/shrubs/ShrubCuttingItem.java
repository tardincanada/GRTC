package com.periodicelements.shrubs;

import net.minecraft.component.DataComponentTypes;
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

	/** 3.56.0: a whole bush lifted with a shovel is "<bush> (with root ball)". */
	@Override
	public Text getName(ItemStack stack) {
		if (stack.contains(DataComponentTypes.BLOCK_STATE)) {
			return Text.translatable("item.periodic_elements.shrub_root_ball", getBlock().getName());
		}
		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		ShrubBlock b = (ShrubBlock) getBlock();
		if (stack.contains(DataComponentTypes.BLOCK_STATE)) {
			tooltip.add(Text.translatable("gui.periodic_elements.shrub.root_ball").formatted(Formatting.GRAY));
		}
		tooltip.add(Text.translatable("gui.periodic_elements.shrub.biomes", b.biomeNames()).formatted(Formatting.DARK_GREEN));
		tooltip.add(Text.translatable("gui.periodic_elements.shrub.greenhouse_" + b.climate)
				.formatted(Formatting.DARK_GREEN));
	}
}
