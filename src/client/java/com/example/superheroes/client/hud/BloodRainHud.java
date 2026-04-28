package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientMadnessState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BloodRainHud {
	private static final int INITIAL_BURST = 15;
	private static final long DARKEN_FADE_MS = 3500L;

	private static final long SPAWN_INTERVAL_MIN_MS = 150L;
	private static final long SPAWN_INTERVAL_MAX_MS = 450L;
	private static final int SPAWN_BATCH_MIN = 1;
	private static final int SPAWN_BATCH_MAX = 3;

	private static volatile long startedMs = 0L;
	private static volatile boolean active = false;
	private static volatile long nextSpawnMs = 0L;
	private static final List<Drop> DROPS = new CopyOnWriteArrayList<>();
	private static final Random RNG = new Random();

	private BloodRainHud() {
	}

	public static void trigger() {
		startedMs = System.currentTimeMillis();
		active = true;
		nextSpawnMs = 0L;
		DROPS.clear();
		List<Drop> initial = new ArrayList<>(INITIAL_BURST);
		for (int i = 0; i < INITIAL_BURST; i++) {
			initial.add(spawnDrop(true));
		}
		DROPS.addAll(initial);
	}

	public static void clear() {
		active = false;
		startedMs = 0L;
		DROPS.clear();
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		boolean madness = ClientMadnessState.isMadness();
		if (!madness && !active) return;
		if (!madness) {
			active = false;
			DROPS.clear();
			return;
		}
		if (!active) {
			trigger();
		}
		long now = System.currentTimeMillis();
		int sw = graphics.guiWidth();
		int sh = graphics.guiHeight();

		long elapsed = startedMs == 0L ? 0L : now - startedMs;
		float darkenAlpha = 0f;
		if (elapsed < DARKEN_FADE_MS) {
			darkenAlpha = 0.45f * (1f - elapsed / (float) DARKEN_FADE_MS);
		}
		if (darkenAlpha > 0f) {
			int a = (int) (darkenAlpha * 255f);
			int topColor = (a << 24) | 0x00000000;
			int midColor = ((a * 2 / 3) << 24) | 0x00000000;
			int bottomColor = (a << 24) | 0x00000000;
			graphics.fillGradient(0, 0, sw, sh / 2, topColor, midColor);
			graphics.fillGradient(0, sh / 2, sw, sh, midColor, bottomColor);
		}

		if (nextSpawnMs == 0L) {
			nextSpawnMs = now + SPAWN_INTERVAL_MIN_MS
					+ RNG.nextInt((int) (SPAWN_INTERVAL_MAX_MS - SPAWN_INTERVAL_MIN_MS));
		}
		while (now >= nextSpawnMs) {
			int batch = SPAWN_BATCH_MIN + RNG.nextInt(SPAWN_BATCH_MAX - SPAWN_BATCH_MIN + 1);
			for (int i = 0; i < batch; i++) {
				DROPS.add(spawnDrop(false));
			}
			nextSpawnMs += SPAWN_INTERVAL_MIN_MS
					+ RNG.nextInt((int) (SPAWN_INTERVAL_MAX_MS - SPAWN_INTERVAL_MIN_MS));
		}

		Iterator<Drop> it = DROPS.iterator();
		List<Drop> toRemove = null;
		while (it.hasNext()) {
			Drop d = it.next();
			float age = (now - d.spawnedMs) / 1000f;
			float progress = age * d.speedPerSec;
			float y = d.yStart + progress;
			if (y > 1.15f) {
				if (toRemove == null) toRemove = new ArrayList<>();
				toRemove.add(d);
				continue;
			}
			int xPx = (int) (d.xNorm * sw);
			int yPx = (int) (y * sh);
			int alpha = Math.min(220, d.baseAlpha);
			if (alpha < 6) continue;
			int color = (alpha << 24) | 0x00A80000;
			int trailColor = ((alpha / 3) << 24) | 0x00700000;
			int w = d.width;
			int h = d.length;
			graphics.fill(xPx - w / 2, yPx, xPx + w - w / 2, yPx + h, color);
			int trailH = Math.min(80, h * 3);
			graphics.fill(xPx - 1, yPx - trailH, xPx + 1, yPx, trailColor);
		}
		if (toRemove != null) {
			DROPS.removeAll(toRemove);
		}
	}

	private static Drop spawnDrop(boolean initialBurst) {
		float xNorm = RNG.nextFloat();
		float yStart = initialBurst ? -0.05f - RNG.nextFloat() * 0.25f : -0.08f - RNG.nextFloat() * 0.05f;
		float speed = 0.010f + RNG.nextFloat() * 0.018f;
		int width = 2 + RNG.nextInt(4);
		int length = 18 + RNG.nextInt(28);
		int alpha = 120 + RNG.nextInt(80);
		return new Drop(xNorm, yStart, speed, width, length, alpha, System.currentTimeMillis());
	}

	private record Drop(
			float xNorm,
			float yStart,
			float speedPerSec,
			int width,
			int length,
			int baseAlpha,
			long spawnedMs
	) {
	}
}
