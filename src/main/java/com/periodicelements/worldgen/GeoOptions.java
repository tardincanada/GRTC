package com.periodicelements.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * 3.49.0: the choices of the Geological world type ("Personalizar" on the world-creation screen), saved with each
 * world in {@value #FILE}. A world made before 3.49 has no file and gets {@link #LEGACY}, so its new chunks match the old.
 */
public record GeoOptions(BiomeSize biomes, Size provinces, Amount volcanoes, Amount ocean, Amount rivers, boolean legacy) {
	public static final String FILE = "elementar_geologico.json";

	/** Climate patch size: temperature and humidity spread over this many times vanilla's distances. */
	public enum BiomeSize {
		SMALL(0.6, 0.6), NORMAL(1, 1), LARGE(2, 2), HUGE(4, 4);

		public final double temperature, humidity;

		BiomeSize(double temperature, double humidity) {
			this.temperature = temperature;
			this.humidity = humidity;
		}
	}

	/** Province cells of 600, 1100 or 1800 blocks. */
	public enum Size {
		SMALL(600), NORMAL(1100), LARGE(1800);

		public final int cell;

		Size(int cell) {
			this.cell = cell;
		}
	}

	public enum Amount { FEW, NORMAL, MANY }

	public static final GeoOptions DEFAULT = new GeoOptions(BiomeSize.NORMAL, Size.NORMAL, Amount.NORMAL, Amount.NORMAL, Amount.NORMAL, false);
	/** Worlds from before 3.49: temperature at twice vanilla's scale, humidity at vanilla's, no variation inside a province. */
	public static final GeoOptions LEGACY = new GeoOptions(BiomeSize.LARGE, Size.NORMAL, Amount.NORMAL, Amount.NORMAL, Amount.NORMAL, true);

	public double temperatureSize() {
		return legacy ? 2 : biomes.temperature;
	}

	public double humiditySize() {
		return legacy ? 1 : biomes.humidity;
	}

	/** Chance that a volcano grid cell holds a cone (before checking that it lies in a volcanic setting). */
	public double coneChance() {
		return switch (volcanoes) {
			case FEW -> 0.18;
			case NORMAL -> 0.42;
			case MANY -> 0.75;
		};
	}

	/** Continent-noise level under which a cell is ocean. */
	public double oceanLevel() {
		return switch (ocean) {
			case FEW -> -0.32;
			case NORMAL -> -0.12;
			case MANY -> -0.02;
		};
	}

	/** Catchment (in 16x16 flow cells) at which a stream becomes a river. */
	public int riverArea() {
		return switch (rivers) {
			case FEW -> 340;
			case NORMAL -> 160;
			case MANY -> 80;
		};
	}

	public GeoOptions with(BiomeSize b, Size p, Amount v, Amount o, Amount r) {
		return new GeoOptions(b, p, v, o, r, false);
	}

	// ---- the current world ------------------------------------------------------------------------------

	private static volatile GeoOptions current = DEFAULT;
	/** Set by the world-creation screen, taken by the next new world the integrated server makes. */
	private static volatile GeoOptions pending;

	public static GeoOptions current() {
		return current;
	}

	public static void setCurrent(GeoOptions o) {
		current = o == null ? DEFAULT : o;
	}

	public static GeoOptions pending() {
		return pending == null ? DEFAULT : pending;
	}

	public static void setPending(GeoOptions o) {
		pending = o;
	}

	/** Loads a world's options; a new world takes the pending choices (or the defaults) and saves them. */
	public static GeoOptions loadFor(Path worldDir, boolean newWorld) {
		Path f = worldDir.resolve(FILE);
		GeoOptions o;
		if (Files.isRegularFile(f)) {
			try {
				o = fromJson(new Gson().fromJson(Files.readString(f), JsonObject.class));
			} catch (Exception e) {
				com.periodicelements.PeriodicElements.LOGGER.warn("Could not read {}: {}", f, e.toString());
				o = DEFAULT;
			}
			return o;
		}
		o = newWorld ? pending() : LEGACY;
		pending = null;
		try {
			Files.writeString(f, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(toJson(o)));
		} catch (Exception e) {
			com.periodicelements.PeriodicElements.LOGGER.warn("Could not write {}: {}", f, e.toString());
		}
		return o;
	}

	public static JsonObject toJson(GeoOptions o) {
		JsonObject j = new JsonObject();
		j.addProperty("biomes", o.biomes.name().toLowerCase(Locale.ROOT));
		j.addProperty("provinces", o.provinces.name().toLowerCase(Locale.ROOT));
		j.addProperty("volcanoes", o.volcanoes.name().toLowerCase(Locale.ROOT));
		j.addProperty("ocean", o.ocean.name().toLowerCase(Locale.ROOT));
		j.addProperty("rivers", o.rivers.name().toLowerCase(Locale.ROOT));
		if (o.legacy) j.addProperty("legacy", true);
		return j;
	}

	public static GeoOptions fromJson(JsonObject j) {
		return new GeoOptions(get(j, "biomes", BiomeSize.class, BiomeSize.NORMAL), get(j, "provinces", Size.class, Size.NORMAL),
				get(j, "volcanoes", Amount.class, Amount.NORMAL), get(j, "ocean", Amount.class, Amount.NORMAL),
				get(j, "rivers", Amount.class, Amount.NORMAL), j.has("legacy") && j.get("legacy").getAsBoolean());
	}

	private static <E extends Enum<E>> E get(JsonObject j, String key, Class<E> type, E fallback) {
		try {
			return Enum.valueOf(type, j.get(key).getAsString().toUpperCase(Locale.ROOT));
		} catch (Exception e) {
			return fallback;
		}
	}
}
