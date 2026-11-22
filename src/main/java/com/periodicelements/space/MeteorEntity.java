package com.periodicelements.space;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A meteorite on its way down (3.62.0): it flies in a straight, steep line from high in the sky to its target column
 * in {@link #FLIGHT} ticks, trailing fire and smoke that players see from far away, then makes its crater
 * ({@link Meteorites#impact}). It is drawn as the meteorite block, glowing.
 */
public class MeteorEntity extends Entity {
	/** Ticks from the top of the sky to the ground. */
	public static final int FLIGHT = 80;
	/** How high above the target and how far back along its path it starts. */
	public static final double HEIGHT = 220, BACK = 70;

	private static final TrackedData<Integer> BLOCK = DataTracker.registerData(MeteorEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private Vec3d start = Vec3d.ZERO;
	private Vec3d end = Vec3d.ZERO;
	private int flight;
	/** True for a fall the night sky scheduled; false for one the Meteor Caller called. */
	private boolean scheduled;

	public MeteorEntity(EntityType<? extends MeteorEntity> type, World world) {
		super(type, world);
		noClip = true;
		setNoGravity(true);
	}

	/**
	 * Sends a {@code kind} meteorite to column ({@code x}, {@code z}), arriving at height {@code groundY}. It comes in
	 * from the direction ({@code dirX}, {@code dirZ}), a unit vector pointing the way it flies.
	 */
	public static MeteorEntity send(ServerWorld world, int x, int groundY, int z, Block kind, double dirX, double dirZ, boolean scheduled) {
		MeteorEntity e = new MeteorEntity(Meteorites.METEOR, world);
		e.end = new Vec3d(x + 0.5, groundY + 0.5, z + 0.5);
		e.start = e.end.add(-dirX * BACK, HEIGHT, -dirZ * BACK);
		e.scheduled = scheduled;
		e.dataTracker.set(BLOCK, Block.getRawIdFromState(kind.getDefaultState()));
		e.setPosition(e.start);
		world.spawnEntity(e);
		world.playSound(null, e.end.x, e.end.y + 40, e.end.z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 6f, 1.6f);
		return e;
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		builder.add(BLOCK, 0);
	}

	public BlockState block() {
		return Block.getStateFromRawId(dataTracker.get(BLOCK));
	}

	public BlockPos target() {
		return BlockPos.ofFloored(end);
	}

	@Override
	public boolean shouldRender(double distance) {
		return distance < 1024 * 1024;
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(getWorld() instanceof ServerWorld world)) {
			// the client only moves it along the velocity the server sends
			setPosition(getPos().add(getVelocity()));
			return;
		}
		flight++;
		double t = Math.min(1.0, flight / (double) FLIGHT);
		Vec3d next = start.lerp(end, t);
		setVelocity(next.subtract(getPos()));
		velocityDirty = true;
		setPosition(next);
		trail(world, next);
		if (flight >= FLIGHT) {
			Meteorites.impact(world, target().getX(), target().getZ(),
					block().getBlock(), world.getRandom(), scheduled);
			discard();
		}
	}

	private void trail(ServerWorld world, Vec3d at) {
		puff(world, ParticleTypes.FLAME, at, 10, 0.35, 0.02);
		puff(world, ParticleTypes.LAVA, at, 2, 0.3, 0.0);
		Vec3d back = at.add(start.subtract(end).normalize().multiply(2.5));
		puff(world, ParticleTypes.LARGE_SMOKE, back, 6, 0.6, 0.01);
		puff(world, ParticleTypes.CAMPFIRE_COSY_SMOKE, back.add(start.subtract(end).normalize().multiply(3)), 2, 0.4, 0.0);
	}

	static void puff(ServerWorld world, ParticleEffect p, Vec3d at, int count, double spread, double speed) {
		for (ServerPlayerEntity viewer : world.getPlayers()) {
			if (viewer.getPos().squaredDistanceTo(at) < 1024 * 1024) {
				world.spawnParticles(viewer, p, true, at.x, at.y, at.z, count, spread, spread, spread, speed);
			}
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		dataTracker.set(BLOCK, nbt.getInt("block"));
		start = new Vec3d(nbt.getDouble("sx"), nbt.getDouble("sy"), nbt.getDouble("sz"));
		end = new Vec3d(nbt.getDouble("ex"), nbt.getDouble("ey"), nbt.getDouble("ez"));
		flight = nbt.getInt("age");
		scheduled = nbt.getBoolean("scheduled");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putInt("block", dataTracker.get(BLOCK));
		nbt.putDouble("sx", start.x);
		nbt.putDouble("sy", start.y);
		nbt.putDouble("sz", start.z);
		nbt.putDouble("ex", end.x);
		nbt.putDouble("ey", end.y);
		nbt.putDouble("ez", end.z);
		nbt.putInt("age", flight);
		nbt.putBoolean("scheduled", scheduled);
	}
}
