package com.example.superheroes.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Состояние Райден Сёгун (Архонт Электро).
 *
 * eyeExpireTick — Глаз Грозного Суда активен до этого тика; 0 = не активен.
 * burstExpireTick — окно Musou Shinsetsu активно до этого тика; 0 = не активно.
 * burstFinalSlashTick — на этом тике сработает финальный AoE-слэш Burst-а; 0 = нет.
 */
public record RaidenState(
		long eyeExpireTick,
		long burstExpireTick,
		long burstFinalSlashTick
) {
	public static final RaidenState EMPTY = new RaidenState(0L, 0L, 0L);

	public static final Codec<RaidenState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.optionalFieldOf("eye_expire_tick", 0L).forGetter(RaidenState::eyeExpireTick),
			Codec.LONG.optionalFieldOf("burst_expire_tick", 0L).forGetter(RaidenState::burstExpireTick),
			Codec.LONG.optionalFieldOf("burst_final_slash_tick", 0L).forGetter(RaidenState::burstFinalSlashTick)
	).apply(instance, RaidenState::new));

	public RaidenState withEyeExpireTick(long v) {
		return new RaidenState(v, burstExpireTick, burstFinalSlashTick);
	}

	public RaidenState withBurstExpireTick(long v) {
		return new RaidenState(eyeExpireTick, v, burstFinalSlashTick);
	}

	public RaidenState withBurstFinalSlashTick(long v) {
		return new RaidenState(eyeExpireTick, burstExpireTick, v);
	}
}
