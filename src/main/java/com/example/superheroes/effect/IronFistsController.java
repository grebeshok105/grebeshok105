package com.example.superheroes.effect;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.ability.AbilityRouter;
import com.example.superheroes.ability.IronFistsAbility;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.network.ScreenShakeS2CPayload;
import com.example.superheroes.physics.ShockwaveUtil;
import com.example.superheroes.sound.ModSounds;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class IronFistsController {
	private static final double SCAN_RADIUS = 30.0;
	private static final double DASH_TRIGGER_DIST = 1.6;
	private static final double DASH_FORCE = 0.55;
	private static final int DASH_COOLDOWN_TICKS = 30;
	private static final int LOOP_INTERVAL_TICKS = 100;
	private static final int AURA_INTERVAL_TICKS = 4;
	private static final int CONE_DOT_THRESHOLD = 0;

	private static final Map<UUID, Integer> ACTIVATE_TICK = new HashMap<>();
	private static final Map<UUID, Integer> LAST_DASH = new HashMap<>();

	private IronFistsController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
				if (!data.isActive(AbilityIds.IRON_FISTS)) {
					if (ACTIVATE_TICK.remove(player.getUUID()) != null) {
						LAST_DASH.remove(player.getUUID());
					}
				}
			}
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
				return InteractionResult.PASS;
			}
			HeroData data = sp.getAttachedOrCreate(ModAttachments.HERO_DATA);
			if (!data.isActive(AbilityIds.IRON_FISTS)) {
				return InteractionResult.PASS;
			}
			if (!(entity instanceof LivingEntity target) || target == sp) {
				return InteractionResult.PASS;
			}
			ServerLevel level = sp.serverLevel();
			target.hurt(level.damageSources().playerAttack(sp), IronFistsAbility.MELEE_DAMAGE);
			Vec3 push = sp.getViewVector(1f).scale(IronFistsAbility.MELEE_KNOCKBACK);
			target.push(push.x, 0.45, push.z);
			target.hurtMarked = true;

			Vec3 hp = target.position().add(0, target.getBbHeight() * 0.5, 0);
			level.sendParticles(ParticleTypes.END_ROD, hp.x, hp.y, hp.z, 18, 0.3, 0.3, 0.3, 0.05);
			level.sendParticles(ParticleTypes.CRIT, hp.x, hp.y, hp.z, 14, 0.3, 0.3, 0.3, 0.2);
			level.playSound(null, target.getX(), target.getY(), target.getZ(),
					ModSounds.HOMELANDER_IRON_FISTS_IMPACT, SoundSource.PLAYERS, 1.0f, 1.0f);

			if (target instanceof ServerPlayer victim) {
				ServerPlayNetworking.send(victim, new ScreenShakeS2CPayload(2.0f, 18));
			}

			sp.swing(hand);
			sp.resetAttackStrengthTicker();
			return InteractionResult.SUCCESS;
		});
	}

	public static void markActivated(ServerPlayer player) {
		ACTIVATE_TICK.put(player.getUUID(), player.tickCount);
		LAST_DASH.remove(player.getUUID());
	}

	public static void markDeactivated(ServerPlayer player) {
		ACTIVATE_TICK.remove(player.getUUID());
		LAST_DASH.remove(player.getUUID());
	}

	public static void tickActive(ServerPlayer player) {
		UUID id = player.getUUID();
		Integer started = ACTIVATE_TICK.get(id);
		if (started == null) {
			started = player.tickCount;
			ACTIVATE_TICK.put(id, started);
		}
		int elapsed = player.tickCount - started;
		if (elapsed >= IronFistsAbility.DURATION_TICKS) {
			AbilityRouter.deactivate(player, AbilityIds.IRON_FISTS);
			return;
		}

		ServerLevel level = player.serverLevel();
		Vec3 p = player.position();

		if (elapsed > 0 && elapsed % LOOP_INTERVAL_TICKS == 0) {
			level.playSound(null, p.x, p.y, p.z, ModSounds.HOMELANDER_IRON_FISTS_CHARGE,
					SoundSource.PLAYERS, 0.8f, 1.0f);
		}

		if (elapsed % AURA_INTERVAL_TICKS == 0) {
			spawnHandAura(player);
		}

		LivingEntity target = findForwardTarget(player);
		if (target == null) return;

		double dist = player.distanceTo(target);
		if (dist > DASH_TRIGGER_DIST) {
			Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.4, 0)
					.subtract(player.position()).normalize();
			Vec3 newVel = toTarget.scale(DASH_FORCE).add(0.0, 0.05, 0.0);
			player.setDeltaMovement(newVel);
			player.hurtMarked = true;
			player.fallDistance = 0f;
			player.hasImpulse = true;
			level.sendParticles(ParticleTypes.END_ROD,
					p.x, p.y + 0.5, p.z, 6, 0.3, 0.3, 0.3, 0.05);
		} else {
			Integer lastDash = LAST_DASH.get(id);
			if (lastDash == null || (player.tickCount - lastDash) >= DASH_COOLDOWN_TICKS) {
				LAST_DASH.put(id, player.tickCount);
				Vec3 impactCenter = target.position();
				ShockwaveUtil.detonate(player, impactCenter, 4.5, 8.0f, false);
				level.playSound(null, impactCenter.x, impactCenter.y, impactCenter.z,
						ModSounds.HOMELANDER_IRON_FISTS_IMPACT, SoundSource.PLAYERS, 1.4f, 0.95f);
			}
		}
	}

	private static LivingEntity findForwardTarget(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 origin = player.getEyePosition();
		Vec3 forward = player.getViewVector(1f).normalize();
		AABB box = new AABB(origin, origin).inflate(SCAN_RADIUS);
		List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != player && e.isAlive() && !e.isSpectator());
		LivingEntity best = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (LivingEntity e : candidates) {
			Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(origin);
			double d = to.length();
			if (d < 0.001 || d > SCAN_RADIUS) continue;
			Vec3 dir = to.scale(1.0 / d);
			double dot = dir.dot(forward);
			if (dot < CONE_DOT_THRESHOLD) continue;
			double score = dot * 1.5 - d / SCAN_RADIUS;
			if (score > bestScore) {
				bestScore = score;
				best = e;
			}
		}
		return best;
	}

	private static void spawnHandAura(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 forward = player.getViewVector(1f).normalize();
		Vec3 right = new Vec3(-forward.z, 0.0, forward.x).normalize();
		Vec3 base = player.position().add(0.0, 1.05, 0.0).add(forward.scale(0.25));
		Vec3 leftHand = base.add(right.scale(-0.35));
		Vec3 rightHand = base.add(right.scale(0.35));
		level.sendParticles(ParticleTypes.END_ROD,
				leftHand.x, leftHand.y, leftHand.z, 2, 0.06, 0.06, 0.06, 0.0);
		level.sendParticles(ParticleTypes.END_ROD,
				rightHand.x, rightHand.y, rightHand.z, 2, 0.06, 0.06, 0.06, 0.0);
	}
}
