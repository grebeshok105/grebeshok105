package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.RemoteHeroSkins;
import com.example.superheroes.hero.Hero;
import com.example.superheroes.hero.Heroes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
	@Inject(method = "setModelProperties", at = @At("RETURN"))
	private void superheroes$hideSkinLayersForHero(AbstractClientPlayer player, CallbackInfo ci) {
		if (!superheroes$hasHero(player)) {
			return;
		}
		PlayerRenderer self = (PlayerRenderer) (Object) this;
		PlayerModel<AbstractClientPlayer> model = self.getModel();
		model.hat.visible = false;
		model.jacket.visible = false;
		model.leftSleeve.visible = false;
		model.rightSleeve.visible = false;
		model.leftPants.visible = false;
		model.rightPants.visible = false;
	}

	@Unique
	private static boolean superheroes$hasHero(AbstractClientPlayer player) {
		if (player == Minecraft.getInstance().player) {
			return ClientHeroState.data().hasHero();
		}
		return RemoteHeroSkins.get(player.getUUID()) != null;
	}
	@ModifyVariable(
			method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V",
			at = @At("STORE"),
			ordinal = 0
	)
	private ResourceLocation superheroes$useHeroHandTexture(ResourceLocation original, PoseStack poseStack, MultiBufferSource multiBufferSource, int light,
			AbstractClientPlayer player, ModelPart arm, ModelPart sleeve) {
		ResourceLocation texture = superheroes$getHeroTexture(player);
		return texture == null ? original : texture;
	}

	@Unique
	private static ResourceLocation superheroes$getHeroTexture(AbstractClientPlayer player) {
		if (player != Minecraft.getInstance().player) {
			return null;
		}
		if (!ClientHeroState.data().hasHero()) {
			return null;
		}
		Hero hero = Heroes.get(ClientHeroState.data().heroId());
		return hero == null ? null : hero.getSkinTexture();
	}
}
