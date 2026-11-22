package com.periodicelements.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.periodicelements.PeriodicElements;
import com.periodicelements.space.Mars;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 3.66.0: the sky of Mars. Fine red dust hangs in the thin air and scatters the light, so the day sky is butterscotch;
 * the same dust sends blue light forward round the Sun, so the sunset is blue (Spirit, Curiosity). A smaller Sun, little
 * Phobos racing backwards across the sky, stars at night, and dust storms that blot it all out.
 */
public final class MarsSky {
	private static final Identifier SUN = Identifier.ofVanilla("textures/environment/sun.png");
	private static final Identifier PHOBOS = Identifier.of(PeriodicElements.MOD_ID, "textures/environment/phobos.png");
	private static final Vector3f DAY = new Vector3f(0.82f, 0.64f, 0.46f), ZENITH = new Vector3f(0.64f, 0.47f, 0.34f),
			HORIZON = new Vector3f(0.86f, 0.72f, 0.56f), BLUE = new Vector3f(0.42f, 0.60f, 0.86f), DUST = new Vector3f(0.58f, 0.40f, 0.27f);
	private static float[] stars;

	private MarsSky() {
	}

	static float storm() {
		ClientWorld w = MinecraftClient.getInstance().world;
		return w == null ? 0 : Mars.storm(w);
	}

	/** Butterscotch fog by day, dusty brown in a storm, dark at night; the fog thickens in a storm. */
	static class Effects extends DimensionEffects {
		Effects() {
			super(Float.NaN, false, SkyType.NONE, false, false);
		}

		@Override
		public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
			float s = storm();
			Vector3f c = new Vector3f(HORIZON).lerp(DUST, s);
			float b = sunHeight * 0.94f + 0.06f;
			return new Vec3d(c.x * b, c.y * b, c.z * b);
		}

		@Override
		public boolean useThickFog(int camX, int camY) {
			return storm() > 0.45f;
		}

		@Override
		public float[] getFogColorOverride(float skyAngle, float tickDelta) {
			return null;
		}
	}

	public static void init() {
		DimensionRenderingRegistry.registerDimensionEffects(Identifier.of(PeriodicElements.MOD_ID, "mars"), new Effects());
		DimensionRenderingRegistry.registerSkyRenderer(Mars.MARS, MarsSky::render);
		DimensionRenderingRegistry.registerCloudRenderer(Mars.MARS, ctx -> { });
		DimensionRenderingRegistry.registerWeatherRenderer(Mars.MARS, ctx -> { });
		ClientTickEvents.END_CLIENT_TICK.register(MarsSky::dust);
	}

	/** Dust blowing past in a storm. */
	private static void dust(MinecraftClient mc) {
		ClientWorld w = mc.world;
		if (w == null || mc.player == null || mc.isPaused() || !Mars.is(w)) {
			return;
		}
		float s = Mars.storm(w);
		if (s < 0.05f) {
			return;
		}
		Random r = w.random;
		Vec3d at = mc.player.getEyePos();
		DustParticleEffect red = new DustParticleEffect(new Vector3f(0.72f, 0.42f, 0.24f), 0.9f);
		for (int i = 0; i < 90 * s; i++) {
			double x = at.x + r.nextGaussian() * 9, y = at.y + r.nextGaussian() * 4, z = at.z + r.nextGaussian() * 9;
			w.addParticle(red, x, y, z, 1.6 + r.nextFloat(), r.nextFloat() * 0.2 - 0.1, 0.6 + r.nextFloat() * 0.4);
		}
	}

	/** Star quads: 4 corners x 3 coordinates, then 1 brightness per star. */
	private static float[] stars() {
		if (stars != null) {
			return stars;
		}
		Random r = Random.create(19760720L);
		int n = 1500;
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
			float size = 0.12f + r.nextFloat() * 0.16f;
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
			out[k++] = 0.4f + r.nextFloat() * 0.6f;
		}
		stars = out;
		return out;
	}

	/** The colour of the sky in direction {@code d} (unit), with the Sun towards {@code sun} (unit). */
	static Vector3f skyColour(Vector3f d, Vector3f sun, float daylight, float storm) {
		float up = Math.max(0, d.y);
		Vector3f c = new Vector3f(HORIZON).lerp(ZENITH, (float) Math.sqrt(up));
		c.lerp(DAY, 0.35f);
		// the blue glow round the Sun: strong when it is low, faint at noon
		float k = Math.max(0, d.dot(sun));
		float low = 1 - MathHelper.clamp(sun.y / 0.5f, 0, 1) * 0.75f;
		float glow = (float) Math.pow(k, 10) * low * 1.1f + (float) Math.pow(k, 60) * 0.5f;
		c.lerp(BLUE, MathHelper.clamp(glow, 0, 0.95f));
		c.lerp(DUST, storm * 0.85f);
		// twilight lingers: the dust stays lit long after sunset
		return c.mul(daylight);
	}

	private static void quad(BufferBuilder b, Matrix4f m, float size, float dist) {
		b.vertex(m, -size, dist, -size).texture(0, 0);
		b.vertex(m, size, dist, -size).texture(1, 0);
		b.vertex(m, size, dist, size).texture(1, 1);
		b.vertex(m, -size, dist, size).texture(0, 1);
	}

	static void render(WorldRenderContext ctx) {
		ClientWorld world = ctx.world();
		float delta = ctx.tickCounter().getTickDelta(false);
		float angle = world.getSkyAngle(delta);
		float storm = Mars.storm(world);
		// direction of the Sun in the world: it rises in the east (+x) and sets in the west
		float a = angle * MathHelper.TAU;
		Vector3f sun = new Vector3f(-MathHelper.sin(a), MathHelper.cos(a), 0).normalize();
		float daylight = MathHelper.clamp(sun.y * 2.2f + 0.45f, 0.02f, 1f) * (1 - storm * 0.45f);

		Matrix4f view = new Matrix4f(ctx.positionMatrix());
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// the dome of the sky, coloured vertex by vertex
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		int rings = 12, segs = 36;
		for (int i = 0; i < rings; i++) {
			float e0 = (float) Math.toRadians(-20 + 110.0 * i / rings), e1 = (float) Math.toRadians(-20 + 110.0 * (i + 1) / rings);
			for (int j = 0; j < segs; j++) {
				float a0 = MathHelper.TAU * j / segs, a1 = MathHelper.TAU * (j + 1) / segs;
				float[][] corners = {{e0, a0}, {e0, a1}, {e1, a1}, {e1, a0}};
				for (float[] q : corners) {
					Vector3f d = new Vector3f(MathHelper.cos(q[0]) * MathHelper.cos(q[1]), MathHelper.sin(q[0]),
							MathHelper.cos(q[0]) * MathHelper.sin(q[1]));
					Vector3f c = skyColour(d, sun, daylight, storm);
					b.vertex(view, d.x * 100, d.y * 100, d.z * 100).color(c.x, c.y, c.z, 1f);
				}
			}
		}
		BufferRenderer.drawWithGlobalProgram(b.end());

		// stars come out when the sky darkens
		float starAlpha = MathHelper.clamp(1 - daylight * 2.2f, 0, 1) * (1 - storm);
		if (starAlpha > 0.01f) {
			Matrix4f sky = new Matrix4f(view);
			sky.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
			sky.rotate(RotationAxis.POSITIVE_X.rotationDegrees(angle * 360f));
			b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			float[] s = stars();
			for (int i = 0; i < s.length; i += 13) {
				float c = s[i + 12];
				for (int v = 0; v < 4; v++) {
					b.vertex(sky, s[i + v * 3], s[i + v * 3 + 1], s[i + v * 3 + 2]).color(c, c, c, starAlpha);
				}
			}
			BufferRenderer.drawWithGlobalProgram(b.end());
		}

		// the Sun, two thirds as wide as from the Earth, and veiled by a storm
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1 - storm * 0.85f);
		Matrix4f sunM = new Matrix4f(view);
		sunM.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
		sunM.rotate(RotationAxis.POSITIVE_X.rotationDegrees(angle * 360f));
		RenderSystem.setShaderTexture(0, SUN);
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE,
				GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
		b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		quad(b, sunM, 20f, 100f);
		BufferRenderer.drawWithGlobalProgram(b.end());

		// Phobos goes round faster than Mars turns: it rises in the west and sets in the east about three times a day
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1, 1, 1, MathHelper.clamp(1 - storm * 1.5f, 0, 1));
		Matrix4f phobos = new Matrix4f(view);
		phobos.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
		phobos.rotate(RotationAxis.POSITIVE_Z.rotationDegrees(15f));
		long t = world.getTimeOfDay();
		phobos.rotate(RotationAxis.POSITIVE_X.rotationDegrees(-((t % 7660L) + delta) / 7660f * 360f + 120f));
		RenderSystem.setShaderTexture(0, PHOBOS);
		b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		quad(b, phobos, 2.6f, 100f);
		BufferRenderer.drawWithGlobalProgram(b.end());

		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.disableBlend();
		RenderSystem.enableCull();
		RenderSystem.depthMask(true);
	}
}
