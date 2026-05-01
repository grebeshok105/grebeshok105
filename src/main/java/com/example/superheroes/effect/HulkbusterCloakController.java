package com.example.superheroes.effect;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.entity.HulkbusterCloakEntity;
import com.example.superheroes.entity.ModEntities;
import com.example.superheroes.hero.IronManHero;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Поддерживает по одному {@link HulkbusterCloakEntity} на каждого Iron Man-игрока,
 * у которого включён toggle {@code iron_man_hulkbuster}. Накидка следует за игроком
 * (позиция + yaw), отражает walking-стейт. При выключении / смерти / disconnect /
 * смене героя — удаляется автоматически.
 */
public final class HulkbusterCloakController {
	private static final Map<UUID, UUID> PLAYER_TO_CLOAK = new ConcurrentHashMap<>();
	private static final Map<UUID, Vec3> LAST_POS = new ConcurrentHashMap<>();

	private HulkbusterCloakController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Map<UUID, ServerPlayer> active = new HashMap<>();
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				if (isHulkbusterActive(p)) active.put(p.getUUID(), p);
			}

			for (ServerPlayer ironman : active.values()) {
				ensureCloak(ironman);
			}

			Iterator<Map.Entry<UUID, UUID>> it = PLAYER_TO_CLOAK.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, UUID> entry = it.next();
				UUID playerId = entry.getKey();
				UUID cloakId = entry.getValue();
				if (!active.containsKey(playerId)) {
					removeCloakById(server, cloakId);
					LAST_POS.remove(playerId);
					it.remove();
				}
			}
		});
	}

	private static boolean isHulkbusterActive(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.hasHero()) return false;
		if (!IronManHero.ID.equals(data.heroId())) return false;
		return data.isActive(AbilityIds.IRON_MAN_HULKBUSTER);
	}

	private static void ensureCloak(ServerPlayer ironman) {
		ServerLevel level = ironman.serverLevel();
		UUID existing = PLAYER_TO_CLOAK.get(ironman.getUUID());
		HulkbusterCloakEntity cloak = null;
		if (existing != null) {
			Entity e = level.getEntity(existing);
			if (e instanceof HulkbusterCloakEntity hc && hc.isAlive()) {
				cloak = hc;
			} else {
				PLAYER_TO_CLOAK.remove(ironman.getUUID());
			}
		}
		if (cloak == null) {
			cloak = ModEntities.HULKBUSTER_CLOAK.create(level);
			if (cloak == null) return;
			cloak.setOwner(ironman);
			cloak.moveTo(ironman.getX(), ironman.getY(), ironman.getZ(), ironman.yBodyRot, 0f);
			cloak.setYRot(ironman.yBodyRot);
			cloak.yRotO = ironman.yBodyRot;
			level.addFreshEntity(cloak);
			PLAYER_TO_CLOAK.put(ironman.getUUID(), cloak.getUUID());
		}

		Vec3 nowPos = new Vec3(ironman.getX(), ironman.getY(), ironman.getZ());
		Vec3 lastPos = LAST_POS.get(ironman.getUUID());
		double horizDeltaSq = 0;
		if (lastPos != null) {
			double dx = nowPos.x - lastPos.x;
			double dz = nowPos.z - lastPos.z;
			horizDeltaSq = dx * dx + dz * dz;
		}
		LAST_POS.put(ironman.getUUID(), nowPos);
		boolean walking = horizDeltaSq > 0.0008;

		float yaw = ironman.yBodyRot;
		// Не moveTo — иначе схлопнется yRotO=yRot, ломает интерполяцию yaw на клиенте.
		cloak.yRotO = cloak.getYRot();
		cloak.setPos(nowPos.x, nowPos.y, nowPos.z);
		cloak.setYRot(yaw);
		cloak.setYHeadRot(yaw);
		cloak.setYBodyRot(yaw);
		cloak.setWalking(walking);
		cloak.setInvisible(ironman.isInvisible());
	}

	private static void removeCloakById(MinecraftServer server, UUID cloakId) {
		for (ServerLevel level : server.getAllLevels()) {
			Entity e = level.getEntity(cloakId);
			if (e != null) {
				e.discard();
				return;
			}
		}
	}

	public static void clear(UUID playerId) {
		PLAYER_TO_CLOAK.remove(playerId);
		LAST_POS.remove(playerId);
	}
}
