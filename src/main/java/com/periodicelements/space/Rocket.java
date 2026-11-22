package com.periodicelements.space;

import com.periodicelements.atlas.LaunchPadBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A rocket as it stands on a Launch Pad, read from the pad upwards: stages (an engine and the tanks on it), then the
 * Payload Fairing on top. {@link #stages} gives each stage's delta-v by the rocket equation, Tsiolkovsky's
 * dv = ve ln(m0 / m1), with everything above the stage as part of its load.
 */
public final class Rocket {
	public record Stage(Propellant propellant, int bottom, int tanks, int propellantMass, boolean legs, boolean worn,
						boolean mixed, double dry, double dv) {
	}

	/** The pad, the blocks from bottom to top, the stages, the payload, and what is wrong (empty when it can fly). */
	public final BlockPos pad;
	public final List<BlockState> blocks = new ArrayList<>();
	public final List<Stage> stages = new ArrayList<>();
	public ItemStack payload = ItemStack.EMPTY;
	public final List<Text> problems = new ArrayList<>();
	public double totalDv;
	/** 3.64.0: the delta-v this flight needs (orbit, or a crewed flight's destination), and where a crew goes. */
	public double targetDv = Propellant.ORBIT_DV;
	public boolean crew;
	public net.minecraft.registry.RegistryKey<World> destination = World.OVERWORLD;

	/** The name of a world a crew can fly to. */
	public static Text place(net.minecraft.registry.RegistryKey<World> w) {
		return Text.translatable(w == Moon.MOON ? "dimension.periodic_elements.moon" : w == Mars.MARS ? "dimension.periodic_elements.mars"
				: w == Mercury.MERCURY ? "dimension.periodic_elements.mercury" : w == Venus.VENUS ? "dimension.periodic_elements.venus"
				: "dimension.periodic_elements.earth");
	}

	private Rocket(BlockPos pad) {
		this.pad = pad;
	}

	static boolean isRocketBlock(BlockState s) {
		return s.getBlock() instanceof RocketEngineBlock || s.getBlock() instanceof PropellantTankBlock
				|| s.getBlock() instanceof PayloadFairingBlock;
	}

	/** The nearest Launch Pad with an engine on it within 8 blocks of {@code from}, or null. */
	public static BlockPos findPad(World world, BlockPos from) {
		BlockPos best = null;
		double bestD = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.iterate(from.add(-8, -4, -8), from.add(8, 4, 8))) {
			if (world.getBlockState(p).getBlock() instanceof LaunchPadBlock
					&& world.getBlockState(p.up()).getBlock() instanceof RocketEngineBlock) {
				double d = p.getSquaredDistance(from);
				if (d < bestD) {
					bestD = d;
					best = p.toImmutable();
				}
			}
		}
		return best;
	}

	private static Text t(String key, Object... args) {
		return Text.translatable("message.periodic_elements.rocket." + key, args);
	}

	static String km(double ms) {
		return String.format(Locale.ROOT, "%.1f", ms / 1000.0);
	}

	/** Reads the rocket standing on {@code pad}. */
	public static Rocket read(World world, BlockPos pad) {
		Rocket r = new Rocket(pad);
		BlockPos p = pad.up();
		while (r.blocks.size() < 48 && isRocketBlock(world.getBlockState(p))) {
			BlockState s = world.getBlockState(p);
			r.blocks.add(s);
			if (s.getBlock() instanceof PayloadFairingBlock) {
				if (world.getBlockEntity(p) instanceof PayloadFairingBlockEntity f) {
					r.payload = f.payload.copy();
				}
				break;
			}
			p = p.up();
		}
		// stages, bottom up
		List<int[]> spans = new ArrayList<>();
		for (int i = 0; i < r.blocks.size(); i++) {
			if (r.blocks.get(i).getBlock() instanceof RocketEngineBlock) {
				spans.add(new int[]{i, i + 1});
			} else if (r.blocks.get(i).getBlock() instanceof PropellantTankBlock && !spans.isEmpty()) {
				spans.get(spans.size() - 1)[1] = i + 1;
			}
		}
		boolean fairing = !r.blocks.isEmpty() && r.blocks.get(r.blocks.size() - 1).getBlock() instanceof PayloadFairingBlock;
		double[] dry = new double[spans.size()];
		int[] prop = new int[spans.size()];
		List<Stage> raw = new ArrayList<>();
		for (int k = 0; k < spans.size(); k++) {
			int[] sp = spans.get(k);
			BlockState eng = r.blocks.get(sp[0]);
			RocketEngineBlock e = (RocketEngineBlock) eng.getBlock();
			int tanks = sp[1] - sp[0] - 1;
			int mass = 0;
			boolean mixed = false;
			for (int i = sp[0] + 1; i < sp[1]; i++) {
				if (world.getBlockEntity(pad.up(1 + i)) instanceof PropellantTankBlockEntity tank && !tank.empty()) {
					if (tank.propellant != e.propellant) {
						mixed = true;
					} else {
						mass += tank.mass();
					}
				}
			}
			dry[k] = e.propellant.engineDry + (eng.get(RocketEngineBlock.LEGS) ? Propellant.LEGS_DRY : 0)
					+ tanks * Propellant.TANK_DRY;
			prop[k] = mass;
			raw.add(new Stage(e.propellant, sp[0], tanks, mass, eng.get(RocketEngineBlock.LEGS), eng.get(RocketEngineBlock.WORN),
					mixed, dry[k], 0));
		}
		// delta-v: each stage carries everything above it
		double above = (fairing ? Propellant.FAIRING_DRY : 0) + (r.payload.isEmpty() ? 0 : "crew".equals(Space.payloadKind(r.payload)) ? Propellant.CAPSULE : Propellant.PAYLOAD);
		double[] dv = new double[spans.size()];
		for (int k = spans.size() - 1; k >= 0; k--) {
			Stage s = raw.get(k);
			double burn = s.propellantMass() * (k == 0 && s.legs() ? 1 - Propellant.LANDING_RESERVE : 1);
			double m0 = above + dry[k] + prop[k];
			double m1 = m0 - burn;
			dv[k] = m1 > 0 ? s.propellant().exhaustVelocity() * Math.log(m0 / m1) : 0;
			above = m0;
		}
		for (int k = 0; k < raw.size(); k++) {
			Stage s = raw.get(k);
			r.stages.add(new Stage(s.propellant(), s.bottom(), s.tanks(), s.propellantMass(), s.legs(), s.worn(), s.mixed(),
					s.dry(), dv[k]));
			r.totalDv += dv[k];
		}
		r.check(world);
		return r;
	}

	private void check(World world) {
		if (stages.isEmpty()) {
			problems.add(t("no_engine"));
			return;
		}
		if (blocks.get(blocks.size() - 1).getBlock() instanceof PayloadFairingBlock == false) {
			problems.add(t("no_fairing"));
		} else if (payload.isEmpty()) {
			problems.add(t("empty_fairing"));
		}
		for (int k = 0; k < stages.size(); k++) {
			Stage s = stages.get(k);
			if (s.tanks() == 0) {
				problems.add(t("no_tanks", k + 1));
			}
			if (s.tanks() > s.propellant().maxTanks) {
				problems.add(t("too_many", k + 1, s.propellant().maxTanks));
			}
			if (s.mixed()) {
				problems.add(t("mixed", k + 1));
			}
			if (s.worn()) {
				problems.add(t("worn", k + 1));
			}
			if (k == 0 && !s.propellant().liftoff) {
				problems.add(t("no_liftoff", Text.translatable("block.periodic_elements." + s.propellant().engine)));
			}
		}
		BlockPos top = pad.up(blocks.size());
		if (world.getTopY(Heightmap.Type.MOTION_BLOCKING, top.getX(), top.getZ()) > top.getY() + 1) {
			problems.add(t("sky"));
		}
		crew = "crew".equals(Space.payloadKind(payload));
		if (!crew && world.getRegistryKey() != World.OVERWORLD) {
			problems.add(t("overworld"));
		}
		if (crew) {
			destination = Moon.destination(world, payload);
			targetDv = destination == Moon.MOON ? Propellant.MOON_DV : destination == Mars.MARS ? Propellant.MARS_DV
					: destination == Mercury.MERCURY ? Propellant.MERCURY_DV : destination == Venus.VENUS ? Propellant.VENUS_DV
					: Mars.is(world) ? Propellant.MARS_RETURN_DV : Mercury.is(world) ? Propellant.MERCURY_RETURN_DV
					: Venus.is(world) ? Propellant.VENUS_RETURN_DV : Propellant.RETURN_DV;
		}
		if (problems.isEmpty() && totalDv < targetDv) {
			problems.add(crew ? t("short_to", km(targetDv - totalDv), km(totalDv), km(targetDv), place(destination))
					: t("short", km(Propellant.ORBIT_DV - totalDv), km(totalDv), km(Propellant.ORBIT_DV)));
		}
	}

	public boolean ready() {
		return problems.isEmpty();
	}

	/** Whether the first stage will fly back to the pad. */
	public boolean reusable() {
		return !stages.isEmpty() && stages.get(0).legs();
	}

	/** One line per stage, then the total. */
	public List<Text> report() {
		List<Text> out = new ArrayList<>();
		for (int k = 0; k < stages.size(); k++) {
			Stage s = stages.get(k);
			out.add(t(s.legs() && k == 0 ? "stage_legs" : "stage", k + 1,
					Text.translatable("block.periodic_elements." + s.propellant().engine), s.tanks(), s.propellantMass(), km(s.dv())));
		}
		out.add(crew ? t("total_to", km(totalDv), km(targetDv), place(destination)) : t("total", km(totalDv), km(Propellant.ORBIT_DV)));
		return out;
	}
}
