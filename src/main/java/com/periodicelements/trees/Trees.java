package com.periodicelements.trees;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Trees of the world (2.8.0): five real species for every land biome, read from runtime trees.json (tools/trees.py).
 * Each species has a log, stripped log, planks, leaves and a {@link BiomeSaplingBlock} that grows only in its biomes.
 * Fruit drops from the leaves (loot tables); bark comes off when an axe strips the log; sap and resin come from a
 * Tree Tap (com.periodicelements.farm.TreeTapBlockEntity asks {@link #tapped}).
 */
public final class Trees {
	public static final FoliagePlacerType<PalmFoliagePlacer> PALM_FOLIAGE = Registry.register(Registries.FOLIAGE_PLACER_TYPE,
			id("palm_foliage_placer"), new FoliagePlacerType<>(PalmFoliagePlacer.CODEC));
	public static final List<Species> ALL = new ArrayList<>();
	private static final Map<Block, Species> BY_LOG = new HashMap<>();

	/** One tree species and its blocks. {@code tap} is the substance a Tree Tap collects (null: none),
	 * every {@code tapTicks} ticks (0: the Tree Tap default). */
	public record Species(String id, List<String> biomes, TagKey<Biome> tag, Block log, Block stripped, Block planks,
						  Block leaves, BiomeSaplingBlock sapling, @Nullable String fruit, @Nullable String bark,
						  @Nullable String tap, boolean tapFlask, int tapTicks) {
		public Item tapItem() {
			return Registries.ITEM.get(Trees.id(tap));
		}
	}

	private Trees() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** The species whose log (bark on) this is, or null. */
	@Nullable
	public static Species ofLog(Block block) {
		return BY_LOG.get(block);
	}

	/** The species a Tree Tap fixed to this block would tap, or null if the block gives nothing. */
	@Nullable
	public static Species tapped(BlockState log) {
		Species s = BY_LOG.get(log.getBlock());
		return s != null && s.tap() != null ? s : null;
	}

	private static Block block(String name, Block block) {
		return Registry.register(Registries.BLOCK, id(name), block);
	}

	public static void register(List<Item> items) {
		JsonObject root = ElementTable.readObject("/periodic_elements/trees.json");
		for (JsonElement e : root.getAsJsonArray("species")) {
			JsonObject o = e.getAsJsonObject();
			String sid = o.get("id").getAsString();
			List<String> biomes = new ArrayList<>();
			o.getAsJsonArray("biomes").forEach(b -> biomes.add(b.getAsString()));
			TagKey<Biome> tag = TagKey.of(RegistryKeys.BIOME, id("trees/" + sid));
			Block log = block(sid + "_log", new PillarBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_LOG).mapColor(MapColor.BROWN)));
			Block stripped = block("stripped_" + sid + "_log", new PillarBlock(AbstractBlock.Settings.copy(Blocks.STRIPPED_JUNGLE_LOG)
					.mapColor(MapColor.OAK_TAN)));
			Block planks = block(sid + "_planks", new Block(AbstractBlock.Settings.copy(Blocks.JUNGLE_PLANKS).mapColor(MapColor.OAK_TAN)));
			Block leaves = block(sid + "_leaves", new LeavesBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_LEAVES)));
			SaplingGenerator generator = new SaplingGenerator(PeriodicElements.MOD_ID + ":" + sid, Optional.empty(),
					Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("tree/" + sid))), Optional.empty());
			BiomeSaplingBlock sapling = (BiomeSaplingBlock) block(sid + "_sapling", new BiomeSaplingBlock(generator, tag, biomes,
					o.get("soil").getAsString(), AbstractBlock.Settings.copy(Blocks.JUNGLE_SAPLING)));
			for (Block b : List.of(log, stripped, planks, leaves)) {
				items.add(Registry.register(Registries.ITEM, Registries.BLOCK.getId(b), new BlockItem(b, new Item.Settings())));
			}
			items.add(Registry.register(Registries.ITEM, id(sid + "_sapling"), new BiomeSaplingItem(sapling, new Item.Settings())));
			Species s = new Species(sid, biomes, tag, log, stripped, planks, leaves, sapling, str(o, "fruit"), str(o, "bark"),
					str(o, "tap"), o.get("tapFlask").getAsBoolean(), o.get("tapTicks").getAsInt());
			ALL.add(s);
			BY_LOG.put(log, s);

			StrippableBlockRegistry.register(log, stripped);
			FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
			fire.add(log, 5, 5);
			fire.add(stripped, 5, 5);
			fire.add(planks, 5, 20);
			fire.add(leaves, 30, 60);
			CompostingChanceRegistry.INSTANCE.add(leaves, 0.3f);
			CompostingChanceRegistry.INSTANCE.add(sapling, 0.3f);
			int fuel = o.get("fuel").getAsInt();
			if (fuel != 300) {
				FuelRegistry.INSTANCE.add(log, fuel);
				FuelRegistry.INSTANCE.add(stripped, fuel);
			}
			for (JsonElement c : o.getAsJsonArray("classes")) {
				String cls = c.getAsString();
				BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, id("trees/" + sid + "_" + cls))),
						GenerationStep.Feature.FLUID_SPRINGS,
						RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("tree/" + sid + "_" + cls)));
			}
		}

		// 2.9.0-beta.3 (Guilherme): no oaks in the wooded badlands; the data pack also takes the oak out of the jungle and
		// savanna tree mixes (data/minecraft/worldgen/configured_feature) and the apples out of oak and dark oak leaves.
		BiomeModifications.create(id("no_vanilla_oak")).add(ModificationPhase.REMOVALS,
				BiomeSelectors.includeByKey(BiomeKeys.WOODED_BADLANDS),
				ctx -> ctx.getGenerationSettings().removeFeature(GenerationStep.Feature.VEGETAL_DECORATION,
						RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.ofVanilla("trees_badlands"))));

		// Fruit, nuts and bark: some are food.
		for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("products").entrySet()) {
			Item.Settings settings = new Item.Settings();
			JsonElement food = e.getValue().getAsJsonObject().get("food");
			if (food != null && !food.isJsonNull()) {
				settings.food(new FoodComponent.Builder().nutrition(food.getAsJsonArray().get(0).getAsInt())
						.saturationModifier(food.getAsJsonArray().get(1).getAsFloat()).build());
			}
			Item item = Registry.register(Registries.ITEM, id(e.getKey()), new Item(settings));
			items.add(item);
			CompostingChanceRegistry.INSTANCE.add(item, 0.5f);
		}

		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (player.isSpectator()) {
				return ActionResult.PASS;
			}
			ItemStack stack = player.getStackInHand(hand);
			BlockPos pos = hit.getBlockPos();
			BlockState state = world.getBlockState(pos);
			// Bone meal on a sapling out of its biomes: a puff of smoke and a note, and the bone meal is kept.
			if (stack.isOf(Items.BONE_MEAL) && state.getBlock() instanceof BiomeSaplingBlock sapling && !sapling.growsAt(world, pos)) {
				if (world instanceof ServerWorld sw) {
					sw.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.01);
					player.sendMessage(Text.translatable("gui.periodic_elements.sapling.wrong_biome", sapling.biomeNames()), true);
				}
				return ActionResult.FAIL;
			}
			// An axe strips the bark off a log whose bark is worth having, and drops it.
			Species s = BY_LOG.get(state.getBlock());
			if (s != null && s.bark() != null && stack.getItem() instanceof AxeItem) {
				if (!world.isClient) {
					world.setBlockState(pos, s.stripped().getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)),
							Block.NOTIFY_ALL_AND_REDRAW);
					world.playSound(null, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0f, 1.0f);
					Block.dropStack(world, pos, hit.getSide(), new ItemStack(Registries.ITEM.get(id(s.bark()))));
					stack.damage(1, player, LivingEntity.getSlotForHand(hand));
				}
				return ActionResult.success(world.isClient);
			}
			return ActionResult.PASS;
		});
	}

	@Nullable
	private static String str(JsonObject o, String key) {
		JsonElement e = o.get(key);
		return e == null || e.isJsonNull() ? null : e.getAsString();
	}
}
