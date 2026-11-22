package com.periodicelements.space;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.List;

/**
 * Level 9, stage 4 (3.64.0): the Moon. Its dimension (MoonChunkGenerator), rocks and ice, real low gravity, vacuum and
 * the Space Suit, the crewed flight there and back (with Space and RocketEntity), and the Emergency Return Capsule.
 * The pressurized base is LifeSupportBlockEntity.
 */
public final class Moon {
	public static final RegistryKey<World> MOON = RegistryKey.of(RegistryKeys.WORLD, id("moon"));

	/** Lunar gravity is 1.62 m/s², a sixth of the Earth's. */
	public static final double GRAVITY = 1.0 / 6;
	/** Ticks of oxygen a full suit holds, and a cylinder gives (10 minutes). */
	public static final int SUIT_OXYGEN = 12000;
	/** Air lost per tick in vacuum (vanilla gives back 4 a tick on land, and holds 300). */
	public static final int VACUUM_AIR_LOSS = 14;

	public static Block LUNAR_REGOLITH, ANORTHOSITE, MARE_BASALT, LUNAR_ICE, LIFE_SUPPORT, KREEP_BASALT;
	public static Item[] SPACE_SUIT = new Item[4];
	public static Item CREW_CAPSULE, EMERGENCY_CAPSULE;
	public static BlockEntityType<LifeSupportBlockEntity> LIFE_SUPPORT_ENTITY;

	private static final Identifier GRAVITY_ID = id("moon_gravity");
	private static final Identifier FALL_ID = id("moon_fall");
	private static final Identifier JUMP_ID = id("space_suit_jump");

	private Moon() {
	}

	static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** Is this world without breathable air (the Moon, and Mars with its thin carbon dioxide)? */
	public static boolean airless(World world) {
		RegistryKey<World> k = world.getRegistryKey();
		// 3.68.0: Mercury has no air at all; Venus's air at 50 km is carbon dioxide, at a breathable pressure but not breathable
		return k == MOON || k == Mars.MARS || k == Mercury.MERCURY || k == Venus.VENUS;
	}

	/** The pull of gravity in {@code world}, as a share of the Earth's. */
	public static double gravityOf(World world) {
		RegistryKey<World> k = world.getRegistryKey();
		return k == MOON ? GRAVITY : k == Mars.MARS ? Mars.GRAVITY : k == Mercury.MERCURY ? Mercury.GRAVITY
				: k == Venus.VENUS ? Venus.GRAVITY : 1;
	}

	private static AbstractBlock.Settings rock(MapColor colour, float hardness) {
		return AbstractBlock.Settings.create().mapColor(colour).strength(hardness, 6f).requiresTool().sounds(BlockSoundGroup.STONE);
	}

	/** The Space Suit's pieces; the chest piece holds the oxygen and shows it as its bar. */
	public static class SpaceSuitItem extends ArmorItem {
		public SpaceSuitItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
			super(material, type, settings);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
			if (getType() == Type.CHESTPLATE) {
				tooltip.add(Text.translatable("item.periodic_elements.space_suit.oxygen", oxygen(stack) / 20)
						.formatted(Formatting.AQUA));
			}
		}

		@Override
		public boolean isItemBarVisible(ItemStack stack) {
			return getType() == Type.CHESTPLATE;
		}

		@Override
		public int getItemBarStep(ItemStack stack) {
			return Math.round(13f * oxygen(stack) / SUIT_OXYGEN);
		}

		@Override
		public int getItemBarColor(ItemStack stack) {
			return 0x60B8FF;
		}
	}

	public static int oxygen(ItemStack suit) {
		NbtComponent c = suit.get(DataComponentTypes.CUSTOM_DATA);
		return c == null ? 0 : c.copyNbt().getInt("oxygen");
	}

	public static void setOxygen(ItemStack suit, int ticks) {
		NbtCompound n = suit.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		n.putInt("oxygen", Math.max(0, Math.min(SUIT_OXYGEN, ticks)));
		suit.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
	}

	public static boolean wearingSuit(LivingEntity p) {
		// 3.67.0: any suit counts, the Thermal Suit as well, piece for piece (a helmet of one and boots of the other too)
		return suitPiece(p, EquipmentSlot.HEAD) && suitPiece(p, EquipmentSlot.CHEST) && suitPiece(p, EquipmentSlot.LEGS)
				&& suitPiece(p, EquipmentSlot.FEET);
	}

	private static boolean suitPiece(LivingEntity p, EquipmentSlot slot) {
		return p.getEquippedStack(slot).getItem() instanceof SpaceSuitItem s && s.getSlotType() == slot;
	}

	/** The Emergency Return Capsule: brings you home from another world, leaving your pack (not your hotbar) behind. */
	static class EmergencyCapsuleItem extends com.periodicelements.ModContent.TippedItem {
		EmergencyCapsuleItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			ItemStack stack = user.getStackInHand(hand);
			if (world.isClient || !(user instanceof ServerPlayerEntity p)) {
				return TypedActionResult.success(stack, world.isClient);
			}
			if (world.getRegistryKey() == World.OVERWORLD) {
				p.sendMessage(Text.translatable("message.periodic_elements.moon.emergency_home").formatted(Formatting.YELLOW), true);
				return TypedActionResult.fail(stack);
			}
			BlockPos at = p.getBlockPos();
			int left = leaveCargo((ServerWorld) world, p, at);
			stack.decrement(1);
			ServerWorld home = p.getServer().getOverworld();
			goTo(p, home, at.getX(), at.getZ());
			p.sendMessage(Text.translatable("message.periodic_elements.moon.emergency", left, at.getX(), at.getY(), at.getZ())
					.formatted(Formatting.GOLD), false);
			return TypedActionResult.success(stack);
		}
	}

	/** Puts the main inventory (not the hotbar, armour or off hand) into a chest where the player stands; returns stacks left. */
	static int leaveCargo(ServerWorld world, ServerPlayerEntity p, BlockPos at) {
		var inv = p.getInventory();
		int n = 0;
		for (int i = 9; i < 36; i++) {
			if (!inv.getStack(i).isEmpty()) {
				n++;
			}
		}
		if (n == 0) {
			return 0;
		}
		world.setBlockState(at, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, p.getHorizontalFacing().getOpposite()));
		if (world.getBlockEntity(at) instanceof ChestBlockEntity chest) {
			int slot = 0;
			for (int i = 9; i < 36; i++) {
				if (!inv.getStack(i).isEmpty()) {
					chest.setStack(slot++, inv.removeStack(i));
				}
			}
		}
		return n;
	}

	/** A natural block of another world: no research needed, its ".desc" line as tooltip. */
	static Block rockBlock(List<Item> items, String name, Block block) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new net.minecraft.item.BlockItem(block, new Item.Settings()) {
			@Override
			public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
				tooltip.add(Text.translatable(getBlock().getTranslationKey() + ".desc").formatted(Formatting.GRAY));
			}
		}));
		return block;
	}

	static void register(List<Item> items) {
		LUNAR_REGOLITH = rockBlock(items, "lunar_regolith", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.LIGHT_GRAY).strength(0.6f).sounds(BlockSoundGroup.SAND)));
		ANORTHOSITE = rockBlock(items, "anorthosite", new Block(rock(MapColor.WHITE_GRAY, 1.5f)));
		MARE_BASALT = rockBlock(items, "mare_basalt", new Block(rock(MapColor.DEEPSLATE_GRAY, 1.8f)));
		KREEP_BASALT = rockBlock(items, "kreep_basalt", new Block(rock(MapColor.TERRACOTTA_GRAY, 1.8f)));   // 3.65.0
		LUNAR_ICE = rockBlock(items, "lunar_ice", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.PALE_PURPLE).strength(0.8f).sounds(BlockSoundGroup.GLASS).slipperiness(0.98f)));
		LIFE_SUPPORT = Space.gatedBlock(items, "life_support", "life_support", new LifeSupportBlock(AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE_GRAY).strength(3f).requiresTool().sounds(BlockSoundGroup.METAL)
				.luminance(s -> s.get(LifeSupportBlock.LIT) ? 6 : 0)));
		LIFE_SUPPORT_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("life_support"),
				BlockEntityType.Builder.create(LifeSupportBlockEntity::new, LIFE_SUPPORT).build(null));

		// The Space Suit (Apollo A7L): aramid and nylon layers, a helmet visor of polycarbonate, a backpack of oxygen.
		EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
		defense.put(ArmorItem.Type.HELMET, 2);
		defense.put(ArmorItem.Type.CHESTPLATE, 4);
		defense.put(ArmorItem.Type.LEGGINGS, 3);
		defense.put(ArmorItem.Type.BOOTS, 2);
		defense.put(ArmorItem.Type.BODY, 4);
		RegistryEntry<ArmorMaterial> suit = Registry.registerReference(Registries.ARMOR_MATERIAL, id("space_suit"),
				new ArmorMaterial(defense, 8, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
						() -> Ingredient.ofItems(Registries.ITEM.get(id("aramid"))), List.of(new ArmorMaterial.Layer(id("space_suit"))), 1f, 0));
		String[] names = {"space_suit_helmet", "space_suit_chestplate", "space_suit_leggings", "space_suit_boots"};
		ArmorItem.Type[] types = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
		for (int i = 0; i < 4; i++) {
			SPACE_SUIT[i] = Space.item(items, names[i], "space_suit", new SpaceSuitItem(suit, types[i],
					new Item.Settings().maxCount(1)));
		}
		CREW_CAPSULE = Space.item(items, "crew_capsule", "lunar_landing",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		EMERGENCY_CAPSULE = Space.item(items, "emergency_capsule", "lunar_landing", new EmergencyCapsuleItem(new Item.Settings().maxCount(1)));

		Registry.register(Registries.CHUNK_GENERATOR, id("moon"), MoonChunkGenerator.CODEC);
		Vacuum.register();

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			boolean airless = airless(world);
			double g = gravityOf(world);
			for (ServerPlayerEntity p : world.getPlayers()) {
				gravity(p, g);
				if (airless) {
					breathe(world, p);
				}
			}
			if (g == 1) {
				return;
			}
			for (var e : world.iterateEntities()) {
				if (e instanceof LivingEntity l && !(l instanceof PlayerEntity) && world.getTime() % 20 == 0) {
					gravity(l, g);
				}
			}
		});
		// a full oxygen cylinder used while wearing the suit refills its backpack
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack held = player.getStackInHand(hand);
			if (!held.isOf(Registries.ITEM.get(id("oxygen_cylinder")))) {
				return TypedActionResult.pass(held);
			}
			ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
			if (!(chest.getItem() instanceof SpaceSuitItem si && si.getType() == ArmorItem.Type.CHESTPLATE)
					|| oxygen(chest) >= SUIT_OXYGEN - 20) {
				return TypedActionResult.pass(held);
			}
			if (!world.isClient) {
				setOxygen(chest, SUIT_OXYGEN);
				world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1.6f);
				if (!player.getAbilities().creativeMode) {
					held.decrement(1);
					ItemStack empty = new ItemStack(Registries.ITEM.get(id("steel_cylinder")));
					if (!player.getInventory().insertStack(empty)) {
						player.dropItem(empty, false);
					}
				}
				player.sendMessage(Text.translatable("message.periodic_elements.moon.suit_filled").formatted(Formatting.AQUA), true);
			}
			return TypedActionResult.success(held, world.isClient);
		});
	}

	private static void modifier(LivingEntity e, RegistryEntry<EntityAttribute> attr, Identifier mid, double value,
								 EntityAttributeModifier.Operation op, boolean on) {
		EntityAttributeInstance inst = e.getAttributeInstance(attr);
		if (inst == null) {
			return;
		}
		EntityAttributeModifier has = inst.getModifier(mid);
		if (has != null && (!on || has.value() != value)) {   // off, or another world's pull
			inst.removeModifier(mid);
			has = null;
		}
		if (on && has == null) {
			inst.addTemporaryModifier(new EntityAttributeModifier(mid, value, op));
		}
	}

	/** Lunar gravity: a sixth of the pull, so jumps go six times higher and falls hurt a sixth as much. */
	public static void gravity(LivingEntity e, boolean moon) {
		gravity(e, moon ? GRAVITY : 1);
	}

	/** Gravity {@code g} times the Earth's (1 takes the change away): jumps go higher and falls hurt less in proportion. */
	public static void gravity(LivingEntity e, double g) {
		boolean low = g < 1;
		modifier(e, EntityAttributes.GENERIC_GRAVITY, GRAVITY_ID, g - 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, low);
		modifier(e, EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, FALL_ID, g - 1,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, low);
		// the stiff pressurized suit takes some spring out of a jump
		modifier(e, EntityAttributes.GENERIC_JUMP_STRENGTH, JUMP_ID, -0.4, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
				low && wearingSuit(e));
	}

	/** Vacuum: a player breathes in a sealed, supplied base or in a full suit with oxygen; otherwise the air runs out. */
	public static void breathe(ServerWorld world, ServerPlayerEntity p) {
		if (!p.isCreative() && !p.isSpectator()) {
			breathe(world, p, true);
		}
	}

	/** One tick of breathing in vacuum for a player who needs air (tests call it directly). */
	public static void breathe(ServerWorld world, ServerPlayerEntity p, boolean needsAir) {
		if (!needsAir) {
			return;
		}
		boolean inside = LifeSupportBlockEntity.pressurized(world, BlockPos.ofFloored(p.getEyePos()));
		ItemStack chest = p.getEquippedStack(EquipmentSlot.CHEST);
		boolean suit = wearingSuit(p);
		if (inside) {
			if (suit && oxygen(chest) < SUIT_OXYGEN) {
				setOxygen(chest, oxygen(chest) + 40);   // the base recharges the backpack
			}
			return;
		}
		if (suit && oxygen(chest) > 0) {
			setOxygen(chest, oxygen(chest) - 1);
			int left = oxygen(chest);
			if (left == 1200 || left == 200) {
				p.sendMessage(Text.translatable("message.periodic_elements.moon.oxygen_low", left / 20).formatted(Formatting.RED), true);
			}
			return;
		}
		int air = p.getAir() - VACUUM_AIR_LOSS;
		if (air <= -20) {
			air = 0;
			p.damage(world.getDamageSources().drown(), 2f);
			if (world.getTime() % 40 == 0) {
				p.sendMessage(Text.translatable(suit ? "message.periodic_elements.moon.no_oxygen" : "message.periodic_elements.moon.vacuum")
						.formatted(Formatting.RED), true);
			}
		}
		p.setAir(air);
	}

	/** The top of the ground at (x, z) in {@code world}, generating it if need be. */
	public static int surface(ServerWorld world, int x, int z) {
		world.getChunk(x >> 4, z >> 4);
		return world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
	}

	/** Sets {@code p} down on the surface of {@code world} at (x, z). */
	public static void goTo(ServerPlayerEntity p, ServerWorld world, int x, int z) {
		int y = surface(world, x, z);
		p.fallDistance = 0;
		p.teleportTo(new TeleportTarget(world, new Vec3d(x + 0.5, y + 0.1, z + 0.5), Vec3d.ZERO, p.getYaw(), p.getPitch(),
				TeleportTarget.NO_OP));
		p.fallDistance = 0;
	}

	/** Where a crewed rocket from {@code from} goes: the Moon from the Earth, the Earth from anywhere else. */
	public static RegistryKey<World> destination(World from) {
		return destination(from, ItemStack.EMPTY);
	}

	/** 3.66.0: from the Earth a Mars Crew Capsule goes to Mars, any other to the Moon; from anywhere else, home. */
	public static RegistryKey<World> destination(World from, ItemStack capsule) {
		if (from.getRegistryKey() != World.OVERWORLD) {
			return World.OVERWORLD;
		}
		if (Mars.MARS_CAPSULE != null && capsule.isOf(Mars.MARS_CAPSULE)) {
			return Mars.MARS;
		}
		if (Mercury.MERCURY_CAPSULE != null && capsule.isOf(Mercury.MERCURY_CAPSULE)) {   // 3.68.0
			return Mercury.MERCURY;
		}
		if (Venus.VENUS_CAPSULE != null && capsule.isOf(Venus.VENUS_CAPSULE)) {
			return Venus.VENUS;
		}
		return MOON;
	}

	/** The crew capsule reached the end of its flight: the crew is set down at the destination with the capsule. */
	static void arrive(ServerWorld from, ServerPlayerEntity crew, ItemStack capsule, BlockPos pad) {
		MinecraftServer server = from.getServer();
		RegistryKey<World> to = destination(from, capsule);
		ServerWorld dest = server.getWorld(to);
		if (dest == null) {
			return;
		}
		crew.stopRiding();
		int x = pad.getX(), z = pad.getZ();
		if (to == Mercury.MERCURY) {   // 3.68.0: down in the dawn band, a few blocks into the morning
			x = (int) Math.floor(Mercury.dawnLine(Mercury.clock(dest))) + 6;
		} else if (to == Venus.VENUS) {   // the capsule's balloon unfolds into a deck over the clouds
			Venus.deck(dest, x, z);
		}
		goTo(crew, dest, x, z);
		if (!crew.getInventory().insertStack(capsule.copy())) {
			crew.dropItem(capsule.copy(), false);
		}
		dest.playSound(null, crew.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.6f, 1.6f);
		crew.sendMessage(Text.translatable(to == MOON ? "message.periodic_elements.moon.landed" : to == Mars.MARS
				? "message.periodic_elements.mars.landed" : to == Mercury.MERCURY ? "message.periodic_elements.mercury.landed"
				: to == Venus.VENUS ? "message.periodic_elements.venus.landed" : "message.periodic_elements.moon.home")
				.formatted(Formatting.GOLD), false);
		SpaceState st = SpaceState.get(server);
		if (to == MOON) {
			st.moonLandings.merge(crew.getUuid(), 1, Integer::sum);
			st.markDirty();
		} else if (to == Mars.MARS) {
			st.marsLandings.merge(crew.getUuid(), 1, Integer::sum);
			st.markDirty();
		} else if (to == Mercury.MERCURY) {
			st.mercuryLandings.merge(crew.getUuid(), 1, Integer::sum);
			st.markDirty();
		} else if (to == Venus.VENUS) {
			st.venusLandings.merge(crew.getUuid(), 1, Integer::sum);
			st.markDirty();
		}
	}
}
