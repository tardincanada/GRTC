package com.periodicelements.research;

import com.periodicelements.ModContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A research station. Put a project's samples in its 9 slots and start the project from the screen; it takes
 * time, then the player who started it knows the project. The station also writes treatises.
 */
public class ResearchStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {
	public static final int SIZE = 9;
	public static final int P_ACTIVE = 0, P_PROGRESS = 1, P_TOTAL = 2, P_LEVEL = 3, P_COUNT = 4;
	public static final TagKey<Item> INK = TagKey.of(RegistryKeys.ITEM, Identifier.of("periodic_elements", "ink"));

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
	private final int level;
	private int active = -1;
	private int progress;
	@Nullable
	private UUID researcher;

	private final PropertyDelegate properties = new PropertyDelegate() {
		@Override
		public int get(int index) {
			return switch (index) {
				case P_ACTIVE -> active;
				case P_PROGRESS -> progress;
				case P_TOTAL -> active < 0 ? 0 : Research.duration(Research.get(active));
				case P_LEVEL -> level;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int size() {
			return P_COUNT;
		}
	};

	public ResearchStationBlockEntity(BlockPos pos, BlockState state) {
		super(ModContent.STATION_ENTITY, pos, state);
		this.level = ((ResearchStationBlock) state.getBlock()).level();
	}

	public int level() {
		return level;
	}

	public int activeProject() {
		return active;
	}

	public void tick(ServerWorld world) {
		if (active < 0) {
			return;
		}
		Research.Project p = Research.get(active);
		if (p == null) {
			active = -1;
			return;
		}
		progress++;
		if (progress >= Research.duration(p)) {
			if (researcher != null) {
				ResearchState.get(world.getServer()).grant(world.getServer(), researcher, List.of(p.id()));
				ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(researcher);
				if (player != null) {
					player.sendMessage(Text.translatable("message.periodic_elements.research.done", p.name())
							.formatted(Formatting.GREEN), false);
				}
			}
			world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 0.6f, 1f);
			active = -1;
			progress = 0;
			researcher = null;
		}
		markDirty();
	}

	/** Starts a project for this player. Returns a message key explaining a refusal, or null on success. */
	@Nullable
	public String startResearch(ServerPlayerEntity player, Research.Project p) {
		if (active >= 0) {
			return "message.periodic_elements.research.busy";
		}
		if (p.level() != level) {
			return "message.periodic_elements.research.wrong_station";
		}
		Set<String> known = ResearchState.get(player.getServer()).known(player.getUuid());
		switch (Research.status(p, known)) {
			case DONE -> {
				return "message.periodic_elements.research.known";
			}
			case NEEDS_LEVEL -> {
				return "message.periodic_elements.research.needs_level";
			}
			case NEEDS_PROJECTS -> {
				return "message.periodic_elements.research.needs_projects";
			}
			default -> {
			}
		}
		List<Requirement> needs = new ArrayList<>();
		for (Research.Sample s : p.samples()) {
			needs.add(new Requirement(s::matches, s.count()));
		}
		if (!consume(needs)) {
			return "message.periodic_elements.research.missing_samples";
		}
		active = p.index();
		progress = 0;
		researcher = player.getUuid();
		markDirty();
		return null;
	}

	/** Treatise cost: 1 leather, plus 1 paper and 1 ink per project recorded. */
	public static List<Requirement> treatiseCost(int projects) {
		return List.of(
				new Requirement(s -> s.isOf(Items.LEATHER), 1),
				new Requirement(s -> s.isOf(Items.PAPER), projects),
				new Requirement(s -> s.isIn(INK), projects));
	}

	/** Printing gives {@link #PRINT_COPIES} copies for 1 type metal, a leather each, and a paper and ink per 2 projects. */
	public static final int PRINT_COPIES = 2;

	public static List<Requirement> printedCost(int projects) {
		int sheets = (projects + 1) / 2;
		return List.of(
				new Requirement(ResearchStationBlockEntity::isTypeMetal, 1),
				new Requirement(s -> s.isOf(Items.LEATHER), PRINT_COPIES),
				new Requirement(s -> s.isOf(Items.PAPER), sheets),
				new Requirement(s -> s.isIn(INK), sheets));
	}

	public static boolean isTypeMetal(ItemStack s) {
		return Registries.ITEM.getId(s.getItem()).toString().equals("periodic_elements:type_metal");
	}

	/** Prints instead of writing by hand when the player knows printing and the station holds type metal. */
	public static boolean prints(Set<String> known, Inventory inv) {
		return Research.allows(known, "item:treatise_printed") && count(inv, ResearchStationBlockEntity::isTypeMetal) > 0;
	}

	@Nullable
	public String writeTreatise(ServerPlayerEntity player) {
		Set<String> known = ResearchState.get(player.getServer()).known(player.getUuid());
		if (!Research.allows(known, "item:treatise_manuscript")) {
			return "message.periodic_elements.treatise.needs_writing";
		}
		if (known.isEmpty()) {
			return "message.periodic_elements.treatise.nothing";
		}
		boolean printed = prints(known, this);
		if (!consume(printed ? printedCost(known.size()) : treatiseCost(known.size()))) {
			return printed ? "message.periodic_elements.treatise.missing_printed" : "message.periodic_elements.treatise.missing";
		}
		for (int copy = 0; copy < (printed ? PRINT_COPIES : 1); copy++) {
			ItemStack treatise = new ItemStack(printed ? ModContent.TREATISE_PRINTED : ModContent.TREATISE_MANUSCRIPT);
			treatise.set(ModContent.TREATISE_CONTENTS, new TreatiseContents(player.getName().getString(),
					Research.techLevel(known), new ArrayList<>(known)));
			player.getInventory().offerOrDrop(treatise);
		}
		world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1f, 1f);
		markDirty();
		return null;
	}

	public record Requirement(Predicate<ItemStack> matches, int count) {
	}

	/** Counts how many matching items the station holds. */
	public static int count(Inventory inv, Predicate<ItemStack> matches) {
		int n = 0;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (!s.isEmpty() && matches.test(s)) {
				n += s.getCount();
			}
		}
		return n;
	}

	/** Takes all requirements from the slots, or nothing if any is short. */
	private boolean consume(List<Requirement> needs) {
		for (Requirement r : needs) {
			if (count(this, r.matches()) < r.count()) {
				return false;
			}
		}
		java.util.List<ItemStack> empties = new java.util.ArrayList<>();
		for (Requirement r : needs) {
			int left = r.count();
			for (int i = 0; i < SIZE && left > 0; i++) {
				ItemStack s = items.get(i);
				if (!s.isEmpty() && r.matches().test(s)) {
					int take = Math.min(left, s.getCount());
					if (s.getItem().getRecipeRemainder() != null) {
						empties.add(new ItemStack(s.getItem().getRecipeRemainder(), take));
					}
					s.decrement(take);
					left -= take;
				}
			}
		}
		// Flasks, cylinders and buckets the samples came in stay in the station.
		for (ItemStack e : empties) {
			for (int i = 0; i < SIZE && !e.isEmpty(); i++) {
				ItemStack s = items.get(i);
				if (s.isEmpty()) {
					items.set(i, e.copy());
					e.setCount(0);
				} else if (ItemStack.areItemsAndComponentsEqual(s, e) && s.getCount() < s.getMaxCount()) {
					int n = Math.min(e.getCount(), s.getMaxCount() - s.getCount());
					s.increment(n);
					e.decrement(n);
				}
			}
		}
		markDirty();
		return true;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		if (active >= 0) {
			nbt.putString("Active", Research.get(active).id());
			nbt.putInt("Progress", progress);
		}
		if (researcher != null) {
			nbt.putUuid("Researcher", researcher);
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		Research.Project p = nbt.contains("Active") ? Research.get(nbt.getString("Active")) : null;
		active = p == null ? -1 : p.index();
		progress = nbt.getInt("Progress");
		researcher = nbt.containsUuid("Researcher") ? nbt.getUuid("Researcher") : null;
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ResearchStationScreenHandler(syncId, inv, this, properties, this);
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public boolean isEmpty() {
		return items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack s = Inventories.splitStack(items, slot, amount);
		markDirty();
		return s;
	}

	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(items, slot);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		items.set(slot, stack);
		stack.capCount(getMaxCount(stack));
		markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return Inventory.canPlayerUse(this, player);
	}

	@Override
	public void clear() {
		items.clear();
	}
}
