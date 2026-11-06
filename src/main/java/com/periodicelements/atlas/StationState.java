package com.periodicelements.atlas;

import com.periodicelements.PeriodicElements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Atlas's stage 3 stations in the Overworld (data/periodic_elements_stations.dat): every Radar Station and Tracking
 * Station with its owner and whether it had power when last loaded, and how many satellites each player has in orbit.
 * A Tracking Station keeps mapping from here while its chunk is unloaded, on the power it last had.
 */
public class StationState extends PersistentState {
	public static final class Station {
		public final StationBlock.Kind kind;
		public final UUID owner;
		public boolean powered;
		/** Tracking stations: how far the current pass of the spiral has got. */
		public int progress;

		Station(StationBlock.Kind kind, UUID owner) {
			this.kind = kind;
			this.owner = owner;
		}
	}

	private static final Type<StationState> TYPE = new Type<>(StationState::new, StationState::fromNbt, null);

	public final Map<BlockPos, Station> stations = new HashMap<>();
	public final Map<UUID, Integer> satellites = new HashMap<>();

	public static StationState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, PeriodicElements.MOD_ID + "_stations");
	}

	public Station add(BlockPos pos, StationBlock.Kind kind, UUID owner) {
		Station s = new Station(kind, owner);
		stations.put(pos.toImmutable(), s);
		markDirty();
		return s;
	}

	public int satellites(UUID owner) {
		return satellites.getOrDefault(owner, 0);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		stations.forEach((pos, s) -> {
			NbtCompound c = new NbtCompound();
			c.putLong("pos", pos.asLong());
			c.putString("kind", s.kind.name());
			c.putUuid("owner", s.owner);
			c.putBoolean("powered", s.powered);
			c.putInt("progress", s.progress);
			list.add(c);
		});
		nbt.put("stations", list);
		NbtCompound sats = new NbtCompound();
		satellites.forEach((k, v) -> sats.putInt(k.toString(), v));
		nbt.put("satellites", sats);
		return nbt;
	}

	private static StationState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		StationState st = new StationState();
		for (NbtElement e : nbt.getList("stations", NbtElement.COMPOUND_TYPE)) {
			NbtCompound c = (NbtCompound) e;
			StationBlock.Kind kind;
			try {
				kind = StationBlock.Kind.valueOf(c.getString("kind"));
			} catch (IllegalArgumentException ex) {
				continue;
			}
			Station s = new Station(kind, c.getUuid("owner"));
			s.powered = c.getBoolean("powered");
			s.progress = c.getInt("progress");
			st.stations.put(BlockPos.fromLong(c.getLong("pos")), s);
		}
		NbtCompound sats = nbt.getCompound("satellites");
		for (String k : sats.getKeys()) {
			try {
				st.satellites.put(UUID.fromString(k), sats.getInt(k));
			} catch (IllegalArgumentException ignored) {
			}
		}
		return st;
	}
}
