package com.periodicelements.client;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.ClientResearch;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchStationBlockEntity;
import com.periodicelements.research.ResearchStationScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Research station screen: sample slots on the left, the station's projects on the right (star = key project),
 * the selected project's samples below the list, and buttons to research it or write a treatise.
 */
public class ResearchStationScreen extends HandledScreen<ResearchStationScreenHandler> {
	private static final String NS = PeriodicElements.MOD_ID;
	private static final int LIST_X = 66, LIST_Y = 18, LIST_W = 182, ROW_H = 12, ROWS = 9;
	private static final int DETAIL_X = 180, DETAIL_Y = 140;

	private int scroll;
	private int selected = -1;
	private ButtonWidget researchButton;
	private ButtonWidget treatiseButton;

	public ResearchStationScreen(ResearchStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = 256;
		backgroundHeight = ResearchStationScreenHandler.PLAYER_INV_Y + 82;
		playerInventoryTitleY = ResearchStationScreenHandler.PLAYER_INV_Y - 11;
	}

	@Override
	protected void init() {
		super.init();
		researchButton = addDrawableChild(ButtonWidget.builder(Text.translatable("gui." + NS + ".station.research"), b -> {
			if (selected >= 0) {
				client.interactionManager.clickButton(handler.syncId, selected);
			}
		}).dimensions(x + 7, y + 96, 54, 16).build());
		treatiseButton = addDrawableChild(ButtonWidget.builder(Text.translatable("gui." + NS + ".station.treatise"),
				b -> client.interactionManager.clickButton(handler.syncId, ResearchStationScreenHandler.WRITE_TREATISE))
				.dimensions(x + 7, y + 114, 54, 16).build());
	}

	private List<Research.Project> projects() {
		int level = handler.property(ResearchStationBlockEntity.P_LEVEL);
		List<Research.Project> list = new ArrayList<>();
		for (Research.Project p : Research.all()) {
			if (p.level() == level) {
				list.add(p);
			}
		}
		// Key projects first, then the rest; each in table order.
		list.sort((a, b) -> Boolean.compare(b.key(), a.key()));
		return list;
	}

	@Override
	protected void handledScreenTick() {
		super.handledScreenTick();
		Research.Project p = Research.get(selected);
		boolean busy = handler.property(ResearchStationBlockEntity.P_ACTIVE) >= 0;
		researchButton.active = p != null && !busy && Research.status(p, ClientResearch.known()) == Research.Status.AVAILABLE;
		treatiseButton.active = Research.allows(ClientResearch.known(), "item:treatise_manuscript")
				&& !ClientResearch.known().isEmpty();
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		Gui.panel(ctx, x, y, backgroundWidth, backgroundHeight);
		for (Slot slot : handler.slots) {
			Gui.slot(ctx, x + slot.x, y + slot.y);
		}
		Gui.inset(ctx, x + LIST_X - 1, y + LIST_Y - 1, LIST_W + 2, ROWS * ROW_H + 2, 0xFF8B8B8B);
		int active = handler.property(ResearchStationBlockEntity.P_ACTIVE);
		int total = Math.max(1, handler.property(ResearchStationBlockEntity.P_TOTAL));
		Gui.bar(ctx, x + 8, y + 86, 52, 6, active < 0 ? 0 : handler.property(ResearchStationBlockEntity.P_PROGRESS) / (double) total,
				0xFF4080E0);
		Gui.inset(ctx, x + DETAIL_X - 1, y + DETAIL_Y - 1, 70, 74, 0xFFB0B0B0);
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		super.drawForeground(ctx, mouseX, mouseY);
		Set<String> known = ClientResearch.known();
		int level = Research.techLevel(known);
		ctx.drawText(textRenderer, Text.translatable("gui." + NS + ".station.your_level", level), LIST_X, playerInventoryTitleY, Gui.TEXT_DIM, false);

		int active = handler.property(ResearchStationBlockEntity.P_ACTIVE);
		Research.Project running = Research.get(active);
		ctx.drawText(textRenderer, running == null ? Text.translatable("gui." + NS + ".station.idle")
				: Text.translatable("gui." + NS + ".station.researching"), 8, 76, Gui.TEXT_DIM, false);

		List<Research.Project> list = projects();
		scroll = Math.max(0, Math.min(scroll, Math.max(0, list.size() - ROWS)));
		for (int i = 0; i < ROWS && i + scroll < list.size(); i++) {
			Research.Project p = list.get(i + scroll);
			int ry = LIST_Y + i * ROW_H;
			if (p.index() == selected) {
				ctx.fill(LIST_X, ry, LIST_X + LIST_W, ry + ROW_H, 0xFF6F8FBF);
			} else if (p.index() == active) {
				ctx.fill(LIST_X, ry, LIST_X + LIST_W, ry + ROW_H, 0xFF7FA07F);
			}
			Research.Status s = Research.status(p, known);
			int color = switch (s) {
				case DONE -> 0x1E6B1E;
				case AVAILABLE -> 0x202020;
				default -> 0x5A5A5A;
			};
			String mark = switch (s) {
				case DONE -> "✔";
				case AVAILABLE -> "•";
				default -> "✖";
			};
			ctx.drawText(textRenderer, Text.literal(mark), LIST_X + 2, ry + 2, color, false);
			MutableText name = p.name().copy();
			String trimmed = textRenderer.trimToWidth(name.getString(), LIST_W - 26);
			ctx.drawText(textRenderer, Text.literal(trimmed), LIST_X + 11, ry + 2, color, false);
			if (p.key()) {
				ctx.drawText(textRenderer, Text.literal("★"), LIST_X + LIST_W - 10, ry + 2, 0xB08000, false);
			}
		}

		// Selected project's samples, or the treatise cost while hovering that button.
		List<ItemStack> stacks = new ArrayList<>();
		Text header;
		if (treatiseButton.isHovered()) {
			int n = known.size();
			if (ResearchStationBlockEntity.prints(known, handler.inventory())) {
				int sheets = Math.max(1, (n + 1) / 2);
				stacks.add(new ItemStack(Registries.ITEM.get(Identifier.of(NS, "type_metal")), 1));
				stacks.add(new ItemStack(Items.LEATHER, ResearchStationBlockEntity.PRINT_COPIES));
				stacks.add(new ItemStack(Items.PAPER, sheets));
				stacks.add(new ItemStack(Registries.ITEM.get(Identifier.of(NS, "carbon_ink")), sheets));
			} else {
				stacks.add(new ItemStack(Items.LEATHER, 1));
				stacks.add(new ItemStack(Items.PAPER, Math.max(1, n)));
				stacks.add(new ItemStack(Registries.ITEM.get(Identifier.of(NS, "carbon_ink")), Math.max(1, n)));
			}
			header = Text.translatable("gui." + NS + ".station.cost");
		} else {
			Research.Project p = Research.get(selected);
			if (p != null) {
				p.samples().forEach(s -> stacks.add(new ItemStack(s.asItem(), s.count())));
			}
			header = Text.translatable("gui." + NS + ".station.samples");
		}
		ctx.drawText(textRenderer, header, DETAIL_X + 1, DETAIL_Y + 1, Gui.TEXT, false);
		for (int i = 0; i < stacks.size() && i < 12; i++) {
			int sx = DETAIL_X + 1 + (i % 4) * 17, sy = DETAIL_Y + 12 + (i / 4) * 18;
			ItemStack stack = stacks.get(i);
			ctx.drawItem(stack, sx, sy);
			boolean enough = ResearchStationBlockEntity.count(handler.inventory(), st -> st.isOf(stack.getItem())
					|| (stack.isOf(Registries.ITEM.get(Identifier.of(NS, "carbon_ink"))) && st.isIn(ResearchStationBlockEntity.INK)))
					>= stack.getCount();
			String label = String.valueOf(stack.getCount());
			ctx.drawItemInSlot(textRenderer, stack.copyWithCount(1), sx, sy, (enough ? "§a" : "§c") + label);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int row = rowAt(mouseX, mouseY);
		if (row >= 0) {
			selected = projects().get(row).index();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (Gui.inside(mouseX, mouseY, x + LIST_X, y + LIST_Y, LIST_W, ROWS * ROW_H)) {
			scroll -= (int) Math.signum(verticalAmount);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private int rowAt(double mouseX, double mouseY) {
		if (!Gui.inside(mouseX, mouseY, x + LIST_X, y + LIST_Y, LIST_W, ROWS * ROW_H)) {
			return -1;
		}
		int row = (int) ((mouseY - y - LIST_Y) / ROW_H) + scroll;
		return row < projects().size() ? row : -1;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
		int row = rowAt(mouseX, mouseY);
		if (row >= 0) {
			ctx.drawTooltip(textRenderer, projectTooltip(projects().get(row)), mouseX, mouseY);
		}
	}

	private List<Text> projectTooltip(Research.Project p) {
		Set<String> known = ClientResearch.known();
		List<Text> lines = new ArrayList<>();
		lines.add(p.name().copy().formatted(p.key() ? Formatting.GOLD : Formatting.WHITE));
		lines.add(Text.literal(p.date()).append(" · ")
				.append(Text.translatable(p.key() ? "gui." + NS + ".station.key" : "gui." + NS + ".station.optional"))
				.formatted(Formatting.DARK_GRAY));
		lines.add(p.description().copy().formatted(Formatting.GRAY));
		if (!p.requires().isEmpty()) {
			MutableText req = Text.translatable("gui." + NS + ".station.requires");
			for (int i = 0; i < p.requires().size(); i++) {
				Research.Project r = Research.get(p.requires().get(i));
				req.append(i == 0 ? " " : ", ").append(r.name().copy()
						.formatted(known.contains(r.id()) ? Formatting.GREEN : Formatting.RED));
			}
			lines.add(req);
		}
		if (Research.status(p, known) == Research.Status.NEEDS_LEVEL) {
			lines.add(Text.translatable("gui." + NS + ".station.needs_level", p.level() - 1).formatted(Formatting.RED));
		}
		MutableText unlocks = Text.translatable("gui." + NS + ".station.unlocks");
		unlocks.append(" " + String.join(", ", p.unlocks().stream()
				.map(u -> u.substring(u.indexOf(':') + 1).replace('_', ' ')).toList()));
		lines.add(unlocks.formatted(Formatting.DARK_AQUA));
		lines.add(Text.translatable("gui." + NS + ".station.time", Research.duration(p) / 20).formatted(Formatting.DARK_GRAY));
		return lines;
	}
}
