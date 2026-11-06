package com.periodicelements.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A Field Guide chapter as an open book: two wide pages side by side, sized to the window (2.2.0; the vanilla book
 * screen was a single 114-pixel page). Arrow keys, Page Up/Down, the mouse wheel or the buttons turn the pages.
 */
public class GuideBookScreen extends Screen {
	private static final int LINE = 10;
	private static final int PAPER = 0xFFF4EBD3;
	private static final int EDGE = 0xFF8A5A2E;
	private static final int SPINE = 0xFFD9C9A3;
	private static final int INK = 0xFF2B2118;

	public record Paragraph(String text, boolean heading, boolean bullet) {
	}

	private final Screen parent;
	private final Text heading;
	private final List<Paragraph> paragraphs;
	/** Each page: its lines, each a Text, a figure (which fills the next lines), or SPACE under a figure. */
	private List<List<Object>> pages = List.of();
	private static final Object SPACE = new Object();
	/** Figures on the open spread and where they were drawn, for clicks and drags. */
	private final List<Object[]> shown = new ArrayList<>();
	private int spread;
	private int pageWidth;
	private int pageLines;
	private ButtonWidget back;
	private ButtonWidget next;

	/** 3.27.0: the words searched for, highlighted on the pages (empty when the book was not opened from a search). */
	private final List<String> find;
	private boolean jumped;

	public GuideBookScreen(Screen parent, Text heading, List<Paragraph> paragraphs) {
		this(parent, heading, paragraphs, null);
	}

	/** Opened from a search: starts at the first page with one of the words, and marks the lines that have them. */
	public GuideBookScreen(Screen parent, Text heading, List<Paragraph> paragraphs, @org.jetbrains.annotations.Nullable String query) {
		super(heading);
		this.parent = parent;
		this.heading = heading;
		this.paragraphs = paragraphs;
		this.find = query == null ? List.of() : SearchText.words(query);
	}

	/** True when some line holds every searched word: then only such lines are marked, else lines with any word. */
	private boolean wholeLines;

	private boolean marked(Text line) {
		if (find.isEmpty()) {
			return false;
		}
		String n = SearchText.normalize(line.getString());
		return wholeLines ? SearchText.all(n, find) : SearchText.any(n, find);
	}

	public int pageCount() {
		return pages.size();
	}

	public int spreads() {
		return Math.max(1, (pages.size() + 1) / 2);
	}

	public void setSpread(int s) {
		spread = Math.max(0, Math.min(spreads() - 1, s));
		updateButtons();
	}

	@Override
	protected void init() {
		pageWidth = Math.max(120, Math.min(200, (width - 72) / 2));
		pageLines = Math.max(8, (height - 80) / LINE);
		pages = paginate();
		if (!find.isEmpty()) {
			wholeLines = false;
			for (List<Object> page : pages) {
				for (Object o : page) {
					if (o instanceof Text t && SearchText.all(SearchText.normalize(t.getString()), find)) {
						wholeLines = true;
					}
				}
			}
		}
		if (!find.isEmpty() && !jumped) {
			jumped = true;
			// Skip the title line on page 1 so a hit in the chapter's name does not hide the first hit in the text.
			search:
			for (int i = 0; i < pages.size(); i++) {
				List<Object> page = pages.get(i);
				for (int k = i == 0 ? 1 : 0; k < page.size(); k++) {
					if (page.get(k) instanceof Text t && marked(t)) {
						spread = i / 2;
						break search;
					}
				}
			}
		}
		setSpread(spread);
		int y = top() + pageLines * LINE + 30;
		back = addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> setSpread(spread - 1))
				.dimensions(width / 2 - pageWidth - 20, y, 30, 20).build());
		next = addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> setSpread(spread + 1))
				.dimensions(width / 2 + pageWidth - 10, y, 30, 20).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(width / 2 - 50, y, 100, 20).build());
		updateButtons();
	}

	private int top() {
		return Math.max(8, (height - (pageLines * LINE + 54)) / 2);
	}

	private void updateButtons() {
		if (back != null) {
			back.active = spread > 0;
			next.active = spread < spreads() - 1;
		}
	}

	/** Wraps the paragraphs to the page width and cuts them into pages, keeping a heading with its text. */
	private List<List<Object>> paginate() {
		List<Object> lines = new ArrayList<>();
		wrap(lines, heading.getString(), new Formatting[]{Formatting.BOLD, Formatting.DARK_BLUE});
		lines.add(Text.empty());
		int captionStart = -1;
		for (Paragraph p : paragraphs) {
			if (p.heading()) {
				if (lines.size() % pageLines != 0 && !blank(lines.get(lines.size() - 1))) {
					lines.add(Text.empty());
				}
				List<Object> head = new ArrayList<>();
				wrap(head, p.text(), new Formatting[]{Formatting.BOLD});
				int left = pageLines - lines.size() % pageLines;
				if (left < head.size() + 2) {
					for (int k = 0; k < left; k++) {
						lines.add(Text.empty());
					}
				}
				lines.addAll(head);
			} else if (GuideFigures.parse(p.text(), pageWidth) instanceof GuideFigures.Figure fig) {
				// A figure stays whole on one page, with the short caption before it.
				int left = pageLines - lines.size() % pageLines;
				int pageStart = lines.size() - lines.size() % pageLines;
				boolean shortCaption = captionStart >= 0 && lines.size() - captionStart <= 4
						&& lines.size() - captionStart + fig.lines() <= pageLines;
				if (left < fig.lines() || (shortCaption && captionStart < pageStart)) {
					List<Object> caption = new ArrayList<>();
					if (shortCaption) {
						caption.addAll(lines.subList(captionStart, lines.size()));
						lines.subList(captionStart, lines.size()).clear();
						left = pageLines - lines.size() % pageLines;
					}
					for (int k = 0; k < left; k++) {
						lines.add(Text.empty());
					}
					lines.addAll(caption);
				}
				lines.add(fig);
				for (int k = 1; k < fig.lines(); k++) {
					lines.add(SPACE);
				}
				lines.add(Text.empty());
				captionStart = -1;
			} else if (p.bullet()) {
				captionStart = -1;
				wrap(lines, "• " + p.text(), new Formatting[0]);
			} else {
				captionStart = lines.size();
				wrap(lines, p.text(), new Formatting[0]);
				lines.add(Text.empty());
			}
		}
		while (!lines.isEmpty() && blank(lines.get(lines.size() - 1))) {
			lines.remove(lines.size() - 1);
		}
		List<List<Object>> out = new ArrayList<>();
		for (int i = 0; i < lines.size(); i += pageLines) {
			List<Object> page = new ArrayList<>(lines.subList(i, Math.min(lines.size(), i + pageLines)));
			// A page never starts with a blank line.
			while (!page.isEmpty() && blank(page.get(0))) {
				page.remove(0);
			}
			out.add(page);
		}
		return out;
	}

	private static boolean blank(Object line) {
		return line instanceof Text t && t.getString().isEmpty();
	}

	private void wrap(List<Object> out, String paragraph, Formatting[] style) {
		var font = MinecraftClient.getInstance().textRenderer;
		StringBuilder line = new StringBuilder();
		for (String word : paragraph.split(" ")) {
			String trial = line.isEmpty() ? word : line + " " + word;
			if (!line.isEmpty() && font.getWidth(Text.literal(trial).formatted(style)) > pageWidth) {
				out.add(Text.literal(line.toString()).formatted(style));
				line = new StringBuilder(word);
			} else {
				line = new StringBuilder(trial);
			}
		}
		if (!line.isEmpty()) {
			out.add(Text.literal(line.toString()).formatted(style));
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int top = top();
		int x0 = width / 2 - pageWidth - 20;
		int x1 = width / 2 + pageWidth + 20;
		int y1 = top + pageLines * LINE + 24;
		ctx.fill(x0 - 3, top - 3, x1 + 3, y1 + 3, EDGE);
		ctx.fill(x0, top, x1, y1, PAPER);
		ctx.fill(width / 2 - 1, top, width / 2 + 1, y1, SPINE);
		shown.clear();
		net.minecraft.item.ItemStack hovered = null;
		for (int side = 0; side < 2; side++) {
			int index = spread * 2 + side;
			if (index >= pages.size()) {
				break;
			}
			int x = side == 0 ? x0 + 10 : width / 2 + 10;
			int y = top + 8;
			for (Object line : pages.get(index)) {
				if (line instanceof Text text) {
					if (marked(text)) {
						ctx.fill(x - 2, y - 1, x + Math.min(pageWidth, textRenderer.getWidth(text) + 4), y + 9, 0x80FFD84A);
					}
					ctx.drawText(textRenderer, text, x, y, INK, false);
				} else if (line instanceof GuideFigures.Figure fig) {
					shown.add(new Object[]{fig, x, y});
					var h = fig.render(ctx, x, y, pageWidth, mouseX, mouseY);
					hovered = h != null ? h : hovered;
				}
				y += LINE;
			}
		}
		if (hovered != null && !hovered.isEmpty()) {
			ctx.drawItemTooltip(textRenderer, hovered, mouseX, mouseY);
		}
		int first = spread * 2 + 1;
		int last = Math.min(pages.size(), spread * 2 + 2);
		MutableText count = Text.translatable("gui.periodic_elements.guide.pages", first, last, pages.size());
		ctx.drawText(textRenderer, count, x1 - 10 - textRenderer.getWidth(count), y1 - 12, 0xFF8A7A60, false);
	}

	@Override
	public boolean keyPressed(int key, int scan, int mods) {
		if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_PAGE_DOWN) {
			setSpread(spread + 1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_PAGE_UP) {
			setSpread(spread - 1);
			return true;
		}
		return super.keyPressed(key, scan, mods);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		for (Object[] f : shown) {
			if (((GuideFigures.Figure) f[0]).click(mx, my, (int) f[1], (int) f[2], pageWidth)) {
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		for (Object[] f : shown) {
			if (((GuideFigures.Figure) f[0]).drag(mx, my, dx, dy, (int) f[1], (int) f[2], pageWidth)) {
				return true;
			}
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double h, double v) {
		setSpread(spread + (v < 0 ? 1 : -1));
		return true;
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
