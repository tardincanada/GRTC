package com.periodicelements.trees;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

/**
 * A palm crown: a tuft on top of the trunk and eight fronds that arch out and droop. Every frond leaf touches the one
 * before it face to face, so the leaves keep their distance to the trunk and do not decay.
 */
public class PalmFoliagePlacer extends FoliagePlacer {
	public static final MapCodec<PalmFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
			instance -> fillFoliagePlacerFields(instance).apply(instance, PalmFoliagePlacer::new));
	private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

	public PalmFoliagePlacer(IntProvider radius, IntProvider offset) {
		super(radius, offset);
	}

	@Override
	protected FoliagePlacerType<?> getType() {
		return Trees.PALM_FOLIAGE;
	}

	@Override
	protected void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight,
							TreeNode node, int foliageHeight, int radius, int offset) {
		BlockPos top = node.getCenter().up(offset);
		placeFoliageBlock(world, placer, random, config, top);
		placeFoliageBlock(world, placer, random, config, top.up());
		for (int[] d : DIRS) {
			int len = d[0] != 0 && d[1] != 0 ? radius : radius + 1;
			int x = 0, y = 0, z = 0;
			for (int i = 1; i <= len; i++) {
				// One axis at a time, so each leaf shares a face with the last.
				if (d[0] != 0) {
					x += d[0];
					placeFoliageBlock(world, placer, random, config, top.add(x, y, z));
				}
				if (d[1] != 0) {
					z += d[1];
					placeFoliageBlock(world, placer, random, config, top.add(x, y, z));
				}
				int droop = (i * i) / (len + 1);
				while (-y < droop) {
					y--;
					placeFoliageBlock(world, placer, random, config, top.add(x, y, z));
				}
			}
		}
	}

	@Override
	public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
		return 2;
	}

	@Override
	protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
		return false;
	}
}
