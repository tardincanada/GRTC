package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * The light of a Carbide Lamp: an invisible, replaceable block put where the lamp's holder stands. It checks itself
 * every {@link #CHECK} ticks and goes out once no lit lamp is near, so it never stays behind.
 */
public class LampLightBlock extends Block {
	public static final MapCodec<LampLightBlock> CODEC = createCodec(LampLightBlock::new);
	public static final int LIGHT = 14;
	public static final int CHECK = 10;
	/** How far from the light a lit lamp's holder may be and keep it on. */
	public static final double REACH = 1.6;

	public LampLightBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	/** Lights the block at the player's head, or at their feet if the head block is taken. */
	public static void lightAround(ServerWorld world, PlayerEntity p) {
		BlockPos head = BlockPos.ofFloored(p.getEyePos());
		for (BlockPos at : new BlockPos[]{head, p.getBlockPos()}) {
			BlockState s = world.getBlockState(at);
			if (s.isOf(Industry.LAMP_LIGHT)) {
				return;
			}
			if (s.isAir()) {
				world.setBlockState(at, Industry.LAMP_LIGHT.getDefaultState(), Block.NOTIFY_LISTENERS);
				world.scheduleBlockTick(at, Industry.LAMP_LIGHT, CHECK);
				return;
			}
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean near = false;
		for (PlayerEntity p : world.getPlayers()) {
			if (Industry.litLamp(p) != null && p.getPos().add(0, 1, 0).isInRange(pos.toCenterPos(), REACH + 0.8)) {
				near = true;
				break;
			}
		}
		if (near) {
			world.scheduleBlockTick(pos, this, CHECK);
		} else {
			world.removeBlock(pos, false);
		}
	}
}
