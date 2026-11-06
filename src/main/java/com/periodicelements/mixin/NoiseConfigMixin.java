package com.periodicelements.mixin;

import com.periodicelements.worldgen.GeoWorld;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 3.45.0: the Geological world's rivers need the climate before the first chunk's terrain is shaped (they do not
 * rise in deserts), so it is registered as soon as the world's noise is set up.
 */
@Mixin(NoiseConfig.class)
public abstract class NoiseConfigMixin {
	@Inject(method = "<init>(Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;Lnet/minecraft/registry/RegistryEntryLookup;J)V", at = @At("TAIL"))
	private void periodicElements$climate(ChunkGeneratorSettings settings, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> lookup,
			long seed, CallbackInfo ci) {
		GeoWorld.prepareClimate((NoiseConfig) (Object) this, settings);
	}
}
