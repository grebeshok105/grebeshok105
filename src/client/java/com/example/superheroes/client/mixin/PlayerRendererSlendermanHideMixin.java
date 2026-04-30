package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.RemoteHeroSkins;
import com.example.superheroes.hero.SlendermanHero;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the vanilla third-person player render when the player is
 * transformed into Slenderman. The {@code SlendermanCloakEntity} provides
 * the GeckoLib visuals instead. First-person hand rendering is unaffected
 * (it uses {@code renderHand} which is not cancelled here).
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererSlendermanHideMixin {
	@Inject(
			method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void superheroes$cancelSlendermanRender(AbstractClientPlayer player, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		if (superheroes$isSlenderman(player)) {
			ci.cancel();
		}
	}

	@Unique
	private static boolean superheroes$isSlenderman(AbstractClientPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation heroId;
		if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
			heroId = ClientHeroState.data().hasHero() ? ClientHeroState.data().heroId() : null;
		} else {
			heroId = RemoteHeroSkins.get(player.getUUID());
		}
		return SlendermanHero.ID.equals(heroId);
	}
}
