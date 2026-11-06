package com.periodicelements.farm;

import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

/**
 * Farm and fat (1.7.0): the rubber tree and its Tree Tap, tallow candles, soap and the Clean effect, Fertilized
 * Farmland, Sneakers and Rubber Wheels. Assets and data come from tools/farm.py.
 */
public final class Farm {
	public static final String WHEELS_TAG = "pe_rubber_wheels";
	public static final double WHEELS_SPEED = 1.25;
	/** Soap keeps you Clean for ten minutes. */
	public static final int CLEAN_TICKS = 12000;
	public static final TagKey<Biome> RUBBER_BIOMES = TagKey.of(RegistryKeys.BIOME, id("rubber_tree_biomes"));
	public static final TagKey<Item> SOAP = TagKey.of(RegistryKeys.ITEM, id("soap"));
	public static final TagKey<Item> FERTILIZER = TagKey.of(RegistryKeys.ITEM, id("fertilizer"));

	public static Block RUBBER_LOG;
	public static Block RUBBER_LEAVES;
	public static Block RUBBER_PLANKS;
	public static Block RUBBER_SAPLING;
	public static Block TREE_TAP;
	public static Block TALLOW_CANDLE;
	public static BlockEntityType<TreeTapBlockEntity> TAP_ENTITY;
	public static Item LATEX;
	public static Item SNEAKERS;
	public static Item RUBBER_WHEELS;
	public static RegistryEntry<StatusEffect> CLEAN;
	public static Block TARMAC;
	public static Block MINING_CHARGE;
	public static Item INSECT_SPRAY;
	public static Item BORDEAUX;
	public static Item CARBOLIC_SOAP;
	private static int tapTicks = 6000;
	private static int tapHolds = 4;
	private static int candleTicks = 72000;
	private static int fertilizerHarvests = 8;

	private Farm() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	public static int tapTicks() {
		return tapTicks;
	}

	public static int tapHolds() {
		return tapHolds;
	}

	public static int candleTicks() {
		return candleTicks;
	}

	public static int fertilizerHarvests() {
		return fertilizerHarvests;
	}

	private static Block block(String name, Block block, List<Item> items) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new BlockItem(block, new Item.Settings())));
		return block;
	}

	private static Block gated(String name, Block block, String unlock, String project, List<Item> items) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new GatedBlockItem(block, new Item.Settings(),
				known -> Research.allows(known, unlock), Research.get(project).name())));
		return block;
	}

	public static void register(List<Item> items) {
		JsonObject cfg = ElementTable.readObject("/periodic_elements/farm.json");
		tapTicks = cfg.get("tapTicks").getAsInt();
		tapHolds = cfg.get("tapHolds").getAsInt();
		candleTicks = cfg.get("candleTicks").getAsInt();
		fertilizerHarvests = cfg.get("fertilizerHarvests").getAsInt();
		LATEX = Registries.ITEM.get(id("latex"));

		RUBBER_LOG = block("rubber_log", new PillarBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_LOG).mapColor(MapColor.PALE_YELLOW)), items);
		RUBBER_PLANKS = block("rubber_planks", new Block(AbstractBlock.Settings.copy(Blocks.JUNGLE_PLANKS).mapColor(MapColor.PALE_YELLOW)), items);
		RUBBER_LEAVES = block("rubber_leaves", new LeavesBlock(AbstractBlock.Settings.copy(Blocks.JUNGLE_LEAVES)), items);
		SaplingGenerator tree = new SaplingGenerator(PeriodicElements.MOD_ID + ":rubber", Optional.empty(),
				Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("rubber_tree"))), Optional.empty());
		// 2.8.0: like the new trees, the rubber sapling grows only where rubber trees grow wild.
		RUBBER_SAPLING = Registry.register(Registries.BLOCK, id("rubber_sapling"), new com.periodicelements.trees.BiomeSaplingBlock(tree,
				RUBBER_BIOMES, List.of("jungle", "sparse_jungle", "bamboo_jungle"), "dirt", AbstractBlock.Settings.copy(Blocks.JUNGLE_SAPLING)));
		items.add(Registry.register(Registries.ITEM, id("rubber_sapling"), new com.periodicelements.trees.BiomeSaplingItem(
				(com.periodicelements.trees.BiomeSaplingBlock) RUBBER_SAPLING, new Item.Settings())));
		TREE_TAP = gated("tree_tap", new TreeTapBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(1.0f).nonOpaque().sounds(BlockSoundGroup.METAL)), "item:tree_tap", "vulcanization", items);
		TALLOW_CANDLE = gated("tallow_candle", new TallowCandleBlock(AbstractBlock.Settings.create().mapColor(MapColor.WHITE_GRAY)
				.strength(0.1f).nonOpaque().noCollision().luminance(state -> 12).sounds(BlockSoundGroup.CANDLE)),
				"item:tallow_candle", "rendering", items);
		TAP_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("tree_tap"),
				BlockEntityType.Builder.create(TreeTapBlockEntity::new, TREE_TAP).build(null));

		// Sneakers: canvas and leather on vulcanized rubber soles, worth one point of armor.
		EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
		for (ArmorItem.Type t : ArmorItem.Type.values()) {
			defense.put(t, t == ArmorItem.Type.BOOTS ? 1 : 0);
		}
		RegistryEntry<ArmorMaterial> rubber = Registry.registerReference(Registries.ARMOR_MATERIAL, id("sneakers"),
				new ArmorMaterial(defense, 10, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
						() -> Ingredient.ofItems(Registries.ITEM.get(id("vulcanized_rubber"))),
						List.of(new ArmorMaterial.Layer(id("sneakers"))), 0, 0));
		SNEAKERS = Registry.register(Registries.ITEM, id("sneakers"), new TippedArmor(rubber,
				new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(10))));
		items.add(SNEAKERS);
		RUBBER_WHEELS = Registry.register(Registries.ITEM, id("rubber_wheels"),
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(16)));
		items.add(RUBBER_WHEELS);
		CLEAN = Registry.registerReference(Registries.STATUS_EFFECT, id("clean"), new CleanEffect());

		// 1.8.0: coal tar and dyes.
		TARMAC = gated("tarmac", new Block(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(1.5f, 6.0f)
				.requiresTool().velocityMultiplier(1.1f).sounds(BlockSoundGroup.STONE)), "item:tarmac", "tar_distillation", items);
		MINING_CHARGE = gated("mining_charge", new MiningChargeBlock(AbstractBlock.Settings.create().mapColor(MapColor.RED)
				.strength(0.5f).sounds(BlockSoundGroup.GRASS)), "item:mining_charge", "dynamite", items);
		INSECT_SPRAY = Registry.register(Registries.ITEM, id("insect_spray"), new InsectSprayItem(new Item.Settings().maxDamage(32)));
		items.add(INSECT_SPRAY);
		BORDEAUX = Registries.ITEM.get(id("bordeaux_mixture"));
		CARBOLIC_SOAP = Registries.ITEM.get(id("carbolic_soap"));
		Industry.register(items);
		Life.register(items);
		com.periodicelements.medicine.Infection.register();
		com.periodicelements.medicine.Medicine.register();
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			BlockPos pos = hit.getBlockPos();
			BlockState state = world.getBlockState(pos);
			if (!stack.isOf(BORDEAUX) || player.isSpectator()) {
				return ActionResult.PASS;
			}
			if (state.isOf(Blocks.FARMLAND)) {
				pos = pos.up();
				state = world.getBlockState(pos);
			}
			if (!(state.getBlock() instanceof CropBlock) && !state.isOf(Blocks.SWEET_BERRY_BUSH)) {
				return ActionResult.PASS;
			}
			if (world instanceof ServerWorld sw) {
				Sprayed.of(sw).spray(pos);
				stack.decrementUnlessCreative(1, player);
				sw.spawnParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0);
				player.sendMessage(Text.translatable("gui.periodic_elements.sprayed"), true);
			}
			return ActionResult.SUCCESS;
		});

		// Rubber tree fix (after 3.4.0-beta.1): placed before the vanilla jungle canopy, like the 2.8.0 trees, or the sapling check fails on leaves.
		BiomeModifications.addFeature(BiomeSelectors.tag(RUBBER_BIOMES), GenerationStep.Feature.FLUID_SPRINGS,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("rubber_tree")));
		FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
		fire.add(RUBBER_LOG, 5, 5);
		fire.add(RUBBER_PLANKS, 5, 20);
		fire.add(RUBBER_LEAVES, 30, 60);
		CompostingChanceRegistry.INSTANCE.add(RUBBER_LEAVES, 0.3f);
		CompostingChanceRegistry.INSTANCE.add(RUBBER_SAPLING, 0.3f);
		CompostingChanceRegistry.INSTANCE.add(Registries.ITEM.get(id("protein_meal")), 0.65f);
		CompostingChanceRegistry.INSTANCE.add(Registries.ITEM.get(id("oil_cake")), 0.65f);
		CompostingChanceRegistry.INSTANCE.add(Registries.ITEM.get(id("spent_bark")), 0.3f);
		// Vanilla furnaces burn spent tan bark like a plank and a half.
		FuelRegistry.INSTANCE.add(Registries.ITEM.get(id("spent_bark")), 450);

		UseItemCallback.EVENT.register(Farm::useSoap);
		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(RUBBER_WHEELS) || !(entity instanceof AbstractMinecartEntity cart)) {
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				if (cart.getCommandTags().contains(WHEELS_TAG)) {
					player.sendMessage(Text.translatable("gui.periodic_elements.wheels.already"), true);
				} else {
					cart.addCommandTag(WHEELS_TAG);
					stack.decrementUnlessCreative(1, player);
					world.playSound(null, cart.getBlockPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.NEUTRAL, 0.4f, 1.6f);
					player.sendMessage(Text.translatable("gui.periodic_elements.wheels"), true);
				}
			}
			return ActionResult.SUCCESS;
		});
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isIn(FERTILIZER) || player.isSpectator()) {
				return ActionResult.PASS;
			}
			BlockPos pos = hit.getBlockPos();
			if (world.getBlockState(pos).getBlock() instanceof CropBlock) {
				pos = pos.down();
			}
			if (!world.getBlockState(pos).isOf(Blocks.FARMLAND)) {
				return ActionResult.PASS;
			}
			if (world instanceof ServerWorld sw) {
				Fertilizer f = Fertilizer.of(sw);
				boolean npk = stack.isOf(Industry.NPK_PRILLS);
				f.fertilize(pos, npk);
				stack.decrementUnlessCreative(1, player);
				sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 8, 0.3, 0.1, 0.3, 0);
				world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1f, 1f);
				player.sendMessage(Text.translatable(f.strong(pos) ? "gui.periodic_elements.fertilized.npk"
						: "gui.periodic_elements.fertilized", f.harvestsLeft(pos)), true);
			}
			return ActionResult.SUCCESS;
		});
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
			harvest(world, pos, state);
			if (world instanceof ServerWorld sw) {
				Sprayed.of(sw).forget(pos);
			}
		});
		ServerTickEvents.END_WORLD_TICK.register(world -> Fertilizer.of(world).tick(world));
	}

	/** A ripe crop broken on fertilized farmland drops one more of its crop. */
	public static void harvest(World world, BlockPos pos, BlockState state) {
		if (!(world instanceof ServerWorld sw) || !(state.getBlock() instanceof CropBlock crop) || !crop.isMature(state)) {
			return;
		}
		Fertilizer f = Fertilizer.of(sw);
		if (f.harvestsLeft(pos.down()) > 0) {
			Block.dropStack(world, pos, new ItemStack(Fertilizer.cropOf(state)));
			f.harvested(pos.down());
		}
	}

	/** Using soap: with dyed leather in the other hand, scrub the dye out; otherwise wash and be Clean. */
	private static TypedActionResult<ItemStack> useSoap(PlayerEntity player, World world, Hand hand) {
		ItemStack soap = player.getStackInHand(hand);
		if (!soap.isIn(SOAP) || player.isSpectator()) {
			return TypedActionResult.pass(soap);
		}
		ItemStack other = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if (!world.isClient) {
			if (other.contains(DataComponentTypes.DYED_COLOR)) {
				other.remove(DataComponentTypes.DYED_COLOR);
				player.sendMessage(Text.translatable("gui.periodic_elements.undyed"), true);
			} else {
				player.addStatusEffect(new StatusEffectInstance(CLEAN, CLEAN_TICKS));
				player.sendMessage(Text.translatable("gui.periodic_elements.clean"), true);
				if (soap.isOf(CARBOLIC_SOAP)) {
					com.periodicelements.medicine.Infection.cureEarly(player); // Lister's antiseptic
				}
			}
			world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 0.6f, 1.2f);
			soap.decrementUnlessCreative(1, player);
		}
		return TypedActionResult.success(soap, world.isClient);
	}

	/** Armor with a ".tip" tooltip line. */
	public static class TippedArmor extends ArmorItem {
		public TippedArmor(RegistryEntry<ArmorMaterial> material, Settings settings) {
			super(material, Type.BOOTS, settings);
		}

		public TippedArmor(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
			super(material, type, settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
			tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(net.minecraft.util.Formatting.GRAY));
		}
	}
}
