package com.periodicelements.client;

import com.periodicelements.worldgen.GeoOptions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * 3.49.0: "Customize" for the Geological world type on the world-creation screen. The choices go to
 * {@link GeoOptions#setPending}; the new world saves them in its folder when it is made.
 */
public final class GeoOptionsScreen extends Screen {
	private static final String P = "gui.periodic_elements.geo_options.";
	private final Screen parent;
	private GeoOptions.BiomeSize biomes;
	private GeoOptions.Size provinces;
	private GeoOptions.Amount volcanoes, ocean, rivers;

	public GeoOptionsScreen(Screen parent) {
		super(Text.translatable(P + "title"));
		this.parent = parent;
		GeoOptions o = GeoOptions.pending();
		biomes = o.biomes();
		provinces = o.provinces();
		volcanoes = o.volcanoes();
		ocean = o.ocean();
		rivers = o.rivers();
	}

	private static Text value(String option, Enum<?> e) {
		return Text.translatable(P + option + "." + e.name().toLowerCase(Locale.ROOT));
	}

	@Override
	protected void init() {
		int w = 240, x = (width - w) / 2, y = Math.max(40, height / 2 - 80);
		addDrawableChild(CyclingButtonWidget.<GeoOptions.BiomeSize>builder(v -> value("biomes", v))
				.values(GeoOptions.BiomeSize.values()).initially(biomes)
				.tooltip(v -> Tooltip.of(Text.translatable(P + "biomes.tip")))
				.build(x, y, w, 20, Text.translatable(P + "biomes"), (b, v) -> biomes = v));
		addDrawableChild(CyclingButtonWidget.<GeoOptions.Size>builder(v -> value("provinces", v))
				.values(GeoOptions.Size.values()).initially(provinces)
				.tooltip(v -> Tooltip.of(Text.translatable(P + "provinces.tip")))
				.build(x, y + 24, w, 20, Text.translatable(P + "provinces"), (b, v) -> provinces = v));
		addDrawableChild(CyclingButtonWidget.<GeoOptions.Amount>builder(v -> value("volcanoes", v))
				.values(GeoOptions.Amount.values()).initially(volcanoes)
				.tooltip(v -> Tooltip.of(Text.translatable(P + "volcanoes.tip")))
				.build(x, y + 48, w, 20, Text.translatable(P + "volcanoes"), (b, v) -> volcanoes = v));
		addDrawableChild(CyclingButtonWidget.<GeoOptions.Amount>builder(v -> value("ocean", v))
				.values(GeoOptions.Amount.values()).initially(ocean)
				.tooltip(v -> Tooltip.of(Text.translatable(P + "ocean.tip")))
				.build(x, y + 72, w, 20, Text.translatable(P + "ocean"), (b, v) -> ocean = v));
		addDrawableChild(CyclingButtonWidget.<GeoOptions.Amount>builder(v -> value("rivers", v))
				.values(GeoOptions.Amount.values()).initially(rivers)
				.tooltip(v -> Tooltip.of(Text.translatable(P + "rivers.tip")))
				.build(x, y + 96, w, 20, Text.translatable(P + "rivers"), (b, v) -> rivers = v));
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(x, y + 140, w, 20).build());
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, Math.max(40, height / 2 - 80) - 20, 0xFFFFFF);
		ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable(P + "note"), width / 2, Math.max(40, height / 2 - 80) + 124, 0xA0A0A0);
	}

	@Override
	public void close() {
		GeoOptions.setPending(GeoOptions.DEFAULT.with(biomes, provinces, volcanoes, ocean, rivers));
		client.setScreen(parent);
	}
}
