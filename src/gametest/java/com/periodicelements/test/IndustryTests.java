package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.CarbideLampItem;
import com.periodicelements.farm.Fertilizer;
import com.periodicelements.farm.Industry;
import com.periodicelements.farm.MiningChargeBlock;
import com.periodicelements.farm.WeldingTorchItem;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.medicine.Infection;
import com.periodicelements.medicine.Medicine;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
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

/** 1.9.0 Industrial organics: carbide, fertilizers from the air, plastics, synthetic rubber, TNT, sulfa, biodiesel. */
public class IndustryTests implements FabricGameTest {
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

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void carbideFromTheArc(TestContext ctx) {
		// 9 quicklime (1000 units) with coke: 1143 units of carbide, 10 items.
		MachineBlockEntity be = run(ctx, "electric_arc_furnace", List.of("dynamo", "arc_furnace", "lime_burning", "calcium_carbide"),
				of("calcium_oxide", 9), ItemStack.EMPTY, coke(), true);
		expect(ctx, be, "calcium_carbide", 10);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void acetyleneFromCarbide(TestContext ctx) {
		// 4 carbide (444 units) and water: 180 units of acetylene (1 flask) and slaked lime.
		MachineBlockEntity be = run(ctx, "digestion_vat", List.of("acid_digestion", "calcium_carbide"),
				of("calcium_carbide", 4), new ItemStack(Items.WATER_BUCKET), charcoal(), false);
		expect(ctx, be, "acetylene", 1);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void nitrolimeInTheKiln(TestContext ctx) {
		// 4 carbide (444 units) and nitrogen: 555 units of calcium cyanamide, 4 items (the fifth is a hair short).
		MachineBlockEntity be = run(ctx, "lime_kiln", List.of("lime_burning", "calcium_carbide", "calcium_cyanamide"),
				of("calcium_carbide", 4), of("nitrogen", 4), coke(), false);
		expect(ctx, be, "calcium_cyanamide", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void formaldehydeAndBakelite(TestContext ctx) {
		// 4 methanol (444 units): 416 units of formaldehyde, 3 flasks.
		MachineBlockEntity conv = run(ctx, "ostwald_converter", List.of("ostwald_process", "formaldehyde"),
				of("methanol", 4), ItemStack.EMPTY, coke(), false);
		expect(ctx, conv, "formaldehyde", 3);
		// 4 phenol (444 units) with formaldehyde: 506 units of Bakelite, 4 items.
		MachineBlockEntity clave = run(ctx, "autoclave", List.of("alizarin", "formaldehyde", "bakelite"),
				of("phenol", 4), of("formaldehyde", 4), charcoal(), false);
		expect(ctx, clave, "bakelite", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void ureaAndPrills(TestContext ctx) {
		// 2 ammonia (222 units) with carbon dioxide: 392 units of urea, 3 items.
		MachineBlockEntity hpr = run(ctx, "high_pressure_reactor", List.of("haber_process", "urea"),
				of("ammonia", 2), of("carbon_dioxide", 4), charcoal(), false);
		expect(ctx, hpr, "urea", 3);
		// 2 urea (222 units) with PK Blend: 555 units of NPK prills, 5 items.
		MachineBlockEntity tower = run(ctx, "prilling_tower", List.of("dynamo", "urea", "prilled_fertilizer"),
				of("urea", 2), of("pk_blend", 8), ItemStack.EMPTY, true);
		expect(ctx, tower, "npk_prills", 5);
		// 1 ammonia (111 units) with nitric acid: 523 units of ammonium nitrate, 4 items.
		MachineBlockEntity an = run(ctx, "prilling_tower", List.of("dynamo", "urea", "prilled_fertilizer"),
				of("ammonia", 1), of("nitric_acid", 8), ItemStack.EMPTY, true);
		expect(ctx, an, "ammonium_nitrate", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void npkPrillsGrowTwiceAsFast(TestContext ctx) {
		BlockPos field = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		Fertilizer f = Fertilizer.of(ctx.getWorld());
		f.fertilize(field, true);
		check(ctx, f.strong(field) && f.harvestsLeft(field) == 8, "NPK: strong, 8 harvests");
		f.harvested(field);
		check(ctx, f.strong(field) && f.harvestsLeft(field) == 7, "a harvest keeps NPK, 7 left");
		f.fertilize(field, false);
		check(ctx, f.strong(field) && f.harvestsLeft(field) == 8, "topping up keeps NPK");
		BlockPos plain = field.east(3);
		f.fertilize(plain, false);
		check(ctx, !f.strong(plain) && f.harvestsLeft(plain) == 8, "single fertilizer: not strong");
		check(ctx, Fertilizer.NPK_GROWTH == 2 * Fertilizer.EXTRA_GROWTH, "NPK gives twice the extra growth");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void tntAndTheDemolitionCharge(TestContext ctx) {
		// 2 toluene (222 units) with nitric acid: 548 units of TNT, 4 items.
		MachineBlockEntity vat = run(ctx, "digestion_vat", List.of("acid_digestion", "dynamite", "tar_distillation", "tnt"),
				of("toluene", 2), of("nitric_acid", 8), charcoal(), false);
		expect(ctx, vat, "trinitrotoluene", 4);
		BlockPos center = new BlockPos(3, 3, 3);
		for (BlockPos p : BlockPos.iterate(center.add(-1, -1, -1), center.add(1, 1, 1))) {
			ctx.setBlockState(p, Blocks.OBSIDIAN);
		}
		ctx.setBlockState(center.add(1, 1, 1), Blocks.BEDROCK);
		ctx.setBlockState(center, Industry.DEMOLITION_CHARGE);
		MiningChargeBlock.detonate(ctx.getWorld(), ctx.getAbsolutePos(center));
		check(ctx, ctx.getBlockState(center.add(-1, -1, -1)).isAir(), "obsidian broken");
		check(ctx, ctx.getBlockState(center.add(1, 1, 1)).isOf(Blocks.BEDROCK), "bedrock stays");
		Box box = new Box(ctx.getAbsolutePos(center)).expand(4);
		int obsidian = ctx.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> e.getStack().isOf(Items.OBSIDIAN))
				.stream().mapToInt(e -> e.getStack().getCount()).sum();
		check(ctx, obsidian == 25, "every obsidian drops: 25, got " + obsidian);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void acetoneFermentation(TestContext ctx) {
		// 18 wheat (1200 units of starch) with calcite: acetone 120 (1), butanol 240 (2).
		MachineBlockEntity be = run(ctx, "fermentation_barrel", List.of("fermentation", "mashing", "cordite", "acetone_fermentation"),
				new ItemStack(Items.WHEAT, 18), new ItemStack(Items.CALCITE), ItemStack.EMPTY, false);
		expect(ctx, be, "acetone", 1);
		expect(ctx, be, "butanol", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void neopreneAndTheWetsuit(TestContext ctx) {
		// 2 acetylene (222 units) with hydrochloric acid: 378 units of neoprene, 3 items.
		MachineBlockEntity hpr = run(ctx, "high_pressure_reactor", List.of("haber_process", "calcium_carbide", "neoprene"),
				of("acetylene", 2), of("hydrochloric_acid", 4), charcoal(), false);
		expect(ctx, hpr, "neoprene", 3);
		ZombieEntity diver = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		diver.equipStack(EquipmentSlot.HEAD, new ItemStack(Industry.WETSUIT[0]));
		check(ctx, !diver.canFreeze(), "one wetsuit piece keeps out the cold of powder snow");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bunaRubber(TestContext ctx) {
		List<String> p = List.of("thermal_cracking", "acid_digestion", "vulcanization", "buna_rubber");
		// 4 ethanol (444 units): 261 units of butadiene, 2 flasks.
		MachineBlockEntity crack = run(ctx, "cracking_unit", p, of("ethanol", 4), ItemStack.EMPTY, coke(), false);
		expect(ctx, crack, "butadiene", 2);
		// 2 benzene (222 units) with ethylene: 296 units of styrene, 2 flasks.
		MachineBlockEntity sty = run(ctx, "cracking_unit", p, of("benzene", 2), of("ethylene", 4), coke(), false);
		expect(ctx, sty, "styrene", 2);
		// 2 butadiene (222 units) with styrene: 296 units of Buna rubber, 2 items.
		MachineBlockEntity vat = run(ctx, "digestion_vat", p, of("butadiene", 2), of("styrene", 2), charcoal(), false);
		expect(ctx, vat, "buna_rubber", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void sulfaCuresSepsis(TestContext ctx) {
		// 2 aniline (222 units) with ammonium sulfate: 411 units of sulfanilamide, 3 items.
		MachineBlockEntity vat = run(ctx, "digestion_vat", List.of("acid_digestion", "aniline", "gas_liquor", "sulfa_drugs"),
				of("aniline", 2), of("ammonium_sulfate", 4), charcoal(), false);
		expect(ctx, vat, "sulfanilamide", 3);
		ZombieEntity patient = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		patient.addStatusEffect(new StatusEffectInstance(Infection.SEPSIS, -1));
		Medicine.takeSulfa(patient);
		check(ctx, !patient.hasStatusEffect(Infection.SEPSIS), "sulfanilamide cures sepsis");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void biodieselFromOil(TestContext ctx) {
		// 4 vegetable oil (444 units) with methanol: 447 units of biodiesel, 4 flasks.
		MachineBlockEntity be = run(ctx, "soap_kettle", List.of("rendering", "soap_boiling", "biodiesel"),
				of("vegetable_oil", 4), of("methanol", 2), charcoal(), false);
		expect(ctx, be, "biodiesel", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void lampAndTorch(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		// The lamp: lit until its carbide is spent, refilled from the pack.
		ItemStack lamp = new ItemStack(Industry.CARBIDE_LAMP);
		check(ctx, CarbideLampItem.lit(lamp), "a new lamp is lit");
		lamp.setDamage(lamp.getMaxDamage() - 1);
		check(ctx, !CarbideLampItem.lit(lamp), "a spent lamp is dark");
		player.getInventory().insertStack(of("calcium_carbide", 1));
		check(ctx, CarbideLampItem.refill(player, lamp) && lamp.getDamage() == 0, "carbide refills the lamp");
		// The torch: an iron ingot mends a quarter of an iron pickaxe.
		ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
		pick.setDamage(200);
		ItemStack torch = new ItemStack(Industry.WELDING_TORCH);
		check(ctx, !WeldingTorchItem.weld(player, torch, pick), "no ingot, no weld");
		player.getInventory().insertStack(new ItemStack(Items.IRON_INGOT, 2));
		check(ctx, WeldingTorchItem.weld(player, torch, pick), "an iron ingot welds iron");
		int mend = (int) Math.ceil(Items.IRON_PICKAXE.getDefaultStack().getMaxDamage() * WeldingTorchItem.MEND);
		check(ctx, pick.getDamage() == 200 - mend && torch.getDamage() == 1, "mended by a quarter: " + pick.getDamage());
		int ingots = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isOf(Items.IRON_INGOT)) {
				ingots += player.getInventory().getStack(i).getCount();
			}
		}
		check(ctx, ingots == 1, "one ingot used, got " + ingots + " left");
		ItemStack diamond = new ItemStack(Items.DIAMOND_PICKAXE);
		diamond.setDamage(100);
		check(ctx, !WeldingTorchItem.weld(player, torch, diamond), "iron does not weld diamond");
		player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
		ctx.complete();
	}
}
