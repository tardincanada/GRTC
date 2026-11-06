package com.periodicelements.explore;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A street lamp head (3.47.0), set on a fence, wall or post. Fed by copper wire touching the lamp or the foot of its
 * post, it lights (light 15) and keeps monsters from spawning within its radius. The Sodium Lamp (low-pressure sodium,
 * Philips 1932) has the yellow 589 nm light; the LED Lamp (blue gallium nitride under a phosphor) a cold white one,
 * farther and on less power.
 */
public class StreetLampBlock extends BlockWithEntity {
	public static final BooleanProperty LIT = Properties.LIT;
	private static final VoxelShape SHAPE = Block.createCuboidShape(3, 4, 3, 13, 12, 13);

	public enum Kind {
		SODIUM(24, 0.05), LED(40, 0.02);

		private final int radius;
		private final double kJPerTick;

		Kind(int radius, double kJPerTick) {
			this.radius = radius;
			this.kJPerTick = kJPerTick;
		}

		public int radius() {
			return radius;
		}

		public double kJPerTick() {
			return kJPerTick;
		}
	}

	private final Kind kind;

	public StreetLampBlock(Kind kind, Settings settings) {
		super(settings);
		this.kind = kind;
		setDefaultState(getDefaultState().with(LIT, false));
	}

	public Kind kind() {
		return kind;
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(s -> new StreetLampBlock(kind, s));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new StreetLampBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Explore.LAMP_ENTITY,
				(w, p, s, be) -> be.tick((ServerWorld) w, p, s));
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			Explore.LitLamps.remove(world, pos);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
