package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.entity.ModEntities;
import com.example.superheroes.entity.SlendermanCloakEntity;
import com.example.superheroes.hero.SlendermanHero;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spawns one {@link SlendermanCloakEntity} per Slenderman player. The cloak
 * follows the player, mirrors yaw / sprint / walk state, and renders the
 * GeckoLib model.
 */
public final class SlendermanCloakController {
	private static final Map<UUID, UUID> PLAYER_TO_CLOAK = new ConcurrentHashMap<>();
	private static final Map<UUID, Vec3> LAST_POS = new ConcurrentHashMap<>();

	private SlendermanCloakController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Map<UUID, ServerPlayer> slendermen = new HashMap<>();
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				if (isSlenderman(p)) slendermen.put(p.getUUID(), p);
			}

			for (ServerPlayer slender : slendermen.values()) {
				ensureCloak(slender);
			}

			java.util.Iterator<Map.Entry<UUID, UUID>> it = PLAYER_TO_CLOAK.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, UUID> entry = it.next();
				UUID playerId = entry.getKey();
				UUID cloakId = entry.getValue();
				if (!slendermen.containsKey(playerId)) {
					removeCloakById(server, cloakId);
					LAST_POS.remove(playerId);
					it.remove();
				}
			}
		});
	}

	private static boolean isSlenderman(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && SlendermanHero.ID.equals(data.heroId());
	}

	private static void ensureCloak(ServerPlayer slender) {
		ServerLevel level = slender.serverLevel();
		UUID existing = PLAYER_TO_CLOAK.get(slender.getUUID());
		SlendermanCloakEntity cloak = null;
		if (existing != null) {
			net.minecraft.world.entity.Entity e = level.getEntity(existing);
			if (e instanceof SlendermanCloakEntity sc && sc.isAlive()) {
				cloak = sc;
			} else {
				PLAYER_TO_CLOAK.remove(slender.getUUID());
			}
		}
		if (cloak == null) {
			cloak = ModEntities.SLENDERMAN_CLOAK.create(level);
			if (cloak == null) return;
			cloak.setOwner(slender);
			cloak.moveTo(slender.getX(), slender.getY(), slender.getZ(), slender.yBodyRot, 0f);
			cloak.setYRot(slender.yBodyRot);
			cloak.yRotO = slender.yBodyRot;
			level.addFreshEntity(cloak);
			PLAYER_TO_CLOAK.put(slender.getUUID(), cloak.getUUID());
		}

		// Detect actual horizontal movement by diffing position with previous tick.
		Vec3 nowPos = new Vec3(slender.getX(), slender.getY(), slender.getZ());
		Vec3 lastPos = LAST_POS.get(slender.getUUID());
		double horizDeltaSq = 0;
		if (lastPos != null) {
			double dx = nowPos.x - lastPos.x;
			double dz = nowPos.z - lastPos.z;
			horizDeltaSq = dx * dx + dz * dz;
		}
		LAST_POS.put(slender.getUUID(), nowPos);

		boolean sprinting = slender.isSprinting() && horizDeltaSq > 0.005;
		boolean walking = horizDeltaSq > 0.0008;

		float yaw = slender.yBodyRot;
		cloak.moveTo(nowPos.x, nowPos.y, nowPos.z, yaw, 0f);
		cloak.setYRot(yaw);
		cloak.setYHeadRot(yaw);
		cloak.setBodyYaw(yaw);
		cloak.setSprinting(sprinting);
		cloak.setWalking(walking);
		cloak.setInvisible(slender.isInvisible());
	}

	public static void triggerTendrilAnimation(ServerPlayer slender) {
		UUID cloakId = PLAYER_TO_CLOAK.get(slender.getUUID());
		if (cloakId == null) return;
		ServerLevel level = slender.serverLevel();
		net.minecraft.world.entity.Entity e = level.getEntity(cloakId);
		if (e instanceof SlendermanCloakEntity cloak) {
			cloak.setAttacking(true);
			cloak.triggerAnim("attack", "attack");
		}
	}

	private static void removeCloakById(net.minecraft.server.MinecraftServer server, UUID cloakId) {
		for (ServerLevel level : server.getAllLevels()) {
			net.minecraft.world.entity.Entity e = level.getEntity(cloakId);
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
