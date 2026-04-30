package com.example.superheroes.mixin;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.SlendermanStaticController;
import com.example.superheroes.hero.SlendermanHero;
import com.example.superheroes.transform.HeroData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(LivingEntity.class)
public abstract class SlenderDodgeMixin {
	private static final Map<UUID, Integer> SLENDER_DODGE_COOLDOWN = new HashMap<>();
	private static final int DODGE_COOLDOWN_TICKS = 20;
	private static final int TELEPORT_RANGE = 8;

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void superheroes$slenderDodge(DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof ServerPlayer player)) {
			return;
		}
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.hasHero() || !SlendermanHero.ID.equals(data.heroId())) {
			return;
		}
		if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)
				|| source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
			return;
		}

		Integer cdEnd = SLENDER_DODGE_COOLDOWN.get(player.getUUID());
		if (cdEnd != null && player.tickCount < cdEnd) {
			return;
		}

		float chance = dodgeChanceFor(source);
		if (SlendermanStaticController.isExposed(player)) {
			chance *= 0.30f;
		}
		if (chance <= 0.0f) {
			return;
		}
		if (ThreadLocalRandom.current().nextFloat() >= chance) {
			return;
		}

		ServerLevel level = player.serverLevel();
		Vec3 from = player.position();
		Vec3 to = pickSafeNearby(level, player, from);
		if (to == null) {
			return;
		}

		level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1, from.z, 32, 0.4, 0.8, 0.4, 0.5);
		level.sendParticles(ParticleTypes.SMOKE, from.x, from.y + 1, from.z, 16, 0.3, 0.5, 0.3, 0.02);
		player.teleportTo(to.x, to.y, to.z);
		player.connection.teleport(to.x, to.y, to.z, player.getYRot(), player.getXRot());
		player.fallDistance = 0f;
		level.sendParticles(ParticleTypes.PORTAL, to.x, to.y + 1, to.z, 32, 0.4, 0.8, 0.4, 0.5);
		level.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT,
				SoundSource.PLAYERS, 0.7f, 1.4f);

		SLENDER_DODGE_COOLDOWN.put(player.getUUID(), player.tickCount + DODGE_COOLDOWN_TICKS);
		cir.setReturnValue(false);
	}

	private static float dodgeChanceFor(DamageSource source) {
		if (source.is(DamageTypeTags.IS_PROJECTILE)) return 0.95f;
		if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_LIGHTNING)) return 1.0f;
		if (source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypeTags.IS_FREEZING)) return 1.0f;
		if (source.is(DamageTypeTags.IS_FALL)) return 0.0f;
		if (source.is(DamageTypeTags.IS_EXPLOSION)) return 0.50f;
		if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) return 0.70f;
		if (source.getEntity() instanceof LivingEntity) return 0.35f;
		return 0.50f;
	}

	private static Vec3 pickSafeNearby(ServerLevel level, ServerPlayer player, Vec3 origin) {
		var rng = ThreadLocalRandom.current();
		for (int attempt = 0; attempt < 12; attempt++) {
			double angle = rng.nextDouble() * Math.PI * 2;
			double dist = 4 + rng.nextDouble() * (TELEPORT_RANGE - 4);
			double x = origin.x + Math.cos(angle) * dist;
			double z = origin.z + Math.sin(angle) * dist;
			for (int dy = 0; dy <= 4; dy++) {
				for (int sign : new int[]{0, 1, -1}) {
					int yOff = sign == 0 ? 0 : (sign > 0 ? dy : -dy);
					BlockPos pos = BlockPos.containing(x, origin.y + yOff, z);
					if (isSafe(level, pos)) {
						return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					}
				}
			}
		}
		return null;
	}

	private static boolean isSafe(ServerLevel level, BlockPos pos) {
		BlockState floor = level.getBlockState(pos.below());
		BlockState feet = level.getBlockState(pos);
		BlockState head = level.getBlockState(pos.above());
		return !floor.isAir()
				&& feet.getCollisionShape(level, pos).isEmpty()
				&& head.getCollisionShape(level, pos.above()).isEmpty()
				&& !feet.liquid() && !head.liquid();
	}
}
