package com.periodicelements.compat.emi;

import com.periodicelements.BlockComposition;
import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.Reactions;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EMI plugin (3.25.0): one recipe category per machine, each listing the reactions it runs with their temperature,
 * pressure, energy, catalyst, reagents and research; and a Composition category that breaks every block and item
 * with a composition into the substances a machine sees in it. Registered through the "emi" entrypoint, so the mod
 * never loads this class (or EMI's API) when EMI is not installed.
 *
 * What a recipe shows follows the player's research at the moment it is opened (see {@link MachineRecipe} and
 * {@link CompositionRecipe}), the way the Field Guide and the Magnifying Lens do.
 */
@EmiEntrypoint
public class PeEmiPlugin implements EmiPlugin {
	public static final Identifier COMPOSITION_ID = Identifier.of(PeriodicElements.MOD_ID, "composition");

	/** A machine's category takes the machine's own name. */
	static final class MachineCategory extends EmiRecipeCategory {
		private final Item machine;

		MachineCategory(Item machine) {
			super(Identifier.of(PeriodicElements.MOD_ID, Registries.ITEM.getId(machine).getPath()), EmiStack.of(machine));
			this.machine = machine;
		}

		@Override
		public Text getName() {
			return Text.translatable(machine.getTranslationKey());
		}
	}

	/**
	 * Substance or element -> research projects of the reactions that use or make it ("" for a reaction that needs
	 * none). The player can name a substance once any of those is known, as the machines show it by name.
	 */
	static Map<String, java.util.Set<String>> namedBy = Map.of();

	@Override
	public void register(EmiRegistry registry) {
		Map<String, java.util.Set<String>> named = new HashMap<>();
		for (Reactions.Reaction r : Reactions.all()) {
			String project = r.project() == null ? "" : r.project();
			named.computeIfAbsent(r.feed(), k -> new java.util.HashSet<>()).add(project);
			for (Reactions.Product p : r.products()) {
				named.computeIfAbsent(com.periodicelements.radiation.Isotopes.baseId(p.id()), k -> new java.util.HashSet<>()).add(project);
			}
			for (Reactions.Reagent g : r.reagents()) {
				named.computeIfAbsent(g.id(), k -> new java.util.HashSet<>()).add(project);
			}
		}
		namedBy = named;
		Map<String, List<EmiStack>> sources = feedSources();
		Map<String, MachineCategory> categories = new LinkedHashMap<>();
		for (Reactions.MachineSpec spec : Reactions.machines()) {
			Item item = Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, spec.id()));
			if (item == Items.AIR) {
				continue;
			}
			MachineCategory category = new MachineCategory(item);
			categories.put(spec.id(), category);
			registry.addCategory(category);
			registry.addWorkstation(category, EmiStack.of(item));
		}
		for (Reactions.Reaction r : Reactions.all()) {
			for (String machine : r.machines()) {
				MachineCategory category = categories.get(machine);
				if (category != null) {
					registry.addRecipe(new MachineRecipe(category, machine, r, sources.getOrDefault(r.feed(), List.of())));
				}
			}
		}

		EmiRecipeCategory composition = new EmiRecipeCategory(COMPOSITION_ID,
				EmiStack.of(com.periodicelements.ModContent.MAGNIFYING_LENS));
		registry.addCategory(composition);
		registry.addWorkstation(composition, EmiStack.of(com.periodicelements.ModContent.MAGNIFYING_LENS));
		for (Map.Entry<String, BlockComposition.Entry> e : BlockComposition.all().entrySet()) {
			Identifier id = Identifier.tryParse(e.getKey());
			if (id == null || !Registries.ITEM.containsId(id) || e.getValue().parts().isEmpty()) {
				continue;
			}
			registry.addRecipe(new CompositionRecipe(composition, Registries.ITEM.get(id), id, e.getValue()));
		}
	}

	/**
	 * For each substance, the items a machine can be fed to get it: its own item first, then every block or item
	 * whose composition holds it, richest first.
	 */
	static Map<String, List<EmiStack>> feedSources() {
		Map<String, List<Map.Entry<Item, Double>>> found = new HashMap<>();
		for (Map.Entry<String, BlockComposition.Entry> e : BlockComposition.all().entrySet()) {
			Identifier id = Identifier.tryParse(e.getKey());
			if (id == null || !Registries.ITEM.containsId(id)) {
				continue;
			}
			Item item = Registries.ITEM.get(id);
			for (BlockComposition.Part part : e.getValue().parts()) {
				found.computeIfAbsent(part.id(), k -> new ArrayList<>()).add(Map.entry(item, part.units()));
			}
		}
		Map<String, List<EmiStack>> out = new HashMap<>();
		for (Reactions.Reaction r : Reactions.all()) {
			if (out.containsKey(r.feed())) {
				continue;
			}
			List<EmiStack> list = new ArrayList<>();
			Item own = EmiText.item(r.feed());
			if (own != null) {
				list.add(EmiStack.of(own));
			}
			List<Map.Entry<Item, Double>> holders = new ArrayList<>(found.getOrDefault(r.feed(), List.of()));
			holders.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
			for (Map.Entry<Item, Double> h : holders) {
				if (h.getKey() != own) {
					list.add(EmiStack.of(h.getKey()));
				}
			}
			out.put(r.feed(), List.copyOf(list));
		}
		return out;
	}
}
