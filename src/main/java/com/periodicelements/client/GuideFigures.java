package com.periodicelements.client;

import com.periodicelements.multiblock.Structures;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 3.3.0: pictures in the Field Guide. A paragraph that starts with "@grid", "@layers" or "@3d" is drawn instead of
 * written: a shaped recipe as a 3x3 grid of icons, a structure layer by layer seen from above, or a structure in 3D.
 */
public final class GuideFigures {
	static final int SLOT = 18;
	private static final int SLOT_BG = 0xFFE2D5B5;
	private static final int SLOT_EDGE = 0xFFB59A6A;
	private static final int INK = 0xFF2B2118;
	private static final int DIM = 0xFF8A7A60;
	private static final int BUTTON = 0xFFD9C9A3;

	private GuideFigures() {
	}

	/** A figure fills a whole number of 10-pixel lines on a page. */
	public interface Figure {
		int lines();

		/** Draws at (x, y); returns the item under the mouse, for its tooltip. */
		@Nullable
		ItemStack render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY);

		default boolean click(double mx, double my, int x, int y, int width) {
			return false;
		}

		default boolean drag(double mx, double my, double dx, double dy, int x, int y, int width) {
			return false;
		}
	}

	@Nullable
	public static Figure parse(String text, int width) {
		if (text.startsWith("@grid ")) {
			return Grid.parse(text.substring(6));
		}
		if (text.startsWith("@layers ")) {
			Structures.Kind k = kind(text.substring(8));
			return k == null ? null : new Layers(k, width);
		}
		if (text.startsWith("@3d ")) {
			Structures.Kind k = kind(text.substring(4));
			return k == null ? null : new View3d(k);
		}
		return null;
	}

	@Nullable
	private static Structures.Kind kind(String id) {
		for (Structures.Kind k : Structures.Kind.values()) {
			if (k.id().equals(id.trim())) {
				return k;
			}
		}
		return null;
	}

	private static Item item(String id) {
		Identifier i = Identifier.tryParse(id);
		return i == null ? Items.AIR : Registries.ITEM.get(i);
	}

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private static void slot(DrawContext ctx, int x, int y, int size) {
		ctx.fill(x, y, x + size, y + size, SLOT_EDGE);
		ctx.fill(x + 1, y + 1, x + size - 1, y + size - 1, SLOT_BG);
	}

	/** Draws an item icon at (x, y), size pixels square. */
	private static void icon(DrawContext ctx, ItemStack stack, int x, int y, int size) {
		if (size == 16) {
			ctx.drawItem(stack, x, y);
			return;
		}
		ctx.getMatrices().push();
		ctx.getMatrices().translate(x, y, 0);
		ctx.getMatrices().scale(size / 16f, size / 16f, 1);
		ctx.drawItem(stack, 0, 0);
		ctx.getMatrices().pop();
	}

	private static void button(DrawContext ctx, TextRenderer font, String label, int x, int y, int w, boolean active) {
		ctx.fill(x, y, x + w, y + 10, SLOT_EDGE);
		ctx.fill(x + 1, y + 1, x + w - 1, y + 9, active ? BUTTON : SLOT_BG);
		ctx.drawText(font, label, x + (w - font.getWidth(label)) / 2, y + 1, active ? INK : DIM, false);
	}

	// ---- Recipe grid. ----

	/** A shaped recipe: up to 3x3 cells (each a list of alternatives, cycled once a second) and the result. */
	public record Grid(List<List<Item>> cells, int cols, int rows, Item result, int count) implements Figure {
		static Grid parse(String spec) {
			String[] parts = spec.split("\\|", 2);
			String[] res = parts[0].split("\\*");
			List<List<Item>> cells = new ArrayList<>();
			String[] rows = parts.length > 1 ? parts[1].split("/", -1) : new String[0];
			int cols = 0;
			for (String row : rows) {
				String[] cs = row.split(",", -1);
				cols = Math.max(cols, cs.length);
			}
			for (String row : rows) {
				String[] cs = row.split(",", -1);
				for (int i = 0; i < cols; i++) {
					List<Item> opts = new ArrayList<>();
					if (i < cs.length && !cs[i].isEmpty()) {
						for (String o : cs[i].split("\\+")) {
							opts.add(item(o));
						}
					}
					cells.add(opts);
				}
			}
			return new Grid(cells, cols, rows.length, item(res[0]), res.length > 1 ? Integer.parseInt(res[1]) : 1);
		}

		@Override
		public int lines() {
			return 6;
		}

		@Override
		public ItemStack render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
			TextRenderer font = MinecraftClient.getInstance().textRenderer;
			ItemStack hovered = null;
			long tick = System.currentTimeMillis() / 1000;
			// Recipes narrower or shorter than 3x3 are drawn in the middle of the grid, as in a crafting table.
			int ox = (3 - cols) / 2, oy = (3 - rows) / 2;
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 3; c++) {
					int sx = x + c * SLOT, sy = y + r * SLOT;
					slot(ctx, sx, sy, SLOT);
					int cr = r - oy, cc = c - ox;
					if (cr < 0 || cc < 0 || cr >= rows || cc >= cols) {
						continue;
					}
					List<Item> opts = cells.get(cr * cols + cc);
					if (opts.isEmpty()) {
						continue;
					}
					ItemStack stack = new ItemStack(opts.get((int) (tick % opts.size())));
					ctx.drawItem(stack, sx + 1, sy + 1);
					if (inside(mouseX, mouseY, sx, sy, SLOT, SLOT)) {
						hovered = stack;
					}
				}
			}
			int ax = x + 3 * SLOT + 6, ay = y + SLOT + 5;
			ctx.drawText(font, "→", ax, ay, INK, false);
			int rx = ax + 14, ry = y + SLOT;
			slot(ctx, rx - 2, ry - 2, SLOT + 4);
			ItemStack out = new ItemStack(result, count);
			ctx.drawItem(out, rx + 1, ry + 1);
			ctx.drawItemInSlot(font, out, rx + 1, ry + 1);
			if (inside(mouseX, mouseY, rx - 2, ry - 2, SLOT + 4, SLOT + 4)) {
				hovered = out;
			}
			// The result's name beside it, wrapped.
			int tx = rx + SLOT + 8, tw = Math.max(20, width - (tx - x));
			var lines = font.wrapLines(out.getName(), tw);
			int ty = ry + SLOT / 2 - lines.size() * 9 / 2;
			for (var line : lines.subList(0, Math.min(4, lines.size()))) {
				ctx.drawText(font, line, tx, ty, INK, false);
				ty += 9;
			}
			return hovered;
		}
	}

	// ---- Layer by layer. ----

	private static ItemStack stackOf(Structures.Template t, Structures.Part p) {
		return new ItemStack(t.block(p).asItem());
	}

	/** The smallest structure seen from above, one layer at a time, with buttons to go up and down. */
	public static final class Layers implements Figure {
		private final Structures.Template t;
		private final int cell;
		private int layer;

		Layers(Structures.Kind kind, int width) {
			this.t = Structures.smallest(kind);
			this.cell = Math.max(8, Math.min(16, (width - 4) / t.sx()));
		}

		@Override
		public int lines() {
			return (14 + t.sz() * cell + 9) / 10;
		}

		@Override
		public ItemStack render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
			TextRenderer font = MinecraftClient.getInstance().textRenderer;
			Text label = layer == 0 ? Text.translatable("gui.periodic_elements.guide.floor")
					: Text.translatable("gui.periodic_elements.guide.layer", layer, t.sy() - 1);
			ctx.drawText(font, label, x, y + 1, INK, false);
			button(ctx, font, "▼", x + width - 26, y, 12, layer > 0);
			button(ctx, font, "▲", x + width - 12, y, 12, layer < t.sy() - 1);
			ItemStack hovered = null;
			int gy = y + 13;
			// The front wall (the controller's) is drawn at the bottom, nearest the reader.
			for (int z = 0; z < t.sz(); z++) {
				for (int xx = 0; xx < t.sx(); xx++) {
					int cx = x + xx * cell, cy = gy + (t.sz() - 1 - z) * cell;
					Structures.Part p = t.parts().get(new BlockPos(xx, layer, z));
					if (p == null) {
						ctx.fill(cx, cy, cx + cell, cy + cell, 0xFFEFE4C8);
						ctx.fill(cx + cell / 2 - 1, cy + cell / 2 - 1, cx + cell / 2 + 1, cy + cell / 2 + 1, 0xFFCDBB94);
						continue;
					}
					ItemStack s = stackOf(t, p);
					icon(ctx, s, cx, cy, cell);
					if (inside(mouseX, mouseY, cx, cy, cell, cell)) {
						hovered = s;
					}
				}
			}
			return hovered;
		}

		@Override
		public boolean click(double mx, double my, int x, int y, int width) {
			if (inside(mx, my, x + width - 26, y, 12, 10) && layer > 0) {
				layer--;
				return true;
			}
			if (inside(mx, my, x + width - 12, y, 12, 10) && layer < t.sy() - 1) {
				layer++;
				return true;
			}
			return false;
		}
	}

	// ---- 3D view. ----

	/** The structure drawn in perspective; drag to turn it, the button switches between smallest and largest. */
	public static final class View3d implements Figure {
		private static final int HEIGHT = 130;
		private final Structures.Kind kind;
		private boolean largest;
		private float yaw = 35;
		private long dragged;
		private long lastFrame;

		View3d(Structures.Kind kind) {
			this.kind = kind;
		}

		@Override
		public int lines() {
			return HEIGHT / 10 + 1;
		}

		private Structures.Template template() {
			return largest ? Structures.largest(kind) : Structures.smallest(kind);
		}

		private String toggleLabel() {
			return Text.translatable("gui.periodic_elements.guide." + (largest ? "smallest" : "largest")).getString();
		}

		@Override
		public ItemStack render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
			TextRenderer font = MinecraftClient.getInstance().textRenderer;
			ctx.fill(x, y, x + width, y + HEIGHT, 0xFFE9DDBF);
			String label = toggleLabel();
			int bw = font.getWidth(label) + 8;
			button(ctx, font, label, x + width - bw - 2, y + 2, bw, true);
			ctx.drawText(font, Text.translatable("gui.periodic_elements.guide.drag"), x + 3, y + HEIGHT - 10, DIM, false);
			long now = System.currentTimeMillis();
			if (now - dragged > 3000 && lastFrame > 0) {
				yaw += (now - lastFrame) / 100f; // a slow turn while nobody drags
			}
			lastFrame = now;
			Structures.Template t = template();
			float diag = (float) Math.sqrt(t.sx() * t.sx() + t.sz() * t.sz());
			float scale = Math.min((width - 16) / (diag + 1f), (HEIGHT - 26) / (t.sy() * 0.9f + diag * 0.5f + 1f));
			MatrixStack m = ctx.getMatrices();
			ctx.draw();
			ctx.enableScissor(x, y, x + width, y + HEIGHT);
			m.push();
			m.translate(x + width / 2f, y + HEIGHT / 2f + 4, 400);
			m.scale(scale, -scale, scale); // the GUI's y runs down: one flip, as for items
			m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30)); // seen from a little above
			m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw + 180)); // + 180: the controller's wall towards the reader
			m.translate(-t.sx() / 2f, -t.sy() / 2f, -t.sz() / 2f);
			DiffuseLighting.enableGuiDepthLighting();
			var brm = MinecraftClient.getInstance().getBlockRenderManager();
			var consumers = ctx.getVertexConsumers();
			for (var e : t.parts().entrySet()) {
				BlockPos p = e.getKey();
				BlockState s = t.block(e.getValue()).getDefaultState();
				if (s.contains(Properties.HORIZONTAL_FACING)) {
					s = s.with(Properties.HORIZONTAL_FACING, Direction.NORTH); // the controller faces the reader (-z)
				}
				m.push();
				m.translate(p.getX(), p.getY(), p.getZ());
				brm.renderBlockAsEntity(s, m, consumers, 0xF000F0, OverlayTexture.DEFAULT_UV);
				m.pop();
			}
			ctx.draw();
			m.pop();
			ctx.disableScissor();
			DiffuseLighting.disableGuiDepthLighting();
			return null;
		}

		@Override
		public boolean click(double mx, double my, int x, int y, int width) {
			TextRenderer font = MinecraftClient.getInstance().textRenderer;
			int bw = font.getWidth(toggleLabel()) + 8;
			if (inside(mx, my, x + width - bw - 2, y + 2, bw, 10)) {
				largest = !largest;
				return true;
			}
			return inside(mx, my, x, y, width, HEIGHT);
		}

		@Override
		public boolean drag(double mx, double my, double dx, double dy, int x, int y, int width) {
			if (!inside(mx, my, x, y, width, HEIGHT)) {
				return false;
			}
			yaw += (float) dx * 2;
			dragged = System.currentTimeMillis();
			return true;
		}
	}
}
