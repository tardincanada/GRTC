package com.periodicelements.client;

import com.periodicelements.PeriodicElements;
import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.atlas.AtlasPayloads;
import com.periodicelements.atlas.AtlasTile;
import com.periodicelements.atlas.FieldAtlasItem;
import com.periodicelements.atlas.Rocks;
import com.periodicelements.research.ClientResearch;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The client's copy of the local player's Atlas: tiles as the server last sent them, their textures, the markers. */
public final class AtlasClient {
	public static final class Tile {
		public final int tx, tz;
		public int rev = -1;
		public byte[] color = new byte[AtlasTile.AREA];
		public short[] height = new short[AtlasTile.AREA];
		public byte[] rock = new byte[AtlasTile.AREA];
		NativeImageBackedTexture texture;
		Identifier id;
		int builtRev = -2, builtMode = -1;

		Tile(int tx, int tz) {
			this.tx = tx;
			this.tz = tz;
		}
	}

	/** Drawing modes: bit 0 = precise (theodolite), bit 1 = rock layer. */
	public static final int PRECISE = 1, ROCKS = 2;
	private static final int PAPER = 0xECE2C4;

	static final Map<Long, Tile> TILES = new HashMap<>();
	static List<AtlasData.Marker> markers = List.of();
	static List<AtlasData.Marker> shared = List.of();
	/** Overlay layers by chunk key, as last sent; -1 = never fetched for the tiles now in view. */
	static final Map<Long, AtlasPayloads.ChunkEntry> CHUNKS = new HashMap<>();
	static int overlayRev = -1;
	private static java.util.Set<Long> lastKeys = java.util.Set.of();
	static int markerLimit = Atlas.MARKERS;
	/** Stage 3: what the radar sees now, and the marker the GPS is routing to (null = none). */
	static List<AtlasPayloads.Contact> contacts = List.of();
	static long contactsAt;
	static AtlasData.Marker route;

	private AtlasClient() {
	}

	public static void register() {
		FieldAtlasItem.open = () -> MinecraftClient.getInstance().setScreen(new AtlasScreen());
		ClientPlayNetworking.registerGlobalReceiver(AtlasPayloads.Tile.ID, (p, ctx) -> {
			Tile t = TILES.computeIfAbsent(p.key(), k -> new Tile(AtlasTile.keyX(k), AtlasTile.keyZ(k)));
			t.rev = p.rev();
			System.arraycopy(p.color(), 0, t.color, 0, Math.min(AtlasTile.AREA, p.color().length));
			AtlasTile.readHeights(p.height(), t.height);
			System.arraycopy(p.rock(), 0, t.rock, 0, Math.min(AtlasTile.AREA, p.rock().length));
		});
		ClientPlayNetworking.registerGlobalReceiver(AtlasPayloads.Markers.ID, (p, ctx) -> {
			markers = new ArrayList<>(p.markers());
			markerLimit = p.limit();
			shared = new ArrayList<>(p.shared());
		});
		ClientPlayNetworking.registerGlobalReceiver(AtlasPayloads.Overlay.ID, (p, ctx) -> {
			CHUNKS.clear();
			for (AtlasPayloads.ChunkEntry e : p.chunks()) CHUNKS.put(e.key(), e);
			overlayRev = p.rev();
		});
		ClientPlayNetworking.registerGlobalReceiver(AtlasPayloads.Contacts.ID, (p, ctx) -> {
			contacts = new ArrayList<>(p.contacts());
			contactsAt = System.currentTimeMillis();
		});
		Minimap.register();
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(AtlasClient::clear));
		HudRenderCallback.EVENT.register((ctx, tick) -> compassBar(ctx));
	}

	static void clear() {
		for (Tile t : TILES.values()) {
			if (t.texture != null) {
				MinecraftClient.getInstance().getTextureManager().destroyTexture(t.id);
			}
		}
		TILES.clear();
		markers = List.of();
		shared = List.of();
		CHUNKS.clear();
		overlayRev = -1;
		contacts = List.of();
		route = null;
	}

	/** Dev screenshots: route to this marker. */
	public static void setRoute(AtlasData.Marker m) {
		route = m;
	}

	/** Radar contacts, or none if the last word from the radar is more than two seconds old. */
	static List<AtlasPayloads.Contact> liveContacts() {
		return System.currentTimeMillis() - contactsAt > 2000 ? List.of() : contacts;
	}

	static int contactColor(int kind) {
		return kind == 0 ? 0xFFFFFFFF : kind == 1 ? 0xFFFF4040 : 0xFF60E060;
	}

	/** A marker's name as shown: the GPS death marker in the player's language. */
	static String markerName(AtlasData.Marker m) {
		if (m.name().startsWith(Atlas.SHARED)) {
			String[] parts = m.name().substring(Atlas.SHARED.length()).split("\n", 2);
			String name = parts[0].equals(com.periodicelements.atlas.Satellites.DEATH)
					? Text.translatable("gui.periodic_elements.atlas.death").getString() : parts[0];
			return Text.translatable("gui.periodic_elements.atlas.shared", name, parts.length > 1 ? parts[1] : "").getString();
		}
		return m.name().equals(com.periodicelements.atlas.Satellites.DEATH)
				? Text.translatable("gui.periodic_elements.atlas.death").getString() : m.name();
	}

	static long countMarkers() {
		return Atlas.countMarkers(markers);
	}

	/** Whether the minimap may show: a handheld radar (Radar) or a GPS receiver (GPS) in the pack. */
	static boolean minimapAllowed(ClientPlayerEntity p, Set<String> known) {
		var inv = p.getInventory();
		boolean radar = false, gps = false;
		for (int i = 0; i < inv.size(); i++) {
			var st = inv.getStack(i);
			radar |= st.isOf(Atlas.HANDHELD_RADAR);
			gps |= st.isOf(Atlas.GPS_RECEIVER);
		}
		return (radar && Atlas.has(known, "minimap")) || (gps && Atlas.has(known, "gps"));
	}

	/** The isotope that gives most of a chunk's reading, or null. */
	static String dominant(java.util.Map<String, Float> iso) {
		String best = null;
		float bv = 0;
		for (var e : iso.entrySet()) {
			if (e.getValue() > bv) {
				bv = e.getValue();
				best = e.getKey();
			}
		}
		return best;
	}

	/** A colour per source element on the isotope layer. */
	static int isotopeColor(String element) {
		return switch (element) {
			case "uranium" -> 0x50D040;
			case "radium", "radon", "polonium" -> 0xC050E0;
			case "thorium", "actinium", "protactinium" -> 0xF09030;
			case "fission_products" -> 0xF03030;
			case "plutonium", "neptunium", "americium", "curium" -> 0x30B0F0;
			default -> 0xE0E0E0;
		};
	}

	/** "U-238 70% · Ra-226 30%": the spectrum of one reading, largest first. */
	static String spectrum(java.util.Map<String, Float> iso) {
		double sum = 0;
		for (float v : iso.values()) sum += v;
		if (sum <= 0) return "";
		StringBuilder sb = new StringBuilder();
		List<java.util.Map.Entry<String, Float>> list = new ArrayList<>(iso.entrySet());
		list.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
		for (int i = 0; i < Math.min(3, list.size()); i++) {
			if (i > 0) sb.append(" · ");
			sb.append(com.periodicelements.atlas.Prospecting.isotope(list.get(i).getKey())).append(' ')
					.append(Math.round(100 * list.get(i).getValue() / sum)).append('%');
		}
		return sb.toString();
	}

	static void request(List<Long> keys) {
		List<Integer> revs = new ArrayList<>();
		for (long k : keys) {
			Tile t = TILES.get(k);
			revs.add(t == null ? -1 : t.rev);
		}
		java.util.Set<Long> now = new java.util.HashSet<>(keys);
		if (!now.equals(lastKeys)) {
			overlayRev = -1; // other tiles in view: fetch their overlay
			lastKeys = now;
		}
		if (ClientPlayNetworking.canSend(AtlasPayloads.Request.ID)) {
			ClientPlayNetworking.send(new AtlasPayloads.Request(keys, revs, overlayRev));
		}
	}

	static int mode(Set<String> known, boolean rockLayer) {
		return (Atlas.has(known, "contours") ? PRECISE : 0) | (rockLayer && Atlas.has(known, "geology") ? ROCKS : 0);
	}

	/** Layers the Atlas shows, by research: terrain always, then rocks, ores, radiation and anomalies. */
	public enum Layer {
		TERRAIN(null), GEOLOGY("geology"), ORES("ores"), RADIATION("radiation"), ISOTOPES("isotopes"), ANOMALIES("anomalies");

		final String feature;

		Layer(String feature) {
			this.feature = feature;
		}

		boolean available(Set<String> known) {
			return feature == null || Atlas.has(known, feature);
		}

		Text label() {
			return Text.translatable("gui.periodic_elements.atlas.layer." + name().toLowerCase(java.util.Locale.ROOT));
		}
	}

	static AtlasPayloads.ChunkEntry chunkAt(int x, int z) {
		return CHUNKS.get(AtlasData.chunkKey(x >> 4, z >> 4));
	}

	/** A fixed colour per ore: the well-known ones by their mineral, the rest from the id. */
	static int oreColor(String id) {
		String p = id.substring(id.indexOf(':') + 1);
		if (p.contains("coal")) return 0x202020;
		if (p.contains("iron")) return 0xD8A070;
		if (p.contains("copper")) return 0xE07040;
		if (p.contains("gold")) return 0xF8D030;
		if (p.contains("redstone")) return 0xE02020;
		if (p.contains("lapis")) return 0x2040D0;
		if (p.contains("diamond")) return 0x50E8E0;
		if (p.contains("emerald")) return 0x20D050;
		if (p.contains("pitchblende")) return 0x60FF40;
		int h = p.hashCode();
		return 0x404040 | (h & 0xBFBFBF);
	}

	/** Green (background) through yellow to red (dangerous), on a log scale from 0.1 µSv/h to 10 mSv/h. */
	static int radColor(float rate) {
		double t = MathHelper.clamp((Math.log10(Math.max(rate, 1e-9)) + 7) / 5, 0, 1);
		int r = (int) (t < 0.5 ? 80 + 350 * t : 255);
		int g = (int) (t < 0.5 ? 200 : 200 - 360 * (t - 0.5));
		return (MathHelper.clamp(r, 0, 255) << 16) | (MathHelper.clamp(g, 0, 255) << 8) | 40;
	}

	/** The tile's texture, rebuilt when the tile or the drawing mode changed. */
	static Identifier texture(Tile t, int mode) {
		if (t.texture == null) {
			t.texture = new NativeImageBackedTexture(new NativeImage(AtlasTile.SIZE, AtlasTile.SIZE, true));
			t.id = Identifier.of(PeriodicElements.MOD_ID, "atlas/" + (t.tx & 0xFFFFFFFFL) + "_" + (t.tz & 0xFFFFFFFFL));
			MinecraftClient.getInstance().getTextureManager().registerTexture(t.id, t.texture);
		}
		if (t.builtRev != t.rev || t.builtMode != mode) {
			paint(t, mode, t.texture.getImage());
			t.texture.upload();
			t.builtRev = t.rev;
			t.builtMode = mode;
		}
		return t.id;
	}

	static void paint(Tile t, int mode, NativeImage img) {
		boolean precise = (mode & PRECISE) != 0, rocks = (mode & ROCKS) != 0;
		int n = AtlasTile.SIZE;
		for (int z = 0; z < n; z++) {
			for (int x = 0; x < n; x++) {
				// A sketch is drawn in 4-block strokes; a theodolite survey column by column.
				int sx = precise ? x : x & ~3, sz = precise ? z : z & ~3;
				int i = sz * n + sx;
				int c = t.color[i] & 0xFF;
				if (c == 0 || t.color[z * n + x] == 0) {
					img.setColor(x, z, 0);
					continue;
				}
				int rgb = MapColor.get(c).color;
				boolean water = c == MapColor.WATER_BLUE.id;
				if (rocks) {
					int r = t.rock[i] & 0xFF;
					rgb = r > 0 && r < Rocks.COLORS.length ? Rocks.COLORS[r] : (water ? rgb : mix(rgb, PAPER, 0.75));
				}
				double f = 1.0;
				if (precise && !water) {
					int h = t.height[i];
					int hn = z > 0 ? t.height[i - n] : h;
					int hw = x > 0 ? t.height[i - 1] : h;
					f = h > hn ? 1.12 : h < hn ? 0.84 : 1.0;
					if (Math.floorDiv(h, 8) != Math.floorDiv(hn, 8) || Math.floorDiv(h, 8) != Math.floorDiv(hw, 8)) {
						rgb = mix(rgb, 0x5A3C1E, 0.55);
					}
				} else if (!precise) {
					rgb = mix(rgb, PAPER, 0.35);
					// Ink outline where the land changes, so it reads as a drawing.
					int left = sx >= 4 ? t.color[sz * n + sx - 4] : t.color[i];
					int up = sz >= 4 ? t.color[(sz - 4) * n + sx] : t.color[i];
					if ((left != t.color[i] && x == sx) || (up != t.color[i] && z == sz)) {
						rgb = mix(rgb, 0x3A3026, 0.6);
					}
				}
				int r = MathHelper.clamp((int) (((rgb >> 16) & 0xFF) * f), 0, 255);
				int g = MathHelper.clamp((int) (((rgb >> 8) & 0xFF) * f), 0, 255);
				int b = MathHelper.clamp((int) ((rgb & 0xFF) * f), 0, 255);
				img.setColor(x, z, 0xFF000000 | (b << 16) | (g << 8) | r);
			}
		}
	}

	static int mix(int a, int b, double t) {
		int r = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
		int g = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
		int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
		return (r << 16) | (g << 8) | bl;
	}

	/** The column under a world position, or null if it was never drawn. */
	static Tile tileAt(int x, int z) {
		return TILES.get(AtlasTile.key(x >> AtlasTile.SHIFT, z >> AtlasTile.SHIFT));
	}

	/** "X 12", "X ?" ... depending on what the player has learned; null when nothing is known. */
	static String coords(Set<String> known, int x, Integer y, int z) {
		boolean lat = Atlas.has(known, "latitude"), lon = Atlas.has(known, "longitude");
		if (!lat && !lon) return null;
		StringBuilder sb = new StringBuilder();
		sb.append(lon ? Text.translatable("gui.periodic_elements.atlas.x", x).getString() : "X ?");
		if (lon && y != null) sb.append("  ").append(Text.translatable("gui.periodic_elements.atlas.y", y).getString());
		sb.append("  ").append(lat ? Text.translatable("gui.periodic_elements.atlas.z", z).getString() : "Z ?");
		return sb.toString();
	}

	static boolean holdsAtlas(ClientPlayerEntity p) {
		return p.getMainHandStack().isOf(Atlas.FIELD_ATLAS) || p.getOffHandStack().isOf(Atlas.FIELD_ATLAS);
	}

	/** The compass bar at the top of the screen while the Atlas is in hand (Magnetic Compass). */
	static void compassBar(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.currentScreen != null || mc.options.hudHidden || !holdsAtlas(p)) return;
		Set<String> known = ClientResearch.known();
		if (!Atlas.has(known, "compass")) return;
		int cx = ctx.getScaledWindowWidth() / 2, y = 4, half = 70;
		ctx.fill(cx - half - 2, y - 2, cx + half + 2, y + 11, 0x80000000);
		float yaw = p.getYaw();
		String[] dirs = {"s", "w", "n", "e"};
		for (int k = 0; k < 8; k++) {
			float rel = MathHelper.wrapDegrees(k * 45f - yaw);
			if (Math.abs(rel) > 90) continue;
			int x = cx + Math.round(rel / 90f * half);
			if (k % 2 == 0) {
				String d = Text.translatable("gui.periodic_elements.atlas.dir." + dirs[k / 2]).getString();
				ctx.drawText(mc.textRenderer, d, x - mc.textRenderer.getWidth(d) / 2, y, k == 4 ? 0xFFFF5555 : 0xFFFFFFFF, true);
			} else {
				ctx.fill(x, y + 3, x + 1, y + 6, 0xFFAAAAAA);
			}
		}
		if (route != null && Atlas.has(known, "gps")) {
			// The GPS route's bearing, as a yellow diamond on the bar.
			float bearing = (float) Math.toDegrees(Math.atan2(-(route.x() + 0.5 - p.getX()), route.z() + 0.5 - p.getZ()));
			float rel = MathHelper.wrapDegrees(bearing - yaw);
			if (Math.abs(rel) <= 90) {
				int x = cx + Math.round(rel / 90f * half);
				ctx.fill(x - 2, y + 2, x + 3, y + 7, 0xFF000000);
				ctx.fill(x - 1, y + 3, x + 2, y + 6, 0xFFFFD040);
			}
		}
		ctx.fill(cx, y - 2, cx + 1, y + 11, 0xFFFFD040);
		String c = coords(known, p.getBlockX(), p.getBlockY(), p.getBlockZ());
		if (c != null) {
			ctx.drawCenteredTextWithShadow(mc.textRenderer, c, cx, y + 14, 0xFFFFFFFF);
		}
	}
}
