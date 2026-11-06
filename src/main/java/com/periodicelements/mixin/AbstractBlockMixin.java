package com.periodicelements.mixin;

import com.periodicelements.crops.Genetics;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** 3.23.0: the seeds a crop drops carry the genes of the plant and its pollen. */
@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
	@Inject(method = "getDroppedStacks", at = @At("RETURN"), cancellable = true)
	private void periodicElements$seedGenes(BlockState state, LootContextParameterSet.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
		if (Genetics.info(state.getBlock()) == null && !Genetics.isGrass(state)) {
			return;
		}
		Vec3d origin = builder.getOptional(LootContextParameters.ORIGIN);
		if (origin == null) {
			return;
		}
		cir.setReturnValue(Genetics.drops(state, builder.getWorld(), BlockPos.ofFloored(origin), cir.getReturnValue()));
	}
}
