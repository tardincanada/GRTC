package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Shaped Charge (2.0.0): RDX behind a copper cone. Put against a wall and lit, its jet bores a 1x1 tunnel
 * {@link #DEPTH} blocks deep into the wall through anything but unbreakable blocks, and every block drops.
 */
public class ShapedChargeBlock extends MiningChargeBlock {
	public static final MapCodec<ShapedChargeBlock> CODEC = createCodec(ShapedChargeBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final int DEPTH = 16;

	public ShapedChargeBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getSide().getOpposite());
	}

	@Override
	public Iterable<BlockPos> targets(BlockState state, BlockPos center) {
		Direction d = state.contains(FACING) ? state.get(FACING) : Direction.NORTH;
		List<BlockPos> line = new ArrayList<>();
		for (int i = 1; i <= DEPTH; i++) {
			line.add(center.offset(d, i));
		}
		return line;
	}

	@Override
	public float blastPower() {
		return 2f;
	}

	@Override
	public boolean breaks(BlockState s, ServerWorld world, BlockPos p) {
		return s.getHardness(world, p) >= 0;
	}
}
