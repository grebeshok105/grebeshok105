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

public final class KratosChainWhirlAbility implements Ability {
	private static final double RADIUS = 4.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.KRATOS_CHAIN_WHIRL;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 80f;
	}

	@Override
	public float costPerTick() {
		return 1.0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.4f, 0.6f);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 center = player.position().add(0, 1, 0);

		double angle = (player.tickCount % 40) / 40.0 * Math.PI * 2;
		for (int i = 0; i < 12; i++) {
			double a = angle + i * (Math.PI / 6);
			double rx = Math.cos(a) * RADIUS;
			double rz = Math.sin(a) * RADIUS;
			level.sendParticles(ParticleTypes.CRIT,
					center.x + rx, center.y + 0.5, center.z + rz,
					1, 0.05, 0.1, 0.05, 0.0);
		}

		if (player.tickCount % 5 == 0) {
			AABB aoe = new AABB(
					center.x - RADIUS, center.y - 1.5, center.z - RADIUS,
					center.x + RADIUS, center.y + 1.5, center.z + RADIUS);
			for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aoe,
					e -> e != player && e.isAlive() && !(e instanceof Player p && p.getUUID().equals(player.getUUID())))) {
				if (le.position().distanceTo(center) > RADIUS) continue;
				le.hurt(ModDamageTypes.kratosBlade(level, player), 8.0f);
				Vec3 push = le.position().subtract(center).normalize().scale(0.6);
				le.setDeltaMovement(le.getDeltaMovement().add(push.x, 0.1, push.z));
				le.hurtMarked = true;
			}
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.6f, 1.4f);
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
	}
}
