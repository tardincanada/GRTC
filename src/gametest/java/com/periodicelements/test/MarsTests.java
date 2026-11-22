package com.periodicelements.test;

import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.space.Mars;
import com.periodicelements.space.MarsChunkGenerator;
import com.periodicelements.space.Moon;
import com.periodicelements.space.Propellant;
import com.periodicelements.space.Rocket;
import com.periodicelements.space.Space;
import com.periodicelements.space.Vacuum;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.Map;

/** Level 9, stage 5 (3.66.0): Mars, its ground and storms, its gravity, the flight there and back, and living off the land. */
public class MarsTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mars")
	public void marsResearchAndTheWayThere(TestContext ctx) {
		Map<String, String> gates = Map.of("item:mars_capsule", "mars_transit", "item:methalox_engine", "methalox_rockets",
				"item:washed_martian_soil", "martian_greenhouse", "reaction:solid_oxide_electrolysis", "martian_oxygen",
				"reaction:martian_carbon_dioxide", "martian_oxygen", "reaction:sabatier_reaction", "sabatier_process",
				"reaction:methane_liquefaction", "methalox_rockets");
		gates.forEach((u, p) -> expect(ctx, p.equals(Research.unlockedBy(u)) && Research.get(p).level() == 9,
				u + " should come from " + p + ", comes from " + Research.unlockedBy(u)));
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("sabatier_reaction") && r.machines().contains("high_pressure_reactor")),
				"the Sabatier reaction runs in the High Pressure Reactor");
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("solid_oxide_electrolysis") && r.machines().contains("electrolysis_cell")),
				"MOXIE is an Electrolysis Cell reaction");
		var server = ctx.getWorld().getServer();
		expect(ctx, server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).containsId(Identifier.of("periodic_elements", "mars")),
				"Mars's dimension type is loaded");
		World earth = ctx.getWorld();
		expect(ctx, Moon.destination(earth, new ItemStack(Mars.MARS_CAPSULE)) == Mars.MARS, "the Mars capsule flies to Mars");
		expect(ctx, Moon.destination(earth, new ItemStack(Moon.CREW_CAPSULE)) == Moon.MOON, "the other capsule still flies to the Moon");
		ServerWorld mars = server.getWorld(Mars.MARS);
		if (mars != null) {   // the test server has no data-pack dimensions; a real world has
			expect(ctx, mars.getChunkManager().getChunkGenerator() instanceof MarsChunkGenerator, "Mars has its own generator");
			expect(ctx, Moon.airless(mars) && Moon.destination(mars, new ItemStack(Mars.MARS_CAPSULE)) == World.OVERWORLD,
					"Mars has no breathable air and leads home");
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mars")
	public void groundHasLowlandsHighlandsVolcanoesCanyonsAndCaps(TestContext ctx) {
		MarsChunkGenerator.Terrain t = new MarsChunkGenerator.Terrain(20261116L);
		EnumMap<MarsChunkGenerator.Ground, Integer> kinds = new EnumMap<>(MarsChunkGenerator.Ground.class);
		double north = 0, south = 0;
		int nN = 0, nS = 0, highest = 0, lowest = 999;
		for (int x = -6000; x <= 6000; x += 40) {
			for (int z = -2400; z <= 2400; z += 40) {
				MarsChunkGenerator.Column c = t.column(x, z);
				kinds.merge(c.ground(), 1, Integer::sum);
				highest = Math.max(highest, c.top());
				lowest = Math.min(lowest, c.top());
				if (z < -700 && Math.abs(z) < MarsChunkGenerator.CAP && c.ground() == MarsChunkGenerator.Ground.PLAIN) {
					north += c.top();
					nN++;
				} else if (z > 700 && z < MarsChunkGenerator.CAP && c.ground() == MarsChunkGenerator.Ground.HIGHLAND) {
					south += c.top();
					nS++;
				}
			}
		}
		for (MarsChunkGenerator.Ground g : MarsChunkGenerator.Ground.values()) {
			expect(ctx, kinds.getOrDefault(g, 0) > 0, "no " + g + " ground found: " + kinds);
		}
		expect(ctx, nN > 0 && nS > 0 && south / nS > north / nN + 12, "the south stands higher than the north: "
				+ south / Math.max(1, nS) + " vs " + north / Math.max(1, nN));
		expect(ctx, highest > 180, "a giant volcano rises over a hundred blocks: highest " + highest);
		expect(ctx, lowest < 50, "a canyon cuts deep: lowest " + lowest);
		// what lies under the ground
		boolean sulfate = false, berries = false, groundIce = false, frost = false, capIce = false;
		for (int x = -6000; x <= 6000 && !(sulfate && berries && groundIce && frost && capIce); x += 24) {
			for (int z = -2400; z <= 2400; z += 24) {
				MarsChunkGenerator.Column c = t.column(x, z);
				for (int y = Math.max(1, c.top() - 8); y <= c.top(); y++) {
					BlockState s = MarsChunkGenerator.block(x, y, z, c);
					sulfate |= s.isOf(Mars.MARTIAN_SULFATE);
					berries |= s.isOf(Mars.HEMATITE_CONCRETION);
					groundIce |= s.isOf(Mars.MARTIAN_ICE) && Math.abs(z) < MarsChunkGenerator.CAP && c.top() - y >= 3;
					frost |= s.isOf(Mars.DRY_ICE);
					capIce |= s.isOf(Mars.MARTIAN_ICE) && Math.abs(z) > MarsChunkGenerator.CAP;
				}
			}
		}
		expect(ctx, sulfate && berries, "sulfate beds with hematite concretions");
		expect(ctx, groundIce, "ice under the soil at high latitudes");
		expect(ctx, frost && capIce, "polar caps of ice frosted with dry ice");
		MarsChunkGenerator.Column equator = null;
		for (int x = 0; equator == null && x < 4000; x += 16) {
			MarsChunkGenerator.Column c = t.column(x, 0);
			if (c.ground() == MarsChunkGenerator.Ground.PLAIN) {
				equator = c;
				expect(ctx, MarsChunkGenerator.block(x, c.top(), 0, c).isOf(Mars.MARTIAN_REGOLITH), "red regolith on top");
				expect(ctx, MarsChunkGenerator.block(x, c.top() - 5, 0, c).isOf(Mars.MARTIAN_BASALT), "basalt under it, no ice");
			}
		}
		expect(ctx, equator != null, "a plain near the equator");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mars")
	public void gravityStormsSunlightAndWashedSoil(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		ZombieEntity z = EntityType.ZOMBIE.create(w);
		double g = z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY);
		Moon.gravity(z, Moon.GRAVITY);
		Moon.gravity(z, Mars.GRAVITY);   // from the Moon to Mars: the pull changes, it does not stack
		expect(ctx, Math.abs(z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY) - g * Mars.GRAVITY) < 1e-9,
				"Mars pulls 0.38 as hard: " + z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY));
		Moon.gravity(z, 1.0);
		expect(ctx, Math.abs(z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY) - g) < 1e-9, "back to full gravity at home");
		// storms: some days, never all
		int stormy = 0, days = 600;
		float peak = 0;
		for (int d = 0; d < days; d++) {
			float s = Mars.storm(d * 24000L + 12000L);
			if (s > 0.05f) {
				stormy++;
			}
			peak = Math.max(peak, s);
		}
		expect(ctx, stormy > days / 20 && stormy < days / 2, "dust storms on some days: " + stormy + " of " + days);
		expect(ctx, peak > 0.95f, "a storm at its height blots out the sky");
		expect(ctx, Mars.sunlight(w) == 1 && Mars.storm(w) == 0, "no storms and full sunlight on the Earth");
		expect(ctx, Vacuum.hasAir(w, ctx.getAbsolutePos(BlockPos.ORIGIN)), "plants on the Earth have air");
		expect(ctx, Mars.WASHED_SOIL.getDefaultState().isIn(BlockTags.DIRT), "washed soil counts as dirt");
		expect(ctx, w.getRecipeManager().get(Identifier.of("periodic_elements", "washed_martian_soil")).isPresent(),
				"regolith and a water bucket make washed soil");
		expect(ctx, Mars.AIR.get("carbon_dioxide") > 900, "Martian air is carbon dioxide");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "mars")
	public void marsNeedsFourStagesAndMethaneBringsYouHome(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		ItemStack capsule = new ItemStack(Mars.MARS_CAPSULE);
		SpaceTests.build(w, pad, capsule, false, Space.KEROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 3);
		Rocket moonRocket = Rocket.read(w, pad);
		expect(ctx, moonRocket.crew && moonRocket.destination == Mars.MARS && moonRocket.targetDv == Propellant.MARS_DV,
				"a Mars capsule flight goes to Mars");
		expect(ctx, moonRocket.totalDv > Propellant.MOON_DV && !moonRocket.ready(), "a Moon rocket falls short of Mars: " + moonRocket.totalDv);
		SpaceTests.build(w, pad, capsule, false, Space.KEROLOX_ENGINE, 6, Space.KEROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 6,
				Space.HYDROLOX_ENGINE, 2);
		Rocket big = Rocket.read(w, pad);
		expect(ctx, big.ready(), "four stages reach Mars: " + big.totalDv + " " + big.problems);
		for (int y = 1; y < 40; y++) {
			w.removeBlock(pad.up(y), false);
		}
		SpaceTests.build(w, pad, capsule, false, Space.METHALOX_ENGINE, 2);
		Rocket home = Rocket.read(w, pad);
		expect(ctx, home.totalDv > Propellant.MARS_RETURN_DV, "one methane stage of two tanks brings a crew home from Mars: " + home.totalDv);
		expect(ctx, Propellant.MARS_RETURN_DV > Propellant.RETURN_DV, "leaving Mars takes more than leaving the Moon");
		ctx.complete();
	}
}
