package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.CleanEffect;
import com.periodicelements.farm.Farm;
import com.periodicelements.farm.Fertilizer;
import com.periodicelements.farm.TreeTapBlock;
import com.periodicelements.farm.TreeTapBlockEntity;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 1.7.0 Farm and fat: soap, oil, leather, mash, ether, alum, fertilizers, rubber, and the farm blocks. */
public class FarmTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static MachineBlockEntity place(TestContext ctx, String machine, String... projects) {
		ctx.setBlockState(POS, Blocks.AIR);
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, new ArrayList<>(List.of(projects)));
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 64));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		return be;
	}

	private static int run(TestContext ctx, MachineBlockEntity be, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, outputs {} {} {} residue {} | buffers {}", be.spec().id(), t,
				be.getStack(2), be.getStack(3), be.getStack(4), be.getStack(5), be.outputBuffers());
		return t;
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

	private static void expect(TestContext ctx, MachineBlockEntity be, String id, int want) {
		Item item = id.startsWith("minecraft:") ? Registries.ITEM.get(Identifier.of(id)) : mod(id);
		int got = count(be, item);
		if (got != want) {
			ctx.throwGameTestException(be.spec().id() + ": " + want + " " + id + " expected, got " + got);
		}
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void renderingGivesTallowAndProteinMeal(TestContext ctx) {
		// 9 porkchops: 25% tallow (250 units, 2 items) and 20% protein (200 units, 1 meal); the water boils off.
		MachineBlockEntity be = place(ctx, "soap_kettle", "rendering");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.PORKCHOP, 9));
		run(ctx, be, 20_000);
		expect(ctx, be, "tallow", 2);
		expect(ctx, be, "protein_meal", 1);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void softSoapFromTallowAndWoodAsh(TestContext ctx) {
		// 4 tallow need 70 units of potash: four wood ash give 89. Soft soap 108.6 per 100 tallow: 4 bars.
		MachineBlockEntity be = place(ctx, "soap_kettle", "rendering", "soap_boiling");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("tallow"), 4));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("wood_ash"), 8));
		run(ctx, be, 20_000);
		expect(ctx, be, "soft_soap", 4);
		expect(ctx, be, "tallow", 0);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void hardSoapFromSodaAsh(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "soap_kettle", "rendering", "soap_boiling", "hard_soap");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("tallow"), 4));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sodium_carbonate"), 2));
		run(ctx, be, 20_000);
		expect(ctx, be, "hard_soap", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void oilPressSqueezesSeeds(TestContext ctx) {
		// 9 pumpkin seeds: 45% oil = 450 units, 4 flasks; protein and fibre stay as oil cake (480 units, 4).
		MachineBlockEntity be = place(ctx, "oil_press", "oil_pressing");
		be.setStack(MachineBlockEntity.FUEL, ItemStack.EMPTY);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.PUMPKIN_SEEDS, 9));
		run(ctx, be, 20_000);
		expect(ctx, be, "vegetable_oil", 4);
		expect(ctx, be, "oil_cake", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void tanningPitTakesMonths(TestContext ctx) {
		// 4 rotten flesh (111 units of protein) and bark from an oak log: one leather after about three game days.
		MachineBlockEntity be = place(ctx, "tanning_pit", "leather_tanning", "pit_tanning");
		be.setStack(MachineBlockEntity.FUEL, ItemStack.EMPTY);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.ROTTEN_FLESH, 4));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(Items.OAK_LOG, 1));
		int t = run(ctx, be, 100_000);
		expect(ctx, be, "minecraft:leather", 1);
		check(ctx, t > 60_000 && t < 90_000, "tanning should take about 72000 ticks, took " + t);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void wheatMashFerments(TestContext ctx) {
		// 9 wheat: 600 units of starch -> 600 glucose -> 307 ethanol, 2 flasks.
		MachineBlockEntity be = place(ctx, "fermentation_barrel", "fermentation", "mashing");
		be.setStack(MachineBlockEntity.FUEL, ItemStack.EMPTY);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.WHEAT, 9));
		run(ctx, be, 20_000);
		expect(ctx, be, "ethanol", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void sweetOilOfVitriol(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "alembic", "alembic_distillation", "mineral_acids", "fermentation", "sweet_oil_of_vitriol");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("ethanol"), 4));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfuric_acid"), 1));
		run(ctx, be, 20_000);
		expect(ctx, be, "diethyl_ether", 3);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void alumFromClayInTwoSteps(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "crystallizer", "saltpeter_refining", "mineral_acids", "alum_making");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("kaolinite"), 4));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfuric_acid"), 8));
		run(ctx, be, 30_000);
		expect(ctx, be, "aluminium_sulfate", 5);
		MachineBlockEntity alum = place(ctx, "crystallizer", "saltpeter_refining", "mineral_acids", "alum_making");
		alum.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("aluminium_sulfate"), 4));
		alum.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("wood_ash"), 8));
		run(ctx, alum, 30_000);
		expect(ctx, alum, "potassium_alum", 8);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void gasLiquorAndSulfateOfAmmonia(TestContext ctx) {
		// 64 coal hold 107 units of nitrogen: 266 units of ammonia water, 2 flasks.
		MachineBlockEntity oven = place(ctx, "coke_oven", "coking", "lead_chamber", "gas_liquor");
		oven.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.COAL, 64));
		run(ctx, oven, 60_000);
		expect(ctx, oven, "ammonia_water", 2);
		MachineBlockEntity chamber = place(ctx, "lead_chamber", "lead_chamber", "coking", "gas_liquor");
		chamber.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("ammonia_water"), 4));
		chamber.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfuric_acid"), 8));
		run(ctx, chamber, 30_000);
		expect(ctx, chamber, "ammonium_sulfate", 7);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void superphosphateFromBoneMeal(TestContext ctx) {
		// 9 bone meal: 800 units of bone phosphate -> 604 superphosphate (5); the gypsum goes to the residue.
		MachineBlockEntity be = place(ctx, "digestion_vat", "acid_digestion", "superphosphate");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.BONE_MEAL, 9));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfuric_acid"), 8));
		run(ctx, be, 30_000);
		expect(ctx, be, "superphosphate", 5);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void starchSugarWithAcid(TestContext ctx) {
		MachineBlockEntity be = place(ctx, "digestion_vat", "acid_digestion", "fermentation", "mashing", "starch_sugar");
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.WHEAT, 9));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfuric_acid"), 1));
		run(ctx, be, 30_000);
		expect(ctx, be, "glucose", 5);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void vulcanizerCuresLatex(TestContext ctx) {
		// 9 flasks of latex: 350 units of rubber, cured with 3% sulfur -> 360 units, 3 items; the flasks come back.
		MachineBlockEntity be = place(ctx, "vulcanizer", "steam_power", "vulcanization");
		be.setStack(MachineBlockEntity.FLASKS, ItemStack.EMPTY);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("latex"), 9));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfur"), 2));
		run(ctx, be, 30_000);
		expect(ctx, be, "vulcanized_rubber", 3);
		check(ctx, be.getStack(MachineBlockEntity.FLASKS).getCount() == 9, "9 flasks back, got " + be.getStack(MachineBlockEntity.FLASKS));
		// Without sulfur the latex only dries to raw rubber.
		MachineBlockEntity dry = place(ctx, "vulcanizer", "steam_power", "vulcanization");
		dry.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("latex"), 9));
		run(ctx, dry, 30_000);
		expect(ctx, dry, "natural_rubber", 3);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void treeTapNeedsTheJungle(TestContext ctx) {
		BlockPos log = new BlockPos(1, 1, 1);
		BlockPos tapPos = log.south();
		ctx.setBlockState(log, Farm.RUBBER_LOG);
		ctx.setBlockState(tapPos, Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH));
		BlockPos abs = ctx.getAbsolutePos(tapPos);
		TreeTapBlockEntity tap = ctx.getBlockEntity(tapPos);
		BlockState state = ctx.getWorld().getBlockState(abs);
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
				String.format("fillbiome %d %d %d %d %d %d minecraft:plains", abs.getX() - 2, abs.getY() - 2, abs.getZ() - 2,
						abs.getX() + 2, abs.getY() + 2, abs.getZ() + 2));
		for (int t = 0; t < Farm.tapTicks() * 2; t++) {
			tap.tick(ctx.getWorld(), abs, state);
		}
		check(ctx, tap.latex() == 0, "no latex outside the jungle, got " + tap.latex());
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
				String.format("fillbiome %d %d %d %d %d %d minecraft:jungle", abs.getX() - 2, abs.getY() - 2, abs.getZ() - 2,
						abs.getX() + 2, abs.getY() + 2, abs.getZ() + 2));
		check(ctx, TreeTapBlockEntity.flows(ctx.getWorld(), abs, state), "the jungle biome should let latex flow");
		for (int t = 0; t < Farm.tapTicks() * 2; t++) {
			tap.tick(ctx.getWorld(), abs, state);
		}
		check(ctx, tap.latex() == 2, "2 flasks of latex in the jungle, got " + tap.latex());
		// A hopper's empty flask is filled and comes out below.
		tap.setStack(TreeTapBlockEntity.IN, new ItemStack(Fluids.GLASS_FLASK, 4));
		tap.tick(ctx.getWorld(), abs, state);
		tap.tick(ctx.getWorld(), abs, state);
		check(ctx, tap.getStack(TreeTapBlockEntity.OUT).isOf(Farm.LATEX) && tap.getStack(TreeTapBlockEntity.OUT).getCount() == 2,
				"2 latex out, got " + tap.getStack(TreeTapBlockEntity.OUT));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void fertilizedFarmlandGivesMore(TestContext ctx) {
		BlockPos soil = new BlockPos(1, 1, 1);
		BlockPos crop = soil.up();
		ctx.setBlockState(soil, Blocks.FARMLAND);
		BlockState ripe = Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 7);
		ctx.setBlockState(crop, ripe);
		Fertilizer f = Fertilizer.of(ctx.getWorld());
		f.fertilize(ctx.getAbsolutePos(soil));
		check(ctx, f.harvestsLeft(ctx.getAbsolutePos(soil)) == Farm.fertilizerHarvests(), "fresh fertilizer lasts 8 harvests");
		Farm.harvest(ctx.getWorld(), ctx.getAbsolutePos(crop), ripe);
		BlockPos a = ctx.getAbsolutePos(crop);
		int wheat = ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(a).expand(2), e -> e.getStack().isOf(Items.WHEAT))
				.stream().mapToInt(e -> e.getStack().getCount()).sum();
		check(ctx, wheat == 1, "one extra wheat, got " + wheat);
		check(ctx, f.harvestsLeft(ctx.getAbsolutePos(soil)) == Farm.fertilizerHarvests() - 1, "one harvest used");
		// Unripe crops give nothing extra and use no harvest.
		Farm.harvest(ctx.getWorld(), ctx.getAbsolutePos(crop), Blocks.WHEAT.getDefaultState());
		check(ctx, f.harvestsLeft(ctx.getAbsolutePos(soil)) == Farm.fertilizerHarvests() - 1, "an unripe crop uses no harvest");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void sneakersWheelsAndSoap(TestContext ctx) throws Exception {
		ZombieEntity zombie = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		check(ctx, !zombie.bypassesSteppingEffects(), "a zombie in plain feet steps loudly");
		zombie.equipStack(EquipmentSlot.FEET, new ItemStack(Farm.SNEAKERS));
		check(ctx, zombie.bypassesSteppingEffects(), "sneakers step softly");
		// Soap's Clean effect washes off Hunger.
		zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600));
		new CleanEffect().applyUpdateEffect(zombie, 0);
		check(ctx, !zombie.hasStatusEffect(StatusEffects.HUNGER), "Clean removes Hunger");
		MinecartEntity cart = ctx.spawnEntity(EntityType.MINECART, new BlockPos(2, 2, 2));
		var speed = AbstractMinecartEntity.class.getDeclaredMethod("getMaxSpeed");
		speed.setAccessible(true);
		double plain = (double) speed.invoke(cart);
		cart.addCommandTag(Farm.WHEELS_TAG);
		double rubber = (double) speed.invoke(cart);
		check(ctx, Math.abs(rubber - plain * 1.25) < 1e-9, "rubber wheels: " + rubber + " vs " + plain);
		ctx.complete();
	}
}
