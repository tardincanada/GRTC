package com.periodicelements.client;

import com.periodicelements.samplecase.SampleCaseScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** The open Sample Case (3.31.0), drawn like a large chest. */
public class SampleCaseScreen extends HandledScreen<SampleCaseScreenHandler> {
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");

	public SampleCaseScreen(SampleCaseScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundHeight = 114 + 6 * 18;
		playerInventoryTitleY = backgroundHeight - 94;
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2, y = (height - backgroundHeight) / 2;
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, 6 * 18 + 17);
		ctx.drawTexture(TEXTURE, x, y + 6 * 18 + 17, 0, 126, backgroundWidth, 96);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
