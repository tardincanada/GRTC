package com.periodicelements.compat.jade;

import com.periodicelements.BlockComposition;
import com.periodicelements.ModContent;
import com.periodicelements.PeriodicElements;
import com.periodicelements.client.Lens;
import com.periodicelements.client.MachineScreen;
import com.periodicelements.compendium.CompendiumData;
import com.periodicelements.machine.MachineBlock;
import com.periodicelements.machine.MachineBlockEntity;
import com.periodicelements.machine.PowerBlock;
import com.periodicelements.machine.PowerBlockEntity;
import com.periodicelements.machine.Reactions;
import com.periodicelements.machine.StorageTankBlock;
import com.periodicelements.machine.StorageTankBlockEntity;
import com.periodicelements.research.ClientResearch;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

import java.util.Locale;

/**
 * Jade plugin (3.26.0). Looking at a machine shows its status, temperature against its limit, the reaction under way
 * with a progress bar, and the fuel left; a power block or a Storage Tank shows the same line its screen does; any
 * block with a composition shows its main elements, by the Magnifying Lens's rules (only elements the player knows,
 * natural rocks and plants only once analysed with the lens). Each part can be turned off in Jade's settings.
 * Loaded only through the "jade" entrypoint, so the mod runs without Jade.
 */
@WailaPlugin
public class PeJadePlugin implements IWailaPlugin {
	static final String NS = PeriodicElements.MOD_ID;
	public static final Identifier MACHINE = Identifier.of(NS, "machine");
	public static final Identifier POWER = Identifier.of(NS, "power");
	public static final Identifier TANK = Identifier.of(NS, "storage_tank");
	public static final Identifier COMPOSITION = Identifier.of(NS, "composition");
	/** How many elements the short composition line names (more with Jade's "details" key, shift by default). */
	static final int SHORT = 3, DETAILED = 8;

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(MachineData.INSTANCE, MachineBlockEntity.class);
		registration.registerBlockDataProvider(DescribeData.POWER_DATA, PowerBlockEntity.class);
		registration.registerBlockDataProvider(DescribeData.TANK_DATA, StorageTankBlockEntity.class);
		registration.registerBlockDataProvider(DescribeData.TANK_DATA, com.periodicelements.fluid.TankControllerBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(MachineView.INSTANCE, MachineBlock.class);
		registration.registerBlockComponent(DescribeView.POWER_VIEW, PowerBlock.class);
		registration.registerBlockComponent(DescribeView.TANK_VIEW, StorageTankBlock.class);
		registration.registerBlockComponent(DescribeView.TANK_VIEW, com.periodicelements.fluid.TankBlocks.Controller.class);
		registration.registerBlockComponent(CompositionView.INSTANCE, Block.class);
	}

	// --- machines ------------------------------------------------------------------------------------------------

	/** Server side: the few numbers the machine's screen shows. */
	enum MachineData implements IServerDataProvider<BlockAccessor> {
		INSTANCE;

		static final int[] SENT = {MachineBlockEntity.P_TEMP, MachineBlockEntity.P_BURN, MachineBlockEntity.P_BURN_TOTAL,
				MachineBlockEntity.P_PROGRESS, MachineBlockEntity.P_STATUS, MachineBlockEntity.P_STATUS_REACTION,
				MachineBlockEntity.P_ACTIVE, MachineBlockEntity.P_LIMIT};

		@Override
		public void appendServerData(NbtCompound data, BlockAccessor accessor) {
			if (accessor.getBlockEntity() instanceof MachineBlockEntity be) {
				int[] values = new int[SENT.length];
				for (int i = 0; i < SENT.length; i++) {
					values[i] = be.property(SENT[i]);
				}
				data.putIntArray("pe_machine", values);
				data.putString("pe_spec", be.spec().id());
			}
		}

		@Override
		public Identifier getUid() {
			return MACHINE;
		}
	}

	enum MachineView implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			NbtCompound data = accessor.getServerData();
			int[] v = data.getIntArray("pe_machine");
			if (v.length != MachineData.SENT.length) {
				return;
			}
			int temp = v[0], burn = v[1], burnTotal = v[2], progress = v[3], statusIndex = v[4], statusReaction = v[5],
					active = v[6], limit = v[7];
			MachineBlockEntity.Status[] all = MachineBlockEntity.Status.values();
			MachineBlockEntity.Status status = all[Math.max(0, Math.min(all.length - 1, statusIndex))];
			Reactions.Reaction shown = Reactions.get(active >= 0 ? active : statusReaction);

			tooltip.add(MachineScreen.statusText(status, shown).copy().withColor(statusColor(status)));
			tooltip.add(Text.translatable("gui." + NS + ".machine.temperature", temp).append(" · ")
					.append(Text.translatable("gui." + NS + ".machine.max", limit)).formatted(Formatting.GRAY));
			if (active >= 0 && shown != null) {
				IElementHelper h = IElementHelper.get();
				MutableText label = shown.name().copy().append(String.format(Locale.ROOT, " %d%%", progress / 10));
				tooltip.add(h.progress(progress / 1000f, label, h.progressStyle().color(0xFF3FA34D, 0xFF2A6E34),
						BoxStyle.getNestedBox(), true));
			}
			Reactions.MachineSpec spec = Reactions.machine(data.getString("pe_spec"));
			if (spec != null && !spec.electric() && burnTotal > 0) {
				tooltip.add(Text.translatable("jade." + NS + ".fuel", Math.round(100.0 * burn / burnTotal))
						.formatted(Formatting.GRAY));
			}
		}

		@Override
		public Identifier getUid() {
			return MACHINE;
		}
	}

	/** Brighter versions of the screen's status colours, for Jade's dark box. */
	static int statusColor(MachineBlockEntity.Status status) {
		return switch (status) {
			case WORKING -> 0x55FF55;
			case IDLE -> 0xAAAAAA;
			case HEATING -> 0xFFAA00;
			default -> 0xFF5555;
		};
	}

	// --- power blocks and tanks: the line their screens show ------------------------------------------------------

	static final class DescribeData implements IServerDataProvider<BlockAccessor> {
		static final DescribeData POWER_DATA = new DescribeData(POWER), TANK_DATA = new DescribeData(TANK);
		private final Identifier uid;

		private DescribeData(Identifier uid) {
			this.uid = uid;
		}

		@Override
		public void appendServerData(NbtCompound data, BlockAccessor accessor) {
			Text line = accessor.getBlockEntity() instanceof PowerBlockEntity p ? p.describe()
					: accessor.getBlockEntity() instanceof StorageTankBlockEntity t ? t.describe()
					: accessor.getBlockEntity() instanceof com.periodicelements.fluid.TankControllerBlockEntity c ? c.describe() : null;
			if (line != null) {
				data.putString("pe_line", Text.Serialization.toJsonString(line, accessor.getLevel().getRegistryManager()));
			}
		}

		@Override
		public Identifier getUid() {
			return uid;
		}
	}

	static final class DescribeView implements IBlockComponentProvider {
		static final DescribeView POWER_VIEW = new DescribeView(POWER), TANK_VIEW = new DescribeView(TANK);
		private final Identifier uid;

		private DescribeView(Identifier uid) {
			this.uid = uid;
		}

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			String json = accessor.getServerData().getString("pe_line");
			if (json.isEmpty()) {
				return;
			}
			Text line = Text.Serialization.fromJson(json, accessor.getLevel().getRegistryManager());
			if (line == null) {
				return;
			}
			// The screen's line starts with the block's name, which Jade already shows; long ones go on several lines.
			String text = line.getString();
			String prefix = accessor.getBlock().getName().getString() + ": ";
			if (text.startsWith(prefix)) {
				text = text.substring(prefix.length());
			}
			for (String part : text.split("; ")) {
				tooltip.add(Text.literal(part).formatted(Formatting.GRAY));
			}
		}

		@Override
		public Identifier getUid() {
			return uid;
		}
	}

	// --- composition ----------------------------------------------------------------------------------------------

	enum CompositionView implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			var player = MinecraftClient.getInstance().player;
			if (player != null && (player.getMainHandStack().isOf(ModContent.MAGNIFYING_LENS)
					|| player.getOffHandStack().isOf(ModContent.MAGNIFYING_LENS))) {
				return; // the lens's own panel already shows it in full
			}
			Block block = accessor.getBlock();
			BlockComposition.Entry entry = BlockComposition.of(Registries.BLOCK.getId(block).toString());
			if (entry == null) {
				return;
			}
			var natural = CompendiumData.mineralOf(block);
			if (natural == null) {
				natural = CompendiumData.plantOf(block);
			}
			if (natural != null && !com.periodicelements.client.CompendiumClient.knows(CompendiumData.analysedKey(natural))) {
				tooltip.add(Text.translatable("jade." + NS + ".analyse").formatted(Formatting.GRAY));
				return;
			}
			Lens.View view = Lens.view(entry, ClientResearch.known());
			int max = accessor.showDetails() ? DETAILED : SHORT;
			MutableText line = Text.empty();
			double rest = view.other();
			for (int i = 0; i < view.known().size(); i++) {
				Lens.Line l = view.known().get(i);
				if (i < max) {
					if (i > 0) {
						line.append(" · ");
					}
					line.append(Text.literal(symbol(l.element())).formatted(Formatting.AQUA))
							.append(Text.literal(" " + percent(l.units(), entry.amount())));
				} else {
					rest += l.units();
				}
			}
			if (rest >= 0.05) {
				line.append(view.known().isEmpty() ? Text.empty() : Text.literal(" · "))
						.append(Text.translatable("jade." + NS + ".other", percent(rest, entry.amount())).formatted(Formatting.GRAY));
			}
			tooltip.add(line.formatted(Formatting.GRAY));
		}

		@Override
		public Identifier getUid() {
			return COMPOSITION;
		}
	}

	static String symbol(String element) {
		for (var e : PeriodicElements.ELEMENTS) {
			if (e.getElement().id().equals(element)) {
				return e.getElement().symbol();
			}
		}
		return element;
	}

	static String percent(double units, double total) {
		double p = total <= 0 ? 0 : 100 * units / total;
		return p < 1 ? "<1%" : String.format(Locale.ROOT, "%.0f%%", p);
	}
}
