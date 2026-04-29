package com.example.superheroes.entity.ai;

import com.example.superheroes.entity.HomelanderBossEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class HomelanderEyeLaserGoal extends Goal {
	private static final int LOCK_TICKS = 10;
	private static final int BEAM_TICKS = 30;
	private static final int CYCLE = LOCK_TICKS + BEAM_TICKS;
	private static final int CD_AFTER = 60;
	private static final float DAMAGE_PER_TICK = 1.4f;
	private static final double RANGE = 24.0;

	private final HomelanderBossEntity boss;
	private int phaseTick;

	public HomelanderEyeLaserGoal(HomelanderBossEntity boss) {
		this.boss = boss;
		this.setFlags(EnumSet.of(Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = boss.getTarget();
		if (target == null || !target.isAlive()) {
			return false;
		}
		if (boss.getLaserCooldown() > 0) {
			return false;
		}
		return boss.distanceToSqr(target) <= RANGE * RANGE && boss.hasLineOfSight(target);
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity target = boss.getTarget();
		return target != null && target.isAlive() && phaseTick < CYCLE;
	}

	@Override
	public void start() {
		phaseTick = 0;
	}

	@Override
	public void stop() {
		boss.setLaserCooldown(CD_AFTER);
		phaseTick = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = boss.getTarget();
		if (target == null) {
			return;
		}
		boss.getLookControl().setLookAt(target, 60f, 60f);
		ServerLevel world = (ServerLevel) boss.level();
		Vec3 eye = boss.getEyePosition();
		Vec3 to = target.getEyePosition();

		if (phaseTick < LOCK_TICKS) {
			if (phaseTick == 0) {
				world.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
						SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.4f, 1.6f);
			}
			world.sendParticles(ParticleTypes.FLAME,
					eye.x, eye.y, eye.z,
					3, 0.1, 0.1, 0.1, 0.0);
		} else {
			Vec3 dir = to.subtract(eye);
			double len = dir.length();
			if (len > 0.01) {
				Vec3 step = dir.scale(1.0 / len);
				int steps = (int) Math.min(40, len);
				for (int i = 1; i <= steps; i++) {
					Vec3 p = eye.add(step.scale(i));
					world.sendParticles(ParticleTypes.FLAME,
							p.x, p.y, p.z,
							1, 0.05, 0.05, 0.05, 0.0);
				}
			}
			if (phaseTick % 4 == 0) {
				world.playSound(null, target.getX(), target.getY(), target.getZ(),
						SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0f, 1.4f);
			}
			DamageSource ds = boss.damageSources().mobAttack(boss);
			target.hurt(ds, DAMAGE_PER_TICK);
			target.setRemainingFireTicks(40);
		}
		phaseTick++;
	}
}
