package com.periodicelements.mixin;

import com.periodicelements.worldgen.GeoWorld;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * In the Geological world type, biomes start cooling with altitude 20 blocks higher than vanilla's y=80, so snow
 * from height (snow layers, snowfall) begins that much higher.
 */
@Mixin(Biome.class)
public abstract class BiomeTemperatureMixin {
	@ModifyConstant(method = "computeTemperature", constant = @Constant(intValue = 80))
	private int periodicElements$coolingStartInt(int y) {
		return GeoWorld.coolingStart(y);
	}

	@ModifyConstant(method = "computeTemperature", constant = @Constant(floatValue = 80.0F))
	private float periodicElements$coolingStartFloat(float y) {
		return GeoWorld.coolingStart((int) y);
	}
}
