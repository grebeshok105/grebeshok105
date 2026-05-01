package com.example.superheroes.effect;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.HomelanderHero;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class HomelanderFlightSpeedController {
	private static final int MIN_PERCENT = 50;
	private static final int MAX_PERCENT = 150;
	private static final int STEP = 5;

	private HomelanderFlightSpeedController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
			}
		});
	}

	public static void adjust(ServerPlayer player, int delta) {
		if (!canAdjust(player)) {
			return;
		}
		int current = player.getAttachedOrCreate(ModAttachments.FLIGHT_SPEED_PERCENT);
		int steppedDelta = delta > 0 ? STEP : -STEP;
		int next = clamp(current + steppedDelta);
		if (next != current) {
			player.setAttached(ModAttachments.FLIGHT_SPEED_PERCENT, next);
			ModNetworking.syncFlightSpeed(player);
		}
	}

	private static void tick(ServerPlayer player) {
		if (!canAdjust(player) || !player.isFallFlying()) {
			return;
		}
		int percent = player.getAttachedOrCreate(ModAttachments.FLIGHT_SPEED_PERCENT);
		if (percent == 100) {
			return;
		}
		double extra = (percent - 100) / 1000.0;
		Vec3 delta = player.getDeltaMovement();
		player.setDeltaMovement(delta.x * (1.0 + extra), delta.y, delta.z * (1.0 + extra));
		player.hurtMarked = true;
	}

	private static boolean canAdjust(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero()
				&& HomelanderHero.ID.equals(data.heroId())
				&& data.isActive(AbilityIds.FLIGHT);
	}

	private static int clamp(int value) {
		return Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, value));
	}
}
