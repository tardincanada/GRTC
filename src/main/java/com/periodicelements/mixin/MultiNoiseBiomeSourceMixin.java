package com.periodicelements.mixin;

import com.periodicelements.worldgen.GeoWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In the Geological world type, the temperate band has no snowy mountain biomes, so tropical biomes never border
 * snow (Guilherme, 2026-11-03). Done here rather than with an inline biome table, which the game flags as an
 * experimental world.
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Inject(method = "getBiomeAtPoint", at = @At("RETURN"), cancellable = true)
	private void periodicElements$snowFreeTemperateMountains(MultiNoiseUtil.NoiseValuePoint point,
			CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
		if (!GeoWorld.active()) return;
		MultiNoiseBiomeSource self = (MultiNoiseBiomeSource) (Object) this;
		RegistryEntry<Biome> swapped = GeoWorld.snowFree(self, cir.getReturnValue(), point.temperatureNoise());
		if (swapped != null) cir.setReturnValue(swapped);
	}
}
