package com.periodicelements.compendium;

import com.periodicelements.PeriodicElements;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

/** Server to client: all of the player's compendium notes and where each was first made. */
public record CompendiumSyncPayload(List<String> keys, List<Long> positions) implements CustomPayload {
	public static final Id<CompendiumSyncPayload> ID = new Id<>(Identifier.of(PeriodicElements.MOD_ID, "compendium_sync"));
	public static final PacketCodec<RegistryByteBuf, CompendiumSyncPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.STRING.collect(PacketCodecs.toList()), CompendiumSyncPayload::keys,
			PacketCodecs.VAR_LONG.collect(PacketCodecs.toList()), CompendiumSyncPayload::positions,
			CompendiumSyncPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
