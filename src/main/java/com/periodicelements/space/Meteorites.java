package com.periodicelements.space;

import com.periodicelements.PeriodicElements;
import com.periodicelements.atlas.Atlas;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Level 8, stage 2 (3.62.0): meteorites. At dusk the sky may pick a spot near a player for tonight's fall; Space
 * Telescope owners are told where and when, and it goes on their Atlas. At that hour a {@link MeteorEntity} streaks
 * down and leaves a small crater with one to three meteorite blocks at the bottom. Players nearby are told where it
 * fell; those who know Radar (within 512 blocks) and telescope owners get it on their Atlas.
 */
public final class Meteorites {
	public static Block CHONDRITE, IRON_METEORITE, CARBONACEOUS_CHONDRITE;
	public static Item METEOR_CALLER;
	public static EntityType<MeteorEntity> METEOR;
	/** Chance that a meteorite falls on a given night (none during automated game tests: a crater would spoil them). */
	public static double NIGHTLY_CHANCE = System.getProperty("fabric-api.gametest") != null ? 0 : 0.25;
	/** Time of day the night's fall is chosen (dusk), and the window it comes down in. */
	public static final int DUSK = 13000, FIRST = 14000, LAST = 22000;
	/** How far from the chosen player it falls. */
	public static final int NEAREST = 48, FARTHEST = 128;
	/** Players this close hear where it fell; with Radar this close it goes on their Atlas. */
	public static final int HEARD = 256, RADAR_RANGE = 512;

	private Meteorites() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	private static Block block(List<Item> items, String name, Block block) {
		Registry.register(Registries.BLOCK, id(name), block);
		items.add(Registry.register(Registries.ITEM, id(name), new BlockItem(block, new Item.Settings()) {
			@Override
			public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
				tooltip.add(Text.translatable(getBlock().getTranslationKey() + ".desc").formatted(Formatting.GRAY));
			}
		}));
		return block;
	}

	static void register(List<Item> items) {
		CHONDRITE = block(items, "chondrite", new Block(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY)
				.strength(3.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.STONE)));
		IRON_METEORITE = block(items, "iron_meteorite", new Block(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
				.strength(5.0f, 6.0f).requiresTool().sounds(BlockSoundGroup.METAL)));
		CARBONACEOUS_CHONDRITE = block(items, "carbonaceous_chondrite", new Block(AbstractBlock.Settings.create()
				.mapColor(MapColor.BLACK).strength(1.5f, 4.0f).requiresTool().sounds(BlockSoundGroup.TUFF)));
		METEOR_CALLER = Registry.register(Registries.ITEM, id("meteor_caller"), new Item(new Item.Settings().maxCount(1)) {
			@Override
			public ActionResult useOnBlock(ItemUsageContext ctx) {
				if (ctx.getWorld() instanceof ServerWorld world) {
					BlockPos p = ctx.getBlockPos();
					double a = world.getRandom().nextDouble() * Math.PI * 2;
					MeteorEntity.send(world, p.getX(), p.getY(), p.getZ(), pick(world.getRandom()), Math.cos(a), Math.sin(a), false);
					if (ctx.getPlayer() != null) {
						ctx.getPlayer().sendMessage(Text.translatable("message.periodic_elements.meteor.called"), true);
					}
				}
				return ActionResult.SUCCESS;
			}

			@Override
			public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
				tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("tooltip.periodic_elements.creative_only").formatted(Formatting.DARK_PURPLE));
			}
		});
		items.add(METEOR_CALLER);
		METEOR = Registry.register(Registries.ENTITY_TYPE, id("meteor"),
				EntityType.Builder.<MeteorEntity>create(MeteorEntity::new, SpawnGroup.MISC).dimensions(1f, 1f)
						.maxTrackingRange(64).trackingTickInterval(1).alwaysUpdateVelocity(true).build());
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getRegistryKey() == World.OVERWORLD) {
				tick(world);
			}
		});
	}

	/** Which kind falls: most are chondrites, then iron meteorites, then carbonaceous chondrites. */
	public static Block pick(Random rnd) {
		int r = rnd.nextInt(100);
		return r < 60 ? CHONDRITE : r < 85 ? IRON_METEORITE : CARBONACEOUS_CHONDRITE;
	}

	public static boolean isMeteorite(BlockState s) {
		return s.isOf(CHONDRITE) || s.isOf(IRON_METEORITE) || s.isOf(CARBONACEOUS_CHONDRITE);
	}

	// --- The night sky -------------------------------------------------------------------------------------------

	static void tick(ServerWorld world) {
		SpaceState st = SpaceState.get(world.getServer());
		long now = world.getTimeOfDay();
		if (now % 24000 == DUSK && st.fallAt < 0 && world.getRandom().nextDouble() < NIGHTLY_CHANCE) {
			List<ServerPlayerEntity> players = new ArrayList<>(world.getPlayers(p -> !p.isSpectator()));
			if (!players.isEmpty()) {
				ServerPlayerEntity near = players.get(world.getRandom().nextInt(players.size()));
				double a = world.getRandom().nextDouble() * Math.PI * 2;
				int d = NEAREST + world.getRandom().nextInt(FARTHEST - NEAREST + 1);
				schedule(world, near.getBlockX() + (int) Math.round(Math.cos(a) * d), near.getBlockZ() + (int) Math.round(Math.sin(a) * d),
						now - DUSK + FIRST + world.getRandom().nextInt(LAST - FIRST));
			}
		}
		if (st.fallAt >= 0 && now >= st.fallAt) {
			if (!st.fallSent) {
				int groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, st.fallX, st.fallZ);
				// it flies away from the nearest player, so its whole streak is in view and starts in loaded land
				ServerPlayerEntity p = nearest(world, st.fallX, st.fallZ);
				double dx = p == null ? 1 : st.fallX - p.getX(), dz = p == null ? 0 : st.fallZ - p.getZ();
				double len = Math.max(1e-3, Math.hypot(dx, dz));
				BlockPos from = BlockPos.ofFloored(st.fallX - dx / len * MeteorEntity.BACK, groundY, st.fallZ - dz / len * MeteorEntity.BACK);
				if (world.shouldTickEntity(new BlockPos(st.fallX, groundY, st.fallZ)) && world.shouldTickEntity(from)) {
					MeteorEntity.send(world, st.fallX, groundY - 1, st.fallZ, pick(world.getRandom()), dx / len, dz / len, true);
					st.fallSent = true;
					st.markDirty();
				} else {
					impact(world, st.fallX, st.fallZ, pick(world.getRandom()), world.getRandom(), true);   // nobody there to see it
				}
			} else if (now > st.fallAt + 600) {
				impact(world, st.fallX, st.fallZ, pick(world.getRandom()), world.getRandom(), true);  // its chunk was unloaded mid-flight
			}
		}
	}

	/** Tonight's fall at column ({@code x}, {@code z}) at time of day {@code at}; telescope owners hear of it now. */
	public static void schedule(ServerWorld world, int x, int z, long at) {
		SpaceState st = SpaceState.get(world.getServer());
		st.fallX = x;
		st.fallZ = z;
		st.fallAt = at;
		st.fallSent = false;
		st.markDirty();
		String clock = clock(at);
		for (ServerPlayerEntity p : world.getServer().getPlayerManager().getPlayerList()) {
			if (Space.has(world.getServer(), p.getUuid(), "telescope")) {
				Atlas.markMeteor(world.getServer(), p.getUuid(), x, z);
				p.sendMessage(Text.translatable("message.periodic_elements.meteor.forecast", x, z, clock).formatted(Formatting.GOLD), false);
			}
		}
	}

	public static String clock(long timeOfDay) {
		long t = Math.floorMod(timeOfDay, 24000L);
		long h = (t / 1000 + 6) % 24, m = (t % 1000) * 60 / 1000;
		return String.format(Locale.ROOT, "%02d:%02d", h, m);
	}

	private static ServerPlayerEntity nearest(ServerWorld world, int x, int z) {
		ServerPlayerEntity best = null;
		double bestD = Double.MAX_VALUE;
		for (ServerPlayerEntity p : world.getPlayers()) {
			double d = Math.hypot(p.getX() - x, p.getZ() - z);
			if (d < bestD) {
				bestD = d;
				best = p;
			}
		}
		return best;
	}

	// --- The impact ----------------------------------------------------------------------------------------------

	/** Blocks a crater may dig out: not air, not unbreakable or very hard, nothing with contents. */
	private static boolean diggable(ServerWorld w, BlockPos p, BlockState s) {
		if (s.isAir() || w.getBlockEntity(p) != null) return false;
		float h = s.getHardness(w, p);
		return h >= 0 && h < 30 && !s.isOf(Blocks.BEDROCK);
	}

	/**
	 * A meteorite of {@code kind} hits column ({@code x}, {@code z}): a bowl-shaped crater 3 to 5 blocks across the
	 * rim, ejecta round it, and one to three meteorite blocks on its floor. Returns where the main block lies.
	 */
	public static BlockPos impact(ServerWorld w, int x, int z, Block kind, Random rnd, boolean scheduled) {
		w.getChunk(x >> 4, z >> 4);   // load it if nobody is near
		return impact(w, x, w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z, kind, rnd, scheduled);
	}

	/** As above, with the ground surface at {@code top} given (the game tests' boxes have a barrier roof). */
	public static BlockPos impact(ServerWorld w, int x, int top, int z, Block kind, Random rnd, boolean scheduled) {
		int r = 3 + rnd.nextInt(3);
		int depth = Math.max(2, (int) Math.round(r * 0.6));
		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				double d2 = dx * dx + dz * dz;
				if (d2 > (r + 0.3) * (r + 0.3)) continue;
				int bottom = top - (int) Math.floor(depth * (1 - d2 / (r * r)));
				int colTop = Math.min(w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz) - 1, top + 3);
				for (int y = bottom + 1; y <= Math.max(colTop, top); y++) {
					BlockPos p = new BlockPos(x + dx, y, z + dz);
					BlockState s = w.getBlockState(p);
					if (diggable(w, p, s)) w.setBlockState(p, Blocks.AIR.getDefaultState());
				}
				BlockPos floor = new BlockPos(x + dx, bottom, z + dz);
				BlockState fs = w.getBlockState(floor);
				if (diggable(w, floor, fs) && fs.isOpaqueFullCube(w, floor)) {
					int roll = rnd.nextInt(10);
					if (roll < 3) w.setBlockState(floor, Blocks.GRAVEL.getDefaultState());
					else if (roll < 5) w.setBlockState(floor, Blocks.COARSE_DIRT.getDefaultState());
				}
			}
		}
		// ejecta thrown over the rim
		for (int dx = -r - 2; dx <= r + 2; dx++) {
			for (int dz = -r - 2; dz <= r + 2; dz++) {
				double d = Math.sqrt(dx * dx + dz * dz);
				if (d <= r + 0.3 || d > r + 2 || rnd.nextFloat() > 0.55f) continue;
				BlockPos above = new BlockPos(x + dx, w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz), z + dz);
				if (w.getBlockState(above).isReplaceable() && w.getBlockState(above.down()).isSolidBlock(w, above.down())) {
					w.setBlockState(above, (rnd.nextBoolean() ? Blocks.GRAVEL : Blocks.COARSE_DIRT).getDefaultState());
				}
			}
		}
		BlockPos main = new BlockPos(x, top - depth, z);
		w.setBlockState(main, kind.getDefaultState());
		int extra = kind == IRON_METEORITE ? rnd.nextInt(2) : rnd.nextInt(3);
		int[][] around = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		for (int i = 0; i < extra; i++) {
			int[] o = around[rnd.nextInt(around.length)];
			BlockPos q = main.add(o[0], 0, o[1]);
			if (w.getBlockState(q.up()).isAir() || w.getBlockState(q.up()).isReplaceable()) w.setBlockState(q, kind.getDefaultState());
		}
		Vec3d at = Vec3d.ofCenter(main.up());
		w.createExplosion(null, at.x, at.y, at.z, 2.5f, false, World.ExplosionSourceType.NONE);
		w.playSound(null, at.x, at.y, at.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 8f, 0.6f);
		MeteorEntity.puff(w, ParticleTypes.LAVA, at, 30, 1.5, 0.0);
		MeteorEntity.puff(w, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, at, 20, 1.5, 0.03);
		MeteorEntity.puff(w, ParticleTypes.EXPLOSION_EMITTER, at, 1, 0.0, 0.0);
		landed(w, main, scheduled);
		return main;
	}

	/** Who hears of it, and whose Atlas gets it. */
	private static void landed(ServerWorld w, BlockPos at, boolean scheduled) {
		MinecraftServer server = w.getServer();
		SpaceState st = SpaceState.get(server);
		if (scheduled) {
			st.fallAt = -1;
			st.fallSent = false;
		}
		st.falls++;
		st.markDirty();
		// telescope owners, online or not
		List<UUID> marked = new ArrayList<>();
		st.satellites.forEach((owner, kinds) -> {
			if (kinds.getOrDefault("telescope", 0) > 0) marked.add(owner);
		});
		for (ServerPlayerEntity p : w.getPlayers()) {
			double d = Math.hypot(p.getX() - at.getX(), p.getZ() - at.getZ());
			if (!marked.contains(p.getUuid()) && d <= RADAR_RANGE && Atlas.knows(p, "radar")) marked.add(p.getUuid());
		}
		for (UUID u : marked) {
			Atlas.markMeteor(server, u, at.getX(), at.getZ());
		}
		for (ServerPlayerEntity p : w.getPlayers()) {
			double dx = at.getX() - p.getX(), dz = at.getZ() - p.getZ();
			double d = Math.hypot(dx, dz);
			if (marked.contains(p.getUuid())) {
				p.sendMessage(Text.translatable("message.periodic_elements.meteor.fell_marked", at.getX(), at.getZ()).formatted(Formatting.GOLD), false);
			} else if (d <= HEARD) {
				p.sendMessage(Text.translatable("message.periodic_elements.meteor.fell", Math.round(d),
						Text.translatable("message.periodic_elements.dir." + direction(dx, dz))).formatted(Formatting.GOLD), false);
			}
		}
	}

	/** Compass direction of (dx, dz): north is -z. */
	public static String direction(double dx, double dz) {
		String[] names = {"s", "sw", "w", "nw", "n", "ne", "e", "se"};
		double deg = Math.toDegrees(Math.atan2(-dx, dz));   // 0 = south, 90 = west, 180 = north, 270 = east
		int i = (int) Math.floorMod(Math.round(deg / 45.0), 8L);
		return names[i];
	}

	/** Test helper: whether the meteorite at {@code p} sits in a crater (air above it). */
	public static boolean inCrater(ServerWorld w, BlockPos p) {
		return w.getBlockState(p.up()).isAir() && isMeteorite(w.getBlockState(p));
	}
}
