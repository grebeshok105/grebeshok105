package com.example.superheroes.client.mixin;

import com.example.superheroes.client.HulkbusterCloakClientTracker;
import com.example.superheroes.client.render.HulkbusterMorphRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Заменяет рендер игрока на Hulkbuster-iron-golem пока на нём активен toggle.
 * <ul>
 *   <li>HEAD: если игрок в трекере morphed — рисуем голема прямо на координатах
 *       игрока через {@link HulkbusterMorphRenderer}, отменяем ванильный рендер.</li>
 *   <li>First-person руки не задеваются (renderHand идёт мимо этого инжекта).</li>
 * </ul>
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererHulkbusterHideMixin {
	@Inject(
			method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void superheroes$hulkbusterMorphRender(AbstractClientPlayer player, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		if (!HulkbusterCloakClientTracker.isWearing(player.getUUID())) return;

		Minecraft mc = Minecraft.getInstance();
		// Local-player в первом лице: ванильный третьеличный рендер вообще не вызывается;
		// если всё-таки сюда заехало — пропускаем, чтобы голем не появился перед глазами.
		if (mc.options.getCameraType().isFirstPerson()
				&& mc.player != null
				&& mc.player.getUUID().equals(player.getUUID())) {
			ci.cancel();
			return;
		}

		if (HulkbusterMorphRenderer.tryRender(player, entityYaw, partialTick, poseStack, bufferSource, packedLight)) {
			ci.cancel();
		}
		// если морф-рендер не получился — даём ванильному отработать,
		// чтобы игрок не пропадал в пустоту.
	}
}
