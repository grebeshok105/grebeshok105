package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.HeroAttributes;
import com.example.superheroes.hero.RegulusHero;
import com.example.superheroes.network.MadnessSyncS2CPayload;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegulusMadnessController {
	private static final long READING_DURATION_MS = 5000L;
	private static final long MANA_LOCK_DURATION_MS = 20_000L;
	private static final int COUNTER_FREEZE_TICKS = 12;
	private static final int COUNTER_LAUNCH_TICKS = 12;
	private static final int COUNTER_SLAM_TICKS = 30;
	private static final double CRATER_RADIUS = 4.0;
	private static final int CRATER_DEPTH = 20;
	private static final int DODGE_COOLDOWN_TICKS = 40;

	private static final Map<UUID, Long> DODGE_COOLDOWN = new ConcurrentHashMap<>();

	private static final Map<UUID, CounterState> COUNTERS = new ConcurrentHashMap<>();

	private RegulusMadnessController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
			List<UUID> done = new ArrayList<>();
			for (Map.Entry<UUID, CounterState> e : COUNTERS.entrySet()) {
				if (e.getValue().tick(server)) {
					done.add(e.getKey());
				}
			}
			for (UUID id : done) {
				COUNTERS.remove(id);
			}
		});

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) {
				return true;
			}
			RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
			if (state.isReading()) {
				return false;
			}
			if (!isRegulus(player) || !state.madness()) {
				return true;
			}
			HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
			Entity attacker = source.getEntity();
			if (!(attacker instanceof LivingEntity living) || attacker == player) {
				return true;
			}
			Long cd = DODGE_COOLDOWN.get(player.getUUID());
			if (cd != null && cd > player.tickCount) {
				return true;
			}
			if (data.energy() <= 0) {
				return true;
			}
			triggerCounter(player, living);
			return false;
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
		if (state.isReading()) {
			player.setDeltaMovement(Vec3.ZERO);
			player.hurtMarked = true;
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8, 4, true, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 250, true, false, false));
			ServerLevel level = (ServerLevel) player.level();
			if (player.tickCount % 2 == 0) {
				level.sendParticles(ParticleTypes.END_ROD,
						player.getX(), player.getY() + 1.0, player.getZ(),
						6, 0.8, 1.2, 0.8, 0.05);
				level.sendParticles(ParticleTypes.ENCHANT,
						player.getX(), player.getY() + 1.5, player.getZ(),
						12, 1.0, 1.5, 1.0, 0.4);
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
						player.getX(), player.getY() + 1.0, player.getZ(),
						3, 0.6, 1.0, 0.6, 0.02);
			}
			if (player.tickCount % 30 == 0) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0f, 0.7f);
				LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
				if (bolt != null) {
					double angle = level.random.nextDouble() * Math.PI * 2;
					double r = 2.5 + level.random.nextDouble() * 1.5;
					bolt.moveTo(player.getX() + Math.cos(angle) * r, player.getY(), player.getZ() + Math.sin(angle) * r);
					bolt.setVisualOnly(true);
					level.addFreshEntity(bolt);
				}
			}
			if (System.currentTimeMillis() >= state.readingUntilMs()) {
				finishReading(player);
			}
		}
		// Mana lock: zero mana while locked
		if (state.isManaRegenLocked() && player.getAttachedOrCreate(ModAttachments.HERO_DATA).mana() > 0f) {
			HeroData d = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
			player.setAttached(ModAttachments.HERO_DATA, d.withMana(0f));
		}
	}

	public static boolean isRegulus(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && RegulusHero.ID.equals(data.heroId());
	}

	public static void startReading(ServerPlayer player) {
		long now = System.currentTimeMillis();
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS)
				.withReading(now + READING_DURATION_MS);
		player.setAttached(ModAttachments.REGULUS_MADNESS, state);
		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.4f, 0.6f);
		sync(player);
	}

	private static void finishReading(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS)
				.withReading(0L)
				.withMadness(true)
				.withBonusLife(true);
		player.setAttached(ModAttachments.REGULUS_MADNESS, state);

		HeroAttributes.REGULUS_MADNESS.apply(player);
		player.setHealth(player.getMaxHealth());
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 2, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, 2, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 2, true, false, true));

		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WARDEN_EMERGE, SoundSource.PLAYERS, 1.5f, 0.7f);
		level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(),
				80, 1.5, 2.0, 1.5, 0.05);
		sync(player);
	}

	public static void clearMadness(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
		if (state.madness()) {
			HeroAttributes.REGULUS_MADNESS.remove(player);
			player.removeEffect(MobEffects.MOVEMENT_SPEED);
			player.removeEffect(MobEffects.DAMAGE_BOOST);
			player.removeEffect(MobEffects.JUMP);
		}
		player.setAttached(ModAttachments.REGULUS_MADNESS, RegulusMadnessState.EMPTY);
		sync(player);
	}

	public static boolean consumeBonusLife(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
		if (!state.bonusLifeAvailable()) {
			return false;
		}
		player.setAttached(ModAttachments.REGULUS_MADNESS, state.withBonusLife(false));
		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.6f, 0.8f);
		level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(),
				120, 1.0, 2.0, 1.0, 0.1);
		sync(player);
		return true;
	}

	public static void sync(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
		ServerPlayNetworking.send(player, new MadnessSyncS2CPayload(
				state.madness(),
				state.bonusLifeAvailable(),
				state.readingUntilMs(),
				state.manaRegenLockUntilMs()
		));
	}

	private static void triggerCounter(ServerPlayer player, LivingEntity attacker) {
		long now = System.currentTimeMillis();
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		player.setAttached(ModAttachments.HERO_DATA, data.withMana(0f).withEnergy(0f));
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS)
				.withManaLock(now + MANA_LOCK_DURATION_MS);
		player.setAttached(ModAttachments.REGULUS_MADNESS, state);
		DODGE_COOLDOWN.put(player.getUUID(), (long) (player.tickCount + DODGE_COOLDOWN_TICKS));

		Vec3 attackerLook = attacker.getViewVector(1.0f);
		Vec3 behind = attacker.position().subtract(attackerLook.x, 0, attackerLook.z).add(-attackerLook.x * 0.5, 0, -attackerLook.z * 0.5);
		player.connection.teleport(behind.x, attacker.getY(), behind.z, (float) Math.toDegrees(Math.atan2(attackerLook.x, -attackerLook.z)) + 180f, 0f);

		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.9f, 1.4f);
		level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
				SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.7f, 1.0f);
		level.sendParticles(ParticleTypes.FLASH, attacker.getX(), attacker.getY() + 1.0, attacker.getZ(), 3, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.END_ROD, attacker.getX(), attacker.getY() + 1.0, attacker.getZ(),
				40, 0.8, 1.0, 0.8, 0.1);

		COUNTERS.put(player.getUUID(), new CounterState(player.getUUID(), attacker.getUUID(), level.dimension()));
		sync(player);
	}

	private static final class CounterState {
		final UUID playerId;
		final UUID attackerId;
		final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim;
		int tick = 0;
		Phase phase = Phase.FREEZE;
		boolean attackerWasNoAi;
		double snapshotX, snapshotY, snapshotZ;

		CounterState(UUID playerId, UUID attackerId, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim) {
			this.playerId = playerId;
			this.attackerId = attackerId;
			this.dim = dim;
		}

		boolean tick(net.minecraft.server.MinecraftServer server) {
			ServerLevel level = server.getLevel(dim);
			if (level == null) return true;
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			Entity ae = level.getEntity(attackerId);
			if (player == null || !(ae instanceof LivingEntity attacker) || !attacker.isAlive()) {
				return true;
			}
			tick++;
			switch (phase) {
				case FREEZE -> {
					if (tick == 1) {
						snapshotX = attacker.getX();
						snapshotY = attacker.getY();
						snapshotZ = attacker.getZ();
						if (attacker instanceof Mob mob) {
							attackerWasNoAi = mob.isNoAi();
							mob.setNoAi(true);
						}
					}
					attacker.setDeltaMovement(Vec3.ZERO);
					attacker.teleportTo(snapshotX, snapshotY, snapshotZ);
					player.setDeltaMovement(Vec3.ZERO);
					if (tick % 3 == 0) {
						level.sendParticles(ParticleTypes.END_ROD,
								attacker.getX(), attacker.getY() + 1.0, attacker.getZ(),
								4, 0.5, 0.8, 0.5, 0.05);
					}
					if (tick >= COUNTER_FREEZE_TICKS) {
						boolean onGround = attacker.onGround();
						if (onGround) {
							attacker.setDeltaMovement(0, 3.5, 0);
							attacker.hurtMarked = true;
							attacker.hurt(level.damageSources().playerAttack(player), 30f);
							level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
									SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.4f, 1.5f);
							phase = Phase.LAUNCH;
							tick = 0;
						} else {
							phase = Phase.SLAM;
							tick = 0;
							if (attacker instanceof Mob mob) {
								mob.setNoAi(attackerWasNoAi);
							}
							attacker.setDeltaMovement(0, -3.5, 0);
							attacker.hurtMarked = true;
							attacker.hurt(level.damageSources().playerAttack(player), 50f);
						}
					}
				}
				case LAUNCH -> {
					if (tick >= COUNTER_LAUNCH_TICKS) {
						if (attacker instanceof Mob mob) {
							mob.setNoAi(attackerWasNoAi);
						}
						attacker.setDeltaMovement(0, -3.5, 0);
						attacker.hurtMarked = true;
						attacker.hurt(level.damageSources().playerAttack(player), 50f);
						phase = Phase.SLAM;
						tick = 0;
					}
				}
				case SLAM -> {
					if (attacker.onGround() || attacker.verticalCollision || tick >= COUNTER_SLAM_TICKS) {
						return finalSlam(level, player, attacker);
					}
				}
			}
			return false;
		}

		boolean finalSlam(ServerLevel level, ServerPlayer player, LivingEntity attacker) {
			BlockPos impact = attacker.blockPosition();
			level.explode(player, impact.getX(), impact.getY(), impact.getZ(), 6.0f, Level.ExplosionInteraction.NONE);
			carveCrater(level, impact);
			attacker.teleportTo(impact.getX() + 0.5, impact.getY() - CRATER_DEPTH + 1, impact.getZ() + 0.5);
			attacker.hurt(level.damageSources().playerAttack(player), 80f);
			level.playSound(null, impact.getX(), impact.getY(), impact.getZ(),
					SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 0.4f);
			level.playSound(null, impact.getX(), impact.getY(), impact.getZ(),
					SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6f, 0.8f);
			level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
					impact.getX(), impact.getY(), impact.getZ(), 3, 1.0, 0.5, 1.0, 0);
			level.sendParticles(ParticleTypes.LARGE_SMOKE,
					impact.getX(), impact.getY(), impact.getZ(), 80, 3.0, 1.0, 3.0, 0.1);
			return true;
		}

		void carveCrater(ServerLevel level, BlockPos impact) {
			int r = (int) CRATER_RADIUS;
			for (int dy = 0; dy < CRATER_DEPTH; dy++) {
				int radius = r - (dy * r / CRATER_DEPTH);
				if (radius < 1) radius = 1;
				int radiusSq = radius * radius;
				for (int dx = -radius; dx <= radius; dx++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if (dx * dx + dz * dz > radiusSq) continue;
						BlockPos p = impact.offset(dx, -dy, dz);
						if (p.getY() <= level.getMinBuildHeight()) continue;
						level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
					}
				}
			}
		}

		enum Phase { FREEZE, LAUNCH, SLAM }
	}
}
