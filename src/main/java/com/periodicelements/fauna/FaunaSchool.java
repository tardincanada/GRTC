package com.periodicelements.fauna;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/** A schooling fish of the fauna plan (the Atlantic herring). */
public class FaunaSchool extends SchoolingFishEntity {
	public FaunaSchool(EntityType<? extends SchoolingFishEntity> type, World world) {
		super(type, world);
	}

	@Override
	public int getMaxGroupSize() {
		Fauna.Species s = Fauna.of(getType());
		return s == null ? 8 : s.max();
	}

	@Override
	public ItemStack getBucketItem() {
		Fauna.Species s = Fauna.of(getType());
		return s != null && s.bucket() != null && Fauna.item(s.bucket()) != null ? new ItemStack(Fauna.item(s.bucket())) : ItemStack.EMPTY;
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
