package com.example.superheroes.client.hud;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.client.ClientFlightSpeedState;
import com.example.superheroes.client.ClientHeroState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;

public final class FlightSpeedHud {
	private FlightSpeedHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.screen instanceof ChatScreen) {
			return;
		}
		if (!ClientFlightSpeedState.visible() && !ClientHeroState.data().isActive(AbilityIds.FLIGHT)) {
			return;
		}
		String text = "Flight: " + ClientFlightSpeedState.percent() + "%";
		int x = (graphics.guiWidth() - mc.font.width(text)) / 2;
		int y = graphics.guiHeight() - 78;
		graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
	}
}
