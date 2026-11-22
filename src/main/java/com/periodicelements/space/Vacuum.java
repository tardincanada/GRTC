package com.periodicelements.space;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Set;

/**
 * 3.65.0: life without air on other worlds. Nothing burns where there is no oxygen (fire, furnaces, fuel-burning
 * machines and generators, torches and candles), except inside a sealed room kept full of air by Life Support; and the
 * bare sky lets cosmic rays through, which a few blocks overhead stop.
 */
public final class Vacuum {
	/** Cosmic rays under an open sky, Sv/h: about 1700 times what the Moon's surface really gets, so it tells in play. */
	public static final double COSMIC_SV_PER_H = 0.1;
	/** How many solid blocks overhead are counted; each halves the dose. */
	public static final int SHIELD_DEPTH = 8;

	/** Blocks that are a flame: they cannot be placed where there is no air. */
	private static final Set<String> FLAMES = Set.of("minecraft:torch", "minecraft:soul_torch", "minecraft:campfire",
			"minecraft:soul_campfire", "minecraft:lantern", "minecraft:soul_lantern", "periodic_elements:tallow_candle");

	private Vacuum() {
	}

	/** Is there oxygen at {@code pos}: any world with air, or a sealed room (the block itself or one beside it)? */
	public static boolean hasAir(World world, BlockPos pos) {
		if (!Moon.airless(world)) {
			return true;
		}
		if (LifeSupportBlockEntity.pressurized(world, pos)) {
			return true;
		}
		for (Direction d : Direction.values()) {
			if (LifeSupportBlockEntity.pressurized(world, pos.offset(d))) {
				return true;
			}
		}
		return false;
	}

	static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			ItemStack held = player.getStackInHand(hand);
			if (!Moon.airless(world) || held.isEmpty() || player.isSpectator()) {
				return ActionResult.PASS;
			}
			BlockPos target = hit.getBlockPos();
			BlockPos placed = world.getBlockState(target).isReplaceable() ? target : target.offset(hit.getSide());
			boolean igniter = held.isOf(Items.FLINT_AND_STEEL) || held.isOf(Items.FIRE_CHARGE);
			boolean flame = held.getItem() instanceof BlockItem bi && (FLAMES.contains(Registries.BLOCK.getId(bi.getBlock()).toString())
					|| bi.getBlock().getDefaultState().isIn(BlockTags.CANDLES));
			if ((igniter && !hasAir(world, target) && !hasAir(world, placed)) || (flame && !hasAir(world, placed))) {
				noFire(player);
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});
	}

	static void noFire(PlayerEntity player) {
		player.sendMessage(Text.translatable("message.periodic_elements.vacuum.no_fire").formatted(Formatting.RED), true);
	}

	/** A fire that finds itself without air goes out (fire blocks call this from their tick). */
	public static boolean snuffed(World world, BlockPos pos, BlockState state) {
		return !hasAir(world, pos);
	}

	/** Cosmic rays reaching {@code player}'s head, Sv/h: none on the Earth; halved by each solid block above. */
	public static double cosmic(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		if (!Moon.airless(world)) {
			return 0;
		}
		// 3.66.0: Mars's thin air stops some of it: Curiosity measured about half the Moon's dose on the ground
		double sky = Mars.is(world) ? COSMIC_SV_PER_H * 0.5 : COSMIC_SV_PER_H;
		if (Venus.is(world)) {   // 3.68.0: 50 km up, Venus still has more air above it than the Earth has in all
			return 0;
		}
		if (Mercury.is(world)) {   // no air, a weak field, and the Sun close by: its wind adds to the cosmic rays
			sky = COSMIC_SV_PER_H * Mercury.COSMIC;
		}
		BlockPos head = BlockPos.ofFloored(player.getEyePos());
		// 3.67.0 (fiction): a Magnetic Shield's field turns most of the particles aside
		double field = Fiction.shielded(world, head) ? Fiction.SHIELD_PASS : 1;
		return sky * field * Math.pow(0.5, cover(world, head));
	}

	/** Opaque full blocks straight above {@code head}, up to {@link #SHIELD_DEPTH}. */
	public static int cover(World world, BlockPos head) {
		int n = 0;
		BlockPos.Mutable p = head.mutableCopy();
		for (int y = head.getY() + 1; y < world.getTopY() && n < SHIELD_DEPTH; y++) {
			p.setY(y);
			BlockState s = world.getBlockState(p);
			if (s.isOpaqueFullCube(world, p)) {   // glass is too thin to count
				n++;
			}
		}
		return n;
	}
}
