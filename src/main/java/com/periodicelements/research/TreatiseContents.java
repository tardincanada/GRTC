package com.periodicelements.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;

/** What a treatise holds: who wrote it, their tech level at the time, and every project they knew. */
public record TreatiseContents(String author, int level, List<String> projects) {
	public static final Codec<TreatiseContents> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("author").forGetter(TreatiseContents::author),
			Codec.INT.fieldOf("level").forGetter(TreatiseContents::level),
			Codec.STRING.listOf().fieldOf("projects").forGetter(TreatiseContents::projects)
	).apply(i, TreatiseContents::new));

	public static final PacketCodec<RegistryByteBuf, TreatiseContents> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.STRING, TreatiseContents::author,
			PacketCodecs.VAR_INT, TreatiseContents::level,
			PacketCodecs.STRING.collect(PacketCodecs.toList()), TreatiseContents::projects,
			TreatiseContents::new);

	public TreatiseContents {
		projects = List.copyOf(projects);
	}
}
