package com.periodicelements.explore;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/**
 * The Explorer's Compass (3.47.0): sneak and use it to choose a biome (every biome this world makes, in turn), use it
 * to find the nearest one within {@link #RADIUS} blocks; the needle then points there and the tooltip gives the
 * distance.
 */
public class ExplorerCompassItem extends Item {
	public static final int RADIUS = 6400;
	public static final int COOLDOWN = 40;

	public ExplorerCompassItem(Settings settings) {
		super(settings);
	}

	/** Every biome this world's generator can make, by id, sorted. */
	public static List<Identifier> biomes(ServerWorld world) {
		List<Identifier> out = new ArrayList<>();
		for (RegistryEntry<Biome> b : world.getChunkManager().getChunkGenerator().getBiomeSource().getBiomes()) {
			b.getKey().ifPresent(k -> out.add(k.getValue()));
		}
		out.sort(java.util.Comparator.comparing(Identifier::toString));
		return out;
	}

	public static Text biomeName(String id) {
		Identifier i = Identifier.tryParse(id);
		return i == null ? Text.literal(id) : Text.translatable(Util.createTranslationKey("biome", i));
	}

	/** Chooses the biome after the current one. */
	public static Identifier next(ServerWorld world, ItemStack stack) {
		List<Identifier> all = biomes(world);
		if (all.isEmpty()) {
			return null;
		}
		String cur = stack.get(Explore.COMPASS_BIOME);
		int i = cur == null ? -1 : all.indexOf(Identifier.tryParse(cur));
		Identifier n = all.get((i + 1) % all.size());
		stack.set(Explore.COMPASS_BIOME, n.toString());
		stack.remove(Explore.COMPASS_TARGET);
		return n;
	}

	/** Finds the nearest place of the chosen biome and points the compass there; null if none is in range. */
	public static BlockPos locate(ServerWorld world, BlockPos from, ItemStack stack) {
		String cur = stack.get(Explore.COMPASS_BIOME);
		if (cur == null) {
			return null;
		}
		Identifier want = Identifier.tryParse(cur);
		Pair<BlockPos, RegistryEntry<Biome>> found = world.locateBiome(
				b -> b.getKey().map(k -> k.getValue().equals(want)).orElse(false), from, RADIUS, 32, 64);
		if (found == null) {
			stack.remove(Explore.COMPASS_TARGET);
			return null;
		}
		stack.set(Explore.COMPASS_TARGET, GlobalPos.create(world.getRegistryKey(), found.getFirst()));
		return found.getFirst();
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!(world instanceof ServerWorld sw)) {
			return TypedActionResult.success(stack, true);
		}
		if (user.isSneaking()) {
			Identifier n = next(sw, stack);
			if (n != null) {
				user.sendMessage(Text.translatable("message.periodic_elements.explorer_compass.chosen", biomeName(n.toString())), true);
			}
			return TypedActionResult.success(stack, false);
		}
		String cur = stack.get(Explore.COMPASS_BIOME);
		if (cur == null) {
			user.sendMessage(Text.translatable("message.periodic_elements.explorer_compass.choose"), true);
			return TypedActionResult.fail(stack);
		}
		user.getItemCooldownManager().set(this, COOLDOWN);
		BlockPos at = locate(sw, user.getBlockPos(), stack);
		if (at == null) {
			user.sendMessage(Text.translatable("message.periodic_elements.explorer_compass.none", biomeName(cur), RADIUS), true);
		} else {
			int d = (int) Math.round(Math.sqrt(at.getSquaredDistance(user.getBlockPos().getX(), at.getY(), user.getBlockPos().getZ())));
			user.sendMessage(Text.translatable("message.periodic_elements.explorer_compass.found", biomeName(cur), d), true);
			world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.8f, 1.0f);
		}
		return TypedActionResult.success(stack, false);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		String cur = stack.get(Explore.COMPASS_BIOME);
		if (cur != null) {
			tooltip.add(Text.translatable("item.periodic_elements.explorer_compass.biome", biomeName(cur)).formatted(Formatting.DARK_AQUA));
		}
		GlobalPos t = stack.get(Explore.COMPASS_TARGET);
		if (t != null) {
			tooltip.add(Text.translatable("item.periodic_elements.explorer_compass.target", t.pos().getX(), t.pos().getZ())
					.formatted(Formatting.GREEN));
		}
	}
}
