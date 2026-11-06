package com.periodicelements.crops;

import com.periodicelements.research.Research;
import com.periodicelements.research.ResearchAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Pollination Brush (3.23.0, Plant Hybridization): take pollen from a flowering plant, then brush it onto another
 * plant of the same kind: that plant's seeds will all have the first plant as their father.
 */
public class PollinationBrushItem extends Item {
	public static final String UNLOCK = "item:pollination_brush";

	public PollinationBrushItem(Settings settings) {
		super(settings);
	}

	public static boolean allowed(PlayerEntity player) {
		return player != null && (player.isCreative() || Research.allows(ResearchAccess.known(player), UNLOCK));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		CropInfo info = Genetics.info(state.getBlock());
		PlayerEntity player = context.getPlayer();
		if (info == null || player == null || info.cloned()) {
			return ActionResult.PASS;
		}
		if (!(world instanceof ServerWorld sw)) {
			return ActionResult.SUCCESS;
		}
		if (!allowed(player)) {
			player.sendMessage(Text.translatable("gui.periodic_elements.brush.needs"), true);
			return ActionResult.FAIL;
		}
		ItemStack brush = context.getStack();
		NbtCompound pollen = brush.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		if (!pollen.contains("Species")) {
			if (!Genetics.flowering(state)) {
				player.sendMessage(Text.translatable("gui.periodic_elements.brush.not_ripe"), true);
				return ActionResult.FAIL;
			}
			pollen.putString("Species", Registries.BLOCK.getId(state.getBlock()).toString());
			pollen.putInt("Pollen", CropGenomes.genome(Genetics.entry(sw, pos, state)));
			NbtComponent.set(DataComponentTypes.CUSTOM_DATA, brush, pollen);
			player.sendMessage(Text.translatable("gui.periodic_elements.brush.collected", state.getBlock().getName()), true);
			return ActionResult.SUCCESS;
		}
		Block species = Registries.BLOCK.get(Identifier.of(pollen.getString("Species")));
		if (species != state.getBlock()) {
			player.sendMessage(Text.translatable("gui.periodic_elements.brush.wrong_species", species.getName()), true);
			return ActionResult.FAIL;
		}
		brushOnto(sw, pos, state, pollen.getInt("Pollen"));
		brush.remove(DataComponentTypes.CUSTOM_DATA);
		brush.damage(1, player, net.minecraft.entity.LivingEntity.getSlotForHand(context.getHand()));
		player.sendMessage(Text.translatable("gui.periodic_elements.brush.applied", species.getName()), true);
		return ActionResult.SUCCESS;
	}

	/** Makes {@code father} the father of the plant's seeds. */
	public static void brushOnto(ServerWorld world, BlockPos pos, BlockState state, int father) {
		long entry = Genetics.entry(world, pos, state);
		CropGenomes.of(world).put(pos, CropGenomes.withFather(entry, father, true));
		world.spawnParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.25, 0.2, 0.25, 0);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return stack.contains(DataComponentTypes.CUSTOM_DATA);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
