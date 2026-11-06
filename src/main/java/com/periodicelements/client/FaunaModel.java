package com.periodicelements.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * An animal of the fauna plan, built from its entry in runtime fauna_models.json (tools/fauna_models.py): boxes in
 * pixels, ground at y = 24, head toward -z. Each part has a role that says how it moves (legs swing, wings flap,
 * tails sway, fins beat).
 */
public class FaunaModel extends EntityModel<MobEntity> {
	private record Part(ModelPart part, String role, float pitch, float yaw, float roll, int index) {
	}

	private final ModelPart root;
	private final List<Part> parts = new ArrayList<>();
	public final float scale;
	public final int texWidth, texHeight;

	public FaunaModel(JsonObject json, Function<Identifier, RenderLayer> layer) {
		super(layer);
		var tex = json.getAsJsonArray("tex");
		texWidth = tex.get(0).getAsInt();
		texHeight = tex.get(1).getAsInt();
		scale = json.has("scale") ? json.get("scale").getAsFloat() : 1f;
		ModelData data = new ModelData();
		ModelPartData rootData = data.getRoot();
		Map<String, ModelPartData> built = new HashMap<>();
		List<String[]> roles = new ArrayList<>();
		for (JsonElement e : json.getAsJsonArray("parts")) {
			JsonObject o = e.getAsJsonObject();
			String name = o.get("name").getAsString();
			String parent = o.has("parent") && !o.get("parent").isJsonNull() ? o.get("parent").getAsString() : null;
			var pv = o.getAsJsonArray("pivot");
			var rt = o.getAsJsonArray("rot");
			ModelPartBuilder b = ModelPartBuilder.create();
			for (JsonElement ce : o.getAsJsonArray("cubes")) {
				JsonObject c = ce.getAsJsonObject();
				var or = c.getAsJsonArray("o");
				var sz = c.getAsJsonArray("s");
				var uv = c.getAsJsonArray("uv");
				b.uv(uv.get(0).getAsInt(), uv.get(1).getAsInt()).cuboid(or.get(0).getAsFloat(), or.get(1).getAsFloat(),
						or.get(2).getAsFloat(), sz.get(0).getAsFloat(), sz.get(1).getAsFloat(), sz.get(2).getAsFloat(), Dilation.NONE);
			}
			ModelPartData parentData = parent == null ? rootData : built.get(parent);
			ModelPartData pd = parentData.addChild(name, b, ModelTransform.of(pv.get(0).getAsFloat(), pv.get(1).getAsFloat(),
					pv.get(2).getAsFloat(), rt.get(0).getAsFloat(), rt.get(1).getAsFloat(), rt.get(2).getAsFloat()));
			built.put(name, pd);
			String role = o.has("role") && !o.get("role").isJsonNull() ? o.get("role").getAsString() : "";
			roles.add(new String[]{name, parent, role});
		}
		root = TexturedModelData.of(data, texWidth, texHeight).createModel();
		Map<String, ModelPart> byName = new HashMap<>();
		int i = 0;
		for (String[] r : roles) {
			ModelPart p = r[1] == null ? root.getChild(r[0]) : byName.get(r[1]).getChild(r[0]);
			byName.put(r[0], p);
			parts.add(new Part(p, r[2], p.pitch, p.yaw, p.roll, i++));
		}
	}

	@Override
	public void setAngles(MobEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
		float swing = MathHelper.cos(limbAngle * 0.6662f) * 1.2f * limbDistance;
		boolean air = !entity.isOnGround() && !entity.isTouchingWater();
		boolean water = entity.isTouchingWater();
		for (Part p : parts) {
			ModelPart m = p.part;
			m.pitch = p.pitch;
			m.yaw = p.yaw;
			m.roll = p.roll;
			float phase = p.index * 0.7f;
			switch (p.role) {
				case "head" -> {
					m.pitch = p.pitch + headPitch * MathHelper.RADIANS_PER_DEGREE;
					m.yaw = p.yaw + headYaw * MathHelper.RADIANS_PER_DEGREE;
				}
				case "leg_a" -> m.pitch = p.pitch + swing;
				case "leg_b" -> m.pitch = p.pitch - swing;
				case "sleg_a" -> m.yaw = p.yaw + swing * 0.5f;
				case "sleg_b" -> m.yaw = p.yaw - swing * 0.5f;
				case "wing_l" -> m.roll = p.roll + (air ? MathHelper.sin(age * 1.6f) * 0.9f + 0.6f : 0f);
				case "wing_r" -> m.roll = p.roll - (air ? MathHelper.sin(age * 1.6f) * 0.9f + 0.6f : 0f);
				case "tail" -> m.yaw = p.yaw + MathHelper.sin(age * 0.12f) * 0.12f + swing * 0.2f;
				case "tail2" -> m.yaw = p.yaw + MathHelper.sin(age * 0.12f - 0.8f) * 0.18f;
				case "stinger" -> m.pitch = p.pitch + MathHelper.sin(age * 0.08f + phase) * 0.06f;
				case "arm" -> m.yaw = p.yaw + MathHelper.sin(age * 0.1f + phase) * 0.12f;
				case "fin_tail" -> m.yaw = p.yaw + MathHelper.sin(age * (water ? 0.45f : 0.9f)) * 0.45f;
				case "fluke" -> m.pitch = p.pitch + MathHelper.sin(age * 0.12f) * 0.25f;
				case "flipper_l" -> m.roll = p.roll + MathHelper.sin(age * 0.2f) * 0.3f + swing * 0.4f;
				case "flipper_r" -> m.roll = p.roll - MathHelper.sin(age * 0.2f) * 0.3f - swing * 0.4f;
				case "tentacle" -> m.pitch = p.pitch + MathHelper.sin(age * 0.15f + phase) * 0.15f;
				case "bell" -> m.yScale = 1f + MathHelper.sin(age * 0.2f) * 0.08f;
				default -> {
				}
			}
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
