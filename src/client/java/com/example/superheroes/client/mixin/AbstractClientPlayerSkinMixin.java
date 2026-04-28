package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.RemoteHeroSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerSkinMixin {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void superheroes$forceWideModel(CallbackInfoReturnable<PlayerSkin> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		if (!superheroes$hasHero(self)) {
			return;
		}
		PlayerSkin orig = cir.getReturnValue();
		if (orig == null || orig.model() == PlayerSkin.Model.WIDE) {
			return;
		}
		cir.setReturnValue(new PlayerSkin(
				orig.texture(),
				orig.textureUrl(),
				orig.capeTexture(),
				orig.elytraTexture(),
				PlayerSkin.Model.WIDE,
				orig.secure()
		));
	}

	@Unique
	private static boolean superheroes$hasHero(AbstractClientPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
			return ClientHeroState.data().hasHero();
		}
		return RemoteHeroSkins.get(player.getUUID()) != null;
	}
}
