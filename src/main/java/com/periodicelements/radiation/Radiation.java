package com.periodicelements.radiation;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import com.periodicelements.ModContent;
import com.periodicelements.research.ResearchState;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

/**
 * Radiation dose from gamma emitters, in real time and at real rates.
 *
 * <p>Every {@code intervalTicks} each player takes the dose rate from radioactive items they carry (at
 * {@code carriedDistance}), and from radioactive blocks and the contents of containers within {@code blockRadius}
 * (falling off with the square of the distance). Effects follow the acute radiation syndrome bands of the dose not
 * yet repaired: 1 Sv nausea; 2 Sv weakness and lower health; 6 Sv hunger and worse; 10 Sv fatal within minutes.
 * The gamerule {@code periodicRadiation} chooses realistic (default), milder (a tenth) or off.
 */
public final class Radiation {
	public enum Mode { realistic, milder, off }

	public static GameRules.Key<EnumRule<Mode>> RULE;
	public static final Identifier DOSE_STAT = Identifier.of(PeriodicElements.MOD_ID, "radiation_dose");
	private static final Identifier HEALTH = Identifier.of(PeriodicElements.MOD_ID, "radiation_sickness");
	/** Acute dose bands in Sv. */
	static final double[] BANDS = {1, 2, 6, 10};

	private Radiation() {
	}

	public static void register() {
		Radioactivity.load();
		RULE = GameRuleRegistry.register("periodicRadiation", GameRules.Category.PLAYER,
				GameRuleFactory.createEnumRule(Mode.realistic));
		Registry.register(Registries.CUSTOM_STAT, DOSE_STAT, DOSE_STAT);
		Stats.CUSTOM.getOrCreateStat(DOSE_STAT, StatFormatter.DEFAULT);
		ServerTickEvents.END_SERVER_TICK.register(Radiation::tick);
	}

	public static Mode mode(MinecraftServer server) {
		return server.getGameRules().get(RULE).get();
	}

	private static void tick(MinecraftServer server) {
		int interval = (int) Radioactivity.setting("intervalTicks");
		if (server.getTicks() % interval != 0) {
			return;
		}
		Mode mode = mode(server);
		long now = System.currentTimeMillis();
		RadiationState state = RadiationState.get(server);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			RadiationState.Dose d = state.dose(player.getUuid(), now);
			if (mode != Mode.off && !player.isCreative() && !player.isSpectator()
					&& !com.periodicelements.creative.CreativeItems.shielded(player)) {
				double rate = doseRate(player) * (mode == Mode.milder ? Radioactivity.setting("milderFactor") : 1);
				double sv = rate * interval / 20.0 / 3600.0 * com.periodicelements.medicine.Medicine.harmFactor(player);
				if (sv > 0) {
					state.add(player.getUuid(), sv, now);
					d.statRemainder += sv * 1e6;
					int whole = (int) Math.floor(d.statRemainder);
					if (whole > 0) {
						player.increaseStat(DOSE_STAT, whole);
						d.statRemainder -= whole;
					}
				}
			}
			applyEffects(player, mode == Mode.off ? 0 : d.recent, d, interval);
			geiger(player, mode);
		}
	}

	/** Sv/h this player receives right now (before the gamerule's factor). */
	public static double doseRate(ServerPlayerEntity player) {
		double carried = 0;
		PlayerInventory inv = player.getInventory();
		// Potassium iodide and Prussian blue cut the share of the dose that comes from fission products.
		double cut = 1 - com.periodicelements.medicine.Medicine.fissionFactor(player);
		for (int i = 0; i < inv.size(); i++) {
			carried += Radioactivity.of(inv.getStack(i)) - cut * Radioactivity.fissionOf(inv.getStack(i));
		}
		double near = Radioactivity.setting("carriedDistance");
		double rate = carried / (near * near);
		rate += surroundings(player.getServerWorld(), player.getPos().add(0, 1, 0));
		return rate;
	}

	// ---- 3.13.0: a reactor that shuts down hot leaks radiation round it for a while. ----

	private record Leak(double strength, long until) {
	}

	private static final java.util.Map<net.minecraft.registry.RegistryKey<net.minecraft.world.World>, java.util.Map<BlockPos, Leak>> LEAKS =
			new java.util.HashMap<>();

	/** {@code strength} Sv/h at one block from {@code pos}, falling off with distance squared, for {@code ticks}. */
	public static synchronized void leak(ServerWorld world, BlockPos pos, double strength, int ticks) {
		LEAKS.computeIfAbsent(world.getRegistryKey(), k -> new java.util.HashMap<>())
				.put(pos.toImmutable(), new Leak(strength, world.getTime() + ticks));
	}

	/** Sv/h at {@code at} from leaking reactors (within 32 blocks). */
	public static synchronized double leaks(ServerWorld world, Vec3d at) {
		var m = LEAKS.get(world.getRegistryKey());
		if (m == null || m.isEmpty()) {
			return 0;
		}
		long now = world.getTime();
		m.values().removeIf(l -> l.until() < now);
		double rate = 0;
		for (var e : m.entrySet()) {
			double d2 = at.squaredDistanceTo(Vec3d.ofCenter(e.getKey()));
			if (d2 < 32 * 32) {
				rate += e.getValue().strength() / Math.max(1, d2);
			}
		}
		return rate;
	}

	/** Sv/h at {@code at} from radioactive blocks and stored items within the block radius. */
	public static double surroundings(ServerWorld world, Vec3d at) {
		int r = (int) Radioactivity.setting("blockRadius");
		BlockPos centre = BlockPos.ofFloored(at);
		double rate = leaks(world, at);
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					p.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
					BlockState s = world.getBlockState(p);
					double source = Radioactivity.of(s.getBlock());
					if (s.hasBlockEntity() && !Radioactivity.shielded(s.getBlock()) && world.getBlockEntity(p) instanceof Inventory inv) {
						for (int i = 0; i < inv.size(); i++) {
							source += Radioactivity.of(inv.getStack(i));
						}
					}
					if (source > 0) {
						double d2 = Math.max(0.25, at.squaredDistanceTo(Vec3d.ofCenter(p)));
						rate += source / d2;
					}
				}
			}
		}
		return rate;
	}

	/** Like {@link #surroundings}, but split by the radioactive element each part comes from (gamma spectrometry). */
	public static java.util.Map<String, Double> surroundingsByElement(ServerWorld world, Vec3d at) {
		int r = (int) Radioactivity.setting("blockRadius");
		BlockPos centre = BlockPos.ofFloored(at);
		java.util.Map<String, Double> out = new java.util.HashMap<>();
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					p.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
					BlockState s = world.getBlockState(p);
					java.util.Map<String, Double> src = new java.util.HashMap<>(Radioactivity.sources(
							net.minecraft.registry.Registries.BLOCK.getId(s.getBlock()).toString()));
					if (s.hasBlockEntity() && !Radioactivity.shielded(s.getBlock()) && world.getBlockEntity(p) instanceof Inventory inv) {
						for (int i = 0; i < inv.size(); i++) {
							ItemStack st = inv.getStack(i);
							if (st.isEmpty()) continue;
							int n = st.getCount();
							Radioactivity.sources(net.minecraft.registry.Registries.ITEM.getId(st.getItem()).toString())
									.forEach((k, v) -> src.merge(k, v * n, Double::sum));
						}
					}
					if (src.isEmpty()) continue;
					double d2 = Math.max(0.25, at.squaredDistanceTo(Vec3d.ofCenter(p)));
					src.forEach((k, v) -> out.merge(k, v / d2, Double::sum));
				}
			}
		}
		return out;
	}

	/** A Geiger counter in either hand reads out by itself, and clicks faster the stronger the field. */
	private static void geiger(ServerPlayerEntity player, Mode mode) {
		ItemStack held = player.getMainHandStack().isOf(ModContent.GEIGER_COUNTER) ? player.getMainHandStack()
				: player.getOffHandStack();
		if (mode == Mode.off || !held.isOf(ModContent.GEIGER_COUNTER) || !ResearchState.get(player.getServer())
				.known(player.getUuid()).contains("geiger_counter")) {
			return;
		}
		ElectroscopeItem.report(player, mode);
		double rate = doseRate(player);
		// Background is about 0.1 µSv/h; each tenfold step above it adds a click.
		int clicks = rate <= 1e-7 ? 0 : (int) Math.min(6, Math.ceil(Math.log10(rate / 1e-7)));
		for (int i = 0; i < clicks; i++) {
			player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), SoundCategory.PLAYERS, 0.4f,
					1.6f + 0.1f * player.getRandom().nextFloat());
		}
	}

	private static void applyEffects(ServerPlayerEntity player, double recent, RadiationState.Dose d, int interval) {
		int band = 0;
		while (band < BANDS.length && recent >= BANDS[band]) {
			band++;
		}
		if (band > d.band) {
			player.sendMessage(Text.translatable("message.periodic_elements.radiation.sick", Radioactivity.format(recent))
					.formatted(Formatting.RED), false);
		}
		d.band = band;
		int duration = interval + 20;
		if (band >= 1 && player.getRandom().nextFloat() < 0.05f) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 240, 0, false, false, true));
		}
		if (band >= 2) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, duration, band >= 3 ? 1 : 0, false, false, true));
		}
		if (band >= 3) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, duration, 0, false, false, true));
		}
		if (band >= 4) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, duration, 1, false, false, true));
		}
		EntityAttributeInstance health = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (health != null) {
			double loss = band >= 3 ? 12 : band >= 2 ? Math.min(10, Math.floor((recent - 1) * 2)) : 0;
			EntityAttributeModifier current = health.getModifier(HEALTH);
			if (current == null || current.value() != -loss) {
				health.removeModifier(HEALTH);
				if (loss > 0) {
					health.addTemporaryModifier(new EntityAttributeModifier(HEALTH, -loss, EntityAttributeModifier.Operation.ADD_VALUE));
				}
				if (player.getHealth() > player.getMaxHealth()) {
					player.setHealth(player.getMaxHealth());
				}
			}
		}
	}
}
