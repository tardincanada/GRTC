package com.periodicelements.space;

import com.periodicelements.PeriodicElements;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * The three propellant pairs of level 8 (3.61.0). Rocket arithmetic counts one item of propellant as one tonne; a tank
 * holds the pair in its real mixture ratio by mass, so a tank of hydrogen weighs far less than one of kerosene.
 */
public enum Propellant {
	/** Kerosene and liquid oxygen, mixture 2.5:1 by mass, Isp about 300 s (R-7, Saturn V first stage, Falcon 9). */
	KEROLOX("kerolox_engine", "kerosene", "liquid_oxygen", 16, 40, 300, 6, 3.0),
	/** Liquid hydrogen and liquid oxygen, mixture 6:1, Isp about 430 s (RL-10, J-2); light, so a tank holds 20 t. */
	HYDROLOX("hydrolox_engine", "liquid_hydrogen", "liquid_oxygen", 3, 17, 430, 6, 3.0),
	/** Hydrazine and dinitrogen tetroxide, mixture about 1.1:1, Isp about 310 s; small engines for upper stages. */
	HYPERGOLIC("hypergolic_engine", "hydrazine", "dinitrogen_tetroxide", 28, 32, 310, 3, 1.5),
	/**
	 * 3.66.0: liquid methane and liquid oxygen, mixture 3.6:1, Isp about 360 s (SpaceX Raptor, 2019). Methane keeps far
	 * better than hydrogen on a long trip, and both halves can be made on Mars.
	 */
	METHALOX("methalox_engine", "liquid_methane", "liquid_oxygen", 10, 36, 360, 6, 3.0),
	/**
	 * 3.67.0, FICTION: metallic hydrogen that stays metal at 1 atm. Turning back into gas gives some 200 times the
	 * energy of burning hydrogen (Isp up to 1700 s in theory); liquid hydrogen dilutes it so the chamber survives.
	 */
	METALLIC("metallic_hydrogen_engine", "metallic_hydrogen", "liquid_hydrogen", 10, 4, 1100, 6, 3.0),
	/** 3.67.0, FICTION: antihydrogen from a Penning trap annihilates in a stream of hydrogen and heats it. */
	ANTIMATTER("antimatter_engine", "antihydrogen", "liquid_hydrogen", 1, 20, 2500, 6, 4.0),
	/**
	 * 3.67.0, FICTION: a compact tokamak fusing deuterium with helium-3 throws its exhaust out at a few percent of the
	 * speed of light (Daedalus, 1978). It pushes far too gently to lift a rocket off the ground.
	 */
	FUSION("fusion_drive", "helium_3", "deuterium", 2, 2, 10000, 3, 6.0, false);

	public static final double G0 = 9.80665;
	/** Dry mass of a tank, of the payload fairing and of a payload, in tonnes. */
	public static final double TANK_DRY = 2.0, FAIRING_DRY = 1.0, PAYLOAD = 1.0;
	/** Landing Legs add this much to the engine. */
	public static final double LEGS_DRY = 1.0;
	/** A reusable first stage keeps this share of its propellant to fly back and land. */
	public static final double LANDING_RESERVE = 0.15;
	/** Low orbit needs about 7.8 km/s plus 1.6 km/s of gravity and drag losses on the way up. */
	public static final double ORBIT_DV = 9400.0;
	/**
	 * 3.64.0: from the ground to the Moon's surface. Real missions need about 15 km/s (orbit 9.4, the push towards the
	 * Moon 3.1, braking into lunar orbit 0.9, landing 1.9); the game asks 13, so that three stages can do it.
	 */
	public static final double MOON_DV = 13000.0;
	/** 3.64.0: from the Moon's surface home: climb out (1.9) and the push back (0.9); the air brakes the rest. */
	public static final double RETURN_DV = 2800.0;
	/**
	 * 3.66.0: from the ground to the surface of Mars. Orbit 9.4, the push towards Mars 3.6, and about 1 more to land
	 * once the thin air has braked the capsule.
	 */
	public static final double MARS_DV = 14000.0;
	/** 3.66.0: from Mars home: the climb to orbit (4.1, a deeper well than the Moon's) and the push back (2.3). */
	public static final double MARS_RETURN_DV = 6400.0;
	/**
	 * 3.68.0 (fiction): from the ground to Mercury's surface. Orbit 9.4, the fall towards the Sun 5.5, the braking to
	 * match Mercury's speed and the landing with no air to help, about 7: no chemical rocket can carry a crew there.
	 */
	public static final double MERCURY_DV = 22000.0;
	/** 3.68.0: from Mercury home: the climb (3), and climbing back out from the Sun's deep well (6). */
	public static final double MERCURY_RETURN_DV = 9000.0;
	/** 3.68.0: from the ground to the cloud deck of Venus: orbit 9.4, the transfer 3.5, braking in the air and a balloon. */
	public static final double VENUS_DV = 18000.0;
	/** 3.68.0: from the cloud deck home: up to orbit from 50 km (7.5, Venus is almost the Earth's size) and the push back. */
	public static final double VENUS_RETURN_DV = 10000.0;
	/** 3.64.0: a crew capsule weighs this much instead of a satellite's tonne. */
	public static final double CAPSULE = 3.0;

	public final String engine;
	public final String fuel;
	public final String oxidizer;
	public final int fuelCapacity;
	public final int oxidizerCapacity;
	public final double isp;
	public final int maxTanks;
	public final double engineDry;
	/** 3.67.0: false for an engine too weak to lift a rocket off the ground; it may only fly an upper stage. */
	public final boolean liftoff;

	Propellant(String engine, String fuel, String oxidizer, int fuelCapacity, int oxidizerCapacity, double isp,
			   int maxTanks, double engineDry) {
		this(engine, fuel, oxidizer, fuelCapacity, oxidizerCapacity, isp, maxTanks, engineDry, true);
	}

	Propellant(String engine, String fuel, String oxidizer, int fuelCapacity, int oxidizerCapacity, double isp,
			   int maxTanks, double engineDry, boolean liftoff) {
		this.liftoff = liftoff;
		this.engine = engine;
		this.fuel = fuel;
		this.oxidizer = oxidizer;
		this.fuelCapacity = fuelCapacity;
		this.oxidizerCapacity = oxidizerCapacity;
		this.isp = isp;
		this.maxTanks = maxTanks;
		this.engineDry = engineDry;
	}

	public Item fuelItem() {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, fuel));
	}

	public Item oxidizerItem() {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, oxidizer));
	}

	/** Exhaust velocity, m/s. */
	public double exhaustVelocity() {
		return isp * G0;
	}

	public static Propellant byName(String name) {
		for (Propellant p : values()) {
			if (p.name().equals(name)) {
				return p;
			}
		}
		return null;
	}
}
