package com.periodicelements.machine;

import com.periodicelements.PeriodicElements;
import com.periodicelements.research.Research;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

/**
 * Machine upgrades: items for a machine's two upgrade slots. Each works only once its owner has the research
 * that unlocks "upgrade:&lt;id&gt;"; two of the same upgrade count once.
 */
public final class Upgrades {
	/** Fills Steel Cylinders with the gases the machine makes. */
	public static final String COMPRESSOR = "compressor";
	/** Fuel-fired machines lose half as much heat (Siemens regenerator). */
	public static final String REGENERATOR = "regenerator";
	/** Fuel-fired machines can run {@link #LINING_BONUS_C} hotter than their own limit. */
	public static final String REFRACTORY_LINING = "refractory_lining";
	/** Captures the gases the machine vents into flasks or cylinders. */
	public static final String GAS_RECOVERY_HOOD = "gas_recovery_hood";
	/** Works through the charge {@link #STIRRER_SPEED} times as fast, at extra fuel or power. */
	public static final String MECHANICAL_STIRRER = "mechanical_stirrer";
	/** Reactions over a solid catalyst run {@link #CATALYST_SPEED} times as fast. */
	public static final String CATALYST_BED = "catalyst_bed";
	/** Moves finished products into a chest or other container next to the machine. */
	public static final String OUTPUT_PUSHER = "output_pusher";

	/** A still, kettle or crystallizer boils {@link #VACUUM_C} cooler (1.8.0). */
	public static final String VACUUM_PUMP = "vacuum_pump";
	/** An Output Pusher on a Buna rubber belt: moves {@link #BELT_ITEMS} items at a time (1.9.0). */
	public static final String CONVEYOR_BELT = "conveyor_belt";
	public static final int BELT_ITEMS = 4;
	/** A machine that runs below {@link #FOAM_MAX_C} loses {@link #FOAM_LOSS} of its heat loss with Foam Lagging (2.0.0). */
	public static final String FOAM_LAGGING = "foam_lagging";
	public static final double FOAM_LOSS = 0.3;
	public static final double FOAM_MAX_C = 300;
	/** Fermenters make {@link #FILTER_BONUS} times the product; the Deep-Tank Fermenter cannot run without one (2.0.0). */
	public static final String STERILE_AIR_FILTER = "sterile_air_filter";
	public static final double FILTER_BONUS = 1.1;
	public static final Set<String> FERMENTERS = Set.of("fermentation_barrel", "vinegar_vat", "deep_tank_fermenter", "bioreactor");

	/**
	 * 3.46.0: a vanilla ghast tear (read as the ruthenium-titanium oxide coating of a DSA anode) in an electric
	 * machine's upgrade slot: electrolysis that gives off chlorine takes {@link #ANODE_ENERGY} of its usual energy.
	 * Needs no research.
	 */
	public static final String GHAST_TEAR_ANODE = "ghast_tear_anode";
	public static final double ANODE_ENERGY = 0.75;

	public static final List<String> ALL = List.of(COMPRESSOR, REGENERATOR, REFRACTORY_LINING, GAS_RECOVERY_HOOD,
			MECHANICAL_STIRRER, CATALYST_BED, OUTPUT_PUSHER, VACUUM_PUMP, CONVEYOR_BELT, FOAM_LAGGING, STERILE_AIR_FILTER);
	public static final double VACUUM_C = 100;
	/** Machines a Vacuum Pump works on: those that boil or evaporate. */
	public static final Set<String> VACUUM_MACHINES = Set.of("alembic", "tar_still", "soap_kettle", "crystallizer",
			"refinery_still", "salt_pan");

	public static final double REGENERATOR_LOSS = 0.5;
	public static final double LINING_BONUS_C = 150;
	public static final double STIRRER_SPEED = 1.5;
	/** Share of a fuel's burn time the stirrer's engine takes, and of an electric machine's draw its motor takes. */
	public static final double STIRRER_COST = 0.25;
	public static final double CATALYST_SPEED = 2.0;
	/** Solid catalysts that a Catalyst Bed holds (reactions.csv "catalyst" column). */
	public static final Set<String> SOLID_CATALYSTS = Set.of("iron", "nickel", "platinum", "platinum-rhodium",
			"vanadium pentoxide", "cobalt-manganese bromide");

	private Upgrades() {
	}

	/** The upgrade id of this item, or null if it is not an upgrade. */
	public static String of(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		Identifier id = Registries.ITEM.getId(stack.getItem());
		if (id.getNamespace().equals("minecraft") && id.getPath().equals("ghast_tear")) {
			return GHAST_TEAR_ANODE;
		}
		return id.getNamespace().equals(PeriodicElements.MOD_ID) && ALL.contains(id.getPath()) ? id.getPath() : null;
	}

	public static boolean isUpgrade(ItemStack stack) {
		return of(stack) != null;
	}

	public static boolean known(Set<String> known, String upgrade) {
		return Research.allows(known, "upgrade:" + upgrade);
	}

	/** Does a Ghast Tear anode save energy on this reaction (electrolysis that makes chlorine)? */
	public static boolean anodeHelps(Reactions.Reaction r) {
		for (Reactions.Product p : r.products()) {
			if (p.id().equals("chlorine")) {
				return r.energy() > 0;
			}
		}
		return false;
	}

	public static boolean catalytic(Reactions.Reaction r) {
		return SOLID_CATALYSTS.contains(r.catalyst());
	}
}
