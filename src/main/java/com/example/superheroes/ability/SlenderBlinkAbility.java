package com.example.superheroes.ability;

import com.example.superheroes.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SlenderBlinkAbility implements Ability {
	private static final double RANGE = 8.0;
	private static final int COOLDOWN_TICKS = 80;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.SLENDER_BLINK;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 30f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return !AbilityCooldowns.isOnCooldown(player, AbilityIds.SLENDER_BLINK);
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 start = player.position();
		Vec3 dir = player.getViewVector(1f).normalize();
		Vec3 desired = start.add(dir.scale(RANGE));

		Vec3 target = findSafeLanding(level, desired, start);
		if (target == null) {
			return false;
		}

		level.sendParticles(ParticleTypes.PORTAL, start.x, start.y + 1.0, start.z, 32, 0.4, 0.8, 0.4, 0.5);
		level.sendParticles(ParticleTypes.SMOKE, start.x, start.y + 1.0, start.z, 16, 0.3, 0.5, 0.3, 0.02);

		player.teleportTo(target.x, target.y, target.z);
		player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
		player.fallDistance = 0f;

		level.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0, target.z, 32, 0.4, 0.8, 0.4, 0.5);
		level.sendParticles(ParticleTypes.SMOKE, target.x, target.y + 1.0, target.z, 16, 0.3, 0.5, 0.3, 0.02);

		level.playSound(null, target.x, target.y, target.z,
				ModSounds.SLENDERMAN_ACTIVE, SoundSource.PLAYERS, 0.7f, 1.0f);
		level.playSound(null, start.x, start.y, start.z,
				net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.2f);

		AbilityCooldowns.setCooldownTicks(player, AbilityIds.SLENDER_BLINK, COOLDOWN_TICKS);
		return true;
	}

	private Vec3 findSafeLanding(ServerLevel level, Vec3 desired, Vec3 fallback) {
		for (int dy = 0; dy <= 4; dy++) {
			for (int sign : new int[]{0, 1, -1}) {
				int yOffset = sign == 0 ? 0 : (sign > 0 ? dy : -dy);
				BlockPos pos = BlockPos.containing(desired.x, desired.y + yOffset, desired.z);
				if (isSafe(level, pos)) {
					return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				}
			}
		}
		BlockPos pos = BlockPos.containing(fallback);
		if (isSafe(level, pos)) {
			return fallback;
		}
		return null;
	}

	private boolean isSafe(ServerLevel level, BlockPos pos) {
		BlockState floor = level.getBlockState(pos.below());
		BlockState feet = level.getBlockState(pos);
		BlockState head = level.getBlockState(pos.above());
		return !floor.isAir()
				&& feet.getCollisionShape(level, pos).isEmpty()
				&& head.getCollisionShape(level, pos.above()).isEmpty()
				&& !feet.liquid() && !head.liquid();
	}
}
