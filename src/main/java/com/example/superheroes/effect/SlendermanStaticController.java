package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.hero.SlendermanHero;
import com.example.superheroes.network.SlenderStaticS2CPayload;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side LoS scanner. For each Slenderman player, every 5 ticks scan
 * for LivingEntities in a 90° cone within R=20 blocks. Targets in LoS gain
 * a "static stack" (max 20). Stacks decay at -1/sec when out of LoS.
 *
 * Stack thresholds:
 *  -  5: Nausea I (60t)
 *  - 10: Nausea II + slow flash sound
 *  - 15: Nausea III + Blindness (40t)
 *  - 20: tendril-strike 8 dmg + reset to 15
 *
 * Per-target stacks are stored Slenderman-UUID → target-UUID → stacks. We
 * also track a "max nearby stacks" per Slenderman that we sync to the
 * client for the personal TV-static overlay.
 */
public final class SlendermanStaticController {
	private static final double SCAN_RADIUS = 20.0;
	private static final double SCAN_RADIUS_SQ = SCAN_RADIUS * SCAN_RADIUS;
	private static final double CONE_DOT = 0.0; // 90° half-angle
	private static final int SCAN_INTERVAL = 5;
	private static final int DECAY_INTERVAL = 20;
	private static final int MAX_STACKS = 20;

	private static final Map<UUID, Map<UUID, Integer>> SLENDER_TARGET_STACKS = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> EXPOSED_UNTIL = new ConcurrentHashMap<>();

	private SlendermanStaticController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer slender : server.getPlayerList().getPlayers()) {
				if (!isSlenderman(slender)) {
					SLENDER_TARGET_STACKS.remove(slender.getUUID());
					EXPOSED_UNTIL.remove(slender.getUUID());
					continue;
				}
				if (slender.tickCount % SCAN_INTERVAL == 0) {
					scan(slender);
				}
				if (slender.tickCount % DECAY_INTERVAL == 0) {
					decay(slender);
				}
				if (slender.tickCount % 5 == 0) {
					syncStaticForOwner(slender);
				}
				checkExposureExpiry(slender);
			}
		});
	}

	private static boolean isSlenderman(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && SlendermanHero.ID.equals(data.heroId());
	}

	private static void scan(ServerPlayer slender) {
		ServerLevel level = slender.serverLevel();
		Vec3 eye = slender.getEyePosition();
		Vec3 forward = slender.getViewVector(1f).normalize();
		AABB box = new AABB(eye, eye).inflate(SCAN_RADIUS);

		Map<UUID, Integer> stacks = SLENDER_TARGET_STACKS
				.computeIfAbsent(slender.getUUID(), k -> new HashMap<>());

		List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != slender && e.isAlive() && !e.isSpectator()
						&& !(e instanceof Player p && p.getUUID().equals(slender.getUUID())));

		for (LivingEntity target : nearby) {
			Vec3 targetEye = target.getEyePosition();
			Vec3 to = targetEye.subtract(eye);
			double distSq = to.lengthSqr();
			if (distSq > SCAN_RADIUS_SQ) continue;
			Vec3 toN = to.normalize();
			if (toN.dot(forward) < CONE_DOT) continue;

			HitResult hr = level.clip(new ClipContext(eye, targetEye,
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, slender));
			if (hr.getType() == HitResult.Type.BLOCK) continue;

			int s = Math.min(MAX_STACKS, stacks.getOrDefault(target.getUUID(), 0) + 1);
			stacks.put(target.getUUID(), s);

			Vec3 to2 = eye.subtract(targetEye).normalize();
			if (to2.dot(target.getViewVector(1f).normalize()) > 0.85) {
				int now = slender.tickCount;
				EXPOSED_UNTIL.merge(slender.getUUID(), now + 80, Integer::max);
			}

			applyEffects(slender, target, s);

			if (s >= MAX_STACKS) {
				target.hurt(ModDamageTypes.slendermanStatic(level, slender), 8.0f);
				level.sendParticles(ParticleTypes.SQUID_INK, target.getX(),
						target.getY() + target.getBbHeight() * 0.5, target.getZ(),
						18, 0.4, 0.4, 0.4, 0.05);
				stacks.put(target.getUUID(), 15);
			}
		}
	}

	private static void applyEffects(ServerPlayer slender, LivingEntity target, int stacks) {
		if (stacks >= 5 && stacks < 10) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, true, false, false));
		} else if (stacks >= 10 && stacks < 15) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 1, true, false, false));
		} else if (stacks >= 15) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 2, true, false, false));
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, false));
		}
	}

	private static void decay(ServerPlayer slender) {
		Map<UUID, Integer> stacks = SLENDER_TARGET_STACKS.get(slender.getUUID());
		if (stacks == null || stacks.isEmpty()) return;
		ServerLevel level = slender.serverLevel();
		Vec3 eye = slender.getEyePosition();
		Vec3 forward = slender.getViewVector(1f).normalize();
		stacks.entrySet().removeIf(entry -> {
			LivingEntity target = (LivingEntity) level.getEntity(entry.getKey());
			if (target == null || !target.isAlive() || target.distanceToSqr(slender) > SCAN_RADIUS_SQ * 1.5) {
				return true;
			}
			Vec3 to = target.getEyePosition().subtract(eye);
			boolean inLos = to.normalize().dot(forward) >= CONE_DOT
					&& level.clip(new ClipContext(eye, target.getEyePosition(),
							ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, slender))
							.getType() != HitResult.Type.BLOCK;
			if (!inLos) {
				int s = entry.getValue() - 1;
				if (s <= 0) {
					return true;
				}
				entry.setValue(s);
			}
			return false;
		});
	}

	private static void syncStaticForOwner(ServerPlayer slender) {
		Map<UUID, Integer> stacks = SLENDER_TARGET_STACKS.get(slender.getUUID());
		int max = 0;
		if (stacks != null) {
			for (Integer v : stacks.values()) {
				if (v != null && v > max) max = v;
			}
		}
		float fade = Math.min(1f, max / 20f);
		ServerPlayNetworking.send(slender, new SlenderStaticS2CPayload(max, fade));
	}

	private static void checkExposureExpiry(ServerPlayer slender) {
		Integer until = EXPOSED_UNTIL.get(slender.getUUID());
		if (until != null && slender.tickCount >= until) {
			EXPOSED_UNTIL.remove(slender.getUUID());
		}
	}

	public static boolean isExposed(ServerPlayer slender) {
		Integer until = EXPOSED_UNTIL.get(slender.getUUID());
		return until != null && slender.tickCount < until;
	}

	public static void clear(UUID id) {
		SLENDER_TARGET_STACKS.remove(id);
		EXPOSED_UNTIL.remove(id);
	}
}
