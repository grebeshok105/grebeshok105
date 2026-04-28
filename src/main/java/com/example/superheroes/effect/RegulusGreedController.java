package com.example.superheroes.effect;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegulusGreedController {
	private static final int FREEZE_TICKS = 200;
	private static final double PULL_STRENGTH = 0.6;

	private static final Map<UUID, MagnetState> MAGNETS = new ConcurrentHashMap<>();
	private static final Map<UUID, FreezeState> FREEZES = new ConcurrentHashMap<>();

	private RegulusGreedController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			List<UUID> toRelease = new ArrayList<>();
			for (Map.Entry<UUID, FreezeState> e : FREEZES.entrySet()) {
				FreezeState st = e.getValue();
				LivingEntity victim = st.victim(server);
				if (victim == null || !victim.isAlive()) {
					toRelease.add(e.getKey());
					continue;
				}
				victim.setDeltaMovement(Vec3.ZERO);
				victim.hurtMarked = true;
				if (victim instanceof ServerPlayer sp) {
					sp.connection.teleport(st.lockX, st.lockY, st.lockZ, sp.getYRot(), sp.getXRot());
				} else {
					victim.teleportTo(st.lockX, st.lockY, st.lockZ);
				}
				if (victim.tickCount % 4 == 0) {
					ServerLevel sl = (ServerLevel) victim.level();
					sl.sendParticles(ParticleTypes.END_ROD,
							victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
							3, 0.4, 0.4, 0.4, 0.0);
				}
				if (--st.remaining <= 0) {
					toRelease.add(e.getKey());
				}
			}
			for (UUID id : toRelease) {
				FreezeState st = FREEZES.remove(id);
				if (st != null) {
					st.release(server);
				}
			}
		});

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			FreezeState st = FREEZES.get(entity.getUUID());
			if (st == null) {
				return true;
			}
			st.queuedDamage.add(new QueuedDamage(source, amount));
			return false;
		});
	}

	public static boolean isFrozen(LivingEntity entity) {
		return FREEZES.containsKey(entity.getUUID());
	}

	public static void startMagnet(ServerPlayer player, LivingEntity victim) {
		MAGNETS.put(player.getUUID(), new MagnetState(victim.getUUID(), player.tickCount));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FREEZE_TICKS, 250, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.JUMP, FREEZE_TICKS, -50, true, false, false));
	}

	public static void tickMagnet(ServerPlayer player) {
		MagnetState m = MAGNETS.get(player.getUUID());
		if (m == null) {
			return;
		}
		LivingEntity victim = (LivingEntity) ((ServerLevel) player.level()).getEntity(m.victimId);
		if (victim == null || !victim.isAlive() || victim.distanceTo(player) > 32) {
			player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
			player.removeEffect(MobEffects.JUMP);
			MAGNETS.remove(player.getUUID());
			return;
		}
		Vec3 dir = player.position().subtract(victim.position()).normalize().scale(PULL_STRENGTH);
		victim.setDeltaMovement(dir);
		victim.hurtMarked = true;
		ServerLevel sl = (ServerLevel) player.level();
		if (player.tickCount % 4 == 0) {
			Vec3 mid = player.position().add(victim.position()).scale(0.5);
			sl.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y + 1.0, mid.z, 4, 0.3, 0.3, 0.3, 0.02);
		}
		if (player.tickCount % 20 == 0) {
			sl.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.6f, 1.2f);
		}
	}

	public static void releaseAndFreeze(ServerPlayer player) {
		MagnetState m = MAGNETS.remove(player.getUUID());
		player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		player.removeEffect(MobEffects.JUMP);
		if (m == null) {
			return;
		}
		LivingEntity victim = (LivingEntity) ((ServerLevel) player.level()).getEntity(m.victimId);
		if (victim == null || !victim.isAlive()) {
			return;
		}
		// Buff regulus
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, FREEZE_TICKS, 4, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, FREEZE_TICKS, 2, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, FREEZE_TICKS, 1, true, false, true));

		FreezeState st = new FreezeState(victim.getUUID(), victim.getX(), victim.getY(), victim.getZ(), FREEZE_TICKS);
		boolean wasNoAi = false;
		if (victim instanceof Mob mob) {
			wasNoAi = mob.isNoAi();
			mob.setNoAi(true);
			st.restoreNoAi = wasNoAi;
		}
		FREEZES.put(victim.getUUID(), st);
		ServerLevel sl = (ServerLevel) player.level();
		sl.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.7f, 1.4f);
		sl.sendParticles(ParticleTypes.FLASH, victim.getX(), victim.getY() + 1.0, victim.getZ(), 1, 0, 0, 0, 0);
	}

	private record MagnetState(UUID victimId, int startTick) {
	}

	private static final class FreezeState {
		final UUID victimId;
		final double lockX, lockY, lockZ;
		int remaining;
		boolean restoreNoAi = false;
		final List<QueuedDamage> queuedDamage = new ArrayList<>();

		FreezeState(UUID victimId, double x, double y, double z, int remaining) {
			this.victimId = victimId;
			this.lockX = x;
			this.lockY = y;
			this.lockZ = z;
			this.remaining = remaining;
		}

		LivingEntity victim(net.minecraft.server.MinecraftServer server) {
			for (ServerLevel level : server.getAllLevels()) {
				Entity e = level.getEntity(victimId);
				if (e instanceof LivingEntity le) {
					return le;
				}
			}
			return null;
		}

		void release(net.minecraft.server.MinecraftServer server) {
			LivingEntity v = victim(server);
			if (v == null) return;
			if (v instanceof Mob mob) {
				mob.setNoAi(restoreNoAi);
			}
			for (QueuedDamage q : queuedDamage) {
				v.hurt(q.source, q.amount);
				if (!v.isAlive()) break;
			}
			ServerLevel sl = (ServerLevel) v.level();
			sl.playSound(null, v.getX(), v.getY(), v.getZ(),
					SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 0.8f);
			sl.sendParticles(ParticleTypes.EXPLOSION, v.getX(), v.getY() + 1.0, v.getZ(), 1, 0, 0, 0, 0);
		}
	}

	private record QueuedDamage(DamageSource source, float amount) {
	}
}
