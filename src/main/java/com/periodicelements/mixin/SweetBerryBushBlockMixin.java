package com.periodicelements.mixin;

import com.periodicelements.farm.Sprayed;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A berry bush sprayed with Bordeaux mixture stops scratching (it still slows you). */
@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushBlockMixin {
	@Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
	private void periodicElements$sprayed(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (world instanceof ServerWorld sw && Sprayed.of(sw).contains(pos)) {
			entity.slowMovement(state, new net.minecraft.util.math.Vec3d(0.8, 0.75, 0.8));
			ci.cancel();
		}
	}
}
