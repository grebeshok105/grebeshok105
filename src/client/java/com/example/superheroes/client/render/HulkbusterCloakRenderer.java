package com.example.superheroes.client.render;

import com.example.superheroes.ModId;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

/**
 * Зарегистрирован для {@code HulkbusterCloakEntity} (это даёт нам экземпляр
 * рендерера через ванильный {@code EntityRenderDispatcher}). Сам cloak-entity
 * мы НЕ рисуем — она существует только как server-side marker / client-side
 * сигнал «этот игрок в Hulkbuster mode». Реальный рендер модели голема делает
 * {@code PlayerRendererHulkbusterMorphMixin} прямо на координатах игрока,
 * через {@link #renderProxy}, чтобы yaw / walking / attack-анимация копировались
 * с самого игрока без задержек сети.
 */
public class HulkbusterCloakRenderer extends IronGolemRenderer {
	private static final ResourceLocation TEXTURE = ModId.of("textures/entity/hulkbuster.png");

	public static volatile HulkbusterCloakRenderer INSTANCE;

	public HulkbusterCloakRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.shadowRadius = 0.7f;
		INSTANCE = this;
	}

	@Override
	public ResourceLocation getTextureLocation(IronGolem entity) {
		return TEXTURE;
	}

	@Override
	public void render(IronGolem entity, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		// no-op; cloak-entity невидима. Morph-рендер вызывается через renderProxy
		// для player-mixin'а.
	}

	/** Вызывается из {@code PlayerRendererHulkbusterMorphMixin}. */
	public void renderProxy(IronGolem proxy, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(proxy, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
