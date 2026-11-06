package com.periodicelements.crops;

import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.FieldState;
import com.periodicelements.farm.Sprayed;
import com.periodicelements.shrubs.Greenhouse;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Crop genetics at work (3.23.0): how fast a plant grows (cycle gene, climate, drought), who pollinates it, what
 * genes its seeds get at harvest, and caterpillars and rust in large monocultures.
 */
public final class Genetics {
	/** Growth with the early-cycle trait (cc): 30% shorter cycle. */
	public static final double EARLY = 1.43;
	/** Growth in the wrong climate, or in dry country without drought tolerance. */
	public static final double PENALTY = 0.5;
	/** Chance a ripe plant with the yield trait (P_) gives one more of its crop. */
	public static final double YIELD_BONUS = 0.3;
	/** More plants of one kind than this around a plant (9x9x3) is a monoculture, where pests break out. */
	public static final int MONOCULTURE = 20;
	/** Chance per random tick that a plant in a monoculture catches caterpillars or rust. */
	public static final double OUTBREAK = 1 / 300.0;
	/** Chance per random tick that an infested plant passes it to a neighbour. */
	public static final double SPREAD = 0.5;
	/** A resistant plant catches it this many times less often. */
	public static final int RESISTANCE = 4;
	/** 3.37.0: an F1 hybrid of two pure lines grows a fifth faster and has 10% more chance of one more crop. */
	public static final double HYBRID_VIGOR = 1.2, HYBRID_BONUS = 0.1;
	/** 3.37.0: semi-dwarf (ss) wheat and rice on NPK ground grow half again as fast and give one more crop. */
	public static final double DWARF_NPK = 1.5;
	/** 3.37.0: crops within this many blocks of a Gamma Garden pass on 20 times as many mutations. */
	public static final int GAMMA_RADIUS = 6;
	public static final double GAMMA_MUTATION = 20.0 / Genome.MUTATION;
	/** Chance an allele from cherry-grove grass wheat is the semi-dwarf s. */
	public static final double WILD_DWARF = 0.15;
	/** Chance a wheat seed from wild grass carries the genes of a wild plant. */
	public static final double WILD_GRASS = 0.3;

	public static GameRules.Key<GameRules.BooleanRule> PESTS;
	static final Map<Block, CropInfo> INFO = new HashMap<>();
	private static final ThreadLocal<Boolean> GUARD = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Boolean> IN_PLACE = ThreadLocal.withInitial(() -> false);

	private Genetics() {
	}

	public static CropInfo info(Block block) {
		return INFO.get(block);
	}

	public static boolean pestsOn(ServerWorld world) {
		return PESTS == null || world.getGameRules().getBoolean(PESTS);
	}

	// ---------- genomes in the world ----------

	/** The plant's entry: what is stored, or the genes of a wild or ordinary plant. */
	public static long entry(ServerWorld world, BlockPos pos, BlockState state) {
		Long e = CropGenomes.of(world).get(pos);
		if (e != null) {
			return e;
		}
		CropInfo info = info(state.getBlock());
		if (info != null && state.contains(GeneticCropBlock.WILD) && state.get(GeneticCropBlock.WILD)) {
			return wildGenome(info, pos.asLong());
		}
		return Genome.DEFAULT;
	}

	public static int wildGenome(CropInfo info, long seed) {
		return Genome.wild(Random.create(seed * 31 + info.id().hashCode()), info.arid(), info.cold());
	}

	/** Seeds planted: the plant takes their genome. */
	public static void planted(ServerWorld world, BlockPos pos, ItemStack stack) {
		Integer g = stack.get(Crops.GENOME);
		CropGenomes.of(world).put(pos, CropGenomes.of(g == null ? Genome.DEFAULT : g, 0));
	}

	public static boolean infested(long entry) {
		return (entry & (CropGenomes.PEST | CropGenomes.DISEASE)) != 0;
	}

	// ---------- growth ----------

	/** cold, temperate or warm: a greenhouse's climate, otherwise the biome's. */
	public static String climate(WorldView world, BlockPos pos) {
		String g = Greenhouse.climateAt(world, pos);
		if (g != null) {
			return g;
		}
		float t = world.getBiome(pos).value().getTemperature();
		return t < 0.3f ? Greenhouse.COLD : t >= 0.95f ? Greenhouse.WARM : Greenhouse.TEMPERATE;
	}

	/** Dry country: a hot biome where it never rains (desert, savanna, badlands), outside a greenhouse. */
	public static boolean dry(WorldView world, BlockPos pos) {
		Biome b = world.getBiome(pos).value();
		return !b.hasPrecipitation() && b.getTemperature() > 1.0f && Greenhouse.climateAt(world, pos) == null;
	}

	/** Rice needs water beside its farmland (or around the plant itself). */
	public static boolean waterNear(WorldView world, BlockPos pos) {
		for (BlockPos p : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 0, 1))) {
			if (world.getFluidState(p).isIn(FluidTags.WATER)) {
				return true;
			}
		}
		return false;
	}

	public static boolean climateFits(CropInfo info, int genome, String here) {
		return info.climates() == null || info.climates().contains(here)
				|| here.equals(Greenhouse.COLD) && Genome.has(genome, Genome.COLD);
	}

	/** Growth speed relative to an ordinary plant in a suitable place: 0 for an infested plant. */
	public static double multiplier(WorldView world, BlockPos pos, CropInfo info, long entry) {
		if (infested(entry)) {
			return 0;
		}
		int g = CropGenomes.genome(entry);
		double m = Genome.has(g, Genome.CYCLE) ? EARLY : 1.0;
		if (!climateFits(info, g, climate(world, pos))) {
			m *= PENALTY;
		}
		if (!info.arid() && dry(world, pos) && !Genome.has(g, Genome.DROUGHT) && !Genome.transgene(g, Genome.DREB)) {
			m *= PENALTY;
		}
		if (Genome.hybrid(g)) {
			m *= HYBRID_VIGOR;
		}
		if (dwarfOnNpk(world, pos, info, g)) {
			m *= DWARF_NPK;
		}
		if (info.water() && !waterNear(world, pos)) {
			return 0;
		}
		return m;
	}

	/** Semi-dwarf wheat or rice on farmland given NPK prills: the Green Revolution. */
	public static boolean dwarfOnNpk(WorldView world, BlockPos pos, CropInfo info, int genome) {
		if (!info.stature() || !Genome.has(genome, Genome.STATURE) || !(world instanceof ServerWorld sw)) {
			return false;
		}
		com.periodicelements.farm.Fertilizer f = com.periodicelements.farm.Fertilizer.of(sw);
		return f.strong(pos.down()) && f.harvestsLeft(pos.down()) > 0;
	}

	/**
	 * Called at the head of {@link CropBlock#randomTick}; returns true if this took over the tick. Grows the plant
	 * at its genetic speed, pollinates it when it flowers, and runs caterpillars and rust.
	 */
	public static boolean randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (GUARD.get()) {
			return false;
		}
		CropInfo info = info(state.getBlock());
		if (info == null) {
			return false;
		}
		CropGenomes genomes = CropGenomes.of(world);
		long entry = entry(world, pos, state);
		if (infested(entry)) {
			infestedTick(world, pos, state, entry, random);
			return true;
		}
		if (pestsOn(world) && random.nextDouble() < OUTBREAK && monoculture(world, pos, state.getBlock())
				&& infect(world, pos, entry, random.nextBoolean(), random)) {
			return true;
		}
		double m = multiplier(world, pos, info, entry);
		int ticks = (int) m + (random.nextDouble() < m - (int) m ? 1 : 0);
		GUARD.set(true);
		try {
			for (int i = 0; i < ticks; i++) {
				BlockState now = world.getBlockState(pos);
				if (!now.isOf(state.getBlock())) {
					break;
				}
				now.randomTick(world, pos, random);
			}
		} finally {
			GUARD.set(false);
		}
		afterGrowth(world, pos);
		return true;
	}

	/** After growth (random tick or bone meal): a flowering plant is pollinated. */
	public static void afterGrowth(ServerWorld world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		CropInfo info = info(state.getBlock());
		if (info != null && flowering(state)) {
			pollinate(world, pos, state, info, entry(world, pos, state), world.random, true);
		}
	}

	public static boolean flowering(BlockState state) {
		if (state.contains(GeneticCropBlock.WILD) && state.get(GeneticCropBlock.WILD)) {
			return true;
		}
		return state.getBlock() instanceof CropBlock c && c.getAge(state) * 4 >= c.getMaxAge() * 3;
	}

	/**
	 * Picks the father of this plant's seeds if it has none yet: a flowering neighbour of its kind (within 2 blocks)
	 * with the crop's outcrossing chance, otherwise its own pollen. Returns the entry with the father.
	 */
	public static long pollinate(ServerWorld world, BlockPos pos, BlockState state, CropInfo info, long entry, Random random, boolean store) {
		if ((entry & CropGenomes.POLLINATED) != 0 || info.cloned()) {
			return entry;
		}
		int mother = CropGenomes.genome(entry);
		int father = mother;
		if (info.outcross() > 0 && random.nextDouble() < info.outcross()) {
			List<BlockPos> donors = new ArrayList<>();
			for (BlockPos p : BlockPos.iterate(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
				if (!p.equals(pos)) {
					BlockState s = world.getBlockState(p);
					if (s.isOf(state.getBlock()) && flowering(s)) {
						donors.add(p.toImmutable());
					}
				}
			}
			if (!donors.isEmpty()) {
				BlockPos d = donors.get(random.nextInt(donors.size()));
				father = CropGenomes.genome(entry(world, d, world.getBlockState(d)));
			}
		}
		long out = CropGenomes.withFather(entry, father, false);
		if (store && (mother != Genome.DEFAULT || father != Genome.DEFAULT || (entry & 0xFFL << 24) != 0)) {
			CropGenomes.of(world).put(pos, out);
		}
		return out;
	}

	// ---------- pests and diseases ----------

	public static boolean monoculture(World world, BlockPos pos, Block block) {
		int n = 0;
		for (BlockPos p : BlockPos.iterate(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {
			if (world.getBlockState(p).isOf(block) && ++n > MONOCULTURE) {
				return true;
			}
		}
		return false;
	}

	/** Whether the plant is protected: DDT keeps caterpillars off, Bordeaux mixture keeps rust off. */
	public static boolean protectedFrom(ServerWorld world, BlockPos pos, boolean pest) {
		return pest ? FieldState.of(world).treated(FieldState.Kind.DDT, pos, world.getTime()) : Sprayed.of(world).contains(pos);
	}

	/** Tries to give the plant caterpillars (pest) or rust; resistant plants catch it 4 times less often. */
	public static boolean infect(ServerWorld world, BlockPos pos, long entry, boolean pest, Random random) {
		if (infested(entry) || protectedFrom(world, pos, pest)) {
			return false;
		}
		if (pest && Genome.transgene(CropGenomes.genome(entry), Genome.BT)) {
			return false; // Bt: the plant makes its own insecticide
		}
		if (Genome.has(CropGenomes.genome(entry), pest ? Genome.PEST : Genome.DISEASE) && random.nextInt(RESISTANCE) != 0) {
			return false;
		}
		CropGenomes.of(world).put(pos, entry | (pest ? CropGenomes.PEST : CropGenomes.DISEASE));
		world.spawnParticles(pest ? ParticleTypes.ASH : ParticleTypes.CRIMSON_SPORE,
				pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0);
		return true;
	}

	private static void infestedTick(ServerWorld world, BlockPos pos, BlockState state, long entry, Random random) {
		boolean pest = (entry & CropGenomes.PEST) != 0;
		if (protectedFrom(world, pos, pest)) {
			cure(world, pos, pest);
			return;
		}
		world.spawnParticles(pest ? ParticleTypes.ASH : ParticleTypes.CRIMSON_SPORE,
				pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0);
		if (!pestsOn(world) || random.nextDouble() >= SPREAD) {
			return;
		}
		BlockPos n = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
		BlockState ns = world.getBlockState(n);
		if (!n.equals(pos) && ns.isOf(state.getBlock())) {
			infect(world, n, entry(world, n, ns), pest, random);
		}
	}

	/** Clears caterpillars (pest) or rust from a plant; true if it had them. */
	public static boolean cure(ServerWorld world, BlockPos pos, boolean pest) {
		CropGenomes genomes = CropGenomes.of(world);
		Long e = genomes.get(pos);
		long flag = pest ? CropGenomes.PEST : CropGenomes.DISEASE;
		if (e == null || (e & flag) == 0) {
			return false;
		}
		genomes.put(pos, e & ~flag);
		return true;
	}

	/** Clears caterpillars from the crops within {@code radius} blocks; returns how many. */
	public static int curePests(ServerWorld world, BlockPos center, int radius) {
		int n = 0;
		for (BlockPos p : BlockPos.iterate(center.add(-radius, -1, -radius), center.add(radius, 1, radius))) {
			if (info(world.getBlockState(p).getBlock()) != null && cure(world, p.toImmutable(), true)) {
				n++;
			}
		}
		return n;
	}

	// ---------- harvest ----------

	/** Picks a perennial in place: its drops, with the plant kept (it flowers again). */
	public static List<ItemStack> harvestInPlace(BlockState state, ServerWorld world, BlockPos pos) {
		IN_PLACE.set(true);
		List<ItemStack> drops;
		try {
			drops = Block.getDroppedStacks(state, world, pos, null);
		} finally {
			IN_PLACE.set(false);
		}
		CropGenomes genomes = CropGenomes.of(world);
		Long e = genomes.get(pos);
		if (e != null) {
			genomes.put(pos, CropGenomes.withoutFather(e));
		}
		return drops;
	}

	/**
	 * The drops of a crop block, with genes: each seed is a child of the plant and its pollen (a copy, for crops grown
	 * from cuttings); a ripe plant with the yield trait may give one more, an infested one gives one less. Wheat
	 * seeds from wild grass sometimes carry wild genes.
	 */
	public static List<ItemStack> drops(BlockState state, ServerWorld world, BlockPos pos, List<ItemStack> drops) {
		Block block = state.getBlock();
		if (isGrass(state)) {
			return wildGrass(world, pos, drops);
		}
		CropInfo info = info(block);
		if (info == null || !(block instanceof CropBlock crop)) {
			return drops;
		}
		Random random = world.random;
		long entry = entry(world, pos, state);
		boolean ripe = crop.isMature(state);
		if (ripe) {
			entry = pollinate(world, pos, state, info, entry, random, false);
		}
		int mother = CropGenomes.genome(entry);
		int father = (entry & CropGenomes.POLLINATED) != 0 ? CropGenomes.father(entry) : mother;
		Item seed = info.seed().get();
		Item product = info.product().get();
		List<ItemStack> out = new ArrayList<>();
		Map<Integer, Integer> seeds = new LinkedHashMap<>();
		double mutation = nearGamma(world, pos) ? GAMMA_MUTATION : 1.0 / Genome.MUTATION;
		boolean less = ripe && infested(entry);
		for (ItemStack s : drops) {
			if (less && s.isOf(product) && !s.isEmpty()) {
				s.decrement(1);
				less = false;
			}
			if (s.isEmpty()) {
				continue;
			}
			if (s.isOf(seed)) {
				for (int i = 0; i < s.getCount(); i++) {
					int g = info.cloned() ? Genome.mutate(mother, random, mutation - 1.0 / Genome.MUTATION)
							: Genome.child(mother, father, random, mutation);
					seeds.merge(g, 1, Integer::sum);
				}
			} else {
				out.add(s);
			}
		}
		int more = 0;
		if (ripe && !infested(entry)) {
			more += Genome.has(mother, Genome.YIELD) && random.nextDouble() < YIELD_BONUS ? 1 : 0;
			more += Genome.hybrid(mother) && random.nextDouble() < HYBRID_BONUS ? 1 : 0;
			more += dwarfOnNpk(world, pos, info, mother) ? 1 : 0;
		}
		for (int i = 0; i < more; i++) {
			if (product == seed) {
				seeds.merge(info.cloned() ? mother : Genome.child(mother, father, random, mutation), 1, Integer::sum);
			} else {
				out.add(new ItemStack(product));
			}
		}
		for (var e : seeds.entrySet()) {
			out.add(seedStack(seed, e.getKey(), e.getValue()));
		}
		if (!IN_PLACE.get()) {
			// Inoculated legumes (3.37.0): rhizobia fix twice the nitrogen, like NPK prills on the farmland.
			if (ripe && info.legume() && (entry & CropGenomes.INOCULATED) != 0 && world.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
				com.periodicelements.farm.Fertilizer.of(world).fertilize(pos.down(), true);
			}
			CropGenomes.of(world).forget(pos);
		}
		return out;
	}

	public static boolean isGrass(BlockState state) {
		return state.isOf(Blocks.SHORT_GRASS) || state.isOf(Blocks.TALL_GRASS) || state.isOf(Blocks.FERN);
	}

	public static ItemStack seedStack(Item seed, int genome, int count) {
		ItemStack s = new ItemStack(seed, count);
		setGenome(s, genome);
		return s;
	}

	/** Puts the genome on a stack (none for the ordinary plant); golden rice becomes something to eat. */
	public static void setGenome(ItemStack s, int genome) {
		if ((genome & Genome.MASK) != Genome.DEFAULT || Genome.stamp(genome) != 0) {
			s.set(Crops.GENOME, genome);
		} else {
			s.remove(Crops.GENOME);
		}
		if (Genome.transgene(genome, Genome.GOLDEN) && s.isOf(Crops.ITEMS.get("rice"))) {
			s.set(net.minecraft.component.DataComponentTypes.FOOD, Crops.GOLDEN_RICE_FOOD);
		}
	}

	/** Is a Gamma Garden close enough to irradiate this plant? */
	public static boolean nearGamma(ServerWorld world, BlockPos pos) {
		if (Crops.GAMMA_GARDEN == null) {
			return false;
		}
		for (BlockPos p : BlockPos.iterate(pos.add(-GAMMA_RADIUS, -2, -GAMMA_RADIUS), pos.add(GAMMA_RADIUS, 2, GAMMA_RADIUS))) {
			if (world.getBlockState(p).isOf(Crops.GAMMA_GARDEN)) {
				return true;
			}
		}
		return false;
	}

	private static List<ItemStack> wildGrass(ServerWorld world, BlockPos pos, List<ItemStack> drops) {
		CropInfo wheat = info(Blocks.WHEAT);
		if (wheat == null) {
			return drops;
		}
		for (ItemStack s : drops) {
			if (s.isOf(net.minecraft.item.Items.WHEAT_SEEDS) && world.random.nextDouble() < WILD_GRASS) {
				int g = wildGenome(wheat, world.random.nextLong());
				// Wheat of the cherry groves sometimes carries the semi-dwarf allele (3.37.0).
				if (world.getBiome(pos).matchesKey(net.minecraft.world.biome.BiomeKeys.CHERRY_GROVE)) {
					int dwarf = (world.random.nextDouble() < WILD_DWARF ? 1 : 0) + (world.random.nextDouble() < WILD_DWARF ? 1 : 0);
					g = Genome.with(g, Genome.STATURE, 2 - dwarf);
				}
				if (g != Genome.DEFAULT) {
					s.set(Crops.GENOME, g);
				}
			}
		}
		return drops;
	}

	static String id(String path) {
		return PeriodicElements.MOD_ID + ":" + path;
	}
}
