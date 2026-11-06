package com.periodicelements.machine;

import com.periodicelements.BlockComposition;
import com.periodicelements.ModContent;
import com.periodicelements.radiation.Isotopes;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A fuel-heated machine that runs the reactions in data/reactions.csv on the composition of what you put in.
 *
 * <p>One input item is broken into its composition (in units, from data/compositions.csv) and held in the
 * machine's charge. Each tick the machine works through up to {@code 1000 / throughput} units, running every
 * reaction whose feed is in the charge, whose temperature range the machine is in, and that the owner has
 * researched. Products that a later reaction uses stay in the charge, so roasting feeds reduction. What no
 * reaction here can use comes out: products as items (one per 111.1 units), everything else as slag or ash.
 * Gases are vented. Mass balance is exact apart from the vented gases and the air a roast consumes.
 *
 * <p>Heat: burning fuel gives {@code firePower} kJ per tick until its energy is spent. The machine loses
 * {@code loss * (T - 20)} kJ per tick, reactions take (or give off) their energy, and the remainder changes
 * the temperature through the machine's heat capacity. A fuel can't heat the machine past its flame
 * temperature (plus 250 °C with Bellows), nor past the machine's own limit.
 */
public class MachineBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
	public static final int INPUT = 0;
	public static final int FUEL = 1;
	public static final int PRODUCT_FIRST = 2;
	public static final int PRODUCT_LAST = 4;
	public static final int RESIDUE = 5;
	/** Second ingredient for reactions that need one (tin for bronze, water for oil of vitriol). */
	public static final int REAGENT = 6;
	/** Empty Glass Flasks for the liquids and gases the machine makes. */
	public static final int FLASKS = 7;
	/** Empty Steel Cylinders, filled with compressed gas when a Compressor is fitted. */
	public static final int CYLINDERS = 8;
	public static final int UPGRADE_FIRST = 9;
	public static final int UPGRADE_LAST = 10;
	public static final int SIZE = 11;
	/** Stop working once this many items' worth of an output is waiting for space. */
	private static final double BACKLOG_ITEMS = 2;
	private static final double COKE_FLAME_C = 1600, STACK_COKE_BONUS_C = 100;
	/** A structure loads at most this many items a tick. */
	private static final int MAX_LOADS = 16;
	/** Output-buffer prefix for gas a Gas Recovery Hood caught. */
	private static final String CAUGHT = "~";

	public enum Status { IDLE, WORKING, HEATING, TOO_HOT, NEEDS_CARBON, NEEDS_RESEARCH, OUTPUT_FULL, NO_FUEL, NEEDS_REAGENT, NO_POWER, NEEDS_FLASKS, NEEDS_CYLINDERS, NEEDS_FILTER }

	// Property indices shared with the screen handler.
	public static final int P_TEMP = 0, P_CAP = 1, P_BURN = 2, P_BURN_TOTAL = 3, P_PROGRESS = 4, P_STATUS = 5,
			P_STATUS_REACTION = 6, P_ACTIVE = 7, P_MACHINE = 8, P_CARBON = 9, P_FLAME = 10, P_LIMIT = 11,
			// 3.3.0 structures: kind (0 none, 1 + Kind ordinal), size, speed x100, heat lost per item in %, problem, wanted block, layer.
			P_S_KIND = 12, P_S_A = 13, P_S_H = 14, P_S_SPEED = 15, P_S_LOSS = 16, P_S_PROBLEM = 17, P_S_BLOCK = 18, P_S_LAYER = 19,
			// 3.8.0: second width, and the column's cut (x100) or 100 when a lead chamber has its tower.
			P_S_B = 20, P_S_EXTRA = 21,
			// 3.29.0: relief valve open (1) or shut (0).
			P_VENT = 22,
			P_COUNT = 23;

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
	private final Reactions.MachineSpec spec;
	private final List<Reactions.Reaction> reactions;

	@Nullable
	private UUID owner;
	private double temperature = Reactions.AMBIENT_C;
	private int burnLeft;
	private int burnTotal;
	private double flame = Reactions.AMBIENT_C;
	private double carbon;
	private final Map<String, Double> charge = new LinkedHashMap<>();
	private double chargeTotal;
	private final Map<String, Double> outputs = new LinkedHashMap<>();
	/** Isotope ratio (atom %) of each tracked element in the charge; missing means natural. */
	private final Map<String, Double> ratios = new LinkedHashMap<>();
	/** Ids that came in as a pure item (an ingot, a reagent): left over, they come back out as that item, not slag. */
	private final Set<String> pure = new java.util.HashSet<>();
	private Status status = Status.IDLE;
	private int statusReaction = -1;
	private int activeReaction = -1;
	/** Ticks the block keeps showing "active" after the last reacting tick, so a stop-start charge doesn't flicker. */
	private int activeHold;
	/** 3.33.0: whether this controller last switched its structure's animated blocks on. */
	private boolean partsOn;
	static final int ACTIVE_HOLD_TICKS = 20;
	private double cap = Reactions.AMBIENT_C;
	/** Electric machines: kJ drawn from the piles last tick. */
	private double lastDraw;
	/**
	 * 3.29.0 relief valve: when open, a gas with no empty flask or cylinder, or no room in the output slots, is
	 * vented instead of stopping the machine. Liquids still wait for flasks.
	 */
	private boolean vent;
	/** Set by emitOutputs when a liquid or gas is waiting for an empty container. */
	private Status containerShort;
	/** Upgrades fitted whose research the owner has (refreshed each tick). */
	private final Set<String> active = new java.util.HashSet<>();
	/** Fraction of a tick of fuel the Mechanical Stirrer's engine has used. */
	private double stirBurn;
	private int pushTimer;
	/** 3.3.0: the multiblock this machine controls (measured once a second), or null when it stands alone. */
	@Nullable
	private com.periodicelements.multiblock.Structures.Result structure;
	@Nullable
	private final com.periodicelements.multiblock.Structures.Kind structureKind;
	private int structureTimer;
	/** 3.38.0: the tanks this machine's pipes reach (looked up once a second). */
	private List<com.periodicelements.fluid.FluidHolder> pipes = List.of();
	private int pipeTimer;

	private final PropertyDelegate properties = new PropertyDelegate() {
		@Override
		public int get(int index) {
			return switch (index) {
				case P_TEMP -> (int) Math.round(temperature);
				case P_CAP -> (int) Math.round(cap);
				case P_BURN -> burnLeft;
				case P_BURN_TOTAL -> burnTotal;
				case P_PROGRESS -> chargeTotal <= 0 ? 0 : (int) Math.round(1000 * (1 - remaining() / chargeTotal));
				case P_STATUS -> status.ordinal();
				case P_STATUS_REACTION -> statusReaction;
				case P_ACTIVE -> activeReaction;
				case P_MACHINE -> spec.index();
				case P_CARBON -> (int) Math.round(carbon * 10);
				case P_FLAME -> (int) Math.round(flame);
				case P_LIMIT -> (int) Math.round(maxC());
				case P_S_KIND -> structure == null ? 0 : 1 + structure.kind().ordinal();
				case P_S_A -> structure == null ? 0 : structure.a();
				case P_S_H -> structure == null ? 0 : structure.h();
				case P_S_SPEED -> structure == null ? 100 : (int) Math.round(structure.speed() * 100);
				case P_S_LOSS -> structure == null ? 100 : (int) Math.round(structure.lossPerItem() * 100);
				case P_S_PROBLEM -> structure == null ? 0 : structure.problem().ordinal();
				case P_S_BLOCK -> structure == null || structure.wanted() == null ? -1 : Registries.BLOCK.getRawId(structure.wanted());
				case P_S_LAYER -> structure == null ? 0 : structure.layer();
				case P_S_B -> structure == null ? 0 : structure.b();
				case P_S_EXTRA -> structure == null ? 0 : (int) Math.round(structure.extra() * 100);
				case P_VENT -> vent ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int size() {
			return P_COUNT;
		}
	};

	public MachineBlockEntity(BlockPos pos, BlockState state) {
		super(ModContent.MACHINE_ENTITY, pos, state);
		this.spec = Reactions.machine(((MachineBlock) state.getBlock()).machineId());
		this.reactions = Reactions.forMachine(spec.id());
		this.structureKind = com.periodicelements.multiblock.Structures.forMachine(spec.id());
	}

	/** The structure this machine controls, valid or not; null when it stands alone. */
	@Nullable
	public com.periodicelements.multiblock.Structures.Result structure() {
		return structure;
	}

	private boolean inStructure() {
		return structure != null && structure.valid();
	}

	/** Work per tick and fire power grow with a valid structure's speed. */
	public double sizeSpeed() {
		return inStructure() ? structure.speed() : 1;
	}

	private double sizeLoss() {
		return inStructure() ? structure.loss() : 1;
	}

	private double firePower() {
		return spec.firePower() * sizeSpeed();
	}

	/** The shell's mass grows with its surface, like the heat it loses. */
	private double heatCapacity() {
		return spec.heatCapacity() * sizeLoss();
	}

	/** 3.8.0: a Distillation Column sends part of the bitumen up as naphtha and kerosene. */
	private double cut(Reactions.Reaction r, Reactions.Product p) {
		if (inStructure() && structure.kind() == com.periodicelements.multiblock.Structures.Kind.ENRICHMENT_CASCADE) {
			// 3.13.0: more stages strip the tails further, so more of the feed comes out enriched.
			return com.periodicelements.multiblock.Structures.cascadeUnits(r.id(), p.id(), p.units(), structure.a());
		}
		if (!inStructure() || structure.kind() != com.periodicelements.multiblock.Structures.Kind.DISTILLATION_COLUMN) {
			return p.units();
		}
		double bitumen = 0;
		for (Reactions.Product q : r.products()) {
			if (q.id().equals("bitumen")) {
				bitumen = q.units();
			}
		}
		return com.periodicelements.multiblock.Structures.cutUnits(r.id(), p.id(), p.units(), structure.extra(), bitumen);
	}

	/** A Coke Oven Battery of 4 or more ovens burns its own gas: no fuel needed. */
	private boolean selfHeated() {
		return inStructure() && structure.selfHeated();
	}

	/** Measures the structure round the controller once a second; ports are bound to it while it is valid. */
	private void measure(ServerWorld world, BlockPos pos, BlockState state) {
		if (structureKind == null || (structureTimer++ % 20 != 0)) {
			return;
		}
		var now = com.periodicelements.multiblock.Structures.detect(structureKind, world, pos, state.get(MachineBlock.FACING));
		com.periodicelements.multiblock.Structures.bind(world, pos, structure, now);
		structure = now;
		// 3.33.0: the structure's blocks animate while it works.
		boolean partsWanted = now != null && now.valid() && state.get(MachineBlock.ACTIVE);
		if (partsWanted || partsOn) {
			com.periodicelements.multiblock.Structures.animate(world, pos, structureKind, partsWanted);
			partsOn = partsWanted;
		}
		if (now != null && now.missing() != null) {
			// Holding the Field Guide shows where the missing block goes.
			for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
				if (player.squaredDistanceTo(pos.toCenterPos()) < 32 * 32
						&& (player.getMainHandStack().getItem() instanceof com.periodicelements.FieldGuideItem
						|| player.getOffHandStack().getItem() instanceof com.periodicelements.FieldGuideItem)) {
					var c = now.missing().toCenterPos();
					world.spawnParticles(player, new net.minecraft.particle.DustParticleEffect(new org.joml.Vector3f(1f, 0.1f, 0.1f), 3f),
							true, c.x, c.y, c.z, 24, 0.35, 0.35, 0.35, 0);
				}
			}
		}
	}

	@Override
	public void markRemoved() {
		super.markRemoved();
		if (world != null && structure != null) {
			com.periodicelements.multiblock.Structures.bind(world, pos, structure, null);
		}
	}

	/** 3.29.0: is the relief valve open (gases with nowhere to go are vented)? */
	public boolean venting() {
		return vent;
	}

	/** 3.29.0: opens or shuts the relief valve (the machine screen's valve button). */
	public void setVenting(boolean open) {
		vent = open;
		markDirty();
	}

	/** One of the screen's P_ properties, read on the server (the Jade plugin, 3.26.0). */
	public int property(int index) {
		return properties.get(index);
	}

	public Reactions.MachineSpec spec() {
		return spec;
	}

	public void setOwner(@Nullable UUID owner) {
		this.owner = owner;
		markDirty();
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	public double temperature() {
		return temperature;
	}

	/** The hottest the fire can make the machine right now (flame, Bellows, Steam Engine, machine limit). */
	public double cap() {
		return cap;
	}

	public Status status() {
		return status;
	}

	private double remaining() {
		double sum = 0;
		for (double v : charge.values()) {
			sum += v;
		}
		return sum;
	}

	private Set<String> ownerKnows(ServerWorld world) {
		return owner == null ? Set.of() : ResearchState.get(world.getServer()).known(owner);
	}

	private static boolean allowed(Reactions.Reaction r, Set<String> known) {
		return r.project() == null || known.contains(r.project());
	}

	/** Could some reaction in this machine use this item? With {@code known} null, research is not checked. */
	public static boolean canProcess(List<Reactions.Reaction> reactions, ItemStack stack, @Nullable Set<String> known) {
		BlockComposition.Entry entry = BlockComposition.of(Registries.ITEM.getId(stack.getItem()).toString());
		if (entry == null) {
			return false;
		}
		for (BlockComposition.Part part : entry.parts()) {
			for (Reactions.Reaction r : reactions) {
				if (r.feed().equals(part.id()) && (known == null || allowed(r, known))
						&& !r.id().equals("wood_drying")) {
					return true;
				}
			}
		}
		return false;
	}

	private static BlockComposition.Entry composition(ItemStack stack) {
		return stack.isEmpty() ? null : BlockComposition.of(Registries.ITEM.getId(stack.getItem()).toString());
	}

	private static boolean contains(@Nullable BlockComposition.Entry entry, String id) {
		if (entry != null) {
			for (BlockComposition.Part part : entry.parts()) {
				if (part.id().equals(id) && part.units() > 1e-9) {
					return true;
				}
			}
		}
		return false;
	}

	/** Can this reagent come from the charge, the item being loaded, or the reagent slot? */
	private boolean reagentAvailable(Reactions.Reagent g, @Nullable BlockComposition.Entry loading) {
		return charge.getOrDefault(g.id(), 0.0) > 1e-6 || contains(loading, g.id()) || contains(composition(items.get(REAGENT)), g.id())
				|| pipesHold(g.id());
	}

	/** 3.38.0: does a tank this machine's pipes reach hold a flask's worth of something with this in it? */
	private boolean pipesHold(String id) {
		for (com.periodicelements.fluid.FluidHolder h : pipes) {
			if (h.gone() || h.fluid() == null) {
				continue;
			}
			BlockComposition.Entry e = composition(h.sample());
			if (contains(e, id) && h.units() >= e.amount() - 1e-6) {
				return true;
			}
		}
		return false;
	}

	private boolean reagentsAvailable(Reactions.Reaction r, @Nullable BlockComposition.Entry loading) {
		for (Reactions.Reagent g : r.reagents()) {
			if (!reagentAvailable(g, loading)) {
				return false;
			}
		}
		return true;
	}

	/** Does feed of this ratio meet the reaction's minimum enrichment (if it has one)? */
	private static boolean enoughEnriched(Reactions.Reaction r, double ratio) {
		Double min = r.minRatio().get(r.feed());
		return min == null || (!Double.isNaN(ratio) && ratio >= min - 1e-9);
	}

	private double ratioInCharge(String key) {
		Isotopes.Tracked t = Isotopes.tracked(Isotopes.elementOf(key));
		return t == null ? Double.NaN : ratios.getOrDefault(key, t.natural());
	}

	/** Mixes {@code units} of ratio {@code ratio} into what the charge already holds under {@code key}. */
	private void mixRatio(String key, double units, double ratio) {
		Isotopes.Tracked t = Isotopes.tracked(Isotopes.elementOf(key));
		if (t == null || units <= 0) {
			return;
		}
		double old = Math.max(0, charge.getOrDefault(key, 0.0));
		double prev = ratios.getOrDefault(key, t.natural());
		ratios.put(key, (prev * old + ratio * units) / (old + units));
	}

	/** Some allowed reaction can use this item, with every reagent it needs at hand. */
	private boolean canLoad(ItemStack stack, Set<String> known) {
		BlockComposition.Entry entry = composition(stack);
		if (entry == null) {
			return false;
		}
		for (BlockComposition.Part part : entry.parts()) {
			for (Reactions.Reaction r : reactions) {
				if (r.feed().equals(part.id()) && allowed(r, known) && !r.id().equals("wood_drying")
						&& reagentsAvailable(r, entry) && enoughEnriched(r, Isotopes.ratio(stack, Isotopes.elementOf(part.id())))) {
					return true;
				}
			}
		}
		return false;
	}

	/** Is a reaction with its feed in the charge short of a reagent the reagent slot can give? */
	private boolean wantsReagent(Set<String> known) {
		return wantsReagent(known, items.get(REAGENT));
	}

	private boolean wantsReagent(Set<String> known, ItemStack offered) {
		BlockComposition.Entry slot = composition(offered);
		if (slot == null) {
			return false;
		}
		double budget = 1000.0 / spec.throughput() * sizeSpeed();
		for (Reactions.Reaction r : reactions) {
			double feed = charge.getOrDefault(r.feed(), 0.0);
			if (feed <= 1e-6 || !allowed(r, known)) {
				continue;
			}
			for (Reactions.Reagent g : r.reagents()) {
				if (charge.getOrDefault(g.id(), 0.0) < Math.min(feed, budget) * g.units() / 100 - 1e-9 && contains(slot, g.id())) {
					return true;
				}
			}
		}
		return false;
	}

	/** The charge is empty, or holds only reagents left over for the next item (tin waiting for more copper). */
	private boolean onlyReagentsLeft() {
		for (String id : charge.keySet()) {
			boolean reagent = false;
			for (Reactions.Reaction r : reactions) {
				for (Reactions.Reagent g : r.reagents()) {
					reagent |= g.id().equals(id);
				}
			}
			if (!reagent) {
				return false;
			}
		}
		return true;
	}

	/** Breaks one item into the charge; a bucket or bottle it came in comes back out. */
	private void load(ServerWorld world, BlockPos pos, ItemStack stack) {
		loadParts(stack, composition(stack));
		useOne(world, pos, stack);
		markDirty();
	}

	/** Puts one item's composition into the charge (its container is the caller's business). */
	private void loadParts(ItemStack stack, BlockComposition.Entry entry) {
		for (BlockComposition.Part part : entry.parts()) {
			String el = Isotopes.elementOf(part.id());
			if (el != null) {
				mixRatio(part.id(), part.units(), Isotopes.ratio(stack, el));
			}
			charge.merge(part.id(), part.units(), Double::sum);
		}
		if (entry.parts().size() == 1) {
			pure.add(entry.parts().get(0).id());
		}
		chargeTotal += entry.amount();
	}

	/** The tanks this machine's pipes reach (3.38.0). */
	public List<com.periodicelements.fluid.FluidHolder> pipes() {
		return pipes;
	}

	private void refreshPipes() {
		if (pipeTimer-- <= 0 && world != null) {
			pipeTimer = 20;
			pipes = com.periodicelements.fluid.FluidNet.reach(world, pos, Direction.values());
		}
	}

	/**
	 * 3.38.0: draws one flask's worth of a liquid or gas from a tank its pipes reach, straight into the charge (no
	 * flask). {@code reagent}: only what a reaction in the charge is short of. Returns whether it drew anything.
	 */
	private boolean pipeLoad(Set<String> known, boolean reagent) {
		for (com.periodicelements.fluid.FluidHolder h : pipes) {
			if (h.gone() || h.fluid() == null) {
				continue;
			}
			ItemStack s = h.sample();
			BlockComposition.Entry entry = composition(s);
			if (entry == null || !(reagent ? wantsReagent(known, s) : canLoad(s, known))) {
				continue;
			}
			if (h.drain(entry.amount(), true) < entry.amount() - 1e-6) {
				continue;
			}
			h.drain(entry.amount(), false);
			if (!reagent && charge.isEmpty()) {
				chargeTotal = 0;
				pure.clear();
			}
			loadParts(s, entry);
			markDirty();
			return true;
		}
		return false;
	}

	/**
	 * Uses one item of a stack; its container (bucket, flask, cylinder) comes back: an empty flask or cylinder
	 * to its slot when there is room, otherwise to the product slots, otherwise onto the ground.
	 */
	private void useOne(@Nullable ServerWorld world, BlockPos pos, ItemStack stack) {
		Item rem = stack.getItem().getRecipeRemainder();
		stack.decrement(1);
		if (rem == null) {
			return;
		}
		ItemStack back = new ItemStack(rem);
		int slot = back.isOf(com.periodicelements.Fluids.GLASS_FLASK) ? FLASKS : back.isOf(com.periodicelements.Fluids.STEEL_CYLINDER) ? CYLINDERS : -1;
		if (slot >= 0) {
			ItemStack held = items.get(slot);
			if (held.isEmpty()) {
				items.set(slot, back);
				return;
			}
			if (held.isOf(rem) && held.getCount() < held.getMaxCount()) {
				held.increment(1);
				return;
			}
		}
		if (!insertOutput(back, false) && world != null) {
			net.minecraft.util.ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, back);
		}
	}

	/** Is a Compressor fitted, and does the owner know how to use it? */
	public boolean compressing() {
		// The Air Compressor (2.1.0) fills cylinders without the upgrade: compressing is what it is for.
		return active.contains(Upgrades.COMPRESSOR) || spec.id().equals("air_compressor");
	}

	public boolean upgraded(String upgrade) {
		return active.contains(upgrade);
	}

	/** The hottest the machine itself can stand: its limit, plus a Refractory Lining's bonus when fuel-fired. */
	public double maxC() {
		return spec.maxC() + (!spec.electric() && upgraded(Upgrades.REFRACTORY_LINING) ? Upgrades.LINING_BONUS_C : 0)
				// A blast furnace stack fired with coke runs 100 °C hotter than the charcoal furnace alone.
				+ (inStructure() && structure.kind() == com.periodicelements.multiblock.Structures.Kind.BLAST_FURNACE
				&& flame >= COKE_FLAME_C ? STACK_COKE_BONUS_C : 0);
	}

	private void refreshUpgrades(Set<String> known) {
		active.clear();
		for (int i = UPGRADE_FIRST; i <= UPGRADE_LAST; i++) {
			String u = Upgrades.of(items.get(i));
			if (u != null && owner != null && Upgrades.known(known, u)) {
				active.add(u);
			}
		}
	}

	/** Is there anything in the charge (a Steam Engine next to it only runs while there is)? */
	public boolean busy() {
		return !charge.isEmpty();
	}

	private List<PowerBlockEntity> neighbours(ServerWorld world, BlockPos pos, String kind) {
		List<PowerBlockEntity> found = new java.util.ArrayList<>();
		for (Direction d : Direction.values()) {
			if (world.getBlockEntity(pos.offset(d)) instanceof PowerBlockEntity p && p.spec().kind().equals(kind)) {
				found.add(p);
			}
		}
		return found;
	}

	/**
	 * Piles and dynamos that can power this machine: those next to it, and those next to any Copper Wire joined
	 * to it (through up to {@link Reactions#wireReach()} wire blocks).
	 */
	public static List<PowerBlockEntity> powerSources(ServerWorld world, BlockPos pos) {
		List<PowerBlockEntity> found = new java.util.ArrayList<>();
		Set<BlockPos> seen = new java.util.HashSet<>();
		java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
		seen.add(pos);
		queue.add(pos);
		double wires = 0;
		while (!queue.isEmpty()) {
			BlockPos at = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos n = at.offset(d);
				if (!seen.add(n)) {
					continue;
				}
				if (world.getBlockEntity(n) instanceof PowerBlockEntity p && p.givesCurrent()) {
					found.add(p);
				} else if (world.getBlockState(n).isOf(ModContent.COPPER_WIRE) && wires < Reactions.wireReach()) {
					wires++;
					queue.add(n);
				} else if (world.getBlockState(n).isOf(ModContent.INSULATED_CABLE) && wires < Reactions.wireReach()) {
					wires += Reactions.cableCost();
					queue.add(n);
				}
			}
		}
		return found;
	}

	/** Breathing machines (the Air Liquefier) draw a block of air when they are empty and powered. */
	private void breathe(ServerWorld world, BlockPos pos, Set<String> known) {
		if (!spec.breathes() || !charge.isEmpty() || !items.get(INPUT).isEmpty()
				|| (spec.project() != null && !known.contains(spec.project()))) {
			return;
		}
		double power = 0;
		for (PowerBlockEntity p : powerSources(world, pos)) {
			power += p.available();
		}
		if (power <= 1e-9) {
			return;
		}
		chargeTotal = 0;
		pure.clear();
		for (var e : spec.intake().entrySet()) {
			charge.merge(e.getKey(), e.getValue(), Double::sum);
			chargeTotal += e.getValue();
		}
		markDirty();
	}

	public void tick(ServerWorld world, BlockPos pos, BlockState state) {
		Set<String> known = ownerKnows(world);
		measure(world, pos, state);
		refreshUpgrades(known);
		refreshPipes();
		breathe(world, pos, known);
		boolean bellows = spec.bellows() && Research.allows(known, "upgrade:forced_air") && owner != null;
		double blast = 0;
		if (bellows) {
			List<BlockPos> blowers = new java.util.ArrayList<>(List.of(pos));
			if (inStructure()) {
				blowers.addAll(structure.tuyeres()); // a furnace stack takes its blast through the Tuyere
			}
			for (BlockPos at : blowers) {
				for (PowerBlockEntity engine : neighbours(world, at, "shaft")) {
					if (engine.blowing()) {
						blast = Math.max(blast, engine.spec().num("blastBonus"));
					}
				}
			}
		}
		statusReaction = -1;
		activeReaction = -1;

		// Load the next item into the charge.
		ItemStack input = items.get(INPUT);
		boolean refused = false;
		boolean noReagent = false;
		if (onlyReagentsLeft() && !input.isEmpty()) {
			if (canLoad(input, known)) {
				if (charge.isEmpty()) {
					chargeTotal = 0;
					pure.clear();
				}
				load(world, pos, input);
			} else if (canProcess(reactions, input, known)) {
				noReagent = true;
			} else {
				refused = true;
			}
		} else if (onlyReagentsLeft() && input.isEmpty() && !pipes.isEmpty()) {
			pipeLoad(known, false);
		}
		// A big structure works through more than one item a tick: keep a tick's worth of work in the charge.
		if (inStructure()) {
			double tickWork = 1000.0 / spec.throughput() * sizeSpeed() * (upgraded(Upgrades.MECHANICAL_STIRRER) ? Upgrades.STIRRER_SPEED : 1);
			for (int i = 1; i < MAX_LOADS && !input.isEmpty() && remaining() < tickWork && canLoad(input, known); i++) {
				load(world, pos, input);
			}
			for (int i = 1; i < MAX_LOADS && input.isEmpty() && !pipes.isEmpty() && remaining() < tickWork && pipeLoad(known, false); i++) {
				// 3.38.0: a structure fed by pipe draws a tick's worth too
			}
		}
		if (!charge.isEmpty() && wantsReagent(known)) {
			load(world, pos, items.get(REAGENT));
		} else if (!charge.isEmpty() && items.get(REAGENT).isEmpty() && !pipes.isEmpty()) {
			pipeLoad(known, true);
		}
		boolean hasWork = !charge.isEmpty();

		// The fire is tended: once the charge is as hot as its reactions allow, the draft is closed and the
		// fire banked (no heat, no fuel burned) until it cools, so a low-temperature process isn't overheated.
		double target = Double.MAX_VALUE;
		if (hasWork) {
			target = 0;
			for (Reactions.Reaction r : reactions) {
				if (charge.getOrDefault(r.feed(), 0.0) > 1e-9 && allowed(r, known)) {
					target = Math.max(target, r.tMax());
				}
			}
			if (target <= 0) {
				target = Double.MAX_VALUE;
			}
		}
		// A Vacuum Pump lowers the boiling points: the charge reacts as if 100 °C hotter.
		double shift = upgraded(Upgrades.VACUUM_PUMP) && Upgrades.VACUUM_MACHINES.contains(spec.id()) ? Upgrades.VACUUM_C : 0;
		boolean banked = temperature + shift >= target;

		// Electricity: what the piles next to an electric machine can give this tick, up to its draw.
		List<PowerBlockEntity> piles = spec.electric() ? powerSources(world, pos) : List.of();
		double electric = 0;
		if (spec.electric() && hasWork) {
			for (PowerBlockEntity pile : piles) {
				electric += pile.available();
			}
			electric = Math.min(electric, firePower());
		}
		double electricUsed = 0;

		// Fuel. Only light a new one while there is something to do.
		boolean gas = selfHeated() && hasWork;
		if (!spec.electric() && !gas && burnLeft <= 0 && hasWork && !banked) {
			ItemStack fuelStack = items.get(FUEL);
			Reactions.Fuel fuel = Reactions.fuel(fuelStack);
			if (fuel != null) {
				burnTotal = burnLeft = Math.max(1, (int) Math.round(fuel.energy() / firePower()));
				flame = fuel.flame();
				useOne(world, pos, fuelStack);
				markDirty();
			}
		}
		double heatIn = 0;
		if (gas) {
			flame = Math.max(flame, spec.maxC());
			if (!banked) {
				heatIn = firePower();
			}
		} else if (burnLeft > 0 && !banked) {
			burnLeft--;
			heatIn = firePower();
		}
		cap = spec.electric() ? maxC() : Math.min(maxC(), flame + (bellows ? Reactions.BELLOWS_BONUS_C + blast : 0));
		if (shift > 0 && target < Double.MAX_VALUE) {
			cap = Math.min(cap, target - shift); // under vacuum the still is held below the top of its reactions' range
		}

		// Reactions.
		double reactionPower = 0;
		Status next = hasWork ? Status.WORKING : (refused ? Status.NEEDS_RESEARCH : noReagent ? Status.NEEDS_REAGENT : Status.IDLE);
		if (hasWork && backlogged()) {
			next = Status.OUTPUT_FULL;
		} else if (hasWork && spec.id().equals("deep_tank_fermenter") && !upgraded(Upgrades.STERILE_AIR_FILTER)) {
			next = Status.NEEDS_FILTER; // wild yeasts and bacteria would take over the tank
		} else if (hasWork) {
			double bonus = upgraded(Upgrades.STERILE_AIR_FILTER) && Upgrades.FERMENTERS.contains(spec.id()) ? Upgrades.FILTER_BONUS : 1;
			double budget = 1000.0 / spec.throughput() * sizeSpeed() * (upgraded(Upgrades.MECHANICAL_STIRRER) ? Upgrades.STIRRER_SPEED : 1);
			boolean bed = upgraded(Upgrades.CATALYST_BED);
			boolean hood = upgraded(Upgrades.GAS_RECOVERY_HOOD);
			boolean anode = upgraded(Upgrades.GHAST_TEAR_ANODE);
			Status blocked = null;
			for (Reactions.Reaction r : reactions) {
				double available = charge.getOrDefault(r.feed(), 0.0);
				if (available <= 1e-9) {
					continue;
				}
				if (!allowed(r, known) || !enoughEnriched(r, ratioInCharge(r.feed()))) {
					continue; // ends up as residue below
				}
				if (temperature + shift < r.tMin()) {
					blocked = blocked == null ? Status.HEATING : blocked;
					statusReaction = statusReaction < 0 ? r.index() : statusReaction;
					continue;
				}
				if (temperature + shift > r.tMax()) {
					blocked = Status.TOO_HOT;
					statusReaction = r.index();
					continue;
				}
				// A Catalyst Bed lets a reaction over a solid catalyst do more in the same time.
				double speed = bed && Upgrades.catalytic(r) ? Upgrades.CATALYST_SPEED : 1;
				double amount = Math.min(available, budget * speed);
				for (Reactions.Reagent g : r.reagents()) {
					amount = Math.min(amount, charge.getOrDefault(g.id(), 0.0) * 100 / g.units());
				}
				if (amount <= 1e-9) {
					blocked = blocked == null ? Status.NEEDS_REAGENT : blocked;
					statusReaction = statusReaction < 0 ? r.index() : statusReaction;
					continue;
				}
				// 3.46.0: a Ghast Tear anode lowers the voltage chlorine electrolysis needs.
				double energy = spec.electric() && anode && Upgrades.anodeHelps(r) ? r.energy() * Upgrades.ANODE_ENERGY : r.energy();
				if (spec.electric() && r.energy() > 0) {
					// Electrolysis takes its energy from the current, not from the heat.
					amount = Math.min(amount, (electric - electricUsed) * 100 / energy);
					if (amount <= 1e-9) {
						blocked = Status.NO_POWER;
						statusReaction = r.index();
						continue;
					}
				}
				if (r.carbon() > 0) {
					double need = amount * r.carbon() / 100;
					if (carbon < need) {
						refillCarbon();
					}
					if (carbon < need) {
						amount = carbon * 100 / r.carbon();
						blocked = Status.NEEDS_CARBON;
						statusReaction = r.index();
					}
					if (amount <= 1e-9) {
						continue;
					}
					carbon -= amount * r.carbon() / 100;
				}
				double feedRatio = ratioInCharge(r.feed());
				charge.put(r.feed(), available - amount);
				for (Reactions.Reagent g : r.reagents()) {
					charge.merge(g.id(), -amount * g.units() / 100, Double::sum);
				}
				for (Reactions.Product p : r.products()) {
					if (p.vented()) {
						// A Gas Recovery Hood catches vented liquids and gases (not steam) for flasks and cylinders.
						if (hood && com.periodicelements.Fluids.isFluid(p.id()) && !p.id().equals("water")) {
							outputs.merge(CAUGHT + p.id(), amount * p.units() / 100, Double::sum);
						}
						continue;
					}
					double units = amount * cut(r, p) / 100 * bonus;
					String el = Isotopes.elementOf(p.id());
					if (el != null && r.separates() && el.equals(Isotopes.elementOf(r.feed()))) {
						// A Calutron pass: the cut and the tails go straight to the output buffers with their ratios.
						double[] split = Isotopes.separate(feedRatio, r.sepAlpha(), r.sepFraction());
						double cut = feedRatio > 0 ? Math.min(r.sepFraction(), feedRatio / split[0]) : 0;
						outputs.merge(Isotopes.key(p.id(), split[0]), units * cut, Double::sum);
						outputs.merge(Isotopes.key(p.id(), split[1]), units * (1 - cut), Double::sum);
						continue;
					}
					if (el != null) {
						Double stamped = r.stamp().get(p.id());
						Isotopes.Tracked t = Isotopes.tracked(el);
						mixRatio(p.id(), units, stamped != null ? stamped
								: el.equals(Isotopes.elementOf(r.feed())) && !Double.isNaN(feedRatio) ? feedRatio : t.natural());
					}
					charge.merge(p.id(), units, Double::sum);
				}
				if (spec.electric() && r.energy() > 0) {
					electricUsed += amount * energy / 100;
				} else {
					reactionPower += amount * r.energy() / 100;
				}
				budget -= amount / speed;
				activeReaction = r.index();
				if (budget <= 1e-9) {
					break;
				}
			}
			settleCharge(known);
			if (activeReaction < 0 && blocked != null) {
				next = blocked == Status.HEATING && spec.electric() && electric <= 1e-9 ? Status.NO_POWER
						: blocked == Status.HEATING && burnLeft <= 0 && !spec.electric() ? Status.NO_FUEL : blocked;
			} else if (activeReaction >= 0 && blocked == Status.NEEDS_CARBON) {
				next = Status.NEEDS_CARBON;
			}
		}
		// The stirrer's engine burns fuel, or its motor draws current, while the charge reacts.
		if (activeReaction >= 0 && upgraded(Upgrades.MECHANICAL_STIRRER)) {
			if (spec.electric()) {
				electricUsed += Math.max(0, Math.min(electric - electricUsed, Upgrades.STIRRER_COST * firePower()));
			} else if (burnLeft > 0) {
				stirBurn += Upgrades.STIRRER_COST;
				if (stirBurn >= 1) {
					stirBurn -= 1;
					burnLeft--;
				}
			}
		}
		emitOutputs();
		boolean belt = upgraded(Upgrades.CONVEYOR_BELT);
		if ((upgraded(Upgrades.OUTPUT_PUSHER) || belt) && ++pushTimer % 8 == 0) {
			pushOutputs(world, pos, belt ? Upgrades.BELT_ITEMS : 1);
		}
		if (containerShort != null && (next == Status.WORKING || next == Status.OUTPUT_FULL || next == Status.IDLE)) {
			next = containerShort;
		}
		status = next;

		// Heat loss this tick.
		double loss = spec.loss() * sizeLoss() * (temperature - Reactions.AMBIENT_C)
				* (!spec.electric() && upgraded(Upgrades.REGENERATOR) ? Upgrades.REGENERATOR_LOSS : 1)
				* (upgraded(Upgrades.FOAM_LAGGING) && maxC() <= Upgrades.FOAM_MAX_C ? Upgrades.FOAM_LOSS : 1);

		// The current the reactions didn't use heats the cell (unless it is already hot enough). A thermostat
		// keeps it from overshooting the top of its reactions' range (2.0.0: the fermenters' narrow ranges).
		if (spec.electric()) {
			heatIn = banked ? 0 : electric - electricUsed;
			if (target < Double.MAX_VALUE) {
				heatIn = Math.max(0, Math.min(heatIn, (target - shift - temperature) * heatCapacity() + loss + reactionPower));
			}
			double draw = electricUsed + heatIn;
			lastDraw = draw;
			for (PowerBlockEntity pile : piles) {
				draw -= pile.draw(draw);
			}
		}

		// Heat balance.
		double dT = (heatIn - loss - reactionPower) / heatCapacity();
		double t = temperature + dT;
		if (dT > 0 && t > cap) {
			t = Math.max(temperature, cap);
		}
		temperature = Math.max(Reactions.AMBIENT_C, Math.min(maxC(), t));

		boolean lit = spec.electric() ? lastDraw > 1e-9 : burnLeft > 0 || (gas && !banked);
		activeHold = activeReaction >= 0 ? ACTIVE_HOLD_TICKS : Math.max(0, activeHold - 1);
		boolean active = activeHold > 0;
		// The look of the running process; it stays on the last one through the hold, so it doesn't blink.
		int mode = activeReaction >= 0 ? MachineAnimations.mode(spec.id(), Reactions.get(activeReaction).id())
				: active ? state.get(MachineBlock.MODE) : 0;
		if (state.get(Properties.LIT) != lit || state.get(MachineBlock.ACTIVE) != active || state.get(MachineBlock.MODE) != mode) {
			world.setBlockState(pos, state.with(Properties.LIT, lit).with(MachineBlock.ACTIVE, active).with(MachineBlock.MODE, mode), 3);
		}
		if (hasWork || lit) {
			markDirty();
		}
	}

	private void refillCarbon() {
		ItemStack fuelStack = items.get(FUEL);
		double c = fuelStack.isEmpty() ? 0 : Reactions.carbonIn(fuelStack);
		if (c > 0) {
			carbon += c;
			useOne((ServerWorld) world, pos, fuelStack);
		}
	}

	/** Moves everything no reaction here will use out of the charge, into product or residue buffers. */
	private void settleCharge(Set<String> known) {
		var it = charge.entrySet().iterator();
		while (it.hasNext()) {
			var e = it.next();
			String id = e.getKey();
			double units = e.getValue();
			if (units <= 1e-6) {
				it.remove();
				ratios.remove(id);
				continue;
			}
			boolean used = false;
			boolean product = pure.contains(id);
			for (Reactions.Reaction r : reactions) {
				if (allowed(r, known)) {
					used |= r.feed().equals(id) && reagentsAvailable(r, null) && enoughEnriched(r, ratioInCharge(id));
					product |= r.produces(id);
					for (Reactions.Reagent g : r.reagents()) {
						if (g.id().equals(id)) {
							// Keep a reagent while its feed is in the charge or waiting in the input slot.
							used |= charge.getOrDefault(r.feed(), 0.0) > 1e-6 || contains(composition(items.get(INPUT)), r.feed());
							product = true;
						}
					}
				}
			}
			if (!used) {
				if (!id.equals("water")) {
					String key = product && Isotopes.elementOf(id) != null ? Isotopes.key(id, ratioInCharge(id)) : id;
					outputs.merge(product ? key : spec.residue(), units, Double::sum);
				}
				it.remove(); // leftover water evaporates
				ratios.remove(id);
			}
		}
		if (charge.isEmpty()) {
			chargeTotal = 0;
			ratios.clear();
		}
	}

	private boolean backlogged() {
		for (var e : outputs.entrySet()) {
			if (e.getKey().startsWith(CAUGHT)) {
				continue; // caught gas never holds the machine up
			}
			double per = compressesInto(e.getKey()) ? com.periodicelements.Fluids.cylinderUnits() : Reactions.unitsPerItem(e.getKey());
			if (e.getValue() >= BACKLOG_ITEMS * per + per) {
				return true;
			}
		}
		return false;
	}

	/** Will this output go into Steel Cylinders (a gas, a Compressor fitted, cylinders in the slot)? */
	private boolean compressesInto(String id) {
		return com.periodicelements.Fluids.cylinderOf(Reactions.outputItem(id)) != null && compressing()
				&& !items.get(CYLINDERS).isEmpty();
	}

	private void emitOutputs() {
		containerShort = null;
		for (var e : outputs.entrySet()) {
			boolean caught = e.getKey().startsWith(CAUGHT);
			String id = caught ? e.getKey().substring(CAUGHT.length()) : e.getKey();
			Item item = Reactions.outputItem(id);
			boolean flask = com.periodicelements.Fluids.inFlask(item);
			Item cylinder = com.periodicelements.Fluids.cylinderOf(item);
			// A gas goes into cylinders while a Compressor is fitted and cylinders are in their slot.
			boolean compress = cylinder != null && compressing() && !items.get(CYLINDERS).isEmpty();
			double per = compress ? com.periodicelements.Fluids.cylinderUnits() : Reactions.unitsPerItem(id);
			int containerSlot = compress ? CYLINDERS : FLASKS;
			// 3.38.0: pipes come first: a liquid or gas goes straight into the tanks they reach, no flask needed.
			if (flask && e.getValue() > 1e-9 && !pipes.isEmpty()) {
				e.setValue(e.getValue() - com.periodicelements.fluid.FluidNet.send(pipes, item, e.getValue(), Isotopes.ratioOfKey(id)));
			}
			while (e.getValue() >= per - 1e-6) {
				ItemStack made = new ItemStack(compress ? cylinder : item);
				String el = Isotopes.elementOf(id);
				if (el != null) {
					Isotopes.stamp(made, el, Isotopes.ratioOfKey(id));
				}
				if (flask && items.get(containerSlot).isEmpty()) {
					if (vent && cylinder != null && !caught) {
						e.setValue(e.getValue() - per); // relief valve: nothing to hold it, so it goes up the flue
						continue;
					}
					if (!caught) {
						containerShort = compress ? Status.NEEDS_CYLINDERS : Status.NEEDS_FLASKS;
					}
					break;
				}
				if (!insertOutput(made, !caught && id.equals(spec.residue()))) {
					if (vent && cylinder != null && !caught) {
						e.setValue(e.getValue() - per); // relief valve: no room in the output slots
						continue;
					}
					break;
				}
				if (flask) {
					items.get(containerSlot).decrement(1);
				}
				e.setValue(e.getValue() - per);
			}
			if (caught && e.getValue() > BACKLOG_ITEMS * per) {
				e.setValue(BACKLOG_ITEMS * per); // with nowhere to put it, the rest is vented after all
			}
		}
	}

	/** Output Pusher: moves one item from each output slot into a container next to the machine. */
	private void pushOutputs(ServerWorld world, BlockPos pos, int count) {
		for (int i = PRODUCT_FIRST; i <= RESIDUE; i++) {
			for (int n = 0; n < count; n++) {
				pushOne(world, pos, items.get(i));
			}
		}
	}

	private void pushOne(ServerWorld world, BlockPos pos, ItemStack stack) {
		{
			if (stack.isEmpty()) {
				return;
			}
			for (Direction d : Direction.values()) {
				BlockEntity be = world.getBlockEntity(pos.offset(d));
				if (be instanceof MachineBlockEntity || be instanceof PowerBlockEntity || be instanceof StorageTankBlockEntity
						|| be instanceof com.periodicelements.research.ResearchStationBlockEntity) {
					continue; // a chest, barrel or hopper, not another machine: no pipes
				}
				net.minecraft.inventory.Inventory inv = net.minecraft.block.entity.HopperBlockEntity.getInventoryAt(world, pos.offset(d));
				if (inv == null) {
					continue;
				}
				ItemStack left = net.minecraft.block.entity.HopperBlockEntity.transfer(this, inv, stack.copyWithCount(1), d.getOpposite());
				if (left.isEmpty()) {
					stack.decrement(1);
					markDirty();
					break;
				}
			}
		}
	}

	private boolean insertOutput(ItemStack stack, boolean residue) {
		int from = residue ? RESIDUE : PRODUCT_FIRST;
		int to = residue ? RESIDUE : PRODUCT_LAST;
		for (int i = from; i <= to; i++) {
			ItemStack slot = items.get(i);
			if (!slot.isEmpty() && ItemStack.areItemsAndComponentsEqual(slot, stack) && slot.getCount() < slot.getMaxCount()) {
				slot.increment(1);
				return true;
			}
		}
		for (int i = from; i <= to; i++) {
			if (items.get(i).isEmpty()) {
				items.set(i, stack.copyWithCount(1));
				return true;
			}
		}
		return false;
	}

	/** Units waiting in the output buffers (for tests and tooltips). */
	public Map<String, Double> outputBuffers() {
		return outputs;
	}

	// ---------------------------------------------------------------- persistence

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		if (owner != null) {
			nbt.putUuid("Owner", owner);
		}
		nbt.putDouble("Temperature", temperature);
		nbt.putInt("BurnLeft", burnLeft);
		nbt.putInt("BurnTotal", burnTotal);
		nbt.putDouble("Flame", flame);
		nbt.putDouble("Carbon", carbon);
		nbt.putDouble("ChargeTotal", chargeTotal);
		nbt.put("Charge", writeMap(charge));
		nbt.put("Outputs", writeMap(outputs));
		nbt.put("Ratios", writeMap(ratios));
		nbt.putString("Pure", String.join(";", pure));
		nbt.putBoolean("Vent", vent);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		owner = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
		temperature = nbt.contains("Temperature") ? nbt.getDouble("Temperature") : Reactions.AMBIENT_C;
		burnLeft = nbt.getInt("BurnLeft");
		burnTotal = nbt.getInt("BurnTotal");
		flame = nbt.contains("Flame") ? nbt.getDouble("Flame") : Reactions.AMBIENT_C;
		carbon = nbt.getDouble("Carbon");
		chargeTotal = nbt.getDouble("ChargeTotal");
		readMap(nbt.getCompound("Charge"), charge);
		readMap(nbt.getCompound("Outputs"), outputs);
		readMap(nbt.getCompound("Ratios"), ratios);
		vent = nbt.getBoolean("Vent");
		pure.clear();
		for (String id : nbt.getString("Pure").split(";")) {
			if (!id.isEmpty()) {
				pure.add(id);
			}
		}
	}

	private static NbtCompound writeMap(Map<String, Double> map) {
		NbtCompound c = new NbtCompound();
		map.forEach(c::putDouble);
		return c;
	}

	private static void readMap(NbtCompound c, Map<String, Double> into) {
		into.clear();
		for (String k : c.getKeys()) {
			into.put(k, c.getDouble(k));
		}
	}

	// ---------------------------------------------------------------- screen

	@Override
	public Text getDisplayName() {
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new MachineScreenHandler(syncId, inv, this, properties);
	}

	// ---------------------------------------------------------------- inventory

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public boolean isEmpty() {
		return items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack s = Inventories.splitStack(items, slot, amount);
		if (!s.isEmpty()) {
			markDirty();
		}
		return s;
	}

	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(items, slot);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		items.set(slot, stack);
		stack.capCount(getMaxCount(stack));
		markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return net.minecraft.inventory.Inventory.canPlayerUse(this, player);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return switch (slot) {
			case INPUT -> canProcess(reactions, stack, null);
			case FUEL -> Reactions.fuel(stack) != null || Reactions.carbonIn(stack) > 0;
			case REAGENT -> isReagent(stack);
			case FLASKS -> stack.isOf(com.periodicelements.Fluids.GLASS_FLASK);
			case CYLINDERS -> stack.isOf(com.periodicelements.Fluids.STEEL_CYLINDER);
			case UPGRADE_FIRST, UPGRADE_LAST -> Upgrades.isUpgrade(stack);
			default -> false;
		};
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return switch (side) {
			case UP -> new int[]{INPUT};
			case DOWN -> new int[]{PRODUCT_FIRST, PRODUCT_FIRST + 1, PRODUCT_LAST, RESIDUE};
			default -> new int[]{FUEL, REAGENT, FLASKS, CYLINDERS};
		};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return isValid(slot, stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot >= PRODUCT_FIRST && slot <= RESIDUE;
	}

	/** Does this item hold a reagent some reaction in this machine needs? */
	public boolean isReagent(ItemStack stack) {
		BlockComposition.Entry entry = composition(stack);
		for (Reactions.Reaction r : reactions) {
			for (Reactions.Reagent g : r.reagents()) {
				if (contains(entry, g.id())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean usesReagents() {
		return reactions.stream().anyMatch(r -> !r.reagents().isEmpty());
	}

	public List<Reactions.Reaction> reactions() {
		return reactions;
	}
}
