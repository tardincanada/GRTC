package com.periodicelements.client;

import com.periodicelements.PeriodicElements;
import com.periodicelements.space.Mercury;
import com.periodicelements.space.Venus;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * 3.68.0: the skies of the inner planets. Mercury's is black like the Moon's, with a Sun two and a half times as wide
 * standing low over the dawn band: its height comes from how far east of the dawn line you stand, not the clock. Venus,
 * 50 km up, has a pale yellow sky over a sea of acid clouds, with the Sun bright through the haze above.
 */
public final class InnerSky {
	private InnerSky() {
	}

	/** Pale sulphur-yellow haze, dimming at night. */
	static class VenusEffects extends DimensionEffects {
		VenusEffects() {
			super(Float.NaN, true, SkyType.NORMAL, false, false);
		}

		@Override
		public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
			float b = sunHeight * 0.9f + 0.1f;
			return new Vec3d(0.93 * b, 0.86 * b, 0.62 * b);
		}

		@Override
		public boolean useThickFog(int camX, int camY) {
			return false;
		}
	}

	/** The Sun's height over Mercury where the camera stands: on the horizon on the dawn line, overhead 150 blocks east. */
	public static float mercurySunAngle(long time, double x) {
		double east = x - Mercury.dawnLine(time);
		double elevation = MathHelper.clamp(east * 0.6, -90, 90);
		return (float) (0.75 + elevation / 360.0);
	}

	public static void init() {
		DimensionRenderingRegistry.registerDimensionEffects(Identifier.of(PeriodicElements.MOD_ID, "mercury"), new MoonSky.Effects());
		DimensionRenderingRegistry.registerSkyRenderer(Mercury.MERCURY, InnerSky::mercury);
		DimensionRenderingRegistry.registerCloudRenderer(Mercury.MERCURY, ctx -> { });
		DimensionRenderingRegistry.registerWeatherRenderer(Mercury.MERCURY, ctx -> { });
		DimensionRenderingRegistry.registerDimensionEffects(Identifier.of(PeriodicElements.MOD_ID, "venus"), new VenusEffects());
		DimensionRenderingRegistry.registerCloudRenderer(Venus.VENUS, ctx -> { });   // the clouds are blocks, below
		DimensionRenderingRegistry.registerWeatherRenderer(Venus.VENUS, ctx -> { });
		BlockRenderLayerMap.INSTANCE.putBlock(Venus.VENUS_CLOUD, RenderLayer.getTranslucent());
	}

	private static void mercury(WorldRenderContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		double x = mc.gameRenderer.getCamera().getPos().x;
		MoonSky.draw(ctx, mercurySunAngle(Mercury.clock(ctx.world()), x), 45f, false);
	}
}
