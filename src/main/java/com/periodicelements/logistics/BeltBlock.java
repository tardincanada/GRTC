package com.periodicelements.logistics;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A conveyor belt (3.40.0): a low slab that carries its load one block forward every {@link Logistics.BeltTier#interval()}
 * ticks, into the next belt or into whatever inventory is in front (chest, hopper, machine, sorter). Hoppers, Output
 * Pushers and other belts feed it from behind or the sides, and it picks up items dropped on it. It also carries
 * players and mobs standing on it (sneak to stand still).
 */
public class BeltBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);
	private final Logistics.BeltTier tier;

	public BeltBlock(Settings settings, Logistics.BeltTier tier) {
		super(settings);
		this.tier = tier;
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
	}

	public Logistics.BeltTier tier() {
		return tier;
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(s -> new BeltBlock(s, Logistics.CANVAS));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
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
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BeltBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? validateTicker(type, Logistics.BELT_ENTITY, (w, pos, s, be) -> be.clientTick())
				: validateTicker(type, Logistics.BELT_ENTITY, (w, pos, s, be) -> be.serverTick((ServerWorld) w, pos, s));
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!entity.isSneaking() || !(entity instanceof PlayerEntity)) {
			Direction d = state.get(FACING);
			double speed = tier.blocksPerSecond() / 20.0;
			var v = entity.getVelocity();
			double along = v.x * d.getOffsetX() + v.z * d.getOffsetZ();
			if (along < speed) {
				double add = Math.min(speed - along, speed * 0.5);
				entity.setVelocity(v.add(d.getOffsetX() * add, 0, d.getOffsetZ() * add));
				entity.velocityModified = true;
			}
		}
		super.onSteppedOn(world, pos, state, entity);
	}

	/** Using an empty hand on a belt takes its load. */
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.getBlockEntity(pos) instanceof BeltBlockEntity belt && !belt.getStack(0).isEmpty()) {
			if (!world.isClient) {
				ItemStack load = belt.removeStack(0);
				player.getInventory().offerOrDrop(load);
			}
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
