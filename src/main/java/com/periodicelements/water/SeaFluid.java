package com.periodicelements.water;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Salt water and brine (3.45.0): flow, look and act like water (they are in the minecraft:water fluid tag, so
 * entities swim in them, lava turns to stone, sponges soak them up). Salt water and fresh water count as one kind
 * for flowing and for infinite sources (WaterFluidMixin, FlowableFluidMixin); brine never makes new sources.
 */
public abstract class SeaFluid extends FlowableFluid {
	/** True for salt water, false for brine. */
	protected abstract boolean salt();

	@Override
	public Fluid getFlowing() {
		return salt() ? Water.FLOWING_SALT_WATER : Water.FLOWING_BRINE;
	}

	@Override
	public Fluid getStill() {
		return salt() ? Water.SALT_WATER : Water.BRINE;
	}

	@Override
	public Item getBucketItem() {
		return salt() ? Water.SALT_WATER_BUCKET : Water.BRINE_BUCKET;
	}

	@Override
	@Nullable
	public ParticleEffect getParticle() {
		return ParticleTypes.DRIPPING_WATER;
	}

	@Override
	protected boolean isInfinite(World world) {
		return salt() && world.getGameRules().getBoolean(GameRules.WATER_SOURCE_CONVERSION);
	}

	@Override
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
		BlockEntity be = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world, pos, be);
	}

	@Override
	public int getMaxFlowDistance(WorldView world) {
		return 4;
	}

	@Override
	public BlockState toBlockState(FluidState state) {
		Block block = salt() ? Water.SALT_WATER_BLOCK : Water.BRINE_BLOCK;
		return block.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
	}

	@Override
	public boolean matchesType(Fluid fluid) {
		return salt() ? Water.freshOrSalt(fluid) : fluid == Water.BRINE || fluid == Water.FLOWING_BRINE;
	}

	@Override
	public int getLevelDecreasePerBlock(WorldView world) {
		return 1;
	}

	@Override
	public int getTickRate(WorldView world) {
		return 5;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
		return direction == Direction.DOWN && !fluid.isIn(FluidTags.WATER);
	}

	@Override
	protected float getBlastResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getBucketFillSound() {
		return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
	}

	public abstract static class Flowing extends SeaFluid {
		@Override
		protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getLevel(FluidState state) {
			return state.get(LEVEL);
		}

		@Override
		public boolean isStill(FluidState state) {
			return false;
		}
	}

	public abstract static class Still extends SeaFluid {
		@Override
		public int getLevel(FluidState state) {
			return 8;
		}

		@Override
		public boolean isStill(FluidState state) {
			return true;
		}
	}
}
