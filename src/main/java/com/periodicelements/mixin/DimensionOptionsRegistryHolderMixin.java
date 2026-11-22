package com.periodicelements.mixin;

import com.mojang.serialization.Lifecycle;
import com.periodicelements.PeriodicElements;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 3.64.0: the game counts any dimension that is not the Overworld, the Nether or the End as experimental, so a world
 * with the Moon would warn "these settings are using experimental features" every time it is created or opened.
 * The mod's own dimensions are as stable as vanilla's.
 */
@Mixin(DimensionOptionsRegistryHolder.class)
public abstract class DimensionOptionsRegistryHolderMixin {
	@Inject(method = "getLifecycle", at = @At("HEAD"), cancellable = true)
	private static void periodicElements$stable(RegistryKey<DimensionOptions> key, DimensionOptions options,
												  CallbackInfoReturnable<Lifecycle> cir) {
		if (PeriodicElements.MOD_ID.equals(key.getValue().getNamespace())) {
			cir.setReturnValue(Lifecycle.stable());
		}
	}

	/**
	 * The dimension registry itself is born experimental when it holds more than the three vanilla dimensions. Born
	 * stable instead, it still turns experimental when another mod's dimension is added, since each entry's own
	 * lifecycle (above) is added to it.
	 */
	@ModifyArg(method = "toConfig", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/registry/SimpleRegistry;<init>(Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Lifecycle;)V"),
			index = 1)
	private Lifecycle periodicElements$stableRegistry(Lifecycle lifecycle) {
		return Lifecycle.stable();
	}
}
