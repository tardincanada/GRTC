package com.periodicelements.crops;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

/**
 * The genes of the crops planted in a world (3.23.0): crop position -> packed entry. Only plants that differ from the
 * ordinary one (or carry pollen, pests or rust) have an entry.
 * <p>Entry bits: 0-11 the plant's genes C to I, 12-23 those of the pollen that fertilised it, 24 pollinated,
 * 25 caterpillars, 26 rust, 27 pollinated by hand, 28 inoculated (3.37.0); 32-41 the rest of the plant's genome
 * (genome bits 12-21: hybrid mark, stature gene, transgenes), 42-51 the same for the pollen.
 */
public class CropGenomes extends PersistentState {
	private static final Type<CropGenomes> TYPE = new Type<>(CropGenomes::new, CropGenomes::read, null);
	public static final long POLLINATED = 1L << 24, PEST = 1L << 25, DISEASE = 1L << 26, HAND = 1L << 27,
			INOCULATED = 1L << 28;
	private static final long FATHER_BITS = 0xFFFL << 12 | 0x3FFL << 42;
	private final Map<Long, Long> plants = new HashMap<>();

	public static CropGenomes of(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, "periodic_elements_crop_genomes");
	}

	private static CropGenomes read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		CropGenomes s = new CropGenomes();
		long[] pos = nbt.getLongArray("Pos");
		long[] entry = nbt.getLongArray("Entry");
		for (int i = 0; i < Math.min(pos.length, entry.length); i++) {
			s.plants.put(pos[i], entry[i]);
		}
		return s;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		long[] pos = new long[plants.size()];
		long[] entry = new long[plants.size()];
		int i = 0;
		for (var e : plants.entrySet()) {
			pos[i] = e.getKey();
			entry[i++] = e.getValue();
		}
		nbt.putLongArray("Pos", pos);
		nbt.putLongArray("Entry", entry);
		return nbt;
	}

	/** The stored entry, or null. */
	public Long get(BlockPos pos) {
		return plants.isEmpty() ? null : plants.get(pos.asLong());
	}

	public void put(BlockPos pos, long entry) {
		if (entry == Genome.DEFAULT) {
			forget(pos);
			return;
		}
		Long old = plants.put(pos.asLong(), entry);
		if (old == null || old != entry) {
			markDirty();
		}
	}

	public void forget(BlockPos pos) {
		if (plants.remove(pos.asLong()) != null) {
			markDirty();
		}
	}

	public int size() {
		return plants.size();
	}

	public static int genome(long entry) {
		return (int) (entry & 0xFFF | (entry >>> 32 & 0x3FF) << 12) & Genome.MASK;
	}

	public static int father(long entry) {
		return (int) (entry >> 12 & 0xFFF | (entry >>> 42 & 0x3FF) << 12) & Genome.MASK;
	}

	/** An entry for a plant with this genome and the flags (pollen, pests...) of {@code flags}. */
	public static long of(int genome, long flags) {
		genome &= Genome.MASK;
		long keep = flags & (FATHER_BITS | 0xFFL << 24);
		return keep | genome & 0xFFF | (long) (genome >>> 12 & 0x3FF) << 32;
	}

	public static long withFather(long entry, int father, boolean hand) {
		father &= Genome.MASK;
		entry &= ~(FATHER_BITS | POLLINATED | HAND);
		return entry | (long) (father & 0xFFF) << 12 | (long) (father >>> 12 & 0x3FF) << 42 | POLLINATED | (hand ? HAND : 0);
	}

	/** The entry with no pollen (after a perennial is picked, it flowers again). */
	public static long withoutFather(long entry) {
		return entry & ~(FATHER_BITS | POLLINATED | HAND);
	}

	/** Round trip for tests: write and read back. */
	public CropGenomes copy(RegistryWrapper.WrapperLookup lookup) {
		return read(writeNbt(new NbtCompound(), lookup), lookup);
	}
}
