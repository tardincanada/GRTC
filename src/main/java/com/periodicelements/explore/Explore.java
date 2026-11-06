package com.periodicelements.explore;

import com.mojang.serialization.Codec;
import com.periodicelements.PeriodicElements;
import com.periodicelements.mining.BatteryPackItem;
import com.periodicelements.mining.DrillItem;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Exploration gear (3.47.0): head lamps, night vision goggles, street lamps, the tent, backpacks, the Explorer's
 * Compass and animal traps. Each item is crafted only by a player who researched it ({@link #gate}).
 */
public final class Explore {
	/** Energy per second of the worn electric gear, in kJ. */
	public static final double CAP_LAMP_KJ = 0.25, SPOTLIGHT_KJ = 0.2, GOGGLES_KJ = 0.1;
	/** How far the LED spotlight reaches, and how far goggles outline monsters, in blocks. */
	public static final int SPOT_REACH = 32, GOGGLES_REACH = 32;

	public static Item CAP_LAMP, SPOTLIGHT_HELMET, GOGGLES, LEATHER_BACKPACK, NYLON_BACKPACK, COMPASS, ROPE_NET, STEEL_CAGE;
	public static Block LIGHT, TENT, SODIUM_LAMP, LED_LAMP;
	public static BlockEntityType<StreetLampBlockEntity> LAMP_ENTITY;
	public static EntityType<TrapEntity> TRAP_ENTITY;
	public static ScreenHandlerType<BackpackScreenHandler> BACKPACK_SCREEN;
	/** Whether worn electric gear has charge right now (set by the server, read by the client). */
	public static ComponentType<Boolean> POWERED;
	public static ComponentType<Boolean> PICKUP;
	public static ComponentType<String> COMPASS_BIOME;
	public static ComponentType<GlobalPos> COMPASS_TARGET;
	public static ComponentType<NbtComponent> TRAPPED;
	/** Animals the Rope Net can hold; the Steel Cage holds any animal. */
	public static final TagKey<EntityType<?>> SMALL_ANIMALS = TagKey.of(RegistryKeys.ENTITY_TYPE, id("trap_small"));

	/** Item path -> research project that unlocks crafting it. */
	public static final Map<String, String> PROJECTS = new HashMap<>();
	/** Where each player's spotlight lands now. */
	static final Map<UUID, BlockPos> SPOTS = new HashMap<>();

	private Explore() {
	}

	static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** The research unlock ("item:<id>") a crafted stack needs, or null if it is not exploration gear. */
	public static String gate(ItemStack stack) {
		Identifier key = Registries.ITEM.getId(stack.getItem());
		if (!key.getNamespace().equals(PeriodicElements.MOD_ID) || !PROJECTS.containsKey(key.getPath())) {
			return null;
		}
		return "item:" + key.getPath();
	}

	private static Item item(List<Item> items, String name, String project, Item item) {
		Registry.register(Registries.ITEM, id(name), item);
		items.add(item);
		PROJECTS.put(name, project);
		return item;
	}

	private static Block gatedBlock(List<Item> items, String name, String project, Block block) {
		Registry.register(Registries.BLOCK, id(name), block);
		PROJECTS.put(name, project);
		items.add(Registry.register(Registries.ITEM, id(name), new GatedBlockItem(block, new Item.Settings(),
				known -> Research.allows(known, "item:" + name), Research.get(project).name())));
		return block;
	}

	public record OpenBackpack() implements CustomPayload {
		public static final Id<OpenBackpack> ID = new Id<>(id("open_backpack"));
		public static final PacketCodec<RegistryByteBuf, OpenBackpack> CODEC = PacketCodec.unit(new OpenBackpack());

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static void register(List<Item> items) {
		POWERED = Registry.register(Registries.DATA_COMPONENT_TYPE, id("powered"),
				ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build());
		PICKUP = Registry.register(Registries.DATA_COMPONENT_TYPE, id("backpack_pickup"),
				ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build());
		COMPASS_BIOME = Registry.register(Registries.DATA_COMPONENT_TYPE, id("compass_biome"),
				ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build());
		COMPASS_TARGET = Registry.register(Registries.DATA_COMPONENT_TYPE, id("compass_target"),
				ComponentType.<GlobalPos>builder().codec(GlobalPos.CODEC).packetCodec(GlobalPos.PACKET_CODEC).build());
		TRAPPED = Registry.register(Registries.DATA_COMPONENT_TYPE, id("trapped"),
				ComponentType.<NbtComponent>builder().codec(NbtComponent.CODEC).packetCodec(NbtComponent.PACKET_CODEC).build());

		LIGHT = Registry.register(Registries.BLOCK, id("explorer_light"), new ExplorerLightBlock(AbstractBlock.Settings.create()
				.replaceable().noCollision().dropsNothing().nonOpaque().luminance(s -> 15)
				.pistonBehavior(PistonBehavior.DESTROY).strength(0f)));

		CAP_LAMP = item(items, "electric_cap_lamp", "electric_cap_lamp",
				new HeadGearItem(new Item.Settings().maxCount(1), HeadGearItem.Kind.CAP_LAMP));
		SPOTLIGHT_HELMET = item(items, "led_spotlight_helmet", "led_lighting",
				new HeadGearItem(new Item.Settings().maxCount(1), HeadGearItem.Kind.SPOTLIGHT));
		GOGGLES = item(items, "night_vision_goggles", "night_vision",
				new HeadGearItem(new Item.Settings().maxCount(1), HeadGearItem.Kind.GOGGLES));

		SODIUM_LAMP = gatedBlock(items, "sodium_lamp", "sodium_lighting", new StreetLampBlock(StreetLampBlock.Kind.SODIUM,
				AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(1.5f).sounds(BlockSoundGroup.LANTERN)
						.nonOpaque().luminance(s -> s.get(StreetLampBlock.LIT) ? 15 : 0)));
		LED_LAMP = gatedBlock(items, "led_lamp", "led_lighting", new StreetLampBlock(StreetLampBlock.Kind.LED,
				AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(1.5f).sounds(BlockSoundGroup.LANTERN)
						.nonOpaque().luminance(s -> s.get(StreetLampBlock.LIT) ? 15 : 0)));
		LAMP_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("street_lamp"),
				BlockEntityType.Builder.create(StreetLampBlockEntity::new, SODIUM_LAMP, LED_LAMP).build(null));

		TENT = gatedBlock(items, "tent", "hide_tent", new TentBlock(AbstractBlock.Settings.create().mapColor(MapColor.BROWN)
				.strength(0.4f).sounds(BlockSoundGroup.WOOL).nonOpaque().suffocates((s, w, p) -> false)
				.blockVision((s, w, p) -> false).pistonBehavior(PistonBehavior.DESTROY)));

		LEATHER_BACKPACK = item(items, "leather_backpack", "leather_pack", new BackpackItem(new Item.Settings().maxCount(1), 3));
		NYLON_BACKPACK = item(items, "nylon_backpack", "nylon_pack", new BackpackItem(new Item.Settings().maxCount(1), 6));
		BACKPACK_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("backpack"),
				new ExtendedScreenHandlerType<>(BackpackScreenHandler::new, PacketCodecs.VAR_INT.cast()));

		COMPASS = item(items, "explorer_compass", "explorer_compass", new ExplorerCompassItem(new Item.Settings().maxCount(1)));

		ROPE_NET = item(items, "rope_net", "hunting_nets", new TrapItem(new Item.Settings().maxDamage(TrapItem.NET_USES), false));
		STEEL_CAGE = item(items, "steel_cage", "animal_cage", new TrapItem(new Item.Settings().maxCount(1), true));
		TRAP_ENTITY = Registry.register(Registries.ENTITY_TYPE, id("thrown_trap"),
				EntityType.Builder.<TrapEntity>create(TrapEntity::new, SpawnGroup.MISC).dimensions(0.4f, 0.4f)
						.maxTrackingRange(4).trackingTickInterval(10).build());

		PayloadTypeRegistry.playC2S().register(OpenBackpack.ID, OpenBackpack.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenBackpack.ID, (p, ctx) -> BackpackItem.openFirst(ctx.player()));

		// The tent is slept in like a bed, but it never moves the spawn point.
		EntitySleepEvents.ALLOW_BED.register((entity, pos, state, vanilla) ->
				state.isOf(TENT) ? ActionResult.SUCCESS : ActionResult.PASS);
		EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, pos, dir) -> {
			BlockState s = entity.getWorld().getBlockState(pos);
			return s.isOf(TENT) ? s.get(TentBlock.FACING) : dir;
		});
		EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, pos) -> !player.getWorld().getBlockState(pos).isOf(TENT));

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getTime() % 10 != 0) {
				return;
			}
			for (ServerPlayerEntity p : world.getPlayers()) {
				if (!p.isSpectator()) {
					tickPlayer(world, p);
				}
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			LitLamps.clear();
			SPOTS.clear();
		});
	}

	/** Draws {@code kJ} from the battery packs in the pack; true if it was all there (creative players pay nothing). */
	public static boolean drawEnergy(PlayerEntity p, double kJ) {
		if (p.getAbilities().creativeMode) {
			return true;
		}
		double got = 0;
		for (ItemStack pack : DrillItem.packs(p)) {
			got += BatteryPackItem.draw(pack, kJ - got);
			if (got >= kJ - 1e-9) {
				return true;
			}
		}
		return false;
	}

	public static boolean powered(ItemStack s) {
		return s.getOrDefault(POWERED, false);
	}

	/** The worn head gear of this kind with charge, or null. */
	public static ItemStack worn(PlayerEntity p, HeadGearItem.Kind kind) {
		ItemStack head = p.getEquippedStack(EquipmentSlot.HEAD);
		return head.getItem() instanceof HeadGearItem g && g.kind() == kind && powered(head) ? head : null;
	}

	/** Every half second: worn electric gear draws its charge, lights the way or gives night vision. */
	public static void tickPlayer(ServerWorld world, ServerPlayerEntity p) {
		ItemStack head = p.getEquippedStack(EquipmentSlot.HEAD);
		if (!(head.getItem() instanceof HeadGearItem gear)) {
			SPOTS.remove(p.getUuid());
			return;
		}
		boolean on = drawEnergy(p, gear.kind().kJ() / 2);
		if (powered(head) != on) {
			head.set(POWERED, on);
		}
		if (!on) {
			SPOTS.remove(p.getUuid());
			return;
		}
		switch (gear.kind()) {
			case CAP_LAMP -> ExplorerLightBlock.lightAt(world, BlockPos.ofFloored(p.getEyePos()), p.getBlockPos(),
					ExplorerLightBlock.Kind.CAP);
			case SPOTLIGHT -> {
				ExplorerLightBlock.lightAt(world, BlockPos.ofFloored(p.getEyePos()), p.getBlockPos(), ExplorerLightBlock.Kind.LED);
				BlockPos spot = ExplorerLightBlock.spotOf(world, p, SPOT_REACH);
				if (spot != null) {
					SPOTS.put(p.getUuid(), spot);
					ExplorerLightBlock.lightAt(world, spot, null, ExplorerLightBlock.Kind.LED);
				} else {
					SPOTS.remove(p.getUuid());
				}
			}
			case GOGGLES -> p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, true));
		}
	}

	/** Server-side register of lit street lamps, for the spawn check. */
	public static final class LitLamps {
		private static final Map<RegistryKey<World>, Map<BlockPos, Integer>> LIT = new HashMap<>();

		private LitLamps() {
		}

		public static void set(World world, BlockPos pos, int radius) {
			LIT.computeIfAbsent(world.getRegistryKey(), k -> new HashMap<>()).put(pos.toImmutable(), radius);
		}

		public static void remove(World world, BlockPos pos) {
			Map<BlockPos, Integer> m = LIT.get(world.getRegistryKey());
			if (m != null) {
				m.remove(pos);
			}
		}

		static void clear() {
			LIT.clear();
		}

		/** Is this place within reach of a lit street lamp (a cylinder of the lamp's radius, as tall as it is wide)? */
		public static boolean covers(World world, BlockPos pos) {
			Map<BlockPos, Integer> m = LIT.get(world.getRegistryKey());
			if (m == null || m.isEmpty()) {
				return false;
			}
			for (var e : m.entrySet()) {
				BlockPos l = e.getKey();
				int r = e.getValue();
				long dx = pos.getX() - l.getX(), dz = pos.getZ() - l.getZ();
				if (dx * dx + dz * dz <= (long) r * r && Math.abs(pos.getY() - l.getY()) <= r) {
					return true;
				}
			}
			return false;
		}
	}
}
