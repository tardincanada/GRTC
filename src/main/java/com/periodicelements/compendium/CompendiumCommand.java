package com.periodicelements.compendium;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * /compendium reveal [player]   (operators) note every tree and biome, for screenshots and tests
 * /compendium forget [player]   (operators) forget all notes
 */
public final class CompendiumCommand {
	private CompendiumCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> d) {
		d.register(CommandManager.literal("compendium").requires(s -> s.hasPermissionLevel(2))
				.then(CommandManager.literal("reveal")
						.executes(c -> reveal(c.getSource(), c.getSource().getPlayerOrThrow()))
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(c -> reveal(c.getSource(), EntityArgumentType.getPlayer(c, "player")))))
				.then(CommandManager.literal("forget")
						.executes(c -> forget(c.getSource(), c.getSource().getPlayerOrThrow()))
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(c -> forget(c.getSource(), EntityArgumentType.getPlayer(c, "player"))))));
	}

	private static int reveal(ServerCommandSource src, ServerPlayerEntity p) {
		int n = Compendium.revealAll(p);
		src.sendFeedback(() -> Text.translatable("command.periodic_elements.compendium.revealed", n, p.getName()), true);
		return n;
	}

	private static int forget(ServerCommandSource src, ServerPlayerEntity p) {
		DiscoveryState.get(p.getServer()).clear(p.getUuid());
		Compendium.sync(p);
		src.sendFeedback(() -> Text.translatable("command.periodic_elements.compendium.forgotten", p.getName()), true);
		return 1;
	}
}
