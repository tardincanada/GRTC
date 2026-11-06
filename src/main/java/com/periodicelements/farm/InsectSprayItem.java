package com.periodicelements.farm;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Insect Spray (1.8.0): a puff of pyrethrum in a cone {@link #RANGE} blocks ahead. Insects and arachnids (spiders,
 * cave spiders, silverfish, endermites) take {@link #DAMAGE}; so do bees, which is pyrethrum's real cost.
 */
public class InsectSprayItem extends Item {
	public static final double RANGE = 4;
	public static final float DAMAGE = 6;
	/** Cosine of the cone's half angle (about 40 degrees). */
	public static final double CONE = 0.75;

	public InsectSprayItem(Settings settings) {
		super(settings);
	}

	public static boolean pest(LivingEntity e) {
		return e instanceof SpiderEntity || e instanceof SilverfishEntity || e instanceof EndermiteEntity || e instanceof BeeEntity;
	}

	/** Sprays from {@code user}; returns how many creatures were hit. */
	public static int spray(LivingEntity user) {
		World world = user.getWorld();
		Vec3d eye = user.getEyePos();
		Vec3d look = user.getRotationVector();
		int hit = 0;
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(eye, eye).expand(RANGE), InsectSprayItem::pest)) {
			Vec3d to = e.getBoundingBox().getCenter().subtract(eye);
			if (to.length() <= RANGE + 0.5 && to.normalize().dotProduct(look) >= CONE) {
				e.damage(world.getDamageSources().magic(), DAMAGE);
				hit++;
			}
		}
		if (world instanceof ServerWorld sw) {
			for (int i = 1; i <= 4; i++) {
				Vec3d p = eye.add(look.multiply(i));
				sw.spawnParticles(ParticleTypes.WHITE_SMOKE, p.x, p.y, p.z, 4, 0.2 * i, 0.2 * i, 0.2 * i, 0.01);
			}
		}
		return hit;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			spray(user);
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.4f, 1.8f);
			stack.damage(1, user, LivingEntity.getSlotForHand(hand));
			user.getItemCooldownManager().set(this, 10);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
