package com.periodicelements.space;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/** What a Propellant Tank holds: which pair, and how many items of fuel and of oxidizer (one item = one tonne). */
public class PropellantTankBlockEntity extends BlockEntity {
	public Propellant propellant;
	public int fuel;
	public int oxidizer;

	public PropellantTankBlockEntity(BlockPos pos, BlockState state) {
		super(Space.TANK_ENTITY, pos, state);
	}

	public boolean empty() {
		return fuel == 0 && oxidizer == 0;
	}

	/** Tonnes of propellant inside. */
	public int mass() {
		return fuel + oxidizer;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (propellant != null) {
			nbt.putString("propellant", propellant.name());
		}
		nbt.putInt("fuel", fuel);
		nbt.putInt("oxidizer", oxidizer);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		propellant = Propellant.byName(nbt.getString("propellant"));
		fuel = nbt.getInt("fuel");
		oxidizer = nbt.getInt("oxidizer");
	}
}
