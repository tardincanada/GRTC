package com.periodicelements.farm;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;

/** Plants sprayed with Bordeaux mixture (1.8.0): they don't wash out, wilt in the dark, or (berry bushes) scratch. */
public class Sprayed extends PersistentState {
	private static final Type<Sprayed> TYPE = new Type<>(Sprayed::new, Sprayed::read, null);
	private final Set<Long> plants = new HashSet<>();

	public static Sprayed of(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, "periodic_elements_sprayed");
	}

	private static Sprayed read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		Sprayed s = new Sprayed();
		for (long l : nbt.getLongArray("Pos")) {
			s.plants.add(l);
		}
		return s;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		nbt.putLongArray("Pos", plants.stream().mapToLong(Long::longValue).toArray());
		return nbt;
	}

	public boolean contains(BlockPos pos) {
		return !plants.isEmpty() && plants.contains(pos.asLong());
	}

	public void spray(BlockPos pos) {
		plants.add(pos.asLong());
		markDirty();
	}

	public void forget(BlockPos pos) {
		if (plants.remove(pos.asLong())) {
			markDirty();
		}
	}
}
