package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
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
import java.util.Locale;
import java.util.UUID;

/** Level 5 (0.8.0): arc furnace alloys, Haber and Ostwald, flotation, carbide, coltan, zirconium and hafnium, rare earths. */
public class LevelFiveTests implements FabricGameTest {
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

	private static ItemStack of(String id, int n) {
		return new ItemStack(mod(id), n);
	}

	// ------------------------------------------------------------ arc furnace

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void arcFurnaceMakesSilicon(TestContext ctx) {
		// 8 quartz: 889 units SiO2 -> 415 units silicon -> 3.
		MachineBlockEntity be = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace"),
				new ItemStack(Items.QUARTZ, 8), ItemStack.EMPTY, coke(), true, 40_000);
		expect(ctx, count(be, mod("silicon")), 3, "silicon");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void arcFurnaceMakesFerrochrome(TestContext ctx) {
		// 8 raw chromite: 667 units FeCr2O4 -> 476 units ferrochrome -> 4.
		MachineBlockEntity be = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace"),
				of("raw_chromite", 8), ItemStack.EMPTY, coke(), true, 40_000);
		expect(ctx, count(be, mod("ferrochrome")), 4, "ferrochrome");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void stainlessSteelFromFerrochrome(TestContext ctx) {
		// 9 iron (1000 units) with ferrochrome: 12.8% chromium.
		MachineBlockEntity be = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "stainless_steel"),
				new ItemStack(Items.IRON_INGOT, 9), of("ferrochrome", 2), ItemStack.EMPTY, true, 40_000);
		check(ctx, count(be, mod("stainless_steel_ingot")) >= 10, "stainless steel: " + count(be, mod("stainless_steel_ingot")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void highSpeedSteelFromTungstenSteel(TestContext ctx) {
		// 9 tungsten steel (Fe 890, W 90) with molybdenum -> 1055 units -> 9.
		MachineBlockEntity be = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "high_speed_steel"),
				of("tungsten_steel_ingot", 9), of("molybdenum", 4), ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(be, mod("high_speed_steel_ingot")), 9, "high-speed steel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void chromiumByGoldschmidt(TestContext ctx) {
		// 8 raw chromite (667 units) roasted with soda -> 965 units sodium chromate -> 8.
		MachineBlockEntity roast = run(ctx, "reverberatory_furnace", List.of("chromium", "arc_furnace"),
				of("raw_chromite", 8), of("sodium_carbonate", 8), charcoal(), false, 40_000);
		expect(ctx, count(roast, mod("sodium_chromate")), 8, "sodium chromate");
		// 4 sodium chromate (444 units) with sulfur -> 208 units Cr2O3 -> 1.
		MachineBlockEntity fire = run(ctx, "reverberatory_furnace", List.of("chromium"),
				of("sodium_chromate", 4), of("sulfur", 1), charcoal(), false, 40_000);
		expect(ctx, count(fire, mod("chromium_oxide")), 1, "chromium oxide");
		// 2 Cr2O3 (222 units) with aluminium -> 152 units chromium -> 1.
		MachineBlockEntity thermite = run(ctx, "reduction_crucible", List.of("chromium"),
				of("chromium_oxide", 2), of("aluminium", 1), charcoal(), false, 40_000);
		expect(ctx, count(thermite, mod("chromium")), 1, "chromium");
		ctx.complete();
	}

	// ------------------------------------------------------------ nitrogen

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void haberMakesAmmonia(TestContext ctx) {
		// 4 nitrogen (444 units) with hydrogen -> 540 units ammonia -> 4.
		MachineBlockEntity be = run(ctx, "high_pressure_reactor", List.of("haber_process"),
				of("nitrogen", 4), of("hydrogen", 1), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("ammonia")), 4, "ammonia");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void ostwaldMakesNitricAcid(TestContext ctx) {
		// 2 ammonia (222 units) burned in air -> 822 units nitric acid -> 7.
		MachineBlockEntity be = run(ctx, "ostwald_converter", List.of("ostwald_process"),
				of("ammonia", 2), ItemStack.EMPTY, charcoal(), false, 40_000);
		expect(ctx, count(be, mod("nitric_acid")), 7, "nitric acid");
		ctx.complete();
	}

	// ------------------------------------------------------------ molybdenum and rhenium

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void molybdenumFromMolybdenite(TestContext ctx) {
		// 8 raw molybdenite: 356 units of sulfides float off -> 3 concentrate.
		MachineBlockEntity cell = run(ctx, "flotation_cell", List.of("dynamo", "flotation"),
				of("raw_molybdenite", 8), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(cell, mod("molybdenite_concentrate")), 3, "molybdenite concentrate");
		// 4 concentrate (MoS2 422 units) -> 380 units MoO3 -> 3; the rhenium goes up the flue.
		MachineBlockEntity roast = run(ctx, "reverberatory_furnace", List.of("flotation", "molybdenum", "rhenium"),
				of("molybdenite_concentrate", 4), ItemStack.EMPTY, charcoal(), false, 40_000);
		expect(ctx, count(roast, mod("molybdenum_trioxide")), 3, "molybdenum trioxide");
		check(ctx, roast.outputBuffers().getOrDefault("item:periodic_elements:molybdenite_flue_dust", 0.0) > 15,
				"flue dust should collect");
		// 4 MoO3 (444 units) with hydrogen -> 296 units molybdenum -> 2.
		MachineBlockEntity reduce = run(ctx, "reduction_crucible", List.of("molybdenum"),
				of("molybdenum_trioxide", 4), of("hydrogen", 1), charcoal(), false, 40_000);
		expect(ctx, count(reduce, mod("molybdenum")), 2, "molybdenum");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void rheniumFromFlueDust(TestContext ctx) {
		// 2 flue dust: 178 units Re2O7 -> 137 units rhenium -> 1.
		MachineBlockEntity be = run(ctx, "reduction_crucible", List.of("molybdenum", "rhenium"),
				of("molybdenite_flue_dust", 2), of("hydrogen", 1), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("rhenium")), 1, "rhenium");
		ctx.complete();
	}

	// ------------------------------------------------------------ tool alloys

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void cementedCarbideSinters(TestContext ctx) {
		// 4 tungsten (444 units) + carbon + cobalt -> 526 units -> 4.
		MachineBlockEntity be = run(ctx, "sintering_furnace", List.of("dynamo", "cemented_carbide"),
				of("tungsten", 4), of("cobalt", 1), coke(), true, 40_000);
		expect(ctx, count(be, mod("cemented_carbide")), 4, "cemented carbide");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void berylliumCopper(TestContext ctx) {
		// 9 copper (1000 units) + 20 units beryllium -> 1020 units -> 9.
		MachineBlockEntity be = run(ctx, "crucible", List.of("beryllium_copper", "bellows"),
				new ItemStack(Items.COPPER_INGOT, 9), of("beryllium", 1), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("beryllium_copper_ingot")), 9, "beryllium copper");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void levelFiveGearExists(TestContext ctx) {
		for (String id : List.of("stainless_steel_chestplate", "stainless_steel_pickaxe", "high_speed_steel_pickaxe",
				"cemented_carbide_pickaxe", "beryllium_copper_pickaxe", "geiger_counter")) {
			check(ctx, mod(id) != Items.AIR, id + " should be registered");
		}
		check(ctx, mod("cemented_carbide_pickaxe").getDefaultStack().getMaxDamage() == 1200, "carbide pickaxe durability");
		ctx.complete();
	}

	// ------------------------------------------------------------ coltan

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void niobiumAndTantalumFromColtan(TestContext ctx) {
		// 8 raw coltan: columbite 267 -> 210 Nb2O5 -> 1; tantalite 178 -> 153 Ta2O5 -> 1.
		MachineBlockEntity cr = run(ctx, "crystallizer", List.of("niobium", "tantalum"),
				of("raw_coltan", 8), of("hydrogen_fluoride", 4), charcoal(), false, 40_000);
		expect(ctx, count(cr, mod("niobium_pentoxide")), 1, "niobium pentoxide");
		expect(ctx, count(cr, mod("tantalum_pentoxide")), 1, "tantalum pentoxide");
		// 2 Ta2O5 (222 units) -> 182 units tantalum -> 1.
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "tantalum", "niobium"),
				of("tantalum_pentoxide", 2), ItemStack.EMPTY, coke(), true, 40_000);
		expect(ctx, count(arc, mod("tantalum")), 1, "tantalum");
		ctx.complete();
	}

	// ------------------------------------------------------------ zirconium and hafnium

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void zirconiumCrystalBar(TestContext ctx) {
		// 32 concentrate: 533 units zircon -> 369 units crude zirconia -> 3.
		MachineBlockEntity fuse = run(ctx, "reverberatory_furnace", List.of("van_arkel"),
				of("heavy_mineral_concentrate", 32), of("sodium_hydroxide", 4), charcoal(), false, 80_000);
		expect(ctx, count(fuse, mod("crude_zirconia")), 3, "crude zirconia");
		// 2 crude zirconia (ZrO2 216 units) with calcium -> 168 units crude zirconium -> 1.
		MachineBlockEntity reduce = run(ctx, "reduction_crucible", List.of("van_arkel", "boron"),
				of("crude_zirconia", 2), of("calcium", 2), charcoal(), false, 40_000);
		expect(ctx, count(reduce, mod("crude_zirconium")), 1, "crude zirconium");
		// 2 crude zirconium (Zr 211 units) -> 1 zirconium.
		MachineBlockEntity bar = run(ctx, "van_arkel_reactor", List.of("dynamo", "van_arkel"),
				of("crude_zirconium", 2), of("iodine", 1), ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(bar, mod("zirconium")), 1, "zirconium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void hafniumHidesInZirconia(TestContext ctx) {
		// 64 crude zirconia: 213 units HfO2 -> 258 units HfF4 -> 2.
		MachineBlockEntity cr = run(ctx, "fractional_crystallizer", List.of("rare_earths", "hafnium"),
				of("crude_zirconia", 64), of("hydrogen_fluoride", 2), charcoal(), false, 80_000);
		expect(ctx, count(cr, mod("hafnium_tetrafluoride")), 2, "hafnium tetrafluoride");
		// 2 HfF4 (222 units) with calcium -> 164 units crude hafnium -> 1; then 1 hafnium.
		MachineBlockEntity reduce = run(ctx, "reduction_crucible", List.of("hafnium", "boron"),
				of("hafnium_tetrafluoride", 2), of("calcium", 1), charcoal(), false, 40_000);
		expect(ctx, count(reduce, mod("crude_hafnium")), 1, "crude hafnium");
		ctx.complete();
	}

	// ------------------------------------------------------------ rare earths

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void rareEarthsFromMonazite(TestContext ctx) {
		// 32 concentrate: 284 units monazite -> 344 units sulfates -> 3.
		MachineBlockEntity vat = run(ctx, "digestion_vat", List.of("rare_earths", "acid_digestion"),
				of("heavy_mineral_concentrate", 32), of("sulfuric_acid", 2), charcoal(), false, 80_000);
		expect(ctx, count(vat, mod("rare_earth_sulfates")), 3, "rare earth sulfates");
		// 8 sulfates: Ce2(SO4)3 427 -> 258 CeO2 -> 2; La2(SO4)3 222 -> 128 La2O3 -> 1.
		MachineBlockEntity cr = run(ctx, "fractional_crystallizer", List.of("rare_earths", "lanthanum", "didymium"),
				of("rare_earth_sulfates", 8), ItemStack.EMPTY, charcoal(), false, 40_000);
		expect(ctx, count(cr, mod("cerium_dioxide")), 2, "ceria");
		expect(ctx, count(cr, mod("lanthanum_oxide")), 1, "lanthana");
		check(ctx, cr.outputBuffers().getOrDefault("neodymium_oxide", 0.0) > 50, "neodymium oxide should collect");
		// 2 CeO2 (222 units) with calcium -> 181 units cerium -> 1.
		MachineBlockEntity reduce = run(ctx, "reduction_crucible", List.of("rare_earths", "boron"),
				of("cerium_dioxide", 2), of("calcium", 1), charcoal(), false, 40_000);
		expect(ctx, count(reduce, mod("cerium")), 1, "cerium");
		ctx.complete();
	}

	// ------------------------------------------------------------ radioactive

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void protactiniumFromResidue(TestContext ctx) {
		// 26 residue: 15% protactinium = 433 units -> 3.
		MachineBlockEntity be = run(ctx, "crystallizer", List.of("protactinium"),
				of("pitchblende_residue", 26), ItemStack.EMPTY, charcoal(), false, 40_000);
		expect(ctx, count(be, mod("protactinium")), 3, "protactinium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void franciumRidesOnCaesium(TestContext ctx) {
		// 10 actinium (1111 units) with caesium chloride -> 111 units francium precipitate -> 1; the actinium comes back.
		MachineBlockEntity be = run(ctx, "crystallizer", List.of("francium"),
				of("actinium", 10), of("caesium_chloride", 1), charcoal(), false, 40_000);
		expect(ctx, count(be, mod("francium_precipitate")), 1, "francium precipitate");
		check(ctx, count(be, mod("actinium")) >= 9, "actinium back: " + count(be, mod("actinium")));
		ctx.complete();
	}
}
