package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.machine.StorageTankBlockEntity;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

/** 1.5.0: liquids and gases in Glass Flasks, compressed gas in Steel Cylinders, the Compressor and the Storage Tank. */
public class FluidTests implements FabricGameTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

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
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
		return be;
	}

	private static void tick(TestContext ctx, MachineBlockEntity be, int limit) {
		BlockPos abs = ctx.getAbsolutePos(POS);
		for (int t = 0; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
					&& (be.status() == MachineBlockEntity.Status.IDLE || be.status() == MachineBlockEntity.Status.NEEDS_FLASKS)) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: status {}, outputs {} {} {} | flasks {} | cylinders {} | buffers {}",
				be.spec().id(), be.status(), be.getStack(2), be.getStack(3), be.getStack(4),
				be.getStack(MachineBlockEntity.FLASKS), be.getStack(MachineBlockEntity.CYLINDERS), be.outputBuffers());
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

	private static MachineBlockEntity trough(TestContext ctx, List<String> projects, int saltpetre) {
		MachineBlockEntity be = place(ctx, "pneumatic_trough", projects);
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("potassium_nitrate"), saltpetre));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		return be;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void gasesWaitForFlasks(TestContext ctx) {
		// 9 saltpetre -> 139 N and 396 O: with no flasks the trough stops and asks for them.
		MachineBlockEntity be = trough(ctx, List.of("pneumatic_chemistry"), 9);
		tick(ctx, be, 40_000);
		check(ctx, be.status() == MachineBlockEntity.Status.NEEDS_FLASKS, "expected NEEDS_FLASKS, got " + be.status());
		expect(ctx, count(be, mod("oxygen")), 0, "oxygen without flasks");
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 10));
		tick(ctx, be, 40_000);
		expect(ctx, count(be, mod("oxygen")), 3, "oxygen");
		expect(ctx, count(be, mod("nitrogen")), 1, "nitrogen");
		expect(ctx, be.getStack(MachineBlockEntity.FLASKS).getCount(), 6, "flasks left");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void reliefValveVentsGases(TestContext ctx) {
		// 3.29.0: with the valve open and no flasks, the trough vents its oxygen and nitrogen and keeps working.
		MachineBlockEntity be = trough(ctx, List.of("pneumatic_chemistry"), 9);
		be.setVenting(true);
		tick(ctx, be, 40_000);
		check(ctx, be.status() == MachineBlockEntity.Status.IDLE, "expected IDLE, got " + be.status());
		check(ctx, be.getStack(MachineBlockEntity.INPUT).isEmpty(), "all saltpetre used");
		expect(ctx, count(be, mod("oxygen")), 0, "oxygen flasks");
		double left = be.outputBuffers().getOrDefault("oxygen", 0.0) + be.outputBuffers().getOrDefault("nitrogen", 0.0);
		check(ctx, left < 2 * 111.2, "expected under one flask of each gas left waiting, got " + be.outputBuffers());
		// Flasks still come first: shut or open, a gas that has a container is kept.
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("potassium_nitrate"), 9));
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 10));
		tick(ctx, be, 40_000);
		check(ctx, count(be, mod("oxygen")) >= 3, "expected oxygen in flasks with the valve open, got " + count(be, mod("oxygen")));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void reliefValveStateIsSaved(TestContext ctx) {
		// The valve starts shut and is saved with the machine.
		MachineBlockEntity be = trough(ctx, List.of("pneumatic_chemistry"), 9);
		check(ctx, !be.venting(), "valve starts shut");
		be.setVenting(true);
		net.minecraft.nbt.NbtCompound nbt = be.createNbt(ctx.getWorld().getRegistryManager());
		check(ctx, nbt.getBoolean("Vent"), "valve state saved");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void compressorFillsCylinders(TestContext ctx) {
		// 27 saltpetre -> 1187 units of oxygen: one 1000-unit cylinder; nitrogen (416) waits for a full cylinder.
		MachineBlockEntity be = trough(ctx, List.of("pneumatic_chemistry", "bessemer", "gas_compression"), 27);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 16));
		be.setStack(MachineBlockEntity.CYLINDERS, new ItemStack(Fluids.STEEL_CYLINDER, 4));
		be.setStack(MachineBlockEntity.UPGRADE_FIRST, new ItemStack(mod("compressor")));
		tick(ctx, be, 120_000);
		expect(ctx, count(be, mod("oxygen_cylinder")), 1, "oxygen cylinders");
		expect(ctx, count(be, mod("oxygen")), 0, "oxygen flasks");
		expect(ctx, count(be, mod("nitrogen")), 0, "nitrogen flasks");
		expect(ctx, be.getStack(MachineBlockEntity.CYLINDERS).getCount(), 3, "empty cylinders left");
		expect(ctx, be.getStack(MachineBlockEntity.FLASKS).getCount(), 16, "flasks left");
		double o = be.outputBuffers().getOrDefault("oxygen", 0.0);
		check(ctx, o > 180 && o < 195, "expected 187 units of oxygen waiting, got " + o);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void compressorNeedsResearch(TestContext ctx) {
		// Without Gas Compression the Compressor does nothing: oxygen goes into flasks.
		MachineBlockEntity be = trough(ctx, List.of("pneumatic_chemistry"), 9);
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 16));
		be.setStack(MachineBlockEntity.CYLINDERS, new ItemStack(Fluids.STEEL_CYLINDER, 4));
		be.setStack(MachineBlockEntity.UPGRADE_FIRST, new ItemStack(mod("compressor")));
		tick(ctx, be, 40_000);
		expect(ctx, count(be, mod("oxygen")), 3, "oxygen flasks");
		expect(ctx, count(be, mod("oxygen_cylinder")), 0, "oxygen cylinders");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cylindersBurnAndComeBack(TestContext ctx) {
		Reactions.Fuel flask = Reactions.fuel(new ItemStack(mod("methane")));
		Reactions.Fuel cyl = Reactions.fuel(new ItemStack(mod("methane_cylinder")));
		check(ctx, cyl != null && Math.abs(cyl.energy() - 9 * flask.energy()) < 1, "a methane cylinder burns like nine flasks");
		// Coal in a Coke Oven fired with a methane cylinder: the empty cylinder comes back to its slot.
		MachineBlockEntity be = place(ctx, "coke_oven", List.of("coking"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.COAL, 9));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(mod("methane_cylinder"), 2));
		tick(ctx, be, 40_000);
		expect(ctx, be.getStack(MachineBlockEntity.CYLINDERS).getCount(), 1, "empty cylinder back");
		check(ctx, be.getStack(MachineBlockEntity.CYLINDERS).isOf(Fluids.STEEL_CYLINDER), "it is a steel cylinder");
		expect(ctx, count(be, mod("coke")), 7, "coke");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void flasksComeBackFromReagents(TestContext ctx) {
		// 4 iron with sulfuric acid in the reagent slot: each flask of acid used comes back empty.
		MachineBlockEntity be = place(ctx, "pneumatic_trough", List.of("pneumatic_chemistry"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.IRON_INGOT, 4));
		be.setStack(MachineBlockEntity.REAGENT, new ItemStack(mod("sulfuric_acid"), 8));
		tick(ctx, be, 40_000);
		int left = be.getStack(MachineBlockEntity.REAGENT).getCount();
		check(ctx, left < 8, "some acid was used");
		expect(ctx, be.getStack(MachineBlockEntity.FLASKS).getCount(), 8 - left, "empty flasks back");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void storageTankPoursAndDraws(TestContext ctx) {
		ctx.setBlockState(POS, block("storage_tank"));
		StorageTankBlockEntity tank = ctx.getBlockEntity(POS);
		tank.setStack(StorageTankBlockEntity.IN, new ItemStack(mod("chlorine"), 3));
		for (int i = 0; i < 5; i++) {
			tank.tick();
		}
		check(ctx, Math.abs(tank.units() - 333.33) < 0.1, "3 flasks in: " + tank.units());
		expect(ctx, tank.getStack(StorageTankBlockEntity.OUT).getCount(), 3, "empty flasks out");
		check(ctx, tank.canPour(new ItemStack(mod("chlorine_cylinder"))), "a chlorine cylinder pours in");
		ItemStack empty = tank.pour(new ItemStack(mod("chlorine_cylinder")));
		check(ctx, empty.isOf(Fluids.STEEL_CYLINDER), "the cylinder comes back");
		check(ctx, !tank.canPour(new ItemStack(mod("oxygen"))), "oxygen can't mix with chlorine");
		tank.setStack(StorageTankBlockEntity.OUT, ItemStack.EMPTY);
		tank.setStack(StorageTankBlockEntity.IN, new ItemStack(Fluids.GLASS_FLASK, 2));
		for (int i = 0; i < 5; i++) {
			tank.tick();
		}
		ItemStack out = tank.getStack(StorageTankBlockEntity.OUT);
		check(ctx, out.isOf(mod("chlorine")) && out.getCount() == 2, "2 chlorine flasks drawn, got " + out);
		check(ctx, Math.abs(tank.units() - 1111.1) < 0.1, "1111 units left: " + tank.units());
		for (int i = 0; i < 200; i++) {
			if (tank.canPour(new ItemStack(mod("chlorine_cylinder")))) {
				tank.pour(new ItemStack(mod("chlorine_cylinder")));
			}
		}
		check(ctx, tank.units() <= Fluids.tankUnits() + 1e-6 && tank.units() > Fluids.tankUnits() - 1000, "fills to 16000: " + tank.units());
		ctx.complete();
	}
}
