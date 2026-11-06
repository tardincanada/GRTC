package com.periodicelements.mixin;

import com.periodicelements.medicine.Infection;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Eating rotten flesh can cause an Infection (1 in 100). */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Inject(method = "eatFood", at = @At("HEAD"))
	private void periodicElements$rottenFlesh(World world, ItemStack stack, FoodComponent food, CallbackInfoReturnable<ItemStack> cir) {
		if (!world.isClient && stack.isOf(Items.ROTTEN_FLESH)) {
			Infection.maybeInfect((PlayerEntity) (Object) this, Infection.FLESH_ODDS);
		}
	}
}
