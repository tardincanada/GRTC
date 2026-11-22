package com.periodicelements.client;

import com.periodicelements.shrubs.PlantWarning;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * 3.54.0 (Guilherme): looking at a sapling or shrub that will not grow where it stands shows the warning just below
 * the crosshair ("This plant doesn't grow here", or "... in this environment" in an unsuitable greenhouse). Works
 * with or without Jade. The greenhouse check is a flood fill, so the answer is kept for a second per block.
 */
public final class PlantWarningHud {
	private static final int REFRESH_TICKS = 20;
	private static final int COLOR = 0xFFFF7060;
	private static BlockPos lastPos;
	private static BlockState lastState;
	private static long lastTime = Long.MIN_VALUE;
	private static String lastKey;

	private PlantWarningHud() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register((ctx, tick) -> render(ctx));
	}

	/** The warning key for the block under the crosshair, or null (also used by the Jade plugin). */
	public static String current() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null || !(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return null;
		}
		return keyAt(hit.getBlockPos());
	}

	/** The warning key for the plant at {@code pos} in the client world, cached for a second. */
	public static String keyAt(BlockPos pos) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null) {
			return null;
		}
		BlockState state = mc.world.getBlockState(pos);
		if (!PlantWarning.applies(state)) {
			return null;
		}
		long now = mc.world.getTime();
		if (pos.equals(lastPos) && state == lastState && now - lastTime >= 0 && now - lastTime < REFRESH_TICKS) {
			return lastKey;
		}
		lastPos = pos.toImmutable();
		lastState = state;
		lastTime = now;
		lastKey = PlantWarning.key(mc.world, pos, state);
		return lastKey;
	}

	private static void render(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden || mc.currentScreen != null) {
			return;
		}
		String key = current();
		if (key == null) {
			return;
		}
		Text text = Text.translatable(key);
		int w = mc.textRenderer.getWidth(text);
		int x = (ctx.getScaledWindowWidth() - w) / 2;
		int y = ctx.getScaledWindowHeight() / 2 + 10;
		ctx.fill(x - 3, y - 2, x + w + 3, y + mc.textRenderer.fontHeight, 0x90000000);
		ctx.drawTextWithShadow(mc.textRenderer, text, x, y, COLOR);
	}
}
