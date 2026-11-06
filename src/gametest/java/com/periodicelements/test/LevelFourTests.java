package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.radiation.RadiationState;
import com.periodicelements.radiation.Radiation;
import com.periodicelements.radiation.Radioactivity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Level 4 (0.7.0): dynamo and wire, steel, aluminium, chemicals, liquid air, refining, radioactivity. */
public class LevelFourTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);
	private static final BlockPos WIRE_A = new BlockPos(2, 1, 1);
	private static final BlockPos WIRE_B = new BlockPos(3, 1, 1);
	private static final BlockPos DYNAMO = new BlockPos(4, 1, 1);
	private static final BlockPos ENGINE = new BlockPos(5, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static MachineBlockEntity place(TestContext ctx, String machine, List<String> projects) {
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(mod("glass_flask"), 32));
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
		return be;
	}

	/** A steam engine driving a dynamo, joined to the machine by two blocks of copper wire. */
	private static List<PowerBlockEntity> powerPlant(TestContext ctx) {
		ctx.setBlockState(WIRE_A, block("copper_wire"));
		ctx.setBlockState(WIRE_B, block("copper_wire"));
		ctx.setBlockState(DYNAMO, block("dynamo"));
		ctx.setBlockState(ENGINE, block("steam_engine"));
		PowerBlockEntity engine = ctx.getBlockEntity(ENGINE);
		engine.setStack(0, new ItemStack(Items.COAL, 64));
		engine.setStack(1, new ItemStack(Items.COAL, 64));
		for (int i = 2; i < 9; i++) {
			engine.setStack(i, new ItemStack(Items.WATER_BUCKET));
		}
		List<PowerBlockEntity> out = new ArrayList<>();
		out.add(engine);
		out.add(ctx.getBlockEntity(DYNAMO));
		return out;
	}

	/** Ticks the machine (and its power plant) until its input is used up and it goes idle. */
	private static int tick(TestContext ctx, MachineBlockEntity be, List<PowerBlockEntity> plant, int limit, boolean untilIdle) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		double peak = 0;
		for (; t < limit; t++) {
			if (plant != null) {
				for (PowerBlockEntity p : plant) {
					p.tick(ctx.getWorld(), p.getPos(), ctx.getWorld().getBlockState(p.getPos()));
				}
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			peak = Math.max(peak, be.temperature());
			for (int s = MachineBlockEntity.PRODUCT_FIRST; s <= MachineBlockEntity.RESIDUE; s++) {
				if (be.getStack(s).getCount() >= 60) {
					be.setStack(s, ItemStack.EMPTY); // keep the test from backing up (counts are checked below that)
				}
			}
			if (untilIdle && t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT)) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, peak {} °C, status {}, outputs {} {} {} | residue {} | reagent {} | fuel {} | buffers {}{}",
				be.spec().id(), t, String.format(Locale.ROOT, "%.0f", peak), be.status(), be.getStack(2), be.getStack(3),
				be.getStack(4), be.getStack(5), be.getStack(6), be.getStack(MachineBlockEntity.FUEL), be.outputBuffers(),
				plant == null ? "" : " | " + plant.get(1).describe().getString());
		return t;
	}

	private static MachineBlockEntity run(TestContext ctx, String machine, List<String> projects, ItemStack input,
										  ItemStack reagent, ItemStack fuel, boolean powered, int limit) {
		MachineBlockEntity be = place(ctx, machine, projects);
		List<PowerBlockEntity> plant = powered ? powerPlant(ctx) : null;
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.REAGENT, reagent);
		be.setStack(MachineBlockEntity.FUEL, fuel);
		tick(ctx, be, plant, limit, true);
		return be;
	}

	private static int count(PowerBlockEntity be, Item item) {
		int n = 0;
		for (int i = 0; i < PowerBlockEntity.SIZE; i++) {
			if (be.getStack(i).isOf(item)) {
				n += be.getStack(i).getCount();
			}
		}
		return n;
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

	private static void expect(TestContext ctx, int got, int want, String what) {
		if (got != want) {
			ctx.throwGameTestException("expected " + want + " " + what + ", got " + got);
		}
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static ItemStack coke() {
		return new ItemStack(mod("coke"), 64);
	}

	private static ItemStack charcoal() {
		return new ItemStack(Items.CHARCOAL, 64);
	}

	// ------------------------------------------------------------ power

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void dynamoPowersHallHeroultThroughWire(TestContext ctx) {
		// 4 alumina (444 units) -> 235 units of aluminium -> 2 ingots; the carbon anode comes from the coke.
		MachineBlockEntity be = run(ctx, "hall_heroult_cell", List.of("dynamo", "bayer_process", "hall_heroult"),
				new ItemStack(mod("aluminium_oxide"), 4), new ItemStack(mod("cryolite"), 1), coke(), true, 40_000);
		expect(ctx, count(be, mod("aluminium")), 2, "aluminium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void noWireNoPower(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "hall_heroult_cell", List.of("dynamo", "bayer_process", "hall_heroult"));
		List<PowerBlockEntity> plant = powerPlant(ctx);
		ctx.setBlockState(WIRE_B, Blocks.AIR);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("aluminium_oxide"), 1));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("cryolite"), 1));
		be.setStack(MachineBlockEntity.FUEL, coke());
		tick(ctx, be, plant, 400, false);
		expect(ctx, count(be, mod("aluminium")), 0, "aluminium without a wire to the dynamo");
		check(ctx, be.status() == MachineBlockEntity.Status.NO_POWER, "status should be NO_POWER, was " + be.status());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void chlorAlkaliMakesLyeAndChlorine(TestContext ctx) {
		// 4 salt: 304 units NaOH -> 2, 269 units Cl -> 2.
		MachineBlockEntity be = run(ctx, "chlor_alkali_cell", List.of("dynamo", "chlor_alkali"),
				new ItemStack(mod("sodium_chloride"), 4), new ItemStack(Items.WATER_BUCKET), ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(be, mod("sodium_hydroxide")), 2, "lye");
		expect(ctx, count(be, mod("chlorine")), 2, "chlorine");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void airLiquefierSeparatesAir(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "air_liquefier", List.of("dynamo", "air_liquefaction"));
		List<PowerBlockEntity> plant = powerPlant(ctx);
		tick(ctx, be, plant, 1200, false);
		check(ctx, count(be, mod("nitrogen")) >= 6, "nitrogen from the air: " + count(be, mod("nitrogen")));
		check(ctx, count(be, mod("oxygen")) >= 1, "oxygen from the air");
		check(ctx, be.outputBuffers().getOrDefault("liquid_air", 0.0) > 0,
				"without Neon, Krypton and Xenon research the rare gases stay in the liquid air");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void nobleGasesComeOutWithResearch(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "air_liquefier", List.of("dynamo", "air_liquefaction", "spectroscopy", "noble_gases"));
		List<PowerBlockEntity> plant = powerPlant(ctx);
		tick(ctx, be, plant, 1200, false);
		check(ctx, be.outputBuffers().getOrDefault("item:periodic_elements:neon", 0.0) > 0, "neon should collect");
		check(ctx, be.outputBuffers().getOrDefault("item:periodic_elements:xenon", 0.0) > 0, "xenon should collect");
		check(ctx, be.outputBuffers().getOrDefault("item:periodic_elements:helium", 0.0) > 0, "3.36.0: a little helium too");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void heliumCollectorFillsAFlaskADayFromPitchblende(TestContext ctx) {
		// 3.36.0: 64 Raw Pitchblende give one flask of helium per Minecraft day, and none of it is used up.
		ctx.setBlockState(POS, block("helium_collector"));
		PowerBlockEntity he = ctx.getBlockEntity(POS);
		he.setStack(0, new ItemStack(mod("raw_pitchblende"), 64));
		he.setStack(1, new ItemStack(mod("glass_flask"), 2));
		BlockPos abs = ctx.getAbsolutePos(POS);
		check(ctx, Math.abs(he.heliumRate() * 24000 - 1000.0 / 9) < 0.01, "one flask a day: " + he.heliumRate() * 24000);
		for (int i = 0; i < 23_900; i++) {
			he.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, count(he, mod("helium")) == 0, "not quite a day yet");
		for (int i = 0; i < 200; i++) {
			he.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		expect(ctx, count(he, mod("helium")), 1, "a flask of helium after a day");
		expect(ctx, count(he, mod("glass_flask")), 1, "one empty flask used");
		expect(ctx, count(he, mod("raw_pitchblende")), 64, "the pitchblende stays");
		check(ctx, !he.canExtract(0, he.getStack(0), net.minecraft.util.math.Direction.DOWN), "hoppers can't take the pitchblende");
		check(ctx, he.heliumPerTick(new ItemStack(mod("radium"))) > 20 * he.heliumPerTick(new ItemStack(mod("raw_pitchblende"))),
				"radium is far quicker than ore");
		check(ctx, he.heliumPerTick(new ItemStack(mod("thorium"))) < he.heliumPerTick(new ItemStack(mod("uranium"))), "thorium slower than uranium");
		check(ctx, he.heliumPerTick(new ItemStack(Items.IRON_INGOT)) == 0, "iron gives none");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void heliumCollectorStaysFullWithoutAFlask(TestContext ctx) {
		ctx.setBlockState(POS, block("helium_collector"));
		PowerBlockEntity he = ctx.getBlockEntity(POS);
		he.setStack(0, new ItemStack(mod("radium"), 8));
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int i = 0; i < 24_000; i++) {
			he.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, Math.abs(he.stored() - 1000.0 / 9) < 1e-6, "holds one flask's worth: " + he.stored());
		he.setStack(1, new ItemStack(mod("glass_flask"), 1));
		he.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		expect(ctx, count(he, mod("helium")), 1, "fills the flask as soon as there is one");
		ctx.complete();
	}

	// ------------------------------------------------------------ steel

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bessemerBlowsCastIronToSteel(TestContext ctx) {
		// 9 cast iron: 960 units of iron -> 8 steel; the carbon burns away.
		MachineBlockEntity be = run(ctx, "bessemer_converter", List.of("bessemer"),
				new ItemStack(mod("cast_iron_ingot"), 9), ItemStack.EMPTY, coke(), false, 40_000);
		expect(ctx, count(be, mod("steel_ingot")), 8, "steel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void hadfieldManganeseSteel(TestContext ctx) {
		// 9 iron (1000 units) with 2 manganese (222 units, 142 needed) -> 1142 units -> 10 ingots.
		MachineBlockEntity be = run(ctx, "bessemer_converter", List.of("bessemer", "manganese"),
				new ItemStack(Items.IRON_INGOT, 9), new ItemStack(mod("manganese"), 2), coke(), false, 40_000);
		expect(ctx, count(be, mod("manganese_steel_ingot")), 10, "manganese steel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pyrolusiteGivesManganese(TestContext ctx) {
		// 8 raw pyrolusite: 711 units MnO2 -> 449 units Mn -> 4.
		MachineBlockEntity be = run(ctx, "charcoal_blast_furnace", List.of("blast_furnace", "bessemer", "manganese"),
				new ItemStack(mod("raw_pyrolusite"), 8), ItemStack.EMPTY, coke(), false, 40_000);
		expect(ctx, count(be, mod("manganese")), 4, "manganese");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void tungstenFromWolframite(TestContext ctx) {
		// 8 raw wolframite: 622 units FeWO4 fused with soda -> 602 units Na2WO4 -> 5.
		MachineBlockEntity roast = run(ctx, "reverberatory_furnace", List.of("tungsten", "manganese", "bessemer", "solvay"),
				new ItemStack(mod("raw_wolframite"), 8), new ItemStack(mod("sodium_carbonate"), 4), charcoal(), false, 40_000);
		expect(ctx, count(roast, mod("sodium_tungstate")), 5, "sodium tungstate");
		// 5 Na2WO4 (556 units) in acid -> 438 units WO3 -> 3; then 3 WO3 with charcoal -> 264 units W -> 2.
		MachineBlockEntity acid = run(ctx, "digestion_vat", List.of("tungsten", "acid_digestion"),
				new ItemStack(mod("sodium_tungstate"), 5), new ItemStack(mod("hydrochloric_acid"), 2), charcoal(), false, 40_000);
		expect(ctx, count(acid, mod("tungsten_trioxide")), 3, "tungsten trioxide");
		MachineBlockEntity reduce = run(ctx, "reverberatory_furnace", List.of("tungsten"),
				new ItemStack(mod("tungsten_trioxide"), 3), ItemStack.EMPTY, charcoal(), false, 40_000);
		expect(ctx, count(reduce, mod("tungsten")), 2, "tungsten");
		ctx.complete();
	}

	// ------------------------------------------------------------ chemicals

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bayerDigestsBauxite(TestContext ctx) {
		// 8 raw bauxite: 533 units gibbsite -> 348 units alumina -> 3.
		MachineBlockEntity be = run(ctx, "bayer_digester", List.of("bayer_process"),
				new ItemStack(mod("raw_bauxite"), 8), new ItemStack(mod("sodium_hydroxide"), 1), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("aluminium_oxide")), 3, "alumina");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void solvayMakesSoda(TestContext ctx) {
		// 4 salt (444 units) with limestone -> 403 units soda ash -> 3; calcium chloride goes to the slag.
		MachineBlockEntity be = run(ctx, "solvay_tower", List.of("solvay"),
				new ItemStack(mod("sodium_chloride"), 4), new ItemStack(Items.CALCITE), ItemStack.EMPTY, false, 40_000);
		expect(ctx, count(be, mod("sodium_carbonate")), 3, "soda ash");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void contactProcessAcid(TestContext ctx) {
		// 2 sulfur (222 units) -> 680 units of sulfuric acid -> 6.
		MachineBlockEntity be = run(ctx, "contact_converter", List.of("contact_process"),
				new ItemStack(mod("sulfur"), 2), new ItemStack(Items.WATER_BUCKET), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("sulfuric_acid")), 6, "sulfuric acid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void fluorineFromFluorspar(TestContext ctx) {
		// 4 raw fluorite: 355 units CaF2 -> 182 units HF -> 1.
		MachineBlockEntity vat = run(ctx, "digestion_vat", List.of("fluorine", "acid_digestion"),
				new ItemStack(mod("raw_fluorite"), 4), new ItemStack(mod("sulfuric_acid"), 4), charcoal(), false, 40_000);
		expect(ctx, count(vat, mod("hydrogen_fluoride")), 1, "hydrogen fluoride");
		// 2 HF (222 units) -> 211 units fluorine -> 1.
		MachineBlockEntity cell = run(ctx, "moissan_cell", List.of("dynamo", "fluorine"),
				new ItemStack(mod("hydrogen_fluoride"), 2), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(cell, mod("fluorine")), 1, "fluorine");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void vanadiumThermite(TestContext ctx) {
		// 2 V2O5 (222 units) with aluminium -> 124 units vanadium -> 1.
		MachineBlockEntity be = run(ctx, "reduction_crucible", List.of("vanadium"),
				new ItemStack(mod("vanadium_pentoxide"), 2), new ItemStack(mod("aluminium"), 2), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("vanadium")), 1, "vanadium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void nickelByMondProcess(TestContext ctx) {
		// 8 raw pentlandite: 640 units -> 309 units NiO -> 2; 2 NiO -> 175 units nickel -> 1.
		MachineBlockEntity roast = run(ctx, "reverberatory_furnace", List.of("mond_process", "copper_smelting"),
				new ItemStack(mod("raw_pentlandite"), 8), ItemStack.EMPTY, charcoal(), false, 40_000);
		expect(ctx, count(roast, mod("nickel_oxide")), 2, "nickel oxide");
		MachineBlockEntity mond = run(ctx, "mond_reactor", List.of("mond_process"),
				new ItemStack(mod("nickel_oxide"), 2), ItemStack.EMPTY, coke(), false, 40_000);
		expect(ctx, count(mond, mod("nickel")), 1, "nickel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smokelessPowderFromWood(TestContext ctx) {
		// 2 oak logs: 1240 units of cellulose (nitric acid for 1447) -> 2273 units -> 20 smokeless powder.
		MachineBlockEntity be = run(ctx, "digestion_vat", List.of("smokeless_powder"),
				new ItemStack(Items.OAK_LOG, 2), new ItemStack(mod("nitric_acid"), 16), ItemStack.EMPTY, false, 40_000);
		expect(ctx, count(be, mod("smokeless_powder")), 20, "smokeless powder");
		ctx.complete();
	}

	// ------------------------------------------------------------ refining and traces

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void refineryLeavesAnodeSlime(TestContext ctx) {
		// 20 copper (2222 units): 90% back as copper -> 18, 10% as slime -> 2.
		MachineBlockEntity be = run(ctx, "electrolytic_refinery", List.of("dynamo", "electrolytic_refining"),
				new ItemStack(Items.COPPER_INGOT, 20), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(be, Items.COPPER_INGOT), 18, "refined copper");
		expect(ctx, count(be, mod("anode_slime")), 2, "anode slime");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void anodeSlimeGivesPreciousMetals(TestContext ctx) {
		// 9 slime (1000 units): silver 170 -> 1; rhodium 80 units waits in the buffer.
		MachineBlockEntity be = run(ctx, "crystallizer", List.of("electrolytic_refining", "platinum_metals", "platinum"),
				new ItemStack(mod("anode_slime"), 9), new ItemStack(mod("aqua_regia"), 4), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("silver")), 1, "silver");
		check(ctx, be.outputBuffers().getOrDefault("item:periodic_elements:rhodium", 0.0) > 70, "rhodium should collect");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void spectroscopeSplitsFlueDust(TestContext ctx) {
		// 5 flue dust (556 units): In2O3 278 -> 2, GeO2 250 -> 2.
		MachineBlockEntity be = run(ctx, "spectroscope_bench", List.of("spectroscopy", "indium", "germanium"),
				new ItemStack(mod("zinc_flue_dust"), 5), ItemStack.EMPTY, ItemStack.EMPTY, false, 40_000);
		expect(ctx, count(be, mod("indium_oxide")), 2, "indium oxide");
		expect(ctx, count(be, mod("germanium_dioxide")), 2, "germanium dioxide");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void germaniumReducedByHydrogen(TestContext ctx) {
		// 2 GeO2 (222 units) -> 154 units germanium -> 1.
		MachineBlockEntity be = run(ctx, "reduction_crucible", List.of("germanium"),
				new ItemStack(mod("germanium_dioxide"), 2), new ItemStack(mod("hydrogen"), 1), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("germanium")), 1, "germanium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void thoriumByPotassium(TestContext ctx) {
		// 2 ThO2 (222 units) -> 314 units ThCl4 -> 2; 2 ThCl4 with potassium -> 138 units thorium -> 1.
		MachineBlockEntity vat = run(ctx, "digestion_vat", List.of("thorium", "acid_digestion"),
				new ItemStack(mod("thorium_dioxide"), 2), new ItemStack(mod("hydrochloric_acid"), 2), charcoal(), false, 40_000);
		expect(ctx, count(vat, mod("thorium_chloride")), 2, "thorium chloride");
		MachineBlockEntity crucible = run(ctx, "reduction_crucible", List.of("thorium", "boron"),
				new ItemStack(mod("thorium_chloride"), 2), new ItemStack(mod("potassium"), 1), charcoal(), false, 40_000);
		expect(ctx, count(crucible, mod("thorium")), 1, "thorium");
		ctx.complete();
	}

	// ------------------------------------------------------------ radioactivity

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void radiumFromResidue(TestContext ctx) {
		// 2 residue: 202 units RaSO4 -> 186 units RaCl2 -> 1; 2 RaCl2 electrolysed -> 169 units radium -> 1.
		MachineBlockEntity cr = run(ctx, "crystallizer", List.of("radium", "radioactivity"),
				new ItemStack(mod("pitchblende_residue"), 2), new ItemStack(mod("hydrochloric_acid"), 1), charcoal(), false, 40_000);
		expect(ctx, count(cr, mod("radium_chloride")), 1, "radium chloride");
		MachineBlockEntity cell = run(ctx, "electrolysis_cell", List.of("radium", "electrolysis"),
				new ItemStack(mod("radium_chloride"), 2), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(cell, mod("radium")), 1, "radium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pitchblendeLeavesResidue(TestContext ctx) {
		// 48 raw pitchblende: 2% RaSO4 + 0.1% Po + 0.1% Ac = 117 units -> 1 residue; uranium oxide as before.
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("radium", "uranium_oxide"),
				new ItemStack(mod("raw_pitchblende"), 48), ItemStack.EMPTY, charcoal(), false, 80_000);
		expect(ctx, count(be, mod("pitchblende_residue")), 1, "pitchblende residue");
		check(ctx, count(be, mod("triuranium_octoxide")) >= 25, "uranium oxide: " + count(be, mod("triuranium_octoxide")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cherenkovGlowFollowsActivity(TestContext ctx) {
		java.util.function.ToDoubleBiFunction<String, Boolean> glow = (id, wet) ->
				com.periodicelements.radiation.Cherenkov.strength(Radioactivity.of(mod(id)), wet);
		// In air only the strongest emitters glow; uranium, thorium and pitchblende don't.
		for (String id : new String[]{"radium", "fission_products", "spent_fuel_rod", "einsteinium", "californium", "radon"}) {
			check(ctx, glow.applyAsDouble(id, false) > 0, id + " glows in air");
		}
		for (String id : new String[]{"uranium", "thorium", "plutonium", "pitchblende_ore", "polonium", "americium"}) {
			check(ctx, glow.applyAsDouble(id, false) == 0, id + " doesn't glow in air");
		}
		// In water more things glow, and brighter.
		for (String id : new String[]{"pitchblende_ore", "polonium", "americium", "actinium"}) {
			check(ctx, glow.applyAsDouble(id, true) > 0, id + " glows in water");
		}
		check(ctx, glow.applyAsDouble("uranium", true) == 0, "fresh uranium doesn't glow even in water");
		check(ctx, glow.applyAsDouble("radium", true) > glow.applyAsDouble("radium", false), "water glows brighter");
		check(ctx, glow.applyAsDouble("californium", false) > glow.applyAsDouble("radium", false), "brighter with activity");
		check(ctx, glow.applyAsDouble("radon_cylinder", true) <= 1, "capped at 1");
		// Running reactors glow; idle ones and ordinary machines don't.
		Block reactor = Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "nuclear_reactor"));
		var lit = reactor.getDefaultState().with(net.minecraft.state.property.Properties.LIT, true);
		check(ctx, com.periodicelements.radiation.Cherenkov.strength(com.periodicelements.radiation.Cherenkov.rate(lit), false) > 0,
				"running reactor glows");
		check(ctx, com.periodicelements.radiation.Cherenkov.rate(reactor.getDefaultState()) == 0, "idle reactor doesn't glow");
		Block kiln = Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "lime_kiln"));
		check(ctx, com.periodicelements.radiation.Cherenkov.rate(kiln.getDefaultState().with(net.minecraft.state.property.Properties.LIT, true)) == 0,
				"a lime kiln doesn't glow");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void radiumDoseRateIsReal(TestContext ctx) {
		// 111 g of radium: 8.25 mSv/h per gram at 1 m -> 0.92 Sv/h.
		double rate = Radioactivity.of(new ItemStack(mod("radium")));
		check(ctx, Math.abs(rate - 0.9166) < 0.01, "radium at 1 m: " + rate);
		check(ctx, Radioactivity.of(new ItemStack(Items.IRON_INGOT)) == 0, "iron is not radioactive");
		check(ctx, Radioactivity.of(new ItemStack(mod("raw_pitchblende"))) > 0.005, "pitchblende carries radium");
		// A chest 2 m away holding one radium item: 0.92 / 4 = 0.23 Sv/h.
		BlockPos chest = new BlockPos(1, 1, 1);
		ctx.setBlockState(chest, Blocks.CHEST);
		ChestBlockEntity c = ctx.getBlockEntity(chest);
		c.setStack(0, new ItemStack(mod("radium")));
		Vec3d at = Vec3d.ofCenter(ctx.getAbsolutePos(chest)).add(2, 0, 0);
		double near = Radiation.surroundings(ctx.getWorld(), at);
		check(ctx, Math.abs(near - 0.229) < 0.01, "radium in a chest 2 m away: " + near);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bodyRepairsDoseOverDays(TestContext ctx) {
		RadiationState state = RadiationState.get(ctx.getWorld().getServer());
		UUID who = UUID.randomUUID();
		long t0 = 1_000_000_000L;
		state.add(who, 4.0, t0);
		double later = state.dose(who, t0 + 72L * 3_600_000L).recent;
		check(ctx, Math.abs(later - 2.0) < 1e-6, "half repaired after 72 real hours: " + later);
		check(ctx, state.dose(who, t0 + 72L * 3_600_000L).lifetime == 4.0, "lifetime dose never falls");
		state.reset(who);
		ctx.complete();
	}
}
