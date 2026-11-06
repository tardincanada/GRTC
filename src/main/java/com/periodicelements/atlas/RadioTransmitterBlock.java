package com.periodicelements.atlas;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A Radio Transmitter: shares its owner's Atlas markers with players who know radio within 512 blocks. */
public class RadioTransmitterBlock extends Block {
	public RadioTransmitterBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		if (world instanceof ServerWorld sw && world.getRegistryKey() == World.OVERWORLD && placer instanceof ServerPlayerEntity p) {
			RadioState r = RadioState.get(sw.getServer());
			r.transmitters.put(pos.toImmutable(), p.getUuid());
			r.markDirty();
			p.sendMessage(Text.translatable("gui.periodic_elements.atlas.radio.placed"), true);
		}
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world instanceof ServerWorld sw) {
			RadioState r = RadioState.get(sw.getServer());
			if (r.transmitters.remove(pos) != null) r.markDirty();
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
