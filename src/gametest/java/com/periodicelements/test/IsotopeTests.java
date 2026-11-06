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

import com.periodicelements.radiation.Isotopes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 1.3.0: isotope ratios carried by items, changed by the Calutron, the reactor and reprocessing. */
public class IsotopeTests implements FabricGameTest {
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


	/** Counts output items of this kind whose ratio is within a range. */
	private static int countRatio(MachineBlockEntity be, Item item, String element, double lo, double hi) {
		int n = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			ItemStack s = be.getStack(i);
			double r = Isotopes.ratio(s, element);
			if (s.isOf(item) && r >= lo && r <= hi) {
				n += s.getCount();
			}
		}
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void ratiosRoundAndKey(TestContext ctx) {
		Isotopes.Tracked u = Isotopes.tracked("uranium");
		check(ctx, u != null && u.natural() == 0.72 && u.isotope().equals("U-235"), "uranium tracks U-235");
		check(ctx, Isotopes.key("uranium", 0.7201).equals("uranium"), "natural ratio keeps the plain id");
		check(ctx, Isotopes.key("item:periodic_elements:uranium", 12.666).equals("item:periodic_elements:uranium#12.67"),
				"key: " + Isotopes.key("item:periodic_elements:uranium", 12.666));
		double[] split = Isotopes.separate(0.72, 20, 0.05);
		check(ctx, Math.abs(split[0] - 12.67) < 0.01 && Math.abs(0.05 * split[0] + 0.95 * split[1] - 0.72) < 1e-9,
				"a pass conserves U-235: " + split[0] + " / " + split[1]);
		ItemStack plain = of("uranium", 1);
		check(ctx, plain.get(Isotopes.RATIO) == null && Isotopes.ratio(plain, "uranium") == 0.72, "ordinary uranium is natural");
		Isotopes.stamp(plain, "uranium", 0.72);
		check(ctx, plain.get(Isotopes.RATIO) == null, "stamping natural adds nothing, so it still stacks");
		check(ctx, Isotopes.natural("silicon").startsWith("92.2% Si-28") && Isotopes.natural("gold").equals("100% Au-197"),
				"natural lines: " + Isotopes.natural("silicon"));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void calutronEnrichesUranium(TestContext ctx) {
		// 21 uranium (2333 units): 5% (117 units) at 12.67% U-235, the rest at 0.09%.
		MachineBlockEntity cal = run(ctx, "calutron", List.of("dynamo", "electromagnetic_separation", "isotope_enrichment"),
				of("uranium", 21), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, countRatio(cal, mod("uranium"), "uranium", 12.6, 12.7), 1, "enriched uranium");
		expect(ctx, countRatio(cal, mod("uranium"), "uranium", 0.08, 0.10), 19, "depleted uranium");
		ctx.setBlockState(POS, Blocks.AIR);
		// Lithium: 7% comes out as nearly pure lithium-6.
		MachineBlockEntity li = run(ctx, "calutron", List.of("dynamo", "electromagnetic_separation", "isotope_enrichment"),
				of("lithium", 16), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, countRatio(li, mod("lithium"), "lithium", 90, 100), 1, "lithium-6");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void onlyEnrichedMetalMakesFuel(TestContext ctx) {
		List<String> projects = List.of("dynamo", "electromagnetic_separation", "isotope_enrichment");
		MachineBlockEntity natural = run(ctx, "sintering_furnace", projects, of("uranium", 3), ItemStack.EMPTY, ItemStack.EMPTY, true, 2_000);
		expect(ctx, natural.getStack(MachineBlockEntity.INPUT).getCount(), 3, "natural uranium left in the input");
		expect(ctx, count(natural, mod("uranium_fuel_pellet")), 0, "pellets from natural uranium");
		ctx.setBlockState(POS, Blocks.AIR);
		ItemStack enriched = Isotopes.stamp(of("uranium", 3), "uranium", 12.67);
		MachineBlockEntity fuel = run(ctx, "sintering_furnace", projects, enriched, ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(fuel, mod("uranium_fuel_pellet")), 3, "fuel pellets from enriched metal");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void plutoniumGrades(TestContext ctx) {
		ctx.setBlockState(POS, block("nuclear_reactor"));
		PowerBlockEntity reactor = ctx.getBlockEntity(POS);
		reactor.setStack(0, of("uranium", 4));
		reactor.irradiate();
		ItemStack pu = reactor.getStack(1);
		check(ctx, pu.isOf(mod("plutonium")) && Isotopes.ratio(pu, "plutonium") == 6.0
				&& Isotopes.label(Isotopes.tracked("plutonium"), 6.0).equals("weapons_grade"),
				"bred plutonium should be weapons grade: " + pu + " " + pu.get(Isotopes.RATIO));
		ctx.setBlockState(POS, Blocks.AIR);
		// Plutonium from spent fuel is reactor grade (24% Pu-240), which is the default.
		MachineBlockEntity crucible = run(ctx, "reduction_crucible", List.of("reprocessing"),
				of("plutonium_dioxide", 2), of("calcium", 2), charcoal(), false, 80_000);
		check(ctx, countRatio(crucible, mod("plutonium"), "plutonium", 23.9, 24.1) >= 1, "reactor-grade plutonium");
		ctx.setBlockState(POS, Blocks.AIR);
		// The calcium-48 cut leaves calcium with none.
		MachineBlockEntity cal = run(ctx, "calutron", List.of("dynamo", "electromagnetic_separation"),
				of("calcium", 32), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, countRatio(cal, mod("calcium"), "calcium", 0, 0.001), 31, "calcium stripped of calcium-48");
		ctx.complete();
	}

	private static int runReactor(TestContext ctx, PowerBlockEntity reactor, int ticks) {
		BlockPos abs = reactor.getPos();
		for (int i = 0; i < ticks; i++) {
			reactor.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		return reactor.burnLeft();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void controlRodsAndHeavyWater(TestContext ctx) {
		ctx.setBlockState(POS.up(), Blocks.WATER);
		ctx.setBlockState(POS, block("nuclear_reactor"));
		PowerBlockEntity reactor = ctx.getBlockEntity(POS);
		reactor.setStack(0, of("uranium_fuel_rod", 1));
		reactor.setStack(1, of("boron_control_rod", 1));
		// 6000 kJ fill in 200 ticks at 30 kJ; then the control rod holds the rod's burn.
		int left = runReactor(ctx, reactor, 400);
		check(ctx, left == 72000 - 200, "the control rod should stop the burn when full: " + left);
		reactor.setStack(1, ItemStack.EMPTY);
		left = runReactor(ctx, reactor, 100);
		check(ctx, left == 72000 - 300, "without it the rod burns on: " + left);
		ctx.setBlockState(POS, Blocks.AIR);
		ctx.setBlockState(POS, block("nuclear_reactor"));
		PowerBlockEntity candu = ctx.getBlockEntity(POS);
		candu.setStack(0, of("natural_uranium_fuel_rod", 1));
		runReactor(ctx, candu, 10);
		check(ctx, !candu.blowing() && candu.getStack(0).getCount() == 1, "natural uranium needs heavy water");
		candu.setStack(1, of("heavy_water", 1));
		runReactor(ctx, candu, 10);
		check(ctx, candu.blowing() && candu.getStack(0).isEmpty() && candu.getStack(1).getCount() == 1,
				"with heavy water it burns, and the heavy water stays");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void lithium6BreedsTritium(TestContext ctx) {
		ctx.setBlockState(POS, block("nuclear_reactor"));
		PowerBlockEntity reactor = ctx.getBlockEntity(POS);
		reactor.setStack(0, Isotopes.stamp(of("lithium", 2), "lithium", 95.0));
		reactor.setStack(1, of("lithium", 2));
		reactor.irradiate();
		int tritium = 0;
		for (int i = 0; i < 9; i++) {
			if (reactor.getStack(i).isOf(mod("tritium_vial"))) {
				tritium += reactor.getStack(i).getCount();
			}
		}
		check(ctx, tritium == 1 && reactor.getStack(1).getCount() == 2, "one tritium from lithium-6, natural lithium untouched: " + tritium);
		check(ctx, Decay.of(mod("tritium_vial")) != null, "tritium decays");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void heavyWaterAndControlRodsAreMade(TestContext ctx) {
		// 16 ice (16,000 units of water) -> 136 units heavy water -> 1.
		MachineBlockEntity cascade = run(ctx, "enrichment_cascade", List.of("dynamo", "isotope_separation", "heavy_water"),
				new ItemStack(Items.ICE, 16), ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(cascade, mod("heavy_water")), 1, "heavy water");
		ctx.setBlockState(POS, Blocks.AIR);
		List<String> projects = List.of("dynamo", "electromagnetic_separation", "isotope_enrichment", "control_rods");
		MachineBlockEntity natural = run(ctx, "sintering_furnace", projects, of("boron", 1), ItemStack.EMPTY, ItemStack.EMPTY, true, 2_000);
		expect(ctx, natural.getStack(MachineBlockEntity.INPUT).getCount(), 1, "natural boron refused");
		ctx.setBlockState(POS, Blocks.AIR);
		MachineBlockEntity press = run(ctx, "sintering_furnace", projects, Isotopes.stamp(of("boron", 2), "boron", 92.5),
				ItemStack.EMPTY, ItemStack.EMPTY, true, 80_000);
		expect(ctx, count(press, mod("boron_control_rod")), 2, "control rods");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void fieldGuideIsBuilt(TestContext ctx) {
		check(ctx, mod("field_guide") instanceof com.periodicelements.FieldGuideItem, "field guide item");
		var chapters = com.periodicelements.ElementTable.readObject("/periodic_elements/guidebook.json").getAsJsonArray("chapters");
		check(ctx, chapters.size() == 44, "44 chapters (each level split into research, machines and reactions; 2.7.0 adds the Atlas, 2.8.0 the trees, 3.2.0 the Tree Compendium, 3.3.0 the structures, 3.5.0 the Plant Compendium, 3.15.0 the Mineral Compendium, 3.16.0 the shrubs, 3.23.0 crops and genetics, 3.42.0 the Geological world, 3.47.0 Exploration, 3.50.0 Animals of the World): " + chapters.size());
		for (var c : chapters) {
			var o = c.getAsJsonObject();
			check(ctx, o.getAsJsonArray("text").size() > 5, "chapter " + o.get("group") + " " + o.get("title") + " has text");
			check(ctx, Registries.ITEM.containsId(Identifier.of(o.get("icon").getAsString())), "icon " + o.get("icon"));
			// 2.2.0: every paragraph carries the tech level it belongs to; the chapter's level is the lowest.
			var levels = o.getAsJsonArray("levels");
			check(ctx, levels.size() == o.getAsJsonArray("text").size(), "a level per paragraph in " + o.get("id"));
			int min = 99;
			for (var l : levels) {
				check(ctx, l.getAsInt() >= 0 && l.getAsInt() <= 7, "levels 0 to 7 in " + o.get("id"));
				min = Math.min(min, l.getAsInt());
			}
			check(ctx, o.get("level").getAsInt() == min, "chapter level is its lowest paragraph's in " + o.get("id"));
			String id = o.get("id").getAsString();
			if (id.startsWith("level_")) {
				check(ctx, min == id.charAt(6) - '0', id + " is shown from its own level");
			}
		}
		int life = -1;
		int start = -1;
		for (var c : chapters) {
			var o = c.getAsJsonObject();
			if (o.get("id").getAsString().equals("life")) {
				life = o.get("level").getAsInt();
			} else if (o.get("id").getAsString().equals("start")) {
				start = o.get("level").getAsInt();
			}
		}
		check(ctx, start == 0 && life == 6, "Getting Started from the start, Life Sciences from level 6: " + start + ", " + life);
		ctx.complete();
	}

	/** 2.4.0/2.5.0: every translation covers every string, with the same placeholders, and the whole Field Guide. */
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void translationsCoverEverything(TestContext ctx) {
		var en = com.periodicelements.ElementTable.readObject("/assets/periodic_elements/lang/en_us.json");
		var guideEn = com.periodicelements.ElementTable.readObject("/periodic_elements/guidebook.json").getAsJsonArray("chapters");
		java.util.regex.Pattern arg = java.util.regex.Pattern.compile("%(\\d\\$)?[sd%]");
		int found = 0;
		for (String code : List.of("pt_br", "fr_fr", "es_es", "nl_nl", "it_it", "de_de", "sv_se", "da_dk", "ru_ru")) {
			if (IsotopeTests.class.getResource("/assets/periodic_elements/lang/" + code + ".json") == null) {
				continue;
			}
			found++;
			var tr = com.periodicelements.ElementTable.readObject("/assets/periodic_elements/lang/" + code + ".json");
			for (String key : en.keySet()) {
				check(ctx, tr.has(key), code + " has " + key);
				String e = en.get(key).getAsString();
				String t = tr.get(key).getAsString();
				check(ctx, arg.matcher(e).results().count() == arg.matcher(t).results().count(), "same placeholders in " + code + " " + key);
			}
			var guide = com.periodicelements.ElementTable.readObject("/periodic_elements/guidebook_" + code + ".json").getAsJsonArray("chapters");
			check(ctx, guideEn.size() == guide.size(), "as many chapters in " + code);
			for (int i = 0; i < guideEn.size(); i++) {
				var a = guideEn.get(i).getAsJsonObject();
				var b = guide.get(i).getAsJsonObject();
				check(ctx, a.get("id").equals(b.get("id")) && a.get("levels").equals(b.get("levels")), "same paragraphs in " + code + " " + a.get("id"));
				check(ctx, a.has("group") == b.has("group"), "same grouping in " + code + " " + a.get("id"));
			}
		}
		check(ctx, found >= 2, "at least Portuguese and French: " + found);
		ctx.complete();
	}
}
