package com.periodicelements.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * 3.38.0: the Tank Gauge's reading, in tenths of a percent (-1: no tank to read), sent to the client for
 * {@code TankGaugeRenderer} whenever it changes.
 */
public class TankGaugeBlockEntity extends BlockEntity {
	public static final int NONE = -1;
	private int permille = NONE;
	private int timer;

	public TankGaugeBlockEntity(BlockPos pos, BlockState state) {
		super(FluidContent.GAUGE_ENTITY, pos, state);
	}

	/** Tenths of a percent in use, or {@link #NONE}. */
	public int permille() {
		return permille;
	}

	/** What this gauge reads: its tank's controller, or a Storage Tank behind its face. */
	@Nullable
	public FluidHolder holder() {
		if (world == null) {
			return null;
		}
		TankControllerBlockEntity c = TankBlocks.controller(world, pos);
		if (c != null && c.valid()) {
			return c;
		}
		var behind = pos.offset(getCachedState().get(TankBlocks.Gauge.FACING).getOpposite());
		return world.getBlockEntity(behind) instanceof com.periodicelements.machine.StorageTankBlockEntity t ? t : null;
	}

	/** Reads the tank now (tests; otherwise twice a second). */
	public void update() {
		FluidHolder h = holder();
		int now = h == null ? NONE : (int) Math.round(1000 * h.fraction());
		if (now != permille) {
			permille = now;
			markDirty();
			if (world != null) {
				world.updateListeners(pos, getCachedState(), getCachedState(), 3);
			}
		}
	}

	public void tick() {
		if (timer-- <= 0) {
			timer = 10;
			update();
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		nbt.putInt("Permille", permille);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		permille = nbt.contains("Permille") ? nbt.getInt("Permille") : NONE;
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
		return createNbt(lookup);
	}
}
