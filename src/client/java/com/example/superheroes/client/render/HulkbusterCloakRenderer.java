package com.example.superheroes.client.render;

import com.example.superheroes.ModId;
import com.example.superheroes.entity.HulkbusterCloakEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

/**
 * Кастомный рендерер: использует ванильный {@link IronGolemRenderer},
 * но подменяет текстуру на Hulkbuster skin. Также пропускает рендер
 * для local-player в первом лице (чтобы накидка не висела перед глазами).
 */
public class HulkbusterCloakRenderer extends IronGolemRenderer {
	private static final ResourceLocation TEXTURE = ModId.of("textures/entity/hulkbuster.png");

	public HulkbusterCloakRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.shadowRadius = 0.7f;
	}

	@Override
	public ResourceLocation getTextureLocation(IronGolem entity) {
		return TEXTURE;
	}

	@Override
	public void render(IronGolem entity, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (entity instanceof HulkbusterCloakEntity cloak) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.getCameraType().isFirstPerson()
					&& mc.player != null
					&& mc.player.getUUID().equals(cloak.getOwnerUuid())) {
				return;
			}
		}
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
