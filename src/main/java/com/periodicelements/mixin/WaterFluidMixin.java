package com.periodicelements.mixin;

import com.periodicelements.water.Water;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 3.45.0: fresh water flows together with salt water (same levels, same infinite-source rule). */
@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin {
	@Inject(method = "matchesType", at = @At("RETURN"), cancellable = true)
	private void periodicElements$salt(Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && Water.isSaltWater(fluid)) {
			cir.setReturnValue(true);
		}
	}
}
