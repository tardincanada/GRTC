package com.periodicelements.crops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * DNA Sequencer (3.37.0, level 7): use seeds on it, with power joined to it, to read their whole genome: every gene,
 * letter by letter, and any transgene. The seeds keep the reading in their description.
 */
public class DnaSequencerBlock extends Block {
	public static final double KJ_PER_SEED = 20;

	public DnaSequencerBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!Crops.isSeed(stack)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world instanceof ServerWorld sw) {
			if (!CropMachineBlockEntity.power(sw, pos, KJ_PER_SEED * stack.getCount())) {
				player.sendMessage(Text.translatable("gui.periodic_elements.dna_sequencer.no_power", (int) (KJ_PER_SEED * stack.getCount())), true);
				return ItemActionResult.FAIL;
			}
			sequence(stack);
			int g = stack.getOrDefault(Crops.GENOME, Genome.DEFAULT);
			player.sendMessage(Text.translatable("gui.periodic_elements.dna_sequencer.read", stack.getName(),
					Genome.genotype(g, Genome.STAMP_SEQUENCE, Crops.hasStature(stack))).formatted(Formatting.AQUA), false);
			Text traits = Genome.traits(g, Genome.STAMP_SEQUENCE, Crops.hasStature(stack));
			if (traits != null) {
				player.sendMessage(Text.translatable("gui.periodic_elements.crop.traits", traits), false);
			}
		}
		return ItemActionResult.success(world.isClient);
	}

	public static void sequence(ItemStack stack) {
		int g = stack.getOrDefault(Crops.GENOME, Genome.DEFAULT);
		Genetics.setGenome(stack, Genome.stamped(g, Genome.STAMP_SEQUENCE));
	}
}
