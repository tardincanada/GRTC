package com.periodicelements.machine;

import com.mojang.serialization.MapCodec;
import com.periodicelements.Fluids;
import com.periodicelements.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A Storage Tank: use it with a filled flask or cylinder to pour it in, with an empty flask to draw one out. */
public class StorageTankBlock extends BlockWithEntity {
	public static final MapCodec<StorageTankBlock> CODEC = createCodec(StorageTankBlock::new);

	public StorageTankBlock(Settings settings) {
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

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new StorageTankBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModContent.TANK_ENTITY, (w, pos, s, be) -> be.tick());
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof StorageTankBlockEntity tank)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		boolean filled = Fluids.inFlask(stack.getItem()) || Fluids.isCylinder(stack.getItem());
		if (!filled && !stack.isOf(Fluids.GLASS_FLASK)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		if (filled) {
			if (tank.canPour(stack)) {
				give(player, hand, stack, tank.pour(stack));
			} else if (tank.fluid() != null && tank.fluid() != stack.getItem() && Fluids.gasOf(stack.getItem()) != tank.fluid()) {
				player.sendMessage(Text.translatable("gui.periodic_elements.tank.wrong", Text.translatable(tank.fluid().getTranslationKey())), true);
				return ItemActionResult.SUCCESS;
			} else {
				player.sendMessage(Text.translatable("gui.periodic_elements.tank.full"), true);
				return ItemActionResult.SUCCESS;
			}
		} else if (tank.canDraw()) {
			ItemStack out = tank.draw();
			stack.decrement(1);
			give(player, hand, stack, out);
		}
		player.sendMessage(tank.describe(), true);
		return ItemActionResult.SUCCESS;
	}

	/** Hands the player what came out: into the emptied hand, or the inventory. */
	private static void give(PlayerEntity player, Hand hand, ItemStack held, ItemStack back) {
		if (back.isEmpty()) {
			return;
		}
		if (held.isEmpty()) {
			player.setStackInHand(hand, back);
		} else {
			player.getInventory().offerOrDrop(back);
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof StorageTankBlockEntity tank) {
			if (player.isSneaking()) {
				// 3.38.0: sneak-use with an empty hand opens the relief valve: a gas goes into the air.
				com.periodicelements.fluid.FluidContent.vent(player, tank.fluid(), tank::vent);
				return ActionResult.SUCCESS;
			}
			player.sendMessage(tank.describe(), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof StorageTankBlockEntity tank ? tank.comparatorOutput() : 0;
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
