package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Demolition Charge (1.9.0): cast TNT in a steel case. Lit like a Mining Charge, it breaks a 3x3x3 cube round it,
 * obsidian included, and drops every block; only unbreakable blocks (bedrock, barriers, portal frames) stop it.
 */
public class DemolitionChargeBlock extends MiningChargeBlock {
	public static final MapCodec<DemolitionChargeBlock> CODEC = createCodec(DemolitionChargeBlock::new);

	public DemolitionChargeBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	public int radius() {
		return 1;
	}

	@Override
	public boolean breaks(BlockState s, ServerWorld world, BlockPos p) {
		return s.getHardness(world, p) >= 0;
	}
}
