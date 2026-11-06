package com.periodicelements.atlas;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/** The Launch Pad: use a Satellite on it, under open sky, to put it in orbit for your Tracking Stations. */
public class LaunchPadBlock extends Block {
	public LaunchPadBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
											 Hand hand, BlockHitResult hit) {
		if (!stack.isOf(Atlas.SATELLITE)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (world instanceof ServerWorld sw && player instanceof ServerPlayerEntity p) {
			launch(sw, pos, p, stack);
		}
		return ItemActionResult.success(world.isClient);
	}

	/** Puts the satellite in {@code stack} in orbit from the pad at {@code pos}; returns whether it went up. */
	public static boolean launch(ServerWorld world, BlockPos pos, ServerPlayerEntity p, ItemStack stack) {
		if (!Atlas.knows(p, "satellite")) {
			p.sendMessage(Text.translatable("gui.periodic_elements.atlas.satellite.needs").formatted(Formatting.RED), true);
			return false;
		}
		if (world.getRegistryKey() != World.OVERWORLD
				|| world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY() + 1) {
			p.sendMessage(Text.translatable("gui.periodic_elements.atlas.satellite.sky").formatted(Formatting.YELLOW), true);
			return false;
		}
		if (!p.isCreative()) stack.decrement(1);
		StationState st = StationState.get(world.getServer());
		int n = st.satellites(p.getUuid()) + 1;
		st.satellites.put(p.getUuid(), n);
		st.markDirty();
		double x = pos.getX() + 0.5, y = pos.getY() + 1.2, z = pos.getZ() + 0.5;
		world.spawnParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, y, z, 40, 0.6, 0.2, 0.6, 0.02);
		world.spawnParticles(ParticleTypes.FLAME, x, y, z, 60, 0.3, 0.1, 0.3, 0.05);
		for (int k = 0; k < 24; k++) {
			world.spawnParticles(ParticleTypes.FIREWORK, x, y + k * 2, z, 4, 0.1, 0.4, 0.1, 0.01);
		}
		world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.BLOCKS, 3f, 0.5f);
		world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 1.5f, 0.6f);
		p.sendMessage(Text.translatable("gui.periodic_elements.atlas.satellite.launched", n), false);
		return true;
	}
}
