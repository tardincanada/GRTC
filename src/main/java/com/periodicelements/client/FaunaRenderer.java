package com.periodicelements.client;

import com.google.gson.JsonObject;
import com.periodicelements.PeriodicElements;
import com.periodicelements.fauna.Fauna;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

/** Draws an animal of the fauna plan with its {@link FaunaModel}: babies at half size, the stoat white in snow. */
public class FaunaRenderer extends MobEntityRenderer<MobEntity, FaunaModel> {
	private final Fauna.Species species;
	private final Identifier texture, winter;

	public FaunaRenderer(EntityRendererFactory.Context ctx, Fauna.Species species, JsonObject model) {
		super(ctx, new FaunaModel(model, species.has("glow") || species.body().equals("jelly")
				? RenderLayer::getEntityTranslucent : RenderLayer::getEntityCutoutNoCull), species.width() * 0.5f);
		this.species = species;
		this.texture = Identifier.of(PeriodicElements.MOD_ID, "textures/entity/fauna/" + species.id() + ".png");
		this.winter = species.has("winter_coat") ? Identifier.of(PeriodicElements.MOD_ID, "textures/entity/fauna/" + species.id() + "_winter.png") : null;
		if (species.has("glow")) {
			addFeature(new FeatureRenderer<>(this) {
				@Override
				public void render(MatrixStack matrices, VertexConsumerProvider vcp, int light, MobEntity e, float limbAngle,
								   float limbDistance, float tickDelta, float age, float headYaw, float headPitch) {
					getContextModel().render(matrices, vcp.getBuffer(RenderLayer.getEyes(getTexture(e))), 0xF000F0,
							OverlayTexture.DEFAULT_UV, 0x80FFFFFF);
				}
			});
		}
	}

	@Override
	public Identifier getTexture(MobEntity entity) {
		if (winter != null && (entity.getWorld().getBiome(entity.getBlockPos()).value().isCold(entity.getBlockPos())
				|| entity.getWorld().getBlockState(entity.getBlockPos()).isIn(BlockTags.SNOW))) {
			return winter;
		}
		return texture;
	}

	@Override
	protected void scale(MobEntity entity, MatrixStack matrices, float tickDelta) {
		float s = getModel().scale * (entity.isBaby() ? 0.5f : 1f);
		matrices.scale(s, s, s);
	}
}
