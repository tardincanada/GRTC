package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
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
import java.util.UUID;

/** 1.6.0: machine upgrades. Each test runs the same job with and without the upgrade. */
public class UpgradeTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	/** A machine with the given research, 64 empty flasks and, if given, an upgrade fitted. */
	private static MachineBlockEntity place(TestContext ctx, String machine, List<String> projects, String upgrade) {
		ctx.setBlockState(POS, Blocks.AIR);
		ctx.setBlockState(POS, block(machine));
		MachineBlockEntity be = ctx.getBlockEntity(POS);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		List<String> all = new ArrayList<>(projects);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, all);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 64));
		if (upgrade != null) {
			be.setStack(MachineBlockEntity.UPGRADE_FIRST, new ItemStack(mod(upgrade)));
		}
		return be;
	}

	/** Ticks until the input is used up and the machine is idle; returns the ticks taken. */
	private static int tick(TestContext ctx, MachineBlockEntity be, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		int t = 0;
		for (; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {} {}: {} ticks, outputs {} {} {} | fuel {} | buffers {}", be.spec().id(),
				be.getStack(MachineBlockEntity.UPGRADE_FIRST), t, be.getStack(2), be.getStack(3), be.getStack(4),
				be.getStack(MachineBlockEntity.FUEL), be.outputBuffers());
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

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static MachineBlockEntity cokeOven(TestContext ctx, String upgrade, List<String> projects, int coal) {
		MachineBlockEntity be = place(ctx, "coke_oven", projects, upgrade);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.COAL, coal));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		return be;
	}

	private static MachineBlockEntity trough(TestContext ctx, String upgrade, List<String> projects) {
		MachineBlockEntity be = place(ctx, "pneumatic_trough", projects, upgrade);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("potassium_nitrate"), 27));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		return be;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void regeneratorSavesFuel(TestContext ctx) {
		// Saltpetre in the Pneumatic Trough: at 900 °C its walls lose more heat than the charcoal fire gives.
		MachineBlockEntity plain = trough(ctx, null, List.of("pneumatic_chemistry"));
		tick(ctx, plain, 160_000);
		int plainFuel = 64 - plain.getStack(MachineBlockEntity.FUEL).getCount();
		MachineBlockEntity regen = trough(ctx, "regenerator", List.of("pneumatic_chemistry", "regenerative_furnace"));
		tick(ctx, regen, 160_000);
		int regenFuel = 64 - regen.getStack(MachineBlockEntity.FUEL).getCount();
		check(ctx, count(regen, mod("oxygen")) == 10, "10 oxygen with the regenerator, got " + count(regen, mod("oxygen")));
		check(ctx, regenFuel < plainFuel * 0.8, "the regenerator should save fuel: " + regenFuel + " vs " + plainFuel);
		// Without the research, the regenerator does nothing.
		MachineBlockEntity unknown = trough(ctx, "regenerator", List.of("pneumatic_chemistry"));
		tick(ctx, unknown, 160_000);
		check(ctx, 64 - unknown.getStack(MachineBlockEntity.FUEL).getCount() == plainFuel, "an unresearched regenerator saves nothing");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void refractoryLiningRunsHotter(TestContext ctx) {
		// Pneumatic Trough (limit 900 °C) on charcoal (1100 °C flame).
		for (boolean lined : new boolean[]{false, true}) {
			MachineBlockEntity be = place(ctx, "pneumatic_trough", lined ? List.of("pneumatic_chemistry", "basic_refractories")
					: List.of("pneumatic_chemistry"), lined ? "refractory_lining" : null);
			be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("potassium_nitrate"), 9));
			be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
			BlockPos abs = ctx.getAbsolutePos(POS);
			for (int t = 0; t < 20; t++) {
				be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			}
			double want = lined ? 1050 : 900;
			check(ctx, Math.abs(be.maxC() - want) < 1e-6 && Math.abs(be.cap() - want) < 1e-6,
					(lined ? "lined" : "plain") + " trough should top out at " + want + ", got " + be.maxC() + " / " + be.cap());
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void stirrerWorksFaster(TestContext ctx) {
		int plain = tick(ctx, cokeOven(ctx, null, List.of("coking"), 27), 120_000);
		MachineBlockEntity stirred = cokeOven(ctx, "mechanical_stirrer", List.of("coking", "electric_motor"), 27);
		int fast = tick(ctx, stirred, 120_000);
		check(ctx, count(stirred, mod("coke")) == 21, "21 coke stirred, got " + count(stirred, mod("coke")));
		check(ctx, fast < plain * 0.85, "the stirrer should be faster: " + fast + " vs " + plain);
		ctx.complete();
	}

	private static MachineBlockEntity converter(TestContext ctx, String upgrade, List<String> projects) {
		MachineBlockEntity be = place(ctx, "contact_converter", projects, upgrade);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("sulfur"), 8));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(Items.WATER_BUCKET));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		return be;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void catalystBedDoublesRate(TestContext ctx) {
		int plain = tick(ctx, converter(ctx, null, List.of("contact_process")), 120_000);
		MachineBlockEntity bed = converter(ctx, "catalyst_bed", List.of("contact_process", "catalysis"));
		int fast = tick(ctx, bed, 120_000);
		check(ctx, count(bed, mod("sulfuric_acid")) == 24, "24 sulfuric acid, got " + count(bed, mod("sulfuric_acid")));
		check(ctx, fast < plain * 0.8, "the catalyst bed should be faster: " + fast + " vs " + plain);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void gasRecoveryHoodCatchesCarbonDioxide(TestContext ctx) {
		// 1 calcite -> 560 units of quicklime and 440 units of CO2, normally vented.
		for (boolean hood : new boolean[]{false, true}) {
			MachineBlockEntity be = place(ctx, "lime_kiln", hood ? List.of("lime_burning", "by_product_recovery")
					: List.of("lime_burning"), hood ? "gas_recovery_hood" : null);
			be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.CALCITE, 1));
			be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
			tick(ctx, be, 60_000);
			int co2 = count(be, mod("carbon_dioxide"));
			check(ctx, co2 == (hood ? 3 : 0), (hood ? "with" : "without") + " the hood: " + co2 + " carbon dioxide");
			check(ctx, count(be, mod("calcium_oxide")) == 5, "5 quicklime either way, got " + count(be, mod("calcium_oxide")));
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void outputPusherFillsChest(TestContext ctx) {
		BlockPos chestPos = POS.east();
		ctx.setBlockState(chestPos, Blocks.CHEST);
		MachineBlockEntity be = cokeOven(ctx, "output_pusher", List.of("coking", "automatic_mill"), 9);
		tick(ctx, be, 40_000);
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int t = 0; t < 40; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		ChestBlockEntity chest = ctx.getBlockEntity(chestPos);
		int inChest = 0;
		for (int i = 0; i < chest.size(); i++) {
			if (chest.getStack(i).isOf(mod("coke"))) {
				inChest += chest.getStack(i).getCount();
			}
		}
		check(ctx, inChest == 7 && count(be, mod("coke")) == 0, "7 coke pushed into the chest, got " + inChest + " (machine " + count(be, mod("coke")) + ")");
		ctx.complete();
	}
}
