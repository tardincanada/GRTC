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

/** Level 7 (1.0.0): superconductors, chips, magnets, composite armour and the superheavy elements. */
public class LevelSevenTests implements FabricGameTest {
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
	public void calutronSeparatesCalcium48(TestContext ctx) {
		// 32 calcium (3556 units): 1% is calcium-48 (36 units), the rest comes back.
		MachineBlockEntity cal = run(ctx, "calutron", List.of("dynamo", "electromagnetic_separation"),
				of("calcium", 32), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		check(ctx, cal.outputBuffers().getOrDefault("calcium_48", 0.0) > 35, "calcium-48 should collect: " + cal.outputBuffers());
		expect(ctx, count(cal, mod("calcium")), 31, "calcium back");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void superconductorsAndChips(TestContext ctx) {
		// 2 niobium (222 units) with 2 titanium -> 419 units Nb-Ti -> 3 wires.
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "superconductors"),
				of("niobium", 2), of("titanium", 2), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(arc, mod("niobium_titanium_wire")), 3, "niobium-titanium wire");
		ctx.setBlockState(POS, Blocks.AIR);
		MachineBlockEntity fab = run(ctx, "chip_fab", List.of("dynamo", "integrated_circuits"),
				of("silicon_wafer", 4), of("boron", 1), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(fab, mod("integrated_circuit")), 4, "integrated circuits");
		ctx.setBlockState(POS, Blocks.AIR);
		// 2 gallium (222 units) with arsenic -> 461 units GaAs -> 4 wafers.
		MachineBlockEntity puller = run(ctx, "crystal_puller", List.of("dynamo", "crystal_growth", "compound_semiconductors"),
				of("gallium", 2), of("arsenic", 3), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(puller, mod("gallium_arsenide_wafer")), 4, "gallium arsenide wafers");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void neodymiumMagnets(TestContext ctx) {
		// 4 iron with boron -> 451 units iron-boron alloy -> 4.
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "permanent_magnets"),
				new ItemStack(Items.IRON_INGOT, 4), of("boron", 1), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(arc, mod("iron_boron_alloy")), 4, "iron-boron alloy");
		ctx.setBlockState(POS, Blocks.AIR);
		// 1 neodymium (111 units) with 3 iron-boron -> 416 units Nd2Fe14B -> 3 magnets.
		MachineBlockEntity sinter = run(ctx, "sintering_furnace", List.of("dynamo", "permanent_magnets"),
				of("neodymium", 1), of("iron_boron_alloy", 3), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(sinter, mod("neodymium_magnet")), 3, "neodymium magnets");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void compositeArmourMaterials(TestContext ctx) {
		// 4 boron (444 units) with coke -> 568 units boron carbide -> 5.
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "composite_armor"),
				of("boron", 4), ItemStack.EMPTY, coke(), true, 80_000);
		expect(ctx, count(arc, mod("boron_carbide")), 5, "boron carbide");
		// Aramid itself comes from oil since 1.2.0 (see PetrochemicalTests).
		check(ctx, mod("composite_chestplate") != Items.AIR && mod("composite_pickaxe") == Items.AIR, "composite is armour only");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void acceleratorMakesSuperheavyRecords(TestContext ctx) {
		// 8 bismuth (889 units) with a zinc beam -> 2 nihonium records, and 7 bismuth back.
		MachineBlockEntity acc = run(ctx, "heavy_ion_accelerator", List.of("dynamo", "heavy_ion_accelerator", "cold_fusion"),
				of("bismuth", 8), of("zinc", 1), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(acc, mod("nihonium_record")), 2, "nihonium records");
		expect(ctx, count(acc, mod("bismuth")), 7, "bismuth targets back");
		ctx.setBlockState(POS, Blocks.AIR);
		// 10 californium samples with a calcium-48 beam -> 1 oganesson record, 9 samples back.
		MachineBlockEntity og = run(ctx, "heavy_ion_accelerator", List.of("dynamo", "heavy_ion_accelerator", "hot_fusion"),
				of("californium_sample", 10), of("calcium_48", 1), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(og, mod("oganesson_record")), 1, "oganesson record");
		expect(ctx, count(og, mod("californium_sample")), 9, "californium samples back");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void windTurbineNeedsOpenSky(TestContext ctx) {
		ctx.setBlockState(POS, block("wind_turbine"));
		PowerBlockEntity turbine = ctx.getBlockEntity(POS);
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int i = 0; i < 100; i++) {
			turbine.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		if (turbine.windPower(ctx.getWorld(), abs) > 0) {
			check(ctx, turbine.stored() >= 49, "turbine should charge under open sky: " + turbine.stored());
		}
		ctx.setBlockState(POS.up(), Blocks.STONE);
		double before = turbine.stored();
		for (int i = 0; i < 100; i++) {
			turbine.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, turbine.stored() == before, "no wind under a roof");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void batteryCathodeAndFibre(TestContext ctx) {
		// 2 cobalt (222 units) with lithium -> 369 units LiCoO2 -> 3.
		MachineBlockEntity sinter = run(ctx, "sintering_furnace", List.of("dynamo", "lithium_ion_battery"),
				of("cobalt", 2), of("lithium", 1), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(sinter, mod("lithium_cobalt_oxide")), 3, "lithium cobalt oxide");
		ctx.setBlockState(POS, Blocks.AIR);
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "optical_fibre"),
				of("silicon_dioxide", 4), of("erbium", 1), ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(arc, mod("optical_fibre")), 4, "optical fibre");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void rtgChargesABattery(TestContext ctx) {
		ctx.setBlockState(POS, block("nuclear_reactor"));
		PowerBlockEntity reactor = ctx.getBlockEntity(POS);
		reactor.setStack(0, of("neptunium", 4));
		reactor.irradiate();
		check(ctx, reactor.getStack(1).isOf(mod("plutonium_238_pellet")), "neptunium should become plutonium-238: " + reactor.getStack(1));
		ctx.setBlockState(POS, block("radioisotope_generator"));
		ctx.setBlockState(POS.east(), block("lithium_ion_battery"));
		PowerBlockEntity rtg = ctx.getBlockEntity(POS);
		PowerBlockEntity battery = ctx.getBlockEntity(POS.east());
		rtg.setStack(0, of("plutonium_238_pellet", 4));
		BlockPos a = ctx.getAbsolutePos(POS), b = ctx.getAbsolutePos(POS.east());
		for (int i = 0; i < 100; i++) {
			rtg.tick(ctx.getWorld(), a, ctx.getWorld().getBlockState(a));
			battery.tick(ctx.getWorld(), b, ctx.getWorld().getBlockState(b));
		}
		check(ctx, Math.abs(rtg.stored() + battery.stored() - 100) < 1e-6, "4 pellets give 1 kJ per tick: " + rtg.stored() + " + " + battery.stored());
		check(ctx, battery.stored() > 90, "the battery should take the current: " + battery.stored());
		// 3.41.0: a little charge shows one red row of the bar.
		var bs = ctx.getWorld().getBlockState(b);
		check(ctx, bs.get(com.periodicelements.machine.BatteryBlock.LEVEL) == 1 && bs.get(com.periodicelements.machine.BatteryBlock.BAND) == 1,
				"0.5% charge: one red row, got " + bs);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void batteryBarColours(TestContext ctx) {
		// 3.41.0: empty black, under 25% red, under 50% yellow, below full green, full blue.
		double[] at = {0, 0.01, 0.24, 0.25, 0.49, 0.5, 0.75, 0.9, 0.99, 1};
		int[] levels = {0, 1, 3, 3, 6, 6, 9, 11, 11, 12};
		int[] bands = {0, 1, 1, 2, 2, 3, 3, 3, 3, 4};
		for (int i = 0; i < at.length; i++) {
			int level = com.periodicelements.machine.BatteryBlock.level(at[i]);
			int band = com.periodicelements.machine.BatteryBlock.band(at[i]);
			check(ctx, band == bands[i] && level == levels[i], at[i] + ": level " + level + " band " + band);
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void smokeDetectorAndAnalyzer(TestContext ctx) {
		ctx.setBlockState(POS, block("smoke_detector"));
		BlockPos abs = ctx.getAbsolutePos(POS);
		check(ctx, !SmokeDetectorBlock.smoke(ctx.getWorld(), abs), "no smoke yet");
		ctx.setBlockState(POS.east(2), Blocks.NETHERRACK);
		ctx.setBlockState(POS.east(2).up(), Blocks.FIRE);
		check(ctx, SmokeDetectorBlock.smoke(ctx.getWorld(), abs), "fire should set it off");
		var granite = ResearchedToolItem.elementsOfBlock(Blocks.GRANITE.getDefaultState());
		check(ctx, granite.keySet().iterator().next().equals("oxygen") && granite.containsKey("silicon"), "granite: " + granite);
		ctx.complete();
	}
}
