package com.periodicelements.research;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

/** What a player knows, on either side: the saved state on the server, the synced copy on the client. */
public final class ResearchAccess {
	private ResearchAccess() {
	}

	public static Set<String> known(PlayerEntity player) {
		if (player instanceof ServerPlayerEntity sp) {
			return ResearchState.get(sp.getServer()).known(sp.getUuid());
		}
		if (player != null && player.getWorld().isClient) {
			return ClientResearch.known();
		}
		return Set.of();
	}
}
