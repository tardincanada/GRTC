package com.periodicelements.explore;

import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
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
 * Exploration backpacks (3.47.0): the Leather Backpack (27 places) and the Nylon Backpack (54). They hold anything but
 * other backpacks and shulker boxes. Use one to open it, or press the backpack key (B) with one anywhere in your
 * pack; sneak and use it to switch picking up. While on, specimens you walk over go into it first: plants (saplings,
 * seeds, flowers, leaves, mushrooms) and minerals (ores, raw ores, gems).
 */
public class BackpackItem extends Item {
	private static final TagKey<Item> C_ORES = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "ores"));
	private static final TagKey<Item> C_RAW = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "raw_materials"));
	private static final TagKey<Item> C_GEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "gems"));
	private static final TagKey<Item> C_SEEDS = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "seeds"));

	private final int rows;

	public BackpackItem(Settings settings, int rows) {
		super(settings);
		this.rows = rows;
	}

	public int rows() {
		return rows;
	}

	public int slots() {
		return rows * 9;
	}

	/** What a backpack takes: anything but a backpack or a shulker box. */
	public static boolean accepts(ItemStack stack) {
		return !stack.isEmpty() && !(stack.getItem() instanceof BackpackItem)
				&& !(stack.getItem() instanceof BlockItem b && b.getBlock() instanceof ShulkerBoxBlock);
	}

	/** A plant or mineral specimen, which a backpack picks up. */
	public static boolean isSpecimen(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.isIn(ItemTags.SAPLINGS) || stack.isIn(ItemTags.FLOWERS) || stack.isIn(ItemTags.LEAVES) || stack.isIn(C_SEEDS)
				|| stack.isIn(C_ORES) || stack.isIn(C_RAW) || stack.isIn(C_GEMS)) {
			return true;
		}
		String path = Registries.ITEM.getId(stack.getItem()).getPath();
		return path.endsWith("seeds") || path.endsWith("_sapling") || path.endsWith("_propagule") || path.endsWith("mushroom")
				|| path.endsWith("_ore") || path.startsWith("raw_") || path.endsWith("_crystal") || path.equals("amethyst_shard");
	}

	public static boolean picksUp(ItemStack pack) {
		return pack.getOrDefault(Explore.PICKUP, true);
	}

	public static DefaultedList<ItemStack> contents(ItemStack pack) {
		int n = pack.getItem() instanceof BackpackItem b ? b.slots() : 0;
		DefaultedList<ItemStack> list = DefaultedList.ofSize(n, ItemStack.EMPTY);
		pack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
		return list;
	}

	public static void store(ItemStack pack, List<ItemStack> list) {
		pack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(list));
	}

	/** Puts as much of a specimen as fits into the backpacks that pick up; shrinks it and returns how many went in. */
	public static int absorb(PlayerEntity player, ItemStack stack) {
		if (!isSpecimen(stack) || !accepts(stack)) {
			return 0;
		}
		int before = stack.getCount();
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size() && !stack.isEmpty(); i++) {
			ItemStack c = inv.getStack(i);
			if (!(c.getItem() instanceof BackpackItem) || !picksUp(c)) {
				continue;
			}
			DefaultedList<ItemStack> list = contents(c);
			boolean changed = false;
			for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
				for (int k = 0; k < list.size() && !stack.isEmpty(); k++) {
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

	/** Opens the backpack in this inventory slot (0-35, or 40 for the off hand). */
	public static void open(ServerPlayerEntity sp, int slot) {
		ItemStack stack = sp.getInventory().getStack(slot);
		if (!(stack.getItem() instanceof BackpackItem)) {
			return;
		}
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
				return new BackpackScreenHandler(syncId, inv, slot);
			}
		});
		sp.getWorld().playSound(null, sp.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), SoundCategory.PLAYERS, 0.6f, 1.0f);
	}

	/** The backpack key: opens the first backpack in the pack (hotbar first, then the rest, then the off hand). */
	public static void openFirst(ServerPlayerEntity sp) {
		if (sp.currentScreenHandler != sp.playerScreenHandler) {
			return;
		}
		PlayerInventory inv = sp.getInventory();
		for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
			if (inv.getStack(i).getItem() instanceof BackpackItem) {
				open(sp, i);
				return;
			}
		}
		if (inv.getStack(PlayerInventory.OFF_HAND_SLOT).getItem() instanceof BackpackItem) {
			open(sp, PlayerInventory.OFF_HAND_SLOT);
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.isSneaking()) {
			boolean on = !picksUp(stack);
			stack.set(Explore.PICKUP, on);
			if (!world.isClient) {
				user.sendMessage(Text.translatable("message." + PeriodicElements.MOD_ID + ".backpack." + (on ? "on" : "off")), true);
			}
			return TypedActionResult.success(stack, world.isClient);
		}
		if (user instanceof ServerPlayerEntity sp) {
			open(sp, hand == Hand.OFF_HAND ? PlayerInventory.OFF_HAND_SLOT : sp.getInventory().selectedSlot);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		for (String part : Text.translatable("item.periodic_elements.backpack.tip", slots()).getString().split("(?<=[.;])\\s+")) {
			String line = part.endsWith(";") ? part.substring(0, part.length() - 1).strip() : part;
			if (!line.isEmpty()) {
				tooltip.add(Text.literal(Character.toUpperCase(line.charAt(0)) + line.substring(1)).formatted(Formatting.GRAY));
			}
		}
		int used = 0;
		for (ItemStack s : contents(stack)) {
			if (!s.isEmpty()) used++;
		}
		tooltip.add(Text.translatable("item.periodic_elements.sample_case.used", used, slots()).formatted(Formatting.DARK_AQUA));
		tooltip.add(Text.translatable("item.periodic_elements.backpack." + (picksUp(stack) ? "pickup_on" : "pickup_off"))
				.formatted(picksUp(stack) ? Formatting.GREEN : Formatting.DARK_GRAY));
	}
}
