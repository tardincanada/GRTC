package com.periodicelements.test;

import com.periodicelements.BlockComposition;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.atlas.Satellites;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.space.Meteorites;
import com.periodicelements.space.Space;
import com.periodicelements.space.SpaceState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.UUID;

/** Level 8, stage 2 (3.62.0): meteorites, their craters, the Mond Reactor's reactions and the Space Telescope. */
public class MeteoriteTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space")
	public void meteoritesHaveCompositionsReactionsAndAPageEach(TestContext ctx) {
		var iron = BlockComposition.of("periodic_elements:iron_meteorite");
		expect(ctx, iron != null && iron.amount() == 1000, "iron meteorite composition");
		double ir = iron.parts().stream().filter(p -> p.id().equals("iridium")).mapToDouble(BlockComposition.Part::units).sum();
		expect(ctx, ir >= 6 && ir <= 8, "an iron meteorite block holds about 7 units of iridium, has " + ir);
		expect(ctx, BlockComposition.of("periodic_elements:chondrite") != null
				&& BlockComposition.of("periodic_elements:carbonaceous_chondrite") != null, "stony meteorites have compositions");
		for (String r : new String[]{"kamacite_parting", "taenite_parting", "meteoric_iridium", "meteoric_platinum"}) {
			var found = Reactions.all().stream().filter(x -> x.id().equals(r)).findFirst();
			expect(ctx, found.isPresent() && found.get().machines().contains("mond_reactor")
					&& "meteorite_analysis".equals(found.get().project()), r + " runs in the Mond Reactor after Meteorite Analysis");
		}
		expect(ctx, CompendiumData.GROUPS.contains("meteorites"), "the Mineral Compendium has a Meteorites group");
		expect(ctx, "item:space_telescope".equals(Space.gate(new ItemStack(Space.SPACE_TELESCOPE)))
				&& Research.get(Research.unlockedBy("item:space_telescope")).level() == 8, "the Space Telescope is level 8 research");
		expect(ctx, "telescope".equals(Space.payloadKind(new ItemStack(Space.SPACE_TELESCOPE))), "the telescope is a payload");
		expect(ctx, Meteorites.clock(13000).equals("19:00") && Meteorites.clock(18000).equals("00:00")
				&& Meteorites.clock(24000 + 1500).equals("07:30"), "time of day as a clock");
		expect(ctx, Meteorites.direction(0, -10).equals("n") && Meteorites.direction(10, 0).equals("e")
				&& Meteorites.direction(-7, 7).equals("sw"), "compass directions");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "meteor_impact", tickLimit = 40)
	public void impactLeavesACraterWithTheMeteoriteAtTheBottom(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos c = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		// solid ground 8 deep under a 13-wide square, so the crater has something to dig
		for (int dx = -7; dx <= 7; dx++) {
			for (int dz = -7; dz <= 7; dz++) {
				for (int dy = -8; dy <= 0; dy++) {
					w.setBlockState(c.add(dx, dy, dz), Blocks.STONE.getDefaultState());
				}
				for (int dy = 1; dy <= 4; dy++) {
					w.setBlockState(c.add(dx, dy, dz), Blocks.AIR.getDefaultState());
				}
			}
		}
		int surface = c.getY();
		UUID owner = UUID.randomUUID();
		SpaceState.get(w.getServer()).add(owner, "telescope");
		int before = SpaceState.get(w.getServer()).falls;
		BlockPos m = Meteorites.impact(w, c.getX(), surface, c.getZ(), Meteorites.IRON_METEORITE, Random.create(42), false);
		expect(ctx, w.getBlockState(m).isOf(Meteorites.IRON_METEORITE), "an iron meteorite at the bottom, found " + w.getBlockState(m));
		expect(ctx, m.getY() <= surface - 2, "the meteorite lies at least 2 below the old surface: " + m.getY() + " vs " + surface);
		expect(ctx, w.getBlockState(m.up()).isAir() && w.getBlockState(new BlockPos(m.getX(), surface, m.getZ())).isAir(), "open crater above it");
		expect(ctx, w.getBlockState(new BlockPos(m.getX() + 2, surface, m.getZ())).isAir(), "the crater is wider than one block");
		expect(ctx, SpaceState.get(w.getServer()).falls == before + 1, "the fall is counted");
		AtlasData atlas = AtlasData.of(w.getServer(), owner);
		expect(ctx, atlas.markers.stream().anyMatch(k -> k.name().equals(Satellites.METEOR) && k.x() == m.getX() && k.z() == m.getZ()),
				"a telescope owner gets the fall on their Atlas");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "meteor_fall", tickLimit = 300)
	public void scheduledFallIsForecastThenComesDown(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos c = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		for (int dx = -7; dx <= 7; dx++) {
			for (int dz = -7; dz <= 7; dz++) {
				for (int dy = -8; dy <= 0; dy++) {
					w.setBlockState(c.add(dx, dy, dz), Blocks.STONE.getDefaultState());
				}
			}
		}
		UUID owner = UUID.randomUUID();
		SpaceState st = SpaceState.get(w.getServer());
		st.add(owner, "telescope");
		int before = st.falls;
		Meteorites.schedule(w, c.getX(), c.getZ(), w.getTimeOfDay());
		expect(ctx, st.fallAt >= 0, "tonight's fall is set");
		ctx.runAtTick(250, () -> {
			expect(ctx, st.fallAt < 0 && st.falls == before + 1, "the meteorite should have come down by now (falls " + st.falls + ")");
			boolean found = false;
			for (int dx = -1; dx <= 1 && !found; dx++) {
				for (int dz = -1; dz <= 1 && !found; dz++) {
					for (int dy = -10; dy <= 24 && !found; dy++) {
						found = Meteorites.isMeteorite(w.getBlockState(c.add(dx, dy, dz)));
					}
				}
			}
			expect(ctx, found, "a meteorite block lies in the crater");
			ctx.complete();
		});
	}
}
