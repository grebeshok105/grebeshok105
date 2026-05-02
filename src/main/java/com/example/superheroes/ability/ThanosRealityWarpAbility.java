package com.example.superheroes.ability;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ThreadLocalRandom;

public final class ThanosRealityWarpAbility implements Ability {
	private static final double RADIUS = 12.0;

	@SuppressWarnings("unchecked")
	private static final Holder<MobEffect>[] EFFECT_POOL = new Holder[]{
			MobEffects.LEVITATION,
			MobEffects.CONFUSION,
			MobEffects.GLOWING,
			MobEffects.WEAKNESS,
			MobEffects.BLINDNESS,
			MobEffects.SLOW_FALLING,
			MobEffects.MOVEMENT_SLOWDOWN
	};

	@Override
	public ResourceLocation getId() {
		return AbilityIds.THANOS_REALITY_WARP;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 30f;
	}

	@Override
	public float costPerTick() {
		return 1.0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 0.6f);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		if (player.tickCount % 4 == 0) {
			level.sendParticles(ParticleTypes.PORTAL,
					player.getX(), player.getY() + 1.0, player.getZ(),
					12, 0.6, 1.0, 0.6, 0.05);
			level.sendParticles(ParticleTypes.WITCH,
					player.getX(), player.getY() + 1.4, player.getZ(),
					4, 0.4, 0.6, 0.4, 0.0);
		}
		if (player.tickCount % 20 == 0) {
			AABB aoe = player.getBoundingBox().inflate(RADIUS, 4, RADIUS);
			for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aoe,
					e -> e != player && e.isAlive() && !(e instanceof Player p && p.getUUID().equals(player.getUUID())))) {
				Holder<MobEffect> picked = EFFECT_POOL[ThreadLocalRandom.current().nextInt(EFFECT_POOL.length)];
				int amp = picked == MobEffects.WEAKNESS ? 1 : 0;
				le.addEffect(new MobEffectInstance(picked, 100, amp, true, true, true));
				level.sendParticles(ParticleTypes.WITCH,
						le.getX(), le.getY() + le.getBbHeight() / 2, le.getZ(),
						6, 0.3, 0.3, 0.3, 0.02);
			}
		}
	}
}
