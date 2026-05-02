package com.example.superheroes.client;

import com.example.superheroes.entity.SlendermanCloakEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиент-сайд карта {@code ownerUUID -> SlendermanCloakEntity}, обновляется
 * через {@code ClientEntityEvents.ENTITY_LOAD/UNLOAD}. Используется
 * {@code PlayerRendererSlendermanHideMixin}-ом чтобы взять cloak-entity
 * (которая хранит GeckoLib-анимационный кэш и синхронные walking/attack стейты)
 * и отрендерить её прямо на координатах игрока.
 */
public final class SlendermanCloakClientTracker {
	private static final Map<UUID, SlendermanCloakEntity> CLOAKS = new ConcurrentHashMap<>();

	private SlendermanCloakClientTracker() {
	}

	public static void put(UUID owner, SlendermanCloakEntity cloak) {
		if (owner != null && cloak != null) CLOAKS.put(owner, cloak);
	}

	public static void remove(UUID owner) {
		if (owner != null) CLOAKS.remove(owner);
	}

	public static SlendermanCloakEntity get(UUID owner) {
		if (owner == null) return null;
		return CLOAKS.get(owner);
	}

	public static void clearAll() {
		CLOAKS.clear();
	}
}
