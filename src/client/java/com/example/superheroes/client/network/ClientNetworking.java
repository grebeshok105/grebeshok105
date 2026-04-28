package com.example.superheroes.client.network;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.ClientReactorState;
import com.example.superheroes.client.RemoteHeroSkins;
import com.example.superheroes.network.MadnessSyncS2CPayload;
import com.example.superheroes.network.MadnessVisualS2CPayload;
import com.example.superheroes.network.ReactorStateS2CPayload;
import com.example.superheroes.client.ClientMadnessState;
import com.example.superheroes.client.hud.BloodRainHud;
import com.example.superheroes.client.fx.ScreenShakeManager;
import com.example.superheroes.client.render.LaserBeamRenderer;
import com.example.superheroes.client.render.RepulsorBeamRenderer;
import com.example.superheroes.network.HeroDataSyncS2CPayload;
import com.example.superheroes.network.LaserFiredS2CPayload;
import com.example.superheroes.network.RepulsorBlastS2CPayload;
import com.example.superheroes.network.RemoteHeroSkinS2CPayload;
import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.network.ResourceUpdateS2CPayload;
import com.example.superheroes.network.ScreenShakeS2CPayload;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientNetworking {
	private ClientNetworking() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(HeroDataSyncS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> {
					HeroData data = payload.data();
					ClientHeroState.update(data);
					LocalPlayer self = Minecraft.getInstance().player;
					if (self != null) {
						HeroData previous = self.getAttachedOrCreate(ModAttachments.HERO_DATA);
						self.setAttached(ModAttachments.HERO_DATA, data);
						if (previous.hasHero() != data.hasHero()
								|| (data.hasHero() && !data.heroId().equals(previous.heroId()))) {
							self.refreshDimensions();
						}
						boolean wasFlight = previous.isActive(AbilityIds.FLIGHT)
								|| previous.isActive(AbilityIds.IRON_MAN_FLIGHT)
								|| previous.isActive(AbilityIds.SUPERSONIC);
						boolean isFlight = data.isActive(AbilityIds.FLIGHT)
								|| data.isActive(AbilityIds.IRON_MAN_FLIGHT)
								|| data.isActive(AbilityIds.SUPERSONIC);
						if (!wasFlight && isFlight && !self.isFallFlying()) {
							self.startFallFlying();
						}
					}
				}));

		ClientPlayNetworking.registerGlobalReceiver(ResourceUpdateS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientHeroState.updateResources(payload.energy(), payload.mana())));

		ClientPlayNetworking.registerGlobalReceiver(LaserFiredS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> LaserBeamRenderer.add(payload.start(), payload.end())));

		ClientPlayNetworking.registerGlobalReceiver(RepulsorBlastS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> RepulsorBeamRenderer.add(payload.start(), payload.end())));

		ClientPlayNetworking.registerGlobalReceiver(ScreenShakeS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ScreenShakeManager.shake(payload.intensity(), payload.durationTicks())));

		ClientPlayNetworking.registerGlobalReceiver(RemoteHeroSkinS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> RemoteHeroSkins.put(payload.playerId(), payload.heroId().orElse(null))));

		ClientPlayNetworking.registerGlobalReceiver(ReactorStateS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientReactorState.update(payload.active(), payload.progressTicks(), payload.totalTicks(), payload.hasStock())));

		ClientPlayNetworking.registerGlobalReceiver(MadnessSyncS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientMadnessState.update(
						payload.madness(), payload.bonusLifeAvailable(),
						payload.readingUntilMs(), payload.manaLockUntilMs())));

		ClientPlayNetworking.registerGlobalReceiver(MadnessVisualS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> {
					if (payload.event() == MadnessVisualS2CPayload.EVENT_ENTER) {
						BloodRainHud.trigger();
					} else if (payload.event() == MadnessVisualS2CPayload.EVENT_EXIT) {
						BloodRainHud.clear();
					}
				}));
	}
}
