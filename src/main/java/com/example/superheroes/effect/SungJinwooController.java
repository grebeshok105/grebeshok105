package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.entity.ModEntities;
import com.example.superheroes.entity.ShadowSoldierEntity;
import com.example.superheroes.hero.SungJinwooHero;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.network.SungShadowArmyS2CPayload;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управляет армией Теневых Солдат для Сон Джи Ву.
 *
 * Главные правила:
 *  - При активации героя автоматически спавнится 10 теней.
 *  - ВЕСЬ урон по Сону перенаправляется на ОДНУ случайную живую тень
 *    (без радиуса — где бы тени ни были, даже в другом измерении).
 *  - Если живых теней нет — Сон получает урон сам.
 *  - При снятии костюма / выходе из игры — тени деспавнятся.
 */
public final class SungJinwooController {
	public static final int MAX_SHADOWS = 10;

	private static final Map<UUID, List<UUID>> ARMY = new ConcurrentHashMap<>();
	private static final Random RNG = new Random();

	private SungJinwooController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});

		// ALWAYS — урон пришёл по Сон Джи Ву → переводим на тень.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			if (!isSung(player)) return true;
			ShadowSoldierEntity victim = pickRandomAliveShadow(player);
			if (victim == null) return true;
			// Не зацикливаемся: если urs damage пришёл сам от тени-хозяина (артефакт) — пропускаем.
			Entity src = source.getEntity();
			if (src != null && src.getUUID().equals(victim.getUUID())) return true;
			// Перенаправляем удар на тень (без радиуса).
			DamageSource diverted = src != null
					? player.damageSources().mobAttack(victim) // фейковый источник чтоб AI не растерялся
					: player.damageSources().generic();
			victim.hurt(diverted, amount);
			ServerLevel level = player.serverLevel();
			level.sendParticles(ParticleTypes.WARPED_SPORE, player.getX(), player.getY() + 1.0, player.getZ(),
					16, 0.4, 0.6, 0.4, 0.05);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.6f, 0.8f);
			return false;
		});
	}

	public static boolean isSung(ServerPlayer player) {
		HeroData data = player.getAttached(ModAttachments.HERO_DATA);
		return data != null && SungJinwooHero.ID.equals(data.heroId());
	}

	private static void tickPlayer(ServerPlayer player) {
		boolean sung = isSung(player);
		if (!sung) {
			disbandIfPresent(player);
			return;
		}

		List<UUID> ids = ARMY.computeIfAbsent(player.getUUID(), u -> new ArrayList<>());
		// Чистим мёртвых
		ids.removeIf(uuid -> {
			Entity e = player.serverLevel().getEntity(uuid);
			return !(e instanceof ShadowSoldierEntity ss) || !ss.isAlive();
		});

		if (ids.isEmpty()) {
			// Свежий герой — призываем 10 теней
			summonInitialArmy(player);
			ids = ARMY.computeIfAbsent(player.getUUID(), u -> new ArrayList<>());
		}

		// Sync Phase: phase 1 если 0 теней, phase 2 если ≥1
		// (тут тени всегда есть после init, но клиент сам по этому флагу решает)
		boolean hasShadows = !ids.isEmpty();
		broadcastArmyState(player, hasShadows, ids.size());
	}

	public static void summonInitialArmy(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		List<UUID> ids = ARMY.computeIfAbsent(player.getUUID(), u -> new ArrayList<>());
		ids.clear();
		for (int i = 0; i < MAX_SHADOWS; i++) {
			ShadowSoldierEntity shadow = spawnOneShadowAt(level, player, player.position().add(
					(RNG.nextDouble() - 0.5) * 4.0, 1.5 + RNG.nextDouble() * 2.0, (RNG.nextDouble() - 0.5) * 4.0));
			if (shadow != null) {
				ids.add(shadow.getUUID());
			}
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.7f, 1.4f);
		level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(),
				120, 1.5, 1.5, 1.5, 0.4);
	}

	public static ShadowSoldierEntity spawnOneShadowAt(ServerLevel level, ServerPlayer owner, Vec3 pos) {
		ShadowSoldierEntity shadow = ModEntities.SHADOW_SOLDIER.create(level);
		if (shadow == null) return null;
		shadow.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), 0f);
		shadow.setOwnerId(owner.getUUID());
		shadow.setVariant(RNG.nextInt(ShadowSoldierEntity.VARIANT_COUNT));
		shadow.finalizeSpawn(level, level.getCurrentDifficultyAt(shadow.blockPosition()),
				MobSpawnType.MOB_SUMMONED, null);
		level.addFreshEntity(shadow);
		level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 0.5, pos.z, 18, 0.3, 0.4, 0.3, 0.05);
		return shadow;
	}

	public static int aliveCount(ServerPlayer player) {
		List<UUID> ids = ARMY.get(player.getUUID());
		if (ids == null) return 0;
		int n = 0;
		for (UUID id : ids) {
			Entity e = player.serverLevel().getEntity(id);
			if (e instanceof ShadowSoldierEntity ss && ss.isAlive()) n++;
		}
		return n;
	}

	public static List<ShadowSoldierEntity> aliveShadows(ServerPlayer player) {
		List<ShadowSoldierEntity> out = new ArrayList<>();
		List<UUID> ids = ARMY.get(player.getUUID());
		if (ids == null) return out;
		for (UUID id : ids) {
			Entity e = player.serverLevel().getEntity(id);
			if (e instanceof ShadowSoldierEntity ss && ss.isAlive()) out.add(ss);
		}
		return out;
	}

	public static ShadowSoldierEntity pickRandomAliveShadow(ServerPlayer player) {
		List<ShadowSoldierEntity> list = aliveShadows(player);
		if (list.isEmpty()) return null;
		return list.get(RNG.nextInt(list.size()));
	}

	public static void registerExtraShadow(ServerPlayer owner, ShadowSoldierEntity shadow) {
		List<UUID> ids = ARMY.computeIfAbsent(owner.getUUID(), u -> new ArrayList<>());
		// Соблюдаем лимит 10 — если больше, удаляем самого старого
		if (ids.size() >= MAX_SHADOWS) {
			UUID oldest = ids.remove(0);
			Entity e = owner.serverLevel().getEntity(oldest);
			if (e instanceof ShadowSoldierEntity old && old.isAlive()) {
				old.discard();
				owner.serverLevel().sendParticles(ParticleTypes.PORTAL, old.getX(), old.getY() + 1, old.getZ(),
						20, 0.3, 0.6, 0.3, 0.1);
			}
		}
		ids.add(shadow.getUUID());
	}

	public static void disbandAll(ServerPlayer player) {
		List<UUID> ids = ARMY.remove(player.getUUID());
		if (ids == null) return;
		ServerLevel level = player.serverLevel();
		for (UUID id : ids) {
			Entity e = level.getEntity(id);
			if (e instanceof ShadowSoldierEntity ss) {
				level.sendParticles(ParticleTypes.PORTAL, ss.getX(), ss.getY() + 1, ss.getZ(),
						20, 0.3, 0.6, 0.3, 0.15);
				ss.discard();
			}
		}
	}

	private static void disbandIfPresent(ServerPlayer player) {
		if (ARMY.containsKey(player.getUUID())) {
			disbandAll(player);
			broadcastArmyState(player, false, 0);
		}
	}

	private static void broadcastArmyState(ServerPlayer player, boolean hasShadows, int count) {
		// Отправляем всем трекающим игрокам (включая самого Сон) флаг для свапа фазы.
		SungShadowArmyS2CPayload payload = new SungShadowArmyS2CPayload(player.getUUID(), hasShadows, count);
		for (ServerPlayer p : player.serverLevel().players()) {
			ServerPlayNetworking.send(p, payload);
		}
	}
}
