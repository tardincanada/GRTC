package com.periodicelements.mixin;

import com.periodicelements.worldgen.GeoWorld;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** In the Geological world type, paints the province's rocks after the vanilla surface rules. */
@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {
	@Inject(method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V",
			at = @At("TAIL"))
	private void periodicElements$paintGeology(ChunkRegion region, StructureAccessor structures, NoiseConfig config,
			Chunk chunk, CallbackInfo ci) {
		if (((NoiseChunkGenerator) (Object) this).matchesSettings(GeoWorld.SETTINGS)) {
			GeoWorld.paint(chunk, config);
		}
	}

	@Inject(method = "carve", at = @At("TAIL"))
	private void periodicElements$repairRivers(ChunkRegion region, long seed, NoiseConfig config,
			net.minecraft.world.biome.source.BiomeAccess biomes, StructureAccessor structures, Chunk chunk,
			net.minecraft.world.gen.GenerationStep.Carver step, CallbackInfo ci) {
		if (((NoiseChunkGenerator) (Object) this).matchesSettings(GeoWorld.SETTINGS)) {
			GeoWorld.repairRivers(chunk, config);
			com.periodicelements.worldgen.Deposits.place(chunk, config);
		}
	}
}
