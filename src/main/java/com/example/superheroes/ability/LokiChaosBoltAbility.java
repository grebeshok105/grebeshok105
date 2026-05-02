package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
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
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public final class LokiChaosBoltAbility implements Ability {
	private static final int COOLDOWN_TICKS = 160;
	private static final double RANGE = 24.0;

	@SuppressWarnings("unchecked")
	private static final Holder<MobEffect>[] EFFECT_POOL = new Holder[]{
			MobEffects.LEVITATION,
			MobEffects.CONFUSION,
			MobEffects.GLOWING,
			MobEffects.WITHER
	};

	@Override
	public ResourceLocation getId() {
		return AbilityIds.LOKI_CHAOS_BOLT;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 100f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return !AbilityCooldowns.isOnCooldown(player, getId());
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1f);
		Vec3 end = eye.add(dir.scale(RANGE));

		LivingEntity target = null;
		double closest = Double.MAX_VALUE;
		AABB scan = new AABB(eye, end).inflate(1.0);
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, scan,
				e -> e != player && e.isAlive() && !(e instanceof Player p && p.getUUID().equals(player.getUUID())))) {
			Vec3 toEntity = le.position().add(0, le.getBbHeight() / 2, 0).subtract(eye);
			double dot = toEntity.normalize().dot(dir);
			if (dot < 0.85) continue;
			double dist = toEntity.length();
			if (dist > RANGE) continue;
			if (dist < closest) {
				closest = dist;
				target = le;
			}
		}

		double traveled = target != null ? eye.distanceTo(target.position()) : RANGE;
		int steps = (int) Math.max(8, traveled * 2);
		for (int i = 0; i < steps; i++) {
			double t = (double) i / steps;
			Vec3 p = eye.add(dir.scale(t * traveled));
			level.sendParticles(ParticleTypes.WITCH, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.0);
			level.sendParticles(ParticleTypes.SOUL, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
		}

		if (target != null) {
			target.hurt(ModDamageTypes.lokiChaos(level, player), 12.0f);
			Holder<MobEffect> picked = EFFECT_POOL[ThreadLocalRandom.current().nextInt(EFFECT_POOL.length)];
			int amp = picked == MobEffects.WITHER ? 1 : (picked == MobEffects.LEVITATION ? 2 : 0);
			target.addEffect(new MobEffectInstance(picked, 100, amp, true, true, true));
			level.sendParticles(ParticleTypes.WITCH,
					target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 40, 0.5, 0.5, 0.5, 0.05);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.4f, 1.2f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
