package com.periodicelements.fauna;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import com.periodicelements.compendium.Compendium;
import com.periodicelements.explore.Explore;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import com.periodicelements.shrubs.Greenhouse;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Animals of the world (3.50.0, the fauna plan). Everything is read from runtime fauna.json (tools/fauna.py): the
 * species (entity types, spawns, behaviour, products), the items and the blocks of the stages built so far.
 *
 * <p>Guilherme's rules: out of its climate an animal breeds, but slowly ({@link #SLOW_BREEDING}); a greenhouse of the
 * right climate (heater for warm animals, cooler for cold ones) brings back normal breeding. Only the scorpion (at
 * night) and the poison frog (to the touch) are dangerous; large animals are neutral (they fight back).
 */
public final class Fauna {
	/** Ticks before an animal bred out of its climate can breed again (vanilla: 6000). */
	public static final int SLOW_BREEDING = 18000;
	/** Average ticks between two manure drops of a penned herbivore, and how near a fence must be. */
	public static final int MANURE_TICKS = 6000, PEN_REACH = 4;
	/** Chance per scan (every 100 ticks) that a player wading in swamp water picks up a leech. */
	public static final float LEECH_CHANCE = 0.04f;

	/** One species, from fauna.json. {@code climate}: warm, temperate or cold (as {@link Greenhouse#climateOf}). */
	public record Species(String id, int stage, String kind, String body, String behaviour, List<String> biomes, int min,
						  int max, int weight, float width, float height, double hp, double speed, double attack,
						  List<String> food, Set<String> traits, String group, int egg1, int egg2, @Nullable String bucket,
						  String climate) {
		public boolean has(String trait) {
			return traits.contains(trait);
		}

		/** The value of a "name:value" trait (eggs:..., venom:..., shear:...), or null. */
		@Nullable
		public String trait(String name) {
			for (String t : traits) {
				if (t.startsWith(name + ":")) return t.substring(name.length() + 1);
			}
			return null;
		}

		public boolean water() {
			return kind.equals("water") || kind.equals("school");
		}
	}

	public static final Map<String, Species> SPECIES = new LinkedHashMap<>();
	public static final Map<String, EntityType<?>> TYPES = new LinkedHashMap<>();
	private static final Map<EntityType<?>, Species> BY_TYPE = new HashMap<>();
	public static final Map<String, Item> ITEMS = new LinkedHashMap<>();
	public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
	public static final List<Item> GROUP = new ArrayList<>();
	public static int STAGE;
	public static RegistryEntry<StatusEffect> LEECH;
	public static final TagKey<EntityType<?>> MANURE_ANIMALS = TagKey.of(RegistryKeys.ENTITY_TYPE, id("manure_animals"));

	private Fauna() {
	}

	static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	@Nullable
	public static Species of(EntityType<?> type) {
		return BY_TYPE.get(type);
	}

	@Nullable
	public static Item item(String id) {
		return ITEMS.get(id);
	}

	public static Block block(String id) {
		return BLOCKS.get(id);
	}

	private static List<String> list(JsonObject o, String k) {
		List<String> out = new ArrayList<>();
		if (o.has(k) && o.get(k).isJsonArray()) o.getAsJsonArray(k).forEach(x -> out.add(x.getAsString()));
		return List.copyOf(out);
	}

	public static void load() {
		if (!SPECIES.isEmpty()) return;
		JsonObject root = ElementTable.readObject("/periodic_elements/fauna.json");
		STAGE = root.get("stage").getAsInt();
		for (JsonElement e : root.getAsJsonArray("species")) {
			JsonObject o = e.getAsJsonObject();
			List<String> biomes = list(o, "biomes");
			var group = o.getAsJsonArray("group");
			var size = o.getAsJsonArray("size");
			var egg = o.getAsJsonArray("egg");
			Species s = new Species(o.get("id").getAsString(), o.get("stage").getAsInt(), o.get("kind").getAsString(),
					o.get("body").getAsString(), o.get("behaviour").getAsString(), biomes, group.get(0).getAsInt(),
					group.get(1).getAsInt(), o.get("weight").getAsInt(), size.get(0).getAsFloat(), size.get(1).getAsFloat(),
					o.get("hp").getAsDouble(), o.get("speed").getAsDouble(), o.get("attack").getAsDouble(), list(o, "food"),
					Set.copyOf(list(o, "traits")), o.get("spawn_group").getAsString(), egg.get(0).getAsInt(), egg.get(1).getAsInt(),
					o.has("bucket") && !o.get("bucket").isJsonNull() ? o.get("bucket").getAsString() : null,
					Greenhouse.climateOf(biomes));
			SPECIES.put(s.id(), s);
		}
	}

	private static Item register(String name, Item item) {
		Registry.register(Registries.ITEM, id(name), item);
		ITEMS.put(name, item);
		GROUP.add(item);
		return item;
	}

	public static void register(List<Item> modItems) {
		load();
		JsonObject root = ElementTable.readObject("/periodic_elements/fauna.json");
		JsonObject gated = root.getAsJsonObject("gated");
		for (String k : gated.keySet()) Explore.PROJECTS.put(k, gated.get(k).getAsString());

		LEECH = Registry.registerReference(Registries.STATUS_EFFECT, id("leech"), new LeechEffect());

		// Items.
		for (JsonElement e : root.getAsJsonArray("items")) {
			JsonObject o = e.getAsJsonObject();
			String iid = o.get("id").getAsString();
			Item.Settings settings = new Item.Settings();
			if (o.has("food") && !o.get("food").isJsonNull()) {
				var f = o.getAsJsonArray("food");
				FoodComponent.Builder food = new FoodComponent.Builder().nutrition(f.get(0).getAsInt())
						.saturationModifier(f.get(1).getAsFloat());
				for (JsonElement fx : o.getAsJsonArray("effects")) {
					var a = fx.getAsJsonArray();
					var effect = Registries.STATUS_EFFECT.getEntry(Identifier.of(a.get(0).getAsString())).orElseThrow();
					food.statusEffect(new StatusEffectInstance(effect, a.get(1).getAsInt(), a.get(2).getAsInt()), 1f);
				}
				settings.food(food.build());
			}
			boolean tip = o.get("tip").getAsBoolean();
			Item item = switch (iid) {
				case "manure", "humus" -> new FaunaItems.Fertilizer(settings);
				case "antivenom" -> new FaunaItems.Antivenom(settings.maxCount(16));
				case "cochineal" -> new FaunaItems.ColonyStarter(settings, "cochineal_colony", Blocks.CACTUS);
				case "sticklac" -> new FaunaItems.ColonyStarter(settings, "lac_colony", Blocks.JUNGLE_LOG);
				default -> tip ? new FaunaItems.Described(settings) : new Item(settings);
			};
			register(iid, item);
		}
		Item manure = ITEMS.get("manure");
		if (manure != null) FuelRegistry.INSTANCE.add(manure, 400);

		// Blocks.
		for (JsonElement e : root.getAsJsonArray("blocks")) {
			String bid = e.getAsString();
			Block block = switch (bid) {
				case "cochineal_colony" -> new ColonyBlock(AbstractBlock.Settings.create().mapColor(MapColor.WHITE).noCollision()
						.strength(0.2f).sounds(BlockSoundGroup.WOOL).ticksRandomly().nonOpaque().pistonBehavior(PistonBehavior.DESTROY),
						Blocks.CACTUS, "cochineal");
				case "lac_colony" -> new ColonyBlock(AbstractBlock.Settings.create().mapColor(MapColor.BROWN).noCollision()
						.strength(0.2f).sounds(BlockSoundGroup.HONEY).ticksRandomly().nonOpaque().pistonBehavior(PistonBehavior.DESTROY),
						Blocks.JUNGLE_LOG, "sticklac");
				case "sericulture_tray" -> new SericultureTrayBlock(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN)
						.strength(0.6f).sounds(BlockSoundGroup.WOOD).ticksRandomly().nonOpaque());
				case "worm_bin" -> new WormBinBlock(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN)
						.strength(1.0f).sounds(BlockSoundGroup.WOOD).ticksRandomly().nonOpaque());
				case "guano" -> new GuanoBlock(AbstractBlock.Settings.create().mapColor(MapColor.WHITE_GRAY).replaceable()
						.strength(0.2f).sounds(BlockSoundGroup.SAND).pistonBehavior(PistonBehavior.DESTROY)
						.blockVision((st, w, p) -> st.get(net.minecraft.block.SnowBlock.LAYERS) >= 8));
				case "beaver_dam" -> new Block(AbstractBlock.Settings.create().mapColor(MapColor.BROWN).strength(1.5f)
						.sounds(BlockSoundGroup.WOOD).burnable());
				case "reindeer_lichen" -> new LichenBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).noCollision()
						.breakInstantly().sounds(BlockSoundGroup.MOSS_CARPET).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY));
				case "pearl_oyster" -> new PearlOysterBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(1.0f)
						.sounds(BlockSoundGroup.STONE).nonOpaque().pistonBehavior(PistonBehavior.DESTROY));
				default -> new Block(AbstractBlock.Settings.create().strength(0.5f));
			};
			Registry.register(Registries.BLOCK, id(bid), block);
			BLOCKS.put(bid, block);
			if (bid.endsWith("_colony")) continue;  // started with cochineal or sticklac, not placed as a block
			Item item = Explore.PROJECTS.containsKey(bid)
					? new GatedBlockItem(block, new Item.Settings(), known -> Research.allows(known, "item:" + bid),
					Research.get(Explore.PROJECTS.get(bid)).name())
					: new FaunaItems.DescribedBlock(block, new Item.Settings());
			register(bid, item);
		}

		// Entities.
		for (Species s : SPECIES.values()) {
			EntityType<? extends MobEntity> type;
			SpawnGroup group = switch (s.group()) {
				case "water_creature" -> SpawnGroup.WATER_CREATURE;
				case "water_ambient" -> SpawnGroup.WATER_AMBIENT;
				default -> SpawnGroup.CREATURE;
			};
			if (s.kind().equals("school")) {
				EntityType<FaunaSchool> t = EntityType.Builder.create(FaunaSchool::new, group).dimensions(s.width(), s.height())
						.maxTrackingRange(4).build();
				type = t;
				FabricDefaultAttributeRegistry.register(t, FishEntity.createFishAttributes()
						.add(EntityAttributes.GENERIC_MAX_HEALTH, s.hp()));
				SpawnRestriction.register(t, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
						WaterCreatureEntity::canSpawn);
			} else if (s.water()) {
				EntityType<FaunaFish> t = EntityType.Builder.create(FaunaFish::new, group).dimensions(s.width(), s.height())
						.maxTrackingRange(s.width() > 2 ? 10 : 6).build();
				type = t;
				FabricDefaultAttributeRegistry.register(t, FishEntity.createFishAttributes()
						.add(EntityAttributes.GENERIC_MAX_HEALTH, s.hp())
						.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, s.speed())
						.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, Math.max(1, s.attack())));
				SpawnRestriction.register(t, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
						WaterCreatureEntity::canSpawn);
			} else {
				EntityType<FaunaAnimal> t = EntityType.Builder.create(FaunaAnimal::new, group).dimensions(s.width(), s.height())
						.maxTrackingRange(10).build();
				type = t;
				FabricDefaultAttributeRegistry.register(t, MobEntity.createMobAttributes()
						.add(EntityAttributes.GENERIC_MAX_HEALTH, s.hp())
						.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, s.speed())
						.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, Math.max(1, s.attack()))
						.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16));
				SpawnRestriction.register(t, SpawnLocationTypes.ON_GROUND, s.has("leaves")
						? Heightmap.Type.MOTION_BLOCKING : Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Fauna::canSpawnOnLand);
			}
			Registry.register(Registries.ENTITY_TYPE, id(s.id()), type);
			TYPES.put(s.id(), type);
			BY_TYPE.put(type, s);
			register(s.id() + "_spawn_egg", new SpawnEggItem(type, s.egg1(), s.egg2(), new Item.Settings()));
			if (s.bucket() != null) {
				// stage 4: herring, cuttlefish and jellyfish are caught in a water bucket
				register(s.bucket(), new net.minecraft.item.EntityBucketItem(type, net.minecraft.fluid.Fluids.WATER,
						net.minecraft.sound.SoundEvents.ITEM_BUCKET_EMPTY_FISH, new Item.Settings().maxCount(1)));
			}
			List<RegistryKey<Biome>> keys = new ArrayList<>();
			for (String b : s.biomes()) keys.add(RegistryKey.of(RegistryKeys.BIOME, b.contains(":") ? Identifier.of(b) : Identifier.ofVanilla(b)));
			BiomeModifications.addSpawn(BiomeSelectors.includeByKey(keys), group, type, s.weight(), s.min(), s.max());
		}


		registerEvents();
	}

	/** A land animal spawns on any solid block (sand and stone too), the sloth on leaves; day animals want light. */
	static boolean canSpawnOnLand(EntityType<? extends MobEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		Species s = of(type);
		BlockState below = world.getBlockState(pos.down());
		if (s != null && s.has("leaves")) return below.isIn(BlockTags.LEAVES) || below.isIn(BlockTags.LOGS);
		if (!below.isSideSolidFullSquare(world, pos.down(), Direction.UP) || !world.getFluidState(pos).isEmpty() && s != null
				&& !s.kind().equals("amphibious")) {
			return false;
		}
		return s != null && s.behaviour().equals("night") || world.getBaseLightLevel(pos, 0) > 8;
	}

	/** Whether {@code pos} suits an animal of {@code s}: one of its biomes, or a greenhouse of its climate. */
	public static boolean inClimate(World world, BlockPos pos, Species s) {
		var key = world.getBiome(pos).getKey();
		if (key.isPresent()) {
			Identifier b = key.get().getValue();
			String name = b.getNamespace().equals("minecraft") ? b.getPath() : b.toString();
			if (s.biomes().contains(name)) return true;
		}
		String here = Greenhouse.climateAt(world, pos);
		return here != null && Greenhouse.allows(world, pos, s.climate())
				&& (s.climate().equals(Greenhouse.TEMPERATE) || here.equals(s.climate()));
	}

	/** Whether a fence (a pen) stands within {@link #PEN_REACH} blocks. */
	public static boolean penned(World world, BlockPos pos) {
		for (BlockPos p : BlockPos.iterate(pos.add(-PEN_REACH, -1, -PEN_REACH), pos.add(PEN_REACH, 1, PEN_REACH))) {
			BlockState s = world.getBlockState(p);
			if (s.isIn(BlockTags.FENCES) || s.isIn(BlockTags.FENCE_GATES)) return true;
		}
		return false;
	}

	private static boolean manureAnimal(AnimalEntity a) {
		if (a.getType().isIn(MANURE_ANIMALS)) return true;
		Species s = of(a.getType());
		return s != null && s.has("manure");
	}

	private static void registerEvents() {
		ServerTickEvents.END_WORLD_TICK.register(Fauna::tickWorld);
		// Earthworms come up with dirt, silkworms with sheared white mulberry leaves.
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
			if (world.isClient || player.isCreative()) return;
			Item drop = null;
			float chance = 0;
			if (ITEMS.containsKey("earthworm") && (state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK)
					|| state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.PODZOL))) {
				drop = ITEMS.get("earthworm");
				chance = 0.05f;
			} else if (ITEMS.containsKey("silkworm") && player.getMainHandStack().isOf(Items.SHEARS)
					&& Registries.BLOCK.getId(state.getBlock()).getPath().equals("white_mulberry_leaves")) {
				drop = ITEMS.get("silkworm");
				chance = 0.2f;
			}
			if (drop != null && world.random.nextFloat() < chance) {
				Block.dropStack(world, pos, new ItemStack(drop));
			}
		});
		// Salt takes a leech off; the leech is kept.
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (LEECH == null || !player.hasStatusEffect(LEECH) || !Registries.ITEM.getId(stack.getItem()).getPath().equals("sodium_chloride")) {
				return TypedActionResult.pass(stack);
			}
			if (!world.isClient) {
				player.removeStatusEffect(LEECH);
				if (!player.getAbilities().creativeMode) stack.decrement(1);
				Item leech = ITEMS.get("leech");
				if (leech != null && !player.getInventory().insertStack(new ItemStack(leech))) {
					player.dropItem(new ItemStack(leech), false);
				}
			}
			return TypedActionResult.success(stack, world.isClient);
		});
	}

	private static void tickWorld(ServerWorld world) {
		long time = world.getTime();
		if (time % 100 != 0) return;
		Random r = world.random;
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (p.isSpectator()) continue;
			// Leeches in swamp water.
			if (LEECH != null && ITEMS.containsKey("leech") && p.isTouchingWater() && !p.hasStatusEffect(LEECH)
					&& r.nextFloat() < LEECH_CHANCE && world.getBiome(p.getBlockPos()).getKey()
					.map(k -> k.getValue().getPath().contains("swamp")).orElse(false)) {
				p.addStatusEffect(new StatusEffectInstance(LEECH, 2400, 0));
				p.sendMessage(Text.translatable("message.periodic_elements.fauna.leech").formatted(Formatting.RED), true);
			}
			// Penned herbivores leave manure (on average once every MANURE_TICKS).
			if (time % 600 == 0 && ITEMS.containsKey("manure")) {
				for (AnimalEntity a : world.getEntitiesByClass(AnimalEntity.class, new Box(p.getBlockPos()).expand(48),
						a -> !a.isBaby() && manureAnimal(a))) {
					if (r.nextInt(MANURE_TICKS / 600) == 0 && penned(world, a.getBlockPos())) {
						a.dropStack(new ItemStack(ITEMS.get("manure")));
					}
				}
			}
			// Colonies of scale insects appear on cacti and jungle trees near the player.
			if (time % 400 == 0) {
				ColonyBlock.seed(world, p.getBlockPos(), r);
				LichenBlock.seed(world, p.getBlockPos(), r);
				PearlOysterBlock.seed(world, p.getBlockPos(), r);
				PearlOysterBlock.ambergris(world, p.getBlockPos(), r);
				PearlOysterBlock.strand(world, p.getBlockPos(), r);
			}
		}
	}
}
