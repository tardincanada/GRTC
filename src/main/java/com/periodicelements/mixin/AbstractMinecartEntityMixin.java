package com.periodicelements.mixin;

import com.periodicelements.farm.Farm;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Rubber Wheels: a minecart fitted with them runs 25% faster on rails. */
@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {
	@Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
	private void periodicElements$rubberWheels(CallbackInfoReturnable<Double> cir) {
		if (((AbstractMinecartEntity) (Object) this).getCommandTags().contains(Farm.WHEELS_TAG)) {
			cir.setReturnValue(cir.getReturnValue() * Farm.WHEELS_SPEED);
		}
	}
}
