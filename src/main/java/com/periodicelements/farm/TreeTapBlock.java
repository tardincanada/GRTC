package com.periodicelements.farm;

import com.mojang.serialization.MapCodec;
import com.periodicelements.Fluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * A Tree Tap: an iron spout driven into the bark of a rubber log, with a flask under it. FACING points away from the
 * log. Use it with an empty Glass Flask to take the latex; hoppers put empty flasks in from above or the sides and take
 * latex out from below.
 */
public class TreeTapBlock extends BlockWithEntity {
	public static final MapCodec<TreeTapBlock> CODEC = createCodec(TreeTapBlock::new);
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	private static final VoxelShape NORTH = Block.createCuboidShape(5, 2, 0, 11, 10, 9);
	private static final VoxelShape SOUTH = Block.createCuboidShape(5, 2, 7, 11, 10, 16);
	private static final VoxelShape WEST = Block.createCuboidShape(0, 2, 5, 9, 10, 11);
	private static final VoxelShape EAST = Block.createCuboidShape(7, 2, 5, 16, 10, 11);

	public TreeTapBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		// The log is behind the tap (opposite FACING), so the shape hugs that side.
		return switch (state.get(FACING)) {
			case SOUTH -> NORTH;
			case NORTH -> SOUTH;
			case EAST -> WEST;
			default -> EAST;
		};
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction side = ctx.getSide();
		if (!side.getAxis().isHorizontal()) {
			return null;
		}
		BlockState state = getDefaultState().with(FACING, side);
		return canPlaceAt(state, ctx.getWorld(), ctx.getBlockPos()) ? state : null;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState log = world.getBlockState(pos.offset(state.get(FACING).getOpposite()));
		return log.isOf(Farm.RUBBER_LOG) || com.periodicelements.trees.Trees.tapped(log) != null;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world,
												   BlockPos pos, BlockPos neighborPos) {
		return direction == state.get(FACING).getOpposite() && !canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TreeTapBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Farm.TAP_ENTITY, (w, pos, s, be) -> be.tick(w, pos, s));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!stack.isOf(Fluids.GLASS_FLASK) || !(world.getBlockEntity(pos) instanceof TreeTapBlockEntity tap)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		if (tap.latex() > 0) {
			stack.decrement(1);
			ItemStack latex = tap.take();
			if (stack.isEmpty()) {
				player.setStackInHand(hand, latex);
			} else {
				player.getInventory().offerOrDrop(latex);
			}
		}
		player.sendMessage(tap.describe(world), true);
		return ItemActionResult.SUCCESS;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof TreeTapBlockEntity tap) {
			// Hard resins need no flask: an empty hand takes them.
			if (!tap.needsFlask() && tap.latex() > 0) {
				player.getInventory().offerOrDrop(tap.take());
			}
			player.sendMessage(tap.describe(world), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
