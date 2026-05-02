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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class KratosLeviathanThrowAbility implements Ability {
	private static final int COOLDOWN_TICKS = 240;
	private static final double RANGE = 32.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.KRATOS_LEVIATHAN_THROW;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 150f;
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

		Vec3 impact;
		if (target != null) {
			impact = target.position().add(0, target.getBbHeight() / 2, 0);
		} else {
			BlockHitResult bh = level.clip(new ClipContext(eye, end,
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
			impact = bh.getType() == HitResult.Type.BLOCK ? bh.getLocation() : end;
		}

		double traveled = eye.distanceTo(impact);
		int steps = (int) Math.max(8, traveled * 2);
		for (int i = 0; i < steps; i++) {
			double t = (double) i / steps;
			Vec3 p = eye.add(dir.scale(t * traveled));
			level.sendParticles(ParticleTypes.SNOWFLAKE,
					p.x, p.y, p.z, 2, 0.15, 0.15, 0.15, 0.02);
		}

		if (target != null) {
			target.hurt(ModDamageTypes.kratosLeviathan(level, player), 30.0f);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3, true, true, true));
			level.sendParticles(ParticleTypes.SNOWFLAKE, impact.x, impact.y, impact.z, 80, 1.0, 1.0, 1.0, 0.1);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.2f, 0.6f);
		level.playSound(null, impact.x, impact.y, impact.z,
				SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
