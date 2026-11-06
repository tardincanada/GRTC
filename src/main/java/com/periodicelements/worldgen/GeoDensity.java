package com.periodicelements.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.periodicelements.PeriodicElements;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Locale;

/**
 * Density function {@code periodic_elements:geology}: one output of the {@link Geology} map. The world seed reaches
 * it through the {@code seed} noise, which the game seeds like any other noise.
 */
public record GeoDensity(Output output, DensityFunction.Noise seed) implements DensityFunction {
	public enum Output { SLOPED, HEIGHT, CONTINENTS, EROSION, RIDGES, DEPTH, SHALLOW, CAVES, CAVE_FILL, CAVE_FLOOD }

	public static final RegistryKey<DoublePerlinNoiseSampler.NoiseParameters> SEED_KEY =
			RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, Identifier.of(PeriodicElements.MOD_ID, "geology_seed"));

	private static final Codec<Output> OUTPUT_CODEC = Codec.STRING.xmap(
			s -> Output.valueOf(s.toUpperCase(Locale.ROOT)), o -> o.name().toLowerCase(Locale.ROOT));

	public static final MapCodec<GeoDensity> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			OUTPUT_CODEC.fieldOf("output").forGetter(GeoDensity::output),
			DensityFunction.Noise.CODEC.fieldOf("seed").forGetter(GeoDensity::seed)
	).apply(i, GeoDensity::new));

	/** A seed from a seeded noise sampler: the same in density functions and in the surface painter. */
	public static long seedOf(DoublePerlinNoiseSampler s) {
		long a = Double.doubleToLongBits(s.sample(0.5, 0.25, 0.75));
		long b = Double.doubleToLongBits(s.sample(1234.5, 7.25, -987.25));
		long c = Double.doubleToLongBits(s.sample(-31.125, 3.5, 4567.0625));
		return a * 0x9E3779B97F4A7C15L ^ Long.rotateLeft(b, 21) ^ Long.rotateLeft(c, 42) * 0xC2B2AE3D27D4EB4FL;
	}

	private Geology geology() {
		DoublePerlinNoiseSampler s = seed.noise();
		return s == null ? null : Geology.of(seedOf(s));
	}

	@Override
	public double sample(NoisePos pos) {
		Geology g = geology();
		if (g == null) return 0;
		int x = pos.blockX(), z = pos.blockZ();
		return switch (output) {
			case SLOPED -> (g.column(x, z).height - pos.blockY()) / 8.0;
			case HEIGHT -> g.column(x, z).height;
			case CONTINENTS -> g.continentalness(x, z);
			case EROSION -> g.erosion(x, z);
			case RIDGES -> g.weirdness(x, z);
			case DEPTH -> 1.5 - 3.0 * (pos.blockY() + 64) / 384.0 + g.offset(x, z);
			case SHALLOW -> shallow(g.column(x, z).height, pos.blockY());
			case CAVES -> g.caves(x, pos.blockY(), z);
			case CAVE_FILL -> g.caveFill(x, pos.blockY(), z);
			case CAVE_FLOOD -> g.caveFlood(x, pos.blockY(), z);
		};
	}

	@Override
	public void fill(double[] densities, EachApplier applier) {
		applier.fill(densities, this);
	}

	@Override
	public DensityFunction apply(DensityFunctionVisitor visitor) {
		return visitor.apply(new GeoDensity(output, visitor.apply(seed)));
	}

	@Override
	public double minValue() {
		return switch (output) {
			case SLOPED -> -50;
			case HEIGHT -> -64;
			case CONTINENTS, EROSION, RIDGES -> -1.2;
			case DEPTH -> -4;
			case SHALLOW, CAVE_FILL, CAVE_FLOOD -> -1;
			case CAVES -> -3;
		};
	}

	@Override
	public double maxValue() {
		return switch (output) {
			case SLOPED -> 50;
			case HEIGHT -> 320;
			case CONTINENTS, EROSION, RIDGES -> 1.2;
			case DEPTH -> 4;
			case SHALLOW, CAVE_FLOOD -> 2;
			case CAVE_FILL -> 1;
			case CAVES -> 4;
		};
	}

	@Override
	public CodecHolder<? extends DensityFunction> getCodecHolder() {
		return CodecHolder.of(CODEC);
	}

	/**
	 * Aquifer floodedness forced on just under shallow seas and low coasts (2 = always the sea's level, -1 = leave
	 * vanilla's): from the surface down to 20 blocks under it, where the column is at most a few blocks above sea.
	 */
	static double shallow(double height, int y) {
		double low = Geology.smooth(Geology.SEA + 12, Geology.SEA + 4, height);
		double near = MathHelper.clamp((y - (height - 20)) / 8.0, 0, 1);
		return -1 + 3 * low * near;
	}
}
