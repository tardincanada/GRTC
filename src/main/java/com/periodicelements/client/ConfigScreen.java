package com.periodicelements.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

/** The settings screen (3.31.0): one column of buttons and sliders; Done saves. */
public class ConfigScreen extends Screen {
	private static final String K = "gui.periodic_elements.config.";
	private final Screen parent;
	private final ClientConfig c = ClientConfig.get();

	public ConfigScreen(Screen parent) {
		super(Text.translatable(K + "title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int w = 240, x = width / 2 - w / 2, y = Math.max(40, height / 2 - 90);
		addDrawableChild(slider(x, y, w, "minimap_size", ClientConfig.MINIMAP_MIN, ClientConfig.MINIMAP_MAX, c.minimapSize,
				v -> c.minimapSize = v));
		y += 24;
		addDrawableChild(CyclingButtonWidget.<ClientConfig.Corner>builder(v -> Text.translatable(K + "corner." + v.name().toLowerCase(java.util.Locale.ROOT)))
				.values(ClientConfig.Corner.values()).initially(c.minimapCorner)
				.build(x, y, w, 20, Text.translatable(K + "minimap_corner"), (b, v) -> c.minimapCorner = v));
		y += 24;
		addDrawableChild(slider(x, y, w, "lens_lines", ClientConfig.LENS_MIN, ClientConfig.LENS_MAX, c.lensLines, v -> c.lensLines = v));
		y += 24;
		addDrawableChild(toggle(x, y, w, "compendium_hud", c.compendiumHud, v -> c.compendiumHud = v));
		y += 24;
		addDrawableChild(toggle(x, y, w, "composition_tooltip", c.compositionTooltip, v -> c.compositionTooltip = v));
		y += 24;
		addDrawableChild(toggle(x, y, w, "radiation_tooltip", c.radiationTooltip, v -> c.radiationTooltip = v));
		y += 32;
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(width / 2 - 100, y, 200, 20).build());
	}

	private CyclingButtonWidget<Boolean> toggle(int x, int y, int w, String key, boolean value, java.util.function.Consumer<Boolean> set) {
		CyclingButtonWidget<Boolean> b = CyclingButtonWidget.onOffBuilder(value)
				.build(x, y, w, 20, Text.translatable(K + key), (btn, v) -> set.accept(v));
		b.setTooltip(Tooltip.of(Text.translatable(K + key + ".tip")));
		return b;
	}

	private SliderWidget slider(int x, int y, int w, String key, int min, int max, int value, IntConsumer set) {
		return new SliderWidget(x, y, w, 20, Text.empty(), (value - min) / (double) (max - min)) {
			{
				updateMessage();
			}

			private int current() {
				return (int) Math.round(min + this.value * (max - min));
			}

			@Override
			protected void updateMessage() {
				setMessage(Text.translatable(K + key, current()));
			}

			@Override
			protected void applyValue() {
				set.accept(current());
			}
		};
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, Math.max(40, height / 2 - 90) - 20, 0xFFFFFF);
	}

	@Override
	public void close() {
		c.save();
		MinecraftClient.getInstance().setScreen(parent);
	}
}
