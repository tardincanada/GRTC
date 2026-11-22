package com.periodicelements.mixin;

import com.periodicelements.space.Vacuum;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 3.65.0: fire cannot start where there is no oxygen (flint and steel, fire charges, lava, lightning). */
@Mixin(AbstractFireBlock.class)
public abstract class VacuumFireMixin {
	@Inject(method = "canPlaceAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z",
			at = @At("HEAD"), cancellable = true)
	private static void periodicElements$noAir(World world, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (!Vacuum.hasAir(world, pos)) {
			cir.setReturnValue(false);
		}
	}
}
