package com.periodicelements.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

/**
 * A shell or core block of a multiblock structure that shows an animated texture while the structure works (3.33.0):
 * glowing joints in a hot furnace, bubbling trays in a column, spinning pumps in a cascade, fuel channels glowing in a
 * reactor. The controller switches {@link #ACTIVE} through {@link Structures#animate}.
 */
public class AnimatedPartBlock extends Block {
	public static final BooleanProperty ACTIVE = com.periodicelements.machine.MachineBlock.ACTIVE;

	public AnimatedPartBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(ACTIVE, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}
}
