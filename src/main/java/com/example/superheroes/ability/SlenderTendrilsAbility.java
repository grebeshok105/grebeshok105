package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class SlenderTendrilsAbility implements Ability {
	private static final double RANGE = 4.5;
	private static final float DAMAGE = 25.0f;
	private static final int COOLDOWN_TICKS = 160;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.SLENDER_TENDRILS;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 50f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return !AbilityCooldowns.isOnCooldown(player, AbilityIds.SLENDER_TENDRILS);
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 origin = player.position().add(0, player.getEyeHeight() * 0.6, 0);
		Vec3 forward = player.getViewVector(1f).normalize();

		AABB box = new AABB(origin, origin).inflate(RANGE);
		List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != player && e.isAlive() && !e.isSpectator());

		int hits = 0;
		for (LivingEntity target : nearby) {
			Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(origin);
			double dist = toTarget.length();
			if (dist > RANGE || dist < 0.001) continue;
			double dot = toTarget.normalize().dot(forward);
			if (dot < 0.30) continue;

			target.hurt(ModDamageTypes.slendermanTendril(level, player), DAMAGE);
			Vec3 push = forward.scale(0.4);
			target.push(push.x, 0.35, push.z);
			target.hurtMarked = true;

			Vec3 hitAt = target.position().add(0, target.getBbHeight() * 0.5, 0);
			level.sendParticles(ParticleTypes.SQUID_INK, hitAt.x, hitAt.y, hitAt.z, 18, 0.4, 0.4, 0.4, 0.05);
			level.sendParticles(ParticleTypes.SMOKE, hitAt.x, hitAt.y, hitAt.z, 8, 0.3, 0.3, 0.3, 0.02);
			if (target instanceof net.minecraft.server.level.ServerPlayer hitPlayer) {
				net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(hitPlayer,
						new com.example.superheroes.network.SlenderJumpscareS2CPayload(12));
			}
			hits++;
			if (hits >= 4) break;
		}

		for (int i = 0; i < 4; i++) {
			double angle = -0.6 + (i * 0.4);
			Vec3 dir = forward.yRot((float) angle).normalize();
			for (double d = 0.6; d <= RANGE; d += 0.5) {
				Vec3 p = origin.add(dir.scale(d));
				level.sendParticles(ParticleTypes.SQUID_INK, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
			}
		}

		ModSounds.playSlender(level, player.getX(), player.getY(), player.getZ(),
				ModSounds.SLENDERMAN_ATTACK, SoundSource.PLAYERS, 0.9f, 1.0f);

		com.example.superheroes.effect.SlendermanCloakController.triggerTendrilAnimation(player);

		AbilityCooldowns.setCooldownTicks(player, AbilityIds.SLENDER_TENDRILS, COOLDOWN_TICKS);
		return true;
	}
}
