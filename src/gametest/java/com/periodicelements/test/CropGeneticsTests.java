package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.crops.CropGenomes;
import com.periodicelements.crops.CropInfo;
import com.periodicelements.crops.CropMachineBlockEntity;
import com.periodicelements.crops.Crops;
import com.periodicelements.crops.CrossingNotebookItem;
import com.periodicelements.crops.DnaSequencerBlock;
import com.periodicelements.crops.FeedingTroughBlockEntity;
import com.periodicelements.crops.Genetics;
import com.periodicelements.crops.Genome;
import com.periodicelements.farm.Fertilizer;
import com.periodicelements.research.Research;
import com.periodicelements.shrubs.Greenhouse;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;

/** 3.37.0 crop genetics stages 2 and 3: hybrids, the stature gene, transgenes, the crop machines and tools. */
public class CropGeneticsTests implements FabricGameTest {
	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
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

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
		Greenhouse.forget();
	}

	private static BlockHitResult hit(BlockPos abs) {
		return new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void hybridsAndTheStatureGene(TestContext ctx) {
		Random random = Random.create(7);
		int a = genome("cc PP rr DD ff ii"), b = Genome.DEFAULT;
		check(ctx, Genome.pure(a) && Genome.pure(b), "both are pure lines");
		int f1 = Genome.child(a, b, random, 0);
		check(ctx, Genome.hybrid(f1) && !Genome.pure(f1), "two different pure lines give an F1 hybrid");
		check(ctx, !Genome.hybrid(Genome.child(a, a, random, 0)), "a pure line selfed is no hybrid");
		check(ctx, !Genome.hybrid(Genome.child(f1, f1, random, 0)), "the hybrid's own seeds lose it");
		// Seeds saved before 3.37.0 read as tall (SS); ss shows the semi-dwarf trait.
		check(ctx, Genome.capitals(Genome.DEFAULT, Genome.STATURE) == 2 && !Genome.has(Genome.DEFAULT, Genome.STATURE), "old seeds are SS");
		int dwarf = Genome.useful(Genome.DEFAULT, Genome.STATURE);
		check(ctx, Genome.has(dwarf, Genome.STATURE) && Genome.genotype(dwarf, Genome.STAMP_SEQUENCE, true).endsWith("ss"), "ss is semi-dwarf");
		check(ctx, Genome.genotype(dwarf, Genome.STAMP_GENOTYPE, true).equals("CC pp"), "Mendel shows only C and P");

		ServerWorld w = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		biome(ctx, abs, 3, "plains");
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, ((CropBlock) Blocks.WHEAT).withAge(2));
		CropInfo wheat = Genetics.info(Blocks.WHEAT);
		check(ctx, wheat.stature(), "wheat has the stature gene");
		double plain = Genetics.multiplier(w, abs, wheat, CropGenomes.of(Genome.DEFAULT, 0));
		check(ctx, Math.abs(Genetics.multiplier(w, abs, wheat, CropGenomes.of(Genome.HYBRID | Genome.DEFAULT, 0)) - plain * Genetics.HYBRID_VIGOR) < 1e-9, "hybrid vigour");
		check(ctx, Genetics.multiplier(w, abs, wheat, CropGenomes.of(dwarf, 0)) == plain, "semi-dwarf without NPK grows as usual");
		Fertilizer.of(w).fertilize(abs.down(), true);
		check(ctx, Math.abs(Genetics.multiplier(w, abs, wheat, CropGenomes.of(dwarf, 0)) - plain * Genetics.DWARF_NPK) < 1e-9, "semi-dwarf on NPK grows half again as fast");
		check(ctx, Genetics.multiplier(w, abs, wheat, CropGenomes.of(Genome.DEFAULT, 0)) == plain, "tall wheat gets no NPK bonus");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void transgenes(TestContext ctx) {
		Random random = Random.create(3);
		int bt = Genome.withTransgene(Genome.DEFAULT, Genome.BT);
		for (int i = 0; i < 20; i++) {
			check(ctx, Genome.transgene(Genome.child(Genome.DEFAULT, bt, random), Genome.BT), "a transgene passes from the father");
		}
		ServerWorld w = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, ((CropBlock) block("maize_crop")).withAge(3));
		for (int i = 0; i < 50; i++) {
			check(ctx, !Genetics.infect(w, abs, CropGenomes.of(bt, 0), true, random), "Bt maize never takes caterpillars");
		}
		// EPSPS: Field Clear spares it.
		CropGenomes.of(w).put(abs, CropGenomes.of(Genome.withTransgene(Genome.DEFAULT, Genome.EPSPS), 0));
		check(ctx, Crops.glyphosateTolerant(w, abs), "EPSPS maize is glyphosate tolerant");
		CropGenomes.of(w).forget(abs);
		check(ctx, !Crops.glyphosateTolerant(w, abs), "plain maize is not");
		// Golden rice grains can be eaten.
		ItemStack rice = Genetics.seedStack(mod("rice"), Genome.withTransgene(Genome.DEFAULT, Genome.GOLDEN), 4);
		check(ctx, rice.contains(DataComponentTypes.FOOD), "golden rice is food");
		check(ctx, !new ItemStack(mod("rice")).contains(DataComponentTypes.FOOD), "plain rice is not");
		// DREB: full speed in a dry land without water.
		biome(ctx, abs, 3, "desert");
		CropInfo maize = Genetics.info(block("maize_crop"));
		double plain = Genetics.multiplier(w, abs, maize, CropGenomes.of(Genome.DEFAULT, 0));
		double dreb = Genetics.multiplier(w, abs, maize, CropGenomes.of(Genome.withTransgene(Genome.DEFAULT, Genome.DREB), 0));
		check(ctx, Math.abs(dreb - plain / Genetics.PENALTY) < 1e-9, "DREB maize ignores drought: " + plain + " vs " + dreb);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void siloIrradiatorAndThermocycler(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(pos, Crops.SILO);
		CropMachineBlockEntity silo = (CropMachineBlockEntity) w.getBlockEntity(ctx.getAbsolutePos(pos));
		check(ctx, !silo.insert(new ItemStack(Items.WHEAT_SEEDS, 4)), "the silo takes no seeds");
		ItemStack maize = new ItemStack(mod("maize"), 3);
		check(ctx, silo.insert(maize) && maize.isEmpty(), "the silo takes maize");
		for (int i = 0; i < 3; i++) {
			silo.ensileOne();
		}
		check(ctx, silo.getStack(0).isEmpty() && silo.getStack(1).isOf(mod("silage")) && silo.getStack(1).getCount() == 3, "3 maize make 3 silage");
		check(ctx, silo.canExtract(1, silo.getStack(1), Direction.DOWN) && !silo.canExtract(0, ItemStack.EMPTY, Direction.DOWN), "hoppers take silage, not maize");
		check(ctx, !CropMachineBlockEntity.power(w, ctx.getAbsolutePos(pos), 10), "no power without a source");

		ctx.setBlockState(pos, Crops.SEED_IRRADIATOR);
		CropMachineBlockEntity xray = (CropMachineBlockEntity) w.getBlockEntity(ctx.getAbsolutePos(pos));
		xray.insert(new ItemStack(Items.WHEAT_SEEDS, 64));
		Random random = Random.create(11);
		int survivors = 0, mutants = 0;
		for (int i = 0; i < 64; i++) {
			xray.irradiateOne(random);
			// a hopper below takes each seed out
			for (int slot = 1; slot < CropMachineBlockEntity.SIZE; slot++) {
				ItemStack s = xray.removeStack(slot);
				survivors += s.getCount();
				if (!s.isEmpty() && (s.getOrDefault(Crops.GENOME, Genome.DEFAULT) & Genome.MASK) != Genome.DEFAULT) {
					mutants += s.getCount();
				}
			}
		}
		check(ctx, xray.getStack(0).isEmpty(), "every seed went through");
		check(ctx, survivors > 32 && survivors < 58, "about 7 in 10 survive: " + survivors);
		check(ctx, mutants > survivors / 5, "X-rays mutate many: " + mutants + " of " + survivors);
		xray.clear();

		ctx.setBlockState(pos, Crops.THERMOCYCLER);
		CropMachineBlockEntity pcr = (CropMachineBlockEntity) w.getBlockEntity(ctx.getAbsolutePos(pos));
		int marker = genome("CC PP rr DD ff ii");
		pcr.setMarker(marker);
		check(ctx, CropMachineBlockEntity.matches(genome("cc Pp Rr DD ff ii"), marker), "a seed with the marker's yield matches");
		check(ctx, !CropMachineBlockEntity.matches(Genome.DEFAULT, marker), "a plain seed does not");
		pcr.insert(Genetics.seedStack(Items.WHEAT_SEEDS, genome("CC Pp rr DD ff ii"), 3));
		for (int i = 0; i < 3; i++) {
			pcr.sortOne();
		}
		pcr.insert(new ItemStack(Items.WHEAT_SEEDS, 2));
		for (int i = 0; i < 2; i++) {
			pcr.sortOne();
		}
		int below = 0, sides = 0;
		for (int slot : pcr.getAvailableSlots(Direction.DOWN)) {
			below += pcr.getStack(slot).getCount();
		}
		for (int slot = 5; slot < CropMachineBlockEntity.SIZE; slot++) {
			sides += pcr.getStack(slot).getCount();
		}
		check(ctx, below == 3 && sides == 2, "matching seeds go below, the rest to the sides: " + below + "/" + sides);
		pcr.clear();
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void notebookSequencerAndGamma(TestContext ctx) {
		int cc = genome("Cc Pp rr DD ff ii");
		check(ctx, Math.abs(CrossingNotebookItem.share(cc, cc, Genome.CYCLE) - 0.25) < 1e-9, "Cc x Cc: a quarter are cc");
		check(ctx, Math.abs(CrossingNotebookItem.share(cc, Genome.DEFAULT, Genome.YIELD) - 0.5) < 1e-9, "Pp x pp: half show P");
		check(ctx, Math.abs(CrossingNotebookItem.share(cc, cc, Genome.YIELD) - 0.75) < 1e-9, "Pp x Pp: three quarters show P");
		ItemStack seeds = Genetics.seedStack(Items.WHEAT_SEEDS, cc, 4);
		DnaSequencerBlock.sequence(seeds);
		check(ctx, Genome.stamp(seeds.get(Crops.GENOME)) == Genome.STAMP_SEQUENCE && (seeds.get(Crops.GENOME) & Genome.MASK) == cc, "the sequencer reads the whole genome");

		ServerWorld w = ctx.getWorld();
		BlockPos crop = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		check(ctx, !Genetics.nearGamma(w, crop), "no gamma garden yet");
		ctx.setBlockState(new BlockPos(5, 2, 1), Crops.GAMMA_GARDEN);
		check(ctx, Genetics.nearGamma(w, crop), "a gamma garden 4 blocks away irradiates the crop");
		ctx.setBlockState(new BlockPos(5, 2, 1), Blocks.AIR);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void geneGunCrisprAndInoculant(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		PlayerEntity player = ctx.createMockPlayer(GameMode.CREATIVE);
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.FARMLAND);
		ctx.setBlockState(pos, ((CropBlock) Blocks.WHEAT).withAge(3));
		ItemStack gun = new ItemStack(Crops.GENE_GUN);
		player.setStackInHand(Hand.MAIN_HAND, gun);
		player.setStackInHand(Hand.OFF_HAND, new ItemStack(mod("bt_gene"), 16));
		for (int i = 0; i < 20 && !Genome.transgene(CropGenomes.genome(Genetics.entry(w, abs, w.getBlockState(abs))), Genome.BT); i++) {
			gun.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit(abs)));
		}
		check(ctx, Genome.transgene(CropGenomes.genome(Genetics.entry(w, abs, w.getBlockState(abs))), Genome.BT), "the gene gun put Bt into the wheat");
		player.setStackInHand(Hand.OFF_HAND, new ItemStack(mod("golden_gene")));
		gun.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit(abs)));
		check(ctx, !Genome.transgene(CropGenomes.genome(Genetics.entry(w, abs, w.getBlockState(abs))), Genome.GOLDEN), "golden rice gene is for rice only");

		ItemStack kit = new ItemStack(Crops.CRISPR_KIT);
		kit.set(Crops.TARGET_GENE, Genome.STATURE);
		player.setStackInHand(Hand.MAIN_HAND, kit);
		kit.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit(abs)));
		int g = CropGenomes.genome(Genetics.entry(w, abs, w.getBlockState(abs)));
		check(ctx, Genome.has(g, Genome.STATURE) && Genome.transgene(g, Genome.BT), "CRISPR made the wheat semi-dwarf and kept its Bt");
		CropGenomes.of(w).forget(abs);

		// Inoculated beans harvested ripe leave NPK-strength nitrogen.
		ctx.setBlockState(pos, ((CropBlock) block("bean_crop")).withAge(7));
		ItemStack inoculant = new ItemStack(mod("inoculant"));
		player.setStackInHand(Hand.MAIN_HAND, inoculant);
		inoculant.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit(abs)));
		check(ctx, (Genetics.entry(w, abs, w.getBlockState(abs)) & CropGenomes.INOCULATED) != 0, "the beans are inoculated");
		var breaker = ctx.createMockCreativeServerPlayerInWorld();
		breaker.interactionManager.tryBreakBlock(abs);
		check(ctx, Fertilizer.of(w).strong(abs.down()), "inoculated beans leave the farmland strongly fertilized");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void silageAndBalancedFeed(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		check(ctx, mod("silage").getDefaultStack().isIn(ItemTags.COW_FOOD), "cows eat silage");
		check(ctx, mod("balanced_feed").getDefaultStack().isIn(ItemTags.PIG_FOOD)
				&& mod("balanced_feed").getDefaultStack().isIn(ItemTags.CHICKEN_FOOD), "pigs and chickens eat balanced feed");
		BlockPos pos = new BlockPos(1, 1, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos, Crops.TROUGH);
		FeedingTroughBlockEntity trough = (FeedingTroughBlockEntity) w.getBlockEntity(abs);
		trough.insert(new ItemStack(mod("silage"), 1));
		CowEntity a = ctx.spawnEntity(EntityType.COW, new BlockPos(3, 2, 3));
		CowEntity b = ctx.spawnEntity(EntityType.COW, new BlockPos(4, 2, 3));
		trough.feedOne(w, abs);
		check(ctx, trough.feed().getCount() == 1, "one silage feeds twice: still there after one serving");
		trough.feedOne(w, abs);
		check(ctx, trough.feed().isEmpty() && a.isInLove() && b.isInLove(), "two cows fed from one silage");
		a.discard();
		b.discard();

		trough.insert(new ItemStack(mod("balanced_feed"), 4));
		CowEntity mother = ctx.spawnEntity(EntityType.COW, new BlockPos(3, 2, 3));
		mother.setBreedingAge(6000);
		trough.feedOne(w, abs);
		check(ctx, mother.getBreedingAge() == 3000, "balanced feed halves the wait to breed again: " + mother.getBreedingAge());
		mother.discard();
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void newResearch(TestContext ctx) {
		String[][] projects = {{"silage", "4"}, {"nitrogen_fixation", "4"}, {"hybrid_maize", "5"}, {"balanced_feed", "5"},
				{"x_ray_mutagenesis", "5"}, {"dna_double_helix", "6"}, {"gamma_garden", "6"}, {"dna_sequencing", "7"},
				{"pcr", "7"}, {"transgenesis", "7"}, {"crispr", "7"}};
		for (String[] p : projects) {
			check(ctx, Research.get(p[0]) != null && Research.get(p[0]).level() == Integer.parseInt(p[1]), "research " + p[0]);
		}
		for (String u : new String[] {"item:crossing_notebook", "item:silo", "item:inoculant", "crop:hybrid", "item:seed_irradiator",
				"crop:dna", "item:gamma_garden", "item:dna_sequencer", "item:thermocycler", "item:gene_gun", "item:crispr_kit"}) {
			check(ctx, Research.unlockedBy(u) != null, "something unlocks " + u);
		}
		ctx.complete();
	}
}
