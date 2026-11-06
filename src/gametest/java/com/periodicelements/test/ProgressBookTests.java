package com.periodicelements.test;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.ProgressBook;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.UUID;

/** The Book of Progress (3.20.0): sets a player's research to exactly one tech level; creative tab only. */
public class ProgressBookTests implements FabricGameTest {
	private static void expect(TestContext ctx, boolean ok, String message) {
		if (!ok) {
			ctx.throwGameTestException(message);
		}
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "progress_book")
	public void setsExactLevel(TestContext ctx) {
		// Mock players share one UUID, and the book replaces all research: use an own UUID so other tests keep theirs.
		var server = ctx.getWorld().getServer();
		UUID player = UUID.randomUUID();
		ResearchState state = ResearchState.get(ctx.getWorld().getServer());
		for (int level = Research.MAX_LEVEL; level >= 0; level--) {
			ProgressBook.setLevel(server, player, level, true);
			Set<String> known = state.known(player);
			expect(ctx, state.techLevel(player) == level, "full level " + level + " gave level "
					+ state.techLevel(player));
			for (Research.Project p : Research.all()) {
				expect(ctx, known.contains(p.id()) == (p.level() <= level), "level " + level + ": wrong state for " + p.id());
			}
		}
		// Going up again with key projects only: the level is reached, no optional project is known.
		ProgressBook.setLevel(server, player, 5, false);
		Set<String> known = state.known(player);
		expect(ctx, state.techLevel(player) == 5, "key projects of level 5 should give level 5");
		for (Research.Project p : Research.all()) {
			expect(ctx, known.contains(p.id()) == (p.key() && p.level() <= 5), "key-only level 5: wrong state for " + p.id());
		}
		// Out-of-range picks are clamped.
		ProgressBook.setLevel(server, player, 99, true);
		expect(ctx, state.techLevel(player) == Research.MAX_LEVEL, "level 99 should clamp to the top level");
		ProgressBook.setLevel(server, player, -3, true);
		expect(ctx, state.known(player).isEmpty(), "negative level should clamp to level 0");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "progress_book")
	public void levelChangesWhatIsAllowed(TestContext ctx) {
		// Mock players share one UUID, and the book replaces all research: use an own UUID so other tests keep theirs.
		var server = ctx.getWorld().getServer();
		UUID player = UUID.randomUUID();
		ResearchState state = ResearchState.get(ctx.getWorld().getServer());
		ProgressBook.setLevel(server, player, 0, true);
		expect(ctx, !Research.allows(state.known(player), "machine:bloomery"), "level 0 should not allow the bloomery");
		ProgressBook.setLevel(server, player, Research.get(Research.unlockedBy("machine:bloomery")).level(), true);
		expect(ctx, Research.allows(state.known(player), "machine:bloomery"), "the bloomery's level should allow it");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void onlyInCreativeTab(TestContext ctx) {
		var server = ctx.getWorld().getServer();
		boolean crafted = server.getRecipeManager().values().stream().anyMatch(r ->
				r.value().getResult(ctx.getWorld().getRegistryManager()).isOf(ProgressBook.BOOK));
		expect(ctx, !crafted, "nothing should craft the Book of Progress");
		ItemGroup workshop = Registries.ITEM_GROUP.get(RegistryKey.of(RegistryKeys.ITEM_GROUP,
				Identifier.of(PeriodicElements.MOD_ID, "creative")));
		workshop.updateEntries(new ItemGroup.DisplayContext(FeatureFlags.VANILLA_FEATURES, true, ctx.getWorld().getRegistryManager()));
		boolean listed = workshop.getDisplayStacks().stream().anyMatch(s -> s.isOf(ProgressBook.BOOK));
		expect(ctx, listed, "the Book of Progress should be in the Creative Testing tab");
		expect(ctx, new ItemStack(ProgressBook.BOOK).getMaxCount() == 1, "the book should not stack");
		ctx.complete();
	}
}
