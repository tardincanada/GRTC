package com.periodicelements.mixin;

import com.periodicelements.atlas.Atlas;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;

/** The vanilla map and compass come out of the crafting grid only for a player who researched them (Atlas, 2.7.0). */
@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin extends Slot {
	private CraftingResultSlotMixin(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean canTakeItems(PlayerEntity player) {
		String gate = Atlas.vanillaGate(getStack());
		if (gate == null) {
			gate = com.periodicelements.explore.Explore.gate(getStack());  // 3.47.0 exploration gear
		}
		if (gate != null && !player.getAbilities().creativeMode && !Research.allows(ResearchAccess.known(player), gate)) {
			if (!player.getWorld().isClient) {
				player.sendMessage(Text.translatable("gui.periodic_elements.atlas.vanilla_locked",
						Research.get(Research.unlockedBy(gate)).name()).formatted(Formatting.RED), true);
			}
			return false;
		}
		return super.canTakeItems(player);
	}
}
