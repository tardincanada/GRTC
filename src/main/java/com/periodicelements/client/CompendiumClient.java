package com.periodicelements.client;

import com.periodicelements.compendium.Compendium;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.compendium.CompendiumSyncPayload;
import com.periodicelements.compendium.TreeCompendiumItem;
import com.periodicelements.research.ClientResearch;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The client's copy of the local player's compendium notes, the corner panel, and the book screen hook. */
public final class CompendiumClient {
	private static volatile Map<String, Long> notes = Map.of();
	/** The Geological formation underfoot, sent by the server while the Mineral Compendium and Hutton allow it. */
	private static volatile String formation = "";

	private CompendiumClient() {
	}

	public static Map<String, Long> notes() {
		return notes;
	}

	public static boolean knows(String key) {
		return notes.containsKey(key);
	}

	public static void register() {
		CompendiumData.load();
		ClientPlayNetworking.registerGlobalReceiver(CompendiumSyncPayload.ID, (p, ctx) -> {
			Map<String, Long> m = new LinkedHashMap<>();
			for (int i = 0; i < p.keys().size(); i++) {
				m.put(p.keys().get(i), i < p.positions().size() ? p.positions().get(i) : 0L);
			}
			notes = Collections.unmodifiableMap(m);
		});
		ClientPlayNetworking.registerGlobalReceiver(com.periodicelements.compendium.FormationPayload.ID,
				(p, ctx) -> formation = p.formation());
		ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> {
			notes = Map.of();
			formation = "";
		});
		HudRenderCallback.EVENT.register(CompendiumClient::hud);
		TreeCompendiumItem.open = kind -> MinecraftClient.getInstance().setScreen(new CompendiumScreen(kind));
	}

	/** Lines of the corner panel: the biome, its climate (Geography of Plants), the tree or plant under the crosshair. */
	public static List<Text> panel(MinecraftClient mc, float delta) {
		List<Text> lines = new ArrayList<>();
		Set<String> known = ClientResearch.known();
		String biome = Compendium.biomeAt(mc.player);
		if (biome != null) {
			lines.add(Text.translatable(Compendium.biomeKey(biome)).formatted(Formatting.WHITE));
			CompendiumData.Biome b = CompendiumData.BIOME_BY_ID.get(biome);
			if (b != null && b.temperature() != null && Compendium.has(known, "climate")) {
				lines.add(Text.translatable("gui.periodic_elements.compendium.hud_climate", b.temperature(), b.rain())
						.formatted(Formatting.GRAY));
			}
		}
		if (!formation.isEmpty() && Compendium.carries(mc.player, Compendium.MINERAL_COMPENDIUM)) {
			lines.add(Text.translatable("gui.periodic_elements.compendium.formation",
					Text.translatable("compendium.periodic_elements.formation." + formation)).formatted(Formatting.GOLD));
		}
		Entity cam = mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity();
		HitResult hit = cam.raycast(Compendium.REACH, delta, false);
		CompendiumData.Tree seen = null;
		if (Compendium.carries(mc.player, Compendium.ANIMAL_COMPENDIUM)) {
			Entity e = Compendium.animalLookedAt(mc.player, delta);
			if (e != null) seen = CompendiumData.animalOf(e.getType());
		}
		if (seen != null || hit.getType() == HitResult.Type.BLOCK) {
			CompendiumData.Tree t = seen != null ? seen
					: Compendium.entryOf(mc.player, mc.world.getBlockState(((BlockHitResult) hit).getBlockPos()).getBlock());
			if (t != null) {
				if (knows(t.key())) {
					MutableText name = Text.translatable(t.nameKey()).formatted(Formatting.GREEN);
					if (t.real() && !CompendiumData.MINERAL.equals(t.kind()) && Compendium.has(known, "classification")) {
						name.append(Text.literal(" (" + t.scientific() + ")").formatted(Formatting.GRAY, Formatting.ITALIC));
					}
					lines.add(name);
				} else {
					lines.add(Text.translatable(CompendiumData.ANIMAL.equals(t.kind()) ? "gui.periodic_elements.compendium.unknown_animal"
							: CompendiumData.PLANT.equals(t.kind()) ? "gui.periodic_elements.compendium.unknown_plant"
							: CompendiumData.MINERAL.equals(t.kind()) ? "gui.periodic_elements.compendium.unknown_rock"
							: "gui.periodic_elements.compendium.unknown_tree").formatted(Formatting.GRAY));
				}
			}
		}
		return lines;
	}

	static void hud(DrawContext ctx, RenderTickCounter tick) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null || mc.options.hudHidden || mc.currentScreen != null
				|| mc.getDebugHud().shouldShowDebugHud() || !Compendium.carries(mc.player) || !ClientConfig.get().compendiumHud) {
			return;
		}
		List<Text> lines = panel(mc, tick.getTickDelta(false));
		if (lines.isEmpty()) return;
		int w = 0;
		for (Text t : lines) w = Math.max(w, mc.textRenderer.getWidth(t));
		// Top-left: the top-right corner belongs to status effect icons and toasts (recipes, advancements), which would cover it.
		// Below the Magnifying Lens panel when that is showing too.
		int x = 4, y = com.periodicelements.PeriodicElementsClient.lensBottom > 0 ? com.periodicelements.PeriodicElementsClient.lensBottom + 6 : 4;
		ctx.fill(x - 2, y - 2, x + w + 4, y + lines.size() * 10, 0xA0101014);
		for (int i = 0; i < lines.size(); i++) {
			ctx.drawTextWithShadow(mc.textRenderer, lines.get(i), x + 1, y + i * 10, 0xFFFFFF);
		}
	}
}
