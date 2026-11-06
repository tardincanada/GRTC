package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.FieldState;
import com.periodicelements.farm.Life;
import com.periodicelements.farm.MiningChargeBlock;
import com.periodicelements.farm.ShapedChargeBlock;
import com.periodicelements.farm.SprayerItem;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.medicine.Infection;
import com.periodicelements.medicine.Medicine;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.ChickenEntity;
import com.periodicelements.machine.Upgrades;
import net.minecraft.util.math.Direction;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 2.0.0 Life sciences: antibiotics, pesticides, plastics, explosives, radiation countermeasures, biotechnology. */
public class LifeTests implements FabricGameTest {
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

	private static ItemStack of(String id, int n) {
		return id.startsWith("minecraft:") ? new ItemStack(Registries.ITEM.get(Identifier.of(id)), n) : new ItemStack(mod(id), n);
	}

	private static MachineBlockEntity place(TestContext ctx, String machine, List<String> projects) {
		ctx.setBlockState(POS, Blocks.AIR);
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(mod("glass_flask"), 32));
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, new ArrayList<>(projects));
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

	private static MachineBlockEntity run(TestContext ctx, String machine, List<String> projects, ItemStack input,
										  ItemStack reagent, ItemStack fuel, boolean powered) {
		MachineBlockEntity be = place(ctx, machine, projects);
		List<PowerBlockEntity> plant = powered ? powerPlant(ctx) : null;
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.REAGENT, reagent);
		be.setStack(MachineBlockEntity.FUEL, fuel);
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		double peak = 0;
		for (; t < 60_000; t++) {
			if (plant != null) {
				for (PowerBlockEntity p : plant) {
					p.tick(ctx.getWorld(), p.getPos(), ctx.getWorld().getBlockState(p.getPos()));
				}
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			peak = Math.max(peak, be.temperature());
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT)) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, peak {} °C, status {}, outputs {} {} {} | residue {} | reagent {} | buffers {}",
				be.spec().id(), t, String.format(Locale.ROOT, "%.0f", peak), be.status(), be.getStack(2), be.getStack(3),
				be.getStack(4), be.getStack(5), be.getStack(6), be.outputBuffers());
		return be;
	}

	private static int count(MachineBlockEntity be, String id) {
		Item item = of(id, 1).getItem();
		int n = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			if (be.getStack(i).isOf(item)) {
				n += be.getStack(i).getCount();
			}
		}
		return n;
	}

	private static void expect(TestContext ctx, MachineBlockEntity be, String id, int want) {
		int got = count(be, id);
		if (got != want) {
			ctx.throwGameTestException(be.spec().id() + ": " + want + " " + id + " expected, got " + got);
		}
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static ItemStack coke() {
		return of("coke", 64);
	}

	private static ItemStack charcoal() {
		return new ItemStack(Items.CHARCOAL, 64);
	}

	private static MachineBlockEntity filtered(TestContext ctx, String machine, List<String> projects, ItemStack input,
											   ItemStack reagent, boolean filter) {
		MachineBlockEntity be = place(ctx, machine, projects);
		if (filter) {
			be.setStack(MachineBlockEntity.UPGRADE_FIRST, of("sterile_air_filter", 1));
		}
		List<PowerBlockEntity> plant = powerPlant(ctx);
		be.setStack(MachineBlockEntity.INPUT, input);
		be.setStack(MachineBlockEntity.REAGENT, reagent);
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		for (; t < 40_000; t++) {
			for (PowerBlockEntity p : plant) {
				p.tick(ctx.getWorld(), p.getPos(), ctx.getWorld().getBlockState(p.getPos()));
			}
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && (be.status() == MachineBlockEntity.Status.NEEDS_FILTER || be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_REAGENT))) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {} filter {}: {} ticks, status {}, outputs {} {} {} | residue {} | buffers {}",
				be.spec().id(), filter, t, be.status(), be.getStack(2), be.getStack(3), be.getStack(4), be.getStack(5), be.outputBuffers());
		return be;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void penicillinNeedsSterileAir(TestContext ctx) {
		List<String> p = List.of("dynamo", "electric_motor", "mashing", "sulfa_drugs", "penicillin");
		MachineBlockEntity bare = filtered(ctx, "deep_tank_fermenter", p, of("glucose", 9), new ItemStack(Items.MILK_BUCKET), false);
		check(ctx, bare.status() == MachineBlockEntity.Status.NEEDS_FILTER, "no filter, no penicillin: " + bare.status());
		// 9 glucose (1000 units) with milk's lactose: 200 units, 220 with the filter's 10%: 1 item.
		MachineBlockEntity tank = filtered(ctx, "deep_tank_fermenter", p, of("glucose", 9), new ItemStack(Items.MILK_BUCKET), true);
		expect(ctx, tank, "penicillin", 1);
		check(ctx, Upgrades.FILTER_BONUS > 1, "the filter adds product");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void streptomycinFromProtein(TestContext ctx) {
		List<String> p = List.of("dynamo", "electric_motor", "mashing", "sulfa_drugs", "penicillin", "streptomycin");
		MachineBlockEntity tank = filtered(ctx, "deep_tank_fermenter", p, of("glucose", 18), of("protein_meal", 4), true);
		// 2000 units of glucose: 300 units, 330 with the filter: 2 items.
		expect(ctx, tank, "streptomycin", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pesticides(TestContext ctx) {
		List<String> p = List.of("acid_digestion", "tar_distillation", "ddt", "two_four_d");
		// 2 benzene (222 units) with chlorine: 505 units of DDT, 4 items.
		MachineBlockEntity ddt = run(ctx, "digestion_vat", p, of("benzene", 2), of("chlorine", 6), charcoal(), false);
		expect(ctx, ddt, "ddt", 4);
		// 2 phenol (222 units) with chlorine: 522 units of 2,4-D, 4 items.
		MachineBlockEntity d = run(ctx, "digestion_vat", p, of("phenol", 2), of("chlorine", 4), charcoal(), false);
		expect(ctx, d, "two_four_d", 4);
		// 2 ammonia (222 units), chlorine and coke: 564 units of atrazine, 5 items.
		MachineBlockEntity a = run(ctx, "high_pressure_reactor", List.of("haber_process", "urea", "atrazine"), of("ammonia", 2),
				of("chlorine", 2), coke(), false);
		expect(ctx, a, "atrazine", 5);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void explosivesAndPlastics(TestContext ctx) {
		List<String> p = List.of("acid_digestion", "formaldehyde", "plastic_explosive");
		// 4 formaldehyde (444 units) with ammonia: 346 units of hexamine, 3 items.
		MachineBlockEntity hex = run(ctx, "digestion_vat", p, of("formaldehyde", 4), of("ammonia", 4), charcoal(), false);
		expect(ctx, hex, "hexamine", 3);
		// 3 hexamine (333 units) with nitric acid, kept cold: 529 units of RDX, 4 items.
		MachineBlockEntity rdx = run(ctx, "digestion_vat", p, of("hexamine", 3), of("nitric_acid", 8), ItemStack.EMPTY, false);
		expect(ctx, rdx, "rdx", 4);
		// 3 styrene (333 units): 2 polystyrene (the third a hair short).
		MachineBlockEntity ps = run(ctx, "high_pressure_reactor", List.of("haber_process", "polystyrene"), of("styrene", 3),
				ItemStack.EMPTY, charcoal(), false);
		expect(ctx, ps, "polystyrene", 2);
		// 2 formaldehyde (222 units) with phosphoric acid: 417 units of glyphosate, 3 items.
		MachineBlockEntity gly = run(ctx, "high_pressure_reactor", List.of("haber_process", "glyphosate"), of("formaldehyde", 2),
				of("phosphoric_acid", 4), charcoal(), false);
		expect(ctx, gly, "glyphosate", 3);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bioreactor(TestContext ctx) {
		List<String> p = List.of("dynamo", "integrated_circuits", "recombinant_dna", "cellulosic_ethanol", "pla", "mrna_vaccine", "amifostine");
		// 1 oak log (620 units of cellulose): 317 units of ethanol, 2 flasks.
		MachineBlockEntity eth = filtered(ctx, "bioreactor", p, new ItemStack(Items.OAK_LOG), ItemStack.EMPTY, false);
		expect(ctx, eth, "ethanol", 2);
		// 4 glucose (444 units): 355 units of PLA, 3 items.
		MachineBlockEntity pla = filtered(ctx, "bioreactor", p, of("glucose", 4), ItemStack.EMPTY, false);
		expect(ctx, pla, "polylactic_acid", 3);
		// 4 protein meal (311 units of protein) with vegetable oil: 342 units, 3 vaccines.
		MachineBlockEntity vac = filtered(ctx, "bioreactor", p, of("protein_meal", 4), of("vegetable_oil", 1), false);
		expect(ctx, vac, "mrna_vaccine", 3);
		// 2 phosphoric acid (222 units) with ammonium sulfate: 422 units of amifostine, 3 items.
		MachineBlockEntity ami = filtered(ctx, "bioreactor", p, of("phosphoric_acid", 2), of("ammonium_sulfate", 4), false);
		expect(ctx, ami, "amifostine", 3);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void iodideInTheCrystallizer(TestContext ctx) {
		// 2 sodium iodide (222 units) with potash salt: 246 units of potassium iodide, 2 items.
		MachineBlockEntity be = run(ctx, "crystallizer", List.of("radiation_countermeasures"), of("sodium_iodide", 2),
				of("potassium_chloride", 2), charcoal(), false);
		expect(ctx, be, "potassium_iodide", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void medicines(TestContext ctx) {
		ZombieEntity patient = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		patient.addStatusEffect(new StatusEffectInstance(Infection.SEPSIS, -1));
		Medicine.take(patient, Medicine.PENICILLIN);
		check(ctx, !patient.hasStatusEffect(Infection.SEPSIS) && patient.hasStatusEffect(Medicine.IMMUNITY), "penicillin cures and guards");
		Infection.maybeInfect(patient, 1);
		check(ctx, !patient.hasStatusEffect(Infection.INFECTION), "immune: odds of 1 in 1 do not infect");
		check(ctx, Medicine.fissionFactor(patient) == 1 && Medicine.harmFactor(patient) == 1, "no countermeasures yet");
		Medicine.take(patient, Medicine.POTASSIUM_IODIDE);
		check(ctx, Math.abs(Medicine.fissionFactor(patient) - 0.6) < 1e-9, "iodide: 60% of the fission dose");
		Medicine.take(patient, Medicine.CAPSULE);
		check(ctx, Math.abs(Medicine.fissionFactor(patient) - 0.35) < 1e-9, "with Prussian blue: 35%");
		Medicine.take(patient, Medicine.AMIFOSTINE);
		check(ctx, Medicine.harmFactor(patient) == 0.5, "amifostine halves the harm");
		ZombieVillagerEntity zv = ctx.spawnEntity(EntityType.ZOMBIE_VILLAGER, new BlockPos(3, 2, 1));
		check(ctx, !Medicine.cureZombie(zv, null), "penicillin alone does not start a cure");
		zv.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600));
		check(ctx, Medicine.cureZombie(zv, null) && zv.isConverting(), "penicillin and Weakness start the cure");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void shapedAndAnfoCharges(TestContext ctx) {
		BlockPos c = new BlockPos(1, 2, 1);
		for (int i = 1; i <= 17; i++) {
			ctx.setBlockState(c.east(i), Blocks.OBSIDIAN);
		}
		ctx.setBlockState(c, Life.SHAPED_CHARGE.getDefaultState().with(ShapedChargeBlock.FACING, Direction.EAST));
		MiningChargeBlock.detonate(ctx.getWorld(), ctx.getAbsolutePos(c));
		check(ctx, ctx.getBlockState(c.east(1)).isAir() && ctx.getBlockState(c.east(16)).isAir(), "16 blocks bored");
		check(ctx, ctx.getBlockState(c.east(17)).isOf(Blocks.OBSIDIAN), "the 17th stays");
		BlockPos q = new BlockPos(5, 6, 5);
		for (BlockPos p : BlockPos.iterate(q.add(-4, -4, -4), q.add(4, 4, 4))) {
			ctx.setBlockState(p, Blocks.STONE);
		}
		ctx.setBlockState(q, Life.ANFO_CHARGE);
		MiningChargeBlock.detonate(ctx.getWorld(), ctx.getAbsolutePos(q));
		check(ctx, ctx.getBlockState(q.add(4, 4, 4)).isAir() && ctx.getBlockState(q.add(-4, -4, -4)).isAir(), "9x9x9 broken");
		Box box = new Box(ctx.getAbsolutePos(q)).expand(7);
		int cobble = ctx.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> e.getStack().isOf(Items.COBBLESTONE))
				.stream().mapToInt(e -> e.getStack().getCount()).sum();
		check(ctx, cobble > 480 && cobble < 610, "about three in four of 728 drop: " + cobble);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void treatedGround(TestContext ctx) {
		var world = ctx.getWorld();
		FieldState fields = FieldState.of(world);
		BlockPos ground = ctx.getAbsolutePos(new BlockPos(3, 1, 3));
		fields.add(FieldState.Kind.DDT, ground.up(), 4, world.getTime() + 1000);
		SpiderEntity spider = ctx.spawnEntity(EntityType.SPIDER, new BlockPos(3, 2, 3));
		spider.setAiDisabled(true);
		ChickenEntity hen = ctx.spawnEntity(EntityType.CHICKEN, new BlockPos(4, 2, 3));
		hen.setAiDisabled(true);
		float hp = spider.getHealth();
		int eggs = hen.eggLayTime;
		Life.tickFields(world);
		check(ctx, spider.getHealth() < hp, "DDT ground hurts the spider");
		check(ctx, hen.eggLayTime == eggs + Life.EGG_DELAY, "and holds the hen back");
		fields.add(FieldState.Kind.PRE_EMERGENT, ground, 4, world.getTime() + 1000);
		check(ctx, fields.treated(FieldState.Kind.PRE_EMERGENT, ground.east(2), world.getTime()), "pre-emergent ground");
		check(ctx, !fields.treated(FieldState.Kind.PRE_EMERGENT, ground.east(9), world.getTime()), "outside it is not");
		// Weed Killer: the poppy dies, the grass and the wheat live.
		BlockPos w = new BlockPos(1, 2, 7);
		ctx.setBlockState(w.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(w, Blocks.POPPY);
		ctx.setBlockState(w.east().down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(w.east(), Blocks.SHORT_GRASS);
		ctx.setBlockState(w.east(2).down(), Blocks.FARMLAND);
		ctx.setBlockState(w.east(2), Blocks.WHEAT);
		ctx.setBlockState(w.east(3).down(), Blocks.FARMLAND);
		ctx.setBlockState(w.east(3), Blocks.WHEAT);
		SprayerItem.kill(world, ctx.getAbsolutePos(w), SprayerItem.Kind.WEED_KILLER);
		check(ctx, ctx.getBlockState(w).isAir() && ctx.getBlockState(w.east()).isOf(Blocks.SHORT_GRASS)
				&& ctx.getBlockState(w.east(2)).isOf(Blocks.WHEAT), "weed killer spares grass and grain");
		// Field Clear: everything dies but the tolerant wheat.
		fields.plantTolerant(ctx.getAbsolutePos(w.east(3)));
		SprayerItem.kill(world, ctx.getAbsolutePos(w), SprayerItem.Kind.FIELD_CLEAR);
		check(ctx, ctx.getBlockState(w.east()).isAir() && !ctx.getBlockState(w.east(2)).isOf(Blocks.WHEAT)
				&& ctx.getBlockState(w.east(3)).isOf(Blocks.WHEAT), "field clear spares only tolerant wheat");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void insulationKeepsIce(TestContext ctx) {
		BlockPos ice = new BlockPos(2, 2, 2);
		ctx.setBlockState(ice, Blocks.ICE);
		ctx.setBlockState(ice.east(), Blocks.GLOWSTONE);
		ctx.setBlockState(ice.west(), Life.INSULATION);
		BlockPos bare = new BlockPos(6, 2, 2);
		ctx.setBlockState(bare, Blocks.ICE);
		ctx.setBlockState(bare.east(), Blocks.GLOWSTONE);
		ctx.runAtTick(5, () -> {
			for (int i = 0; i < 20; i++) {
				ctx.getBlockState(ice).randomTick(ctx.getWorld(), ctx.getAbsolutePos(ice), ctx.getWorld().random);
				if (ctx.getBlockState(bare).isOf(Blocks.ICE)) {
					ctx.getBlockState(bare).randomTick(ctx.getWorld(), ctx.getAbsolutePos(bare), ctx.getWorld().random);
				}
			}
			check(ctx, ctx.getBlockState(ice).isOf(Blocks.ICE), "insulated ice stays");
			check(ctx, !ctx.getBlockState(bare).isOf(Blocks.ICE), "bare ice by a light melts");
			ctx.complete();
		});
	}
}
