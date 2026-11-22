package com.periodicelements.mixin;

import com.periodicelements.space.Vacuum;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 3.65.0: a furnace, smoker or blast furnace does not burn without oxygen; a lit one goes out. */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class VacuumFurnaceMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private static void periodicElements$noAir(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace,
											   CallbackInfo ci) {
		if (!Vacuum.hasAir(world, pos)) {
			if (state.get(AbstractFurnaceBlock.LIT)) {
				world.setBlockState(pos, state.with(AbstractFurnaceBlock.LIT, false), 3);
			}
			ci.cancel();
		}
	}
}
