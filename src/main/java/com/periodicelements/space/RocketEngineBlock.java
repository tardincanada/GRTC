package com.periodicelements.space;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A rocket engine: the bottom block of a stage; the Propellant Tanks stacked on it are that stage's. Landing Legs fitted
 * to the first stage's engine bring the stage back to the pad; a stage that flew back may come home worn.
 */
public class RocketEngineBlock extends Block {
	public static final BooleanProperty LEGS = BooleanProperty.of("legs");
	public static final BooleanProperty WORN = BooleanProperty.of("worn");

	public final Propellant propellant;

	public RocketEngineBlock(Propellant propellant, Settings settings) {
		super(settings);
		this.propellant = propellant;
		setDefaultState(getStateManager().getDefaultState().with(LEGS, false).with(WORN, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LEGS, WORN);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		String ns = "message.periodic_elements.rocket.";
		if (stack.isOf(Space.LANDING_LEGS) && !state.get(LEGS)) {
			if (!world.isClient) {
				world.setBlockState(pos, state.with(LEGS, true));
				if (!player.isCreative()) {
					stack.decrement(1);
				}
				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.6f, 1.4f);
				player.sendMessage(Text.translatable(ns + "legs_fitted").formatted(Formatting.GREEN), true);
			}
			return ItemActionResult.success(world.isClient);
		}
		if (stack.isOf(Space.repairItem()) && state.get(WORN)) {
			if (!world.isClient) {
				world.setBlockState(pos, state.with(WORN, false));
				if (!player.isCreative()) {
					stack.decrement(1);
				}
				world.playSound(null, pos, SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 0.8f, 1f);
				player.sendMessage(Text.translatable(ns + "repaired").formatted(Formatting.GREEN), true);
			}
			return ItemActionResult.success(world.isClient);
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}
