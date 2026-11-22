package com.periodicelements.test;

import com.periodicelements.creative.CreativeItems;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.space.LifeSupportBlock;
import com.periodicelements.space.LifeSupportBlockEntity;
import com.periodicelements.space.Moon;
import com.periodicelements.space.MoonChunkGenerator;
import com.periodicelements.space.Propellant;
import com.periodicelements.space.Rocket;
import com.periodicelements.space.Space;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Level 9, stage 4 (3.64.0): the Moon, its ground, its sky-less vacuum, the suit, life support, the crew flight, helium-3. */
public class MoonTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static ItemStack stack(String id, int n) {
		return new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", id)), n);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon")
	public void levelNineResearchAndTheMoonWorld(TestContext ctx) {
		expect(ctx, Research.MAX_LEVEL >= 9, "the tech tree should reach level 9");
		for (String id : new String[]{"space_suit_helmet", "space_suit_chestplate", "space_suit_leggings", "space_suit_boots",
				"crew_capsule", "emergency_capsule", "life_support"}) {
			String project = Research.unlockedBy("item:" + id);
			expect(ctx, project != null && Research.get(project).level() == 9, id + " should be level 9 research, is " + project);
		}
		expect(ctx, "lunar_landing".equals(Research.unlockedBy("item:crew_capsule")), "the capsule comes from Lunar Landing");
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("ilmenite_hydrogen_reduction")), "lunar oxygen from ilmenite");
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("regolith_degassing")
				&& r.machines().contains("sintering_furnace")), "helium-3 from regolith in the Sintering Furnace");
		var server = ctx.getWorld().getServer();
		expect(ctx, server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).containsId(Identifier.of("periodic_elements", "moon")),
				"the Moon's dimension type is loaded");
		expect(ctx, !Moon.airless(ctx.getWorld()) && Moon.destination(ctx.getWorld()) == Moon.MOON, "a crew flies to the Moon from the Earth");
		// the test server builds its own flat world without data-pack dimensions; in a normal world the Moon is there
		ServerWorld moon = server.getWorld(Moon.MOON);
		if (moon != null) {
			expect(ctx, moon.getChunkManager().getChunkGenerator() instanceof MoonChunkGenerator, "the Moon has its own generator");
			expect(ctx, Moon.airless(moon) && Moon.destination(moon) == World.OVERWORLD, "the Moon is airless and leads home");
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon")
	public void groundHasMariaHighlandsCratersAndPolarIce(TestContext ctx) {
		MoonChunkGenerator.Terrain t = new MoonChunkGenerator.Terrain(12345L);
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, maria = 0, highland = 0, iceNear = 0, iceFar = 0;
		boolean[] ice = new boolean[1];
		for (int x = 0; x < 2400; x += 8) {
			for (int z = -1600; z <= 1600; z += 16) {
				ice[0] = false;
				int h = t.height(x, z, ice);
				min = Math.min(min, h);
				max = Math.max(max, h);
				double m = t.mare(x, z);
				if (m > 0.9) {
					maria++;
				} else if (m < 0.1) {
					highland++;
				}
				if (ice[0]) {
					if (Math.abs(z) > MoonChunkGenerator.POLE) {
						iceFar++;
					} else {
						iceNear++;
					}
				}
			}
		}
		expect(ctx, min >= 8 && max <= MoonChunkGenerator.HEIGHT - 20, "ground between 8 and 236, found " + min + ".." + max);
		expect(ctx, max - min > 30, "craters and highlands give relief, found " + min + ".." + max);
		expect(ctx, maria > 0 && highland > 0, "both maria (" + maria + ") and highlands (" + highland + ")");
		expect(ctx, iceNear == 0 && iceFar > 0, "ice only near the poles: near " + iceNear + ", far " + iceFar);
		// a column: bedrock, rock, regolith on top; maria are basalt, highlands anorthosite
		expect(ctx, MoonChunkGenerator.block(0, 70, 1, false).isOf(Blocks.BEDROCK), "bedrock at the bottom");
		expect(ctx, MoonChunkGenerator.block(70, 70, 1, false).isOf(Moon.LUNAR_REGOLITH), "regolith on top");
		expect(ctx, MoonChunkGenerator.block(62, 70, 1, false).isOf(Moon.MARE_BASALT), "basalt under a mare");
		expect(ctx, MoonChunkGenerator.block(62, 70, 0, false).isOf(Moon.ANORTHOSITE), "anorthosite under a highland");
		expect(ctx, MoonChunkGenerator.block(70, 70, 0, true).isOf(Moon.LUNAR_ICE), "ice on a polar crater floor");
		expect(ctx, MoonChunkGenerator.block(71, 70, 0, false).isAir(), "vacuum above");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon")
	public void lowGravityAndASuitThatBreathes(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		ZombieEntity z = ctx.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		double g = z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY);
		Moon.gravity(z, true);
		double lunar = z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY);
		expect(ctx, Math.abs(lunar - g / 6) < 1e-6, "a sixth of the pull: " + g + " -> " + lunar);
		expect(ctx, Math.abs(z.getAttributeValue(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER) - 1.0 / 6) < 1e-6, "falls hurt a sixth");
		Moon.gravity(z, false);
		expect(ctx, Math.abs(z.getAttributeValue(EntityAttributes.GENERIC_GRAVITY) - g) < 1e-9, "back to full gravity at home");

		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		p.setAir(p.getMaxAir());
		Moon.breathe(w, p, true);
		expect(ctx, p.getAir() == p.getMaxAir() - Moon.VACUUM_AIR_LOSS, "no suit: the air runs out, has " + p.getAir());
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < 4; i++) {
			p.equipStack(slots[i], new ItemStack(Moon.SPACE_SUIT[i]));
		}
		expect(ctx, Moon.wearingSuit(p), "four pieces make a suit");
		ItemStack chest = p.getEquippedStack(EquipmentSlot.CHEST);
		Moon.setOxygen(chest, 100);
		p.setAir(p.getMaxAir());
		Moon.breathe(w, p, true);
		expect(ctx, p.getAir() == p.getMaxAir() && Moon.oxygen(chest) == 99, "the suit breathes from its backpack, has " + Moon.oxygen(chest));
		Moon.setOxygen(chest, 0);
		Moon.breathe(w, p, true);
		expect(ctx, p.getAir() < p.getMaxAir(), "an empty backpack: the air runs out");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon_base", tickLimit = 200)
	public void lifeSupportFillsASealedRoomAndFindsTheLeak(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos o = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		// a 5 x 4 x 5 glass box: a 3 x 2 x 3 room inside
		for (int dx = 0; dx < 5; dx++) {
			for (int dy = 0; dy < 4; dy++) {
				for (int dz = 0; dz < 5; dz++) {
					boolean wall = dx == 0 || dx == 4 || dy == 0 || dy == 3 || dz == 0 || dz == 4;
					w.setBlockState(o.add(dx, dy, dz), wall ? Blocks.GLASS.getDefaultState() : Blocks.AIR.getDefaultState());
				}
			}
		}
		BlockPos ls = o.add(2, 1, 2);
		w.setBlockState(ls, Moon.LIFE_SUPPORT.getDefaultState());
		LifeSupportBlockEntity be = (LifeSupportBlockEntity) w.getBlockEntity(ls);
		expect(ctx, be.addCylinders(20) == LifeSupportBlockEntity.MAX_CYLINDERS, "it holds 16 cylinders");
		ctx.runAtTick(60, () -> {
			expect(ctx, w.getBlockState(ls).get(LifeSupportBlock.LIT), "a sealed room lights the lamp");
			expect(ctx, be.volume() == 3 * 2 * 3 - 1, "17 blocks of air, found " + be.volume());
			expect(ctx, LifeSupportBlockEntity.pressurized(w, o.add(1, 2, 1)), "the room holds air");
			expect(ctx, !LifeSupportBlockEntity.pressurized(w, o.add(-2, 2, 1)), "outside does not");
			w.setBlockState(o.add(4, 1, 2), Blocks.AIR.getDefaultState());   // a hole in the wall
		});
		ctx.runAtTick(140, () -> {
			expect(ctx, !w.getBlockState(ls).get(LifeSupportBlock.LIT), "a leak puts the lamp out");
			expect(ctx, !LifeSupportBlockEntity.pressurized(w, o.add(1, 2, 1)), "the air escapes");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon")
	public void aCrewNeedsMoreDeltaVThanASatellite(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		SpaceTests.build(w, pad, new ItemStack(Moon.CREW_CAPSULE), false, Space.KEROLOX_ENGINE, 4, Space.KEROLOX_ENGINE, 2);
		Rocket small = Rocket.read(w, pad);
		expect(ctx, small.crew && small.destination == Moon.MOON && small.targetDv == Propellant.MOON_DV, "a crew flight goes to the Moon");
		expect(ctx, small.totalDv > Propellant.ORBIT_DV && !small.ready(), "an orbital rocket falls short of the Moon: " + small.totalDv);
		SpaceTests.build(w, pad, new ItemStack(Moon.CREW_CAPSULE), false, Space.KEROLOX_ENGINE, 6, Space.HYDROLOX_ENGINE, 6,
				Space.HYDROLOX_ENGINE, 3);
		Rocket big = Rocket.read(w, pad);
		expect(ctx, big.ready(), "three stages reach the Moon: " + big.totalDv + " " + big.problems);
		expect(ctx, Propellant.RETURN_DV < Propellant.ORBIT_DV, "coming home from the Moon takes less than reaching orbit");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon_fusion", tickLimit = 300)
	public void tokamakBurnsHeliumThreeWithoutLithium(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos c = ctx.getAbsolutePos(new BlockPos(4, 3, 4));
		BlockPos t = c.west(3);
		FusionTests.ring(w, c, true);
		w.setBlockState(t, Registries.BLOCK.get(Identifier.of("periodic_elements", "tokamak")).getDefaultState());
		w.setBlockState(t.west(), CreativeItems.GENERATOR.getDefaultState());
		PowerBlockEntity be = (PowerBlockEntity) w.getBlockEntity(t);
		be.setStack(0, stack("deuterium", 2));
		be.setStack(1, stack("helium_3", 1));
		be.setStack(3, stack("helium", 1));
		ctx.runAtTick(160, () -> {
			expect(ctx, be.plasmaLeft() > 0 && be.burningHelium3(), "deuterium and helium-3 burn");
			int left = 0;
			for (int i = 0; i < be.size(); i++) {
				left += be.getStack(i).isOf(stack("helium_3", 1).getItem()) ? be.getStack(i).getCount() : 0;
			}
			expect(ctx, left == 0, "the helium-3 is used, " + left + " left");
			ctx.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "moon")
	public void lavaTubesKreepAndShelter(TestContext ctx) {
		MoonChunkGenerator.Terrain t = new MoonChunkGenerator.Terrain(12345L);
		int tubes = 0, skylights = 0, highlandTubes = 0, kreep = 0;
		for (int x = -3000; x < 3000; x += 6) {
			for (int z = -3000; z < 3000; z += 24) {
				int top = t.height(x, z, null);
				int[] tube = t.tube(x, z, top);
				if (tube != null) {
					tubes++;
					if (tube[1] == top) {
						skylights++;
					} else if (tube[1] > top - 4) {
						ctx.throwGameTestException("a closed tube keeps at least 3 blocks of roof, at " + x + " " + z);
					}
					if (t.mare(x, z) < 0.6) {
						highlandTubes++;
					}
				}
				if (t.kreep(x, z)) {
					kreep++;
				}
			}
		}
		expect(ctx, tubes > 0 && skylights > 0 && highlandTubes == 0, "tubes under maria only, some open: " + tubes + " / " + skylights);
		expect(ctx, kreep > 0, "some maria are KREEP basalt");
		expect(ctx, MoonChunkGenerator.block(60, 70, 1, false, true, null).isOf(Moon.KREEP_BASALT), "KREEP basalt replaces mare basalt");
		expect(ctx, MoonChunkGenerator.block(52, 70, 1, false, false, new int[]{50, 55}).isAir(), "the tube is hollow");
		expect(ctx, com.periodicelements.radiation.Radioactivity.of(Moon.KREEP_BASALT) > 0, "KREEP basalt is radioactive (thorium)");
		// shelter: each opaque block overhead halves cosmic rays; none reach the Earth
		ServerWorld w = ctx.getWorld();
		BlockPos head = ctx.getAbsolutePos(new BlockPos(2, 2, 2));
		int before = com.periodicelements.space.Vacuum.cover(w, head);
		for (int i = 1; i <= 3; i++) {
			w.setBlockState(head.up(i), Blocks.STONE.getDefaultState());
		}
		w.setBlockState(head.up(4), Blocks.GLASS.getDefaultState());
		expect(ctx, com.periodicelements.space.Vacuum.cover(w, head) == before + 3, "three stone blocks count, glass does not");
		expect(ctx, com.periodicelements.space.Vacuum.hasAir(w, head), "the Earth has air everywhere");
		ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
		expect(ctx, com.periodicelements.space.Vacuum.cosmic(p) == 0, "no cosmic dose under the Earth's air");
		// the Atlas keeps one map per world
		var server = w.getServer();
		var home = com.periodicelements.atlas.AtlasData.of(server, p.getUuid());
		var moon = com.periodicelements.atlas.AtlasData.of(server, p.getUuid(), Moon.MOON);
		expect(ctx, home != moon && home == com.periodicelements.atlas.AtlasData.of(server, p.getUuid(), World.OVERWORLD),
				"the Moon has its own Atlas, the Earth keeps the old one");
		ctx.complete();
	}
}
