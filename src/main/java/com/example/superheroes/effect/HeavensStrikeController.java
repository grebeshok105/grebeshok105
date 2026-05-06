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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HeavensStrikeController {
	public static final int WINDUP_TICKS = 80;
	private static final int FREEZE_REFRESH_TICKS = 18;

	public record Variant(int length, int width, int depth, float shakeIntensity,
	                      float damage, float pitch, double shakeRadius) {
		public static final Variant REINHARD = new Variant(50, 6, 12, 6.0f, 1500f, 0.55f, 100.0);
		public static final Variant RAIDEN = new Variant(30, 4, 6, 3.5f, 60f, 0.78f, 60.0);
	}

	public record Pending(Vec3 origin, Vec3 lookFlat, long startTick, long impactTick,
	                      Variant variant, Vec3 lockPos, float lockYaw, float lockPitch) {}

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
		PENDING.put(player.getUUID(), new Pending(origin, flat, now, now + WINDUP_TICKS, variant,
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
				enforceLock(player, p, now);
				if (now >= p.impactTick) {
					clearFreeze(player);
					impact(player, p);
					it.remove();
				} else {
					windupTick(player, p, now);
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

		double traceLength = v.length * Math.min(1.0, progress * 1.2);
		int previewSteps = Math.max(8, (int) (traceLength / 2.0));
		for (int i = 1; i <= previewSteps; i++) {
			double t = (double) i / previewSteps;
			double dist = traceLength * t;
			double px = p.origin.x + p.lookFlat.x * dist;
			double pz = p.origin.z + p.lookFlat.z * dist;
			double py = p.origin.y + 1.0 + Math.sin((now * 0.4) + i * 0.6) * 0.3;
			level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.04, 0.05, 0.04, 0.0);
			if (i % 3 == 0) {
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py - 0.4, pz, 1, 0.1, 0.0, 0.1, 0.0);
			}
		}

		Vec3 endPoint = p.origin.add(p.lookFlat.scale(v.length));
		if (now % 4 == 0) {
			level.sendParticles(ParticleTypes.FLASH,
					endPoint.x, endPoint.y + 1.5, endPoint.z, 1, 0, 0, 0, 0);
			level.sendParticles(ParticleTypes.END_ROD,
					endPoint.x, endPoint.y + 1.0, endPoint.z,
					6, 0.4, 0.6, 0.4, 0.05);
		}

		if (now % 6 == 0) {
			for (int a = 0; a < 8; a++) {
				double ang = (Math.PI * 2 * a) / 8 + (now * 0.05);
				double rad = 1.6;
				double rx = p.origin.x + Math.cos(ang) * rad;
				double rz = p.origin.z + Math.sin(ang) * rad;
				level.sendParticles(ParticleTypes.END_ROD,
						rx, p.origin.y + 0.05, rz, 1, 0, 0.04, 0, 0.0);
			}
		}

		if (now % 10 == 0) {
			level.playSound(null, p.origin.x, p.origin.y, p.origin.z,
					SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
					1.4f, 0.4f + (float) progress * 0.8f);
		}

		if (ticksDone == WINDUP_TICKS / 2) {
			level.playSound(null, p.origin.x, p.origin.y, p.origin.z,
					SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 2.0f, 0.5f);
		}

		if (ticksDone == WINDUP_TICKS - 5) {
			for (int i = 0; i < 4; i++) {
				double dist = (i + 1) * (v.length / 5.0);
				Vec3 spot = p.origin.add(p.lookFlat.scale(dist));
				level.sendParticles(ParticleTypes.FLASH,
						spot.x, spot.y + 1.0, spot.z, 1, 0, 0, 0, 0);
			}
		}

		Vec3 jitter = p.origin;
		level.sendParticles(ParticleTypes.LARGE_SMOKE,
				jitter.x + (r.nextDouble() - 0.5) * 1.6,
				jitter.y + 0.2 + r.nextDouble() * 0.6,
				jitter.z + (r.nextDouble() - 0.5) * 1.6,
				1, 0, 0, 0, 0.0);
	}

	private static void impact(ServerPlayer player, Pending p) {
		ServerLevel level = player.serverLevel();
		Variant v = p.variant;
		Vec3 origin = p.origin;
		Vec3 lookFlat = p.lookFlat;
		Vec3 perpendicular = new Vec3(-lookFlat.z, 0, lookFlat.x);
		int halfWidth = v.width / 2;

		for (int i = 1; i <= v.length; i++) {
			Vec3 point = origin.add(lookFlat.scale(i));
			for (int w = -halfWidth; w <= halfWidth; w++) {
				Vec3 offset = perpendicular.scale(w);
				int ox = (int) Math.floor(point.x + offset.x);
				int oz = (int) Math.floor(point.z + offset.z);
				int sy = findSurface(level, ox, (int) origin.y, oz);
				int localDepth = depthAt(v, i, w, halfWidth);

				for (int dy = 0; dy < localDepth; dy++) {
					BlockPos pos = new BlockPos(ox, sy - dy, oz);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) continue;
					if (UNBREAKABLE.contains(state.getBlock())) continue;
					if (state.getDestroySpeed(level, pos) < 0) continue;
					level.destroyBlock(pos, false);
				}
			}

			if (i % 4 == 0) {
				int sy = findSurface(level, (int) Math.floor(point.x), (int) origin.y, (int) Math.floor(point.z));
				level.sendParticles(ParticleTypes.FLASH,
						point.x, sy + 1.0, point.z, 1, 0, 0, 0, 0);
				level.sendParticles(ParticleTypes.END_ROD,
						point.x, sy + 1.5, point.z,
						16, 1.0, 1.2, 1.0, 0.15);
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
						point.x, sy + 1.0, point.z,
						18, 0.8, 1.0, 0.8, 0.10);
				level.sendParticles(ParticleTypes.EXPLOSION,
						point.x, sy + 0.8, point.z, 1, 0, 0, 0, 0);
			}
		}

		Vec3 endPoint = origin.add(lookFlat.scale(v.length));
		AABB slashBox = new AABB(
				Math.min(origin.x, endPoint.x) - v.width, origin.y - v.depth - 2, Math.min(origin.z, endPoint.z) - v.width,
				Math.max(origin.x, endPoint.x) + v.width, origin.y + 6, Math.max(origin.z, endPoint.z) + v.width
		);
		DamageSource src = level.damageSources().playerAttack(player);
		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, slashBox,
				e -> e != player && e.isAlive() && !e.isSpectator()
						&& !(e instanceof Player pp && pp.getUUID().equals(player.getUUID()))
						&& isInSlashPath(e.position(), origin, lookFlat, perpendicular, v));
		for (LivingEntity le : targets) {
			le.invulnerableTime = 0;
			le.hurt(src, v.damage);
			Vec3 push = le.position().subtract(origin);
			double horiz = Math.max(0.01, Math.sqrt(push.x * push.x + push.z * push.z));
			le.setDeltaMovement(push.x / horiz * 1.2, 0.7, push.z / horiz * 1.2);
			le.hurtMarked = true;
		}

		int boltCount = Math.max(3, v.length / 12);
		for (int n = 0; n < boltCount; n++) {
			LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
			if (bolt != null) {
				double dist = (n + 1) * (v.length / (double) (boltCount + 1));
				Vec3 spot = origin.add(lookFlat.scale(dist));
				bolt.moveTo(spot.x, spot.y, spot.z);
				bolt.setVisualOnly(true);
				level.addFreshEntity(bolt);
			}
		}

		level.playSound(null, origin.x, origin.y, origin.z, ModSounds.HOMELANDER_HAND_CLAP,
				SoundSource.PLAYERS, 4.0f, v.pitch);
		level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.PLAYERS, 4.0f, 0.4f);
		level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.LIGHTNING_BOLT_THUNDER,
				SoundSource.WEATHER, 3.5f, 0.5f);
		Vec3 mid = origin.add(lookFlat.scale(v.length * 0.5));
		level.playSound(null, mid.x, mid.y, mid.z, SoundEvents.LIGHTNING_BOLT_IMPACT,
				SoundSource.PLAYERS, 3.0f, 0.5f);
		level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.NETHERITE_BLOCK_HIT,
				SoundSource.PLAYERS, 3.0f, 0.4f);

		double shakeFrom = origin.x + lookFlat.x * (v.length * 0.5);
		double shakeFromZ = origin.z + lookFlat.z * (v.length * 0.5);
		Vec3 shakeCenter = new Vec3(shakeFrom, origin.y, shakeFromZ);
		double shakeR = v.shakeRadius;
		for (ServerPlayer near : PlayerLookup.around(level, shakeCenter, shakeR)) {
			double dist = near.position().distanceTo(shakeCenter);
			float falloff = (float) Math.max(0.0, 1.0 - dist / shakeR);
			float intensity = Math.max(0.4f, falloff * v.shakeIntensity);
			ServerPlayNetworking.send(near, new ScreenShakeS2CPayload(intensity, 32));
		}
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
