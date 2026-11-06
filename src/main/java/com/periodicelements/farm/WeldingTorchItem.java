package com.periodicelements.farm;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Welding Torch (1.9.0): acetylene burnt in oxygen. With a damaged metal tool or piece of armour in the other hand,
 * using it melts one ingot of that metal from the pack into the damage and mends {@link #MEND} of the item, with no
 * experience. Its cylinders last {@link #WELDS} welds; crafting it again with fresh ones refills it.
 */
public class WeldingTorchItem extends Item {
	public static final int WELDS = 64;
	public static final double MEND = 0.25;

	public WeldingTorchItem(Settings settings) {
		super(settings);
	}

	/** An ingot that repairs this item (the item's own repair material, if that is an ingot). */
	public static int findIngot(PlayerEntity user, ItemStack damaged) {
		for (int i = 0; i < user.getInventory().size(); i++) {
			ItemStack s = user.getInventory().getStack(i);
			if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).getPath().contains("ingot")
					&& damaged.getItem().canRepair(damaged, s)) {
				return i;
			}
		}
		return -1;
	}

	/** Welds once: returns true if the item was mended. */
	public static boolean weld(PlayerEntity user, ItemStack torch, ItemStack damaged) {
		int slot = findIngot(user, damaged);
		if (slot < 0) {
			return false;
		}
		ItemStack ingot = user.getInventory().getStack(slot);
		user.sendMessage(Text.translatable("gui.periodic_elements.weld.mended", damaged.getName(), ingot.getName()), true);
		ingot.decrementUnlessCreative(1, user);
		damaged.setDamage(Math.max(0, damaged.getDamage() - (int) Math.ceil(damaged.getMaxDamage() * MEND)));
		if (!user.getAbilities().creativeMode) {
			torch.setDamage(torch.getDamage() + 1);
		}
		return true;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack torch = user.getStackInHand(hand);
		ItemStack other = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if (world.isClient) {
			return TypedActionResult.consume(torch);
		}
		if (torch.getDamage() >= torch.getMaxDamage() - 1) {
			user.sendMessage(Text.translatable("gui.periodic_elements.weld.empty"), true);
			return TypedActionResult.fail(torch);
		}
		if (other.isEmpty() || !other.isDamageable() || other.getDamage() == 0) {
			user.sendMessage(Text.translatable("gui.periodic_elements.weld.nothing"), true);
			return TypedActionResult.fail(torch);
		}
		if (!weld(user, torch, other)) {
			user.sendMessage(Text.translatable("gui.periodic_elements.weld.no_ingot"), true);
			return TypedActionResult.fail(torch);
		}
		world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.6f, 1.8f);
		var at = user.getEyePos().add(user.getRotationVector().multiply(0.8));
		((ServerWorld) world).spawnParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y - 0.3, at.z, 12, 0.15, 0.15, 0.15, 0.05);
		user.getItemCooldownManager().set(this, 10);
		return TypedActionResult.success(torch, false);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
