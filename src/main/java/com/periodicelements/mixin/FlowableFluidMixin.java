package com.periodicelements.mixin;

import com.periodicelements.farm.Sprayed;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Water and lava don't wash out a plant sprayed with Bordeaux mixture. 3.45.0: a new infinite source between fresh
 * and salt water is salt water, and the fresh sources that made it turn salt (see water.Water.newSource).
 */
@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {
	@Inject(method = "canFill", at = @At("HEAD"), cancellable = true)
	private void periodicElements$sprayed(BlockView world, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
		if (world instanceof ServerWorld sw && !state.isAir() && Sprayed.of(sw).contains(pos)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getUpdatedState", at = @At("RETURN"), cancellable = true)
	private void periodicElements$saltSource(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FluidState> cir) {
		FluidState made = cir.getReturnValue();
		if (made.isStill() && com.periodicelements.water.Water.freshOrSalt((Fluid) (Object) this)) {
			cir.setReturnValue(com.periodicelements.water.Water.newSource(world, pos, made));
		}
	}
}
