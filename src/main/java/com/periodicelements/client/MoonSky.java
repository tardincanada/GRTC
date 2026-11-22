package com.periodicelements.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.periodicelements.PeriodicElements;
import com.periodicelements.space.Moon;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

/**
 * 3.64.0: the Moon's sky. No air, so it is black by day: thousands of steady stars, the Sun, and the Earth hanging in
 * the same place all the time (the Moon always turns the same face to it).
 */
public final class MoonSky {
	private static final Identifier SUN = Identifier.ofVanilla("textures/environment/sun.png");
	private static final Identifier EARTH = Identifier.of(PeriodicElements.MOD_ID, "textures/environment/earth.png");
	private static float[] stars;

	private MoonSky() {
	}

	/** No sky colour, no fog colour: black. */
	static class Effects extends DimensionEffects {
		Effects() {
			super(Float.NaN, false, SkyType.NONE, false, false);
		}

		@Override
		public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
			return Vec3d.ZERO;
		}

		@Override
		public boolean useThickFog(int camX, int camY) {
			return false;
		}

		@Override
		public float[] getFogColorOverride(float skyAngle, float tickDelta) {
			return null;
		}
	}

	public static void init() {
		DimensionRenderingRegistry.registerDimensionEffects(Identifier.of(PeriodicElements.MOD_ID, "moon"), new Effects());
		DimensionRenderingRegistry.registerSkyRenderer(Moon.MOON, MoonSky::render);
		DimensionRenderingRegistry.registerCloudRenderer(Moon.MOON, ctx -> { });
		DimensionRenderingRegistry.registerWeatherRenderer(Moon.MOON, ctx -> { });
	}

	/** Star quads: 4 corners x 3 coordinates, then 1 brightness per star. */
	private static float[] stars() {
		if (stars != null) {
			return stars;
		}
		Random r = Random.create(10842L);
		int n = 3000;
		float[] out = new float[n * 13];
		int k = 0;
		for (int i = 0; i < n; i++) {
			Vector3f d = new Vector3f(r.nextFloat() * 2 - 1, r.nextFloat() * 2 - 1, r.nextFloat() * 2 - 1);
			float len = d.length();
			if (len < 0.01f || len > 1) {
				i--;
				continue;
			}
			d.normalize().mul(100);
			float size = 0.12f + r.nextFloat() * 0.18f;
			Vector3f a = new Vector3f(d).cross(0, 1, 0);
			if (a.lengthSquared() < 1e-3) {
				a.set(1, 0, 0);
			}
			a.normalize().mul(size);
			Vector3f b = new Vector3f(d).cross(a).normalize().mul(size);
			float[][] c = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
			for (float[] q : c) {
				out[k++] = d.x + a.x * q[0] + b.x * q[1];
				out[k++] = d.y + a.y * q[0] + b.y * q[1];
				out[k++] = d.z + a.z * q[0] + b.z * q[1];
			}
			out[k++] = 0.5f + r.nextFloat() * 0.5f;
		}
		stars = out;
		return out;
	}

	private static void quad(BufferBuilder b, Matrix4f m, float size, float dist) {
		b.vertex(m, -size, dist, -size).texture(0, 0);
		b.vertex(m, size, dist, -size).texture(1, 0);
		b.vertex(m, size, dist, size).texture(1, 1);
		b.vertex(m, -size, dist, size).texture(0, 1);
	}

	static void render(WorldRenderContext ctx) {
		draw(ctx, ctx.world().getSkyAngle(ctx.tickCounter().getTickDelta(false)), 18f, true);
	}

	/** 3.68.0: the airless sky with the Sun at {@code angle} (vanilla's sky angle), {@code sunSize} wide, the Earth or not. */
	static void draw(WorldRenderContext ctx, float angle, float sunSize, boolean showEarth) {
		Matrix4fStack view = new Matrix4fStack(4);
		view.set(ctx.positionMatrix());
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();   // star and Earth quads face whichever way their corners happen to wind
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// stars
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		float[] s = stars();
		for (int i = 0; i < s.length; i += 13) {
			float c = s[i + 12];
			for (int v = 0; v < 4; v++) {
				b.vertex(view, s[i + v * 3], s[i + v * 3 + 1], s[i + v * 3 + 2]).color(c, c, c, 1f);
			}
		}
		BufferRenderer.drawWithGlobalProgram(b.end());

		// the Sun, where the day's fixed time puts it
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		Matrix4f sun = new Matrix4f(view);
		sun.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
		sun.rotate(RotationAxis.POSITIVE_X.rotationDegrees(angle * 360f));
		RenderSystem.setShaderTexture(0, SUN);
		// added light, as vanilla draws it: with no air to dim it the Sun is a blinding white disc
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE,
				GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
		b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		quad(b, sun, sunSize, 100f);
		BufferRenderer.drawWithGlobalProgram(b.end());

		// the Earth, high in the northern sky, about four times as wide as the Moon looks from home
		if (showEarth) {
			earth(view);
		}

		RenderSystem.disableBlend();
		RenderSystem.enableCull();
		RenderSystem.depthMask(true);
	}

	private static void earth(Matrix4f view) {
		Matrix4f earth = new Matrix4f(view);
		earth.rotate(RotationAxis.POSITIVE_X.rotationDegrees(-35f));
		earth.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(20f));
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, EARTH);
		BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		quad(b, earth, 9f, 100f);
		BufferRenderer.drawWithGlobalProgram(b.end());
	}
}
