package com.periodicelements.test;

import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.space.Fiction;
import com.periodicelements.space.Moon;
import com.periodicelements.space.Propellant;
import com.periodicelements.space.Rocket;
import com.periodicelements.space.Space;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/** Level 10, stage 6 (3.67.0): the materials of science fiction, their engines, the shield, the suit and the reactor. */
public class FictionTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static ItemStack stack(String id, int n) {
		return new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", id)), n);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fiction")
	public void fictionIsLevelTenResearch(TestContext ctx) {
		expect(ctx, Research.MAX_LEVEL == 10, "level 10 is open: " + Research.MAX_LEVEL);
		Map<String, String> gates = Map.ofEntries(
				Map.entry("item:magnetic_shield", "room_temperature_superconductivity"),
				Map.entry("reaction:superconductor_synthesis", "room_temperature_superconductivity"),
				Map.entry("item:unbihexium", "island_of_stability"),
				Map.entry("item:stability_alloy_ingot", "island_of_stability"),
				Map.entry("item:thermal_suit_chestplate", "island_of_stability"),
				Map.entry("reaction:unbihexium_synthesis", "island_of_stability"),
				Map.entry("item:metallic_hydrogen_engine", "metallic_hydrogen"),
				Map.entry("reaction:hydrogen_metallization", "metallic_hydrogen"),
				Map.entry("item:antimatter_engine", "antimatter_containment"),
				Map.entry("machine:annihilation_reactor", "antimatter_containment"),
				Map.entry("reaction:antihydrogen_trapping", "antimatter_containment"),
				Map.entry("item:fusion_drive", "fusion_propulsion"));
		gates.forEach((u, p) -> expect(ctx, p.equals(Research.unlockedBy(u)) && Research.get(p).level() == 10,
				u + " should come from " + p + ", comes from " + Research.unlockedBy(u)));
		for (String id : Fiction.FICTION) {
			expect(ctx, Registries.ITEM.containsId(Identifier.of("periodic_elements", id)), "fiction item " + id + " exists");
		}
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("unbihexium_synthesis")
				&& r.machines().contains("heavy_ion_accelerator")), "element 126 comes from the Heavy-Ion Accelerator");
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("hydrogen_metallization")
				&& r.machines().contains("high_pressure_reactor")), "metallic hydrogen comes from the High Pressure Reactor");
		Block station = Registries.BLOCK.get(Identifier.of("periodic_elements", "frontier_laboratory"));
		expect(ctx, station != null && Registries.BLOCK.getId(station).getPath().equals("frontier_laboratory"), "a level 10 station");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fiction")
	public void fictionEnginesGoFartherThanChemistry(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		ItemStack capsule = new ItemStack(Moon.CREW_CAPSULE);
		SpaceTests.build(w, pad, capsule, false, Space.KEROLOX_ENGINE, 6, Space.KEROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 6,
				Space.HYDROLOX_ENGINE, 2);
		double chemistry = Rocket.read(w, pad).totalDv;
		clear(w, pad);
		SpaceTests.build(w, pad, capsule, false, Space.METALLIC_HYDROGEN_ENGINE, 6, Space.METALLIC_HYDROGEN_ENGINE, 6);
		double metallic = Rocket.read(w, pad).totalDv;
		expect(ctx, metallic > 20000 && metallic > chemistry + 5000, "two metallic hydrogen stages beat four chemical ones: "
				+ metallic + " vs " + chemistry);
		clear(w, pad);
		SpaceTests.build(w, pad, capsule, false, Space.ANTIMATTER_ENGINE, 6);
		Rocket anti = Rocket.read(w, pad);
		expect(ctx, anti.ready() && anti.totalDv > metallic, "one antimatter stage beats them: " + anti.totalDv);
		clear(w, pad);
		SpaceTests.build(w, pad, capsule, false, Space.FUSION_DRIVE, 3);
		Rocket alone = Rocket.read(w, pad);
		expect(ctx, !alone.ready() && alone.problems.stream().anyMatch(t -> t.getContent() instanceof net.minecraft.text.TranslatableTextContent c
				&& c.getKey().endsWith("no_liftoff")), "a fusion drive cannot lift off: " + alone.problems);
		clear(w, pad);
		SpaceTests.build(w, pad, capsule, false, Space.KEROLOX_ENGINE, 6, Space.FUSION_DRIVE, 3);
		Rocket stacked = Rocket.read(w, pad);
		expect(ctx, stacked.ready() && stacked.stages.get(1).dv() > 40000, "over a chemical stage it flies far: " + stacked.totalDv
				+ " " + stacked.problems);
		expect(ctx, Propellant.FUSION.isp > Propellant.ANTIMATTER.isp && Propellant.ANTIMATTER.isp > Propellant.METALLIC.isp,
				"fusion > antimatter > metallic hydrogen");
		ctx.complete();
	}

	private static void clear(ServerWorld w, BlockPos pad) {
		for (int y = 1; y < 40; y++) {
			w.removeBlock(pad.up(y), false);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fiction")
	public void shieldTurnsRaysAndSuitTakesHeat(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos s = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		expect(ctx, !Fiction.shielded(w, s.up(4)), "no field before the shield is placed");
		w.setBlockState(s, Fiction.MAGNETIC_SHIELD.getDefaultState());
		expect(ctx, Fiction.shielded(w, s.up(4)) && Fiction.shielded(w, s.east(15)), "inside the field");
		expect(ctx, !Fiction.shielded(w, s.east(Fiction.SHIELD_RANGE + 3)), "outside the field");
		expect(ctx, Fiction.MAGNETIC_SHIELD.getDefaultState().getLuminance() > 0, "the field glows");
		w.removeBlock(s, false);
		expect(ctx, !Fiction.shielded(w, s.up(4)), "the field goes with the shield");

		ZombieEntity z = EntityType.ZOMBIE.create(w);
		z.refreshPositionAndAngles(ctx.getAbsolutePos(new BlockPos(3, 2, 3)), 0, 0);
		w.spawnEntity(z);
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < 4; i++) {
			z.equipStack(slots[i], new ItemStack(Fiction.THERMAL_SUIT[i]));
		}
		expect(ctx, Moon.wearingSuit(z) && Fiction.wearingThermalSuit(z), "the Thermal Suit is a space suit too");
		float hp = z.getHealth();
		z.damage(w.getDamageSources().onFire(), 4);
		z.damage(w.getDamageSources().hotFloor(), 4);
		expect(ctx, z.getHealth() == hp, "fire and hot ground cannot reach inside: " + z.getHealth() + " of " + hp);
		z.equipStack(EquipmentSlot.HEAD, new ItemStack(Moon.SPACE_SUIT[0]));
		expect(ctx, Moon.wearingSuit(z) && !Fiction.wearingThermalSuit(z), "a mixed suit still holds air, but not the heat");
		z.discard();
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fiction", tickLimit = 100)
	public void annihilationReactorRunsOnAntihydrogen(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos at = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		w.setBlockState(at, Registries.BLOCK.get(Identifier.of("periodic_elements", "annihilation_reactor")).getDefaultState());
		PowerBlockEntity be = (PowerBlockEntity) w.getBlockEntity(at);
		ctx.runAtTick(10, () -> {
			expect(ctx, be.stored() == 0, "nothing to annihilate yet");
			be.setStack(0, stack("antihydrogen", 1));
			be.setStack(1, stack("hydrogen", 1));
		});
		ctx.runAtTick(40, () -> {
			expect(ctx, be.stored() > 10000, "a flask of antihydrogen gives 1000 kJ a tick: " + be.stored());
			expect(ctx, be.getStack(0).isEmpty() || !be.getStack(0).isOf(stack("antihydrogen", 1).getItem()), "the antihydrogen was used");
			expect(ctx, w.getBlockState(at).get(Properties.LIT), "it shines while it runs");
			ctx.complete();
		});
	}
}
