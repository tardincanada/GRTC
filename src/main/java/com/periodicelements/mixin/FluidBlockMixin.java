package com.periodicelements.mixin;

import com.periodicelements.water.Water;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 3.45.0: a fresh water source that appears inside the sea (salt water on three or four sides) is sea water. */
@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {
	@Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
	private void periodicElements$intoTheSea(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
		if (!world.isClient && state.isOf(Blocks.WATER) && state.getFluidState().isStill() && Water.drownedInSea(world, pos)) {
			world.setBlockState(pos, Water.SALT_WATER_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
			ci.cancel();
		}
	}
}
