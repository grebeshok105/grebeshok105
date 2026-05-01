package com.example.superheroes.ability;

import com.example.superheroes.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class SlenderPhaseStalkAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return AbilityIds.SLENDER_PHASE_STALK;
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
		return 0.6f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false, false));
		ServerLevel level = player.serverLevel();
		ModSounds.playSlender(level, player.getX(), player.getY(), player.getZ(),
				ModSounds.SLENDERMAN_LIVING, SoundSource.PLAYERS, 0.4f, 1.0f);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		if (!player.hasEffect(MobEffects.INVISIBILITY) || player.getEffect(MobEffects.INVISIBILITY).getDuration() < 30) {
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false, false));
		}
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.SMOKE,
				player.getX(), player.getY() + 0.05, player.getZ(),
				3, 0.25, 0.02, 0.25, 0.0);
		level.sendParticles(ParticleTypes.SQUID_INK,
				player.getX(), player.getY() + 0.05, player.getZ(),
				2, 0.2, 0.02, 0.2, 0.0);
		if (player.tickCount % 4 == 0) {
			level.sendParticles(ParticleTypes.LARGE_SMOKE,
					player.getX(), player.getY() + 0.05, player.getZ(),
					1, 0.15, 0.0, 0.15, 0.0);
		}
		double speedSq = player.getDeltaMovement().horizontalDistanceSqr();
		if (speedSq > 0.005 && player.tickCount % 80 == 0) {
			ModSounds.playSlender(level, player.getX(), player.getY(), player.getZ(),
					ModSounds.SLENDERMAN_BUSH, SoundSource.PLAYERS, 0.4f, 1.0f);
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		player.removeEffect(MobEffects.INVISIBILITY);
	}
}
