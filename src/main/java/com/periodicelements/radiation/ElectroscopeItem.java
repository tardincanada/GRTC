package com.periodicelements.radiation;

import com.periodicelements.research.ResearchState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * A gold-leaf electroscope: radiation ionizes the air and the charged leaves fall together. Using it reads the dose
 * rate where you stand, your unrepaired dose and your lifetime dose.
 */
public class ElectroscopeItem extends Item {
	private final String project;
	private final String name;
	private final int cooldown;

	/** {@code project} must be known to use it; {@code name} picks its messages. */
	public ElectroscopeItem(Settings settings, String name, String project, int cooldown) {
		super(settings);
		this.name = name;
		this.project = project;
		this.cooldown = cooldown;
	}

	public String project() {
		return project;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient || !(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.success(stack, world.isClient);
		}
		var server = player.getServer();
		if (!ResearchState.get(server).known(player.getUuid()).contains(project)) {
			player.sendMessage(Text.translatable("message.periodic_elements." + name + ".needs"), true);
			return TypedActionResult.fail(stack);
		}
		Radiation.Mode mode = Radiation.mode(server);
		if (mode == Radiation.Mode.off) {
			player.sendMessage(Text.translatable("message.periodic_elements.electroscope.off"), true);
			return TypedActionResult.success(stack);
		}
		report(player, mode);
		com.periodicelements.atlas.Prospecting.logRadiation(player);
		if (cooldown > 0) {
			player.getItemCooldownManager().set(this, cooldown);
		}
		return TypedActionResult.success(stack);
	}

	/** Shows the dose rate here, the unrepaired dose and the lifetime dose on the action bar. */
	static void report(ServerPlayerEntity player, Radiation.Mode mode) {
		double rate = Radiation.doseRate(player) * (mode == Radiation.Mode.milder ? Radioactivity.setting("milderFactor") : 1);
		var d = RadiationState.get(player.getServer()).dose(player.getUuid(), System.currentTimeMillis());
		player.sendMessage(Text.translatable("message.periodic_elements.electroscope", Radioactivity.formatRate(rate),
				Radioactivity.format(d.recent), Radioactivity.format(d.lifetime)), true);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(getTranslationKey() + ".tip").formatted(Formatting.GRAY));
	}
}
