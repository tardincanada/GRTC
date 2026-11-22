package com.periodicelements.trees;

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Trees fall when cut, and loose leaves fall at once (Guilherme, 2026-11-08).
 * <ul>
 * <li>3.55.0: a natural leaf falls on the next tick (dropping its loot, instead of vanilla's slow decay) unless a
 * natural log of its own species is within {@link #RANGE} blocks in a straight line (a 7x7x7 cube). Natural = grown
 * there (world generation or a sapling), not placed by a player ({@link PlacedLogs}). Leaves a player placed never
 * fall. Applies to the mod's species, the rubber tree and the vanilla trees.</li>
 * <li>3.58.0: the leaf must also be joined, through leaves of its species, to such a log ({@link #connected}).</li>
 * <li>3.59.0: cut a trunk and the tree above the cut comes down: every natural log of that species left without
 * anything under it (the logs touching each other, diagonals too, rest on nothing but air, their own leaves or each
 * other) breaks and drops as an item, and the leaves fall with it.</li>
 * </ul>
 * Nothing here loads a chunk: a block in a chunk that is not loaded counts as holding the tree up.
 */
public final class LeafFall {
	public static final int RANGE = 3;
	private static final int MAX_CLUSTER = 4096;
	private static final int MAX_TRUNK = 2048;
	private static final Map<Block, Set<Block>> SUPPORT = new HashMap<>();
	private static final Map<Block, Set<Block>> LEAVES_OF_LOG = new HashMap<>();

	private static ServerWorld cacheWorld;
	private static long cacheTime = Long.MIN_VALUE;
	private static final Long2BooleanOpenHashMap JOINED = new Long2BooleanOpenHashMap();
	private static final Long2BooleanOpenHashMap STANDS = new Long2BooleanOpenHashMap();
	private static boolean felling;

	private LeafFall() {
	}

	/** These leaves are held up by these logs (and nothing else). */
	public static void support(Block leaves, Block... logs) {
		for (Block log : logs) {
			SUPPORT.computeIfAbsent(leaves, k -> new HashSet<>()).add(log);
			LEAVES_OF_LOG.computeIfAbsent(log, k -> new HashSet<>()).add(leaves);
		}
	}

	public static void registerVanilla() {
		Block[] oak = {Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD};
		support(Blocks.OAK_LEAVES, oak);
		support(Blocks.AZALEA_LEAVES, oak);
		support(Blocks.FLOWERING_AZALEA_LEAVES, oak);
		support(Blocks.SPRUCE_LEAVES, Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_WOOD);
		support(Blocks.BIRCH_LEAVES, Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD);
		support(Blocks.JUNGLE_LEAVES, Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD);
		support(Blocks.ACACIA_LEAVES, Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_ACACIA_WOOD);
		support(Blocks.DARK_OAK_LEAVES, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG,
				Blocks.STRIPPED_DARK_OAK_WOOD);
		support(Blocks.MANGROVE_LEAVES, Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_LOG,
				Blocks.STRIPPED_MANGROVE_WOOD);
		support(Blocks.CHERRY_LEAVES, Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD);
	}

	/** True if these leaves follow the rule. */
	public static boolean governs(Block leaves) {
		return SUPPORT.containsKey(leaves);
	}

	/** The block there, or null when its chunk is not loaded (never loads one). */
	@Nullable
	private static BlockState at(ServerWorld world, BlockPos pos) {
		// 3.68.4: the chunk manager's own lookup, which never waits for a chunk (and so never for the server thread)
		net.minecraft.world.chunk.WorldChunk chunk = world.getChunkManager().getWorldChunk(
				ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
		return chunk == null ? null : chunk.getBlockState(pos);
	}

	private static void freshCache(ServerWorld world) {
		if (cacheWorld != world || cacheTime != world.getTime()) {
			cacheWorld = world;
			cacheTime = world.getTime();
			JOINED.clear();
			STANDS.clear();
		}
	}

	/** True if a standing natural log of the leaves' species is within {@link #RANGE} blocks. */
	public static boolean supported(ServerWorld world, BlockPos pos, Block leaves) {
		Set<Block> logs = SUPPORT.get(leaves);
		if (logs == null) {
			return true;
		}
		PlacedLogs placed = PlacedLogs.of(world);
		BlockPos.Mutable m = new BlockPos.Mutable();
		for (int dy = -RANGE; dy <= RANGE; dy++) {
			for (int dx = -RANGE; dx <= RANGE; dx++) {
				for (int dz = -RANGE; dz <= RANGE; dz++) {
					m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
					BlockState s = at(world, m);
					if (s == null) {
						return true;
					}
					if (logs.contains(s.getBlock()) && !placed.placed(m) && stands(world, m, logs)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Should these (non-player) leaves fall now? */
	public static boolean loose(BlockState state, ServerWorld world, BlockPos pos) {
		return governs(state.getBlock()) && !state.get(LeavesBlock.PERSISTENT)
				&& (!supported(world, pos, state.getBlock()) || !connected(world, pos, state.getBlock()));
	}

	/**
	 * 3.58.0: the leaf is joined, through leaves of its own species, to a standing natural log of that species. When it
	 * is not, every leaf of that loose clump is checked on the next tick, so the whole crown comes down together.
	 */
	public static boolean connected(ServerWorld world, BlockPos pos, Block leaves) {
		Set<Block> logs = SUPPORT.get(leaves);
		if (logs == null) {
			return true;
		}
		freshCache(world);
		long key = pos.asLong();
		if (JOINED.containsKey(key)) {
			return JOINED.get(key);
		}
		PlacedLogs placed = PlacedLogs.of(world);
		LongOpenHashSet seen = new LongOpenHashSet();
		LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
		seen.add(key);
		queue.enqueue(key);
		boolean found = false;
		BlockPos.Mutable m = new BlockPos.Mutable();
		search:
		while (!queue.isEmpty()) {
			long here = queue.dequeueLong();
			for (Direction d : Direction.values()) {
				m.set(here).move(d);
				BlockState s = at(world, m);
				if (s == null || logs.contains(s.getBlock()) && !placed.placed(m) && stands(world, m, logs)) {
					found = true;
					break search;
				}
				if (s.isOf(leaves) && seen.add(m.asLong())) {
					if (seen.size() > MAX_CLUSTER) {
						found = true;
						break search;
					}
					queue.enqueue(m.asLong());
				}
			}
		}
		for (long p : seen) {
			JOINED.put(p, found);
		}
		if (!found) {
			for (long p : seen) {
				if (p != key) {
					world.scheduleBlockTick(BlockPos.fromLong(p), leaves, 1);
				}
			}
		}
		return found;
	}

	/** 3.59.0: does the trunk this log belongs to still stand on something? */
	public static boolean stands(ServerWorld world, BlockPos pos, Set<Block> logs) {
		freshCache(world);
		long key = pos.asLong();
		if (STANDS.containsKey(key)) {
			return STANDS.get(key);
		}
		return trunk(world, pos, logs, null);
	}

	/** Floods the trunk (same-species logs, diagonals too) from {@code pos}; fills {@code out} with its logs if given. */
	private static boolean trunk(ServerWorld world, BlockPos pos, Set<Block> logs, @Nullable LongOpenHashSet out) {
		freshCache(world);
		Set<Block> ownLeaves = new HashSet<>();
		for (Block l : logs) {
			Set<Block> ls = LEAVES_OF_LOG.get(l);
			if (ls != null) {
				ownLeaves.addAll(ls);
			}
		}
		long key = pos.asLong();
		LongOpenHashSet seen = new LongOpenHashSet();
		LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
		seen.add(key);
		queue.enqueue(key);
		boolean stands = false, complete = true;
		BlockPos.Mutable m = new BlockPos.Mutable();
		search:
		while (!queue.isEmpty()) {
			long here = queue.dequeueLong();
			m.set(here).move(Direction.DOWN);
			BlockState under = at(world, m);
			if (under == null || !under.isAir() && !ownLeaves.contains(under.getBlock()) && !logs.contains(under.getBlock())) {
				stands = true;
				if (out == null) {
					complete = false;
					break;
				}
			}
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						m.set(here).move(dx, dy, dz);
						BlockState s = at(world, m);
						if (s == null) {
							stands = true;
							complete = false;
							break search;
						}
						if (logs.contains(s.getBlock()) && seen.add(m.asLong())) {
							if (seen.size() > MAX_TRUNK) {
								stands = true;
								complete = false;
								break search;
							}
							queue.enqueue(m.asLong());
						}
					}
				}
			}
		}
		if (out != null) {
			out.addAll(seen);
		}
		if (stands || complete) {
			for (long p : seen) {
				STANDS.put(p, stands);
			}
		}
		return stands;
	}

	/** Runs a multi-block harvest (the chainsaw takes the whole tree into the pack) with no felling of its own. */
	public static int withoutFelling(java.util.function.IntSupplier harvest) {
		boolean was = felling;
		felling = true;
		try {
			return harvest.getAsInt();
		} finally {
			felling = was;
		}
	}

	/** Drops the leaves' loot and removes them, as vanilla decay does. */
	public static void fall(BlockState state, ServerWorld world, BlockPos pos) {
		Block.dropStacks(state, world, pos);
		world.removeBlock(pos, false);
	}

	/** A player placed a block: logs they place are not natural. */
	public static void placedByPlayer(ServerWorld world, BlockPos pos, BlockState state) {
		if (state.isIn(BlockTags.LOGS)) {
			PlacedLogs.of(world).add(pos);
		}
	}

	/** Any block change on the server: when a log goes, what it held up comes down. */
	public static void changed(ServerWorld world, BlockPos pos, BlockState old, BlockState now) {
		// 3.68.4: world generation also reports block changes, from its worker threads (a tree feature replacing a
		// log). Looking at the world from there waited for the server thread while the server thread waited for that
		// chunk: the world froze, nothing dropped and saving never ended. Only changes on the server thread count.
		if (world.getServer() == null || !world.getServer().isOnThread()) {
			return;
		}
		if (old.getBlock() == now.getBlock() || !old.isIn(BlockTags.LOGS)) {
			return;
		}
		if (!now.isIn(BlockTags.LOGS)) {
			PlacedLogs.of(world).forget(pos);
		}
		Set<Block> leaves = LEAVES_OF_LOG.get(old.getBlock());
		if (leaves == null) {
			return;
		}
		scheduleAround(world, pos, leaves);
		if (felling || now.isIn(BlockTags.LOGS)) {
			return;
		}
		Set<Block> logs = SUPPORT.get(leaves.iterator().next());
		// The logs left above the cut: if their trunk no longer stands, the tree comes down.
		PlacedLogs placed = PlacedLogs.of(world);
		LongOpenHashSet done = new LongOpenHashSet();
		LongArrayList fell = new LongArrayList();
		BlockPos.Mutable n = new BlockPos.Mutable();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					n.set(pos).move(dx, dy, dz);
					if (done.contains(n.asLong())) {
						continue;
					}
					BlockState s = at(world, n);
					if (s == null || !logs.contains(s.getBlock())) {
						continue;
					}
					LongOpenHashSet trunk = new LongOpenHashSet();
					if (!trunk(world, n.toImmutable(), logs, trunk)) {
						for (long p : trunk) {
							if (!placed.placed(BlockPos.fromLong(p))) {
								fell.add(p);
							}
						}
					}
					done.addAll(trunk);
				}
			}
		}
		if (fell.isEmpty()) {
			return;
		}
		felling = true;
		try {
			for (long p : fell) {
				world.breakBlock(BlockPos.fromLong(p), true);
			}
		} finally {
			felling = false;
		}
	}

	private static void scheduleAround(ServerWorld world, BlockPos pos, Set<Block> leaves) {
		BlockPos.Mutable m = new BlockPos.Mutable();
		for (int dy = -RANGE; dy <= RANGE; dy++) {
			for (int dx = -RANGE; dx <= RANGE; dx++) {
				for (int dz = -RANGE; dz <= RANGE; dz++) {
					m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
					BlockState s = at(world, m);
					if (s != null && leaves.contains(s.getBlock()) && !s.get(LeavesBlock.PERSISTENT)) {
						world.scheduleBlockTick(m.toImmutable(), s.getBlock(), 1);
					}
				}
			}
		}
	}
}
