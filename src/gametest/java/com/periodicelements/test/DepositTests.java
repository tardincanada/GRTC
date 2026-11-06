package com.periodicelements.test;

import com.periodicelements.GeoOres;
import com.periodicelements.worldgen.Deposits;
import com.periodicelements.worldgen.Geology;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/** Stage 3a of the Geological world (3.17.0): ores by host rock, and deposits in place of veins. */
public class DepositTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) ctx.throwGameTestException(message);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void oresFollowTheHostRock(TestContext ctx) {
		expect(ctx, GeoOres.ore("iron", Blocks.STONE.getDefaultState()).isOf(Blocks.IRON_ORE), "iron in stone is not iron ore");
		expect(ctx, GeoOres.ore("iron", Blocks.DEEPSLATE.getDefaultState()).isOf(Blocks.DEEPSLATE_IRON_ORE), "iron in deepslate");
		var granite = GeoOres.ore("cassiterite", Blocks.GRANITE.getDefaultState());
		expect(ctx, granite != null && Registries.BLOCK.getId(granite.getBlock()).getPath().equals("granite_cassiterite_ore"),
				"cassiterite in granite: " + granite);
		expect(ctx, GeoOres.ore("cassiterite", Blocks.CALCITE.getDefaultState()) == null, "cassiterite has no calcite ore");
		expect(ctx, GeoOres.ore("coal", Blocks.BASALT.getDefaultState()) != null, "no coal in basalt (lignite)");
		expect(ctx, GeoOres.baseOf(granite.getBlock()).equals(Identifier.of("periodic_elements", "cassiterite_ore")),
				"granite cassiterite does not stand for cassiterite ore");
		expect(ctx, !GeoOres.host(Blocks.DIRT.getDefaultState()), "dirt must not hold ore");
		expect(ctx, GeoOres.ore("chromite", Blocks.BASALT.getDefaultState()) != null, "no chromite in basalt");
		expect(ctx, GeoOres.ore("chromite", Blocks.DEEPSLATE.getDefaultState()) != null, "chromite lost its deepslate ore");
		expect(ctx, GeoOres.ore("saltpetre", Blocks.SANDSTONE.getDefaultState()).getBlock() == Registries.BLOCK.get(Identifier.of("periodic_elements", "saltpetre_crust")),
				"saltpetre in sandstone is not the saltpetre crust");
		expect(ctx, GeoOres.ore("diamond", Blocks.TUFF.getDefaultState()) != null, "no diamond in tuff (kimberlite)");
		var gravelGold = GeoOres.ore("gold", Blocks.GRAVEL.getDefaultState());
		expect(ctx, gravelGold != null && !gravelGold.isToolRequired(), "gold gravel must be dug without a pickaxe");
		expect(ctx, GeoOres.ore("bauxite", Blocks.ORANGE_TERRACOTTA.getDefaultState()) != null, "no bauxite in laterite");
		// stage 4a: the mod's own rocks
		expect(ctx, GeoOres.ore("diamond", com.periodicelements.GeoRocks.KIMBERLITE.getDefaultState()) != null, "no diamond in kimberlite");
		expect(ctx, GeoOres.ore("bauxite", com.periodicelements.GeoRocks.LATERITE.getDefaultState()) != null, "no bauxite in laterite rock");
		expect(ctx, GeoOres.ore("chromite", com.periodicelements.GeoRocks.PERIDOTITE.getDefaultState()) != null, "no chromite in peridotite");
		expect(ctx, GeoOres.ore("oil_shale", com.periodicelements.GeoRocks.SHALE.getDefaultState()) != null, "no oil shale in shale");
		expect(ctx, GeoOres.ore("pitchblende", com.periodicelements.GeoRocks.GNEISS.getDefaultState()) != null, "no pitchblende in gneiss");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void depositsReplaceTheCoveredVeinsOnly(TestContext ctx) {
		for (String v : new String[]{"ore_iron_upper", "ore_coal_lower", "ore_copper", "ore_gold", "ore_granite_upper", "ore_tuff"}) {
			expect(ctx, Deposits.replacesVein(Identifier.ofVanilla(v)), v + " still generates in the Geological world");
		}
		expect(ctx, Deposits.replacesVein(Identifier.of("periodic_elements", "ore_cassiterite")), "cassiterite veins still generate");
		for (String v : new String[]{"ore_redstone", "ore_lapis", "ore_emerald"}) {
			expect(ctx, !Deposits.replacesVein(Identifier.ofVanilla(v)), v + " keeps its veins (no geological pair)");
		}
		// 3b: kimberlite, magmatic layers, evaporites, fumaroles and laterite replace these
		expect(ctx, Deposits.replacesVein(Identifier.ofVanilla("ore_diamond")), "diamond veins still generate");
		for (String v : new String[]{"ore_chromite", "ore_pentlandite", "ore_sulfur_deep", "ore_saltpetre_desert", "ore_borax_desert", "ore_bauxite", "ore_pyrolusite"}) {
			expect(ctx, Deposits.replacesVein(Identifier.of("periodic_elements", v)), v + " still generates");
		}
		expect(ctx, !Deposits.replacesVein(Identifier.of("periodic_elements", "ore_heavy_mineral_sand")), "beach heavy mineral sand must stay");
		expect(ctx, !Deposits.replacesVein(Identifier.of("periodic_elements", "ore_sulfur_nether")), "nether sulfur must stay");
		ctx.complete();
	}

	/** Guilherme's rule: iron, copper and coal within about 500 blocks of any point on land. */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 1200)
	public void ironCopperAndCoalInEveryLandRegion(TestContext ctx) {
		Geology g = Geology.fresh(123321L);
		int land = 0;
		java.util.Set<String> all = new java.util.TreeSet<>();
		for (int rx = -6; rx < 6; rx++) {
			for (int rz = -6; rz < 6; rz++) {
				var m = Deposits.mineralsIn(g, rx, rz);
				all.addAll(m);
				if (m.isEmpty()) continue;
				land++;
				for (String need : new String[]{"iron", "copper", "coal"}) {
					expect(ctx, m.contains(need), "region " + rx + "," + rz + " has no " + need + ": " + m);
				}
			}
		}
		expect(ctx, land > 40, "only " + land + " of 144 regions have deposits");
		for (String need : new String[]{"gold", "cassiterite", "spodumene", "pitchblende", "galena", "sphalerite", "molybdenite", "oil_shale"}) {
			expect(ctx, all.contains(need), "no " + need + " deposit in 144 regions: " + all);
		}
		ctx.complete();
	}

	/** Not a check: writes the estimated ore per chunk (seed 123321, 16 x 16 regions) for balancing. */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 2400)
	public void surveyDeposits(TestContext ctx) {
		if (!Boolean.getBoolean("pe.survey")) { // slow (about 10 minutes): only when asked for
			ctx.complete();
			return;
		}
		var config = net.minecraft.world.gen.noise.NoiseConfig.create(ctx.getWorld().getRegistryManager().createRegistryLookup(),
				com.periodicelements.worldgen.GeoWorld.SETTINGS, 123321L);
		Geology g = com.periodicelements.worldgen.GeoWorld.geology(config);
		Deposits.useClimate(g, config);
		if (Boolean.getBoolean("pe.depositspots")) {
			try {
				java.nio.file.Files.write(java.nio.file.Path.of("deposit-spots.txt"), Deposits.describe(g, -4, 4));
			} catch (java.io.IOException e) {
				ctx.throwGameTestException(e.toString());
			}
		}
		var m = Deposits.survey(g, -8, -8, 7, 7, 2);
		StringBuilder sb = new StringBuilder();
		m.forEach((k, v) -> sb.append(k).append('\t').append(String.format(java.util.Locale.ROOT, "%.2f", v)).append('\n'));
		try {
			java.nio.file.Files.writeString(java.nio.file.Path.of("deposit-survey.txt"), sb.toString());
		} catch (java.io.IOException e) {
			ctx.throwGameTestException(e.toString());
		}
		ctx.complete();
	}
}
