package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientReinhardSwordKillState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Полноэкранный кровавый оверлей для жертвы меча Рейнхарда.
 * Только красный экран — без текста и заголовков.
 */
public final class ReinhardSwordDeathOverlay {
	private static final long FADE_IN_MS = 220L;

	private ReinhardSwordDeathOverlay() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientReinhardSwordKillState.active()) return;
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();

		long elapsed = System.currentTimeMillis() - ClientReinhardSwordKillState.activatedAtMs();
		float fade = Math.min(1f, elapsed / (float) FADE_IN_MS);
		float pulse = 0.85f + 0.15f * (float) Math.sin(elapsed / 180.0);

		int edgeAlpha = (int) (Math.min(255f, 230f * fade * pulse));
		int corner = (edgeAlpha << 24) | 0x6E0000;
		int center = ((int) (60 * fade) << 24) | 0xA00000;
		graphics.fillGradient(0, 0, w, h, corner, center);
		graphics.fillGradient(0, 0, w, h / 2, corner, 0x00000000);
		graphics.fillGradient(0, h / 2, w, h, 0x00000000, corner);
	}
}
