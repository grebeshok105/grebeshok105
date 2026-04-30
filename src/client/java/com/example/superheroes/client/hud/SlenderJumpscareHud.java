package com.example.superheroes.client.hud;

import com.example.superheroes.ModId;
import com.example.superheroes.client.ClientSlenderState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class SlenderJumpscareHud {
	private static final ResourceLocation TEX = ModId.of("textures/gui/slenderman/jumpscare.png");

	private SlenderJumpscareHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		int now = mc.player.tickCount;
		if (!ClientSlenderState.jumpscareActive(now)) return;
		int remaining = ClientSlenderState.jumpscareRemainingTicks(now);
		float alpha = Math.min(1f, remaining / 18f);

		int sw = graphics.guiWidth();
		int sh = graphics.guiHeight();
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		graphics.blit(TEX, 0, 0, 0, 0, sw, sh, sw, sh);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
	}
}
