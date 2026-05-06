package com.example.superheroes.effect;

import com.example.superheroes.network.ScreenShakeS2CPayload;
import com.example.superheroes.sound.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HeavensStrikeController {
	public static final int WINDUP_TICKS = 80;
	public static final int IMPACT_TICKS = 80;
	private static final int FREEZE_REFRESH_TICKS = 18;
	private static final double SKY_DROP_HEIGHT = 80.0;

	public record Variant(int length, int width, int depth, float shakeIntensity,
	                      float damage, float pitch, double shakeRadius) {
		public static final Variant REINHARD = new Variant(500, 12, 16, 12.0f, 2500f, 0.45f, 400.0);
		public static final Variant RAIDEN = new Variant(90, 6, 8, 8.0f, 110f, 0.78f, 80.0);
	}

	public static final class Pending {
		final Vec3 origin;
		final Vec3 lookFlat;
		final long startTick;
		final long impactStartTick;
		final long impactEndTick;
		final Variant variant;
		final Vec3 lockPos;
		final float lockYaw;
		final float lockPitch;
		final Set<UUID> hitEntities = new HashSet<>();
		int sweptIndex = 0;

		Pending(Vec3 origin, Vec3 lookFlat, long startTick, Variant variant,
		        Vec3 lockPos, float lockYaw, float lockPitch) {
			this.origin = origin;
			this.lookFlat = lookFlat;
			this.startTick = startTick;
			this.impactStartTick = startTick + WINDUP_TICKS;
			this.impactEndTick = this.impactStartTick + IMPACT_TICKS;
			this.variant = variant;
			this.lockPos = lockPos;
			this.lockYaw = lockYaw;
			this.lockPitch = lockPitch;
		}
	}

	private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

	private static final Set<Block> UNBREAKABLE = Set.of(
			Blocks.BEDROCK,
			Blocks.BARRIER,
			Blocks.END_PORTAL,
			Blocks.END_PORTAL_FRAME,
			Blocks.END_GATEWAY,
			Blocks.NETHER_PORTAL,
			Blocks.COMMAND_BLOCK,
			Blocks.CHAIN_COMMAND_BLOCK,
			Blocks.REPEATING_COMMAND_BLOCK,
			Blocks.STRUCTURE_BLOCK,
			Blocks.STRUCTURE_VOID,
			Blocks.JIGSAW,
			Blocks.LIGHT,
			Blocks.REINFORCED_DEEPSLATE
	);

	private HeavensStrikeController() {}

	public static boolean isCharging(ServerPlayer player) {
		return PENDING.containsKey(player.getUUID());
	}

	public static boolean start(ServerPlayer player, Variant variant) {
		if (PENDING.containsKey(player.getUUID())) return false;
		ServerLevel level = player.serverLevel();
		long now = level.getGameTime();
		Vec3 origin = player.position();
		Vec3 look = player.getLookAngle();
		Vec3 flat = new Vec3(look.x, 0, look.z);
		if (flat.lengthSqr() < 0.001) {
			flat = new Vec3(1, 0, 0);
		} else {
			flat = flat.normalize();
		}
		PENDING.put(player.getUUID(), new Pending(origin, flat, now, variant,
				origin, player.getYRot(), player.getXRot()));
		applyFreeze(player);

		level.playSound(null, origin.x, origin.y, origin.z,
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 0.6f);
		level.playSound(null, origin.x, origin.y, origin.z,
				SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.6f, 0.5f);
		level.sendParticles(ParticleTypes.FLASH,
				origin.x, origin.y + 1.5, origin.z, 4, 0, 0, 0, 0);
		return true;
	}

	public static void cancel(UUID id) {
		PENDING.remove(id);
	}

	private static void applyFreeze(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
				FREEZE_REFRESH_TICKS + 6, 6, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
				FREEZE_REFRESH_TICKS + 6, 4, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
				FREEZE_REFRESH_TICKS + 6, 4, true, false, false));
	}

	private static void clearFreeze(ServerPlayer player) {
		player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		player.removeEffect(MobEffects.WEAKNESS);
		player.removeEffect(MobEffects.DIG_SLOWDOWN);
	}

	private static void enforceLock(ServerPlayer player, Pending p, long now) {
		long elapsed = now - p.startTick;
		if (elapsed > 0 && elapsed % FREEZE_REFRESH_TICKS == 0) {
			applyFreeze(player);
		}
		player.setDeltaMovement(Vec3.ZERO);
		player.hasImpulse = true;
		player.fallDistance = 0f;
		Vec3 cur = player.position();
		double dx = cur.x - p.lockPos.x;
		double dy = cur.y - p.lockPos.y;
		double dz = cur.z - p.lockPos.z;
		if (dx * dx + dy * dy + dz * dz > 0.04) {
			player.teleportTo(p.lockPos.x, p.lockPos.y, p.lockPos.z);
			player.setYRot(p.lockYaw);
			player.setXRot(p.lockPitch);
			player.hurtMarked = true;
		}
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (PENDING.isEmpty()) return;
			Iterator<Map.Entry<UUID, Pending>> it = PENDING.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, Pending> e = it.next();
				ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
				if (player == null) {
					it.remove();
					continue;
				}
				Pending p = e.getValue();
				long now = player.serverLevel().getGameTime();
				if (now >= p.impactEndTick) {
					clearFreeze(player);
					it.remove();
					continue;
				}
				enforceLock(player, p, now);
				if (now < p.impactStartTick) {
					windupTick(player, p, now);
				} else {
					impactSweepTick(player, p, now);
				}
			}
		});
	}

	private static void windupTick(ServerPlayer player, Pending p, long now) {
		ServerLevel level = player.serverLevel();
		Variant v = p.variant;
		long ticksDone = now - p.startTick;
		double progress = (double) ticksDone / WINDUP_TICKS;
		RandomSource r = level.random;

		double swordY = p.origin.y + SKY_DROP_HEIGHT * (1.0 - progress);
		double swordX = p.origin.x + p.lookFlat.x * (v.length * 0.5);
		double swordZ = p.origin.z + p.lookFlat.z * (v.length * 0.5);

		double bladeLength = Math.min(20.0, v.length * 0.25);
		double bladeWidth = Math.max(0.6, v.width * 0.15);
		double guardWidth = bladeWidth * 4.0;

		int bladeSegments = (int) bladeLength * 2;
		for (int i = 0; i <= bladeSegments; i++) {
			double t = (double) i / Math.max(1, bladeSegments);
			double y = swordY - bladeLength * t;
			double jitter = (r.nextDouble() - 0.5) * bladeWidth * 0.6;
			double jitterZ = (r.nextDouble() - 0.5) * bladeWidth * 0.6;
			level.sendParticles(ParticleTypes.END_ROD,
					swordX + jitter, y, swordZ + jitterZ, 1, 0, 0, 0, 0);
			if (i % 3 == 0) {
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
						swordX + jitter * 1.4, y, swordZ + jitterZ * 1.4, 1, 0, 0, 0, 0);
			}
		}

		int guardSamples = 14;
		for (int i = 0; i < guardSamples; i++) {
			double t = (double) i / (guardSamples - 1);
			double off = (t - 0.5) * 2.0 * guardWidth;
			double gx = swordX + p.lookFlat.x * 0.0 + (-p.lookFlat.z) * off;
			double gz = swordZ + p.lookFlat.z * 0.0 + (p.lookFlat.x) * off;
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					gx, swordY + 0.05, gz, 1, 0, 0, 0, 0);
			level.sendParticles(ParticleTypes.END_ROD,
					gx, swordY, gz, 1, 0, 0, 0, 0);
		}
		level.sendParticles(ParticleTypes.FLASH,
				swordX, swordY + 0.4, swordZ, 1, 0, 0, 0, 0);

		if (now % 4 == 0) {
			double trailX = swordX + (r.nextDouble() - 0.5) * 1.2;
			double trailZ = swordZ + (r.nextDouble() - 0.5) * 1.2;
			level.sendParticles(ParticleTypes.LARGE_SMOKE,
					trailX, swordY + bladeLength * 0.3, trailZ, 2, 0.2, 0.5, 0.2, 0.0);
		}

		if (now % 12 == 0) {
			level.playSound(null, p.origin.x, p.origin.y, p.origin.z,
					SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
					1.6f, 0.35f + (float) progress * 0.6f);
		}
		if (now % 18 == 0) {
			level.playSound(null, swordX, swordY, swordZ,
					SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS,
					2.0f, 0.4f + (float) progress * 0.4f);
		}
		if (ticksDone == WINDUP_TICKS / 2) {
			level.playSound(null, p.origin.x, p.origin.y, p.origin.z,
					SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 2.0f, 0.5f);
		}
	}

	private static void impactSweepTick(ServerPlayer player, Pending p, long now) {
		ServerLevel level = player.serverLevel();
		Variant v = p.variant;
		long impactElapsed = now - p.impactStartTick;
		double progress = (double) impactElapsed / IMPACT_TICKS;
		double prevProgress = (double) (impactElapsed - 1) / IMPACT_TICKS;
		if (impactElapsed == 0) prevProgress = 0.0;

		double sweepFromDist = Math.max(1.0, v.length * Math.max(0.0, prevProgress));
		double sweepToDist = v.length * Math.min(1.0, progress);

		Vec3 lookFlat = p.lookFlat;
		Vec3 perpendicular = new Vec3(-lookFlat.z, 0, lookFlat.x);
		Vec3 origin = p.origin;
		int halfWidth = v.width / 2;

		int sliceFrom = Math.max(p.sweptIndex + 1, (int) Math.floor(sweepFromDist));
		int sliceTo = (int) Math.ceil(sweepToDist);
		if (sliceTo > v.length) sliceTo = v.length;

		int destroyed = 0;
		final int destroyBudget = 1500;
		for (int i = sliceFrom; i <= sliceTo && destroyed < destroyBudget; i++) {
			Vec3 point = origin.add(lookFlat.scale(i));
			for (int w = -halfWidth; w <= halfWidth && destroyed < destroyBudget; w++) {
				Vec3 offset = perpendicular.scale(w);
				int ox = (int) Math.floor(point.x + offset.x);
				int oz = (int) Math.floor(point.z + offset.z);
				int sy = findSurface(level, ox, (int) origin.y, oz);
				int localDepth = depthAt(v, i, w, halfWidth);

				for (int dy = 0; dy < localDepth && destroyed < destroyBudget; dy++) {
					BlockPos pos = new BlockPos(ox, sy - dy, oz);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) continue;
					if (UNBREAKABLE.contains(state.getBlock())) continue;
					if (state.getDestroySpeed(level, pos) < 0) continue;
					level.destroyBlock(pos, false);
					destroyed++;
				}
			}

			if (i % Math.max(1, v.length / 10) == 0) {
				int sy = findSurface(level, (int) Math.floor(point.x), (int) origin.y, (int) Math.floor(point.z));
				level.sendParticles(ParticleTypes.FLASH,
						point.x, sy + 1.0, point.z, 1, 0, 0, 0, 0);
				level.sendParticles(ParticleTypes.END_ROD,
						point.x, sy + 1.5, point.z,
						20, v.width * 0.15, 1.4, v.width * 0.15, 0.18);
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
						point.x, sy + 1.0, point.z,
						22, v.width * 0.12, 1.0, v.width * 0.12, 0.10);
				level.sendParticles(ParticleTypes.EXPLOSION,
						point.x, sy + 0.8, point.z, 1, 0, 0, 0, 0);
			}
		}
		p.sweptIndex = Math.max(p.sweptIndex, sliceTo);

		if (sliceTo > sliceFrom) {
			Vec3 frontPoint = origin.add(lookFlat.scale(sliceTo));
			AABB sliceBox = buildSliceAABB(origin, lookFlat, perpendicular, sweepFromDist, sweepToDist, v);
			DamageSource src = level.damageSources().playerAttack(player);
			java.util.List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, sliceBox,
					e -> e != player && e.isAlive() && !e.isSpectator()
							&& !p.hitEntities.contains(e.getUUID())
							&& !(e instanceof Player pp && pp.getUUID().equals(player.getUUID()))
							&& isInSlashPath(e.position(), origin, lookFlat, perpendicular, v));
			for (LivingEntity le : targets) {
				le.invulnerableTime = 0;
				le.hurt(src, v.damage);
				Vec3 push = le.position().subtract(origin);
				double horiz = Math.max(0.01, Math.sqrt(push.x * push.x + push.z * push.z));
				le.setDeltaMovement(push.x / horiz * 1.4, 0.9, push.z / horiz * 1.4);
				le.hurtMarked = true;
				p.hitEntities.add(le.getUUID());
			}

			RandomSource r = level.random;
			float pitchVar = v.pitch * (0.85f + r.nextFloat() * 0.45f);
			level.playSound(null, frontPoint.x, origin.y, frontPoint.z,
					ModSounds.HOMELANDER_HAND_CLAP, SoundSource.PLAYERS, 4.0f, pitchVar);

			if (impactElapsed % Math.max(2, IMPACT_TICKS / 16) == 0) {
				LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
				if (bolt != null) {
					bolt.moveTo(frontPoint.x, origin.y, frontPoint.z);
					bolt.setVisualOnly(true);
					level.addFreshEntity(bolt);
				}
			}
		}

		if (impactElapsed % 4 == 0) {
			Vec3 mid = origin.add(lookFlat.scale(v.length * Math.min(1.0, progress)));
			RandomSource r = level.random;
			level.playSound(null, mid.x, origin.y, mid.z,
					SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
					2.6f, 0.35f + r.nextFloat() * 0.2f);
		}
		if (impactElapsed % 7 == 0) {
			Vec3 mid = origin.add(lookFlat.scale(v.length * Math.min(1.0, progress)));
			level.playSound(null, mid.x, origin.y, mid.z,
					SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE,
					3.0f, 0.4f);
		}
		if (impactElapsed % 11 == 0) {
			level.playSound(null, origin.x, origin.y, origin.z,
					SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE,
					2.0f, 0.45f);
		}
		if (impactElapsed == 0) {
			level.playSound(null, origin.x, origin.y, origin.z,
					ModSounds.HOMELANDER_HAND_CLAP, SoundSource.PLAYERS, 5.0f, v.pitch);
			level.playSound(null, origin.x, origin.y, origin.z,
					SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 4.0f, 0.4f);
		}

		Vec3 shakeCenter = origin.add(lookFlat.scale(v.length * Math.min(1.0, progress)));
		double shakeR = v.shakeRadius;
		float baseIntensity = v.shakeIntensity;
		for (ServerPlayer near : PlayerLookup.around(level, shakeCenter, shakeR)) {
			double dist = near.position().distanceTo(shakeCenter);
			float falloff = (float) Math.max(0.0, 1.0 - dist / shakeR);
			float intensity = Math.max(0.6f, falloff * baseIntensity);
			ServerPlayNetworking.send(near, new ScreenShakeS2CPayload(intensity, 18));
		}
	}

	private static AABB buildSliceAABB(Vec3 origin, Vec3 lookFlat, Vec3 perp,
	                                    double fromDist, double toDist, Variant v) {
		Vec3 a = origin.add(lookFlat.scale(fromDist));
		Vec3 b = origin.add(lookFlat.scale(toDist));
		double widthMargin = v.width;
		double minX = Math.min(a.x, b.x) - widthMargin;
		double maxX = Math.max(a.x, b.x) + widthMargin;
		double minZ = Math.min(a.z, b.z) - widthMargin;
		double maxZ = Math.max(a.z, b.z) + widthMargin;
		double minY = origin.y - v.depth - 2;
		double maxY = origin.y + 6;
		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private static int depthAt(Variant v, int alongIdx, int widthIdx, int halfWidth) {
		double tAlong = (double) alongIdx / Math.max(1, v.length);
		double centerWeight = 1.0 - Math.abs((double) widthIdx) / Math.max(1, halfWidth + 1);
		double profile = Math.sin(Math.PI * tAlong) * 0.55 + 0.45;
		double d = v.depth * profile * (0.5 + 0.5 * centerWeight);
		return Math.max(1, (int) Math.round(d));
	}

	private static int findSurface(ServerLevel level, int x, int originY, int z) {
		for (int y = originY + 6; y >= originY - 16; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (!level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
				return y;
			}
		}
		return originY;
	}

	private static boolean isInSlashPath(Vec3 entityPos, Vec3 origin, Vec3 dir, Vec3 perp, Variant v) {
		Vec3 diff = entityPos.subtract(origin);
		double along = diff.x * dir.x + diff.z * dir.z;
		if (along < -2 || along > v.length + 2) return false;
		double across = Math.abs(diff.x * perp.x + diff.z * perp.z);
		return across < (v.width / 2.0) + 1.5;
	}
}
