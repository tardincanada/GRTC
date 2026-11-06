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

import com.periodicelements.ResearchedToolItem;
import com.periodicelements.SmokeDetectorBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 1.2.0: oil shale, refining, coal liquefaction, cracking, plastics, nylon, real aramid, and what they make. */
public class PetrochemicalTests implements FabricGameTest {
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


	private static ItemStack coal() {
		return new ItemStack(Items.COAL, 64);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void shaleRetortsAndCrudeDistils(TestContext ctx) {
		// 16 raw oil shale (1778 units): 40% kerogen (711) -> 462 units crude oil -> 4 crude oil.
		MachineBlockEntity retort = run(ctx, "alembic", List.of("alembic_distillation", "oil_refining"),
				of("raw_oil_shale", 16), ItemStack.EMPTY, coal(), false, 80_000);
		expect(ctx, count(retort, mod("crude_oil")), 4, "crude oil");
		ctx.setBlockState(POS, Blocks.AIR);
		// 9 crude oil (1000 units) -> 32% diesel (320), 25% bitumen (250), 22% naphtha (220), 15% kerosene (150).
		MachineBlockEntity still = run(ctx, "refinery_still", List.of("oil_refining"),
				of("crude_oil", 9), ItemStack.EMPTY, coal(), false, 80_000);
		expect(ctx, count(still, mod("diesel")), 2, "diesel");
		expect(ctx, count(still, mod("bitumen")), 2, "bitumen (the still's residue)");
		expect(ctx, count(still, mod("naphtha")), 1, "naphtha");
		expect(ctx, count(still, mod("kerosene")), 1, "kerosene");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void coalLiquefies(TestContext ctx) {
		// 16 coal (1778 units): 1422 units of carbon take hydrogen -> 831 naphtha and 831 diesel.
		MachineBlockEntity hpr = run(ctx, "high_pressure_reactor", List.of("haber_process", "coal_liquefaction"),
				new ItemStack(Items.COAL, 16), of("hydrogen", 4), charcoal(), false, 80_000);
		expect(ctx, count(hpr, mod("naphtha")), 7, "naphtha");
		expect(ctx, count(hpr, mod("diesel")), 7, "diesel");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void crackingAndReforming(TestContext ctx) {
		// 8 diesel (889 units) -> 400 gasoline, 196 ethylene.
		MachineBlockEntity cracker = run(ctx, "cracking_unit", List.of("oil_refining", "thermal_cracking"),
				of("diesel", 8), ItemStack.EMPTY, coal(), false, 80_000);
		expect(ctx, count(cracker, mod("gasoline")), 3, "gasoline from cracking");
		expect(ctx, count(cracker, mod("ethylene")), 1, "ethylene");
		ctx.setBlockState(POS, Blocks.AIR);
		// 8 naphtha (889 units) -> 400 gasoline, 178 benzene, 178 p-xylene.
		MachineBlockEntity reformer = run(ctx, "cracking_unit", List.of("oil_refining", "thermal_cracking", "catalytic_reforming"),
				of("naphtha", 8), ItemStack.EMPTY, coal(), false, 80_000);
		expect(ctx, count(reformer, mod("gasoline")), 3, "gasoline from reforming");
		expect(ctx, count(reformer, mod("benzene")), 1, "benzene");
		expect(ctx, count(reformer, mod("xylene")), 1, "p-xylene");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void polyethyleneAndPvc(TestContext ctx) {
		List<String> projects = List.of("haber_process", "thermal_cracking", "polyethylene", "pvc");
		MachineBlockEntity pe = run(ctx, "high_pressure_reactor", projects, of("ethylene", 5), ItemStack.EMPTY, charcoal(), false, 80_000);
		expect(ctx, count(pe, mod("polyethylene")), 4, "polyethylene (and most of a fifth in the buffer)");
		ctx.setBlockState(POS, Blocks.AIR);
		// 2 ethylene (222 units) with chlorine -> 496 PVC and 290 hydrochloric acid.
		MachineBlockEntity pvc = run(ctx, "high_pressure_reactor", projects, of("ethylene", 2), of("chlorine", 8), charcoal(), false, 80_000);
		expect(ctx, count(pvc, mod("polyvinyl_chloride")), 4, "PVC");
		expect(ctx, count(pvc, mod("hydrochloric_acid")), 2, "hydrochloric acid");
		expect(ctx, count(pvc, mod("polyethylene")), 0, "polyethylene while chlorine is there");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void nylonAndAramidFromOil(TestContext ctx) {
		List<String> projects = List.of("haber_process", "catalytic_reforming", "nylon", "composite_armor", "acid_digestion", "nitrobenzene");
		// 2 benzene (222 units) with ammonia -> 322 nylon.
		MachineBlockEntity nylon = run(ctx, "high_pressure_reactor", projects, of("benzene", 2), of("ammonia", 1), charcoal(), false, 80_000);
		expect(ctx, count(nylon, mod("nylon")), 2, "nylon");
		ctx.setBlockState(POS, Blocks.AIR);
		// 2 p-xylene -> 348 terephthalic acid.
		MachineBlockEntity tpa = run(ctx, "high_pressure_reactor", projects, of("xylene", 2), ItemStack.EMPTY, charcoal(), false, 80_000);
		expect(ctx, count(tpa, mod("terephthalic_acid")), 3, "terephthalic acid");
		ctx.setBlockState(POS, Blocks.AIR);
		// 2 benzene with nitric acid -> 350 nitrobenzene.
		MachineBlockEntity vat = run(ctx, "digestion_vat", projects, of("benzene", 2), of("nitric_acid", 2), charcoal(), false, 80_000);
		expect(ctx, count(vat, mod("nitrobenzene")), 3, "nitrobenzene");
		ctx.setBlockState(POS, Blocks.AIR);
		// 3 nitrobenzene (333 units) with ammonia -> 293 p-phenylenediamine.
		MachineBlockEntity ppd = run(ctx, "high_pressure_reactor", projects, of("nitrobenzene", 3), of("ammonia", 1), charcoal(), false, 80_000);
		expect(ctx, count(ppd, mod("p_phenylenediamine")), 2, "p-phenylenediamine");
		ctx.setBlockState(POS, Blocks.AIR);
		// 3 terephthalic acid (333 units) with 2 p-phenylenediamine (222, of 217 needed) -> 478 aramid.
		MachineBlockEntity aramid = run(ctx, "high_pressure_reactor", projects, of("terephthalic_acid", 3), of("p_phenylenediamine", 2),
				charcoal(), false, 80_000);
		expect(ctx, count(aramid, mod("aramid")), 4, "aramid");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void dieselGeneratorAndCableReach(TestContext ctx) {
		ctx.setBlockState(POS, block("diesel_generator"));
		PowerBlockEntity gen = ctx.getBlockEntity(POS);
		gen.setStack(0, of("diesel", 1));
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int i = 0; i < 100; i++) {
			gen.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, Math.abs(gen.stored() - 1200) < 1e-6, "100 ticks at 12 kJ: " + gen.stored());
		check(ctx, gen.getStack(0).isOf(com.periodicelements.Fluids.GLASS_FLASK), "the diesel should be burning, its flask back in the slot");
		// 100 blocks of line: too far for bare copper wire (64), well within insulated cable (100 x 0.25 = 25).
		for (String line : List.of("copper_wire", "insulated_cable")) {
			for (int x = 1; x <= 100; x++) {
				ctx.setBlockState(POS.east(x), block(line));
			}
			boolean reached = MachineBlockEntity.powerSources(ctx.getWorld(), ctx.getAbsolutePos(POS.east(101))).contains(gen);
			check(ctx, reached == line.equals("insulated_cable"), line + " over 100 blocks reached: " + reached);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void asphaltParachuteAndRocket(TestContext ctx) {
		check(ctx, block("asphalt").getVelocityMultiplier() > 1.05f, "asphalt should speed you up");
		ZombieEntity faller = new ZombieEntity(EntityType.ZOMBIE, ctx.getWorld());
		faller.setOnGround(false);
		faller.setVelocity(0, -0.6, 0);
		faller.fallDistance = 2;
		check(ctx, !com.periodicelements.ParachuteItem.opens(faller), "a short drop doesn't open it");
		faller.fallDistance = 5;
		check(ctx, com.periodicelements.ParachuteItem.opens(faller), "a long fall opens it");
		var recipe = ctx.getWorld().getRecipeManager().get(Identifier.of(PeriodicElements.MOD_ID, "kerosene_rocket"));
		check(ctx, recipe.isPresent(), "kerosene rocket recipe");
		ItemStack rocket = recipe.get().value().getResult(ctx.getWorld().getRegistryManager());
		var fw = rocket.get(net.minecraft.component.DataComponentTypes.FIREWORKS);
		check(ctx, rocket.isOf(Items.FIREWORK_ROCKET) && rocket.getCount() == 3 && fw != null && fw.flightDuration() == 3,
				"3 rockets of flight 3: " + rocket + " " + fw);
		ctx.complete();
	}
}
