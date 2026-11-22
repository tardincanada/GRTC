package com.periodicelements.test;

import com.periodicelements.creative.CreativeItems;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.research.Research;
import com.periodicelements.space.Fusion;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Level 8, stage 3 (3.63.0): the Tokamak, its 5 x 5 x 3 ring and its burn. */
public class FusionTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static Block tokamak() {
		return Registries.BLOCK.get(Identifier.of("periodic_elements", "tokamak"));
	}

	private static ItemStack stack(String id, int n) {
		return new ItemStack(Registries.ITEM.get(Identifier.of("periodic_elements", id)), n);
	}

	/** Builds the ring round {@code c} (all of it, or without the coil at +2, 0, +2). */
	static void ring(ServerWorld w, BlockPos c, boolean whole) {
		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					boolean gap = !whole && dx == 2 && dy == 0 && dz == 2;
					w.setBlockState(c.add(dx, dy, dz), gap ? Blocks.AIR.getDefaultState() : Fusion.expected(dx, dy, dz).getDefaultState());
				}
			}
		}
	}

	private static int count(PowerBlockEntity be, String id) {
		int n = 0;
		for (int i = 0; i < be.size(); i++) {
			if (Registries.ITEM.getId(be.getStack(i).getItem()).getPath().equals(id)) {
				n += be.getStack(i).getCount();
			}
		}
		return n;
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fusion")
	public void tokamakIsLevelEightResearchWithItsRing(TestContext ctx) {
		var spec = Reactions.power("tokamak");
		expect(ctx, spec != null && spec.kind().equals("fusion") && spec.num("gain") > 30, "the Tokamak is a fusion power block stronger than fission");
		var project = Research.get(Research.unlockedBy("machine:tokamak"));
		expect(ctx, project != null && project.level() == 8, "the Tokamak is level 8 research");
		for (String part : new String[]{"toroidal_field_coil", "central_solenoid", "vacuum_vessel", "lithium_blanket"}) {
			expect(ctx, Research.unlockedBy("item:" + part) != null, part + " is unlocked by research");
		}
		expect(ctx, Reactions.all().stream().anyMatch(r -> r.id().equals("heavy_water_electrolysis")
				&& r.machines().contains("electrolysis_cell")), "deuterium comes from heavy water in the Electrolysis Cell");
		// the ring: a solenoid column, vessels and blankets round it, coils outside
		expect(ctx, Fusion.expected(0, 1, 0) == Fusion.CENTRAL_SOLENOID && Fusion.expected(1, 0, -1) == Fusion.VACUUM_VESSEL
				&& Fusion.expected(-1, 1, 0) == Fusion.LITHIUM_BLANKET && Fusion.expected(2, -1, -2) == Fusion.TOROIDAL_FIELD_COIL, "ring layout");
		ServerWorld w = ctx.getWorld();
		BlockPos c = ctx.getAbsolutePos(new BlockPos(4, 3, 4));
		BlockPos t = c.west(3);
		w.setBlockState(t, tokamak().getDefaultState());
		expect(ctx, Fusion.check(w, t).centre() == null, "no ring yet");
		ring(w, c, false);
		var broken = Fusion.check(w, t);
		expect(ctx, c.equals(broken.centre()) && !broken.valid() && c.add(2, 0, 2).equals(broken.missingAt())
				&& broken.missing() == Fusion.TOROIDAL_FIELD_COIL, "the check names the missing coil, found " + broken);
		ring(w, c, true);
		var whole = Fusion.check(w, t);
		expect(ctx, whole.valid() && whole.vessels().size() == 8, "a whole ring with 8 vessels");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "fusion_burn", tickLimit = 300)
	public void tokamakLightsBurnsBreedsAndQuenches(TestContext ctx) {
		ServerWorld w = ctx.getWorld();
		BlockPos c = ctx.getAbsolutePos(new BlockPos(4, 3, 4));
		BlockPos t = c.west(3);
		ring(w, c, true);
		w.setBlockState(t, tokamak().getDefaultState());
		w.setBlockState(t.west(), CreativeItems.GENERATOR.getDefaultState());
		PowerBlockEntity be = (PowerBlockEntity) w.getBlockEntity(t);
		be.setStack(0, stack("deuterium", 2));
		be.setStack(1, stack("tritium_vial", 1));
		be.setStack(2, stack("lithium", 1));
		be.setStack(3, stack("helium", 1));
		ctx.runAtTick(40, () -> expect(ctx, be.plasmaLeft() == 0 && be.ignition() > 0, "still gathering current to light the plasma, has "
				+ be.ignition()));
		ctx.runAtTick(160, () -> {
			expect(ctx, be.plasmaLeft() > 0, "the plasma should be burning after 20,000 kJ at 200 a tick");
			expect(ctx, be.stored() > 0, "it makes current");
			expect(ctx, count(be, "deuterium") == 1 && count(be, "helium") == 0, "a burn uses one deuterium and the helium flask");
			expect(ctx, count(be, "tritium_vial") == 1 && count(be, "lithium") == 0, "the blanket bred the lithium into a new tritium vial");
			expect(ctx, w.getBlockState(c.add(1, 0, 0)).get(Fusion.VacuumVesselBlock.LIT), "the vessels glow");
			w.setBlockState(c.add(2, 1, 2), Blocks.AIR.getDefaultState());
		});
		ctx.runAtTick(200, () -> {
			expect(ctx, be.plasmaLeft() == 0, "a broken ring puts the plasma out");
			expect(ctx, !w.getBlockState(c.add(1, 0, 0)).get(Fusion.VacuumVesselBlock.LIT), "the vessels go dark");
			ctx.complete();
		});
	}
}
