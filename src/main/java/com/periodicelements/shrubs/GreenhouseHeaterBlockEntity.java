package com.periodicelements.shrubs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Fuel left in a Greenhouse Heater, in game ticks; it burns one tick of fuel every {@link Shrubs#burnDivisor} ticks. */
public class GreenhouseHeaterBlockEntity extends BlockEntity {
	/** At most about 16 coal's worth (an hour and a half of heat). */
	public static final int MAX_FUEL = 16 * 1600;
	private int fuel;
	private int step;

	public GreenhouseHeaterBlockEntity(BlockPos pos, BlockState state) {
		super(Shrubs.HEATER_ENTITY, pos, state);
	}

	public boolean canTake() {
		return fuel < MAX_FUEL;
	}

	public void addFuel(int ticks) {
		fuel = Math.min(MAX_FUEL + ticks, fuel + ticks);
		markDirty();
	}

	public int secondsLeft() {
		return fuel * Shrubs.burnDivisor / 20;
	}

	public void tick(World world, BlockPos pos, BlockState state) {
		if (fuel > 0 && ++step >= Shrubs.burnDivisor) {
			step = 0;
			fuel--;
			markDirty();
		}
		boolean lit = fuel > 0;
		if (state.get(GreenhouseHeaterBlock.LIT) != lit) {
			world.setBlockState(pos, state.with(GreenhouseHeaterBlock.LIT, lit), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putInt("fuel", fuel);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		fuel = nbt.getInt("fuel");
	}
}
