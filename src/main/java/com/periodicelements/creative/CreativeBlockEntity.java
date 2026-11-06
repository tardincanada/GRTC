package com.periodicelements.creative;

import com.periodicelements.Fluids;
import com.periodicelements.PeriodicElements;
import com.periodicelements.fluid.FluidHolder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 3.51.0: block entity of the Infinite Fluid Source, Infinite Item Source, Universal Void and Accelerator Clock.
 * <ul>
 * <li>Fluid source: a holder that never runs dry; pumps, pipes and machines draw the chosen liquid or gas from it.</li>
 * <li>Item source: hoppers, belts and machines take the chosen item from it for ever (one full stack is always there).</li>
 * <li>Void: takes every item, liquid and gas offered and destroys it.</li>
 * <li>Accelerator: block entities, crops and young animals within {@link #RADIUS} blocks run {@link #SPEED} times as fast.</li>
 * </ul>
 */
public class CreativeBlockEntity extends BlockEntity implements SidedInventory, FluidHolder {
	public static final int RADIUS = 8;
	public static final int SPEED = 10;
	private static final double ENDLESS = 1.0e9;
	private static final int[] SLOT = {0};
	private static final int[] NONE = {};

	private ItemStack template = ItemStack.EMPTY;
	@Nullable
	private Item fluid;

	public CreativeBlockEntity(BlockPos pos, BlockState state) {
		super(CreativeItems.CREATIVE_ENTITY, pos, state);
	}

	private CreativeBlock.Kind kind() {
		return getCachedState().getBlock() instanceof CreativeBlock b ? b.kind : CreativeBlock.Kind.VOID;
	}

	// ---------- choosing ----------

	public void setTemplate(ItemStack stack) {
		template = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
		markDirty();
	}

	public void setFluid(@Nullable Item fluid) {
		this.fluid = fluid;
		markDirty();
	}

	/** The fluid a flask, cylinder or water bucket holds, or null. */
	@Nullable
	public static Item fluidOf(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		Item gas = Fluids.gasOf(stack.getItem());
		if (gas != null) {
			return gas;
		}
		if (stack.isOf(Items.WATER_BUCKET)) {
			return Registries.ITEM.get(Identifier.of(PeriodicElements.MOD_ID, "water"));
		}
		Identifier id = Registries.ITEM.getId(stack.getItem());
		return id.getNamespace().equals(PeriodicElements.MOD_ID) && Fluids.isFluid(id.getPath()) ? stack.getItem() : null;
	}

	/** One filled flask or cylinder of the chosen fluid for an empty one, or empty if it does not fit. */
	public ItemStack fillContainer(ItemStack empty) {
		if (fluid == null) {
			return ItemStack.EMPTY;
		}
		Item cylinder = Fluids.cylinderOf(fluid);
		if (empty.isOf(Fluids.STEEL_CYLINDER) && cylinder != null) {
			return new ItemStack(cylinder);
		}
		if (empty.isOf(Fluids.GLASS_FLASK) && cylinder == null) {
			return new ItemStack(fluid);
		}
		return ItemStack.EMPTY;
	}

	public Text describe(CreativeBlock.Kind kind) {
		String k = "message.periodic_elements.";
		return switch (kind) {
			case FLUID_SOURCE -> fluid == null ? Text.translatable(k + "creative_fluid_source.empty")
					: Text.translatable(k + "creative_fluid_source.set", fluid.getName());
			case ITEM_SOURCE -> template.isEmpty() ? Text.translatable(k + "creative_item_source.empty")
					: Text.translatable(k + "creative_item_source.set", template.getName());
			case VOID -> Text.translatable(k + "creative_void.info");
			default -> Text.translatable(k + "creative_accelerator.info", SPEED, RADIUS);
		};
	}

	// ---------- items ----------

	@Override
	public int[] getAvailableSlots(Direction side) {
		return switch (kind()) {
			case ITEM_SOURCE, VOID -> SLOT;
			default -> NONE;
		};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return kind() == CreativeBlock.Kind.VOID;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return kind() == CreativeBlock.Kind.ITEM_SOURCE;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return kind() == CreativeBlock.Kind.VOID;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return kind() != CreativeBlock.Kind.ITEM_SOURCE || template.isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		return kind() == CreativeBlock.Kind.ITEM_SOURCE && !template.isEmpty()
				? template.copyWithCount(template.getMaxCount()) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return kind() == CreativeBlock.Kind.ITEM_SOURCE && !template.isEmpty()
				? template.copyWithCount(Math.min(amount, template.getMaxCount())) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int slot) {
		return removeStack(slot, Integer.MAX_VALUE);
	}

	/** The void destroys what is put in; the source keeps its item whatever is done to it. */
	@Override
	public void setStack(int slot, ItemStack stack) {
	}

	@Override
	public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
		return false;
	}

	@Override
	public void clear() {
		template = ItemStack.EMPTY;
		markDirty();
	}

	// ---------- fluids ----------

	@Nullable
	@Override
	public Item fluid() {
		return kind() == CreativeBlock.Kind.FLUID_SOURCE ? fluid : null;
	}

	@Override
	public double units() {
		return fluid() == null ? 0 : ENDLESS;
	}

	@Override
	public double capacity(Item what) {
		return switch (kind()) {
			case VOID -> ENDLESS;
			case FLUID_SOURCE -> fluid == null ? 0 : ENDLESS;
			default -> 0;
		};
	}

	@Override
	public double room(Item what) {
		return kind() == CreativeBlock.Kind.VOID ? ENDLESS : 0;
	}

	@Override
	public ItemStack sample() {
		return fluid() == null ? ItemStack.EMPTY : new ItemStack(fluid);
	}

	@Override
	public double fill(Item what, double units, double ratio, boolean simulate) {
		return kind() == CreativeBlock.Kind.VOID ? units : 0;
	}

	@Override
	public double drain(double units, boolean simulate) {
		return fluid() == null ? 0 : units;
	}

	@Override
	public boolean gone() {
		return isRemoved();
	}

	// ---------- accelerator ----------

	/** Runs everything near it {@link #SPEED} - 1 extra times this tick. */
	public void accelerate(ServerWorld world, BlockPos pos) {
		int extra = SPEED - 1;
		Box box = new Box(pos).expand(RADIUS);
		// block entities (machines, kilns, generators, research stations...)
		List<BlockEntity> found = new ArrayList<>();
		for (int cx = (pos.getX() - RADIUS) >> 4; cx <= (pos.getX() + RADIUS) >> 4; cx++) {
			for (int cz = (pos.getZ() - RADIUS) >> 4; cz <= (pos.getZ() + RADIUS) >> 4; cz++) {
				if (!world.isChunkLoaded(ChunkPos.toLong(cx, cz))) {
					continue;
				}
				WorldChunk chunk = world.getChunk(cx, cz);
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					BlockPos at = be.getPos();
					if (!be.isRemoved() && box.contains(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5)
							&& !(be instanceof CreativeBlockEntity) && !(be instanceof CreativeGeneratorBlockEntity)) {
						found.add(be);
					}
				}
			}
		}
		for (BlockEntity be : found) {
			tickExtra(world, be, extra);
		}
		// random ticks (crops, saplings, shrubs): the same rate as the gamerule gives, times SPEED - 1
		int perSection = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
		int side = 2 * RADIUS + 1;
		double expected = extra * perSection * (side * side * side) / 4096.0;
		int n = (int) expected + (world.random.nextDouble() < expected - (int) expected ? 1 : 0);
		for (int i = 0; i < n; i++) {
			BlockPos at = pos.add(world.random.nextInt(side) - RADIUS, world.random.nextInt(side) - RADIUS, world.random.nextInt(side) - RADIUS);
			BlockState s = world.getBlockState(at);
			if (s.hasRandomTicks()) {
				s.randomTick(world, at, world.random);
			}
		}
		// young animals grow up, and adults are ready to breed again, SPEED times as fast
		for (PassiveEntity animal : world.getEntitiesByClass(PassiveEntity.class, box, e -> e.getBreedingAge() != 0)) {
			int age = animal.getBreedingAge();
			animal.setBreedingAge(age < 0 ? Math.min(0, age + extra) : Math.max(0, age - extra));
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void tickExtra(ServerWorld world, BlockEntity be, int times) {
		BlockState state = be.getCachedState();
		BlockEntityTicker ticker = state.getBlockEntityTicker(world, (BlockEntityType) be.getType());
		if (ticker == null) {
			return;
		}
		for (int i = 0; i < times && !be.isRemoved(); i++) {
			ticker.tick(world, be.getPos(), state, be);
		}
	}

	// ---------- saving ----------

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!template.isEmpty()) {
			nbt.put("Template", template.encode(lookup));
		}
		if (fluid != null) {
			nbt.putString("Fluid", Registries.ITEM.getId(fluid).toString());
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		template = nbt.contains("Template") ? ItemStack.fromNbt(lookup, nbt.get("Template")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
		fluid = nbt.contains("Fluid") ? Registries.ITEM.get(Identifier.of(nbt.getString("Fluid"))) : null;
		if (fluid == Items.AIR) {
			fluid = null;
		}
	}
}
