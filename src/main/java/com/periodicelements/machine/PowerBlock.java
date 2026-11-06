package com.periodicelements.machine;

import com.mojang.serialization.MapCodec;
import com.periodicelements.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A Voltaic Pile or Steam Engine block (machines.py POWER). */
public class PowerBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	private final String powerId;
	private final MapCodec<PowerBlock> codec;

	public PowerBlock(String powerId, Settings settings) {
		super(settings);
		this.powerId = powerId;
		this.codec = createCodec(s -> new PowerBlock(powerId, s));
		setDefaultState(getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(LIT, false));
	}

	public String powerId() {
		return powerId;
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return codec;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
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
		return new PowerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModContent.POWER_ENTITY,
				(w, pos, s, be) -> be.tick((ServerWorld) w, pos, s));
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof PowerBlockEntity be) {
			player.openHandledScreen(be);
			player.sendMessage(be.describe(), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		// 3.41.0: only engines that burn fuel smoke: grey from the steam engine, black from the diesel generator.
		if (!state.get(LIT) || !(powerId.equals("steam_engine") || powerId.equals("diesel_generator"))) {
			return;
		}
		double x = pos.getX() + 0.5, y = pos.getY() + 1.05, z = pos.getZ() + 0.5;
		if (powerId.equals("steam_engine")) {
			world.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.4, y,
					z + (random.nextDouble() - 0.5) * 0.4, 0, 0.05, 0);
			if (random.nextInt(4) == 0) {
				world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0, 0.04, 0);
			}
		} else {
			for (int i = 0; i < 2; i++) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, x + (random.nextDouble() - 0.5) * 0.3, y,
						z + (random.nextDouble() - 0.5) * 0.3, 0, 0.06, 0);
			}
			if (random.nextInt(3) == 0) {
				world.addParticle(ParticleTypes.SQUID_INK, x, y + 0.1, z, 0, 0.08, 0);
			}
		}
	}
}
