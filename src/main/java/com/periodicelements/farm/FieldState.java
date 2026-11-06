package com.periodicelements.farm;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Treated ground (2.0.0), kept per world: areas dusted with DDT or sprayed with Pre-Emergent (centre, radius and the
 * game time they wear off), and the farmland planted with Tolerant Seeds.
 */
public class FieldState extends PersistentState {
	private static final String KEY = "periodic_elements_fields";
	private static final Type<FieldState> TYPE = new Type<>(FieldState::new, FieldState::read, null);

	public enum Kind { DDT, PRE_EMERGENT }

	public record Area(Kind kind, BlockPos center, int radius, long until) {
		public boolean contains(BlockPos p) {
			return Math.abs(p.getX() - center.getX()) <= radius && Math.abs(p.getZ() - center.getZ()) <= radius
					&& Math.abs(p.getY() - center.getY()) <= radius;
		}
	}

	private final List<Area> areas = new ArrayList<>();
	private final Set<Long> tolerant = new HashSet<>();

	public static FieldState of(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, KEY);
	}

	private static FieldState read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		FieldState f = new FieldState();
		NbtList list = nbt.getList("Areas", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound a = list.getCompound(i);
			f.areas.add(new Area(Kind.values()[a.getInt("Kind")], BlockPos.fromLong(a.getLong("Pos")), a.getInt("R"), a.getLong("Until")));
		}
		for (long p : nbt.getLongArray("Tolerant")) {
			f.tolerant.add(p);
		}
		return f;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (Area a : areas) {
			NbtCompound c = new NbtCompound();
			c.putInt("Kind", a.kind().ordinal());
			c.putLong("Pos", a.center().asLong());
			c.putInt("R", a.radius());
			c.putLong("Until", a.until());
			list.add(c);
		}
		nbt.put("Areas", list);
		nbt.putLongArray("Tolerant", tolerant.stream().mapToLong(Long::longValue).toArray());
		return nbt;
	}

	public void add(Kind kind, BlockPos center, int radius, long until) {
		areas.add(new Area(kind, center.toImmutable(), radius, until));
		markDirty();
	}

	public List<Area> areas() {
		return areas;
	}

	/** Is this position inside a live area of this kind? */
	public boolean treated(Kind kind, BlockPos p, long now) {
		for (Area a : areas) {
			if (a.kind() == kind && a.until() > now && a.contains(p)) {
				return true;
			}
		}
		return false;
	}

	/** Drops the areas that have worn off. */
	public void expire(long now) {
		Iterator<Area> it = areas.iterator();
		while (it.hasNext()) {
			if (it.next().until() <= now) {
				it.remove();
				markDirty();
			}
		}
	}

	public boolean tolerant(BlockPos crop) {
		return tolerant.contains(crop.asLong());
	}

	public void plantTolerant(BlockPos crop) {
		tolerant.add(crop.asLong());
		markDirty();
	}

	public void forget(BlockPos crop) {
		if (tolerant.remove(crop.asLong())) {
			markDirty();
		}
	}

	public Set<Long> tolerantCrops() {
		return tolerant;
	}
}
