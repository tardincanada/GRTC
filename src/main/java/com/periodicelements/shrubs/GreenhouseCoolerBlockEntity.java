package com.periodicelements.shrubs;

import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Cold left in a Greenhouse Cooler, in ticks of melting ice. Without ice it draws {@link Shrubs#coolerPower} kJ a tick
 * of current from the power sources its wires reach.
 */
public class GreenhouseCoolerBlockEntity extends BlockEntity {
	/** At most an hour of cold stored. */
	public static final int MAX_ICE = 72000;
	private int ice;
	/** 1 while the last tick's current was paid for. */
	private int powered;

	public GreenhouseCoolerBlockEntity(BlockPos pos, BlockState state) {
		super(Shrubs.COOLER_ENTITY, pos, state);
	}

	public boolean canTake() {
		return ice < MAX_ICE;
	}

	public void addIce(int ticks) {
		ice = Math.min(MAX_ICE + ticks, ice + ticks);
		markDirty();
	}

	public int secondsLeft() {
		return ice / 20;
	}

	public boolean powered() {
		return powered > 0;
	}

	public void tick(ServerWorld world, BlockPos pos, BlockState state) {
		if (ice > 0) {
			ice--;
			markDirty();
		} else {
			// A dynamo hands out only so much a tick, so the cooler draws its current every tick.
			double want = Shrubs.coolerPower, got = 0;
			for (PowerBlockEntity source : MachineBlockEntity.powerSources(world, pos)) {
				got += source.draw(want - got);
				if (got >= want - 1e-9) {
					break;
				}
			}
			powered = got >= want - 1e-9 ? 1 : 0;
		}
		boolean on = ice > 0 || powered > 0;
		if (state.get(GreenhouseCoolerBlock.LIT) != on) {
			world.setBlockState(pos, state.with(GreenhouseCoolerBlock.LIT, on), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putInt("ice", ice);
		nbt.putInt("powered", powered);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		ice = nbt.getInt("ice");
		powered = nbt.getInt("powered");
	}
}
