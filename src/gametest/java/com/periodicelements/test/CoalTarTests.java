package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.Farm;
import com.periodicelements.farm.InsectSprayItem;
import com.periodicelements.farm.MiningChargeBlock;
import com.periodicelements.farm.Sprayed;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.medicine.Infection;
import com.periodicelements.medicine.Medicine;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 1.8.0 Coal tar and dyes: tar, dyes, drugs, explosives, Infection, and the new items. */
public class CoalTarTests implements FabricGameTest {
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

	/** Runs until idle; returns the peak temperature. */
	private static double run(TestContext ctx, MachineBlockEntity be, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		double peak = 0;
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			peak = Math.max(peak, be.temperature());
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: {} ticks, peak {}, outputs {} {} {} residue {} | buffers {}", be.spec().id(), t,
				(int) peak, be.getStack(2), be.getStack(3), be.getStack(4), be.getStack(5), be.outputBuffers());
		return peak;
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

	private static MachineBlockEntity load(MachineBlockEntity be, Item input, int n, Item reagent, int r) {
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(input, n));
		if (reagent != null) {
			be.setStack(MachineBlockEntity.REAGENT, new ItemStack(reagent, r));
		}
		return be;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cokeOvenCatchesTar(TestContext ctx) {
		// 64 coal hold 355 units of hydrogen: 533 units of tar, 4 flasks. The coke is unchanged.
		MachineBlockEntity be = load(place(ctx, "coke_oven", "coking", "alembic_distillation", "tar_distillation"),
				Items.COAL, 64, null, 0);
		run(ctx, be, 60_000);
		expect(ctx, be, "coal_tar", 4);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void tarStillCutsTar(TestContext ctx) {
		// 18 tar (2000 units): benzene 300 (2), toluene 100 (0), phenol 200 (1), anthracene 160 (1), pitch 1240 (11).
		MachineBlockEntity be = load(place(ctx, "tar_still", "coking", "alembic_distillation", "tar_distillation"),
				mod("coal_tar"), 18, null, 0);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 8)); // room for the tar's empties
		double peak = run(ctx, be, 30_000);
		expect(ctx, be, "benzene", 2);
		expect(ctx, be, "toluene", 0);
		expect(ctx, be, "phenol", 1);
		expect(ctx, be, "anthracene", 1);
		expect(ctx, be, "pitch", 11);
		int fuel = 64 - be.getStack(MachineBlockEntity.FUEL).getCount();
		// A Vacuum Pump: the same cuts, with the still about 100 °C cooler.
		MachineBlockEntity vac = load(place(ctx, "tar_still", "coking", "alembic_distillation", "tar_distillation",
				"steam_power", "vacuum_distillation"), mod("coal_tar"), 18, null, 0);
		vac.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 8));
		vac.setStack(MachineBlockEntity.UPGRADE_FIRST, new ItemStack(mod("vacuum_pump")));
		double vacPeak = run(ctx, vac, 30_000);
		expect(ctx, vac, "benzene", 2);
		check(ctx, vacPeak < peak - 80, "vacuum still should run cooler: " + vacPeak + " vs " + peak);
		int vacFuel = 64 - vac.getStack(MachineBlockEntity.FUEL).getCount();
		PeriodicElements.LOGGER.info("[gametest] tar still fuel: {} plain, {} with a vacuum pump", fuel, vacFuel);
		check(ctx, vacFuel <= fuel, "the vacuum still should burn no more fuel: " + vacFuel + " vs " + fuel);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void anilineAndFuchsine(TestContext ctx) {
		MachineBlockEntity vat = load(place(ctx, "digestion_vat", "acid_digestion", "nitrobenzene", "aniline"),
				mod("nitrobenzene"), 4, Items.IRON_INGOT, 8);
		run(ctx, vat, 30_000);
		expect(ctx, vat, "aniline", 3);
		// 2 aniline: 133 units of fuchsine, 38 magenta dyes.
		MachineBlockEntity dye = load(place(ctx, "dye_vat", "prussian_blue", "aniline", "fuchsine"),
				mod("aniline"), 2, mod("arsenic_trioxide"), 2);
		run(ctx, dye, 30_000);
		expect(ctx, dye, "minecraft:magenta_dye", 38);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void prussianBlueFromRottenFlesh(TestContext ctx) {
		// 9 rotten flesh: 250 units of protein, 50 of pigment, 3 blue dyes (8 per item of pigment).
		MachineBlockEntity be = load(place(ctx, "dye_vat", "prussian_blue"), Items.ROTTEN_FLESH, 9, mod("iron_sulfate"), 4);
		run(ctx, be, 30_000);
		expect(ctx, be, "minecraft:blue_dye", 3);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void picricAcidDyesYellow(TestContext ctx) {
		MachineBlockEntity vat = load(place(ctx, "digestion_vat", "acid_digestion", "picric_acid"), mod("phenol"), 2,
				mod("nitric_acid"), 8);
		run(ctx, vat, 30_000);
		expect(ctx, vat, "picric_acid", 4);
		MachineBlockEntity dye = load(place(ctx, "dye_vat", "prussian_blue", "picric_acid"), mod("picric_acid"), 1, null, 0);
		run(ctx, dye, 30_000);
		expect(ctx, dye, "minecraft:yellow_dye", 32);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void alizarinInTheAutoclave(TestContext ctx) {
		MachineBlockEntity be = load(place(ctx, "autoclave", "alizarin"), mod("anthracene"), 2, mod("sodium_hydroxide"), 2);
		run(ctx, be, 30_000);
		expect(ctx, be, "alizarin", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void aspirinFromPhenol(TestContext ctx) {
		MachineBlockEntity clave = load(place(ctx, "autoclave", "alizarin", "aspirin"), mod("phenol"), 2,
				mod("sodium_carbonate"), 4);
		run(ctx, clave, 30_000);
		expect(ctx, clave, "salicylic_acid", 2);
		expect(ctx, clave, "sodium_hydroxide", 1);
		MachineBlockEntity vat = load(place(ctx, "digestion_vat", "acid_digestion", "aspirin"), mod("salicylic_acid"), 2,
				mod("acetic_acid"), 2);
		run(ctx, vat, 30_000);
		expect(ctx, vat, "aspirin", 2);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void nitroglycerinAndCordite(TestContext ctx) {
		MachineBlockEntity vat = load(place(ctx, "digestion_vat", "acid_digestion", "dynamite"), mod("glycerol"), 2,
				mod("nitric_acid"), 8);
		vat.setStack(MachineBlockEntity.FUEL, ItemStack.EMPTY);
		run(ctx, vat, 30_000);
		expect(ctx, vat, "nitroglycerin", 4);
		MachineBlockEntity cord = load(place(ctx, "digestion_vat", "acid_digestion", "dynamite", "cordite"),
				mod("nitroglycerin"), 2, mod("smokeless_powder"), 4);
		cord.setStack(MachineBlockEntity.FUEL, ItemStack.EMPTY);
		run(ctx, cord, 30_000);
		expect(ctx, cord, "cordite", 3);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pyrethrumAndBlueVitriol(TestContext ctx) {
		MachineBlockEntity press = load(place(ctx, "oil_press", "oil_pressing", "insect_powder"), Items.OXEYE_DAISY, 9, null, 0);
		press.setStack(MachineBlockEntity.FUEL, ItemStack.EMPTY);
		run(ctx, press, 30_000);
		expect(ctx, press, "pyrethrin", 1);
		MachineBlockEntity vat = load(place(ctx, "digestion_vat", "acid_digestion", "bordeaux_mixture"), Items.COPPER_INGOT, 2,
				mod("sulfuric_acid"), 16);
		run(ctx, vat, 30_000);
		expect(ctx, vat, "copper_sulfate", 5);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void infectionStagesAndCures(TestContext ctx) {
		ZombieEntity patient = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		Infection.infect(patient);
		check(ctx, patient.hasStatusEffect(Infection.INFECTION), "infected");
		check(ctx, Infection.cureEarly(patient) && !patient.hasStatusEffect(Infection.INFECTION), "carbolic soap or aspirin cures stage 1");
		// Stage 1 runs out and turns to sepsis (forced): aspirin no longer helps; a sulfa drug or antibiotic would.
		try {
			Infection.forceRecovery = false;
			patient.addStatusEffect(new StatusEffectInstance(Infection.INFECTION, 1));
			Infection.INFECTION.value().applyUpdateEffect(patient, 0);
		} finally {
			Infection.forceRecovery = null;
		}
		check(ctx, patient.hasStatusEffect(Infection.SEPSIS), "untreated, stage 1 can become sepsis");
		Infection.clearExceptDisease(patient);
		check(ctx, patient.hasStatusEffect(Infection.SEPSIS), "milk does not cure sepsis");
		Medicine.takeAspirin(patient);
		check(ctx, patient.hasStatusEffect(Infection.SEPSIS), "aspirin does not cure sepsis");
		check(ctx, Infection.cureAll(patient) && !patient.hasStatusEffect(Infection.SEPSIS), "antibiotics cure sepsis");
		// With the game rule off, nothing is ever infected.
		var rule = ctx.getWorld().getGameRules().get(Infection.RULE);
		rule.set(false, ctx.getWorld().getServer());
		for (int i = 0; i < 2000; i++) {
			Infection.maybeInfect(patient, 1);
		}
		rule.set(true, ctx.getWorld().getServer());
		check(ctx, !patient.hasStatusEffect(Infection.INFECTION), "the game rule turns infection off");
		Infection.maybeInfect(patient, 1);
		check(ctx, patient.hasStatusEffect(Infection.INFECTION), "odds of 1 in 1 always infect");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void miningChargeKeepsEveryBlock(TestContext ctx) {
		BlockPos center = new BlockPos(3, 3, 3);
		for (BlockPos p : BlockPos.iterate(center.add(-2, -2, -2), center.add(2, 2, 2))) {
			ctx.setBlockState(p, Blocks.STONE);
		}
		ctx.setBlockState(center.add(-2, -2, -2), Blocks.OBSIDIAN);
		ctx.setBlockState(center.add(2, 2, 2), Blocks.DIAMOND_ORE);
		ctx.setBlockState(center, Farm.MINING_CHARGE);
		BlockPos abs = ctx.getAbsolutePos(center);
		MiningChargeBlock.detonate(ctx.getWorld(), abs);
		check(ctx, ctx.getBlockState(center.add(-2, -2, -2)).isOf(Blocks.OBSIDIAN), "obsidian stops the charge");
		check(ctx, ctx.getBlockState(center.add(1, 1, 1)).isAir() && ctx.getBlockState(center.add(2, 2, 2)).isAir(), "rock broken");
		Box box = new Box(abs).expand(4);
		int cobble = ctx.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> e.getStack().isOf(Items.COBBLESTONE))
				.stream().mapToInt(e -> e.getStack().getCount()).sum();
		int diamond = ctx.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> e.getStack().isOf(Items.DIAMOND))
				.stream().mapToInt(e -> e.getStack().getCount()).sum();
		check(ctx, cobble == 122 && diamond == 1, "every block drops: 122 cobblestone and 1 diamond, got " + cobble + " and " + diamond);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void insectSprayHitsOnlyPests(TestContext ctx) {
		ZombieEntity user = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 1));
		user.setAiDisabled(true);
		user.setYaw(0);
		user.setHeadYaw(0);
		user.setPitch(0);
		SpiderEntity spider = ctx.spawnEntity(EntityType.SPIDER, new BlockPos(2, 2, 3));
		spider.setAiDisabled(true);
		CowEntity cow = ctx.spawnEntity(EntityType.COW, new BlockPos(2, 2, 4));
		cow.setAiDisabled(true);
		float spiderHp = spider.getHealth(), cowHp = cow.getHealth();
		int hit = InsectSprayItem.spray(user);
		check(ctx, hit == 1 && spider.getHealth() < spiderHp && cow.getHealth() == cowHp,
				"spray hits the spider and spares the cow: hit " + hit + ", spider " + spider.getHealth() + ", cow " + cow.getHealth());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void bordeauxStopsBerryScratches(TestContext ctx) {
		BlockPos bush = new BlockPos(1, 1, 1);
		ctx.setBlockState(bush, Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 3));
		BlockPos abs = ctx.getAbsolutePos(bush);
		BlockState state = ctx.getWorld().getBlockState(abs);
		ZombieEntity walker = ctx.spawnEntity(EntityType.ZOMBIE, bush);
		walker.setAiDisabled(true);
		float hp = walker.getHealth();
		walker.age = 5;
		walker.lastRenderX = walker.getX() - 1;
		state.onEntityCollision(ctx.getWorld(), abs, walker);
		check(ctx, walker.getHealth() < hp, "an unsprayed bush scratches");
		Sprayed.of(ctx.getWorld()).spray(abs);
		walker.timeUntilRegen = 0;
		float hp2 = walker.getHealth();
		walker.lastRenderX = walker.getX() - 1;
		state.onEntityCollision(ctx.getWorld(), abs, walker);
		Sprayed.of(ctx.getWorld()).forget(abs);
		check(ctx, walker.getHealth() == hp2, "a sprayed bush does not");
		ctx.complete();
	}
}
