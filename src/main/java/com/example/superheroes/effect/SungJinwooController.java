package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.entity.ModEntities;
import com.example.superheroes.entity.ShadowSoldierEntity;
import com.example.superheroes.hero.SungJinwooHero;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управляет армией Теневых Солдат для Сон Джи Ву.
 *
 *  - При активации героя один раз спавнится 10 теней (5 наземных + 5 летающих)
 *    в полукруге сзади.
 *  - **Тени одноразовые** — после смерти не возвращаются. Новые тени можно
 *    получить только через Arise (поднять из тела врага).
 *  - ВЕСЬ урон по Сону всегда переадресуется на одну случайную живую тень
 *    (без радиуса). Если живых нет — Сон получает урон сам.
 *  - При снятии костюма / выходе из игры — тени деспавнятся.
 */
public final class SungJinwooController {
	public static final int MAX_SHADOWS = 10;

	private static final Map<UUID, List<UUID>> ARMY = new ConcurrentHashMap<>();
	private static final Set<UUID> SUMMONED = ConcurrentHashMap.newKeySet();
	private static final Random RNG = new Random();

	private SungJinwooController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			if (!isSung(player)) return true;
			ShadowSoldierEntity victim = pickRandomAliveShadow(player);
			if (victim == null) return true;
			Entity src = source.getEntity();
			if (src != null && src.getUUID().equals(victim.getUUID())) return true;
			DamageSource diverted = src != null
					? player.damageSources().mobAttack(victim)
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
		// Чистим мёртвых — но НЕ респавним.
		ids.removeIf(uuid -> {
			Entity e = player.serverLevel().getEntity(uuid);
			return !(e instanceof ShadowSoldierEntity ss) || !ss.isAlive();
		});

		if (!SUMMONED.contains(player.getUUID())) {
			summonInitialArmy(player);
			SUMMONED.add(player.getUUID());
			ids = ARMY.get(player.getUUID());
		}

		boolean hasShadows = ids != null && !ids.isEmpty();
		broadcastArmyState(player, hasShadows, ids == null ? 0 : ids.size());
	}

	public static void summonInitialArmy(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		List<UUID> ids = ARMY.computeIfAbsent(player.getUUID(), u -> new ArrayList<>());
		ids.clear();
		// 5 наземных, 5 летающих, расставлены полукругом сзади
		for (int i = 0; i < MAX_SHADOWS; i++) {
			boolean grounded = (i % 2 == 0); // чередуем
			ShadowSoldierEntity shadow = spawnSlotShadow(level, player, i, MAX_SHADOWS, grounded);
			if (shadow != null) {
				ids.add(shadow.getUUID());
			}
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WARDEN_EMERGE, SoundSource.PLAYERS, 0.5f, 1.6f);
		level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(),
				120, 1.5, 1.5, 1.5, 0.4);
	}

	private static ShadowSoldierEntity spawnSlotShadow(ServerLevel level, ServerPlayer owner,
			int slotIndex, int slotCount, boolean grounded) {
		double radius = grounded ? 3.0 : 4.0;
		// Полукруг за спиной: yaw + 180 ± 90°
		double baseYawRad = Math.toRadians(owner.getYRot() + 180.0);
		double slotAngle = (slotCount > 1)
				? (slotIndex - (slotCount - 1) / 2.0) * (Math.PI / Math.max(1, slotCount - 1)) * 0.95
				: 0.0;
		double angle = baseYawRad + slotAngle;
		double dx = -Math.sin(angle) * radius;
		double dz = Math.cos(angle) * radius;
		double y = grounded ? owner.getY() : owner.getY() + 2.5;
		Vec3 pos = new Vec3(owner.getX() + dx, y, owner.getZ() + dz);
		return spawnConfiguredShadowAt(level, owner, pos, slotIndex, slotCount, grounded);
	}

	public static ShadowSoldierEntity spawnConfiguredShadowAt(ServerLevel level, ServerPlayer owner,
			Vec3 pos, int slotIndex, int slotCount, boolean grounded) {
		ShadowSoldierEntity shadow = ModEntities.SHADOW_SOLDIER.create(level);
		if (shadow == null) return null;
		shadow.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), 0f);
		shadow.setOwnerId(owner.getUUID());
		shadow.setVariant(RNG.nextInt(ShadowSoldierEntity.VARIANT_COUNT));
		shadow.setGrounded(grounded);
		shadow.setSlot(slotIndex, slotCount);
		shadow.finalizeSpawn(level, level.getCurrentDifficultyAt(shadow.blockPosition()),
				MobSpawnType.MOB_SUMMONED, null);
		level.addFreshEntity(shadow);
		level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 0.5, pos.z, 18, 0.3, 0.4, 0.3, 0.05);
		return shadow;
	}

	/** Совместимость с прежним API (Arise и др.). Случайный слот, случайная позиция. */
	public static ShadowSoldierEntity spawnOneShadowAt(ServerLevel level, ServerPlayer owner, Vec3 pos) {
		boolean grounded = RNG.nextBoolean();
		List<UUID> ids = ARMY.computeIfAbsent(owner.getUUID(), u -> new ArrayList<>());
		int slotIndex = ids.size();
		int slotCount = Math.max(MAX_SHADOWS, slotIndex + 1);
		return spawnConfiguredShadowAt(level, owner, pos, slotIndex, slotCount, grounded);
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
		SUMMONED.remove(player.getUUID());
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
		if (ARMY.containsKey(player.getUUID()) || SUMMONED.contains(player.getUUID())) {
			disbandAll(player);
			broadcastArmyState(player, false, 0);
		}
	}

	private static void broadcastArmyState(ServerPlayer player, boolean hasShadows, int count) {
		SungShadowArmyS2CPayload payload = new SungShadowArmyS2CPayload(player.getUUID(), hasShadows, count);
		for (ServerPlayer p : player.serverLevel().players()) {
			ServerPlayNetworking.send(p, payload);
		}
	}
}
