package com.periodicelements;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** The Field Guide: opens the chaptered guide on the client (see client.FieldGuideScreen). */
public class FieldGuideItem extends ModContent.TippedItem {
	/** Set by the client initializer; does nothing on a dedicated server. */
	public static Runnable open = () -> {
	};

	public FieldGuideItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (world.isClient) {
			open.run();
		}
		return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
	}
}
