package com.periodicelements.client;

import com.periodicelements.fluid.TankBlocks;
import com.periodicelements.fluid.TankGaugeBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import java.util.Locale;

/** 3.38.0: writes the Tank Gauge's reading on its dial: the percent in use and a bar of ten segments. */
public class TankGaugeRenderer implements BlockEntityRenderer<TankGaugeBlockEntity> {
	private static final int LIT = 0xFF66FF66, DARK = 0xFF1E3A1E, WARN = 0xFFFFB040, NONE = 0xFF888888;
	private final TextRenderer text;

	public TankGaugeRenderer(BlockEntityRendererFactory.Context ctx) {
		text = ctx.getTextRenderer();
	}

	@Override
	public void render(TankGaugeBlockEntity gauge, float tickDelta, MatrixStack m, VertexConsumerProvider vcp, int light, int overlay) {
		Direction facing = gauge.getCachedState().get(TankBlocks.Gauge.FACING);
		int pm = gauge.permille();
		m.push();
		m.translate(0.5, 0.5, 0.5);
		m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
		m.translate(0, 0, 0.5 + 0.002);
		float s = 1 / 64f;
		m.scale(s, -s, s);
		int full = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		String reading = pm < 0 ? "--" : pm >= 1000 ? "100%" : String.format(Locale.ROOT, pm < 100 ? "%.1f%%" : "%.0f%%", pm / 10.0);
		int color = pm < 0 ? NONE : pm >= 950 ? WARN : LIT;
		float big = 1.6f;
		m.push();
		m.scale(big, big, big);
		text.draw(reading, -text.getWidth(reading) / 2f, -9, color, false, m.peek().getPositionMatrix(), vcp,
				TextRenderer.TextLayerType.POLYGON_OFFSET, 0, full);
		m.pop();
		// The bar: ten segments, lit up to the reading.
		int lit = pm < 0 ? 0 : (int) Math.round(pm / 100.0);
		StringBuilder on = new StringBuilder(), off = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			(i < lit ? on : off).append('|');
		}
		String bar = "||||||||||";
		float x = -text.getWidth(bar) / 2f;
		text.draw(on.toString(), x, 6, color, false, m.peek().getPositionMatrix(), vcp, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, full);
		text.draw(off.toString(), x + text.getWidth(on.toString()), 6, DARK, false, m.peek().getPositionMatrix(), vcp,
				TextRenderer.TextLayerType.POLYGON_OFFSET, 0, full);
		m.pop();
	}
}
