package com.periodicelements.test;

import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasCopyItem;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.atlas.AtlasPayloads;
import com.periodicelements.atlas.AtlasTile;
import com.periodicelements.atlas.FieldAtlasItem;
import com.periodicelements.atlas.Rocks;
import com.periodicelements.atlas.SurveyBalloonItem;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.List;

/**
 * 2.7.0 Field Atlas (map plan stage 1). Mock players share one UUID, so every test has its own batch and starts from
 * an empty atlas and no research.
 */
public class AtlasTests implements FabricGameTest {
	private static void check(TestContext ctx, boolean ok, String what) {
		if (!ok) {
			ctx.throwGameTestException(what);
		}
	}

	private static ServerPlayerEntity player(TestContext ctx, String... projects) {
		ServerPlayerEntity player = ctx.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		MinecraftServer server = ctx.getWorld().getServer();
		ResearchState rs = ResearchState.get(server);
		rs.revoke(server, player.getUuid(), Research.all().stream().map(Research.Project::id).toList());
		rs.grant(server, player.getUuid(), List.of(projects));
		AtlasData data = AtlasData.of(server, player.getUuid());
		data.tiles.clear();
		data.markers.clear();
		data.chunks.clear();
		Atlas.forget(player.getUuid());
		var st = com.periodicelements.atlas.StationState.get(server);
		st.satellites.remove(player.getUuid());
		st.stations.values().removeIf(s -> s.owner.equals(player.getUuid()));
		BlockPos at = ctx.getAbsolutePos(new BlockPos(1, 2, 1));
		player.setPosition(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		return player;
	}

	private static AtlasData atlas(TestContext ctx, ServerPlayerEntity p) {
		return AtlasData.of(ctx.getWorld().getServer(), p.getUuid());
	}

	private static int colorAt(AtlasData d, BlockPos pos) {
		AtlasTile t = d.tile(pos.getX(), pos.getZ(), false);
		return t == null ? 0 : t.color[AtlasTile.index(pos.getX(), pos.getZ())] & 0xFF;
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_research")
	public void needsSurveyingAndTheAtlas(TestContext ctx) {
		ServerPlayerEntity p = player(ctx);
		p.getInventory().insertStack(new ItemStack(Atlas.FIELD_ATLAS));
		Atlas.tickPlayer(p);
		check(ctx, atlas(ctx, p).tiles.isEmpty(), "drew without Surveying");
		p.getInventory().clear();
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("surveying"));
		Atlas.tickPlayer(p);
		check(ctx, atlas(ctx, p).tiles.isEmpty(), "drew without carrying the Atlas");
		p.getInventory().insertStack(new ItemStack(Atlas.FIELD_ATLAS));
		Atlas.tickPlayer(p);
		check(ctx, !atlas(ctx, p).tiles.isEmpty(), "did not draw with Surveying and the Atlas");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_draw")
	public void drawsOnlyWithinReach(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		BlockPos block = ctx.getAbsolutePos(new BlockPos(3, 4, 3));
		ctx.setBlockState(new BlockPos(3, 4, 3), Blocks.GOLD_BLOCK);
		AtlasData d = atlas(ctx, p);
		int n = Atlas.record(ctx.getWorld(), d, p.getBlockX(), p.getBlockZ(), Atlas.reach(Research.all().isEmpty()
				? java.util.Set.of() : java.util.Set.of("surveying")), false);
		check(ctx, n > 1000, "drew only " + n + " columns at reach 24");
		check(ctx, colorAt(d, block) == MapColor.GOLD.id, "gold block drawn as " + colorAt(d, block));
		AtlasTile t = d.tile(block.getX(), block.getZ(), false);
		check(ctx, t.height[AtlasTile.index(block.getX(), block.getZ())] == block.getY(), "wrong height");
		BlockPos far = p.getBlockPos().add(30, 0, 0);
		check(ctx, colorAt(d, far) == 0, "drew 30 blocks away at reach 24");
		check(ctx, Atlas.reach(java.util.Set.of("surveying", "magnetic_compass", "astrolabe")) == Atlas.REACH_QUADRANT,
				"the quadrant should widen the reach");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_rocks")
	public void rockLayerFindsTheRockUnderTheSoil(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		ctx.setBlockState(new BlockPos(2, 5, 2), Blocks.GRASS_BLOCK);
		ctx.setBlockState(new BlockPos(2, 4, 2), Blocks.DIRT);
		ctx.setBlockState(new BlockPos(2, 3, 2), Blocks.GRANITE);
		BlockPos col = ctx.getAbsolutePos(new BlockPos(2, 5, 2));
		AtlasData d = atlas(ctx, p);
		Atlas.record(ctx.getWorld(), d, col.getX(), col.getZ(), 2, false);
		AtlasTile t = d.tile(col.getX(), col.getZ(), false);
		int i = AtlasTile.index(col.getX(), col.getZ());
		check(ctx, t.rock[i] == Rocks.UNKNOWN, "looked for rock without the Geological Map");
		Atlas.record(ctx.getWorld(), d, col.getX(), col.getZ(), 2, true);
		check(ctx, t.rock[i] == Rocks.of(Blocks.GRANITE), "rock " + t.rock[i] + ", wanted granite");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_copy")
	public void copyCarriesTheMapToAnotherAtlas(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		AtlasData d = atlas(ctx, p);
		Atlas.record(ctx.getWorld(), d, p.getBlockX(), p.getBlockZ(), 16, false);
		d.markers.add(new AtlasData.Marker("Camp", p.getBlockX(), p.getBlockZ()));
		int drawn = d.tiles.values().stream().mapToInt(AtlasTile::drawn).sum();
		ItemStack paper = new ItemStack(Items.PAPER, 2);
		check(ctx, FieldAtlasItem.makeCopy(p, ResearchState.get(ctx.getWorld().getServer()).known(p.getUuid()), paper).isEmpty(),
				"copied without Portolan Charts");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(),
				List.of("magnetic_compass", "astrolabe", "portolan"));
		ItemStack copy = FieldAtlasItem.makeCopy(p, ResearchState.get(ctx.getWorld().getServer()).known(p.getUuid()), paper);
		check(ctx, copy.isOf(Atlas.ATLAS_COPY) && paper.getCount() == 1, "no copy, or paper not used");
		// "Another player": the same mock UUID with its atlas wiped.
		d.tiles.clear();
		d.markers.clear();
		int gained = AtlasCopyItem.read(p, copy);
		check(ctx, gained == drawn, "copy gave " + gained + " columns, wanted " + drawn);
		check(ctx, d.markers.size() == 1 && d.markers.get(0).name().equals("Camp"), "marker not copied");
		check(ctx, copy.isEmpty(), "the copy should be used up");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_markers")
	public void markersAreLimitedBySurvey(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		for (int i = 0; i < 12; i++) {
			Atlas.editMarker(p, new AtlasPayloads.EditMarker(true, "m" + i, i * 10, 0));
		}
		check(ctx, atlas(ctx, p).markers.size() == Atlas.MARKERS, atlas(ctx, p).markers.size() + " markers, limit 8");
		Atlas.editMarker(p, new AtlasPayloads.EditMarker(false, "", 21, 1));
		check(ctx, atlas(ctx, p).markers.stream().noneMatch(m -> m.name().equals("m2")), "nearest marker not removed");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_save")
	public void tilesSurviveSaving(TestContext ctx) {
		AtlasTile t = new AtlasTile(-3, 5);
		t.color[77] = 12;
		t.height[77] = -50;
		t.height[78] = 300;
		t.rock[77] = 2;
		t.rev = 9;
		NbtCompound nbt = t.toNbt();
		AtlasTile back = AtlasTile.fromNbt(nbt);
		check(ctx, back.tx == -3 && back.tz == 5 && back.rev == 9 && back.color[77] == 12 && back.height[77] == -50
				&& back.height[78] == 300 && back.rock[77] == 2, "tile changed on saving");
		check(ctx, AtlasTile.keyX(AtlasTile.key(-3, 5)) == -3 && AtlasTile.keyZ(AtlasTile.key(-3, 5)) == 5, "tile key");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_vanilla")
	public void vanillaMapAndCompassNeedResearch(TestContext ctx) {
		check(ctx, "surveying".equals(Research.unlockedBy("item:vanilla_map")), "map gate");
		check(ctx, "magnetic_compass".equals(Research.unlockedBy("item:vanilla_compass")), "compass gate");
		ServerPlayerEntity p = player(ctx);
		SimpleInventory result = new SimpleInventory(1);
		CraftingResultSlot slot = new CraftingResultSlot(p, new CraftingInventory(p.playerScreenHandler, 2, 2), result, 0, 0, 0);
		result.setStack(0, new ItemStack(Items.COMPASS));
		check(ctx, !slot.canTakeItems(p), "took a compass without Magnetic Compass");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(),
				List.of("surveying", "iron_tools", "magnetic_compass"));
		check(ctx, slot.canTakeItems(p), "could not take a compass with Magnetic Compass");
		result.setStack(0, new ItemStack(Items.CLOCK));
		check(ctx, slot.canTakeItems(p), "an ungated item was held back");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_balloon")
	public void balloonDrawsFarAndIsUsedUp(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		ItemStack balloon = new ItemStack(Atlas.SURVEY_BALLOON, 2);
		check(ctx, SurveyBalloonItem.fly(p, balloon) < 0 && balloon.getCount() == 2, "flew without Hot-Air Balloon");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("hot_air_balloon"));
		int n = SurveyBalloonItem.fly(p, balloon);
		check(ctx, n > 0 && balloon.getCount() == 1, "balloon drew " + n + ", count " + balloon.getCount());
		ctx.complete();
	}

	// Stage 2 (3.10.0): ores, radiation, anomalies, radio.

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_core")
	public void coreDrillFindsOresAndMarksTheChunk(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		ctx.setBlockState(new BlockPos(4, 1, 4), Blocks.IRON_ORE);
		ctx.setBlockState(new BlockPos(4, 0, 4), Blocks.DEEPSLATE_IRON_ORE);
		BlockPos top = ctx.getAbsolutePos(new BlockPos(4, 3, 4));
		ItemStack drill = new ItemStack(Atlas.CORE_DRILL);
		check(ctx, !com.periodicelements.atlas.CoreDrillItem.drill(p, drill, top), "cored without Diamond Core Drill");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("core_drilling"));
		check(ctx, com.periodicelements.atlas.CoreDrillItem.drill(p, drill, top), "could not core");
		AtlasData.ChunkInfo c = atlas(ctx, p).chunk(top.getX(), top.getZ());
		check(ctx, c.cored && c.ores.getOrDefault("minecraft:iron_ore", 0) == 2, "core ores " + c.ores + " cored " + c.cored);
		check(ctx, drill.getDamage() == 1, "the drill should wear");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_mined")
	public void minedOresGoOnTheOreLayer(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		BlockPos pos = ctx.getAbsolutePos(new BlockPos(2, 2, 2));
		net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.invoker()
				.afterBlockBreak(ctx.getWorld(), p, pos, Blocks.GOLD_ORE.getDefaultState(), null);
		check(ctx, atlas(ctx, p).chunks.isEmpty(), "logged an ore without the research");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("core_drilling"));
		net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.invoker()
				.afterBlockBreak(ctx.getWorld(), p, pos, Blocks.GOLD_ORE.getDefaultState(), null);
		net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.invoker()
				.afterBlockBreak(ctx.getWorld(), p, pos, Blocks.STONE.getDefaultState(), null);
		var ores = atlas(ctx, p).chunk(pos.getX(), pos.getZ()).ores;
		check(ctx, ores.size() == 1 && ores.get("minecraft:gold_ore") == 1, "ores " + ores);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_magnetometer")
	public void magnetometerFindsTheIronChunk(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		for (int x = 0; x < 4; x++) {
			for (int z = 0; z < 4; z++) {
				ctx.setBlockState(new BlockPos(x, 1, z), Blocks.IRON_ORE);
			}
		}
		// Other tests can leave ore blocks in this chunk; clear the non-magnetic ones so only our iron counts.
		BlockPos corner = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		var w = ctx.getWorld();
		BlockPos.Mutable q = new BlockPos.Mutable();
		for (int x = corner.getX() & ~15; x < (corner.getX() & ~15) + 16; x++) {
			for (int z = corner.getZ() & ~15; z < (corner.getZ() & ~15) + 16; z++) {
				for (int y = w.getBottomY(); y < w.getTopY(); y++) {
					var st = w.getBlockState(q.set(x, y, z));
					if (com.periodicelements.atlas.Prospecting.isOre(st)
							&& !com.periodicelements.atlas.Prospecting.magnetic(com.periodicelements.atlas.Prospecting.oreId(st.getBlock()))) {
						w.setBlockState(q, Blocks.STONE.getDefaultState());
					}
				}
			}
		}
		ItemStack m = new ItemStack(Atlas.MAGNETOMETER);
		check(ctx, !com.periodicelements.atlas.MagnetometerItem.read(p, m), "read without the research");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(),
				List.of("core_drilling", "magnetic_prospecting"));
		check(ctx, com.periodicelements.atlas.MagnetometerItem.read(p, m), "could not read");
		BlockPos ore = ctx.getAbsolutePos(new BlockPos(0, 1, 0));
		AtlasData.ChunkInfo c = atlas(ctx, p).chunk(ore.getX(), ore.getZ());
		check(ctx, c.magnetic > 100 && c.dense <= 0, "iron chunk magnetic " + c.magnetic + " dense " + c.dense);
		check(ctx, com.periodicelements.atlas.Prospecting.magnetic("minecraft:iron_ore")
				&& !com.periodicelements.atlas.Prospecting.magnetic("periodic_elements:galena_ore"), "magnetic ores");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_radiation")
	public void readingsGoOnTheRadiationLayer(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		ctx.setBlockState(new BlockPos(1, 4, 1), net.minecraft.registry.Registries.BLOCK.get(
				net.minecraft.util.Identifier.of("periodic_elements", "pitchblende_ore")));
		com.periodicelements.atlas.Prospecting.logRadiation(p);
		check(ctx, atlas(ctx, p).chunks.isEmpty(), "logged radiation without the research");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("radiation_mapping"));
		com.periodicelements.atlas.Prospecting.logRadiation(p);
		float rad = atlas(ctx, p).chunk(p.getBlockX(), p.getBlockZ()).rad;
		check(ctx, rad > 2 * com.periodicelements.atlas.Prospecting.BACKGROUND, "radiation " + rad + " next to pitchblende");
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_radio")
	public void radioSharesSomeoneElsesMarkers(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		MinecraftServer server = ctx.getWorld().getServer();
		java.util.UUID other = java.util.UUID.nameUUIDFromBytes("atlas radio".getBytes());
		AtlasData theirs = AtlasData.of(server, other);
		theirs.markers.clear();
		theirs.markers.add(new AtlasData.Marker("Mine", 10, 10));
		BlockPos tx = p.getBlockPos().add(5, 0, 0);
		com.periodicelements.atlas.RadioState.get(server).transmitters.put(tx, other);
		var known = ResearchState.get(server).known(p.getUuid());
		check(ctx, Atlas.shared(p, known).isEmpty(), "heard the radio without the research");
		ResearchState.get(server).grant(server, p.getUuid(), List.of("radio_direction_finding"));
		var heard = Atlas.shared(p, ResearchState.get(server).known(p.getUuid()));
		check(ctx, heard.size() == 2 && heard.stream().anyMatch(m -> m.x() == 10 && m.z() == 10), "heard " + heard);
		com.periodicelements.atlas.RadioState.get(server).transmitters.remove(tx);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_copy2")
	public void copyCarriesTheLayers(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying", "magnetic_compass", "astrolabe", "portolan");
		AtlasData d = atlas(ctx, p);
		d.chunk(0, 0).ores.put("minecraft:coal_ore", 3);
		d.chunk(0, 0).rad = 2e-6f;
		ItemStack copy = FieldAtlasItem.makeCopy(p, ResearchState.get(ctx.getWorld().getServer()).known(p.getUuid()),
				new ItemStack(Items.PAPER));
		d.chunks.clear();
		AtlasCopyItem.read(p, copy);
		check(ctx, d.chunk(0, 0).ores.get("minecraft:coal_ore") == 3 && d.chunk(0, 0).rad > 0, "layers not copied");
		ctx.complete();
	}

	// --- 3.14.0, stage 3 ------------------------------------------------------------------------------------------

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_isotopes")
	public void spectrometrySplitsTheReadingByIsotope(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying", "radiation_mapping");
		ctx.setBlockState(new BlockPos(1, 4, 1), net.minecraft.registry.Registries.BLOCK.get(
				net.minecraft.util.Identifier.of("periodic_elements", "pitchblende_ore")));
		com.periodicelements.atlas.Prospecting.logRadiation(p);
		AtlasData.ChunkInfo c = atlas(ctx, p).chunk(p.getBlockX(), p.getBlockZ());
		check(ctx, c.rad > 0 && c.isotopes.isEmpty(), "split by isotope without the research");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("gamma_spectrometry"));
		com.periodicelements.atlas.Prospecting.logRadiation(p);
		check(ctx, !c.isotopes.isEmpty(), "no isotopes next to pitchblende");
		String main = c.isotopes.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).get().getKey();
		check(ctx, main.equals("uranium") || main.equals("radium"), "pitchblende read as " + c.isotopes);
		double sum = c.isotopes.values().stream().mapToDouble(Float::doubleValue).sum();
		check(ctx, Math.abs(sum + com.periodicelements.atlas.Prospecting.BACKGROUND - c.rad) < 0.01 * c.rad,
				"isotopes " + sum + " do not add up to " + c.rad);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_radar")
	public void radarSeesCreatures(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		ctx.spawnEntity(net.minecraft.entity.EntityType.ZOMBIE, new BlockPos(3, 2, 3));
		ctx.spawnEntity(net.minecraft.entity.EntityType.COW, new BlockPos(1, 2, 3));
		p.getInventory().insertStack(new ItemStack(Atlas.HANDHELD_RADAR));
		check(ctx, com.periodicelements.atlas.Satellites.contacts(p).isEmpty(), "radar without the research");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("radar"));
		List<AtlasPayloads.Contact> c = com.periodicelements.atlas.Satellites.contacts(p);
		check(ctx, c.stream().anyMatch(x -> x.kind() == 1) && c.stream().anyMatch(x -> x.kind() == 2), "contacts " + c);
		// Without the handheld set, a powered station of the player's sees them too.
		p.getInventory().clear();
		check(ctx, com.periodicelements.atlas.Satellites.contacts(p).isEmpty(), "contacts with no radar");
		var st = com.periodicelements.atlas.StationState.get(ctx.getWorld().getServer());
		var station = st.add(ctx.getAbsolutePos(new BlockPos(0, 1, 0)), com.periodicelements.atlas.StationBlock.Kind.RADAR, p.getUuid());
		check(ctx, com.periodicelements.atlas.Satellites.contacts(p).isEmpty(), "contacts from a station without power");
		station.powered = true;
		check(ctx, com.periodicelements.atlas.Satellites.contacts(p).size() >= 2, "no contacts from a powered station");
		st.stations.values().remove(station);
		ctx.killAllEntities();
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_launch")
	public void satellitesGoUpFromThePad(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		BlockPos pad = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		ctx.setBlockState(new BlockPos(1, 1, 1), Atlas.LAUNCH_PAD);
		var st = com.periodicelements.atlas.StationState.get(ctx.getWorld().getServer());
		ItemStack sat = new ItemStack(Atlas.SATELLITE);
		check(ctx, !com.periodicelements.atlas.LaunchPadBlock.launch(ctx.getWorld(), pad, p, sat) && st.satellites(p.getUuid()) == 0,
				"launched without the research");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("satellite"));
		ctx.setBlockState(new BlockPos(1, 3, 1), Blocks.STONE);
		check(ctx, !com.periodicelements.atlas.LaunchPadBlock.launch(ctx.getWorld(), pad, p, sat), "launched under a roof");
		// Under open sky: a pad on top of whatever is highest over this column.
		BlockPos open = new BlockPos(pad.getX(), ctx.getWorld().getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING,
				pad.getX(), pad.getZ()), pad.getZ());
		ctx.getWorld().setBlockState(open, Atlas.LAUNCH_PAD.getDefaultState());
		boolean up = com.periodicelements.atlas.LaunchPadBlock.launch(ctx.getWorld(), open, p, sat);
		ctx.getWorld().setBlockState(open, Blocks.AIR.getDefaultState());
		check(ctx, up, "did not launch under open sky");
		check(ctx, st.satellites(p.getUuid()) == 1 && (sat.isEmpty() || p.isCreative()), "satellite not in orbit or not used up: " + st.satellites(p.getUuid()) + " up, stack " + sat + ", creative " + p.isCreative());
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_satellite")
	public void trackingStationMapsLandNobodyVisited(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying", "satellite");
		MinecraftServer server = ctx.getWorld().getServer();
		var st = com.periodicelements.atlas.StationState.get(server);
		// A station far from anyone, its land never loaded.
		BlockPos far = new BlockPos(p.getBlockX() + 40000, 70, p.getBlockZ() + 40000);
		check(ctx, ctx.getWorld().getChunkManager().getWorldChunk(far.getX() >> 4, far.getZ() >> 4) == null, "far land is loaded");
		var station = st.add(far, com.periodicelements.atlas.StationBlock.Kind.TRACKING, p.getUuid());
		station.powered = true;
		com.periodicelements.atlas.Satellites.track(server, 50);
		check(ctx, colorAt(atlas(ctx, p), far) == 0, "mapped with no satellite up");
		st.satellites.put(p.getUuid(), 1);
		com.periodicelements.atlas.Satellites.track(server, 50);
		check(ctx, colorAt(atlas(ctx, p), far) != 0, "the station's own cell was not mapped");
		check(ctx, station.progress == 50, "progress " + station.progress);
		// The estimate is cheap enough to run every tick.
		long t0 = System.nanoTime();
		com.periodicelements.atlas.Satellites.track(server, 20);
		double ms = (System.nanoTime() - t0) / 1e6 / 20;
		check(ctx, ms < com.periodicelements.atlas.Satellites.BUDGET_NANOS / 1e6, "a cell takes " + ms + " ms");
		int n = 0;
		for (int dx = -24; dx < 24; dx++) {
			for (int dz = -24; dz < 24; dz++) {
				if (colorAt(atlas(ctx, p), far.add(dx, 0, dz)) != 0) n++;
			}
		}
		check(ctx, n > 1500, "only " + n + " columns mapped round the station");
		check(ctx, ctx.getWorld().getChunkManager().getWorldChunk(far.getX() >> 4, far.getZ() >> 4) == null, "mapping loaded the land");
		int[] seen = new int[2];
		java.util.Set<Long> cells = new java.util.HashSet<>();
		for (int i = 0; i < com.periodicelements.atlas.Satellites.CELLS; i++) {
			int[] c = com.periodicelements.atlas.Satellites.spiral(i);
			cells.add(((long) c[0] << 32) ^ (c[1] & 0xFFFFFFFFL));
			seen[0] = Math.max(seen[0], Math.abs(c[0]));
			seen[1] = Math.max(seen[1], Math.abs(c[1]));
		}
		check(ctx, cells.size() == com.periodicelements.atlas.Satellites.CELLS && seen[0] == 64 && seen[1] == 64, "spiral " + cells.size());
		st.stations.values().remove(station);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_landsat")
	public void scannerSeesOresNearTheSurface(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying", "satellite", "landsat");
		ctx.setBlockState(new BlockPos(1, 1, 1), Blocks.IRON_ORE);
		BlockPos abs = ctx.getAbsolutePos(new BlockPos(1, 1, 1));
		var chunk = ctx.getWorld().getWorldChunk(abs);
		com.periodicelements.atlas.Satellites.shallowOres(ctx.getWorld(), atlas(ctx, p), chunk);
		check(ctx, atlas(ctx, p).chunk(abs.getX(), abs.getZ()).ores.containsKey("minecraft:iron_ore"),
				"no iron noted: " + atlas(ctx, p).chunk(abs.getX(), abs.getZ()).ores);
		ctx.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "atlas_gps")
	public void gpsMarksWhereYouDie(TestContext ctx) {
		ServerPlayerEntity p = player(ctx, "surveying");
		com.periodicelements.atlas.Satellites.died(p);
		check(ctx, atlas(ctx, p).markers.isEmpty(), "death marker without GPS");
		ResearchState.get(ctx.getWorld().getServer()).grant(ctx.getWorld().getServer(), p.getUuid(), List.of("gps"));
		com.periodicelements.atlas.Satellites.died(p);
		p.setPosition(p.getX() + 5, p.getY(), p.getZ());
		com.periodicelements.atlas.Satellites.died(p);
		List<AtlasData.Marker> m = atlas(ctx, p).markers;
		check(ctx, m.size() == 1 && m.get(0).name().equals(com.periodicelements.atlas.Satellites.DEATH) && m.get(0).x() == p.getBlockX(),
				"death markers " + m);
		check(ctx, Atlas.countMarkers(m) == 0, "the death marker counts toward the limit");
		ctx.complete();
	}
}
