package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.effect.ModEffects;
import com.example.superheroes.hero.Hero;
import com.example.superheroes.hero.Heroes;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.particle.ModParticles;
import com.example.superheroes.transform.HeroData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EyeLasersAbility implements Ability {
	private static final double RANGE = 64.0;
	private static final float MIN_DPS = 14.0f;
	private static final float MAX_DPS = 30.0f;
	private static final float MADNESS_DAMAGE_MUL = 3.0f;
	private static final double CHEST_FRACTION = 0.7;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.EYE_LASERS;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 4.0f;
	}

	@Override
	public float costPerTick() {
		return 1.5f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6f, 1.8f);
		fireBeam(player);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		fireBeam(player);
		if (player.tickCount % 6 == 0) {
			player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.35f, 1.6f);
		}
	}

	private static void fireBeam(ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1f);
		Vec3 end = eye.add(dir.scale(RANGE));
		ServerLevel level = player.serverLevel();
		boolean madness = ModEffects.isMadness(player);
		BlockHitResult blockHit = level.clip(new ClipContext(
				eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 entitySearchEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;
		AABB box = player.getBoundingBox().expandTowards(dir.scale(RANGE)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(
				level, player, eye, entitySearchEnd, box,
				e -> e instanceof LivingEntity && e.isAlive() && e != player && !e.isSpectator());
		Vec3 actualEnd = entitySearchEnd;
		float damage = damagePerTick(player) * (madness ? MADNESS_DAMAGE_MUL : 1f);
		if (hit != null) {
			LivingEntity target = (LivingEntity) hit.getEntity();
			target.hurt(ModDamageTypes.eyeLaser(level, player), damage);
			actualEnd = new Vec3(target.getX(), target.getY() + target.getBbHeight() * CHEST_FRACTION, target.getZ());
			level.sendParticles(ModParticles.LASER_SPARK,
					actualEnd.x, actualEnd.y, actualEnd.z,
					3, 0.10, 0.10, 0.10, 0.04);
			if (madness) {
				if (player.tickCount % 2 == 0) {
					level.explode(player, actualEnd.x, actualEnd.y, actualEnd.z,
							2.4f, true, Level.ExplosionInteraction.MOB);
					target.igniteForSeconds(8f);
				}
				placeFireRing(level, actualEnd, 3);
			}
		} else if (madness && blockHit.getType() == HitResult.Type.BLOCK) {
			if (player.tickCount % 2 == 0) {
				level.explode(player, actualEnd.x, actualEnd.y, actualEnd.z,
						2.0f, true, Level.ExplosionInteraction.MOB);
			}
			placeFireRing(level, actualEnd, 3);
		}
		ModNetworking.broadcastLaser(player, eye, actualEnd);
	}

	private static void placeFireRing(ServerLevel level, Vec3 center, int radius) {
		BlockPos centerPos = BlockPos.containing(center);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				for (int dy = -1; dy <= 1; dy++) {
					BlockPos pos = centerPos.offset(dx, dy, dz);
					if (!level.getBlockState(pos).isAir()) {
						continue;
					}
					BlockPos below = pos.below();
					if (BaseFireBlock.canBePlacedAt(level, pos, net.minecraft.core.Direction.UP)
							&& !level.getBlockState(below).isAir()) {
						level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
					}
				}
			}
		}
	}

	private static float damagePerTick(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		float frac = 0f;
		if (data.hasHero()) {
			Hero hero = Heroes.get(data.heroId());
			if (hero != null && hero.getManaMax() > 0f) {
				frac = Math.max(0f, Math.min(1f, data.mana() / hero.getManaMax()));
			}
		}
		float dps = MIN_DPS + (MAX_DPS - MIN_DPS) * frac;
		return dps / 20f;
	}
}
