package com.periodicelements.test;

import com.periodicelements.crops.Crops;
import com.periodicelements.crops.GeneticCropBlock;
import com.periodicelements.shrubs.Greenhouse;
import com.periodicelements.shrubs.ShrubBlock;
import com.periodicelements.shrubs.Shrubs;
import com.periodicelements.trees.Trees;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** 3.52.1: bone meal used by a player (right click, the full game path) grows the mod's crops, shrubs and saplings. */
public class BoneMealTests implements FabricGameTest {
	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
		Greenhouse.forget();
	}

	/** Right-clicks the block at {@code abs} with bone meal, the way a player does. */
	private static void boneMeal(ServerPlayerEntity player, ServerWorld world, BlockPos abs) {
		ItemStack stack = new ItemStack(Items.BONE_MEAL, 64);
		player.setStackInHand(Hand.MAIN_HAND, stack);
		player.interactionManager.interactBlock(player, world, stack, Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void boneMealGrowsCrops(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		ServerPlayerEntity player = (ServerPlayerEntity) ctx.createMockCreativeServerPlayerInWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		List<String> failed = new ArrayList<>();
		for (var e : Crops.BLOCKS.entrySet()) {
			GeneticCropBlock crop = e.getValue();
			ctx.setBlockState(pos.down(), Blocks.FARMLAND);
			ctx.setBlockState(pos.up(), Blocks.AIR);
			ctx.setBlockState(pos, crop.getDefaultState());
			for (int i = 0; i < 12 && world.getBlockState(abs).isOf(crop) && !crop.isMature(world.getBlockState(abs)); i++) {
				boneMeal(player, world, abs);
			}
			BlockState now = world.getBlockState(abs);
			if (!now.isOf(crop) || !crop.isMature(now)) {
				failed.add(e.getKey() + " (" + now + ")");
			}
		}
		check(ctx, failed.isEmpty(), "crops bone meal did not ripen: " + failed);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void boneMealGrowsShrubs(TestContext ctx) throws Exception {
		ServerWorld world = ctx.getWorld();
		ServerPlayerEntity player = (ServerPlayerEntity) ctx.createMockCreativeServerPlayerInWorld();
		BlockPos pos = new BlockPos(1, 2, 1);
		BlockPos abs = ctx.getAbsolutePos(pos);
		var field = ShrubBlock.class.getDeclaredField("biomeIds");
		field.setAccessible(true);
		List<String> failed = new ArrayList<>();
		for (ShrubBlock bush : Shrubs.ALL) {
			@SuppressWarnings("unchecked")
			List<String> biomes = (List<String>) field.get(bush);
			biome(ctx, abs, 2, biomes.get(0));
			ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
			ctx.setBlockState(pos, bush.getDefaultState().with(ShrubBlock.AGE, 0));
			if (!world.getBlockState(abs).isOf(bush)) {
				ctx.setBlockState(pos.down(), Blocks.SAND);
				ctx.setBlockState(pos, bush.getDefaultState().with(ShrubBlock.AGE, 0));
			}
			for (int i = 0; i < 6; i++) {
				boneMeal(player, world, abs);
			}
			BlockState now = world.getBlockState(abs);
			if (!now.isOf(bush) || now.get(ShrubBlock.AGE) < 3) {
				failed.add(bush + " in " + biomes.get(0) + " (" + now + ")");
			}
		}
		check(ctx, failed.isEmpty(), "shrubs bone meal did not grow: " + failed);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void boneMealGrowsSaplings(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		ServerPlayerEntity player = (ServerPlayerEntity) ctx.createMockCreativeServerPlayerInWorld();
		BlockPos base = ctx.getAbsolutePos(new BlockPos(1, 2, 1)).add(0, 40, 0);
		List<String> failed = new ArrayList<>();
		for (Trees.Species s : Trees.ALL) {
			for (BlockPos p : BlockPos.iterate(base.add(-8, -1, -8), base.add(8, 34, 8))) {
				world.setBlockState(p, p.getY() < base.getY() ? Blocks.DIRT.getDefaultState() : Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
			biome(ctx, base, 3, s.biomes().get(0));
			world.setBlockState(base.down(), Blocks.GRASS_BLOCK.getDefaultState());
			world.setBlockState(base, s.sapling().getDefaultState());
			if (!world.getBlockState(base).isOf(s.sapling())) {
				world.setBlockState(base.down(), Blocks.SAND.getDefaultState());
				world.setBlockState(base, s.sapling().getDefaultState());
			}
			for (int i = 0; i < 40 && world.getBlockState(base).isOf(s.sapling()); i++) {
				boneMeal(player, world, base);
			}
			if (world.getBlockState(base).isOf(s.sapling())) {
				failed.add(s.id() + " in " + s.biomes().get(0));
			}
		}
		for (BlockPos p : BlockPos.iterate(base.add(-8, -1, -8), base.add(8, 34, 8))) {
			world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		check(ctx, failed.isEmpty(), "saplings bone meal did not grow: " + failed);
		ctx.complete();
	}
}
