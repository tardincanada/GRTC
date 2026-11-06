package com.periodicelements;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A penetrator arrow: a dense, hard core that hits harder than a flint arrow. Depleted uranium is pyrophoric, so
 * its arrows set what they strike alight.
 */
public class PenetratorArrowItem extends ArrowItem {
	private final double damageFactor;
	private final boolean incendiary;

	public PenetratorArrowItem(Item.Settings settings, double damageFactor, boolean incendiary) {
		super(settings);
		this.damageFactor = damageFactor;
		this.incendiary = incendiary;
	}

	public double damageFactor() {
		return damageFactor;
	}

	@Override
	public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom) {
		ArrowEntity arrow = new ArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
		arrow.setDamage(arrow.getDamage() * damageFactor);
		if (incendiary) {
			arrow.setOnFireFor(100);
		}
		return arrow;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		super.appendTooltip(stack, context, tooltip, type);
	}
}
