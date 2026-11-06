package com.periodicelements.mixin;

import com.periodicelements.farm.Life;
import net.minecraft.block.BlockState;
import net.minecraft.block.IceBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ice and snow next to Insulation (polystyrene foam) never melt. */
@Mixin({IceBlock.class, SnowBlock.class})
public abstract class IceBlockMixin {
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void periodicElements$insulated(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		for (Direction d : Direction.values()) {
			if (world.getBlockState(pos.offset(d)).isOf(Life.INSULATION)) {
				ci.cancel();
				return;
			}
		}
	}
}
