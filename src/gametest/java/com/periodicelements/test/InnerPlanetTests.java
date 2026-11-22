package com.periodicelements.test;

import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.space.Fiction;
import com.periodicelements.space.Mercury;
import com.periodicelements.space.MercuryChunkGenerator;
import com.periodicelements.space.Moon;
import com.periodicelements.space.Propellant;
import com.periodicelements.space.Rocket;
import com.periodicelements.space.Space;
import com.periodicelements.space.Venus;
import com.periodicelements.space.VenusChunkGenerator;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/** Level 10, stage 7 (3.68.0): Mercury's dawn band and Venus over the clouds, and the fiction engines that reach them. */
public class InnerPlanetTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "inner")
	public void researchAndTheWayToMercuryAndVenus(TestContext ctx) {
		Map<String, String> gates = Map.of("item:mercury_capsule", "mercury_landing", "item:venus_capsule", "venus_aerostat",
				"item:aerostat_deck", "venus_aerostat", "item:venus_probe", "venus_aerostat",
				"reaction:venus_sulfuric_acid", "venus_aerostat", "reaction:venus_sulfur_dioxide", "venus_aerostat");
		gates.forEach((u, p) -> expect(ctx, p.equals(Research.unlockedBy(u)) && Research.get(p).level() == 10,
				u + " should come from " + p + ", comes from " + Research.unlockedBy(u)));
		expect(ctx, Research.get("mercury_landing").requires().contains("island_of_stability"), "Mercury needs the Thermal Suit's alloy");
		var server = ctx.getWorld().getServer();
		for (String d : new String[]{"mercury", "venus"}) {
			expect(ctx, server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).containsId(Identifier.of("periodic_elements", d)),
					d + "'s dimension type is loaded");
		}
		World earth = ctx.getWorld();
		ItemStack mercury = new ItemStack(Mercury.MERCURY_CAPSULE), venus = new ItemStack(Venus.VENUS_CAPSULE);
		expect(ctx, Moon.destination(earth, mercury) == Mercury.MERCURY && Moon.destination(earth, venus) == Venus.VENUS,
				"each capsule flies to its own world");
		expect(ctx, "crew".equals(Space.payloadKind(mercury)) && "crew".equals(Space.payloadKind(venus)), "both carry a crew");
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("venus_sulfuric_acid") && r.machines().contains("air_liquefier")),
				"the Air Liquefier catches the clouds' acid");
		double air = Venus.AIR.values().stream().mapToDouble(Double::doubleValue).sum();
		expect(ctx, Math.abs(air - 1000) < 1e-6 && Venus.AIR.get("carbon_dioxide") > 900, "Venus's air is carbon dioxide: " + Venus.AIR);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "inner")
	public void onlyFictionEnginesReachTheInnerPlanets(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		ItemStack mercury = new ItemStack(Mercury.MERCURY_CAPSULE), venus = new ItemStack(Venus.VENUS_CAPSULE);
		SpaceTests.build(w, pad, mercury, false, Space.KEROLOX_ENGINE, 6, Space.KEROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 6,
				Space.HYDROLOX_ENGINE, 2);
		Rocket chemical = Rocket.read(w, pad);
		expect(ctx, chemical.crew && chemical.destination == Mercury.MERCURY && chemical.targetDv == Propellant.MERCURY_DV,
				"a Mercury capsule flight goes to Mercury");
		expect(ctx, !chemical.ready(), "four chemical stages fall short of Mercury: " + chemical.totalDv);
		clear(w, pad);
		SpaceTests.build(w, pad, venus, false, Space.KEROLOX_ENGINE, 6, Space.KEROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 6,
				Space.HYDROLOX_ENGINE, 2);
		Rocket toVenus = Rocket.read(w, pad);
		expect(ctx, toVenus.destination == Venus.VENUS && !toVenus.ready(), "and of Venus: " + toVenus.totalDv);
		clear(w, pad);
		SpaceTests.build(w, pad, mercury, false, Space.METALLIC_HYDROGEN_ENGINE, 6, Space.METALLIC_HYDROGEN_ENGINE, 6);
		Rocket metallic = Rocket.read(w, pad);
		expect(ctx, metallic.ready(), "two metallic hydrogen stages reach Mercury: " + metallic.totalDv + " " + metallic.problems);
		clear(w, pad);
		SpaceTests.build(w, pad, venus, false, Space.KEROLOX_ENGINE, 6, Space.FUSION_DRIVE, 3);
		Rocket fusion = Rocket.read(w, pad);
		expect(ctx, fusion.ready(), "a fusion drive over a chemical stage reaches Venus: " + fusion.totalDv + " " + fusion.problems);
		clear(w, pad);
		expect(ctx, Propellant.MERCURY_DV > Propellant.VENUS_DV && Propellant.VENUS_DV > Propellant.MARS_DV,
				"Mercury is the hardest to reach, then Venus, then Mars");
		ctx.complete();
	}

	private static void clear(ServerWorld w, BlockPos pad) {
		for (int y = 0; y < 40; y++) {
			w.removeBlock(pad.up(y), false);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "inner")
	public void mercuryDawnCrawlsWestBetweenHeatAndCold(TestContext ctx) {
		long t = 240000;
		double line = Mercury.dawnLine(t);
		expect(ctx, Mercury.dawnLine(t + Mercury.TICKS_PER_BLOCK) == line - 1, "the dawn moves a block west each minute");
		expect(ctx, Math.abs(Mercury.temperature(t, line) - 20) < 1e-9, "mild on the dawn line");
		expect(ctx, Mercury.temperature(t, line + 300) == 430 && Mercury.temperature(t, line - 300) == -180,
				"430 °C in the day, -180 °C in the night");
		expect(ctx, Mercury.temperature(t, line + 40) < Mercury.SUIT_HOT && Mercury.temperature(t, line + 80) > Mercury.SUIT_HOT,
				"a band of a few dozen blocks to live in");
		// standing still, the day comes to you
		expect(ctx, Mercury.temperature(t + 70L * Mercury.TICKS_PER_BLOCK, line) > Mercury.SUIT_HOT, "a little over an hour standing still and it is too hot");
		expect(ctx, Mercury.sunlight(t, line - 10) == 0 && Math.abs(Mercury.sunlight(t, line + 300) - Mercury.SUNLIGHT) < 1e-9,
				"no sun in the night, 6.7 times the Earth's in the day");
		expect(ctx, Mercury.GRAVITY == 0.38 && Mercury.COSMIC > 1, "small, airless and close to the Sun");
		expect(ctx, MercuryChunkGenerator.block(60, 64, false, false).isOf(Mercury.MERCURIAN_CRUST)
				&& MercuryChunkGenerator.block(64, 64, false, false).isOf(Mercury.MERCURIAN_REGOLITH)
				&& MercuryChunkGenerator.block(64, 64, false, true).isOf(Mercury.MERCURIAN_HOLLOWS)
				&& MercuryChunkGenerator.block(64, 64, true, false).isOf(Moon.LUNAR_ICE), "crust, regolith, hollows and polar ice");
		ServerWorld mercury = ctx.getWorld().getServer().getWorld(Mercury.MERCURY);
		if (mercury != null) {   // the test server has no data-pack dimensions; a real world has
			expect(ctx, Moon.airless(mercury) && Moon.gravityOf(mercury) == Mercury.GRAVITY, "Mercury has no air and low gravity");
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "inner")
	public void venusCloudsBurnAndProbesBringBackTheGround(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		VenusChunkGenerator.Clouds c = new VenusChunkGenerator.Clouds(20261116L);
		int inLayer = 0, outside = 0;
		for (int x = 0; x < 64; x += 2) {
			for (int z = 0; z < 64; z += 2) {
				for (int y = 0; y < 200; y += 2) {
					if (c.cloud(x, y, z)) {
						if (y >= Venus.CLOUD_BOTTOM && y <= Venus.CLOUD_TOP) {
							inLayer++;
						} else {
							outside++;
						}
					}
				}
			}
		}
		int samples = 32 * 32 * ((Venus.CLOUD_TOP - Venus.CLOUD_BOTTOM) / 2 + 1);
		expect(ctx, outside == 0 && inLayer > samples / 8 && inLayer < samples * 7 / 8,
				"clouds fill part of the layer, with gaps, and nothing above or below: " + inLayer + " of " + samples);
		BlockPos at = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		w.setBlockState(at, Venus.VENUS_CLOUD.getDefaultState());
		expect(ctx, w.getBlockState(at).getCollisionShape(w, at).isEmpty(), "a cloud holds nothing up");

		ZombieEntity z = EntityType.ZOMBIE.create(w);
		z.refreshPositionAndAngles(at, 0, 0);
		w.spawnEntity(z);
		ItemStack helmet = new ItemStack(Items.IRON_HELMET);
		z.equipStack(EquipmentSlot.HEAD, helmet);
		expect(ctx, Venus.inCloud(w, z), "the zombie stands in the cloud");
		float hp = z.getHealth();
		Venus.corrode(w, z);
		expect(ctx, z.getHealth() < hp && z.getEquippedStack(EquipmentSlot.HEAD).getDamage() > 0,
				"the acid burns and eats at iron: " + z.getHealth() + " " + z.getEquippedStack(EquipmentSlot.HEAD).getDamage());
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < 4; i++) {
			z.equipStack(slots[i], new ItemStack(Fiction.THERMAL_SUIT[i]));
		}
		z.setHealth(z.getMaxHealth());
		z.timeUntilRegen = 0;
		Venus.corrode(w, z);
		expect(ctx, z.getHealth() == z.getMaxHealth(), "Stability Alloy shrugs it off");
		z.discard();
		w.removeBlock(at, false);

		var sample = Venus.sample();
		expect(ctx, sample.stream().anyMatch(s -> s.isOf(Venus.VENUSIAN_BASALT.asItem()))
				&& sample.stream().anyMatch(s -> s.isOf(Venus.METALLIC_SNOW.asItem())), "a probe sends up basalt and metallic snow");
		expect(ctx, Venus.GRAVITY > 0.9 && Venus.SUNLIGHT > 1 && Propellant.VENUS_RETURN_DV > Propellant.MARS_RETURN_DV,
				"Venus is almost the Earth's size: a deep well to climb home from");
		ServerWorld venus = w.getServer().getWorld(Venus.VENUS);
		if (venus != null) {
			Venus.deck(venus, 4000, 4000);
			expect(ctx, venus.getBlockState(new BlockPos(4000, Venus.DECK_Y, 4000)).isOf(Venus.AEROSTAT_DECK)
					&& Moon.surface(venus, 4000, 4000) == Venus.DECK_Y + 1, "the capsule's deck floats over the clouds");
		}
		ctx.complete();
	}
}
