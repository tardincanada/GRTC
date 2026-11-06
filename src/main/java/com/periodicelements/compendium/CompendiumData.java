package com.periodicelements.compendium;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the Tree and Plant Compendiums know about, from runtime compendium.json (tools/compendium.py,
 * tools/compendium_plants.py). Both sides. A tree and a plant are the same kind of entry ({@link Tree}), told apart by
 * {@code kind}: "tree" or "plant".
 */
public final class CompendiumData {
	public static final String TREE = "tree", PLANT = "plant", MINERAL = "mineral", ANIMAL = "animal";

	/**
	 * What only a mineral entry has. {@code group}: rocks, sediments, ores, other_worlds; {@code cls}: igneous,
	 * sedimentary, metamorphic or null; {@code items}: items (raw ores) that hide their composition with the blocks;
	 * {@code depth}: {min y, max y} of an ore, or null; {@code origin}, {@code made}, {@code mineral}: whether the
	 * entry has that text.
	 */
	public record Mineral(String id, String group, @Nullable String cls, List<String> items, @Nullable String formula,
						  @Nullable String system, int @Nullable [] depth, boolean real, boolean origin, boolean made,
						  boolean mineral) {
		/** The group the book lists it under: by class once Theory of the Earth is known (rocks and sediments only). */
		public String section(boolean byClass) {
			return byClass && cls != null ? cls : group;
		}
	}

	/** One tree or plant. {@code scientific}, {@code family}, {@code iucn} are null for one of this world only. */
	public record Tree(String kind, String id, boolean vanilla, String icon, @Nullable String scientific, @Nullable String family,
					   double height, @Nullable String iucn, List<String> biomes, List<String> blocks, List<String> products) {
		public String key() {
			return kind + ":" + id;
		}

		/** Its name: tree.periodic_elements.&lt;id&gt;, plant.periodic_elements.&lt;id&gt;, or a mineral's first block's name. */
		public String nameKey() {
			if (MINERAL.equals(kind)) return Registries.BLOCK.get(Identifier.of(blocks.get(0))).getTranslationKey();
			return kind + ".periodic_elements." + id;
		}

		/** compendium.periodic_elements.&lt;kind&gt;.&lt;id&gt; + ".about" / ".range". */
		public String textKey(String part) {
			return "compendium.periodic_elements." + kind + "." + id + "." + part;
		}

		public boolean real() {
			return scientific != null;
		}
	}

	/** One biome of the book; temperature and rain are null where there is no climate to give (caves). */
	public record Biome(String id, @Nullable Integer temperature, @Nullable Integer rain) {
		public String key() {
			return "biome:" + id;
		}
	}

	public static final List<Tree> TREES = new ArrayList<>();
	public static final Map<String, Tree> BY_ID = new LinkedHashMap<>();
	public static final List<Biome> BIOMES = new ArrayList<>();
	public static final Map<String, Biome> BIOME_BY_ID = new LinkedHashMap<>();
	public static final List<Tree> PLANTS = new ArrayList<>();
	public static final Map<String, Tree> PLANT_BY_ID = new LinkedHashMap<>();
	public static final List<Tree> MINERALS = new ArrayList<>();
	public static final Map<String, Tree> MINERAL_BY_ID = new LinkedHashMap<>();
	public static final Map<String, Mineral> MINERAL_INFO = new LinkedHashMap<>();
	/** 3.50.0: the Animal Compendium. An animal's {@code blocks} are its entity type ids (or a small animal's blocks). */
	public static final List<Tree> ANIMALS = new ArrayList<>();
	public static final Map<String, Tree> ANIMAL_BY_ID = new LinkedHashMap<>();
	/** Small animals (insects, worms): the items that note them when carried. */
	public static final Map<String, List<String>> ANIMAL_ITEMS = new LinkedHashMap<>();
	private static Map<net.minecraft.entity.EntityType<?>, Tree> animalByEntity;
	private static Map<Block, Tree> animalByBlock;
	/** The groups of the Mineral Compendium, in the book's order, before and after Theory of the Earth. */
	public static final List<String> GROUPS = List.of("rocks", "sediments", "ores", "other_worlds");
	public static final List<String> CLASS_GROUPS = List.of("igneous", "sedimentary", "metamorphic", "ores", "other_worlds");
	private static Map<Block, Tree> byBlock, plantByBlock, mineralByBlock;
	private static Map<net.minecraft.item.Item, String> naturalItems;

	private CompendiumData() {
	}

	public static void load() {
		if (!TREES.isEmpty()) return;  // trees, plants and minerals load together
		JsonObject root = ElementTable.readObject("/periodic_elements/compendium.json");
		for (JsonElement e : root.getAsJsonArray("trees")) {
			Tree t = entry(TREE, e.getAsJsonObject());
			TREES.add(t);
			BY_ID.put(t.id(), t);
		}
		for (JsonElement e : root.getAsJsonArray("plants")) {
			Tree t = entry(PLANT, e.getAsJsonObject());
			PLANTS.add(t);
			PLANT_BY_ID.put(t.id(), t);
		}
		for (JsonElement e : root.getAsJsonArray("minerals")) {
			JsonObject o = e.getAsJsonObject();
			String id = o.get("id").getAsString();
			String group = o.get("group").getAsString();
			Tree t = new Tree(MINERAL, id, true, o.get("icon").getAsString(), null, null, 0, null, List.of(group),
					list(o, "blocks"), list(o, "gives"));
			MINERALS.add(t);
			MINERAL_BY_ID.put(id, t);
			int[] depth = null;
			if (o.has("depth") && !o.get("depth").isJsonNull()) {
				var a = o.getAsJsonArray("depth");
				depth = new int[]{a.get(0).getAsInt(), a.get(1).getAsInt()};
			}
			MINERAL_INFO.put(id, new Mineral(id, group, str(o, "cls"), list(o, "items"), str(o, "formula"), str(o, "system"),
					depth, o.get("real").getAsBoolean(), o.get("origin").getAsBoolean(), o.get("made").getAsBoolean(),
					o.get("mineral").getAsBoolean()));
		}
		if (root.has("animals")) {
			for (JsonElement e : root.getAsJsonArray("animals")) {
				JsonObject o = e.getAsJsonObject();
				Tree t = entry(ANIMAL, o);
				ANIMALS.add(t);
				ANIMAL_BY_ID.put(t.id(), t);
				if (o.has("items")) ANIMAL_ITEMS.put(t.id(), list(o, "items"));
			}
		}
		for (JsonElement e : root.getAsJsonArray("biomes")) {
			JsonObject o = e.getAsJsonObject();
			Biome b = new Biome(o.get("id").getAsString(), num(o, "temperature"), num(o, "rain"));
			BIOMES.add(b);
			BIOME_BY_ID.put(b.id(), b);
		}
	}

	private static Tree entry(String kind, JsonObject o) {
		return new Tree(kind, o.get("id").getAsString(), o.get("vanilla").getAsBoolean(), o.get("icon").getAsString(),
					str(o, "scientific"), str(o, "family"), o.get("height").getAsDouble(), str(o, "iucn"),
					list(o, "biomes"), list(o, "blocks"), list(o, "products"));
	}

	private static String str(JsonObject o, String k) {
		return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
	}

	private static Integer num(JsonObject o, String k) {
		return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : null;
	}

	private static List<String> list(JsonObject o, String k) {
		List<String> out = new ArrayList<>();
		o.getAsJsonArray(k).forEach(x -> out.add(x.getAsString()));
		return List.copyOf(out);
	}

	private static Map<Block, Tree> blockMap(List<Tree> entries) {
		Map<Block, Tree> m = new HashMap<>();
		for (Tree t : entries) {
			for (String id : t.blocks()) {
				Identifier ident = Identifier.of(id);
				if (Registries.BLOCK.containsId(ident)) {
					m.put(Registries.BLOCK.get(ident), t);
				}
			}
		}
		return m;
	}

	/** The tree a block belongs to (log, stripped log, leaves, sapling), or null. */
	@Nullable
	public static Tree treeOf(Block block) {
		if (byBlock == null) byBlock = blockMap(TREES);
		return byBlock.get(block);
	}

	/** The plant a block belongs to (any of its blocks, crop stages included), or null. */
	@Nullable
	public static Tree plantOf(Block block) {
		if (plantByBlock == null) plantByBlock = blockMap(PLANTS);
		return plantByBlock.get(block);
	}

	/** The rock, sediment or ore a block belongs to, or null. */
	@Nullable
	public static Tree mineralOf(Block block) {
		if (mineralByBlock == null) mineralByBlock = blockMap(MINERALS);
		return mineralByBlock.get(block);
	}

	/** The small animal a block belongs to (a colony, a tray, a bin), or null. */
	@Nullable
	public static Tree animalOf(Block block) {
		if (animalByBlock == null) animalByBlock = blockMap(ANIMALS);
		return animalByBlock.get(block);
	}

	/** The animal an entity type is, or null. */
	@Nullable
	public static Tree animalOf(net.minecraft.entity.EntityType<?> type) {
		if (animalByEntity == null) {
			Map<net.minecraft.entity.EntityType<?>, Tree> m = new HashMap<>();
			for (Tree t : ANIMALS) {
				for (String id : t.blocks()) {
					Identifier ident = Identifier.of(id);
					if (Registries.ENTITY_TYPE.containsId(ident)) m.put(Registries.ENTITY_TYPE.get(ident), t);
				}
			}
			animalByEntity = m;
		}
		return animalByEntity.get(type);
	}

	/** The entry of {@code kind} a block belongs to, or null. */
	@Nullable
	public static Tree of(String kind, Block block) {
		return PLANT.equals(kind) ? plantOf(block) : MINERAL.equals(kind) ? mineralOf(block) : ANIMAL.equals(kind) ? animalOf(block)
				: treeOf(block);
	}

	public static List<Tree> entries(String kind) {
		return PLANT.equals(kind) ? PLANTS : MINERAL.equals(kind) ? MINERALS : ANIMAL.equals(kind) ? ANIMALS : TREES;
	}

	@Nullable
	public static Tree byId(String kind, String id) {
		return PLANT.equals(kind) ? PLANT_BY_ID.get(id) : MINERAL.equals(kind) ? MINERAL_BY_ID.get(id)
				: ANIMAL.equals(kind) ? ANIMAL_BY_ID.get(id) : BY_ID.get(id);
	}

	/** "analysed:&lt;kind&gt;:&lt;id&gt;": the note that the entry was analysed with the Magnifying Lens. */
	public static String analysedKey(Tree t) {
		return "analysed:" + t.key();
	}

	/**
	 * The analysis note a natural item waits for before its tooltip shows its composition (a rock's or plant's block
	 * item, or a raw ore), or null for anything made, which always shows it.
	 */
	@Nullable
	public static String naturalKey(net.minecraft.item.Item item) {
		if (naturalItems == null) {
			Map<net.minecraft.item.Item, String> m = new HashMap<>();
			for (List<Tree> list : List.of(MINERALS, PLANTS)) {
				for (Tree t : list) {
					for (String b : t.blocks()) {
						Identifier ident = Identifier.of(b);
						if (Registries.ITEM.containsId(ident)) m.put(Registries.ITEM.get(ident), analysedKey(t));
					}
					Mineral info = MINERAL_INFO.get(t.id());
					if (t.kind().equals(MINERAL) && info != null) {
						for (String i : info.items()) {
							Identifier ident = Identifier.of(i);
							if (Registries.ITEM.containsId(ident)) m.put(Registries.ITEM.get(ident), analysedKey(t));
						}
					}
				}
			}
			m.remove(net.minecraft.item.Items.AIR);
			naturalItems = m;
		}
		return naturalItems.get(item);
	}

	/** The trees of one biome, in the book's order. */
	public static List<Tree> treesOf(String biome) {
		return of(TREE, biome);
	}

	/** The entries of {@code kind} in one biome, in the book's order. */
	public static List<Tree> of(String kind, String biome) {
		List<Tree> out = new ArrayList<>();
		for (Tree t : entries(kind)) {
			if (t.biomes().contains(biome)) out.add(t);
		}
		return out;
	}

	/** The minerals of one group or class (see {@link Mineral#section}), in the book's order. */
	public static List<Tree> mineralsIn(String section, boolean byClass) {
		List<Tree> out = new ArrayList<>();
		for (Tree t : MINERALS) {
			if (MINERAL_INFO.get(t.id()).section(byClass).equals(section)) out.add(t);
		}
		return out;
	}

	/** The biomes a book of {@code kind} lists: those with at least one of its entries. */
	public static List<Biome> biomes(String kind) {
		List<Biome> out = new ArrayList<>();
		for (Biome b : BIOMES) {
			if (!of(kind, b.id()).isEmpty()) out.add(b);
		}
		return out;
	}
}
