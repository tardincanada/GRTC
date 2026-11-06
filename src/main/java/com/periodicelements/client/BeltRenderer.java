package com.periodicelements.client;

import com.periodicelements.logistics.BeltBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

/** Draws a belt's load riding along it (3.40.0): one to three copies of the item, more for a bigger load. */
public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity> {
	public BeltRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(BeltBlockEntity belt, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
		ItemStack stack = belt.getStack(0);
		if (stack.isEmpty() || belt.getWorld() == null) {
			return;
		}
		Direction d = belt.facing();
		float along = belt.progress(tickDelta) - 0.5f;
		int copies = stack.getCount() >= 16 ? 3 : stack.getCount() >= 4 ? 2 : 1;
		int itemLight = WorldRenderer.getLightmapCoordinates(belt.getWorld(), belt.getPos().up());
		boolean block = stack.getItem() instanceof BlockItem;
		for (int i = 0; i < copies; i++) {
			matrices.push();
			float back = -i * 0.14f;
			matrices.translate(0.5 + d.getOffsetX() * (along + back), 0.25 + (block ? 0.12 : 0.02) + i * 0.02,
					0.5 + d.getOffsetZ() * (along + back));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-d.asRotation() + i * 15));
			if (!block) {
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
			}
			float s = block ? 0.35f : 0.45f;
			matrices.scale(s, s, s);
			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, itemLight, overlay,
					matrices, vertices, belt.getWorld(), (int) belt.getPos().asLong() + i);
			matrices.pop();
		}
	}
}
