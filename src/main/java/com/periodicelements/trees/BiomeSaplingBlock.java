package com.periodicelements.trees;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

import java.util.List;

/**
 * A sapling that grows only in the biomes its species comes from (Guilherme's rule, 2.8.0): anywhere else it stays a
 * sapling and bone meal is not used up. Desert and beach species also take root in sand and
 * terracotta; rocky-coast and mountain species (2.9.0) in stone and gravel;
 * tree-line and tundra species (3.0.0) also in snow and sand.
 */
public class BiomeSaplingBlock extends SaplingBlock {
	private final TagKey<Biome> biomes;
	private final List<String> biomeIds;
	private final String soil;
	/** The greenhouse climate it needs to grow away from its biomes (3.16.0). */
	private final String climate;

	public BiomeSaplingBlock(SaplingGenerator generator, TagKey<Biome> biomes, List<String> biomeIds, String soil, Settings settings) {
		super(generator, settings);
		this.biomes = biomes;
		this.biomeIds = List.copyOf(biomeIds);
		this.soil = soil;
		this.climate = com.periodicelements.shrubs.Greenhouse.climateOf(biomeIds);
	}

	public boolean growsAt(WorldView world, BlockPos pos) {
		return world.getBiome(pos).isIn(biomes) || com.periodicelements.shrubs.Greenhouse.allows(world, pos, climate);
	}

	/** The biomes this sapling grows in, by their game names ("Jungle, Sparse Jungle"). */
	public Text biomeNames() {
		return Texts.join(biomeIds.stream().map(b -> (Text) Text.translatable("biome.minecraft." + b)).toList(), Text.literal(", "));
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (growsAt(world, pos)) {
			super.randomTick(state, world, pos, random);
		}
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return growsAt(world, pos);
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		if (super.canPlantOnTop(floor, world, pos)) {
			return true;
		}
		return switch (soil) {
			case "sand" -> floor.isIn(BlockTags.SAND) || floor.isIn(BlockTags.TERRACOTTA);
			case "rock" -> floor.isIn(BlockTags.BASE_STONE_OVERWORLD) || floor.isOf(Blocks.GRAVEL) || floor.isOf(Blocks.COARSE_DIRT);
			case "alpine" -> floor.isIn(BlockTags.BASE_STONE_OVERWORLD) || floor.isOf(Blocks.GRAVEL) || floor.isOf(Blocks.COARSE_DIRT)
					|| floor.isOf(Blocks.SNOW_BLOCK) || floor.isOf(Blocks.POWDER_SNOW) || floor.isIn(BlockTags.SAND);
			default -> false;
		};
	}
}
