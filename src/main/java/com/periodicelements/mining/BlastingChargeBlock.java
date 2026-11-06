package com.periodicelements.mining;

import com.mojang.serialization.MapCodec;
import com.periodicelements.farm.MiningChargeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * The level 2 Blasting Charge: black powder in a drilled hole. Breaks a 3x3x3 block of stone-type blocks (those a
 * pickaxe mines), drops every one, and never breaks anything with a block entity, so machines are safe.
 */
public class BlastingChargeBlock extends MiningChargeBlock {
	public static final MapCodec<BlastingChargeBlock> CODEC = createCodec(BlastingChargeBlock::new);

	public BlastingChargeBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends net.minecraft.block.Block> getCodec() {
		return CODEC;
	}

	@Override
	public int radius() {
		return 1;
	}

	@Override
	public float blastPower() {
		return 2f;
	}

	@Override
	public boolean breaks(BlockState s, ServerWorld world, BlockPos p) {
		return s.isIn(BlockTags.PICKAXE_MINEABLE) && !s.hasBlockEntity() && super.breaks(s, world, p);
	}
}
