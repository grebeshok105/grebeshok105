package com.example.superheroes.effect;

import com.example.superheroes.damage.ModDamageTypes;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the Static Field ULT zone. Each tick: applies Nausea III + Blindness +
 * Slowness II to enemies inside, applies Regen I + Invisibility to the
 * Slenderman caster, every 40 ticks does a tendril-strike on a random enemy.
 */
public final class SlendermanFieldController {
	private static final int STRIKE_INTERVAL = 40;
	private static final float STRIKE_DAMAGE = 6.0f;

	private static final Map<UUID, FieldState> ACTIVE = new ConcurrentHashMap<>();

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
		ServerPlayNetworking.send(player, new SlenderFieldS2CPayload(true, durationTicks));
	}

	public static boolean isFieldActive(ServerPlayer player) {
		FieldState state = ACTIVE.get(player.getUUID());
		return state != null && player.tickCount < state.endTick;
	}

	private static void tick(ServerPlayer player, FieldState state) {
		ServerLevel level = player.serverLevel();
		Vec3 center = player.position();
		double r = state.radius;

		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 0, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 30, 0, true, false, false));

		AABB box = player.getBoundingBox().inflate(r);
		List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != player && e.isAlive() && !e.isSpectator()
						&& !(e instanceof Player p && p.getUUID().equals(player.getUUID())));

		for (LivingEntity enemy : enemies) {
			if (enemy.distanceToSqr(center) > r * r) continue;
			enemy.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 2, true, false, false));
			enemy.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, false));
			enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false, false));
		}

		if (player.tickCount % STRIKE_INTERVAL == 0 && !enemies.isEmpty()) {
			LivingEntity victim = enemies.get(player.getRandom().nextInt(enemies.size()));
			victim.hurt(ModDamageTypes.slendermanField(level, player), STRIKE_DAMAGE);
			Vec3 v = victim.position().add(0, victim.getBbHeight() * 0.5, 0);
			level.sendParticles(ParticleTypes.SQUID_INK, v.x, v.y, v.z, 24, 0.5, 0.5, 0.5, 0.05);
			if (victim instanceof net.minecraft.server.level.ServerPlayer victimPlayer) {
				ServerPlayNetworking.send(victimPlayer, new SlenderJumpscareS2CPayload(20));
			}
		}

		if (player.tickCount % 4 == 0) {
			double angle = (player.tickCount * 0.15) % (Math.PI * 2);
			for (int i = 0; i < 3; i++) {
				double a = angle + i * (Math.PI * 2 / 3);
				double x = center.x + Math.cos(a) * r;
				double z = center.z + Math.sin(a) * r;
				level.sendParticles(ParticleTypes.LARGE_SMOKE, x, center.y + 0.5, z, 1, 0.0, 0.3, 0.0, 0.01);
				level.sendParticles(ParticleTypes.SQUID_INK, x, center.y + 1.0, z, 1, 0.0, 0.5, 0.0, 0.0);
			}
		}
	}

	private static void expire(ServerPlayer player) {
		ACTIVE.remove(player.getUUID());
		ServerPlayNetworking.send(player, new SlenderFieldS2CPayload(false, 0));
	}

	public static void clear(UUID id) {
		ACTIVE.remove(id);
	}

	private record FieldState(int endTick, double radius, int startTick) {
	}
}
