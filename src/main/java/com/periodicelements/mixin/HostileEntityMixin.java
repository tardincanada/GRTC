package com.periodicelements.mixin;

import com.periodicelements.explore.Explore;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 3.47.0: monsters that need the dark do not spawn by themselves within reach of a lit street lamp. */
@Mixin(HostileEntity.class)
public abstract class HostileEntityMixin {
	@Inject(method = "canSpawnInDark", at = @At("HEAD"), cancellable = true)
	private static void periodicElements$streetLamps(EntityType<? extends HostileEntity> type, ServerWorldAccess world,
			SpawnReason reason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
		if ((reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION)
				&& Explore.LitLamps.covers(world.toServerWorld(), pos)) {
			cir.setReturnValue(false);
		}
	}
}
