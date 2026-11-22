package com.periodicelements.space;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/** The payload inside a Payload Fairing: one satellite. */
public class PayloadFairingBlockEntity extends BlockEntity {
	public ItemStack payload = ItemStack.EMPTY;

	public PayloadFairingBlockEntity(BlockPos pos, BlockState state) {
		super(Space.FAIRING_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!payload.isEmpty()) {
			nbt.put("payload", payload.encode(lookup));
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		payload = nbt.contains("payload") ? ItemStack.fromNbt(lookup, nbt.getCompound("payload")).orElse(ItemStack.EMPTY)
				: ItemStack.EMPTY;
	}
}
