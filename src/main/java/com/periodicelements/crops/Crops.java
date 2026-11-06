package com.periodicelements.crops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import com.periodicelements.farm.Fertilizer;
import com.periodicelements.farm.InsectSprayItem;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GenerationStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Crops and genetics (3.23.0): the 20 crops of runtime crops.json (tools/crops.py), their genes, the Seed Lens,
 * the Pollination Brush, Tobacco Wash and the Feeding Trough. Wheat, carrots, potatoes and beetroots carry genes too.
 */
public final class Crops {
	public static ComponentType<Integer> GENOME;
	public static final Map<String, GeneticCropBlock> BLOCKS = new LinkedHashMap<>();
	public static final Map<String, Item> ITEMS = new HashMap<>();
	private static final Set<Item> SEEDS = new HashSet<>();
	public static Item SEED_LENS, POLLINATION_BRUSH, TOBACCO_WASH;
	public static Block TROUGH;
	public static BlockEntityType<FeedingTroughBlockEntity> TROUGH_ENTITY;
	public static final String ROTATION = "crop:rotation";
	// 3.37.0: stages 2 and 3 of the crop plan.
	public static ComponentType<Integer> TARGET_GENE;
	public static Item CROSSING_NOTEBOOK, SILAGE, INOCULANT, BALANCED_FEED, COBALT_60_SOURCE, GENE_GUN, CRISPR_KIT;
	public static final Item[] CONSTRUCTS = new Item[Genome.TRANSGENES.length];
	public static Block SILO, SEED_IRRADIATOR, THERMOCYCLER, GAMMA_GARDEN, DNA_SEQUENCER;
	public static BlockEntityType<CropMachineBlockEntity> MACHINE_ENTITY;
	public static final FoodComponent GOLDEN_RICE_FOOD = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f)
			.statusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 600), 1f).build();
	private static final Set<Item> FODDER = new HashSet<>();

	private Crops() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	private static List<String> strings(JsonArray a) {
		List<String> out = new ArrayList<>();
		a.forEach(e -> out.add(e.getAsString()));
		return out;
	}

	private static FoodComponent food(JsonElement food, boolean poison) {
		if (food == null || food.isJsonNull()) {
			return null;
		}
		JsonArray f = food.getAsJsonArray();
		FoodComponent.Builder b = new FoodComponent.Builder().nutrition(f.get(0).getAsInt()).saturationModifier(f.get(1).getAsFloat());
		if (poison) {
			b.statusEffect(new StatusEffectInstance(StatusEffects.POISON, 200), 1f);
		}
		return b.build();
	}

	private static Item item(String path, Item item, List<Item> items) {
		Registry.register(Registries.ITEM, id(path), item);
		ITEMS.put(path, item);
		items.add(item);
		return item;
	}

	public static void register(List<Item> items) {
		GENOME = Registry.register(Registries.DATA_COMPONENT_TYPE, id("genome"),
				ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());
		Genetics.PESTS = GameRuleRegistry.register("periodicElementsCropPests", GameRules.Category.UPDATES,
				GameRuleFactory.createBooleanRule(true));
		JsonObject root = ElementTable.readObject("/periodic_elements/crops.json");
		for (JsonElement e : root.getAsJsonArray("crops")) {
			JsonObject o = e.getAsJsonObject();
			String cid = o.get("id").getAsString();
			String seedId = o.get("seed").getAsString();
			String productId = o.get("product").getAsString();
			boolean poison = o.get("poison").getAsBoolean();
			GeneticCropBlock block = Registry.register(Registries.BLOCK, id(cid + "_crop"),
					new GeneticCropBlock(AbstractBlock.Settings.copy(Blocks.WHEAT)));
			Item.Settings seedSettings = new Item.Settings();
			FoodComponent seedFood = food(o.get(seedId.equals(productId) ? "productFood" : "seedFood"), poison);
			if (seedFood == null && seedId.equals(productId)) {
				seedFood = food(o.get("seedFood"), poison);
			}
			if (seedFood != null) {
				seedSettings.food(seedFood);
			}
			Item seed = item(seedId, new AliasedBlockItem(block, seedSettings), items);
			Item product = seed;
			if (!seedId.equals(productId)) {
				Item.Settings ps = new Item.Settings();
				FoodComponent pf = food(o.get("productFood"), poison);
				if (pf != null) {
					ps.food(pf);
				}
				product = item(productId, new Item(ps), items);
				CompostingChanceRegistry.INSTANCE.add(product, 0.65f);
			}
			CompostingChanceRegistry.INSTANCE.add(seed, 0.3f);
			Item fSeed = seed, fProduct = product;
			JsonElement per = o.get("perennial");
			CropInfo info = new CropInfo(cid, o.get("outcross").getAsDouble(), o.get("clone").getAsBoolean(),
					strings(o.getAsJsonArray("climates")), strings(o.getAsJsonArray("biomes")), o.get("arid").getAsBoolean(),
					o.get("water").getAsBoolean(), o.get("legume").getAsBoolean(), per == null || per.isJsonNull() ? null : per.getAsInt(),
					() -> fSeed, () -> fProduct);
			block.setInfo(info);
			Genetics.INFO.put(block, info);
			BLOCKS.put(cid, block);
			SEEDS.add(seed);
			BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, id("crops/" + cid))),
					GenerationStep.Feature.FLUID_SPRINGS, RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("crop/" + cid)));
		}
		JsonObject vanilla = root.getAsJsonObject("vanilla");
		vanilla("wheat", Blocks.WHEAT, Items.WHEAT_SEEDS, Items.WHEAT, vanilla);
		vanilla("carrots", Blocks.CARROTS, Items.CARROT, Items.CARROT, vanilla);
		vanilla("potatoes", Blocks.POTATOES, Items.POTATO, Items.POTATO, vanilla);
		vanilla("beetroots", Blocks.BEETROOTS, Items.BEETROOT_SEEDS, Items.BEETROOT, vanilla);

		for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("made").entrySet()) {
			String mid = e.getKey();
			JsonElement f = e.getValue().getAsJsonObject().get("food");
			Item.Settings s = new Item.Settings();
			if (mid.equals("tobacco_wash")) {
				TOBACCO_WASH = item(mid, new TobaccoWashItem(s.maxDamage(16)), items);
				continue;
			}
			FoodComponent food = food(f, false);
			if (mid.equals("coffee") && food != null) {
				JsonArray a = f.getAsJsonArray();
				food = new FoodComponent.Builder().nutrition(a.get(0).getAsInt()).saturationModifier(a.get(1).getAsFloat())
						.alwaysEdible().statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200), 1f).build();
			}
			if (food != null) {
				s.food(food);
			}
			item(mid, new Item(s), items);
		}
		SEED_LENS = item("seed_lens", new SeedLensItem(new Item.Settings().maxCount(1)), items);
		POLLINATION_BRUSH = item("pollination_brush", new PollinationBrushItem(new Item.Settings().maxDamage(64)), items);
		TROUGH = Registry.register(Registries.BLOCK, id("feeding_trough"), new FeedingTroughBlock(AbstractBlock.Settings.create()
				.mapColor(MapColor.OAK_TAN).strength(2.0f).sounds(BlockSoundGroup.WOOD).nonOpaque().burnable()));
		item("feeding_trough", new GatedBlockItem(TROUGH, new Item.Settings(),
				known -> Research.allows(known, "item:feeding_trough"), Research.get("animal_husbandry").name()), items);
		TROUGH_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("feeding_trough"),
				BlockEntityType.Builder.create(FeedingTroughBlockEntity::new, TROUGH).build(null));

		registerStage23(items);

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
			if (!(world instanceof ServerWorld sw) || Genetics.info(state.getBlock()) == null) {
				return;
			}
			Long stored = CropGenomes.of(sw).get(pos);
			CropGenomes.of(sw).forget(pos);
			CropInfo info = Genetics.info(state.getBlock());
			// 3.37.0: an inoculated legume (its entry is gone already if it dropped its seeds) fixes NPK-strength nitrogen.
			if (stored != null && (stored & CropGenomes.INOCULATED) != 0 && info.legume() && state.getBlock() instanceof CropBlock c
					&& c.isMature(state) && sw.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
				Fertilizer.of(sw).fertilize(pos.down(), true);
			}
			// Crop rotation: a ripe legume leaves its farmland fertilised for the next crop.
			if (info.legume() && state.getBlock() instanceof CropBlock crop && crop.isMature(state)
					&& sw.getBlockState(pos.down()).isOf(Blocks.FARMLAND)
					&& (player.isCreative() || Research.allows(ResearchAccess.known(player), ROTATION))) {
				Fertilizer f = Fertilizer.of(sw);
				if (f.harvestsLeft(pos.down()) == 0) {
					f.fertilize(pos.down());
				}
			}
		});
		// Insect spray on a crop kills the caterpillars on the plants around it.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			BlockPos pos = hit.getBlockPos();
			if (player.isSpectator() || !(stack.getItem() instanceof InsectSprayItem)
					|| Genetics.info(world.getBlockState(pos).getBlock()) == null) {
				return ActionResult.PASS;
			}
			if (world instanceof ServerWorld sw) {
				int n = Genetics.curePests(sw, pos, TobaccoWashItem.RADIUS);
				player.sendMessage(Text.translatable("gui.periodic_elements.spray.cured", n), true);
			}
			return ActionResult.SUCCESS;
		});
	}

	private static Block gatedBlock(String path, Block block, String unlock, String project, List<Item> items) {
		Registry.register(Registries.BLOCK, id(path), block);
		item(path, new GatedBlockItem(block, new Item.Settings(), known -> Research.allows(known, unlock),
				Research.get(project).name()), items);
		return block;
	}

	/** 3.37.0: crossing notebook, silo, inoculant, irradiator, feed; gamma garden, sequencer, PCR, gene gun, CRISPR. */
	private static void registerStage23(List<Item> items) {
		TARGET_GENE = Registry.register(Registries.DATA_COMPONENT_TYPE, id("target_gene"),
				ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());
		CROSSING_NOTEBOOK = item("crossing_notebook", new CrossingNotebookItem(new Item.Settings().maxCount(1)), items);
		SILAGE = item("silage", new Item(new Item.Settings()), items);
		INOCULANT = item("inoculant", new PlantToolItem(PlantToolItem.Kind.INOCULANT, "item:inoculant", new Item.Settings()), items);
		BALANCED_FEED = item("balanced_feed", new Item(new Item.Settings()), items);
		COBALT_60_SOURCE = item("cobalt_60_source", new Item(new Item.Settings().maxCount(16)), items);
		GENE_GUN = item("gene_gun", new PlantToolItem(PlantToolItem.Kind.GENE_GUN, "item:gene_gun", new Item.Settings().maxDamage(32)), items);
		CRISPR_KIT = item("crispr_kit", new PlantToolItem(PlantToolItem.Kind.CRISPR, "item:crispr_kit", new Item.Settings().maxDamage(16)), items);
		for (int t = 0; t < Genome.TRANSGENES.length; t++) {
			CONSTRUCTS[t] = item(Genome.TRANSGENES[t] + "_gene", new GeneConstructItem(t, new Item.Settings().maxCount(16)), items);
		}
		AbstractBlock.Settings wood = AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable();
		AbstractBlock.Settings metal = AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3.0f, 6.0f)
				.requiresTool().sounds(BlockSoundGroup.METAL);
		SILO = gatedBlock("silo", new CropMachineBlock(CropMachineBlock.Kind.SILO, wood), "item:silo", "silage", items);
		SEED_IRRADIATOR = gatedBlock("seed_irradiator", new CropMachineBlock(CropMachineBlock.Kind.IRRADIATOR, metal),
				"item:seed_irradiator", "x_ray_mutagenesis", items);
		THERMOCYCLER = gatedBlock("thermocycler", new CropMachineBlock(CropMachineBlock.Kind.THERMOCYCLER, metal),
				"item:thermocycler", "pcr", items);
		GAMMA_GARDEN = gatedBlock("gamma_garden", new GammaGardenBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY)
				.strength(5.0f, 12.0f).requiresTool().sounds(BlockSoundGroup.METAL).luminance(s -> 4)), "item:gamma_garden", "gamma_garden", items);
		DNA_SEQUENCER = gatedBlock("dna_sequencer", new DnaSequencerBlock(metal), "item:dna_sequencer", "dna_sequencing", items);
		MACHINE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("crop_machine"),
				BlockEntityType.Builder.create(CropMachineBlockEntity::new, SILO, SEED_IRRADIATOR, THERMOCYCLER).build(null));
		for (String f : new String[]{"maize", "sorghum", "alfalfa"}) {
			FODDER.add(ITEMS.get(f));
		}
		CompostingChanceRegistry.INSTANCE.add(SILAGE, 0.65f);
		CompostingChanceRegistry.INSTANCE.add(BALANCED_FEED, 0.5f);
	}

	/** Maize, sorghum and alfalfa: what goes into a Silo. */
	public static boolean isFodder(ItemStack stack) {
		return !stack.isEmpty() && FODDER.contains(stack.getItem());
	}

	/** Wheat and rice seeds, which carry the stature gene. */
	public static boolean hasStature(ItemStack stack) {
		return stack.isOf(Items.WHEAT_SEEDS) || stack.isOf(ITEMS.get("rice"));
	}

	/** Does the crop at {@code pos} carry the EPSPS transgene (glyphosate does not kill it)? */
	public static boolean glyphosateTolerant(ServerWorld world, BlockPos pos) {
		Long e = CropGenomes.of(world).get(pos);
		return e != null && Genome.transgene(CropGenomes.genome(e), Genome.EPSPS);
	}

	private static void vanilla(String name, Block block, Item seed, Item product, JsonObject rates) {
		double out = rates.get(name).getAsDouble();
		Genetics.INFO.put(block, new CropInfo(name, out, out == 0, null, List.of(), false, false, false, null,
				() -> seed, () -> product));
		SEEDS.add(seed);
	}

	/** Seeds (or tubers, cuttings) of a crop that carries genes. */
	public static boolean isSeed(ItemStack stack) {
		return !stack.isEmpty() && SEEDS.contains(stack.getItem());
	}

	public static Set<Item> seeds() {
		return SEEDS;
	}
}
