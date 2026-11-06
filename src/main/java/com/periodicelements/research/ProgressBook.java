package com.periodicelements.research;

import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * The Book of Progress (3.20.0): a testing aid found only in the creative inventory (no recipe, no loot). Using it opens
 * a page with the eight tech levels; picking one sets the reader's research to exactly that level: every key project
 * (and, unless turned off, every optional project) of that level and the levels below it, and nothing of later levels.
 * It works in survival too, so a tester can pick a level in creative and switch to survival, where research gates apply.
 */
public final class ProgressBook {
	public static Item BOOK;

	private ProgressBook() {
	}

	/** Client to server: set my research to this level, with or without the optional projects. */
	public record SetLevel(int level, boolean optional) implements CustomPayload {
		public static final Id<SetLevel> ID = new Id<>(Identifier.of(PeriodicElements.MOD_ID, "progress_book_set"));
		public static final PacketCodec<RegistryByteBuf, SetLevel> CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_INT, SetLevel::level, PacketCodecs.BOOL, SetLevel::optional, SetLevel::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static class BookItem extends ModContent.TippedItem {
		/** Set by the client initializer; does nothing on a dedicated server. */
		public static Runnable open = () -> {
		};

		public BookItem(Settings settings) {
			super(settings);
		}

		@Override
		public boolean hasGlint(ItemStack stack) {
			return true;
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (world.isClient) {
				open.run();
			}
			return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
		}
	}

	/** Registers the item; it goes in the Workshop creative tab with the rest, but nothing crafts or drops it. */
	public static void register(List<Item> items) {
		BOOK = Registry.register(Registries.ITEM, Identifier.of(PeriodicElements.MOD_ID, "progress_book"),
				new BookItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
		items.add(BOOK);
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(SetLevel.ID, SetLevel.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetLevel.ID, (p, ctx) -> {
			ServerPlayerEntity player = ctx.player();
			// Only someone holding the book may use it (the packet alone is not enough).
			if (player.getMainHandStack().isOf(BOOK) || player.getOffHandStack().isOf(BOOK)) {
				apply(player, p.level(), p.optional());
			}
		});
	}

	/** The projects a player at exactly {@code level} knows: key projects up to it, plus optional ones if asked. */
	public static List<String> projectsFor(int level, boolean optional) {
		List<String> ids = new ArrayList<>();
		for (Research.Project p : Research.all()) {
			if (p.level() <= level && (optional || p.key())) {
				ids.add(p.id());
			}
		}
		return ids;
	}

	/** Sets the research of the player with this UUID (online or not) to the given level; returns what they now know. */
	public static List<String> setLevel(net.minecraft.server.MinecraftServer server, java.util.UUID player, int level,
										boolean optional) {
		List<String> ids = projectsFor(Math.max(0, Math.min(Research.MAX_LEVEL, level)), optional);
		ResearchState.get(server).set(server, player, ids);
		return ids;
	}

	/** Sets the player's research to the given level and tells them. Returns the number of projects they now know. */
	public static int apply(ServerPlayerEntity player, int level, boolean optional) {
		int lvl = Math.max(0, Math.min(Research.MAX_LEVEL, level));
		List<String> ids = setLevel(player.getServer(), player.getUuid(), lvl, optional);
		player.sendMessage(Text.translatable("message.periodic_elements.progress_book.set", lvl,
				Text.translatable("level.periodic_elements." + lvl), ids.size()).formatted(Formatting.LIGHT_PURPLE), false);
		return ids.size();
	}
}
