package com.periodicelements.mixin;

import com.periodicelements.water.Water;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 3.45.0: salt water freezes in the cold like fresh water (sea ice; it melts back to fresh water, as sea ice does). */
@Mixin(Biome.class)
public abstract class BiomeIceMixin {
	@Redirect(method = "canSetIce(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getFluid()Lnet/minecraft/fluid/Fluid;"))
	private Fluid periodicElements$seaIce(FluidState state) {
		Fluid f = state.getFluid();
		return f == Water.SALT_WATER ? net.minecraft.fluid.Fluids.WATER : f;
	}
}
