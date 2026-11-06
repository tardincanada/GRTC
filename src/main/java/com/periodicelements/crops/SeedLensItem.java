package com.periodicelements.crops;

import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

/**
 * Seed Lens (3.23.0, Domestication): used with seeds in the other hand, it marks them with their traits (after
 * Mendel's Laws, their genotype); used on a crop, it tells its health, climate, pollination and genes.
 */
public class SeedLensItem extends Item {
	public static final String UNLOCK = "item:seed_lens";
	public static final String GENOTYPE = "crop:genotype";
	public static final String DNA = "crop:dna", HYBRID = "crop:hybrid";

	public SeedLensItem(Settings settings) {
		super(settings);
	}

	/** 0 cannot use it, 1 traits, 2 genotype. */
	public static int level(PlayerEntity player) {
		if (player == null) {
			return 0;
		}
		if (player.isCreative()) {
			return Genome.STAMP_DNA;
		}
		Set<String> known = ResearchAccess.known(player);
		return Research.allows(known, DNA) ? Genome.STAMP_DNA : Research.allows(known, GENOTYPE) ? Genome.STAMP_GENOTYPE
				: Research.allows(known, UNLOCK) ? Genome.STAMP_TRAITS : 0;
	}

	/** Does the player know hybrid vigour (Hybrid Maize, level 5), to tell pure lines and F1 hybrids? */
	public static boolean knowsHybrids(PlayerEntity player) {
		return player != null && (player.isCreative() || Research.allows(ResearchAccess.known(player), HYBRID));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack lens = player.getStackInHand(hand);
		ItemStack seeds = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if (world.isClient) {
			return TypedActionResult.success(lens);
		}
		int level = level(player);
		if (level == 0) {
			player.sendMessage(Text.translatable("gui.periodic_elements.lens.needs"), true);
			return TypedActionResult.fail(lens);
		}
		if (!Crops.isSeed(seeds)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.lens.nothing"), true);
			return TypedActionResult.fail(lens);
		}
		analyse(seeds, level);
		player.sendMessage(Text.translatable("gui.periodic_elements.lens.analysed", seeds.getCount()), true);
		return TypedActionResult.success(lens);
	}

	public static void analyse(ItemStack seeds, int level) {
		int g = seeds.getOrDefault(Crops.GENOME, Genome.DEFAULT);
		seeds.set(Crops.GENOME, Genome.stamped(g, level));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		CropInfo info = Genetics.info(state.getBlock());
		PlayerEntity player = context.getPlayer();
		if (info == null || player == null) {
			return ActionResult.PASS;
		}
		if (world instanceof ServerWorld sw) {
			int level = level(player);
			if (level == 0) {
				player.sendMessage(Text.translatable("gui.periodic_elements.lens.needs"), true);
				return ActionResult.FAIL;
			}
			for (Text line : describe(sw, pos, state, info, level, knowsHybrids(player))) {
				player.sendMessage(line, false);
			}
		}
		return ActionResult.success(world.isClient);
	}

	public static List<Text> describe(ServerWorld world, BlockPos pos, BlockState state, CropInfo info, int level) {
		return describe(world, pos, state, info, level, false);
	}

	public static List<Text> describe(ServerWorld world, BlockPos pos, BlockState state, CropInfo info, int level, boolean hybrids) {
		List<Text> out = new java.util.ArrayList<>();
		boolean wild = state.contains(GeneticCropBlock.WILD) && state.get(GeneticCropBlock.WILD);
		Text name = state.getBlock().getName().formatted(Formatting.GREEN);
		out.add(wild ? Text.empty().append(name).append(" - ").append(Text.translatable("gui.periodic_elements.crop.wild")) : name);
		long entry = Genetics.entry(world, pos, state);
		if ((entry & CropGenomes.PEST) != 0) {
			out.add(Text.translatable("gui.periodic_elements.crop.pest").formatted(Formatting.RED));
		} else if ((entry & CropGenomes.DISEASE) != 0) {
			out.add(Text.translatable("gui.periodic_elements.crop.disease").formatted(Formatting.RED));
		} else {
			out.add(Text.translatable("gui.periodic_elements.crop.healthy"));
		}
		if (!wild) {
			if (info.water() && !Genetics.waterNear(world, pos)) {
				out.add(Text.translatable("gui.periodic_elements.crop.needs_water").formatted(Formatting.GOLD));
			} else {
				double m = Genetics.multiplier(world, pos, info, CropGenomes.of(CropGenomes.genome(entry), 0));
				out.add(m >= 1 ? Text.translatable("gui.periodic_elements.crop.climate_ok")
						: Text.translatable("gui.periodic_elements.crop.climate_slow", Math.round(m * 100)).formatted(Formatting.GOLD));
			}
		}
		if (info.cloned()) {
			out.add(Text.translatable("gui.periodic_elements.crop.clone"));
		} else if (Genetics.flowering(state)) {
			entry = Genetics.pollinate(world, pos, state, info, entry, world.random, true);
			int mother = CropGenomes.genome(entry);
			out.add(Text.translatable((entry & CropGenomes.HAND) != 0 ? "gui.periodic_elements.crop.hand_pollinated"
					: CropGenomes.father(entry) == mother ? "gui.periodic_elements.crop.selfed" : "gui.periodic_elements.crop.pollinated"));
		} else {
			out.add(Text.translatable("gui.periodic_elements.crop.not_pollinated"));
		}
		int g = CropGenomes.genome(entry);
		if (level >= Genome.STAMP_GENOTYPE) {
			out.add(Text.translatable("gui.periodic_elements.crop.genotype", Genome.genotype(g, level, info.stature())).formatted(Formatting.AQUA));
		}
		if (hybrids) {
			out.add(Text.translatable(Genome.hybrid(g) ? "gui.periodic_elements.crop.hybrid"
					: Genome.pure(g) ? "gui.periodic_elements.crop.pure_line" : "gui.periodic_elements.crop.mixed_line"));
		}
		if ((entry & CropGenomes.INOCULATED) != 0) {
			out.add(Text.translatable("gui.periodic_elements.crop.inoculated"));
		}
		Text traits = Genome.traits(g, level, info.stature());
		out.add(traits == null ? Text.translatable("gui.periodic_elements.crop.traits_none")
				: Text.translatable("gui.periodic_elements.crop.traits", traits).formatted(Formatting.AQUA));
		return out;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}

	static boolean isCrop(BlockState state) {
		return state.getBlock() instanceof CropBlock && Genetics.info(state.getBlock()) != null;
	}
}
