package com.periodicelements.mixin;

import com.periodicelements.space.Vacuum;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 3.66.0: plants need air. On the Moon and Mars crops, stems and saplings grow only in a room kept full of air. */
@Mixin({CropBlock.class, StemBlock.class, SaplingBlock.class})
public abstract class VacuumPlantMixin {
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void periodicElements$needsAir(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if (!Vacuum.hasAir(world, pos)) {
			ci.cancel();
		}
	}
}
