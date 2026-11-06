package com.periodicelements.explore;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

/**
 * The light of worn electric lamps (3.47.0): an invisible, replaceable block, light 15, put at the wearer's head or
 * where a spotlight lands. Since 3.48.1 it draws one fully transparent quad (so shaders such as Fiat Lux see it) and
 * its {@link #KIND} says which lamp lit it: {@code cap} (Electric Cap Lamp, warm) or {@code led} (LED Spotlight Helmet). It checks itself every {@link #CHECK} ticks and goes out when no lamp keeps it.
 */
public class ExplorerLightBlock extends Block {
	public static final MapCodec<ExplorerLightBlock> CODEC = createCodec(ExplorerLightBlock::new);
	public static final int CHECK = 10;
	public static final EnumProperty<Kind> KIND = EnumProperty.of("kind", Kind.class);

	public enum Kind implements StringIdentifiable {
		CAP("cap"), LED("led");

		private final String name;

		Kind(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return name;
		}
	}

	public ExplorerLightBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(KIND, Kind.CAP));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(KIND);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	/** Lights {@code at}, or {@code fallback} if {@code at} is taken; nothing if neither is air. */
	public static void lightAt(ServerWorld world, BlockPos at, BlockPos fallback, Kind kind) {
		BlockState light = Explore.LIGHT.getDefaultState().with(KIND, kind);
		for (BlockPos p : fallback == null ? new BlockPos[]{at} : new BlockPos[]{at, fallback}) {
			BlockState s = world.getBlockState(p);
			if (s.isOf(Explore.LIGHT)) {
				if (s.get(KIND) != kind) {
					world.setBlockState(p, light, Block.NOTIFY_LISTENERS);
				}
				return;
			}
			if (s.isAir()) {
				world.setBlockState(p, light, Block.NOTIFY_LISTENERS);
				world.scheduleBlockTick(p, Explore.LIGHT, CHECK);
				return;
			}
		}
	}

	/** The air block in front of what the player looks at, up to {@code reach} blocks away, or null. */
	public static BlockPos spotOf(ServerWorld world, PlayerEntity p, int reach) {
		Vec3d eye = p.getEyePos();
		Vec3d end = eye.add(p.getRotationVec(1f).multiply(reach));
		BlockHitResult hit = world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, p));
		if (hit.getType() != HitResult.Type.BLOCK) {
			return null;
		}
		BlockPos at = hit.getBlockPos().offset(hit.getSide());
		return at.isWithinDistance(p.getBlockPos(), 3) ? null : at;
	}

	/** Is a lit lamp still keeping this light? */
	static boolean kept(ServerWorld world, BlockPos pos) {
		for (PlayerEntity p : world.getPlayers()) {
			if (Explore.worn(p, HeadGearItem.Kind.CAP_LAMP) == null && Explore.worn(p, HeadGearItem.Kind.SPOTLIGHT) == null) {
				continue;
			}
			if (p.getPos().add(0, 1, 0).isInRange(pos.toCenterPos(), 2.4)) {
				return true;
			}
			BlockPos spot = Explore.SPOTS.get(p.getUuid());
			if (spot != null && spot.isWithinDistance(pos, 1.5)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (kept(world, pos)) {
			world.scheduleBlockTick(pos, this, CHECK);
		} else {
			world.removeBlock(pos, false);
		}
	}
}
