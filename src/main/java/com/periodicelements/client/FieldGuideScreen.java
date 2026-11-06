package com.periodicelements.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * The Field Guide's contents page: one button per chapter, each opening the chapter as a book. Since 2.2.0 the guide
 * grows with the reader: it shows what belongs to the levels they have reached and the next one, never later eras
 * (in creative too), and a chapter shows only the paragraphs of those levels.
 */
public class FieldGuideScreen extends Screen {
	/** Dev screenshots only: show this level instead of the reader's own. Null in play. */
	public static Integer revealOverride;

	private record Chapter(String group, String title, ItemStack icon, List<String> text, List<Integer> levels, int level) {
	}

	/** A button on a contents page: one chapter, or a group of chapters (a level) with its own page. */
	private record Entry(String title, ItemStack icon, List<Chapter> chapters) {
	}

	/** At most this many search results are listed (the best first). */
	private static final int MAX_RESULTS = 20;

	private static List<Chapter> chapters;
	/** The game language the cached chapters were read for. */
	private static String chaptersLanguage;
	private final Screen parent;
	private final List<Entry> entries;
	/** 3.27.0: the search box of the top contents page (null on a level's page), kept across re-inits. */
	private net.minecraft.client.gui.widget.TextFieldWidget search;
	private String query = "";
	private List<Entry> results = List.of();

	/** The top contents page: chapters outside a group, and one button per group. */
	public FieldGuideScreen() {
		this(null, Text.translatable("gui.periodic_elements.guide.title"), topEntries());
	}

	private FieldGuideScreen(Screen parent, Text title, List<Entry> entries) {
		super(title);
		this.parent = parent;
		this.entries = entries;
	}

	/** The highest level the reader may read about: one past their tech level (the level they are working on). */
	public static int visibleLevel() {
		if (revealOverride != null) {
			return revealOverride;
		}
		return Math.min(com.periodicelements.research.Research.MAX_LEVEL,
				com.periodicelements.research.Research.techLevel(com.periodicelements.research.ClientResearch.known()) + 1);
	}

	/** The chapter cut to the visible levels; null if nothing but headings is left. */
	private static Chapter visible(Chapter c, int level) {
		if (c.level() > level) {
			return null;
		}
		List<String> text = new ArrayList<>();
		List<Integer> levels = new ArrayList<>();
		boolean body = false;
		for (int i = 0; i < c.text().size(); i++) {
			if (c.levels().get(i) <= level) {
				text.add(c.text().get(i));
				levels.add(c.levels().get(i));
				body |= !c.text().get(i).startsWith("# ");
			}
		}
		// Drop headings left with nothing under them.
		for (int i = text.size() - 1; i >= 0; i--) {
			boolean heading = text.get(i).startsWith("# ");
			boolean nextHeading = i + 1 >= text.size() || text.get(i + 1).startsWith("# ");
			if (heading && nextHeading) {
				text.remove(i);
				levels.remove(i);
			}
		}
		return body ? new Chapter(c.group(), c.title(), c.icon(), text, levels, c.level()) : null;
	}

	private static List<Entry> topEntries() {
		List<Entry> out = new ArrayList<>();
		java.util.Map<String, Entry> groups = new java.util.LinkedHashMap<>();
		int level = visibleLevel();
		for (Chapter full : chapters()) {
			Chapter c = visible(full, level);
			if (c == null) {
				continue;
			}
			if (c.group().isEmpty()) {
				out.add(new Entry(c.title(), c.icon(), List.of(c)));
			} else {
				Entry g = groups.get(c.group());
				if (g == null) {
					g = new Entry(c.group(), c.icon(), new ArrayList<>());
					groups.put(c.group(), g);
					out.add(g);
				}
				g.chapters().add(c);
			}
		}
		return out;
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(parent);
	}

	private static List<Chapter> chapters() {
		// Since 2.4.0 the guide follows the game language: guidebook_<lang>.json if there is one, else English.
		String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
		if (chapters == null || !language.equals(chaptersLanguage)) {
			List<Chapter> out = new ArrayList<>();
			String path = "/periodic_elements/guidebook_" + language + ".json";
			if (FieldGuideScreen.class.getResource(path) == null) {
				path = "/periodic_elements/guidebook.json";
			}
			JsonObject root = ElementTable.readObject(path);
			for (JsonElement e : root.getAsJsonArray("chapters")) {
				JsonObject o = e.getAsJsonObject();
				List<String> text = new ArrayList<>();
				o.getAsJsonArray("text").forEach(t -> text.add(t.getAsString()));
				List<Integer> levels = new ArrayList<>();
				o.getAsJsonArray("levels").forEach(t -> levels.add(t.getAsInt()));
				ItemStack icon = new ItemStack(Registries.ITEM.get(Identifier.of(o.get("icon").getAsString())));
				out.add(new Chapter(o.has("group") ? o.get("group").getAsString() : "", o.get("title").getAsString(), icon,
						text, levels, o.get("level").getAsInt()));
			}
			chapters = out;
			chaptersLanguage = language;
		}
		return chapters;
	}

	/** The buttons shown: the contents, or the search results while there is a query. */
	private List<Entry> shown() {
		return searching() ? results : entries;
	}

	private boolean searching() {
		return parent == null && !SearchText.words(query).isEmpty();
	}

	/** Top of the button grid; the top page keeps room above it for the search box. */
	private int gridTop(int rows) {
		return Math.max(parent == null ? 60 : 32, height / 2 - rows * 24 / 2);
	}

	@Override
	protected void init() {
		List<Entry> list = shown();
		int columns = 2;
		int w = 150;
		int h = 20;
		int rows = (list.size() + columns - 1) / columns;
		int left = width / 2 - w - 4;
		int top = gridTop(rows);
		for (int i = 0; i < list.size(); i++) {
			Entry c = list.get(i);
			int x = left + (i % columns) * (w + 8);
			int y = top + (i / columns) * (h + 4);
			String label = c.title();
			if (textRenderer.getWidth(label) > w - 28) {
				label = textRenderer.trimToWidth(label, w - 28 - textRenderer.getWidth("…")).stripTrailing() + "…";
			}
			ButtonWidget button = addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> open(c))
					.dimensions(x + 20, y, w - 20, h).build());
			if (!label.equals(c.title())) {
				button.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(c.title())));
			}
		}
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(width / 2 - 100, top + Math.max(rows, 1) * (h + 4) + 8, 200, 20).build());
		if (parent == null) {
			if (search == null) {
				search = new net.minecraft.client.gui.widget.TextFieldWidget(textRenderer, 0, 0, 200, 16,
						Text.translatable("gui.periodic_elements.search"));
				search.setMaxLength(40);
				search.setPlaceholder(Text.translatable("gui.periodic_elements.search.hint").formatted(Formatting.DARK_GRAY));
				search.setChangedListener(this::onSearch);
			}
			search.setDimensions(200, 16);
			search.setPosition(width / 2 - 100, top - 52);
			addDrawableChild(search);
		}
	}

	/** Dev screenshots: type a query into the search box (after the screen is open). */
	public void typeSearch(String text) {
		if (search != null) {
			search.setText(text);
		}
	}

	/** Dev screenshots: open the first search result. */
	public void openFirstResult() {
		if (!results.isEmpty()) open(results.get(0));
	}

	private void onSearch(String text) {
		if (text.equals(query)) {
			return;
		}
		query = text;
		results = find(query);
		clearAndInit();
		setFocused(search);
		search.setFocused(true);
	}

	/** Chapters (cut to the levels the reader may see) whose title or text holds every word, most hits first. */
	private static List<Entry> find(String query) {
		List<String> words = SearchText.words(query);
		if (words.isEmpty()) {
			return List.of();
		}
		int level = visibleLevel();
		List<Object[]> hits = new ArrayList<>();
		for (Chapter full : chapters()) {
			Chapter c = visible(full, level);
			if (c == null) {
				continue;
			}
			String title = c.group().isEmpty() ? c.title() : c.group() + ": " + c.title();
			String all = SearchText.normalize(title + "\n" + String.join("\n", c.text()));
			if (SearchText.all(all, words)) {
				int n = SearchText.count(all, words) + (SearchText.all(SearchText.normalize(title), words) ? 1000 : 0);
				hits.add(new Object[]{new Entry(title, c.icon(), List.of(c)), n});
			}
		}
		hits.sort((a, b) -> Integer.compare((int) b[1], (int) a[1]));
		List<Entry> out = new ArrayList<>();
		for (int i = 0; i < Math.min(MAX_RESULTS, hits.size()); i++) {
			out.add((Entry) hits.get(i)[0]);
		}
		return out;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		List<Entry> list = shown();
		int w = 150;
		int rows = (list.size() + 1) / 2;
		int left = width / 2 - w - 4;
		int top = gridTop(rows);
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, top - 24, 0xFFFFFF);
		Text pick = !searching() ? Text.translatable("gui.periodic_elements.guide.pick")
				: results.isEmpty() ? Text.translatable("gui.periodic_elements.guide.no_results")
				: Text.translatable("gui.periodic_elements.guide.results", results.size());
		ctx.drawCenteredTextWithShadow(textRenderer, pick.copy().formatted(Formatting.GRAY), width / 2, top - 12, 0xAAAAAA);
		if (parent == null && !searching()) {
			int level = visibleLevel();
			Text more = level >= com.periodicelements.research.Research.MAX_LEVEL
					? Text.translatable("gui.periodic_elements.guide.all")
					: Text.translatable("gui.periodic_elements.guide.more", level - 1, level + 1);
			ctx.drawCenteredTextWithShadow(textRenderer, more.copy().formatted(Formatting.GRAY), width / 2,
					top + rows * 24 + 34, 0xAAAAAA);
		}
		for (int i = 0; i < list.size(); i++) {
			ctx.drawItem(list.get(i).icon(), left + (i % 2) * (w + 8) + 1, top + (i / 2) * 24 + 2);
		}
	}

	private void open(Entry e) {
		if (e.chapters().size() == 1) {
			openChapter(e.chapters().get(0));
		} else {
			List<Entry> sub = new ArrayList<>();
			for (Chapter c : e.chapters()) {
				sub.add(new Entry(c.title(), c.icon(), List.of(c)));
			}
			MinecraftClient.getInstance().setScreen(new FieldGuideScreen(this, Text.literal(e.title()), sub));
		}
	}

	private void openChapter(Chapter c) {
		List<GuideBookScreen.Paragraph> ps = new ArrayList<>();
		for (String p : c.text()) {
			if (p.startsWith("# ")) {
				ps.add(new GuideBookScreen.Paragraph(p.substring(2), true, false));
			} else if (p.startsWith("- ")) {
				ps.add(new GuideBookScreen.Paragraph(p.substring(2), false, true));
			} else {
				ps.add(new GuideBookScreen.Paragraph(p, false, false));
			}
		}
		Text heading = Text.literal(c.group().isEmpty() ? c.title() : c.group() + ": " + c.title());
		MinecraftClient.getInstance().setScreen(new GuideBookScreen(this, heading, ps, searching() ? query : null));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
