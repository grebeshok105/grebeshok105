package com.example.superheroes.ability;

import com.example.superheroes.network.LokiMindManipulationS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LokiMindManipulationAbility implements Ability {
	private static final int COOLDOWN_TICKS = 600;
	private static final double RANGE = 24.0;
	private static final int DURATION_MS = 10_000;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.LOKI_MIND_MANIPULATION;
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

		Player targetPlayer = null;
		double closest = Double.MAX_VALUE;
		AABB scan = new AABB(eye, end).inflate(2.0);
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, scan,
				e -> e instanceof Player p && e.isAlive() && !p.getUUID().equals(player.getUUID())
						&& !p.isCreative() && !p.isSpectator())) {
			Vec3 toEntity = le.position().add(0, le.getBbHeight() / 2, 0).subtract(eye);
			double len = toEntity.length();
			if (len < 0.001 || len > RANGE) continue;
			double dot = toEntity.scale(1.0 / len).dot(dir);
			if (dot < 0.55) continue;
			if (len < closest) {
				closest = len;
				targetPlayer = (Player) le;
			}
		}

		int steps = 16;
		for (int i = 0; i < steps; i++) {
			double t = (double) i / steps;
			Vec3 p = eye.add(dir.scale(t * RANGE));
			level.sendParticles(ParticleTypes.WITCH, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
			level.sendParticles(ParticleTypes.PORTAL, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.4f, 0.7f);

		if (targetPlayer instanceof ServerPlayer victim) {
			ServerPlayNetworking.send(victim, new LokiMindManipulationS2CPayload(DURATION_MS));
			level.sendParticles(ParticleTypes.WITCH,
					victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
					80, 0.6, 1.0, 0.6, 0.08);
			level.sendParticles(ParticleTypes.PORTAL,
					victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
					60, 0.6, 1.0, 0.6, 0.08);
			level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
					SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.6f, 0.4f);
			level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
					SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.2f, 0.6f);
		}

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
