package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.ReinhardState;
import com.example.superheroes.effect.ReinhardWorthyOpponent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ReinhardHeavensStrikeAbility implements Ability {
	private static final double RADIUS = 7.0;
	private static final float DAMAGE_MOB = 12.0f;
	private static final float DAMAGE_WORTHY = 22.0f;
	private static final int COOLDOWN_TICKS = 200;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.REINHARD_HEAVENS_STRIKE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 320f;
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
		AABB box = player.getBoundingBox().inflate(RADIUS);
		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e.isAlive() && e != player && !e.isSpectator()
						&& e.distanceTo(player) <= RADIUS);

		DamageSource src = level.damageSources().playerAttack(player);
		for (LivingEntity target : targets) {
			boolean worthy = ReinhardWorthyOpponent.isWorthy(target);
			float dmg = worthy ? DAMAGE_WORTHY : DAMAGE_MOB;
			target.hurt(src, dmg);
			Vec3 push = target.position().subtract(player.position()).normalize().scale(1.4);
			target.push(push.x, 0.85, push.z);
			target.hurtMarked = true;
		}

		double cx = player.getX();
		double cy = player.getY();
		double cz = player.getZ();
		level.sendParticles(ParticleTypes.FLASH, cx, cy + 1.0, cz, 3, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.EXPLOSION, cx, cy + 0.5, cz, 6, 1.0, 0.3, 1.0, 0.0);
		for (int ring = 0; ring < 36; ring++) {
			double a = (ring / 36.0) * Math.PI * 2;
			double rx = Math.cos(a) * RADIUS;
			double rz = Math.sin(a) * RADIUS;
			level.sendParticles(ParticleTypes.END_ROD,
					cx + rx, cy + 0.2, cz + rz, 2, 0.1, 0.3, 0.1, 0.05);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					cx + rx * 0.6, cy + 0.5, cz + rz * 0.6, 2, 0.1, 0.3, 0.1, 0.02);
		}

		level.playSound(null, cx, cy, cz, SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.PLAYERS, 1.2f, 1.6f);
		level.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_THUNDER,
				SoundSource.PLAYERS, 0.9f, 1.2f);
		level.playSound(null, cx, cy, cz, SoundEvents.NETHERITE_BLOCK_HIT,
				SoundSource.PLAYERS, 1.4f, 0.6f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
