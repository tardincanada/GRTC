package com.periodicelements.atlas;

import com.periodicelements.ModContent;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Set;

/** An Atlas Copy: use it to add everything drawn in it to your own Atlas. It is used up, like a treatise. */
public class AtlasCopyItem extends ModContent.TippedItem {
	public AtlasCopyItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient || !(user instanceof ServerPlayerEntity sp)) {
			return TypedActionResult.success(stack, world.isClient);
		}
		return read(sp, stack) >= 0 ? TypedActionResult.consume(stack) : TypedActionResult.fail(stack);
	}

	/** Merges the copy into the reader's atlas and uses it up; returns the columns gained, or -1 if it could not. */
	public static int read(ServerPlayerEntity player, ItemStack stack) {
		Set<String> known = ResearchAccess.known(player);
		if (!Atlas.canDraw(known)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.atlas.needs").formatted(Formatting.RED), true);
			return -1;
		}
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		NbtCompound tag = data == null ? null : data.copyNbt();
		if (tag == null || !tag.containsUuid("copy")) {
			player.sendMessage(Text.translatable("gui.periodic_elements.atlas.copy.blank"), true);
			return -1;
		}
		AtlasData copy = AtlasData.copy(player.getServer(), tag.getUuid("copy"));
		if (copy.tiles.isEmpty() && copy.markers.isEmpty() && copy.chunks.isEmpty()) {
			player.sendMessage(Text.translatable("gui.periodic_elements.atlas.copy.blank"), true);
			stack.decrement(1);
			return 0;
		}
		AtlasData mine = AtlasData.of(player.getServer(), player.getUuid());
		int gained = mine.mergeFrom(copy, Atlas.markerLimit(known));
		// The copy's sheets are now in the reader's atlas: empty its file so it does not linger in the save.
		copy.tiles.clear();
		copy.markers.clear();
		copy.chunks.clear();
		copy.markDirty();
		stack.decrement(1);
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 1f);
		player.sendMessage(Text.translatable("gui.periodic_elements.atlas.merged", gained), true);
		Atlas.sendMarkers(player, mine);
		return gained;
	}
}
