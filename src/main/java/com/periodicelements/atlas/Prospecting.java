package com.periodicelements.atlas;

import com.periodicelements.OreContent;
import com.periodicelements.radiation.Radiation;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage 2 of the Atlas (levels 4 and 5): the ore layer (cores and mined ores), the radiation layer (electroscope and
 * Geiger readings) and the magnetometer's anomaly layer.
 */
public final class Prospecting {
	/** How many chunks out from the player's own the magnetometer reads (3 = a 7 x 7 survey). */
	public static final int SURVEY_CHUNKS = 3;
	/** Ores whose minerals pull a compass needle: iron, nickel, cobalt, chromium and ilmenite/magnetite sand. */
	private static final String[] MAGNETIC = {"iron", "pentlandite", "cobaltite", "chromite", "heavy_mineral_sand"};

	private Prospecting() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
			if (player instanceof ServerPlayerEntity sp) {
				minedOre(sp, pos, state);
			}
		});
	}

	public static boolean isOre(BlockState s) {
		return s.isIn(ConventionalBlockTags.ORES) || OreContent.ORES.contains(s.getBlock())
				|| Registries.BLOCK.getId(s.getBlock()).getPath().endsWith("_ore");
	}

	/** The ore's id with deepslate folded into the stone ore, so both count as one ore on the map. */
	public static String oreId(Block b) {
		if (com.periodicelements.GeoOres.baseOf(b) != null) return oreId(Registries.BLOCK.get(com.periodicelements.GeoOres.baseOf(b)));
		var id = Registries.BLOCK.getId(b);
		String path = id.getPath().startsWith("deepslate_") ? id.getPath().substring("deepslate_".length()) : id.getPath();
		var plain = net.minecraft.util.Identifier.of(id.getNamespace(), path);
		return Registries.BLOCK.containsId(plain) ? plain.toString() : id.toString();
	}

	public static boolean magnetic(String oreId) {
		for (String m : MAGNETIC) {
			if (oreId.contains(m)) return true;
		}
		return false;
	}

	private static Set<String> known(ServerPlayerEntity p) {
		return ResearchState.get(p.getServer()).known(p.getUuid());
	}

	static void minedOre(ServerPlayerEntity p, BlockPos pos, BlockState state) {
		if (p.getWorld().getRegistryKey() != World.OVERWORLD || !isOre(state) || !Atlas.has(known(p), "ores")
				|| !Atlas.canDraw(known(p))) {
			return;
		}
		AtlasData d = AtlasData.of(p.getServer(), p.getUuid());
		d.chunk(pos.getX(), pos.getZ()).ores.merge(oreId(state.getBlock()), 1, Integer::sum);
		d.overlayChanged();
	}

	/** Logs the radiation from the surroundings (not what the player carries) at the player's chunk. */
	public static final double BACKGROUND = 1e-7;

	public static void logRadiation(ServerPlayerEntity p) {
		if (p.getWorld().getRegistryKey() != World.OVERWORLD || Radiation.mode(p.getServer()) == Radiation.Mode.off
				|| !Atlas.has(known(p), "radiation")) {
			return;
		}
		AtlasData d = AtlasData.of(p.getServer(), p.getUuid());
		if (reading(p.getServerWorld(), d, p.getPos().add(0, 1, 0), Atlas.has(known(p), "isotopes"))) {
			d.overlayChanged();
		}
	}

	/**
	 * Logs the radiation at {@code at} on the chunk it is in: the natural background (about 0.1 µSv/h) plus what the
	 * blocks round about give, and with {@code spectrum} (gamma spectrometry) the share of each source element.
	 * Returns whether the chunk changed.
	 */
	public static boolean reading(ServerWorld w, AtlasData d, net.minecraft.util.math.Vec3d at, boolean spectrum) {
		AtlasData.ChunkInfo c = d.chunk(floor(at.x), floor(at.z));
		boolean changed = false;
		double rate;
		if (spectrum) {
			Map<String, Double> by = Radiation.surroundingsByElement(w, at);
			rate = BACKGROUND + by.values().stream().mapToDouble(Double::doubleValue).sum();
			Map<String, Float> iso = new java.util.TreeMap<>();
			by.forEach((k, v) -> {
				if (v > 1e-9) iso.put(k, v.floatValue());
			});
			if (!iso.equals(c.isotopes)) {
				c.isotopes.clear();
				c.isotopes.putAll(iso);
				changed = true;
			}
		} else {
			rate = BACKGROUND + Radiation.surroundings(w, at);
		}
		if (c.rad != (float) rate) {
			c.rad = (float) rate;
			changed = true;
		}
		return changed;
	}

	private static int floor(double v) {
		return net.minecraft.util.math.MathHelper.floor(v);
	}

	/**
	 * Airborne gamma spectrometry: flying (or 16 blocks above the ground) with a Geiger counter, the ground of the 3 x 3
	 * chunks below is read at once. Returns how many chunks changed.
	 */
	public static int aerial(ServerPlayerEntity p) {
		ServerWorld w = p.getServerWorld();
		int ground = w.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, p.getBlockX(), p.getBlockZ());
		if (!p.isFallFlying() && p.getY() - ground < AIRBORNE) return 0;
		AtlasData d = AtlasData.of(p.getServer(), p.getUuid());
		int pcx = p.getBlockX() >> 4, pcz = p.getBlockZ() >> 4, n = 0;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int x = ((pcx + i) << 4) + 8, z = ((pcz + j) << 4) + 8;
				if (w.getChunkManager().getWorldChunk(x >> 4, z >> 4) == null) continue;
				int y = w.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);
				if (reading(w, d, new net.minecraft.util.math.Vec3d(x + 0.5, y + 1, z + 0.5), true)) n++;
			}
		}
		if (n > 0) d.overlayChanged();
		return n;
	}

	public static final int AIRBORNE = 16;

	/** The isotope a source element is read as on the spectrum: its main gamma emitter in the ground or a reactor. */
	public static String isotope(String element) {
		return switch (element) {
			case "radium" -> "Ra-226";
			case "uranium" -> "U-238";
			case "thorium" -> "Th-232";
			case "radon" -> "Rn-222";
			case "polonium" -> "Po-210";
			case "actinium" -> "Ac-227";
			case "protactinium" -> "Pa-231";
			case "neptunium" -> "Np-237";
			case "plutonium" -> "Pu-239";
			case "americium" -> "Am-241";
			case "curium" -> "Cm-244";
			case "promethium" -> "Pm-147";
			case "astatine" -> "At-211";
			case "berkelium" -> "Bk-249";
			case "californium" -> "Cf-252";
			case "einsteinium" -> "Es-253";
			case "fermium" -> "Fm-257";
			case "mendelevium" -> "Md-258";
			case "fission_products" -> "Cs-137";
			default -> element;
		};
	}

	/** Every rock and ore in the column under {@code top}, top down; records the ores. Returns the message to show. */
	public static Text core(ServerPlayerEntity p, BlockPos top) {
		ServerWorld w = p.getServerWorld();
		List<String> layers = new ArrayList<>();
		Map<String, Integer> oreTop = new LinkedHashMap<>();
		Map<String, Integer> ores = new LinkedHashMap<>();
		BlockPos.Mutable m = new BlockPos.Mutable();
		int bottom = w.getBottomY();
		int lastRock = -1;
		for (int y = top.getY(); y >= bottom; y--) {
			BlockState s = w.getBlockState(m.set(top.getX(), y, top.getZ()));
			if (isOre(s)) {
				String id = oreId(s.getBlock());
				oreTop.putIfAbsent(id, y);
				ores.merge(id, 1, Integer::sum);
				continue;
			}
			int rock = Rocks.of(s.getBlock());
			if (rock != Rocks.UNKNOWN && rock != lastRock) {
				layers.add(Rocks.NAMES[rock] + "@" + y);
				lastRock = rock;
			}
		}
		AtlasData d = AtlasData.of(p.getServer(), p.getUuid());
		AtlasData.ChunkInfo c = d.chunk(top.getX(), top.getZ());
		c.cored = true;
		ores.forEach((k, v) -> c.ores.merge(k, v, Integer::sum));
		d.overlayChanged();
		MutableText rocks = Text.empty();
		for (int i = 0; i < layers.size(); i++) {
			String[] parts = layers.get(i).split("@");
			if (i > 0) rocks.append(" › ");
			rocks.append(Text.translatable("block.minecraft." + parts[0])).append(" " + parts[1]);
		}
		MutableText msg = Text.translatable("gui.periodic_elements.atlas.core", bottom, rocks);
		if (!oreTop.isEmpty()) {
			MutableText o = Text.empty();
			int i = 0;
			for (var e : oreTop.entrySet()) {
				if (i++ > 0) o.append(", ");
				o.append(Registries.BLOCK.get(net.minecraft.util.Identifier.of(e.getKey())).getName()).append(" Y " + e.getValue());
			}
			msg.append("\n").append(Text.translatable("gui.periodic_elements.atlas.core.ores", o));
		}
		return msg;
	}

	/**
	 * Reads the 7 x 7 chunks round the player: ore blocks counted per chunk and split into magnetic and dense, each
	 * scaled against the survey's own mean, as a real anomaly stands out from its background. Returns {max magnetic,
	 * max dense, chunks}.
	 */
	public static int[] survey(ServerPlayerEntity p) {
		ServerWorld w = p.getServerWorld();
		int pcx = p.getBlockX() >> 4, pcz = p.getBlockZ() >> 4, n = 2 * SURVEY_CHUNKS + 1;
		long[][] mag = new long[n][n], dense = new long[n][n];
		boolean[][] loaded = new boolean[n][n];
		long sumM = 0, sumD = 0;
		int count = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				WorldChunk ch = w.getChunkManager().getWorldChunk(pcx + i - SURVEY_CHUNKS, pcz + j - SURVEY_CHUNKS);
				if (ch == null) continue;
				loaded[i][j] = true;
				count++;
				long[] mm = {0}, dd = {0};
				for (ChunkSection sec : ch.getSectionArray()) {
					if (sec.isEmpty()) continue;
					sec.getBlockStateContainer().count((state, k) -> {
						if (isOre(state)) {
							if (magnetic(oreId(state.getBlock()))) mm[0] += k;
							else dd[0] += k;
						}
					});
				}
				mag[i][j] = mm[0];
				dense[i][j] = dd[0];
				sumM += mm[0];
				sumD += dd[0];
			}
		}
		if (count == 0) return new int[]{0, 0, 0};
		double meanM = Math.max(1, sumM / (double) count), meanD = Math.max(1, sumD / (double) count);
		AtlasData d = AtlasData.of(p.getServer(), p.getUuid());
		int maxM = 0, maxD = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (!loaded[i][j]) continue;
				int vm = (int) Math.min(255, Math.round(mag[i][j] / (2 * meanM) * 255));
				int vd = (int) Math.min(255, Math.round(dense[i][j] / (2 * meanD) * 255));
				AtlasData.ChunkInfo c = d.chunks.computeIfAbsent(AtlasData.chunkKey(pcx + i - SURVEY_CHUNKS, pcz + j - SURVEY_CHUNKS),
						k -> new AtlasData.ChunkInfo());
				c.magnetic = vm;
				c.dense = vd;
				maxM = Math.max(maxM, vm);
				maxD = Math.max(maxD, vd);
			}
		}
		d.overlayChanged();
		return new int[]{maxM, maxD, count};
	}

	/** A strength from 0-255 as one to four dots, as the magnetometer reads it. */
	public static Text strength(int v) {
		return Text.literal(v < 64 ? "·" : v < 128 ? "··" : v < 192 ? "···" : "····");
	}

	static List<String> oreList(AtlasData.ChunkInfo c) {
		List<String> out = new ArrayList<>();
		c.ores.forEach((k, v) -> out.add(k + "*" + v));
		return out;
	}

}
