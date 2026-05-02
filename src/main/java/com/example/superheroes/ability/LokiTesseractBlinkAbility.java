package com.example.superheroes.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LokiTesseractBlinkAbility implements Ability {
	private static final int COOLDOWN_TICKS = 80;
	private static final double RANGE = 40.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.LOKI_TESSERACT_BLINK;
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
	public boolean canActivate(ServerPlayer player) {
		return !AbilityCooldowns.isOnCooldown(player, getId());
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1f);
		Vec3 end = eye.add(dir.scale(RANGE));

		BlockHitResult bh = level.clip(new ClipContext(eye, end,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 dest;
		if (bh.getType() == HitResult.Type.BLOCK) {
			Vec3 hit = bh.getLocation();
			dest = hit.subtract(dir.scale(0.6));
		} else {
			dest = end;
		}

		level.sendParticles(ParticleTypes.PORTAL,
				player.getX(), player.getY() + 1, player.getZ(), 40, 0.4, 1.0, 0.4, 0.5);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.6f);

		player.teleportTo(dest.x, dest.y - 1.5, dest.z);
		player.connection.resetPosition();

		level.sendParticles(ParticleTypes.PORTAL,
				dest.x, dest.y, dest.z, 60, 0.5, 1.0, 0.5, 0.6);

		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false, true));

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
