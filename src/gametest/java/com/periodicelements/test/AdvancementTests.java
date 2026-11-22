package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAdvancements;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/** The Elementar advancement tab (3.60.0): one advancement per research project, kept in step with research. */
public class AdvancementTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	private static AdvancementEntry get(TestContext ctx, String path) {
		return ctx.getWorld().getServer().getAdvancementLoader().get(Identifier.of(PeriodicElements.MOD_ID, path));
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void everyProjectHasAnAdvancement(TestContext ctx) {
		AdvancementEntry root = get(ctx, "elementar/root");
		expect(ctx, root != null && root.value().parent().isEmpty(), "the Elementar tab needs a root");
		expect(ctx, root.value().rewards().loot().stream().anyMatch(k ->
				k.getValue().equals(Identifier.of(PeriodicElements.MOD_ID, "advancement/starter_guide"))),
				"the root should hand out the starter Field Guide");
		expect(ctx, get(ctx, "elementar/station/potters_workshop") != null, "the Workshop step is missing");
		for (Research.Project p : Research.all()) {
			AdvancementEntry e = get(ctx, "elementar/research/" + p.id());
			expect(ctx, e != null, "no advancement for " + p.id());
			expect(ctx, e.value().display().isPresent() && e.value().parent().isPresent(), p.id() + " is not in the tab");
			// Every advancement climbs back to the root.
			AdvancementEntry cur = e;
			int steps = 0;
			while (cur.value().parent().isPresent() && steps++ < 400) {
				cur = ctx.getWorld().getServer().getAdvancementLoader().get(cur.value().parent().get());
				expect(ctx, cur != null, p.id() + " has a missing ancestor");
			}
			expect(ctx, cur.id().equals(root.id()), p.id() + " does not lead back to the Elementar root");
		}
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void followsResearch(TestContext ctx) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		Set<String> known = new HashSet<>(Set.of("fired_pottery", "copper_smelting"));
		ResearchAdvancements.sync(player, known);
		var tracker = player.getAdvancementTracker();
		expect(ctx, tracker.getProgress(get(ctx, "elementar/research/fired_pottery")).isDone(), "Fired Pottery not granted");
		expect(ctx, tracker.getProgress(get(ctx, "elementar/research/copper_smelting")).isDone(), "Copper Smelting not granted");
		expect(ctx, !tracker.getProgress(get(ctx, "elementar/research/lime_burning")).isDone(), "Lime Burning granted early");
		known.remove("copper_smelting");
		ResearchAdvancements.sync(player, known);
		expect(ctx, !tracker.getProgress(get(ctx, "elementar/research/copper_smelting")).isDone(),
				"a revoked project should lose its advancement");
		expect(ctx, tracker.getProgress(get(ctx, "elementar/research/fired_pottery")).isDone(), "Fired Pottery was lost");
		ResearchAdvancements.sync(player, Set.of());
		ctx.complete();
	}
}
