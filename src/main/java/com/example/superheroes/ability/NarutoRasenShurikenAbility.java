package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class NarutoRasenShurikenAbility implements Ability {
	private static final int COOLDOWN_TICKS = 180;
	private static final double RANGE = 24.0;
	private static final double IMPACT_RADIUS = 4.0;
	private static final float CORE_DAMAGE = 20.0f;
	private static final float SPLASH_DAMAGE = 12.0f;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.NARUTO_RASENSHURIKEN;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 120f;
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

		BlockHitResult bh = level.clip(new ClipContext(eye, end,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

		double maxDist = RANGE;
		LivingEntity firstHit = null;
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class,
				new AABB(eye, end).inflate(1.5),
				e -> e != player && e.isAlive() && !e.isSpectator())) {
			Vec3 toE = le.getBoundingBox().getCenter().subtract(eye);
			double along = toE.dot(dir);
			if (along < 0 || along > maxDist) continue;
			double perp = toE.subtract(dir.scale(along)).length();
			if (perp > 1.6) continue;
			maxDist = along;
			firstHit = le;
		}

		Vec3 actualEnd;
		if (firstHit != null) {
			actualEnd = eye.add(dir.scale(maxDist));
		} else if (bh.getType() == HitResult.Type.BLOCK) {
			actualEnd = bh.getLocation();
		} else {
			actualEnd = end;
		}

		spawnTrail(level, eye, dir, eye.distanceTo(actualEnd));

		float sageBoost = NarutoSageModeAbility.damageMultiplier(player);
		float coreDamage = CORE_DAMAGE * sageBoost;
		float splashDamage = SPLASH_DAMAGE * sageBoost;

		if (firstHit != null) {
			firstHit.hurt(ModDamageTypes.narutoRasenShuriken(level, player), coreDamage);
		}

		final LivingEntity excluded = firstHit;
		AABB aoe = new AABB(
				actualEnd.x - IMPACT_RADIUS, actualEnd.y - IMPACT_RADIUS, actualEnd.z - IMPACT_RADIUS,
				actualEnd.x + IMPACT_RADIUS, actualEnd.y + IMPACT_RADIUS, actualEnd.z + IMPACT_RADIUS);
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aoe,
				e -> e != player && e != excluded && e.isAlive() && !e.isSpectator()
						&& !(e instanceof Player p2 && p2.getUUID().equals(player.getUUID())))) {
			le.hurt(ModDamageTypes.narutoRasenShuriken(level, player), splashDamage);
			Vec3 away = le.position().subtract(actualEnd);
			double horiz = Math.max(0.01, Math.sqrt(away.x * away.x + away.z * away.z));
			le.setDeltaMovement(away.x / horiz * 0.6, 0.4, away.z / horiz * 0.6);
			le.hurtMarked = true;
		}

		level.sendParticles(ModParticles.NARUTO_RASENGAN_SWIRL,
				actualEnd.x, actualEnd.y, actualEnd.z, 60, IMPACT_RADIUS * 0.4, IMPACT_RADIUS * 0.4, IMPACT_RADIUS * 0.4, 0.4);
		level.sendParticles(ParticleTypes.SWEEP_ATTACK,
				actualEnd.x, actualEnd.y + 0.5, actualEnd.z, 8, IMPACT_RADIUS * 0.3, 0.2, IMPACT_RADIUS * 0.3, 0.0);
		level.sendParticles(ParticleTypes.CLOUD,
				actualEnd.x, actualEnd.y + 0.4, actualEnd.z, 30, IMPACT_RADIUS * 0.4, 0.3, IMPACT_RADIUS * 0.4, 0.05);

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.4f, 1.4f);
		level.playSound(null, actualEnd.x, actualEnd.y, actualEnd.z,
				SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.5f, 1.0f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}

	private static void spawnTrail(ServerLevel level, Vec3 from, Vec3 dir, double distance) {
		int steps = (int) Math.max(8, distance * 2);
		for (int i = 0; i < steps; i++) {
			double t = (double) i / steps;
			Vec3 p = from.add(dir.scale(t * distance));
			level.sendParticles(ModParticles.NARUTO_RASENGAN_SWIRL,
					p.x, p.y, p.z, 1, 0.12, 0.12, 0.12, 0.0);
			level.sendParticles(ParticleTypes.CLOUD,
					p.x, p.y, p.z, 1, 0.2, 0.2, 0.2, 0.0);
		}
	}
}
