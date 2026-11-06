package com.periodicelements.client;

import com.periodicelements.explore.BackpackScreenHandler;
import com.periodicelements.explore.Explore;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Client side of the exploration gear (3.47.0): backpack screen and key, compass needle, thrown traps, lamps. */
public final class ExploreClient {
	private static KeyBinding backpackKey;

	private ExploreClient() {
	}

	public static void init() {
		HandledScreens.register(Explore.BACKPACK_SCREEN, BackpackScreen::new);
		backpackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.periodic_elements.backpack", InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B, "key.categories.periodic_elements"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (backpackKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) {
					ClientPlayNetworking.send(new Explore.OpenBackpack());
				}
			}
		});
		ModelPredicateProviderRegistry.register(Explore.COMPASS, Identifier.ofVanilla("angle"),
				new CompassAnglePredicateProvider((world, stack, entity) -> stack.get(Explore.COMPASS_TARGET)));
		ModelPredicateProviderRegistry.register(Explore.ROPE_NET, Identifier.of("periodic_elements", "full"),
				(ClampedModelPredicateProvider) (stack, world, entity, seed) -> stack.contains(Explore.TRAPPED) ? 1f : 0f);
		ModelPredicateProviderRegistry.register(Explore.STEEL_CAGE, Identifier.of("periodic_elements", "full"),
				(ClampedModelPredicateProvider) (stack, world, entity, seed) -> stack.contains(Explore.TRAPPED) ? 1f : 0f);
		EntityRendererRegistry.register(Explore.TRAP_ENTITY, FlyingItemEntityRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), Explore.SODIUM_LAMP, Explore.LED_LAMP, Explore.TENT);
	}

	/** An open backpack, drawn like a chest of its size. */
	public static class BackpackScreen extends HandledScreen<BackpackScreenHandler> {
		private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
		private final int rows;

		public BackpackScreen(BackpackScreenHandler handler, PlayerInventory inventory, Text title) {
			super(handler, inventory, title);
			rows = handler.rows();
			backgroundHeight = 114 + rows * 18;
			playerInventoryTitleY = backgroundHeight - 94;
		}

		@Override
		protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
			int x = (width - backgroundWidth) / 2, y = (height - backgroundHeight) / 2;
			ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, rows * 18 + 17);
			ctx.drawTexture(TEXTURE, x, y + rows * 18 + 17, 0, 126, backgroundWidth, 96);
		}

		@Override
		public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
			super.render(ctx, mouseX, mouseY, delta);
			drawMouseoverTooltip(ctx, mouseX, mouseY);
		}
	}
}
