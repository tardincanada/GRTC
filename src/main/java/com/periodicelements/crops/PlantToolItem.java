package com.periodicelements.crops;

import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 3.37.0 tools used on a growing crop: the Inoculant (legumes fix twice the nitrogen), the Gene Gun (shoots the
 * transgene in the other hand into the plant) and the CRISPR Kit (rewrites one gene; sneak-use to choose which).
 */
public class PlantToolItem extends Item {
	public enum Kind { INOCULANT, GENE_GUN, CRISPR }

	/** The Gene Gun takes one shot in two. */
	public static final double GUN_SUCCESS = 0.5;
	private final Kind kind;
	private final String unlock;

	public PlantToolItem(Kind kind, String unlock, Settings settings) {
		super(settings);
		this.kind = kind;
		this.unlock = unlock;
	}

	public static int target(ItemStack kit) {
		return kit.getOrDefault(Crops.TARGET_GENE, 0);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		CropInfo info = Genetics.info(state.getBlock());
		PlayerEntity player = context.getPlayer();
		ItemStack stack = context.getStack();
		if (player == null) {
			return ActionResult.PASS;
		}
		if (kind == Kind.CRISPR && player.isSneaking()) {
			if (!world.isClient) {
				int next = (target(stack) + 1) % Genome.GENES;
				stack.set(Crops.TARGET_GENE, next);
				player.sendMessage(Text.translatable("gui.periodic_elements.crispr.target",
						Text.translatable("gene.periodic_elements." + Genome.LETTERS.charAt(next))), true);
			}
			return ActionResult.success(world.isClient);
		}
		if (info == null) {
			return ActionResult.PASS;
		}
		if (!(world instanceof ServerWorld sw)) {
			return ActionResult.SUCCESS;
		}
		String key = "gui.periodic_elements." + kind.name().toLowerCase(java.util.Locale.ROOT);
		if (!player.isCreative() && !Research.allows(ResearchAccess.known(player), unlock)) {
			player.sendMessage(Text.translatable(key + ".needs"), true);
			return ActionResult.FAIL;
		}
		long entry = Genetics.entry(sw, pos, state);
		int g = CropGenomes.genome(entry);
		switch (kind) {
			case INOCULANT -> {
				if (!info.legume()) {
					player.sendMessage(Text.translatable(key + ".not_legume"), true);
					return ActionResult.FAIL;
				}
				CropGenomes.of(sw).put(pos, entry | CropGenomes.INOCULATED);
				stack.decrementUnlessCreative(1, player);
				player.sendMessage(Text.translatable(key + ".done"), true);
			}
			case GENE_GUN -> {
				Hand other = context.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
				ItemStack construct = player.getStackInHand(other);
				if (!(construct.getItem() instanceof GeneConstructItem c)) {
					player.sendMessage(Text.translatable(key + ".empty"), true);
					return ActionResult.FAIL;
				}
				if (c.transgene() == Genome.GOLDEN && !info.id().equals("rice")) {
					player.sendMessage(Text.translatable(key + ".rice_only"), true);
					return ActionResult.FAIL;
				}
				Text name = construct.getName();
				construct.decrementUnlessCreative(1, player);
				stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
				if (sw.random.nextDouble() < GUN_SUCCESS) {
					CropGenomes.of(sw).put(pos, CropGenomes.of(Genome.withTransgene(g, c.transgene()), entry));
					player.sendMessage(Text.translatable(key + ".took", name), true);
					sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0);
				} else {
					player.sendMessage(Text.translatable(key + ".missed"), true);
					sw.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0);
				}
			}
			case CRISPR -> {
				int gene = target(stack);
				if (gene == Genome.STATURE && !info.stature()) {
					player.sendMessage(Text.translatable(key + ".no_stature"), true);
					return ActionResult.FAIL;
				}
				CropGenomes.of(sw).put(pos, CropGenomes.of(Genome.useful(g, gene), entry));
				stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
				player.sendMessage(Text.translatable(key + ".done", Text.translatable("gene.periodic_elements." + Genome.LETTERS.charAt(gene)),
						Text.translatable("gene.periodic_elements." + Genome.LETTERS.charAt(gene) + ".good")), true);
				sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
		if (kind == Kind.CRISPR) {
			tooltip.add(Text.translatable("gui.periodic_elements.crispr.target",
					Text.translatable("gene.periodic_elements." + Genome.LETTERS.charAt(target(stack)))).formatted(Formatting.AQUA));
		}
	}
}
