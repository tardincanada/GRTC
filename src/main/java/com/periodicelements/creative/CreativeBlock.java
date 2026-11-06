package com.periodicelements.creative;

import com.mojang.serialization.MapCodec;
import com.periodicelements.Fluids;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** 3.51.0: the five creative test blocks; see {@link CreativeItems}. */
public class CreativeBlock extends BlockWithEntity {
	public enum Kind { GENERATOR, FLUID_SOURCE, ITEM_SOURCE, VOID, ACCELERATOR }

	public static final MapCodec<CreativeBlock> CODEC = createCodec(s -> new CreativeBlock(Kind.VOID, s));
	public final Kind kind;

	public CreativeBlock(Kind kind, Settings settings) {
		super(settings);
		this.kind = kind;
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
		return kind == Kind.GENERATOR ? new CreativeGeneratorBlockEntity(pos, state) : new CreativeBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (world.isClient || kind != Kind.ACCELERATOR) {
			return null;
		}
		return validateTicker(type, CreativeItems.CREATIVE_ENTITY, (w, pos, s, be) -> be.accelerate((ServerWorld) w, pos));
	}

	/**
	 * Fluid source: a flask, a cylinder or a bucket of something picks the fluid; an empty flask or cylinder is filled.
	 * Item source: any item picks what it gives. An empty hand while sneaking clears either.
	 */
	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof CreativeBlockEntity be)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (kind == Kind.ITEM_SOURCE) {
			if (!world.isClient) {
				be.setTemplate(stack);
				player.sendMessage(Text.translatable("message.periodic_elements.creative_item_source.set", stack.getName()), true);
			}
			return ItemActionResult.success(world.isClient);
		}
		if (kind != Kind.FLUID_SOURCE) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (Fluids.isEmptyContainer(stack)) {
			if (!world.isClient) {
				ItemStack filled = be.fillContainer(stack);
				if (filled.isEmpty()) {
					player.sendMessage(Text.translatable("message.periodic_elements.creative_fluid_source.wrong_container"), true);
				} else {
					if (!player.getAbilities().creativeMode) {
						stack.decrement(1);
					}
					if (!player.getInventory().insertStack(filled)) {
						player.dropItem(filled, false);
					}
				}
			}
			return ItemActionResult.success(world.isClient);
		}
		var fluid = CreativeBlockEntity.fluidOf(stack);
		if (fluid == null) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			be.setFluid(fluid);
			player.sendMessage(Text.translatable("message.periodic_elements.creative_fluid_source.set", fluid.getName()), true);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}
		if (world.getBlockEntity(pos) instanceof CreativeBlockEntity be) {
			if (player.isSneaking()) {
				be.clear();
				be.setFluid(null);
				player.sendMessage(Text.translatable("message.periodic_elements.creative.cleared"), true);
			} else {
				player.sendMessage(be.describe(kind), true);
			}
			return ActionResult.CONSUME;
		}
		if (world.getBlockEntity(pos) instanceof CreativeGeneratorBlockEntity g) {
			player.sendMessage(g.describe(), true);
			return ActionResult.CONSUME;
		}
		return ActionResult.PASS;
	}

	static String id(net.minecraft.item.Item item) {
		return Registries.ITEM.getId(item).toString();
	}
}
