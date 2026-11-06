package com.periodicelements.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Silo, Seed Irradiator and Thermocycler (3.37.0): use an item on it to fill it, an empty hand to take out. */
public class CropMachineBlock extends BlockWithEntity {
	public enum Kind {
		SILO("silo"), IRRADIATOR("seed_irradiator"), THERMOCYCLER("thermocycler");

		public final String id;

		Kind(String id) {
			this.id = id;
		}
	}

	private final Kind kind;

	public CropMachineBlock(Kind kind, Settings settings) {
		super(settings);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return MapCodec.unit(() -> this);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CropMachineBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Crops.MACHINE_ENTITY, (w, pos, s, be) -> be.tick((ServerWorld) w, pos));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof CropMachineBlockEntity be)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!world.isClient) {
			if (kind == Kind.THERMOCYCLER && player.isSneaking() && Crops.isSeed(stack)) {
				be.setMarker(stack.getOrDefault(Crops.GENOME, Genome.DEFAULT));
				player.sendMessage(Text.translatable("gui.periodic_elements.thermocycler.marker", stack.getName()), true);
			} else if (be.insert(stack)) {
				player.sendMessage(be.describe(), true);
			} else {
				player.sendMessage(Text.translatable("gui.periodic_elements." + kind.id + ".wrong"), true);
			}
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof CropMachineBlockEntity be) {
			be.takeOutputs(player);
			player.sendMessage(be.describe(), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof CropMachineBlockEntity be) {
			ItemScatterer.spawn(world, pos, be);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
