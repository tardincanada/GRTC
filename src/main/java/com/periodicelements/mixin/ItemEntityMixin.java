package com.periodicelements.mixin;

import com.periodicelements.samplecase.SampleCase;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 3.31.0: a Sample Case that picks up takes element and substance items before the pack does. Whatever does not fit
 * is left for vanilla pickup, so nothing changes for a player without a case.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
	private void periodicElements$sampleCase(PlayerEntity player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (self.getWorld().isClient || self.cannotPickup()) {
			return;
		}
		ItemStack stack = self.getStack();
		boolean sample = SampleCase.accepts(stack);
		if (!sample && !com.periodicelements.explore.BackpackItem.isSpecimen(stack)) {
			return;
		}
		// 3.47.0: specimens (plants and minerals) go into a backpack that picks up.
		int taken = sample ? SampleCase.absorb(player, stack) : com.periodicelements.explore.BackpackItem.absorb(player, stack);
		if (taken <= 0) {
			return;
		}
		player.sendPickup(self, taken);
		player.increaseStat(Stats.PICKED_UP.getOrCreateStat(stack.getItem()), taken);
		if (stack.isEmpty()) {
			self.discard();
			ci.cancel();
		} else {
			self.setStack(stack);
		}
	}
}
