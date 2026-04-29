package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.RemoteHeroSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerSkinMixin {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void superheroes$forceDefaultSteve(CallbackInfoReturnable<PlayerSkin> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		if (!superheroes$hasHero(self)) {
			return;
		}
		PlayerSkin orig = cir.getReturnValue();
		cir.setReturnValue(new PlayerSkin(
				DefaultPlayerSkin.getDefaultTexture(),
				null,
				null,
				null,
				PlayerSkin.Model.WIDE,
				orig != null && orig.secure()
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
