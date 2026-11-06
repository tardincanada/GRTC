package com.periodicelements.farm;

import com.periodicelements.Fluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Latex collects in the tap's flask while it is fixed to a rubber log in a jungle biome: one flask's worth every
 * {@link Farm#tapTicks()} ticks, up to {@link Farm#tapHolds()}. Outside the jungle the tree gives nothing (Guilherme's
 * rule: a rubber tree planted elsewhere grows but makes no rubber).
 */
public class TreeTapBlockEntity extends BlockEntity implements SidedInventory {
	public static final int IN = 0;
	public static final int OUT = 1;
	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
	private int latex;
	private int timer;

	public TreeTapBlockEntity(BlockPos pos, BlockState state) {
		super(Farm.TAP_ENTITY, pos, state);
	}

	public int latex() {
		return latex;
	}

	/** Does this tap's tree give latex here? */
	public static boolean flows(World world, BlockPos pos, BlockState state) {
		BlockState log = world.getBlockState(pos.offset(state.get(TreeTapBlock.FACING).getOpposite()));
		if (log.isOf(Farm.RUBBER_LOG)) {
			return world.getBiome(pos).isIn(Farm.RUBBER_BIOMES);
		}
		// 2.8.0: trees that give sap or resin, each only in its own biomes.
		com.periodicelements.trees.Trees.Species s = com.periodicelements.trees.Trees.tapped(log);
		return s != null && world.getBiome(pos).isIn(s.tag());
	}

	/** The tree behind the tap, if it is one of the 2.8.0 species (null for the rubber tree). */
	@Nullable
	private com.periodicelements.trees.Trees.Species species() {
		if (world == null) {
			return null;
		}
		return com.periodicelements.trees.Trees.tapped(world.getBlockState(pos.offset(getCachedState().get(TreeTapBlock.FACING).getOpposite())));
	}

	/** What this tap collects: latex, or the species' sap or resin. */
	public Item product() {
		com.periodicelements.trees.Trees.Species s = species();
		return s != null ? s.tapItem() : Farm.LATEX;
	}

	/** Liquids go into a glass flask; hard resins drop straight into the tap. */
	public boolean needsFlask() {
		com.periodicelements.trees.Trees.Species s = species();
		return s == null || s.tapFlask();
	}

	public void tick(World world, BlockPos pos, BlockState state) {
		com.periodicelements.trees.Trees.Species sp = species();
		int every = sp != null && sp.tapTicks() > 0 ? sp.tapTicks() : Farm.tapTicks();
		if (++timer >= every) {
			timer = 0;
			if (latex < Farm.tapHolds() && flows(world, pos, state)) {
				latex++;
				markDirty();
			}
		}
		ItemStack in = items.get(IN);
		ItemStack out = items.get(OUT);
		Item product = product();
		boolean room = out.isEmpty() || (out.isOf(product) && out.getCount() < out.getMaxCount());
		boolean flask = needsFlask();
		if (latex > 0 && room && (!flask || in.isOf(Fluids.GLASS_FLASK))) {
			if (flask) {
				in.decrement(1);
			}
			if (out.isEmpty()) {
				items.set(OUT, take());
			} else {
				take();
				out.increment(1);
			}
			markDirty();
		}
	}

	/** Takes one flask of latex (the caller supplies the empty flask), or one lump of resin. */
	public ItemStack take() {
		latex--;
		markDirty();
		return new ItemStack(product());
	}

	public Text describe(World world) {
		com.periodicelements.trees.Trees.Species s = species();
		if (!flows(world, pos, getCachedState())) {
			return Text.translatable(s != null ? "gui.periodic_elements.tap.wild" : "gui.periodic_elements.tap.climate");
		}
		if (s != null) {
			return Text.translatable("gui.periodic_elements.tap.ready", latex, Farm.tapHolds(), product().getName());
		}
		return Text.translatable("gui.periodic_elements.tap.latex", latex, Farm.tapHolds());
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		nbt.putInt("Latex", latex);
		nbt.putInt("Timer", timer);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		latex = nbt.getInt("Latex");
		timer = nbt.getInt("Timer");
	}

	@Override
	public int size() {
		return 2;
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
		if (!s.isEmpty()) {
			markDirty();
		}
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
		return net.minecraft.inventory.Inventory.canPlayerUse(this, player);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return slot == IN && stack.isOf(Fluids.GLASS_FLASK);
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return side == Direction.DOWN ? new int[]{OUT} : new int[]{IN};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return isValid(slot, stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot == OUT;
	}
}
