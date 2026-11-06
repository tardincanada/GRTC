package com.periodicelements.test;

import com.periodicelements.BlockComposition;
import com.periodicelements.ModContent;
import com.periodicelements.compendium.Compendium;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.compendium.DiscoveryState;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.worldgen.Geology;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 3.15.0 Mineral Compendium. Mock players share one UUID, so each test has its own batch and starts with no notes. */
public class MineralCompendiumTests implements FabricGameTest {
	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static ServerPlayerEntity player(TestContext ctx) {
		// Clear the air round the player: blocks left by earlier tests in the same spot can block the line of sight.
		for (int x = 0; x <= 5; x++) for (int y = 2; y <= 6; y++) for (int z = 0; z <= 5; z++) ctx.setBlockState(new BlockPos(x, y, z), net.minecraft.block.Blocks.AIR);
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		MinecraftServer server = ctx.getWorld().getServer();
		ResearchState.get(server).revoke(server, player.getUuid(), Research.all().stream().map(Research.Project::id).toList());
		DiscoveryState.get(server).clear(player.getUuid());
		Compendium.forgetLooks();
		BlockPos at = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		player.setPosition(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		return player;
	}

	private static boolean knows(TestContext ctx, ServerPlayerEntity p, String key) {
		return DiscoveryState.get(ctx.getWorld().getServer()).knows(p.getUuid(), key);
	}

	/** The ores in other rocks of the Geological world (3.17, 3.21) are their mineral's ore in the book. */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_data")
	public void rockOresBelongToTheirMineral(TestContext ctx) {
		check(ctx, !com.periodicelements.GeoOres.VARIANTS.isEmpty(), "no ores in other rocks");
		for (net.minecraft.block.Block b : com.periodicelements.GeoOres.VARIANTS) {
			CompendiumData.Tree t = CompendiumData.mineralOf(b);
			var base = net.minecraft.registry.Registries.BLOCK.get(com.periodicelements.GeoOres.baseOf(b));
			check(ctx, t != null && t == CompendiumData.mineralOf(base),
					net.minecraft.registry.Registries.BLOCK.getId(b) + " is not in the Mineral Compendium as " + CompendiumData.mineralOf(base));
		}
		check(ctx, CompendiumData.mineralOf(net.minecraft.registry.Registries.BLOCK.get(
				net.minecraft.util.Identifier.of("periodic_elements", "basalt_chromite_ore"))).id().equals("chromite"), "basalt chromite");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_data")
	public void everyMineralIsWellFormed(TestContext ctx) {
		check(ctx, CompendiumData.MINERALS.size() == 66, "expected 66 minerals, found " + CompendiumData.MINERALS.size());
		Set<net.minecraft.block.Block> seen = new HashSet<>();
		int ores = 0, other = 0;
		for (CompendiumData.Tree t : CompendiumData.MINERALS) {
			CompendiumData.Mineral m = CompendiumData.MINERAL_INFO.get(t.id());
			check(ctx, m != null && t.kind().equals(CompendiumData.MINERAL), t.id() + " is not a mineral");
			check(ctx, CompendiumData.GROUPS.contains(m.group()), t.id() + " is in an unknown group " + m.group());
			check(ctx, CompendiumData.CLASS_GROUPS.contains(m.section(true)), t.id() + " has an unknown class " + m.cls());
			boolean rock = m.group().equals("rocks") || m.group().equals("sediments");
			check(ctx, rock == (m.cls() != null), t.id() + ": only rocks and sediments have a class");
			check(ctx, rock == m.origin(), t.id() + ": only rocks and sediments tell their origin");
			check(ctx, m.real() == !m.group().equals("other_worlds"), t.id() + " is real in another world");
			if (m.group().equals("ores")) {
				ores++;
				check(ctx, m.mineral() && m.system() != null, t.id() + " lacks its mineral or crystal system");
				check(ctx, m.formula() != null || m.system().equals("amorphous"), t.id() + " lacks a formula");
				check(ctx, !t.products().isEmpty(), t.id() + " gives nothing");
			}
			if (m.group().equals("other_worlds")) other++;
			check(ctx, !t.blocks().isEmpty(), t.id() + " has no blocks");
			for (String b : t.blocks()) {
				check(ctx, Registries.BLOCK.containsId(Identifier.of(b)), t.id() + " names an unknown block " + b);
				var block = Registries.BLOCK.get(Identifier.of(b));
				check(ctx, seen.add(block), b + " belongs to two minerals");
				check(ctx, CompendiumData.mineralOf(block) == t, b + " is not recognised as " + t.id());
				check(ctx, CompendiumData.treeOf(block) == null && CompendiumData.plantOf(block) == null, b + " is also a tree or plant");
			}
			check(ctx, BlockComposition.of(t.blocks().get(0)) != null, t.id() + " has no composition");
			check(ctx, Registries.ITEM.containsId(Identifier.of(t.icon())), t.id() + " has no icon item");
			for (String pr : t.products()) {
				check(ctx, Registries.ITEM.containsId(Identifier.of(pr)), t.id() + " gives an unknown item " + pr);
			}
			for (String i : m.items()) {
				check(ctx, Registries.ITEM.containsId(Identifier.of(i)), t.id() + " hides an unknown item " + i);
			}
			if (m.depth() != null) {
				check(ctx, m.depth()[0] < m.depth()[1], t.id() + " has an empty depth range");
			}
		}
		check(ctx, ores == 33 && other == 5, "expected 33 ores and 5 of other worlds, found " + ores + " and " + other);
		check(ctx, CompendiumData.mineralOf(Blocks.DEEPSLATE_IRON_ORE) == CompendiumData.MINERAL_BY_ID.get("iron"), "deepslate iron ore");
		check(ctx, CompendiumData.mineralOf(Blocks.ORANGE_TERRACOTTA) == CompendiumData.MINERAL_BY_ID.get("terracotta"), "coloured terracotta");
		check(ctx, CompendiumData.mineralOf(Blocks.POLISHED_GRANITE) == null, "polished granite is made, not natural");
		check(ctx, CompendiumData.mineralsIn("igneous", true).contains(CompendiumData.MINERAL_BY_ID.get("basalt")), "basalt is igneous");
		check(ctx, CompendiumData.mineralsIn("rocks", false).contains(CompendiumData.MINERAL_BY_ID.get("basalt")), "basalt is a rock");
		for (String id : List.of("de_re_metallica", "theory_of_the_earth", "thin_sections", "xray_diffraction")) {
			check(ctx, Research.get(id) != null, "no research " + id);
		}
		check(ctx, Research.get("de_re_metallica").level() == 2 && Research.get("xray_diffraction").level() == 5, "research levels");
		check(ctx, Research.unlockedBy("compendium:origin").equals("theory_of_the_earth"), "Hutton does not unlock origin");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_look")
	public void looksAtARockForASecond(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		BlockPos rock = new BlockPos(3, 2, 1);
		ctx.setBlockState(rock, Blocks.GRANITE);
		p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(ctx.getAbsolutePos(rock)));
		p.getInventory().setStack(0, new ItemStack(Compendium.PLANT_COMPENDIUM));
		Compendium.tickPlayer(p, 0);
		Compendium.tickPlayer(p, Compendium.LOOK_TICKS);
		check(ctx, !knows(ctx, p, "mineral:granite"), "the Plant Compendium noted a rock");
		p.getInventory().setStack(1, new ItemStack(Compendium.MINERAL_COMPENDIUM));
		Compendium.tickPlayer(p, 100);
		Compendium.tickPlayer(p, 100 + Compendium.LOOK_TICKS);
		check(ctx, knows(ctx, p, "mineral:granite"), "did not note granite looked at for a second");
		check(ctx, !knows(ctx, p, "analysed:mineral:granite"), "looking analysed the rock without a lens");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_lens", maxAttempts = 3, requiredSuccesses = 1)
	public void theLensAnalysesInTwoSeconds(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		BlockPos rock = new BlockPos(3, 2, 1);
		// the test world is a normal world and test plots sit near y -60, where cave water or lava can wash the flower away:
		// shut the plot in a stone box first
		for (BlockPos q : BlockPos.iterate(-1, 1, -1, 8, 7, 4)) {
			boolean wall = q.getX() == -1 || q.getX() == 8 || q.getY() == 1 || q.getY() == 7 || q.getZ() == -1 || q.getZ() == 4;
			ctx.setBlockState(q, wall ? Blocks.STONE : Blocks.AIR);
		}
		ctx.setBlockState(rock, Blocks.BASALT);
		p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(ctx.getAbsolutePos(rock)));
		p.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModContent.MAGNIFYING_LENS));
		Compendium.tickPlayer(p, 0);
		Compendium.tickPlayer(p, Compendium.ANALYSE_TICKS - 1);
		check(ctx, !knows(ctx, p, "analysed:mineral:basalt"), "analysed before two seconds");
		Compendium.tickPlayer(p, Compendium.ANALYSE_TICKS);
		check(ctx, knows(ctx, p, "analysed:mineral:basalt") && knows(ctx, p, "mineral:basalt"),
				"the lens did not analyse and note basalt");
		// A plant too: the lens works without any book.
		BlockPos flower = new BlockPos(1, 2, 3);
		ctx.setBlockState(flower.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(flower, Blocks.POPPY);
		// flowers are shifted sideways by a random amount that depends on where they stand (and test plots move each run):
		// aim at the flower's own shape, not the middle of the block, or the eye can look past it
		BlockPos flowerAt = ctx.getAbsolutePos(flower);
		var flowerShape = ctx.getWorld().getBlockState(flowerAt).getOutlineShape(ctx.getWorld(), flowerAt);
		p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, flowerShape.isEmpty() ? Vec3d.ofCenter(flowerAt)
				: flowerShape.getBoundingBox().getCenter().add(Vec3d.of(flowerAt)));
		Compendium.tickPlayer(p, 200);
		Compendium.tickPlayer(p, 200 + Compendium.ANALYSE_TICKS);
		CompendiumData.Tree poppy = CompendiumData.plantOf(Blocks.POPPY);
		if (poppy == null || !knows(ctx, p, CompendiumData.analysedKey(poppy))) {
			var hit = p.raycast(Compendium.REACH, 1f, false);
			ctx.throwGameTestException("the lens did not analyse a poppy; entry " + poppy + " hit " + hit.getType() + " "
					+ (hit instanceof net.minecraft.util.hit.BlockHitResult b ? ctx.getRelativePos(b.getBlockPos()) + " " + ctx.getWorld().getBlockState(b.getBlockPos()) : "")
					+ " player " + p.getPos() + " flower " + ctx.getAbsolutePos(flower) + " " + ctx.getBlockState(flower) + " lens " + p.getMainHandStack());
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_tooltip")
	public void naturalThingsWaitForAnalysis(TestContext ctx) {
		check(ctx, "analysed:mineral:granite".equals(CompendiumData.naturalKey(Items.GRANITE)), "granite's block item");
		check(ctx, "analysed:mineral:iron".equals(CompendiumData.naturalKey(Items.RAW_IRON)), "raw iron");
		check(ctx, "analysed:mineral:iron".equals(CompendiumData.naturalKey(Items.DEEPSLATE_IRON_ORE)), "deepslate iron ore");
		check(ctx, CompendiumData.naturalKey(Items.POPPY) != null, "a poppy's item");
		check(ctx, CompendiumData.naturalKey(Items.IRON_INGOT) == null, "an iron ingot is made");
		check(ctx, CompendiumData.naturalKey(Items.POLISHED_GRANITE) == null, "polished granite is made");
		check(ctx, CompendiumData.naturalKey(Items.AIR) == null, "air");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_formation")
	public void formationsNeedHutton(TestContext ctx) {
		Geology.Column c = new Geology.Column();
		c.province = Geology.Province.BASIN;
		check(ctx, Compendium.formation(c).equals("sedimentary_basin"), "basin");
		c.plateau = 0.8;
		check(ctx, Compendium.formation(c).equals("mesa"), "mesa");
		c.province = Geology.Province.VOLCANIC;
		check(ctx, Compendium.formation(c).equals("basalt_plateau"), "trap plateau");
		c.neck = true;
		check(ctx, Compendium.formation(c).equals("volcanic_neck"), "neck");
		c.ocean = 0.9;
		check(ctx, Compendium.formation(c).equals("ocean_floor"), "ocean");
		c = new Geology.Column();
		c.province = Geology.Province.CRATON;
		check(ctx, Compendium.formation(c).equals("granite_shield"), "shield");
		c.mountain = 0.8;
		check(ctx, Compendium.formation(c).equals("mountain_belt"), "mountains");
		ServerPlayerEntity p = player(ctx);
		p.getInventory().setStack(0, new ItemStack(Compendium.MINERAL_COMPENDIUM));
		check(ctx, Compendium.formationOf(p).isEmpty(), "named a formation without Hutton");
		MinecraftServer server = ctx.getWorld().getServer();
		ResearchState.get(server).grant(server, p.getUuid(), List.of("theory_of_the_earth"));
		check(ctx, Compendium.knows(p, "origin"), "Hutton does not grant the origin layer");
		// The test world is not Geological: still nothing to name.
		check(ctx, Compendium.formationOf(p).isEmpty(), "named a formation outside the Geological world");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mineral_compendium_reveal")
	public void revealNotesEveryRock(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		int all = Compendium.revealAll(p);
		check(ctx, all == CompendiumData.TREES.size() + 2 * CompendiumData.PLANTS.size() + 2 * CompendiumData.MINERALS.size()
				+ CompendiumData.ANIMALS.size() + CompendiumData.BIOMES.size(), "reveal noted " + all);
		check(ctx, knows(ctx, p, "mineral:end_stone") && knows(ctx, p, "analysed:mineral:pitchblende"), "reveal missed a rock");
		ctx.complete();
	}
}
