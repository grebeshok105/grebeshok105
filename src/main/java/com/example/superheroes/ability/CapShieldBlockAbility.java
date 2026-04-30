package com.example.superheroes.ability;

import com.example.superheroes.effect.CapShieldBlockController;
import com.example.superheroes.particle.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public final class CapShieldBlockAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return AbilityIds.CAP_SHIELD_BLOCK;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 0f;
	}

	@Override
	public float costPerTick() {
		return 0.2f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		CapShieldBlockController.setBlocking(player, true);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.4f, 0.9f);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		if (!CapShieldBlockController.isBlocking(player)) {
			CapShieldBlockController.setBlocking(player, true);
		}
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 2, true, false, false));

		if (player.tickCount % 4 == 0) {
			ServerLevel level = player.serverLevel();
			Vec3 forward = player.getLookAngle().normalize();
			Vec3 chest = player.position().add(0, 1.2, 0).add(forward.scale(0.6));
			for (int i = 0; i < 3; i++) {
				double a = (Math.random() - 0.5) * Math.PI * 0.8;
				Vec3 dir = rotateY(forward, a);
				level.sendParticles(ModParticles.CAP_SHIELD_BLOCK_GLOW,
						chest.x + dir.x * 0.4,
						chest.y + (Math.random() - 0.3) * 0.6,
						chest.z + dir.z * 0.4,
						1, 0, 0, 0, 0.0);
			}
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		CapShieldBlockController.setBlocking(player, false);
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.5f, 1.5f);
	}

	private static Vec3 rotateY(Vec3 v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec3(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos);
	}
}
