package com.periodicelements.explore;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Animal traps (3.47.0): throw an empty trap at an animal to catch it, throw the full trap to let it out; the trap
 * drops where it lands either way. The Rope Net holds small animals (the trap_small entity tag) and wears out; the
 * Steel Cage holds any animal. Neither takes monsters, villagers, bosses, ridden animals or another player's pet.
 */
public class TrapItem extends Item {
	public static final int NET_USES = 48;
	private final boolean cage;

	public TrapItem(Settings settings, boolean cage) {
		super(settings);
		this.cage = cage;
	}

	public boolean cage() {
		return cage;
	}

	public static boolean full(ItemStack stack) {
		return stack.contains(Explore.TRAPPED);
	}

	/** Can this trap hold this entity, thrown by this player? */
	public boolean canHold(Entity e, PlayerEntity thrower) {
		if (!(e instanceof AnimalEntity) || e instanceof Monster || !e.isAlive() || e.hasPassengers() || e.hasVehicle()) {
			return false;
		}
		if (e instanceof TameableEntity t && t.isTamed() && (thrower == null || !thrower.getUuid().equals(t.getOwnerUuid()))) {
			return false;
		}
		return cage || e.getType().isIn(Explore.SMALL_ANIMALS);
	}

	/** The entity's saved state for the trap (without its UUID and place). */
	public static NbtCompound capture(Entity e) {
		NbtCompound nbt = new NbtCompound();
		e.saveSelfNbt(nbt);
		nbt.remove("UUID");
		nbt.remove("Pos");
		nbt.remove("Motion");
		nbt.remove("Leash");
		return nbt;
	}

	/** What a full trap holds, for its name and tooltip. */
	public static Text heldName(ItemStack stack) {
		NbtComponent c = stack.get(Explore.TRAPPED);
		if (c == null) {
			return null;
		}
		NbtCompound nbt = c.copyNbt();
		if (nbt.contains("CustomName")) {
			Text custom = Text.Serialization.fromJson(nbt.getString("CustomName"), net.minecraft.registry.DynamicRegistryManager.EMPTY);
			if (custom != null) {
				return custom;
			}
		}
		return EntityType.get(nbt.getString("id")).map(EntityType::getName).orElse(Text.literal(nbt.getString("id")));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
				0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
		if (!world.isClient) {
			TrapEntity trap = new TrapEntity(world, user, stack.copyWithCount(1));
			trap.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, cage ? 1.0f : 1.4f, 1.0f);
			world.spawnEntity(trap);
		}
		user.getItemCooldownManager().set(this, 10);
		if (!user.getAbilities().creativeMode) {
			stack.decrement(1);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public Text getName(ItemStack stack) {
		Text held = heldName(stack);
		return held == null ? super.getName(stack) : Text.translatable("item.periodic_elements.trap.full_name", super.getName(stack), held);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		Text held = heldName(stack);
		if (held != null) {
			tooltip.add(Text.translatable("item.periodic_elements.trap.holds", held).formatted(Formatting.GREEN));
		}
	}

	static Identifier idOf(ItemStack stack) {
		NbtComponent c = stack.get(Explore.TRAPPED);
		return c == null ? null : Identifier.tryParse(c.copyNbt().getString("id"));
	}
}
