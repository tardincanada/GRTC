package com.periodicelements.client;

import net.minecraft.client.gui.DrawContext;

/** Flat vanilla-style panels and slots drawn with fills, so the screens need no texture files. */
final class Gui {
	static final int TEXT = 0x404040;
	static final int TEXT_DIM = 0x707070;

	private Gui() {
	}

	static void panel(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, 0xFF000000);
		ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6);
		ctx.fill(x + 1, y + 1, x + w - 2, y + 2, 0xFFFFFFFF);
		ctx.fill(x + 1, y + 1, x + 2, y + h - 2, 0xFFFFFFFF);
		ctx.fill(x + 2, y + h - 2, x + w - 1, y + h - 1, 0xFF555555);
		ctx.fill(x + w - 2, y + 2, x + w - 1, y + h - 1, 0xFF555555);
	}

	/** An 18x18 slot frame whose item area starts at (x, y). */
	static void slot(DrawContext ctx, int x, int y) {
		inset(ctx, x - 1, y - 1, 18, 18, 0xFF8B8B8B);
	}

	static void inset(DrawContext ctx, int x, int y, int w, int h, int fill) {
		ctx.fill(x, y, x + w, y + h, 0xFF373737);
		ctx.fill(x + 1, y + 1, x + w, y + h, 0xFFFFFFFF);
		ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
	}

	static void bar(DrawContext ctx, int x, int y, int w, int h, double fraction, int color) {
		inset(ctx, x, y, w, h, 0xFF2A2A2A);
		int fw = (int) Math.round((w - 2) * Math.max(0, Math.min(1, fraction)));
		if (fw > 0) {
			ctx.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, color);
		}
	}

	/** Colour of something glowing at this temperature, from dull red to white. */
	static int heatColor(double c) {
		if (c < 400) {
			return 0xFF6A6A7A;
		}
		double t = Math.min(1, (c - 400) / 1100);
		int r = 0xFF;
		int g = (int) (40 + 200 * t);
		int b = (int) (20 + 120 * Math.max(0, t - 0.6) / 0.4);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
