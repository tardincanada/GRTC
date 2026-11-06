package com.periodicelements.farm;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Carbide Lamp (1.9.0): water drips on calcium carbide and the acetylene burns in front of a reflector. Held in
 * either hand or worn on the head it gives light 14 for {@link #BURN_SECONDS} seconds; using it with calcium carbide
 * in the pack refills it. An empty lamp stays whole and dark.
 */
public class CarbideLampItem extends Item implements Equipment {
	public static final int BURN_SECONDS = 1800;

	public CarbideLampItem(Settings settings) {
		super(settings);
	}

	public static boolean lit(ItemStack s) {
		return s.isOf(Industry.CARBIDE_LAMP) && s.getDamage() < s.getMaxDamage() - 1;
	}

	@Override
	public EquipmentSlot getSlotType() {
		return EquipmentSlot.HEAD;
	}

	/** Puts one calcium carbide from the pack into the lamp; false if it is full or there is no carbide. */
	public static boolean refill(PlayerEntity user, ItemStack lamp) {
		if (lamp.getDamage() == 0) {
			return false;
		}
		for (int i = 0; i < user.getInventory().size(); i++) {
			ItemStack s = user.getInventory().getStack(i);
			if (s.isOf(Industry.CALCIUM_CARBIDE)) {
				s.decrementUnlessCreative(1, user);
				lamp.setDamage(0);
				return true;
			}
		}
		return false;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (stack.getDamage() > 0) {
			if (!world.isClient) {
				if (refill(user, stack)) {
					world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 0.6f, 1.4f);
					user.sendMessage(Text.translatable("gui.periodic_elements.lamp.refilled"), true);
				} else if (!lit(stack)) {
					user.sendMessage(Text.translatable("gui.periodic_elements.lamp.empty"), true);
					return TypedActionResult.fail(stack);
				} else {
					return equipAndSwap(this, world, user, hand);
				}
			}
			return TypedActionResult.success(stack, world.isClient);
		}
		return equipAndSwap(this, world, user, hand);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
