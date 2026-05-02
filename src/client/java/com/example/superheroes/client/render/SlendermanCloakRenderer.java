package com.example.superheroes.client.render;

import com.example.superheroes.entity.SlendermanCloakEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Зарегистрирован для {@code SlendermanCloakEntity}, но сам по себе ничего
 * НЕ рисует на позиции cloak'а. Реальный рендер вызывается из
 * {@code PlayerRendererSlendermanHideMixin} через {@link #renderProxy} прямо
 * на координатах/yaw/walking-стейте игрока — это убирает desync rotation
 * и позиционный лаг, который был у follow-entity-подхода.
 */
public class SlendermanCloakRenderer extends GeoEntityRenderer<SlendermanCloakEntity> {
	public static volatile SlendermanCloakRenderer INSTANCE;

	public SlendermanCloakRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new SlendermanCloakModel());
		this.shadowRadius = 0.5f;
		this.withScale(1.0f);
		INSTANCE = this;
	}

	@Override
	public void render(SlendermanCloakEntity entity, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		// no-op
	}

	public void renderProxy(SlendermanCloakEntity cloak, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(cloak, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
