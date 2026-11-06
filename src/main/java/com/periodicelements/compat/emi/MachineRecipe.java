package com.periodicelements.compat.emi;

import com.periodicelements.machine.Reactions;
import com.periodicelements.research.ClientResearch;
import com.periodicelements.research.Research;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * One reaction in one machine. Left: the feed (any item whose composition holds the feed substance, cycling), the
 * reagents and the charcoal reductant; right: the products per 100 units of feed. Below: temperature range, pressure,
 * energy, catalyst and the research that unlocks it.
 *
 * Research: a reaction the player has researched (or that needs none) shows in full; one of the next tech level up
 * shows in full with its research in red; anything further shows only "reach level N".
 */
final class MachineRecipe implements EmiRecipe {
	private static final int IN_COLS = 2, OUT_COLS = 4, OUT_X = 68, ARROW_X = 40;

	private final EmiRecipeCategory category;
	private final Identifier id;
	private final Reactions.Reaction reaction;
	private final List<EmiIngredient> inputs = new ArrayList<>();
	private final List<Text> inputTips = new ArrayList<>();
	private final List<EmiStack> outputs = new ArrayList<>();
	private final List<Text> outputTips = new ArrayList<>();
	private final List<Text> lostProducts = new ArrayList<>();
	private final int slotRows;
	private final int height;

	MachineRecipe(EmiRecipeCategory category, String machine, Reactions.Reaction r, List<EmiStack> feedSources) {
		this.category = category;
		this.reaction = r;
		this.id = Identifier.of(EmiText.NS, "/" + machine + "/" + r.id());
		String ns = EmiText.NS;
		Text per100 = Text.translatable("gui." + ns + ".machine.per100", EmiText.chemName(r.feed()));

		inputs.add(feedSources.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(feedSources));
		inputTips.add(Text.translatable("emi." + ns + ".feed", EmiText.chemName(r.feed())).formatted(Formatting.GRAY));
		for (Reactions.Reagent g : r.reagents()) {
			Item item = EmiText.item(g.id());
			inputs.add(item == null ? EmiStack.EMPTY : EmiStack.of(item));
			inputTips.add(Text.translatable("gui." + ns + ".machine.reagent_line", EmiText.units(g.units()),
					EmiText.chemName(g.id())).append(" ").append(per100).formatted(Formatting.YELLOW));
		}
		if (r.carbon() > 0) {
			inputs.add(EmiIngredient.of(List.of(EmiStack.of(Items.CHARCOAL), EmiStack.of(Items.COAL))));
			inputTips.add(Text.translatable("gui." + ns + ".machine.carbon", EmiText.units(r.carbon())).append(" ")
					.append(per100).formatted(Formatting.YELLOW));
		}
		for (Reactions.Product p : r.products()) {
			Item item = EmiText.item(p.id());
			MutableText line = Text.translatable("emi." + ns + ".units", EmiText.units(p.units())).append(" ").append(per100);
			if (p.vented()) {
				line.append(Text.translatable("gui." + ns + ".machine.vented"));
			} else if (p.slag()) {
				line.append(Text.translatable("gui." + ns + ".machine.to_slag"));
			}
			if (item == null) {
				lostProducts.add(EmiText.chemName(p.id()));
				continue;
			}
			outputs.add(EmiStack.of(item));
			outputTips.add(line.formatted(p.vented() ? Formatting.DARK_GRAY : Formatting.GREEN));
		}
		int inRows = (inputs.size() + IN_COLS - 1) / IN_COLS;
		int outRows = Math.max(1, (outputs.size() + OUT_COLS - 1) / OUT_COLS);
		slotRows = Math.max(inRows, outRows);
		int lines = 0;
		for (Text t : textLines(Set.of(), true)) {
			lines += EmiText.wrap(t).size();
		}
		height = slotRows * 18 + 3 + lines * 10;
	}

	/** 0: show it all; 1: show it, research still needed; 2: hide it (more than one level ahead). */
	private int state(Set<String> known) {
		if (reaction.project() == null || known.contains(reaction.project())) {
			return 0;
		}
		return reaction.level() <= Research.techLevel(known) + 1 ? 1 : 2;
	}

	private List<Text> textLines(Set<String> known, boolean full) {
		String ns = EmiText.NS;
		List<Text> out = new ArrayList<>();
		out.add(reaction.name().copy().formatted(Formatting.DARK_GRAY));
		out.add(conditions(reaction));
		if (!reaction.catalyst().isEmpty()) {
			out.add(Text.translatable("gui." + ns + ".machine.catalyst", reaction.catalyst()).formatted(Formatting.DARK_GRAY));
		}
		if (!lostProducts.isEmpty()) {
			MutableText lost = Text.translatable("emi." + ns + ".also_made");
			for (int i = 0; i < lostProducts.size(); i++) {
				lost.append(i == 0 ? " " : ", ").append(lostProducts.get(i));
			}
			out.add(lost.formatted(Formatting.DARK_GRAY));
		}
		if (reaction.project() == null) {
			out.add(Text.translatable("emi." + ns + ".research.none").formatted(Formatting.DARK_GRAY));
		} else {
			Research.Project p = Research.get(reaction.project());
			Text name = p == null ? Text.literal(reaction.project()) : p.name();
			boolean done = !full && known.contains(reaction.project());
			out.add(Text.translatable("emi." + ns + ".research", name, reaction.level())
					.formatted(full || done ? Formatting.DARK_GRAY : Formatting.DARK_RED));
		}
		return out;
	}

	static Text conditions(Reactions.Reaction r) {
		String ns = EmiText.NS;
		String energy = r.energy() < 0
				? Text.translatable("gui." + ns + ".machine.releases", (int) -r.energy()).getString()
				: Text.translatable("gui." + ns + ".machine.needs", (int) r.energy()).getString();
		return Text.translatable("gui." + ns + ".machine.conditions", (int) r.tMin(), (int) r.tMax(),
				r.pressure() == Math.rint(r.pressure()) ? String.valueOf((int) r.pressure()) : String.valueOf(r.pressure()),
				energy).formatted(Formatting.DARK_GRAY);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false; // amounts are units of a substance, not item counts
	}

	@Override
	public int getDisplayWidth() {
		return EmiText.WIDTH;
	}

	@Override
	public int getDisplayHeight() {
		return height;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		Set<String> known = ClientResearch.known();
		int state = state(known);
		if (state == 2) {
			int y = 0;
			for (OrderedText line : EmiText.wrap(Text.translatable("emi." + EmiText.NS + ".locked", reaction.level())
					.formatted(Formatting.DARK_RED))) {
				widgets.addText(line, 0, y, 0xFFFFFF, false);
				y += 10;
			}
			return;
		}
		for (int i = 0; i < inputs.size(); i++) {
			SlotWidget slot = widgets.addSlot(inputs.get(i), (i % IN_COLS) * 18, (i / IN_COLS) * 18);
			slot.appendTooltip(inputTips.get(i));
		}
		int rowsY = (slotRows * 18 - 17) / 2;
		widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, rowsY);
		for (int i = 0; i < outputs.size(); i++) {
			widgets.addSlot(outputs.get(i), OUT_X + (i % OUT_COLS) * 18, (i / OUT_COLS) * 18)
					.appendTooltip(outputTips.get(i)).recipeContext(this);
		}
		int y = slotRows * 18 + 3;
		for (Text t : textLines(known, false)) {
			for (OrderedText line : EmiText.wrap(t)) {
				widgets.addText(line, 0, y, 0xFFFFFF, false);
				y += 10;
			}
		}
	}
}
