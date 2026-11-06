package com.periodicelements.atlas;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A Radar Station (1940) or a Tracking Station (1957). Both draw power from the wire network; the radar shows its
 * owner the creatures and players within 96 blocks, the tracking station takes in what its owner's satellites see.
 */
public class StationBlock extends BlockWithEntity {
	public enum Kind {
		/** kJ a tick each needs to run. */
		RADAR(1.0), TRACKING(2.0);

		public final double rate;

		Kind(double rate) {
			this.rate = rate;
		}
	}

	private final Kind kind;

	public StationBlock(Settings settings, Kind kind) {
		super(settings);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(s -> new StationBlock(s, kind));
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new StationBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Atlas.STATION_ENTITY, (w, pos, s, be) -> be.tick((ServerWorld) w, pos));
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		if (world instanceof ServerWorld sw && world.getRegistryKey() == World.OVERWORLD && placer instanceof ServerPlayerEntity p) {
			StationState.get(sw.getServer()).add(pos, kind, p.getUuid());
			if (world.getBlockEntity(pos) instanceof StationBlockEntity be) be.setOwner(p.getUuid());
			p.sendMessage(Text.translatable("gui.periodic_elements.atlas." + (kind == Kind.RADAR ? "radar" : "tracking") + ".placed"), true);
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world instanceof ServerWorld sw) {
			player.sendMessage(Satellites.describe(sw, pos, kind), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world instanceof ServerWorld sw) {
			StationState s = StationState.get(sw.getServer());
			if (s.stations.remove(pos) != null) s.markDirty();
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
