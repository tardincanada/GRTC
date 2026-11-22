package com.periodicelements.trees;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

/**
 * The logs a player placed in a world (3.55.0). They do not hold up leaves: only a tree that grew there (world
 * generation or a sapling) does (Guilherme, 2026-11-08). Stripping keeps the mark; breaking the log clears it.
 */
public class PlacedLogs extends PersistentState {
	private static final Type<PlacedLogs> TYPE = new Type<>(PlacedLogs::new, PlacedLogs::read, null);
	private final LongOpenHashSet logs = new LongOpenHashSet();

	public static PlacedLogs of(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, "periodic_elements_placed_logs");
	}

	private static PlacedLogs read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		PlacedLogs s = new PlacedLogs();
		for (long p : nbt.getLongArray("Pos")) {
			s.logs.add(p);
		}
		return s;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		nbt.putLongArray("Pos", logs.toLongArray());
		return nbt;
	}

	public boolean placed(BlockPos pos) {
		return !logs.isEmpty() && logs.contains(pos.asLong());
	}

	public void add(BlockPos pos) {
		if (logs.add(pos.asLong())) {
			markDirty();
		}
	}

	public void forget(BlockPos pos) {
		if (!logs.isEmpty() && logs.remove(pos.asLong())) {
			markDirty();
		}
	}

	public int size() {
		return logs.size();
	}
}
