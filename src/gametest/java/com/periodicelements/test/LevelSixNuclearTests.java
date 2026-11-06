package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlock;
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

import com.periodicelements.radiation.Decay;
import com.periodicelements.radiation.Radiation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import com.periodicelements.PenetratorArrowItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Level 6, second half (0.9.5): enrichment, the reactor, reprocessing, the cyclotron, decay, penetrators. */
public class LevelSixNuclearTests implements FabricGameTest {
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


	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void uraniumIsEnriched(TestContext ctx) {
		// 4 UF4 (444 units) with fluorine -> 498 units UF6 -> 4.
		MachineBlockEntity cl = run(ctx, "chlorinator", List.of("kroll_process", "isotope_separation"),
				of("uranium_tetrafluoride", 4), of("fluorine", 1), coke(), false, 40_000);
		expect(ctx, count(cl, mod("uranium_hexafluoride")), 4, "uranium hexafluoride");
		// 14 UF6 (1556 units) -> 14.5% enriched (226 units, 2) and 85.5% depleted (1330 units, 11).
		MachineBlockEntity cascade = run(ctx, "enrichment_cascade", List.of("dynamo", "isotope_separation"),
				of("uranium_hexafluoride", 14), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(cascade, mod("enriched_uranium_hexafluoride")), 2, "enriched UF6");
		expect(ctx, count(cascade, mod("depleted_uranium_hexafluoride")), 11, "depleted UF6");
		// 4 depleted UF6 (444 units) with magnesium -> 300 units uranium -> 2 ingots.
		MachineBlockEntity red = run(ctx, "reduction_crucible", List.of("isotope_separation"),
				of("depleted_uranium_hexafluoride", 4), of("magnesium", 1), charcoal(), false, 40_000);
		expect(ctx, count(red, mod("depleted_uranium_ingot")), 2, "depleted uranium ingots");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void cascadeShowsActiveWhileEnriching(TestContext ctx) {
		// 3.28.2: the cascade's control panel and compressor animate only while UF6 is being enriched.
		MachineBlockEntity be = place(ctx, "enrichment_cascade", List.of("dynamo", "isotope_separation"));
		List<PowerBlockEntity> plant = powerPlant(ctx);
		BlockPos abs = ctx.getAbsolutePos(POS);
		tick(ctx, be, plant, 200, false);
		check(ctx, !ctx.getWorld().getBlockState(abs).get(MachineBlock.ACTIVE), "powered cascade with no gas shows active");
		be.setStack(MachineBlockEntity.INPUT, of("uranium_hexafluoride", 7));
		int active = 0;
		for (int t = 0; t < 40_000 && !(be.getStack(MachineBlockEntity.INPUT).isEmpty() && t > 100
				&& be.status() == MachineBlockEntity.Status.IDLE); t++) {
			tick(ctx, be, plant, 1, false);
			if (ctx.getWorld().getBlockState(abs).get(MachineBlock.ACTIVE)) {
				active++;
			}
		}
		check(ctx, active > 100, "cascade was active for only " + active + " ticks while enriching");
		tick(ctx, be, plant, 30, false);
		check(ctx, !ctx.getWorld().getBlockState(abs).get(MachineBlock.ACTIVE), "cascade still active after the gas ran out");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void fuelPelletsAndZircaloy(TestContext ctx) {
		// 4 enriched UF6 (444 units) and steam -> 341 units UO2 -> 3.
		MachineBlockEntity conv = run(ctx, "chlorinator", List.of("kroll_process", "nuclear_reactor"),
				of("enriched_uranium_hexafluoride", 4), new ItemStack(Items.WATER_BUCKET), coke(), false, 40_000);
		expect(ctx, count(conv, mod("enriched_uranium_dioxide")), 3, "enriched uranium dioxide");
		ctx.setBlockState(POS, Blocks.AIR);
		MachineBlockEntity sinter = run(ctx, "sintering_furnace", List.of("dynamo", "nuclear_reactor"),
				of("enriched_uranium_dioxide", 3), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(sinter, mod("uranium_fuel_pellet")), 3, "fuel pellets");
		ctx.setBlockState(POS, Blocks.AIR);
		// 4 zirconium with tin -> 451 units zircaloy -> 4.
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "nuclear_reactor"),
				of("zirconium", 4), of("tin", 1), ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(arc, mod("zircaloy_ingot")), 4, "zircaloy");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void reactorBurnsRodsAndBreedsPlutonium(TestContext ctx) {
		ctx.setBlockState(POS, block("nuclear_reactor"));
		PowerBlockEntity reactor = ctx.getBlockEntity(POS);
		BlockPos abs = ctx.getAbsolutePos(POS);
		reactor.setStack(0, of("uranium_fuel_rod", 1));
		reactor.setStack(1, of("depleted_uranium_ingot", 2));
		for (int i = 0; i < 100; i++) {
			reactor.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, reactor.stored() == 0, "a dry reactor must not run");
		ctx.setBlockState(POS.east(), Blocks.WATER);
		for (int i = 0; i < 2400; i++) {
			reactor.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, reactor.stored() > 1000, "reactor should store current: " + reactor.stored());
		expect(ctx, countIn(reactor, mod("plutonium")), 1, "plutonium bred from depleted uranium");
		expect(ctx, countIn(reactor, mod("depleted_uranium_ingot")), 1, "depleted uranium left");
		for (int i = 0; i < 72_000; i++) {
			reactor.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		expect(ctx, countIn(reactor, mod("spent_fuel_rod")), 1, "spent fuel rod");
		expect(ctx, countIn(reactor, mod("uranium_fuel_rod")), 0, "fresh rods");
		check(ctx, countIn(reactor, mod("curium")) >= 1, "plutonium should go on to americium and curium");
		check(ctx, reactor.canExtract(0, of("spent_fuel_rod", 1), net.minecraft.util.math.Direction.DOWN)
				&& !reactor.canExtract(0, of("plutonium", 1), net.minecraft.util.math.Direction.DOWN), "hoppers take only spent rods");
		ctx.complete();
	}

	private static int countIn(Inventory inv, Item item) {
		int n = 0;
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(item)) {
				n += inv.getStack(i).getCount();
			}
		}
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void hotCellReprocessesSpentFuel(TestContext ctx) {
		// 9 rods (4000 units): uranium dioxide 2412 -> 21, plutonium dioxide 120 -> 1, waste 1152 -> 10; neptunium 160 units collects.
		MachineBlockEntity cell = run(ctx, "hot_cell", List.of("dynamo", "reprocessing", "neptunium", "technetium",
						"promethium", "americium_curium"),
				of("spent_fuel_rod", 9), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(cell, mod("uranium_dioxide")), 21, "recovered uranium dioxide");
		check(ctx, count(cell, mod("neptunium_dioxide")) >= 1, "neptunium dioxide: about 7 rods per item since 1.4.1");
		expect(ctx, count(cell, mod("plutonium_dioxide")), 1, "plutonium dioxide");
		expect(ctx, count(cell, mod("high_level_waste")), 10, "high-level waste");
		check(ctx, cell.outputBuffers().getOrDefault("item:periodic_elements:technetium", 0.0) > 15, "technetium should collect");
		// 4 PuO2 (444 units) with calcium -> 392 units plutonium -> 3.
		MachineBlockEntity red = run(ctx, "reduction_crucible", List.of("reprocessing"),
				of("plutonium_dioxide", 4), of("calcium", 2), charcoal(), false, 40_000);
		expect(ctx, count(red, mod("plutonium")), 3, "plutonium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void cyclotronMakesHeavyElements(TestContext ctx) {
		MachineBlockEntity cyc = run(ctx, "cyclotron", List.of("dynamo", "cyclotron", "berkelium_californium"),
				of("americium", 4), of("helium", 1), ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(cyc, mod("berkelium_sample")), 4, "berkelium samples");
		expect(ctx, count(cyc, mod("americium")), 3, "americium targets back");
		ctx.setBlockState(POS, Blocks.AIR);
		MachineBlockEntity md = run(ctx, "cyclotron", List.of("dynamo", "cyclotron", "mendelevium"),
				of("einsteinium_sample", 10), of("helium", 1), ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(md, mod("mendelevium_sample")), 1, "mendelevium sample");
		expect(ctx, count(md, mod("einsteinium_sample")), 9, "einsteinium samples back");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void samplesDecayOnTheGameClock(TestContext ctx) {
		ItemStack es = of("einsteinium_sample", 64);
		Random random = Random.create(1);
		expect(ctx, Decay.update(es, 0, random), 0, "decays on first stamp");
		long halfLife = Decay.of(mod("einsteinium_sample")).halfLifeTicks();
		int one = Decay.update(es, halfLife, random);
		check(ctx, one > 16 && one < 48, "about half should decay in one half-life: " + one);
		int rest = Decay.update(es, 30 * halfLife, random);
		expect(ctx, one + rest, 64, "all decayed after 30 half-lives");
		check(ctx, es.isEmpty(), "stack should be gone");
		check(ctx, Decay.of(mod("einsteinium_sample")).daughter() == mod("berkelium_sample"), "Es-253 decays to Bk-249");
		check(ctx, Decay.of(mod("polonium")) != null && Decay.of(mod("plutonium")) == null, "polonium decays, plutonium lasts");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void hotCellShieldsItsContents(TestContext ctx) {
		Vec3d at = Vec3d.ofCenter(ctx.getAbsolutePos(POS.east(2)));
		ctx.setBlockState(POS, Blocks.CHEST);
		Inventory chest = ctx.getBlockEntity(POS);
		chest.setStack(0, of("spent_fuel_rod", 8));
		double open = Radiation.surroundings(ctx.getWorld(), at);
		check(ctx, open > 0.1, "spent fuel in a chest should be hot: " + open);
		chest.setStack(0, ItemStack.EMPTY);
		MachineBlockEntity cell = place(ctx, "hot_cell", List.of());
		cell.setStack(MachineBlockEntity.INPUT, of("spent_fuel_rod", 8));
		double shielded = Radiation.surroundings(ctx.getWorld(), at);
		check(ctx, shielded < 1e-9, "the hot cell should shield it: " + shielded);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void penetratorArrows(TestContext ctx) {
		ItemStack du = of("depleted_uranium_arrow", 1);
		check(ctx, du.isIn(ItemTags.ARROWS) && of("tungsten_carbide_arrow", 1).isIn(ItemTags.ARROWS), "bows should fire them");
		ZombieEntity shooter = ctx.spawnEntity(EntityType.ZOMBIE, POS.up());
		PersistentProjectileEntity arrow = ((PenetratorArrowItem) mod("depleted_uranium_arrow"))
				.createArrow(ctx.getWorld(), du, shooter, null);
		check(ctx, Math.abs(arrow.getDamage() - 3.5) < 1e-6, "DU arrow damage: " + arrow.getDamage());
		check(ctx, arrow.isOnFire(), "DU arrows burn");
		ctx.complete();
	}
}
