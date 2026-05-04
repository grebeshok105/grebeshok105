package com.example.superheroes.effect;

import com.example.superheroes.sound.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReinhardTimeSlowController {
	private static final float NORMAL_TICK_RATE = 20.0f;
	private static final float SLOW_TICK_RATE = 4.0f;
	private static final long SLOW_DURATION_MS = 8500L;

	private static final Set<UUID> ARMED = ConcurrentHashMap.newKeySet();
	private static final Map<UUID, Long> ACTIVE = new ConcurrentHashMap<>();

	private ReinhardTimeSlowController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(ReinhardTimeSlowController::tick);

		AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
			if (world.isClientSide) return InteractionResult.PASS;
			if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
			if (!ReinhardController.isReinhard(sp)) return InteractionResult.PASS;
			if (!(target instanceof LivingEntity living) || living == sp) return InteractionResult.PASS;
			if (!ARMED.remove(sp.getUUID())) return InteractionResult.PASS;
			triggerSlow(sp);
			return InteractionResult.PASS;
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

		applyServerTickRate(player.getServer(), SLOW_TICK_RATE);

		player.displayClientMessage(
				Component.translatable("ability.superheroes.reinhard.time_slow.start"),
				true);
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
