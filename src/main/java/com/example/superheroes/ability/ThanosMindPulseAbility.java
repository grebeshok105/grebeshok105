package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ThanosMindPulseAbility implements Ability {
	private static final int COOLDOWN_TICKS = 100;
	private static final double RANGE = 32.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.THANOS_MIND_PULSE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 80f;
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

		AABB scan = new AABB(eye, end).inflate(1.4);
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, scan,
				e -> e != player && e.isAlive() && !(e instanceof Player p && p.getUUID().equals(player.getUUID())))) {
			Vec3 toEntity = le.position().add(0, le.getBbHeight() / 2, 0).subtract(eye);
			double dot = toEntity.normalize().dot(dir);
			if (dot < 0.85) continue;
			double dist = toEntity.length();
			if (dist > RANGE) continue;

			le.hurt(ModDamageTypes.thanosMindPulse(level, player), 18.0f);
			le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, true, true, true));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1, true, true, true));
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2, true, true, true));
			level.sendParticles(ParticleTypes.ENCHANT,
					le.getX(), le.getY() + le.getBbHeight() / 2, le.getZ(), 30, 0.5, 0.5, 0.5, 0.4);
		}

		int steps = (int) Math.max(12, RANGE * 2);
		for (int i = 0; i < steps; i++) {
			double t = (double) i / steps;
			Vec3 p = eye.add(dir.scale(t * RANGE));
			level.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.1);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.4f, 1.5f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
