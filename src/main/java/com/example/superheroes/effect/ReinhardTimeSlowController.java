package com.example.superheroes.effect;

import com.example.superheroes.network.ReinhardTimeSlowS2CPayload;
import com.example.superheroes.sound.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReinhardTimeSlowController {
	private static final float NORMAL_TICK_RATE = 20.0f;
	// 0.6 tps == 3% of normal — effectively a global freeze (no one can attack/move during it).
	private static final float SLOW_TICK_RATE = 0.6f;
	private static final long SLOW_DURATION_MS = 8500L;

	private static final Set<UUID> ARMED = ConcurrentHashMap.newKeySet();
	private static final Map<UUID, Long> ACTIVE = new ConcurrentHashMap<>();

	private ReinhardTimeSlowController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(ReinhardTimeSlowController::tick);

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (ReinhardSwordDeathMarkController.isFlushing()) return true;
			if (amount <= 0f) return true;
			if (!(source.getEntity() instanceof ServerPlayer attacker)) return true;
			if (!(entity instanceof LivingEntity living) || living == attacker) return true;
			if (!ReinhardController.isReinhard(attacker)) return true;
			if (!ARMED.contains(attacker.getUUID())) return true;
			if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
			if (!(attacker.getMainHandItem().getItem() instanceof com.example.superheroes.item.RoyalIcicleItem)) return true;
			if (!source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) return true;
			ReinhardState rstate = attacker.getAttachedOrCreate(com.example.superheroes.attachment.ModAttachments.REINHARD_STATE);
			if (!rstate.swordDrawn()) return true;
			if (!ARMED.remove(attacker.getUUID())) return true;
			triggerSlow(attacker);
			return true;
		});
	}

	public static void armForFirstStrike(ServerPlayer player) {
		ARMED.add(player.getUUID());
	}

	public static void disarmForFirstStrike(ServerPlayer player) {
		ARMED.remove(player.getUUID());
	}

	private static void triggerSlow(ServerPlayer player) {
		long endAt = System.currentTimeMillis() + SLOW_DURATION_MS;
		ACTIVE.put(player.getUUID(), endAt);

		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.REINHARD_SWORD_STRIKE_VOICE, SoundSource.PLAYERS, 2.0f, 1.0f);

		broadcastTimeSlow(player, true);
		applyServerTickRate(player.getServer(), SLOW_TICK_RATE);
	}

	private static void broadcastTimeSlow(ServerPlayer reinhard, boolean active) {
		ServerLevel level = reinhard.serverLevel();
		AABB box = new AABB(reinhard.position(), reinhard.position()).inflate(80.0);
		ReinhardTimeSlowS2CPayload payload = new ReinhardTimeSlowS2CPayload(active);
		for (ServerPlayer target : level.getEntitiesOfClass(ServerPlayer.class, box, p -> true)) {
			ServerPlayNetworking.send(target, payload);
		}
	}

	private static void broadcastTimeSlowOff(MinecraftServer server) {
		ReinhardTimeSlowS2CPayload payload = new ReinhardTimeSlowS2CPayload(false);
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(p, payload);
		}
	}

	private static void tick(MinecraftServer server) {
		if (ACTIVE.isEmpty()) return;
		long now = System.currentTimeMillis();
		boolean anyEnded = false;
		Iterator<Map.Entry<UUID, Long>> it = ACTIVE.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Long> e = it.next();
			if (now >= e.getValue()) {
				it.remove();
				anyEnded = true;
				ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
				if (player != null) {
					rearmIfStillDrawn(player);
				}
			}
		}
		if (anyEnded && ACTIVE.isEmpty()) {
			applyServerTickRate(server, NORMAL_TICK_RATE);
			broadcastTimeSlowOff(server);
			ReinhardSwordDeathMarkController.flushDeaths(server);
		}
	}

	private static void rearmIfStillDrawn(ServerPlayer player) {
		var attach = com.example.superheroes.attachment.ModAttachments.REINHARD_STATE;
		ReinhardState state = player.getAttachedOrCreate(attach);
		if (state.swordDrawn()) {
			ARMED.add(player.getUUID());
		}
	}

	private static void applyServerTickRate(MinecraftServer server, float rate) {
		if (server == null) return;
		try {
			server.tickRateManager().setTickRate(rate);
		} catch (Throwable t) {
			com.example.superheroes.SuperheroesMod.LOGGER.warn("Failed to set tick rate", t);
		}
	}
}
