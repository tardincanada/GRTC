package com.periodicelements.shrubs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The hydrangea (3.22.0): its flowers are pink in limy soil and blue in acid soil, where aluminium dissolves and
 * colours them. Aluminium sulfate or alum used on it makes it flower blue; quicklime, slaked lime or chalk pink.
 */
public class HydrangeaBlock extends ShrubBlock {
	public static final BooleanProperty BLUE = BooleanProperty.of("blue");
	static Set<String> blueItems = Set.of();
	static Set<String> pinkItems = Set.of();

	public HydrangeaBlock(String id, List<String> biomeIds, String soil, String climate, boolean thorny, Supplier<Item> product,
						  Settings settings) {
		super(id, biomeIds, soil, climate, thorny, product, settings);
		setDefaultState(getDefaultState().with(BLUE, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(BLUE);
	}

	/** True for blue, false for pink, null if the item does not change the flowers. */
	static Boolean colourOf(ItemStack stack) {
		String id = Registries.ITEM.getId(stack.getItem()).toString();
		return blueItems.contains(id) ? Boolean.TRUE : pinkItems.contains(id) ? Boolean.FALSE : null;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		Boolean blue = colourOf(stack);
		if (blue == null) {
			return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
		}
		if (state.get(BLUE) == blue) {
			return ItemActionResult.CONSUME;
		}
		if (!world.isClient) {
			world.setBlockState(pos, state.with(BLUE, blue), Block.NOTIFY_LISTENERS);
			stack.decrementUnlessCreative(1, player);
			world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
			if (world instanceof ServerWorld sw) {
				sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0);
			}
			player.sendMessage(Text.translatable("gui.periodic_elements.hydrangea." + (blue ? "blue" : "pink")), true);
		}
		return ItemActionResult.success(world.isClient);
	}
}
