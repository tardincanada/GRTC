package com.periodicelements.mixin;

import com.periodicelements.medicine.ZombieVillagerAccess;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

/** Lets the ether anaesthetic shorten a zombie villager's cure, and penicillin start one. */
@Mixin(ZombieVillagerEntity.class)
public abstract class ZombieVillagerEntityMixin implements ZombieVillagerAccess {
	@Shadow
	private int conversionTimer;

	@Override
	public int periodicElements$getConversionTimer() {
		return conversionTimer;
	}

	@Override
	public void periodicElements$setConversionTimer(int ticks) {
		conversionTimer = ticks;
	}

	@Override
	@Invoker("setConverting")
	public abstract void periodicElements$startCure(UUID player, int delay);
}
