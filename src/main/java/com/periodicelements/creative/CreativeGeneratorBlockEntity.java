package com.periodicelements.creative;

import com.google.gson.JsonObject;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.machine.Reactions;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * 3.51.0: Creative Generator. Gives current without limit to everything wired to it (or touching it), like a
 * dynamo that never runs down. Machines take what they need; nothing is stored or used up.
 */
public class CreativeGeneratorBlockEntity extends PowerBlockEntity {
	/** kJ a tick any one consumer may draw: far more than any machine of the mod asks for. */
	public static final double OUTPUT = 1_000_000;
	private static final Reactions.PowerSpec SPEC = makeSpec();

	public CreativeGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(CreativeItems.GENERATOR_ENTITY, pos, state, SPEC);
	}

	private static Reactions.PowerSpec makeSpec() {
		JsonObject s = new JsonObject();
		s.addProperty("output", OUTPUT);
		s.addProperty("store", OUTPUT);
		return new Reactions.PowerSpec("creative_generator", 0, null, "creative", s);
	}

	@Override
	public boolean givesCurrent() {
		return true;
	}

	@Override
	public double stored() {
		return OUTPUT;
	}

	@Override
	public double available() {
		return OUTPUT;
	}

	@Override
	public double draw(double kJ) {
		return Math.max(0, Math.min(kJ, OUTPUT));
	}

	@Override
	public boolean hungry() {
		return false;
	}

	@Override
	public int[] getAvailableSlots(net.minecraft.util.math.Direction side) {
		return new int[0];
	}

	@Override
	public void tick(ServerWorld world, BlockPos pos, BlockState state) {
	}

	@Override
	public Text describe() {
		return Text.translatable("gui.periodic_elements.creative_generator", String.format(java.util.Locale.ROOT, "%,.0f", OUTPUT));
	}
}
