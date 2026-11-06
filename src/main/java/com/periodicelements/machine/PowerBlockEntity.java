package com.periodicelements.machine;

import com.periodicelements.ModContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Locale;

/**
 * A Voltaic Pile or a Steam Engine: nine slots for whatever it burns, and power for the machines next to it.
 *
 * <p>Pile ("electric"): each zinc item dissolves into {@code fuelEnergy} kJ of current, stored up to {@code store}
 * kJ and handed out at up to {@code output} kJ per tick to electric machines alongside. Every
 * {@code zincPerElectrolyte} zinc use up one electrolyte item (acid, brine or salt); the zinc comes back as zinc oxide.
 *
 * <p>Engine ("shaft"): burns any fuel at {@code burn} kJ per tick with a water bucket in it (one bucket lasts
 * {@code waterTicks}), but only while a machine next to it has work. Machines with Bellows next to a running engine
 * get {@code blastBonus} °C more from their fire. An engine also runs while a Dynamo next to it has room.
 *
 * <p>Dynamo ("dynamo"): while a Steam Engine next to it runs, it turns {@code efficiency} of the engine's
 * {@code burn} into current, stored up to {@code store} kJ and given out at up to {@code output} kJ per tick.
 *
 * <p>Reactor ("reactor"): while water touches it, burns one fuel rod for {@code rodTicks}, making {@code gain} kJ
 * per tick (stored up to {@code store}); the rod comes back spent. Every {@code irradiateTicks} while it runs, one
 * item of each stack in its slots that is listed under {@code irradiation} captures neutrons and becomes the next
 * element. Hoppers can only take spent rods out of it.
 */
public class PowerBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
	private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
	public static final int SIZE = 9;
	private static final double ITEM_UNITS = 1000.0 / 9;

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
	private final Reactions.PowerSpec spec;
	private double stored;
	private int electrolyteLeft;
	private double waste;
	private int burnLeft;
	private int waterLeft;
	private boolean running;
	private int irradiateLeft;
	private int pendingSpent;
	/** 3.13.0: the reactor structure round this block (measured once a second), or null. */
	@org.jetbrains.annotations.Nullable
	private com.periodicelements.multiblock.Structures.Result structure;
	private int structureTimer;
	private boolean partsOn;
	/** Rod-ticks burnt this tick beyond whole ticks (a big core burns several rod-ticks a tick). */
	private double burnCarry;
	/** Is the rod in the core a natural-uranium one? */
	private boolean burningNatural;
	/** Why a reactor is not running: 0 it is (or idle), 1 subcritical, 2 scrammed (ran away), 3 scrammed (dry). */
	private int stopped;

	public PowerBlockEntity(BlockPos pos, BlockState state) {
		super(ModContent.POWER_ENTITY, pos, state);
		this.spec = Reactions.power(((PowerBlock) state.getBlock()).powerId());
	}

	/** 3.51.0: for the Creative Generator, which has its own block entity type and a spec not in power.json. */
	protected PowerBlockEntity(net.minecraft.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state, Reactions.PowerSpec spec) {
		super(type, pos, state);
		this.spec = spec;
	}

	public Reactions.PowerSpec spec() {
		return spec;
	}

	public double stored() {
		return stored;
	}

	/** Ticks left on the burning fuel rod or fuel item. */
	public int burnLeft() {
		return burnLeft;
	}

	/** Does this block supply current (a pile or a dynamo)? */
	public boolean givesCurrent() {
		return spec.kind().equals("electric") || spec.kind().equals("dynamo") || spec.kind().equals("solar")
				|| spec.kind().equals("reactor") || spec.kind().equals("wind") || spec.kind().equals("rtg")
				|| spec.kind().equals("battery") || spec.kind().equals("generator");
	}

	/** Would a dynamo take more current from an engine? */
	public boolean hungry() {
		return spec.kind().equals("dynamo") && stored < spec.num("store") - 1e-6;
	}

	/** kJ the pile or dynamo can give this tick. */
	public double available() {
		return givesCurrent() ? Math.min(stored, spec.num("output") * scale()) : 0;
	}

	/** 3.13.0: a reactor structure makes, stores and gives current like this many lone reactors. */
	private double scale() {
		return inStructure() ? structure.speed() : 1;
	}

	private double storeCap() {
		return spec.num("store") * scale();
	}

	public boolean inStructure() {
		return structure != null && structure.valid();
	}

	@org.jetbrains.annotations.Nullable
	public com.periodicelements.multiblock.Structures.Result structure() {
		return structure;
	}

	/** Why the reactor is stopped: 0 running or idle, 1 subcritical, 2 scrammed (runaway), 3 scrammed (dry). */
	public int stopped() {
		return stopped;
	}

	private void measure(ServerWorld world, BlockPos pos, BlockState state) {
		if (structureTimer++ % 20 != 0) {
			return;
		}
		var now = com.periodicelements.multiblock.Structures.detect(com.periodicelements.multiblock.Structures.Kind.NUCLEAR_REACTOR,
				world, pos, state.get(PowerBlock.FACING));
		com.periodicelements.multiblock.Structures.bind(world, pos, structure, now);
		structure = now;
		// 3.33.0: the core glows and the shielding's lamps blink while the reactor runs.
		boolean partsWanted = now != null && now.valid() && state.get(Properties.LIT);
		if (partsWanted || partsOn) {
			com.periodicelements.multiblock.Structures.animate(world, pos,
					com.periodicelements.multiblock.Structures.Kind.NUCLEAR_REACTOR, partsWanted);
			partsOn = partsWanted;
		}
	}

	@Override
	public void markRemoved() {
		if (world != null && structure != null) {
			com.periodicelements.multiblock.Structures.bind(world, pos, structure, null);
		}
		super.markRemoved();
	}

	/** Takes up to {@code kJ} from the pile and returns what it gave. */
	public double draw(double kJ) {
		double given = Math.min(kJ, available());
		stored -= given;
		markDirty();
		return given;
	}

	/** Is the engine running (and blowing the furnaces next to it)? */
	public boolean blowing() {
		return running;
	}

	private static String id(ItemStack stack) {
		return Registries.ITEM.getId(stack.getItem()).toString();
	}

	private int find(java.util.function.Predicate<ItemStack> test) {
		for (int i = 0; i < SIZE; i++) {
			if (!items.get(i).isEmpty() && test.test(items.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/** Puts one item into the slots; false if there is no room. */
	private boolean insert(ItemStack stack) {
		for (int i = 0; i < SIZE; i++) {
			ItemStack slot = items.get(i);
			if (!slot.isEmpty() && ItemStack.areItemsAndComponentsEqual(slot, stack) && slot.getCount() < slot.getMaxCount()) {
				slot.increment(1);
				return true;
			}
		}
		for (int i = 0; i < SIZE; i++) {
			if (items.get(i).isEmpty()) {
				items.set(i, stack.copyWithCount(1));
				return true;
			}
		}
		return false;
	}

	/** Uses one item from slot i, returning its bucket if it had one. */
	private void use(int i) {
		ItemStack stack = items.get(i);
		Item remainder = stack.getItem().getRecipeRemainder();
		stack.decrement(1);
		if (remainder != null) {
			if (stack.isEmpty()) {
				items.set(i, new ItemStack(remainder));
			} else {
				insert(new ItemStack(remainder));
			}
		}
	}

	public void tick(ServerWorld world, BlockPos pos, BlockState state) {
		if (spec.kind().equals("electric")) {
			tickPile();
			running = stored > 1e-6;
		} else if (spec.kind().equals("dynamo")) {
			tickDynamo(world, pos);
		} else if (spec.kind().equals("solar")) {
			tickSolar(world, pos);
		} else if (spec.kind().equals("reactor")) {
			measure(world, pos, state);
			if (inStructure()) {
				tickReactorStructure(world, pos);
			} else {
				tickReactor(world, pos);
			}
		} else if (spec.kind().equals("wind")) {
			tickWind(world, pos);
		} else if (spec.kind().equals("rtg")) {
			tickRtg();
		} else if (spec.kind().equals("battery")) {
			tickBattery(world, pos);
		} else if (spec.kind().equals("generator")) {
			tickGenerator();
		} else if (spec.kind().equals("helium")) {
			tickHelium();
		} else {
			tickEngine(world, pos);
		}
		BlockState next = state.with(Properties.LIT, running);
		if (state.getBlock() instanceof BatteryBlock) {
			double f = storeCap() > 0 ? stored / storeCap() : 0;
			next = next.with(BatteryBlock.LEVEL, BatteryBlock.level(f)).with(BatteryBlock.BAND, BatteryBlock.band(f));
		}
		if (next != state) {
			world.setBlockState(pos, next, 3);
		}
	}

	private void tickPile() {
		double perZinc = spec.num("fuelEnergy");
		if (stored + perZinc > spec.num("store")) {
			return;
		}
		String fuel = spec.str("fuel");
		int zinc = find(s -> id(s).equals(fuel));
		if (zinc < 0) {
			return;
		}
		if (electrolyteLeft <= 0) {
			List<String> electrolytes = spec.settings().getAsJsonArray("electrolytes").asList().stream()
					.map(e -> e.getAsString()).toList();
			int e = find(s -> electrolytes.contains(id(s)));
			if (e < 0) {
				return;
			}
			use(e);
			electrolyteLeft = (int) spec.num("zincPerElectrolyte");
		}
		double wasteUnits = spec.num("wasteUnits");
		Item wasteItem = Registries.ITEM.get(Identifier.of(spec.str("waste")));
		if (waste + wasteUnits >= 2 * ITEM_UNITS && find(s -> s.isEmpty()) < 0
				&& find(s -> s.isOf(wasteItem) && s.getCount() < s.getMaxCount()) < 0) {
			return; // no room for the zinc oxide
		}
		use(zinc);
		electrolyteLeft--;
		stored += perZinc;
		waste += wasteUnits;
		while (waste >= ITEM_UNITS - 1e-6 && insert(new ItemStack(wasteItem))) {
			waste -= ITEM_UNITS;
		}
		markDirty();
	}

	/** Sunlit: open sky above, daytime, no rain. */
	public static boolean sunlit(ServerWorld world, BlockPos pos) {
		long t = Math.floorMod(world.getTimeOfDay(), 24000L);
		boolean day = t < 12300 || t > 23850; // sunrise to sunset
		return day && !world.isRaining() && world.isSkyVisible(pos.up());
	}

	/** kJ per tick a wind turbine here makes: none without open sky, rising with height, more in rain and storms. */
	public double windPower(ServerWorld world, BlockPos pos) {
		// Open sky: nothing above it (by the heightmap, which is always up to date).
		if (world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY() + 1) {
			return 0;
		}
		double sea = spec.num("seaLevel");
		double height = Math.max(0, Math.min(1, (pos.getY() - sea) / (spec.num("fullHeight") - sea)));
		double weather = world.isThundering() ? spec.num("thunderFactor") : world.isRaining() ? spec.num("rainFactor") : 1;
		return spec.num("gain") * (0.25 + 0.75 * height) * weather;
	}

	private int pellets() {
		String fuel = spec.str("fuel");
		int n = 0;
		for (ItemStack s : items) {
			if (!s.isEmpty() && id(s).equals(fuel)) {
				n += s.getCount();
			}
		}
		return n;
	}

	private void tickRtg() {
		double gain = pellets() * spec.num("perPellet");
		running = gain > 0;
		if (running && stored < spec.num("store")) {
			stored = Math.min(spec.num("store"), stored + gain);
			markDirty();
		}
	}

	/** 3.36.0: game weight of one unit of this item's alpha activity, cached per item id. */
	private static final java.util.Map<String, Double> ALPHA = new java.util.concurrent.ConcurrentHashMap<>();
	/** Units of helium per tick per unit of alpha weight, from the calibration item; NaN until worked out. */
	private static double heliumScale = Double.NaN;

	private double alphaWeight(String itemId) {
		return ALPHA.computeIfAbsent(itemId, k -> {
			var items = spec.settings().getAsJsonObject("alphaItems");
			var alpha = spec.settings().getAsJsonObject("alpha");
			double sum = 0;
			for (var e : com.periodicelements.radiation.Radioactivity.elementsOf(k).entrySet()) {
				if (alpha.has(e.getKey())) {
					double f = items.has(k) && e.getKey().equals("plutonium") ? items.get(k).getAsDouble() : alpha.get(e.getKey()).getAsDouble();
					sum += e.getValue() * f;
				}
			}
			return sum;
		});
	}

	/** Units of helium a tick that this one item gives off (0 for anything not alpha-active). */
	public double heliumPerTick(ItemStack stack) {
		if (stack.isEmpty() || !spec.kind().equals("helium")) {
			return 0;
		}
		if (Double.isNaN(heliumScale)) {
			var cal = spec.settings().getAsJsonObject("calibration");
			double w = alphaWeight(cal.get("item").getAsString()) * cal.get("count").getAsDouble();
			heliumScale = w > 0 ? spec.num("hold") / (w * cal.get("ticks").getAsDouble()) : 0;
		}
		return alphaWeight(id(stack)) * heliumScale;
	}

	/** Units of helium a tick from everything in the slots. */
	public double heliumRate() {
		double sum = 0;
		for (ItemStack s : items) {
			sum += heliumPerTick(s) * s.getCount();
		}
		return sum;
	}

	private int emptyFlasks() {
		int n = 0;
		String flask = spec.str("flask");
		for (ItemStack s : items) {
			if (!s.isEmpty() && id(s).equals(flask)) {
				n += s.getCount();
			}
		}
		return n;
	}

	/**
	 * 3.36.0 Helium Collector: every alpha particle from what is kept in it becomes helium. The gas collects in the
	 * vessel ({@code stored}, up to {@code hold} units) and fills an empty Glass Flask whenever there is a flask's worth;
	 * with no empty flask it stays full and the rest escapes. Nothing in the slots is used up.
	 */
	private void tickHelium() {
		double rate = heliumRate();
		running = rate > 0 && stored < spec.num("hold");
		if (rate <= 0) {
			return;
		}
		double hold = spec.num("hold");
		stored = Math.min(hold, stored + rate);
		if (stored >= hold - 1e-9) {
			String flask = spec.str("flask");
			int f = find(s -> id(s).equals(flask));
			Item product = Registries.ITEM.get(Identifier.of(spec.str("product")));
			if (f >= 0) {
				ItemStack slot = items.get(f);
				if (slot.getCount() == 1) {
					items.set(f, new ItemStack(product));
					stored = 0;
				} else if (insert(new ItemStack(product))) {
					slot.decrement(1);
					stored = 0;
				}
			}
		}
		markDirty();
	}

	/** Charges from the sources it is wired to (never from other batteries). */
	private void tickBattery(ServerWorld world, BlockPos pos) {
		double room = Math.min(spec.num("chargeRate"), spec.num("store") - stored);
		running = false;
		if (room <= 1e-9) {
			return;
		}
		for (PowerBlockEntity p : MachineBlockEntity.powerSources(world, pos)) {
			if (p.spec().kind().equals("battery") || room <= 1e-9) {
				continue;
			}
			double got = p.draw(room);
			stored += got;
			room -= got;
			running |= got > 0;
		}
		if (running) {
			markDirty();
		}
	}

	/** Burns liquid fuel straight into current while there is room to store it. */
	private void tickGenerator() {
		double gain = spec.num("burn") * spec.num("efficiency");
		running = false;
		if (stored + gain > spec.num("store") + 1e-9) {
			return;
		}
		if (burnLeft <= 0) {
			List<String> fuels = spec.settings().getAsJsonArray("fuels").asList().stream().map(e -> e.getAsString()).toList();
			int f = find(s -> fuels.contains(id(s)) && Reactions.fuel(s) != null);
			if (f < 0) {
				return;
			}
			burnLeft = Math.max(1, (int) Math.round(Reactions.fuel(items.get(f)).energy() / spec.num("burn")));
			use(f);
		}
		burnLeft--;
		stored += gain;
		running = true;
		markDirty();
	}

	private void tickWind(ServerWorld world, BlockPos pos) {
		double gain = windPower(world, pos);
		running = gain > 0 && stored < spec.num("store");
		if (running) {
			stored = Math.min(spec.num("store"), stored + gain);
			markDirty();
		}
	}

	private void tickSolar(ServerWorld world, BlockPos pos) {
		running = sunlit(world, pos) && stored < spec.num("store");
		if (running) {
			stored = Math.min(spec.num("store"), stored + spec.num("gain"));
			markDirty();
		}
	}

	/** Does water touch the block (a pool reactor needs it as moderator and coolant)? */
	public static boolean cooled(ServerWorld world, BlockPos pos) {
		for (Direction d : Direction.values()) {
			if (world.getFluidState(pos.offset(d)).isIn(FluidTags.WATER)) {
				return true;
			}
		}
		return false;
	}

	private void tickReactor(ServerWorld world, BlockPos pos) {
		running = false;
		Item spent = Registries.ITEM.get(Identifier.of(spec.str("spent")));
		while (pendingSpent > 0 && insert(new ItemStack(spent))) {
			pendingSpent--;
			markDirty();
		}
		if (!cooled(world, pos)) {
			return;
		}
		// A boron control rod in the slots holds the chain reaction back while there is no room for more current.
		String control = spec.settings().has("controlRod") ? spec.str("controlRod") : "";
		if (!control.isEmpty() && find(s -> id(s).equals(control)) >= 0 && stored + spec.num("gain") > spec.num("store")) {
			return;
		}
		if (burnLeft <= 0) {
			String fuel = spec.str("fuel");
			// Natural uranium only goes critical with heavy water to slow the neutrons.
			String natural = spec.settings().has("naturalFuel") ? spec.str("naturalFuel") : "";
			String moderator = spec.settings().has("moderator") ? spec.str("moderator") : "";
			boolean moderated = !moderator.isEmpty() && find(s -> id(s).equals(moderator)) >= 0;
			int rod = find(s -> id(s).equals(fuel) || (moderated && id(s).equals(natural)));
			if (rod < 0 || pendingSpent > 0) {
				return;
			}
			use(rod);
			burnLeft = (int) spec.num("rodTicks");
		}
		burnLeft--;
		running = true;
		stored = Math.min(spec.num("store"), stored + spec.num("gain"));
		if (burnLeft <= 0 && !insert(new ItemStack(spent))) {
			pendingSpent++;
		}
		if (--irradiateLeft <= 0) {
			irradiateLeft = (int) spec.num("irradiateTicks");
			irradiate();
		}
		markDirty();
	}

	/** Sv/h at one block from a reactor that shut down hot, and for how long (half a game day). */
	private static final double LEAK_STRENGTH = 2.0;
	private static final int LEAK_TICKS = 12000;

	/**
	 * 3.13.0: a reactor block in the wall of a reactor structure. Its fuel channels burn rods and make current like
	 * that many lone reactors, as long as the core is critical: enriched rods go critical in any core, natural rods
	 * (with heavy water in the slots) only in a big one. A bigger core loses fewer neutrons, so a rod lasts longer.
	 * Water must touch the shielding; losing it, or running away without a Boron Control Rod, shuts the reactor down
	 * and leaks radiation round it for half a day.
	 */
	private void tickReactorStructure(ServerWorld world, BlockPos pos) {
		boolean wasRunning = running;
		running = false;
		Item spent = Registries.ITEM.get(Identifier.of(spec.str("spent")));
		while (pendingSpent > 0 && insert(new ItemStack(spent))) {
			pendingSpent--;
			markDirty();
		}
		String control = spec.settings().has("controlRod") ? spec.str("controlRod") : "";
		boolean rodIn = !control.isEmpty() && find(s -> id(s).equals(control)) >= 0;
		if (!structure.selfHeated()) {
			if (wasRunning || burnLeft > 0) {
				scram(world, pos, 3);
			}
			stopped = 3;
			return;
		}
		double m = structure.speed();
		double gain = spec.num("gain") * m;
		if (rodIn && stored + gain > storeCap()) {
			stopped = 0;
			return;
		}
		int a = structure.a(), b = structure.b(), h = structure.h();
		if (burnLeft <= 0) {
			if (pendingSpent > 0) {
				return;
			}
			String fuel = spec.str("fuel");
			String natural = spec.settings().has("naturalFuel") ? spec.str("naturalFuel") : "";
			String moderator = spec.settings().has("moderator") ? spec.str("moderator") : "";
			boolean moderated = !moderator.isEmpty() && find(s -> id(s).equals(moderator)) >= 0;
			int rod = find(s -> id(s).equals(fuel));
			boolean nat = false;
			if (rod < 0 && moderated && !natural.isEmpty()) {
				rod = find(s -> id(s).equals(natural));
				nat = rod >= 0;
			}
			if (rod < 0) {
				stopped = 0;
				return;
			}
			double kNew = com.periodicelements.multiblock.Structures.k(a, b, h, nat);
			if (kNew < 1) {
				stopped = 1;
				return;
			}
			if (kNew > com.periodicelements.multiblock.Structures.K_RUNAWAY && !rodIn) {
				stopped = 2; // it would run away: a fresh rod does not go in without a control rod
				return;
			}
			use(rod);
			burningNatural = nat;
			burnLeft = (int) spec.num("rodTicks");
		}
		double k = com.periodicelements.multiblock.Structures.k(a, b, h, burningNatural);
		if (k < 1) {
			stopped = 1; // the core shrank or lost its heavy water under a burning rod
			return;
		}
		if (k > com.periodicelements.multiblock.Structures.K_RUNAWAY && !rodIn) {
			scram(world, pos, 2);
			return;
		}
		stopped = 0;
		running = true;
		stored = Math.min(storeCap(), stored + gain);
		burnCarry += m / com.periodicelements.multiblock.Structures.rodLife(a, b, h);
		while (burnCarry >= 1) {
			burnCarry -= 1;
			if (burnLeft <= 0) {
				// The next rod goes in as soon as this one is spent.
				String fuel = burningNatural ? spec.str("naturalFuel") : spec.str("fuel");
				int rod = find(s -> id(s).equals(fuel));
				if (rod < 0 || pendingSpent > 0) {
					burnCarry = 0;
					break;
				}
				use(rod);
				burnLeft = (int) spec.num("rodTicks");
			}
			if (--burnLeft <= 0 && !insert(new ItemStack(spent))) {
				pendingSpent++;
			}
		}
		if (--irradiateLeft <= 0) {
			irradiateLeft = (int) spec.num("irradiateTicks");
			irradiate();
		}
		markDirty();
	}

	/** Drops the rods into the core's floor, so to speak: the reactor stops, its rod is spent, radiation leaks out. */
	private void scram(ServerWorld world, BlockPos pos, int why) {
		stopped = why;
		running = false;
		if (burnLeft > 0) {
			burnLeft = 0;
			burnCarry = 0;
			Item spent = Registries.ITEM.get(Identifier.of(spec.str("spent")));
			if (!insert(new ItemStack(spent))) {
				pendingSpent++;
			}
			com.periodicelements.radiation.Radiation.leak(world, pos, LEAK_STRENGTH * Math.sqrt(structure.speed()), LEAK_TICKS);
		}
		markDirty();
	}

	/** One item of each target stack captures neutrons and becomes its product. */
	public void irradiate() {
		var table = spec.settings().getAsJsonObject("irradiation");
		List<Integer> targets = new java.util.ArrayList<>();
		for (int i = 0; i < SIZE; i++) {
			if (!items.get(i).isEmpty() && (table.has(id(items.get(i)))
					|| com.periodicelements.radiation.Isotopes.irradiatedIf(items.get(i)) != null)) {
				targets.add(i);
			}
		}
		for (int i : targets) {
			ItemStack target = items.get(i);
			String conditional = com.periodicelements.radiation.Isotopes.irradiatedIf(target);
			Item product = Registries.ITEM.get(Identifier.of(conditional != null ? conditional : table.get(id(target)).getAsString()));
			ItemStack before = target.copy();
			target.decrement(1);
			ItemStack made = new ItemStack(product);
			Double ratio = com.periodicelements.radiation.Isotopes.irradiated(Registries.ITEM.getId(product).toString());
			if (ratio != null && product instanceof com.periodicelements.ElementItem e) {
				com.periodicelements.radiation.Isotopes.stamp(made, e.getElement().id(), ratio);
			}
			if (!insert(made)) {
				items.set(i, before);
			}
		}
		markDirty();
	}

	private void tickDynamo(ServerWorld world, BlockPos pos) {
		running = false;
		for (Direction d : Direction.values()) {
			if (world.getBlockEntity(pos.offset(d)) instanceof PowerBlockEntity e && e.spec().kind().equals("shaft")
					&& e.blowing() && hungry()) {
				stored = Math.min(spec.num("store"), stored + e.spec().num("burn") * spec.num("efficiency"));
				running = true;
				markDirty();
			}
		}
	}

	private void tickEngine(ServerWorld world, BlockPos pos) {
		boolean wanted = false;
		for (Direction d : Direction.values()) {
			if (world.getBlockEntity(pos.offset(d)) instanceof MachineBlockEntity m && m.busy() && m.spec().bellows()) {
				wanted = true;
			}
			if (world.getBlockEntity(pos.offset(d)) instanceof PowerBlockEntity p && p.hungry()) {
				wanted = true;
			}
		}
		running = false;
		if (!wanted) {
			return;
		}
		if (waterLeft <= 0) {
			int w = find(s -> s.isOf(Items.WATER_BUCKET));
			if (w < 0) {
				return;
			}
			use(w);
			waterLeft = (int) spec.num("waterTicks");
		}
		if (burnLeft <= 0) {
			int f = find(s -> Reactions.fuel(s) != null);
			if (f < 0) {
				return;
			}
			burnLeft = Math.max(1, (int) Math.round(Reactions.fuel(items.get(f)).energy() / spec.num("burn")));
			use(f);
		}
		burnLeft--;
		waterLeft--;
		running = true;
		markDirty();
	}

	/** A one-line state for the action bar when the block is opened. */
	public Text describe() {
		String ns = "gui.periodic_elements.power.";
		if (spec.kind().equals("dynamo")) {
			return Text.translatable(ns + "dynamo", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")),
					Text.translatable(ns + (running ? "dynamo.turning" : "dynamo.still")));
		}
		if (spec.kind().equals("solar")) {
			return Text.translatable(ns + "solar", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")),
					Text.translatable(ns + (getWorld() instanceof ServerWorld w && sunlit(w, pos) ? "solar.sun" : "solar.dark")));
		}
		if (spec.kind().equals("rtg")) {
			return Text.translatable(ns + "rtg", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")), pellets(),
					String.format(Locale.ROOT, "%.2f", pellets() * spec.num("perPellet")));
		}
		if (spec.kind().equals("generator")) {
			return Text.translatable(ns + "generator", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")),
					running ? Text.translatable(ns + "generator.running", burnLeft / 20) : Text.translatable(ns + "generator.idle"));
		}
		if (spec.kind().equals("helium")) {
			double perDay = heliumRate() * 24000;
			Text state = perDay <= 0 ? Text.translatable(ns + "helium.nothing")
					: stored >= spec.num("hold") - 1e-6 && emptyFlasks() == 0 ? Text.translatable(ns + "helium.no_flask")
					: Text.translatable(ns + "helium.flasks", emptyFlasks());
			return Text.translatable(ns + "helium", String.format(Locale.ROOT, "%.1f", stored),
					String.format(Locale.ROOT, "%.1f", spec.num("hold")),
					String.format(Locale.ROOT, perDay >= 10 ? "%.0f" : perDay >= 0.1 ? "%.2f" : "%.4f", perDay), state);
		}
		if (spec.kind().equals("battery")) {
			return Text.translatable(ns + "battery", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")));
		}
		if (spec.kind().equals("wind")) {
			return Text.translatable(ns + "wind", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")),
					String.format(Locale.ROOT, "%.2f", getWorld() instanceof ServerWorld w ? windPower(w, pos) : 0));
		}
		if (spec.kind().equals("reactor") && structure != null) {
			return describeStructure(ns);
		}
		if (spec.kind().equals("reactor")) {
			int rod = (int) spec.num("rodTicks");
			return Text.translatable(ns + "reactor", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")),
					burnLeft > 0 ? Text.translatable(ns + "reactor.burning", Math.round(100.0 * (rod - burnLeft) / rod))
							: Text.translatable(ns + "reactor.no_rod"),
					Text.translatable(ns + (getWorld() instanceof ServerWorld w && cooled(w, pos) ? "reactor.cooled" : "reactor.dry")));
		}
		if (spec.kind().equals("electric")) {
			return Text.translatable(ns + "pile", String.format(Locale.ROOT, "%.0f", stored),
					String.format(Locale.ROOT, "%.0f", spec.num("store")), electrolyteLeft);
		}
		return Text.translatable(ns + (running ? "engine_running" : "engine_idle"), burnLeft / 20, waterLeft / 20);
	}

	/** 3.13.0: the reactor structure's line: its core, current, rod, multiplication and state (or what is wrong). */
	private Text describeStructure(String ns) {
		String sk = "gui.periodic_elements.structure.";
		var r = structure;
		switch (r.problem()) {
			case MISSING -> {
				Text block = r.wanted() == null ? Text.literal("?") : r.wanted().getName();
				return r.layer() == 0 ? Text.translatable(sk + "missing.floor", block) : Text.translatable(sk + "missing", block, r.layer());
			}
			case TOO_SMALL -> {
				return Text.translatable(sk + "too_small.nuclear_reactor");
			}
			case NO_CHANNEL -> {
				return Text.translatable(sk + "no_channel");
			}
			default -> {
			}
		}
		int rod = (int) spec.num("rodTicks");
		double k = com.periodicelements.multiblock.Structures.k(r.a(), r.b(), r.h(), burnLeft > 0 && burningNatural);
		String state = switch (stopped) {
			case 1 -> "reactor.subcritical";
			case 2 -> "reactor.runaway";
			case 3 -> "reactor.dry";
			default -> "reactor.cooled";
		};
		return Text.translatable(ns + "reactor_core", r.a(), r.b(), r.h(), Math.round(r.speed() * com.periodicelements.multiblock.Structures.CHANNEL_BLOCKS),
				String.format(Locale.ROOT, "%.0f", stored), String.format(Locale.ROOT, "%.0f", storeCap()),
				burnLeft > 0 ? Text.translatable(ns + "reactor.burning", Math.round(100.0 * (rod - burnLeft) / rod))
						: Text.translatable(ns + "reactor.no_rod"),
				String.format(Locale.ROOT, "%.2f", k), Text.translatable(ns + state));
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new Generic3x3ContainerScreenHandler(syncId, inv, this);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		nbt.putDouble("Stored", stored);
		nbt.putInt("ElectrolyteLeft", electrolyteLeft);
		nbt.putDouble("Waste", waste);
		nbt.putInt("BurnLeft", burnLeft);
		nbt.putInt("WaterLeft", waterLeft);
		nbt.putInt("IrradiateLeft", irradiateLeft);
		nbt.putInt("PendingSpent", pendingSpent);
		nbt.putDouble("BurnCarry", burnCarry);
		nbt.putBoolean("BurningNatural", burningNatural);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		stored = nbt.getDouble("Stored");
		electrolyteLeft = nbt.getInt("ElectrolyteLeft");
		waste = nbt.getDouble("Waste");
		burnLeft = nbt.getInt("BurnLeft");
		waterLeft = nbt.getInt("WaterLeft");
		irradiateLeft = nbt.getInt("IrradiateLeft");
		pendingSpent = nbt.getInt("PendingSpent");
		burnCarry = nbt.getDouble("BurnCarry");
		burningNatural = nbt.getBoolean("BurningNatural");
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return ALL_SLOTS;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		if (spec.kind().equals("helium")) {
			return id(stack).equals(spec.str("product")); // hoppers take the helium, never the radioactive charge
		}
		return !spec.kind().equals("reactor") || id(stack).equals(spec.str("spent"));
	}

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
		markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return Inventory.canPlayerUse(this, player);
	}

	@Override
	public void clear() {
		items.clear();
	}
}
