package com.periodicelements.mixin;

import com.periodicelements.explore.Explore;
import com.periodicelements.explore.HeadGearItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 3.47.0: through charged Night Vision Goggles, monsters within reach are outlined (seen through walls). */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow
	public ClientPlayerEntity player;

	@Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
	private void periodicElements$goggles(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (player != null && entity instanceof Monster && entity != player
				&& Explore.worn(player, HeadGearItem.Kind.GOGGLES) != null
				&& entity.squaredDistanceTo(player) <= (double) Explore.GOGGLES_REACH * Explore.GOGGLES_REACH) {
			cir.setReturnValue(true);
		}
	}
}
