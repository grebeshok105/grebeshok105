package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientSlenderState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class SlenderFieldHud {
	private SlenderFieldHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		int now = mc.player.tickCount;
		if (!ClientSlenderState.fieldActive(now)) return;

		int sw = graphics.guiWidth();
		int sh = graphics.guiHeight();
		long t = System.currentTimeMillis();
		float pulse = 0.55f + 0.20f * (float) Math.sin(t / 220.0);

		RenderSystem.enableBlend();

		int bands = 28;
		for (int i = 0; i < bands; i++) {
			float k = (float) i / (float) bands;
			float falloff = (1f - k);
			int alpha = (int) (falloff * falloff * 255f * pulse);
			if (alpha <= 1) continue;
			int color = (alpha << 24) | 0x000000;
			graphics.fill(i, i, sw - i, i + 1, color);
			graphics.fill(i, sh - i - 1, sw - i, sh - i, color);
			graphics.fill(i, i, i + 1, sh - i, color);
			graphics.fill(sw - i - 1, i, sw - i, sh - i, color);
		}

		int veil = (int) (40 * pulse);
		graphics.fill(0, 0, sw, sh, (veil << 24) | 0x000000);

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
	}
}
