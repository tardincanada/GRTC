package com.periodicelements.atlas;

import com.periodicelements.PeriodicElements;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

/** The Atlas's packets: the client asks for tiles it lacks, the server sends tiles and markers, the client edits markers. */
public final class AtlasPayloads {
	private AtlasPayloads() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** Client to server: these tiles, and the revision the client already has of each (-1 = none). */
	public record Request(List<Long> keys, List<Integer> revs, int overlayRev) implements CustomPayload {
		public static final Id<Request> ID = new Id<>(id("atlas_request"));
		public static final PacketCodec<RegistryByteBuf, Request> CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_LONG.collect(PacketCodecs.toList()), Request::keys,
				PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), Request::revs,
				PacketCodecs.VAR_INT, Request::overlayRev, Request::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** Server to client: one tile, whole. */
	public record Tile(long key, int rev, byte[] color, byte[] height, byte[] rock) implements CustomPayload {
		public static final Id<Tile> ID = new Id<>(id("atlas_tile"));
		public static final PacketCodec<RegistryByteBuf, Tile> CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_LONG, Tile::key, PacketCodecs.VAR_INT, Tile::rev, PacketCodecs.BYTE_ARRAY, Tile::color,
				PacketCodecs.BYTE_ARRAY, Tile::height, PacketCodecs.BYTE_ARRAY, Tile::rock, Tile::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static final PacketCodec<RegistryByteBuf, AtlasData.Marker> MARKER = PacketCodec.tuple(
			PacketCodecs.STRING, AtlasData.Marker::name, PacketCodecs.VAR_INT, AtlasData.Marker::x,
			PacketCodecs.VAR_INT, AtlasData.Marker::z, AtlasData.Marker::new);

	/** Server to client: the player's markers, how many they may have, and markers shared by radio (name = owner). */
	public record Markers(List<AtlasData.Marker> markers, int limit, List<AtlasData.Marker> shared) implements CustomPayload {
		public static final Id<Markers> ID = new Id<>(id("atlas_markers"));
		public static final PacketCodec<RegistryByteBuf, Markers> CODEC = PacketCodec.tuple(
				MARKER.collect(PacketCodecs.toList()), Markers::markers, PacketCodecs.VAR_INT, Markers::limit,
				MARKER.collect(PacketCodecs.toList()), Markers::shared, Markers::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** One chunk of the overlay layers; ores as "block id*count"; isotopes as source element -> Sv/h (stage 3). */
	public record ChunkEntry(long key, float rad, int magnetic, int dense, List<String> ores, boolean cored,
							 java.util.Map<String, Float> isotopes) {
		private static final PacketCodec<RegistryByteBuf, java.util.Map<String, Float>> ISOTOPES =
				PacketCodecs.map(java.util.HashMap::new, PacketCodecs.STRING, PacketCodecs.FLOAT);
		public static final PacketCodec<RegistryByteBuf, ChunkEntry> CODEC = PacketCodec.of((e, buf) -> {
			buf.writeVarLong(e.key());
			buf.writeFloat(e.rad());
			buf.writeInt(e.magnetic());
			buf.writeInt(e.dense());
			PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, e.ores());
			buf.writeBoolean(e.cored());
			ISOTOPES.encode(buf, e.isotopes());
		}, buf -> new ChunkEntry(buf.readVarLong(), buf.readFloat(), buf.readInt(), buf.readInt(),
				PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf), buf.readBoolean(), ISOTOPES.decode(buf)));
	}

	/** One radar contact: where, and what (0 = player, 1 = hostile, 2 = other creature). */
	public record Contact(int x, int z, int kind) {
		public static final PacketCodec<RegistryByteBuf, Contact> CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_INT, Contact::x, PacketCodecs.VAR_INT, Contact::z, PacketCodecs.VAR_INT, Contact::kind, Contact::new);
	}

	/** Server to client, every half second while the player has a radar: what it sees now. */
	public record Contacts(List<Contact> contacts) implements CustomPayload {
		public static final Id<Contacts> ID = new Id<>(id("atlas_contacts"));
		public static final PacketCodec<RegistryByteBuf, Contacts> CODEC = PacketCodec.tuple(
				Contact.CODEC.collect(PacketCodecs.toList()), Contacts::contacts, Contacts::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** Server to client: the overlay of every chunk in the requested tiles, at revision {@code rev}. */
	public record Overlay(int rev, List<ChunkEntry> chunks) implements CustomPayload {
		public static final Id<Overlay> ID = new Id<>(id("atlas_overlay"));
		public static final PacketCodec<RegistryByteBuf, Overlay> CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_INT, Overlay::rev, ChunkEntry.CODEC.collect(PacketCodecs.toList()), Overlay::chunks, Overlay::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** Client to server: add a marker, or (add false) remove the one nearest x, z. */
	public record EditMarker(boolean add, String name, int x, int z) implements CustomPayload {
		public static final Id<EditMarker> ID = new Id<>(id("atlas_marker"));
		public static final PacketCodec<RegistryByteBuf, EditMarker> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, EditMarker::add, PacketCodecs.STRING, EditMarker::name, PacketCodecs.VAR_INT, EditMarker::x,
				PacketCodecs.VAR_INT, EditMarker::z, EditMarker::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
