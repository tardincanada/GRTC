package com.periodicelements.client;

import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.atlas.AtlasPayloads;
import com.periodicelements.atlas.AtlasTile;
import com.periodicelements.research.ClientResearch;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The minimap (map plan, stage 3): the Atlas's own drawing in a square at the top left of the screen, north up, with
 * the radar's contacts, the markers and the GPS route. Shown while a handheld radar (Radar) or a GPS receiver (GPS)
 * is in the pack; the minimap key (N) hides and shows it.
 */
public final class Minimap {
	/** Default side; the player's own is ClientConfig.minimapSize (3.31.0). */
	public static final int SIZE = 96, MARGIN = 4;

	static int size() {
		return ClientConfig.get().minimapSize;
	}
	private static KeyBinding toggle;
	private static boolean hidden;
	private static int ticks;

	private Minimap() {
	}

	static void register() {
		toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.periodic_elements.minimap", InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N, "key.categories.periodic_elements"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggle.wasPressed()) hidden = !hidden;
			if (client.player == null || client.currentScreen != null || !visible(client)) return;
			if (++ticks % 40 == 1) request(client.player);
		});
		HudRenderCallback.EVENT.register((ctx, tick) -> render(ctx));
	}

	static boolean visible(MinecraftClient mc) {
		return !hidden && mc.player != null && !mc.options.hudHidden
				&& mc.world != null && mc.world.getRegistryKey() == net.minecraft.world.World.OVERWORLD
				&& AtlasClient.minimapAllowed(mc.player, ClientResearch.known());
	}

	/** Asks for the tiles under the minimap. */
	private static void request(ClientPlayerEntity p) {
		int r = size() / 2 + 8;
		List<Long> keys = new ArrayList<>();
		for (int tz = (p.getBlockZ() - r) >> AtlasTile.SHIFT; tz <= (p.getBlockZ() + r) >> AtlasTile.SHIFT; tz++) {
			for (int tx = (p.getBlockX() - r) >> AtlasTile.SHIFT; tx <= (p.getBlockX() + r) >> AtlasTile.SHIFT; tx++) {
				keys.add(AtlasTile.key(tx, tz));
			}
		}
		AtlasClient.request(keys);
	}

	static void render(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.currentScreen != null || !visible(mc)) return;
		ClientPlayerEntity p = mc.player;
		Set<String> known = ClientResearch.known();
		final int SIZE = size();
		ClientConfig.Corner corner = ClientConfig.get().minimapCorner;
		boolean right = corner == ClientConfig.Corner.TOP_RIGHT || corner == ClientConfig.Corner.BOTTOM_RIGHT;
		boolean bottom = corner == ClientConfig.Corner.BOTTOM_LEFT || corner == ClientConfig.Corner.BOTTOM_RIGHT;
		int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
		// Bottom corners leave room for the text lines (route, coordinates), which then go above the map.
		int x0 = right ? sw - SIZE - MARGIN : MARGIN, y0 = bottom ? sh - SIZE - MARGIN - 24 : MARGIN, half = SIZE / 2;
		double px = p.getX(), pz = p.getZ();
		ctx.fill(x0 - 2, y0 - 2, x0 + SIZE + 2, y0 + SIZE + 2, 0xFF2A251E);
		ctx.fill(x0 - 1, y0 - 1, x0 + SIZE + 1, y0 + SIZE + 1, 0xFF8A7A60);
		ctx.fill(x0, y0, x0 + SIZE, y0 + SIZE, 0xFF1E2228);
		ctx.enableScissor(x0, y0, x0 + SIZE, y0 + SIZE);
		int mode = AtlasClient.mode(known, false);
		for (AtlasClient.Tile t : AtlasClient.TILES.values()) {
			double sx = x0 + half + (t.tx * (double) AtlasTile.SIZE - px), sy = y0 + half + (t.tz * (double) AtlasTile.SIZE - pz);
			if (sx > x0 + SIZE || sy > y0 + SIZE || sx + AtlasTile.SIZE < x0 || sy + AtlasTile.SIZE < y0) continue;
			Identifier id = AtlasClient.texture(t, mode);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(sx, sy, 0);
			ctx.drawTexture(id, 0, 0, AtlasTile.SIZE, AtlasTile.SIZE, 0, 0, AtlasTile.SIZE, AtlasTile.SIZE, AtlasTile.SIZE, AtlasTile.SIZE);
			ctx.getMatrices().pop();
		}
		for (AtlasData.Marker m : AtlasClient.shared) dot(ctx, x0 + half + (m.x() + 0.5 - px), y0 + half + (m.z() + 0.5 - pz), 0xFF40A0FF);
		for (AtlasData.Marker m : AtlasClient.markers) {
			boolean death = m.name().equals(com.periodicelements.atlas.Satellites.DEATH);
			dot(ctx, x0 + half + (m.x() + 0.5 - px), y0 + half + (m.z() + 0.5 - pz), death ? 0xFFB0B0B0 : 0xFFE03C28);
		}
		for (AtlasPayloads.Contact c : AtlasClient.liveContacts()) {
			int cx = (int) (x0 + half + (c.x() + 0.5 - px)), cy = (int) (y0 + half + (c.z() + 0.5 - pz));
			ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFF000000);
			ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, AtlasClient.contactColor(c.kind()));
		}
		arrow(ctx, x0 + half, y0 + half, p.getYaw());
		ctx.disableScissor();
		AtlasData.Marker route = AtlasClient.route;
		int ty = bottom ? y0 - 24 : y0 + SIZE + 4;
		if (route != null && Atlas.has(known, "gps")) {
			// The route: an arrow on the frame pointing at the marker, and its name and distance.
			double dx = route.x() + 0.5 - px, dz = route.z() + 0.5 - pz;
			double d = Math.sqrt(dx * dx + dz * dz);
			if (d > half - 4) {
				int ex = x0 + half + (int) (dx / d * (half - 3)), ez = y0 + half + (int) (dz / d * (half - 3));
				ctx.fill(ex - 2, ez - 2, ex + 3, ez + 3, 0xFF000000);
				ctx.fill(ex - 1, ez - 1, ex + 2, ez + 2, 0xFFFFD040);
			} else {
				dot(ctx, x0 + half + dx, y0 + half + dz, 0xFFFFD040);
			}
			String s = Text.translatable("gui.periodic_elements.atlas.route", AtlasClient.markerName(route), (int) Math.round(d)).getString();
			ctx.drawTextWithShadow(mc.textRenderer, s, x0, ty, 0xFFFFE080);
			ty += 10;
		}
		String c = AtlasClient.coords(known, p.getBlockX(), p.getBlockY(), p.getBlockZ());
		if (c != null) ctx.drawTextWithShadow(mc.textRenderer, c, x0, ty, 0xFFFFFFFF);
	}

	private static void dot(DrawContext ctx, double x, double y, int color) {
		int ix = (int) x, iy = (int) y;
		ctx.fill(ix - 2, iy - 2, ix + 3, iy + 3, 0xFF000000);
		ctx.fill(ix - 1, iy - 1, ix + 2, iy + 2, color);
	}

	static void arrow(DrawContext ctx, double x, double y, float yaw) {
		ctx.getMatrices().push();
		ctx.getMatrices().translate(x, y, 0);
		ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw - 180));
		for (int row = 0; row < 7; row++) {
			int h = row / 2;
			ctx.fill(-h - 1, row - 4, h + 2, row - 3, 0xFF000000);
		}
		for (int row = 1; row < 6; row++) {
			int h = (row - 1) / 2;
			ctx.fill(-h, row - 4, h + 1, row - 3, row < 3 ? 0xFFFF4030 : 0xFFFFFFFF);
		}
		ctx.getMatrices().pop();
	}

	/** Dev screenshots: show the minimap whatever the key says. */
	public static void show() {
		hidden = false;
	}

	static int clamp(int v) {
		return MathHelper.clamp(v, 0, size());
	}
}
