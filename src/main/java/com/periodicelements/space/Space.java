package com.periodicelements.space;

import com.periodicelements.PeriodicElements;
import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.StationState;
import com.periodicelements.research.GatedBlockItem;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Level 8, stage 1 (3.61.0): rockets built from blocks on the Launch Pad, three propellant pairs, the Launch Console's
 * countdown, satellites with a job (weather, communication) and, after Propulsive Landing, first stages that fly
 * back to the pad. Each block and item is crafted only by a player who researched it ({@link #gate}).
 */
public final class Space {
	public static Block KEROLOX_ENGINE, HYDROLOX_ENGINE, HYPERGOLIC_ENGINE, METHALOX_ENGINE, METALLIC_HYDROGEN_ENGINE, ANTIMATTER_ENGINE, FUSION_DRIVE, PROPELLANT_TANK, PAYLOAD_FAIRING, LAUNCH_CONSOLE;
	public static Item LANDING_LEGS, WEATHER_SATELLITE, COMMUNICATION_SATELLITE, SPACE_TELESCOPE;
	public static BlockEntityType<PropellantTankBlockEntity> TANK_ENTITY;
	public static BlockEntityType<PayloadFairingBlockEntity> FAIRING_ENTITY;
	public static EntityType<RocketEntity> ROCKET;
	/** Item path -> research project that unlocks crafting it. */
	public static final Map<String, String> PROJECTS = new HashMap<>();
	/** Seconds of countdown. */
	public static final int COUNTDOWN = 10;
	/** 3.64.0: how near the pad the crew must stand when the countdown ends. */
	public static final int CREW_REACH = 24;
	/** True while a rocket's blocks are lifted off the pad or set back on it: tanks and fairings drop nothing then. */
	public static boolean lifting;

	private record Countdown(ServerWorld world, BlockPos console, BlockPos pad, UUID owner, int[] ticks) {
	}

	private static final Map<BlockPos, Countdown> COUNTDOWNS = new HashMap<>();

	private Space() {
	}

	static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	/** The research unlock ("item:<id>") a crafted stack needs, or null if it is not space gear. */
	public static String gate(ItemStack stack) {
		Identifier key = Registries.ITEM.getId(stack.getItem());
		if (!key.getNamespace().equals(PeriodicElements.MOD_ID) || !PROJECTS.containsKey(key.getPath())) {
			return null;
		}
		return "item:" + key.getPath();
	}

	static Block gatedBlock(List<Item> items, String name, String project, Block block) {
		Registry.register(Registries.BLOCK, id(name), block);
		PROJECTS.put(name, project);
		items.add(Registry.register(Registries.ITEM, id(name), new GatedBlockItem(block, new Item.Settings(),
				known -> Research.allows(known, "item:" + name), Research.get(project).name())));
		return block;
	}

	static Item item(List<Item> items, String name, String project, Item item) {
		Registry.register(Registries.ITEM, id(name), item);
		items.add(item);
		PROJECTS.put(name, project);
		return item;
	}

	private static AbstractBlock.Settings metal() {
		return AbstractBlock.Settings.create().mapColor(MapColor.WHITE_GRAY).strength(2.5f).requiresTool()
				.sounds(BlockSoundGroup.METAL).nonOpaque();
	}

	public static void register(List<Item> items) {
		KEROLOX_ENGINE = gatedBlock(items, "kerolox_engine", "orbital_rocketry", new RocketEngineBlock(Propellant.KEROLOX, metal()));
		HYDROLOX_ENGINE = gatedBlock(items, "hydrolox_engine", "cryogenic_hydrogen", new RocketEngineBlock(Propellant.HYDROLOX, metal()));
		HYPERGOLIC_ENGINE = gatedBlock(items, "hypergolic_engine", "hypergolic_propellants",
				new RocketEngineBlock(Propellant.HYPERGOLIC, metal()));
		// 3.66.0: methane and oxygen, both made on Mars
		METHALOX_ENGINE = gatedBlock(items, "methalox_engine", "methalox_rockets", new RocketEngineBlock(Propellant.METHALOX, metal()));
		// 3.67.0, fiction
		METALLIC_HYDROGEN_ENGINE = gatedBlock(items, "metallic_hydrogen_engine", "metallic_hydrogen",
				new RocketEngineBlock(Propellant.METALLIC, metal()));
		ANTIMATTER_ENGINE = gatedBlock(items, "antimatter_engine", "antimatter_containment",
				new RocketEngineBlock(Propellant.ANTIMATTER, metal()));
		FUSION_DRIVE = gatedBlock(items, "fusion_drive", "fusion_propulsion", new RocketEngineBlock(Propellant.FUSION, metal()));
		PROPELLANT_TANK = gatedBlock(items, "propellant_tank", "orbital_rocketry", new PropellantTankBlock(metal()));
		PAYLOAD_FAIRING = gatedBlock(items, "payload_fairing", "orbital_rocketry", new PayloadFairingBlock(metal()));
		LAUNCH_CONSOLE = gatedBlock(items, "launch_console", "orbital_rocketry", new LaunchConsoleBlock(metal()));
		LANDING_LEGS = item(items, "landing_legs", "propulsive_landing",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(16)));
		WEATHER_SATELLITE = item(items, "weather_satellite", "weather_satellite",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		COMMUNICATION_SATELLITE = item(items, "communication_satellite", "communication_satellite",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		// 3.62.0: the Space Telescope forecasts meteorite falls (Meteorites)
		SPACE_TELESCOPE = item(items, "space_telescope", "space_telescope",
				new com.periodicelements.ModContent.TippedItem(new Item.Settings().maxCount(1)));
		Meteorites.register(items);
		Fusion.register(items);   // 3.63.0: the Tokamak's ring parts
		Moon.register(items);   // 3.64.0: the Moon
		Mars.register(items);   // 3.66.0: Mars
		Fiction.register(items);   // 3.67.0: the materials of science fiction
		Mercury.register(items);   // 3.68.0: Mercury
		Venus.register(items);   // 3.68.0: Venus, over the clouds
		TANK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("propellant_tank"),
				BlockEntityType.Builder.create(PropellantTankBlockEntity::new, PROPELLANT_TANK).build(null));
		FAIRING_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("payload_fairing"),
				BlockEntityType.Builder.create(PayloadFairingBlockEntity::new, PAYLOAD_FAIRING).build(null));
		ROCKET = Registry.register(Registries.ENTITY_TYPE, id("rocket"),
				EntityType.Builder.<RocketEntity>create(RocketEntity::new, SpawnGroup.MISC).dimensions(1f, 1f)
						.maxTrackingRange(32).trackingTickInterval(1).alwaysUpdateVelocity(true).build());

		ServerTickEvents.END_SERVER_TICK.register(Space::tickCountdowns);
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			// the weather satellites' morning report, at 6:00 (time of day 0)
			if (world.getRegistryKey() == World.OVERWORLD && world.getTimeOfDay() % 24000 == 0) {
				for (ServerPlayerEntity p : world.getServer().getPlayerManager().getPlayerList()) {
					forecast(p);
				}
			}
		});
	}

	public static Item repairItem() {
		return Registries.ITEM.get(id("titanium_alloy_ingot"));
	}

	/** What job a payload does in orbit: "mapping" (the Atlas satellite), "weather", "communication", "telescope", or null. */
	public static String payloadKind(ItemStack stack) {
		if (stack.isOf(Atlas.SATELLITE)) {
			return "mapping";
		}
		if (stack.isOf(WEATHER_SATELLITE)) {
			return "weather";
		}
		if (stack.isOf(COMMUNICATION_SATELLITE)) {
			return "communication";
		}
		if (stack.isOf(SPACE_TELESCOPE)) {
			return "telescope";
		}
		if (stack.isOf(Moon.CREW_CAPSULE) || stack.isOf(Mars.MARS_CAPSULE) || stack.isOf(Mercury.MERCURY_CAPSULE)
				|| stack.isOf(Venus.VENUS_CAPSULE)) {
			return "crew";
		}
		return null;
	}

	public static boolean isPayload(ItemStack stack) {
		return payloadKind(stack) != null;
	}

	/** Whether {@code owner} has a satellite of {@code kind} in orbit. */
	public static boolean has(MinecraftServer server, UUID owner, String kind) {
		return SpaceState.get(server).count(owner, kind) > 0;
	}

	private static net.minecraft.text.MutableText msg(String key, Object... args) {
		return Text.translatable("message.periodic_elements.rocket." + key, args);
	}

	/** The Launch Console was used: abort a countdown, or check the rocket and start one. */
	public static void useConsole(ServerWorld world, BlockPos console, ServerPlayerEntity player) {
		Countdown running = COUNTDOWNS.remove(console);
		if (running != null) {
			tellNear(world, running.pad(), "abort");
			return;
		}
		if (!ResearchState.get(world.getServer()).known(player.getUuid()).contains("orbital_rocketry")) {
			player.sendMessage(msg("needs", Research.get("orbital_rocketry").name()).formatted(Formatting.RED), true);
			return;
		}
		BlockPos pad = Rocket.findPad(world, console);
		if (pad == null) {
			player.sendMessage(msg("no_pad").formatted(Formatting.YELLOW), false);
			return;
		}
		Rocket r = Rocket.read(world, pad);
		for (Text line : r.report()) {
			player.sendMessage(line, false);
		}
		if (!r.ready()) {
			for (Text t : r.problems) {
				player.sendMessage(t.copy().formatted(Formatting.RED), false);
			}
			return;
		}
		for (Countdown c : COUNTDOWNS.values()) {
			if (c.pad().equals(pad)) {
				return;
			}
		}
		player.sendMessage(msg(r.reusable() ? "ready_reusable" : "ready").formatted(Formatting.GREEN), false);
		COUNTDOWNS.put(console.toImmutable(), new Countdown(world, console.toImmutable(), pad, player.getUuid(),
				new int[]{COUNTDOWN * 20}));
	}

	private static void tickCountdowns(MinecraftServer server) {
		Iterator<Countdown> it = COUNTDOWNS.values().iterator();
		List<Countdown> go = new ArrayList<>();
		while (it.hasNext()) {
			Countdown c = it.next();
			if (!(c.world().getBlockState(c.console()).getBlock() instanceof LaunchConsoleBlock)) {
				it.remove();
				continue;
			}
			int t = --c.ticks()[0];
			if (t > 0 && t % 20 == 0) {
				for (ServerPlayerEntity p : c.world().getPlayers()) {
					if (p.getBlockPos().isWithinDistance(c.pad(), 96)) {
						p.sendMessage(msg("countdown", t / 20).formatted(Formatting.GOLD), true);
					}
				}
				c.world().playSound(null, c.console(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS,
						1f, t <= 60 ? 1.2f : 0.9f);
			}
			if (t <= 0) {
				it.remove();
				go.add(c);
			}
		}
		for (Countdown c : go) {
			launch(c.world(), c.pad(), c.owner());
		}
	}

	/** Lifts the rocket on {@code pad} off as a {@link RocketEntity}; returns it, or null if it can no longer fly. */
	public static RocketEntity launch(ServerWorld world, BlockPos pad, UUID owner) {
		Rocket r = Rocket.read(world, pad);
		if (!r.ready()) {
			tellNear(world, pad, "abort");
			for (ServerPlayerEntity p : world.getPlayers()) {
				if (p.getBlockPos().isWithinDistance(pad, 96)) {
					r.problems.forEach(t -> p.sendMessage(t.copy().formatted(Formatting.RED), false));
				}
			}
			return null;
		}
		// 3.64.0: a crew capsule flies only with its crew aboard, the player who started the countdown
		ServerPlayerEntity crew = null;
		if ("crew".equals(payloadKind(r.payload))) {
			crew = owner == null ? null : world.getServer().getPlayerManager().getPlayer(owner);
			if (crew == null || crew.getWorld() != world || !crew.getBlockPos().isWithinDistance(pad, CREW_REACH)) {
				tellNear(world, pad, "crew_missing", CREW_REACH);
				return null;
			}
		}
		int[] starts = r.stages.stream().mapToInt(Rocket.Stage::bottom).toArray();
		List<BlockState> blocks = new ArrayList<>(r.blocks);
		lifting = true;
		try {
			for (int i = blocks.size() - 1; i >= 0; i--) {
				world.setBlockState(pad.up(1 + i), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
		} finally {
			lifting = false;
		}
		SpaceState st = SpaceState.get(world.getServer());
		st.launches.merge(owner, 1, Integer::sum);
		st.markDirty();
		tellNear(world, pad, "liftoff");
		RocketEntity rocket = RocketEntity.launch(world, pad, blocks, starts, owner, r.payload);
		if (crew != null) {
			rocket.board(crew);
		}
		return rocket;
	}

	/** The last stage got to orbit: the payload starts its job. */
	public static void reachOrbit(ServerWorld world, UUID owner, ItemStack payload, BlockPos pad) {
		String kind = payloadKind(payload);
		if (owner == null || kind == null) {
			return;
		}
		MinecraftServer server = world.getServer();
		ServerPlayerEntity p = server.getPlayerManager().getPlayer(owner);
		if (kind.equals("mapping")) {
			StationState stations = StationState.get(server);
			int n = stations.satellites(owner) + 1;
			stations.satellites.put(owner, n);
			stations.markDirty();
			if (p != null) {
				p.sendMessage(Text.translatable("gui.periodic_elements.atlas.satellite.launched", n), false);
			}
		} else {
			SpaceState.get(server).add(owner, kind);
		}
		tellNear(world, pad, "orbit", payload.getName());
		if (p != null && kind.equals("weather")) {
			forecast(p);
		}
	}

	/** A first stage set down on its pad. */
	static void landed(ServerWorld world, UUID owner) {
		if (owner != null) {
			SpaceState st = SpaceState.get(world.getServer());
			st.landings.merge(owner, 1, Integer::sum);
			st.markDirty();
		}
	}

	/** A message to everyone within 256 blocks of the pad. */
	public static void tellNear(ServerWorld world, BlockPos pad, String key, Object... args) {
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (p.getBlockPos().isWithinDistance(pad, 256)) {
				p.sendMessage(msg(key, args), false);
			}
		}
	}

	/** The weather satellite's report for {@code p}: when the rain or the storm comes, or when it stops. */
	public static void forecast(ServerPlayerEntity p) {
		MinecraftServer server = p.getServer();
		if (server == null || !has(server, p.getUuid(), "weather")) {
			return;
		}
		ServerWorld ow = server.getOverworld();
		if (!(ow.getLevelProperties() instanceof ServerWorldProperties props)) {
			return;
		}
		String ns = "message.periodic_elements.weather.";
		int day = 24000;
		Text line;
		if (props.isThundering()) {
			line = Text.translatable(ns + "storm_now", minutes(props.getThunderTime()));
		} else if (props.isRaining()) {
			line = Text.translatable(ns + "rain_now", minutes(props.getRainTime()));
		} else if (props.getClearWeatherTime() > 0) {
			line = Text.translatable(ns + "clear", minutes(props.getClearWeatherTime()));
		} else if (props.getRainTime() <= day) {
			boolean storm = props.getThunderTime() <= props.getRainTime() + 600;
			line = Text.translatable(ns + (storm ? "storm_in" : "rain_in"), minutes(props.getRainTime()));
		} else {
			line = Text.translatable(ns + "dry", minutes(props.getRainTime()));
		}
		p.sendMessage(line.copy().formatted(Formatting.AQUA), false);
	}

	private static String minutes(int ticks) {
		return Integer.toString(Math.max(1, Math.round(ticks / 1200f)));
	}
}
