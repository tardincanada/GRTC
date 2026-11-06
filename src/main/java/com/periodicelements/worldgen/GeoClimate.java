package com.periodicelements.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Locale;

/**
 * 3.49.0, density function {@code periodic_elements:climate}: vanilla's shifted temperature or humidity noise, with its
 * horizontal scale divided by the biome size the world was made with ({@link GeoOptions#current()}). The same as
 * {@code minecraft:shifted_noise} with {@code xz_scale} = base / size and {@code y_scale} 0.
 */
public record GeoClimate(Kind kind, double baseScale, DensityFunction shiftX, DensityFunction shiftZ, DensityFunction.Noise noise)
		implements DensityFunction {
	public enum Kind { TEMPERATURE, HUMIDITY }

	private static final Codec<Kind> KIND_CODEC = Codec.STRING.xmap(
			s -> Kind.valueOf(s.toUpperCase(Locale.ROOT)), k -> k.name().toLowerCase(Locale.ROOT));

	public static final MapCodec<GeoClimate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			KIND_CODEC.fieldOf("kind").forGetter(GeoClimate::kind),
			Codec.DOUBLE.fieldOf("xz_scale").forGetter(GeoClimate::baseScale),
			DensityFunction.FUNCTION_CODEC.fieldOf("shift_x").forGetter(GeoClimate::shiftX),
			DensityFunction.FUNCTION_CODEC.fieldOf("shift_z").forGetter(GeoClimate::shiftZ),
			DensityFunction.Noise.CODEC.fieldOf("noise").forGetter(GeoClimate::noise)
	).apply(i, GeoClimate::new));

	/** The horizontal scale for the current world's options. */
	public double scale() {
		GeoOptions o = GeoOptions.current();
		return baseScale / (kind == Kind.TEMPERATURE ? o.temperatureSize() : o.humiditySize());
	}

	@Override
	public double sample(NoisePos pos) {
		double s = scale();
		return noise.sample(pos.blockX() * s + shiftX.sample(pos), 0, pos.blockZ() * s + shiftZ.sample(pos));
	}

	@Override
	public void fill(double[] densities, EachApplier applier) {
		applier.fill(densities, this);
	}

	@Override
	public DensityFunction apply(DensityFunctionVisitor visitor) {
		return visitor.apply(new GeoClimate(kind, baseScale, shiftX.apply(visitor), shiftZ.apply(visitor), visitor.apply(noise)));
	}

	@Override
	public double minValue() {
		return -maxValue();
	}

	@Override
	public double maxValue() {
		return noise.getMaxValue();
	}

	@Override
	public CodecHolder<? extends DensityFunction> getCodecHolder() {
		return CodecHolder.of(CODEC);
	}
}
