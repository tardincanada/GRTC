package com.periodicelements.mixin;

import com.periodicelements.farm.FieldState;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ground sprayed with Pre-Emergent (atrazine) takes no grass or mycelium while it lasts. */
@Mixin(SpreadableBlock.class)
public abstract class SpreadableBlockMixin {
	@Inject(method = "canSpread", at = @At("HEAD"), cancellable = true)
	private static void periodicElements$treated(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (world instanceof ServerWorld sw && FieldState.of(sw).treated(FieldState.Kind.PRE_EMERGENT, pos, sw.getTime())) {
			cir.setReturnValue(false);
		}
	}
}
