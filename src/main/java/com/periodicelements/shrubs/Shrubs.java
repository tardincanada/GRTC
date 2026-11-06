package com.periodicelements.shrubs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.GenerationStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shrubs of the world (3.16.0): read from runtime shrubs.json (tools/shrubs.py). Each species is a {@link ShrubBlock}
 * with a cutting item; products are items, and seeds that can be sown plant the bush. Also the Greenhouse Heater.
 */
public final class Shrubs {
	public static final List<ShrubBlock> ALL = new ArrayList<>();
	public static final Map<String, ShrubBlock> BY_ID = new HashMap<>();
	public static GreenhouseHeaterBlock HEATER;
	public static BlockEntityType<GreenhouseHeaterBlockEntity> HEATER_ENTITY;
	public static int burnDivisor = 4;
	public static GreenhouseCoolerBlock COOLER;
	public static BlockEntityType<GreenhouseCoolerBlockEntity> COOLER_ENTITY;
	/** Ticks of cold per item put in a cooler (3.22.0). */
	public static final Map<String, Integer> coolerIce = new HashMap<>();
	public static double coolerPower = 1;
	public static final List<TeaItem> TEAS = new ArrayList<>();

	private Shrubs() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	private static net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect(String id) {
		return Registries.STATUS_EFFECT.getEntry(Identifier.of(id)).orElseThrow(() -> new IllegalStateException("no effect " + id));
	}

	public static void register(List<Item> items) {
		JsonObject root = ElementTable.readObject("/periodic_elements/shrubs.json");
		burnDivisor = root.get("heaterBurnDivisor").getAsInt();
		root.getAsJsonObject("coolerIce").entrySet().forEach(e -> coolerIce.put(e.getKey(), e.getValue().getAsInt()));
		coolerPower = root.get("coolerPower").getAsDouble();
		HydrangeaBlock.blueItems = new java.util.HashSet<>();
		root.getAsJsonArray("hydrangeaBlue").forEach(e -> HydrangeaBlock.blueItems.add(e.getAsString()));
		HydrangeaBlock.pinkItems = new java.util.HashSet<>();
		root.getAsJsonArray("hydrangeaPink").forEach(e -> HydrangeaBlock.pinkItems.add(e.getAsString()));
		JsonObject products = root.getAsJsonObject("products");
		Map<String, Item> productItems = new HashMap<>();
		for (JsonElement e : root.getAsJsonArray("shrubs")) {
			JsonObject o = e.getAsJsonObject();
			String sid = o.get("id").getAsString();
			List<String> biomes = new ArrayList<>();
			o.getAsJsonArray("biomes").forEach(b -> biomes.add(b.getAsString()));
			String pid = o.get("product").isJsonNull() ? null : o.get("product").getAsString();
			AbstractBlock.Settings bs = AbstractBlock.Settings.copy(Blocks.SWEET_BERRY_BUSH).mapColor(MapColor.DARK_GREEN);
			java.util.function.Supplier<Item> product = () -> pid == null ? null : productItems.get(pid);
			ShrubBlock bush = Registry.register(Registries.BLOCK, id(sid + "_bush"), sid.equals("hydrangea")
					? new HydrangeaBlock(sid, biomes, o.get("soil").getAsString(), o.get("climate").getAsString(),
					o.get("thorny").getAsBoolean(), product, bs)
					: new ShrubBlock(sid, biomes, o.get("soil").getAsString(), o.get("climate").getAsString(),
					o.get("thorny").getAsBoolean(), product, bs));
			Item cutting = Registry.register(Registries.ITEM, id(sid + "_cutting"), new ShrubCuttingItem(bush,
					"item.periodic_elements." + sid + "_cutting", new Item.Settings()));
			bush.setCutting(cutting);
			items.add(cutting);
			ALL.add(bush);
			BY_ID.put(sid, bush);
			FlammableBlockRegistry.getDefaultInstance().add(bush, 60, 100);
			CompostingChanceRegistry.INSTANCE.add(cutting, 0.3f);
			BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, id("shrubs/" + sid))),
					GenerationStep.Feature.FLUID_SPRINGS, RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("shrub/" + sid)));
		}
		for (Map.Entry<String, JsonElement> e : products.entrySet()) {
			JsonObject p = e.getValue().getAsJsonObject();
			Item.Settings settings = new Item.Settings();
			JsonElement food = p.get("food");
			if (food != null && !food.isJsonNull()) {
				FoodComponent.Builder fb = new FoodComponent.Builder().nutrition(food.getAsJsonArray().get(0).getAsInt())
						.saturationModifier(food.getAsJsonArray().get(1).getAsFloat());
				JsonElement effect = p.get("effect");
				if (effect != null && !effect.isJsonNull()) {
					var a = effect.getAsJsonArray();
					fb.statusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(Identifier.of(a.get(0).getAsString())).orElseThrow(),
							a.get(1).getAsInt(), 0), a.get(2).getAsFloat());
				}
				settings.food(fb.build());
			}
			JsonElement sow = p.get("sow");
			Item item = sow != null && !sow.isJsonNull()
					? new ShrubCuttingItem(BY_ID.get(sow.getAsString()), "item.periodic_elements." + e.getKey(), settings)
					: new Item(settings);
			Registry.register(Registries.ITEM, id(e.getKey()), item);
			productItems.put(e.getKey(), item);
			items.add(item);
			CompostingChanceRegistry.INSTANCE.add(item, 0.3f);
		}

		// Teas (3.22.0).
		for (JsonElement e : root.getAsJsonArray("teas")) {
			JsonObject t = e.getAsJsonObject();
			TeaItem tea = new TeaItem(effect(t.get("effect").getAsString()), t.get("ticks").getAsInt(), effect(t.get("cures").getAsString()),
					TeaItem.settings());
			Registry.register(Registries.ITEM, id(t.get("id").getAsString()), tea);
			TEAS.add(tea);
			items.add(tea);
		}

		// Greenhouse Cooler (3.22.0, Cold Greenhouse).
		COOLER = Registry.register(Registries.BLOCK, id("greenhouse_cooler"), new GreenhouseCoolerBlock(AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY).strength(3.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)));
		items.add(Registry.register(Registries.ITEM, id("greenhouse_cooler"), new GatedBlockItem(COOLER, new Item.Settings(),
				known -> Research.allows(known, "item:greenhouse_cooler"), Research.get("cold_greenhouse").name())));
		COOLER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("greenhouse_cooler"),
				BlockEntityType.Builder.create(GreenhouseCoolerBlockEntity::new, COOLER).build(null));

		HEATER = Registry.register(Registries.BLOCK, id("greenhouse_heater"), new GreenhouseHeaterBlock(AbstractBlock.Settings.create()
				.mapColor(MapColor.RED).strength(2.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.STONE)
				.luminance(s -> s.get(GreenhouseHeaterBlock.LIT) ? 9 : 0)));
		items.add(Registry.register(Registries.ITEM, id("greenhouse_heater"), new GatedBlockItem(HEATER, new Item.Settings(),
				known -> Research.allows(known, "item:greenhouse_heater"), Research.get("heated_greenhouse").name())));
		HEATER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("greenhouse_heater"),
				BlockEntityType.Builder.create(GreenhouseHeaterBlockEntity::new, HEATER).build(null));

		// Bone meal on a shrub that cannot grow here: a puff of smoke and a note, and the bone meal is kept.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			BlockPos pos = hit.getBlockPos();
			BlockState state = world.getBlockState(pos);
			if (!player.isSpectator() && stack.isOf(Items.BONE_MEAL) && state.getBlock() instanceof ShrubBlock bush
					&& state.get(ShrubBlock.AGE) < 3 && !bush.growsAt(world, pos)) {
				if (world instanceof ServerWorld sw) {
					sw.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.01);
					player.sendMessage(Text.translatable("gui.periodic_elements.shrub.wrong_place", bush.biomeNames()), true);
				}
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});
	}
}
