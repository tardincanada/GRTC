package com.periodicelements.shrubs;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Greenhouses (3.16.0, Guilherme): a shrub or a tree sapling grows outside its biomes in a closed room, entered only by
 * a door or a trapdoor, under a roof all of glass. Up to {@link #MAX} blocks across and high. Unheated it is
 * temperate; a lit Greenhouse Heater among its walls or floor makes it warm, a running Greenhouse Cooler (3.22.0)
 * cold. A heater and a cooler in the same room cancel out. Temperate plants grow in all three.
 *
 * <p>The room is found by a flood fill from the plant through air, plants, leaves and logs. Any block that stops the
 * fill is a wall, floor or roof; a block straight above the room must be glass, a glass pane, a trapdoor (or a hanging
 * lantern). If the fill runs further than {@link #MAX} in any direction the room is open.
 */
public final class Greenhouse {
	public static final int MAX = 32;
	public static final String WARM = "warm", TEMPERATE = "temperate", COLD = "cold";
	private static final Map<Long, Long> CACHE_TIME = new java.util.concurrent.ConcurrentHashMap<>();
	private static final Map<Long, String> CACHE = new java.util.concurrent.ConcurrentHashMap<>();
	/** Biomes whose plants want warmth: a tree from any of them needs a heated greenhouse. */
	private static final Set<String> WARM_BIOMES = Set.of("jungle", "sparse_jungle", "bamboo_jungle", "mangrove_swamp",
			"savanna", "savanna_plateau", "windswept_savanna", "desert", "badlands", "eroded_badlands", "wooded_badlands", "beach");
	/** Biomes of cold lands and mountain tops: a tree only from these needs a cold greenhouse. */
	private static final Set<String> COLD_BIOMES = Set.of("snowy_plains", "ice_spikes", "snowy_taiga", "snowy_beach",
			"snowy_slopes", "frozen_peaks", "jagged_peaks", "stony_peaks", "grove");

	private Greenhouse() {
	}

	/** The climate a tree species needs in a greenhouse, from the biomes it grows in. */
	public static String climateOf(List<String> biomes) {
		if (biomes.stream().anyMatch(WARM_BIOMES::contains)) {
			return WARM;
		}
		return biomes.stream().allMatch(COLD_BIOMES::contains) ? COLD : TEMPERATE;
	}

	/** Whether a plant that needs {@code climate} grows at {@code pos} because it stands in a fitting greenhouse. */
	public static boolean allows(WorldView world, BlockPos pos, String climate) {
		String here = climateAt(world, pos);
		if (here == null) {
			return false;
		}
		return switch (climate) {
			case WARM -> here.equals(WARM);
			case TEMPERATE -> true;
			default -> here.equals(COLD);
		};
	}

	/** {@link #WARM}, {@link #TEMPERATE}, {@link #COLD}, or null if {@code pos} is not inside a greenhouse. Cached for 5 seconds. */
	@Nullable
	public static String climateAt(WorldView world, BlockPos pos) {
		long now = world instanceof World w && !w.isClient ? w.getTime() : -1;
		long key = pos.asLong();
		if (now >= 0) {
			Long t = CACHE_TIME.get(key);
			if (t != null && now - t < 100 && now >= t) {
				String c = CACHE.get(key);
				return c == null || c.isEmpty() ? null : c;
			}
			if (CACHE_TIME.size() > 4096) {
				CACHE_TIME.clear();
				CACHE.clear();
			}
		}
		String result = measure(world, pos);
		if (now >= 0) {
			CACHE_TIME.put(key, now);
			CACHE.put(key, result == null ? "" : result);
		}
		return result;
	}

	/** Forgets cached answers (tests change rooms within a few ticks). */
	public static void forget() {
		CACHE_TIME.clear();
		CACHE.clear();
	}

	private static boolean inside(BlockState s) {
		return s.getCollisionShape(EmptyView.INSTANCE, BlockPos.ORIGIN).isEmpty() || s.isIn(BlockTags.LEAVES)
				|| s.isIn(BlockTags.LOGS) || s.getBlock() instanceof PlantBlock || s.getBlock() instanceof SaplingBlock;
	}

	private static boolean roof(BlockState s) {
		return s.isIn(ConventionalBlockTags.GLASS_BLOCKS) || s.isIn(ConventionalBlockTags.GLASS_PANES)
				|| s.getBlock() instanceof TrapdoorBlock || s.getBlock() instanceof LanternBlock;
	}

	@Nullable
	private static String measure(WorldView world, BlockPos start) {
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<Long> seen = new HashSet<>();
		queue.add(start);
		seen.add(start.asLong());
		int minX = start.getX(), maxX = minX, minY = start.getY(), maxY = minY, minZ = start.getZ(), maxZ = minZ;
		boolean warm = false, cold = false;
		while (!queue.isEmpty()) {
			BlockPos p = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos n = p.offset(d);
				if (seen.contains(n.asLong())) {
					continue;
				}
				BlockState s = world.getBlockState(n);
				if (inside(s)) {
					seen.add(n.asLong());
					minX = Math.min(minX, n.getX());
					maxX = Math.max(maxX, n.getX());
					minY = Math.min(minY, n.getY());
					maxY = Math.max(maxY, n.getY());
					minZ = Math.min(minZ, n.getZ());
					maxZ = Math.max(maxZ, n.getZ());
					if (maxX - minX >= MAX || maxY - minY >= MAX || maxZ - minZ >= MAX || world.isOutOfHeightLimit(n)) {
						return null;
					}
					queue.add(n);
					continue;
				}
				if (d == Direction.UP && !roof(s)) {
					return null;
				}
				if (s.getBlock() instanceof GreenhouseHeaterBlock && s.get(GreenhouseHeaterBlock.LIT)) {
					warm = true;
				}
				if (s.getBlock() instanceof GreenhouseCoolerBlock && s.get(GreenhouseCoolerBlock.LIT)) {
					cold = true;
				}
			}
		}
		return warm == cold ? TEMPERATE : warm ? WARM : COLD;
	}

	/** Doors are walls even when open: their block is always there. */
	static boolean isDoor(BlockState s) {
		return s.getBlock() instanceof DoorBlock || s.getBlock() instanceof TrapdoorBlock;
	}

	/** An empty world for asking a block's collision shape without its neighbours. */
	private enum EmptyView implements BlockView {
		INSTANCE;

		@Override
		public net.minecraft.block.entity.BlockEntity getBlockEntity(BlockPos pos) {
			return null;
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			return net.minecraft.block.Blocks.AIR.getDefaultState();
		}

		@Override
		public net.minecraft.fluid.FluidState getFluidState(BlockPos pos) {
			return net.minecraft.fluid.Fluids.EMPTY.getDefaultState();
		}

		@Override
		public int getHeight() {
			return 384;
		}

		@Override
		public int getBottomY() {
			return -64;
		}
	}
}
