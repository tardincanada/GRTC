package com.periodicelements.test;

import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.atlas.RadioState;
import com.periodicelements.atlas.StationState;
import com.periodicelements.research.Research;
import com.periodicelements.space.PayloadFairingBlockEntity;
import com.periodicelements.space.Propellant;
import com.periodicelements.space.PropellantTankBlockEntity;
import com.periodicelements.space.Rocket;
import com.periodicelements.space.RocketEngineBlock;
import com.periodicelements.space.RocketEntity;
import com.periodicelements.space.Space;
import com.periodicelements.space.SpaceState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Level 8, stage 1 (3.61.0): research gates, the rocket equation, launch to orbit, the reusable first stage. */
public class SpaceTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static Block block(String path) {
		return Registries.BLOCK.get(Identifier.of("periodic_elements", path));
	}

	/** Builds, on a Launch Pad at {@code pad}, stages of (engine, full tanks) bottom up, then a loaded fairing. */
	static void build(ServerWorld w, BlockPos pad, ItemStack payload, boolean legs, Object... stages) {
		w.setBlockState(pad, block("launch_pad").getDefaultState());
		BlockPos p = pad.up();
		for (int i = 0; i < stages.length; i += 2) {
			Block engine = (Block) stages[i];
			int tanks = (Integer) stages[i + 1];
			w.setBlockState(p, engine.getDefaultState().with(RocketEngineBlock.LEGS, legs && i == 0));
			Propellant prop = ((RocketEngineBlock) engine).propellant;
			p = p.up();
			for (int t = 0; t < tanks; t++, p = p.up()) {
				w.setBlockState(p, Space.PROPELLANT_TANK.getDefaultState());
				PropellantTankBlockEntity tank = (PropellantTankBlockEntity) w.getBlockEntity(p);
				tank.propellant = prop;
				tank.fuel = prop.fuelCapacity;
				tank.oxidizer = prop.oxidizerCapacity;
			}
		}
		w.setBlockState(p, Space.PAYLOAD_FAIRING.getDefaultState());
		((PayloadFairingBlockEntity) w.getBlockEntity(p)).payload = payload.copy();
	}

	private static double dv(Propellant p, double m0, double burn) {
		return p.exhaustVelocity() * Math.log(m0 / (m0 - burn));
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space")
	public void spaceGearNeedsLevelEightResearch(TestContext ctx) {
		for (String id : List.of("kerolox_engine", "hydrolox_engine", "hypergolic_engine", "propellant_tank", "payload_fairing",
				"launch_console", "landing_legs", "weather_satellite", "communication_satellite")) {
			String gate = Space.gate(new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", id))));
			expect(ctx, ("item:" + id).equals(gate), id + " has gate " + gate);
			String project = Research.unlockedBy(gate);
			expect(ctx, project != null && Research.get(project).level() == 8, id + " should be unlocked at level 8");
		}
		expect(ctx, Research.unlockedBy("item:landing_legs").equals("propulsive_landing"), "legs come from Propulsive Landing");
		expect(ctx, Space.gate(new ItemStack(Items.STONE)) == null, "stone needs no research");
		expect(ctx, Research.MAX_LEVEL >= 8, "the tech tree should reach level 8");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space")
	public void oneStageCannotReachOrbitTwoCan(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		Propellant k = Propellant.KEROLOX;
		build(w, pad, new ItemStack(Atlas.SATELLITE), false, Space.KEROLOX_ENGINE, 6);
		Rocket one = Rocket.read(w, pad);
		double m0 = 2 + k.engineDry + 6 * (2 + 56);
		expect(ctx, Math.abs(one.totalDv - dv(k, m0, 336)) < 1, "one stage dv " + one.totalDv);
		expect(ctx, one.totalDv < Propellant.ORBIT_DV && !one.ready(), "one kerosene stage must fall short, has " + one.totalDv);
		build(w, pad, new ItemStack(Atlas.SATELLITE), false, Space.KEROLOX_ENGINE, 4, Space.KEROLOX_ENGINE, 2);
		Rocket two = Rocket.read(w, pad);
		double upper = 2 + 3 + 2 * 58;
		double lower = upper + 3 + 4 * 58;
		double want = dv(k, upper, 112) + dv(k, lower, 224);
		expect(ctx, two.stages.size() == 2, "two stages, found " + two.stages.size());
		expect(ctx, Math.abs(two.totalDv - want) < 1, "two-stage dv " + two.totalDv + ", want " + want);
		expect(ctx, two.ready(), "a 4+2 kerosene rocket should reach orbit: " + two.problems);
		// legs keep 15% of the first stage's propellant: less delta-v
		build(w, pad, new ItemStack(Atlas.SATELLITE), true, Space.KEROLOX_ENGINE, 4, Space.KEROLOX_ENGINE, 2);
		Rocket reuse = Rocket.read(w, pad);
		double lowerLegs = upper + 3 + 1 + 4 * 58;
		double wantLegs = dv(k, upper, 112) + dv(k, lowerLegs, 224 * 0.85);
		expect(ctx, reuse.reusable() && Math.abs(reuse.totalDv - wantLegs) < 1, "reusable dv " + reuse.totalDv + ", want " + wantLegs);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space")
	public void wrongPropellantAndEmptyFairingStopTheCount(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		build(w, pad, ItemStack.EMPTY, false, Space.KEROLOX_ENGINE, 4, Space.HYDROLOX_ENGINE, 2);
		PropellantTankBlockEntity tank = (PropellantTankBlockEntity) w.getBlockEntity(pad.up(2));
		tank.propellant = Propellant.HYDROLOX;
		Rocket r = Rocket.read(w, pad);
		expect(ctx, r.stages.get(0).mixed(), "a hydrogen tank on a kerosene engine should be flagged");
		expect(ctx, r.problems.size() >= 2 && !r.ready(), "mixed tank and empty fairing should both be problems: " + r.problems);
		// a hypergolic engine lifts at most 3 tanks
		build(w, pad, new ItemStack(Atlas.SATELLITE), false, Space.HYPERGOLIC_ENGINE, 4);
		expect(ctx, !Rocket.read(w, pad).ready(), "four tanks are too many for a hypergolic engine");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space_launch", tickLimit = 700)
	public void rocketPutsTheWeatherSatelliteInOrbit(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		UUID owner = UUID.randomUUID();
		build(w, pad, new ItemStack(Space.WEATHER_SATELLITE), false, Space.KEROLOX_ENGINE, 4, Space.KEROLOX_ENGINE, 2);
		RocketEntity rocket = Space.launch(w, pad, owner);
		expect(ctx, rocket != null, "the rocket should lift off");
		expect(ctx, w.getBlockState(pad.up()).isAir() && w.getBlockState(pad.up(9)).isAir(), "the pad should be clear after lift-off");
		ctx.runAtTick(600, () -> {
			expect(ctx, rocket.isRemoved(), "the rocket should have reached orbit by now, at y " + rocket.getY());
			expect(ctx, SpaceState.get(w.getServer()).count(owner, "weather") == 1, "the weather satellite should be in orbit");
			expect(ctx, SpaceState.get(w.getServer()).launches.getOrDefault(owner, 0) == 1, "one launch recorded");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space_landing", tickLimit = 1000)
	public void reusableFirstStageLandsBackOnThePad(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		UUID owner = UUID.randomUUID();
		int before = StationState.get(w.getServer()).satellites(owner);
		build(w, pad, new ItemStack(Atlas.SATELLITE), true, Space.KEROLOX_ENGINE, 5, Space.KEROLOX_ENGINE, 2);
		expect(ctx, Space.launch(w, pad, owner) != null, "the rocket should lift off");
		ctx.runAtTick(900, () -> {
			var engine = w.getBlockState(pad.up());
			expect(ctx, engine.isOf(Space.KEROLOX_ENGINE) && engine.get(RocketEngineBlock.LEGS), "the first stage should be back on the pad, found " + engine);
			for (int i = 2; i <= 6; i++) {
				expect(ctx, w.getBlockEntity(pad.up(i)) instanceof PropellantTankBlockEntity t && t.empty(), "tank " + i + " should be back, empty");
			}
			expect(ctx, w.getBlockState(pad.up(7)).isAir(), "only the first stage comes back");
			expect(ctx, SpaceState.get(w.getServer()).landings.getOrDefault(owner, 0) == 1, "one landing recorded");
			expect(ctx, StationState.get(w.getServer()).satellites(owner) == before + 1, "the mapping satellite should count for the Atlas");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "space")
	public void communicationSatelliteSendsRadioAroundTheWorld(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		ServerPlayerEntity listener = ctx.createMockCreativeServerPlayerInWorld();
		UUID owner = UUID.randomUUID();
		BlockPos far = listener.getBlockPos().add(RadioState.RANGE * 4, 0, 0);
		RadioState radio = RadioState.get(w.getServer());
		radio.transmitters.put(far, owner);
		Set<String> known = Set.of(Research.unlockedBy("map:radio"));
		try {
			List<AtlasData.Marker> none = Atlas.shared(listener, known);
			expect(ctx, none.isEmpty(), "a transmitter 2048 blocks away is out of range");
			SpaceState.get(w.getServer()).add(owner, "communication");
			expect(ctx, !Atlas.shared(listener, known).isEmpty(), "with a communication satellite it reaches the listener");
		} finally {
			radio.transmitters.remove(far);
		}
		ctx.complete();
	}
}
