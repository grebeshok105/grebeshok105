package com.example.superheroes.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class LokiAstralClonesAbility implements Ability {
	private static final int COOLDOWN_TICKS = 400;
	private static final int CONFUSE_DURATION = 160;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.LOKI_ASTRAL_CLONES;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 80f;
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

		AABB scan = player.getBoundingBox().inflate(20.0);
		int affected = 0;
		for (Mob mob : level.getEntitiesOfClass(Mob.class, scan,
				e -> e.isAlive() && e.getTarget() == player)) {
			mob.setTarget(null);
			mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION, CONFUSE_DURATION, 0, true, true, true));
			mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, CONFUSE_DURATION, 0, true, true, true));
			affected++;
		}

		for (int i = 0; i < 3; i++) {
			double angle = i * (Math.PI * 2 / 3);
			double rx = Math.cos(angle) * 1.5;
			double rz = Math.sin(angle) * 1.5;
			level.sendParticles(ParticleTypes.SOUL,
					player.getX() + rx, player.getY() + 1, player.getZ() + rz,
					30, 0.4, 0.8, 0.4, 0.05);
		}

		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false, true));

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.4f, 1.0f);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return affected >= 0;
	}
}
