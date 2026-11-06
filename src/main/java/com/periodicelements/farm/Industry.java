package com.periodicelements.farm;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Industrial organics (1.9.0): the Demolition Charge, Bakelite blocks, the Carbide Lamp and its light, the Welding
 * Torch, the neoprene Wetsuit and NPK prills. Assets and data come from tools/industry.py.
 */
public final class Industry {
	public static final String[] BAKELITE = {"brown_bakelite", "black_bakelite", "red_bakelite", "green_bakelite",
			"ivory_bakelite", "amber_bakelite"};
	/** Wetsuit armour points: hood, jacket, leggings, boots. */
	public static final Map<ArmorItem.Type, Integer> WETSUIT_DEFENSE = Map.of(ArmorItem.Type.HELMET, 2,
			ArmorItem.Type.CHESTPLATE, 4, ArmorItem.Type.LEGGINGS, 3, ArmorItem.Type.BOOTS, 1, ArmorItem.Type.BODY, 0);

	public static Block DEMOLITION_CHARGE;
	public static Block LAMP_LIGHT;
	public static Item CARBIDE_LAMP;
	public static Item WELDING_TORCH;
	public static Item NPK_PRILLS;
	public static Item CALCIUM_CARBIDE;
	public static Item SULFANILAMIDE;
	public static Item[] WETSUIT = new Item[4];

	private Industry() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	public static void register(List<Item> items) {
		NPK_PRILLS = Registries.ITEM.get(id("npk_prills"));
		CALCIUM_CARBIDE = Registries.ITEM.get(id("calcium_carbide"));
		SULFANILAMIDE = Registries.ITEM.get(id("sulfanilamide"));

		DEMOLITION_CHARGE = Registry.register(Registries.BLOCK, id("demolition_charge"), new DemolitionChargeBlock(
				AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(1.0f).sounds(BlockSoundGroup.METAL)));
		items.add(Registry.register(Registries.ITEM, id("demolition_charge"), new GatedBlockItem(DEMOLITION_CHARGE,
				new Item.Settings(), known -> Research.allows(known, "item:demolition_charge"), Research.get("tnt").name())));
		for (String b : BAKELITE) {
			Block block = Registry.register(Registries.BLOCK, id(b), new Block(AbstractBlock.Settings.create()
					.mapColor(MapColor.BROWN).strength(1.5f, 4.0f).requiresTool().sounds(BlockSoundGroup.STONE)));
			items.add(Registry.register(Registries.ITEM, id(b), new BlockItem(block, new Item.Settings())));
		}
		LAMP_LIGHT = Registry.register(Registries.BLOCK, id("lamp_light"), new LampLightBlock(AbstractBlock.Settings.create()
				.replaceable().noCollision().dropsNothing().nonOpaque().luminance(s -> LampLightBlock.LIGHT)
				.pistonBehavior(PistonBehavior.DESTROY).strength(0f)));

		CARBIDE_LAMP = Registry.register(Registries.ITEM, id("carbide_lamp"),
				new CarbideLampItem(new Item.Settings().maxDamage(CarbideLampItem.BURN_SECONDS)));
		items.add(CARBIDE_LAMP);
		WELDING_TORCH = Registry.register(Registries.ITEM, id("welding_torch"),
				new WeldingTorchItem(new Item.Settings().maxDamage(WeldingTorchItem.WELDS)));
		items.add(WELDING_TORCH);

		EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(WETSUIT_DEFENSE);
		RegistryEntry<ArmorMaterial> neoprene = Registry.registerReference(Registries.ARMOR_MATERIAL, id("wetsuit"),
				new ArmorMaterial(defense, 10, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
						() -> Ingredient.ofItems(Registries.ITEM.get(id("neoprene"))),
						List.of(new ArmorMaterial.Layer(id("wetsuit"))), 0, 0));
		String[] names = {"wetsuit_hood", "wetsuit_jacket", "wetsuit_leggings", "wetsuit_boots"};
		ArmorItem.Type[] types = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
		for (int i = 0; i < 4; i++) {
			WETSUIT[i] = Registry.register(Registries.ITEM, id(names[i]), new Farm.TippedArmor(neoprene, types[i],
					new Item.Settings().maxDamage(types[i].getMaxDamage(12))));
			items.add(WETSUIT[i]);
		}

		// Vanilla furnaces burn the new fuels too.
		FuelRegistry.INSTANCE.add(Registries.ITEM.get(id("biodiesel")), 2000);
		FuelRegistry.INSTANCE.add(Registries.ITEM.get(id("butanol")), 1600);

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			long t = world.getTime();
			if (t % 10 != 0) {
				return;
			}
			for (ServerPlayerEntity p : world.getPlayers()) {
				if (!p.isSpectator()) {
					tickPlayer(world, p, t % 20 == 0);
				}
			}
		});
	}

	/** Every half second: the Carbide Lamp lights the block the player stands in, and the Wetsuit helps a swimmer. */
	static void tickPlayer(ServerWorld world, PlayerEntity p, boolean burn) {
		ItemStack lamp = litLamp(p);
		if (lamp != null) {
			LampLightBlock.lightAround(world, p);
			if (burn && !p.getAbilities().creativeMode) {
				lamp.setDamage(lamp.getDamage() + 1);
			}
		}
		if (wearingWetsuit(p) && p.isTouchingWater()) {
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 40, 0, true, false, true));
		}
	}

	/** The lit Carbide Lamp the player holds or wears, or null. */
	public static ItemStack litLamp(PlayerEntity p) {
		for (ItemStack s : new ItemStack[]{p.getEquippedStack(EquipmentSlot.HEAD), p.getMainHandStack(), p.getOffHandStack()}) {
			if (CarbideLampItem.lit(s)) {
				return s;
			}
		}
		return null;
	}

	public static boolean wearingWetsuit(PlayerEntity p) {
		return p.getEquippedStack(EquipmentSlot.HEAD).isOf(WETSUIT[0]) && p.getEquippedStack(EquipmentSlot.CHEST).isOf(WETSUIT[1])
				&& p.getEquippedStack(EquipmentSlot.LEGS).isOf(WETSUIT[2]) && p.getEquippedStack(EquipmentSlot.FEET).isOf(WETSUIT[3]);
	}
}
