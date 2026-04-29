package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.HomelanderHero;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class AutoSaturationController {
	private AutoSaturationController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 40 != 0) return;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isSpectator() || player.isCreative()) continue;
				HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
				if (data.hasHero() && HomelanderHero.ID.equals(data.heroId())) {
					continue;
				}
				if (player.getFoodData().getFoodLevel() >= 20 && player.getFoodData().getSaturationLevel() >= 19.9f) {
					continue;
				}
				player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2, 20, true, false, false));
			}
		});
	}
}
