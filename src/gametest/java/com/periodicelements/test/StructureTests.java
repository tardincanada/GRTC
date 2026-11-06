package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.multiblock.AnimatedPartBlock;
import com.periodicelements.multiblock.Multiblock;
import com.periodicelements.multiblock.Structures;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 3.3.0 multiblock structures: the smallest and largest of each are recognised with the gains in the plan, a wrong
 * block stops them and is named, ports reach the controller, and a structure really works faster.
 * Structures are built 60 blocks above the test so the big ones don't reach into the neighbouring tests.
 */
public class StructureTests implements FabricGameTest {
	private static final BlockPos AT = new BlockPos(2, 60, 2);

	private static Item mod(String id) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void check(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	/** Builds the template with its controller at AT (facing north) and returns the controller's position. */
	private static BlockPos build(TestContext ctx, Structures.Template t) {
		BlockPos c = ctx.getAbsolutePos(AT);
		t.build(ctx.getWorld(), c, Direction.NORTH);
		return c;
	}

	private static Structures.Result measure(TestContext ctx, Structures.Kind kind, BlockPos c) {
		return Structures.detect(kind, ctx.getWorld(), c, Direction.NORTH);
	}

	private static MachineBlockEntity controller(TestContext ctx, BlockPos c, List<String> projects) {
		MachineBlockEntity be = (MachineBlockEntity) ctx.getWorld().getBlockEntity(c);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner, projects);
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

	/** Ticks until the input is used up and the machine is idle; returns {ticks, fuel used}. */
	private static int[] run(ServerWorld world, BlockPos c, MachineBlockEntity be, int limit) {
		int fuel = be.getStack(MachineBlockEntity.FUEL).getCount();
		int t = 0;
		for (; t < limit; t++) {
			be.tick(world, c, world.getBlockState(c));
			if (be.getStack(MachineBlockEntity.RESIDUE).getCount() >= 60) {
				be.setStack(MachineBlockEntity.RESIDUE, ItemStack.EMPTY);
			}
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		return new int[]{t, fuel - be.getStack(MachineBlockEntity.FUEL).getCount()};
	}

	private static boolean near(double a, double b) {
		return Math.abs(a - b) < 0.05;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void blastFurnaceSmallestAndLargest(TestContext ctx) {
		Structures.Template small = Structures.smallest(Structures.Kind.BLAST_FURNACE);
		Structures.Template large = Structures.largest(Structures.Kind.BLAST_FURNACE);
		check(ctx, small.parts().size() == 33 && large.parts().size() == 185, "33 and 185 blocks: " + small.parts().size() + ", " + large.parts().size());
		BlockPos c = build(ctx, small);
		Structures.Result r = measure(ctx, Structures.Kind.BLAST_FURNACE, c);
		check(ctx, r != null && r.valid() && r.a() == 1 && r.h() == 3 && near(r.speed(), 1), "smallest furnace: " + r);
		check(ctx, r.ports().size() == 2 && r.tuyeres().size() == 1, "two ports and a tuyere: " + r);
		build(ctx, large);
		r = measure(ctx, Structures.Kind.BLAST_FURNACE, c);
		check(ctx, r != null && r.valid() && r.a() == 3 && r.h() == 10, "largest furnace: " + r);
		check(ctx, Math.abs(r.speed() - 18.0) < 0.1 && Math.abs(r.lossPerItem() - 0.55) < 0.01,
				"x18 and 55% heat per item: " + r.speed() + ", " + r.lossPerItem());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void wrongBlockStopsTheFurnace(TestContext ctx) {
		Structures.Template t = Structures.furnace(2, 5);
		BlockPos c = build(ctx, t);
		check(ctx, measure(ctx, Structures.Kind.BLAST_FURNACE, c).valid(), "2x2x5 furnace");
		// Layer 3, back wall: cobblestone instead of refractory brick.
		BlockPos wrong = t.toWorld(new BlockPos(2, 3, 3), c, Direction.NORTH);
		ctx.getWorld().setBlockState(wrong, Blocks.COBBLESTONE.getDefaultState());
		Structures.Result r = measure(ctx, Structures.Kind.BLAST_FURNACE, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && r.layer() == 3 && wrong.equals(r.missing())
				&& r.wanted() == Multiblock.REFRACTORY_BRICK, "missing brick in layer 3 at " + wrong + ": " + r);
		ctx.getWorld().setBlockState(wrong, Multiblock.REFRACTORY_BRICK.getDefaultState());
		// No Tuyere: the furnace stack can't be blown.
		ctx.getWorld().setBlockState(t.toWorld(new BlockPos(0, 1, 1), c, Direction.NORTH), Multiblock.REFRACTORY_BRICK.getDefaultState());
		r = measure(ctx, Structures.Kind.BLAST_FURNACE, c);
		check(ctx, r != null && r.problem() == Structures.Problem.NO_TUYERE, "no tuyere: " + r);
		// A lone furnace in the open is just a machine.
		BlockPos lone = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		ctx.getWorld().setBlockState(lone, Structures.Kind.BLAST_FURNACE.controllerBlock().getDefaultState());
		check(ctx, measure(ctx, Structures.Kind.BLAST_FURNACE, lone) == null, "a lone machine is not a structure");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void portsReachTheController(TestContext ctx) {
		Structures.Template t = Structures.furnace(1, 3);
		BlockPos c = build(ctx, t);
		MachineBlockEntity be = controller(ctx, c, List.of("blast_furnace"));
		be.tick(ctx.getWorld(), c, ctx.getWorld().getBlockState(c)); // measures and binds the ports
		BlockPos port = t.toWorld(new BlockPos(1, 3, 0), c, Direction.NORTH);
		Inventory inv = HopperBlockEntity.getInventoryAt(ctx.getWorld(), port);
		check(ctx, inv == be, "the port gives the controller's inventory: " + inv);
		ctx.getWorld().setBlockState(t.toWorld(new BlockPos(0, 2, 0), c, Direction.NORTH), Blocks.AIR.getDefaultState());
		for (int i = 0; i < 20; i++) {
			be.tick(ctx.getWorld(), c, ctx.getWorld().getBlockState(c));
		}
		check(ctx, HopperBlockEntity.getInventoryAt(ctx.getWorld(), port) == null, "a broken structure lets go of its ports");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void furnaceBlocksAnimateWhileSmelting(TestContext ctx) {
		// 3.33.0: the tuyere and the bricks of a working blast furnace switch to their animated look, and back.
		Structures.Template t = Structures.furnace(1, 3);
		BlockPos c = build(ctx, t);
		BlockPos tuyere = t.toWorld(new BlockPos(0, 1, 1), c, Direction.NORTH);
		BlockPos port = t.toWorld(new BlockPos(1, 3, 0), c, Direction.NORTH);
		check(ctx, ctx.getWorld().getBlockState(tuyere).isOf(Multiblock.TUYERE), "tuyere where expected");
		MachineBlockEntity be = controller(ctx, c, List.of("blast_furnace", "bellows"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.RAW_IRON, 16));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		int seen = 0;
		for (int i = 0; i < 120_000 && !(i > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty()
				&& be.status() == MachineBlockEntity.Status.IDLE); i++) {
			be.tick(ctx.getWorld(), c, ctx.getWorld().getBlockState(c));
			if (ctx.getWorld().getBlockState(tuyere).get(AnimatedPartBlock.ACTIVE)
					&& ctx.getWorld().getBlockState(port).get(AnimatedPartBlock.ACTIVE)) {
				seen++;
			}
		}
		check(ctx, seen > 100, "tuyere and port animated for only " + seen + " ticks");
		for (int i = 0; i < 60; i++) {
			be.tick(ctx.getWorld(), c, ctx.getWorld().getBlockState(c));
		}
		check(ctx, !ctx.getWorld().getBlockState(tuyere).get(AnimatedPartBlock.ACTIVE), "tuyere still animated after the furnace stopped");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void biggerFurnaceIsFaster(TestContext ctx) {
		List<String> projects = List.of("blast_furnace", "bellows");
		// The machine alone.
		BlockPos lone = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		ctx.getWorld().setBlockState(lone, Structures.Kind.BLAST_FURNACE.controllerBlock().getDefaultState());
		MachineBlockEntity alone = controller(ctx, lone, projects);
		alone.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.RAW_IRON, 64));
		alone.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		run(ctx.getWorld(), lone, alone, 120_000);
		// A second charge while hot: what a furnace in steady work burns.
		alone.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.RAW_IRON, 64));
		alone.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		int[] a = run(ctx.getWorld(), lone, alone, 120_000);
		// The same charge in a 2x2x5 stack (x5 speed).
		BlockPos c = build(ctx, Structures.furnace(2, 5));
		MachineBlockEntity stack = controller(ctx, c, projects);
		stack.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.RAW_IRON, 64));
		stack.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		run(ctx.getWorld(), c, stack, 120_000);
		stack.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.RAW_IRON, 64));
		stack.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		int[] s = run(ctx.getWorld(), c, stack, 120_000);
		PeriodicElements.LOGGER.info("[gametest] blast furnace alone {} ticks {} fuel, 2x2x5 stack {} ticks {} fuel",
				a[0], a[1], s[0], s[1]);
		int ironAlone = count(alone, mod("cast_iron_ingot")), ironStack = count(stack, mod("cast_iron_ingot"));
		check(ctx, ironAlone == ironStack && ironStack >= 60, "same cast iron: " + ironAlone + " / " + ironStack);
		check(ctx, s[0] * 2 < a[0], String.format(Locale.ROOT, "the stack should take under half the time: %d vs %d ticks", s[0], a[0]));
		check(ctx, s[1] <= a[1], "the stack should burn no more fuel: " + s[1] + " vs " + a[1]);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void cokeBatteryFiresItself(TestContext ctx) {
		Structures.Template small = Structures.battery(1);
		check(ctx, Structures.battery(4).parts().size() == 156, "4 ovens: 156 blocks, got " + Structures.battery(4).parts().size());
		BlockPos c = build(ctx, Structures.battery(12));
		Structures.Result r = measure(ctx, Structures.Kind.COKE_BATTERY, c);
		check(ctx, r != null && r.valid() && r.a() == 12 && r.selfHeated() && near(r.speed(), Math.pow(12, 0.85)), "12 ovens: " + r);
		// A hole in the roof of the third oven.
		BlockPos hole = Structures.battery(12).toWorld(new BlockPos(5, 3, 2), c, Direction.NORTH);
		ctx.getWorld().setBlockState(hole, Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.COKE_BATTERY, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && hole.equals(r.missing()) && r.layer() == 3,
				"hole in the roof: " + r);
		check(ctx, small.parts().size() == 54, "one oven: 54 blocks");
		// Four ovens make coke with no fuel at all.
		for (int x = 0; x < 25; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 5; z++) {
					ctx.getWorld().setBlockState(Structures.battery(12).toWorld(new BlockPos(x, y, z), c, Direction.NORTH), Blocks.AIR.getDefaultState());
				}
			}
		}
		build(ctx, Structures.battery(4));
		MachineBlockEntity be = controller(ctx, c, List.of("coking"));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(Items.COAL, 9));
		int[] t = run(ctx.getWorld(), c, be, 80_000);
		PeriodicElements.LOGGER.info("[gametest] 4-oven battery: {} ticks, coke {}", t[0], count(be, mod("coke")));
		check(ctx, count(be, mod("coke")) == 7, "7 coke from 9 coal with no fuel, got " + count(be, mod("coke"))
				+ "; " + be.status() + " at " + be.temperature() + " °C, " + be.structure());
		ctx.complete();
	}

	// ---- 3.8.0: lead chamber and distillation column ----

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void leadChamberSizesAndTower(TestContext ctx) {
		Structures.Template small = Structures.smallest(Structures.Kind.LEAD_CHAMBER);
		Structures.Template large = Structures.largest(Structures.Kind.LEAD_CHAMBER);
		check(ctx, small.parts().size() == 98 && large.parts().size() == 485, "98 and 485 blocks: " + small.parts().size() + ", " + large.parts().size());
		BlockPos c = build(ctx, small);
		Structures.Result r = measure(ctx, Structures.Kind.LEAD_CHAMBER, c);
		check(ctx, r != null && r.valid() && r.a() == 3 && r.b() == 3 && r.h() == 3 && near(r.speed(), 1) && near(r.loss(), 1)
				&& r.extra() == 0 && r.ports().size() == 1, "smallest chamber: " + r);
		build(ctx, large);
		r = measure(ctx, Structures.Kind.LEAD_CHAMBER, c);
		check(ctx, r != null && r.valid() && r.a() == 9 && r.b() == 9 && r.h() == 6 && r.extra() > 0
				&& near(r.speed(), Math.pow(18, 0.85) * 1.25), "largest chamber with its tower: " + r);
		// Two tower blocks are not a tower.
		BlockPos top = Structures.largest(Structures.Kind.LEAD_CHAMBER).toWorld(new BlockPos(11, 3, 1), c, Direction.NORTH);
		ctx.getWorld().setBlockState(top, Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.LEAD_CHAMBER, c);
		check(ctx, r != null && r.valid() && r.extra() == 0 && near(r.speed(), Math.pow(18, 0.85)), "two tower blocks: " + r);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void leadChamberProblems(TestContext ctx) {
		// A long room is fine: 5 across, 3 deep, 4 high.
		Structures.Template t = Structures.chamber(5, 3, 4, false);
		BlockPos c = build(ctx, t);
		Structures.Result r = measure(ctx, Structures.Kind.LEAD_CHAMBER, c);
		check(ctx, r != null && r.valid() && r.a() == 5 && r.b() == 3 && r.h() == 4
				&& near(r.speed(), Math.pow(60 / 27.0, 0.85)) && near(r.loss(), 2.0 * (15 + 20 + 12) / 54), "5x3x4 chamber: " + r);
		// Glass in the roof.
		BlockPos wrong = t.toWorld(new BlockPos(3, 5, 2), c, Direction.NORTH);
		ctx.getWorld().setBlockState(wrong, Blocks.GLASS.getDefaultState());
		r = measure(ctx, Structures.Kind.LEAD_CHAMBER, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && wrong.equals(r.missing()) && r.layer() == 5
				&& r.wanted() == Multiblock.LEAD_SHEET, "glass in the roof at " + wrong + ": " + r);
		// Too narrow: 2 deep.
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 7; y++) {
				for (int z = 0; z < 6; z++) {
					ctx.getWorld().setBlockState(t.toWorld(new BlockPos(x, y, z), c, Direction.NORTH), Blocks.AIR.getDefaultState());
				}
			}
		}
		build(ctx, Structures.chamber(3, 2, 3, false));
		r = measure(ctx, Structures.Kind.LEAD_CHAMBER, c);
		check(ctx, r != null && r.problem() == Structures.Problem.TOO_SMALL, "2 deep is too small: " + r);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void distillationColumnSizes(TestContext ctx) {
		check(ctx, Structures.smallest(Structures.Kind.DISTILLATION_COLUMN).parts().size() == 5
				&& Structures.largest(Structures.Kind.DISTILLATION_COLUMN).parts().size() == 145, "5 and 145 blocks");
		BlockPos c = build(ctx, Structures.column(1, 4));
		Structures.Result r = measure(ctx, Structures.Kind.DISTILLATION_COLUMN, c);
		check(ctx, r != null && r.valid() && r.a() == 1 && r.h() == 4 && near(r.speed(), 1) && r.extra() == 0 && r.ports().size() == 1,
				"1x1x4 column: " + r);
		ctx.getWorld().setBlockState(c.up(4), Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.DISTILLATION_COLUMN, c);
		check(ctx, r != null && r.problem() == Structures.Problem.TOO_SMALL, "3 trays are too few: " + r);
		build(ctx, Structures.largest(Structures.Kind.DISTILLATION_COLUMN));
		r = measure(ctx, Structures.Kind.DISTILLATION_COLUMN, c);
		check(ctx, r != null && r.valid() && r.a() == 3 && r.h() == 16 && near(r.speed(), Math.pow(9, 0.85)) && near(r.extra(), 0.4),
				"3x3x16 column: " + r);
		BlockPos gap = c.add(1, 5, 0);
		ctx.getWorld().setBlockState(gap, Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.DISTILLATION_COLUMN, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && gap.equals(r.missing()) && r.layer() == 5
				&& r.wanted() == Multiblock.DISTILLATION_TRAY, "gap in tray 5: " + r);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void tallColumnCutsTheBitumen(TestContext ctx) {
		List<String> projects = List.of("oil_refining");
		BlockPos lone = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		ctx.getWorld().setBlockState(lone, Structures.Kind.DISTILLATION_COLUMN.controllerBlock().getDefaultState());
		MachineBlockEntity alone = controller(ctx, lone, projects);
		alone.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("crude_oil"), 27));
		alone.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.COAL, 64));
		run(ctx.getWorld(), lone, alone, 120_000);
		BlockPos c = build(ctx, Structures.column(1, 16));
		MachineBlockEntity col = controller(ctx, c, projects);
		col.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("crude_oil"), 27));
		col.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.COAL, 64));
		run(ctx.getWorld(), c, col, 120_000);
		int b0 = count(alone, mod("bitumen")), b1 = count(col, mod("bitumen"));
		int n0 = count(alone, mod("naphtha")), n1 = count(col, mod("naphtha"));
		int k0 = count(alone, mod("kerosene")), k1 = count(col, mod("kerosene"));
		PeriodicElements.LOGGER.info("[gametest] crude x27: still alone bitumen {} naphtha {} kerosene {}; 16-tray column {} {} {}",
				b0, n0, k0, b1, n1, k1);
		// 3000 units: alone 750 bitumen, 660 naphtha, 450 kerosene; 16 trays send 40% of the bitumen up (22:15).
		check(ctx, b0 == 6 && b1 == 4, "bitumen 6 alone, 4 in the column: " + b0 + ", " + b1);
		check(ctx, n1 > n0 && k1 > k0, "more naphtha and kerosene: " + n0 + "->" + n1 + ", " + k0 + "->" + k1);
		ctx.complete();
	}

	// ---- 3.13.0: enrichment cascade and nuclear reactor ----

	private static net.minecraft.block.Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void cascadeSizes(TestContext ctx) {
		check(ctx, Structures.smallest(Structures.Kind.ENRICHMENT_CASCADE).parts().size() == 5
				&& Structures.largest(Structures.Kind.ENRICHMENT_CASCADE).parts().size() == 65, "5 and 65 blocks");
		BlockPos c = build(ctx, Structures.cascade(1, 1, 4));
		Structures.Result r = measure(ctx, Structures.Kind.ENRICHMENT_CASCADE, c);
		check(ctx, r != null && r.valid() && r.a() == 4 && near(r.speed(), 1) && near(r.extra(), 14.5) && r.ports().size() == 1,
				"4 stages: " + r);
		ctx.getWorld().setBlockState(c.north(-4), Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.ENRICHMENT_CASCADE, c);
		check(ctx, r != null && r.problem() == Structures.Problem.TOO_SMALL, "3 stages are too few: " + r);
		Structures.Template big = Structures.largest(Structures.Kind.ENRICHMENT_CASCADE);
		build(ctx, big);
		r = measure(ctx, Structures.Kind.ENRICHMENT_CASCADE, c);
		check(ctx, r != null && r.valid() && r.a() == 64 && near(r.speed(), Math.pow(16, 0.85)) && near(r.extra(), 18), "64 stages: " + r);
		BlockPos gap = big.toWorld(new BlockPos(2, 2, 3), c, Direction.NORTH);
		ctx.getWorld().setBlockState(gap, Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.ENRICHMENT_CASCADE, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && gap.equals(r.missing()) && r.layer() == 3
				&& r.wanted() == Multiblock.DIFFUSER_STAGE, "a stage missing in layer 3: " + r);
		// 2 x 2 x 8 = 32 stages, enrichment halfway.
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 1; z <= 4; z++) {
					ctx.getWorld().setBlockState(big.toWorld(new BlockPos(x, y, z), c, Direction.NORTH), Blocks.AIR.getDefaultState());
				}
			}
		}
		build(ctx, Structures.cascade(2, 2, 8));
		r = measure(ctx, Structures.Kind.ENRICHMENT_CASCADE, c);
		check(ctx, r != null && r.valid() && r.a() == 32 && r.h() == 8 && near(r.extra(), 14.5 + 3.5 * 28 / 60.0), "32 stages: " + r);
		ctx.complete();
	}

	/**
	 * Runs an electric machine fed by four steam engines and dynamos (in front, on both sides and on top): a big
	 * structure loses more heat and needs more current, as in a real cascade plant.
	 */
	private static void runPowered(ServerWorld world, BlockPos c, MachineBlockEntity be, int limit) {
		List<PowerBlockEntity> engines = new java.util.ArrayList<>(), dynamos = new java.util.ArrayList<>();
		for (Direction d : List.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.UP)) {
			world.setBlockState(c.offset(d), block("dynamo").getDefaultState());
			world.setBlockState(c.offset(d, 2), block("steam_engine").getDefaultState());
			dynamos.add((PowerBlockEntity) world.getBlockEntity(c.offset(d)));
			engines.add((PowerBlockEntity) world.getBlockEntity(c.offset(d, 2)));
		}
		for (int t = 0; t < limit; t++) {
			for (int k = 0; k < engines.size(); k++) {
				PowerBlockEntity engine = engines.get(k), dynamo = dynamos.get(k);
				if (engine.getStack(0).getCount() < 8) {
					engine.setStack(0, new ItemStack(Items.COAL, 64));
				}
				if (t % 200 == 0) {
					for (int i = 1; i < 9; i++) {
						if (!engine.getStack(i).isOf(Items.WATER_BUCKET)) {
							engine.setStack(i, new ItemStack(Items.WATER_BUCKET));
						}
					}
				}
				engine.tick(world, engine.getPos(), world.getBlockState(engine.getPos()));
				dynamo.tick(world, dynamo.getPos(), world.getBlockState(dynamo.getPos()));
			}
			be.tick(world, c, world.getBlockState(c));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		PeriodicElements.LOGGER.info("[gametest] powered run: status {} at {} °C", be.status(), be.temperature());
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void bigCascadeEnrichesMore(TestContext ctx) {
		List<String> projects = List.of("dynamo", "isotope_separation");
		BlockPos lone = ctx.getAbsolutePos(new BlockPos(4, 1, 4));
		ctx.getWorld().setBlockState(lone, Structures.Kind.ENRICHMENT_CASCADE.controllerBlock().getDefaultState());
		MachineBlockEntity alone = controller(ctx, lone, projects);
		alone.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("uranium_hexafluoride"), 27));
		runPowered(ctx.getWorld(), lone, alone, 200_000);
		BlockPos c = build(ctx, Structures.largest(Structures.Kind.ENRICHMENT_CASCADE));
		MachineBlockEntity big = controller(ctx, c, projects);
		big.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("uranium_hexafluoride"), 27));
		runPowered(ctx.getWorld(), c, big, 200_000);
		int e0 = count(alone, mod("enriched_uranium_hexafluoride")), e1 = count(big, mod("enriched_uranium_hexafluoride"));
		int d0 = count(alone, mod("depleted_uranium_hexafluoride")), d1 = count(big, mod("depleted_uranium_hexafluoride"));
		PeriodicElements.LOGGER.info("[gametest] UF6 x27: cascade alone {} enriched {} depleted; 64 stages {} {} ({})", e0, d0, e1, d1, big.structure());
		// 3000 units: alone 435 enriched (3 items), 64 stages 540 (4 items).
		check(ctx, e0 == 3 && e1 == 4 && d0 == 23 && d1 == 22, "enriched 3 -> 4, depleted 23 -> 22: " + e0 + ", " + e1 + ", " + d0 + ", " + d1);
		ctx.complete();
	}

	private static PowerBlockEntity reactorAt(TestContext ctx, BlockPos c) {
		return (PowerBlockEntity) ctx.getWorld().getBlockEntity(c);
	}

	private static void tickReactor(TestContext ctx, BlockPos c, int n) {
		PowerBlockEntity r = reactorAt(ctx, c);
		for (int i = 0; i < n; i++) {
			r.tick(ctx.getWorld(), c, ctx.getWorld().getBlockState(c));
		}
	}

	private static int countIn(PowerBlockEntity be, Item item) {
		int n = 0;
		for (int i = 0; i < be.size(); i++) {
			if (be.getStack(i).isOf(item)) {
				n += be.getStack(i).getCount();
			}
		}
		return n;
	}

	/** Water against the shielding, beside the controller. */
	private static void wet(TestContext ctx, Structures.Template t, BlockPos c) {
		ctx.getWorld().setBlockState(t.toWorld(new BlockPos(0, 2, 0), c, Direction.NORTH).north(), Blocks.WATER.getDefaultState());
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void reactorSizes(TestContext ctx) {
		Structures.Template small = Structures.smallest(Structures.Kind.NUCLEAR_REACTOR);
		BlockPos c = build(ctx, small);
		Structures.Result r = measure(ctx, Structures.Kind.NUCLEAR_REACTOR, c);
		check(ctx, r != null && r.valid() && r.a() == 3 && r.b() == 3 && r.h() == 3 && near(r.speed(), 4) && !r.selfHeated()
				&& near(r.extra(), Structures.nonLeakage(3, 3, 3)) && r.ports().size() == 1, "3x3x3 core, 12 channels, dry: " + r);
		check(ctx, Structures.k(3, 3, 3, false) > 1 && Structures.k(3, 3, 3, true) < 1, "small core: enriched critical, natural not");
		check(ctx, Structures.k(7, 7, 7, true) > 1 && Structures.k(7, 7, 6, true) < 1 && Structures.k(9, 9, 5, true) > 1,
				"natural uranium from 7x7x7 or 9x9x5");
		wet(ctx, small, c);
		r = measure(ctx, Structures.Kind.NUCLEAR_REACTOR, c);
		check(ctx, r != null && r.selfHeated(), "water touching the shielding: " + r);
		// A hole in the core.
		BlockPos hole = small.toWorld(new BlockPos(2, 2, 2), c, Direction.NORTH);
		ctx.getWorld().setBlockState(hole, Blocks.AIR.getDefaultState());
		r = measure(ctx, Structures.Kind.NUCLEAR_REACTOR, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && hole.equals(r.missing()) && r.wanted() == Multiblock.GRAPHITE_BLOCK,
				"hole in the core: " + r);
		ctx.getWorld().setBlockState(hole, Multiblock.GRAPHITE_BLOCK.getDefaultState());
		// Stone in the shielding.
		BlockPos wrong = small.toWorld(new BlockPos(4, 4, 4), c, Direction.NORTH);
		ctx.getWorld().setBlockState(wrong, Blocks.STONE.getDefaultState());
		r = measure(ctx, Structures.Kind.NUCLEAR_REACTOR, c);
		check(ctx, r != null && r.problem() == Structures.Problem.MISSING && wrong.equals(r.missing()) && r.layer() == 4
				&& r.wanted() == Multiblock.REACTOR_SHIELDING, "stone in the shielding: " + r);
		ctx.getWorld().setBlockState(wrong, Multiblock.REACTOR_SHIELDING.getDefaultState());
		// No fuel channels at all.
		small.parts().forEach((rel, part) -> {
			if (part == Structures.Part.CHANNEL) {
				ctx.getWorld().setBlockState(small.toWorld(rel, c, Direction.NORTH), Multiblock.GRAPHITE_BLOCK.getDefaultState());
			}
		});
		r = measure(ctx, Structures.Kind.NUCLEAR_REACTOR, c);
		check(ctx, r != null && r.problem() == Structures.Problem.NO_CHANNEL, "no channels: " + r);
		Structures.Template large = Structures.largest(Structures.Kind.NUCLEAR_REACTOR);
		build(ctx, large);
		r = measure(ctx, Structures.Kind.NUCLEAR_REACTOR, c);
		check(ctx, r != null && r.valid() && r.a() == 9 && r.h() == 7 && near(r.speed(), 175 / 3.0), "9x9x7 core, 175 channels: " + r);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void reactorCriticality(TestContext ctx) {
		Structures.Template small = Structures.reactor(3, 3, 3);
		BlockPos c = build(ctx, small);
		wet(ctx, small, c);
		PowerBlockEntity r = reactorAt(ctx, c);
		// Natural rods with heavy water: a 3x3x3 core is subcritical.
		r.setStack(0, new ItemStack(mod("natural_uranium_fuel_rod"), 2));
		r.setStack(1, new ItemStack(mod("heavy_water")));
		tickReactor(ctx, c, 100);
		check(ctx, r.stored() == 0 && r.stopped() == 1 && countIn(r, mod("natural_uranium_fuel_rod")) == 2, "small core with natural rods: subcritical");
		// An enriched rod: 4 reactors' worth, and the rod burns 4 / 1 times as fast.
		r.setStack(2, new ItemStack(mod("uranium_fuel_rod")));
		tickReactor(ctx, c, 100);
		check(ctx, r.stopped() == 0 && Math.abs(r.stored() - 100 * 120) < 1, "4 x 30 kJ a tick: " + r.stored());
		check(ctx, Math.abs(r.burnLeft() - (72000 - 400)) <= 1, "rod burns 4 rod-ticks a tick: " + r.burnLeft());
		// 7x7x7 with natural rods and heavy water is critical (CANDU).
		Structures.Template big = Structures.reactor(7, 7, 7);
		BlockPos c2 = ctx.getAbsolutePos(new BlockPos(2, 70, 2));
		big.build(ctx.getWorld(), c2, Direction.NORTH);
		wet(ctx, big, c2);
		PowerBlockEntity r2 = reactorAt(ctx, c2);
		r2.setStack(0, new ItemStack(mod("natural_uranium_fuel_rod"), 2));
		r2.setStack(1, new ItemStack(mod("heavy_water")));
		tickReactor(ctx, c2, 40);
		check(ctx, r2.stopped() == 0 && r2.stored() > 0 && countIn(r2, mod("natural_uranium_fuel_rod")) == 1, "7x7x7 natural core runs: "
				+ r2.stored() + " stopped " + r2.stopped() + " " + r2.structure());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void reactorScrams(TestContext ctx) {
		Structures.Template big = Structures.reactor(7, 7, 7);
		BlockPos c = build(ctx, big);
		wet(ctx, big, c);
		PowerBlockEntity r = reactorAt(ctx, c);
		// Enriched fuel in a big core (k 1.4) would run away: no rod goes in without a Boron Control Rod.
		r.setStack(0, new ItemStack(mod("uranium_fuel_rod"), 2));
		tickReactor(ctx, c, 20);
		check(ctx, r.stopped() == 2 && countIn(r, mod("uranium_fuel_rod")) == 2 && r.stored() == 0, "held back: stopped " + r.stopped());
		r.setStack(5, new ItemStack(mod("boron_control_rod")));
		tickReactor(ctx, c, 20);
		check(ctx, r.stopped() == 0 && r.stored() > 0 && countIn(r, mod("uranium_fuel_rod")) == 1, "runs with a control rod: " + r.stopped());
		// Pull the control rod out while it burns: scram, rod lost, radiation leaks round it.
		r.setStack(5, ItemStack.EMPTY);
		tickReactor(ctx, c, 20);
		check(ctx, r.stopped() == 2 && countIn(r, mod("spent_fuel_rod")) == 1 && countIn(r, mod("uranium_fuel_rod")) == 1,
				"runaway scram: stopped " + r.stopped() + ", spent " + countIn(r, mod("spent_fuel_rod")));
		double leak = com.periodicelements.radiation.Radiation.leaks(ctx.getWorld(), net.minecraft.util.math.Vec3d.ofCenter(c.north(3)));
		check(ctx, leak > 0.1, "radiation leaks round it: " + leak);
		r.setStack(5, new ItemStack(mod("boron_control_rod")));
		tickReactor(ctx, c, 20);
		check(ctx, r.stopped() == 0 && countIn(r, mod("uranium_fuel_rod")) == 0, "back on with the last rod: " + r.stopped());
		// Drain the water: scram.
		ctx.getWorld().setBlockState(big.toWorld(new BlockPos(0, 2, 0), c, Direction.NORTH).north(), Blocks.AIR.getDefaultState());
		tickReactor(ctx, c, 40);
		check(ctx, r.stopped() == 3 && countIn(r, mod("spent_fuel_rod")) == 2, "dry scram: stopped " + r.stopped());
		ctx.complete();
	}
}
