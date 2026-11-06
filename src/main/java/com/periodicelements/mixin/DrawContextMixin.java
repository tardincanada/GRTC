package com.periodicelements.mixin;

import com.periodicelements.client.CherenkovRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Strongly radioactive items glow blue in their inventory slot (under the count and wear bar). */
@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
	@Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At("HEAD"))
	private void periodicElements$cherenkov(TextRenderer text, ItemStack stack, int x, int y, String count, CallbackInfo ci) {
		CherenkovRenderer.drawSlotGlow((DrawContext) (Object) this, stack, x, y);
	}
}
