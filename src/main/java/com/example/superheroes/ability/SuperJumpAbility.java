package com.example.superheroes.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class SuperJumpAbility implements Ability {
	private static final double JUMP_VELOCITY = 2.2;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.SUPER_JUMP;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 60f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		Vec3 v = player.getDeltaMovement();
		player.setDeltaMovement(v.x, JUMP_VELOCITY, v.z);
		player.hurtMarked = true;
		player.fallDistance = 0f;
		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4f, 0.6f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.6f);
		level.sendParticles(ParticleTypes.CLOUD,
				player.getX(), player.getY(), player.getZ(),
				40, 0.6, 0.05, 0.6, 0.4);
		level.sendParticles(ParticleTypes.LARGE_SMOKE,
				player.getX(), player.getY(), player.getZ(),
				20, 0.8, 0.05, 0.8, 0.05);
		return true;
	}
}
