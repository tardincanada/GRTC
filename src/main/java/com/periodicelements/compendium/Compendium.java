package com.periodicelements.compendium;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Tree Compendium (3.1.0) and the Plant Compendium (3.5.0): carried in the hotbar it notes the trees its carrier looks at for a second (or fells)
 * and the biomes they walk into; the client shows the biome and the tree under the crosshair, and the book shows what
 * the player has noted. Research unlocks compendium:classification (Species Plantarum), compendium:climate (Geography
 * of Plants) and compendium:conservation (The Red List).
 */
public final class Compendium {
	public static Item TREE_COMPENDIUM, PLANT_COMPENDIUM, MINERAL_COMPENDIUM, ANIMAL_COMPENDIUM, COMPENDIUM_NOTES;
	/** 3.50.0: how far an animal can be noted from. */
	public static final double ANIMAL_REACH = 16.0;
	/** How long the Magnifying Lens must rest on a rock or plant to analyse it. */
	public static final int ANALYSE_TICKS = 40;
	/** How far a tree can be noted from, and how long the carrier must look at it. */
	public static final double REACH = 8.0;
	public static final int LOOK_TICKS = 20;
	private static final int TICKS = 5;

	private static final Map<UUID, Object[]> LOOKING = new HashMap<>();
	private static final Map<UUID, Object[]> ANALYSING = new HashMap<>();
	private static final Map<UUID, String> FORMATION = new HashMap<>();

	private Compendium() {
	}

	public static boolean has(Set<String> known, String feature) {
		return Research.allows(known, "compendium:" + feature);
	}

	public static boolean knows(PlayerEntity player, String feature) {
		return has(ResearchAccess.known(player), feature);
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	public static void register(List<Item> items) {
		CompendiumData.load();
		TREE_COMPENDIUM = Registry.register(Registries.ITEM, id("tree_compendium"),
				new TreeCompendiumItem(new Item.Settings().maxCount(1), CompendiumData.TREE));
		PLANT_COMPENDIUM = Registry.register(Registries.ITEM, id("plant_compendium"),
				new TreeCompendiumItem(new Item.Settings().maxCount(1), CompendiumData.PLANT));
		MINERAL_COMPENDIUM = Registry.register(Registries.ITEM, id("mineral_compendium"),
				new TreeCompendiumItem(new Item.Settings().maxCount(1), CompendiumData.MINERAL));
		ANIMAL_COMPENDIUM = Registry.register(Registries.ITEM, id("animal_compendium"),
				new TreeCompendiumItem(new Item.Settings().maxCount(1), CompendiumData.ANIMAL));
		COMPENDIUM_NOTES = Registry.register(Registries.ITEM, id("compendium_notes"),
				new CompendiumNotesItem(new Item.Settings().maxCount(1)));
		items.add(TREE_COMPENDIUM);
		items.add(PLANT_COMPENDIUM);
		items.add(MINERAL_COMPENDIUM);
		items.add(ANIMAL_COMPENDIUM);
		items.add(COMPENDIUM_NOTES);
	}

	public static void init() {
		PayloadTypeRegistry.playS2C().register(CompendiumSyncPayload.ID, CompendiumSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FormationPayload.ID, FormationPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % TICKS == 0) {
				for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
					tickPlayer(p, server.getTicks());
				}
			}
		});
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
			if (player instanceof ServerPlayerEntity sp) {
				CompendiumData.Tree t = entryOf(sp, state.getBlock());
				if (t != null) note(sp, t.key(), pos);
			}
		});
	}

	/** Either compendium in the hotbar or the off hand. */
	public static boolean carries(PlayerEntity player) {
		return carries(player, TREE_COMPENDIUM) || carries(player, PLANT_COMPENDIUM) || carries(player, MINERAL_COMPENDIUM)
				|| carries(player, ANIMAL_COMPENDIUM);
	}

	/** This book in the hotbar or the off hand. */
	public static boolean carries(PlayerEntity player, Item book) {
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (inv.getStack(i).isOf(book)) return true;
		}
		return player.getOffHandStack().isOf(book);
	}

	public static Item book(String kind) {
		return CompendiumData.PLANT.equals(kind) ? PLANT_COMPENDIUM : CompendiumData.MINERAL.equals(kind) ? MINERAL_COMPENDIUM
				: CompendiumData.ANIMAL.equals(kind) ? ANIMAL_COMPENDIUM : TREE_COMPENDIUM;
	}

	/** The tree (with the Tree Compendium carried) or plant (with the Plant Compendium) a block belongs to, or null. */
	@Nullable
	public static CompendiumData.Tree entryOf(PlayerEntity player, net.minecraft.block.Block block) {
		if (carries(player, TREE_COMPENDIUM)) {
			CompendiumData.Tree t = CompendiumData.treeOf(block);
			if (t != null) return t;
		}
		if (carries(player, PLANT_COMPENDIUM)) {
			CompendiumData.Tree t = CompendiumData.plantOf(block);
			if (t != null) return t;
		}
		if (carries(player, MINERAL_COMPENDIUM)) {
			CompendiumData.Tree t = CompendiumData.mineralOf(block);
			if (t != null) return t;
		}
		return carries(player, ANIMAL_COMPENDIUM) ? CompendiumData.animalOf(block) : null;
	}

	/** The animal the player looks at within {@link #ANIMAL_REACH} (not through blocks), or null. */
	@Nullable
	public static net.minecraft.entity.Entity animalLookedAt(PlayerEntity player, float delta) {
		net.minecraft.util.math.Vec3d eye = player.getCameraPosVec(delta);
		net.minecraft.util.math.Vec3d dir = player.getRotationVec(delta);
		double reach = ANIMAL_REACH;
		HitResult block = player.raycast(reach, delta, false);
		if (block.getType() != HitResult.Type.MISS) reach = block.getPos().distanceTo(eye);
		net.minecraft.util.math.Vec3d end = eye.add(dir.multiply(reach));
		net.minecraft.util.math.Box box = player.getBoundingBox().stretch(dir.multiply(reach)).expand(1.0);
		net.minecraft.util.hit.EntityHitResult hit = net.minecraft.entity.projectile.ProjectileUtil.raycast(player, eye, end, box,
				e -> !e.isSpectator() && CompendiumData.animalOf(e.getType()) != null, reach * reach);
		return hit == null ? null : hit.getEntity();
	}

	/** Notes the animal an entity is, if it is in the Animal Compendium. */
	public static boolean noteAnimal(ServerPlayerEntity player, net.minecraft.entity.Entity e) {
		CompendiumData.Tree t = CompendiumData.animalOf(e.getType());
		return t != null && note(player, t.key(), e.getBlockPos());
	}

	/** Holding a Magnifying Lens in either hand. */
	public static boolean holdsLens(PlayerEntity player) {
		return player.getMainHandStack().isOf(com.periodicelements.ModContent.MAGNIFYING_LENS)
				|| player.getOffHandStack().isOf(com.periodicelements.ModContent.MAGNIFYING_LENS);
	}

	/** Lens resting on a rock or plant for {@link #ANALYSE_TICKS}: notes it and its analysis. */
	private static void analyse(ServerPlayerEntity player, int now) {
		CompendiumData.Tree t = null;
		BlockPos.Mutable at = new BlockPos.Mutable();
		if (!player.isSpectator() && holdsLens(player)) {
			HitResult hit = player.raycast(REACH, 1f, false);
			if (hit.getType() == HitResult.Type.BLOCK) {
				at.set(((BlockHitResult) hit).getBlockPos());
				var block = player.getWorld().getBlockState(at).getBlock();
				t = CompendiumData.mineralOf(block);
				if (t == null) t = CompendiumData.plantOf(block);
			}
		}
		if (t == null) {
			ANALYSING.remove(player.getUuid());
			return;
		}
		String key = CompendiumData.analysedKey(t);
		if (DiscoveryState.get(player.getServer()).knows(player.getUuid(), key)) return;
		Object[] look = ANALYSING.get(player.getUuid());
		if (look == null || !look[0].equals(key)) {
			ANALYSING.put(player.getUuid(), new Object[]{key, now});
		} else if (now - (Integer) look[1] >= ANALYSE_TICKS) {
			DiscoveryState s = DiscoveryState.get(player.getServer());
			s.add(player.getUuid(), t.key(), at.asLong());
			s.add(player.getUuid(), key, at.asLong());
			sync(player);
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.analysed", Text.translatable(t.nameKey()))
					.formatted(Formatting.AQUA), true);
		}
	}

	/** The Geological formation under the player for the corner panel, or "" (no Mineral Compendium, no Hutton, other world). */
	public static String formationOf(ServerPlayerEntity player) {
		if (!carries(player, MINERAL_COMPENDIUM) || !knows(player, "origin")) return "";
		var world = player.getServerWorld();
		if (world.getRegistryKey() != net.minecraft.world.World.OVERWORLD) return "";
		if (!(world.getChunkManager().getChunkGenerator() instanceof net.minecraft.world.gen.chunk.NoiseChunkGenerator gen)
				|| !gen.getSettings().matchesKey(com.periodicelements.worldgen.GeoWorld.SETTINGS)) {
			return "";
		}
		var g = com.periodicelements.worldgen.GeoWorld.geology(world.getChunkManager().getNoiseConfig());
		return formation(g.column(player.getBlockX(), player.getBlockZ()));
	}

	/** The formation a Geological column belongs to (ids of compendium.periodic_elements.formation.*). */
	public static String formation(com.periodicelements.worldgen.Geology.Column c) {
		if (c.ocean > 0.5) return "ocean_floor";
		if (c.neck) return "volcanic_neck";
		if (c.mountain > 0.45) return "mountain_belt";
		if (c.dome) return "granite_dome";
		return switch (c.province) {
			case OCEAN -> "ocean_floor";
			case CRATON -> "granite_shield";
			case BASIN -> c.plateau > 0.5 ? "mesa" : "sedimentary_basin";
			case VOLCANIC -> "basalt_plateau";
		};
	}

	/** The tree or plant (as for {@link #entryOf}) the player looks at within reach, or null. */
	@Nullable
	public static CompendiumData.Tree lookedAt(PlayerEntity player, BlockPos.Mutable at) {
		HitResult hit = player.raycast(REACH, 1f, false);
		if (hit.getType() != HitResult.Type.BLOCK) return null;
		BlockPos pos = ((BlockHitResult) hit).getBlockPos();
		at.set(pos);
		return entryOf(player, player.getWorld().getBlockState(pos).getBlock());
	}

	/** The biome id at the player's feet, e.g. "jungle" (vanilla ids without the namespace), or null. */
	@Nullable
	public static String biomeAt(PlayerEntity player) {
		RegistryEntry<Biome> entry = player.getWorld().getBiome(player.getBlockPos());
		return entry.getKey().map(RegistryKey::getValue)
				.map(i -> i.getNamespace().equals("minecraft") ? i.getPath() : i.toString()).orElse(null);
	}

	public static void tickPlayer(ServerPlayerEntity player, int now) {
		analyse(player, now);
		String formation = player.isSpectator() ? "" : formationOf(player);
		if (!formation.equals(FORMATION.getOrDefault(player.getUuid(), ""))) {
			FORMATION.put(player.getUuid(), formation);
			ServerPlayNetworking.send(player, new FormationPayload(formation));
		}
		if (player.isSpectator() || !carries(player)) {
			LOOKING.remove(player.getUuid());
			return;
		}
		String biome = biomeAt(player);
		if (biome != null && CompendiumData.BIOME_BY_ID.containsKey(biome)) {
			note(player, "biome:" + biome, player.getBlockPos());
		}
		BlockPos.Mutable at = new BlockPos.Mutable();
		CompendiumData.Tree t = lookedAt(player, at);
		if (t == null && carries(player, ANIMAL_COMPENDIUM)) {
			net.minecraft.entity.Entity e = animalLookedAt(player, 1f);
			if (e != null) {
				t = CompendiumData.animalOf(e.getType());
				at.set(e.getBlockPos());
			}
			if (now % 20 == 0) {
				for (Map.Entry<String, List<String>> en : CompendiumData.ANIMAL_ITEMS.entrySet()) {
					for (String i : en.getValue()) {
						Identifier ident = Identifier.of(i);
						if (Registries.ITEM.containsId(ident) && player.getInventory().contains(new net.minecraft.item.ItemStack(Registries.ITEM.get(ident)))) {
							note(player, "animal:" + en.getKey(), player.getBlockPos());
						}
					}
				}
			}
		}
		if (t == null) {
			LOOKING.remove(player.getUuid());
			return;
		}
		Object[] look = LOOKING.get(player.getUuid());
		if (look == null || !look[0].equals(t.key())) {
			LOOKING.put(player.getUuid(), new Object[]{t.key(), now});
		} else if (now - (Integer) look[1] >= LOOK_TICKS) {
			note(player, t.key(), at);
		}
	}

	/** Notes a tree or biome for the player; tells them and syncs if it is new. Returns whether it was new. */
	public static boolean note(ServerPlayerEntity player, String key, BlockPos pos) {
		if (!DiscoveryState.get(player.getServer()).add(player.getUuid(), key, pos.asLong())) return false;
		sync(player);
		String id = key.substring(key.indexOf(':') + 1);
		if (key.startsWith("tree:")) {
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.new_tree",
					Text.translatable("tree.periodic_elements." + id)).formatted(Formatting.DARK_GREEN), true);
		} else if (key.startsWith("mineral:")) {
			CompendiumData.Tree t = CompendiumData.MINERAL_BY_ID.get(id);
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.new_mineral",
					t == null ? Text.literal(id) : Text.translatable(t.nameKey())).formatted(Formatting.DARK_GREEN), true);
		} else if (key.startsWith("animal:")) {
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.new_animal",
					Text.translatable("animal.periodic_elements." + id)).formatted(Formatting.DARK_GREEN), true);
		} else if (key.startsWith("plant:")) {
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.new_plant",
					Text.translatable("plant.periodic_elements." + id)).formatted(Formatting.DARK_GREEN), true);
		} else {
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.new_biome",
					Text.translatable(biomeKey(id))).formatted(Formatting.DARK_GREEN), true);
		}
		return true;
	}

	public static String biomeKey(String biome) {
		Identifier i = biome.contains(":") ? Identifier.of(biome) : Identifier.ofVanilla(biome);
		return i.toTranslationKey("biome");
	}

	public static void sync(ServerPlayerEntity player) {
		Map<String, Long> notes = DiscoveryState.get(player.getServer()).of(player.getUuid());
		List<String> keys = new ArrayList<>(notes.keySet());
		List<Long> pos = new ArrayList<>(notes.values());
		ServerPlayNetworking.send(player, new CompendiumSyncPayload(keys, pos));
	}

	/** Notes every tree and biome in the book (tests, the reveal command); returns how many were new. */
	public static int revealAll(ServerPlayerEntity player) {
		DiscoveryState s = DiscoveryState.get(player.getServer());
		int n = 0;
		long here = player.getBlockPos().asLong();
		for (CompendiumData.Tree t : CompendiumData.TREES) {
			if (s.add(player.getUuid(), t.key(), here)) n++;
		}
		for (CompendiumData.Tree t : CompendiumData.PLANTS) {
			if (s.add(player.getUuid(), t.key(), here)) n++;
			if (s.add(player.getUuid(), CompendiumData.analysedKey(t), here)) n++;
		}
		for (CompendiumData.Tree t : CompendiumData.MINERALS) {
			if (s.add(player.getUuid(), t.key(), here)) n++;
			if (s.add(player.getUuid(), CompendiumData.analysedKey(t), here)) n++;
		}
		for (CompendiumData.Tree t : CompendiumData.ANIMALS) {
			if (s.add(player.getUuid(), t.key(), here)) n++;
		}
		for (CompendiumData.Biome b : CompendiumData.BIOMES) {
			if (s.add(player.getUuid(), b.key(), here)) n++;
		}
		sync(player);
		return n;
	}

	public static void forgetLooks() {
		LOOKING.clear();
		ANALYSING.clear();
	}
}
