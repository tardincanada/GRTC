package com.periodicelements.compat.emi;

import com.periodicelements.BlockComposition;
import com.periodicelements.client.Lens;
import com.periodicelements.research.ClientResearch;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.Item;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * What a block or item is made of: the substances and elements a machine finds in it, in units (1000 to a block).
 * Like the Magnifying Lens, a part the player cannot name yet (an element in it is still unknown) shows as a blank
 * slot, and its units are summed into "not identified yet".
 */
final class CompositionRecipe implements EmiRecipe {
	private static final int COLS = 5, OUT_X = 50;

	private final EmiRecipeCategory category;
	private final Identifier id;
	private final EmiStack input;
	private final BlockComposition.Entry entry;
	private final List<BlockComposition.Part> parts = new ArrayList<>();
	private final List<EmiStack> outputs = new ArrayList<>();
	private final int rows;
	private final int textLines;

	CompositionRecipe(EmiRecipeCategory category, Item item, Identifier itemId, BlockComposition.Entry entry) {
		this.category = category;
		this.id = Identifier.of(EmiText.NS, "/composition/" + itemId.getNamespace() + "/" + itemId.getPath());
		this.input = EmiStack.of(item);
		this.entry = entry;
		for (BlockComposition.Part part : entry.parts()) {
			Item out = EmiText.item(part.id());
			if (out != null) {
				parts.add(part);
				outputs.add(EmiStack.of(out));
			}
		}
		rows = Math.max(1, (outputs.size() + COLS - 1) / COLS);
		// Room for the total and, at worst, the "not identified" line.
		textLines = EmiText.wrap(Text.translatable("emi." + EmiText.NS + ".total", EmiText.units(entry.amount()))).size()
				+ EmiText.wrap(Text.translatable("emi." + EmiText.NS + ".hidden", EmiText.units(entry.amount()))).size();
	}

	static boolean knows(String part, Set<String> known) {
		if (part.equals(BlockComposition.GANGUE) || Lens.knowsPart(part, known)) {
			return true;
		}
		for (String project : PeEmiPlugin.namedBy.getOrDefault(part, Set.of())) {
			if (project.isEmpty() || known.contains(project)) {
				return true;
			}
		}
		return false;
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
		return List.of(input);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public int getDisplayWidth() {
		return EmiText.WIDTH;
	}

	@Override
	public int getDisplayHeight() {
		return rows * 18 + 3 + textLines * 10;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		Set<String> known = ClientResearch.known();
		String ns = EmiText.NS;
		widgets.addSlot(input, 0, (rows * 18 - 18) / 2)
				.appendTooltip(Text.translatable("emi." + ns + ".total", EmiText.units(entry.amount())).formatted(Formatting.GRAY));
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 22, (rows * 18 - 17) / 2);
		double hidden = 0;
		for (int i = 0; i < outputs.size(); i++) {
			BlockComposition.Part part = parts.get(i);
			int x = OUT_X + (i % COLS) * 18, y = (i / COLS) * 18;
			if (knows(part.id(), known)) {
				var slot = widgets.addSlot(outputs.get(i), x, y).recipeContext(this);
				if (part.id().equals(BlockComposition.GANGUE)) {
					slot.appendTooltip(EmiText.chemName(part.id()).formatted(Formatting.GRAY));
				}
				slot.appendTooltip(Text.translatable("emi." + ns + ".units", EmiText.units(part.units())).formatted(Formatting.GREEN));
			} else {
				hidden += part.units();
				widgets.addSlot(EmiStack.EMPTY, x, y);
				widgets.addTooltipText(List.of(Text.translatable("emi." + ns + ".unknown")), x, y, 18, 18);
			}
		}
		for (BlockComposition.Part part : entry.parts()) {
			if (EmiText.item(part.id()) == null) {
				hidden += part.units();
			}
		}
		int y = rows * 18 + 3;
		List<Text> lines = new ArrayList<>();
		lines.add(Text.translatable("emi." + ns + ".total", EmiText.units(entry.amount())).formatted(Formatting.DARK_GRAY));
		if (hidden > 0.05) {
			lines.add(Text.translatable("emi." + ns + ".hidden", EmiText.units(hidden)).formatted(Formatting.DARK_GRAY));
		}
		for (Text t : lines) {
			for (OrderedText line : EmiText.wrap(t)) {
				widgets.addText(line, 0, y, 0xFFFFFF, false);
				y += 10;
			}
		}
	}
}
