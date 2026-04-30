package com.example.superheroes.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AbilityCooldowns {
	private static final Map<UUID, Map<ResourceLocation, Long>> MAP = new ConcurrentHashMap<>();

	private AbilityCooldowns() {
	}

	public static void setCooldownTicks(ServerPlayer player, ResourceLocation abilityId, int ticks) {
		long deadline = player.tickCount + ticks;
		MAP.computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>())
				.put(abilityId, deadline);
	}

	public static boolean isOnCooldown(ServerPlayer player, ResourceLocation abilityId) {
		Map<ResourceLocation, Long> m = MAP.get(player.getUUID());
		if (m == null) return false;
		Long deadline = m.get(abilityId);
		if (deadline == null) return false;
		if (player.tickCount >= deadline) {
			m.remove(abilityId);
			return false;
		}
		return true;
	}

	public static int remainingTicks(ServerPlayer player, ResourceLocation abilityId) {
		Map<ResourceLocation, Long> m = MAP.get(player.getUUID());
		if (m == null) return 0;
		Long deadline = m.get(abilityId);
		if (deadline == null) return 0;
		long left = deadline - player.tickCount;
		return left > 0 ? (int) left : 0;
	}

	public static void clear(UUID id) {
		MAP.remove(id);
	}
}
