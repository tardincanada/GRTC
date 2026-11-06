package com.periodicelements.research;

import com.periodicelements.PeriodicElements;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A block item that can only be placed by a player whose research allows it: a machine needs the project that
 * unlocks it, a research station needs the tech level below its own. Creative players can always place it.
 */
public class GatedBlockItem extends BlockItem {
	private final Predicate<Set<String>> allowed;
	private final Text requirement;

	public GatedBlockItem(Block block, Settings settings, Predicate<Set<String>> allowed, Text requirement) {
		super(block, settings);
		this.allowed = allowed;
		this.requirement = requirement;
	}

	@Override
	public ActionResult place(ItemPlacementContext context) {
		PlayerEntity player = context.getPlayer();
		if (player != null && !player.isCreative() && !allowed.test(ResearchAccess.known(player))) {
			if (!context.getWorld().isClient) {
				player.sendMessage(Text.translatable("message." + PeriodicElements.MOD_ID + ".needs_research", requirement)
						.formatted(Formatting.RED), true);
			}
			return ActionResult.FAIL;
		}
		return super.place(context);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getBlock().getTranslationKey() + ".desc").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip." + PeriodicElements.MOD_ID + ".requires", requirement).formatted(Formatting.DARK_AQUA));
		super.appendTooltip(stack, context, tooltip, type);
	}
}
