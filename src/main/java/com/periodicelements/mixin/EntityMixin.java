package com.periodicelements.mixin;

import com.periodicelements.farm.Farm;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sneakers: rubber soles step as softly as sneaking (no vibrations; magma and turtle eggs don't notice). */
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "bypassesSteppingEffects", at = @At("HEAD"), cancellable = true)
	private void periodicElements$sneakers(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof LivingEntity living && Farm.SNEAKERS != null
				&& living.getEquippedStack(EquipmentSlot.FEET).isOf(Farm.SNEAKERS)) {
			cir.setReturnValue(true);
		}
	}
}
