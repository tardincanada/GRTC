package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;

/**
 * ANFO Charge (2.0.0): prills soaked in diesel. A quarry blast: it breaks a 9x9x9 cube of everything weaker than
 * obsidian, and three blocks in four drop.
 */
public class AnfoChargeBlock extends MiningChargeBlock {
	public static final MapCodec<AnfoChargeBlock> CODEC = createCodec(AnfoChargeBlock::new);
	public static final float DROPS = 0.75f;

	public AnfoChargeBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	public int radius() {
		return 4;
	}

	@Override
	public float dropChance() {
		return DROPS;
	}

	@Override
	public float blastPower() {
		return 5f;
	}
}
