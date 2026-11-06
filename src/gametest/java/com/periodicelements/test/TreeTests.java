package com.periodicelements.test;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.Farm;
import com.periodicelements.farm.TreeTapBlock;
import com.periodicelements.farm.TreeTapBlockEntity;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.research.ResearchState;
import com.periodicelements.trees.BiomeSaplingBlock;
import com.periodicelements.trees.Trees;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 2.8.0 Trees of the world: saplings only grow in their biomes, every species grows, bark, Tree Tap, resin. */
public class TreeTests implements FabricGameTest {
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

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void saplingGrowsOnlyInItsBiome(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(pos.down(), Blocks.DIRT);
		ctx.setBlockState(pos, block("brazilwood_sapling"));
		BlockPos abs = ctx.getAbsolutePos(pos);
		ServerWorld world = ctx.getWorld();
		BiomeSaplingBlock sapling = (BiomeSaplingBlock) block("brazilwood_sapling");
		biome(ctx, abs, 2, "plains");
		check(ctx, !sapling.growsAt(world, abs), "brazilwood should not grow in the plains");
		check(ctx, !sapling.isFertilizable(world, abs, world.getBlockState(abs)), "bone meal should not work in the plains");
		for (int i = 0; i < 2000; i++) {
			world.getBlockState(abs).randomTick(world, abs, world.random);
		}
		check(ctx, world.getBlockState(abs).isOf(sapling) && world.getBlockState(abs).get(SaplingBlock.STAGE) == 0,
				"the sapling should not even ripen in the plains: " + world.getBlockState(abs));
		biome(ctx, abs, 2, "jungle");
		check(ctx, sapling.growsAt(world, abs), "brazilwood should grow in the jungle");
		check(ctx, sapling.isFertilizable(world, abs, world.getBlockState(abs)), "bone meal should work in the jungle");
		// The rubber sapling follows the same rule now.
		BiomeSaplingBlock rubber = (BiomeSaplingBlock) Farm.RUBBER_SAPLING;
		check(ctx, rubber.growsAt(world, abs), "the rubber sapling should grow in the jungle");
		biome(ctx, abs, 2, "desert");
		check(ctx, !rubber.growsAt(world, abs), "the rubber sapling should not grow in the desert");
		// Desert trees take root in sand.
		ctx.setBlockState(pos.down(), Blocks.SAND);
		check(ctx, block("date_palm_sapling").getDefaultState().canPlaceAt(world, abs), "a date palm sapling should root in sand");
		check(ctx, !block("mahogany_sapling").getDefaultState().canPlaceAt(world, abs), "a mahogany sapling should not root in sand");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void everySpeciesGrowsIntoATree(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		BlockPos base = ctx.getAbsolutePos(new BlockPos(1, 1, 1)).add(40, 0, 40);
		List<String> failed = new ArrayList<>();
		for (Trees.Species s : Trees.ALL) {
			for (BlockPos p : BlockPos.iterate(base.add(-8, -1, -8), base.add(8, 34, 8))) {
				world.setBlockState(p, p.getY() < base.getY() ? Blocks.DIRT.getDefaultState() : Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
			world.setBlockState(base, s.sapling().getDefaultState().with(SaplingBlock.STAGE, 1));
			s.sapling().generate(world, base, world.getBlockState(base), world.random);
			int logs = 0, leaves = 0;
			for (BlockPos p : BlockPos.iterate(base.add(-8, -1, -8), base.add(8, 34, 8))) {
				BlockState st = world.getBlockState(p);
				logs += st.isOf(s.log()) ? 1 : 0;
				leaves += st.isOf(s.leaves()) ? 1 : 0;
			}
			if (logs < 1 || leaves < 3) {
				failed.add(s.id() + " (" + logs + " logs, " + leaves + " leaves)");
			}
		}
		for (BlockPos p : BlockPos.iterate(base.add(-8, -1, -8), base.add(8, 34, 8))) {
			world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		check(ctx, failed.isEmpty(), "species that did not grow: " + failed);
		check(ctx, Trees.ALL.size() == 138, "138 species (stages 1 to 3, the apple tree and 21 added species), got " + Trees.ALL.size());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void boneMealIsKeptOutsideTheBiome(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(pos.down(), Blocks.DIRT);
		ctx.setBlockState(pos, block("mahogany_sapling"));
		BlockPos abs = ctx.getAbsolutePos(pos);
		biome(ctx, abs, 2, "plains");
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BONE_MEAL, 4));
		ActionResult r = UseBlockCallback.EVENT.invoker().interact(player, ctx.getWorld(), Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.UP, abs, false));
		check(ctx, r == ActionResult.FAIL, "bone meal outside the biome should be refused, got " + r);
		check(ctx, player.getStackInHand(Hand.MAIN_HAND).getCount() == 4, "the bone meal should be kept");
		check(ctx, ctx.getWorld().getBlockState(abs).isOf(block("mahogany_sapling")), "the sapling should still be there");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void axeStripsUsefulBark(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(pos, block("cinchona_log"));
		BlockPos abs = ctx.getAbsolutePos(pos);
		PlayerEntity player = ctx.createMockPlayer(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
		ActionResult r = UseBlockCallback.EVENT.invoker().interact(player, ctx.getWorld(), Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.NORTH, abs, false));
		check(ctx, r.isAccepted(), "the axe should strip the log, got " + r);
		check(ctx, ctx.getWorld().getBlockState(abs).isOf(block("stripped_cinchona_log")), "the log should be stripped");
		List<ItemEntity> drops = ctx.getWorld().getEntitiesByClass(ItemEntity.class, new Box(abs).expand(2),
				e -> e.getStack().isOf(mod("cinchona_bark")));
		check(ctx, drops.size() == 1, "one piece of cinchona bark should drop, got " + drops.size());
		drops.forEach(ItemEntity::discard);
		// A log whose bark is nothing special strips the vanilla way (no extra drop).
		ctx.setBlockState(pos, block("mahogany_log"));
		check(ctx, UseBlockCallback.EVENT.invoker().interact(player, ctx.getWorld(), Hand.MAIN_HAND,
				new BlockHitResult(Vec3d.ofCenter(abs), Direction.NORTH, abs, false)) == ActionResult.PASS, "mahogany bark is left to vanilla");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void treeTapGetsResinAndSap(TestContext ctx) {
		ServerWorld world = ctx.getWorld();
		// Pine resin is a liquid: it needs a flask, and flows only where the pinyon grows wild.
		BlockPos log = new BlockPos(1, 1, 1);
		BlockPos tapPos = log.south();
		ctx.setBlockState(log, block("pinyon_pine_log"));
		ctx.setBlockState(tapPos, Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH));
		BlockPos abs = ctx.getAbsolutePos(tapPos);
		TreeTapBlockEntity tap = ctx.getBlockEntity(tapPos);
		BlockState state = world.getBlockState(abs);
		biome(ctx, abs, 2, "plains");
		for (int t = 0; t < Farm.tapTicks() * 2; t++) {
			tap.tick(world, abs, state);
		}
		check(ctx, tap.latex() == 0, "no resin outside the badlands, got " + tap.latex());
		biome(ctx, abs, 2, "badlands");
		for (int t = 0; t < Farm.tapTicks() * 2; t++) {
			tap.tick(world, abs, state);
		}
		check(ctx, tap.latex() == 2, "2 flasks of pine resin in the badlands, got " + tap.latex());
		check(ctx, tap.getStack(TreeTapBlockEntity.OUT).isEmpty(), "no resin out without a flask");
		tap.setStack(TreeTapBlockEntity.IN, new ItemStack(Fluids.GLASS_FLASK, 4));
		tap.tick(world, abs, state);
		tap.tick(world, abs, state);
		check(ctx, tap.getStack(TreeTapBlockEntity.OUT).isOf(mod("pine_resin")) && tap.getStack(TreeTapBlockEntity.OUT).getCount() == 2,
				"2 pine resin out, got " + tap.getStack(TreeTapBlockEntity.OUT));

		// Copal is a hard resin: it drops straight into the tap, no flask.
		BlockPos log2 = new BlockPos(4, 1, 1);
		BlockPos tap2Pos = log2.south();
		ctx.setBlockState(log2, block("jatoba_log"));
		ctx.setBlockState(tap2Pos, Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH));
		BlockPos abs2 = ctx.getAbsolutePos(tap2Pos);
		TreeTapBlockEntity tap2 = ctx.getBlockEntity(tap2Pos);
		biome(ctx, abs2, 2, "savanna_plateau");
		for (int t = 0; t < Farm.tapTicks() * 3; t++) {
			tap2.tick(world, abs2, world.getBlockState(abs2));
		}
		check(ctx, tap2.getStack(TreeTapBlockEntity.OUT).isOf(mod("copal")) && tap2.getStack(TreeTapBlockEntity.OUT).getCount() == 3,
				"3 copal out with no flask, got " + tap2.getStack(TreeTapBlockEntity.OUT));
		// A tap cannot go on a tree that gives nothing.
		check(ctx, !Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH).canPlaceAt(world,
				ctx.getAbsolutePos(new BlockPos(7, 1, 2))), "no tap on thin air");
		ctx.setBlockState(new BlockPos(7, 1, 1), block("mahogany_log"));
		check(ctx, !Farm.TREE_TAP.getDefaultState().with(TreeTapBlock.FACING, Direction.SOUTH).canPlaceAt(world,
				ctx.getAbsolutePos(new BlockPos(7, 1, 2))), "no tap on mahogany");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void alembicSplitsPineResin(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, block("alembic"));
		MachineBlockEntity be = ctx.getBlockEntity(pos);
		UUID owner = UUID.randomUUID();
		be.setOwner(owner);
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), owner,
				new ArrayList<>(List.of("alembic_distillation")));
		be.setStack(MachineBlockEntity.FLASKS, new ItemStack(Fluids.GLASS_FLASK, 64));
		be.setStack(MachineBlockEntity.FUEL, new ItemStack(Items.CHARCOAL, 64));
		be.setStack(MachineBlockEntity.INPUT, new ItemStack(mod("pine_resin"), 9));
		BlockPos abs = ctx.getAbsolutePos(pos);
		for (int t = 0; t < 30_000; t++) {
			be.tick(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
			if (t > 100 && be.getStack(MachineBlockEntity.INPUT).isEmpty() && be.status() == MachineBlockEntity.Status.IDLE) {
				break;
			}
		}
		int turp = 0, rosin = 0;
		for (int i = MachineBlockEntity.PRODUCT_FIRST; i <= MachineBlockEntity.RESIDUE; i++) {
			ItemStack s = be.getStack(i);
			turp += s.isOf(mod("turpentine")) ? s.getCount() : 0;
			rosin += s.isOf(mod("rosin")) ? s.getCount() : 0;
		}
		// 9 flasks = 1000 units: 31% turpentine (2 flasks and change), 69% rosin (6 lumps).
		check(ctx, turp == 2 && rosin == 6, "2 turpentine and 6 rosin expected, got " + turp + " and " + rosin);
		ctx.complete();
	}
}
