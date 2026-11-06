package com.periodicelements.crops;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/** The feed in a Feeding Trough; every 5 seconds it feeds one animal around that eats it. */
public class FeedingTroughBlockEntity extends BlockEntity {
	public static final int RANGE = 6;
	public static final int PERIOD = 100;
	private ItemStack feed = ItemStack.EMPTY;
	/** Servings left in the last item eaten (silage feeds two). */
	private int spare;
	/** Balanced feed halves the wait after breeding (5 minutes) while more than this many ticks are left. */
	public static final int BALANCED_WAIT = 3000;

	public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
		super(Crops.TROUGH_ENTITY, pos, state);
	}

	public ItemStack feed() {
		return feed;
	}

	/** Puts the whole stack (as much as fits) in; false if the trough holds another kind of feed. */
	public boolean insert(ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}
		if (feed.isEmpty()) {
			feed = stack.split(stack.getCount());
		} else if (ItemStack.areItemsAndComponentsEqual(feed, stack)) {
			int n = Math.min(stack.getCount(), feed.getMaxCount() - feed.getCount());
			feed.increment(n);
			stack.decrement(n);
		} else {
			return false;
		}
		markDirty();
		return true;
	}

	public Text contents() {
		return feed.isEmpty() ? Text.translatable("gui.periodic_elements.trough.empty")
				: Text.translatable("gui.periodic_elements.trough.contents", feed.getCount(), feed.getName());
	}

	void tick(World world, BlockPos pos) {
		if (feed.isEmpty() || (world.getTime() + pos.asLong()) % PERIOD != 0) {
			return;
		}
		feedOne((ServerWorld) world, pos);
	}

	/**
	 * Feeds one animal that eats this feed: first an adult ready to breed (it falls in love), else a young one (it
	 * grows). Adults come first so the young cannot eat everything while the adults wait (3.34.0).
	 */
	public boolean feedOne(ServerWorld world, BlockPos pos) {
		Box box = new Box(pos).expand(RANGE);
		var animals = world.getEntitiesByClass(AnimalEntity.class, box, a -> a.isAlive() && a.isBreedingItem(feed));
		boolean balanced = feed.isOf(Crops.BALANCED_FEED);
		// 3.37.0: balanced feed also shortens the wait of animals that have just bred (third pass).
		for (int pass = 0; pass < (balanced ? 3 : 2); pass++) {
			for (AnimalEntity a : animals) {
				if (pass == 0 && !a.isBaby() && a.getBreedingAge() == 0 && a.canEat()) {
					a.lovePlayer(null);
				} else if (pass == 1 && a.isBaby()) {
					int growth = PassiveEntity.toGrowUpAge(-a.getBreedingAge());
					a.growUp(balanced ? 2 * growth : growth, true);
				} else if (pass == 2 && !a.isBaby() && a.getBreedingAge() > BALANCED_WAIT) {
					a.setBreedingAge(a.getBreedingAge() / 2);
				} else {
					continue;
				}
				world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, a.getX(), a.getBodyY(0.6), a.getZ(), 4, 0.3, 0.3, 0.3, 0);
				eat();
				return true;
			}
		}
		return false;
	}

	/** One serving: silage goes twice as far as plain fodder (3.37.0). */
	private void eat() {
		if (feed.isOf(Crops.SILAGE) && spare == 0) {
			spare = 1; // the first serving of a silage leaves it half eaten
		} else {
			spare = 0;
			feed.decrement(1);
		}
		markDirty();
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		if (!feed.isEmpty()) {
			nbt.put("Feed", feed.encode(registries));
			nbt.putInt("Spare", spare);
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		feed = nbt.contains("Feed") ? ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("Feed")) : ItemStack.EMPTY;
		spare = nbt.getInt("Spare");
	}
}
