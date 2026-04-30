package com.example.superheroes.client;

public final class ClientSlenderState {
	private static int staticStacks = 0;
	private static float staticFade = 0f;
	private static boolean fieldInside = false;
	private static int fieldExpiryTick = 0;
	private static int jumpscareExpiryTick = 0;

	private ClientSlenderState() {
	}

	public static void updateStatic(int stacks, float fade) {
		staticStacks = stacks;
		staticFade = fade;
	}

	public static void updateField(boolean inside, int remainingTicks, int currentTick) {
		fieldInside = inside;
		fieldExpiryTick = currentTick + remainingTicks;
	}

	public static void triggerJumpscare(int duration, int currentTick) {
		jumpscareExpiryTick = currentTick + duration;
	}

	public static int staticStacks() {
		return staticStacks;
	}

	public static float staticFade() {
		return staticFade;
	}

	public static boolean fieldActive(int currentTick) {
		return fieldInside && currentTick < fieldExpiryTick;
	}

	public static boolean jumpscareActive(int currentTick) {
		return currentTick < jumpscareExpiryTick;
	}

	public static int jumpscareRemainingTicks(int currentTick) {
		return Math.max(0, jumpscareExpiryTick - currentTick);
	}
}
