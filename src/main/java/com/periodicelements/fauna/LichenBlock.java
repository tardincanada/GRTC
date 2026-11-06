package com.periodicelements.fauna;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;

import java.util.Set;

/**
 * 3.50.0 (fauna stage 3): reindeer lichen, the winter food of reindeer. It grows low on soil, stone or snow in the cold
 * biomes and appears near players there, a few tufts at a time.
 */
public class LichenBlock extends PlantBlock {
	public static final MapCodec<LichenBlock> CODEC = createCodec(LichenBlock::new);
	public static final Set<String> BIOMES = Set.of("snowy_plains", "snowy_taiga", "ice_spikes", "snowy_slopes", "grove",
			"frozen_peaks", "taiga");
	private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 5, 14);

	public LichenBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return floor.isIn(BlockTags.DIRT) || floor.isIn(BlockTags.BASE_STONE_OVERWORLD) || floor.isOf(net.minecraft.block.Blocks.SNOW_BLOCK)
				|| floor.isOf(net.minecraft.block.Blocks.GRAVEL) || floor.isOf(net.minecraft.block.Blocks.MOSS_BLOCK);
	}

	/** Near a player in a cold biome, a few tufts of lichen may appear where there are none near. */
	static void seed(ServerWorld world, BlockPos center, Random r) {
		Block lichen = Fauna.block("reindeer_lichen");
		if (lichen == null || r.nextInt(3) != 0) return;
		int x = center.getX() + r.nextInt(65) - 32, z = center.getZ() + r.nextInt(65) - 32;
		if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
		BlockPos top = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
		String biome = world.getBiome(top).getKey().map(k -> k.getValue().getPath()).orElse("");
		if (!BIOMES.contains(biome)) return;
		for (BlockPos p : BlockPos.iterate(top.add(-8, -3, -8), top.add(8, 3, 8))) {
			if (world.getBlockState(p).isOf(lichen)) return;
		}
		for (int i = 0; i < 6; i++) {
			BlockPos p = top.add(r.nextInt(5) - 2, 0, r.nextInt(5) - 2);
			p = new BlockPos(p.getX(), world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()), p.getZ());
			BlockState here = world.getBlockState(p);
			if (here.isOf(net.minecraft.block.Blocks.SNOW)) {
				// under a thin snow cover: put the lichen in its place
				if (lichen.getDefaultState().canPlaceAt(world, p)) world.setBlockState(p, lichen.getDefaultState());
			} else if (here.isAir() && lichen.getDefaultState().canPlaceAt(world, p)) {
				world.setBlockState(p, lichen.getDefaultState());
			}
		}
	}
}
