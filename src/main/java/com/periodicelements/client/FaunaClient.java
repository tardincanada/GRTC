package com.periodicelements.client;

import com.google.gson.JsonObject;
import com.periodicelements.ElementTable;
import com.periodicelements.fauna.Fauna;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import java.util.Map;

/** Client side of the fauna plan: a renderer for each animal, cutout colonies. */
public final class FaunaClient {
	private FaunaClient() {
	}

	@SuppressWarnings("unchecked")
	public static void init() {
		JsonObject models = ElementTable.readObject("/periodic_elements/fauna_models.json");
		for (Map.Entry<String, EntityType<?>> e : Fauna.TYPES.entrySet()) {
			Fauna.Species s = Fauna.SPECIES.get(e.getKey());
			JsonObject model = models.getAsJsonObject(e.getKey());
			EntityRendererRegistry.register((EntityType<MobEntity>) e.getValue(), ctx -> new FaunaRenderer(ctx, s, model));
		}
		for (String b : new String[]{"cochineal_colony", "lac_colony", "reindeer_lichen", "pearl_oyster"}) {
			Block block = Fauna.block(b);
			if (block != null) BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
		}
	}
}
