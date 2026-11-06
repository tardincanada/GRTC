package com.periodicelements.explore;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A thrown animal trap: catches the animal it hits, or lets out the one it holds where it lands. */
public class TrapEntity extends ThrownItemEntity {
	public TrapEntity(EntityType<? extends TrapEntity> type, World world) {
		super(type, world);
	}

	public TrapEntity(World world, LivingEntity owner, ItemStack trap) {
		super(Explore.TRAP_ENTITY, owner, world);
		setItem(trap);
	}

	@Override
	protected Item getDefaultItem() {
		return Explore.ROPE_NET;
	}

	private ItemStack trap() {
		return getStack().copy();
	}

	/** Catches {@code target} into the empty trap if it can hold it: true and the trap is full, the animal gone. */
	public static boolean tryCatch(ItemStack trap, Entity target, PlayerEntity thrower) {
		if (TrapItem.full(trap) || !(trap.getItem() instanceof TrapItem t) || !t.canHold(target, thrower)) {
			return false;
		}
		if (thrower instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
			com.periodicelements.compendium.Compendium.noteAnimal(sp, target);  // 3.50.0: a caught animal is noted
		}
		NbtCompound nbt = TrapItem.capture(target);
		target.discard();
		trap.set(Explore.TRAPPED, NbtComponent.of(nbt));
		if (!t.cage()) {
			trap.setDamage(trap.getDamage() + 1);
		}
		target.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_WOOL_PLACE,
				SoundCategory.NEUTRAL, 1f, 0.8f);
		return true;
	}

	/** Lets the animal in a full trap out at {@code at}; the trap is empty afterwards. Returns the animal, or null. */
	public static Entity release(ServerWorld world, ItemStack trap, Vec3d at, float yaw) {
		NbtCompound nbt = trap.get(Explore.TRAPPED).copyNbt();
		Entity e = EntityType.getEntityFromNbt(nbt, world).orElse(null);
		if (e != null) {
			e.refreshPositionAndAngles(at.x, at.y, at.z, yaw, 0f);
			world.spawnEntity(e);
		}
		trap.remove(Explore.TRAPPED);
		world.playSound(null, at.x, at.y, at.z, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.NEUTRAL, 0.8f, 1.2f);
		return e;
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		if (getWorld().isClient) {
			return;
		}
		ItemStack trap = trap();
		Entity target = hit.getEntity();
		if (TrapItem.full(trap)) {
			letOut(trap, hit.getPos());
		} else if (tryCatch(trap, target, getOwner() instanceof PlayerEntity p ? p : null)) {
			drop(trap, target.getPos());
		} else {
			drop(trap, getPos());
		}
		discard();
	}

	@Override
	protected void onBlockHit(BlockHitResult hit) {
		super.onBlockHit(hit);
		if (getWorld().isClient) {
			return;
		}
		ItemStack trap = trap();
		Vec3d at = Vec3d.ofBottomCenter(hit.getBlockPos().offset(hit.getSide()));
		if (TrapItem.full(trap)) {
			letOut(trap, at);
		} else {
			drop(trap, getPos());
		}
		discard();
	}

	@Override
	protected void onCollision(HitResult hit) {
		super.onCollision(hit);
		if (!getWorld().isClient && !isRemoved()) {
			discard();
		}
	}

	/** Lets the animal out and drops the empty trap there (a worn-out net is used up). */
	void letOut(ItemStack trap, Vec3d at) {
		release((ServerWorld) getWorld(), trap, at, getYaw());
		if (!trap.isDamageable() || trap.getDamage() < trap.getMaxDamage()) {
			drop(trap, at);
		}
	}

	void drop(ItemStack trap, Vec3d at) {
		ItemEntity item = new ItemEntity(getWorld(), at.x, at.y + 0.2, at.z, trap);
		item.setToDefaultPickupDelay();
		getWorld().spawnEntity(item);
	}
}
