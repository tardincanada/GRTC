package com.periodicelements.explore;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Electric gear worn on the head (3.47.0). It runs on the battery packs in the wearer's pack: the Electric Cap Lamp
 * (a tungsten bulb, light 15 that follows you), the LED Spotlight Helmet (also lights the spot you look at, up to
 * {@link Explore#SPOT_REACH} blocks) and the Night Vision Goggles (night vision, and monsters outlined).
 */
public class HeadGearItem extends Item implements Equipment {
	public enum Kind {
		CAP_LAMP(Explore.CAP_LAMP_KJ), SPOTLIGHT(Explore.SPOTLIGHT_KJ), GOGGLES(Explore.GOGGLES_KJ);

		private final double kJ;

		Kind(double kJ) {
			this.kJ = kJ;
		}

		/** Energy per second, in kJ. */
		public double kJ() {
			return kJ;
		}
	}

	private final Kind kind;

	public HeadGearItem(Settings settings, Kind kind) {
		super(settings);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	@Override
	public EquipmentSlot getSlotType() {
		return EquipmentSlot.HEAD;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return equipAndSwap(this, world, user, hand);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.periodic_elements.head_gear.draw", String.format(java.util.Locale.ROOT, "%.2f", kind.kJ()))
				.formatted(Formatting.DARK_AQUA));
		if (Explore.powered(stack)) {
			tooltip.add(Text.translatable("item.periodic_elements.head_gear.on").formatted(Formatting.GREEN));
		}
	}
}
