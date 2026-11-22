package com.periodicelements.shrubs;

import com.periodicelements.PeriodicElements;
import com.periodicelements.trees.BiomeSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * The warning shown when looking at a plant that will not grow where it stands (3.54.0, Guilherme): a tree sapling or
 * a shrub of the mod outside its biomes says "This plant doesn't grow here"; inside a greenhouse whose climate does
 * not suit it, "This plant doesn't grow in this environment". A plant that grows (in its biome, or in a fitting
 * greenhouse) and a full-grown shrub say nothing. Shown under the crosshair and in Jade's panel.
 */
public final class PlantWarning {
	public static final String HERE = "gui." + PeriodicElements.MOD_ID + ".plant.not_here";
	public static final String ENVIRONMENT = "gui." + PeriodicElements.MOD_ID + ".plant.wrong_environment";

	private PlantWarning() {
	}

	/** The translation key of the warning for the plant {@code state} at {@code pos}, or null when there is none. */
	@Nullable
	public static String key(WorldView world, BlockPos pos, BlockState state) {
		String climate;
		if (state.getBlock() instanceof BiomeSaplingBlock sapling) {
			if (sapling.inBiome(world, pos)) {
				return null;
			}
			climate = sapling.climate();
		} else if (state.getBlock() instanceof ShrubBlock bush) {
			if (state.get(ShrubBlock.AGE) >= 3 || bush.inBiome(world, pos)) {
				return null;
			}
			climate = bush.climate;
		} else {
			return null;
		}
		String here = Greenhouse.climateAt(world, pos);
		if (here == null) {
			return HERE;
		}
		return Greenhouse.allows(world, pos, climate) ? null : ENVIRONMENT;
	}

	/** Whether {@code state} is a plant this warning can apply to (cheap; no world lookups). */
	public static boolean applies(BlockState state) {
		return state.getBlock() instanceof BiomeSaplingBlock || state.getBlock() instanceof ShrubBlock;
	}

	@Nullable
	public static Text text(WorldView world, BlockPos pos, BlockState state) {
		String k = key(world, pos, state);
		return k == null ? null : Text.translatable(k);
	}
}
