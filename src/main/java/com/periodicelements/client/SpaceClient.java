package com.periodicelements.client;

import com.periodicelements.space.MeteorEntity;
import com.periodicelements.space.Meteorites;
import com.periodicelements.space.RocketEntity;
import com.periodicelements.space.Space;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

/** Client side of level 8 (3.61.0): the rocket in flight is drawn from the blocks it was built of. */
public final class SpaceClient {
	private SpaceClient() {
	}

	public static void init() {
		EntityRendererRegistry.register(Space.ROCKET, RocketRenderer::new);
		EntityRendererRegistry.register(Meteorites.METEOR, MeteorRenderer::new);
		MoonSky.init();   // 3.64.0
		MarsSky.init();   // 3.66.0
		InnerSky.init();   // 3.68.0
		// 3.67.0: every material of science fiction says so in its tooltip
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			Identifier id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
			if (id.getNamespace().equals("periodic_elements") && com.periodicelements.space.Fiction.FICTION.contains(id.getPath())) {
				lines.add(Math.min(1, lines.size()), net.minecraft.text.Text.translatable("tooltip.periodic_elements.fiction")
						.formatted(net.minecraft.util.Formatting.LIGHT_PURPLE));
			}
		});
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), Space.KEROLOX_ENGINE, Space.HYDROLOX_ENGINE,
				Space.HYPERGOLIC_ENGINE, Space.METHALOX_ENGINE, Space.METALLIC_HYDROGEN_ENGINE, Space.ANTIMATTER_ENGINE,
				Space.FUSION_DRIVE, Space.PAYLOAD_FAIRING);
	}

	static class RocketRenderer extends EntityRenderer<RocketEntity> {
		private final BlockRenderManager blocks;

		RocketRenderer(EntityRendererFactory.Context ctx) {
			super(ctx);
			blocks = ctx.getBlockRenderManager();
			shadowRadius = 0f;
		}

		@Override
		public void render(RocketEntity rocket, float yaw, float tickDelta, MatrixStack matrices,
						   VertexConsumerProvider consumers, int light) {
			List<BlockState> list = rocket.blocks();
			matrices.push();
			matrices.translate(-0.5, 0, -0.5);
			int bright = 0xF000F0;   // lit by its own flame and the open sky
			for (BlockState s : list) {
				blocks.renderBlockAsEntity(s, matrices, consumers, bright, OverlayTexture.DEFAULT_UV);
				matrices.translate(0, 1, 0);
			}
			matrices.pop();
			super.render(rocket, yaw, tickDelta, matrices, consumers, light);
		}

		@Override
		public Identifier getTexture(RocketEntity entity) {
			return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
		}
	}

	/** 3.62.0: a meteorite on its way down, tumbling and glowing hot. */
	static class MeteorRenderer extends EntityRenderer<MeteorEntity> {
		private final BlockRenderManager blocks;

		MeteorRenderer(EntityRendererFactory.Context ctx) {
			super(ctx);
			blocks = ctx.getBlockRenderManager();
			shadowRadius = 0f;
		}

		@Override
		public void render(MeteorEntity meteor, float yaw, float tickDelta, MatrixStack matrices,
						   VertexConsumerProvider consumers, int light) {
			float spin = (meteor.age + tickDelta) * 18f;
			matrices.push();
			matrices.translate(0, 0.5, 0);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(spin));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spin * 0.7f));
			matrices.translate(-0.5, -0.5, -0.5);
			blocks.renderBlockAsEntity(meteor.block(), matrices, consumers, 0xF000F0, OverlayTexture.DEFAULT_UV);
			matrices.pop();
			super.render(meteor, yaw, tickDelta, matrices, consumers, light);
		}

		@Override
		public Identifier getTexture(MeteorEntity entity) {
			return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
		}
	}
}
