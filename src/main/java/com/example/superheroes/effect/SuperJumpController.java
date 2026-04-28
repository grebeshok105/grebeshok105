package com.example.superheroes.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SuperJumpController {
	private static final double JUMP_VELOCITY = 2.7;
	private static final int COOLDOWN_TICKS = 40;
	private static final int IMMUNITY_LIFE_TICKS = 400;

	private static final Map<UUID, Integer> COOLDOWN = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> FALL_IMMUNITY_UNTIL = new ConcurrentHashMap<>();

	private SuperJumpController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				Integer until = FALL_IMMUNITY_UNTIL.get(player.getUUID());
				if (until == null) {
					continue;
				}
				if (player.tickCount >= until) {
					FALL_IMMUNITY_UNTIL.remove(player.getUUID());
				} else if (player.onGround() && player.getDeltaMovement().y <= 0.0) {
					player.fallDistance = 0f;
					FALL_IMMUNITY_UNTIL.remove(player.getUUID());
				}
			}
		});
	}

	public static void activate(ServerPlayer player) {
		UUID id = player.getUUID();
		Integer ready = COOLDOWN.get(id);
		if (ready != null && player.tickCount < ready) {
			return;
		}
		COOLDOWN.put(id, player.tickCount + COOLDOWN_TICKS);
		FALL_IMMUNITY_UNTIL.put(id, player.tickCount + IMMUNITY_LIFE_TICKS);

		Vec3 v = player.getDeltaMovement();
		player.setDeltaMovement(v.x, JUMP_VELOCITY, v.z);
		player.hurtMarked = true;
		player.fallDistance = 0f;

		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4f, 0.6f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.6f);
		level.sendParticles(ParticleTypes.CLOUD,
				player.getX(), player.getY(), player.getZ(),
				40, 0.6, 0.05, 0.6, 0.4);
		level.sendParticles(ParticleTypes.LARGE_SMOKE,
				player.getX(), player.getY(), player.getZ(),
				20, 0.8, 0.05, 0.8, 0.05);
	}

	public static boolean hasFallImmunity(Player player) {
		Integer until = FALL_IMMUNITY_UNTIL.get(player.getUUID());
		return until != null && player.tickCount < until;
	}
}
