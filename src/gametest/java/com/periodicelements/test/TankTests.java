package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.fluid.FluidContent;
import com.periodicelements.fluid.FluidPumpBlock;
import com.periodicelements.fluid.TankBlocks;
import com.periodicelements.fluid.TankControllerBlockEntity;
import com.periodicelements.fluid.TankGaugeBlockEntity;
import com.periodicelements.fluid.TankStructure;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.StorageTankBlockEntity;
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
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.UUID;

/** 3.38.0: the multiblock tank, its gauge and compressor, Fluid Pipes between machines and tanks, the Fluid Pump. */
public class TankTests implements FabricGameTest {
	/** The controller of the test tank: in the middle of its floor. */
	private static final BlockPos CONTROLLER = new BlockPos(3, 1, 3);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static boolean near(double a, double b) {
		return Math.abs(a - b) < 0.5;
	}

	private static void clear(TestContext ctx) {
		for (int x = 0; x <= 7; x++) {
			for (int y = 1; y <= 7; y++) {
				for (int z = 0; z <= 7; z++) {
					ctx.setBlockState(new BlockPos(x, y, z), Blocks.AIR);
				}
			}
		}
	}

	/** A closed box of Tank Plates from (1,1,1) to (5,5,5), inside 3x3x3, the controller in the floor's middle. */
	private static TankControllerBlockEntity tank(TestContext ctx) {
		clear(ctx);
		for (int x = 1; x <= 5; x++) {
			for (int y = 1; y <= 5; y++) {
				for (int z = 1; z <= 5; z++) {
					boolean inside = x > 1 && x < 5 && y > 1 && y < 5 && z > 1 && z < 5;
					if (!inside) {
						ctx.setBlockState(new BlockPos(x, y, z), FluidContent.TANK_WALL);
					}
				}
			}
		}
		ctx.setBlockState(CONTROLLER, FluidContent.TANK_CONTROLLER);
		TankControllerBlockEntity c = ctx.getBlockEntity(CONTROLLER);
		c.measure();
		return c;
	}

	private static MachineBlockEntity machine(TestContext ctx, BlockPos at, String id, List<String> projects) {
		ctx.setBlockState(at, block(id));
		MachineBlockEntity be = ctx.getBlockEntity(at);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
		return be;
	}

	private static void run(TestContext ctx, MachineBlockEntity be, BlockPos at, int limit) {
		BlockPos abs = ctx.getAbsolutePos(at);
		for (int t = 0; t < limit; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] {}: status {}, buffers {}, flasks {}", be.spec().id(), be.status(),
				be.outputBuffers(), be.getStack(MachineBlockEntity.FLASKS));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void tankHoldsSixteenThousandPerBlock(TestContext ctx) {
		TankControllerBlockEntity c = tank(ctx);
		TankStructure.Result r = c.structure();
		check(ctx, r.valid() && r.dx() == 3 && r.dy() == 3 && r.dz() == 3 && r.volume() == 27, "3x3x3 inside: " + r);
		check(ctx, near(c.capacity(mod("sulfuric_acid")), 432_000) && near(c.capacity(mod("oxygen")), 432_000),
				"27 blocks x 16,000 of liquid or gas: " + c.capacity(mod("oxygen")));
		// A Tank Compressor in the shell: 50,000 of gas a block; liquids unchanged.
		ctx.setBlockState(new BlockPos(5, 3, 3), FluidContent.TANK_COMPRESSOR);
		c.measure();
		check(ctx, c.structure().compressed() && near(c.capacity(mod("oxygen")), 1_350_000), "gas with a compressor: " + c.capacity(mod("oxygen")));
		check(ctx, near(c.capacity(mod("sulfuric_acid")), 432_000), "liquid with a compressor: " + c.capacity(mod("sulfuric_acid")));
		// Flasks poured in by the input slot, as in the Storage Tank.
		c.setStack(TankControllerBlockEntity.IN, new ItemStack(mod("oxygen"), 3));
		for (int i = 0; i < 5; i++) {
			c.tick();
		}
		check(ctx, Math.abs(c.units() - 333.33) < 0.1 && c.getStack(TankControllerBlockEntity.OUT).isOf(Fluids.GLASS_FLASK)
				&& c.getStack(TankControllerBlockEntity.OUT).getCount() == 3, "3 flasks in: " + c.units());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void tankNeedsAClosedEmptyShell(TestContext ctx) {
		TankControllerBlockEntity c = tank(ctx);
		// A gap in an edge: no tank.
		ctx.setBlockState(new BlockPos(1, 5, 3), Blocks.AIR);
		c.measure();
		check(ctx, !c.valid() && c.fill(mod("oxygen"), 100, Double.NaN, false) == 0, "an open shell holds nothing: " + c.structure());
		// Stone in the shell is named.
		ctx.setBlockState(new BlockPos(1, 5, 3), Blocks.STONE);
		c.measure();
		check(ctx, c.structure().problem() == TankStructure.Problem.MISSING, "stone in the shell: " + c.structure());
		ctx.setBlockState(new BlockPos(1, 5, 3), FluidContent.TANK_WALL);
		// Something inside.
		ctx.setBlockState(new BlockPos(3, 3, 3), Blocks.STONE);
		c.measure();
		check(ctx, c.structure().problem() == TankStructure.Problem.NOT_BOX, "stone inside: " + c.structure());
		ctx.setBlockState(new BlockPos(3, 3, 3), Blocks.AIR);
		// Two controllers.
		ctx.setBlockState(new BlockPos(3, 5, 3), FluidContent.TANK_CONTROLLER);
		c.measure();
		check(ctx, c.structure().problem() == TankStructure.Problem.TWO_CONTROLLERS, "two controllers: " + c.structure());
		ctx.setBlockState(new BlockPos(3, 5, 3), FluidContent.TANK_WALL);
		c.measure();
		check(ctx, c.valid(), "closed again: " + c.structure());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void removingTheCompressorLetsGasOut(TestContext ctx) {
		TankControllerBlockEntity c = tank(ctx);
		ctx.setBlockState(new BlockPos(5, 3, 3), FluidContent.TANK_COMPRESSOR);
		c.measure();
		check(ctx, near(c.fill(mod("oxygen"), 600_000, Double.NaN, false), 600_000), "600,000 oxygen in: " + c.units());
		// Empty steel cylinders come out filled while the compressor is there.
		c.setStack(TankControllerBlockEntity.IN, new ItemStack(Fluids.STEEL_CYLINDER, 2));
		c.tick();
		c.tick();
		check(ctx, c.getStack(TankControllerBlockEntity.OUT).isOf(mod("oxygen_cylinder")) && c.getStack(TankControllerBlockEntity.OUT).getCount() == 2,
				"two oxygen cylinders: " + c.getStack(TankControllerBlockEntity.OUT));
		ctx.setBlockState(new BlockPos(5, 3, 3), FluidContent.TANK_WALL);
		c.measure();
		check(ctx, near(c.units(), 432_000), "the gas it can no longer hold escapes: " + c.units());
		// The relief valve lets a gas out; a liquid stays.
		check(ctx, near(c.vent(), 432_000) && c.fluid() == null, "vented");
		c.fill(mod("sulfuric_acid"), 1000, Double.NaN, false);
		check(ctx, c.vent() == 0 && near(c.units(), 1000), "a liquid is not vented");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void gaugeShowsPercent(TestContext ctx) {
		TankControllerBlockEntity c = tank(ctx);
		BlockPos at = new BlockPos(3, 3, 1);
		ctx.setBlockState(at, FluidContent.TANK_GAUGE.getDefaultState().with(TankBlocks.Gauge.FACING, Direction.NORTH));
		c.measure();
		c.fill(mod("sulfuric_acid"), 108_000, Double.NaN, false);
		TankGaugeBlockEntity g = ctx.getBlockEntity(at);
		g.update();
		check(ctx, g.permille() == 250, "a quarter full: " + g.permille());
		// Against a Storage Tank it reads that tank (the tank behind its face).
		BlockPos s = new BlockPos(6, 1, 6), gs = new BlockPos(6, 1, 5);
		ctx.setBlockState(s, block("storage_tank"));
		ctx.setBlockState(gs, FluidContent.TANK_GAUGE.getDefaultState().with(TankBlocks.Gauge.FACING, Direction.NORTH));
		StorageTankBlockEntity st = ctx.getBlockEntity(s);
		st.fill(mod("chlorine"), 8000, Double.NaN, false);
		TankGaugeBlockEntity g2 = ctx.getBlockEntity(gs);
		g2.update();
		check(ctx, g2.permille() == 500, "half a Storage Tank: " + g2.permille());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void machineSendsGasesDownPipes(TestContext ctx) {
		// 9 saltpetre in a trough, no flasks: the oxygen and nitrogen go down the pipe into two Storage Tanks.
		clear(ctx);
		BlockPos m = new BlockPos(1, 1, 1);
		MachineBlockEntity be = machine(ctx, m, "pneumatic_trough", List.of("pneumatic_chemistry"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("potassium_nitrate"), 9));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		ctx.setBlockState(new BlockPos(2, 1, 1), FluidContent.FLUID_PIPE);
		ctx.setBlockState(new BlockPos(3, 1, 1), FluidContent.FLUID_PIPE);
		ctx.setBlockState(new BlockPos(4, 1, 1), block("storage_tank"));
		ctx.setBlockState(new BlockPos(3, 1, 2), block("storage_tank"));
		run(ctx, be, m, 40_000);
		StorageTankBlockEntity a = ctx.getBlockEntity(new BlockPos(4, 1, 1)), b = ctx.getBlockEntity(new BlockPos(3, 1, 2));
		double oxygen = (a.fluid() == mod("oxygen") ? a.units() : 0) + (b.fluid() == mod("oxygen") ? b.units() : 0);
		double nitrogen = (a.fluid() == mod("nitrogen") ? a.units() : 0) + (b.fluid() == mod("nitrogen") ? b.units() : 0);
		check(ctx, be.status() == MachineBlockEntity.Status.IDLE, "no flasks needed: " + be.status());
		check(ctx, oxygen > 380 && nitrogen > 130, "oxygen " + oxygen + ", nitrogen " + nitrogen);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void machineDrawsReagentFromPipes(TestContext ctx) {
		// Iron in a trough, sulfuric acid only in a tank down the pipe: the acid comes by pipe, with no flasks.
		clear(ctx);
		BlockPos m = new BlockPos(1, 1, 1);
		MachineBlockEntity be = machine(ctx, m, "pneumatic_trough", List.of("pneumatic_chemistry"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.IRON_INGOT, 4));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		be.setVenting(true);
		ctx.setBlockState(new BlockPos(2, 1, 1), FluidContent.FLUID_PIPE);
		ctx.setBlockState(new BlockPos(3, 1, 1), block("storage_tank"));
		StorageTankBlockEntity acid = ctx.getBlockEntity(new BlockPos(3, 1, 1));
		acid.fill(mod("sulfuric_acid"), 8 * 1000.0 / 9, Double.NaN, false);
		run(ctx, be, m, 40_000);
		check(ctx, acid.units() < 8 * 1000.0 / 9 - 100, "acid drawn: " + acid.units());
		check(ctx, be.getStack(MachineBlockEntity.INPUT).getCount() < 4, "iron used: " + be.getStack(MachineBlockEntity.INPUT));
		check(ctx, be.getStack(MachineBlockEntity.FLASKS).isEmpty(), "no flasks made from nothing: " + be.getStack(MachineBlockEntity.FLASKS));
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void pumpMovesBetweenTanks(TestContext ctx) {
		clear(ctx);
		BlockPos a = new BlockPos(1, 1, 1), p = new BlockPos(2, 1, 1), pipe = new BlockPos(3, 1, 1);
		ctx.setBlockState(a, block("storage_tank"));
		ctx.setBlockState(p, FluidContent.FLUID_PUMP.getDefaultState().with(FluidPumpBlock.FACING, Direction.EAST));
		ctx.setBlockState(pipe, FluidContent.FLUID_PIPE);
		ctx.setBlockState(new BlockPos(4, 1, 1), block("storage_tank"));
		StorageTankBlockEntity from = ctx.getBlockEntity(a), to = ctx.getBlockEntity(new BlockPos(4, 1, 1));
		from.fill(mod("chlorine"), 1000, Double.NaN, false);
		FluidPumpBlock.Entity pump = ctx.getBlockEntity(p);
		BlockPos abs = ctx.getAbsolutePos(p);
		for (int i = 0; i < 5; i++) {
			pump.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, near(to.units(), 1000) && from.fluid() == null && to.fluid() == mod("chlorine"), "all moved: " + to.units());
		// It does not pump backwards.
		for (int i = 0; i < 5; i++) {
			pump.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, near(to.units(), 1000), "stays put");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void valveReachesTheTank(TestContext ctx) {
		// A pipe from a Storage Tank to a Tank Valve in the multiblock's wall; a pump pushes into the big tank.
		TankControllerBlockEntity c = tank(ctx);
		ctx.setBlockState(new BlockPos(5, 3, 3), FluidContent.TANK_VALVE);
		c.measure();
		ctx.setBlockState(new BlockPos(6, 3, 3), FluidContent.FLUID_PUMP.getDefaultState().with(FluidPumpBlock.FACING, Direction.WEST));
		ctx.setBlockState(new BlockPos(7, 3, 3), block("storage_tank"));
		StorageTankBlockEntity small = ctx.getBlockEntity(new BlockPos(7, 3, 3));
		small.fill(mod("sulfuric_acid"), 500, Double.NaN, false);
		FluidPumpBlock.Entity pump = ctx.getBlockEntity(new BlockPos(6, 3, 3));
		BlockPos abs = ctx.getAbsolutePos(new BlockPos(6, 3, 3));
		for (int i = 0; i < 3; i++) {
			pump.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
		}
		check(ctx, near(c.units(), 500) && c.fluid() == mod("sulfuric_acid"), "500 acid in the big tank through its valve: " + c.units());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void gaugeGoesOnAFinishedTank(TestContext ctx) {
		// The gauge used on a plate of a full tank takes its place; the tank keeps what it holds. One gauge per tank.
		TankControllerBlockEntity c = tank(ctx);
		c.fill(mod("sulfuric_acid"), 108_000, Double.NaN, false);
		var player = ctx.createMockPlayer(net.minecraft.world.GameMode.CREATIVE);
		ItemStack gauge = new ItemStack(FluidContent.TANK_GAUGE);
		BlockPos plate = new BlockPos(3, 3, 1), other = new BlockPos(1, 3, 3);
		for (BlockPos at : List.of(plate, other)) {
			BlockPos abs = ctx.getAbsolutePos(at);
			var hit = new net.minecraft.util.hit.BlockHitResult(net.minecraft.util.math.Vec3d.ofCenter(abs), Direction.NORTH, abs, false);
			ctx.getWorld().getBlockState(abs).onUseWithItem(gauge, ctx.getWorld(), player, net.minecraft.util.Hand.MAIN_HAND, hit);
		}
		check(ctx, ctx.getBlockState(plate).isOf(FluidContent.TANK_GAUGE), "the plate became a gauge: " + ctx.getBlockState(plate));
		check(ctx, ctx.getBlockState(other).isOf(FluidContent.TANK_WALL), "a second gauge is refused: " + ctx.getBlockState(other));
		check(ctx, c.valid() && near(c.units(), 108_000), "the tank keeps its acid: " + c.units());
		TankGaugeBlockEntity g = ctx.getBlockEntity(plate);
		g.update();
		check(ctx, g.permille() == 250, "a quarter full: " + g.permille());
		// A second gauge set by hand stops the tank, but nothing is lost.
		ctx.setBlockState(other, FluidContent.TANK_GAUGE);
		c.measure();
		check(ctx, c.structure().problem() == TankStructure.Problem.TWO_GAUGES && near(c.units(), 108_000), "two gauges: " + c.structure());
		ctx.complete();
	}
}
