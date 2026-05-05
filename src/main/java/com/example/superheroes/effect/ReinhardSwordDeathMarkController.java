package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.network.ReinhardSwordKillS2CPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Когда меч Рейнхарда наносит игроку летальный удар, мы:
 *  - отменяем смерть, садим жертву на 1 ХП,
 *  - запоминаем её и кто её отметил,
 *  - на её клиент шлём S2C payload — там HUD рисует красный оверлей,
 *  - после {@link #MAX_MARK_DURATION_MS} (или после окончания time-slow) жертва
 *    добивается финальным ударом.
 *
 * Используем {@link ServerLivingEntityEvents#ALLOW_DEATH} (а не ALLOW_DAMAGE),
 * чтобы у вызывающего {@code Item.hurtEnemy} прошёл штатный путь — внутри
 * {@code hurtEnemy} меч раздаёт BLINDNESS/AoE/VFX. ALLOW_DAMAGE отменял урон
 * целиком, и hurtEnemy не запускался — отсюда баг "по игрокам тёмнота не
 * срабатывала".
 *
 * На обычных мобах НЕ работает (фильтр {@code instanceof ServerPlayer victim}).
 */
public final class ReinhardSwordDeathMarkController {
	private static final long MAX_MARK_DURATION_MS = 9500L;

	/** victim UUID -> attacker UUID. */
	private static final Map<UUID, UUID> MARKED = new ConcurrentHashMap<>();
	/** victim UUID -> mark expiry (System.currentTimeMillis()). */
	private static final Map<UUID, Long> MARK_END_AT = new ConcurrentHashMap<>();
	/** Не даём собственному финальному {@code hurt(MAX_VALUE)} зациклиться. */
	private static final ThreadLocal<Boolean> FLUSHING = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private ReinhardSwordDeathMarkController() {
	}

	public static boolean isFlushing() {
		return Boolean.TRUE.equals(FLUSHING.get());
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (isFlushing()) return true;
			if (!(entity instanceof ServerPlayer victim)) return true;
			if (!(source.getEntity() instanceof ServerPlayer attacker)) return true;
			if (attacker == victim) return true;
			if (!ReinhardController.isReinhard(attacker)) return true;
			if (!source.is(DamageTypes.PLAYER_ATTACK)) return true;
			if (!(attacker.getMainHandItem().getItem() instanceof com.example.superheroes.item.RoyalIcicleItem)) return true;
			ReinhardState astate = attacker.getAttachedOrCreate(ModAttachments.REINHARD_STATE);
			if (!astate.swordDrawn()) return true;

			boolean firstMark = MARKED.putIfAbsent(victim.getUUID(), attacker.getUUID()) == null;
			MARK_END_AT.put(victim.getUUID(), System.currentTimeMillis() + MAX_MARK_DURATION_MS);
			victim.setHealth(0.5f);
			victim.invulnerableTime = 0;
			if (firstMark) {
				ServerPlayNetworking.send(victim, new ReinhardSwordKillS2CPayload(true));
			}
			return false;
		});

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (isFlushing()) return true;
			if (!(entity instanceof ServerPlayer victim)) return true;
			if (!MARKED.containsKey(victim.getUUID())) return true;
			if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
			victim.setHealth(0.5f);
			victim.invulnerableTime = 0;
			return false;
		});

		ServerTickEvents.END_SERVER_TICK.register(ReinhardSwordDeathMarkController::tick);
	}

	private static void tick(MinecraftServer server) {
		// Pin marked victims at 0.5 HP every tick so health regen / etc. can't pull them above the freeze.
		for (UUID id : MARKED.keySet()) {
			ServerPlayer victim = server.getPlayerList().getPlayer(id);
			if (victim == null || victim.isRemoved() || !victim.isAlive()) continue;
			if (victim.getHealth() > 0.5f) {
				victim.setHealth(0.5f);
			}
			victim.invulnerableTime = 0;
		}

		if (MARK_END_AT.isEmpty()) return;
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<UUID, Long>> it = MARK_END_AT.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Long> e = it.next();
			if (now < e.getValue()) continue;
			UUID victimId = e.getKey();
			it.remove();
			UUID attackerId = MARKED.remove(victimId);
			ServerPlayer victim = server.getPlayerList().getPlayer(victimId);
			if (victim == null || victim.isRemoved() || !victim.isAlive()) continue;
			ServerPlayNetworking.send(victim, new ReinhardSwordKillS2CPayload(false));
			ServerPlayer attacker = attackerId != null ? server.getPlayerList().getPlayer(attackerId) : null;
			finalKill(victim, attacker);
		}
	}

	public static void flushDeaths(MinecraftServer server) {
		if (MARKED.isEmpty()) return;
		Iterator<Map.Entry<UUID, UUID>> it = MARKED.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, UUID> entry = it.next();
			it.remove();
			MARK_END_AT.remove(entry.getKey());
			ServerPlayer victim = server.getPlayerList().getPlayer(entry.getKey());
			if (victim == null || victim.isRemoved() || !victim.isAlive()) continue;
			ServerPlayNetworking.send(victim, new ReinhardSwordKillS2CPayload(false));
			ServerPlayer attacker = server.getPlayerList().getPlayer(entry.getValue());
			finalKill(victim, attacker);
		}
	}

	public static void clearMark(UUID victimId) {
		MARKED.remove(victimId);
		MARK_END_AT.remove(victimId);
	}

	private static void finalKill(ServerPlayer victim, ServerPlayer attacker) {
		FLUSHING.set(Boolean.TRUE);
		try {
			var damage = attacker != null
					? victim.serverLevel().damageSources().playerAttack(attacker)
					: victim.serverLevel().damageSources().genericKill();
			victim.invulnerableTime = 0;
			victim.hurt(damage, Float.MAX_VALUE);
		} finally {
			FLUSHING.set(Boolean.FALSE);
		}
	}
}
