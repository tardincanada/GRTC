package com.periodicelements.mixin;

import com.periodicelements.PeriodicElements;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

/** A new world starts with the Geological world type selected (Guilherme, 2026-11-04); the others stay one click away. */
@Mixin(WorldCreator.class)
public abstract class WorldCreatorMixin {
	private static final RegistryKey<WorldPreset> GEOLOGICAL = RegistryKey.of(RegistryKeys.WORLD_PRESET,
			Identifier.of(PeriodicElements.MOD_ID, "geological"));

	@Inject(method = "<init>", at = @At("TAIL"))
	private void periodicElements$geologicalByDefault(Path savesDirectory, GeneratorOptionsHolder options,
			Optional<RegistryKey<WorldPreset>> defaultType, OptionalLong seed, CallbackInfo ci) {
		com.periodicelements.worldgen.GeoOptions.setPending(null); // 3.49.0: each new world starts from the defaults
		if (defaultType.isEmpty() || !defaultType.get().equals(WorldPresets.DEFAULT)) return; // re-creating a world keeps its type
		WorldCreator self = (WorldCreator) (Object) this;
		self.getNormalWorldTypes().stream().filter(t -> t.preset() != null && t.preset().matchesKey(GEOLOGICAL))
				.findFirst().ifPresent(self::setWorldType);
	}

	/** 3.49.0: the Geological type gets a "Customize" button (its options: worldgen/GeoOptions). */
	@Inject(method = "getLevelScreenProvider", at = @At("RETURN"), cancellable = true)
	private void periodicElements$geologicalOptions(CallbackInfoReturnable<net.minecraft.client.gui.screen.world.LevelScreenProvider> cir) {
		WorldCreator.WorldType type = ((WorldCreator) (Object) this).getWorldType();
		if (type.preset() != null && type.preset().matchesKey(GEOLOGICAL)) {
			cir.setReturnValue((parent, holder) -> new com.periodicelements.client.GeoOptionsScreen(parent));
		}
	}
}
