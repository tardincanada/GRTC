package com.periodicelements.atlas;

import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/** Draws a station's power each tick from the wire network and notes in {@link StationState} whether it ran. */
public class StationBlockEntity extends BlockEntity {
	private UUID owner;
	private boolean powered;

	public StationBlockEntity(BlockPos pos, BlockState state) {
		super(Atlas.STATION_ENTITY, pos, state);
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		markDirty();
	}

	public boolean powered() {
		return powered;
	}

	public void tick(ServerWorld world, BlockPos pos) {
		if (!(getCachedState().getBlock() instanceof StationBlock block)) return;
		double want = block.kind().rate, got = 0;
		if (world.getTime() % 20 == 0 || powered) {
			for (PowerBlockEntity pile : MachineBlockEntity.powerSources(world, pos)) {
				got += pile.draw(want - got);
				if (got >= want - 1e-9) break;
			}
		}
		boolean now = got >= want - 1e-6;
		StationState st = StationState.get(world.getServer());
		StationState.Station s = st.stations.get(pos);
		if (s == null && owner != null && world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
			s = st.add(pos, block.kind(), owner);
		}
		if (s != null && s.powered != now) {
			s.powered = now;
			st.markDirty();
		}
		if (now != powered) {
			powered = now;
			markDirty();
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		owner = nbt.containsUuid("owner") ? nbt.getUuid("owner") : null;
		powered = nbt.getBoolean("powered");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (owner != null) nbt.putUuid("owner", owner);
		nbt.putBoolean("powered", powered);
	}
}
