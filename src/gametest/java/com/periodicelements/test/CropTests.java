package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.crops.CropGenomes;
import com.periodicelements.crops.CropInfo;
import com.periodicelements.crops.Crops;
import com.periodicelements.crops.FeedingTroughBlockEntity;
import com.periodicelements.crops.GeneticCropBlock;
import com.periodicelements.crops.Genetics;
import com.periodicelements.crops.Genome;
import com.periodicelements.crops.PollinationBrushItem;
import com.periodicelements.crops.SeedLensItem;
import com.periodicelements.farm.Fertilizer;
import com.periodicelements.farm.Sprayed;
import com.periodicelements.research.Research;
import com.periodicelements.shrubs.Greenhouse;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;

import java.util.List;

/** 3.23.0 crops and genetics: Mendel's ratios, seeds that keep their genes, growth, pests, tools and the trough. */
public class CropTests implements FabricGameTest {
	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	/** How many alleles differ between two genomes (mutations are single allele flips). */
	private static int letters(int a, int b) {
		return Integer.bitCount((a ^ b) & Genome.MASK);
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
		Greenhouse.forget();
	}

	private static List<ItemEntity> dropped(TestContext ctx, BlockPos abs, Item item) {
		List<ItemEntity> list = ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(abs).expand(2),
				// only this test's own plot: the next plot may hold another genetics test's seeds
				e -> e.getStack().isOf(item) && ctx.getTestBox().expand(0.5).contains(e.getPos()));
		return list;
	}

	/** A field of {@code n x n} ripe or young plants on farmland, starting at (1, 2, 1). */
	private static void field(TestContext ctx, Block crop, int n, int age) {
		for (int x = 0; x < n; x++) {
			for (int z = 0; z < n; z++) {
				BlockPos p = new BlockPos(1 + x, 2, 1 + z);
				ctx.setBlockState(p.down(), Blocks.FARMLAND);
				ctx.setBlockState(p, ((CropBlock) crop).withAge(age));
				CropGenomes.of(ctx.getWorld()).forget(ctx.getAbsolutePos(p));
			}
		}
	}

	private static int genome(String genotype) {
		int g = 0;
		String[] parts = genotype.split(" ");
		for (int i = 0; i < parts.length; i++) {
			int caps = (Character.isUpperCase(parts[i].charAt(0)) ? 1 : 0) + (Character.isUpperCase(parts[i].charAt(1)) ? 1 : 0);
			g = Genome.with(g, i, caps);
		}
		return g;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void mendelsRatios(TestContext ctx) {
		Random random = Random.create(42);
		int hetero = genome("Cc Pp Rr Dd Ff Ii Ss");
		int n = 8000;
		int[] shows = new int[Genome.GENES];
		for (int i = 0; i < n; i++) {
			int child = Genome.child(hetero, hetero, random);
			for (int g = 0; g < Genome.GENES; g++) {
				if (Genome.has(child, g)) {
					shows[g]++;
				}
			}
		}
		// Dominant traits (P, R, F, I) show in 3/4 of the children, recessive ones (cc, dd, ss) in 1/4.
		for (int g = 0; g < Genome.GENES; g++) {
			double share = shows[g] / (double) n;
			double expect = g == Genome.CYCLE || g == Genome.DROUGHT || g == Genome.STATURE ? 0.25 : 0.75;
			check(ctx, Math.abs(share - expect) < 0.03, "gene " + Genome.LETTERS.charAt(g) + ": " + share + " not ~" + expect);
		}
		// A homozygote selfed breeds true (but for the rare mutation).
		int pure = genome("cc PP RR dd FF II");
		int same = 0;
		for (int i = 0; i < 1000; i++) {
			if (Genome.child(pure, pure, random) == pure) {
				same++;
			}
		}
		check(ctx, same > 950 && same < 1000, "a pure line breeds true but for ~2% mutations: " + same);
		check(ctx, Genome.genotype(genome("Cc PP rr dd Ff ii")).equals("Cc PP rr dd Ff ii"), "genotype text");
		check(ctx, Genome.DEFAULT == genome("CC pp rr DD ff ii"), "the ordinary plant is CC pp rr DD ff ii");
		check(ctx, Genome.traits(Genome.DEFAULT) == null, "the ordinary plant has no traits");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void seedsCarryTheirParentsGenes(TestContext ctx) {
		check(ctx, Crops.BLOCKS.size() == 20, "20 crops, got " + Crops.BLOCKS.size());
		ServerWorld w = ctx.getWorld();
		// Planting seeds with genes gives a plant with those genes.
		int pure = genome("cc PP RR dd FF II");
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, Blocks.AIR);
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, Genetics.seedStack(mod("oats"), pure, 4));
		player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs.down()).add(0, 0.5, 0), Direction.UP, abs.down(), false)));
		check(ctx, w.getBlockState(abs).isOf(block("oats_crop")), "oats planted, got " + w.getBlockState(abs));
		check(ctx, CropGenomes.genome(Genetics.entry(w, abs, w.getBlockState(abs))) == pure, "the plant has the seeds' genes");

		// Ripe and broken alone: it pollinated itself, so its seeds are like it.
		w.setBlockState(abs, ((CropBlock) block("oats_crop")).withAge(7));
		w.breakBlock(abs, true);
		List<ItemEntity> seeds = dropped(ctx, abs, mod("oats"));
		int total = 0, same = 0;
		for (ItemEntity e : seeds) {
			total += e.getStack().getCount();
			int g = e.getStack().getOrDefault(Crops.GENOME, Genome.DEFAULT);
			if (g == pure) {
				same += e.getStack().getCount();
			}
			check(ctx, letters(g, pure) <= 2, "selfed pure oats gave a seed far from its parent");
			e.discard();
		}
		check(ctx, total >= 2 && same >= total / 2, "selfed pure oats give pure seeds: " + same + " of " + total);
		check(ctx, CropGenomes.of(w).get(abs) == null, "the broken plant is forgotten");

		// Ordinary wheat gives ordinary seeds that stack with any others.
		ctx.setBlockState(pos, Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 7));
		w.breakBlock(abs, true);
		// Each seed may mutate (1 in Genome.MUTATION per allele, about 2.4% of seeds), so a seed is only checked to be
		// ordinary or one or two letters away from it, and to carry no component when it is ordinary.
		for (ItemEntity e : dropped(ctx, abs, Items.WHEAT_SEEDS)) {
			Integer g = e.getStack().get(Crops.GENOME);
			check(ctx, g == null || g != Genome.DEFAULT, "ordinary wheat seeds carry no genome component");
			check(ctx, g == null || letters(g, Genome.DEFAULT) <= 2, "wheat seed far from ordinary: " + (g == null ? "-" : Genome.genotype(g)));
			e.discard();
		}
		dropped(ctx, abs, Items.WHEAT).forEach(ItemEntity::discard);
		dropped(ctx, abs, mod("oats")).forEach(ItemEntity::discard);

		// A cutting (cassava) is a copy of its parent.
		int cassava = genome("Cc Pp rr Dd ff Ii");
		ctx.setBlockState(pos, ((CropBlock) block("cassava_crop")).withAge(7));
		CropGenomes.of(w).put(abs, cassava);
		w.breakBlock(abs, true);
		for (ItemEntity e : dropped(ctx, abs, mod("cassava_stem"))) {
			check(ctx, e.getStack().getOrDefault(Crops.GENOME, -1) == cassava, "cassava stems are clones of the plant");
			e.discard();
		}
		dropped(ctx, abs, mod("cassava_root")).forEach(ItemEntity::discard);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void handPollinationChoosesTheFather(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos a = new BlockPos(1, 2, 1), b = new BlockPos(5, 2, 5);
		ctx.setBlockState(a.down(), Blocks.FARMLAND);
		ctx.setBlockState(b.down(), Blocks.FARMLAND);
		Block maize = block("maize_crop");
		ctx.setBlockState(a, ((CropBlock) maize).withAge(6));
		ctx.setBlockState(b, ((CropBlock) maize).withAge(6));
		BlockPos absA = ctx.getAbsolutePos(a), absB = ctx.getAbsolutePos(b);
		int father = genome("CC PP RR DD ff ii");
		int mother = genome("CC pp rr DD ff ii");
		CropGenomes.of(w).put(absA, father);
		CropGenomes.of(w).forget(absB);
		PlayerEntity player = ctx.createMockPlayer(GameMode.CREATIVE);
		ItemStack brush = new ItemStack(Crops.POLLINATION_BRUSH);
		player.setStackInHand(Hand.MAIN_HAND, brush);
		brush.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(absA), Direction.UP, absA, false)));
		brush.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(absB), Direction.UP, absB, false)));
		long entry = Genetics.entry(w, absB, w.getBlockState(absB));
		check(ctx, (entry & CropGenomes.HAND) != 0 && CropGenomes.father(entry) == father, "the brush made A the father of B's seeds");
		w.setBlockState(absB, ((CropBlock) maize).withAge(7));
		w.breakBlock(absB, true);
		int total = 0, hybrid = 0;
		for (ItemEntity e : dropped(ctx, absB, mod("maize_kernels"))) {
			int g = e.getStack().getOrDefault(Crops.GENOME, Genome.DEFAULT);
			total += e.getStack().getCount();
			if (Genome.capitals(g, Genome.YIELD) == 1 && Genome.capitals(g, Genome.DISEASE) == 1) {
				hybrid += e.getStack().getCount();
			}
			e.discard();
		}
		dropped(ctx, absB, mod("maize")).forEach(ItemEntity::discard);
		check(ctx, total >= 1 && hybrid >= total - 1, "pp x PP gives Pp Rr hybrids: " + hybrid + " of " + total);
		check(ctx, mother == Genome.DEFAULT, "the unstored plant is ordinary");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void genesClimateAndWaterSetTheGrowth(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pos = new BlockPos(2, 2, 2);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		CropInfo oats = Genetics.info(block("oats_crop"));
		CropInfo rice = Genetics.info(block("rice_crop"));
		CropInfo sorghum = Genetics.info(block("sorghum_crop"));
		biome(ctx, abs, 3, "plains");
		check(ctx, Genetics.multiplier(w, abs, oats, Genome.DEFAULT) == 1.0, "ordinary oats in the plains: full speed");
		check(ctx, Math.abs(Genetics.multiplier(w, abs, oats, genome("cc pp rr DD ff ii")) - Genetics.EARLY) < 1e-9,
				"early-cycle oats grow 43% faster");
		check(ctx, Genetics.multiplier(w, abs, rice, Genome.DEFAULT) == 0, "rice without water does not grow");
		ctx.setBlockState(pos.add(1, -1, 0), Blocks.WATER);
		check(ctx, Genetics.multiplier(w, abs, rice, Genome.DEFAULT) == 1.0, "rice with water beside its farmland grows");
		biome(ctx, abs, 3, "snowy_plains");
		check(ctx, Genetics.multiplier(w, abs, rice, Genome.DEFAULT) == Genetics.PENALTY, "rice in the snow grows at half speed");
		check(ctx, Genetics.multiplier(w, abs, rice, genome("CC pp rr DD Ff ii")) == 1.0, "cold-tolerant rice grows at full speed in the snow");
		biome(ctx, abs, 3, "desert");
		ctx.setBlockState(pos.add(1, -1, 0), Blocks.DIRT);
		CropInfo wheat = Genetics.info(Blocks.WHEAT);
		check(ctx, Genetics.multiplier(w, abs, wheat, Genome.DEFAULT) == Genetics.PENALTY, "wheat in the desert: half speed");
		check(ctx, Genetics.multiplier(w, abs, wheat, genome("CC pp rr dd ff ii")) == 1.0, "drought-tolerant (dd) wheat: full speed");
		check(ctx, Genetics.multiplier(w, abs, sorghum, Genome.DEFAULT) == 1.0, "sorghum is a crop of dry country");
		check(ctx, Genetics.multiplier(w, abs, sorghum, Genome.DEFAULT | CropGenomes.PEST) == 0, "a plant with caterpillars does not grow");
		biome(ctx, abs, 3, "plains");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pestsResistanceAndRemedies(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		Block barley = block("barley_crop");
		field(ctx, barley, 5, 3);
		BlockPos centre = ctx.getAbsolutePos(new BlockPos(3, 2, 3));
		check(ctx, Genetics.monoculture(w, centre, barley), "25 barley plants are a monoculture");
		Random random = Random.create(7);
		int resistant = genome("CC pp rr DD ff II");
		int caught = 0, caughtPlain = 0;
		for (int i = 0; i < 400; i++) {
			CropGenomes.of(w).put(centre, resistant);
			if (Genetics.infect(w, centre, resistant, true, random)) {
				caught++;
			}
			CropGenomes.of(w).forget(centre);
			if (Genetics.infect(w, centre, Genome.DEFAULT, true, random)) {
				caughtPlain++;
			}
			CropGenomes.of(w).forget(centre);
		}
		check(ctx, caughtPlain == 400 && caught > 60 && caught < 150, "resistant plants catch caterpillars 4x less: " + caught + " vs " + caughtPlain);
		// Bordeaux mixture keeps rust off.
		Sprayed.of(w).spray(centre);
		check(ctx, !Genetics.infect(w, centre, Genome.DEFAULT, false, random), "a plant sprayed with Bordeaux mixture does not rust");
		Sprayed.of(w).forget(centre);
		// An infested plant does not grow, and passes it on.
		Genetics.infect(w, centre, Genome.DEFAULT, true, random);
		BlockState before = w.getBlockState(centre);
		for (int i = 0; i < 200; i++) {
			w.getBlockState(centre).randomTick(w, centre, random);
		}
		check(ctx, w.getBlockState(centre).equals(before), "a plant with caterpillars does not grow");
		int infested = 0;
		for (BlockPos p : BlockPos.iterate(centre.add(-1, 0, -1), centre.add(1, 0, 1))) {
			if (Genetics.infested(Genetics.entry(w, p, w.getBlockState(p)))) {
				infested++;
			}
		}
		check(ctx, infested > 1, "caterpillars spread to neighbours: " + infested);
		// Tobacco wash clears them around the plant.
		PlayerEntity player = ctx.createMockPlayer(GameMode.CREATIVE);
		ItemStack wash = new ItemStack(Crops.TOBACCO_WASH);
		player.setStackInHand(Hand.MAIN_HAND, wash);
		wash.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(centre), Direction.UP, centre, false)));
		for (BlockPos p : BlockPos.iterate(centre.add(-1, 0, -1), centre.add(1, 0, 1))) {
			check(ctx, !Genetics.infested(Genetics.entry(w, p, w.getBlockState(p))), "tobacco wash cleared " + p);
		}
		check(ctx, wash.getDamage() == 1, "the wash wears");
		// The game rule turns outbreaks off.
		w.getGameRules().get(Genetics.PESTS).set(false, w.getServer());
		check(ctx, !Genetics.pestsOn(w), "pests can be turned off");
		w.getGameRules().get(Genetics.PESTS).set(true, w.getServer());
		field(ctx, barley, 5, 0);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void seedLensReadsTheGenes(TestContext ctx) {
		PlayerEntity player = ctx.createMockPlayer(GameMode.CREATIVE);
		int g = genome("cc Pp rr DD ff Ii");
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Crops.SEED_LENS));
		player.setStackInHand(Hand.OFF_HAND, Genetics.seedStack(mod("soybeans"), g, 5));
		check(ctx, SeedLensItem.level(player) == Genome.STAMP_DNA, "a creative player reads every trait");
		player.getMainHandStack().use(ctx.getWorld(), player, Hand.MAIN_HAND);
		int stamped = player.getOffHandStack().getOrDefault(Crops.GENOME, 0);
		check(ctx, Genome.stamp(stamped) == Genome.STAMP_DNA && (stamped & Genome.MASK) == g, "the lens marks the seeds with their genotype");
		// On a plant, it describes it.
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, ((CropBlock) block("soy_crop")).withAge(7));
		BlockPos abs = ctx.getAbsolutePos(pos);
		CropGenomes.of(ctx.getWorld()).put(abs, g);
		var lines = SeedLensItem.describe(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs), Genetics.info(block("soy_crop")), 2);
		check(ctx, lines.stream().anyMatch(t -> t.getString().contains("Cc") || t.getString().contains("cc Pp")), "the lens shows the genotype: " + lines);
		CropGenomes.of(ctx.getWorld()).forget(abs);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void wildCropsAndPerennials(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		BlockState wild = ((CropBlock) block("maize_crop")).withAge(7).with(GeneticCropBlock.WILD, true);
		check(ctx, wild.canPlaceAt(w, abs), "wild maize stands on grass");
		check(ctx, !((CropBlock) block("maize_crop")).withAge(7).canPlaceAt(w, abs), "planted maize needs farmland");
		check(ctx, !wild.hasRandomTicks(), "a wild plant does not grow");
		ctx.setBlockState(pos, wild);
		w.breakBlock(abs, true);
		int kernels = dropped(ctx, abs, mod("maize_kernels")).stream().mapToInt(e -> e.getStack().getCount()).sum();
		check(ctx, kernels >= 1, "wild maize gives kernels");
		dropped(ctx, abs, mod("maize_kernels")).forEach(ItemEntity::discard);
		dropped(ctx, abs, mod("maize")).forEach(ItemEntity::discard);

		// Coffee is picked and stays.
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, ((CropBlock) block("coffee_crop")).withAge(7));
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		w.getBlockState(abs).onUse(w, player, new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, w.getBlockState(abs).isOf(block("coffee_crop")) && w.getBlockState(abs).get(CropBlock.AGE) == 4,
				"picked coffee goes back to age 4, got " + w.getBlockState(abs));
		check(ctx, !dropped(ctx, abs, mod("coffee_beans")).isEmpty(), "coffee gives beans");
		dropped(ctx, abs, mod("coffee_beans")).forEach(ItemEntity::discard);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void feedingTroughBreedsAndRaises(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Crops.TROUGH);
		BlockPos abs = ctx.getAbsolutePos(pos);
		FeedingTroughBlockEntity trough = (FeedingTroughBlockEntity) w.getBlockEntity(abs);
		ItemStack wheat = new ItemStack(Items.WHEAT, 10);
		check(ctx, trough.insert(wheat) && wheat.isEmpty(), "the trough takes the wheat");
		check(ctx, !trough.insert(new ItemStack(Items.CARROT)), "one kind of feed at a time");
		CowEntity a = ctx.spawnEntity(EntityType.COW, new BlockPos(3, 2, 3));
		CowEntity b = ctx.spawnEntity(EntityType.COW, new BlockPos(4, 2, 3));
		CowEntity calf = ctx.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4));
		calf.setBaby(true);
		for (int i = 0; i < 4; i++) {
			trough.feedOne(w, abs);
		}
		check(ctx, a.isInLove() && b.isInLove(), "both cows fed and ready to breed");
		check(ctx, trough.feed().getCount() <= 8, "the trough used its wheat: " + trough.feed().getCount());
		check(ctx, mod("alfalfa").getDefaultStack().isIn(net.minecraft.registry.tag.ItemTags.COW_FOOD), "cows eat alfalfa");
		a.discard();
		b.discard();
		calf.discard();
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void legumesAndResearch(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		for (String r : new String[] {"domestication", "animal_husbandry", "crop_rotation", "natural_dyes", "plant_hybridization",
				"tobacco_wash", "mendels_laws"}) {
			check(ctx, Research.get(r) != null, "research " + r);
		}
		check(ctx, Research.get("domestication").level() == 1 && Research.get("mendels_laws").level() == 4, "research levels");
		check(ctx, Research.unlockedBy(SeedLensItem.UNLOCK) != null && Research.unlockedBy(PollinationBrushItem.UNLOCK) != null, "tools unlocked by research");
		// A ripe bean broken by a player who knows crop rotation leaves the farmland fertilised.
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, ((CropBlock) block("bean_crop")).withAge(7));
		var player = ctx.createMockCreativeServerPlayerInWorld();
		player.interactionManager.tryBreakBlock(abs);
		check(ctx, Fertilizer.of(w).harvestsLeft(abs.down()) > 0, "beans leave nitrogen in the soil");
		check(ctx, Fertilizer.cropOf(((CropBlock) block("bean_crop")).withAge(7)) == mod("beans"), "fertiliser bonus gives beans");
		CropGenomes copy = CropGenomes.of(w);
		copy.put(abs, 123L | CropGenomes.PEST);
		check(ctx, copy.copy(w.getRegistryManager()).get(abs) == (123L | CropGenomes.PEST), "crop genes are saved");
		copy.forget(abs);
		ctx.complete();
	}
}
