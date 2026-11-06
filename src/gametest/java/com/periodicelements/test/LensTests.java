package com.periodicelements.test;

import com.periodicelements.BlockComposition;
import com.periodicelements.client.Lens;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.Set;

/** 0.6.1: the Magnifying Lens names only known elements and sums the rest as "other". */
public class LensTests implements FabricGameTest {
	private static double units(Lens.View v, String element) {
		return v.known().stream().filter(l -> l.element().equals(element)).mapToDouble(Lens.Line::units).sum();
	}

	private static void check(TestContext ctx, boolean ok, String msg) {
		if (!ok) {
			ctx.throwGameTestException(msg);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void sphaleriteHidesZincUntilResearched(TestContext ctx) {
		Lens.load();
		BlockComposition.Entry e = BlockComposition.of("periodic_elements:raw_sphalerite");
		Lens.View fresh = Lens.view(e, Set.of());
		check(ctx, units(fresh, "zinc") == 0, "zinc shown before research");
		check(ctx, units(fresh, "sulfur") > 25, "sulfur (Stone Age) should be named: " + fresh);
		check(ctx, fresh.other() > 60, "zinc should be in other: " + fresh);
		double sum = fresh.other() + fresh.known().stream().mapToDouble(Lens.Line::units).sum();
		check(ctx, Math.abs(sum - e.amount()) < 0.5, "parts should add up to the block: " + sum);

		Lens.View learned = Lens.view(e, Set.of("zinc_distillation"));
		check(ctx, units(learned, "zinc") > 55, "zinc should be named after Zinc Distillation: " + learned);
		check(ctx, units(learned, "cadmium") == 0, "cadmium stays hidden: " + learned);
		check(ctx, learned.other() < fresh.other() - 55, "other should shrink: " + learned);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void stoneIsMostlyOtherAtFirst(TestContext ctx) {
		Lens.load();
		Lens.View v = Lens.view(BlockComposition.of("minecraft:stone"), Set.of());
		check(ctx, v.known().isEmpty(), "no stone element is known in the Stone Age: " + v);
		check(ctx, Math.abs(v.other() - 1000) < 1, "all of stone should be other: " + v);
		Lens.View o = Lens.view(BlockComposition.of("minecraft:stone"), Set.of("pneumatic_chemistry", "iron_smelting"));
		check(ctx, units(o, "oxygen") > 400 && units(o, "iron") > 30, "oxygen and iron should be named: " + o);
		ctx.complete();
	}
}
