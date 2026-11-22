package com.periodicelements.mixin;

import com.periodicelements.space.Moon;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 3.66.0: the Moon and Mars share the Earth's weather by default (vanilla keeps one weather for the whole server). It
 * never rains there: Mars has dust storms of its own instead (Mars.storm).
 */
@Mixin(World.class)
public abstract class AirlessWeatherMixin {
	@Inject(method = {"getRainGradient", "getThunderGradient"}, at = @At("HEAD"), cancellable = true)
	private void periodicElements$dry(float delta, CallbackInfoReturnable<Float> cir) {
		if (Moon.airless((World) (Object) this)) {
			cir.setReturnValue(0f);
		}
	}
}
