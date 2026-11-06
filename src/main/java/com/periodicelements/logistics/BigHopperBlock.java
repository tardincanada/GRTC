package com.periodicelements.logistics;

import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A large hopper (3.40.0): shaped, placed and switched off by redstone like the vanilla one, but with more places and
 * moving many items each time, to keep up with the faster belts.
 */
public class BigHopperBlock extends HopperBlock {
	private final Logistics.HopperTier tier;

	public BigHopperBlock(Settings settings, Logistics.HopperTier tier) {
		super(settings);
		this.tier = tier;
	}

	public Logistics.HopperTier tier() {
		return tier;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BigHopperBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Logistics.HOPPER_ENTITY,
				(w, pos, s, be) -> be.serverTick((ServerWorld) w, pos, s));
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof BigHopperBlockEntity hopper) {
			player.openHandledScreen(hopper);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		// Items are gathered in the block entity's tick.
	}
}
