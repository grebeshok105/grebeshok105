package com.example.superheroes.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BloodRainHud {
	private static final int DROP_COUNT = 15;
	private static final long FADE_IN_MS = 250L;
	private static final long HOLD_MS = 7000L;
	private static final long FADE_OUT_MS = 1500L;
	private static final long TOTAL_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS;
	private static final long DARKEN_FADE_MS = 3500L;

	private static volatile long startedMs = 0L;
	private static List<Drop> drops = List.of();

	private BloodRainHud() {
	}

	public static void trigger() {
		startedMs = System.currentTimeMillis();
		Random r = new Random();
		List<Drop> list = new ArrayList<>(DROP_COUNT);
		for (int i = 0; i < DROP_COUNT; i++) {
			list.add(new Drop(
					r.nextFloat(),
					-0.05f - r.nextFloat() * 0.25f,
					0.010f + r.nextFloat() * 0.018f,
					2 + r.nextInt(4),
					18 + r.nextInt(28),
					r.nextLong()
			));
		}
		drops = list;
	}

	public static void clear() {
		startedMs = 0L;
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		long started = startedMs;
		if (started == 0L) return;
		long now = System.currentTimeMillis();
		long elapsed = now - started;
		if (elapsed > TOTAL_MS) {
			startedMs = 0L;
			return;
		}
		int sw = graphics.guiWidth();
		int sh = graphics.guiHeight();

		float darkenAlpha;
		if (elapsed < DARKEN_FADE_MS) {
			darkenAlpha = 0.45f * (1f - elapsed / (float) DARKEN_FADE_MS);
		} else {
			darkenAlpha = 0f;
		}
		if (darkenAlpha > 0f) {
			int a = (int) (darkenAlpha * 255f);
			int color = (a << 24) | 0x00000000;
			graphics.fill(0, 0, sw, sh, color);
		}

		float dropAlphaScale;
		if (elapsed < FADE_IN_MS) {
			dropAlphaScale = elapsed / (float) FADE_IN_MS;
		} else if (elapsed < FADE_IN_MS + HOLD_MS) {
			dropAlphaScale = 1f;
		} else {
			dropAlphaScale = 1f - (elapsed - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS;
		}
		if (dropAlphaScale <= 0f) return;

		for (Drop d : drops) {
			float progress = (elapsed / 1000f) * d.speedPerSec;
			float y = d.yStart + progress;
			if (y < -0.1f) continue;
			if (y > 1.1f) continue;
			int xPx = (int) (d.xNorm * sw);
			int yPx = (int) (y * sh);
			int alpha = (int) (dropAlphaScale * 200f);
			if (alpha < 6) continue;
			int color = (alpha << 24) | 0x00A80000;
			int trailColor = ((alpha / 3) << 24) | 0x00700000;
			int w = d.width;
			int h = d.length;
			graphics.fill(xPx - w / 2, yPx, xPx + w - w / 2, yPx + h, color);
			int trailH = Math.min(80, h * 3);
			graphics.fill(xPx - 1, yPx - trailH, xPx + 1, yPx, trailColor);
		}
	}

	private record Drop(
			float xNorm,
			float yStart,
			float speedPerSec,
			int width,
			int length,
			long seed
	) {
	}
}
