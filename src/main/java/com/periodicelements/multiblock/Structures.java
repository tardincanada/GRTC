package com.periodicelements.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Multiblock structures (3.3.0): a shell of casing blocks round an empty interior, with an ordinary machine set in the
 * wall as its controller. The controller measures the structure once a second; a bigger structure works faster and
 * loses less heat per item:
 *
 * <pre>speed = (V / Vmin)^0.85     heat lost per tick x (S / Smin)     (so heat lost per item = (S/Smin) / speed)</pre>
 *
 * with V the interior volume and S its inner surface. Fire power grows with the speed and heat capacity with the surface, so the
 * a big structure heats up a little faster than the machine alone.
 */
public final class Structures {
	public static final double EXPONENT = 0.85;

	public enum Kind {
		/** Charcoal Blast Furnace: refractory column, interior 1x1 to 3x3, 3 to 10 high, open on top, a Tuyere at the base. */
		BLAST_FURNACE("charcoal_blast_furnace", 1, 3, 3, 10),
		/** Coke Oven Battery: 1 to 12 ovens (each 1 wide, 3 deep, 2 high) in a silica-brick block, heating walls between. */
		COKE_BATTERY("coke_oven", 1, 12, 1, 12),
		/** Lead Chamber (3.8.0): a closed room of Lead Sheet, inside 3x3x3 to 9x9x6; a Gay-Lussac Tower beside it adds 25%. */
		LEAD_CHAMBER("lead_chamber", 3, 9, 3, 6),
		/** Distillation Column (3.8.0): Distillation Trays stacked on the Oil Refinery Still, 1x1 or 3x3, 4 to 16 high. */
		DISTILLATION_COLUMN("refinery_still", 1, 3, 4, 16),
		/** Enrichment Cascade (3.13.0): a solid block of Diffuser Stages behind the controller, up to 4 wide and 4 high, 4 to 64 stages. */
		ENRICHMENT_CASCADE("enrichment_cascade", 1, 4, 4, 64),
		/** Nuclear Reactor (3.13.0): a core of Graphite and Fuel Channels, 3x3 to 9x9 and 3 to 7 high, in Reactor Shielding. */
		NUCLEAR_REACTOR("nuclear_reactor", 3, 9, 3, 7);

		public final String controller;
		public final int minA, maxA, minH, maxH;

		Kind(String controller, int minA, int maxA, int minH, int maxH) {
			this.controller = controller;
			this.minA = minA;
			this.maxA = maxA;
			this.minH = minH;
			this.maxH = maxH;
		}

		public String id() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}

		public Block shell() {
			return switch (this) {
				case BLAST_FURNACE -> Multiblock.REFRACTORY_BRICK;
				case COKE_BATTERY -> Multiblock.SILICA_BRICK;
				case LEAD_CHAMBER -> Multiblock.LEAD_SHEET;
				case DISTILLATION_COLUMN -> Multiblock.DISTILLATION_TRAY;
				case ENRICHMENT_CASCADE -> Multiblock.DIFFUSER_STAGE;
				case NUCLEAR_REACTOR -> Multiblock.REACTOR_SHIELDING;
			};
		}

		public Block controllerBlock() {
			return Registries.BLOCK.get(Identifier.of("periodic_elements", controller));
		}
	}

	public enum Problem { NONE, MISSING, TOO_SMALL, NO_TUYERE, NO_CHANNEL }

	/**
	 * What the controller found. {@code a} is the interior width (blast furnace) or the number of ovens (battery);
	 * {@code h} the interior height (blast furnace) or 0. {@code missing} is where a shell block is wanted, and
	 * {@code layer} its layer counted from the floor (0). 3.8.0: {@code b} is the lead chamber's second width (or the
	 * column's width, with {@code a} = {@code b}); {@code extra} is the column's shift of bitumen into light cuts (0 to 0.4)
	 * or 1 when a lead chamber has its Gay-Lussac Tower.
	 */
	public record Result(Kind kind, Problem problem, int a, int h, double speed, double loss, boolean selfHeated,
						 List<BlockPos> ports, List<BlockPos> tuyeres, @Nullable BlockPos missing, @Nullable Block wanted, int layer,
						 int b, double extra) {
		public boolean valid() {
			return problem == Problem.NONE;
		}

		/** Heat lost per item, as a fraction of the machine alone. */
		public double lossPerItem() {
			return loss / speed;
		}
	}

	@Nullable
	public static Kind forMachine(String machineId) {
		for (Kind k : Kind.values()) {
			if (k.controller.equals(machineId)) {
				return k;
			}
		}
		return null;
	}

	/** Speed of a blast furnace with interior a x a x h. */
	public static double furnaceSpeed(int a, int h) {
		return Math.pow(a * a * h / 3.0, EXPONENT);
	}

	/** Heat lost per tick by a blast furnace, as a multiple of the smallest one (walls and floor of the interior). */
	public static double furnaceLoss(int a, int h) {
		return (4.0 * a * h + a * a) / 13.0;
	}

	public static double batterySpeed(int n) {
		return Math.pow(n, EXPONENT);
	}

	/** Shared heating walls: n ovens lose (n + 1) / 2 times what one oven does. */
	public static double batteryLoss(int n) {
		return (n + 1) / 2.0;
	}

	/** From this many ovens the battery heats itself by burning its own coke-oven gas. */
	public static final int SELF_HEATING_OVENS = 4;

	// ---- Ports: which controller a Structure Port belongs to (filled by controllers as they measure). ----

	private static final Map<WorldAccess, Map<BlockPos, BlockPos>> PORTS = new WeakHashMap<>();

	@Nullable
	public static synchronized BlockPos controllerOf(WorldAccess world, BlockPos port) {
		Map<BlockPos, BlockPos> m = PORTS.get(world);
		return m == null ? null : m.get(port);
	}

	public static synchronized void bind(WorldAccess world, BlockPos controller, @Nullable Result old, @Nullable Result now) {
		Map<BlockPos, BlockPos> m = PORTS.computeIfAbsent(world, w -> new HashMap<>());
		if (old != null) {
			for (BlockPos p : old.ports()) {
				m.remove(p, controller);
			}
		}
		if (now != null && now.valid()) {
			for (BlockPos p : now.ports()) {
				m.put(p.toImmutable(), controller.toImmutable());
			}
		}
	}

	// ---- Detection. ----

	private static boolean air(BlockView w, BlockPos p) {
		return w.getBlockState(p).isAir();
	}

	private static boolean shell(Kind kind, BlockView w, BlockPos p, BlockPos controller) {
		if (p.equals(controller)) {
			return true;
		}
		BlockState s = w.getBlockState(p);
		return s.isOf(kind.shell()) || s.isOf(Multiblock.STRUCTURE_PORT) || (kind == Kind.BLAST_FURNACE && s.isOf(Multiblock.TUYERE));
	}

	/** Measures the structure round a controller facing out of its wall; null when nothing structural is there. */
	@Nullable
	/** The blocks of a kind of structure that animate while it works (3.33.0). */
	private static java.util.Set<Block> parts(Kind kind) {
		return switch (kind) {
			case BLAST_FURNACE -> java.util.Set.of(Multiblock.REFRACTORY_BRICK, Multiblock.TUYERE, Multiblock.STRUCTURE_PORT);
			case COKE_BATTERY -> java.util.Set.of(Multiblock.SILICA_BRICK, Multiblock.STRUCTURE_PORT);
			case LEAD_CHAMBER -> java.util.Set.of(Multiblock.LEAD_SHEET, Multiblock.GAY_LUSSAC_TOWER, Multiblock.STRUCTURE_PORT);
			case DISTILLATION_COLUMN -> java.util.Set.of(Multiblock.DISTILLATION_TRAY, Multiblock.STRUCTURE_PORT);
			case ENRICHMENT_CASCADE -> java.util.Set.of(Multiblock.DIFFUSER_STAGE, Multiblock.STRUCTURE_PORT);
			case NUCLEAR_REACTOR -> java.util.Set.of(Multiblock.GRAPHITE_BLOCK, Multiblock.FUEL_CHANNEL,
					Multiblock.REACTOR_SHIELDING, Multiblock.STRUCTURE_PORT);
		};
	}

	/** Most blocks one controller lights up: the largest reactor or cascade is well under this. */
	private static final int ANIMATE_LIMIT = 6000;

	/**
	 * 3.33.0: switches the animated texture of every structure block joined to the controller (through other blocks
	 * of the same structure) on or off. Called by the controller once a second; only blocks that change are updated.
	 */
	public static void animate(net.minecraft.world.World world, BlockPos controller, Kind kind, boolean on) {
		java.util.Set<Block> wanted = parts(kind);
		java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
		java.util.Set<BlockPos> seen = new java.util.HashSet<>();
		seen.add(controller);
		queue.add(controller);
		while (!queue.isEmpty() && seen.size() < ANIMATE_LIMIT) {
			BlockPos at = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos next = at.offset(d);
				if (!seen.add(next) || !world.isChunkLoaded(next)) {
					continue;
				}
				var state = world.getBlockState(next);
				if (!wanted.contains(state.getBlock())) {
					continue;
				}
				queue.add(next);
				if (state.contains(AnimatedPartBlock.ACTIVE) && state.get(AnimatedPartBlock.ACTIVE) != on) {
					world.setBlockState(next, state.with(AnimatedPartBlock.ACTIVE, on), net.minecraft.block.Block.NOTIFY_LISTENERS);
				}
			}
		}
	}

	public static Result detect(Kind kind, BlockView w, BlockPos c, Direction facing) {
		return switch (kind) {
			case BLAST_FURNACE -> furnace(w, c, facing);
			case COKE_BATTERY -> battery(w, c, facing);
			case LEAD_CHAMBER -> chamber(w, c, facing);
			case DISTILLATION_COLUMN -> column(w, c);
			case ENRICHMENT_CASCADE -> cascade(w, c, facing);
			case NUCLEAR_REACTOR -> reactor(w, c, facing);
		};
	}

	// ---- 3.8.0: lead chamber and distillation column. ----

	public static double chamberSpeed(int a, int b, int h, boolean tower) {
		return Math.pow(a * b * h / 27.0, EXPONENT) * (tower ? GAY_LUSSAC_SPEED : 1);
	}

	/** The whole inner surface of the room (walls, floor and roof) over the smallest room's. */
	public static double chamberLoss(int a, int b, int h) {
		return 2.0 * (a * b + a * h + b * h) / 54.0;
	}

	/** A Gay-Lussac Tower catches the nitrous fumes and sends them back: the chamber works 25% faster. */
	public static final double GAY_LUSSAC_SPEED = 1.25;
	public static final int TOWER_MIN = 3;

	public static double columnSpeed(int w) {
		return Math.pow(w * w, EXPONENT);
	}

	/** Heat is lost round the still and the bottom four trays, where the column is hot. */
	public static double columnLoss(int w) {
		return (16.0 * w + w * w) / 17.0;
	}

	/** Fraction of the bitumen a column of h trays sends up as naphtha and kerosene: none at 4, 40% at 16. */
	public static double columnCut(int h) {
		return 0.4 * Math.max(0, Math.min(1, (h - 4) / 12.0));
	}

	/**
	 * Units of a crude-distillation product with a column's cut: bitumen gives up {@code f} of itself, shared between
	 * naphtha and kerosene as they come (22:15). Mass balance holds.
	 */
	public static double cutUnits(String reaction, String product, double units, double f, double bitumen) {
		if (f <= 0 || !reaction.equals("crude_distillation")) {
			return units;
		}
		return switch (product) {
			case "bitumen" -> units * (1 - f);
			case "naphtha" -> units + bitumen * f * 22 / 37;
			case "kerosene" -> units + bitumen * f * 15 / 37;
			default -> units;
		};
	}

	@Nullable
	private static Result chamber(BlockView w, BlockPos c, Direction facing) {
		Kind kind = Kind.LEAD_CHAMBER;
		BlockPos start = c.offset(facing.getOpposite());
		if (!air(w, start)) {
			return null;
		}
		int limit = kind.maxA * kind.maxA * kind.maxH;
		Set<BlockPos> cells = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		cells.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockPos p = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos n = p.offset(d);
				if (air(w, n) && cells.add(n)) {
					if (cells.size() > limit) {
						return null; // open air: not a room
					}
					queue.add(n);
				}
			}
		}
		int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE, z0 = Integer.MAX_VALUE, z1 = Integer.MIN_VALUE;
		for (BlockPos p : cells) {
			x0 = Math.min(x0, p.getX());
			x1 = Math.max(x1, p.getX());
			y0 = Math.min(y0, p.getY());
			y1 = Math.max(y1, p.getY());
			z0 = Math.min(z0, p.getZ());
			z1 = Math.max(z1, p.getZ());
		}
		int dx = x1 - x0 + 1, dy = y1 - y0 + 1, dz = z1 - z0 + 1;
		if (cells.size() != dx * dy * dz || dx > kind.maxA || dz > kind.maxA || dy > kind.maxH) {
			return null; // not a box
		}
		// The shell: every block of the box one larger that is not inside.
		int seen = 0, looked = 0;
		BlockPos gap = null;
		List<BlockPos> ports = new ArrayList<>();
		List<BlockPos> shellCells = new ArrayList<>();
		for (int y = y0 - 1; y <= y1 + 1; y++) {
			for (int z = z0 - 1; z <= z1 + 1; z++) {
				for (int x = x0 - 1; x <= x1 + 1; x++) {
					if (x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1) {
						continue;
					}
					BlockPos p = new BlockPos(x, y, z);
					looked++;
					if (shell(kind, w, p, c)) {
						seen++;
						shellCells.add(p);
						if (w.getBlockState(p).isOf(Multiblock.STRUCTURE_PORT)) {
							ports.add(p);
						}
					} else if (gap == null) {
						gap = p;
					}
				}
			}
		}
		if (seen * 3 < looked) {
			return null;
		}
		if (gap != null) {
			return missing(kind, gap, kind.shell(), gap.getY() - (y0 - 1));
		}
		int a = Math.max(dx, dz), b = Math.min(dx, dz);
		if (b < kind.minA || dy < kind.minH) {
			return problem(kind, Problem.TOO_SMALL, a, dy);
		}
		boolean tower = false;
		for (BlockPos p : shellCells) {
			for (Direction d : Direction.Type.HORIZONTAL) {
				BlockPos n = p.offset(d);
				if (w.getBlockState(n).isOf(Multiblock.GAY_LUSSAC_TOWER)) {
					BlockPos bottom = n;
					while (w.getBlockState(bottom.down()).isOf(Multiblock.GAY_LUSSAC_TOWER)) {
						bottom = bottom.down();
					}
					int run = 0;
					while (run < 16 && w.getBlockState(bottom.up(run)).isOf(Multiblock.GAY_LUSSAC_TOWER)) {
						run++;
					}
					tower |= run >= TOWER_MIN;
				}
			}
			if (tower) {
				break;
			}
		}
		return new Result(kind, Problem.NONE, a, dy, chamberSpeed(a, b, dy, tower), chamberLoss(a, b, dy), false, ports, List.of(),
				null, null, 0, b, tower ? 1 : 0);
	}

	private static boolean tray(BlockView w, BlockPos p) {
		BlockState s = w.getBlockState(p);
		return s.isOf(Multiblock.DISTILLATION_TRAY) || s.isOf(Multiblock.STRUCTURE_PORT);
	}

	/** Trays stacked on the still: a 1x1 or 3x3 column, the still under its middle. */
	@Nullable
	private static Result column(BlockView w, BlockPos c) {
		Kind kind = Kind.DISTILLATION_COLUMN;
		BlockPos p1 = c.up();
		if (!tray(w, p1)) {
			return null;
		}
		int around = 0;
		BlockPos firstGap = null;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (tray(w, p1.add(dx, 0, dz))) {
					around++;
				} else if (firstGap == null) {
					firstGap = p1.add(dx, 0, dz);
				}
			}
		}
		if (around >= 5 && around < 9) {
			return missing(kind, firstGap, kind.shell(), 1);
		}
		int width = around == 9 ? 3 : 1, r = width / 2;
		int h = 0;
		List<BlockPos> ports = new ArrayList<>();
		for (int k = 1; k <= kind.maxH; k++) {
			int have = 0;
			BlockPos gap = null;
			List<BlockPos> layerPorts = new ArrayList<>();
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					BlockPos p = c.add(dx, k, dz);
					if (tray(w, p)) {
						have++;
						if (w.getBlockState(p).isOf(Multiblock.STRUCTURE_PORT)) {
							layerPorts.add(p);
						}
					} else if (gap == null) {
						gap = p;
					}
				}
			}
			if (have == width * width) {
				h++;
				ports.addAll(layerPorts);
				continue;
			}
			if (width == 3 && have * 2 >= width * width) {
				return missing(kind, gap, kind.shell(), k);
			}
			break;
		}
		if (h < kind.minH) {
			return problem(kind, Problem.TOO_SMALL, width, h);
		}
		return new Result(kind, Problem.NONE, width, h, columnSpeed(width), columnLoss(width), false, ports, List.of(), null, null, 0,
				width, columnCut(h));
	}

	// ---- 3.13.0: enrichment cascade and nuclear reactor. ----

	/** The cascade's longest row (stages one behind the other). */
	public static final int CASCADE_MAX_LENGTH = 16;
	/** Enriched UF6 per 100 natural: 14.5 for the machine alone (tails 0.25% U-235), 18 for 64 stages (tails 0.1%). */
	public static final double ENRICHED_ALONE = 14.5, ENRICHED_MAX = 18;

	public static double cascadeSpeed(int stages) {
		return Math.pow(stages / 4.0, EXPONENT);
	}

	/** Heat lost grows a little slower than the number of stages (neighbouring stages keep each other warm). */
	public static double cascadeLoss(int stages) {
		return Math.pow(stages / 4.0, 0.7);
	}

	/** Units of enriched UF6 per 100 natural for a cascade of {@code stages}: more stages strip the tails further. */
	public static double cascadeEnriched(int stages) {
		return ENRICHED_ALONE + (ENRICHED_MAX - ENRICHED_ALONE) * Math.max(0, Math.min(1, (stages - 4) / 60.0));
	}

	/** Product units of uranium enrichment in a cascade of {@code stages}; mass balance holds. */
	public static double cascadeUnits(String reaction, String product, double units, int stages) {
		if (!reaction.equals("uranium_enrichment")) {
			return units;
		}
		double e = cascadeEnriched(stages);
		return switch (product) {
			case "enriched_uranium_hexafluoride" -> e;
			case "depleted_uranium_hexafluoride" -> 100 - e;
			default -> units;
		};
	}

	private static boolean stage(BlockView w, BlockPos p) {
		BlockState s = w.getBlockState(p);
		return s.isOf(Multiblock.DIFFUSER_STAGE) || s.isOf(Multiblock.STRUCTURE_PORT);
	}

	/** A solid box of Diffuser Stages behind the controller: up to 4 wide, 4 high and 16 long. */
	@Nullable
	private static Result cascade(BlockView w, BlockPos c, Direction facing) {
		Kind kind = Kind.ENRICHMENT_CASCADE;
		Direction back = facing.getOpposite(), right = facing.rotateYClockwise();
		BlockPos start = c.offset(back);
		if (!stage(w, start)) {
			return null;
		}
		int left = 0, rightN = 0, down = 0, up = 0, len = 1;
		while (left + rightN + 1 < kind.maxA && stage(w, start.offset(right.getOpposite(), left + 1))) {
			left++;
		}
		while (left + rightN + 1 < kind.maxA && stage(w, start.offset(right, rightN + 1))) {
			rightN++;
		}
		while (down + up + 1 < kind.maxA && stage(w, start.down(down + 1))) {
			down++;
		}
		while (down + up + 1 < kind.maxA && stage(w, start.up(up + 1))) {
			up++;
		}
		while (len < CASCADE_MAX_LENGTH && stage(w, start.offset(back, len))) {
			len++;
		}
		int width = left + rightN + 1, height = down + up + 1;
		List<BlockPos> ports = new ArrayList<>();
		BlockPos corner = start.offset(right.getOpposite(), left).down(down);
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < len; z++) {
				for (int x = 0; x < width; x++) {
					BlockPos p = corner.offset(right, x).up(y).offset(back, z);
					if (!stage(w, p)) {
						return missing(kind, p, kind.shell(), y + 1);
					}
					if (w.getBlockState(p).isOf(Multiblock.STRUCTURE_PORT)) {
						ports.add(p.toImmutable());
					}
				}
			}
		}
		int n = Math.min(kind.maxH, width * height * len);
		if (n < kind.minH) {
			return problem(kind, Problem.TOO_SMALL, n, 0);
		}
		// Over 64 stages the extra rows add nothing: the block counts as 64.
		double loss = cascadeLoss(n);
		return new Result(kind, Problem.NONE, n, len, cascadeSpeed(n), loss, false, ports, List.of(), null, null, 0, width * 10 + height,
				cascadeEnriched(n));
	}

	/** Fuel Channel blocks that burn rods and give current like one reactor block alone. */
	public static final double CHANNEL_BLOCKS = 3;

	/** Neutron migration area in blocks squared: sets how many neutrons leak out of a small core. */
	public static final double MIGRATION = 0.3;
	/** Neutrons born per neutron lost inside an endless core: enriched fuel in graphite, and natural uranium with heavy water. */
	public static final double K_ENRICHED = 1.6, K_NATURAL = 1.15;
	/** Above this the chain reaction runs away unless a Boron Control Rod is in the reactor's slots. */
	public static final double K_RUNAWAY = 1.3;

	/** The share of neutrons that stay in an a x b x h core (one-group diffusion, reflector adds a block each side). */
	public static double nonLeakage(int a, int b, int h) {
		double b2 = Math.pow(Math.PI / (a + 1), 2) + Math.pow(Math.PI / (b + 1), 2) + Math.pow(Math.PI / (h + 1), 2);
		return 1 / (1 + MIGRATION * b2);
	}

	/** Neutron multiplication of a core: 1 or more is critical. */
	public static double k(int a, int b, int h, boolean natural) {
		return (natural ? K_NATURAL : K_ENRICHED) * nonLeakage(a, b, h);
	}

	/** How much longer a fuel rod lasts per channel than in the smallest core: fewer neutrons are lost at the edge. */
	public static double rodLife(int a, int b, int h) {
		return nonLeakage(a, b, h) / nonLeakage(3, 3, 3);
	}

	private static boolean core(BlockView w, BlockPos p) {
		BlockState s = w.getBlockState(p);
		return s.isOf(Multiblock.GRAPHITE_BLOCK) || s.isOf(Multiblock.FUEL_CHANNEL);
	}

	/**
	 * A box of Graphite Blocks and Fuel Channels behind the controller, closed all round by Reactor Shielding. Result:
	 * {@code a} x {@code b} x {@code h} core, {@code speed} = fuel channel blocks / 3 (one lone reactor's worth each), {@code extra} = share of neutrons kept,
	 * {@code selfHeated} = water touches the shielding.
	 */
	@Nullable
	private static Result reactor(BlockView w, BlockPos c, Direction facing) {
		Kind kind = Kind.NUCLEAR_REACTOR;
		BlockPos start = c.offset(facing.getOpposite());
		if (!core(w, start)) {
			return null;
		}
		int limit = kind.maxA * kind.maxA * kind.maxH;
		Set<BlockPos> cells = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		cells.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockPos p = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos n = p.offset(d);
				if (core(w, n) && cells.add(n)) {
					if (cells.size() > limit) {
						return null;
					}
					queue.add(n);
				}
			}
		}
		int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE, z0 = Integer.MAX_VALUE, z1 = Integer.MIN_VALUE;
		int channels = 0;
		for (BlockPos p : cells) {
			x0 = Math.min(x0, p.getX());
			x1 = Math.max(x1, p.getX());
			y0 = Math.min(y0, p.getY());
			y1 = Math.max(y1, p.getY());
			z0 = Math.min(z0, p.getZ());
			z1 = Math.max(z1, p.getZ());
			if (w.getBlockState(p).isOf(Multiblock.FUEL_CHANNEL)) {
				channels++;
			}
		}
		int dx = x1 - x0 + 1, dy = y1 - y0 + 1, dz = z1 - z0 + 1;
		if (dx > kind.maxA || dz > kind.maxA || dy > kind.maxH) {
			return null;
		}
		List<BlockPos> ports = new ArrayList<>();
		BlockPos gap = null;
		int gapLayer = 0;
		boolean wet = false;
		for (int y = y0 - 1; y <= y1 + 1; y++) {
			for (int z = z0 - 1; z <= z1 + 1; z++) {
				for (int x = x0 - 1; x <= x1 + 1; x++) {
					BlockPos p = new BlockPos(x, y, z);
					boolean inside = x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1;
					if (inside) {
						if (!cells.contains(p) && gap == null) {
							gap = p; // a hole in the core
							gapLayer = y - (y0 - 1);
						}
						continue;
					}
					if (!shell(kind, w, p, c)) {
						if (gap == null) {
							gap = p;
							gapLayer = y - (y0 - 1);
						}
						continue;
					}
					if (w.getBlockState(p).isOf(Multiblock.STRUCTURE_PORT)) {
						ports.add(p);
					}
					if (!wet) {
						for (Direction d : Direction.values()) {
							if (w.getFluidState(p.offset(d)).isIn(net.minecraft.registry.tag.FluidTags.WATER)) {
								wet = true;
								break;
							}
						}
					}
				}
			}
		}
		if (gap != null) {
			boolean inCore = gap.getX() >= x0 && gap.getX() <= x1 && gap.getY() >= y0 && gap.getY() <= y1 && gap.getZ() >= z0 && gap.getZ() <= z1;
			return missing(kind, gap, inCore ? Multiblock.GRAPHITE_BLOCK : kind.shell(), gapLayer);
		}
		int a = Math.max(dx, dz), b = Math.min(dx, dz);
		if (b < kind.minA || dy < kind.minH) {
			return problem(kind, Problem.TOO_SMALL, a, dy);
		}
		if (channels == 0) {
			return new Result(kind, Problem.NO_CHANNEL, a, dy, 1, 1, false, List.of(), List.of(), null, Multiblock.FUEL_CHANNEL, 0, b, 0);
		}
		return new Result(kind, Problem.NONE, a, dy, channels / CHANNEL_BLOCKS, 1, wet, ports, List.of(), null, null, 0, b, nonLeakage(a, b, dy));
	}


	private static Result missing(Kind kind, BlockPos at, Block wanted, int layer) {
		return new Result(kind, Problem.MISSING, 0, 0, 1, 1, false, List.of(), List.of(), at.toImmutable(), wanted, layer, 0, 0);
	}

	private static Result problem(Kind kind, Problem p, int a, int h) {
		return new Result(kind, p, a, h, 1, 1, false, List.of(), List.of(), null, p == Problem.NO_TUYERE ? Multiblock.TUYERE : null, 0, a, 0);
	}

	@Nullable
	private static Result furnace(BlockView w, BlockPos c, Direction facing) {
		Kind kind = Kind.BLAST_FURNACE;
		BlockPos start = c.offset(facing.getOpposite());
		if (!air(w, start)) {
			return null;
		}
		BlockPos b0 = start;
		for (int i = 0; i < kind.maxH && air(w, b0.down()); i++) {
			b0 = b0.down();
		}
		// The interior's bottom layer: the air reachable sideways, at most 3 x 3.
		Set<BlockPos> cells = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		cells.add(b0);
		queue.add(b0);
		while (!queue.isEmpty()) {
			BlockPos p = queue.poll();
			for (Direction d : Direction.Type.HORIZONTAL) {
				BlockPos n = p.offset(d);
				if (air(w, n) && cells.add(n)) {
					if (cells.size() > kind.maxA * kind.maxA) {
						return null; // open ground, not a furnace
					}
					queue.add(n);
				}
			}
		}
		int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, z0 = Integer.MAX_VALUE, z1 = Integer.MIN_VALUE;
		for (BlockPos p : cells) {
			x0 = Math.min(x0, p.getX());
			x1 = Math.max(x1, p.getX());
			z0 = Math.min(z0, p.getZ());
			z1 = Math.max(z1, p.getZ());
		}
		int a = x1 - x0 + 1;
		if (a != z1 - z0 + 1 || cells.size() != a * a) {
			return null;
		}
		// Count the shell blocks we look at: a lone machine in a field reports nothing.
		int seen = 0, looked = 0;
		BlockPos firstGap = null;
		int gapLayer = 0;
		// Floor under the whole furnace.
		int y0 = b0.getY();
		for (int x = x0 - 1; x <= x1 + 1; x++) {
			for (int z = z0 - 1; z <= z1 + 1; z++) {
				BlockPos p = new BlockPos(x, y0 - 1, z);
				looked++;
				if (shell(kind, w, p, c)) {
					seen++;
				} else if (firstGap == null) {
					firstGap = p;
				}
			}
		}
		// Layers: walls all round an empty footprint, up to where the walls stop.
		int h = 0;
		List<BlockPos> ports = new ArrayList<>();
		List<BlockPos> tuyeres = new ArrayList<>();
		for (int k = 0; k < kind.maxH; k++) {
			int y = y0 + k;
			boolean empty = true;
			for (int x = x0; x <= x1 && empty; x++) {
				for (int z = z0; z <= z1 && empty; z++) {
					empty = air(w, new BlockPos(x, y, z));
				}
			}
			List<BlockPos> ring = ring(x0, x1, z0, z1, y);
			int have = 0;
			BlockPos gap = null;
			for (BlockPos p : ring) {
				if (shell(kind, w, p, c)) {
					have++;
				} else if (gap == null) {
					gap = p;
				}
			}
			if (empty && have == ring.size()) {
				h++;
				looked += ring.size();
				seen += have;
				for (BlockPos p : ring) {
					BlockState s = w.getBlockState(p);
					if (s.isOf(Multiblock.STRUCTURE_PORT)) {
						ports.add(p);
					} else if (k == 0 && s.isOf(Multiblock.TUYERE)) {
						tuyeres.add(p);
					}
				}
				continue;
			}
			if (have * 2 >= ring.size() && firstGap == null) {
				// A wall with holes in it: the furnace goes on up, but this block is missing.
				firstGap = gap != null ? gap : ring.get(0);
				gapLayer = k + 1;
				looked += ring.size();
				seen += have;
			}
			break;
		}
		if (looked == 0 || seen * 3 < looked) {
			return null;
		}
		if (firstGap != null) {
			return missing(kind, firstGap, kind.shell(), firstGap.getY() < y0 ? 0 : gapLayer);
		}
		if (c.getY() >= y0 + h || h < kind.minH) {
			return problem(kind, Problem.TOO_SMALL, a, h);
		}
		if (tuyeres.isEmpty()) {
			return problem(kind, Problem.NO_TUYERE, a, h);
		}
		return new Result(kind, Problem.NONE, a, h, furnaceSpeed(a, h), furnaceLoss(a, h), false, ports, tuyeres, null, null, 0, a, 0);
	}

	private static List<BlockPos> ring(int x0, int x1, int z0, int z1, int y) {
		List<BlockPos> out = new ArrayList<>();
		for (int x = x0 - 1; x <= x1 + 1; x++) {
			for (int z = z0 - 1; z <= z1 + 1; z++) {
				if (x < x0 || x > x1 || z < z0 || z > z1) {
					out.add(new BlockPos(x, y, z));
				}
			}
		}
		return out;
	}

	/**
	 * An oven: 1 x 3 x 2 of air, with most of the floor, roof and back wall round it of shell blocks (open air beside a
	 * battery is not an oven; an oven with a hole in its roof is, and the hole is reported).
	 */
	private static boolean oven(BlockView w, BlockPos b0, Direction right, Direction back, int k, BlockPos c) {
		BlockPos o = b0.offset(right, 2 * k);
		int shells = 0;
		for (int d = 0; d < 3; d++) {
			for (int j = 0; j < 2; j++) {
				if (!air(w, o.offset(back, d).up(j))) {
					return false;
				}
			}
			shells += (shell(Kind.COKE_BATTERY, w, o.offset(back, d).down(), c) ? 1 : 0)
					+ (shell(Kind.COKE_BATTERY, w, o.offset(back, d).up(2), c) ? 1 : 0);
		}
		shells += (shell(Kind.COKE_BATTERY, w, o.offset(back, 3), c) ? 1 : 0) + (shell(Kind.COKE_BATTERY, w, o.offset(back, 3).up(), c) ? 1 : 0);
		return shells * 2 >= 8;
	}

	@Nullable
	private static Result battery(BlockView w, BlockPos c, Direction facing) {
		Kind kind = Kind.COKE_BATTERY;
		Direction back = facing.getOpposite();
		Direction right = facing.rotateYClockwise();
		BlockPos start = c.offset(back);
		if (!air(w, start)) {
			return null;
		}
		BlockPos b0 = air(w, start.down()) ? start.down() : start;
		if (!oven(w, b0, right, back, 0, c)) {
			return null;
		}
		int kMax = 0, kMin = 0;
		while (kMax - kMin + 1 < kind.maxA && oven(w, b0, right, back, kMax + 1, c)) {
			kMax++;
		}
		while (kMax - kMin + 1 < kind.maxA && oven(w, b0, right, back, kMin - 1, c)) {
			kMin--;
		}
		int n = kMax - kMin + 1;
		List<BlockPos> ports = new ArrayList<>();
		BlockPos gap = null;
		int gapLayer = 0, seen = 0, looked = 0;
		for (int j = -1; j <= 2; j++) {
			for (int d = -1; d <= 3; d++) {
				for (int r = 2 * kMin - 1; r <= 2 * kMax + 1; r++) {
					boolean inside = j >= 0 && j <= 1 && d >= 0 && d <= 2 && Math.floorMod(r, 2) == 0;
					if (inside) {
						continue;
					}
					BlockPos p = b0.offset(right, r).offset(back, d).up(j);
					looked++;
					if (shell(kind, w, p, c)) {
						seen++;
						if (w.getBlockState(p).isOf(Multiblock.STRUCTURE_PORT)) {
							ports.add(p);
						}
					} else if (gap == null) {
						gap = p;
						gapLayer = j + 1;
					}
				}
			}
		}
		if (seen * 3 < looked) {
			return null;
		}
		if (gap != null) {
			return missing(kind, gap, kind.shell(), gapLayer);
		}
		return new Result(kind, Problem.NONE, n, 0, batterySpeed(n), batteryLoss(n), n >= SELF_HEATING_OVENS, ports,
				List.of(), null, null, 0, 0, 0);
	}

	// ---- Templates: the guide draws them and the game tests build them. ----

	public enum Part { SHELL, TUYERE, PORT, CONTROLLER, TOWER, GRAPHITE, CHANNEL }

	/**
	 * A structure laid out in its own coordinates: x to the right as you face the controller, y up from the floor,
	 * z away from you (z = 0 is the wall with the controller in it). Interior cells are simply absent.
	 */
	public record Template(Kind kind, int sx, int sy, int sz, Map<BlockPos, Part> parts, BlockPos controller) {
		public Block block(Part p) {
			return switch (p) {
				case SHELL -> kind.shell();
				case TUYERE -> Multiblock.TUYERE;
				case PORT -> Multiblock.STRUCTURE_PORT;
				case CONTROLLER -> kind.controllerBlock();
				case TOWER -> Multiblock.GAY_LUSSAC_TOWER;
				case GRAPHITE -> Multiblock.GRAPHITE_BLOCK;
				case CHANNEL -> Multiblock.FUEL_CHANNEL;
			};
		}

		/** Where a template position lands in the world, for a controller at {@code c} facing {@code facing}. */
		public BlockPos toWorld(BlockPos rel, BlockPos c, Direction facing) {
			Direction right = facing.rotateYClockwise();
			Direction back = facing.getOpposite();
			return c.offset(right, rel.getX() - controller.getX()).up(rel.getY() - controller.getY())
					.offset(back, rel.getZ() - controller.getZ());
		}

		/** Builds the structure (tests): the controller with its facing, the shell, and air inside. */
		public void build(net.minecraft.world.World world, BlockPos c, Direction facing) {
			for (int y = 0; y < sy; y++) {
				for (int z = 0; z < sz; z++) {
					for (int x = 0; x < sx; x++) {
						BlockPos rel = new BlockPos(x, y, z);
						Part p = parts.get(rel);
						BlockState s = p == null ? Blocks.AIR.getDefaultState() : block(p).getDefaultState();
						if (p == Part.CONTROLLER) {
							s = s.with(com.periodicelements.machine.MachineBlock.FACING, facing);
						}
						world.setBlockState(toWorld(rel, c, facing), s, 3);
					}
				}
			}
		}

		public int count(Part p) {
			return (int) parts.values().stream().filter(q -> q == p).count();
		}
	}

	/** A blast furnace with interior a x a x h: controller and a port low in the front, a Tuyere in the left wall, a port at the top. */
	public static Template furnace(int a, int h) {
		Map<BlockPos, Part> parts = new HashMap<>();
		int s = a + 2;
		for (int y = 0; y <= h; y++) {
			for (int z = 0; z < s; z++) {
				for (int x = 0; x < s; x++) {
					boolean inside = y >= 1 && x >= 1 && x <= a && z >= 1 && z <= a;
					if (!inside) {
						parts.put(new BlockPos(x, y, z), Part.SHELL);
					}
				}
			}
		}
		BlockPos controller = new BlockPos(1, 1, 0);
		parts.put(controller, Part.CONTROLLER);
		parts.put(new BlockPos(0, 1, 1), Part.TUYERE);
		parts.put(new BlockPos(1, 1, s - 1), Part.PORT);
		parts.put(new BlockPos(1, h, 0), Part.PORT);
		return new Template(Kind.BLAST_FURNACE, s, h + 1, s, parts, controller);
	}

	/** A coke battery of n ovens: controller in front of the first oven, a port above it. */
	public static Template battery(int n) {
		Map<BlockPos, Part> parts = new HashMap<>();
		int sx = 2 * n + 1;
		for (int y = 0; y < 4; y++) {
			for (int z = 0; z < 5; z++) {
				for (int x = 0; x < sx; x++) {
					boolean inside = y >= 1 && y <= 2 && z >= 1 && z <= 3 && x % 2 == 1;
					if (!inside) {
						parts.put(new BlockPos(x, y, z), Part.SHELL);
					}
				}
			}
		}
		BlockPos controller = new BlockPos(1, 1, 0);
		parts.put(controller, Part.CONTROLLER);
		parts.put(new BlockPos(1, 2, 0), Part.PORT);
		return new Template(Kind.COKE_BATTERY, sx, 4, 5, parts, controller);
	}

	/**
	 * A lead chamber with inside a (across) x b (deep) x h: controller and a port low in the front wall; with
	 * {@code tower}, a Gay-Lussac Tower three high against the right wall.
	 */
	public static Template chamber(int a, int b, int h, boolean tower) {
		Map<BlockPos, Part> parts = new HashMap<>();
		int sx = a + 2, sy = h + 2, sz = b + 2;
		for (int y = 0; y < sy; y++) {
			for (int z = 0; z < sz; z++) {
				for (int x = 0; x < sx; x++) {
					boolean inside = x >= 1 && x <= a && y >= 1 && y <= h && z >= 1 && z <= b;
					if (!inside) {
						parts.put(new BlockPos(x, y, z), Part.SHELL);
					}
				}
			}
		}
		BlockPos controller = new BlockPos(1, 1, 0);
		parts.put(controller, Part.CONTROLLER);
		parts.put(new BlockPos(a, 1, 0), Part.PORT);
		if (tower) {
			for (int y = 1; y <= TOWER_MIN; y++) {
				parts.put(new BlockPos(sx, y, 1), Part.TOWER);
			}
			sx++;
		}
		return new Template(Kind.LEAD_CHAMBER, sx, sy, sz, parts, controller);
	}

	/** A distillation column of w x w trays (w 1 or 3), h high, on the still; a port at the top. */
	public static Template column(int w, int h) {
		Map<BlockPos, Part> parts = new HashMap<>();
		int r = w / 2;
		BlockPos controller = new BlockPos(r, 0, r);
		parts.put(controller, Part.CONTROLLER);
		for (int y = 1; y <= h; y++) {
			for (int z = 0; z < w; z++) {
				for (int x = 0; x < w; x++) {
					parts.put(new BlockPos(x, y, z), Part.SHELL);
				}
			}
		}
		parts.put(new BlockPos(0, h, 0), Part.PORT);
		return new Template(Kind.DISTILLATION_COLUMN, w, h + 1, w, parts, controller);
	}

	/** A cascade of w x h rows of l stages behind the controller, which sits in front of the bottom-left row; a port at the far end. */
	public static Template cascade(int w, int h, int l) {
		Map<BlockPos, Part> parts = new HashMap<>();
		BlockPos controller = new BlockPos(0, 0, 0);
		parts.put(controller, Part.CONTROLLER);
		for (int y = 0; y < h; y++) {
			for (int z = 1; z <= l; z++) {
				for (int x = 0; x < w; x++) {
					parts.put(new BlockPos(x, y, z), Part.SHELL);
				}
			}
		}
		parts.put(new BlockPos(0, 0, l), Part.PORT);
		return new Template(Kind.ENRICHMENT_CASCADE, w, h, l + 1, parts, controller);
	}

	/**
	 * A reactor with an a x a x h core: graphite with a column of fuel channels at every other block both ways
	 * (corners included), shielding round it, the controller low in the front wall and a port beside it.
	 */
	public static Template reactor(int a, int b, int h) {
		Map<BlockPos, Part> parts = new HashMap<>();
		int sx = a + 2, sy = h + 2, sz = b + 2;
		for (int y = 0; y < sy; y++) {
			for (int z = 0; z < sz; z++) {
				for (int x = 0; x < sx; x++) {
					boolean inside = x >= 1 && x <= a && y >= 1 && y <= h && z >= 1 && z <= b;
					parts.put(new BlockPos(x, y, z), !inside ? Part.SHELL : x % 2 == 1 && z % 2 == 1 ? Part.CHANNEL : Part.GRAPHITE);
				}
			}
		}
		BlockPos controller = new BlockPos(1, 1, 0);
		parts.put(controller, Part.CONTROLLER);
		parts.put(new BlockPos(2, 1, 0), Part.PORT);
		return new Template(Kind.NUCLEAR_REACTOR, sx, sy, sz, parts, controller);
	}

	public static Template smallest(Kind kind) {
		return switch (kind) {
			case BLAST_FURNACE -> furnace(kind.minA, kind.minH);
			case COKE_BATTERY -> battery(kind.minA);
			case LEAD_CHAMBER -> chamber(3, 3, 3, false);
			case DISTILLATION_COLUMN -> column(1, kind.minH);
			case ENRICHMENT_CASCADE -> cascade(1, 1, 4);
			case NUCLEAR_REACTOR -> reactor(3, 3, 3);
		};
	}

	public static Template largest(Kind kind) {
		return switch (kind) {
			case BLAST_FURNACE -> furnace(kind.maxA, kind.maxH);
			case COKE_BATTERY -> battery(kind.maxA);
			case LEAD_CHAMBER -> chamber(9, 9, 6, true);
			case DISTILLATION_COLUMN -> column(3, kind.maxH);
			case ENRICHMENT_CASCADE -> cascade(4, 4, 4);
			case NUCLEAR_REACTOR -> reactor(9, 9, 7);
		};
	}

	private Structures() {
	}
}
