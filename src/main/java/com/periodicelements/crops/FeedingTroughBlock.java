package com.periodicelements.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Feeding Trough (3.23.0, Animal Husbandry): holds one kind of feed and feeds the animals around it. */
public class FeedingTroughBlock extends BlockWithEntity {
	public static final MapCodec<FeedingTroughBlock> CODEC = createCodec(FeedingTroughBlock::new);
	private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 8, 16);

	public FeedingTroughBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FeedingTroughBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Crops.TROUGH_ENTITY, (w, pos, s, be) -> be.tick(w, pos));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			if (!trough.insert(stack)) {
				player.sendMessage(Text.translatable("gui.periodic_elements.trough.full"), true);
			} else {
				player.sendMessage(trough.contents(), true);
			}
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
			player.sendMessage(trough.contents(), true);
		}
		return net.minecraft.util.ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
			ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), trough.feed());
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
