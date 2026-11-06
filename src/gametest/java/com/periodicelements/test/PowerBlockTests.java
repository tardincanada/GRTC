package com.periodicelements.test;

import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.item.Item;

/** Helpers for power blocks in tests. */
final class PowerBlockTests {
	private PowerBlockTests() {
	}

	static boolean has(PowerBlockEntity p, Item item) {
		for (int i = 0; i < p.size(); i++) {
			if (p.getStack(i).isOf(item)) {
				return true;
			}
		}
		return false;
	}
}
