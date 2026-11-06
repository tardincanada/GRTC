package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.Farm;
import com.periodicelements.farm.TreeTapBlock;
import com.periodicelements.farm.TreeTapBlockEntity;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import com.periodicelements.trees.BiomeSaplingBlock;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 2.9.0 temperate trees: rocky-coast saplings, maple sap to sugar, cork and willow bark, salicin. */
public class TemperateTreeTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

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

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
	}

	private static MachineBlockEntity place(TestContext ctx, String machine, String... projects) {
		ctx.setBlockState(POS, Blocks.AIR);
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, new ArrayList<>(List.of(projects)));
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 64));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		return be;
	}

	private static int run(TestContext ctx, MachineBlockEntity be, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, outputs {} {} {} residue {} | buffers {}", be.spec().id(), t,
				be.getStack(2), be.getStack(3), be.getStack(4), be.getStack(5), be.outputBuffers());
		return t;
	}

	private static int count(MachineBlockEntity be, Item item) {
		int n = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			if (be.getStack(i).isOf(item)) {
				n += be.getStack(i).getCount();
			}
		}
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void rockyCoastTreesRootInStone(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.STONE);
		check(ctx, block("stone_pine_sapling").getDefaultState().canPlaceAt(world, abs), "a stone pine should root in stone");
		check(ctx, !block("date_palm_sapling").getDefaultState().canPlaceAt(world, abs), "a date palm should not root in stone");
		check(ctx, !block("beech_sapling").getDefaultState().canPlaceAt(world, abs), "a beech should not root in stone");
		ctx.setBlockState(pos.down(), Blocks.GRAVEL);
		check(ctx, block("aleppo_pine_sapling").getDefaultState().canPlaceAt(world, abs), "an Aleppo pine should root in gravel");
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(pos, block("stone_pine_sapling"));
		BiomeSaplingBlock sapling = (BiomeSaplingBlock) block("stone_pine_sapling");
		biome(ctx, abs, 2, "forest");
		check(ctx, !sapling.isFertilizable(world, abs, world.getBlockState(abs)), "no stone pine in the forest");
		biome(ctx, abs, 2, "stony_shore");
		check(ctx, sapling.isFertilizable(world, abs, world.getBlockState(abs)), "stone pines grow on the stony shore");
		BiomeSaplingBlock beech = (BiomeSaplingBlock) block("beech_sapling");
		ctx.setBlockState(pos, beech);
		for (String b : List.of("forest", "birch_forest", "old_growth_birch_forest", "dark_forest")) {
			biome(ctx, abs, 2, b);
			check(ctx, beech.isFertilizable(world, abs, world.getBlockState(abs)), "beech grows in " + b);
		}
		biome(ctx, abs, 2, "taiga");
		check(ctx, !beech.isFertilizable(world, abs, world.getBlockState(abs)), "no beech in the taiga");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void mapleSapBoilsDownToSugar(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos log = new BlockPos(4, 1, 1);
		BlockPos tapPos = log.south();
		ctx.setBlockState(log, block("sugar_maple_log"));
		ctx.setBlockState(tapPos, Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH));
		BlockPos abs = ctx.getAbsolutePos(tapPos);
		TreeTapBlockEntity tap = ctx.getBlockEntity(tapPos);
		biome(ctx, abs, 2, "forest");
		// Maple sap comes once a minute (1200 ticks), not every five minutes.
		for (int t = 0; t < 1200 * 2; t++) {
			tap.tick(world, abs, world.getBlockState(abs));
		}
		check(ctx, tap.latex() == 2, "2 flasks of maple sap in the forest, got " + tap.latex());
		tap.setStack(TreeTapBlockEntity.IN, new ItemStack(Fluids.GLASS_FLASK, 4));
		tap.tick(world, abs, world.getBlockState(abs));
		tap.tick(world, abs, world.getBlockState(abs));
		check(ctx, tap.getStack(TreeTapBlockEntity.OUT).isOf(mod("maple_sap")) && tap.getStack(TreeTapBlockEntity.OUT).getCount() == 2,
				"2 maple sap out, got " + tap.getStack(TreeTapBlockEntity.OUT));

		// Real maple sap is about 3% sugar: 40 flasks boil down to one sugar in the Salt Pan.
		MachineBlockEntity be = place(ctx, "salt_pan", "salt_evaporation");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("maple_sap"), 40));
		run(ctx, be, 400_000);
		check(ctx, count(be, mod("sucrose")) == 1, "1 sugar from 40 flasks of maple sap, got " + count(be, mod("sucrose")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void corkAndWillowBarkComeOffWithAnAxe(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
		for (String[] t : new String[][]{{"cork_oak", "cork"}, {"white_willow", "willow_bark"}}) {
			ctx.setBlockState(pos, block(t[0] + "_log"));
			ActionResult r = UseBlockCallback.EVENT.invoker().interact(player, ctx.getWorld(), Hand.MAIN_HAND,
					new BlockHitResult(Vec3d.ofCenter(abs), Direction.NORTH, abs, false));
			check(ctx, r.isAccepted(), t[0] + ": the axe should strip the log, got " + r);
			check(ctx, ctx.getWorld().getBlockState(abs).isOf(block("stripped_" + t[0] + "_log")), t[0] + " should be stripped");
			List<ItemEntity> drops = ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(abs).expand(2),
					e -> e.getStack().isOf(mod(t[1])));
			check(ctx, drops.size() == 1, "one " + t[1] + " should drop, got " + drops.size());
			drops.forEach(ItemEntity::discard);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void willowBarkGivesSalicin(TestContext ctx) {
		// 32 willow bark = 3556 units at 7% salicin = 249 units: 2 salicin.
		MachineBlockEntity be = place(ctx, "digestion_vat", "acid_digestion");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("willow_bark"), 32));
		run(ctx, be, 200_000);
		check(ctx, count(be, mod("salicin")) == 2, "2 salicin from 32 willow bark, got " + count(be, mod("salicin")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void applesComeFromAppleTreesNotOaks(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos abs = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		int oakApples = 0, treeApples = 0;
		for (int i = 0; i < 3000; i++) {
			for (ItemStack st : Block.getDroppedStacks(Blocks.OAK_LEAVES.getDefaultState(), world, abs, null)) {
				oakApples += st.isOf(Items.APPLE) ? st.getCount() : 0;
			}
			for (ItemStack st : Block.getDroppedStacks(Blocks.DARK_OAK_LEAVES.getDefaultState(), world, abs, null)) {
				oakApples += st.isOf(Items.APPLE) ? st.getCount() : 0;
			}
			for (ItemStack st : Block.getDroppedStacks(block("apple_leaves").getDefaultState(), world, abs, null)) {
				treeApples += st.isOf(Items.APPLE) ? st.getCount() : 0;
			}
		}
		check(ctx, oakApples == 0, "oak leaves should drop no apples, got " + oakApples);
		check(ctx, treeApples > 50, "apple tree leaves should drop apples (5%), got " + treeApples + " in 3000");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void noVanillaOaksInTheTropicsOrTheBadlands(TestContext ctx) {
		var reg = ctx.getWorld().getRegistryManager();
		var biomes = reg.get(net.minecraft.registry.RegistryKeys.BIOME);
		var wooded = biomes.get(net.minecraft.world.biome.BiomeKeys.WOODED_BADLANDS);
		boolean badlandsOak = wooded.getGenerationSettings().getFeatures().stream().flatMap(l -> l.stream())
				.anyMatch(e -> e.matchesId(Identifier.ofVanilla("trees_badlands")));
		check(ctx, !badlandsOak, "the wooded badlands should have no vanilla oaks");
		var features = reg.get(net.minecraft.registry.RegistryKeys.CONFIGURED_FEATURE);
		for (String mix : List.of("trees_jungle", "trees_sparse_jungle", "bamboo_vegetation", "trees_savanna")) {
			var cfg = features.get(Identifier.ofVanilla(mix)).config();
			String text = cfg.toString();
			if (cfg instanceof net.minecraft.world.gen.feature.RandomFeatureConfig r) {
				StringBuilder sb = new StringBuilder();
				r.features.forEach(f -> sb.append(f.feature.getKey().map(k -> k.getValue().toString()).orElse("?")).append(' '));
				sb.append(r.defaultFeature.getKey().map(k -> k.getValue().toString()).orElse("?"));
				text = sb.toString();
			}
			check(ctx, !text.contains("oak"), mix + " should hold no oak: " + text);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void treeLineTreesRootInSnowAndGrowOnThePeaks(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.SNOW_BLOCK);
		check(ctx, block("siberian_dwarf_pine_sapling").getDefaultState().canPlaceAt(world, abs), "a dwarf pine should root in snow");
		check(ctx, !block("scots_pine_sapling").getDefaultState().canPlaceAt(world, abs), "a Scots pine should not root in snow");
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		BiomeSaplingBlock pine = (BiomeSaplingBlock) block("siberian_dwarf_pine_sapling");
		ctx.setBlockState(pos, pine);
		biome(ctx, abs, 2, "frozen_peaks");
		check(ctx, pine.isFertilizable(world, abs, world.getBlockState(abs)), "dwarf pines grow on the frozen peaks");
		BiomeSaplingBlock redwood = (BiomeSaplingBlock) block("coast_redwood_sapling");
		ctx.setBlockState(pos, redwood);
		check(ctx, !redwood.isFertilizable(world, abs, world.getBlockState(abs)), "no redwood on the frozen peaks");
		biome(ctx, abs, 2, "old_growth_spruce_taiga");
		check(ctx, redwood.isFertilizable(world, abs, world.getBlockState(abs)), "redwoods grow in the old growth spruce taiga");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void balsamFirGivesCanadaBalsam(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos log = new BlockPos(4, 1, 1);
		BlockPos tapPos = log.south();
		ctx.setBlockState(log, block("balsam_fir_log"));
		ctx.setBlockState(tapPos, Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH));
		BlockPos abs = ctx.getAbsolutePos(tapPos);
		TreeTapBlockEntity tap = ctx.getBlockEntity(tapPos);
		biome(ctx, abs, 2, "taiga");
		tap.setStack(TreeTapBlockEntity.IN, new ItemStack(Fluids.GLASS_FLASK, 4));
		for (int t = 0; t < Farm.tapTicks() * 2 + 2; t++) {
			tap.tick(world, abs, world.getBlockState(abs));
		}
		check(ctx, tap.getStack(TreeTapBlockEntity.OUT).isOf(mod("canada_balsam")) && tap.getStack(TreeTapBlockEntity.OUT).getCount() == 2,
				"2 Canada balsam out, got " + tap.getStack(TreeTapBlockEntity.OUT));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void lemonsGiveCitricAcid(TestContext ctx) {
		// 41 lemons = 4556 units at 5% citric acid = 228 units: 2 citric acid.
		MachineBlockEntity be = place(ctx, "digestion_vat", "acid_digestion");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("lemon"), 41));
		run(ctx, be, 200_000);
		check(ctx, count(be, mod("citric_acid")) == 2, "2 citric acid from 41 lemons, got " + count(be, mod("citric_acid")));
		ServerWorld world = ctx.getWorld();
		BlockPos abs = ctx.getAbsolutePos(new BlockPos(3, 2, 3));
		int mangoes = 0;
		for (int i = 0; i < 2000; i++) {
			for (ItemStack st : Block.getDroppedStacks(block("mango_leaves").getDefaultState(), world, abs, null)) {
				mangoes += st.isOf(mod("mango")) ? st.getCount() : 0;
			}
		}
		check(ctx, mangoes > 40, "mango leaves should drop mangoes, got " + mangoes + " in 2000");
		ctx.complete();
	}
}
