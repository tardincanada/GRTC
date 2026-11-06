package com.periodicelements.test;

import com.periodicelements.compendium.Compendium;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.compendium.CompendiumNotesItem;
import com.periodicelements.compendium.DiscoveryState;
import com.periodicelements.compendium.TreeCompendiumItem;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 3.5.0 Plant Compendium. Mock players share one UUID, so each test has its own batch and starts with no notes. */
public class PlantCompendiumTests implements FabricGameTest {
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

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "plant_compendium_data")
	public void everyPlantIsWellFormed(TestContext ctx) {
		check(ctx, CompendiumData.PLANTS.size() == 101, "expected 101 plants (3.22.0: 41 more shrubs), found " + CompendiumData.PLANTS.size());
		Set<net.minecraft.block.Block> seen = new HashSet<>();
		int real = 0;
		for (CompendiumData.Tree t : CompendiumData.PLANTS) {
			check(ctx, t.kind().equals(CompendiumData.PLANT) && t.key().equals("plant:" + t.id()), t.id() + " is not a plant");
			check(ctx, !t.blocks().isEmpty(), t.id() + " has no blocks");
			for (String b : t.blocks()) {
				check(ctx, Registries.BLOCK.containsId(Identifier.of(b)), t.id() + " names an unknown block " + b);
				var block = Registries.BLOCK.get(Identifier.of(b));
				check(ctx, seen.add(block), b + " belongs to two plants");
				check(ctx, CompendiumData.plantOf(block) == t, b + " is not recognised as " + t.id());
				check(ctx, CompendiumData.treeOf(block) == null, b + " is also a tree");
			}
			check(ctx, Registries.ITEM.containsId(Identifier.of(t.icon())), t.id() + " has no icon item");
			for (String pr : t.products()) {
				check(ctx, Registries.ITEM.containsId(Identifier.of(pr)), t.id() + " gives an unknown item " + pr);
			}
			check(ctx, !t.biomes().isEmpty(), t.id() + " grows nowhere");
			for (String b : t.biomes()) {
				check(ctx, CompendiumData.BIOME_BY_ID.containsKey(b), t.id() + " grows in an unlisted biome " + b);
			}
			if (t.real()) {
				real++;
				check(ctx, t.family() != null && t.iucn() != null, t.id() + " lacks a family or Red List status");
			}
		}
		check(ctx, real == 94, "expected 94 real species, found " + real);
		check(ctx, CompendiumData.plantOf(Blocks.WHEAT) == CompendiumData.PLANT_BY_ID.get("wheat"), "wheat crop");
		check(ctx, CompendiumData.plantOf(Blocks.KELP_PLANT) == CompendiumData.PLANT_BY_ID.get("kelp"), "kelp stem");
		check(ctx, CompendiumData.plantOf(Blocks.OAK_LEAVES) == null, "oak leaves are a plant");
		// The tree book keeps every biome it had; the water biomes are only in the plant book, and since 3.22.0 the
		// mountain tops too (alpine shrubs).
		check(ctx, CompendiumData.biomes(CompendiumData.TREE).size() == 39, "the Tree Compendium's biome list changed");
		List<String> plantBiomes = CompendiumData.biomes(CompendiumData.PLANT).stream().map(CompendiumData.Biome::id).toList();
		check(ctx, plantBiomes.contains("warm_ocean") && plantBiomes.contains("lush_caves") && plantBiomes.contains("frozen_peaks"),
				"the Plant Compendium lists the wrong biomes: " + plantBiomes);
		check(ctx, new ItemStack(Items.DANDELION).isIn(ItemTags.SMALL_FLOWERS), "the recipe's flower tag");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "plant_compendium_look", maxAttempts = 3, requiredSuccesses = 1)
	public void looksAtAFlowerForASecond(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		BlockPos flower = new BlockPos(3, 2, 1);
		// leftovers of earlier tests (a tree grown past its plot) must not stand between the eye and the flower
		// the test world is a normal world and test plots sit near y -60, where cave water or lava can wash the flower away:
		// shut the plot in a stone box first
		for (BlockPos q : BlockPos.iterate(-1, 1, -1, 8, 7, 4)) {
			boolean wall = q.getX() == -1 || q.getX() == 8 || q.getY() == 1 || q.getY() == 7 || q.getZ() == -1 || q.getZ() == 4;
			ctx.setBlockState(q, wall ? Blocks.STONE : Blocks.AIR);
		}
		ctx.setBlockState(flower.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(flower, Blocks.CORNFLOWER);
		// flowers are shifted sideways by a random amount that depends on where they stand (and test plots move each run):
		// aim at the flower's own shape, not the middle of the block, or the eye can look past it
		BlockPos flowerAt = ctx.getAbsolutePos(flower);
		var flowerShape = ctx.getWorld().getBlockState(flowerAt).getOutlineShape(ctx.getWorld(), flowerAt);
		p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, flowerShape.isEmpty() ? Vec3d.ofCenter(flowerAt)
				: flowerShape.getBoundingBox().getCenter().add(Vec3d.of(flowerAt)));
		p.getInventory().setStack(0, new ItemStack(Compendium.TREE_COMPENDIUM));
		Compendium.tickPlayer(p, 0);
		Compendium.tickPlayer(p, Compendium.LOOK_TICKS);
		check(ctx, !knows(ctx, p, "plant:cornflower"), "the Tree Compendium noted a flower");
		p.getInventory().setStack(1, new ItemStack(Compendium.PLANT_COMPENDIUM));
		Compendium.tickPlayer(p, 100);
		check(ctx, !knows(ctx, p, "plant:cornflower"), "noted a plant at the first glance");
		Compendium.tickPlayer(p, 100 + Compendium.LOOK_TICKS);
		if (!knows(ctx, p, "plant:cornflower")) {
			var hit = p.raycast(Compendium.REACH, 1f, false);
			BlockPos.Mutable at = new BlockPos.Mutable();
			ctx.throwGameTestException("did not note a cornflower looked at for a second; hit " + hit.getType() + " "
					+ (hit instanceof net.minecraft.util.hit.BlockHitResult b ? b.getBlockPos() + " " + ctx.getWorld().getBlockState(b.getBlockPos()) : "")
					+ " entry " + Compendium.lookedAt(p, at) + " player " + p.getPos() + " flower " + ctx.getAbsolutePos(flower)
					+ " uuid " + p.getUuid() + " stacks " + p.getInventory().getStack(0) + "," + p.getInventory().getStack(1));
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "plant_compendium_harvest")
	public void harvestingNotesTheCrop(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		p.getInventory().setStack(8, new ItemStack(Compendium.PLANT_COMPENDIUM));
		BlockPos crop = new BlockPos(2, 2, 2);
		ctx.setBlockState(crop.down(), Blocks.FARMLAND);
		ctx.setBlockState(crop, Blocks.POTATOES);
		p.interactionManager.tryBreakBlock(ctx.getAbsolutePos(crop));
		check(ctx, knows(ctx, p, "plant:potato"), "harvesting potatoes did not note the potato");
		check(ctx, !knows(ctx, p, "tree:oak"), "noted a tree");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "plant_compendium_notes")
	public void notesCarryBothBooks(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		MinecraftServer server = ctx.getWorld().getServer();
		Compendium.note(p, "tree:teak", p.getBlockPos());
		Compendium.note(p, "plant:cocoa", p.getBlockPos());
		Compendium.note(p, "biome:warm_ocean", p.getBlockPos());
		ItemStack notes = TreeCompendiumItem.makeNotes(p, new ItemStack(Items.BOOK));
		DiscoveryState.get(server).clear(p.getUuid());
		int gained = CompendiumNotesItem.read(p, notes);
		check(ctx, gained == 3, "reading the notes gave " + gained + " entries");
		check(ctx, knows(ctx, p, "plant:cocoa") && knows(ctx, p, "tree:teak") && knows(ctx, p, "biome:warm_ocean"),
				"the notes' entries are missing");
		int all = Compendium.revealAll(p);
		check(ctx, knows(ctx, p, "plant:glow_lichen") && all == CompendiumData.TREES.size() + 2 * CompendiumData.PLANTS.size()
				+ 2 * CompendiumData.MINERALS.size() + CompendiumData.ANIMALS.size() + CompendiumData.BIOMES.size() - 3, "reveal did not note every plant (" + all + ")");
		ctx.complete();
	}
}
