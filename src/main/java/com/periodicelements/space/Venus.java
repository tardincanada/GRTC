package com.periodicelements.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Level 10, stage 7 (3.68.0), FICTION in the getting there: Venus. Its ground is at 465 °C under 92 atmospheres, but
 * 50 km up the air is at 1 atm and 0 to 50 °C, the kindest place in the Solar System after the Earth (NASA's HAVOC
 * study, 2015). Air we breathe is lighter than Venus's carbon dioxide, so a bubble of it floats: the base is the
 * balloon. The dimension is sky (VenusChunkGenerator), with the clouds of sulfuric acid below the Aerostat Deck; probes
 * go down to the surface and send rock back up.
 */
public final class Venus {
	public static final RegistryKey<World> VENUS = RegistryKey.of(RegistryKeys.WORLD, Moon.id("venus"));
	public static final RegistryKey<DamageType> ACID = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Moon.id("acid"));

	/** Venus's gravity is 8.87 m/s², 0.904 of the Earth's (a little less again 50 km up). */
	public static final double GRAVITY = 0.904;
	/** Above the clouds the Sun gives 1.9 times the Earth's light (0.72 times as far away, and the clouds reflect more). */
	public static final double SUNLIGHT = 1.9;
	/** Where the Aerostat Deck is set down for a crew arriving, and the cloud layer below it. */
	public static final int DECK_Y = 120, CLOUD_BOTTOM = 20, CLOUD_TOP = 64;
	/** How far across the deck a capsule's balloon unfolds, each way from its middle. */
	public static final int DECK_RADIUS = 4;
	/** Ticks a probe takes to fall to the ground and send its sample back up on a balloon. */
	public static final int PROBE_TICKS = 600;
	/** What the Air Liquefier draws from the air at 50 km, per load (Venera and Pioneer Venus): with acid droplets. */
	public static final Map<String, Double> AIR = new LinkedHashMap<>();

	static {
		AIR.put("carbon_dioxide", 955.0);
		AIR.put("nitrogen", 35.0);
		AIR.put("sulfur_dioxide", 6.0);
		AIR.put("sulfuric_acid", 4.0);
	}

	public static Block VENUS_CLOUD, AEROSTAT_DECK, VENUSIAN_BASALT, METALLIC_SNOW;
	public static Item VENUS_CAPSULE, VENUS_PROBE;

	private record Probe(UUID player, long due) {
	}

	private static final List<Probe> PROBES = new ArrayList<>();

	private Venus() {
	}

	public static boolean is(World world) {
		return world != null && world.getRegistryKey() == VENUS;
	}

	/** A cloud of sulfuric acid droplets: no floor, no collision; you fall through it and it burns. */
	public static class CloudBlock extends Block {
		public CloudBlock(Settings settings) {
			super(settings);
		}

		@Override
		protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
			return VoxelShapes.empty();
		}

		@Override
		protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
			return true;
		}

		@Override
		protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
			return 1f;
		}

		@Override
		protected boolean isSideInvisible(BlockState state, BlockState next, net.minecraft.util.math.Direction direction) {
			return next.isOf(this) || super.isSideInvisible(state, next, direction);
		}
	}

	/** Venus Probe: drop it from the deck; it falls to the ground and sends rock and metallic snow back up. */
	static class ProbeItem extends com.periodicelements.ModContent.TippedItem {
		ProbeItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			ItemStack stack = user.getStackInHand(hand);
			if (world.isClient || !(user instanceof ServerPlayerEntity p)) {
				return TypedActionResult.success(stack, world.isClient);
			}
			if (!is(world)) {
				p.sendMessage(Text.translatable("message.periodic_elements.venus.probe_only").formatted(Formatting.YELLOW), true);
				return TypedActionResult.fail(stack);
			}
			drop((ServerWorld) world, p);
			if (!p.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			return TypedActionResult.success(stack);
		}
	}

	/** A probe leaves {@code p}'s hands: its sample comes back after {@link #PROBE_TICKS}. */
	public static void drop(ServerWorld world, ServerPlayerEntity p) {
		PROBES.add(new Probe(p.getUuid(), world.getTime() + PROBE_TICKS));
		world.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.6f, 0.6f);
		p.sendMessage(Text.translatable("message.periodic_elements.venus.probe_dropped", PROBE_TICKS / 20).formatted(Formatting.GOLD), false);
	}

	/** The sample a probe sends back: basalt from the plains and the metallic snow of the high peaks. */
	public static List<ItemStack> sample() {
		return List.of(new ItemStack(VENUSIAN_BASALT, 16), new ItemStack(METALLIC_SNOW, 4));
	}

	private static void tickProbes(MinecraftServer server) {
		if (PROBES.isEmpty()) {
			return;
		}
		ServerWorld venus = server.getWorld(VENUS);
		long now = venus != null ? venus.getTime() : server.getOverworld().getTime();
		for (Iterator<Probe> it = PROBES.iterator(); it.hasNext(); ) {
			Probe pr = it.next();
			if (now < pr.due()) {
				continue;
			}
			it.remove();
			ServerPlayerEntity p = server.getPlayerManager().getPlayer(pr.player());
			if (p == null) {
				continue;
			}
			for (ItemStack s : sample()) {
				if (!p.getInventory().insertStack(s)) {
					p.dropItem(s, false);
				}
			}
			p.sendMessage(Text.translatable("message.periodic_elements.venus.probe_back").formatted(Formatting.GOLD), false);
		}
	}

	/** Lays the Aerostat Deck round (x, z) at {@link #DECK_Y}, unless a floor is already there. */
	public static void deck(ServerWorld world, int x, int z) {
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int dx = -DECK_RADIUS; dx <= DECK_RADIUS; dx++) {
			for (int dz = -DECK_RADIUS; dz <= DECK_RADIUS; dz++) {
				if (dx * dx + dz * dz > DECK_RADIUS * DECK_RADIUS + 2) {
					continue;
				}
				p.set(x + dx, DECK_Y, z + dz);
				if (world.getBlockState(p).isAir()) {
					world.setBlockState(p, AEROSTAT_DECK.getDefaultState());
				}
			}
		}
	}

	/** Whether {@code e} stands, swims or breathes in an acid cloud. */
	public static boolean inCloud(World world, LivingEntity e) {
		return world.getBlockState(e.getBlockPos()).isOf(VENUS_CLOUD)
				|| world.getBlockState(BlockPos.ofFloored(e.getEyePos())).isOf(VENUS_CLOUD);
	}

	public static DamageSource acid(World world) {
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(ACID));
	}

	/** One second in the clouds: the acid burns skin and eats at armour, but not Stability Alloy. */
	public static void corrode(ServerWorld world, LivingEntity e) {
		if (Fiction.wearingThermalSuit(e)) {
			return;
		}
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack a = e.getEquippedStack(slot);
			if (a.isDamageable()) {
				a.damage(2, e, slot);
			}
		}
		e.damage(acid(world), 2f);
		if (e instanceof ServerPlayerEntity p && world.getTime() % 60 == 0) {
			p.sendMessage(Text.translatable("message.periodic_elements.venus.acid").formatted(Formatting.RED), true);
		}
	}

	static void register(List<Item> items) {
		VENUS_CLOUD = Moon.rockBlock(items, "venus_cloud", new CloudBlock(AbstractBlock.Settings.create()
				.mapColor(MapColor.PALE_YELLOW).strength(0.1f).sounds(BlockSoundGroup.WOOL).noCollision().nonOpaque().replaceable()
				.dropsNothing().allowsSpawning((s, w, p, t) -> false).solidBlock((s, w, p) -> false)
				.suffocates((s, w, p) -> false).blockVision((s, w, p) -> false)));
		VENUSIAN_BASALT = Moon.rockBlock(items, "venusian_basalt", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.TERRACOTTA_BLACK).strength(1.8f, 6f).requiresTool().sounds(BlockSoundGroup.BASALT)));
		METALLIC_SNOW = Moon.rockBlock(items, "metallic_snow", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY).strength(0.8f).requiresTool().sounds(BlockSoundGroup.CHAIN)));
		AEROSTAT_DECK = Space.gatedBlock(items, "aerostat_deck", "venus_aerostat", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE).strength(1.5f, 6f).sounds(BlockSoundGroup.WOOL)));
		VENUS_CAPSULE = Space.item(items, "venus_capsule", "venus_aerostat",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		VENUS_PROBE = Space.item(items, "venus_probe", "venus_aerostat", new ProbeItem(new Item.Settings().maxCount(16)));
		Registry.register(Registries.CHUNK_GENERATOR, Moon.id("venus"), VenusChunkGenerator.CODEC);

		ServerTickEvents.END_SERVER_TICK.register(Venus::tickProbes);
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (!is(world) || world.getTime() % 20 != 0) {
				return;
			}
			for (ServerPlayerEntity p : world.getPlayers()) {
				if (!p.isCreative() && !p.isSpectator() && inCloud(world, p)) {
					corrode(world, p);
				}
			}
		});
	}
}
