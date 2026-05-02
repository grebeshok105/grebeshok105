package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ThanosCosmicSlamAbility implements Ability {
	private static final int COOLDOWN_TICKS = 100;
	private static final double RADIUS = 14.0;
	private static final float CENTER_DAMAGE = 40.0f;
	private static final float EDGE_DAMAGE = 16.0f;

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
		Vec3 center = player.position();

		AABB aoe = new AABB(
				center.x - RADIUS, center.y - 4, center.z - RADIUS,
				center.x + RADIUS, center.y + 6, center.z + RADIUS);
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aoe,
				e -> e != player && e.isAlive() && !(e instanceof Player p && p.getUUID().equals(player.getUUID())))) {
			double dist = le.position().distanceTo(center);
			if (dist > RADIUS) continue;
			float falloff = (float) Math.max(0.0, 1.0 - dist / RADIUS);
			float damage = EDGE_DAMAGE + (CENTER_DAMAGE - EDGE_DAMAGE) * falloff;
			le.hurt(ModDamageTypes.thanosCosmicSlam(level, player), damage);
			Vec3 push = le.position().subtract(center);
			double horiz = Math.max(0.01, Math.sqrt(push.x * push.x + push.z * push.z));
			le.setDeltaMovement(push.x / horiz * 2.5, 1.4, push.z / horiz * 2.5);
			le.hurtMarked = true;
		}

		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.2, center.z, 12, 2.0, 0.4, 2.0, 0.0);
		level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z, 4, 0.5, 0.3, 0.5, 0.0);
		level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 1.0, center.z, 1, 0.0, 0.0, 0.0, 0.0);

		BlockParticleOption stone = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState());
		BlockParticleOption dust = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState());
		for (int ring = 0; ring < 3; ring++) {
			double r = (ring + 1) * (RADIUS / 3.0);
			int count = 24 + ring * 12;
			for (int i = 0; i < count; i++) {
				double angle = (Math.PI * 2 * i) / count;
				double px = center.x + Math.cos(angle) * r;
				double pz = center.z + Math.sin(angle) * r;
				level.sendParticles(stone, px, center.y + 0.3, pz, 3, 0.2, 0.4, 0.2, 0.4);
				level.sendParticles(dust, px, center.y + 0.6, pz, 2, 0.3, 0.4, 0.3, 0.0);
			}
		}

		level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.5, center.z, 220, RADIUS * 0.6, 1.2, RADIUS * 0.6, 0.05);
		level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0, center.z, 300, RADIUS * 0.6, 1.5, RADIUS * 0.6, 0.5);

		level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.5f, 0.4f);
		level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.7f);
		level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4f, 0.6f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
