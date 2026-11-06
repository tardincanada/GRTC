package com.periodicelements.mining;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * A drill bit: sets the drill's mining tier and speed and takes the wear, one point a swing. Enchant the bit, not the
 * drill: a fitted bit lends the drill its enchantments (all but Mending).
 */
public class DrillBitItem extends Item {
	private final float speed;
	private final TagKey<Block> incorrect;
	private final int enchantability;

	public DrillBitItem(Settings settings, float speed, TagKey<Block> incorrect, int enchantability) {
		super(settings);
		this.speed = speed;
		this.incorrect = incorrect;
		this.enchantability = enchantability;
	}

	public float speed() {
		return speed;
	}

	public TagKey<Block> incorrect() {
		return incorrect;
	}

	@Override
	public int getEnchantability() {
		return enchantability;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return stack.getCount() == 1;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
