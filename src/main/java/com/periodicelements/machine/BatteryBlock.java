package com.periodicelements.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

/**
 * The Lithium-Ion Battery (3.41.0): instead of an animation it shows its charge, as a bar on the front that fills with
 * the charge and a button on top, both in the charge's colour: empty black, under 25% red, under 50% yellow, below
 * full green, full blue.
 */
public class BatteryBlock extends PowerBlock {
	/** Bar height in rows, 0 (empty) to {@link #ROWS} (full). */
	public static final int ROWS = 12;
	public static final IntProperty LEVEL = IntProperty.of("level", 0, ROWS);
	/** 0 empty (black), 1 red, 2 yellow, 3 green, 4 full (blue). */
	public static final IntProperty BAND = IntProperty.of("band", 0, 4);

	private final MapCodec<BatteryBlock> codec;

	public BatteryBlock(String powerId, Settings settings) {
		super(powerId, settings);
		this.codec = createCodec(s -> new BatteryBlock(powerId, s));
		setDefaultState(getDefaultState().with(LEVEL, 0).with(BAND, 0));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return codec;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(LEVEL, BAND);
	}

	/** The colour band for a fraction of full charge (0..1). */
	public static int band(double fraction) {
		if (fraction <= 1e-9) {
			return 0;
		}
		if (fraction >= 1 - 1e-9) {
			return 4;
		}
		return fraction < 0.25 ? 1 : fraction < 0.5 ? 2 : 3;
	}

	/** Bar rows for a fraction of full charge: any charge shows at least one row, only a full battery all of them. */
	public static int level(double fraction) {
		if (fraction <= 1e-9) {
			return 0;
		}
		if (fraction >= 1 - 1e-9) {
			return ROWS;
		}
		return Math.max(1, Math.min(ROWS - 1, (int) Math.round(fraction * ROWS)));
	}
}
