package com.periodicelements.machine;

import com.mojang.serialization.MapCodec;
import com.periodicelements.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A block for one machine in data/machines.csv. All machines share {@link MachineBlockEntity}. */
public class MachineBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	/** True while a reaction is running. Machines with an animated texture (3.28.0: the charcoal pit) switch model on it. */
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
	/** Which process animation shows while active (3.33.0): see {@link MachineAnimations}. */
	public static final int MAX_MODE = 15;
	public static final net.minecraft.state.property.IntProperty MODE = net.minecraft.state.property.IntProperty.of("mode", 0, MAX_MODE);

	private final String machineId;
	private final MapCodec<MachineBlock> codec;

	public MachineBlock(String machineId, Settings settings) {
		super(settings);
		this.machineId = machineId;
		this.codec = createCodec(s -> new MachineBlock(machineId, s));
		setDefaultState(getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(LIT, false).with(ACTIVE, false).with(MODE, 0));
	}

	public String machineId() {
		return machineId;
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return codec;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, ACTIVE, MODE);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MachineBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModContent.MACHINE_ENTITY,
				(w, pos, s, be) -> be.tick((ServerWorld) w, pos, s));
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		if (!world.isClient && placer instanceof PlayerEntity player && world.getBlockEntity(pos) instanceof MachineBlockEntity be) {
			be.setOwner(player.getUuid());
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof MachineBlockEntity be) {
			player.openHandledScreen(be);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		// Electric machines are "lit" while they draw current, but have no fire to smoke (3.28.2).
		if (state.get(LIT) && !Reactions.machine(machineId).electric()) {
			double x = pos.getX() + 0.5, y = pos.getY() + 1.05, z = pos.getZ() + 0.5;
			world.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.4, y,
					z + (random.nextDouble() - 0.5) * 0.4, 0, 0.05, 0);
			if (random.nextInt(4) == 0) {
				world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0, 0.04, 0);
			}
		}
	}
}
