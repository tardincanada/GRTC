package com.periodicelements.test;

import com.periodicelements.logistics.BeltBlock;
import com.periodicelements.logistics.BeltBlockEntity;
import com.periodicelements.logistics.BigHopperBlockEntity;
import com.periodicelements.logistics.CrateBlockEntity;
import com.periodicelements.logistics.Logistics;
import com.periodicelements.logistics.SorterBlock;
import com.periodicelements.logistics.SorterBlockEntity;
import com.periodicelements.logistics.Transfer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Belts, the Belt Sorter, the large hoppers and the larger chests (3.40.0). */
public class LogisticsTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static int count(Inventory inv, Item item) {
		int n = 0;
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(item)) {
				n += inv.getStack(i).getCount();
			}
		}
		return n;
	}

	private static BlockState belt(int tier, Direction d) {
		return Logistics.BELTS.get(tier).getDefaultState().with(BeltBlock.FACING, d);
	}

	private static Inventory inv(TestContext ctx, BlockPos rel) {
		return (Inventory) ctx.getBlockEntity(rel);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void beltCarriesIntoChest(TestContext ctx) {
		for (int z = 0; z < 4; z++) {
			ctx.setBlockState(new BlockPos(1, 1, z), belt(0, Direction.SOUTH));
		}
		ctx.setBlockState(new BlockPos(1, 1, 4), Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.NORTH));
		BeltBlockEntity first = (BeltBlockEntity) ctx.getBlockEntity(new BlockPos(1, 1, 0));
		ItemStack load = new ItemStack(Items.COBBLESTONE, 20);
		int took = Transfer.insert(first, load, Direction.NORTH);
		expect(ctx, took == Logistics.CANVAS.capacity(), "a canvas belt should take " + Logistics.CANVAS.capacity() + ", took " + took);
		// Four belts at 10 ticks each: the load should be in the chest after about 40 ticks, not before 30.
		ctx.runAtTick(30, () -> expect(ctx, count(inv(ctx, new BlockPos(1, 1, 4)), Items.COBBLESTONE) == 0,
				"the load reached the chest too soon"));
		ctx.runAtTick(60, () -> {
			expect(ctx, count(inv(ctx, new BlockPos(1, 1, 4)), Items.COBBLESTONE) == took,
					"the chest should hold the belt's load");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void fasterTiersCarryMore(TestContext ctx) {
		int[] caps = new int[3];
		for (int t = 0; t < 3; t++) {
			ctx.setBlockState(new BlockPos(t * 2, 1, 1), belt(t, Direction.SOUTH));
			BeltBlockEntity b = (BeltBlockEntity) ctx.getBlockEntity(new BlockPos(t * 2, 1, 1));
			caps[t] = Transfer.insert(b, new ItemStack(Items.IRON_INGOT, 64), Direction.NORTH);
		}
		expect(ctx, caps[0] < caps[1] && caps[1] < caps[2], "each tier should carry more: " + caps[0] + ", " + caps[1] + ", " + caps[2]);
		expect(ctx, Logistics.CANVAS.interval() > Logistics.RUBBER.interval() && Logistics.RUBBER.interval() > Logistics.ROLLER.interval(),
				"each tier should be faster");
		// A belt takes nothing from its front, and only the same kind of item on top of its load.
		BeltBlockEntity b = (BeltBlockEntity) ctx.getBlockEntity(new BlockPos(0, 1, 1));
		expect(ctx, Transfer.insert(b, new ItemStack(Items.IRON_INGOT, 1), Direction.SOUTH) == 0, "a belt took items from its front");
		expect(ctx, Transfer.insert(b, new ItemStack(Items.DIRT, 1), Direction.NORTH) == 0, "a belt mixed two kinds of item");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void sorterTurnsChosenItemsDown(TestContext ctx) {
		BlockPos sorterPos = new BlockPos(1, 2, 2);
		ctx.setBlockState(new BlockPos(1, 2, 1), belt(1, Direction.SOUTH));
		ctx.setBlockState(sorterPos, Logistics.SORTER.getDefaultState().with(SorterBlock.FACING, Direction.SOUTH));
		ctx.setBlockState(new BlockPos(1, 2, 3), Blocks.BARREL.getDefaultState());
		ctx.setBlockState(new BlockPos(1, 1, 2), Blocks.HOPPER.getDefaultState().with(HopperBlock.ENABLED, false));
		SorterBlockEntity sorter = (SorterBlockEntity) ctx.getBlockEntity(sorterPos);
		sorter.filter().setStack(0, new ItemStack(Items.COBBLESTONE));
		BeltBlockEntity feed = (BeltBlockEntity) ctx.getBlockEntity(new BlockPos(1, 2, 1));
		Transfer.insert(feed, new ItemStack(Items.COBBLESTONE, 4), Direction.NORTH);
		ctx.runAtTick(20, () -> {
			Transfer.insert(feed, new ItemStack(Items.DIRT, 6), Direction.NORTH);
		});
		ctx.runAtTick(50, () -> {
			Inventory below = inv(ctx, new BlockPos(1, 1, 2));
			Inventory front = inv(ctx, new BlockPos(1, 2, 3));
			expect(ctx, count(below, Items.COBBLESTONE) == 4, "cobblestone should go down, below holds " + count(below, Items.COBBLESTONE));
			expect(ctx, count(front, Items.DIRT) == 6, "dirt should go straight on, front holds " + count(front, Items.DIRT));
			expect(ctx, count(front, Items.COBBLESTONE) == 0 && count(below, Items.DIRT) == 0, "the sorter mixed them up");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void sorterUsesASideWithNothingBelow(TestContext ctx) {
		BlockPos sorterPos = new BlockPos(2, 1, 2);
		ctx.setBlockState(sorterPos, Logistics.SORTER.getDefaultState().with(SorterBlock.FACING, Direction.SOUTH));
		// Facing south, its left is east.
		ctx.setBlockState(new BlockPos(3, 1, 2), Blocks.BARREL.getDefaultState());
		SorterBlockEntity sorter = (SorterBlockEntity) ctx.getBlockEntity(sorterPos);
		sorter.filter().setStack(4, new ItemStack(Items.REDSTONE));
		Transfer.insert(sorter, new ItemStack(Items.REDSTONE, 10), Direction.NORTH);
		ctx.runAtTick(10, () -> {
			expect(ctx, count(inv(ctx, new BlockPos(3, 1, 2)), Items.REDSTONE) == 10, "redstone should leave by the side");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void steelHopperMovesSixteenAtATime(TestContext ctx) {
		ctx.setBlockState(new BlockPos(1, 1, 1), Blocks.BARREL.getDefaultState());
		ctx.setBlockState(new BlockPos(1, 2, 1), Logistics.HOPPERS.get(0).getDefaultState());
		ctx.setBlockState(new BlockPos(1, 3, 1), Blocks.BARREL.getDefaultState());
		Inventory top = inv(ctx, new BlockPos(1, 3, 1));
		top.setStack(0, new ItemStack(Items.COAL, 64));
		BigHopperBlockEntity hopper = (BigHopperBlockEntity) ctx.getBlockEntity(new BlockPos(1, 2, 1));
		expect(ctx, hopper.size() == Logistics.STEEL_HOPPER.slots(), "the steel hopper should have " + Logistics.STEEL_HOPPER.slots() + " places");
		ctx.runAtTick(2, () -> expect(ctx, count(hopper, Items.COAL) + count(inv(ctx, new BlockPos(1, 1, 1)), Items.COAL) == 16,
				"one cycle should move 16 coal, moved " + (count(hopper, Items.COAL) + count(inv(ctx, new BlockPos(1, 1, 1)), Items.COAL))));
		ctx.runAtTick(40, () -> {
			expect(ctx, count(inv(ctx, new BlockPos(1, 1, 1)), Items.COAL) == 64, "all 64 coal should be through by tick 40");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void chestsAreLarger(TestContext ctx) {
		int[] want = {54, 78, 108};
		for (int i = 0; i < 3; i++) {
			BlockPos p = new BlockPos(i * 2, 1, 1);
			ctx.setBlockState(p, Logistics.CHESTS.get(i).getDefaultState());
			CrateBlockEntity chest = (CrateBlockEntity) ctx.getBlockEntity(p);
			expect(ctx, chest.size() == want[i], Logistics.CHEST_TIERS.get(i).id() + " should have " + want[i] + ", has " + chest.size());
		}
		// A vanilla hopper fills one like a chest.
		ctx.setBlockState(new BlockPos(0, 2, 1), Blocks.HOPPER.getDefaultState());
		inv(ctx, new BlockPos(0, 2, 1)).setStack(0, new ItemStack(Items.APPLE, 2));
		ctx.runAtTick(30, () -> {
			expect(ctx, count(inv(ctx, new BlockPos(0, 1, 1)), Items.APPLE) == 2, "a hopper should fill the iron chest");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "logistics")
	public void forbiddenModeTurnsAsideAllButTheFilter(TestContext ctx) {
		BlockPos sorterPos = new BlockPos(2, 2, 2);
		ctx.setBlockState(sorterPos, Logistics.SORTER.getDefaultState().with(SorterBlock.FACING, Direction.SOUTH));
		ctx.setBlockState(new BlockPos(2, 2, 3), Blocks.BARREL.getDefaultState());
		ctx.setBlockState(new BlockPos(2, 1, 2), Blocks.HOPPER.getDefaultState().with(HopperBlock.ENABLED, false));
		SorterBlockEntity sorter = (SorterBlockEntity) ctx.getBlockEntity(sorterPos);
		sorter.filter().setStack(0, new ItemStack(Items.COBBLESTONE));
		sorter.setForbidden(true);
		expect(ctx, sorter.forbidden(), "the sorter should be in forbidden mode");
		Transfer.insert(sorter, new ItemStack(Items.COBBLESTONE, 5), Direction.NORTH);
		ctx.runAtTick(10, () -> Transfer.insert(sorter, new ItemStack(Items.DIRT, 7), Direction.NORTH));
		ctx.runAtTick(20, () -> {
			Inventory below = inv(ctx, new BlockPos(2, 1, 2));
			Inventory front = inv(ctx, new BlockPos(2, 2, 3));
			expect(ctx, count(front, Items.COBBLESTONE) == 5, "the filter's cobblestone should go straight on, front holds " + count(front, Items.COBBLESTONE));
			expect(ctx, count(below, Items.DIRT) == 7, "dirt should be turned aside, below holds " + count(below, Items.DIRT));
			ctx.complete();
		});
	}
}
