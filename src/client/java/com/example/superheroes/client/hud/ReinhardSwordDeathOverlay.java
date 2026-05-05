package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientReinhardSwordKillState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Полноэкранный кровавый оверлей для жертвы меча Рейнхарда.
 * Только красный экран — без текста и заголовков.
 * После S2C(false) ещё какое-то время остаётся на экране и плавно гаснет.
 */
public final class ReinhardSwordDeathOverlay {
	private static final long FADE_IN_MS = 220L;

	private ReinhardSwordDeathOverlay() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientReinhardSwordKillState.shouldRender()) return;
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();

		long now = System.currentTimeMillis();
		float intensity;
		if (ClientReinhardSwordKillState.active()) {
			long elapsed = now - ClientReinhardSwordKillState.activatedAtMs();
			intensity = Math.min(1f, elapsed / (float) FADE_IN_MS);
		} else {
			long elapsedSinceOff = now - ClientReinhardSwordKillState.deactivatedAtMs();
			intensity = 1f - Math.min(1f, elapsedSinceOff / (float) ClientReinhardSwordKillState.LINGER_MS);
		}

		float pulse = 0.85f + 0.15f * (float) Math.sin(now / 180.0);
		int edgeAlpha = (int) Math.min(255f, 230f * intensity * pulse);
		int corner = (edgeAlpha << 24) | 0x6E0000;
		int center = ((int) (60 * intensity) << 24) | 0xA00000;
		graphics.fillGradient(0, 0, w, h, corner, center);
		graphics.fillGradient(0, 0, w, h / 2, corner, 0x00000000);
		graphics.fillGradient(0, h / 2, w, h, 0x00000000, corner);
	}
}
