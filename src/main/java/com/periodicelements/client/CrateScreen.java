package com.periodicelements.client;

import com.periodicelements.logistics.CrateScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A larger chest open (3.40.0): a grey panel as wide as the chest, its slots and the player's pack drawn from the
 * vanilla large-chest picture so they look the same.
 */
public class CrateScreen extends HandledScreen<CrateScreenHandler> {
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
	private static final int FACE = 0xFFC6C6C6, LIGHT = 0xFFFFFFFF, DARK = 0xFF555555, EDGE = 0xFF000000;

	public CrateScreen(CrateScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = CrateScreenHandler.width(handler.columns());
		backgroundHeight = 114 + CrateScreenHandler.ROWS * 18;
		playerInventoryTitleY = backgroundHeight - 94;
		playerInventoryTitleX = (backgroundWidth - 176) / 2 + 8;
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2, y = (height - backgroundHeight) / 2;
		int w = backgroundWidth, h = backgroundHeight;
		// Panel with the vanilla bevel.
		ctx.fill(x + 1, y, x + w - 1, y + h, EDGE);
		ctx.fill(x, y + 1, x + w, y + h - 1, EDGE);
		ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, FACE);
		ctx.fill(x + 1, y + 1, x + w - 3, y + 3, LIGHT);
		ctx.fill(x + 1, y + 1, x + 3, y + h - 3, LIGHT);
		ctx.fill(x + 3, y + h - 3, x + w - 1, y + h - 1, DARK);
		ctx.fill(x + w - 3, y + 3, x + w - 1, y + h - 1, DARK);
		// The chest's slots: one 18 x 18 slot picture from the large chest per place.
		int cols = handler.columns();
		for (int r = 0; r < CrateScreenHandler.ROWS; r++) {
			for (int c = 0; c < cols; c++) {
				ctx.drawTexture(TEXTURE, x + 7 + c * 18, y + 17 + r * 18, 7, 17, 18, 18);
			}
		}
		// The player's pack: the bottom of the large-chest picture, without its outer border.
		int px = x + (w - 176) / 2;
		int py = y + CrateScreenHandler.ROWS * 18 + 30;
		ctx.drawTexture(TEXTURE, px + 7, py, 7, 138, 162, 76);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
