package com.periodicelements.client;

import com.periodicelements.atlas.Atlas;
import com.periodicelements.atlas.AtlasData;
import com.periodicelements.atlas.AtlasPayloads;
import com.periodicelements.atlas.AtlasTile;
import com.periodicelements.atlas.Rocks;
import com.periodicelements.research.ClientResearch;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Field Atlas, full screen. What it shows grows with research: a rough sketch and a guessed position at first;
 * north and your exact place (compass); Z and a scale (quadrant); X and Y (chronometer); sharp drawing with relief and
 * contours (theodolite); a rock layer (geological map).
 */
public class AtlasScreen extends Screen {
	private static final float[] ZOOMS = {0.5f, 1, 2, 4, 8};
	private static final int BAR = 26;
	private static AtlasClient.Layer layer = AtlasClient.Layer.TERRAIN;
	private static int zoomIndex = 1;

	private double cx, cz;
	private boolean dragging;
	private int ticks;
	private TextFieldWidget nameField;
	private ButtonWidget layerButton;
	private int pendingX, pendingZ;

	/** Dev screenshots: which layer and zoom the next screen opens with. */
	public static void preset(AtlasClient.Layer show, int zoom) {
		layer = show;
		zoomIndex = MathHelper.clamp(zoom, 0, ZOOMS.length - 1);
	}

	public AtlasScreen() {
		super(Text.translatable("gui.periodic_elements.atlas.title"));
	}

	private Set<String> known() {
		return ClientResearch.known();
	}

	private float zoom() {
		return ZOOMS[zoomIndex];
	}

	@Override
	protected void init() {
		ClientPlayerEntity p = client.player;
		if (p != null && cx == 0 && cz == 0) {
			cx = p.getX();
			cz = p.getZ();
		}
		nameField = new TextFieldWidget(textRenderer, width / 2 - 80, height / 2 - 10, 160, 20,
				Text.translatable("gui.periodic_elements.atlas.marker.name"));
		nameField.setMaxLength(32);
		nameField.setVisible(false);
		addDrawableChild(nameField);
		if (!layer.available(known())) layer = AtlasClient.Layer.TERRAIN;
		if (AtlasClient.Layer.GEOLOGY.available(known())) {
			layerButton = addDrawableChild(ButtonWidget.builder(layer.label(), b -> {
				AtlasClient.Layer[] all = AtlasClient.Layer.values();
				do {
					layer = all[(layer.ordinal() + 1) % all.length];
				} while (!layer.available(known()));
				b.setMessage(layer.label());
			}).dimensions(width - 94, 4, 90, 18).build());
		}
		requestVisible();
	}


	@Override
	public void tick() {
		if (++ticks % 20 == 0) {
			requestVisible();
		}
	}

	private int mapBottom() {
		return height - BAR;
	}

	private void requestVisible() {
		float z = zoom();
		int x0 = MathHelper.floor((cx - width / 2.0 / z)) >> AtlasTile.SHIFT;
		int x1 = MathHelper.floor((cx + width / 2.0 / z)) >> AtlasTile.SHIFT;
		int z0 = MathHelper.floor((cz - mapBottom() / 2.0 / z)) >> AtlasTile.SHIFT;
		int z1 = MathHelper.floor((cz + mapBottom() / 2.0 / z)) >> AtlasTile.SHIFT;
		List<Long> keys = new ArrayList<>();
		for (int tz = z0; tz <= z1; tz++) {
			for (int tx = x0; tx <= x1 && keys.size() < 96; tx++) {
				keys.add(AtlasTile.key(tx, tz));
			}
		}
		AtlasClient.request(keys);
	}

	private double screenX(double wx) {
		return width / 2.0 + (wx - cx) * zoom();
	}

	private double screenY(double wz) {
		return mapBottom() / 2.0 + (wz - cz) * zoom();
	}

	private double worldX(double sx) {
		return cx + (sx - width / 2.0) / zoom();
	}

	private double worldZ(double sy) {
		return cz + (sy - mapBottom() / 2.0) / zoom();
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		ctx.fill(0, 0, width, height, 0xFF2A251E);
		Set<String> known = known();
		int mode = AtlasClient.mode(known, layer == AtlasClient.Layer.GEOLOGY);
		ctx.enableScissor(0, 0, width, mapBottom());
		ctx.fill(0, 0, width, mapBottom(), (mode & AtlasClient.PRECISE) != 0 ? 0xFF1E2228 : 0xFF6E6250);
		float z = zoom();
		boolean any = false;
		for (AtlasClient.Tile t : AtlasClient.TILES.values()) {
			double sx = screenX((double) t.tx * AtlasTile.SIZE), sy = screenY((double) t.tz * AtlasTile.SIZE);
			if (sx > width || sy > mapBottom() || sx + AtlasTile.SIZE * z < 0 || sy + AtlasTile.SIZE * z < 0) continue;
			Identifier id = AtlasClient.texture(t, mode);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(sx, sy, 0);
			ctx.getMatrices().scale(z, z, 1);
			ctx.drawTexture(id, 0, 0, AtlasTile.SIZE, AtlasTile.SIZE, 0, 0, AtlasTile.SIZE, AtlasTile.SIZE, AtlasTile.SIZE, AtlasTile.SIZE);
			ctx.getMatrices().pop();
			any = true;
		}
		drawOverlay(ctx);
		drawRoute(ctx);
		drawMarkers(ctx, mouseX, mouseY);
		if (Atlas.has(known, "radar")) drawContacts(ctx);
		drawPlayer(ctx, known);
		ctx.disableScissor();
		if (!any) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.periodic_elements.atlas.empty"), width / 2, mapBottom() / 2 + 20, 0xFFE0D8C0);
		}
		drawBar(ctx, known, mouseX, mouseY);
	}

	/** The chunk layers: ores as coloured dots, radiation and anomalies as tints. */
	private void drawOverlay(DrawContext ctx) {
		if (layer != AtlasClient.Layer.ORES && layer != AtlasClient.Layer.RADIATION && layer != AtlasClient.Layer.ANOMALIES
				&& layer != AtlasClient.Layer.ISOTOPES) return;
		float z = zoom();
		for (AtlasPayloads.ChunkEntry e : AtlasClient.CHUNKS.values()) {
			int cx = (int) (e.key() >> 32), cz = (int) e.key();
			int x0 = (int) Math.floor(screenX(cx * 16.0)), y0 = (int) Math.floor(screenY(cz * 16.0));
			int x1 = (int) Math.floor(screenX(cx * 16.0 + 16)), y1 = (int) Math.floor(screenY(cz * 16.0 + 16));
			if (x1 < 0 || y1 < 0 || x0 > width || y0 > mapBottom()) continue;
			if (layer == AtlasClient.Layer.ISOTOPES && !e.isotopes().isEmpty()) {
				// The colour of the main source, stronger the higher the reading above the background.
				String main = AtlasClient.dominant(e.isotopes());
				double t = MathHelper.clamp((Math.log10(Math.max(e.rad(), 1e-9)) + 7) / 4, 0.15, 1);
				ctx.fill(x0, y0, x1, y1, ((int) (60 + 170 * t) << 24) | AtlasClient.isotopeColor(main));
			} else if (layer == AtlasClient.Layer.RADIATION && e.rad() >= 0) {
				ctx.fill(x0, y0, x1, y1, 0x70000000 | AtlasClient.radColor(e.rad()));
			} else if (layer == AtlasClient.Layer.ANOMALIES && e.magnetic() >= 0) {
				// Only ground above the survey's own background is tinted (128 is the mean).
				// Surveyed ground is greyed a little so the coloured anomalies stand out.
				ctx.fill(x0, y0, x1, y1, 0x60303030);
				int am = Math.min(230, Math.max(0, e.magnetic() - 128) * 2), ad = Math.min(230, Math.max(0, e.dense() - 128) * 2);
				if (am > 0) ctx.fill(x0, y0, x1, y1, (am << 24) | 0x2050FF);
				if (ad > 0) ctx.fill(x0, y0, x1, y1, (ad << 24) | 0xFF7010);
			} else if (layer == AtlasClient.Layer.ORES && (!e.ores().isEmpty() || e.cored())) {
				ctx.fill(x0, y0, x1, y1, 0x50000000);
				int dot = Math.max(2, Math.round(3 * z));
				int i = 0;
				for (String o : e.ores()) {
					if (i >= 4) break;
					int dx = x0 + 1 + (i % 2) * (dot + 1), dy = y0 + 1 + (i / 2) * (dot + 1);
					ctx.fill(dx, dy, dx + dot, dy + dot, 0xFF000000 | AtlasClient.oreColor(o.split("\\*")[0]));
					i++;
				}
				if (e.cored()) {
					int mx = (x0 + x1) / 2, my = (y0 + y1) / 2;
					ctx.fill(mx - 1, my - 1, mx + 2, my + 2, 0xFFFFFFFF);
					ctx.fill(mx, my, mx + 1, my + 1, 0xFF000000);
				}
			}
		}
	}

	/** What the radar sees now: players white, hostile creatures red, the rest green. */
	private void drawContacts(DrawContext ctx) {
		int s = Math.max(1, Math.round(zoom()));
		for (AtlasPayloads.Contact c : AtlasClient.liveContacts()) {
			int x = (int) screenX(c.x() + 0.5), y = (int) screenY(c.z() + 0.5);
			ctx.fill(x - s - 1, y - s - 1, x + s + 1, y + s + 1, 0xFF000000);
			ctx.fill(x - s, y - s, x + s, y + s, AtlasClient.contactColor(c.kind()));
		}
	}

	/** The GPS route: a dotted line from you to the marker. */
	private void drawRoute(DrawContext ctx) {
		AtlasData.Marker r = AtlasClient.route;
		if (r == null || client.player == null || !Atlas.has(known(), "gps")) return;
		double ax = screenX(client.player.getX()), ay = screenY(client.player.getZ());
		double bx = screenX(r.x() + 0.5), by = screenY(r.z() + 0.5);
		double len = Math.hypot(bx - ax, by - ay);
		for (double d = 0; d < len; d += 6) {
			int x = (int) (ax + (bx - ax) * d / len), y = (int) (ay + (by - ay) * d / len);
			ctx.fill(x - 1, y - 1, x + 2, y + 2, 0xFF000000);
			ctx.fill(x, y, x + 1, y + 1, 0xFFFFD040);
		}
	}

	private void drawMarkers(DrawContext ctx, int mouseX, int mouseY) {
		for (AtlasData.Marker m : AtlasClient.shared) {
			int x = (int) screenX(m.x() + 0.5), y = (int) screenY(m.z() + 0.5);
			ctx.fill(x - 2, y - 2, x + 3, y + 3, 0xFF000000);
			ctx.fill(x - 1, y - 1, x + 2, y + 2, 0xFF40A0FF);
			boolean hover = Math.abs(mouseX - x) < 6 && Math.abs(mouseY - y) < 6;
			if (!m.name().isEmpty() && (zoom() >= 2 || hover)) {
				ctx.drawCenteredTextWithShadow(textRenderer, AtlasClient.markerName(m), x, y - 11, 0xFFB0D8FF);
			}
		}
		for (AtlasData.Marker m : AtlasClient.markers) {
			int x = (int) screenX(m.x() + 0.5), y = (int) screenY(m.z() + 0.5);
			boolean death = m.name().equals(com.periodicelements.atlas.Satellites.DEATH);
			boolean routed = m.equals(AtlasClient.route);
			ctx.fill(x - 2, y - 1, x + 3, y + 2, 0xFF000000);
			ctx.fill(x - 1, y - 2, x + 2, y + 3, 0xFF000000);
			if (death) {
				// A grey cross where you died.
				ctx.fill(x - 3, y, x + 4, y + 1, 0xFF000000);
				ctx.fill(x, y - 3, x + 1, y + 4, 0xFF000000);
				ctx.fill(x - 2, y, x + 3, y + 1, 0xFFD0D0D0);
				ctx.fill(x, y - 2, x + 1, y + 3, 0xFFD0D0D0);
			} else {
				ctx.fill(x - 1, y - 1, x + 2, y + 2, routed ? 0xFFFFD040 : 0xFFE03C28);
			}
			boolean hover = Math.abs(mouseX - x) < 6 && Math.abs(mouseY - y) < 6;
			if (!m.name().isEmpty() && (zoom() >= 2 || hover || death)) {
				ctx.drawCenteredTextWithShadow(textRenderer, AtlasClient.markerName(m), x, y - 11, death ? 0xFFE0E0E0 : 0xFFFFF0D0);
			}
		}
	}

	private void drawPlayer(DrawContext ctx, Set<String> known) {
		ClientPlayerEntity p = client.player;
		if (p == null) return;
		if (Atlas.has(known, "compass")) {
			double x = screenX(p.getX()), y = screenY(p.getZ());
			ctx.getMatrices().push();
			ctx.getMatrices().translate(x, y, 0);
			ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.getYaw() - 180));
			for (int row = 0; row < 7; row++) {
				int half = row / 2;
				ctx.fill(-half - 1, row - 4, half + 2, row - 3, 0xFF000000);
			}
			for (int row = 1; row < 6; row++) {
				int half = (row - 1) / 2;
				ctx.fill(-half, row - 4, half + 1, row - 3, row < 3 ? 0xFFFF4030 : 0xFFFFFFFF);
			}
			ctx.getMatrices().pop();
		} else {
			// Without a compass you only know roughly where you are: a wandering ring about 24 blocks across.
			long slot = client.world == null ? 0 : client.world.getTime() / 600;
			double ox = (MathHelper.hashCode((int) slot, 1, 7) % 9) - 4, oz = (MathHelper.hashCode((int) slot, 2, 11) % 9) - 4;
			double x = screenX(p.getX() + ox * 2), y = screenY(p.getZ() + oz * 2);
			double r = Math.max(6, 12 * zoom());
			for (int a = 0; a < 48; a++) {
				double t = a * Math.PI / 24;
				int px = (int) (x + Math.cos(t) * r), py = (int) (y + Math.sin(t) * r);
				ctx.fill(px, py, px + 1, py + 1, 0xFF3A1E10);
			}
			ctx.drawCenteredTextWithShadow(textRenderer, "?", (int) x, (int) y - 4, 0xFFFFE0A0);
		}
	}

	private void drawBar(DrawContext ctx, Set<String> known, int mouseX, int mouseY) {
		int y = mapBottom();
		ctx.fill(0, y, width, height, 0xFF1A1612);
		ctx.fill(0, y, width, y + 1, 0xFF8A7A60);
		ClientPlayerEntity p = client.player;
		int ty = y + 4;
		String mine = p == null ? null : AtlasClient.coords(known, p.getBlockX(), p.getBlockY(), p.getBlockZ());
		int left = 6;
		if (Atlas.has(known, "latitude")) {
			left = scaleBar(ctx, left, ty);
		}
		if (mine != null) {
			ctx.drawText(textRenderer, mine, left, ty, 0xFFFFE6B0, false);
		}
		String status;
		if (mouseY < y) {
			int wx = MathHelper.floor(worldX(mouseX)), wz = MathHelper.floor(worldZ(mouseY));
			AtlasClient.Tile t = AtlasClient.tileAt(wx, wz);
			int i = AtlasTile.index(wx, wz);
			Integer wy = t != null && t.color[i] != 0 ? (int) t.height[i] : null;
			String c = AtlasClient.coords(known, wx, wy, wz);
			status = c == null ? "" : c;
			String extra = null;
			AtlasPayloads.ChunkEntry ce = AtlasClient.chunkAt(wx, wz);
			if (layer == AtlasClient.Layer.GEOLOGY && t != null && t.color[i] != 0) {
				extra = Text.translatable(Rocks.key(t.rock[i] & 0xFF)).getString();
			} else if (layer == AtlasClient.Layer.ORES) {
				if (ce == null || ce.ores().isEmpty()) {
					extra = Text.translatable("gui.periodic_elements.atlas.ores.none").getString();
				} else {
					StringBuilder sb = new StringBuilder();
					for (String o : ce.ores()) {
						String[] parts = o.split("\\*");
						if (sb.length() > 0) sb.append(", ");
						sb.append(net.minecraft.registry.Registries.BLOCK.get(Identifier.of(parts[0])).getName().getString())
								.append(" ×").append(parts.length > 1 ? parts[1] : "?");
					}
					extra = sb.toString();
				}
			} else if (layer == AtlasClient.Layer.ISOTOPES) {
				extra = ce == null || ce.isotopes().isEmpty() ? Text.translatable("gui.periodic_elements.atlas.rad.unknown").getString()
						: com.periodicelements.radiation.Radioactivity.formatRate(ce.rad()) + "  " + AtlasClient.spectrum(ce.isotopes());
			} else if (layer == AtlasClient.Layer.RADIATION) {
				extra = ce == null || ce.rad() < 0 ? Text.translatable("gui.periodic_elements.atlas.rad.unknown").getString()
						: com.periodicelements.radiation.Radioactivity.formatRate(ce.rad());
			} else if (layer == AtlasClient.Layer.ANOMALIES && ce != null && ce.magnetic() >= 0) {
				extra = Text.translatable("gui.periodic_elements.atlas.anomaly",
						com.periodicelements.atlas.Prospecting.strength(ce.magnetic()),
						com.periodicelements.atlas.Prospecting.strength(ce.dense())).getString();
			}
			if (extra != null) status += (status.isEmpty() ? "" : "  ") + extra;
		} else {
			status = Text.translatable(Atlas.has(known, "gps") ? "gui.periodic_elements.atlas.help.gps"
					: "gui.periodic_elements.atlas.help").getString();
		}
		String marks = Text.translatable("gui.periodic_elements.atlas.markers", AtlasClient.countMarkers(), AtlasClient.markerLimit).getString();
		ctx.drawText(textRenderer, marks, width - textRenderer.getWidth(marks) - 6, ty, 0xFFB0A080, false);
		ctx.drawText(textRenderer, status, width / 2 - textRenderer.getWidth(status) / 2, ty + 11, 0xFFE0E0E0, false);
	}

	/** A scale bar about 60 px long in a round number of blocks; returns where text may start after it. */
	private int scaleBar(DrawContext ctx, int x, int y) {
		int blocks = 1;
		while (blocks * zoom() < 40) blocks *= 2;
		int w = Math.round(blocks * zoom());
		ctx.fill(x, y + 3, x + w, y + 5, 0xFFFFFFFF);
		ctx.fill(x, y, x + 1, y + 8, 0xFFFFFFFF);
		ctx.fill(x + w - 1, y, x + w, y + 8, 0xFFFFFFFF);
		String label = Text.translatable("gui.periodic_elements.atlas.scale", blocks).getString();
		ctx.drawText(textRenderer, label, x + w + 4, y, 0xFFFFFFFF, false);
		return x + w + 12 + textRenderer.getWidth(label);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (nameField.isVisible()) {
			closeField();
			return true;
		}
		if (mouseY >= mapBottom()) return false;
		if (button == 2 && Atlas.has(known(), "gps")) {
			// GPS: middle-click a marker to route to it, again to stop.
			for (AtlasData.Marker m : allMarkers()) {
				if (Math.abs(screenX(m.x() + 0.5) - mouseX) < 6 && Math.abs(screenY(m.z() + 0.5) - mouseY) < 6) {
					AtlasClient.route = m.equals(AtlasClient.route) ? null : m;
					return true;
				}
			}
			return true;
		}
		if (button == 0) {
			dragging = true;
			return true;
		}
		if (button == 1) {
			for (AtlasData.Marker m : AtlasClient.markers) {
				if (Math.abs(screenX(m.x() + 0.5) - mouseX) < 6 && Math.abs(screenY(m.z() + 0.5) - mouseY) < 6) {
					ClientPlayNetworking.send(new AtlasPayloads.EditMarker(false, "", m.x(), m.z()));
					return true;
				}
			}
			if (AtlasClient.countMarkers() >= AtlasClient.markerLimit) {
				if (client.player != null) client.player.sendMessage(Text.translatable("gui.periodic_elements.atlas.marker.full"), true);
				return true;
			}
			pendingX = MathHelper.floor(worldX(mouseX));
			pendingZ = MathHelper.floor(worldZ(mouseY));
			nameField.setText("");
			nameField.setVisible(true);
			setFocused(nameField);
			nameField.setFocused(true);
			return true;
		}
		return false;
	}

	private List<AtlasData.Marker> allMarkers() {
		List<AtlasData.Marker> all = new ArrayList<>(AtlasClient.markers);
		all.addAll(AtlasClient.shared);
		return all;
	}

	private void closeField() {
		nameField.setVisible(false);
		nameField.setFocused(false);
		setFocused(null);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		if (dragging && button == 0) {
			cx -= dx / zoom();
			cz -= dy / zoom();
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
		double wx = worldX(mouseX), wz = worldZ(mouseY);
		int old = zoomIndex;
		zoomIndex = MathHelper.clamp(zoomIndex + (v > 0 ? 1 : v < 0 ? -1 : 0), 0, ZOOMS.length - 1);
		if (old != zoomIndex) {
			// Keep the point under the cursor where it is.
			cx = wx - (mouseX - width / 2.0) / zoom();
			cz = wz - (mouseY - mapBottom() / 2.0) / zoom();
			requestVisible();
		}
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (nameField.isVisible()) {
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				ClientPlayNetworking.send(new AtlasPayloads.EditMarker(true, nameField.getText(), pendingX, pendingZ));
				closeField();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				closeField();
				return true;
			}
			return nameField.keyPressed(keyCode, scanCode, modifiers) || true;
		}
		if (client != null && client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if (nameField.isVisible()) {
			renderBackground(ctx, mouseX, mouseY, delta);
			ctx.fill(nameField.getX() - 6, nameField.getY() - 16, nameField.getX() + nameField.getWidth() + 6,
					nameField.getY() + nameField.getHeight() + 6, 0xE0000000);
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.periodic_elements.atlas.marker.name"),
					width / 2, nameField.getY() - 12, 0xFFFFFFFF);
			for (var d : List.of(nameField)) d.render(ctx, mouseX, mouseY, delta);
			if (layerButton != null) layerButton.render(ctx, mouseX, mouseY, delta);
			return;
		}
		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
