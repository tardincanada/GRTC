package com.periodicelements.research;

import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * A treatise: written at a research station, it records every project its author knew.
 * Using it teaches the reader all of them at once and consumes it. The author keeps their knowledge.
 */
public class TreatiseItem extends Item {
	private static final int SHORT_LIST = 8;

	public TreatiseItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		TreatiseContents contents = stack.get(ModContent.TREATISE_CONTENTS);
		if (contents == null || contents.projects().isEmpty()) {
			if (!world.isClient) {
				user.sendMessage(Text.translatable("message.periodic_elements.treatise.blank"), true);
			}
			return TypedActionResult.fail(stack);
		}
		if (user instanceof ServerPlayerEntity player) {
			int added = ResearchState.get(player.getServer()).grant(player.getServer(), player.getUuid(), contents.projects());
			if (added == 0) {
				player.sendMessage(Text.translatable("message.periodic_elements.treatise.nothing_new", contents.author()), true);
				return TypedActionResult.fail(stack);
			}
			player.sendMessage(Text.translatable("message.periodic_elements.treatise.learned", added, contents.author())
					.formatted(Formatting.GREEN), false);
			world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 1f);
			stack.decrementUnlessCreative(1, player);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		String ns = PeriodicElements.MOD_ID;
		TreatiseContents contents = stack.get(ModContent.TREATISE_CONTENTS);
		if (contents == null) {
			tooltip.add(Text.translatable("tooltip." + ns + ".treatise.blank").formatted(Formatting.GRAY));
			return;
		}
		tooltip.add(Text.translatable("tooltip." + ns + ".treatise.author", contents.author()).formatted(Formatting.GOLD));
		tooltip.add(Text.translatable("tooltip." + ns + ".treatise.count", contents.projects().size(), contents.level())
				.formatted(Formatting.GRAY));
		boolean all = contents.projects().size() <= SHORT_LIST + 2;
		int shown = all ? contents.projects().size() : Math.min(SHORT_LIST, contents.projects().size());
		for (int i = 0; i < shown; i++) {
			Research.Project p = Research.get(contents.projects().get(i));
			if (p != null) {
				tooltip.add(Text.literal(" ").append(p.name()).formatted(p.key() ? Formatting.AQUA : Formatting.DARK_AQUA));
			}
		}
		if (shown < contents.projects().size()) {
			tooltip.add(Text.translatable("tooltip." + ns + ".treatise.more", contents.projects().size() - shown)
					.formatted(Formatting.DARK_GRAY));
		}
		tooltip.add(Text.translatable("tooltip." + ns + ".treatise.use").formatted(Formatting.DARK_GRAY));
	}
}
