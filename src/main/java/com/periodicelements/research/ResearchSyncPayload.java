package com.periodicelements.research;

import com.periodicelements.PeriodicElements;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

/** Server to client: the full list of research projects the player has finished. */
public record ResearchSyncPayload(List<String> known) implements CustomPayload {
	public static final Id<ResearchSyncPayload> ID = new Id<>(Identifier.of(PeriodicElements.MOD_ID, "research_sync"));
	public static final PacketCodec<RegistryByteBuf, ResearchSyncPayload> CODEC =
			PacketCodecs.STRING.collect(PacketCodecs.toList()).<RegistryByteBuf>cast()
					.xmap(ResearchSyncPayload::new, ResearchSyncPayload::known);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
