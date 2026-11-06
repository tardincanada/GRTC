package com.periodicelements.client;

import com.periodicelements.multiblock.Structures;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * 3.8.0: sneak and use a structure's controller with the Field Guide in hand, and the smallest structure is drawn round
 * it as translucent blocks (orange shell, grey ports, red tuyere, blue tower); blocks already in place are left out.
 * Do it again to hide it.
 */
public final class StructureOutline {
	private static final long SHOW_MS = 10 * 60 * 1000;

	private record Shown(Structures.Template template, BlockPos controller, Direction facing, long until) {
	}

	@Nullable
	private static Shown shown;

	private StructureOutline() {
	}

	public static void register() {
		com.periodicelements.multiblock.Multiblock.outline = (kind, pos, facing) -> {
			if (shown != null && shown.controller().equals(pos)) {
				shown = null;
			} else {
				// The lead chamber is drawn with its optional tower, so the player sees where it goes.
				Structures.Template t = kind == Structures.Kind.LEAD_CHAMBER ? Structures.chamber(3, 3, 3, true) : Structures.smallest(kind);
				shown = new Shown(t, pos.toImmutable(), facing, System.currentTimeMillis() + SHOW_MS);
			}
		};
		WorldRenderEvents.AFTER_TRANSLUCENT.register(StructureOutline::render);
	}

	private static float[] color(Structures.Part p) {
		return switch (p) {
			case SHELL -> new float[]{1f, 0.6f, 0.2f};
			case PORT -> new float[]{0.7f, 0.7f, 0.75f};
			case TUYERE -> new float[]{1f, 0.2f, 0.2f};
			case TOWER -> new float[]{0.3f, 0.5f, 1f};
			case GRAPHITE -> new float[]{0.25f, 0.25f, 0.28f};
			case CHANNEL -> new float[]{0.9f, 0.9f, 0.2f};
			case CONTROLLER -> new float[]{0.2f, 1f, 0.3f};
		};
	}

	private static void render(WorldRenderContext context) {
		Shown s = shown;
		var world = context.world();
		if (s == null || world == null) {
			return;
		}
		if (System.currentTimeMillis() > s.until() || !(world.getBlockState(s.controller()).getBlock() == s.template().kind().controllerBlock())) {
			shown = null;
			return;
		}
		Vec3d cam = context.camera().getPos();
		MatrixStack m = context.matrixStack() != null ? context.matrixStack() : new MatrixStack();
		VertexConsumerProvider.Immediate buffers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		var fill = buffers.getBuffer(RenderLayer.getDebugFilledBox());
		Structures.Template t = s.template();
		for (var e : t.parts().entrySet()) {
			if (e.getValue() == Structures.Part.CONTROLLER) {
				continue;
			}
			BlockPos p = t.toWorld(e.getKey(), s.controller(), s.facing());
			if (world.getBlockState(p).isOf(t.block(e.getValue()))) {
				continue;
			}
			float[] c = color(e.getValue());
			double x = p.getX() - cam.x, y = p.getY() - cam.y, z = p.getZ() - cam.z;
			WorldRenderer.renderFilledBox(m, fill, x + 0.1, y + 0.1, z + 0.1, x + 0.9, y + 0.9, z + 0.9, c[0], c[1], c[2], 0.3f);
		}
		buffers.draw(RenderLayer.getDebugFilledBox());
		var lines = buffers.getBuffer(RenderLayer.getLines());
		for (var e : t.parts().entrySet()) {
			if (e.getValue() == Structures.Part.CONTROLLER) {
				continue;
			}
			BlockPos p = t.toWorld(e.getKey(), s.controller(), s.facing());
			if (world.getBlockState(p).isOf(t.block(e.getValue()))) {
				continue;
			}
			float[] c = color(e.getValue());
			double x = p.getX() - cam.x, y = p.getY() - cam.y, z = p.getZ() - cam.z;
			WorldRenderer.drawBox(m, lines, x + 0.1, y + 0.1, z + 0.1, x + 0.9, y + 0.9, z + 0.9, c[0], c[1], c[2], 0.8f);
		}
		buffers.draw(RenderLayer.getLines());
	}
}
