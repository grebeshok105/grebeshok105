package com.example.superheroes.client;

public final class ClientFlightSpeedState {
	private static volatile int percent = 100;
	private static volatile long visibleUntilMs;

	private ClientFlightSpeedState() {
	}

	public static void update(int value) {
		percent = value;
		visibleUntilMs = System.currentTimeMillis() + 2000L;
	}

	public static int percent() {
		return percent;
	}

	public static boolean visible() {
		return System.currentTimeMillis() < visibleUntilMs;
	}
}
