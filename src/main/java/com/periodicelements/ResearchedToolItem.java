package com.periodicelements;

import com.periodicelements.radiation.Radioactivity;
import com.periodicelements.research.ResearchState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Level 7 handhelds that need their research: the Fibre Communicator (finds the other players on the network) and the
 * Element Analyzer (an alpha-particle X-ray spectrometer that reads every element in the block you look at).
 */
public class ResearchedToolItem extends Item {
	public enum Kind { COMMUNICATOR, ANALYZER }

	private final Kind kind;
	private final String project;

	public ResearchedToolItem(Settings settings, Kind kind, String project) {
		super(settings);
		this.kind = kind;
		this.project = project;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient || !(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.success(stack, world.isClient);
		}
		String ns = "message." + PeriodicElements.MOD_ID + ".";
		String what = kind == Kind.COMMUNICATOR ? "communicator" : "analyzer";
		if (!ResearchState.get(player.getServer()).known(player.getUuid()).contains(project)) {
			player.sendMessage(Text.translatable(ns + what + ".needs"), true);
			return TypedActionResult.fail(stack);
		}
		if (kind == Kind.COMMUNICATOR) {
			communicate(player);
		} else {
			analyze(player);
		}
		player.getItemCooldownManager().set(this, 20);
		return TypedActionResult.success(stack);
	}

	private static String compass(Vec3d d) {
		String[] dirs = {"south", "south-west", "west", "north-west", "north", "north-east", "east", "south-east"};
		double deg = Math.toDegrees(Math.atan2(-d.x, d.z));
		return dirs[(int) Math.floorMod(Math.round(deg / 45), 8)];
	}

	private static void communicate(ServerPlayerEntity player) {
		String ns = "message." + PeriodicElements.MOD_ID + ".communicator.";
		int n = 0;
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			if (other == player) {
				continue;
			}
			n++;
			if (other.getWorld() != player.getWorld()) {
				player.sendMessage(Text.translatable(ns + "other_world", other.getName(),
						other.getWorld().getRegistryKey().getValue().toString()), false);
			} else {
				Vec3d d = other.getPos().subtract(player.getPos());
				player.sendMessage(Text.translatable(ns + "player", other.getName(),
						String.format(Locale.ROOT, "%.0f", d.length()), compass(d)), false);
			}
		}
		if (n == 0) {
			player.sendMessage(Text.translatable(ns + "alone"), true);
		}
	}

	/** Every element in the block, by mass, from its composition. */
	public static Map<String, Double> elementsOfBlock(BlockState state) {
		return Radioactivity.elementsOf(Registries.BLOCK.getId(state.getBlock()).toString());
	}

	private static void analyze(ServerPlayerEntity player) {
		String ns = "message." + PeriodicElements.MOD_ID + ".analyzer.";
		HitResult hit = player.getWorld().raycast(new RaycastContext(player.getEyePos(),
				player.getEyePos().add(player.getRotationVec(1).multiply(5)), RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE, player));
		if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockState state = player.getWorld().getBlockState(bh.getBlockPos());
		Map<String, Double> els = elementsOfBlock(state);
		if (els.isEmpty()) {
			player.sendMessage(Text.translatable(ns + "nothing", state.getBlock().getName()), false);
			return;
		}
		double total = els.values().stream().mapToDouble(Double::doubleValue).sum();
		MutableText line = Text.empty();
		int i = 0;
		for (var e : els.entrySet()) {
			double pct = 100 * e.getValue() / total;
			if (i >= 8 || pct < 0.01) {
				break;
			}
			if (i++ > 0) {
				line.append(", ");
			}
			line.append(Text.translatable("item." + PeriodicElements.MOD_ID + "." + e.getKey()))
					.append(String.format(Locale.ROOT, " %.2f%%", pct));
		}
		player.sendMessage(Text.translatable(ns + "result", state.getBlock().getName(), line), false);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
