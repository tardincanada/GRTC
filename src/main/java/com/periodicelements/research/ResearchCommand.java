package com.periodicelements.research;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * /research                         your level and projects
 * /research list [player]           same, for another player
 * /research grant|revoke player id  (operators) add or remove a project, or "all", or "level:N"
 */
public final class ResearchCommand {
	private static final SuggestionProvider<ServerCommandSource> PROJECTS = (ctx, builder) ->
			CommandSource.suggestMatching(Stream.concat(Stream.of("all", "level:1", "level:2"),
					Research.all().stream().map(Research.Project::id)), builder);

	private ResearchCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("research")
				.executes(ctx -> list(ctx, ctx.getSource().getPlayerOrThrow()))
				.then(CommandManager.literal("list")
						.executes(ctx -> list(ctx, ctx.getSource().getPlayerOrThrow()))
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(ctx -> list(ctx, EntityArgumentType.getPlayer(ctx, "player")))))
				.then(CommandManager.literal("grant").requires(s -> s.hasPermissionLevel(2))
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("project", StringArgumentType.greedyString()).suggests(PROJECTS)
										.executes(ctx -> change(ctx, true)))))
				.then(CommandManager.literal("revoke").requires(s -> s.hasPermissionLevel(2))
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("project", StringArgumentType.greedyString()).suggests(PROJECTS)
										.executes(ctx -> change(ctx, false))))));
	}

	private static int list(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
		Set<String> known = ResearchState.get(ctx.getSource().getServer()).known(player.getUuid());
		int level = Research.techLevel(known);
		ctx.getSource().sendFeedback(() -> Text.translatable("command.periodic_elements.research.level",
				player.getName(), level, Text.translatable("level.periodic_elements." + level)).formatted(Formatting.GOLD), false);
		for (Research.Project p : Research.all()) {
			if (p.level() > level + 1) {
				continue;
			}
			Research.Status status = Research.status(p, known);
			Formatting color = switch (status) {
				case DONE -> Formatting.GREEN;
				case AVAILABLE -> Formatting.YELLOW;
				default -> Formatting.DARK_GRAY;
			};
			MutableText line = Text.literal((p.key() ? " * " : "   ") + "L" + p.level() + " ").append(p.name())
					.append(Text.literal(" [" + p.id() + "]").formatted(Formatting.DARK_GRAY));
			ctx.getSource().sendFeedback(() -> line.formatted(color), false);
		}
		return known.size();
	}

	private static int change(CommandContext<ServerCommandSource> ctx, boolean grant) throws CommandSyntaxException {
		String arg = StringArgumentType.getString(ctx, "project");
		List<String> ids = new ArrayList<>();
		if (arg.equals("all")) {
			Research.all().forEach(p -> ids.add(p.id()));
		} else if (arg.startsWith("level:")) {
			int lvl;
			try {
				lvl = Integer.parseInt(arg.substring(6));
			} catch (NumberFormatException e) {
				lvl = -1;
			}
			final int max = lvl;
			Research.all().stream().filter(p -> p.level() <= max && p.key()).forEach(p -> ids.add(p.id()));
		} else if (Research.get(arg) != null) {
			ids.add(arg);
		}
		if (ids.isEmpty()) {
			ctx.getSource().sendError(Text.translatable("command.periodic_elements.research.unknown", arg));
			return 0;
		}
		Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(ctx, "players");
		ResearchState state = ResearchState.get(ctx.getSource().getServer());
		int total = 0;
		for (ServerPlayerEntity p : players) {
			total += grant ? state.grant(ctx.getSource().getServer(), p.getUuid(), ids)
					: state.revoke(ctx.getSource().getServer(), p.getUuid(), ids);
		}
		int n = total;
		ctx.getSource().sendFeedback(() -> Text.translatable(grant ? "command.periodic_elements.research.granted"
				: "command.periodic_elements.research.revoked", n, players.size()), true);
		return total;
	}
}
