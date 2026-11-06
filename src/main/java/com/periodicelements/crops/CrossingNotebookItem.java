package com.periodicelements.crops;

import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Crossing Notebook (3.37.0, Mendel's Laws): note one parent (seeds in the other hand), then the other; the notebook
 * works out the Punnett square, gene by gene: what share of the seeds of that cross will show each trait.
 */
public class CrossingNotebookItem extends Item {
	public static final String UNLOCK = "item:crossing_notebook";

	public CrossingNotebookItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack book = player.getStackInHand(hand);
		ItemStack seeds = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if (world.isClient) {
			return TypedActionResult.success(book);
		}
		if (!player.isCreative() && !Research.allows(ResearchAccess.known(player), UNLOCK)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.notebook.needs"), true);
			return TypedActionResult.fail(book);
		}
		if (!Crops.isSeed(seeds)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.notebook.nothing"), true);
			return TypedActionResult.fail(book);
		}
		int g = seeds.getOrDefault(Crops.GENOME, Genome.DEFAULT) & Genome.MASK;
		String species = Registries.ITEM.getId(seeds.getItem()).toString();
		NbtCompound nbt = book.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		if (!nbt.contains("Species")) {
			nbt.putString("Species", species);
			nbt.putInt("Parent", g);
			NbtComponent.set(DataComponentTypes.CUSTOM_DATA, book, nbt);
			player.sendMessage(Text.translatable("gui.periodic_elements.notebook.first", seeds.getName()), true);
			return TypedActionResult.success(book);
		}
		book.remove(DataComponentTypes.CUSTOM_DATA);
		if (!nbt.getString("Species").equals(species)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.notebook.species"), true);
			return TypedActionResult.fail(book);
		}
		int level = Math.max(SeedLensItem.level(player), Genome.STAMP_GENOTYPE);
		player.sendMessage(Text.translatable("gui.periodic_elements.notebook.cross", seeds.getName()).formatted(Formatting.GREEN), false);
		for (Text line : punnett(nbt.getInt("Parent"), g, level >= Genome.STAMP_DNA ? Genome.GENES : Genome.MENDEL_GENES,
				Crops.hasStature(seeds))) {
			player.sendMessage(line, false);
		}
		return TypedActionResult.success(book);
	}

	/** Share (0 to 1) of the children of a x b that show the useful form of the gene. */
	public static double share(int a, int b, int gene) {
		int shows = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				int caps = (Genome.alleles(a, gene) >> i & 1) + (Genome.alleles(b, gene) >> j & 1);
				int child = Genome.with(Genome.DEFAULT, gene, caps);
				if (Genome.has(child, gene)) {
					shows++;
				}
			}
		}
		return shows / 4.0;
	}

	/** One line per gene: "Cycle: 1 CC : 2 Cc : 1 cc, 25% early". */
	public static List<Text> punnett(int a, int b, int genes, boolean stature) {
		List<Text> out = new ArrayList<>();
		for (int g = 0; g < genes; g++) {
			if (g == Genome.STATURE && !stature) {
				continue;
			}
			int[] n = new int[3];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					n[(Genome.alleles(a, g) >> i & 1) + (Genome.alleles(b, g) >> j & 1)]++;
				}
			}
			char up = Genome.LETTERS.charAt(g), lo = Character.toLowerCase(up);
			StringBuilder ratio = new StringBuilder();
			String[] kinds = {"" + up + up, "" + up + lo, "" + lo + lo};
			for (int k = 2; k >= 0; k--) {
				if (n[k] > 0) {
					ratio.append(ratio.isEmpty() ? "" : " : ").append(n[k]).append(' ').append(kinds[2 - k]);
				}
			}
			out.add(Text.translatable("gui.periodic_elements.notebook.gene",
					Text.translatable("gene.periodic_elements." + up), ratio.toString(), Math.round(share(a, b, g) * 100),
					Text.translatable("gene.periodic_elements." + up + ".good")));
		}
		return out;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return stack.contains(DataComponentTypes.CUSTOM_DATA);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
