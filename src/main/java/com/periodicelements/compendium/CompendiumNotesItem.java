package com.periodicelements.compendium;

import com.periodicelements.ModContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Compendium Notes: use them to add every tree and biome in them to your own compendium. Used up, like a treatise. */
public class CompendiumNotesItem extends ModContent.TippedItem {
	public CompendiumNotesItem(Settings settings) {
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

	/** Adds the notes to the reader's compendium and uses them up; returns the entries gained, or -1 if blank. */
	public static int read(ServerPlayerEntity player, ItemStack stack) {
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		NbtCompound tag = data == null ? null : data.copyNbt();
		if (tag == null || !tag.contains("notes")) {
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.blank"), true);
			return -1;
		}
		NbtList keys = tag.getList("notes", NbtElement.STRING_TYPE);
		long[] pos = tag.getLongArray("pos");
		DiscoveryState s = DiscoveryState.get(player.getServer());
		int gained = 0;
		for (int i = 0; i < keys.size(); i++) {
			if (s.add(player.getUuid(), keys.getString(i), i < pos.length ? pos[i] : player.getBlockPos().asLong())) gained++;
		}
		stack.decrement(1);
		Compendium.sync(player);
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 1f);
		player.sendMessage(Text.translatable("gui.periodic_elements.compendium.merged", gained), true);
		return gained;
	}
}
