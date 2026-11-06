package com.periodicelements.compendium;

import com.periodicelements.ModContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Map;

/**
 * The Tree or Plant Compendium ({@code kind} "tree" or "plant"). Use: opens the book (client). Sneak and use with a
 * book in the other hand: Compendium Notes of everything noted, in both books.
 */
public class TreeCompendiumItem extends ModContent.TippedItem {
	/** Set by the client initializer (opens the book of that kind); does nothing on a dedicated server. */
	public static java.util.function.Consumer<String> open = kind -> {
	};

	public final String kind;

	public TreeCompendiumItem(Settings settings, String kind) {
		super(settings);
		this.kind = kind;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.isSneaking()) {
			if (!world.isClient && user instanceof ServerPlayerEntity sp) {
				makeNotes(sp, user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
			}
			return TypedActionResult.success(stack, world.isClient);
		}
		if (world.isClient) {
			open.accept(kind);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	/** Uses up the book in {@code book} for Compendium Notes of all the player's notes; returns them, or empty. */
	public static ItemStack makeNotes(ServerPlayerEntity player, ItemStack book) {
		if (!book.isOf(Items.BOOK)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.compendium.copy_book").formatted(Formatting.YELLOW), true);
			return ItemStack.EMPTY;
		}
		Map<String, Long> notes = DiscoveryState.get(player.getServer()).of(player.getUuid());
		NbtList keys = new NbtList();
		long[] pos = new long[notes.size()];
		int i = 0;
		for (Map.Entry<String, Long> e : notes.entrySet()) {
			keys.add(NbtString.of(e.getKey()));
			pos[i++] = e.getValue();
		}
		NbtCompound tag = new NbtCompound();
		tag.put("notes", keys);
		tag.putLongArray("pos", pos);
		ItemStack out = new ItemStack(Compendium.COMPENDIUM_NOTES);
		out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
		if (!player.getAbilities().creativeMode) {
			book.decrement(1);
		}
		ItemStack made = out.copy();
		if (!player.getInventory().insertStack(out)) {
			player.dropItem(out, false);
		}
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 1f);
		player.sendMessage(Text.translatable("gui.periodic_elements.compendium.copied", notes.size()), true);
		return made;
	}
}
