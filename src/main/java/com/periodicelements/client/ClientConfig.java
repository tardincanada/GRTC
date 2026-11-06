package com.periodicelements.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.periodicelements.PeriodicElements;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The player's own settings (3.31.0), in config/periodic_elements.json. Client side only: how the HUDs look and
 * what tooltips show; nothing here changes the game's rules. Edited in game from Mod Menu or /periodicelements config.
 */
public final class ClientConfig {
	public enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

	public static final int MINIMAP_MIN = 64, MINIMAP_MAX = 160, LENS_MIN = 3, LENS_MAX = 12;

	/** Side of the minimap in GUI pixels. */
	public int minimapSize = 96;
	public Corner minimapCorner = Corner.TOP_LEFT;
	/** Elements the Magnifying Lens panel names before summing the rest. */
	public int lensLines = 8;
	/** The Tree/Plant/Mineral Compendium's corner panel (biome, climate, what is under the crosshair). */
	public boolean compendiumHud = true;
	/** Block and item compositions in item tooltips. */
	public boolean compositionTooltip = true;
	/** "Radioactive" lines in item tooltips. */
	public boolean radiationTooltip = true;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ClientConfig current;

	public static ClientConfig get() {
		if (current == null) {
			current = load();
		}
		return current;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve(PeriodicElements.MOD_ID + ".json");
	}

	private static ClientConfig load() {
		Path f = file();
		if (Files.isRegularFile(f)) {
			try {
				ClientConfig c = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), ClientConfig.class);
				if (c != null) {
					c.clamp();
					return c;
				}
			} catch (IOException | RuntimeException e) {
				PeriodicElements.LOGGER.warn("Could not read {}, using defaults: {}", f, e.toString());
			}
		}
		ClientConfig c = new ClientConfig();
		c.save();
		return c;
	}

	private void clamp() {
		minimapSize = Math.max(MINIMAP_MIN, Math.min(MINIMAP_MAX, minimapSize));
		lensLines = Math.max(LENS_MIN, Math.min(LENS_MAX, lensLines));
		if (minimapCorner == null) {
			minimapCorner = Corner.TOP_LEFT;
		}
	}

	public void save() {
		clamp();
		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			PeriodicElements.LOGGER.warn("Could not save {}: {}", file(), e.toString());
		}
	}
}
