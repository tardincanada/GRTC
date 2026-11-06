package com.periodicelements.mixin;

import com.periodicelements.farm.Sprayed;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import com.periodicelements.crops.Genetics;

/** A crop sprayed with Bordeaux mixture stays on its farmland in the dark. */
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
	@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
	private void periodicElements$sprayed(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (world instanceof ServerWorld sw && world.getBlockState(pos.down()).isOf(Blocks.FARMLAND) && Sprayed.of(sw).contains(pos)) {
			cir.setReturnValue(true);
		}
	}

	/** 3.23.0: genes, climate and pests set the growth speed; a flowering plant is pollinated. */
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void periodicElements$genetics(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if (Genetics.randomTick(state, world, pos, random)) {
			ci.cancel();
		}
	}

	@Inject(method = "applyGrowth", at = @At("TAIL"))
	private void periodicElements$pollinate(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (world instanceof ServerWorld sw) {
			Genetics.afterGrowth(sw, pos);
		}
	}
}
