package com.periodicelements.mixin;

import com.periodicelements.weather.CloudCeiling;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fiat Lux requests P4 (3.49.2) and P5 (3.49.3, top fixed at Y 272): no rain above the shader's top cloud layer. In {@code tickRainSplashing} the rain
 * sound ({@code weather.rain} / {@code weather.rain.above}) is faded by {@link CloudCeiling#volume} at the block where
 * it plays, and rain splashes (and the smoke of rain on lava or a campfire) are not made above {@link CloudCeiling#top}.
 * Client only: nothing in gameplay changes.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererRainMixin {
	@Redirect(method = "tickRainSplashing", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
	private void periodic_elements$splashBelowClouds(ClientWorld world, ParticleEffect effect, double x, double y, double z,
			double vx, double vy, double vz) {
		if (CloudCeiling.splashes(y)) {
			world.addParticle(effect, x, y, z, vx, vy, vz);
		}
	}

	@Redirect(method = "tickRainSplashing", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/world/ClientWorld;playSoundAtBlockCenter(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
	private void periodic_elements$rainSoundBelowClouds(ClientWorld world, BlockPos pos, SoundEvent sound, SoundCategory category,
			float volume, float pitch, boolean useDistance) {
		float f = CloudCeiling.volume(pos.getY() + 0.5);
		if (f > 0) {
			world.playSoundAtBlockCenter(pos, sound, category, volume * f, pitch, useDistance);
		}
	}
}
