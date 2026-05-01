package com.example.superheroes.client;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.hero.HomelanderHero;
import com.example.superheroes.network.FlightSpeedC2SPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class FlightSpeedScrollHandler {
	private FlightSpeedScrollHandler() {
	}

	public static boolean handle(double vertical) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null || vertical == 0) {
			return false;
		}
		long window = mc.getWindow().getWindow();
		boolean altDown = InputConstants.isKeyDown(window, InputConstants.KEY_LALT)
				|| InputConstants.isKeyDown(window, InputConstants.KEY_RALT);
		if (!altDown) {
			return false;
		}
		if (!ClientHeroState.data().hasHero()
				|| !HomelanderHero.ID.equals(ClientHeroState.data().heroId())
				|| !ClientHeroState.data().isActive(AbilityIds.FLIGHT)) {
			return false;
		}
		int delta = vertical > 0 ? 5 : -5;
		ClientPlayNetworking.send(new FlightSpeedC2SPayload(delta));
		ClientFlightSpeedState.update(Math.max(50, Math.min(150, ClientFlightSpeedState.percent() + delta)));
		return true;
	}
}
