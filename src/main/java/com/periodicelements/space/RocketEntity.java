package com.periodicelements.space;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A rocket in flight, drawn as the blocks it was built from. ASCENT climbs and drops each spent stage on the way
 * (3.61.0); the last stage puts the payload in orbit. A dropped stage either FALLS downrange and is gone, or, with
 * Landing Legs, RETURNS: it coasts, turns, and burns its reserve to set down on the pad, where it becomes blocks again.
 */
public class RocketEntity extends Entity {
	public static final int ASCENT = 0, FALLING = 1, RETURNING = 2;
	/** Ticks each lower stage burns before it is dropped. */
	public static final int STAGE_BURN = 90;
	/** Chance that a stage which flew back comes home with a worn engine. */
	public static final double WEAR_CHANCE = 0.1;

	private static final TrackedData<NbtCompound> BLOCKS = DataTracker.registerData(RocketEntity.class,
			TrackedDataHandlerRegistry.NBT_COMPOUND);
	private static final TrackedData<Integer> MODE = DataTracker.registerData(RocketEntity.class,
			TrackedDataHandlerRegistry.INTEGER);

	/** Server side: where each stage starts in the block list (index 0 is always 0), bottom up. */
	private int[] stageStarts = {0};
	private UUID owner;
	/** 3.64.0: the player riding in the crew capsule on top, or null. */
	private UUID crew;
	private ItemStack payload = ItemStack.EMPTY;
	private BlockPos pad = BlockPos.ORIGIN;
	private int stageTicks;
	private int flightTicks;
	private int dropped;
	private List<BlockState> cache;
	private NbtCompound cacheOf;

	public RocketEntity(EntityType<? extends RocketEntity> type, World world) {
		super(type, world);
		noClip = true;
		setNoGravity(true);
	}

	/** A rocket leaving {@code pad} with {@code blocks} (bottom up) split into stages at {@code stageStarts}. */
	public static RocketEntity launch(ServerWorld world, BlockPos pad, List<BlockState> blocks, int[] stageStarts,
									  UUID owner, ItemStack payload) {
		RocketEntity e = new RocketEntity(Space.ROCKET, world);
		e.setBlocks(blocks);
		e.stageStarts = stageStarts.clone();
		e.owner = owner;
		e.payload = payload.copy();
		e.pad = pad.toImmutable();
		e.setPosition(pad.getX() + 0.5, pad.getY() + 1, pad.getZ() + 0.5);
		e.setVelocity(0, 0.02, 0);
		world.spawnEntity(e);
		return e;
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		builder.add(BLOCKS, new NbtCompound());
		builder.add(MODE, ASCENT);
	}

	public int mode() {
		return dataTracker.get(MODE);
	}

	private void setBlocks(List<BlockState> states) {
		NbtCompound nbt = new NbtCompound();
		nbt.putIntArray("b", states.stream().mapToInt(Block::getRawIdFromState).toArray());
		dataTracker.set(BLOCKS, nbt);
	}

	/** The blocks, bottom up (both sides). */
	public List<BlockState> blocks() {
		NbtCompound nbt = dataTracker.get(BLOCKS);
		if (cache == null || cacheOf != nbt) {
			cache = Arrays.stream(nbt.getIntArray("b")).mapToObj(Block::getStateFromRawId).toList();
			cacheOf = nbt;
		}
		return cache;
	}

	@Override
	public boolean shouldRender(double distance) {
		return distance < 640 * 640;
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
		if (getWorld().isClient) {
			setPosition(getPos().add(getVelocity()));
			return;
		}
		ServerWorld world = (ServerWorld) getWorld();
		flightTicks++;
		switch (mode()) {
			case ASCENT -> ascend(world);
			case FALLING -> fall(world);
			default -> fly_back(world);
		}
		velocityDirty = true;
	}

	private void ascend(ServerWorld world) {
		stageTicks++;
		boolean upper = stageStarts.length == 1 && flightTicks > STAGE_BURN;
		double vy = getVelocity().y;
		vy = Math.min(upper ? 4.0 : 2.5, vy + (upper ? 0.03 : flightTicks < 40 ? 0.006 : 0.02));
		setVelocity(0, vy, 0);
		setPosition(getPos().add(0, vy, 0));
		exhaust(world, flightTicks < 60 ? 1.6 : 1.0);
		if (flightTicks == 1) {
			world.playSound(null, pad, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 4f, 0.5f);
		}
		if (flightTicks % 16 == 1) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.BLOCKS, 6f, 0.3f);
			world.playSound(null, getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 4f, 0.4f);
		}
		if (stageStarts.length > 1 && stageTicks >= STAGE_BURN) {
			separate(world);
		} else if (stageStarts.length == 1 && getY() > Math.max(world.getTopY() + 160, pad.getY() + 360)) {
			ServerPlayerEntity cp = crew == null ? null : world.getServer().getPlayerManager().getPlayer(crew);
			if (cp != null) {
				Moon.arrive(world, cp, payload, pad);
			} else {
				Space.reachOrbit(world, owner, payload, pad);
			}
			discard();
		}
		if (crew != null && flightTicks % 5 == 0) {   // the crew stays strapped in
			ServerPlayerEntity cp = world.getServer().getPlayerManager().getPlayer(crew);
			if (cp != null && cp.getVehicle() != this && cp.getWorld() == world) {
				cp.startRiding(this, true);
			}
		}
	}

	/** 3.64.0: puts {@code p} in the crew capsule; at the end of the climb they are set down at the destination. */
	public void board(ServerPlayerEntity p) {
		crew = p.getUuid();
		p.startRiding(this, true);
	}

	@Override
	protected void updatePassengerPosition(Entity passenger, PositionUpdater updater) {
		updater.accept(passenger, getX(), getY() + blocks().size() - 0.4, getZ());
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return getPassengerList().isEmpty();
	}

	/** Drops the lowest stage: it flies back (Landing Legs) or falls downrange; this rocket goes on with the rest. */
	private void separate(ServerWorld world) {
		List<BlockState> all = blocks();
		int cut = stageStarts[1];
		List<BlockState> lower = new ArrayList<>(all.subList(0, cut));
		List<BlockState> rest = new ArrayList<>(all.subList(cut, all.size()));
		boolean legs = lower.get(0).getBlock() instanceof RocketEngineBlock && lower.get(0).get(RocketEngineBlock.LEGS)
				&& flightTicks <= STAGE_BURN + 1;
		RocketEntity spent = new RocketEntity(Space.ROCKET, world);
		spent.setBlocks(lower);
		spent.dataTracker.set(MODE, legs ? RETURNING : FALLING);
		spent.owner = owner;
		spent.pad = pad;
		spent.setPosition(getPos());
		spent.setVelocity(0, getVelocity().y * 0.85, 0);
		world.spawnEntity(spent);
		setBlocks(rest);
		int[] next = new int[stageStarts.length - 1];
		for (int i = 1; i < stageStarts.length; i++) {
			next[i - 1] = stageStarts[i] - cut;
		}
		stageStarts = next;
		stageTicks = 0;
		setPosition(getPos().add(0, cut, 0));
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 3f, 1.6f);
		puff(world, ParticleTypes.CLOUD, getPos(), 30, 0.8, 0.1);
		Space.tellNear(world, pad, "separation", ++dropped);
	}

	private void fall(ServerWorld world) {
		Vec3d v = getVelocity();
		v = new Vec3d(Math.min(0.8, v.x + 0.02), v.y - 0.05, 0);
		setVelocity(v);
		setPosition(getPos().add(v));
		if (flightTicks % 2 == 0) {
			puff(world, ParticleTypes.SMOKE, getPos(), 2, 0.2, 0.01);
		}
		if (flightTicks > 220) {
			discard();
		}
	}

	private void fly_back(ServerWorld world) {
		Vec3d v = getVelocity();
		double target = pad.getY() + 1;
		double vy = v.y;
		boolean burning = false;
		if (vy > 0) {
			vy = vy - 0.05;
			if (flightTicks % 6 == 0) {
				puff(world, ParticleTypes.CLOUD, getPos().add(0, blocks().size(), 0), 3, 0.3, 0.05);   // cold-gas thrusters
			}
		} else {
			double d = getY() - target;
			double want = -Math.max(0.12, Math.min(3.0, d / 18.0));
			vy = Math.max(vy - 0.08, want);
			burning = d < 70;
			if (d <= Math.abs(vy) + 0.01) {
				land(world);
				return;
			}
		}
		// drift back over the pad if anything moved us
		double dx = (pad.getX() + 0.5 - getX()) * 0.2, dz = (pad.getZ() + 0.5 - getZ()) * 0.2;
		setVelocity(dx, vy, dz);
		setPosition(getPos().add(dx, vy, dz));
		if (burning) {
			exhaust(world, 0.8);
			if (flightTicks % 10 == 0) {
				world.playSound(null, getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 3f, 0.6f);
			}
		}
		if (flightTicks > 2400) {   // never got down: lost
			discard();
		}
	}

	private void land(ServerWorld world) {
		List<BlockState> list = blocks();
		boolean worn = world.random.nextDouble() < WEAR_CHANCE;
		Space.lifting = true;
		try {
			for (int i = 0; i < list.size(); i++) {
				BlockState s = list.get(i);
				if (i == 0 && s.getBlock() instanceof RocketEngineBlock) {
					s = s.with(RocketEngineBlock.WORN, worn);
				}
				BlockPos at = pad.up(1 + i);
				if (world.getBlockState(at).isReplaceable()) {
					world.setBlockState(at, s);
				} else {
					Block.dropStacks(s, world, at);
				}
			}
		} finally {
			Space.lifting = false;
		}
		puff(world, ParticleTypes.CAMPFIRE_COSY_SMOKE, Vec3d.ofBottomCenter(pad.up()), 30, 1.5, 0.02);
		world.playSound(null, pad, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.5f, 0.6f);
		Space.tellNear(world, pad, worn ? "landed_worn" : "landed");
		Space.landed(world, owner);
		discard();
	}

	private void exhaust(ServerWorld world, double scale) {
		Vec3d base = getPos();
		puff(world, ParticleTypes.FLAME, base.add(0, -0.4, 0), (int) (8 * scale), 0.25 * scale, 0.04);
		puff(world, ParticleTypes.LARGE_SMOKE, base.add(0, -1.5, 0), (int) (6 * scale), 0.5 * scale, 0.03);
		if (flightTicks < 70 && mode() == ASCENT) {   // the cloud of steam and smoke over the pad at lift-off
			puff(world, ParticleTypes.CAMPFIRE_COSY_SMOKE, Vec3d.ofBottomCenter(pad.up()), 6, 2.5, 0.05);
			puff(world, ParticleTypes.CLOUD, Vec3d.ofBottomCenter(pad.up()), 8, 2.0, 0.15);
		}
	}

	private static void puff(ServerWorld world, ParticleEffect p, Vec3d at, int count, double spread, double speed) {
		for (ServerPlayerEntity viewer : world.getPlayers()) {
			if (viewer.getPos().squaredDistanceTo(at) < 512 * 512) {
				world.spawnParticles(viewer, p, true, at.x, at.y, at.z, count, spread, spread * 0.5, spread, speed);
			}
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if (nbt.contains("blocks", NbtElement.INT_ARRAY_TYPE)) {
			NbtCompound b = new NbtCompound();
			b.putIntArray("b", nbt.getIntArray("blocks"));
			dataTracker.set(BLOCKS, b);
		}
		dataTracker.set(MODE, nbt.getInt("mode"));
		stageStarts = nbt.contains("stages") ? nbt.getIntArray("stages") : new int[]{0};
		if (stageStarts.length == 0) {
			stageStarts = new int[]{0};
		}
		owner = nbt.containsUuid("owner") ? nbt.getUuid("owner") : null;
		crew = nbt.containsUuid("crew") ? nbt.getUuid("crew") : null;
		payload = nbt.contains("payload") ? ItemStack.fromNbt(getRegistryManager(), nbt.getCompound("payload"))
				.orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
		pad = BlockPos.fromLong(nbt.getLong("pad"));
		stageTicks = nbt.getInt("stageTicks");
		flightTicks = nbt.getInt("flightTicks");
		dropped = nbt.getInt("dropped");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putIntArray("blocks", dataTracker.get(BLOCKS).getIntArray("b"));
		nbt.putInt("mode", mode());
		nbt.putIntArray("stages", stageStarts);
		if (crew != null) {
			nbt.putUuid("crew", crew);
		}
		if (owner != null) {
			nbt.putUuid("owner", owner);
		}
		if (!payload.isEmpty()) {
			nbt.put("payload", payload.encode(getRegistryManager()));
		}
		nbt.putLong("pad", pad.asLong());
		nbt.putInt("stageTicks", stageTicks);
		nbt.putInt("flightTicks", flightTicks);
		nbt.putInt("dropped", dropped);
	}
}
