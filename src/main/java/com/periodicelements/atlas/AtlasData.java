package com.periodicelements.atlas;

import com.periodicelements.PeriodicElements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One player's Field Atlas (data/periodic_elements_atlas_&lt;uuid&gt;.dat), or one Atlas Copy waiting to be read
 * (periodic_elements_atlas_copy_&lt;id&gt;.dat). Overworld only in this stage.
 */
public class AtlasData extends PersistentState {
	public record Marker(String name, int x, int z) {
	}

	/**
	 * What the Atlas knows of one chunk beyond its drawing (stage 2): the last radiation reading (Sv/h, -1 = none), the
	 * magnetometer's magnetic and dense anomalies (0-255, -1 = not surveyed), ores found (block id -> blocks) and
	 * whether a core was taken there.
	 */
	public static final class ChunkInfo {
		public float rad = -1;
		public int magnetic = -1, dense = -1;
		public final Map<String, Integer> ores = new java.util.TreeMap<>();
		public boolean cored;
		/** Gamma spectrometry (stage 3): the last reading split by source element, Sv/h; empty = not measured. */
		public final Map<String, Float> isotopes = new java.util.TreeMap<>();

		NbtCompound toNbt(long key) {
			NbtCompound c = new NbtCompound();
			c.putLong("k", key);
			c.putFloat("rad", rad);
			c.putInt("mag", magnetic);
			c.putInt("dense", dense);
			c.putBoolean("cored", cored);
			NbtCompound o = new NbtCompound();
			ores.forEach(o::putInt);
			c.put("ores", o);
			if (!isotopes.isEmpty()) {
				NbtCompound iso = new NbtCompound();
				isotopes.forEach(iso::putFloat);
				c.put("iso", iso);
			}
			return c;
		}

		static ChunkInfo fromNbt(NbtCompound c) {
			ChunkInfo i = new ChunkInfo();
			i.rad = c.getFloat("rad");
			i.magnetic = c.getInt("mag");
			i.dense = c.getInt("dense");
			i.cored = c.getBoolean("cored");
			NbtCompound o = c.getCompound("ores");
			for (String k : o.getKeys()) i.ores.put(k, o.getInt(k));
			NbtCompound iso = c.getCompound("iso");
			for (String k : iso.getKeys()) i.isotopes.put(k, iso.getFloat(k));
			return i;
		}

		/** Fills in what this chunk lacks from {@code o}; returns whether anything changed. */
		boolean mergeFrom(ChunkInfo o) {
			boolean ch = false;
			if (rad < 0 && o.rad >= 0) { rad = o.rad; ch = true; }
			if (magnetic < 0 && o.magnetic >= 0) { magnetic = o.magnetic; dense = o.dense; ch = true; }
			for (var e : o.ores.entrySet()) {
				if (!ores.containsKey(e.getKey())) { ores.put(e.getKey(), e.getValue()); ch = true; }
			}
			if (!cored && o.cored) { cored = true; ch = true; }
			if (isotopes.isEmpty() && !o.isotopes.isEmpty()) { isotopes.putAll(o.isotopes); ch = true; }
			return ch;
		}
	}

	public static long chunkKey(int cx, int cz) {
		return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
	}

	private static final Type<AtlasData> TYPE = new Type<>(AtlasData::new, AtlasData::fromNbt, null);

	public final Map<Long, AtlasTile> tiles = new HashMap<>();
	public final List<Marker> markers = new ArrayList<>();
	public final Map<Long, ChunkInfo> chunks = new HashMap<>();
	/** Goes up whenever {@link #chunks} changes, so clients refetch the overlay layers only then. */
	public int overlayRev;

	public ChunkInfo chunk(int blockX, int blockZ) {
		return chunks.computeIfAbsent(chunkKey(blockX >> 4, blockZ >> 4), k -> new ChunkInfo());
	}

	public void overlayChanged() {
		overlayRev++;
		markDirty();
	}

	public static AtlasData of(MinecraftServer server, UUID player) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, PeriodicElements.MOD_ID + "_atlas_" + player);
	}

	public static AtlasData copy(MinecraftServer server, UUID copy) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, PeriodicElements.MOD_ID + "_atlas_copy_" + copy);
	}

	public AtlasTile tile(int x, int z, boolean create) {
		int tx = x >> AtlasTile.SHIFT, tz = z >> AtlasTile.SHIFT;
		long key = AtlasTile.key(tx, tz);
		AtlasTile t = tiles.get(key);
		if (t == null && create) {
			t = new AtlasTile(tx, tz);
			tiles.put(key, t);
		}
		return t;
	}

	/** Adds everything in {@code other} that this atlas lacks; returns the columns gained. */
	public int mergeFrom(AtlasData other, int markerLimit) {
		int gained = 0;
		for (AtlasTile o : other.tiles.values()) {
			AtlasTile mine = tiles.computeIfAbsent(o.key(), k -> new AtlasTile(o.tx, o.tz));
			gained += mine.mergeFrom(o);
		}
		for (Marker m : other.markers) {
			if (markers.size() >= markerLimit) break;
			if (markers.stream().noneMatch(x -> x.x() == m.x() && x.z() == m.z())) {
				markers.add(m);
			}
		}
		boolean over = false;
		for (var e : other.chunks.entrySet()) {
			ChunkInfo mine = chunks.get(e.getKey());
			if (mine == null) {
				mine = new ChunkInfo();
				chunks.put(e.getKey(), mine);
			}
			over |= mine.mergeFrom(e.getValue());
		}
		if (over) overlayRev++;
		markDirty();
		return gained;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		tiles.values().forEach(t -> list.add(t.toNbt()));
		nbt.put("tiles", list);
		NbtList ms = new NbtList();
		for (Marker m : markers) {
			NbtCompound c = new NbtCompound();
			c.putString("name", m.name());
			c.putInt("x", m.x());
			c.putInt("z", m.z());
			ms.add(c);
		}
		nbt.put("markers", ms);
		NbtList cs = new NbtList();
		chunks.forEach((k, i) -> cs.add(i.toNbt(k)));
		nbt.put("chunks", cs);
		nbt.putInt("overlayRev", overlayRev);
		return nbt;
	}

	private static AtlasData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		AtlasData d = new AtlasData();
		for (NbtElement e : nbt.getList("tiles", NbtElement.COMPOUND_TYPE)) {
			AtlasTile t = AtlasTile.fromNbt((NbtCompound) e);
			d.tiles.put(t.key(), t);
		}
		for (NbtElement e : nbt.getList("markers", NbtElement.COMPOUND_TYPE)) {
			NbtCompound c = (NbtCompound) e;
			d.markers.add(new Marker(c.getString("name"), c.getInt("x"), c.getInt("z")));
		}
		for (NbtElement e : nbt.getList("chunks", NbtElement.COMPOUND_TYPE)) {
			NbtCompound c = (NbtCompound) e;
			d.chunks.put(c.getLong("k"), ChunkInfo.fromNbt(c));
		}
		d.overlayRev = nbt.getInt("overlayRev");
		return d;
	}
}
