package com.periodicelements.atlas;

import com.periodicelements.ModContent;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Set;

/** A Survey Balloon (Hot-Air Balloon, 1783): one ascent draws 128 blocks all round on the Atlas. Used up. */
public class SurveyBalloonItem extends ModContent.TippedItem {
	public SurveyBalloonItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient || !(user instanceof ServerPlayerEntity sp)) {
			return TypedActionResult.success(stack, world.isClient);
		}
		return fly(sp, stack) >= 0 ? TypedActionResult.consume(stack) : TypedActionResult.fail(stack);
	}

	/** Returns the columns drawn, or -1 if the player may not fly it here. */
	public static int fly(ServerPlayerEntity player, ItemStack stack) {
		Set<String> known = ResearchAccess.known(player);
		if (!player.getAbilities().creativeMode && (!Research.allows(known, "item:survey_balloon") || !Atlas.canDraw(known))) {
			player.sendMessage(Text.translatable("gui.periodic_elements.atlas.balloon.needs").formatted(Formatting.RED), true);
			return -1;
		}
		if (player.getWorld().getRegistryKey() != World.OVERWORLD) {
			return -1;
		}
		int n = Atlas.record(player.getServerWorld(), AtlasData.of(player.getServer(), player.getUuid()),
				player.getBlockX(), player.getBlockZ(), Atlas.BALLOON_REACH, Atlas.has(known, "geology"));
		if (!player.getAbilities().creativeMode) {
			stack.decrement(1);
		}
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100));
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1f, 0.8f);
		player.sendMessage(Text.translatable("gui.periodic_elements.atlas.balloon", n), true);
		return n;
	}
}
