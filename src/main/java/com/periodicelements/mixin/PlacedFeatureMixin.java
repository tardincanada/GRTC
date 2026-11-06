package com.periodicelements.mixin;

import com.periodicelements.worldgen.Deposits;
import com.periodicelements.worldgen.GeoWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** In the Geological world type, the veins that the rock-bound deposits replace do not generate. */
@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {
	@Inject(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Z",
			at = @At("HEAD"), cancellable = true)
	private void periodicElements$depositsReplaceVeins(StructureWorldAccess world, ChunkGenerator generator, Random random,
			BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (generator instanceof NoiseChunkGenerator n && n.matchesSettings(GeoWorld.SETTINGS)) {
			var id = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE).getId((PlacedFeature) (Object) this);
			if (id != null && Deposits.replacesVein(id)) cir.setReturnValue(false);
		}
	}
}
