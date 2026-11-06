package com.periodicelements.mixin;

import com.periodicelements.water.Water;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 3.45.0: once a chunk's features are in, the sea's water turns salt and brine springs are dug (any world type). */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
	@Inject(method = "generateFeatures", at = @At("TAIL"))
	private void periodicElements$saltSea(StructureWorldAccess world, Chunk chunk, StructureAccessor structures, CallbackInfo ci) {
		Water.afterFeatures(world, chunk);
	}
}
