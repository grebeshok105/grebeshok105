package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientMadnessState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public final class MadnessHudOverlay {
	private static final String[] VOICES = new String[]{
			"Желай больше.",
			"Лев не делится.",
			"Никто не достоин.",
			"Всё — моё.",
			"Откажись… или возьми всё.",
			"Жадность — добродетель.",
			"Я — закон.",
			"Корнеас",
			"Голод не утолить."
	};

	private static final String[] FLOATING_SYMBOLS = new String[]{
			"卐", "✟", "Ω", "Жадность", "Корнеас", "罪", "Лев", "Я", "獅"
	};

	private static long lastBeatMs = 0L;
	private static long lastVoiceMs = 0L;
	private static String currentVoice = null;
	private static long currentVoiceUntilMs = 0L;
	private static final Random RNG = new Random();
	private static long readingFlashMs = 0L;

	private MadnessHudOverlay() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		Player p = mc.player;
		if (p == null) return;
		int sw = graphics.guiWidth();
		int sh = graphics.guiHeight();

		if (ClientMadnessState.isReading()) {
			renderReading(graphics, sw, sh);
		}
		if (!ClientMadnessState.isMadness()) {
			lastBeatMs = 0L;
			currentVoice = null;
			return;
		}
		long now = System.currentTimeMillis();
		long elapsed = now - ClientMadnessState.madnessStartedAtMs();
		double phase = Math.min(1.0, elapsed / 60_000.0);
		double interval = 5000.0 - phase * 4500.0;
		long interv = (long) interval;
		if (lastBeatMs == 0L) lastBeatMs = now - interv;
		float beatStrength = 0f;
		if (now - lastBeatMs >= interv) {
			lastBeatMs = now;
			mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(),
					SoundEvents.WARDEN_HEARTBEAT, net.minecraft.sounds.SoundSource.PLAYERS,
					0.6f + (float) phase * 0.6f, 1.0f - (float) phase * 0.2f, false);
			beatStrength = 1f;
		} else {
			float t = (now - lastBeatMs) / (float) interv;
			beatStrength = Math.max(0f, 1f - t * 4f);
		}

		float alphaMax = 0.15f + (float) phase * 0.45f;
		int redA = (int) (beatStrength * alphaMax * 255f);
		if (redA > 4) {
			int color = (Math.min(255, redA) << 24) | 0x00DD0000;
			graphics.fill(0, 0, sw, sh, color);
		}

		if (currentVoice == null || now > currentVoiceUntilMs) {
			if (now - lastVoiceMs > 18_000L && RNG.nextInt(160) == 0) {
				currentVoice = VOICES[RNG.nextInt(VOICES.length)];
				currentVoiceUntilMs = now + 3000L;
				lastVoiceMs = now;
			} else if (now - lastVoiceMs > 60_000L) {
				currentVoice = VOICES[RNG.nextInt(VOICES.length)];
				currentVoiceUntilMs = now + 3000L;
				lastVoiceMs = now;
			}
		}
		if (currentVoice != null && now <= currentVoiceUntilMs) {
			float fade = 1f;
			long left = currentVoiceUntilMs - now;
			if (left < 600L) fade = left / 600f;
			int va = (int) (fade * 200f);
			int color = (Math.max(0, Math.min(255, va)) << 24) | 0x00FFD700;
			Component msg = Component.literal(currentVoice).withStyle(ChatFormatting.ITALIC);
			int w = mc.font.width(msg);
			graphics.drawString(mc.font, msg, sw / 2 - w / 2, sh / 4, color, true);
		}

		long seed = (now / 600L);
		Random r = new Random(seed);
		int symbolCount = 4;
		for (int i = 0; i < symbolCount; i++) {
			String sym = FLOATING_SYMBOLS[r.nextInt(FLOATING_SYMBOLS.length)];
			int x = r.nextInt(Math.max(1, sw - 80)) + 20;
			int y = r.nextInt(Math.max(1, sh - 80)) + 20;
			int alpha = 30 + r.nextInt(50);
			int color = (alpha << 24) | 0x00BB0011;
			graphics.drawString(mc.font, sym, x, y, color, false);
		}

		if (ClientMadnessState.isBonusLifeAvailable()) {
			Component lifeText = Component.literal("◆ ДОП. ЖИЗНЬ").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
			int w = mc.font.width(lifeText);
			graphics.drawString(mc.font, lifeText, sw - w - 8, sh - 32, 0xFFFFD700, true);
		}
		if (ClientMadnessState.isManaLocked()) {
			long left = ClientMadnessState.manaLockUntilMs() - now;
			Component lockText = Component.literal("Мана заблокирована: " + (left / 1000L + 1) + "с").withStyle(ChatFormatting.RED);
			int w = mc.font.width(lockText);
			graphics.drawString(mc.font, lockText, sw - w - 8, sh - 22, 0xFFFF6060, true);
		}
	}

	private static void renderReading(GuiGraphics graphics, int sw, int sh) {
		long now = System.currentTimeMillis();
		long left = ClientMadnessState.readingUntilMs() - now;
		float t = 1.0f - Math.max(0, Math.min(5000, left)) / 5000.0f;
		int gold = (int) (t * 80f);
		int color = (Math.min(255, gold) << 24) | 0x00FFD700;
		graphics.fill(0, 0, sw, sh, color);
		Minecraft mc = Minecraft.getInstance();
		Component msg = Component.literal("Чтение Евангелия…").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
		int w = mc.font.width(msg);
		int sec = (int) (left / 1000L) + 1;
		graphics.drawString(mc.font, msg, sw / 2 - w / 2, sh / 3, 0xFFFFD700, true);
		Component sub = Component.literal(sec + " …").withStyle(ChatFormatting.YELLOW);
		int w2 = mc.font.width(sub);
		graphics.drawString(mc.font, sub, sw / 2 - w2 / 2, sh / 3 + 16, 0xFFFFE680, true);
	}
}
