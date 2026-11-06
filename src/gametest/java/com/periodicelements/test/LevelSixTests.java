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

/** Level 6, first half (0.9.0): titanium, ion exchange, uranium metal, crystals, diamond, solar power. */
public class LevelSixTests implements FabricGameTest {
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
	public void titaniumByKroll(TestContext ctx) {
		// 16 concentrate: ilmenite 569 + rutile 142 units chlorinated -> 1048 units TiCl4 -> 9.
		MachineBlockEntity cl = run(ctx, "chlorinator", List.of("kroll_process"),
				of("heavy_mineral_concentrate", 16), of("chlorine", 16), coke(), false, 80_000);
		check(ctx, count(cl, mod("titanium_tetrachloride")) >= 8, "titanium tetrachloride: " + count(cl, mod("titanium_tetrachloride")));
		// 5 TiCl4 (555 units) with magnesium -> 140 units titanium -> 1, and 557 units MgCl2 -> 5 to recycle.
		MachineBlockEntity kroll = run(ctx, "kroll_reactor", List.of("kroll_process"),
				of("titanium_tetrachloride", 5), of("magnesium", 2), charcoal(), false, 40_000);
		expect(ctx, count(kroll, mod("titanium")), 1, "titanium");
		expect(ctx, count(kroll, mod("magnesium_chloride")), 5, "magnesium chloride");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void titaniumAlloyWithMasterAlloy(TestContext ctx) {
		// 6 aluminium + vanadium 6:4 -> 1111 units master alloy.
		MachineBlockEntity cr = run(ctx, "crucible", List.of("titanium_alloy", "bellows"),
				of("aluminium", 6), of("vanadium", 5), charcoal(), false, 40_000);
		check(ctx, count(cr, mod("aluminium_vanadium_master_alloy")) >= 9, "master alloy: " + count(cr, mod("aluminium_vanadium_master_alloy")));
		// 9 titanium (1000 units) with 1 master alloy -> 1111 units Ti-6Al-4V.
		MachineBlockEntity arc = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "titanium_alloy"),
				of("titanium", 9), of("aluminium_vanadium_master_alloy", 1), ItemStack.EMPTY, true, 40_000);
		check(ctx, count(arc, mod("titanium_alloy_ingot")) >= 9, "titanium alloy: " + count(arc, mod("titanium_alloy_ingot")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void ionExchangeSeparatesYttrium(TestContext ctx) {
		// 32 concentrate: 356 units xenotime -> 378 units mixed chlorides -> 3.
		MachineBlockEntity vat = run(ctx, "digestion_vat", List.of("ion_exchange", "acid_digestion"),
				of("heavy_mineral_concentrate", 32), of("hydrochloric_acid", 4), charcoal(), false, 80_000);
		expect(ctx, count(vat, mod("heavy_rare_earth_chlorides")), 3, "heavy rare earth chlorides");
		// 9 mixed chlorides: YCl3 200 units -> 116 units Y2O3 -> 1; the others collect.
		MachineBlockEntity col = run(ctx, "ion_exchange_column", List.of("dynamo", "ion_exchange", "scandium",
						"samarium_europium", "gadolinium_terbium", "dysprosium_holmium", "erbium_thulium", "ytterbium_lutetium"),
				of("heavy_rare_earth_chlorides", 9), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		expect(ctx, count(col, mod("yttrium_oxide")), 1, "yttria");
		check(ctx, col.outputBuffers().getOrDefault("europium_oxide", 0.0) > 40, "europium oxide should collect");
		check(ctx, col.outputBuffers().getOrDefault("lutetium_oxide", 0.0) > 40, "lutetium oxide should collect");
		// 2 Y2O3 (222 units) with calcium -> 175 units yttrium -> 1.
		MachineBlockEntity red = run(ctx, "reduction_crucible", List.of("ion_exchange", "boron"),
				of("yttrium_oxide", 2), of("calcium", 2), charcoal(), false, 40_000);
		expect(ctx, count(red, mod("yttrium")), 1, "yttrium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void uraniumMetalByAmesProcess(TestContext ctx) {
		// 4 U3O8 (444 units) in hydrogen fluoride -> 497 units UF4 -> 4.
		MachineBlockEntity cl = run(ctx, "chlorinator", List.of("kroll_process", "uranium_metal"),
				of("triuranium_octoxide", 4), of("hydrogen_fluoride", 2), charcoal(), false, 40_000);
		expect(ctx, count(cl, mod("uranium_tetrafluoride")), 4, "uranium tetrafluoride");
		// 4 UF4 (444 units) with magnesium -> 337 units uranium -> 3.
		MachineBlockEntity red = run(ctx, "reduction_crucible", List.of("uranium_metal"),
				of("uranium_tetrafluoride", 4), of("magnesium", 1), charcoal(), false, 40_000);
		expect(ctx, count(red, mod("uranium")), 3, "uranium");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void crystalsAndDiamond(TestContext ctx) {
		MachineBlockEntity puller = run(ctx, "crystal_puller", List.of("dynamo", "crystal_growth"),
				of("silicon", 3), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		check(ctx, count(puller, mod("silicon_wafer")) >= 2, "silicon wafers: " + count(puller, mod("silicon_wafer")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void zoneRefinedGermanium(TestContext ctx) {
		MachineBlockEntity zone = run(ctx, "zone_refiner", List.of("dynamo", "crystal_growth"),
				of("germanium", 2), ItemStack.EMPTY, ItemStack.EMPTY, true, 40_000);
		check(ctx, count(zone, mod("germanium_crystal")) >= 1, "germanium crystal");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void diamondFromCharcoal(TestContext ctx) {
		// 8 charcoal (888 units of carbon) in molten nickel -> about 8 diamonds.
		MachineBlockEntity press = run(ctx, "diamond_press", List.of("dynamo", "diamond_synthesis"),
				new ItemStack(Items.CHARCOAL, 8), of("nickel", 1), ItemStack.EMPTY, true, 80_000);
		check(ctx, count(press, mod("synthetic_diamond")) >= 7, "synthetic diamond: " + count(press, mod("synthetic_diamond")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void solarPanelChargesInSunlight(TestContext ctx) {
		ctx.getWorld().setTimeOfDay(6000);
		ctx.setBlockState(POS, block("solar_panel"));
		PowerBlockEntity panel = ctx.getBlockEntity(POS);
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int i = 0; i < 100; i++) {
			panel.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		if (PowerBlockEntity.sunlit(ctx.getWorld(), abs)) {
			check(ctx, panel.stored() > 90, "solar panel should charge: " + panel.stored());
		}
		ctx.getWorld().setTimeOfDay(18000);
		double before = panel.stored();
		for (int i = 0; i < 100; i++) {
			panel.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, panel.stored() == before, "no charge at night");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void levelSixGearExists(TestContext ctx) {
		for (String id : List.of("titanium_alloy_chestplate", "titanium_alloy_pickaxe", "synthetic_diamond_pickaxe", "solar_panel")) {
			check(ctx, mod(id) != Items.AIR, id + " should be registered");
		}
		check(ctx, mod("synthetic_diamond_pickaxe").getDefaultStack().getMaxDamage() == 2500, "diamond-tipped pickaxe durability");
		ctx.complete();
	}
}
