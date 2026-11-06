package com.periodicelements.fauna;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** A leech latched on in swamp water: a little blood now and then until salt takes it off (or it lets go). */
public class LeechEffect extends StatusEffect {
	public LeechEffect() {
		super(StatusEffectCategory.HARMFUL, 0x3A2A1E);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return duration % 120 == 0;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity.getHealth() > 2f) {
			entity.damage(entity.getDamageSources().magic(), 1f);
		}
		return true;
	}
}
