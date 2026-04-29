package com.example.superheroes.client.hud;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.ClientMadnessState;
import com.example.superheroes.hero.HeroTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class AbilitiesTooltipHud {
	private static final int ANCHOR_X = 12;
	private static final int ANCHOR_Y = 12;
	private static final int RESOURCE_PANEL_HEIGHT_DUAL = 70;
	private static final int RESOURCE_PANEL_HEIGHT_SOLO = 48;
	private static final int GAP_BELOW_RESOURCE = 4;
	private static final int PANEL_WIDTH = 260;

	private static final int PADDING_X = 12;
	private static final int PADDING_TOP = 8;
	private static final int PADDING_BOTTOM = 10;
	private static final int SECTION_SPACING = 6;
	private static final int ROW_HEIGHT = 22;
	private static final int ROW_HEIGHT_COMPACT = 14;
	private static final int ICON_SIZE = 16;
	private static final int SECTION_HEADER_HEIGHT = 12;

	private static final int ANIM_TICKS = 10;
	private static final int SLIDE_DISTANCE = 12;

	private static float progress = 0f;
	private static float lastProgress = 0f;
	private static boolean userVisible = true;

	private AbilitiesTooltipHud() {
	}

	public static void toggleVisible() {
		userVisible = !userVisible;
	}

	public static boolean isUserVisible() {
		return userVisible;
	}

	public static void tick() {
		lastProgress = progress;
		boolean visible = userVisible && ClientHeroState.data().hasHero();
		float delta = 1f / ANIM_TICKS;
		if (visible) {
			progress = Math.min(1f, progress + delta);
		} else {
			progress = Math.max(0f, progress - delta);
		}
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if ((!userVisible || !ClientHeroState.data().hasHero()) && progress <= 0f) {
			return;
		}
		float ramp = ClientHudGlitch.ramp();
		if (ramp > 0.001f) {
			graphics.pose().pushPose();
			graphics.pose().translate(ClientHudGlitch.jitterX(), ClientHudGlitch.jitterY(), 0f);
			renderInner(graphics, tracker);
			graphics.pose().popPose();
			if (ClientHudGlitch.ghostDouble()) {
				graphics.pose().pushPose();
				graphics.pose().translate(ClientHudGlitch.ghostOffsetX(), 0f, 0f);
				renderInner(graphics, tracker);
				graphics.pose().popPose();
			}
			return;
		}
		renderInner(graphics, tracker);
	}

	private static void renderInner(GuiGraphics graphics, DeltaTracker tracker) {
		float partial = tracker.getGameTimeDeltaPartialTick(false);
		float p = lastProgress + (progress - lastProgress) * partial;
		if (p <= 0.001f) {
			return;
		}
		float eased = smoothstep(p);

		ResourceLocation heroId = ClientHeroState.data().heroId();
		if (heroId == null) {
			return;
		}
		List<ResourceLocation> abilities = ClientHeroState.abilities();
		int passiveCount = AbilityDescriptions.passiveCount(heroId);

		int togglesCount = 0;
		int activesCount = 0;
		for (ResourceLocation id : abilities) {
			AbilityDescriptions.Kind kind = AbilityDescriptions.kindOf(id);
			if (kind == AbilityDescriptions.Kind.TOGGLE) {
				togglesCount++;
			} else {
				activesCount++;
			}
		}

		int panelHeight = PADDING_TOP;
		if (passiveCount > 0) {
			panelHeight += SECTION_HEADER_HEIGHT + passiveCount * ROW_HEIGHT_COMPACT + SECTION_SPACING;
		}
		if (activesCount > 0) {
			panelHeight += SECTION_HEADER_HEIGHT + activesCount * ROW_HEIGHT + SECTION_SPACING;
		}
		if (togglesCount > 0) {
			panelHeight += SECTION_HEADER_HEIGHT + togglesCount * ROW_HEIGHT + SECTION_SPACING;
		}
		panelHeight = Math.max(panelHeight - SECTION_SPACING, 0) + PADDING_BOTTOM;

		if (panelHeight < 16) {
			return;
		}

		int resourcePanelHeight = ClientHeroState.manaMax() > 0f ? RESOURCE_PANEL_HEIGHT_DUAL : RESOURCE_PANEL_HEIGHT_SOLO;
		int baseY = ANCHOR_Y + resourcePanelHeight + GAP_BELOW_RESOURCE;
		int yOffset = (int) ((1f - eased) * -SLIDE_DISTANCE);
		int x = ANCHOR_X;
		int y = baseY + yOffset;

		int alpha = (int) (eased * 255f);
		if (alpha <= 2) {
			return;
		}

		HeroTheme theme = ClientHeroState.theme();
		drawPanel(graphics, x, y, PANEL_WIDTH, panelHeight, theme, alpha);

		Minecraft mc = Minecraft.getInstance();
		int cursorY = y + PADDING_TOP;

		if (passiveCount > 0) {
			drawSectionHeader(graphics, mc, x + PADDING_X, cursorY, PANEL_WIDTH - PADDING_X * 2,
					Component.translatable("hud.superheroes.abilities.passives"), theme, alpha);
			cursorY += SECTION_HEADER_HEIGHT;
			for (int i = 1; i <= passiveCount; i++) {
				drawPassiveRow(graphics, mc, x + PADDING_X, cursorY,
						Component.translatable(AbilityDescriptions.passiveKey(heroId, i)),
						theme, alpha);
				cursorY += ROW_HEIGHT_COMPACT;
			}
			cursorY += SECTION_SPACING;
		}

		if (activesCount > 0) {
			drawSectionHeader(graphics, mc, x + PADDING_X, cursorY, PANEL_WIDTH - PADDING_X * 2,
					Component.translatable("hud.superheroes.abilities.active"), theme, alpha);
			cursorY += SECTION_HEADER_HEIGHT;
			for (ResourceLocation id : abilities) {
				if (AbilityDescriptions.kindOf(id) != AbilityDescriptions.Kind.ACTIVE) {
					continue;
				}
				drawAbilityRow(graphics, mc, x + PADDING_X, cursorY, PANEL_WIDTH - PADDING_X * 2, id, theme, alpha);
				cursorY += ROW_HEIGHT;
			}
			cursorY += SECTION_SPACING;
		}

		if (togglesCount > 0) {
			drawSectionHeader(graphics, mc, x + PADDING_X, cursorY, PANEL_WIDTH - PADDING_X * 2,
					Component.translatable("hud.superheroes.abilities.toggle"), theme, alpha);
			cursorY += SECTION_HEADER_HEIGHT;
			for (ResourceLocation id : abilities) {
				if (AbilityDescriptions.kindOf(id) != AbilityDescriptions.Kind.TOGGLE) {
					continue;
				}
				drawAbilityRow(graphics, mc, x + PADDING_X, cursorY, PANEL_WIDTH - PADDING_X * 2, id, theme, alpha);
				cursorY += ROW_HEIGHT;
			}
		}
	}

	private static void drawPanel(GuiGraphics g, int x, int y, int w, int h, HeroTheme theme, int alpha) {
		int shadowAlpha = Math.min(0x88, alpha / 2);
		HudUtil.dropShadow(g, x, y, w, h, 3, (shadowAlpha << 24) | 0x000000);

		int top = applyAlpha(theme.panelTop(), alpha, 0.55f);
		int bottom = applyAlpha(theme.panelBottom(), alpha, 0.55f);
		HudUtil.roundedRectGradient(g, x, y, w, h, top, bottom);

		int border = applyAlpha(theme.panelBorder(), alpha, 0.9f);
		HudUtil.roundedRectBorder(g, x, y, w, h, border);

		int hi = applyAlpha(theme.panelHighlight(), alpha, 1.0f);
		g.fill(x + 3, y + 2, x + w - 3, y + 3, hi);
	}

	private static void drawSectionHeader(GuiGraphics g, Minecraft mc, int x, int y, int width, Component label, HeroTheme theme, int alpha) {
		int color = applyAlpha(theme.heroNameColor(), alpha, 1.0f);
		g.drawString(mc.font, Component.empty().append(label).withStyle(ChatFormatting.BOLD), x, y, color, true);
		int line = applyAlpha(theme.panelBorder(), alpha, 0.35f);
		g.fill(x, y + 10, x + width, y + 11, line);
	}

	private static void drawPassiveRow(GuiGraphics g, Minecraft mc, int x, int y, Component name, HeroTheme theme, int alpha) {
		int nameColor = applyAlpha(0xFFE8E9F2, alpha, 1.0f);
		int bulletColor = applyAlpha(theme.energyIcon(), alpha, 1.0f);
		g.drawString(mc.font, Component.literal("▸ ").withStyle(ChatFormatting.BOLD), x, y + 1, bulletColor, true);
		int maxTextWidth = PANEL_WIDTH - PADDING_X * 2 - 10;
		g.drawString(mc.font, ellipsize(mc, name, maxTextWidth), x + 10, y + 1, nameColor, true);
	}

	private static void drawAbilityRow(GuiGraphics g, Minecraft mc, int x, int y, int width, ResourceLocation abilityId, HeroTheme theme, int alpha) {
		AbilityDescriptions.Kind kind = AbilityDescriptions.kindOf(abilityId);
		boolean glitchSecret = AbilityIds.COUNTER_STRIKE.equals(abilityId) && !ClientMadnessState.isMadness();
		int iconBg = applyAlpha(0xFF0A0B14, alpha, 1.0f);
		int iconBorder = applyAlpha(kind == AbilityDescriptions.Kind.TOGGLE ? theme.manaIcon() : theme.energyIcon(), alpha, 1.0f);
		HudUtil.roundedRectFill(g, x, y, ICON_SIZE, ICON_SIZE, iconBg);
		HudUtil.roundedRectBorder(g, x, y, ICON_SIZE, ICON_SIZE, iconBorder);
		Component badge = glitchSecret
				? Component.literal("?").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD)
				: Component.literal(kind.badge()).withStyle(ChatFormatting.BOLD);
		g.drawCenteredString(mc.font, badge, x + ICON_SIZE / 2, y + (ICON_SIZE - 8) / 2, iconBorder);

		int textX = x + ICON_SIZE + 6;
		int maxTextWidth = width - ICON_SIZE - 6;
		int nameColor = applyAlpha(0xFFF4F5FC, alpha, 1.0f);
		Component name = glitchSecret
				? Component.literal("????????").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD)
				: Component.translatable(AbilityDescriptions.nameKey(abilityId)).withStyle(ChatFormatting.BOLD);
		g.drawString(mc.font, ellipsize(mc, name, maxTextWidth), textX, y + 1, nameColor, true);

		int descColor = applyAlpha(0xFFA2A6B8, alpha, 1.0f);
		Component desc = glitchSecret
				? Component.literal("????????????????????").withStyle(ChatFormatting.OBFUSCATED)
				: Component.translatable(AbilityDescriptions.descKey(abilityId));
		g.drawString(mc.font, ellipsize(mc, desc, maxTextWidth), textX, y + 11, descColor, true);
	}

	private static Component ellipsize(Minecraft mc, Component component, int maxWidth) {
		String text = component.getString();
		if (mc.font.width(component) <= maxWidth) {
			return component;
		}
		String trimmed = mc.font.plainSubstrByWidth(text, Math.max(0, maxWidth - mc.font.width("…")));
		net.minecraft.network.chat.Style style = component.getStyle();
		return Component.literal(trimmed + "…").setStyle(style);
	}

	private static int applyAlpha(int argb, int alpha, float mult) {
		int originalA = (argb >>> 24) & 0xFF;
		int finalA = Math.min(255, Math.max(0, (int) (originalA * (alpha / 255f) * mult)));
		return (finalA << 24) | (argb & 0x00FFFFFF);
	}

	private static float smoothstep(float x) {
		float c = Math.max(0f, Math.min(1f, x));
		return c * c * (3f - 2f * c);
	}
}
