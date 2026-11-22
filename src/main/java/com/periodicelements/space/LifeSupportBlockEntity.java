package com.periodicelements.space;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 3.64.0: Life Support's state. Every two seconds it floods the air round it: if the flood stays within
 * {@link #MAX_VOLUME} blocks, the room is sealed and, while it has oxygen, everyone in it breathes without a suit.
 */
public class LifeSupportBlockEntity extends BlockEntity {
	public static final int MAX_VOLUME = 4096;
	public static final int MAX_CYLINDERS = 16;
	/** One cylinder keeps a base breathing for two Minecraft days (a crew of a few uses about a kilogram a day each). */
	public static final int TICKS_PER_CYLINDER = 48000;

	/** Sealed, supplied rooms per world: Life Support position -> the air blocks it fills. */
	private static final Map<World, Map<BlockPos, Set<Long>>> SEALED = new HashMap<>();

	int cylinders;
	int oxygenTicks;
	private int timer;
	/** Last measure: air blocks (or 0) and the first block where air escapes (null when sealed). */
	private int volume;
	@Nullable
	private BlockPos leak;

	public LifeSupportBlockEntity(BlockPos pos, BlockState state) {
		super(Moon.LIFE_SUPPORT_ENTITY, pos, state);
	}

	/** Does a sealed room with air hold {@code pos}? */
	public static boolean pressurized(World world, BlockPos pos) {
		Map<BlockPos, Set<Long>> rooms = SEALED.get(world);
		if (rooms == null) {
			return false;
		}
		long key = pos.asLong();
		for (Set<Long> room : rooms.values()) {
			if (room.contains(key)) {
				return true;
			}
		}
		return false;
	}

	static void forget(World world, BlockPos pos) {
		Map<BlockPos, Set<Long>> rooms = SEALED.get(world);
		if (rooms != null) {
			rooms.remove(pos);
		}
	}

	/** Can air pass through this block? Air and anything not a full cube, except a closed door or trapdoor. */
	static boolean open(World world, BlockPos p, BlockState s) {
		if (s.isAir()) {
			return true;
		}
		if (s.getBlock() instanceof DoorBlock || s.getBlock() instanceof TrapdoorBlock) {
			return s.get(Properties.OPEN);
		}
		return !s.isFullCube(world, p);
	}

	/** Floods the room round the block; fills {@link #volume} and {@link #leak}; returns the air blocks when sealed. */
	@Nullable
	Set<Long> measure(ServerWorld world, BlockPos pos) {
		Set<Long> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		for (Direction d : Direction.values()) {
			BlockPos n = pos.offset(d);
			if (open(world, n, world.getBlockState(n)) && seen.add(n.asLong())) {
				queue.add(n);
			}
		}
		leak = null;
		while (!queue.isEmpty()) {
			BlockPos at = queue.poll();
			if (seen.size() > MAX_VOLUME || at.getY() <= world.getBottomY() || at.getY() >= world.getTopY() - 1
					|| !world.isChunkLoaded(at)) {
				leak = at;
				break;
			}
			for (Direction d : Direction.values()) {
				BlockPos n = at.offset(d);
				if (!seen.contains(n.asLong()) && open(world, n, world.getBlockState(n))) {
					seen.add(n.asLong());
					queue.add(n);
				}
			}
		}
		if (seen.isEmpty()) {
			leak = pos.up();
		}
		volume = leak == null ? seen.size() : 0;
		return leak == null ? seen : null;
	}

	void tick(ServerWorld world, BlockPos pos, BlockState state) {
		if (timer++ % 40 != 0) {
			return;
		}
		if (oxygenTicks <= 0 && cylinders > 0) {
			cylinders--;
			oxygenTicks = TICKS_PER_CYLINDER;
			markDirty();
		}
		Set<Long> room = oxygenTicks > 0 ? measure(world, pos) : null;
		if (room != null) {
			SEALED.computeIfAbsent(world, w -> new HashMap<>()).put(pos.toImmutable(), room);
			oxygenTicks -= 40;
			markDirty();
		} else {
			forget(world, pos);
		}
		boolean lit = room != null;
		if (state.get(LifeSupportBlock.LIT) != lit) {
			world.setBlockState(pos, state.with(LifeSupportBlock.LIT, lit), 3);
		}
	}

	Text describe() {
		String ns = "message.periodic_elements.life_support.";
		int minutes = (oxygenTicks + cylinders * TICKS_PER_CYLINDER) / 1200;
		if (oxygenTicks <= 0 && cylinders == 0) {
			return Text.translatable(ns + "no_oxygen").formatted(Formatting.RED);
		}
		if (leak != null) {
			return Text.translatable(ns + "leak", leak.getX(), leak.getY(), leak.getZ(), minutes).formatted(Formatting.YELLOW);
		}
		return Text.translatable(ns + "sealed", volume, minutes, cylinders).formatted(Formatting.GREEN);
	}

	public int volume() {
		return volume;
	}

	/** Stores {@code n} more oxygen cylinders, up to the limit; returns how many it took. */
	public int addCylinders(int n) {
		int took = Math.max(0, Math.min(n, MAX_CYLINDERS - cylinders));
		cylinders += took;
		markDirty();
		return took;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		nbt.putInt("Cylinders", cylinders);
		nbt.putInt("OxygenTicks", oxygenTicks);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		cylinders = nbt.getInt("Cylinders");
		oxygenTicks = nbt.getInt("OxygenTicks");
	}
}
