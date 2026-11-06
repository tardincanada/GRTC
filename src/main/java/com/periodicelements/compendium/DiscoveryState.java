package com.periodicelements.compendium;

import com.periodicelements.PeriodicElements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every player's compendium notes (data/periodic_elements_compendium.dat): "tree:&lt;id&gt;" and "biome:&lt;id&gt;",
 * each with the block position where it was first noted (packed with BlockPos.asLong). Per player, like research.
 */
public class DiscoveryState extends PersistentState {
	private static final String KEY = PeriodicElements.MOD_ID + "_compendium";
	private static final Type<DiscoveryState> TYPE = new Type<>(DiscoveryState::new, DiscoveryState::fromNbt, null);

	private final Map<UUID, LinkedHashMap<String, Long>> notes = new HashMap<>();

	public static DiscoveryState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, KEY);
	}

	public Map<String, Long> of(UUID player) {
		Map<String, Long> m = notes.get(player);
		return m == null ? Map.of() : Collections.unmodifiableMap(m);
	}

	public boolean knows(UUID player, String key) {
		Map<String, Long> m = notes.get(player);
		return m != null && m.containsKey(key);
	}

	/** Notes {@code key} at {@code pos}; returns true if it is new. */
	public boolean add(UUID player, String key, long pos) {
		LinkedHashMap<String, Long> m = notes.computeIfAbsent(player, k -> new LinkedHashMap<>());
		if (m.containsKey(key)) return false;
		m.put(key, pos);
		markDirty();
		return true;
	}

	public void clear(UUID player) {
		if (notes.remove(player) != null) markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtCompound players = new NbtCompound();
		notes.forEach((uuid, m) -> {
			NbtCompound c = new NbtCompound();
			m.forEach(c::putLong);
			players.put(uuid.toString(), c);
		});
		nbt.put("players", players);
		return nbt;
	}

	private static DiscoveryState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		DiscoveryState s = new DiscoveryState();
		NbtCompound players = nbt.getCompound("players");
		for (String uuid : players.getKeys()) {
			NbtCompound c = players.getCompound(uuid);
			LinkedHashMap<String, Long> m = new LinkedHashMap<>();
			for (String k : c.getKeys()) m.put(k, c.getLong(k));
			s.notes.put(UUID.fromString(uuid), m);
		}
		return s;
	}
}
