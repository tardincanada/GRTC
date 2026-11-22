package com.periodicelements.test;

import com.periodicelements.shrubs.HydrangeaBlock;
import com.periodicelements.shrubs.ShrubBlock;
import com.periodicelements.shrubs.Shrubs;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** 3.56.0 (Guilherme): move a bush whole with a shovel, a cutting when broken, one or two cuttings from shears. */
public class ShrubTransplantTests implements FabricGameTest {
	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static void click(ServerPlayerEntity player, ServerWorld world, ItemStack stack, BlockPos abs, Direction side) {
		player.setStackInHand(Hand.MAIN_HAND, stack);
		player.interactionManager.interactBlock(player, world, stack, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs).offset(side, 0.5), side, abs, false));
	}

	private static int dropped(TestContext ctx, BlockPos abs, net.minecraft.item.Item item) {
		List<ItemEntity> list = ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(abs).expand(2),
				e -> e.getStack().isOf(item));
		int n = list.stream().mapToInt(e -> e.getStack().getCount()).sum();
		list.forEach(ItemEntity::discard);
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void shovelLiftsTheWholeBushAndItKeepsItsSize(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		ServerPlayerEntity player = (ServerPlayerEntity) ctx.createMockCreativeServerPlayerInWorld();
		BlockPos from = new BlockPos(1, 2, 1), to = new BlockPos(3, 2, 1);
		ctx.setBlockState(from.down(), Blocks.GRASS_BLOCK);
		ctx.setBlockState(to.down(), Blocks.GRASS_BLOCK);
		List<String> failed = new ArrayList<>();
		for (ShrubBlock bush : Shrubs.ALL) {
			for (int age : new int[]{0, 3}) {
				BlockState planted = bush.getDefaultState().with(ShrubBlock.AGE, age);
				if (bush instanceof HydrangeaBlock) {
					planted = planted.with(HydrangeaBlock.BLUE, true);
				}
				ctx.setBlockState(from.down(), soil(bush, world, ctx, from));
				ctx.setBlockState(to.down(), soil(bush, world, ctx, to));
				ctx.setBlockState(from, planted);
				ctx.setBlockState(to, Blocks.AIR);
				player.getInventory().clear();
				player.setSneaking(true);
				click(player, world, new ItemStack(Items.IRON_SHOVEL), ctx.getAbsolutePos(from), Direction.UP);
				if (!world.getBlockState(ctx.getAbsolutePos(from)).isAir()) {
					failed.add(bush.id + " age " + age + ": not lifted");
					continue;
				}
				ItemStack ball = ItemStack.EMPTY;
				for (int i = 0; i < player.getInventory().size(); i++) {
					if (player.getInventory().getStack(i).isOf(bush.cutting())) {
						ball = player.getInventory().getStack(i);
					}
				}
				if (ball.isEmpty() || !ball.contains(DataComponentTypes.BLOCK_STATE)) {
					failed.add(bush.id + " age " + age + ": no root ball in inventory");
					continue;
				}
				if (dropped(ctx, ctx.getAbsolutePos(from), Items.STICK) != 0) {
					failed.add(bush.id + ": lifting dropped loot");
				}
				player.setSneaking(false);
				click(player, world, ball.copy(), ctx.getAbsolutePos(to.down()), Direction.UP);
				BlockState now = world.getBlockState(ctx.getAbsolutePos(to));
				if (!now.isOf(bush) || now.get(ShrubBlock.AGE) != age
						|| (bush instanceof HydrangeaBlock && !now.get(HydrangeaBlock.BLUE))) {
					failed.add(bush.id + " age " + age + ": replanted as " + now);
				}
			}
		}
		check(ctx, failed.isEmpty(), "root ball failed: " + failed);
		ctx.complete();
	}

	private static Block soil(ShrubBlock bush, ServerWorld world, TestContext ctx, BlockPos pos) {
		for (Block b : new Block[]{Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.STONE}) {
			ctx.setBlockState(pos.down(), b);
			if (bush.getDefaultState().canPlaceAt(world, ctx.getAbsolutePos(pos))) {
				return b;
			}
		}
		return Blocks.GRASS_BLOCK;
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void breakingABushAlwaysGivesACutting(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos abs = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		List<String> failed = new ArrayList<>();
		for (ShrubBlock bush : Shrubs.ALL) {
			for (int age = 0; age <= 3; age++) {
				List<ItemStack> drops = Block.getDroppedStacks(bush.getDefaultState().with(ShrubBlock.AGE, age), world, abs, null);
				int n = drops.stream().filter(s -> s.isOf(bush.cutting())).mapToInt(ItemStack::getCount).sum();
				if (n != 1) {
					failed.add(bush.id + " age " + age + ": " + n);
				}
			}
		}
		check(ctx, failed.isEmpty(), "breaking should give exactly one cutting: " + failed);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void shearsNeedAGrownBush(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		ServerPlayerEntity player = (ServerPlayerEntity) ctx.createMockCreativeServerPlayerInWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		ShrubBlock rose = (ShrubBlock) Shrubs.BY_ID.get("damask_rose");
		ctx.setBlockState(pos, rose.getDefaultState().with(ShrubBlock.AGE, 1));
		click(player, world, new ItemStack(Items.SHEARS), abs, Direction.UP);
		check(ctx, dropped(ctx, abs, rose.cutting()) == 0, "a young bush gives no cutting");
		check(ctx, world.getBlockState(abs).isOf(rose) && world.getBlockState(abs).get(ShrubBlock.AGE) == 1,
				"a young bush is left as it was");
		int seen1 = 0, seen2 = 0;
		for (int i = 0; i < 40; i++) {
			ctx.setBlockState(pos, rose.getDefaultState().with(ShrubBlock.AGE, 2 + (i % 2)));
			click(player, world, new ItemStack(Items.SHEARS), abs, Direction.UP);
			int n = dropped(ctx, abs, rose.cutting());
			check(ctx, n == 1 || n == 2, "shears cut one or two cuttings, got " + n);
			if (n == 1) seen1++; else seen2++;
			check(ctx, world.getBlockState(abs).get(ShrubBlock.AGE) == 1, "a cut bush goes back to age 1");
		}
		check(ctx, seen1 > 0 && seen2 > 0, "both one and two cuttings should happen in 40 cuts");
		ctx.complete();
	}
}
