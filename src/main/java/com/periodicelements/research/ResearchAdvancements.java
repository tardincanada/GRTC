package com.periodicelements.research;

import com.periodicelements.PeriodicElements;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Keeps the "Elementar" advancement tab (3.60.0) in step with a player's research: one advancement per project,
 * data/periodic_elements/advancement/elementar/research/&lt;id&gt;.json (written by tools/advancements.py), whose only
 * criterion "done" is granted when the player knows the project and revoked when they do not (/research revoke,
 * Book of Progress). Called from {@link ResearchState#sync}, which runs on join and after every research change.
 */
public final class ResearchAdvancements {
	private static final String CRITERION = "done";

	private ResearchAdvancements() {
	}

	public static Identifier id(String project) {
		return Identifier.of(PeriodicElements.MOD_ID, "elementar/research/" + project);
	}

	public static void sync(ServerPlayerEntity player, Set<String> known) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		PlayerAdvancementTracker tracker = player.getAdvancementTracker();
		for (Research.Project p : Research.all()) {
			AdvancementEntry entry = server.getAdvancementLoader().get(id(p.id()));
			if (entry == null) {
				continue;
			}
			AdvancementProgress progress = tracker.getProgress(entry);
			boolean has = progress.isDone();
			if (known.contains(p.id()) && !has) {
				tracker.grantCriterion(entry, CRITERION);
			} else if (!known.contains(p.id()) && has) {
				tracker.revokeCriterion(entry, CRITERION);
			}
		}
	}
}
