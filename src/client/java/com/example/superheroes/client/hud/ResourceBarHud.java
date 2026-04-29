package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.hero.HeroTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ResourceBarHud {
	private static final int X = 12;
	private static final int Y = 12;
	private static final int PANEL_WIDTH = 260;
	private static final int PANEL_HEIGHT_DUAL = 70;
	private static final int PANEL_HEIGHT_SOLO = 48;
	private static final int BAR_WIDTH = 162;
	private static final int BAR_HEIGHT = 10;
	private static final int BAR_X_OFFSET = 38;
	private static final int ICON_SIZE = 18;
	private static final int VALUE_GAP = 6;
	private static final int HERO_BADGE_SIZE = 14;

	private static final int SHADOW = 0x66000000;
	private static final int BAR_BG = 0xFF02030A;
	private static final int BAR_INNER_SHADOW = 0xAA000000;

	private static final int VALUE_COLOR = 0xFFEFEFF7;
	private static final int VALUE_DIM = 0xFF8B8FA0;

	private ResourceBarHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientHeroState.data().hasHero()) {
			return;
		}
		float ramp = ClientHudGlitch.ramp();
		if (ramp > 0.001f) {
			graphics.pose().pushPose();
			graphics.pose().translate(ClientHudGlitch.jitterX(), ClientHudGlitch.jitterY(), 0f);
			renderInner(graphics, tracker);
			graphics.pose().popPose();
			if (ClientHudGlitch.ghostDouble()) {
				int gx = ClientHudGlitch.ghostOffsetX();
				graphics.pose().pushPose();
				graphics.pose().translate(gx, 0f, 0f);
				renderInner(graphics, tracker);
				graphics.pose().popPose();
			}
			return;
		}
		renderInner(graphics, tracker);
	}

	private static void renderInner(GuiGraphics graphics, DeltaTracker tracker) {
		HeroTheme theme = ClientHeroState.theme();
		float energyMax = ClientHeroState.energyMax();
		float manaMax = ClientHeroState.manaMax();
		float energy = ClientHeroState.data().energy();
		float mana = ClientHeroState.data().mana();
		float energyPct = energyMax <= 0f ? 0f : Math.min(1f, energy / energyMax);
		float manaPct = manaMax <= 0f ? 0f : Math.min(1f, mana / manaMax);
		boolean showMana = manaMax > 0f;

		Minecraft mc = Minecraft.getInstance();
		ResourceLocation heroId = ClientHeroState.data().heroId();
		Component heroName = Component.translatable("hero." + heroId.getNamespace() + "." + heroId.getPath());

		int panelHeight = showMana ? PANEL_HEIGHT_DUAL : PANEL_HEIGHT_SOLO;
		HudUtil.dropShadow(graphics, X, Y, PANEL_WIDTH, panelHeight, 3, SHADOW);
		HudUtil.roundedRectGradient(graphics, X, Y, PANEL_WIDTH, panelHeight, theme.panelTop(), theme.panelBottom());
		HudUtil.roundedRectBorder(graphics, X, Y, PANEL_WIDTH, panelHeight, theme.panelBorder());
		graphics.fill(X + 3, Y + 2, X + PANEL_WIDTH - 3, Y + 3, theme.panelHighlight());

		graphics.drawString(mc.font, heroName, X + 12, Y + 6, theme.heroNameColor(), true);
		graphics.fill(X + 12, Y + 18, X + PANEL_WIDTH - 12, Y + 19, (theme.panelBorder() & 0x00FFFFFF) | 0x33000000);

		int row1Y = Y + 26;
		drawIcon(graphics, X + 12, row1Y - 4, theme.energyIcon(), "E");
		drawBar(graphics, mc, X + BAR_X_OFFSET, row1Y, energyPct, theme.energyDark(), theme.energyBright(), theme.energyGlow());
		drawValue(graphics, mc, X + BAR_X_OFFSET + BAR_WIDTH + VALUE_GAP, row1Y - 1, energy, energyMax);

		if (showMana) {
			int row2Y = Y + 48;
			drawIcon(graphics, X + 12, row2Y - 4, theme.manaIcon(), "M");
			drawBar(graphics, mc, X + BAR_X_OFFSET, row2Y, manaPct, theme.manaDark(), theme.manaBright(), theme.manaGlow());
			drawValue(graphics, mc, X + BAR_X_OFFSET + BAR_WIDTH + VALUE_GAP, row2Y - 1, mana, manaMax);
		}

		drawHeroBadge(graphics, theme);
	}

	private static void drawHeroBadge(GuiGraphics g, HeroTheme theme) {
		int bx = X + PANEL_WIDTH - HERO_BADGE_SIZE - 6;
		int by = Y + 3;
		HudUtil.roundedRectFill(g, bx, by, HERO_BADGE_SIZE, HERO_BADGE_SIZE, 0xFF0A0408);
		HudUtil.roundedRectBorder(g, bx, by, HERO_BADGE_SIZE, HERO_BADGE_SIZE, theme.energyIcon());
		int cx = bx + HERO_BADGE_SIZE / 2;
		int cy = by + HERO_BADGE_SIZE / 2;
		g.fill(cx - 3, cy - 4, cx + 3, cy - 3, theme.manaIcon());
		g.fill(cx - 4, cy - 3, cx - 2, cy + 1, theme.manaIcon());
		g.fill(cx + 2, cy - 3, cx + 4, cy + 1, theme.manaIcon());
		g.fill(cx - 1, cy - 1, cx, cy, theme.energyDark());
		g.fill(cx + 1, cy - 1, cx + 2, cy, theme.energyDark());
		g.fill(cx - 3, cy + 2, cx + 3, cy + 3, theme.energyIcon());
		g.fill(cx - 1, cy + 3, cx + 1, cy + 4, theme.energyIcon());
	}

	private static void drawIcon(GuiGraphics g, int x, int y, int color, String letter) {
		HudUtil.roundedRectFill(g, x, y, ICON_SIZE, ICON_SIZE, 0xFF0A0B14);
		HudUtil.roundedRectBorder(g, x, y, ICON_SIZE, ICON_SIZE, color);
		g.drawCenteredString(Minecraft.getInstance().font, Component.literal(letter).withStyle(ChatFormatting.BOLD),
				x + ICON_SIZE / 2, y + (ICON_SIZE - 8) / 2, color);
	}

	private static void drawBar(GuiGraphics g, Minecraft mc, int x, int y, float pct, int dark, int bright, int glow) {
		HudUtil.roundedRectFill(g, x, y, BAR_WIDTH, BAR_HEIGHT, BAR_BG);
		g.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + 2, BAR_INNER_SHADOW);

		int filled = (int) (BAR_WIDTH * pct);
		if (filled >= 4) {
			HudUtil.roundedRectGradient(g, x, y, filled, BAR_HEIGHT, bright, dark);
			g.fill(x + 1, y + 1, x + filled - 1, y + 2, 0x55FFFFFF);
			if (filled - 8 > 0) {
				g.fill(x + filled - 8, y, x + filled, y + BAR_HEIGHT, glow);
			}
		} else if (filled > 0) {
			g.fillGradient(x, y, x + filled, y + BAR_HEIGHT, bright, dark);
		}
		HudUtil.roundedRectBorder(g, x, y, BAR_WIDTH, BAR_HEIGHT, 0x55000000);
	}

	private static void drawValue(GuiGraphics g, Minecraft mc, int x, int y, float value, float max) {
		String cur = String.valueOf((int) value);
		String tot = "/" + (int) max;
		g.drawString(mc.font, cur, x, y, VALUE_COLOR, true);
		g.drawString(mc.font, tot, x + mc.font.width(cur), y, VALUE_DIM, true);
	}
}
