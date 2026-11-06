package com.periodicelements.creative;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

/**
 * 3.51.0: items for testing the mod, only in the Creative Testing tab: no recipe, no loot, not in the Field Guide.
 * Blocks: Creative Generator, Infinite Fluid Source, Infinite Item Source, Universal Void, Accelerator Clock.
 * Items: Protection Amulet, Growth Wand, Debug Magnifier, Structure Builder.
 */
public final class CreativeItems {
	public static Block GENERATOR, FLUID_SOURCE, ITEM_SOURCE, VOID, ACCELERATOR;
	public static Item AMULET, GROWTH_WAND, MAGNIFIER, BUILDER;
	public static BlockEntityType<CreativeGeneratorBlockEntity> GENERATOR_ENTITY;
	public static BlockEntityType<CreativeBlockEntity> CREATIVE_ENTITY;
	public static final List<Item> ITEMS = new ArrayList<>();

	private CreativeItems() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** Called from ModContent.register(). */
	public static void register() {
		GENERATOR = block("creative_generator", CreativeBlock.Kind.GENERATOR);
		FLUID_SOURCE = block("creative_fluid_source", CreativeBlock.Kind.FLUID_SOURCE);
		ITEM_SOURCE = block("creative_item_source", CreativeBlock.Kind.ITEM_SOURCE);
		VOID = block("creative_void", CreativeBlock.Kind.VOID);
		ACCELERATOR = block("creative_accelerator", CreativeBlock.Kind.ACCELERATOR);
		AMULET = item("protection_amulet", new CreativeTools.Amulet(settings()));
		GROWTH_WAND = item("growth_wand", new CreativeTools.GrowthWand(settings()));
		MAGNIFIER = item("debug_magnifier", new CreativeTools.Magnifier(settings()));
		BUILDER = item("structure_builder", new CreativeTools.Builder(settings()));
		GENERATOR_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("creative_generator"),
				BlockEntityType.Builder.create(CreativeGeneratorBlockEntity::new, GENERATOR).build(null));
		CREATIVE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("creative_block"),
				BlockEntityType.Builder.create(CreativeBlockEntity::new, FLUID_SOURCE, ITEM_SOURCE, VOID, ACCELERATOR).build(null));
	}

	private static Item.Settings settings() {
		return new Item.Settings().maxCount(1).rarity(Rarity.EPIC).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
	}

	private static Block block(String path, CreativeBlock.Kind kind) {
		Block b = Registry.register(Registries.BLOCK, id(path), new CreativeBlock(kind, AbstractBlock.Settings.create()
				.mapColor(MapColor.PURPLE).strength(2.0f, 1200.0f).dropsNothing().sounds(BlockSoundGroup.AMETHYST_BLOCK)));
		item(path, new BlockItem(b, new Item.Settings().rarity(Rarity.EPIC)) {
			@Override
			public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
				tip(this, tooltip);
			}
		});
		return b;
	}

	private static Item item(String path, Item item) {
		Item i = Registry.register(Registries.ITEM, id(path), item);
		ITEMS.add(i);
		return i;
	}

	static void tip(Item item, List<Text> tooltip) {
		tooltip.add(Text.translatable(item.getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.periodic_elements.creative_only").formatted(Formatting.LIGHT_PURPLE));
	}

	/** Does the player carry a Protection Amulet (anywhere in the inventory)? */
	public static boolean shielded(PlayerEntity player) {
		return AMULET != null && player.getInventory().contains(s -> s.isOf(AMULET));
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 10 != 0) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (shielded(player)) {
					protect(player);
				}
			}
		});
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
				!(entity instanceof PlayerEntity p && shielded(p) && blocked(source)));
	}

	/** Clears every harmful effect, fire, freezing, the radiation dose and any infection. */
	static void protect(ServerPlayerEntity player) {
		for (StatusEffectInstance e : List.copyOf(player.getStatusEffects())) {
			if (e.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) {
				player.removeStatusEffect(e.getEffectType());
			}
		}
		player.setFireTicks(0);
		player.setFrozenTicks(0);
		var radiation = com.periodicelements.radiation.RadiationState.get(player.getServer());
		if (radiation.dose(player.getUuid(), System.currentTimeMillis()).recent > 0) {
			radiation.reset(player.getUuid());
		}
	}

	/** Damage the amulet stops: fire, heat, cold, poison and magic, suffocation and drowning, and animals of the mod. */
	static boolean blocked(DamageSource source) {
		if (source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_FREEZING) || source.isIn(DamageTypeTags.IS_DROWNING)
				|| source.isOf(DamageTypes.HOT_FLOOR) || source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.INDIRECT_MAGIC)
				|| source.isOf(DamageTypes.WITHER) || source.isOf(DamageTypes.IN_WALL) || source.isOf(DamageTypes.CACTUS)
				|| source.isOf(DamageTypes.SWEET_BERRY_BUSH) || source.isOf(DamageTypes.DRY_OUT)) {
			return true;
		}
		if (source.getAttacker() instanceof LivingEntity attacker
				&& Registries.ENTITY_TYPE.getId(attacker.getType()).getNamespace().equals(PeriodicElements.MOD_ID)) {
			return true;
		}
		// the mod's own damage types (radiation, infection, thorns of shrubs, venom...)
		return source.getTypeRegistryEntry().getKey().map(k -> k.getValue().getNamespace().equals(PeriodicElements.MOD_ID)).orElse(false);
	}
}
