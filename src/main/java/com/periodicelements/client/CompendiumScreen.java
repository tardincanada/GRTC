package com.periodicelements.client;

import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasPayloads;
import com.periodicelements.compendium.Compendium;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.research.ClientResearch;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Tree or Plant Compendium as an open book: the biomes with entries of the book's kind on the left page (with how
 * many of their trees or plants are noted), the chosen biome or entry on the right. Only what the reader has noted is
 * named; research adds paragraphs.
 */
public class CompendiumScreen extends Screen {
	private static final int LINE = 10;
	private static final int PAPER = 0xFFEFE8D2;
	private static final int EDGE = 0xFF2E5C34;
	private static final int SPINE = 0xFFD2C8A8;
	private static final int INK = 0xFF2B2118;
	private static final int FAINT = 0xFF8A7A60;
	private static final int LINK = 0xFF2E6B2A;
	private static final int SELECTED = 0x302E6B2A;

	/** A line on the right page: text, and what clicking it opens (a tree id, a biome id, or nothing). */
	private record Line(Text text, int color, String tree, String biome) {
	}

	/** Dev screenshots: open on this biome or tree. */
	public static String startBiome, startTree;

	private final String kind;
	private final boolean plants, minerals, animals;
	/** Mineral Compendium: groups by class once Theory of the Earth is known. */
	private final boolean byClass;
	private final List<CompendiumData.Biome> biomes;
	private int pageWidth, pageLines, top;
	private int leftScroll;
	private String biome, tree;
	private List<List<Line>> pages = List.of();
	private int page;
	private ButtonWidget back, prev, next, mark;
	/** 3.27.0: the search box over the left page, and its query (results replace the right page while it has one). */
	private net.minecraft.client.gui.widget.TextFieldWidget search;
	private String query = "";
	private final List<int[]> rightHits = new ArrayList<>();
	private final List<Line> rightHitLines = new ArrayList<>();

	public CompendiumScreen() {
		this(CompendiumData.TREE);
	}

	public CompendiumScreen(String kind) {
		super(Text.translatable(CompendiumData.PLANT.equals(kind) ? "gui.periodic_elements.compendium.plant_title"
				: CompendiumData.MINERAL.equals(kind) ? "gui.periodic_elements.compendium.mineral_title"
				: CompendiumData.ANIMAL.equals(kind) ? "gui.periodic_elements.compendium.animal_title"
				: "gui.periodic_elements.compendium.title"));
		this.kind = kind;
		this.plants = CompendiumData.PLANT.equals(kind);
		this.minerals = CompendiumData.MINERAL.equals(kind);
		this.animals = CompendiumData.ANIMAL.equals(kind);
		this.byClass = minerals && Compendium.has(ClientResearch.known(), "origin");
		if (minerals) {
			// The left page lists groups (or classes), not biomes: groups are always "seen".
			List<CompendiumData.Biome> groups = new ArrayList<>();
			for (String g : byClass ? CompendiumData.CLASS_GROUPS : CompendiumData.GROUPS) groups.add(new CompendiumData.Biome(g, null, null));
			this.biomes = groups;
		} else {
			this.biomes = CompendiumData.biomes(kind);
		}
		biome = startBiome;
		tree = startTree;
	}

	@Override
	protected void init() {
		pageWidth = Math.max(120, Math.min(200, (width - 72) / 2));
		pageLines = Math.max(10, (height - 80) / LINE);
		top = Math.max(8, (height - (pageLines * LINE + 54)) / 2);
		int y = top + pageLines * LINE + 30;
		prev = addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> turn(-1))
				.dimensions(width / 2 + 10, y, 20, 20).build());
		next = addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> turn(1))
				.dimensions(width / 2 + pageWidth - 10, y, 20, 20).build());
		back = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.periodic_elements.compendium.back"), b -> goBack())
				.dimensions(width / 2 - pageWidth - 20, y, 50, 20).build());
		mark = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.periodic_elements.compendium.mark"), b -> markOnAtlas())
				.dimensions(width / 2 + 34, y, pageWidth - 48, 20).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(width / 2 - 90, y, 80, 20).build());
		if (search == null) {
			search = new net.minecraft.client.gui.widget.TextFieldWidget(textRenderer, 0, 0, 100, 14,
					Text.translatable("gui.periodic_elements.search"));
			search.setMaxLength(40);
			search.setPlaceholder(Text.translatable("gui.periodic_elements.search.hint").formatted(Formatting.DARK_GRAY));
			search.setChangedListener(this::onSearch);
		}
		int x0 = width / 2 - pageWidth - 20;
		search.setDimensions(pageWidth - 4, 14);
		search.setPosition(x0 + 8, top + 4);
		addDrawableChild(search);
		rebuild();
	}

	private boolean searching() {
		return !SearchText.words(query).isEmpty();
	}

	/** Dev screenshots: type a query into the search box (after the screen is open). */
	public void typeSearch(String text) {
		if (search != null) {
			search.setText(text);
		}
	}

	/** Dev screenshots: open the first search result. */
	public void openFirstResult() {
		for (Line l : pages.get(0)) {
			if (l.tree() != null) {
				tree = l.tree();
				page = 0;
				rebuild();
				return;
			}
		}
	}

	private void onSearch(String text) {
		if (text.equals(query)) return;
		query = text;
		tree = null;
		page = 0;
		rebuild();
	}

	/**
	 * Search results: the noted entries whose page, as this reader sees it now (their research), holds every word,
	 * and the biomes or groups whose name does. Entries not noted yet are never searched, so nothing is spoiled.
	 */
	private List<Line> searchLines() {
		List<Line> out = new ArrayList<>();
		List<String> words = SearchText.words(query);
		heading(out, Text.translatable("gui.periodic_elements.compendium.results", query.trim()));
		int found = 0;
		for (CompendiumData.Biome b : biomes) {
			boolean seen = minerals || CompendiumClient.knows(b.key());
			if (!seen) continue;
			Text name = minerals ? Text.translatable("compendium.periodic_elements.group." + b.id())
					: Text.translatable(Compendium.biomeKey(b.id()));
			if (SearchText.all(SearchText.normalize(name.getString()), words)) {
				out.add(new Line(Text.literal("§ ").append(name), LINK, null, b.id()));
				found++;
			}
		}
		List<Object[]> hits = new ArrayList<>();
		for (CompendiumData.Tree t : CompendiumData.entries(kind)) {
			if (!CompendiumClient.knows(t.key())) continue;
			StringBuilder sb = new StringBuilder();
			for (Line l : treeLines(t)) sb.append(l.text().getString()).append('\n');
			String all = SearchText.normalize(sb.toString());
			if (SearchText.all(all, words)) {
				String name = Text.translatable(t.nameKey()).getString();
				int score = SearchText.count(all, words) + (SearchText.all(SearchText.normalize(name), words) ? 1000 : 0);
				hits.add(new Object[]{t, score});
			}
		}
		hits.sort((a, b) -> Integer.compare((int) b[1], (int) a[1]));
		for (Object[] h : hits) {
			CompendiumData.Tree t = (CompendiumData.Tree) h[0];
			out.add(new Line(Text.literal("• ").append(Text.translatable(t.nameKey())), LINK, t.id(), null));
			found++;
		}
		if (found == 0) {
			para(out, Text.translatable("gui.periodic_elements.compendium.no_results").getString(), FAINT);
		}
		return out;
	}

	private void rebuild() {
		pages = paginate(tree != null ? treeLines(CompendiumData.byId(kind, tree))
				: searching() ? searchLines() : biome != null ? biomeLines(biome) : homeLines());
		page = Math.min(page, pages.size() - 1);
		prev.active = page > 0;
		next.active = page < pages.size() - 1;
		prev.visible = next.visible = pages.size() > 1;
		back.visible = biome != null || tree != null || searching();
		Set<String> known = ClientResearch.known();
		mark.visible = tree != null && CompendiumClient.knows(kind + ":" + tree) && Atlas.canDraw(known) && Atlas.has(known, "compass");
	}

	private void turn(int d) {
		page = Math.max(0, Math.min(pages.size() - 1, page + d));
		rebuild();
	}

	private void goBack() {
		if (tree != null) {
			tree = null;
		} else if (searching()) {
			search.setText("");
		} else {
			biome = null;
		}
		page = 0;
		rebuild();
	}

	private void markOnAtlas() {
		Long pos = CompendiumClient.notes().get(kind + ":" + tree);
		if (pos == null) return;
		BlockPos p = BlockPos.fromLong(pos);
		String name = Text.translatable(kind + ".periodic_elements." + tree).getString();
		ClientPlayNetworking.send(new AtlasPayloads.EditMarker(true, name, p.getX(), p.getZ()));
		mark.active = false;
	}

	// ---- Right page contents ----

	private List<Line> homeLines() {
		List<Line> out = new ArrayList<>();
		heading(out, title);
		if (minerals) {
			int noted = 0, analysed = 0;
			for (CompendiumData.Tree t : CompendiumData.MINERALS) {
				if (CompendiumClient.knows(t.key())) noted++;
				if (CompendiumClient.knows(CompendiumData.analysedKey(t))) analysed++;
			}
			para(out, Text.translatable("gui.periodic_elements.compendium.mineral_summary", noted, CompendiumData.MINERALS.size(),
					analysed).getString(), INK);
			para(out, Text.translatable("gui.periodic_elements.compendium.mineral_help").getString(), FAINT);
			return out;
		}
		int entries = 0, seen = 0;
		List<CompendiumData.Tree> all = CompendiumData.entries(kind);
		for (CompendiumData.Tree t : all) if (CompendiumClient.knows(t.key())) entries++;
		for (CompendiumData.Biome b : biomes) if (CompendiumClient.knows(b.key())) seen++;
		para(out, Text.translatable(animals ? "gui.periodic_elements.compendium.animal_summary" : plants ? "gui.periodic_elements.compendium.plant_summary" : "gui.periodic_elements.compendium.summary",
				entries, all.size(), seen, biomes.size()).getString(), INK);
		para(out, Text.translatable(animals ? "gui.periodic_elements.compendium.animal_help" : plants ? "gui.periodic_elements.compendium.plant_help" : "gui.periodic_elements.compendium.help").getString(), FAINT);
		return out;
	}

	private List<Line> groupLines(String g) {
		List<Line> out = new ArrayList<>();
		heading(out, Text.translatable("compendium.periodic_elements.group." + g));
		para(out, Text.translatable("compendium.periodic_elements.group." + g + ".about").getString(), INK);
		out.add(new Line(Text.translatable("gui.periodic_elements.compendium.rocks_here").formatted(Formatting.BOLD), INK, null, null));
		for (CompendiumData.Tree t : CompendiumData.mineralsIn(g, byClass)) {
			if (CompendiumClient.knows(t.key())) {
				out.add(new Line(Text.literal("• ").append(Text.translatable(t.nameKey())), LINK, t.id(), null));
			} else {
				out.add(new Line(Text.literal("• ").append(Text.translatable("gui.periodic_elements.compendium.unseen")), FAINT, null, null));
			}
		}
		return out;
	}

	private List<Line> mineralLines(CompendiumData.Tree t) {
		List<Line> out = new ArrayList<>();
		Set<String> known = ClientResearch.known();
		CompendiumData.Mineral m = CompendiumData.MINERAL_INFO.get(t.id());
		out.add(new Line(Text.empty(), INK, null, null));  // room for the icon
		heading(out, Text.translatable(t.nameKey()));
		if (!m.real()) para(out, Text.translatable("gui.periodic_elements.compendium.other_world").getString(), FAINT);
		para(out, Text.translatable(t.textKey("about")).getString(), INK);
		if (m.cls() != null && m.origin() && Compendium.has(known, "origin")) {
			para(out, Text.translatable("gui.periodic_elements.compendium.class",
					Text.translatable("compendium.periodic_elements.group." + m.cls())).getString() + " "
					+ Text.translatable(t.textKey("origin")).getString(), INK);
		}
		if (m.made() && Compendium.has(known, "petrography")) {
			para(out, Text.translatable("gui.periodic_elements.compendium.minerals", Text.translatable(t.textKey("made"))).getString(), INK);
		}
		if (m.mineral() && Compendium.has(known, "ores")) {
			para(out, Text.translatable("gui.periodic_elements.compendium.ore_mineral", Text.translatable(t.textKey("mineral"))).getString(), INK);
			if (m.depth() != null) {
				para(out, Text.translatable("gui.periodic_elements.compendium.ore_depth", m.depth()[0], m.depth()[1]).getString(), INK);
			}
			if (!t.products().isEmpty()) {
				List<String> gives = new ArrayList<>();
				for (String pr : t.products()) gives.add(Registries.ITEM.get(Identifier.of(pr)).getName().getString());
				para(out, Text.translatable("gui.periodic_elements.compendium.gives", String.join(", ", gives)).getString(), INK);
			}
		}
		if (m.system() != null && Compendium.has(known, "crystals")) {
			String line = (m.formula() != null ? Text.translatable("gui.periodic_elements.compendium.formula", subscripts(m.formula())).getString() + " " : "")
					+ Text.translatable("gui.periodic_elements.compendium.system",
					Text.translatable("compendium.periodic_elements.system." + m.system())).getString();
			para(out, line, INK);
		}
		// Composition: only once analysed with the Lens, and only the elements the reader knows.
		if (!CompendiumClient.knows(CompendiumData.analysedKey(t))) {
			para(out, Text.translatable("gui.periodic_elements.compendium.not_analysed").getString(), FAINT);
		} else {
			com.periodicelements.BlockComposition.Entry entry = com.periodicelements.BlockComposition.of(t.blocks().get(0));
			if (entry == null) {
				para(out, Text.translatable("gui.periodic_elements.compendium.no_composition").getString(), FAINT);
			} else {
				Lens.View view = Lens.view(entry, known);
				out.add(new Line(Text.translatable("gui.periodic_elements.compendium.composition").formatted(Formatting.BOLD), INK, null, null));
				double rest = view.other();
				for (int i = 0; i < view.known().size(); i++) {
					Lens.Line l = view.known().get(i);
					if (i < 8) {
						out.add(new Line(Text.literal(percent(l.units(), view.total()) + " ")
								.append(Text.translatable("item.periodic_elements." + l.element())), INK, null, null));
					} else {
						rest += l.units();
					}
				}
				if (rest / view.total() >= 0.0005) {
					for (Text line : wrap(Text.translatable("gui.periodic_elements.compendium.other_elements", percent(rest, view.total())).getString(), Style.EMPTY)) {
						out.add(new Line(line, FAINT, null, null));
					}
				}
				out.add(new Line(Text.empty(), INK, null, null));
			}
		}
		Long pos = CompendiumClient.notes().get(t.key());
		if (pos != null) {
			BlockPos p = BlockPos.fromLong(pos);
			para(out, Text.translatable("gui.periodic_elements.compendium.depth", p.getY()).getString(), FAINT);
			if (Atlas.has(known, "compass")) {
				para(out, Text.translatable("gui.periodic_elements.compendium.first_seen", p.getX(), p.getZ()).getString(), FAINT);
			}
		}
		if (!Compendium.has(known, "ores") || !Compendium.has(known, "origin")) {
			para(out, Text.translatable("gui.periodic_elements.compendium.mineral_hint").getString(), FAINT);
		}
		return out;
	}

	/** Digits of a formula as subscripts: Fe2O3 to Fe₂O₃ (a digit after "·" stays a coefficient). */
	private static String subscripts(String formula) {
		StringBuilder b = new StringBuilder();
		boolean lead = true;
		for (char c : formula.toCharArray()) {
			if (Character.isDigit(c) && !lead) b.append((char) ('\u2080' + (c - '0')));
			else b.append(c);
			lead = c == '\u00b7' || c == ' ' || (lead && Character.isDigit(c));
		}
		return b.toString();
	}

	private static String percent(double part, double total) {
		double v = 100 * part / total;
		return v >= 10 ? String.format(java.util.Locale.ROOT, "%.0f%%", v)
				: v >= 0.1 ? String.format(java.util.Locale.ROOT, "%.1f%%", v) : "<0.1%";
	}

	private List<Line> biomeLines(String b) {
		if (minerals) return groupLines(b);
		List<Line> out = new ArrayList<>();
		// An unvisited biome keeps the "???" it has in the list.
		heading(out, CompendiumClient.knows("biome:" + b) ? Text.translatable(Compendium.biomeKey(b))
				: Text.translatable("gui.periodic_elements.compendium.unseen"));
		CompendiumData.Biome bio = CompendiumData.BIOME_BY_ID.get(b);
		if (CompendiumClient.knows("biome:" + b)) {
			para(out, Text.translatable("compendium.periodic_elements.biome." + b + ".about").getString(), INK);
			if (bio != null && bio.temperature() != null && Compendium.has(ClientResearch.known(), "climate")) {
				para(out, Text.translatable("gui.periodic_elements.compendium.climate", bio.temperature(), bio.rain()).getString(), INK);
			}
		} else {
			para(out, Text.translatable("gui.periodic_elements.compendium.not_visited").getString(), FAINT);
		}
		out.add(new Line(Text.translatable(animals ? "gui.periodic_elements.compendium.animals_here" : plants ? "gui.periodic_elements.compendium.plants_here"
				: "gui.periodic_elements.compendium.trees_here").formatted(Formatting.BOLD), INK, null, null));
		for (CompendiumData.Tree t : CompendiumData.of(kind, b)) {
			if (CompendiumClient.knows(t.key())) {
				out.add(new Line(Text.literal("• ").append(Text.translatable(t.nameKey())), LINK, t.id(), null));
			} else {
				out.add(new Line(Text.literal("• ").append(Text.translatable("gui.periodic_elements.compendium.unseen")), FAINT, null, null));
			}
		}
		return out;
	}

	private List<Line> treeLines(CompendiumData.Tree t) {
		if (minerals) return mineralLines(t);
		List<Line> out = new ArrayList<>();
		Set<String> known = ClientResearch.known();
		boolean classified = Compendium.has(known, "classification");
		out.add(new Line(Text.empty(), INK, null, null));  // room for the icon
		heading(out, Text.translatable(t.nameKey()));
		if (!t.real()) {
			para(out, Text.translatable(animals ? "gui.periodic_elements.compendium.animal_this_world" : plants ? "gui.periodic_elements.compendium.plant_this_world"
					: "gui.periodic_elements.compendium.this_world").getString(), FAINT);
		} else if (classified) {
			for (Text line : wrap(t.scientific(), Style.EMPTY.withItalic(true))) {
				out.add(new Line(line, INK, null, null));
			}
			para(out, Text.translatable(animals ? "gui.periodic_elements.compendium.animal_family" : "gui.periodic_elements.compendium.family",
					t.family()).getString(), INK);
		}
		para(out, Text.translatable(t.textKey("about")).getString(), INK);
		String height = t.height() < 1 ? String.valueOf(t.height()) : t.height() == Math.floor(t.height())
				? String.valueOf((int) t.height()) : String.valueOf(t.height());
		para(out, Text.translatable(animals ? "gui.periodic_elements.compendium.length" : "gui.periodic_elements.compendium.height",
				height).getString(), INK);
		List<String> where = new ArrayList<>();
		for (String b : t.biomes()) where.add(Text.translatable(Compendium.biomeKey(b)).getString());
		para(out, Text.translatable(animals ? "gui.periodic_elements.compendium.lives" : "gui.periodic_elements.compendium.grows",
				String.join(", ", where)).getString(), INK);
		if (!t.products().isEmpty()) {
			List<String> gives = new ArrayList<>();
			for (String p : t.products()) {
				if (!Registries.ITEM.containsId(Identifier.of(p))) continue;
				gives.add(Registries.ITEM.get(Identifier.of(p)).getName().getString());
			}
			para(out, Text.translatable("gui.periodic_elements.compendium.gives", String.join(", ", gives)).getString(), INK);
		}
		if (animals && Compendium.has(known, "evolution")) {
			para(out, Text.translatable("gui.periodic_elements.compendium.evolution", Text.translatable(t.textKey("evolution"))).getString(), INK);
		}
		if (t.real() && classified && !animals) {
			para(out, Text.translatable("gui.periodic_elements.compendium.range",
					Text.translatable(t.textKey("range"))).getString(), INK);
		}
		if (t.real() && t.iucn() != null && Compendium.has(known, "conservation")) {
			para(out, Text.translatable("gui.periodic_elements.compendium.status",
					Text.translatable("gui.periodic_elements.compendium.iucn." + t.iucn().toLowerCase())).getString(), INK);
		}
		Long pos = CompendiumClient.notes().get(t.key());
		if (pos != null && Atlas.has(known, "compass")) {
			BlockPos p = BlockPos.fromLong(pos);
			para(out, Text.translatable("gui.periodic_elements.compendium.first_seen", p.getX(), p.getZ()).getString(), FAINT);
		}
		if (animals && (!classified || !Compendium.has(known, "evolution"))) {
			para(out, Text.translatable("gui.periodic_elements.compendium.animal_hint").getString(), FAINT);
		} else if (t.real() && !classified && !animals) {
			para(out, Text.translatable("gui.periodic_elements.compendium.hint").getString(), FAINT);
		}
		return out;
	}

	private void heading(List<Line> out, Text text) {
		for (Text line : wrap(text.getString(), Style.EMPTY.withBold(true).withColor(Formatting.DARK_GREEN))) {
			out.add(new Line(line, INK, null, null));
		}
		out.add(new Line(Text.empty(), INK, null, null));
	}

	private void para(List<Line> out, String text, int color) {
		for (Text line : wrap(text, Style.EMPTY)) {
			out.add(new Line(line, color, null, null));
		}
		out.add(new Line(Text.empty(), INK, null, null));
	}

	private List<Text> wrap(String paragraph, Style style) {
		List<Text> out = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		int max = pageWidth - 12;
		for (String word : paragraph.split(" ")) {
			String trial = line.isEmpty() ? word : line + " " + word;
			if (!line.isEmpty() && textRenderer.getWidth(Text.literal(trial).setStyle(style)) > max) {
				out.add(Text.literal(line.toString()).setStyle(style));
				line = new StringBuilder(word);
			} else {
				line = new StringBuilder(trial);
			}
		}
		if (!line.isEmpty()) out.add(Text.literal(line.toString()).setStyle(style));
		return out;
	}

	private List<List<Line>> paginate(List<Line> lines) {
		while (!lines.isEmpty() && lines.get(lines.size() - 1).text().getString().isEmpty()) {
			lines.remove(lines.size() - 1);
		}
		List<List<Line>> out = new ArrayList<>();
		for (int i = 0; i < lines.size(); i += pageLines) {
			List<Line> p = new ArrayList<>(lines.subList(i, Math.min(lines.size(), i + pageLines)));
			while (!p.isEmpty() && p.get(0).text().getString().isEmpty() && i > 0) p.remove(0);
			out.add(p);
		}
		if (out.isEmpty()) out.add(List.of());
		return out;
	}

	// ---- Left page: the biomes ----

	private int leftRows() {
		return pageLines - 2;
	}

	private String countOf(String b) {
		int all = 0, noted = 0;
		for (CompendiumData.Tree t : minerals ? CompendiumData.mineralsIn(b, byClass) : CompendiumData.of(kind, b)) {
			all++;
			if (CompendiumClient.knows(t.key())) noted++;
		}
		return noted + "/" + all;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int x0 = width / 2 - pageWidth - 20;
		int x1 = width / 2 + pageWidth + 20;
		int y1 = top + pageLines * LINE + 24;
		ctx.fill(x0 - 3, top - 3, x1 + 3, y1 + 3, EDGE);
		ctx.fill(x0, top, x1, y1, PAPER);
		ctx.fill(width / 2 - 1, top, width / 2 + 1, y1, SPINE);
		// Left page.
		int lx = x0 + 10, y = top + 8;
		// 3.27.0: the search box sits where the "Biomes" / "Groups" heading was.
		y += 2 * LINE;
		List<CompendiumData.Biome> bs = biomes;
		int rows = leftRows();
		for (int i = leftScroll; i < Math.min(bs.size(), leftScroll + rows); i++) {
			CompendiumData.Biome b = bs.get(i);
			boolean seen = minerals || CompendiumClient.knows(b.key());
			Text name = minerals ? Text.translatable("compendium.periodic_elements.group." + b.id())
					: seen ? Text.translatable(Compendium.biomeKey(b.id()))
					: Text.translatable("gui.periodic_elements.compendium.unseen");
			if (b.id().equals(biome)) ctx.fill(lx - 2, y - 1, width / 2 - 8, y + 9, SELECTED);
			String count = countOf(b.id());
			int cw = textRenderer.getWidth(count);
			int room = width / 2 - 14 - lx - cw - 6;
			String shown = name.getString();
			if (textRenderer.getWidth(shown) > room) {
				shown = textRenderer.trimToWidth(shown, room - textRenderer.getWidth("…")).stripTrailing() + "…";
			}
			ctx.drawText(textRenderer, shown, lx, y, seen ? LINK : FAINT, false);
			ctx.drawText(textRenderer, count, width / 2 - 12 - cw, y, FAINT, false);
			y += LINE;
		}
		if (leftScroll > 0) ctx.drawText(textRenderer, "▲", width / 2 - 16, top + 20, FAINT, false);
		if (leftScroll + rows < bs.size()) ctx.drawText(textRenderer, "▼", width / 2 - 16, y1 - 12, FAINT, false);
		// Right page.
		rightHits.clear();
		rightHitLines.clear();
		int rx = width / 2 + 10;
		y = top + 8;
		if (tree != null && page == 0) {
			CompendiumData.Tree t = CompendiumData.byId(kind, tree);
			ItemStack icon = new ItemStack(Registries.ITEM.get(Identifier.of(t.icon())));
			ctx.getMatrices().push();
			ctx.getMatrices().translate(x1 - 44, top + 6, 0);
			ctx.getMatrices().scale(2f, 2f, 1f);
			ctx.drawItem(icon, 0, 0);
			ctx.getMatrices().pop();
		}
		for (Line line : pages.get(page)) {
			ctx.drawText(textRenderer, line.text(), rx, y, line.color(), false);
			if (line.tree() != null || line.biome() != null) {
				rightHits.add(new int[]{rx, y, rx + textRenderer.getWidth(line.text()), y + 9});
				rightHitLines.add(line);
				if (mouseX >= rx && mouseX < rx + textRenderer.getWidth(line.text()) && mouseY >= y && mouseY < y + 9) {
					ctx.fill(rx, y + 8, rx + textRenderer.getWidth(line.text()), y + 9, LINK);
				}
			}
			y += LINE;
		}
		if (pages.size() > 1) {
			String count = (page + 1) + "/" + pages.size();
			ctx.drawText(textRenderer, count, x1 - 10 - textRenderer.getWidth(count), y1 - 12, FAINT, false);
		}
		// The search box sits on the paper, which was drawn over it: draw it again on top.
		if (search != null) search.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (super.mouseClicked(mx, my, button)) return true;
		int x0 = width / 2 - pageWidth - 20;
		int lx = x0 + 10;
		int y = top + 8 + 2 * LINE;
		if (mx >= lx - 2 && mx < width / 2 - 8 && my >= y) {
			int i = leftScroll + (int) ((my - y) / LINE);
			if (i < biomes.size() && i < leftScroll + leftRows()) {
				if (searching()) {
					query = "";
					search.setText("");
				}
				biome = biomes.get(i).id();
				tree = null;
				page = 0;
				rebuild();
				return true;
			}
		}
		for (int k = 0; k < rightHits.size(); k++) {
			int[] r = rightHits.get(k);
			if (mx >= r[0] && mx < r[2] && my >= r[1] && my < r[3]) {
				Line l = rightHitLines.get(k);
				if (l.tree() != null) tree = l.tree();
				if (l.biome() != null) {
					biome = l.biome();
					if (searching()) {
						query = "";
						search.setText("");
					}
				}
				page = 0;
				rebuild();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double h, double v) {
		if (mx < width / 2.0) {
			int max = Math.max(0, biomes.size() - leftRows());
			leftScroll = Math.max(0, Math.min(max, leftScroll + (v < 0 ? 3 : -3)));
		} else {
			turn(v < 0 ? 1 : -1);
		}
		return true;
	}

	@Override
	public boolean keyPressed(int key, int scan, int mods) {
		if (search != null && search.isFocused() && key != GLFW.GLFW_KEY_ESCAPE) {
			return super.keyPressed(key, scan, mods);
		}
		if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_PAGE_DOWN) {
			turn(1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_PAGE_UP) {
			turn(-1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_BACKSPACE && (biome != null || tree != null)) {
			goBack();
			return true;
		}
		return super.keyPressed(key, scan, mods);
	}

	/** Dev screenshots: scroll the biome list so {@code b} is in view. */
	public void showBiomeInList(String b) {
		int i = 0;
		for (CompendiumData.Biome x : biomes) {
			if (x.id().equals(b)) break;
			i++;
		}
		leftScroll = Math.max(0, Math.min(i - 2, biomes.size() - leftRows()));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
