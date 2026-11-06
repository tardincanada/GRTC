package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Level 3 (0.6.0): coke, crucible steel, gases, electricity, acids, and the new elements. */
public class LevelThreeTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);
	private static final BlockPos SIDE = new BlockPos(2, 1, 1);

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

	/** Ticks the machine (and a power block next to it) until its input is used up and it goes idle. */
	private static double[] tick(TestContext ctx, MachineBlockEntity be, PowerBlockEntity power, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		BlockPos side = ctx.getAbsolutePos(SIDE);
		double peak = 0;
		double cap = 0;
		int t = 0;
		for (; t < limit; t++) {
			if (power != null) {
				power.tick(ctx.getWorld(), side, ctx.getWorld().getBlockState(side));
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			peak = Math.max(peak, be.temperature());
			cap = Math.max(cap, be.cap());
			if (be.getStack(MachineBlockEntity.RESIDUE).getCount() >= 60) {
				be.setStack(MachineBlockEntity.RESIDUE, ItemStack.EMPTY);
			}
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT)) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, peak {} °C, status {}, outputs {} {} {} | residue {} | reagent {} | fuel {} | buffers {}{}",
				be.spec().id(), t, String.format(Locale.ROOT, "%.0f", peak), be.status(), be.getStack(2), be.getStack(3),
				be.getStack(4), be.getStack(5), be.getStack(6), be.getStack(MachineBlockEntity.FUEL), be.outputBuffers(),
				power == null ? "" : " | power " + power.describe().getString());
		return new double[]{t, peak, cap};
	}

	private static MachineBlockEntity run(TestContext ctx, String machine, List<String> projects, ItemStack input,
										  ItemStack reagent, ItemStack fuel, int limit) {
		MachineBlockEntity be = place(ctx, machine, projects);
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.REAGENT, reagent);
		be.setStack(MachineBlockEntity.FUEL, fuel);
		tick(ctx, be, null, limit);
		return be;
	}

	private static PowerBlockEntity power(TestContext ctx, String id, ItemStack... contents) {
		ctx.setBlockState(SIDE, block(id));
		PowerBlockEntity p = ctx.getBlockEntity(SIDE);
		for (int i = 0; i < contents.length; i++) {
			p.setStack(i, contents[i]);
		}
		return p;
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

	private static ItemStack charcoal() {
		return new ItemStack(Items.CHARCOAL, 64);
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cokeOvenBakesCoal(TestContext ctx) {
		// 9 coal: 800 units of fixed carbon -> 7 coke; the gas and tar are vented.
		MachineBlockEntity be = run(ctx, "coke_oven", List.of("coking"), new ItemStack(Items.COAL, 9),
				ItemStack.EMPTY, charcoal(), 40_000);
		expect(ctx, count(be, mod("coke")), 7, "coke");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void huntsmanMeltsCrucibleSteel(TestContext ctx) {
		// 9 blister steel -> 9 crucible steel; needs coke (1600 °C); charcoal can't reach 1500 °C.
		MachineBlockEntity be = run(ctx, "huntsman_furnace", List.of("coking", "crucible_steel"),
				new ItemStack(mod("steel_ingot"), 9), ItemStack.EMPTY, new ItemStack(mod("coke"), 64), 40_000);
		expect(ctx, count(be, mod("crucible_steel_ingot")), 9, "crucible steel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void huntsmanNeedsCoke(TestContext ctx) {
		MachineBlockEntity cold = run(ctx, "huntsman_furnace", List.of("coking", "crucible_steel"),
				new ItemStack(mod("steel_ingot"), 1), ItemStack.EMPTY, charcoal(), 4_000);
		expect(ctx, count(cold, mod("crucible_steel_ingot")), 0, "crucible steel from a charcoal fire");
		check(ctx, cold.status() == MachineBlockEntity.Status.HEATING || cold.status() == MachineBlockEntity.Status.NO_FUEL,
				"status " + cold.status());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void troughCollectsOxygenAndNitrogen(TestContext ctx) {
		// 9 saltpetre (1000 units) -> 466 K2O (4), 139 N (1), 396 O (3).
		MachineBlockEntity be = run(ctx, "pneumatic_trough", List.of("pneumatic_chemistry"),
				new ItemStack(mod("potassium_nitrate"), 9), ItemStack.EMPTY, charcoal(), 40_000);
		expect(ctx, count(be, mod("oxygen")), 3, "oxygen");
		expect(ctx, count(be, mod("nitrogen")), 1, "nitrogen");
		expect(ctx, count(be, mod("potassium_oxide")), 4, "potash");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void troughMakesHydrogenFromIron(TestContext ctx) {
		// 4 iron + sulfuric acid -> 16 units of hydrogen (it takes 31 iron per hydrogen item).
		MachineBlockEntity be = run(ctx, "pneumatic_trough", List.of("pneumatic_chemistry"),
				new ItemStack(Items.IRON_INGOT, 4), new ItemStack(mod("sulfuric_acid"), 8), ItemStack.EMPTY, 40_000);
		double h = be.outputBuffers().getOrDefault("hydrogen", 0.0);
		check(ctx, h > 15 && h < 17, "expected 16 units of hydrogen, got " + h);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pileDrivesSaltElectrolysis(TestContext ctx) {
		// 2 salt (222 units) -> 87 Na + 135 Cl -> 1 chlorine; about 6 zinc of current, and zinc oxide back.
		MachineBlockEntity be = place(ctx, "electrolysis_cell", List.of("voltaic_pile", "electrolysis"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("sodium_chloride"), 2));
		PowerBlockEntity pile = power(ctx, "voltaic_pile", new ItemStack(mod("zinc"), 32), new ItemStack(mod("sulfuric_acid"), 8));
		double[] r = tick(ctx, be, pile, 40_000);
		expect(ctx, count(be, mod("chlorine")), 1, "chlorine");
		double na = be.outputBuffers().getOrDefault("sodium", 0.0);
		check(ctx, na > 86 && na < 88, "expected 87 units of sodium, got " + na);
		check(ctx, r[1] >= 801, "the cell must melt the salt, peak " + r[1]);
		int zincLeft = pile.getStack(0).getCount();
		PeriodicElements.LOGGER.info("[gametest] salt electrolysis used {} zinc", 32 - zincLeft);
		// Most of the current goes into melting the salt: about 4 zinc for the electrolysis, the rest for heat.
		check(ctx, zincLeft <= 28 && zincLeft >= 8, "zinc used: " + (32 - zincLeft));
		check(ctx, PowerBlockTests.has(pile, mod("zinc_oxide")), "the pile should give back zinc oxide");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cellWithoutPowerWaits(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "electrolysis_cell", List.of("voltaic_pile", "electrolysis"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("sodium_chloride"), 2));
		be.setStack(MachineBlockEntity.FUEL, charcoal());
		tick(ctx, be, null, 1_000);
		check(ctx, be.status() == MachineBlockEntity.Status.NO_POWER, "status " + be.status());
		expect(ctx, be.getStack(MachineBlockEntity.FUEL).getCount(), 64, "charcoal (an electric cell burns no fuel)");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void moistLimeGivesCalcium(TestContext ctx) {
		// 4 quicklime (444 units) -> 318 units of calcium -> 2.
		MachineBlockEntity be = place(ctx, "electrolysis_cell", List.of("voltaic_pile", "electrolysis", "alkaline_earths"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("calcium_oxide"), 4));
		PowerBlockEntity pile = power(ctx, "voltaic_pile", new ItemStack(mod("zinc"), 64), new ItemStack(mod("sulfuric_acid"), 16));
		tick(ctx, be, pile, 60_000);
		expect(ctx, count(be, mod("calcium")), 2, "calcium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void steamEngineBlowsTheFurnaceHotter(TestContext ctx) {
		// Charcoal plus Bellows can reach 1350 °C; with a running Steam Engine next to it, 1550 °C.
		MachineBlockEntity be = place(ctx, "charcoal_blast_furnace", List.of("blast_furnace", "bellows", "steam_power"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.RAW_IRON, 9));
		be.setStack(MachineBlockEntity.FUEL, charcoal());
		PowerBlockEntity engine = power(ctx, "steam_engine", new ItemStack(Items.COAL, 64), new ItemStack(Items.WATER_BUCKET));
		double[] r = tick(ctx, be, engine, 6_000);
		check(ctx, r[2] >= 1549, "expected the fire to reach 1550 °C with the engine, got " + r[2]);
		check(ctx, engine.getStack(0).getCount() < 64, "the engine should have burned coal");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void digestionVatMakesBoricAcid(TestContext ctx) {
		// 8 raw borax: 533 units of borax -> 346 units of boric acid -> 3.
		MachineBlockEntity be = run(ctx, "digestion_vat", List.of("acid_digestion"), new ItemStack(mod("raw_borax"), 8),
				new ItemStack(mod("sulfuric_acid"), 4), charcoal(), 40_000);
		expect(ctx, count(be, mod("boric_acid")), 3, "boric acid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void potassiumReducesBoricAcid(TestContext ctx) {
		// 7 boric acid (778 units) + 1475 units of potassium -> 136 units of boron -> 1.
		MachineBlockEntity be = run(ctx, "reduction_crucible", List.of("boron", "bellows"), new ItemStack(mod("boric_acid"), 7),
				new ItemStack(mod("potassium"), 14), charcoal(), 40_000);
		expect(ctx, count(be, mod("boron")), 1, "boron");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void berylBecomesBeryllium(TestContext ctx) {
		// 64 emeralds: 2823 units of beryl -> 1259 BeCl2 (11); then 9 BeCl2 + potassium -> 113 Be (1).
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("beryllium", "bellows"), new ItemStack(Items.EMERALD, 64),
				new ItemStack(mod("chlorine"), 12), charcoal(), 120_000);
		expect(ctx, count(be, mod("beryllium_chloride")), 11, "beryllium chloride");
		MachineBlockEntity cr = run(ctx, "reduction_crucible", List.of("beryllium", "boron", "bellows"),
				new ItemStack(mod("beryllium_chloride"), 9), new ItemStack(mod("potassium"), 9), charcoal(), 40_000);
		expect(ctx, count(cr, mod("beryllium")), 1, "beryllium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void leadChamberMakesSulfuricAcid(TestContext ctx) {
		// 8 sulfur (889 units) + water -> 2719 units of sulfuric acid (24) and a little sludge.
		MachineBlockEntity be = run(ctx, "lead_chamber", List.of("lead_chamber"), new ItemStack(mod("sulfur"), 8),
				new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.OAK_LOG, 16), 80_000);
		expect(ctx, count(be, mod("sulfuric_acid")), 24, "sulfuric acid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void sludgeGivesSelenium(TestContext ctx) {
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("selenium", "bellows"), new ItemStack(mod("chamber_sludge"), 8),
				ItemStack.EMPTY, charcoal(), 40_000);
		expect(ctx, count(be, mod("selenium")), 1, "selenium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void chlorineFreesBromineFromBittern(TestContext ctx) {
		// 12 bittern: 533 units of MgBr2 (40% since 0.7.0) -> 463 Br -> 4.
		MachineBlockEntity be = run(ctx, "alembic", List.of("bromine", "alembic_distillation"), new ItemStack(mod("bittern"), 12),
				new ItemStack(mod("chlorine"), 2), charcoal(), 40_000);
		expect(ctx, count(be, mod("bromine")), 4, "bromine");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void kelpGivesIodine(TestContext ctx) {
		// 32 dried kelp: 178 units of NaI -> 150 I -> 1.
		MachineBlockEntity be = run(ctx, "alembic", List.of("iodine", "alembic_distillation"), new ItemStack(Items.DRIED_KELP, 32),
				new ItemStack(mod("sulfuric_acid"), 8), charcoal(), 60_000);
		expect(ctx, count(be, mod("iodine")), 1, "iodine");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void zincRetortCatchesCadmium(TestContext ctx) {
		// 64 raw sphalerite: 427 units of CdS (6% since 1.4.1) -> 332 Cd -> 2, before the zinc.
		MachineBlockEntity be = run(ctx, "zinc_retort", List.of("zinc_distillation", "cadmium", "bellows"),
				new ItemStack(mod("raw_sphalerite"), 64), ItemStack.EMPTY, charcoal(), 120_000);
		expect(ctx, count(be, mod("cadmium")), 2, "cadmium");
		check(ctx, count(be, mod("zinc")) > 30, "zinc too");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void aquaRegiaGivesPlatinum(TestContext ctx) {
		// 64 raw gold: 356 units of platinum grains (5% since 1.4.1) -> 3.
		MachineBlockEntity be = run(ctx, "alembic", List.of("alembic_distillation", "mineral_acids", "aqua_regia", "platinum"),
				new ItemStack(Items.RAW_GOLD, 64), new ItemStack(mod("aqua_regia"), 8), ItemStack.EMPTY, 60_000);
		expect(ctx, count(be, mod("platinum")), 3, "platinum");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bariteRoastsToBaryta(TestContext ctx) {
		// 8 raw barite: 545 units of BaSO4 -> 358 BaO -> 3.
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("alkaline_earths", "bellows"),
				new ItemStack(mod("raw_barite"), 8), ItemStack.EMPTY, charcoal(), 60_000);
		expect(ctx, count(be, mod("barium_oxide")), 3, "baryta");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void spodumeneLeachesToLithiumChloride(TestContext ctx) {
		// 8 raw spodumene: 667 units -> 152 LiCl -> 1.
		MachineBlockEntity be = run(ctx, "digestion_vat", List.of("acid_digestion", "lithium"),
				new ItemStack(mod("raw_spodumene"), 8), new ItemStack(mod("hydrochloric_acid"), 4), charcoal(), 40_000);
		expect(ctx, count(be, mod("lithium_chloride")), 1, "lithium chloride");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pitchblendeRoastsToUraniumOxide(TestContext ctx) {
		// 4 raw pitchblende: 267 units of UO2 -> 277 U3O8 -> 2.
		MachineBlockEntity be = run(ctx, "reverberatory_furnace", List.of("uranium_oxide", "bellows"),
				new ItemStack(mod("raw_pitchblende"), 4), ItemStack.EMPTY, charcoal(), 40_000);
		expect(ctx, count(be, mod("triuranium_octoxide")), 2, "uranium oxide");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void levelThreeGear(TestContext ctx) {
		var recipes = ctx.getWorld().getRecipeManager();
		for (String id : List.of("crucible_steel_pickaxe", "crucible_steel_chestplate", "flintlock_musket",
				"natural_philosophers_laboratory", "voltaic_pile", "steam_engine", "electrolysis_cell")) {
			check(ctx, recipes.get(Identifier.of(PeriodicElements.MOD_ID, id)).isPresent(), "missing recipe " + id);
		}
		check(ctx, new ItemStack(mod("crucible_steel_pickaxe")).getMaxDamage() == 900, "crucible steel pickaxe durability");
		check(ctx, new ItemStack(mod("flintlock_musket")).getMaxDamage() == 300, "musket durability");
		ctx.complete();
	}
}
