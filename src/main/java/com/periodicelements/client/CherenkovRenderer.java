package com.periodicelements.client;

import com.periodicelements.radiation.Cherenkov;
import com.periodicelements.radiation.Radioactivity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The blue Cherenkov glow of strongly radioactive things, drawn without shaders: soft camera-facing discs blended
 * additively (source alpha, one), the way vanilla draws lightning. Bright in the dark, faint in daylight.
 *
 * <p>Glows: dropped items, radioactive blocks within {@link #SCAN_RADIUS} and running reactors and accelerators. In
 * inventories a simpler glow tints the slot (see {@link #drawSlotGlow}).
 */
public final class CherenkovRenderer {
	static final int SCAN_RADIUS = 16;
	private static final int SCAN_EVERY = 20;
	private static final int SEGMENTS = 18;
	private static final double KNEE = 0.35;
	private static final double KNEE_ALPHA = 0.45;
	/** A glow source: centre, brightness 0..1 and radius in blocks. */
	/** A glow source: centre, brightness 0..1, radius in blocks, and the height of the water surface above it (NaN if none). */
	record Glow(Vec3d at, double strength, double radius, double surface) {
	}

	/** The glows drawn this frame, for the water-surface pass after translucent blocks. */
	private static List<Glow> frame = List.of();

	private static final RenderLayer LAYER = RenderLayer.of("periodic_elements_cherenkov", VertexFormats.POSITION_COLOR,
			VertexFormat.DrawMode.QUADS, 4096, false, true, RenderLayer.MultiPhaseParameters.builder()
					.program(RenderPhase.COLOR_PROGRAM)
					.transparency(RenderPhase.LIGHTNING_TRANSPARENCY)
					.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
					.cull(RenderPhase.DISABLE_CULLING)
					.writeMaskState(RenderPhase.COLOR_MASK)
					.build(false));

	/** The same for inventory slots: drawn over everything in the slot. */
	private static final RenderLayer GUI_LAYER = RenderLayer.of("periodic_elements_cherenkov_gui", VertexFormats.POSITION_COLOR,
			VertexFormat.DrawMode.QUADS, 1536, false, false, RenderLayer.MultiPhaseParameters.builder()
					.program(RenderPhase.COLOR_PROGRAM)
					.transparency(RenderPhase.LIGHTNING_TRANSPARENCY)
					.depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
					.cull(RenderPhase.DISABLE_CULLING)
					.writeMaskState(RenderPhase.COLOR_MASK)
					.build(false));

	private static List<Glow> blocks = List.of();
	private static int ticks;

	private CherenkovRenderer() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) {
				blocks = List.of();
				return;
			}
			if (ticks++ % SCAN_EVERY == 0) {
				blocks = scan(client.world, client.player.getBlockPos());
			}
		});
		WorldRenderEvents.AFTER_ENTITIES.register(CherenkovRenderer::render);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(CherenkovRenderer::renderSurfaces);
	}

	/** Glowing blocks around {@code centre}. */
	static List<Glow> scan(ClientWorld world, BlockPos centre) {
		List<Glow> out = new ArrayList<>();
		BlockPos.Mutable p = new BlockPos.Mutable();
		int r = SCAN_RADIUS;
		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				for (int dy = -r; dy <= r; dy++) {
					p.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
					BlockState s = world.getBlockState(p);
					if (s.isAir()) {
						continue;
					}
					double rate = Cherenkov.rate(s);
					if (rate <= 0) {
						continue;
					}
					double strength = Cherenkov.strength(rate, wet(world, p, s));
					if (strength > 0) {
						out.add(new Glow(Vec3d.ofCenter(p), strength, 0.9 + 1.1 * strength, surface(world, p.up())));
					}
				}
			}
		}
		return out;
	}

	/** Is the block waterlogged or touching water? */
	static boolean wet(ClientWorld world, BlockPos pos, BlockState state) {
		if (state.getFluidState().isIn(FluidTags.WATER)) {
			return true;
		}
		for (Direction d : Direction.values()) {
			if (world.getFluidState(pos.offset(d)).isIn(FluidTags.WATER)) {
				return true;
			}
		}
		return false;
	}

	/** How much the glow shows here: full in darkness, a fifth in full daylight or torchlight. */
	static double visibility(ClientWorld world, BlockPos pos, float delta) {
		// Sky brightness runs from about 0.2 at night to 1 at noon; the sky light reaching this block scales it.
		double daylight = MathHelper.clamp((world.getSkyBrightness(delta) - 0.2) / 0.8, 0, 1);
		double sky = world.getLightLevel(LightType.SKY, pos) / 15.0 * daylight;
		double block = world.getLightLevel(LightType.BLOCK, pos) / 15.0;
		return 0.2 + 0.8 * (1 - Math.max(sky, block));
	}

	/** Height of the water surface over a column of water starting at {@code start}; NaN if {@code start} isn't water. */
	static double surface(ClientWorld world, BlockPos start) {
		BlockPos.Mutable q = start.mutableCopy();
		int n = 0;
		while (n < 12 && world.getFluidState(q).isIn(FluidTags.WATER)) {
			q.move(Direction.UP);
			n++;
		}
		return n == 0 ? Double.NaN : q.getY() - 1 + 0.89;
	}

	/** The pool lights up: a wide soft glow on the water surface over each wet source, drawn over the water. */
	private static void renderSurfaces(WorldRenderContext context) {
		ClientWorld world = context.world();
		if (world == null || frame.isEmpty() || context.consumers() == null) {
			return;
		}
		Vec3d cam = context.camera().getPos();
		Matrix4f m = context.matrixStack() != null ? context.matrixStack().peek().getPositionMatrix() : new Matrix4f();
		int color = Cherenkov.color();
		int cr = color >> 16 & 255, cg = color >> 8 & 255, cb = color & 255;
		float delta = context.tickCounter().getTickDelta(false);
		Vector3f east = new Vector3f(1, 0, 0), south = new Vector3f(0, 0, 1);
		VertexConsumer buf = context.consumers().getBuffer(LAYER);
		for (Glow g : frame) {
			if (Double.isNaN(g.surface())) {
				continue;
			}
			Vec3d top = new Vec3d(g.at().x, g.surface() + 0.01, g.at().z);
			double vis = visibility(world, BlockPos.ofFloored(top), delta);
			double depth = Math.max(0, g.surface() - g.at().y);
			// Deeper sources spread wider and fainter on the surface.
			double alpha = MathHelper.clamp(g.strength() * vis * 0.55 / (1 + 0.15 * depth), 0, 1);
			disc(buf, m, top.subtract(cam), east, south, g.radius() * 1.2 + 0.3 * depth, cr, cg, cb, (int) (alpha * 255));
		}
		if (context.consumers() instanceof net.minecraft.client.render.VertexConsumerProvider.Immediate immediate) {
			immediate.draw(LAYER);
		}
	}

	/** Is a dropped item in water, or floating on it? */
	static boolean wet(ClientWorld world, ItemEntity item) {
		BlockPos at = item.getBlockPos();
		return item.isTouchingWater() || world.getFluidState(at).isIn(FluidTags.WATER)
				|| world.getFluidState(at.down()).isIn(FluidTags.WATER);
	}

	private static void render(WorldRenderContext context) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ClientWorld world = context.world();
		if (world == null || mc.player == null) {
			return;
		}
		List<Glow> glows = new ArrayList<>(blocks);
		float delta = context.tickCounter().getTickDelta(false);
		Box around = mc.player.getBoundingBox().expand(SCAN_RADIUS * 2);
		for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, around, e -> true)) {
			double rate = Radioactivity.of(item.getStack().getItem());
			if (rate <= 0) {
				continue;
			}
			double strength = Cherenkov.strength(rate, wet(world, item));
			if (strength > 0) {
				Vec3d at = item.getLerpedPos(delta).add(0, 0.3, 0);
				glows.add(new Glow(at, strength, 0.35 + 0.65 * strength, surface(world, item.getBlockPos())));
			}
		}
		frame = glows;
		if (glows.isEmpty()) {
			return;
		}
		Camera camera = context.camera();
		Vec3d cam = camera.getPos();
		Vector3f left = new Vector3f(camera.getDiagonalPlane());
		Vector3f up = new Vector3f(camera.getVerticalPlane());
		Matrix4f m = context.matrixStack() != null ? context.matrixStack().peek().getPositionMatrix() : new Matrix4f();
		int color = Cherenkov.color();
		int cr = color >> 16 & 255, cg = color >> 8 & 255, cb = color & 255;
		long time = world.getTime();
		VertexConsumer buf = context.consumers().getBuffer(LAYER);
		for (Glow g : glows) {
			if (g.at().squaredDistanceTo(cam) > 48 * 48) {
				continue;
			}
			double vis = visibility(world, BlockPos.ofFloored(g.at()), delta);
			// A slow shimmer, different for each source.
			double shimmer = 0.92 + 0.08 * MathHelper.sin((float) (time + delta + g.at().hashCode() % 97) * 0.15f);
			double alpha = MathHelper.clamp(g.strength() * vis * shimmer, 0, 1);
			if (alpha < 0.01) {
				continue;
			}
			// Bring the disc forward out of the block towards the camera, so the faces don't hide it.
			Vec3d toCam = cam.subtract(g.at());
			double dist = toCam.length();
			Vec3d c = g.at().add(toCam.multiply(Math.min(0.6, dist * 0.5) / Math.max(dist, 1e-3))).subtract(cam);
			disc(buf, m, c, left, up, g.radius(), cr, cg, cb, (int) (alpha * 255));
			disc(buf, m, c, left, up, g.radius() * 0.4, Math.min(255, cr + 90), Math.min(255, cg + 80), cb, (int) (alpha * 200));
		}
	}

	/** A soft disc: bright centre, a knee at {@link #KNEE} of the radius with {@link #KNEE_ALPHA} of the brightness, nothing at the rim. */
	private static void disc(VertexConsumer buf, Matrix4f m, Vec3d c, Vector3f left, Vector3f up, double radius,
							 int r, int g, int b, int a) {
		float cx = (float) c.x, cy = (float) c.y, cz = (float) c.z;
		int mid = (int) (a * KNEE_ALPHA);
		for (int i = 0; i < SEGMENTS; i++) {
			double a0 = Math.PI * 2 * i / SEGMENTS, a1 = Math.PI * 2 * (i + 1) / SEGMENTS;
			// Centre to knee: a quad with the centre twice.
			buf.vertex(m, cx, cy, cz).color(r, g, b, a);
			buf.vertex(m, cx, cy, cz).color(r, g, b, a);
			ring(buf, m, cx, cy, cz, left, up, radius * KNEE, a0, r, g, b, mid);
			ring(buf, m, cx, cy, cz, left, up, radius * KNEE, a1, r, g, b, mid);
			// Knee to rim.
			ring(buf, m, cx, cy, cz, left, up, radius * KNEE, a1, r, g, b, mid);
			ring(buf, m, cx, cy, cz, left, up, radius * KNEE, a0, r, g, b, mid);
			ring(buf, m, cx, cy, cz, left, up, radius, a0, r, g, b, 0);
			ring(buf, m, cx, cy, cz, left, up, radius, a1, r, g, b, 0);
		}
	}

	private static void ring(VertexConsumer buf, Matrix4f m, float cx, float cy, float cz, Vector3f left, Vector3f up,
							 double radius, double angle, int r, int g, int b, int a) {
		float s = (float) (Math.sin(angle) * radius), k = (float) (Math.cos(angle) * radius);
		buf.vertex(m, cx + left.x * s + up.x * k, cy + left.y * s + up.y * k, cz + left.z * s + up.z * k).color(r, g, b, a);
	}

	/** Inventory: a blue glow over a slot holding something radioactive enough to glow in air. */
	public static void drawSlotGlow(DrawContext ctx, ItemStack stack, int x, int y) {
		if (stack.isEmpty()) {
			return;
		}
		double strength = Cherenkov.strength(Radioactivity.of(stack.getItem()), false);
		if (strength <= 0) {
			return;
		}
		ctx.draw();
		int color = Cherenkov.color();
		int cr = color >> 16 & 255, cg = color >> 8 & 255, cb = color & 255;
		long time = MinecraftClient.getInstance().world != null ? MinecraftClient.getInstance().world.getTime() : 0;
		double pulse = 0.85 + 0.15 * MathHelper.sin(time * 0.12f);
		int a = (int) (MathHelper.clamp(strength / Cherenkov.AIR_FACTOR * pulse, 0, 1) * 255);
		Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
		VertexConsumer buf = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().getBuffer(GUI_LAYER);
		Vector3f left = new Vector3f(1, 0, 0), up = new Vector3f(0, -1, 0);
		Vec3d c = new Vec3d(x + 8, y + 8, 180);
		disc(buf, m, c, left, up, 11, cr, cg, cb, a);
		disc(buf, m, c, left, up, 5, Math.min(255, cr + 90), Math.min(255, cg + 80), cb, a / 2);
		ctx.draw();
	}
}
