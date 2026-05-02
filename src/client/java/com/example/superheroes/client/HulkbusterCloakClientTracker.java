package com.example.superheroes.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиент-сайд список UUID-владельцев Hulkbuster-накидки. Используется
 * {@code PlayerRendererHulkbusterHideMixin} чтобы не рисовать самого
 * игрока пока его накидка-голем активна.
 *
 * <p>Заполняется/чистится самой {@code HulkbusterCloakEntity} в её клиентском
 * tick / при удалении из мира — см. {@code HulkbusterCloakEntity}.
 */
public final class HulkbusterCloakClientTracker {
	private static final Set<UUID> OWNERS = ConcurrentHashMap.newKeySet();

	private HulkbusterCloakClientTracker() {
	}

	public static void add(UUID owner) {
		if (owner != null) OWNERS.add(owner);
	}

	public static void remove(UUID owner) {
		if (owner != null) OWNERS.remove(owner);
	}

	public static boolean isWearing(UUID owner) {
		return owner != null && OWNERS.contains(owner);
	}

	public static void clearAll() {
		OWNERS.clear();
	}
}
