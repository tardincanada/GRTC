package com.periodicelements.shrubs;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

/**
 * A tea (3.22.0, Guilherme): drunk, it gives a mild effect tied to its plant and cures one bad effect, as milk cures
 * them all. Two leaves or flowers and a water bottle make one; the bottle comes back.
 */
public class TeaItem extends Item {
	private final RegistryEntry<StatusEffect> effect;
	private final int ticks;
	private final RegistryEntry<StatusEffect> cures;

	public TeaItem(RegistryEntry<StatusEffect> effect, int ticks, RegistryEntry<StatusEffect> cures, Settings settings) {
		super(settings);
		this.effect = effect;
		this.ticks = ticks;
		this.cures = cures;
	}

	public RegistryEntry<StatusEffect> effect() {
		return effect;
	}

	public RegistryEntry<StatusEffect> cures() {
		return cures;
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		ItemStack rest = super.finishUsing(stack, world, user);
		if (!world.isClient) {
			user.removeStatusEffect(cures);
			user.addStatusEffect(new StatusEffectInstance(effect, ticks, 0));
		}
		return rest;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public SoundEvent getEatSound() {
		return SoundEvents.ENTITY_GENERIC_DRINK;
	}

	@Override
	public SoundEvent getDrinkSound() {
		return SoundEvents.ENTITY_GENERIC_DRINK;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		tooltip.add(Text.translatable(effect.value().getTranslationKey()).append(" (" + ticks / 20 + " s)").formatted(Formatting.BLUE));
		tooltip.add(Text.translatable("gui.periodic_elements.tea.cures", Text.translatable(cures.value().getTranslationKey()))
				.formatted(Formatting.GRAY));
	}

	static Item.Settings settings() {
		return new Item.Settings().maxCount(16).food(new net.minecraft.component.type.FoodComponent.Builder()
				.nutrition(1).saturationModifier(0.2f).alwaysEdible().usingConvertsTo(Items.GLASS_BOTTLE).build());
	}
}
