package com.periodicelements.radiation;

import com.periodicelements.PeriodicElements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every player's radiation dose, saved with the world. {@code recent} is the dose the body has not yet repaired
 * (it halves every {@code repairHalfLifeHours} of real time, online or not); {@code lifetime} only grows.
 */
public class RadiationState extends PersistentState {
	private static final String KEY = PeriodicElements.MOD_ID + "_radiation";
	private static final Type<RadiationState> TYPE = new Type<>(RadiationState::new, RadiationState::fromNbt, null);

	public static final class Dose {
		public double recent;
		public double lifetime;
		public long updatedMillis;
		/** Fraction of a microsievert not yet added to the statistic. */
		public double statRemainder;
		public int band;
	}

	private final Map<UUID, Dose> doses = new HashMap<>();

	public static RadiationState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, KEY);
	}

	/** The player's dose, with repair applied up to {@code nowMillis}. */
	public Dose dose(UUID player, long nowMillis) {
		Dose d = doses.computeIfAbsent(player, k -> {
			Dose n = new Dose();
			n.updatedMillis = nowMillis;
			return n;
		});
		if (nowMillis > d.updatedMillis) {
			double hours = (nowMillis - d.updatedMillis) / 3_600_000.0;
			d.recent *= Math.pow(0.5, hours / Radioactivity.setting("repairHalfLifeHours"));
			d.updatedMillis = nowMillis;
			markDirty();
		}
		return d;
	}

	public void add(UUID player, double sv, long nowMillis) {
		Dose d = dose(player, nowMillis);
		d.recent += sv;
		d.lifetime += sv;
		markDirty();
	}

	public void reset(UUID player) {
		doses.remove(player);
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtCompound players = new NbtCompound();
		doses.forEach((uuid, d) -> {
			NbtCompound c = new NbtCompound();
			c.putDouble("Recent", d.recent);
			c.putDouble("Lifetime", d.lifetime);
			c.putLong("Updated", d.updatedMillis);
			c.putDouble("StatRemainder", d.statRemainder);
			c.putInt("Band", d.band);
			players.put(uuid.toString(), c);
		});
		nbt.put("players", players);
		return nbt;
	}

	private static RadiationState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		RadiationState state = new RadiationState();
		NbtCompound players = nbt.getCompound("players");
		for (String key : players.getKeys()) {
			NbtCompound c = players.getCompound(key);
			Dose d = new Dose();
			d.recent = c.getDouble("Recent");
			d.lifetime = c.getDouble("Lifetime");
			d.updatedMillis = c.getLong("Updated");
			d.statRemainder = c.getDouble("StatRemainder");
			d.band = c.getInt("Band");
			state.doses.put(UUID.fromString(key), d);
		}
		return state;
	}
}
