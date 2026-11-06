package com.periodicelements.samplecase;

import com.mojang.serialization.Codec;
import com.periodicelements.ElementItem;
import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import com.periodicelements.SubstanceItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

/**
 * The Sample Case (3.31.0): a leather case with 54 places that holds only elements and substances (any of the mod's
 * element or substance items, flasks of liquids and gases included). Use it to open it; sneak and use it to switch
 * picking up on or off: while on, element and substance items you walk over go into the case first, the rest into the
 * pack as usual. Contents travel with the item (the vanilla container component), so a case keeps them when dropped.
 */
public final class SampleCase {
	public static final int SLOTS = 54;
	public static Item CASE;
	/** Whether the case picks up samples (on unless the player switched it off). */
	public static ComponentType<Boolean> PICKUP;
	public static ScreenHandlerType<SampleCaseScreenHandler> SCREEN;

	private SampleCase() {
	}

	/** What a case takes: the mod's elements and substances. */
	public static boolean accepts(ItemStack stack) {
		return !stack.isEmpty() && (stack.getItem() instanceof ElementItem || stack.getItem() instanceof SubstanceItem);
	}

	public static boolean picksUp(ItemStack caseStack) {
		return caseStack.getOrDefault(PICKUP, true);
	}

	public static DefaultedList<ItemStack> contents(ItemStack caseStack) {
		DefaultedList<ItemStack> list = DefaultedList.ofSize(SLOTS, ItemStack.EMPTY);
		caseStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
		return list;
	}

	public static void store(ItemStack caseStack, List<ItemStack> list) {
		caseStack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(list));
	}

	/**
	 * Puts as much of {@code stack} as fits into the cases in the player's pack that pick up (topping up stacks of the
	 * same item first, then empty places). Shrinks {@code stack}; returns how many items went in.
	 */
	public static int absorb(PlayerEntity player, ItemStack stack) {
		if (!accepts(stack)) {
			return 0;
		}
		int before = stack.getCount();
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size() && !stack.isEmpty(); i++) {
			ItemStack c = inv.getStack(i);
			if (!c.isOf(CASE) || !picksUp(c)) {
				continue;
			}
			DefaultedList<ItemStack> list = contents(c);
			boolean changed = false;
			for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
				for (int k = 0; k < SLOTS && !stack.isEmpty(); k++) {
					ItemStack s = list.get(k);
					if (pass == 0 && !s.isEmpty() && ItemStack.areItemsAndComponentsEqual(s, stack)) {
						int move = Math.min(stack.getCount(), s.getMaxCount() - s.getCount());
						if (move > 0) {
							s.increment(move);
							stack.decrement(move);
							changed = true;
						}
					} else if (pass == 1 && s.isEmpty()) {
						list.set(k, stack.split(Math.min(stack.getCount(), stack.getMaxCount())));
						changed = true;
					}
				}
			}
			if (changed) {
				store(c, list);
			}
		}
		return before - stack.getCount();
	}

	public static class CaseItem extends Item {
		public CaseItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			ItemStack stack = user.getStackInHand(hand);
			if (user.isSneaking()) {
				boolean on = !picksUp(stack);
				stack.set(PICKUP, on);
				if (!world.isClient) {
					user.sendMessage(Text.translatable("message." + PeriodicElements.MOD_ID + ".sample_case." + (on ? "on" : "off")), true);
				}
				return TypedActionResult.success(stack, world.isClient);
			}
			if (user instanceof ServerPlayerEntity sp) {
				int slot = hand == Hand.OFF_HAND ? PlayerInventory.OFF_HAND_SLOT : sp.getInventory().selectedSlot;
				sp.openHandledScreen(new ExtendedScreenHandlerFactory<Integer>() {
					@Override
					public Integer getScreenOpeningData(ServerPlayerEntity player) {
						return slot;
					}

					@Override
					public Text getDisplayName() {
						return stack.getName();
					}

					@Override
					public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
						return new SampleCaseScreenHandler(syncId, inv, slot);
					}
				});
				world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.6f, 1.2f);
			}
			return TypedActionResult.success(stack, world.isClient);
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			// One line per sentence or clause: vanilla tooltips do not wrap.
			for (String part : Text.translatable(getTranslationKey() + ".tip").getString().split("(?<=[.;])\\s+")) {
				String line = part.endsWith(";") ? part.substring(0, part.length() - 1).strip() : part;
				if (!line.isEmpty()) {
					tooltip.add(Text.literal(Character.toUpperCase(line.charAt(0)) + line.substring(1)).formatted(Formatting.GRAY));
				}
			}
			int used = 0;
			for (ItemStack s : contents(stack)) {
				if (!s.isEmpty()) used++;
			}
			tooltip.add(Text.translatable(getTranslationKey() + ".used", used, SLOTS).formatted(Formatting.DARK_AQUA));
			tooltip.add(Text.translatable(getTranslationKey() + (picksUp(stack) ? ".pickup_on" : ".pickup_off"))
					.formatted(picksUp(stack) ? Formatting.GREEN : Formatting.DARK_GRAY));
		}
	}

	public static void register(List<Item> items) {
		PICKUP = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(PeriodicElements.MOD_ID, "sample_case_pickup"),
				ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build());
		CASE = Registry.register(Registries.ITEM, Identifier.of(PeriodicElements.MOD_ID, "sample_case"),
				new CaseItem(new Item.Settings().maxCount(1)));
		items.add(CASE);
		SCREEN = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(PeriodicElements.MOD_ID, "sample_case"),
				new ExtendedScreenHandlerType<>(SampleCaseScreenHandler::new, PacketCodecs.VAR_INT.cast()));
	}
}
