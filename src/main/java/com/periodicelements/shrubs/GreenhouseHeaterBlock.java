package com.periodicelements.shrubs;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.registry.FuelRegistry;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Greenhouse Heater (3.16.0, Heated Greenhouse, level 2): a brick stove that burns any fuel slowly. Lit, it makes the
 * greenhouse it stands in warm (see {@link Greenhouse}). Use fuel on it to load it; an empty hand shows what is left.
 */
public class GreenhouseHeaterBlock extends BlockWithEntity {
	public static final MapCodec<GreenhouseHeaterBlock> CODEC = createCodec(GreenhouseHeaterBlock::new);
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	public GreenhouseHeaterBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(LIT, false));
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
		return new GreenhouseHeaterBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, Shrubs.HEATER_ENTITY, (w, pos, s, be) -> be.tick(w, pos, s));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		Integer burn = FuelRegistry.INSTANCE.get(stack.getItem());
		if (burn == null || burn <= 0 || !(world.getBlockEntity(pos) instanceof GreenhouseHeaterBlockEntity be)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!be.canTake()) {
			return ItemActionResult.CONSUME;
		}
		if (!world.isClient) {
			be.addFuel(burn);
			ItemStack rest = stack.getItem().getRecipeRemainder() == null ? ItemStack.EMPTY
					: new ItemStack(stack.getItem().getRecipeRemainder());
			stack.decrementUnlessCreative(1, player);
			if (!rest.isEmpty() && !player.getInventory().insertStack(rest)) {
				player.dropItem(rest, false);
			}
			world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);
		}
		return ItemActionResult.success(world.isClient);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof GreenhouseHeaterBlockEntity be) {
			int s = be.secondsLeft();
			player.sendMessage(s > 0 ? Text.translatable("gui.periodic_elements.greenhouse.heater", s)
					: Text.translatable("gui.periodic_elements.greenhouse.heater_empty"), true);
		}
		return ActionResult.success(world.isClient);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT) && random.nextInt(3) == 0) {
			world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 0, 0.03, 0);
		}
	}
}
