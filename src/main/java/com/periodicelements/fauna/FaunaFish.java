package com.periodicelements.fauna;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * A water animal of the fauna plan that does not school: sturgeon, sperm whale, Greenland shark, murex, cuttlefish,
 * crystal jelly. The whale cannot be hurt by players (Guilherme: it is not hunted); the jelly stings.
 */
public class FaunaFish extends FishEntity {
	public FaunaFish(EntityType<? extends FishEntity> type, World world) {
		super(type, world);
	}

	public Fauna.Species species() {
		return Fauna.of(getType());
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		Fauna.Species s = species();
		if (s != null && s.behaviour().equals("neutral")) {
			goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));
			targetSelector.add(1, new RevengeGoal(this));
		}
	}

	@Override
	protected boolean hasSelfControl() {
		return true;
	}

	@Override
	public ItemStack getBucketItem() {
		Fauna.Species s = species();
		return s != null && s.bucket() != null && Fauna.item(s.bucket()) != null ? new ItemStack(Fauna.item(s.bucket())) : ItemStack.EMPTY;
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (getBucketItem().isEmpty()) return ActionResult.PASS;
		return super.interactMob(player, hand);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		Fauna.Species s = species();
		if (s != null && s.has("invulnerable") && source.getAttacker() instanceof PlayerEntity) return false;
		return super.damage(source, amount);
	}

	@Override
	public void onPlayerCollision(PlayerEntity player) {
		Fauna.Species s = species();
		if (s != null && s.has("sting") && !getWorld().isClient && player.isAlive() && !player.isCreative()) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0), this);
		}
	}

	@Override
	public boolean canImmediatelyDespawn(double distance) {
		Fauna.Species s = species();
		return s == null || s.width() < 1 && super.canImmediatelyDespawn(distance);
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.ENTITY_COD_FLOP;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_COD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_COD_DEATH;
	}
}
