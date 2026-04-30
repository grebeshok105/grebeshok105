package com.example.superheroes.effect;

import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.network.ScreenShakeS2CPayload;
import com.example.superheroes.network.SlenderFieldS2CPayload;
import com.example.superheroes.network.SlenderJumpscareS2CPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the Static Field ULT zone. Each tick: applies Nausea III + Blindness +
 * Slowness II to enemies inside, applies Regen + Invisibility to the
 * Slenderman caster, every 40 ticks does a tendril-strike on a random enemy.
 *
 * Broadcasts a vignette + screen-shake "inside-field" state to every player
 * (including non-Slenderman heroes from other mods) standing in the radius.
 */
public final class SlendermanFieldController {
	private static final int STRIKE_INTERVAL = 40;
	private static final float STRIKE_DAMAGE = 6.0f;

	private static final Map<UUID, FieldState> ACTIVE = new ConcurrentHashMap<>();
	// caster UUID -> set of player UUIDs that were inside last tick
	private static final Map<UUID, Set<UUID>> INSIDE_LAST = new ConcurrentHashMap<>();

	private SlendermanFieldController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				FieldState state = ACTIVE.get(player.getUUID());
				if (state == null) continue;
				if (player.tickCount >= state.endTick) {
					expire(player);
					continue;
				}
				tick(player, state);
			}
		});
	}

	public static void startField(ServerPlayer player, int durationTicks, double radius) {
		ACTIVE.put(player.getUUID(), new FieldState(player.tickCount + durationTicks, radius, player.tickCount));
		INSIDE_LAST.put(player.getUUID(), new HashSet<>());
		ServerPlayNetworking.send(player, new SlenderFieldS2CPayload(true, durationTicks));
	}

	public static boolean isFieldActive(ServerPlayer player) {
		FieldState state = ACTIVE.get(player.getUUID());
		return state != null && player.tickCount < state.endTick;
	}

	private static void tick(ServerPlayer caster, FieldState state) {
		ServerLevel level = caster.serverLevel();
		Vec3 center = caster.position();
		double r = state.radius;
		double rSq = r * r;

		caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false, false));
		caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 0, true, false, false));
		caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 30, 0, true, false, false));

		AABB box = caster.getBoundingBox().inflate(r);
		List<LivingEntity> entitiesInBox = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e.isAlive() && !e.isSpectator());

		Set<UUID> insideNow = new HashSet<>();
		int remaining = state.endTick - caster.tickCount;
		List<LivingEntity> enemies = new java.util.ArrayList<>();
		for (LivingEntity e : entitiesInBox) {
			if (e.distanceToSqr(center) > rSq) continue;
			if (e.getUUID().equals(caster.getUUID())) continue;
			enemies.add(e);
			if (e instanceof ServerPlayer sp) {
				insideNow.add(sp.getUUID());
				ServerPlayNetworking.send(sp, new SlenderFieldS2CPayload(true, remaining));
				if (caster.tickCount % 8 == 0) {
					ServerPlayNetworking.send(sp, new ScreenShakeS2CPayload(0.6f, 10));
				}
				sp.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 2, true, false, false));
				sp.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, false));
				sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false, false));
			} else {
				e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 2, true, false, false));
				e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, false));
				e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false, false));
			}
		}

		// Send "leave" payload to anyone who was inside last tick but isn't now.
		Set<UUID> last = INSIDE_LAST.computeIfAbsent(caster.getUUID(), k -> new HashSet<>());
		for (UUID id : last) {
			if (insideNow.contains(id)) continue;
			ServerPlayer left = level.getServer().getPlayerList().getPlayer(id);
			if (left != null) {
				ServerPlayNetworking.send(left, new SlenderFieldS2CPayload(false, 0));
			}
		}
		INSIDE_LAST.put(caster.getUUID(), insideNow);

		// Caster keeps overlay too.
		ServerPlayNetworking.send(caster, new SlenderFieldS2CPayload(true, remaining));

		if (caster.tickCount % STRIKE_INTERVAL == 0 && !enemies.isEmpty()) {
			LivingEntity victim = enemies.get(caster.getRandom().nextInt(enemies.size()));
			victim.hurt(ModDamageTypes.slendermanField(level, caster), STRIKE_DAMAGE);
			Vec3 v = victim.position().add(0, victim.getBbHeight() * 0.5, 0);
			level.sendParticles(ParticleTypes.SQUID_INK, v.x, v.y, v.z, 24, 0.5, 0.5, 0.5, 0.05);
			if (victim instanceof ServerPlayer victimPlayer) {
				ServerPlayNetworking.send(victimPlayer, new SlenderJumpscareS2CPayload(20));
			}
		}

		if (caster.tickCount % 4 == 0) {
			double angle = (caster.tickCount * 0.15) % (Math.PI * 2);
			for (int i = 0; i < 3; i++) {
				double a = angle + i * (Math.PI * 2 / 3);
				double x = center.x + Math.cos(a) * r;
				double z = center.z + Math.sin(a) * r;
				level.sendParticles(ParticleTypes.LARGE_SMOKE, x, center.y + 0.5, z, 1, 0.0, 0.3, 0.0, 0.01);
				level.sendParticles(ParticleTypes.SQUID_INK, x, center.y + 1.0, z, 1, 0.0, 0.5, 0.0, 0.0);
			}
		}
	}

	private static void expire(ServerPlayer caster) {
		ACTIVE.remove(caster.getUUID());
		Set<UUID> last = INSIDE_LAST.remove(caster.getUUID());
		ServerPlayNetworking.send(caster, new SlenderFieldS2CPayload(false, 0));
		if (last != null) {
			ServerLevel level = caster.serverLevel();
			for (UUID id : last) {
				ServerPlayer left = level.getServer().getPlayerList().getPlayer(id);
				if (left != null) {
					ServerPlayNetworking.send(left, new SlenderFieldS2CPayload(false, 0));
				}
			}
		}
	}

	public static void clear(UUID id) {
		ACTIVE.remove(id);
		INSIDE_LAST.remove(id);
	}

	private record FieldState(int endTick, double radius, int startTick) {
	}
}
