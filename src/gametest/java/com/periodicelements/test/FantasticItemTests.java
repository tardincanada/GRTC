package com.periodicelements.test;

import com.periodicelements.BlockComposition;
import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.machine.Upgrades;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 3.46.0: special roles for the Nether and End items (blaze and magma fuels, ender pearl, nether star, ghast tear). */
public class FantasticItemTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static boolean has(String item, String part) {
		BlockComposition.Entry e = BlockComposition.of(item);
		return e != null && e.parts().stream().anyMatch(p -> p.id().equals(part));
	}

	private static Reactions.Reaction reaction(String machine, String id) {
		return Reactions.forMachine(machine).stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void blazeAndMagmaAreFuels(TestContext ctx) {
		Reactions.Fuel rod = Reactions.fuel(new ItemStack(Items.BLAZE_ROD));
		check(ctx, rod != null && rod.flame() >= 1600 && rod.energy() > Reactions.fuel(new ItemStack(Items.COAL)).energy(),
				"blaze rod should burn hotter and longer than coal: " + rod);
		check(ctx, Reactions.fuel(new ItemStack(Items.BLAZE_POWDER)) != null, "blaze powder is a fuel");
		check(ctx, Reactions.fuel(new ItemStack(Items.MAGMA_CREAM)) != null, "magma cream is a fuel");
		check(ctx, Reactions.fuel(new ItemStack(Items.MAGMA_BLOCK)) != null, "magma block is a fuel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void newReadingsOfPearlStarAndTear(TestContext ctx) {
		check(ctx, has("minecraft:ender_pearl", "monazite") && has("minecraft:ender_pearl", "magnetite"), "ender pearl = magnetite + monazite");
		check(ctx, has("minecraft:nether_star", "iridium") && has("minecraft:nether_star", "osmium")
				&& !has("minecraft:nether_star", "carbon"), "nether star = osmiridium, no longer diamond");
		check(ctx, has("minecraft:ghast_tear", "ruthenium"), "ghast tear = DSA anode coating");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void ghastTearIsAChlorineAnode(TestContext ctx) {
		check(ctx, Upgrades.GHAST_TEAR_ANODE.equals(Upgrades.of(new ItemStack(Items.GHAST_TEAR))), "ghast tear is an upgrade");
		check(ctx, Upgrades.anodeHelps(reaction("electrolysis_cell", "salt_electrolysis")), "salt electrolysis makes chlorine");
		check(ctx, Upgrades.anodeHelps(reaction("chlor_alkali_cell", "brine_electrolysis")), "brine electrolysis makes chlorine");
		check(ctx, !Upgrades.anodeHelps(reaction("electrolysis_cell", "water_electrolysis")), "water electrolysis makes no chlorine");
		// In a cell's upgrade slot it is active with no research.
		ctx.setBlockState(POS, Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "electrolysis_cell")));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		be.setOwner(UUID.randomUUID());
		be.setStack(MachineBlockEntity.UPGRADE_FIRST, new ItemStack(Items.GHAST_TEAR));
		BlockPos abs = ctx.getAbsolutePos(POS);
		be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		check(ctx, be.upgraded(Upgrades.GHAST_TEAR_ANODE), "the tear should be active in the upgrade slot");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void netherStarGivesPlatinumMetals(TestContext ctx) {
		// 9 stars = 1000 units: 400 iridium, 350 osmium -> 3 of each from the crystallizer, like anode slime.
		ctx.setBlockState(POS, Blocks.AIR);
		ctx.setBlockState(POS, Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "crystallizer")));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner,
				new ArrayList<>(List.of("saltpeter_refining", "platinum_metals")));
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 64));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.NETHER_STAR, 9));
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int t = 0; t < 60_000; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT)) {
				break;
			}
		}
		int ir = 0;
		int os = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			ItemStack s = be.getStack(i);
			ir += s.isOf(mod("iridium")) ? s.getCount() : 0;
			os += s.isOf(mod("osmium")) ? s.getCount() : 0;
		}
		PeriodicElements.LOGGER.info("[gametest] nether stars: {} iridium, {} osmium, buffers {}", ir, os, be.outputBuffers());
		check(ctx, ir == 3 && os == 3, "expected 3 iridium and 3 osmium, got " + ir + " and " + os);
		ctx.complete();
	}
}
