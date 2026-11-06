package com.periodicelements.research;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Every player's finished research projects, saved with the world (data/periodic_elements_research.dat).
 * Kept per UUID in world data rather than on the player, so a machine can check its owner's research
 * while the owner is offline.
 */
public class ResearchState extends PersistentState {
	private static final String KEY = PeriodicElements.MOD_ID + "_research";
	private static final Type<ResearchState> TYPE = new Type<>(ResearchState::new, ResearchState::fromNbt, null);

	private final Map<UUID, Set<String>> known = new HashMap<>();

	public static ResearchState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, KEY);
	}

	/** Read-only view of what a player knows. */
	public Set<String> known(UUID player) {
		Set<String> set = known.get(player);
		return set == null ? Set.of() : Collections.unmodifiableSet(set);
	}

	public int techLevel(UUID player) {
		return Research.techLevel(known(player));
	}

	/** Adds projects; returns how many were new. Syncs the player if they are online. */
	public int grant(MinecraftServer server, UUID player, Collection<String> projects) {
		Set<String> set = known.computeIfAbsent(player, k -> new LinkedHashSet<>());
		int before = set.size();
		int levelBefore = Research.techLevel(set);
		for (String id : projects) {
			if (Research.get(id) != null) {
				set.add(id);
			}
		}
		int added = set.size() - before;
		if (added > 0) {
			markDirty();
			ServerPlayerEntity online = server.getPlayerManager().getPlayer(player);
			if (online != null) {
				sync(online);
				int levelAfter = Research.techLevel(set);
				if (levelAfter > levelBefore) {
					online.sendMessage(net.minecraft.text.Text.translatable("message.periodic_elements.level_up", levelAfter,
							net.minecraft.text.Text.translatable("level.periodic_elements." + levelAfter))
							.formatted(net.minecraft.util.Formatting.GOLD), false);
				}
			}
		}
		return added;
	}

	/** Replaces everything the player knows with {@code projects} (the Book of Progress). Syncs them if online. */
	public void set(MinecraftServer server, UUID player, Collection<String> projects) {
		Set<String> set = new LinkedHashSet<>();
		for (String id : projects) {
			if (Research.get(id) != null) {
				set.add(id);
			}
		}
		known.put(player, set);
		markDirty();
		ServerPlayerEntity online = server.getPlayerManager().getPlayer(player);
		if (online != null) {
			sync(online);
		}
	}

	public int revoke(MinecraftServer server, UUID player, Collection<String> projects) {
		Set<String> set = known.get(player);
		if (set == null) {
			return 0;
		}
		int before = set.size();
		set.removeAll(projects);
		int removed = before - set.size();
		if (removed > 0) {
			markDirty();
			ServerPlayerEntity online = server.getPlayerManager().getPlayer(player);
			if (online != null) {
				sync(online);
			}
		}
		return removed;
	}

	public void sync(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, new ResearchSyncPayload(new ArrayList<>(known(player.getUuid()))));
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtCompound players = new NbtCompound();
		known.forEach((uuid, set) -> {
			NbtList list = new NbtList();
			set.forEach(id -> list.add(NbtString.of(id)));
			players.put(uuid.toString(), list);
		});
		nbt.put("players", players);
		return nbt;
	}

	private static ResearchState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		ResearchState state = new ResearchState();
		NbtCompound players = nbt.getCompound("players");
		for (String key : players.getKeys()) {
			Set<String> set = new LinkedHashSet<>();
			for (NbtElement e : players.getList(key, NbtElement.STRING_TYPE)) {
				set.add(e.asString());
			}
			state.known.put(UUID.fromString(key), set);
		}
		return state;
	}
}
