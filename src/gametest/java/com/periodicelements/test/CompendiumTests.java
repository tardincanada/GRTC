package com.periodicelements.test;

import com.periodicelements.compendium.Compendium;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.compendium.CompendiumNotesItem;
import com.periodicelements.compendium.DiscoveryState;
import com.periodicelements.compendium.TreeCompendiumItem;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.trees.Trees;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.List;

/** 3.1.0 Tree Compendium. Mock players share one UUID, so each test has its own batch and starts with no notes. */
public class CompendiumTests implements FabricGameTest {
	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static ServerPlayerEntity player(TestContext ctx, String... projects) {
		// Clear the air round the player: blocks left by earlier tests in the same spot can block the line of sight.
		for (int x = 0; x <= 5; x++) for (int y = 2; y <= 6; y++) for (int z = 0; z <= 5; z++) ctx.setBlockState(new BlockPos(x, y, z), net.minecraft.block.Blocks.AIR);
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		MinecraftServer server = ctx.getWorld().getServer();
		ResearchState rs = ResearchState.get(server);
		rs.revoke(server, player.getUuid(), Research.all().stream().map(Research.Project::id).toList());
		rs.grant(server, player.getUuid(), List.of(projects));
		DiscoveryState.get(server).clear(player.getUuid());
		Compendium.forgetLooks();
		BlockPos at = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		player.setPosition(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		return player;
	}

	private static boolean knows(TestContext ctx, ServerPlayerEntity p, String key) {
		return DiscoveryState.get(ctx.getWorld().getServer()).knows(p.getUuid(), key);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_look")
	public void looksAtATreeForASecond(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		BlockPos log = new BlockPos(4, 3, 1);
		ctx.setBlockState(log, Registries.BLOCK.get(Identifier.of("periodic_elements", "mahogany_log")).getDefaultState());
		p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(ctx.getAbsolutePos(log)));
		Compendium.tickPlayer(p, 0);
		Compendium.tickPlayer(p, 40);
		check(ctx, !knows(ctx, p, "tree:mahogany"), "noted a tree without the Compendium");
		p.getInventory().setStack(3, new ItemStack(Compendium.TREE_COMPENDIUM));
		Compendium.tickPlayer(p, 100);
		check(ctx, !knows(ctx, p, "tree:mahogany"), "noted a tree at the first glance");
		Compendium.tickPlayer(p, 100 + Compendium.LOOK_TICKS);
		check(ctx, knows(ctx, p, "tree:mahogany"), "did not note a tree looked at for a second");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_backpack")
	public void onlyFromTheHotbar(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		BlockPos log = new BlockPos(4, 3, 1);
		ctx.setBlockState(log, Blocks.BIRCH_LOG);
		p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(ctx.getAbsolutePos(log)));
		p.getInventory().setStack(20, new ItemStack(Compendium.TREE_COMPENDIUM));
		check(ctx, !Compendium.carries(p), "a book in the backpack counts as carried");
		p.getInventory().setStack(20, ItemStack.EMPTY);
		p.getInventory().setStack(0, new ItemStack(Compendium.TREE_COMPENDIUM));
		Compendium.tickPlayer(p, 0);
		Compendium.tickPlayer(p, Compendium.LOOK_TICKS);
		check(ctx, knows(ctx, p, "tree:birch"), "a vanilla birch log was not noted as the birch");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_fell")
	public void fellingNotesTheTree(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		p.getInventory().setStack(0, new ItemStack(Compendium.TREE_COMPENDIUM));
		BlockPos leaves = new BlockPos(2, 2, 2);
		ctx.setBlockState(leaves, Registries.BLOCK.get(Identifier.of("periodic_elements", "baobab_leaves")).getDefaultState());
		p.interactionManager.tryBreakBlock(ctx.getAbsolutePos(leaves));
		check(ctx, knows(ctx, p, "tree:baobab"), "breaking baobab leaves did not note the baobab");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_biome")
	public void walkingInNotesTheBiome(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		p.getInventory().setStack(0, new ItemStack(Compendium.TREE_COMPENDIUM));
		String biome = Compendium.biomeAt(p);
		Compendium.tickPlayer(p, 0);
		if (CompendiumData.BIOME_BY_ID.containsKey(biome)) {
			check(ctx, knows(ctx, p, "biome:" + biome), "the biome " + biome + " was not noted");
		} else {
			check(ctx, DiscoveryState.get(ctx.getWorld().getServer()).of(p.getUuid()).isEmpty(),
					"noted a biome the book does not list: " + biome);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_notes")
	public void notesCarryEntriesToAnotherBook(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		MinecraftServer server = ctx.getWorld().getServer();
		Compendium.note(p, "tree:teak", p.getBlockPos());
		Compendium.note(p, "biome:jungle", p.getBlockPos());
		ItemStack none = TreeCompendiumItem.makeNotes(p, new ItemStack(Items.PAPER));
		check(ctx, none.isEmpty(), "made notes without a book");
		ItemStack book = new ItemStack(Items.BOOK);
		ItemStack notes = TreeCompendiumItem.makeNotes(p, book);
		check(ctx, notes.isOf(Compendium.COMPENDIUM_NOTES) && book.isEmpty(), "the book did not become Compendium Notes");
		DiscoveryState.get(server).clear(p.getUuid());
		int gained = CompendiumNotesItem.read(p, notes);
		check(ctx, gained == 2 && notes.isEmpty(), "reading the notes gave " + gained + " entries");
		check(ctx, knows(ctx, p, "tree:teak") && knows(ctx, p, "biome:jungle"), "the notes' entries are missing");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_research")
	public void researchAddsLayers(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		var known = ResearchState.get(ctx.getWorld().getServer()).known(p.getUuid());
		check(ctx, !Compendium.has(known, "classification") && !Compendium.has(known, "climate")
				&& !Compendium.has(known, "conservation"), "layers open without research");
		p = player(ctx, "species_plantarum", "plant_geography", "red_list");
		known = ResearchState.get(ctx.getWorld().getServer()).known(p.getUuid());
		check(ctx, Compendium.has(known, "classification") && Compendium.has(known, "climate")
				&& Compendium.has(known, "conservation"), "the research did not open the layers");
		check(ctx, Research.get("species_plantarum").level() == 3 && Research.get("red_list").level() == 6,
				"research levels changed");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "compendium_data")
	public void everyTreeIsInTheBook(TestContext ctx) {
		for (Trees.Species s : Trees.ALL) {
			CompendiumData.Tree t = CompendiumData.BY_ID.get(s.id());
			check(ctx, t != null, s.id() + " is not in the compendium");
			check(ctx, CompendiumData.treeOf(s.log()) == t && CompendiumData.treeOf(s.leaves()) == t
					&& CompendiumData.treeOf(s.sapling()) == t, s.id() + "'s blocks are not recognised");
			check(ctx, t.real(), s.id() + " has no scientific name");
		}
		for (CompendiumData.Tree t : CompendiumData.TREES) {
			for (String b : t.biomes()) {
				check(ctx, CompendiumData.BIOME_BY_ID.containsKey(b), t.id() + " grows in an unlisted biome " + b);
			}
			check(ctx, Registries.ITEM.containsId(Identifier.of(t.icon())), t.id() + " has no icon item");
			for (String pr : t.products()) {
				check(ctx, Registries.ITEM.containsId(Identifier.of(pr)), t.id() + " gives an unknown item " + pr);
			}
		}
		check(ctx, CompendiumData.treeOf(Blocks.OAK_LEAVES) == CompendiumData.BY_ID.get("oak"), "oak leaves");
		check(ctx, CompendiumData.TREES.size() == Trees.ALL.size() + 9, "expected the mod's trees plus 9 vanilla");
		ctx.complete();
	}
}
