package com.example.superheroes.ability;

import com.example.superheroes.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class IronFistsAbility implements Ability {
	public static final float MELEE_DAMAGE = 5.0f;
	public static final double MELEE_KNOCKBACK = 2.5;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.IRON_FISTS;
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
		return 0.5f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 p = player.position();
		level.playSound(null, p.x, p.y, p.z, ModSounds.HOMELANDER_IRON_FISTS_CHARGE,
				SoundSource.PLAYERS, 0.9f, 1.0f);
		spawnHandParticles(player, 24);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		if ((player.tickCount & 3) == 0) {
			spawnHandParticles(player, 4);
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 p = player.position();
		level.sendParticles(ParticleTypes.SMOKE,
				p.x, p.y + 1.0, p.z, 14, 0.4, 0.5, 0.4, 0.02);
	}

	private static void spawnHandParticles(ServerPlayer player, int count) {
		ServerLevel level = player.serverLevel();
		Vec3 forward = player.getViewVector(1f).normalize();
		Vec3 right = new Vec3(-forward.z, 0.0, forward.x).normalize();
		Vec3 base = player.position().add(0.0, 1.1, 0.0).add(forward.scale(0.25));
		Vec3 left = base.add(right.scale(-0.35));
		Vec3 rightHand = base.add(right.scale(0.35));
		level.sendParticles(ParticleTypes.LARGE_SMOKE,
				left.x, left.y, left.z, count, 0.08, 0.08, 0.08, 0.0);
		level.sendParticles(ParticleTypes.LARGE_SMOKE,
				rightHand.x, rightHand.y, rightHand.z, count, 0.08, 0.08, 0.08, 0.0);
		level.sendParticles(ParticleTypes.SMOKE,
				left.x, left.y, left.z, count / 2 + 1, 0.06, 0.06, 0.06, 0.0);
		level.sendParticles(ParticleTypes.SMOKE,
				rightHand.x, rightHand.y, rightHand.z, count / 2 + 1, 0.06, 0.06, 0.06, 0.0);
	}
}
