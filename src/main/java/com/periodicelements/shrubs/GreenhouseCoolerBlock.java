package com.periodicelements.shrubs;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Greenhouse Cooler (3.22.0, Cold Greenhouse, level 4; Guilherme: it needs current or ice). Running, it makes the
 * greenhouse it stands in cold (see {@link Greenhouse}). Use ice on it to load it; with no ice left it draws current
 * from a dynamo, pile or battery through copper wire. An empty hand shows what it runs on.
 */
public class GreenhouseCoolerBlock extends BlockWithEntity {
	public static final MapCodec<GreenhouseCoolerBlock> CODEC = createCodec(GreenhouseCoolerBlock::new);
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	/** Running (cold). */
	public static final BooleanProperty LIT = Properties.LIT;

	public GreenhouseCoolerBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GreenhouseCoolerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Shrubs.COOLER_ENTITY, (w, pos, s, be) -> be.tick((ServerWorld) w, pos, s));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		Integer cold = Shrubs.coolerIce.get(Registries.ITEM.getId(stack.getItem()).toString());
		if (cold == null || !(world.getBlockEntity(pos) instanceof GreenhouseCoolerBlockEntity be)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!be.canTake()) {
			return ItemActionResult.CONSUME;
		}
		if (!world.isClient) {
			be.addIce(cold);
			stack.decrementUnlessCreative(1, player);
			world.playSound(null, pos, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.BLOCKS, 0.6f, 0.8f);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof GreenhouseCoolerBlockEntity be) {
			int s = be.secondsLeft();
			player.sendMessage(s > 0 ? Text.translatable("gui.periodic_elements.greenhouse.cooler", s)
					: be.powered() ? Text.translatable("gui.periodic_elements.greenhouse.cooler_power")
					: Text.translatable("gui.periodic_elements.greenhouse.cooler_empty"), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT) && random.nextInt(3) == 0) {
			world.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + random.nextDouble(), pos.getY() + 1.05,
					pos.getZ() + random.nextDouble(), 0, -0.01, 0);
		}
	}
}
