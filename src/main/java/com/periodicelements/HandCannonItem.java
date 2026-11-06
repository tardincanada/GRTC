package com.periodicelements;

import com.google.gson.JsonObject;
import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * A 14th-century hand cannon: each shot burns one black powder and fires one lead ball. Using it needs the
 * Black Powder research; it then cools down for a few seconds.
 */
public class HandCannonItem extends Item {
	private final String project;
	private final Item powder;
	private final Item shot;
	private final int cooldown;
	private final float velocity;
	private final double damage;
	public static final double CORDITE_BONUS = 1.25;

	public HandCannonItem(Settings settings, JsonObject spec) {
		super(settings);
		this.project = spec.get("project").getAsString();
		this.powder = Registries.ITEM.get(Identifier.of(spec.get("powder").getAsString()));
		this.shot = Registries.ITEM.get(Identifier.of(spec.get("shot").getAsString()));
		this.cooldown = spec.get("cooldown").getAsInt();
		this.velocity = spec.get("velocity").getAsFloat();
		this.damage = spec.get("damage").getAsDouble();
	}

	private static int find(PlayerEntity player, Item item) {
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isOf(item)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient) {
			return TypedActionResult.consume(stack);
		}
		boolean creative = user.getAbilities().creativeMode;
		if (!creative && !ResearchState.get(world.getServer()).known(user.getUuid()).contains(project)) {
			user.sendMessage(Text.translatable("message.periodic_elements.needs_research",
					Research.get(project).name()), true);
			return TypedActionResult.fail(stack);
		}
		// The Breech-Loading Rifle also takes Cordite Cartridges (1.8.0): 25% harder and no smoke.
		Item cartridge = Registries.ITEM.get(Identifier.of("periodic_elements", "cordite_cartridge"));
		int c = powder == Registries.ITEM.get(Identifier.of("periodic_elements", "smokeless_powder")) ? find(user, cartridge) : -1;
		int p = find(user, powder);
		int s = find(user, shot);
		if (!creative && c < 0 && (p < 0 || s < 0)) {
			user.sendMessage(Text.translatable("message.periodic_elements.gun.empty", powder.getName(), shot.getName()), true);
			return TypedActionResult.fail(stack);
		}
		if (!creative && c >= 0) {
			user.getInventory().getStack(c).decrement(1);
		} else if (!creative) {
			user.getInventory().getStack(p).decrement(1);
			user.getInventory().getStack(s).decrement(1);
		}
		ArrowEntity ball = new ArrowEntity(world, user, new ItemStack(Items.ARROW), stack.copy());
		ball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, velocity, 2.5f);
		ball.setDamage(c >= 0 ? damage * CORDITE_BONUS : damage);
		ball.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
		world.spawnEntity(ball);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
				SoundCategory.PLAYERS, 0.7f, 1.7f);
		Vec3d muzzle = user.getEyePos().add(user.getRotationVector().multiply(1.2));
		if (c < 0) ((ServerWorld) world).spawnParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 8, 0.1, 0.1, 0.1, 0.02);
		stack.damage(1, user, LivingEntity.getSlotForHand(hand));
		user.getItemCooldownManager().set(this, cooldown);
		return TypedActionResult.success(stack, false);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
