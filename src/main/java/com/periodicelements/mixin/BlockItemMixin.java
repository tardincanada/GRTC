package com.periodicelements.mixin;

import com.periodicelements.crops.Genetics;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 3.23.0: a crop planted from seeds takes the seeds' genes. 3.45.0: a waterloggable block placed in salt water is
 * waterlogged, as it would be in fresh water.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Inject(method = "postPlacement", at = @At("HEAD"))
	private void periodicElements$plantGenome(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state,
			CallbackInfoReturnable<Boolean> cir) {
		if (world instanceof ServerWorld sw && Genetics.info(state.getBlock()) != null) {
			Genetics.planted(sw, pos, stack);
		}
	}

	@Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
	private void periodicElements$saltWaterlogged(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = cir.getReturnValue();
		if (state != null && state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED)) {
			var fluid = ctx.getWorld().getFluidState(ctx.getBlockPos());
			if (fluid.isStill() && com.periodicelements.water.Water.isSaltWater(fluid.getFluid())) {
				cir.setReturnValue(state.with(Properties.WATERLOGGED, true));
			}
		}
	}
}
