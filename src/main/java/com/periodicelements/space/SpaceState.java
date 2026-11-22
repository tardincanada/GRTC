package com.periodicelements.space;

import com.periodicelements.PeriodicElements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Satellites with a job (3.61.0), per owner: how many of each kind ("weather", "communication") are in orbit. */
public class SpaceState extends PersistentState {
	private static final Type<SpaceState> TYPE = new Type<>(SpaceState::new, SpaceState::fromNbt, null);

	public final Map<UUID, Map<String, Integer>> satellites = new HashMap<>();
	/** Rockets launched and first stages landed, per owner (for the Field Guide's story and the tests). */
	public final Map<UUID, Integer> launches = new HashMap<>();
	public final Map<UUID, Integer> landings = new HashMap<>();
	/** 3.64.0: crewed landings on the Moon, per player. */
	public final Map<UUID, Integer> moonLandings = new HashMap<>();
	/** 3.66.0: crewed landings on Mars per player. */
	public final Map<UUID, Integer> marsLandings = new HashMap<>();
	/** 3.68.0: crewed landings on Mercury and arrivals at Venus's cloud deck, per player. */
	public final Map<UUID, Integer> mercuryLandings = new HashMap<>();
	public final Map<UUID, Integer> venusLandings = new HashMap<>();
	/** 3.62.0: tonight's meteorite, chosen at dusk: column x, z and the time of day (absolute) it comes down. */
	public int fallX, fallZ;
	public long fallAt = -1;
	/** Whether its MeteorEntity is already on the way. */
	public boolean fallSent;
	/** Meteorites that have come down in this world. */
	public int falls;

	public static SpaceState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, PeriodicElements.MOD_ID + "_space");
	}

	public int count(UUID owner, String kind) {
		Map<String, Integer> m = satellites.get(owner);
		return m == null ? 0 : m.getOrDefault(kind, 0);
	}

	public void add(UUID owner, String kind) {
		satellites.computeIfAbsent(owner, k -> new HashMap<>()).merge(kind, 1, Integer::sum);
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtCompound sats = new NbtCompound();
		satellites.forEach((owner, m) -> {
			NbtCompound c = new NbtCompound();
			m.forEach(c::putInt);
			sats.put(owner.toString(), c);
		});
		nbt.put("satellites", sats);
		NbtCompound l = new NbtCompound();
		launches.forEach((k, v) -> l.putInt(k.toString(), v));
		nbt.put("launches", l);
		NbtCompound d = new NbtCompound();
		landings.forEach((k, v) -> d.putInt(k.toString(), v));
		nbt.put("landings", d);
		NbtCompound ml = new NbtCompound();
		moonLandings.forEach((k, v) -> ml.putInt(k.toString(), v));
		nbt.put("moonLandings", ml);
		NbtCompound mars = new NbtCompound();
		marsLandings.forEach((k, v) -> mars.putInt(k.toString(), v));
		nbt.put("marsLandings", mars);
		NbtCompound mer = new NbtCompound();
		mercuryLandings.forEach((k, v) -> mer.putInt(k.toString(), v));
		nbt.put("mercuryLandings", mer);
		NbtCompound ven = new NbtCompound();
		venusLandings.forEach((k, v) -> ven.putInt(k.toString(), v));
		nbt.put("venusLandings", ven);
		nbt.putInt("fallX", fallX);
		nbt.putInt("fallZ", fallZ);
		nbt.putLong("fallAt", fallAt);
		nbt.putBoolean("fallSent", fallSent);
		nbt.putInt("falls", falls);
		return nbt;
	}

	private static SpaceState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		SpaceState s = new SpaceState();
		NbtCompound sats = nbt.getCompound("satellites");
		for (String k : sats.getKeys()) {
			try {
				UUID owner = UUID.fromString(k);
				NbtCompound c = sats.getCompound(k);
				Map<String, Integer> m = new HashMap<>();
				for (String kind : c.getKeys()) {
					m.put(kind, c.getInt(kind));
				}
				s.satellites.put(owner, m);
			} catch (IllegalArgumentException ignored) {
			}
		}
		readCounts(nbt.getCompound("launches"), s.launches);
		readCounts(nbt.getCompound("landings"), s.landings);
		readCounts(nbt.getCompound("moonLandings"), s.moonLandings);
		readCounts(nbt.getCompound("marsLandings"), s.marsLandings);
		readCounts(nbt.getCompound("mercuryLandings"), s.mercuryLandings);
		readCounts(nbt.getCompound("venusLandings"), s.venusLandings);
		s.fallX = nbt.getInt("fallX");
		s.fallZ = nbt.getInt("fallZ");
		s.fallAt = nbt.contains("fallAt") ? nbt.getLong("fallAt") : -1;
		s.fallSent = nbt.getBoolean("fallSent");
		s.falls = nbt.getInt("falls");
		return s;
	}

	private static void readCounts(NbtCompound c, Map<UUID, Integer> into) {
		for (String k : c.getKeys()) {
			try {
				into.put(UUID.fromString(k), c.getInt(k));
			} catch (IllegalArgumentException ignored) {
			}
		}
	}
}
