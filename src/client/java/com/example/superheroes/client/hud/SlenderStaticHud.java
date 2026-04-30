package com.example.superheroes.client.hud;

import com.example.superheroes.ModId;
import com.example.superheroes.client.ClientSlenderState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class SlenderStaticHud {
	private static final ResourceLocation FAR = ModId.of("textures/gui/slenderman/static_far.png");
	private static final ResourceLocation MID = ModId.of("textures/gui/slenderman/static_mid.png");
	private static final ResourceLocation CLOSE = ModId.of("textures/gui/slenderman/static_close.png");

	private SlenderStaticHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		int stacks = ClientSlenderState.staticStacks();
		if (stacks < 5) return;

		ResourceLocation tex;
		float alpha;
		if (stacks >= 15) {
			tex = CLOSE;
			alpha = Math.min(1f, (stacks - 14) / 6f);
		} else if (stacks >= 10) {
			tex = MID;
			alpha = Math.min(0.85f, (stacks - 9) / 6f);
		} else {
			tex = FAR;
			alpha = Math.min(0.65f, (stacks - 4) / 6f);
		}

		int sw = graphics.guiWidth();
		int sh = graphics.guiHeight();
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		graphics.blit(tex, 0, 0, 0, 0, sw, sh, sw, sh);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
	}
}
