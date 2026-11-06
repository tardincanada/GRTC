package com.periodicelements.client;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.MachineScreenHandler;
import com.periodicelements.machine.Reactions;
import com.periodicelements.research.ClientResearch;
import com.periodicelements.research.Research;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Machine screen: input, fuel, thermometer, the running reaction with its conditions, and outputs. */
public class MachineScreen extends HandledScreen<MachineScreenHandler> {
	private static final String NS = PeriodicElements.MOD_ID;
	private static final int THERMO_X = 30, THERMO_Y = 18, THERMO_W = 9, THERMO_H = 54;
	private static final int SIDE_PANEL_X = MachineScreenHandler.SIDE_X - 6, SIDE_PANEL_W = 28, SIDE_PANEL_H = 114;
	/** 3.29.0: the relief valve button, under the upgrade slots. */
	private static final int VALVE_X = MachineScreenHandler.SIDE_X, VALVE_Y = 103;

	public MachineScreen(MachineScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = 176;
		backgroundHeight = MachineScreenHandler.PLAYER_INV_Y + 82;
		playerInventoryTitleY = MachineScreenHandler.PLAYER_INV_Y - 11;
	}

	private Reactions.MachineSpec spec() {
		return Reactions.machine(handler.property(MachineBlockEntity.P_MACHINE));
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		int x = this.x, y = this.y;
		Gui.panel(ctx, x, y, backgroundWidth, backgroundHeight);
		Gui.panel(ctx, x + SIDE_PANEL_X, y + 10, SIDE_PANEL_W, SIDE_PANEL_H);
		ctx.fill(x + SIDE_PANEL_X + 4, y + 57, x + SIDE_PANEL_X + SIDE_PANEL_W - 4, y + 58, 0xFF8B8B8B);
		ctx.fill(x + SIDE_PANEL_X + 4, y + 99, x + SIDE_PANEL_X + SIDE_PANEL_W - 4, y + 100, 0xFF8B8B8B);
		drawValve(ctx, x + VALVE_X, y + VALVE_Y, venting(), Gui.inside(mouseX, mouseY, x + VALVE_X, y + VALVE_Y, 16, 16));
		for (Slot slot : handler.slots) {
			if (slot.isEnabled()) {
				Gui.slot(ctx, x + slot.x, y + slot.y);
			}
		}
		Reactions.MachineSpec spec = spec();
		double maxC = limit(spec);
		int temp = handler.property(MachineBlockEntity.P_TEMP);

		// Flame between input and fuel.
		int burn = handler.property(MachineBlockEntity.P_BURN);
		int total = Math.max(1, handler.property(MachineBlockEntity.P_BURN_TOTAL));
		Gui.inset(ctx, x + 9, y + 37, 14, 14, 0xFF2A2A2A);
		int fh = burn > 0 ? Math.max(1, (int) Math.round(12.0 * burn / total)) : 0;
		if (fh > 0) {
			ctx.fill(x + 10, y + 50 - fh, x + 22, y + 50, Gui.heatColor(handler.property(MachineBlockEntity.P_FLAME)));
		}

		// Thermometer with marks for the machine's reachable limit and the next reaction's minimum.
		Gui.inset(ctx, x + THERMO_X, y + THERMO_Y, THERMO_W, THERMO_H, 0xFF2A2A2A);
		int inner = THERMO_H - 2;
		int th = (int) Math.round(inner * Math.min(1, temp / maxC));
		if (th > 0) {
			ctx.fill(x + THERMO_X + 1, y + THERMO_Y + 1 + inner - th, x + THERMO_X + THERMO_W - 1,
					y + THERMO_Y + 1 + inner, Gui.heatColor(temp));
		}
		Reactions.Reaction shown = shownReaction();
		if (shown != null) {
			int my = y + THERMO_Y + 1 + inner - (int) Math.round(inner * Math.min(1, shown.tMin() / maxC));
			ctx.fill(x + THERMO_X - 2, my, x + THERMO_X + THERMO_W + 2, my + 1, 0xFF3060FF);
		}
		int cap = handler.property(MachineBlockEntity.P_CAP);
		if (burn > 0 && cap > 20) {
			int cy = y + THERMO_Y + 1 + inner - (int) Math.round(inner * Math.min(1, cap / maxC));
			ctx.fill(x + THERMO_X + THERMO_W, cy, x + THERMO_X + THERMO_W + 3, cy + 1, 0xFFFF8020);
		}

		Gui.bar(ctx, x + 44, y + 62, 62, 6, handler.property(MachineBlockEntity.P_PROGRESS) / 1000.0, 0xFF40B040);
	}

	/** The machine's temperature limit, with a Refractory Lining's bonus. */
	private double limit(Reactions.MachineSpec spec) {
		int l = handler.property(MachineBlockEntity.P_LIMIT);
		return l > 0 ? l : spec == null ? 1500 : spec.maxC();
	}

	private Reactions.Reaction shownReaction() {
		Reactions.Reaction r = Reactions.get(handler.property(MachineBlockEntity.P_ACTIVE));
		return r != null ? r : Reactions.get(handler.property(MachineBlockEntity.P_STATUS_REACTION));
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		super.drawForeground(ctx, mouseX, mouseY);
		Reactions.MachineSpec spec = spec();
		int temp = handler.property(MachineBlockEntity.P_TEMP);
		ctx.drawText(textRenderer, Text.literal(temp + " °C"), 44, 18, 0x202020, false);
		fit(ctx, Text.translatable("gui." + NS + ".machine.max", (int) limit(spec)), 44, 30, 70, Gui.TEXT_DIM);
		fit(ctx, Text.translatable("gui." + NS + ".machine.pressure", "1"), 44, 42, 70, Gui.TEXT_DIM);
		if (spec != null) {
			fit(ctx, Text.translatable("item." + NS + "." + spec.residue()), 112, 59, 38, Gui.TEXT_DIM);
		}

		Reactions.Reaction r = shownReaction();
		int ly = 76;
		if (r != null) {
			fit(ctx, r.name().copy().formatted(Formatting.BOLD), 8, ly, 160, 0x303030);
			ly += 11;
			fit(ctx, conditions(r), 8, ly, 160, Gui.TEXT_DIM);
			ly += 11;
		}
		MachineBlockEntity.Status status = MachineBlockEntity.Status.values()[Math.max(0,
				Math.min(MachineBlockEntity.Status.values().length - 1, handler.property(MachineBlockEntity.P_STATUS)))];
		fit(ctx, statusText(status, r), 8, ly, 160, statusColor(status));
		ly += 11;
		if (spec != null && Reactions.forMachine(spec.id()).stream().anyMatch(x -> x.carbon() > 0)) {
			fit(ctx, Text.translatable("gui." + NS + ".machine.carbon",
					String.format(Locale.ROOT, "%.1f", handler.property(MachineBlockEntity.P_CARBON) / 10.0)), 8, ly, 160, Gui.TEXT_DIM);
			ly += 11;
		}
		Text structure = structureLine();
		if (structure != null) {
			boolean ok = handler.property(MachineBlockEntity.P_S_PROBLEM) == 0;
			fit(ctx, structure, 8, ly, 160, ok ? 0x2E6B2E : 0xA03020);
		} else if (spec != null && spec.bellows() && Research.allows(ClientResearch.known(), "upgrade:forced_air")
				&& Research.unlockedBy("upgrade:forced_air") != null) {
			fit(ctx, Text.translatable("gui." + NS + ".machine.bellows"), 8, ly, 160, Gui.TEXT_DIM);
		}
	}

	/** 3.3.0: the structure this machine controls ("Blast furnace 3x3x10: speed x18, heat per item 55%"), or null. */
	private Text structureLine() {
		int kind = handler.property(MachineBlockEntity.P_S_KIND);
		if (kind <= 0) {
			return null;
		}
		var k = com.periodicelements.multiblock.Structures.Kind.values()[kind - 1];
		String key = "gui." + NS + ".structure.";
		int a = handler.property(MachineBlockEntity.P_S_A), h = handler.property(MachineBlockEntity.P_S_H);
		var problem = com.periodicelements.multiblock.Structures.Problem.values()[handler.property(MachineBlockEntity.P_S_PROBLEM)];
		switch (problem) {
			case MISSING -> {
				int raw = handler.property(MachineBlockEntity.P_S_BLOCK);
				Text block = raw < 0 ? Text.literal("?") : net.minecraft.registry.Registries.BLOCK.get(raw).getName();
				int layer = handler.property(MachineBlockEntity.P_S_LAYER);
				return layer == 0 ? Text.translatable(key + "missing.floor", block) : Text.translatable(key + "missing", block, layer);
			}
			case TOO_SMALL -> {
				return Text.translatable(key + "too_small." + k.id());
			}
			case NO_TUYERE -> {
				return Text.translatable(key + "no_tuyere");
			}
			default -> {
			}
		}
		String speed = String.format(Locale.ROOT, "%.1f", handler.property(MachineBlockEntity.P_S_SPEED) / 100.0)
				.replace(".0", "").replace('.', Text.translatable(key + "decimal").getString().charAt(0));
		int loss = handler.property(MachineBlockEntity.P_S_LOSS);
		if (k == com.periodicelements.multiblock.Structures.Kind.BLAST_FURNACE) {
			return Text.translatable(key + "blast_furnace", a, a, h, speed, loss);
		}
		int b = handler.property(MachineBlockEntity.P_S_B), extra = handler.property(MachineBlockEntity.P_S_EXTRA);
		if (k == com.periodicelements.multiblock.Structures.Kind.LEAD_CHAMBER) {
			return Text.translatable(key + "lead_chamber" + (extra > 0 ? ".tower" : ""), a, b, h, speed, loss);
		}
		if (k == com.periodicelements.multiblock.Structures.Kind.DISTILLATION_COLUMN) {
			return Text.translatable(key + "distillation_column", b, b, h, speed, extra);
		}
		if (k == com.periodicelements.multiblock.Structures.Kind.ENRICHMENT_CASCADE) {
			String enriched = String.format(Locale.ROOT, "%.1f", extra / 100.0)
					.replace('.', Text.translatable(key + "decimal").getString().charAt(0));
			return Text.translatable(key + "enrichment_cascade", a, speed, loss, enriched);
		}
		return a >= com.periodicelements.multiblock.Structures.SELF_HEATING_OVENS
				? Text.translatable(key + "coke_battery.gas", a, speed)
				: Text.translatable(key + "coke_battery", a, speed, loss);
	}

	/** Draws a line of text, shrunk to fit maxWidth if it is wider (2.4.0: Portuguese runs longer than English). */
	private void fit(DrawContext ctx, Text text, int x, int y, int maxWidth, int color) {
		int w = textRenderer.getWidth(text);
		if (w <= maxWidth) {
			ctx.drawText(textRenderer, text, x, y, color, false);
			return;
		}
		float s = maxWidth / (float) w;
		ctx.getMatrices().push();
		ctx.getMatrices().translate(x, y + 8 * (1 - s) / 2, 0);
		ctx.getMatrices().scale(s, s, 1);
		ctx.drawText(textRenderer, text, 0, 0, color, false);
		ctx.getMatrices().pop();
	}

	/** What an empty side-panel slot is for, when the mouse is over it. */
	private Text sideSlotHint(int mouseX, int mouseY) {
		int[] slots = {MachineBlockEntity.FLASKS, MachineBlockEntity.CYLINDERS, MachineBlockEntity.UPGRADE_FIRST, MachineBlockEntity.UPGRADE_LAST};
		for (int i : slots) {
			Slot s = handler.slots.get(i);
			if (!s.hasStack() && Gui.inside(mouseX, mouseY, x + s.x, y + s.y, 16, 16)) {
				String key = i == MachineBlockEntity.FLASKS ? "flasks" : i == MachineBlockEntity.CYLINDERS ? "cylinders"
						: "upgrade";
				return Text.translatable("gui." + NS + ".machine." + key);
			}
		}
		return null;
	}

	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
		return super.isClickOutsideBounds(mouseX, mouseY, left, top, button)
				&& !Gui.inside(mouseX, mouseY, left + SIDE_PANEL_X, top + 10, SIDE_PANEL_W, SIDE_PANEL_H);
	}

	private boolean venting() {
		return handler.property(MachineBlockEntity.P_VENT) != 0;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && Gui.inside(mouseX, mouseY, x + VALVE_X, y + VALVE_Y, 16, 16)) {
			client.interactionManager.clickButton(handler.syncId, MachineScreenHandler.TOGGLE_VENT);
			client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
					net.minecraft.sound.SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, venting() ? 0.8f : 1.2f));
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	/** A hand wheel on a pipe: red and shut, or green and open with a puff of gas above it. */
	private static void drawValve(DrawContext ctx, int x, int y, boolean open, boolean hover) {
		Gui.inset(ctx, x, y, 16, 16, hover ? 0xFF505050 : 0xFF2A2A2A);
		int wheel = open ? 0xFF3CB043 : 0xFFC83C2C;
		ctx.fill(x + 7, y + 8, x + 9, y + 15, 0xFF9A9A9A); // stem
		ctx.fill(x + 2, y + 13, x + 14, y + 15, 0xFF7A7A7A); // pipe
		// Ring of the wheel, 8x8, with two spokes.
		ctx.fill(x + 4, y + 4, x + 12, y + 5, wheel);
		ctx.fill(x + 4, y + 10, x + 12, y + 11, wheel);
		ctx.fill(x + 4, y + 4, x + 5, y + 11, wheel);
		ctx.fill(x + 11, y + 4, x + 12, y + 11, wheel);
		ctx.fill(x + 7, y + 4, x + 9, y + 11, wheel);
		ctx.fill(x + 4, y + 7, x + 12, y + 8, wheel);
		if (open) {
			ctx.fill(x + 6, y + 1, x + 8, y + 2, 0xFFD0D0D0);
			ctx.fill(x + 9, y + 2, x + 11, y + 3, 0xFFB0B0B0);
		}
	}

	static Text conditions(Reactions.Reaction r) {
		String energy = r.energy() < 0
				? Text.translatable("gui." + NS + ".machine.releases", (int) -r.energy()).getString()
				: Text.translatable("gui." + NS + ".machine.needs", (int) r.energy()).getString();
		return Text.translatable("gui." + NS + ".machine.conditions", (int) r.tMin(), (int) r.tMax(),
				r.pressure() == Math.rint(r.pressure()) ? String.valueOf((int) r.pressure()) : String.valueOf(r.pressure()), energy);
	}

	/** The status line, as the screen shows it (also the Jade plugin, 3.26.0). */
	public static Text statusText(MachineBlockEntity.Status status, Reactions.Reaction r) {
		String key = "gui." + NS + ".status." + status.name().toLowerCase(Locale.ROOT);
		if (status == MachineBlockEntity.Status.HEATING || status == MachineBlockEntity.Status.NO_FUEL) {
			return Text.translatable(key, r == null ? "?" : (int) r.tMin());
		}
		if (status == MachineBlockEntity.Status.NEEDS_REAGENT) {
			return Text.translatable(key, r == null || r.reagents().isEmpty() ? Text.literal("?") : chemName(r.reagents().get(0).id()));
		}
		if (status == MachineBlockEntity.Status.TOO_HOT) {
			return Text.translatable(key, r == null ? "?" : (int) r.tMax());
		}
		return Text.translatable(key);
	}

	private static Text chemName(String id) {
		return id.startsWith("item:") ? Text.translatable(net.minecraft.registry.Registries.ITEM
				.get(net.minecraft.util.Identifier.of(id.substring(5))).getTranslationKey())
				: Text.translatable("item." + NS + "." + id);
	}

	public static int statusColor(MachineBlockEntity.Status status) {
		return switch (status) {
			case WORKING -> 0x207020;
			case IDLE -> Gui.TEXT_DIM;
			case HEATING -> 0xA05000;
			default -> 0xA02020;
		};
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
		if (Gui.inside(mouseX, mouseY, x + VALVE_X, y + VALVE_Y, 16, 16)) {
			String key = "gui." + NS + ".machine.valve";
			ctx.drawTooltip(textRenderer, List.of(Text.translatable(key),
					Text.translatable(key + (venting() ? ".open" : ".shut")).formatted(venting() ? Formatting.GREEN : Formatting.RED),
					Text.translatable(key + ".hint").formatted(Formatting.GRAY)), mouseX, mouseY);
		}
		Text side = sideSlotHint(mouseX, mouseY);
		if (side != null) {
			ctx.drawTooltip(textRenderer, List.of(side), mouseX, mouseY);
		} else if (Gui.inside(mouseX, mouseY, x + THERMO_X - 2, y + THERMO_Y, THERMO_W + 4, THERMO_H)) {
			List<Text> lines = new ArrayList<>();
			lines.add(Text.translatable("gui." + NS + ".machine.temperature", handler.property(MachineBlockEntity.P_TEMP)));
			if (handler.property(MachineBlockEntity.P_BURN) > 0) {
				lines.add(Text.translatable("gui." + NS + ".machine.cap", handler.property(MachineBlockEntity.P_CAP))
						.formatted(Formatting.GOLD));
			}
			Reactions.Reaction r = shownReaction();
			if (r != null) {
				lines.add(Text.translatable("gui." + NS + ".machine.needs_temp", (int) r.tMin()).formatted(Formatting.BLUE));
			}
			ctx.drawTooltip(textRenderer, lines, mouseX, mouseY);
		} else if (handler.slots.get(MachineBlockEntity.REAGENT).isEnabled() && !handler.slots.get(MachineBlockEntity.REAGENT).hasStack()
				&& Gui.inside(mouseX, mouseY, x + 116, y + 40, 16, 16)) {
			List<Text> lines = new ArrayList<>();
			lines.add(Text.translatable("gui." + NS + ".machine.reagent"));
			java.util.Set<String> seen = new java.util.LinkedHashSet<>();
			for (Reactions.Reaction r : Reactions.forMachine(spec().id())) {
				for (Reactions.Reagent g : r.reagents()) {
					if (seen.add(g.id())) {
						lines.add(Text.literal(" ").append(chemName(g.id())).append(Text.literal(" → "))
								.append(r.name()).formatted(Formatting.GRAY));
					}
				}
			}
			ctx.drawTooltip(textRenderer, lines, mouseX, mouseY);
		} else if (Gui.inside(mouseX, mouseY, x + 8, y + 76, 160, 22)) {
			Reactions.Reaction r = shownReaction();
			if (r != null) {
				List<Text> lines = new ArrayList<>();
				lines.add(r.name());
				lines.add(Text.translatable("reaction." + NS + "." + r.id() + ".desc").formatted(Formatting.GRAY));
				if (!r.catalyst().isEmpty()) {
					lines.add(Text.translatable("gui." + NS + ".machine.catalyst", r.catalyst()).formatted(Formatting.DARK_GRAY));
				}
				for (Reactions.Product p : r.products()) {
					MutableText name = p.id().startsWith("item:") ? Text.translatable(
							net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(p.id().substring(5))).getTranslationKey())
							: Text.translatable("item." + NS + "." + p.id());
					lines.add(Text.literal(String.format(Locale.ROOT, " %.1f ", p.units())).append(name)
							.append(p.vented() ? Text.translatable("gui." + NS + ".machine.vented")
									: p.slag() ? Text.translatable("gui." + NS + ".machine.to_slag") : Text.empty())
							.formatted(p.vented() ? Formatting.DARK_GRAY : Formatting.GREEN));
				}
				for (Reactions.Reagent g : r.reagents()) {
					lines.add(Text.translatable("gui." + NS + ".machine.reagent_line",
							String.format(Locale.ROOT, "%.1f", g.units()), chemName(g.id())).formatted(Formatting.YELLOW));
				}
				lines.add(Text.translatable("gui." + NS + ".machine.per100", Text.translatable("item." + NS + "." + r.feed()))
						.formatted(Formatting.DARK_GRAY));
				ctx.drawTooltip(textRenderer, lines, mouseX, mouseY);
			}
		}
	}
}
