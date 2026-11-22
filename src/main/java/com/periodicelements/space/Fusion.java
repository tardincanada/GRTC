package com.periodicelements.space;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Level 8, stage 3 (3.63.0): fusion power. The four parts of a Tokamak's ring, and the check of that ring.
 * <p>
 * The ring is 5 x 5 wide and 3 high round a column of three Central Solenoids. Middle layer: 8 Vacuum Vessels round the
 * solenoid, then 16 Toroidal Field Coils. Top and bottom layers: 8 Lithium Blankets, then 16 coils. The Tokamak (a power
 * block of kind "fusion", machine/PowerBlockEntity.tickFusion) stands against the middle of one side, on the middle
 * layer, so the centre of the ring is three blocks from it.
 */
public final class Fusion {
	public static Block TOROIDAL_FIELD_COIL, CENTRAL_SOLENOID, VACUUM_VESSEL, LITHIUM_BLANKET;

	private Fusion() {
	}

	/** The Vacuum Vessel: glows pink-violet while the plasma burns (a Fiat Lux request: vacuum_vessel lit=true). */
	public static class VacuumVesselBlock extends Block {
		public static final BooleanProperty LIT = Properties.LIT;

		public VacuumVesselBlock(Settings settings) {
			super(settings);
			setDefaultState(getStateManager().getDefaultState().with(LIT, false));
		}

		@Override
		protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
			builder.add(LIT);
		}

		@Nullable
		@Override
		public BlockState getPlacementState(ItemPlacementContext ctx) {
			return getDefaultState();
		}
	}

	private static AbstractBlock.Settings metal(MapColor colour) {
		return AbstractBlock.Settings.create().mapColor(colour).strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL);
	}

	static void register(List<Item> items) {
		TOROIDAL_FIELD_COIL = Space.gatedBlock(items, "toroidal_field_coil", "fusion_power", new Block(metal(MapColor.IRON_GRAY)));
		CENTRAL_SOLENOID = Space.gatedBlock(items, "central_solenoid", "fusion_power", new Block(metal(MapColor.ORANGE)));
		VACUUM_VESSEL = Space.gatedBlock(items, "vacuum_vessel", "fusion_power",
				new VacuumVesselBlock(metal(MapColor.GRAY).luminance(s -> s.get(VacuumVesselBlock.LIT) ? 13 : 0)));
		LITHIUM_BLANKET = Space.gatedBlock(items, "lithium_blanket", "fusion_power", new Block(metal(MapColor.WHITE_GRAY)));
	}

	/** The part that belongs at (dx, dy, dz) from the centre of the ring (each from -2 to 2, dy from -1 to 1). */
	public static Block expected(int dx, int dy, int dz) {
		int ring = Math.max(Math.abs(dx), Math.abs(dz));
		if (ring == 0) {
			return CENTRAL_SOLENOID;
		}
		if (ring == 1) {
			return dy == 0 ? VACUUM_VESSEL : LITHIUM_BLANKET;
		}
		return TOROIDAL_FIELD_COIL;
	}

	/**
	 * The ring round a Tokamak: its centre, the first wrong block (or null when it is whole) and its vessels.
	 * {@code centre} is null when no side of the Tokamak has a Central Solenoid three blocks away.
	 */
	public record Ring(@Nullable BlockPos centre, @Nullable BlockPos missingAt, @Nullable Block missing, List<BlockPos> vessels) {
		public boolean valid() {
			return centre != null && missingAt == null;
		}
	}

	/** Checks the four sides of the Tokamak at {@code pos} and returns the best ring found. */
	public static Ring check(World world, BlockPos pos) {
		Ring best = new Ring(null, null, null, List.of());
		int bestWrong = Integer.MAX_VALUE;
		for (Direction d : Direction.Type.HORIZONTAL) {
			BlockPos c = pos.offset(d, 3);
			if (!world.getBlockState(c).isOf(CENTRAL_SOLENOID)) {
				continue;
			}
			int wrong = 0;
			BlockPos firstAt = null;
			Block first = null;
			List<BlockPos> vessels = new ArrayList<>();
			for (int dy = -1; dy <= 1; dy++) {
				for (int dx = -2; dx <= 2; dx++) {
					for (int dz = -2; dz <= 2; dz++) {
						BlockPos at = c.add(dx, dy, dz);
						Block want = expected(dx, dy, dz);
						if (!world.getBlockState(at).isOf(want)) {
							wrong++;
							if (firstAt == null) {
								firstAt = at;
								first = want;
							}
						} else if (want == VACUUM_VESSEL) {
							vessels.add(at);
						}
					}
				}
			}
			if (wrong < bestWrong) {
				bestWrong = wrong;
				best = new Ring(c, firstAt, first, vessels);
			}
		}
		return best;
	}

	/** Lights or darkens the vessels of a ring. */
	public static void light(World world, Ring ring, boolean lit) {
		for (BlockPos v : ring.vessels()) {
			BlockState s = world.getBlockState(v);
			if (s.isOf(VACUUM_VESSEL) && s.get(VacuumVesselBlock.LIT) != lit) {
				world.setBlockState(v, s.with(VacuumVesselBlock.LIT, lit), 3);
			}
		}
	}
}
