package com.periodicelements.compendium;

import com.periodicelements.PeriodicElements;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server to client: the Geological formation under the player for the Mineral Compendium's corner panel ("" for none). */
public record FormationPayload(String formation) implements CustomPayload {
	public static final Id<FormationPayload> ID = new Id<>(Identifier.of(PeriodicElements.MOD_ID, "compendium_formation"));
	public static final PacketCodec<RegistryByteBuf, FormationPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.STRING, FormationPayload::formation, FormationPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
