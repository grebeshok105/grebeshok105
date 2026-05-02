package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
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

public final class ThanosCosmicSlamAbility implements Ability {
	private static final int COOLDOWN_TICKS = 140;
	private static final double RADIUS = 9.0;
	private static final float CENTER_DAMAGE = 35.0f;
	private static final float EDGE_DAMAGE = 12.0f;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.THANOS_COSMIC_SLAM;
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
		Vec3 center = player.position();

		AABB aoe = new AABB(
				center.x - RADIUS, center.y - 3, center.z - RADIUS,
				center.x + RADIUS, center.y + 5, center.z + RADIUS);
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aoe,
				e -> e != player && e.isAlive() && !(e instanceof Player p && p.getUUID().equals(player.getUUID())))) {
			double dist = le.position().distanceTo(center);
			if (dist > RADIUS) continue;
			float falloff = (float) Math.max(0.0, 1.0 - dist / RADIUS);
			float damage = EDGE_DAMAGE + (CENTER_DAMAGE - EDGE_DAMAGE) * falloff;
			le.hurt(ModDamageTypes.thanosCosmicSlam(level, player), damage);
			Vec3 push = le.position().subtract(center).normalize().scale(2.5);
			le.setDeltaMovement(push.x, 0.9, push.z);
			le.hurtMarked = true;
		}

		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5, center.z, 6, 1.5, 0.5, 1.5, 0.0);
		level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 0.5, center.z, 240, RADIUS * 0.7, 1.0, RADIUS * 0.7, 0.4);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.5, center.z, 140, RADIUS * 0.7, 0.7, RADIUS * 0.7, 0.1);

		level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.7f, 0.4f);
		level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5f, 0.7f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
