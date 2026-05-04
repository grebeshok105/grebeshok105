package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.ReinhardState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Wish (Загадать желание) — даёт иммунитет к самому свежему типу урона
 * из последних 5 источников. 3 заряда на жизнь, 30s КД между активациями.
 * После 3-х желаний игрок получает +30% входящий урон до конца жизни.
 *
 * Упрощено: вместо radial-меню берём первый из recentDamageTypes (последний источник).
 */
public final class ReinhardWishAbility implements Ability {
	private static final int MAX_WISHES = 3;
	private static final int COOLDOWN_TICKS = 600; // 30s

	@Override
	public ResourceLocation getId() {
		return AbilityIds.REINHARD_WISH;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 0f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		ReinhardState s = player.getAttachedOrCreate(ModAttachments.REINHARD_STATE);
		ServerLevel level = player.serverLevel();
		long now = level.getGameTime();
		if (s.lastWishTick() != 0 && now - s.lastWishTick() < COOLDOWN_TICKS) return false;
		if (s.wishesUsed() >= MAX_WISHES) return false;
		if (s.recentDamageTypes().isEmpty()) return false;
		return true;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ReinhardState s = player.getAttachedOrCreate(ModAttachments.REINHARD_STATE);
		ServerLevel level = player.serverLevel();
		long now = level.getGameTime();
		List<String> recent = s.recentDamageTypes();
		if (recent.isEmpty()) return false;

		String pick = recent.get(0);
		List<String> adapted = new ArrayList<>(s.adaptedDamageTypes());
		if (!adapted.contains(pick)) adapted.add(pick);

		ReinhardState updated = s.withAdaptedDamageTypes(adapted)
				.withWishesUsed(s.wishesUsed() + 1)
				.withLastWishTick(now);
		player.setAttached(ModAttachments.REINHARD_STATE, updated);

		level.sendParticles(ParticleTypes.GLOW,
				player.getX(), player.getY() + 1.0, player.getZ(),
				50, 0.6, 1.0, 0.6, 0.05);
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.0, player.getZ(),
				24, 0.3, 0.6, 0.3, 0.1);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.6f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4f, 1.4f);

		String label = pick.startsWith("minecraft:") ? pick.substring("minecraft:".length()) : pick;
		player.displayClientMessage(
				Component.translatable("ability.superheroes.reinhard_wish.granted", label),
				true);
		return true;
	}
}
