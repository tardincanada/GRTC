package com.periodicelements.farm;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Washed with soap: while it lasts, Hunger (the food poisoning of rotten flesh and raw chicken) is washed off.
 * Infection, which soap will also guard against, comes with the medicines in 1.8.0.
 */
public class CleanEffect extends StatusEffect {
	public CleanEffect() {
		super(StatusEffectCategory.BENEFICIAL, 0xBFE8F0);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity.hasStatusEffect(StatusEffects.HUNGER)) {
			entity.removeStatusEffect(StatusEffects.HUNGER);
		}
		return true;
	}
}
