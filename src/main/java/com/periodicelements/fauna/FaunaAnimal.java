package com.periodicelements.fauna;

import com.periodicelements.crops.Genetics;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A land or amphibious animal of the fauna plan. Its species ({@link Fauna.Species}) comes from its entity type;
 * goals, products and dangers follow the species' behaviour and traits.
 */
public class FaunaAnimal extends AnimalEntity {
	private static final TrackedData<Boolean> SHEARED = DataTracker.registerData(FaunaAnimal.class, TrackedDataHandlerRegistry.BOOLEAN);
	/** Ticks between two bottles of venom or blood, two milkings of arrows on a frog. */
	public static final int MILK_TICKS = 6000, ARROW_TICKS = 1200;
	private int productTimer, antlerTimer, cooldown, woolTimer;

	public FaunaAnimal(EntityType<? extends AnimalEntity> type, World world) {
		super(type, world);
		productTimer = 6000 + random.nextInt(6000);
		antlerTimer = 24000 + random.nextInt(24000);
	}

	public Fauna.Species species() {
		return Fauna.of(getType());
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(SHEARED, false);
	}

	public boolean isSheared() {
		return dataTracker.get(SHEARED);
	}

	@Override
	protected void initGoals() {
		Fauna.Species s = species();
		goalSelector.add(0, new SwimGoal(this));
		boolean fights = s.behaviour().equals("neutral") || s.behaviour().equals("night");
		if (fights) {
			goalSelector.add(1, new MeleeAttackGoal(this, 1.25, true));
			targetSelector.add(1, new RevengeGoal(this));
		} else {
			goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		}
		if (s.behaviour().equals("night")) {
			targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
					e -> getWorld().isNight() && !e.isSpectator()));
		}
		if (s.has("hunts_rabbits")) {
			targetSelector.add(3, new ActiveTargetGoal<>(this, RabbitEntity.class, 20, true, false, null));
		}
		goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		goalSelector.add(3, new TemptGoal(this, 1.2, this::isBreedingItem, false));
		goalSelector.add(4, new FollowParentGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6f));
		goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		Fauna.Species s = species();
		if (s == null) return false;
		Identifier id = Registries.ITEM.getId(stack.getItem());
		return s.food().contains(id.toString());
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity mate) {
		Entity child = getType().create(world);
		return child instanceof PassiveEntity p ? p : null;
	}

	/** Out of its climate (and outside a greenhouse of its climate) a pair breeds again only after {@link Fauna#SLOW_BREEDING}. */
	@Override
	public void breed(ServerWorld world, AnimalEntity other, @Nullable PassiveEntity baby) {
		ServerPlayerEntity lover = getLovingPlayer() != null ? getLovingPlayer() : other.getLovingPlayer();
		super.breed(world, other, baby);
		if (!Fauna.inClimate(world, getBlockPos(), species())) {
			setBreedingAge(Fauna.SLOW_BREEDING);
			other.setBreedingAge(Fauna.SLOW_BREEDING);
			if (lover != null) {
				lover.sendMessage(Text.translatable("message.periodic_elements.fauna.out_of_climate", getType().getName())
						.formatted(Formatting.YELLOW), true);
			}
		}
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		Fauna.Species s = species();
		ItemStack stack = player.getStackInHand(hand);
		World world = getWorld();
		if (s.behaviour().equals("touch") && stack.isEmpty()) {
			if (!world.isClient) poison(player);
			return ActionResult.success(world.isClient);
		}
		String venom = s.trait("venom");
		if (stack.isOf(Items.GLASS_BOTTLE) && !isBaby() && (venom != null || s.has("blood"))) {
			Item out = venom != null ? Registries.ITEM.get(Identifier.of(venom)) : Fauna.item("horseshoe_crab_blood");
			return give(player, hand, stack, out == null ? ItemStack.EMPTY : new ItemStack(out), MILK_TICKS, SoundEvents.ITEM_BOTTLE_FILL);
		}
		if (stack.isOf(Items.BUCKET) && s.has("milk") && !isBaby()) {
			player.playSound(SoundEvents.ENTITY_COW_MILK, 1f, 1f);
			player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.MILK_BUCKET)));
			return ActionResult.success(world.isClient);
		}
		if (stack.isOf(Items.ARROW) && s.has("arrows")) {
			int n = Math.min(8, stack.getCount());
			ItemStack tipped = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.POISON);
			tipped.setCount(n);
			if (cooldown > 0) return tooSoon(player);
			if (!world.isClient) {
				stack.decrement(n);
				if (!player.getInventory().insertStack(tipped)) player.dropItem(tipped, false);
				cooldown = ARROW_TICKS;
				world.playSound(null, getBlockPos(), SoundEvents.ENTITY_SLIME_SQUISH_SMALL, SoundCategory.NEUTRAL, 1f, 1.3f);
			}
			return ActionResult.success(world.isClient);
		}
		String wool = s.trait("shear");
		if (stack.isOf(Items.SHEARS) && wool != null && !isBaby()) {
			if (isSheared()) return tooSoon(player);
			if (!world.isClient) {
				dataTracker.set(SHEARED, true);
				woolTimer = 6000 + random.nextInt(6000);
				Item w = Registries.ITEM.get(Identifier.of(wool));
				dropStack(new ItemStack(w, 1 + random.nextInt(3)), 1f);
				world.playSound(null, getBlockPos(), SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1f, 1f);
				stack.damage(1, player, getSlotForHand(hand));
			}
			return ActionResult.success(world.isClient);
		}
		return super.interactMob(player, hand);
	}

	private ActionResult give(PlayerEntity player, Hand hand, ItemStack stack, ItemStack out, int ticks, SoundEvent sound) {
		if (out.isEmpty()) return ActionResult.PASS;
		if (cooldown > 0) return tooSoon(player);
		if (!getWorld().isClient) {
			cooldown = ticks;
			player.playSound(sound, 1f, 1f);
			player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, out));
		}
		return ActionResult.success(getWorld().isClient);
	}

	private ActionResult tooSoon(PlayerEntity player) {
		if (!getWorld().isClient) {
			player.sendMessage(Text.translatable("message.periodic_elements.fauna.no_more").formatted(Formatting.GRAY), true);
		}
		return ActionResult.CONSUME;
	}

	/** The poison frog's skin. */
	private void poison(LivingEntity who) {
		who.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 140, 1), this);
		if (who instanceof PlayerEntity p) {
			p.sendMessage(Text.translatable("message.periodic_elements.fauna.poisoned").formatted(Formatting.RED), true);
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		Fauna.Species s = species();
		if (s != null && s.behaviour().equals("touch") && !getWorld().isClient && source.getAttacker() instanceof PlayerEntity p
				&& source.getSource() == p && p.getMainHandStack().isEmpty()) {
			poison(p);
		}
		return super.damage(source, amount);
	}

	@Override
	public boolean tryAttack(Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit && target instanceof LivingEntity l && species().trait("venom") != null) {
			l.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, species().has("bite") ? 100 : 160, 0), this);
		}
		return hit;
	}

	@Override
	public boolean handleFallDamage(float distance, float multiplier, DamageSource source) {
		Fauna.Species s = species();
		if (s.has("flutter") || s.body().equals("frog") || s.has("leaves")) return false;
		return super.handleFallDamage(distance, multiplier, source);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		Fauna.Species s = species();
		if (s.has("flutter") && !isOnGround() && getVelocity().y < 0) {
			setVelocity(getVelocity().multiply(1, 0.6, 1));
		}
		World world = getWorld();
		if (world.isClient || !isAlive()) return;
		if (cooldown > 0) cooldown--;
		if (isBaby()) return;
		String egg = s.trait("eggs");
		if (egg != null && --productTimer <= 0) {
			productTimer = 6000 + random.nextInt(6000);
			playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1f);
			dropStack(new ItemStack(Registries.ITEM.get(Identifier.of(egg))));
			if (s.has("down") && random.nextInt(3) == 0 && Fauna.item("duck_down") != null) {
				dropStack(new ItemStack(Fauna.item("duck_down")));
			}
		}
		if (s.has("antlers") && --antlerTimer <= 0) {
			antlerTimer = 24000 + random.nextInt(24000);
			if (Fauna.item("antler") != null) dropStack(new ItemStack(Fauna.item("antler")));
		}
		if (isSheared() && --woolTimer <= 0) {
			dataTracker.set(SHEARED, false);
		}
		if (world instanceof ServerWorld sw && s.has("guano") && --productTimer <= 0) {
			productTimer = 4000 + random.nextInt(4000);
			GuanoBlock.addLayer(sw, getBlockPos());
		}
		if (world instanceof ServerWorld sw && s.has("dam") && age % 1200 == 0 && random.nextInt(4) == 0) {
			buildDam(sw);
		}
		if (world instanceof ServerWorld sw && age % 200 == 0) {
			if (s.has("honey")) {
				// 3.50.0: a bear raids a full beehive or bee nest next to it, as bears do
				for (BlockPos p : BlockPos.iterateOutwards(getBlockPos(), 2, 2, 2)) {
					BlockState st = sw.getBlockState(p);
					if (st.contains(net.minecraft.block.BeehiveBlock.HONEY_LEVEL) && st.get(net.minecraft.block.BeehiveBlock.HONEY_LEVEL) >= 5) {
						sw.setBlockState(p, st.with(net.minecraft.block.BeehiveBlock.HONEY_LEVEL, 0));
						heal(4f);
						playSound(SoundEvents.BLOCK_BEEHIVE_DRIP, 1f, 0.8f);
						break;
					}
				}
			}
			if (s.has("cure_pests")) {
				Genetics.curePests(sw, getBlockPos(), 4);
			}
			if (s.has("eats_crops") && Genetics.pestsOn(sw) && random.nextInt(3) == 0) {
				eatCrop(sw);
			}
		}
	}

	/** 3.50.0: a beaver fills a shallow water block near it with a dam of sticks and mud, up to 12 near each other. */
	public void buildDam(ServerWorld world) {
		var dam = Fauna.block("beaver_dam");
		if (dam == null) return;
		int near = 0;
		for (BlockPos p : BlockPos.iterate(getBlockPos().add(-8, -3, -8), getBlockPos().add(8, 3, 8))) {
			if (world.getBlockState(p).isOf(dam) && ++near >= 12) return;
		}
		for (BlockPos p : BlockPos.iterateOutwards(getBlockPos(), 4, 2, 4)) {
			if (world.getFluidState(p).isOf(net.minecraft.fluid.Fluids.WATER) && world.getBlockState(p).isOf(net.minecraft.block.Blocks.WATER)
					&& world.getBlockState(p.down()).isSolidBlock(world, p.down()) && !world.getBlockState(p.up()).isOf(net.minecraft.block.Blocks.WATER)
					&& world.random.nextInt(3) == 0) {
				world.setBlockState(p.toImmutable(), dam.getDefaultState());
				world.playSound(null, p, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.NEUTRAL, 0.8f, 0.9f);
				return;
			}
		}
	}

	/** A locust takes a growth stage off a crop near it. */
	private void eatCrop(ServerWorld world) {
		for (BlockPos p : BlockPos.iterateOutwards(getBlockPos(), 2, 1, 2)) {
			BlockState st = world.getBlockState(p);
			if (st.getBlock() instanceof CropBlock crop && crop.getAge(st) > 0) {
				world.setBlockState(p, crop.withAge(crop.getAge(st) - 1));
				world.playSound(null, p, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.NEUTRAL, 0.6f, 1.4f);
				return;
			}
		}
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return switch (species().body()) {
			case "bird" -> SoundEvents.ENTITY_PARROT_AMBIENT;
			case "frog" -> SoundEvents.ENTITY_FROG_AMBIENT;
			case "locust" -> SoundEvents.ENTITY_SILVERFISH_AMBIENT;
			case "scorpion" -> SoundEvents.ENTITY_SPIDER_AMBIENT;
			case "reptile" -> SoundEvents.ENTITY_TURTLE_AMBIENT_LAND;
			default -> null;
		};
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return switch (species().body()) {
			case "bird" -> SoundEvents.ENTITY_PARROT_HURT;
			case "frog" -> SoundEvents.ENTITY_FROG_HURT;
			case "scorpion", "locust" -> SoundEvents.ENTITY_SPIDER_HURT;
			case "reptile" -> SoundEvents.ENTITY_TURTLE_HURT_BABY;
			default -> species().width() >= 1.0f ? SoundEvents.ENTITY_COW_HURT : SoundEvents.ENTITY_GENERIC_HURT;
		};
	}

	@Override
	protected SoundEvent getDeathSound() {
		return species().width() >= 1.0f && species().body().equals("quad") ? SoundEvents.ENTITY_COW_DEATH : SoundEvents.ENTITY_GENERIC_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return species().width() < 0.5f ? 0.5f : 0.8f;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("ProductTimer", productTimer);
		nbt.putInt("AntlerTimer", antlerTimer);
		nbt.putInt("Cooldown", cooldown);
		nbt.putInt("WoolTimer", woolTimer);
		nbt.putBoolean("Sheared", isSheared());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("ProductTimer")) productTimer = nbt.getInt("ProductTimer");
		if (nbt.contains("AntlerTimer")) antlerTimer = nbt.getInt("AntlerTimer");
		cooldown = nbt.getInt("Cooldown");
		woolTimer = nbt.getInt("WoolTimer");
		dataTracker.set(SHEARED, nbt.getBoolean("Sheared"));
	}
}
