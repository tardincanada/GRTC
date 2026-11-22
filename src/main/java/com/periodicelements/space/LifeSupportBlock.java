package com.periodicelements.space;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * 3.64.0: Life Support. Feed it oxygen cylinders; it fills the room it stands in with air, if the room is sealed:
 * walls of full blocks, closed doors and trapdoors for airlocks. Use it with an empty hand to read its state.
 */
public class LifeSupportBlock extends BlockWithEntity {
	public static final MapCodec<LifeSupportBlock> CODEC = createCodec(LifeSupportBlock::new);
	/** Lit while the room it serves is sealed and has air. */
	public static final BooleanProperty LIT = Properties.LIT;

	public LifeSupportBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(LIT, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LifeSupportBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Moon.LIFE_SUPPORT_ENTITY,
				(w, pos, st, be) -> be.tick((ServerWorld) w, pos, st));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!stack.isOf(Registries.ITEM.get(Moon.id("oxygen_cylinder")))) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient && world.getBlockEntity(pos) instanceof LifeSupportBlockEntity be) {
			if (be.cylinders >= LifeSupportBlockEntity.MAX_CYLINDERS) {
				player.sendMessage(Text.translatable("message.periodic_elements.life_support.full").formatted(Formatting.YELLOW), true);
				return ItemActionResult.SUCCESS;
			}
			be.cylinders++;
			be.markDirty();
			if (!player.isCreative()) {
				stack.decrement(1);
				player.getInventory().offerOrDrop(new ItemStack(Registries.ITEM.get(Moon.id("steel_cylinder"))));
			}
			world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.6f, 1.4f);
			player.sendMessage(be.describe(), true);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof LifeSupportBlockEntity be) {
			be.measure((ServerWorld) world, pos);
			player.sendMessage(be.describe(), false);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof LifeSupportBlockEntity be) {
			LifeSupportBlockEntity.forget(world, pos);
			if (be.cylinders > 0) {
				ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(),
						new ItemStack(Registries.ITEM.get(Moon.id("oxygen_cylinder")), be.cylinders));
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
