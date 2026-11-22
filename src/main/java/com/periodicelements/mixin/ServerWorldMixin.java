package com.periodicelements.mixin;

import com.periodicelements.trees.LeafFall;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 3.55.0: when a log goes, the leaves around it are checked ({@link LeafFall}). */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Inject(method = "onBlockChanged", at = @At("HEAD"))
	private void periodicElements$logGone(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
		LeafFall.changed((ServerWorld) (Object) this, pos, oldBlock, newBlock);
	}
}
