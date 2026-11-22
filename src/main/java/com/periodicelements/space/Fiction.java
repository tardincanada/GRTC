package com.periodicelements.space;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Level 10, stage 6 (3.67.0): the materials of science fiction. Each starts from a real idea and goes one step past
 * what anyone has made; the Field Guide says where the science stops.
 * <ul>
 * <li>Room-temperature superconductor: the Magnetic Shield, which turns cosmic rays aside round a base.</li>
 * <li>Element 126 on the island of stability: Unbihexium and its alloy, and the Thermal Suit made of it.</li>
 * <li>Metallic hydrogen, antihydrogen and helium-3 fusion: three engines ({@link Propellant}) and the Annihilation
 * Reactor (a power block, kind "annihilation").</li>
 * </ul>
 */
public final class Fiction {
	/** A Magnetic Shield guards everything within this many blocks. */
	public static final int SHIELD_RANGE = 16;
	/** Share of the cosmic dose that gets through a shield's field. */
	public static final double SHIELD_PASS = 0.1;
	/** Item ids whose tooltip says they are fiction. */
	public static final Set<String> FICTION = Set.of("magnetic_shield", "unbihexium", "stability_alloy_ingot",
			"thermal_suit_helmet", "thermal_suit_chestplate", "thermal_suit_leggings", "thermal_suit_boots",
			"metallic_hydrogen_engine", "antimatter_engine", "fusion_drive", "annihilation_reactor",
			"room_temperature_superconductor", "metallic_hydrogen", "antihydrogen");

	public static Block MAGNETIC_SHIELD;
	public static BlockEntityType<ShieldEntity> SHIELD_ENTITY;
	public static Item UNBIHEXIUM, STABILITY_ALLOY;
	public static Item[] THERMAL_SUIT = new Item[4];

	/** Loaded shields, by world. */
	private static final Map<RegistryKey<World>, Set<BlockPos>> SHIELDS = new ConcurrentHashMap<>();

	private Fiction() {
	}

	/** The Magnetic Shield: a coil of warm superconductor. Its field glows faintly where it turns particles aside. */
	public static class ShieldBlock extends Block implements BlockEntityProvider {
		public ShieldBlock(Settings settings) {
			super(settings);
		}

		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new ShieldEntity(pos, state);
		}
	}

	/** Only keeps the shield's place in {@link #SHIELDS} while its chunk is loaded. */
	public static class ShieldEntity extends BlockEntity {
		public ShieldEntity(BlockPos pos, BlockState state) {
			super(SHIELD_ENTITY, pos, state);
		}

		@Override
		public void setWorld(World world) {
			super.setWorld(world);
			if (!world.isClient) {
				SHIELDS.computeIfAbsent(world.getRegistryKey(), k -> ConcurrentHashMap.newKeySet()).add(pos.toImmutable());
			}
		}

		@Override
		public void markRemoved() {
			super.markRemoved();
			if (world != null && !world.isClient) {
				Set<BlockPos> s = SHIELDS.get(world.getRegistryKey());
				if (s != null) {
					s.remove(pos);
				}
			}
		}

		@Override
		public void cancelRemoval() {
			super.cancelRemoval();
			if (world != null && !world.isClient) {
				SHIELDS.computeIfAbsent(world.getRegistryKey(), k -> ConcurrentHashMap.newKeySet()).add(pos.toImmutable());
			}
		}
	}

	/** Is {@code pos} inside the field of a Magnetic Shield? */
	public static boolean shielded(World world, BlockPos pos) {
		Set<BlockPos> s = SHIELDS.get(world.getRegistryKey());
		if (s == null || s.isEmpty()) {
			return false;
		}
		for (BlockPos p : s) {
			if (p.getSquaredDistance(pos) <= SHIELD_RANGE * SHIELD_RANGE && world.getBlockState(p).isOf(MAGNETIC_SHIELD)) {
				return true;
			}
		}
		return false;
	}

	/** All four pieces of the Thermal Suit. */
	public static boolean wearingThermalSuit(LivingEntity e) {
		return e.getEquippedStack(EquipmentSlot.HEAD).isOf(THERMAL_SUIT[0]) && e.getEquippedStack(EquipmentSlot.CHEST).isOf(THERMAL_SUIT[1])
				&& e.getEquippedStack(EquipmentSlot.LEGS).isOf(THERMAL_SUIT[2]) && e.getEquippedStack(EquipmentSlot.FEET).isOf(THERMAL_SUIT[3]);
	}

	static void register(List<Item> items) {
		MAGNETIC_SHIELD = Space.gatedBlock(items, "magnetic_shield", "room_temperature_superconductivity",
				new ShieldBlock(AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(3f, 6f).requiresTool()
						.sounds(BlockSoundGroup.METAL).luminance(s -> 7)));
		SHIELD_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Moon.id("magnetic_shield"),
				BlockEntityType.Builder.create(ShieldEntity::new, MAGNETIC_SHIELD).build(null));
		UNBIHEXIUM = Space.item(items, "unbihexium", "island_of_stability",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings()));
		STABILITY_ALLOY = Space.item(items, "stability_alloy_ingot", "island_of_stability",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings()));

		// The Thermal Suit: the Space Suit's air and oxygen inside a shell of element-126 alloy
		EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
		defense.put(ArmorItem.Type.HELMET, 3);
		defense.put(ArmorItem.Type.CHESTPLATE, 7);
		defense.put(ArmorItem.Type.LEGGINGS, 5);
		defense.put(ArmorItem.Type.BOOTS, 3);
		defense.put(ArmorItem.Type.BODY, 7);
		RegistryEntry<ArmorMaterial> suit = Registry.registerReference(Registries.ARMOR_MATERIAL, Moon.id("thermal_suit"),
				new ArmorMaterial(defense, 12, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
						() -> Ingredient.ofItems(STABILITY_ALLOY), List.of(new ArmorMaterial.Layer(Moon.id("thermal_suit"))), 2f, 0.1f));
		String[] names = {"thermal_suit_helmet", "thermal_suit_chestplate", "thermal_suit_leggings", "thermal_suit_boots"};
		ArmorItem.Type[] types = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
		for (int i = 0; i < 4; i++) {
			THERMAL_SUIT[i] = Space.item(items, names[i], "island_of_stability", new Moon.SpaceSuitItem(suit, types[i],
					new Item.Settings().maxCount(1).fireproof()));
		}
		// The suit keeps out the heat of fire and of hot ground (465 °C on Venus, 430 °C on Mercury), not lava at 1200 °C
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
				!((source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE) || source.isOf(DamageTypes.HOT_FLOOR))
						&& wearingThermalSuit(entity)));
	}
}
