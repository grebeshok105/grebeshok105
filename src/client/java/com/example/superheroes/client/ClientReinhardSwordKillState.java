package com.example.superheroes.client;

public final class ClientReinhardSwordKillState {
	public static final long LINGER_MS = 5000L;

	private static volatile boolean active;
	private static volatile long activatedAtMs;
	private static volatile long deactivatedAtMs;

	private ClientReinhardSwordKillState() {
	}

	public static void update(boolean newActive) {
		if (newActive && !active) {
			activatedAtMs = System.currentTimeMillis();
			deactivatedAtMs = 0L;
		} else if (!newActive && active) {
			deactivatedAtMs = System.currentTimeMillis();
		}
		active = newActive;
	}

	public static boolean active() {
		return active;
	}

	public static boolean shouldRender() {
		if (active) return true;
		return deactivatedAtMs != 0L && System.currentTimeMillis() - deactivatedAtMs < LINGER_MS;
	}

	public static long activatedAtMs() {
		return activatedAtMs;
	}

	public static long deactivatedAtMs() {
		return deactivatedAtMs;
	}
}
