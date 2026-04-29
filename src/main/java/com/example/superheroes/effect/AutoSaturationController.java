package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

public final class AutoSaturationController {
	private static final int CAP_FOOD = 17;

	private AutoSaturationController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 != 0) return;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isSpectator() || player.isCreative()) continue;
				HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
				if (!data.hasHero()) continue;
				int food = player.getFoodData().getFoodLevel();
				if (food < CAP_FOOD) {
					player.getFoodData().setFoodLevel(Math.min(CAP_FOOD, food + 1));
				} else if (food > CAP_FOOD) {
					player.getFoodData().setFoodLevel(CAP_FOOD);
				}
				player.getFoodData().setSaturation(0f);
			}
		});
	}
}
