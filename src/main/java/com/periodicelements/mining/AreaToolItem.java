package com.periodicelements.mining;

import com.periodicelements.PeriodicElements;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

/**
 * Sledgehammer (pickaxe blocks) and Excavator (shovel blocks), from level 2 steel on: 3x3 square across the face you
 * hit, at {@link #SLOW} of the speed (player.block_break_speed, applied after Efficiency) and one wear per block.
 */
public class AreaToolItem extends MiningToolItem implements AreaMining.AreaTool {
	/** Break-speed multiplier the plan approved: x0.3. */
	public static final double SLOW = 0.3;
	private final boolean hammer;

	public AreaToolItem(ToolMaterial material, boolean hammer, Settings settings) {
		super(material, hammer ? BlockTags.PICKAXE_MINEABLE : BlockTags.SHOVEL_MINEABLE, settings);
		this.hammer = hammer;
	}

	public static AttributeModifiersComponent modifiers(ToolMaterial material, float base, float speed) {
		return MiningToolItem.createAttributeModifiers(material, base, speed).with(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED,
				new EntityAttributeModifier(Identifier.of(PeriodicElements.MOD_ID, "area_tool_slow"), SLOW - 1,
						EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), AttributeModifierSlot.MAINHAND);
	}

	public boolean hammer() {
		return hammer;
	}

	@Override
	public String unlock() {
		return hammer ? "item:sledgehammers" : "item:excavators";
	}

	@Override
	public List<BlockPos> targets(ItemStack stack, ServerPlayerEntity player, ServerWorld world, BlockPos origin, Direction side) {
		return AreaMining.square(origin, side, 1);
	}

	@Override
	public boolean pay(ItemStack stack, ServerPlayerEntity player, ServerWorld world, BlockState state, float hardness) {
		if (!player.getAbilities().creativeMode) {
			stack.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
		}
		return true;
	}

	@Override
	public void afterSwing(ItemStack stack, ServerPlayerEntity player, int broken) {
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.periodic_elements." + (hammer ? "sledgehammer" : "excavator")).formatted(Formatting.GRAY));
	}
}
