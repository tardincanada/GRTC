package com.periodicelements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * A nylon parachute: held in either hand, it opens once its holder has fallen more than three blocks and gives
 * Slow Falling until they land. It does not wear out.
 */
public class ParachuteItem extends ModContent.TippedItem {
	/** Blocks of free fall before the canopy opens. */
	public static final float OPENS_AFTER = 3.0f;

	public ParachuteItem(Settings settings) {
		super(settings);
	}

	/** Should a parachute this entity holds be open right now? */
	public static boolean opens(LivingEntity holder) {
		return !holder.isOnGround() && !holder.isTouchingWater() && !holder.isFallFlying() && !holder.hasVehicle()
				&& holder.getVelocity().y <= 0
				// Slow Falling resets the fall distance, so an open canopy stays open until landing.
				&& (holder.fallDistance > OPENS_AFTER || holder.hasStatusEffect(StatusEffects.SLOW_FALLING));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient || !(entity instanceof LivingEntity holder)) {
			return;
		}
		if ((holder.getMainHandStack() == stack || holder.getOffHandStack() == stack) && opens(holder)) {
			holder.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 10, 0, false, false, true));
		}
	}
}
