package com.periodicelements.space;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** The nose of the rocket: use a satellite on it to load it, an empty hand to take it out again. */
public class PayloadFairingBlock extends BlockWithEntity {
	public static final MapCodec<PayloadFairingBlock> CODEC = createCodec(PayloadFairingBlock::new);
	private static final VoxelShape SHAPE = VoxelShapes.union(createCuboidShape(1, 0, 1, 15, 6, 15),
			createCuboidShape(3, 6, 3, 13, 11, 13), createCuboidShape(5, 11, 5, 11, 14, 11), createCuboidShape(7, 14, 7, 9, 16, 9));

	public PayloadFairingBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PayloadFairingBlockEntity(pos, state);
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
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!Space.isPayload(stack)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient && world.getBlockEntity(pos) instanceof PayloadFairingBlockEntity f) {
			if (!f.payload.isEmpty()) {
				player.sendMessage(Text.translatable("message.periodic_elements.fairing.full", f.payload.getName())
						.formatted(Formatting.YELLOW), true);
				return ItemActionResult.SUCCESS;
			}
			f.payload = stack.copyWithCount(1);
			f.markDirty();
			if (!player.isCreative()) {
				stack.decrement(1);
			}
			world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.7f, 1.2f);
			player.sendMessage(Text.translatable("message.periodic_elements.fairing.loaded", f.payload.getName())
					.formatted(Formatting.GREEN), true);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof PayloadFairingBlockEntity f) {
			if (f.payload.isEmpty()) {
				player.sendMessage(Text.translatable("message.periodic_elements.fairing.empty"), true);
			} else {
				player.getInventory().offerOrDrop(f.payload);
				f.payload = ItemStack.EMPTY;
				f.markDirty();
				world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.7f, 1.2f);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && !Space.lifting
				&& world.getBlockEntity(pos) instanceof PayloadFairingBlockEntity f && !f.payload.isEmpty()) {
			ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), f.payload);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
