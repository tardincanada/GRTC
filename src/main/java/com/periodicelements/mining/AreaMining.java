package com.periodicelements.mining;

import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchState;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Breaking several blocks with one swing (2.1.0). The rules for every area tool: sneaking mines one block; only blocks
 * the tool is right for, that it can harvest and that are no harder than the one you aimed at break; nothing with a
 * block entity (machines, chests) ever breaks; drops go to your inventory first.
 */
public final class AreaMining {
	public static final int VEIN_MAX = 32;
	public static final int TREE_MAX = 128;

	private AreaMining() {
	}

	/** An item that mines an area: the hammers, the excavators, the drills and the chainsaw. */
	public interface AreaTool {
		/** The research unlock ("item:...") that lets the tool work more than one block. */
		String unlock();

		/** The blocks one swing would break, the aimed block first; a single entry means one block. */
		List<BlockPos> targets(ItemStack stack, ServerPlayerEntity player, ServerWorld world, BlockPos origin, Direction side);

		/** Pays for one block (wear or energy); false stops the swing before that block. */
		boolean pay(ItemStack stack, ServerPlayerEntity player, ServerWorld world, BlockState state, float hardness);

		/** Once per swing, after the blocks broke (bit wear). */
		void afterSwing(ItemStack stack, ServerPlayerEntity player, int broken);
	}

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
			if (!(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw) || player.isSneaking()) {
				return true;
			}
			ItemStack stack = player.getMainHandStack();
			if (!(stack.getItem() instanceof AreaTool tool)) {
				return true;
			}
			if (!knows(sp, tool.unlock()) || !(stack.getItem().getMiningSpeed(stack, state) > 1f && stack.isSuitableFor(state))) {
				return true;
			}
			Direction side = aimedSide(sp, pos);
			List<BlockPos> targets = tool.targets(stack, sp, sw, pos, side);
			if (targets.size() <= 1) {
				return true;
			}
			harvest(sp, stack, pos, side);
			return false; // we broke the aimed block ourselves, with its drops in the inventory
		});
	}

	public static boolean knows(ServerPlayerEntity player, String unlock) {
		return player.getAbilities().creativeMode || Research.allows(ResearchState.get(player.getServer()).known(player.getUuid()), unlock);
	}

	/** The project's display name for an unlock, for "learn X first" messages. */
	public static Text projectName(String unlock) {
		String p = Research.unlockedBy(unlock);
		return p == null ? Text.literal("?") : Research.get(p).name();
	}

	/** The face of {@code pos} the player is looking at (the top if the ray misses it). */
	public static Direction aimedSide(ServerPlayerEntity player, BlockPos pos) {
		HitResult hit = player.getWorld().raycast(new RaycastContext(player.getEyePos(),
				player.getEyePos().add(player.getRotationVec(1).multiply(8)), RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE, player));
		if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK && bh.getBlockPos().equals(pos)) {
			return bh.getSide();
		}
		return Direction.UP;
	}

	/** A square of side 2r+1 across the face {@code side} of {@code origin}, the origin first. */
	public static List<BlockPos> square(BlockPos origin, Direction side, int r) {
		List<BlockPos> out = new ArrayList<>();
		out.add(origin);
		Direction.Axis axis = side.getAxis();
		for (int a = -r; a <= r; a++) {
			for (int b = -r; b <= r; b++) {
				if (a == 0 && b == 0) {
					continue;
				}
				out.add(switch (axis) {
					case X -> origin.add(0, a, b);
					case Y -> origin.add(a, 0, b);
					case Z -> origin.add(a, b, 0);
				});
			}
		}
		return out;
	}

	/** A 1x3 tunnel face: the aimed block and the ones above and below (on a floor or ceiling, the two ahead). */
	public static List<BlockPos> tunnel(BlockPos origin, Direction side, Direction facing) {
		if (side.getAxis().isHorizontal()) {
			return List.of(origin, origin.up(), origin.down());
		}
		return List.of(origin, origin.offset(facing), origin.offset(facing, 2));
	}

	/** Every block joined to {@code origin} (faces, edges and corners) that passes the test, up to {@code max}. */
	public static List<BlockPos> connected(ServerWorld world, BlockPos origin, Predicate<BlockState> same, int max) {
		List<BlockPos> out = new ArrayList<>();
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		seen.add(origin);
		while (!queue.isEmpty() && out.size() < max) {
			BlockPos p = queue.poll();
			out.add(p);
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						BlockPos n = p.add(dx, dy, dz);
						if (seen.add(n) && same.test(world.getBlockState(n))) {
							queue.add(n);
						}
					}
				}
			}
		}
		return out;
	}

	/** Is this block an ore, for vein mode? */
	public static boolean ore(BlockState s) {
		return s.isIn(ConventionalBlockTags.ORES) || Registries.BLOCK.getId(s.getBlock()).getPath().endsWith("_ore");
	}

	public static boolean log(BlockState s) {
		return s.isIn(BlockTags.LOGS);
	}

	/**
	 * One swing: breaks the tool's targets round {@code origin} that pass the rules, paying for each, and returns how
	 * many broke. The aimed block always breaks (if the tool can pay for it).
	 */
	public static int harvest(ServerPlayerEntity player, ItemStack stack, BlockPos origin, Direction side) {
		ServerWorld world = player.getServerWorld();
		if (!(stack.getItem() instanceof AreaTool tool)) {
			return 0;
		}
		BlockState aimed = world.getBlockState(origin);
		float limit = aimed.getHardness(world, origin);
		int broken = 0;
		for (BlockPos p : tool.targets(stack, player, world, origin, side)) {
			BlockState s = world.getBlockState(p);
			boolean first = p.equals(origin);
			if (!first && !eligible(player, stack, world, p, s, limit)) {
				continue;
			}
			if (s.isAir()) {
				continue;
			}
			float hardness = s.getHardness(world, p);
			if (!tool.pay(stack, player, world, s, hardness)) {
				break;
			}
			breakInto(player, stack, world, p, s);
			broken++;
			if (stack.isEmpty()) {
				break;
			}
		}
		if (!stack.isEmpty()) {
			tool.afterSwing(stack, player, broken);
		}
		return broken;
	}

	private static boolean eligible(ServerPlayerEntity player, ItemStack stack, ServerWorld world, BlockPos p, BlockState s, float limit) {
		if (s.isAir() || s.hasBlockEntity() || !world.canPlayerModifyAt(player, p)) {
			return false;
		}
		float h = s.getHardness(world, p);
		if (h < 0 || h > limit + 1e-4f) {
			return false;
		}
		return stack.getItem().getMiningSpeed(stack, s) > 1f && stack.isSuitableFor(s);
	}

	/** Breaks the block as the player would, with its drops (and the tool's Fortune or Silk Touch) put in the inventory. */
	public static void breakInto(ServerPlayerEntity player, ItemStack stack, ServerWorld world, BlockPos p, BlockState s) {
		BlockEntity be = world.getBlockEntity(p);
		Block block = s.getBlock();
		BlockState broke = block.onBreak(world, p, s, player);
		boolean removed = world.removeBlock(p, false);
		if (removed) {
			block.onBroken(world, p, broke);
		}
		world.syncWorldEvent(2001, p, Block.getRawIdFromState(s));
		if (player.getAbilities().creativeMode || !removed) {
			return;
		}
		player.incrementStat(Stats.MINED.getOrCreateStat(block));
		player.addExhaustion(0.005f);
		if (s.isToolRequired() && !stack.isSuitableFor(s)) {
			return;
		}
		for (ItemStack drop : Block.getDroppedStacks(s, world, p, be, player, stack)) {
			player.getInventory().insertStack(drop);
			if (!drop.isEmpty()) {
				Block.dropStack(world, p, drop);
			}
		}
		s.onStacksDropped(world, p, stack, true);
	}
}
