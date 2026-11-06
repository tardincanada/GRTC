package com.periodicelements.client;

import com.periodicelements.research.ClientResearch;
import com.periodicelements.research.ProgressBook;
import com.periodicelements.research.Research;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/** The Book of Progress's page: one button per tech level, a switch for optional research, and the reader's level now. */
public class ProgressBookScreen extends Screen {
	/** Remembered while the game runs, so the switch stays as the tester left it. */
	private static boolean optional = true;
	private final List<ButtonWidget> levelButtons = new ArrayList<>();
	private ButtonWidget optionalButton;

	public ProgressBookScreen() {
		super(Text.translatable("gui.periodic_elements.progress_book.title"));
	}

	private static int currentLevel() {
		return Research.techLevel(ClientResearch.known());
	}

	private static Text levelLabel(int level, boolean current) {
		Text t = Text.translatable("gui.periodic_elements.progress_book.level", level,
				Text.translatable("level.periodic_elements." + level));
		return current ? Text.literal("> ").append(t).append(" <").formatted(Formatting.GOLD) : t;
	}

	private static Text optionalLabel() {
		return Text.translatable(optional ? "gui.periodic_elements.progress_book.optional.on"
				: "gui.periodic_elements.progress_book.optional.off");
	}

	/** Top of the level buttons: the page (titles, two columns of four levels, switch, done, hint) is centred. */
	private int top() {
		return Math.max(30, (height - 200) / 2 + 30);
	}

	private static final int ROWS = (Research.MAX_LEVEL + 2) / 2;

	@Override
	protected void init() {
		levelButtons.clear();
		int col = Math.min(200, (width - 24) / 2);
		int x = width / 2 - col - 2;
		int top = top();
		int current = currentLevel();
		for (int level = 0; level <= Research.MAX_LEVEL; level++) {
			int l = level;
			ButtonWidget b = ButtonWidget.builder(levelLabel(level, level == current),
					btn -> ClientPlayNetworking.send(new ProgressBook.SetLevel(l, optional)))
					.dimensions(level < ROWS ? x : width / 2 + 2, top + (level % ROWS) * 22, col, 20).build();
			levelButtons.add(addDrawableChild(b));
		}
		int y = top + ROWS * 22 + 4;
		optionalButton = addDrawableChild(ButtonWidget.builder(optionalLabel(), btn -> {
			optional = !optional;
			btn.setMessage(optionalLabel());
		}).dimensions(x, y, col * 2 + 4, 20).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, btn -> close()).dimensions(width / 2 - 100, y + 24, 200, 20).build());
	}

	@Override
	public void tick() {
		// The server answers a pick by syncing the research; refresh the marks when it arrives.
		int current = currentLevel();
		for (int i = 0; i < levelButtons.size(); i++) {
			levelButtons.get(i).setMessage(levelLabel(i, i == current));
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int top = top();
		ctx.drawCenteredTextWithShadow(textRenderer, title.copy().formatted(Formatting.LIGHT_PURPLE), width / 2, top - 30, 0xFFFFFF);
		int level = currentLevel();
		ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.periodic_elements.progress_book.current", level,
				Text.translatable("level.periodic_elements." + level), ClientResearch.known().size()), width / 2, top - 18, 0xE0E0E0);
		int y = top + ROWS * 22 + 4 + 50;
		for (var line : textRenderer.wrapLines(Text.translatable("gui.periodic_elements.progress_book.hint"), Math.min(width - 20, 404))) {
			ctx.drawCenteredTextWithShadow(textRenderer, line, width / 2, y, 0xA0A0A0);
			y += 10;
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
