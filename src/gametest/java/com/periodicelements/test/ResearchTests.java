package com.periodicelements.test;

import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import com.periodicelements.research.ResearchStationBlockEntity;
import com.periodicelements.research.TreatiseContents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ResearchTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void onlyKeyProjectsGateLevels(TestContext ctx) {
		expect(ctx, Research.techLevel(Set.of()) == 0, "empty research should be level 0");
		Set<String> keys1 = Set.of("lime_burning", "copper_smelting", "bronze_alloying", "iron_smelting");
		expect(ctx, Research.techLevel(keys1) == 1, "level 1 key projects should give level 1");
		Set<String> missing = Set.of("lime_burning", "copper_smelting", "bronze_alloying");
		expect(ctx, Research.techLevel(missing) == 0, "a missing key project must block the level");
		Research.Project alembic = Research.get("alembic_distillation");
		expect(ctx, Research.status(alembic, missing) == Research.Status.NEEDS_LEVEL, "level 2 project open too early");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void stationResearchesWithSamples(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "potters_workshop")));
		ResearchStationBlockEntity station = ctx.getBlockEntity(pos);
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		ResearchState state = ResearchState.get(ctx.getWorld().getServer());

		Research.Project pottery = Research.get("fired_pottery");
		expect(ctx, station.startResearch(player, pottery) != null, "started without samples");
		station.setStack(0, new ItemStack(Items.CLAY_BALL, 8));
		station.setStack(1, new ItemStack(Items.CHARCOAL, 4));
		expect(ctx, station.startResearch(player, pottery) == null, "could not start with samples");
		expect(ctx, station.getStack(0).isEmpty() && station.getStack(1).isEmpty(), "samples not consumed");
		for (int i = 0; i < Research.duration(pottery); i++) {
			station.tick(ctx.getWorld());
		}
		expect(ctx, state.known(player.getUuid()).contains("fired_pottery"), "project not granted");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void limeBurningTakesDripstone(TestContext ctx) {
		// Since 1.4.1 a sample can name alternatives: Lime Burning takes calcite or dripstone blocks.
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "potters_workshop")));
		ResearchStationBlockEntity station = ctx.getBlockEntity(pos);
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), player.getUuid(), List.of("fired_pottery"));
		Research.Project lime = Research.get("lime_burning");
		station.setStack(0, new ItemStack(Items.DRIPSTONE_BLOCK, 8));
		station.setStack(1, new ItemStack(Items.CHARCOAL, 8));
		expect(ctx, station.startResearch(player, lime) == null, "dripstone should do for calcite");
		expect(ctx, station.getStack(0).isEmpty(), "dripstone not consumed");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void treatiseTransfersKnowledge(TestContext ctx) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ctx.setBlockState(pos, Registries.BLOCK.get(Identifier.of(PeriodicElements.MOD_ID, "potters_workshop")));
		ResearchStationBlockEntity station = ctx.getBlockEntity(pos);
		ServerPlayerEntity writer = ctx.createMockCreativeServerPlayerInWorld();
		var server = ctx.getWorld().getServer();
		ResearchState state = ResearchState.get(server);
		List<String> known = List.of("writing", "fired_pottery", "lime_burning");
		state.grant(server, writer.getUuid(), known);

		// 3 projects: 1 leather + 3 paper + 3 ink. Two paper is not enough.
		station.setStack(0, new ItemStack(Items.LEATHER, 1));
		station.setStack(1, new ItemStack(Items.PAPER, 2));
		station.setStack(2, new ItemStack(Items.INK_SAC, 3));
		expect(ctx, station.writeTreatise(writer) != null, "wrote a treatise without enough paper");
		station.setStack(1, new ItemStack(Items.PAPER, 3));
		expect(ctx, station.writeTreatise(writer) == null, "could not write the treatise");
		ItemStack treatise = ItemStack.EMPTY;
		for (int i = 0; i < writer.getInventory().size(); i++) {
			if (writer.getInventory().getStack(i).isOf(ModContent.TREATISE_MANUSCRIPT)) {
				treatise = writer.getInventory().getStack(i);
			}
		}
		expect(ctx, !treatise.isEmpty(), "treatise not given to the writer");
		TreatiseContents contents = treatise.get(ModContent.TREATISE_CONTENTS);
		expect(ctx, contents != null && contents.projects().size() == 3, "treatise should hold 3 projects");

		// A second player reads it: learns everything at once, and (in survival) the book is consumed.
		ServerPlayerEntity reader = ctx.createMockCreativeServerPlayerInWorld();
		reader.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
		UUID readerId = reader.getUuid();
		reader.setStackInHand(Hand.MAIN_HAND, treatise.copy());
		reader.getStackInHand(Hand.MAIN_HAND).use(ctx.getWorld(), reader, Hand.MAIN_HAND);
		expect(ctx, state.known(readerId).containsAll(known), "reader did not learn the treatise");
		expect(ctx, reader.getStackInHand(Hand.MAIN_HAND).isEmpty(), "treatise not consumed");
		expect(ctx, state.known(writer.getUuid()).containsAll(known), "writer lost knowledge");
		ctx.complete();
	}
}
