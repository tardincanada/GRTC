package com.periodicelements.atlas;

import com.periodicelements.PeriodicElements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Every Radio Transmitter in the Overworld and who set it up (data/periodic_elements_radio.dat). */
public class RadioState extends PersistentState {
	public static final int RANGE = 512;
	private static final Type<RadioState> TYPE = new Type<>(RadioState::new, RadioState::fromNbt, null);

	public final Map<BlockPos, UUID> transmitters = new HashMap<>();

	public static RadioState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, PeriodicElements.MOD_ID + "_radio");
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		transmitters.forEach((pos, owner) -> {
			NbtCompound c = new NbtCompound();
			c.putLong("pos", pos.asLong());
			c.putUuid("owner", owner);
			list.add(c);
		});
		nbt.put("transmitters", list);
		return nbt;
	}

	private static RadioState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		RadioState s = new RadioState();
		for (NbtElement e : nbt.getList("transmitters", NbtElement.COMPOUND_TYPE)) {
			NbtCompound c = (NbtCompound) e;
			s.transmitters.put(BlockPos.fromLong(c.getLong("pos")), c.getUuid("owner"));
		}
		return s;
	}
}
