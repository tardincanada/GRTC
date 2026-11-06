package com.periodicelements.crops;

import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

/**
 * The inside of the 3.37.0 crop machines: slot 0 takes the input (from a hopper or by hand), slots 1 to 8 hold what
 * comes out (hoppers below take it). The Silo turns fodder into silage; the Seed Irradiator X-rays seeds (powered);
 * the Thermocycler tests seeds against a marker seed and sorts them: matching ones to slots 1-4 (taken from below),
 * the rest to slots 5-8 (taken from the sides).
 */
public class CropMachineBlockEntity extends BlockEntity implements SidedInventory {
	public static final int SIZE = 9;
	/** Silo: one fodder becomes silage every 5 seconds. */
	public static final int SILO_TICKS = 100;
	/** Irradiator and Thermocycler: one seed every half second. */
	public static final int SEED_TICKS = 10;
	public static final double XRAY_KJ = 40, PCR_KJ = 10;
	/** X-rays: 3 seeds in 10 die; each allele of the survivors mutates 20 times as often as in nature. */
	public static final double XRAY_DEATH = 0.3, XRAY_MUTATION = 20.0 / Genome.MUTATION;
	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
	private int marker = -1;
	private boolean powered;

	public CropMachineBlockEntity(BlockPos pos, BlockState state) {
		super(Crops.MACHINE_ENTITY, pos, state);
	}

	public CropMachineBlock.Kind kind() {
		return getCachedState().getBlock() instanceof CropMachineBlock b ? b.kind() : CropMachineBlock.Kind.SILO;
	}

	public boolean accepts(ItemStack stack) {
		return switch (kind()) {
			case SILO -> Crops.isFodder(stack);
			case IRRADIATOR, THERMOCYCLER -> Crops.isSeed(stack);
		};
	}

	/** Puts as much of the stack in the input slot as fits; false if it is not something this machine takes. */
	public boolean insert(ItemStack stack) {
		if (!accepts(stack)) {
			return false;
		}
		ItemStack in = items.get(0);
		if (in.isEmpty()) {
			items.set(0, stack.split(stack.getCount()));
		} else if (ItemStack.areItemsAndComponentsEqual(in, stack)) {
			int n = Math.min(stack.getCount(), in.getMaxCount() - in.getCount());
			in.increment(n);
			stack.decrement(n);
		} else {
			return false;
		}
		markDirty();
		return true;
	}

	/** Hands everything that came out to the player. */
	public int takeOutputs(PlayerEntity player) {
		int n = 0;
		for (int i = 1; i < SIZE; i++) {
			ItemStack s = items.get(i);
			if (!s.isEmpty()) {
				n += s.getCount();
				player.getInventory().offerOrDrop(s);
				items.set(i, ItemStack.EMPTY);
			}
		}
		if (n > 0) {
			markDirty();
		}
		return n;
	}

	public int marker() {
		return marker;
	}

	public void setMarker(int genome) {
		marker = genome & Genome.MASK;
		markDirty();
	}

	public Text describe() {
		ItemStack in = items.get(0);
		int out = IntStream.range(1, SIZE).map(i -> items.get(i).getCount()).sum();
		String k = "gui.periodic_elements." + kind().id;
		if (kind() != CropMachineBlock.Kind.SILO && !powered && !in.isEmpty()) {
			return Text.translatable(k + ".no_power", in.getCount(), out);
		}
		if (kind() == CropMachineBlock.Kind.THERMOCYCLER && marker < 0) {
			return Text.translatable(k + ".no_marker");
		}
		return Text.translatable(k + ".status", in.getCount(), out);
	}

	void tick(ServerWorld world, BlockPos pos) {
		CropMachineBlock.Kind kind = kind();
		int period = kind == CropMachineBlock.Kind.SILO ? SILO_TICKS : SEED_TICKS;
		if ((world.getTime() + pos.asLong()) % period != 0 || items.get(0).isEmpty()) {
			return;
		}
		switch (kind) {
			case SILO -> ensileOne();
			case IRRADIATOR -> {
				powered = power(world, pos, XRAY_KJ);
				if (powered) {
					irradiateOne(world.random);
					world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 3, 0.2, 0, 0.2, 0);
				}
			}
			case THERMOCYCLER -> {
				if (marker >= 0) {
					powered = power(world, pos, PCR_KJ);
					if (powered) {
						sortOne();
					}
				}
			}
		}
	}

	/** One fodder packed into silage. */
	public void ensileOne() {
		if (!items.get(0).isEmpty() && offer(new ItemStack(Crops.SILAGE), 1, SIZE)) {
			items.get(0).decrement(1);
			markDirty();
		}
	}

	/** One seed through the X-ray tube: it dies, or comes out with its genes shaken. */
	public void irradiateOne(Random random) {
		ItemStack in = items.get(0);
		int g = in.getOrDefault(Crops.GENOME, Genome.DEFAULT) & Genome.MASK;
		ItemStack one = in.copyWithCount(1);
		if (random.nextDouble() >= XRAY_DEATH) {
			Genetics.setGenome(one, Genome.mutate(g, random, XRAY_MUTATION));
			if (!offer(one, 1, SIZE)) {
				return;
			}
		}
		in.decrement(1);
		markDirty();
	}

	/** Does the seed show every useful trait the marker seed shows? */
	public static boolean matches(int genome, int marker) {
		for (int g = 0; g < Genome.GENES; g++) {
			if (Genome.has(marker, g) && !Genome.has(genome, g)) {
				return false;
			}
		}
		for (int t = 0; t < Genome.TRANSGENES.length; t++) {
			if (Genome.transgene(marker, t) && !Genome.transgene(genome, t)) {
				return false;
			}
		}
		return true;
	}

	/** One seed tested against the marker: to the matching or the other outputs. */
	public void sortOne() {
		ItemStack in = items.get(0);
		int g = in.getOrDefault(Crops.GENOME, Genome.DEFAULT) & Genome.MASK;
		boolean match = matches(g, marker);
		if (offer(in.copyWithCount(1), match ? 1 : 5, match ? 5 : SIZE)) {
			in.decrement(1);
			markDirty();
		}
	}

	private boolean offer(ItemStack stack, int from, int to) {
		for (int i = from; i < to; i++) {
			ItemStack s = items.get(i);
			if (!s.isEmpty() && ItemStack.areItemsAndComponentsEqual(s, stack) && s.getCount() + stack.getCount() <= s.getMaxCount()) {
				s.increment(stack.getCount());
				return true;
			}
		}
		for (int i = from; i < to; i++) {
			if (items.get(i).isEmpty()) {
				items.set(i, stack);
				return true;
			}
		}
		return false;
	}

	/** Draws {@code kJ} from the piles and dynamos joined to {@code pos}; false (and nothing drawn) if they have less. */
	public static boolean power(ServerWorld world, BlockPos pos, double kJ) {
		var sources = MachineBlockEntity.powerSources(world, pos);
		double have = 0;
		for (PowerBlockEntity p : sources) {
			have += p.available();
		}
		if (have < kJ - 1e-9) {
			return false;
		}
		double got = 0;
		for (PowerBlockEntity p : sources) {
			got += p.draw(kJ - got);
			if (got >= kJ - 1e-9) {
				break;
			}
		}
		return true;
	}

	public DefaultedList<ItemStack> items() {
		return items;
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		items.clear();
		Inventories.readNbt(nbt, items, lookup);
		marker = nbt.contains("Marker") ? nbt.getInt("Marker") : -1;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		Inventories.writeNbt(nbt, items, lookup);
		if (marker >= 0) {
			nbt.putInt("Marker", marker);
		}
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		if (side == Direction.DOWN) {
			return kind() == CropMachineBlock.Kind.THERMOCYCLER ? new int[]{1, 2, 3, 4} : new int[]{1, 2, 3, 4, 5, 6, 7, 8};
		}
		if (side != Direction.UP && kind() == CropMachineBlock.Kind.THERMOCYCLER) {
			return new int[]{0, 5, 6, 7, 8};
		}
		return new int[]{0};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return slot == 0 && accepts(stack) && (items.get(0).isEmpty() || ItemStack.areItemsAndComponentsEqual(items.get(0), stack));
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot > 0;
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
		ItemStack s = Inventories.removeStack(items, slot);
		markDirty();
		return s;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		items.set(slot, stack);
		markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void clear() {
		items.clear();
	}
}
