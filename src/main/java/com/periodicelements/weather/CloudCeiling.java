package com.periodicelements.weather;

/**
 * Fiat Lux requests P4 (3.49.2) and P5 (3.49.3): the top cloud layer of the Fiat Lux shader, above which it does not
 * rain. The client fades the rain sound and drops rain splashes above it (see {@code mixin.WorldRendererRainMixin});
 * gameplay (fires put out, cauldrons filling, etc.) is untouched.
 *
 * <p>Since P5 the top layer is fixed at Y 272: in rain the lower layers move down, the top stays. The numbers are the
 * shader's defaults. If the player changes them in the shader menu (Altitude > Cloud height, Layer distance), change
 * them here too.
 */
public final class CloudCeiling {
	/** Base of the lowest cloud layer in clear weather (shader: Altitude > Cloud height). */
	public static final double CLOUD_BASE = 192;
	/** Distance between cloud layers (shader: Altitude > Layer distance). */
	public static final double LAYER_GAP = 40;
	/** Blocks above the top layer over which the rain sound fades out. */
	public static final double FADE = 24;

	private CloudCeiling() {
	}

	/** Height of the top cloud layer: CLOUD_BASE + 2 LAYER_GAP = Y 272, whatever the rain. */
	public static double top() {
		return CLOUD_BASE + 2 * LAYER_GAP;
	}

	/** Volume factor for rain heard at height {@code y}: 1 - smoothstep(top, top + FADE, y). */
	public static float volume(double y) {
		double t = Math.clamp((y - top()) / FADE, 0.0, 1.0);
		return (float) (1 - t * t * (3 - 2 * t));
	}

	/** Can rain splash at height {@code y}? Not above the top layer. */
	public static boolean splashes(double y) {
		return y <= top();
	}
}
