package com.periodicelements.mixin;

import com.periodicelements.medicine.Infection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Milk clears status effects as usual, but not Infection or Sepsis: those need medicine. */
@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {
	@Redirect(method = "finishUsing", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
	private boolean periodicElements$keepDisease(LivingEntity user) {
		return Infection.clearExceptDisease(user);
	}
}
