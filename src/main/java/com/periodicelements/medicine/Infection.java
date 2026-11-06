package com.periodicelements.medicine;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

/**
 * Infection (1.8.0), as Guilherme decided: very rare, and survivable untreated 7 times in 10.
 *
 * <p>A hit from a zombie, husk, drowned or zombie villager infects 1 time in {@link #HIT_ODDS}; eating rotten flesh 1
 * time in {@link #FLESH_ODDS}; being Clean halves both. Stage 1 lasts one game day with Weakness and Hunger. When it
 * ends it clears on its own {@link #RECOVERY} of the time; otherwise it becomes Sepsis, 1 damage every 10 seconds
 * until cured or dead. Carbolic soap and aspirin cure stage 1; only sulfa drugs and antibiotics (later releases) cure
 * Sepsis. The game rule periodicElementsInfection turns it all off.
 */
public final class Infection {
	public static final int HIT_ODDS = 200;
	public static final int FLESH_ODDS = 100;
	public static final int STAGE_TICKS = 24000;
	public static final double RECOVERY = 0.7;
	public static final int SEPSIS_INTERVAL = 200;
	public static RegistryEntry<StatusEffect> INFECTION;
	public static RegistryEntry<StatusEffect> SEPSIS;
	public static GameRules.Key<GameRules.BooleanRule> RULE;
	/** For tests: when set, the 70/30 roll at the end of stage 1 uses this instead of chance. */
	public static Boolean forceRecovery;

	private Infection() {
	}

	private static Identifier id(String path) {
		return Identifier.of(PeriodicElements.MOD_ID, path);
	}

	public static void register() {
		INFECTION = Registry.registerReference(Registries.STATUS_EFFECT, id("infection"), new Stage1());
		SEPSIS = Registry.registerReference(Registries.STATUS_EFFECT, id("sepsis"), new Sepsis());
		RULE = GameRuleRegistry.register("periodicElementsInfection", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
			Entity attacker = source.getAttacker();
			if (!blocked && taken > 0 && entity instanceof PlayerEntity player && (attacker instanceof ZombieEntity
					|| attacker instanceof HuskEntity || attacker instanceof DrownedEntity || attacker instanceof ZombieVillagerEntity)) {
				maybeInfect(player, HIT_ODDS);
			}
		});
	}

	public static boolean enabled(ServerWorld world) {
		return world.getGameRules().getBoolean(RULE);
	}

	/** Rolls for an infection at 1 in {@code odds} (Clean halves the chance). */
	public static void maybeInfect(LivingEntity entity, int odds) {
		if (!(entity.getWorld() instanceof ServerWorld world) || !enabled(world)) {
			return;
		}
		if (Medicine.IMMUNITY != null && entity.hasStatusEffect(Medicine.IMMUNITY)) {
			return; // an antibiotic course or the vaccine
		}
		double chance = 1.0 / odds * (entity.hasStatusEffect(com.periodicelements.farm.Farm.CLEAN) ? 0.5 : 1);
		if (entity.getRandom().nextDouble() < chance) {
			infect(entity);
		}
	}

	public static void infect(LivingEntity entity) {
		if (entity.hasStatusEffect(INFECTION) || entity.hasStatusEffect(SEPSIS)) {
			return;
		}
		entity.addStatusEffect(new StatusEffectInstance(INFECTION, STAGE_TICKS));
		if (entity instanceof PlayerEntity p) {
			p.sendMessage(Text.translatable("gui.periodic_elements.infected"), false);
		}
	}

	/** Cures stage 1 (carbolic soap, aspirin and every stronger medicine). Returns whether there was one. */
	public static boolean cureEarly(LivingEntity entity) {
		if (entity.hasStatusEffect(INFECTION)) {
			entity.removeStatusEffect(INFECTION);
			if (entity instanceof PlayerEntity p) {
				p.sendMessage(Text.translatable("gui.periodic_elements.cured"), true);
			}
			return true;
		}
		return false;
	}

	/** Cures stage 1 or Sepsis (sulfa drugs and antibiotics). */
	public static boolean cureAll(LivingEntity entity) {
		boolean had = entity.hasStatusEffect(SEPSIS);
		entity.removeStatusEffect(SEPSIS);
		return cureEarly(entity) || had;
	}

	/** Milk's effect clearing (see MilkBucketItemMixin): removes every effect but Infection and Sepsis. */
	public static boolean clearExceptDisease(LivingEntity entity) {
		boolean any = false;
		for (var e : new java.util.ArrayList<>(entity.getStatusEffects())) {
			if (!e.getEffectType().equals(INFECTION) && !e.getEffectType().equals(SEPSIS)) {
				any |= entity.removeStatusEffect(e.getEffectType());
			}
		}
		return any;
	}

	/** Stage 1: Weakness and Hunger for a game day; at its end, recovery or Sepsis. */
	public static class Stage1 extends StatusEffect {
		Stage1() {
			super(StatusEffectCategory.HARMFUL, 0x7A8A3A);
		}

		@Override
		public boolean canApplyUpdateEffect(int duration, int amplifier) {
			return duration % 40 == 0 || duration <= 1;
		}

		@Override
		public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
			StatusEffectInstance self = entity.getStatusEffect(INFECTION);
			if (self != null && self.getDuration() <= 1) {
				boolean recover = forceRecovery != null ? forceRecovery : entity.getRandom().nextDouble() < RECOVERY;
				if (recover) {
					if (entity instanceof PlayerEntity p) {
						p.sendMessage(Text.translatable("gui.periodic_elements.recovered"), false);
					}
				} else {
					entity.addStatusEffect(new StatusEffectInstance(SEPSIS, StatusEffectInstance.INFINITE));
					if (entity instanceof PlayerEntity p) {
						p.sendMessage(Text.translatable("gui.periodic_elements.sepsis"), false);
					}
				}
				return true;
			}
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, true, false));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 0, true, false));
			return true;
		}
	}

	/** Sepsis: 1 damage every 10 seconds until cured. */
	public static class Sepsis extends StatusEffect {
		Sepsis() {
			super(StatusEffectCategory.HARMFUL, 0x5A1010);
		}

		@Override
		public boolean canApplyUpdateEffect(int duration, int amplifier) {
			return true;
		}

		@Override
		public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
			if (entity.age % SEPSIS_INTERVAL == 0 && entity.getWorld() instanceof ServerWorld world) {
				entity.damage(world.getDamageSources().magic(), 1.0f);
			}
			return true;
		}
	}
}
