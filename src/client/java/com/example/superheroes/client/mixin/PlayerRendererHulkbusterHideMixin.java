package com.example.superheroes.client.mixin;

import com.example.superheroes.client.HulkbusterCloakClientTracker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Скрывает ванильный рендер игрока в third-person, пока на нём активна
 * Hulkbuster-накидка (визуально игрок выглядит как iron golem с кастомной
 * текстурой через {@code HulkbusterCloakRenderer}). First-person руки —
 * не задеваются (renderHand идёт мимо этого инжекта).
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererHulkbusterHideMixin {
	@Inject(
			method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void superheroes$cancelHulkbusterRender(AbstractClientPlayer player, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		if (HulkbusterCloakClientTracker.isWearing(player.getUUID())) {
			ci.cancel();
		}
	}
}
