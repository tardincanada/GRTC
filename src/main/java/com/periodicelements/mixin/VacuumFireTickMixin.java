package com.periodicelements.mixin;

import com.periodicelements.space.Vacuum;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 3.65.0: a fire that finds itself without oxygen (its room opened to the vacuum) goes out. */
@Mixin(FireBlock.class)
public abstract class VacuumFireTickMixin {
	@Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
	private void periodicElements$snuff(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if (Vacuum.snuffed(world, pos, state)) {
			world.removeBlock(pos, false);
			ci.cancel();
		}
	}
}
