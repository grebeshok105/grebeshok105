package com.example.superheroes.client.render;

import com.example.superheroes.item.VibraniumShieldItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class VibraniumShieldRenderer extends GeoItemRenderer<VibraniumShieldItem> {
	public VibraniumShieldRenderer() {
		super(new VibraniumShieldGeoModel());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
	                         MultiBufferSource buffers, int light, int overlay) {
		pose.pushPose();
		applyDisplayTransform(context, pose);
		super.renderByItem(stack, context, pose, buffers, light, overlay);
		pose.popPose();
	}

	private void applyDisplayTransform(ItemDisplayContext ctx, PoseStack pose) {
		switch (ctx) {
			case GUI -> {
				pose.translate(0.5, 0.5, 0.0);
				pose.mulPose(Axis.XP.rotationDegrees(180));
				pose.scale(1.0f, 1.0f, 1.0f);
			}
			case GROUND -> {
				pose.translate(0.5, 0.25, 0.5);
				pose.mulPose(Axis.XP.rotationDegrees(90));
				pose.scale(0.5f, 0.5f, 0.5f);
			}
			case FIXED -> {
				pose.translate(0.5, 0.5, 0.5);
				pose.mulPose(Axis.YP.rotationDegrees(180));
				pose.scale(1.0f, 1.0f, 1.0f);
			}
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				pose.translate(0.7, 0.6, -0.3);
				pose.mulPose(Axis.YP.rotationDegrees(ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 90 : -90));
				pose.mulPose(Axis.ZP.rotationDegrees(0));
				pose.scale(1.4f, 1.4f, 1.4f);
			}
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
				pose.translate(0.5, 0.4, 0.6);
				pose.mulPose(Axis.YP.rotationDegrees(ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? 90 : -90));
				pose.mulPose(Axis.XP.rotationDegrees(-10));
				pose.scale(1.1f, 1.1f, 1.1f);
			}
			case HEAD -> {
				pose.translate(0.5, 1.0, 0.5);
				pose.scale(1.4f, 1.4f, 1.4f);
			}
			default -> {
				pose.translate(0.5, 0.5, 0.5);
				pose.scale(1.0f, 1.0f, 1.0f);
			}
		}
	}
}
