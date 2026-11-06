package com.periodicelements.farm;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Fertilized Farmland: superphosphate or sulfate of ammonia spread on farmland makes the crop on it grow half again as
 * fast and give one more of its crop when harvested ripe, for {@link Farm#fertilizerHarvests()} harvests. Kept per
 * world as farmland position -> harvests left.
 */
public class Fertilizer extends PersistentState {
	private static final String KEY = "periodic_elements_fertilized";
	private static final Type<Fertilizer> TYPE = new Type<>(Fertilizer::new, Fertilizer::read, null);
	/** Extra growth: random ticks given on top of the world's own, as a share of them. */
	public static final double EXTRA_GROWTH = 0.5;
	/** NPK prills (1.9.0): the crop grows twice as fast. */
	public static final double NPK_GROWTH = 1.0;
	/** Stored with the harvests left on farmland given NPK prills. */
	private static final int STRONG = 1000;
	private final Map<Long, Integer> fields = new HashMap<>();

	public static Fertilizer of(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, KEY);
	}

	private static Fertilizer read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		Fertilizer f = new Fertilizer();
		long[] pos = nbt.getLongArray("Pos");
		int[] left = nbt.getIntArray("Left");
		for (int i = 0; i < Math.min(pos.length, left.length); i++) {
			f.fields.put(pos[i], left[i]);
		}
		return f;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		long[] pos = new long[fields.size()];
		int[] left = new int[fields.size()];
		int i = 0;
		for (var e : fields.entrySet()) {
			pos[i] = e.getKey();
			left[i++] = e.getValue();
		}
		nbt.putLongArray("Pos", pos);
		nbt.putIntArray("Left", left);
		return nbt;
	}

	public int harvestsLeft(BlockPos farmland) {
		return fields.getOrDefault(farmland.asLong(), 0) % STRONG;
	}

	/** Was this farmland given NPK prills (twice as fast) rather than a single fertilizer? */
	public boolean strong(BlockPos farmland) {
		return fields.getOrDefault(farmland.asLong(), 0) >= STRONG;
	}

	public void fertilize(BlockPos farmland) {
		fertilize(farmland, false);
	}

	public void fertilize(BlockPos farmland, boolean npk) {
		// A single fertilizer on NPK ground tops up the harvests but keeps the faster growth.
		boolean strong = npk || strong(farmland) && harvestsLeft(farmland) > 0;
		fields.put(farmland.asLong(), Farm.fertilizerHarvests() + (strong ? STRONG : 0));
		markDirty();
	}

	/** A ripe crop was harvested from this farmland: one fewer harvest left. */
	public void harvested(BlockPos farmland) {
		Integer left = fields.get(farmland.asLong());
		if (left != null) {
			if (left % STRONG <= 1) {
				fields.remove(farmland.asLong());
			} else {
				fields.put(farmland.asLong(), left - 1);
			}
			markDirty();
		}
	}

	/** Extra random ticks for crops on fertilized farmland; forgets farmland that is gone. */
	public void tick(ServerWorld world) {
		if (fields.isEmpty()) {
			return;
		}
		int speed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
		double chance = speed / 4096.0 * EXTRA_GROWTH;
		double strongChance = speed / 4096.0 * NPK_GROWTH;
		Iterator<Map.Entry<Long, Integer>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, Integer> field = it.next();
			BlockPos pos = BlockPos.fromLong(field.getKey());
			if (!world.isChunkLoaded(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4))) {
				continue;
			}
			if (!world.getBlockState(pos).isOf(Blocks.FARMLAND)) {
				it.remove(); // trampled, dug up or built over
				markDirty();
				continue;
			}
			BlockPos crop = pos.up();
			BlockState state = world.getBlockState(crop);
			if (state.getBlock() instanceof CropBlock && state.hasRandomTicks() && world.random.nextDouble() < (field.getValue() >= STRONG ? strongChance : chance)) {
				state.randomTick(world, crop, world.random);
			}
		}
	}

	/** The crop item a ripe crop block gives (wheat, not seeds). */
	public static Item cropOf(BlockState state) {
		if (state.isOf(Blocks.WHEAT)) {
			return Items.WHEAT;
		}
		if (state.isOf(Blocks.BEETROOTS)) {
			return Items.BEETROOT;
		}
		if (state.getBlock() instanceof com.periodicelements.crops.GeneticCropBlock crop && crop.info() != null) {
			return crop.product();
		}
		return state.getBlock().asItem();
	}
}
