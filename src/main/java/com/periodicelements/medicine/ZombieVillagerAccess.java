package com.periodicelements.medicine;

/** Implemented on ZombieVillagerEntity by a mixin: its cure countdown. */
public interface ZombieVillagerAccess {
	int periodicElements$getConversionTimer();

	void periodicElements$setConversionTimer(int ticks);

	/** Starts the cure, as a golden apple would. */
	void periodicElements$startCure(java.util.UUID player, int delay);
}
