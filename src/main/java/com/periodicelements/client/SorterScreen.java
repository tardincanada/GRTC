package com.periodicelements.client;

import com.periodicelements.logistics.SorterScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/** The Belt Sorter open (3.40.1): the dispenser's grid for the filter and, beside it, the mode button. */
public class SorterScreen extends HandledScreen<SorterScreenHandler> {
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/dispenser.png");
	private static final int BUTTON_W = 96;
	private ButtonWidget mode;
	private boolean shown;

	public SorterScreen(SorterScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		mode = addDrawableChild(ButtonWidget.builder(label(handler.forbidden()), b -> {
			if (client != null && client.interactionManager != null) {
				client.interactionManager.clickButton(handler.syncId, 0);
			}
		}).dimensions(x + backgroundWidth + 4, y + 30, BUTTON_W, 20).build());
		update();
	}

	private static Text label(boolean forbidden) {
		return Text.translatable("gui.periodic_elements.sorter." + (forbidden ? "forbidden" : "required"))
				.formatted(forbidden ? Formatting.RED : Formatting.DARK_GREEN);
	}

	private void update() {
		boolean f = handler.forbidden();
		shown = f;
		mode.setMessage(label(f));
		mode.setTooltip(Tooltip.of(Text.translatable("gui.periodic_elements.sorter." + (f ? "forbidden" : "required") + ".tip")));
	}

	@Override
	protected void handledScreenTick() {
		super.handledScreenTick();
		if (shown != handler.forbidden()) {
			update();
		}
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
		ctx.drawText(textRenderer, Text.translatable("gui.periodic_elements.sorter.mode"), x + backgroundWidth + 4, y + 18, 0xFFFFFF, true);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
