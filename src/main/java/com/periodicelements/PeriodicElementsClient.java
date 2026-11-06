package com.periodicelements;

import com.periodicelements.client.Lens;
import com.periodicelements.client.MachineScreen;
import com.periodicelements.client.ResearchStationScreen;
import com.periodicelements.research.ClientResearch;
import com.periodicelements.research.ResearchSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;

/** Adds what each vanilla block or item yields (in units) to its tooltip. Hold Shift to see every part. */
public class PeriodicElementsClient implements ClientModInitializer {
	private static final int SHORT_LIST = 5;

	@Override
	public void onInitializeClient() {
		HandledScreens.register(ModContent.MACHINE_SCREEN, MachineScreen::new);
		// 3.45.0: salt water and brine are drawn exactly like water (same textures, biome water colour, overlay).
		{
			var water = net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.get(net.minecraft.fluid.Fluids.WATER);
			net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(
					com.periodicelements.water.Water.SALT_WATER, com.periodicelements.water.Water.FLOWING_SALT_WATER, water);
			net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(
					com.periodicelements.water.Water.BRINE, com.periodicelements.water.Water.FLOWING_BRINE, water);
			net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putFluids(net.minecraft.client.render.RenderLayer.getTranslucent(),
					com.periodicelements.water.Water.SALT_WATER, com.periodicelements.water.Water.FLOWING_SALT_WATER,
					com.periodicelements.water.Water.BRINE, com.periodicelements.water.Water.FLOWING_BRINE);
		}
		HandledScreens.register(com.periodicelements.samplecase.SampleCase.SCREEN, com.periodicelements.client.SampleCaseScreen::new);
		com.periodicelements.client.ExploreClient.init();
		com.periodicelements.client.FaunaClient.init();
		HandledScreens.register(com.periodicelements.logistics.Logistics.CHEST_SCREEN, com.periodicelements.client.CrateScreen::new);
		HandledScreens.register(com.periodicelements.logistics.Logistics.SORTER_SCREEN, com.periodicelements.client.SorterScreen::new);
		net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(
				com.periodicelements.logistics.Logistics.BELT_ENTITY, com.periodicelements.client.BeltRenderer::new);
		// 3.48.1 (Fiat Lux P1): the worn-lamp lights draw one fully transparent quad; cutout drops it, shaders still see it.
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(net.minecraft.client.render.RenderLayer.getCutout(),
				com.periodicelements.explore.Explore.LIGHT, com.periodicelements.farm.Industry.LAMP_LIGHT);
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(net.minecraft.client.render.RenderLayer.getCutout(),
				com.periodicelements.logistics.Logistics.HOPPERS.toArray(new net.minecraft.block.Block[0]));
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
				net.minecraft.client.render.RenderLayer.getCutoutMipped(), com.periodicelements.farm.Farm.RUBBER_LEAVES);
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
				net.minecraft.client.render.RenderLayer.getCutout(), com.periodicelements.farm.Farm.RUBBER_SAPLING);
		for (com.periodicelements.shrubs.ShrubBlock b : com.periodicelements.shrubs.Shrubs.ALL) {
			net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(b, net.minecraft.client.render.RenderLayer.getCutout());
		}
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
				net.minecraft.client.render.RenderLayer.getCutoutMipped(), GeoOres.VARIANTS.toArray(new net.minecraft.block.Block[0]));
		for (com.periodicelements.trees.Trees.Species s : com.periodicelements.trees.Trees.ALL) {
			net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
					net.minecraft.client.render.RenderLayer.getCutoutMipped(), s.leaves());
			net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
					net.minecraft.client.render.RenderLayer.getCutout(), s.sapling());
		}
		FieldGuideItem.open = () -> MinecraftClient.getInstance().setScreen(new com.periodicelements.client.FieldGuideScreen());
		com.periodicelements.research.ProgressBook.BookItem.open = () ->
				MinecraftClient.getInstance().setScreen(new com.periodicelements.client.ProgressBookScreen());
		HandledScreens.register(ModContent.STATION_SCREEN, ResearchStationScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(ResearchSyncPayload.ID,
				(payload, context) -> ClientResearch.set(payload.known()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientResearch.set(java.util.List.of()));
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			double r = com.periodicelements.radiation.Radioactivity.of(stack.getItem());
			if (r >= 1e-8 && com.periodicelements.client.ClientConfig.get().radiationTooltip) {
				lines.add(net.minecraft.text.Text.translatable("tooltip.periodic_elements.radioactive",
						com.periodicelements.radiation.Radioactivity.formatRate(r))
						.formatted(net.minecraft.util.Formatting.GREEN));
			}
			var nuclide = com.periodicelements.radiation.Decay.of(stack.getItem());
			if (nuclide != null) {
				double days = nuclide.halfLifeTicks() / 24000.0;
				double realMinutes = nuclide.halfLifeTicks() / 1200.0;
				String game = days >= 1 ? String.format(Locale.ROOT, "%.4g days", days)
						: String.format(Locale.ROOT, "%.3g hours", days * 24);
				String real = realMinutes >= 120 ? String.format(Locale.ROOT, "%.3g hours", realMinutes / 60)
						: String.format(Locale.ROOT, "%.3g minutes", realMinutes);
				lines.add(net.minecraft.text.Text.translatable("tooltip.periodic_elements.decay", nuclide.nuclide(), game, real)
						.formatted(net.minecraft.util.Formatting.DARK_GREEN));
				if (nuclide.daughter() != null) {
					lines.add(net.minecraft.text.Text.translatable("tooltip.periodic_elements.decay.into",
							nuclide.daughter().getName()).formatted(net.minecraft.util.Formatting.DARK_GREEN));
				}
			}
		});
		com.periodicelements.client.CherenkovRenderer.register();
		// 3.38.0: the Tank Gauge's dial, and the pipes' see-through ends.
		net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(com.periodicelements.fluid.FluidContent.GAUGE_ENTITY,
				com.periodicelements.client.TankGaugeRenderer::new);
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(com.periodicelements.fluid.FluidContent.FLUID_PIPE,
				net.minecraft.client.render.RenderLayer.getCutout());
		com.periodicelements.client.StructureOutline.register();
		com.periodicelements.client.AtlasClient.register();
		com.periodicelements.client.CompendiumClient.register();
		// 3.23.0: crops are cut out; seeds show what the Seed Lens found in them.
		com.periodicelements.crops.Crops.BLOCKS.values().forEach(b -> net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE
				.putBlock(b, net.minecraft.client.render.RenderLayer.getCutout()));
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (stack.isOf(net.minecraft.item.Items.GHAST_TEAR)) {
				// 3.46.0: the tear works as an anode upgrade in electrolysis machines.
				lines.add(Text.translatable("tooltip." + PeriodicElements.MOD_ID + ".ghast_tear_anode").formatted(Formatting.GOLD));
			}
			if (!com.periodicelements.crops.Crops.isSeed(stack)) {
				return;
			}
			Integer g = stack.get(com.periodicelements.crops.Crops.GENOME);
			int stamp = g == null ? 0 : com.periodicelements.crops.Genome.stamp(g);
			if (stamp == 0) {
				lines.add(Text.translatable("tooltip.periodic_elements.genome.unknown").formatted(Formatting.DARK_GRAY));
				return;
			}
			boolean stature = com.periodicelements.crops.Crops.hasStature(stack);
			Text traits = com.periodicelements.crops.Genome.traits(g, stamp, stature);
			lines.add((traits == null ? Text.translatable("tooltip.periodic_elements.genome.plain")
					: Text.translatable("tooltip.periodic_elements.genome.traits", traits)).formatted(Formatting.AQUA));
			if (stamp >= com.periodicelements.crops.Genome.STAMP_GENOTYPE) {
				lines.add(Text.translatable("tooltip.periodic_elements.genome.genotype",
						com.periodicelements.crops.Genome.genotype(g, stamp, stature)).formatted(Formatting.DARK_AQUA));
				if (com.periodicelements.crops.Genome.hybrid(g)) {
					lines.add(Text.translatable("gui.periodic_elements.crop.hybrid").formatted(Formatting.DARK_AQUA));
				}
			}
		});
		Lens.load();
		// 3.31.0: /periodicelements config opens the settings (also from Mod Menu when installed).
		net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
				dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("periodicelements")
						.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("config").executes(c -> {
							MinecraftClient mc = MinecraftClient.getInstance();
							mc.send(() -> mc.setScreen(new com.periodicelements.client.ConfigScreen(null)));
							return 1;
						}))));
		HudRenderCallback.EVENT.register((ctx, tick) -> lensHud(ctx));
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			BlockComposition.Entry entry = BlockComposition.of(Registries.ITEM.getId(stack.getItem()).toString());
			if (entry == null || !com.periodicelements.client.ClientConfig.get().compositionTooltip) {
				return;
			}
			List<BlockComposition.Part> parts = entry.parts();
			if (parts.size() == 1 && Registries.ITEM.getId(stack.getItem()).getPath().equals(parts.get(0).id())) {
				return; // an element or substance item is just itself
			}
			String ns = PeriodicElements.MOD_ID;
			// 3.15.0: natural rocks, ores and plants keep their composition hidden until analysed with the Lens.
			String analysed = com.periodicelements.compendium.CompendiumData.naturalKey(stack.getItem());
			if (analysed != null && !com.periodicelements.client.CompendiumClient.knows(analysed)) {
				lines.add(Text.translatable("tooltip." + ns + ".not_analysed").formatted(Formatting.DARK_GRAY));
				return;
			}
			lines.add(Text.translatable("tooltip." + ns + ".composition", units(entry.amount()))
					.formatted(Formatting.AQUA));
			boolean all = Screen.hasShiftDown();
			int shown = all ? parts.size() : Math.min(SHORT_LIST, parts.size());
			for (int i = 0; i < shown; i++) {
				BlockComposition.Part part = parts.get(i);
				lines.add(Text.translatable("tooltip." + ns + ".composition.line", units(part.units()),
						Text.translatable("item." + ns + "." + part.id())).formatted(Formatting.GRAY));
			}
			if (shown < parts.size()) {
				lines.add(Text.translatable("tooltip." + ns + ".composition.more", parts.size() - shown)
						.formatted(Formatting.DARK_GRAY));
			}
		});
	}

	private static final int LENS_LINES = 8;

	/** While a magnifying lens is in either hand, lists what the block under the crosshair is made of. */
	/** Bottom edge of the lens panel drawn last frame (0 when none): the Tree Compendium's panel goes below it. */
	public static int lensBottom;

	private static void lensHud(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		lensBottom = 0;
		if (mc.player == null || mc.world == null || mc.options.hudHidden || mc.currentScreen != null
				|| !(mc.player.getMainHandStack().isOf(ModContent.MAGNIFYING_LENS)
				|| mc.player.getOffHandStack().isOf(ModContent.MAGNIFYING_LENS))
				|| !(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		Block block = mc.world.getBlockState(hit.getBlockPos()).getBlock();
		String ns = PeriodicElements.MOD_ID;
		BlockComposition.Entry entry = BlockComposition.of(Registries.BLOCK.getId(block).toString());
		List<Text> lines = new java.util.ArrayList<>();
		var natural = com.periodicelements.compendium.CompendiumData.mineralOf(block);
		if (natural == null) natural = com.periodicelements.compendium.CompendiumData.plantOf(block);
		if (entry == null) {
			lines.add(Text.translatable("hud." + ns + ".lens.nothing", block.getName()));
		} else if (natural != null && !com.periodicelements.client.CompendiumClient.knows(
				com.periodicelements.compendium.CompendiumData.analysedKey(natural))) {
			// 3.15.0: a natural rock or plant shows its composition only once the lens has rested on it for 2 seconds.
			lines.add(Text.translatable("hud." + ns + ".lens", block.getName(), units(entry.amount())));
			lines.add(Text.translatable("hud." + ns + ".lens.analysing"));
		} else {
			Lens.View view = Lens.view(entry, ClientResearch.known());
			lines.add(Text.translatable("hud." + ns + ".lens", block.getName(), units(entry.amount())));
			int shown = Math.min(com.periodicelements.client.ClientConfig.get().lensLines, view.known().size());
			double rest = view.other();
			for (int i = 0; i < view.known().size(); i++) {
				Lens.Line line = view.known().get(i);
				if (i < shown) {
					lines.add(Text.translatable("tooltip." + ns + ".composition.line", units(line.units()),
							Text.translatable("item." + ns + "." + line.element())));
				} else {
					rest += line.units();
				}
			}
			if (rest >= 0.05) {
				lines.add(Text.translatable("hud." + ns + ".lens.other", units(rest)));
			}
		}
		int w = 0;
		for (Text t : lines) {
			w = Math.max(w, mc.textRenderer.getWidth(t));
		}
		ctx.fill(4, 4, 10 + w, 8 + lines.size() * 10, 0xA0101014);
		lensBottom = 8 + lines.size() * 10;
		for (int i = 0; i < lines.size(); i++) {
			ctx.drawText(mc.textRenderer, lines.get(i), 7, 7 + i * 10, i == 0 ? 0x80E0FF : 0xD0D0D0, false);
		}
	}

	private static String units(double value) {
		if (value < 0.1) {
			return "<0.1";
		}
		return value >= 100 ? String.format(Locale.ROOT, "%.0f", value) : String.format(Locale.ROOT, "%.1f", value);
	}
}
