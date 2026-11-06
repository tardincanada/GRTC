package com.periodicelements.medicine;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

/** Aspirin (use it) and the ether anaesthetic (use it on a zombie villager being cured). */
public final class Medicine {
	public static Item ASPIRIN;
	public static Item ANAESTHETIC;
	public static Item SULFANILAMIDE;
	/** 2.0.0: antibiotics, the vaccine and the radiation countermeasures. */
	public static Item PENICILLIN;
	public static Item STREPTOMYCIN;
	public static Item VACCINE;
	public static Item POTASSIUM_IODIDE;
	public static Item CAPSULE;
	public static Item AMIFOSTINE;
	public static RegistryEntry<StatusEffect> IMMUNITY;
	public static RegistryEntry<StatusEffect> IODIDE_SHIELD;
	public static RegistryEntry<StatusEffect> CAESIUM_BINDER;
	public static RegistryEntry<StatusEffect> RADIOPROTECTION;
	public static final int ANTIBIOTIC_IMMUNITY = 24000;
	public static final int VACCINE_IMMUNITY = 7 * 24000;
	public static final int COUNTERMEASURE_TICKS = 24000;
	/** Amifostine lasts an hour of real time. */
	public static final int AMIFOSTINE_TICKS = 72000;
	/** Share of the fission products' gamma dose that is radioiodine, and that is radiocaesium. */
	public static final double IODINE_SHARE = 0.4;
	public static final double CAESIUM_SHARE = 0.5;
	/** Prussian blue binds half the caesium. */
	public static final double BINDER_EFFECT = 0.5;
	public static final double RADIOPROTECTION_FACTOR = 0.5;

	/** A plain beneficial effect: what it does is checked where it matters. */
	static final class Boon extends StatusEffect {
		Boon(int color) {
			super(StatusEffectCategory.BENEFICIAL, color);
		}
	}

	private static RegistryEntry<StatusEffect> effect(String name, int color) {
		return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(PeriodicElements.MOD_ID, name), new Boon(color));
	}

	private static Item item(String name) {
		return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, name));
	}

	/** Share of the fission products' dose this player still takes (iodide and Prussian blue cut it). */
	public static double fissionFactor(LivingEntity e) {
		double f = 1;
		if (IODIDE_SHIELD != null && e.hasStatusEffect(IODIDE_SHIELD)) {
			f -= IODINE_SHARE;
		}
		if (CAESIUM_BINDER != null && e.hasStatusEffect(CAESIUM_BINDER)) {
			f -= CAESIUM_SHARE * BINDER_EFFECT;
		}
		return f;
	}

	/** Share of the dose that harms: amifostine halves it. */
	public static double harmFactor(LivingEntity e) {
		return RADIOPROTECTION != null && e.hasStatusEffect(RADIOPROTECTION) ? RADIOPROTECTION_FACTOR : 1;
	}

	/** Uses a 2.0.0 medicine; returns false if the item is not one. */
	public static boolean take(LivingEntity e, Item item) {
		if (item == PENICILLIN || item == STREPTOMYCIN) {
			Infection.cureAll(e);
			e.addStatusEffect(new StatusEffectInstance(IMMUNITY, ANTIBIOTIC_IMMUNITY));
			say(e, "antibiotic");
		} else if (item == VACCINE) {
			e.addStatusEffect(new StatusEffectInstance(IMMUNITY, VACCINE_IMMUNITY));
			say(e, "vaccinated");
		} else if (item == POTASSIUM_IODIDE) {
			e.addStatusEffect(new StatusEffectInstance(IODIDE_SHIELD, COUNTERMEASURE_TICKS));
			say(e, "iodide");
		} else if (item == CAPSULE) {
			e.addStatusEffect(new StatusEffectInstance(CAESIUM_BINDER, COUNTERMEASURE_TICKS));
			say(e, "capsule");
		} else if (item == AMIFOSTINE) {
			e.addStatusEffect(new StatusEffectInstance(RADIOPROTECTION, AMIFOSTINE_TICKS));
			say(e, "amifostine");
		} else {
			return false;
		}
		return true;
	}

	private static void say(LivingEntity e, String key) {
		if (e instanceof PlayerEntity p) {
			p.sendMessage(Text.translatable("gui.periodic_elements." + key), true);
		}
	}

	/** Penicillin on a weakened zombie villager starts its cure, as a golden apple would. */
	public static boolean cureZombie(ZombieVillagerEntity zv, PlayerEntity by) {
		if (zv.isConverting() || !zv.hasStatusEffect(StatusEffects.WEAKNESS)) {
			return false;
		}
		((ZombieVillagerAccess) zv).periodicElements$startCure(by == null ? null : by.getUuid(), zv.getRandom().nextInt(2401) + 3600);
		return true;
	}

	private Medicine() {
	}

	public static void register() {
		ASPIRIN = Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, "aspirin"));
		ANAESTHETIC = Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, "anaesthetic"));
		SULFANILAMIDE = Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, "sulfanilamide"));
		PENICILLIN = item("penicillin");
		STREPTOMYCIN = item("streptomycin");
		VACCINE = item("mrna_vaccine");
		POTASSIUM_IODIDE = item("potassium_iodide");
		CAPSULE = item("prussian_blue_capsule");
		AMIFOSTINE = item("amifostine");
		IMMUNITY = effect("immunity", 0x5AC878);
		IODIDE_SHIELD = effect("iodide_shield", 0xBE78C8);
		CAESIUM_BINDER = effect("caesium_binder", 0x2846A0);
		RADIOPROTECTION = effect("radioprotection", 0xE6C83C);
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			Item it = stack.getItem();
			if (player.isSpectator() || !(it == PENICILLIN || it == STREPTOMYCIN || it == VACCINE || it == POTASSIUM_IODIDE
					|| it == CAPSULE || it == AMIFOSTINE)) {
				return TypedActionResult.pass(stack);
			}
			if (!world.isClient) {
				take(player, it);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5f, 1.3f);
				stack.decrementUnlessCreative(1, player);
			}
			return TypedActionResult.success(stack, world.isClient);
		});
		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(PENICILLIN) || !(entity instanceof ZombieVillagerEntity zv)) {
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				if (cureZombie(zv, player)) {
					stack.decrementUnlessCreative(1, player);
					player.sendMessage(Text.translatable("gui.periodic_elements.zombie_cure"), true);
				} else {
					player.sendMessage(Text.translatable("gui.periodic_elements.zombie_cure.weakness"), true);
				}
			}
			return ActionResult.SUCCESS;
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(SULFANILAMIDE) || player.isSpectator()) {
				return TypedActionResult.pass(stack);
			}
			if (!world.isClient) {
				takeSulfa(player);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5f, 1.2f);
				stack.decrementUnlessCreative(1, player);
			}
			return TypedActionResult.success(stack, world.isClient);
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(ASPIRIN) || player.isSpectator()) {
				return TypedActionResult.pass(stack);
			}
			if (!world.isClient) {
				takeAspirin(player);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5f, 1.4f);
				stack.decrementUnlessCreative(1, player);
			}
			return TypedActionResult.success(stack, world.isClient);
		});
		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(ANAESTHETIC) || !(entity instanceof ZombieVillagerEntity zv)) {
				return ActionResult.PASS;
			}
			if (!world.isClient) {
				if (anaesthetize(zv)) {
					stack.decrementUnlessCreative(1, player);
					player.sendMessage(Text.translatable("gui.periodic_elements.anaesthetic"), true);
				} else {
					player.sendMessage(Text.translatable("gui.periodic_elements.anaesthetic.not_curing"), true);
				}
			}
			return ActionResult.SUCCESS;
		});
	}

	/** Aspirin: clears Nausea and Mining Fatigue, cures an early Infection, and gives Regeneration I for 10 s. */
	public static void takeAspirin(net.minecraft.entity.LivingEntity entity) {
		entity.removeStatusEffect(StatusEffects.NAUSEA);
		entity.removeStatusEffect(StatusEffects.MINING_FATIGUE);
		Infection.cureEarly(entity);
		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
		if (entity instanceof net.minecraft.entity.player.PlayerEntity p) {
			p.sendMessage(Text.translatable("gui.periodic_elements.aspirin"), true);
		}
	}

	/** A sulfa drug (1.9.0): cures Infection at either stage, sepsis included. */
	public static void takeSulfa(net.minecraft.entity.LivingEntity entity) {
		Infection.cureAll(entity);
		if (entity instanceof net.minecraft.entity.player.PlayerEntity p) {
			p.sendMessage(Text.translatable("gui.periodic_elements.sulfa"), true);
		}
	}

	/** Halves what is left of a zombie villager's cure. Returns false if it is not being cured. */
	public static boolean anaesthetize(ZombieVillagerEntity zv) {
		if (!zv.isConverting()) {
			return false;
		}
		ZombieVillagerAccess access = (ZombieVillagerAccess) zv;
		access.periodicElements$setConversionTimer(access.periodicElements$getConversionTimer() / 2);
		return true;
	}
}
