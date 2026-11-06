package com.periodicelements.explore;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * A hide tent (3.47.0): sleep in it to pass the night as in a bed, far from home. It never moves your spawn point
 * (see Explore). Sneak and use it with an empty hand to strike it: it goes back into your pack.
 */
public class TentBlock extends HorizontalFacingBlock {
	public static final MapCodec<TentBlock> CODEC = createCodec(TentBlock::new);
	private static final VoxelShape FLOOR = Block.createCuboidShape(0, 0, 0, 16, 2, 16);
	private static final VoxelShape OUTLINE = VoxelShapes.union(Block.createCuboidShape(0, 0, 0, 16, 4, 16),
			Block.createCuboidShape(2, 4, 2, 14, 8, 14), Block.createCuboidShape(4, 8, 4, 12, 12, 12),
			Block.createCuboidShape(6, 12, 6, 10, 15, 10));

	public TentBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
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
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FLOOR;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.CONSUME;
		}
		if (player.isSneaking()) {
			world.removeBlock(pos, false);
			world.playSound(null, pos, BlockSoundGroup.WOOL.getBreakSound(), SoundCategory.BLOCKS, 1f, 0.9f);
			if (!player.getAbilities().creativeMode) {
				player.getInventory().offerOrDrop(new ItemStack(Explore.TENT));
			}
			return ActionResult.SUCCESS;
		}
		if (!world.getDimension().bedWorks()) {
			player.sendMessage(Text.translatable("message.periodic_elements.tent.no_sleep"), true);
			return ActionResult.SUCCESS;
		}
		player.trySleep(pos).ifLeft(reason -> {
			if (reason.getMessage() != null) {
				player.sendMessage(reason.getMessage(), true);
			}
		});
		return ActionResult.SUCCESS;
	}
}
