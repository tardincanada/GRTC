package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.shrubs.Greenhouse;
import com.periodicelements.shrubs.GreenhouseHeaterBlock;
import com.periodicelements.shrubs.PlantWarning;
import com.periodicelements.shrubs.ShrubBlock;
import com.periodicelements.shrubs.Shrubs;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/** 3.54.0: the look-at warning on saplings and shrubs that will not grow where they stand. */
public class PlantWarningTests implements FabricGameTest {
	private static Block block(String id) {
		return Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, id));
	}

	private static void biome(TestContext ctx, BlockPos abs, int r, String biome) {
		var server = ctx.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(4),
				String.format("fillbiome %d %d %d %d %d %d minecraft:%s", abs.getX() - r, abs.getY() - r, abs.getZ() - r,
						abs.getX() + r, abs.getY() + r, abs.getZ() + r, biome));
		Greenhouse.forget();
	}

	private static String warning(TestContext ctx, BlockPos pos, BlockState state) {
		ctx.setBlockState(pos, state);
		Greenhouse.forget();
		BlockPos abs = ctx.getAbsolutePos(pos);
		return PlantWarning.key(ctx.getWorld(), abs, ctx.getWorld().getBlockState(abs));
	}

	private static void expect(TestContext ctx, BlockPos pos, String plant, BlockState state, String want) {
		String got = warning(ctx, pos, state);
		if (!Objects.equals(got, want)) {
			ctx.throwGameTestException(plant + ": expected " + want + ", got " + got);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void outOfBiomePlantsSayTheyDoNotGrowHere(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 2, 1);
		ctx.setBlockState(pos.down(), Blocks.GRASS_BLOCK);
		biome(ctx, ctx.getAbsolutePos(pos), 2, "jungle");
		expect(ctx, pos, "mahogany in the jungle", block("mahogany_sapling").getDefaultState(), null);
		expect(ctx, pos, "guarana in the jungle", block("guarana_bush").getDefaultState(), null);
		biome(ctx, ctx.getAbsolutePos(pos), 2, "plains");
		expect(ctx, pos, "mahogany in the plains", block("mahogany_sapling").getDefaultState(), PlantWarning.HERE);
		expect(ctx, pos, "guarana in the plains", block("guarana_bush").getDefaultState(), PlantWarning.HERE);
		expect(ctx, pos, "ripe guarana in the plains", block("guarana_bush").getDefaultState().with(ShrubBlock.AGE, 3), null);
		expect(ctx, pos, "vanilla oak sapling", Blocks.OAK_SAPLING.getDefaultState(), null);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void wrongGreenhouseSaysWrongEnvironment(TestContext ctx) {
		BlockPos corner = new BlockPos(0, 1, 0);
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				for (int z = 0; z < 5; z++) {
					boolean edge = x == 0 || x == 4 || z == 0 || z == 4;
					ctx.setBlockState(corner.add(x, y, z), y == 0 ? Blocks.GRASS_BLOCK : y == 4 ? Blocks.GLASS
							: edge ? Blocks.STONE : Blocks.AIR);
				}
			}
		}
		BlockPos in = corner.add(2, 1, 2);
		biome(ctx, ctx.getAbsolutePos(in), 6, "snowy_plains");
		// A plain (temperate) greenhouse: tea and beech grow, guarana and mahogany want warmth.
		expect(ctx, in, "tea in a plain greenhouse", block("tea_bush").getDefaultState(), null);
		expect(ctx, in, "beech in a plain greenhouse", block("beech_sapling").getDefaultState(), null);
		expect(ctx, in, "guarana in a plain greenhouse", block("guarana_bush").getDefaultState(), PlantWarning.ENVIRONMENT);
		expect(ctx, in, "mahogany in a plain greenhouse", block("mahogany_sapling").getDefaultState(), PlantWarning.ENVIRONMENT);
		// A lit heater makes it warm: now they grow.
		ctx.setBlockState(corner.add(4, 1, 2), Shrubs.HEATER.getDefaultState().with(GreenhouseHeaterBlock.LIT, true));
		expect(ctx, in, "guarana in a heated greenhouse", block("guarana_bush").getDefaultState(), null);
		expect(ctx, in, "mahogany in a heated greenhouse", block("mahogany_sapling").getDefaultState(), null);
		// A hole in the roof: no greenhouse any more, so it just doesn't grow here.
		ctx.setBlockState(corner.add(1, 4, 1), Blocks.AIR);
		expect(ctx, in, "guarana under a broken roof", block("guarana_bush").getDefaultState(), PlantWarning.HERE);
		ctx.complete();
	}
}
