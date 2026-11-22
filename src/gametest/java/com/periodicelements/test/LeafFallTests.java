package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.trees.LeafFall;
import com.periodicelements.trees.PlacedLogs;
import com.periodicelements.trees.Trees;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

/** 3.55.0: leaves out of reach (3 blocks) of a natural log of their own species fall at once. */
public class LeafFallTests implements FabricGameTest {
	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static int count(ServerWorld world, BlockPos a, BlockPos b, Block leaves, boolean natural) {
		int n = 0;
		for (BlockPos p : BlockPos.iterate(a, b)) {
			BlockState s = world.getBlockState(p);
			if (s.isOf(leaves) && (!natural || !s.get(LeavesBlock.PERSISTENT))) {
				n++;
			}
		}
		return n;
	}

	private static BlockState natural(Block leaves) {
		return leaves.getDefaultState().with(LeavesBlock.PERSISTENT, false);
	}

	/** Grows a tree from the configured feature, cuts every log, and expects the whole crown down within a few ticks. */
	private static void fellTree(TestContext ctx, String feature, Block log, Block leaves) {
		ServerWorld world = ctx.getWorld();
		BlockPos base = ctx.getAbsolutePos(new BlockPos(1, 1, 1)).add(0, 80, 0); // high up, clear of the other tests
		BlockPos a = base.add(-8, -1, -8), b = base.add(8, 30, 8);
		for (BlockPos p : BlockPos.iterate(a, b)) {
			world.setBlockState(p, p.getY() < base.getY() ? Blocks.DIRT.getDefaultState() : Blocks.AIR.getDefaultState(),
					Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
		}
		var f = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(Identifier.of(feature));
		boolean grown = false;
		for (int t = 0; t < 10 && !grown; t++) {
			grown = f.generate(world, world.getChunkManager().getChunkGenerator(), world.random, base);
		}
		check(ctx, grown, feature + " did not grow");
		int before = count(world, a, b, leaves, true);
		check(ctx, before > 10, feature + " has too few leaves: " + before);
		for (BlockPos p : BlockPos.iterate(a, b)) {
			if (world.getBlockState(p).isOf(log)) {
				world.setBlockState(p, Blocks.AIR.getDefaultState());
			}
		}
		ctx.runAtTick(ctx.getTick() + 6, () -> {
			int after = count(world, a, b, leaves, true);
			for (BlockPos p : BlockPos.iterate(a, b)) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
			}
			check(ctx, after == 0, feature + ": " + after + " of " + before + " leaves still hang in the air");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void felledModTreeDropsItsLeavesAtOnce(TestContext ctx) {
		fellTree(ctx, "periodic_elements:tree/mahogany", block("mahogany_log"), block("mahogany_leaves"));
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void felledOakDropsItsLeavesAtOnce(TestContext ctx) {
		fellTree(ctx, "minecraft:fancy_oak", Blocks.OAK_LOG, Blocks.OAK_LEAVES);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void reachIsThreeBlocksOfTheSameSpecies(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		Block leaves = block("brazilwood_leaves");
		BlockPos log = new BlockPos(1, 2, 1);
		ctx.setBlockState(log.down(), Blocks.DIRT);
		ctx.setBlockState(log, block("brazilwood_log"));
		// A row of leaves from the log: 1, 2, 3 blocks away stay; 4 falls.
		for (int i = 1; i <= 4; i++) {
			ctx.setBlockState(log.east(i), natural(leaves));
		}
		BlockPos near = log.east(3), far = log.east(4);
		BlockPos lonely = log.add(2, 2, 2); // within 3 blocks but joined to nothing: falls
		BlockPos stranger = new BlockPos(9, 2, 1); // beside an oak log: wrong species, falls
		BlockPos kept = new BlockPos(4, 6, 6); // placed by a player (persistent), 5 away: stays
		ctx.setBlockState(stranger.west(), Blocks.OAK_LOG);
		ctx.setBlockState(lonely, natural(leaves));
		ctx.setBlockState(stranger, natural(leaves));
		ctx.setBlockState(kept, leaves.getDefaultState().with(LeavesBlock.PERSISTENT, true));
		check(ctx, LeafFall.governs(leaves) && LeafFall.governs(Blocks.CHERRY_LEAVES) && LeafFall.governs(Blocks.AZALEA_LEAVES),
				"mod, vanilla and azalea leaves should follow the rule");
		// A touch: a block set next to each leaf.
		for (BlockPos p : new BlockPos[]{near, far, lonely, stranger, kept}) {
			ctx.setBlockState(p.up(), Blocks.STONE);
		}
		ctx.runAtTick(ctx.getTick() + 4, () -> {
			check(ctx, world.getBlockState(ctx.getAbsolutePos(near)).isOf(leaves), "a joined leaf 3 blocks away should stay");
			check(ctx, world.getBlockState(ctx.getAbsolutePos(far)).isAir(), "a leaf 4 blocks away should fall");
			check(ctx, world.getBlockState(ctx.getAbsolutePos(lonely)).isAir(), "a leaf joined to no log should fall");
			check(ctx, world.getBlockState(ctx.getAbsolutePos(stranger)).isAir(), "an oak log should not hold brazilwood leaves");
			check(ctx, world.getBlockState(ctx.getAbsolutePos(kept)).isOf(leaves), "leaves a player placed should never fall");
			ctx.complete();
		});
	}

	/** Guilherme's oak (3.58.0): cut the logs the leaves touch, leave a stump; the whole crown must come down. */
	private static void cutAboveStump(TestContext ctx, String feature, Block log, Block leaves) {
		ServerWorld world = ctx.getWorld();
		BlockPos base = ctx.getAbsolutePos(new BlockPos(1, 1, 1)).add(0, 80, 0); // high up, clear of the other tests
		BlockPos a = base.add(-8, -1, -8), b = base.add(8, 30, 8);
		for (BlockPos p : BlockPos.iterate(a, b)) {
			world.setBlockState(p, p.getY() < base.getY() ? Blocks.DIRT.getDefaultState() : Blocks.AIR.getDefaultState(),
					Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
		}
		var f = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(Identifier.of(feature));
		boolean grown = false;
		for (int t = 0; t < 10 && !grown; t++) {
			grown = f.generate(world, world.getChunkManager().getChunkGenerator(), world.random, base);
		}
		check(ctx, grown, feature + " did not grow");
		int before = count(world, a, b, leaves, true);
		int stump = 0;
		for (BlockPos p : BlockPos.iterate(a, b)) {
			if (!world.getBlockState(p).isOf(log)) {
				continue;
			}
			boolean touches = false;
			for (Direction d : Direction.values()) {
				touches |= world.getBlockState(p.offset(d)).isOf(leaves);
			}
			if (touches) {
				world.breakBlock(p, true);
			} else {
				stump++;
			}
		}
		check(ctx, stump > 0, feature + ": no stump left to test with");
		ctx.runAtTick(ctx.getTick() + 6, () -> {
			int after = count(world, a, b, leaves, true);
			for (BlockPos p : BlockPos.iterate(a, b)) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
			}
			check(ctx, after == 0, feature + ": " + after + " of " + before + " leaves still hang over the stump");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void oakCrownFallsWhenOnlyAStumpIsLeft(TestContext ctx) {
		cutAboveStump(ctx, "minecraft:oak", Blocks.OAK_LOG, Blocks.OAK_LEAVES);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void walnutCrownFallsWhenOnlyAStumpIsLeft(TestContext ctx) {
		cutAboveStump(ctx, "periodic_elements:tree/walnut", block("walnut_log"), block("walnut_leaves"));
	}

	/** Guilherme's walnut (3.59.0, answer c): cut the foot of the trunk and the whole tree comes down as items. */
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void cuttingTheFootDropsTheCrownOverHiddenLogs(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		Block log = block("walnut_log"), leaves = block("walnut_leaves");
		BlockPos base = ctx.getAbsolutePos(new BlockPos(1, 1, 1)).add(0, 80, 0); // high up, clear of the other tests
		BlockPos a = base.add(-8, -1, -8), b = base.add(8, 30, 8);
		for (BlockPos p : BlockPos.iterate(a, b)) {
			world.setBlockState(p, p.getY() < base.getY() ? Blocks.DIRT.getDefaultState() : Blocks.AIR.getDefaultState(),
					Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
		}
		var f = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(Identifier.of("periodic_elements:tree/walnut"));
		boolean grown = false;
		for (int t = 0; t < 10 && !grown; t++) {
			grown = f.generate(world, world.getChunkManager().getChunkGenerator(), world.random, base);
		}
		check(ctx, grown, "the walnut did not grow");
		int before = count(world, a, b, leaves, true), trunk = 0;
		for (BlockPos p : BlockPos.iterate(a, b)) {
			trunk += world.getBlockState(p).isOf(log) ? 1 : 0;
		}
		world.breakBlock(base, true); // only the bottom log
		final int total = trunk;
		ctx.runAtTick(ctx.getTick() + 6, () -> {
			int after = count(world, a, b, leaves, true), logs = 0;
			for (BlockPos p : BlockPos.iterate(a, b)) {
				logs += world.getBlockState(p).isOf(log) ? 1 : 0;
				world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
			}
			var items = world.getEntitiesByClass(net.minecraft.entity.ItemEntity.class, new net.minecraft.util.math.Box(a.toCenterPos(),
					b.toCenterPos()).expand(2), e -> e.getStack().isOf(log.asItem()));
			int dropped = items.stream().mapToInt(e -> e.getStack().getCount()).sum();
			world.getEntitiesByClass(net.minecraft.entity.ItemEntity.class, new net.minecraft.util.math.Box(a.toCenterPos(),
					b.toCenterPos()).expand(2), e -> true).forEach(net.minecraft.entity.Entity::discard);
			check(ctx, logs == 0, logs + " walnut logs still hang above the cut");
			check(ctx, dropped == total, "every log of the tree should drop as an item: " + dropped + " of " + total);
			check(ctx, after == 0, after + " of " + before + " walnut leaves still hang in the air");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void logsAPlayerPlacedHoldNoLeaves(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		Block log = block("teak_log");
		Block leaves = block("teak_leaves");
		BlockPos floor = new BlockPos(1, 1, 1);
		ctx.setBlockState(floor, Blocks.STONE);
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		ItemStack stack = new ItemStack(log.asItem(), 4);
		player.setStackInHand(Hand.MAIN_HAND, stack);
		BlockPos abs = ctx.getAbsolutePos(floor);
		((BlockItem) log.asItem()).place(new ItemPlacementContext(player, Hand.MAIN_HAND, stack,
				new BlockHitResult(Vec3d.ofCenter(abs).add(0, 0.5, 0), Direction.UP, abs, false)));
		BlockPos placed = floor.up();
		check(ctx, world.getBlockState(ctx.getAbsolutePos(placed)).isOf(log), "the player should have placed the log");
		check(ctx, PlacedLogs.of(world).placed(ctx.getAbsolutePos(placed)), "the placed log should be marked");
		BlockPos leaf = placed.east();
		ctx.setBlockState(leaf, natural(leaves));
		ctx.setBlockState(leaf.up(), Blocks.STONE);
		// A natural log of the same species (grown from a sapling) does hold leaves.
		BlockPos grown = new BlockPos(6, 2, 1);
		ctx.setBlockState(grown.down(), Blocks.DIRT);
		ctx.setBlockState(grown, log);
		ctx.setBlockState(grown.east(), natural(leaves));
		ctx.setBlockState(grown.east().up(), Blocks.STONE);
		ctx.runAtTick(ctx.getTick() + 4, () -> {
			check(ctx, world.getBlockState(ctx.getAbsolutePos(leaf)).isAir(), "a log the player placed should not hold leaves");
			check(ctx, world.getBlockState(ctx.getAbsolutePos(grown.east())).isOf(leaves), "a natural log should hold leaves");
			ctx.setBlockState(placed, Blocks.AIR);
			check(ctx, !PlacedLogs.of(world).placed(ctx.getAbsolutePos(placed)), "breaking the log should clear the mark");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 40)
	public void strippedLogStillHoldsLeaves(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		Trees.Species s = Trees.ALL.stream().filter(x -> x.id().equals("mahogany")).findFirst().orElseThrow();
		BlockPos log = new BlockPos(1, 2, 1);
		ctx.setBlockState(log.down(), Blocks.DIRT);
		ctx.setBlockState(log, s.log());
		BlockPos leaf = log.east(2);
		ctx.setBlockState(log.east(), natural(s.leaves()));
		ctx.setBlockState(leaf, natural(s.leaves()));
		ctx.setBlockState(log, s.stripped());
		ctx.setBlockState(leaf.up(), Blocks.STONE);
		ctx.runAtTick(ctx.getTick() + 4, () -> {
			check(ctx, world.getBlockState(ctx.getAbsolutePos(leaf)).isOf(s.leaves()), "a stripped natural log should still hold leaves");
			ctx.complete();
		});
	}

	/**
	 * 3.68.4: world generation reports block changes from worker threads. A log changed there must not make LeafFall
	 * look at the world, which waits for the server thread: with the server thread waiting for that chunk, the world
	 * froze. Here the server thread waits 3 s for the worker; before the fix the worker waited for it in turn.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void offThreadLogChangeDoesNotWait(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos pos = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		ctx.setBlockState(new BlockPos(1, 1, 1), Blocks.OAK_LOG);
		ctx.setBlockState(new BlockPos(2, 2, 1), natural(Blocks.OAK_LEAVES));
		java.util.concurrent.CompletableFuture<Void> f = java.util.concurrent.CompletableFuture.runAsync(() ->
				LeafFall.changed(world, pos, Blocks.OAK_LOG.getDefaultState(), Blocks.AIR.getDefaultState()));
		try {
			f.get(3, java.util.concurrent.TimeUnit.SECONDS);
		} catch (Exception e) {
			ctx.throwGameTestException("a log change off the server thread waited for the server thread: " + e);
		}
		ctx.complete();
	}
}
