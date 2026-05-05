package com.example.superheroes.client;

/**
 * Локальное состояние "Манипуляции разумом" Локи на клиенте жертвы.
 * Пока active() — миксины переворачивают камеру/HUD и инвертируют ввод.
 */
public final class ClientLokiMindManipulationState {
	private static volatile long endAtMs;

	private ClientLokiMindManipulationState() {
	}

	public static void start(int durationMs) {
		if (durationMs <= 0) {
			endAtMs = 0L;
			return;
		}
		endAtMs = System.currentTimeMillis() + durationMs;
	}

	public static void clear() {
		endAtMs = 0L;
	}

	public static boolean active() {
		return endAtMs != 0L && System.currentTimeMillis() < endAtMs;
	}
}
