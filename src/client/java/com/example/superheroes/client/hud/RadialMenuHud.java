package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.ClientMadnessState;
import com.example.superheroes.client.ModKeys;
import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.hero.HeroTheme;
import com.example.superheroes.network.ActivateAbilityC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class RadialMenuHud {
	private static final float DEAD_ZONE = 5f;
	private static final int ITEM_RADIUS = 110;
	private static final int BACKPLATE_RADIUS = 104;
	private static final int SLOT_MIN_WIDTH = 96;
	private static final int SLOT_HEIGHT = 26;
	private static final int SLOT_PADDING_X = 14;
	private static final int CURSOR_RADIUS = 56;

	private static final int COLOR_TEXT_IDLE = 0xFFE3E5F0;
	private static final int COLOR_KEY_IDLE = 0xFF7C8499;
	private static final int COLOR_SHADOW = 0x77000000;

	private static boolean open;
	private static float startYaw;
	private static float startPitch;
	private static int selected = -1;

	private RadialMenuHud() {
	}

	private static List<ResourceLocation> visibleAbilities() {
		List<ResourceLocation> all = ClientHeroState.abilities();
		boolean madness = ClientMadnessState.isMadness();
		List<ResourceLocation> out = new ArrayList<>(all.size());
		for (ResourceLocation id : all) {
			if (id.equals(AbilityIds.COUNTER_STRIKE) && !madness) continue;
			out.add(id);
		}
		return out;
	}

	public static void clientTick(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			closeWithoutActivate();
			return;
		}
		boolean down = ModKeys.RADIAL != null && ModKeys.RADIAL.isDown();
		List<ResourceLocation> abilities = visibleAbilities();
		if (down && !open) {
			if (abilities.isEmpty()) {
				return;
			}
			open = true;
			startYaw = mc.player.getYRot();
			startPitch = mc.player.getXRot();
			selected = -1;
			return;
		}
		if (!down && open) {
			closeAndActivate(abilities);
			return;
		}
		if (open) {
			updateSelection(mc, abilities.size());
		}
	}

	private static void updateSelection(Minecraft mc, int n) {
		if (n <= 0) {
			selected = -1;
			return;
		}
		float dyaw = mc.player.getYRot() - startYaw;
		float dpitch = mc.player.getXRot() - startPitch;
		float magSq = dyaw * dyaw + dpitch * dpitch;
		if (magSq < DEAD_ZONE * DEAD_ZONE) {
			selected = -1;
			return;
		}
		float per = 360f / n;
		double angle = Math.toDegrees(Math.atan2(dpitch, dyaw)) + 90.0 + per / 2.0;
		angle = ((angle % 360.0) + 360.0) % 360.0;
		selected = ((int) Math.floor(angle / per)) % n;
	}

	private static void closeAndActivate(List<ResourceLocation> abilities) {
		int idx = selected;
		open = false;
		selected = -1;
		if (idx >= 0 && idx < abilities.size()) {
			ClientPlayNetworking.send(new ActivateAbilityC2SPayload(abilities.get(idx)));
		}
	}

	private static void closeWithoutActivate() {
		open = false;
		selected = -1;
	}

	public static boolean isOpen() {
		return open;
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!open) {
			return;
		}
		List<ResourceLocation> abilities = visibleAbilities();
		if (abilities.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;
		int n = abilities.size();
		HeroTheme theme = ClientHeroState.theme();
		drawBackplate(graphics, cx, cy);
		drawCursor(graphics, mc, cx, cy, theme);
		drawHub(graphics, cx, cy, theme);
		for (int i = 0; i < n; i++) {
			double angle = (i * 2 * Math.PI / n) - Math.PI / 2;
			int x = cx + (int) (Math.cos(angle) * ITEM_RADIUS);
			int y = cy + (int) (Math.sin(angle) * ITEM_RADIUS);
			ResourceLocation aid = abilities.get(i);
			Component name = Component.translatable("ability." + aid.getNamespace() + "." + aid.getPath());
			Component key = keyForSlot(i);
			boolean active = i == selected;
			int textWidth = mc.font.width(name);
			int slotWidth = Math.max(SLOT_MIN_WIDTH, textWidth + SLOT_PADDING_X * 2);
			int slotX = x - slotWidth / 2;
			int slotY = y - SLOT_HEIGHT / 2;
			drawSlot(graphics, slotX, slotY, slotWidth, SLOT_HEIGHT, active, theme);
			graphics.drawCenteredString(mc.font, name, x, y - 9, active ? theme.radialTextActive() : COLOR_TEXT_IDLE);
			graphics.drawCenteredString(mc.font, key, x, y + 3, active ? theme.radialKeyActive() : COLOR_KEY_IDLE);
		}
	}

	private static void drawHub(GuiGraphics graphics, int cx, int cy, HeroTheme theme) {
		HudUtil.roundedRectFill(graphics, cx - 24, cy - 24, 48, 48, 0xCC080A14);
		HudUtil.roundedRectBorder(graphics, cx - 24, cy - 24, 48, 48, theme.radialBorderIdle());
		graphics.fill(cx - 18, cy - 22, cx + 18, cy - 21, 0x44FFFFFF);
	}

	private static void drawBackplate(GuiGraphics graphics, int cx, int cy) {
		int outer = BACKPLATE_RADIUS + 36;
		graphics.fillGradient(cx - outer, cy - outer, cx + outer, cy + outer, 0x44060814, 0x00040614);
		graphics.fillGradient(cx - BACKPLATE_RADIUS, cy - BACKPLATE_RADIUS,
				cx + BACKPLATE_RADIUS, cy + BACKPLATE_RADIUS, 0x88101422, 0x44050710);
	}

	private static void drawCursor(GuiGraphics graphics, Minecraft mc, int cx, int cy, HeroTheme theme) {
		if (mc.player == null) {
			return;
		}
		float dyaw = mc.player.getYRot() - startYaw;
		float dpitch = mc.player.getXRot() - startPitch;
		float magSq = dyaw * dyaw + dpitch * dpitch;
		boolean inDeadZone = magSq < DEAD_ZONE * DEAD_ZONE;
		if (inDeadZone) {
			return;
		}
		double a = Math.atan2(dpitch, dyaw);
		double r = CURSOR_RADIUS;
		int px = cx + (int) Math.round(r * Math.cos(a));
		int py = cy + (int) Math.round(r * Math.sin(a));
		drawSmoothLine(graphics, cx, cy, px, py, theme.radialGlow());
		graphics.fill(px - 4, py - 4, px + 5, py + 5, COLOR_SHADOW);
		graphics.fill(px - 3, py - 3, px + 4, py + 4, theme.radialBorderActive());
		graphics.fill(px - 2, py - 2, px + 3, py + 3, theme.radialTextActive());
	}

	private static void drawSmoothLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
		int dx = x1 - x0;
		int dy = y1 - y0;
		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		if (steps == 0) {
			return;
		}
		float fx = (float) dx / steps;
		float fy = (float) dy / steps;
		for (int i = 24; i < steps - 4; i += 1) {
			int px = x0 + Math.round(fx * i);
			int py = y0 + Math.round(fy * i);
			graphics.fill(px, py, px + 1, py + 1, color);
		}
	}

	private static void drawSlot(GuiGraphics graphics, int x, int y, int width, int height, boolean selectedSlot, HeroTheme theme) {
		HudUtil.dropShadow(graphics, x, y, width, height, 2, COLOR_SHADOW);
		if (selectedSlot) {
			HudUtil.roundedRectFill(graphics, x - 3, y - 3, width + 6, height + 6, theme.radialGlow());
		}
		int top = selectedSlot ? 0xF02A1A14 : theme.panelTop();
		int bottom = selectedSlot ? 0xE01A0F0A : theme.panelBottom();
		int border = selectedSlot ? theme.radialBorderActive() : theme.radialBorderIdle();
		HudUtil.roundedRectGradient(graphics, x, y, width, height, top, bottom);
		HudUtil.roundedRectBorder(graphics, x, y, width, height, border);
		graphics.fill(x + 3, y + 1, x + width - 3, y + 2, 0x33FFFFFF);
	}

	private static Component keyForSlot(int index) {
		if (ModKeys.ABILITY_SLOTS != null && index < ModKeys.ABILITY_SLOTS.length) {
			return ModKeys.ABILITY_SLOTS[index].getTranslatedKeyMessage();
		}
		return Component.literal(String.valueOf(index + 1));
	}
}
