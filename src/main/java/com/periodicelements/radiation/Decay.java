package com.periodicelements.radiation;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.periodicelements.ElementTable;
import com.periodicelements.PeriodicElements;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Short-lived nuclides decay on Minecraft's own clock: one game day is 24 hours, so time runs 72 times faster than
 * real time. Each decaying stack carries the game time it was last brought up to date; when a player carries it,
 * the decays since then are drawn at once (each atom-item has the exact exponential chance), and the stack shrinks
 * while its daughter, if worth keeping, goes into the player's inventory.
 *
 * <p>Updates fall on fixed steps of game time per nuclide, so stacks of the same item carried together keep the
 * same stamp and still stack.
 */
public final class Decay {
	public record Nuclide(String nuclide, long halfLifeTicks, @Nullable Item daughter) {
		/** Game ticks between updates: a fiftieth of the half-life, from 10 seconds to one game day. */
		public long step() {
			return Math.max(200, Math.min(24000, halfLifeTicks / 50));
		}
	}

	public static ComponentType<Long> CLOCK;
	private static final Map<Item, Nuclide> ITEMS = new HashMap<>();
	private static final Map<String, String> ISOTOPES = new HashMap<>();

	private Decay() {
	}

	public static void register() {
		CLOCK = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(PeriodicElements.MOD_ID, "decay_clock"),
				ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build());
		ServerTickEvents.END_SERVER_TICK.register(Decay::tick);
	}

	/** Reads decay.json; call once the items are registered. */
	public static void load() {
		JsonObject root = ElementTable.readObject("/periodic_elements/decay.json");
		ITEMS.clear();
		for (var e : root.getAsJsonObject("items").entrySet()) {
			JsonObject o = e.getValue().getAsJsonObject();
			String daughter = o.get("daughter").getAsString();
			ITEMS.put(Registries.ITEM.get(Identifier.of(e.getKey())), new Nuclide(o.get("nuclide").getAsString(),
					o.get("halfLifeTicks").getAsLong(), daughter.isEmpty() ? null : Registries.ITEM.get(Identifier.of(daughter))));
		}
		ISOTOPES.clear();
		root.getAsJsonObject("isotopes").entrySet().forEach(e -> ISOTOPES.put(e.getKey(), e.getValue().getAsString()));
	}

	@Nullable
	public static Nuclide of(Item item) {
		return ITEMS.get(item);
	}

	/** "Pu-239, half-life 24,100 years" for an element id, or null. */
	@Nullable
	public static String isotope(String element) {
		return ISOTOPES.get(element);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % 20 != 0 || ITEMS.isEmpty()) {
			return;
		}
		long now = server.getOverworld().getTime();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			PlayerInventory inv = player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) {
					continue;
				}
				Nuclide n = ITEMS.get(stack.getItem());
				if (n == null) {
					continue;
				}
				int decayed = update(stack, now, player.getRandom());
				if (decayed > 0 && n.daughter() != null) {
					inv.offerOrDrop(new ItemStack(n.daughter(), decayed));
				}
			}
		}
	}

	/**
	 * Brings a stack up to game time {@code now}: stamps it if it has no clock yet, otherwise removes the items that
	 * decayed since its stamp. Returns how many decayed (the caller hands out the daughters).
	 */
	public static int update(ItemStack stack, long now, Random random) {
		Nuclide n = ITEMS.get(stack.getItem());
		if (n == null || stack.isEmpty()) {
			return 0;
		}
		long step = n.step();
		long due = Math.floorDiv(now, step) * step;
		Long since = stack.get(CLOCK);
		if (since == null) {
			stack.set(CLOCK, due);
			return 0;
		}
		if (due <= since) {
			return 0;
		}
		double survive = Math.pow(0.5, (double) (due - since) / n.halfLifeTicks());
		int decayed = 0;
		for (int i = 0; i < stack.getCount(); i++) {
			if (random.nextDouble() >= survive) {
				decayed++;
			}
		}
		stack.set(CLOCK, due);
		stack.decrement(decayed);
		return decayed;
	}
}
