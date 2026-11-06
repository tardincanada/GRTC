package com.periodicelements.compat.emi;

import com.periodicelements.PeriodicElements;
import com.periodicelements.machine.Reactions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;

/** Small text helpers shared by the EMI recipes. Only loaded when EMI is installed. */
final class EmiText {
	static final String NS = PeriodicElements.MOD_ID;
	static final int WIDTH = 160;

	private EmiText() {
	}

	/** The display name of a substance, element or "item:ns:path" id. */
	static MutableText chemName(String id) {
		id = com.periodicelements.radiation.Isotopes.baseId(id);
		if (id.startsWith("item:")) {
			return Text.translatable(Registries.ITEM.get(Identifier.of(id.substring(5))).getTranslationKey());
		}
		if (id.equals(com.periodicelements.BlockComposition.GANGUE)) {
			return Text.translatable("emi." + NS + ".gangue");
		}
		return Text.translatable("item." + NS + "." + id);
	}

	/** The item a substance, element or "item:" id comes out as, or null if there is none. */
	static Item item(String id) {
		if (id.equals(com.periodicelements.BlockComposition.GANGUE)) {
			return Items.STONE; // gangue separates like ordinary stone
		}
		Item item = Reactions.outputItem(id);
		return item == null || item == Items.AIR ? null : item;
	}

	static String units(double u) {
		return u == Math.rint(u) && Math.abs(u) < 1e6 ? String.valueOf((long) u) : String.format(Locale.ROOT, "%.1f", u);
	}

	static List<OrderedText> wrap(Text text) {
		return MinecraftClient.getInstance().textRenderer.wrapLines(text, WIDTH);
	}
}
