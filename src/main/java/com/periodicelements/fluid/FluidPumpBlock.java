package com.periodicelements.fluid;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 3.38.0: a Fluid Pump moves liquid or gas from the tanks behind it (its back, directly or through pipes) to the tanks
 * in front of it, {@link #RATE} units a tick. A redstone signal stops it.
 */
public class FluidPumpBlock extends BlockWithEntity {
	public static final MapCodec<FluidPumpBlock> CODEC = createCodec(FluidPumpBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final double RATE = 250;

	public FluidPumpBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		// It pumps away from the player, the way they look.
		return getDefaultState().with(FACING, ctx.getPlayerLookDirection())
				.with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		boolean powered = world.isReceivingRedstonePower(pos);
		if (powered != state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, powered), 3);
		}
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new Entity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, FluidContent.PUMP_ENTITY, (w, p, s, be) -> be.tick(w, p, s));
	}

	/** Moves fluid each tick; looks its two networks up again once a second. */
	public static class Entity extends BlockEntity {
		private List<FluidHolder> from = List.of(), to = List.of();
		private int timer;

		public Entity(BlockPos pos, BlockState state) {
			super(FluidContent.PUMP_ENTITY, pos, state);
		}

		/** Units moved in one tick (tests call it directly). */
		public double tick(World world, BlockPos pos, BlockState state) {
			if (state.get(POWERED)) {
				return 0;
			}
			Direction f = state.get(FACING);
			if (timer-- <= 0) {
				timer = 20;
				from = FluidNet.reach(world, pos, f.getOpposite());
				to = FluidNet.reach(world, pos, f);
			}
			for (FluidHolder src : from) {
				if (src.gone() || src.fluid() == null || to.contains(src)) {
					continue;
				}
				var fluid = src.fluid();
				double r = FluidStore.ratioOf(src.sample(), fluid);
				double want = src.drain(RATE, true);
				double room = 0;
				for (FluidHolder d : to) {
					if (!d.gone() && d != src) {
						room += d.room(fluid);
					}
				}
				double move = Math.min(want, room);
				if (move <= 1e-9) {
					continue;
				}
				List<FluidHolder> dest = to.stream().filter(d -> d != src && !d.gone()).toList();
				double sent = FluidNet.send(dest, fluid, move, r);
				src.drain(sent, false);
				return sent;
			}
			return 0;
		}
	}
}
