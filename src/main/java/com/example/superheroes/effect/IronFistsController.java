package com.example.superheroes.effect;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.ability.IronFistsAbility;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.network.ScreenShakeS2CPayload;
import com.example.superheroes.sound.ModSounds;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class IronFistsController {
	private static final int PULL_INTERVAL = 5;
	private static final double PULL_RADIUS = 8.0;
	private static final double PULL_STRENGTH = 0.4;

	private IronFistsController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isSpectator() || player.isCreative()) continue;
				HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
				if (!data.isActive(AbilityIds.IRON_FISTS)) continue;
				if ((player.tickCount % PULL_INTERVAL) != 0) continue;
				autoPull(player);
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

			Vec3 p = target.position().add(0, target.getBbHeight() * 0.5, 0);
			level.sendParticles(ParticleTypes.LARGE_SMOKE,
					p.x, p.y, p.z, 18, 0.3, 0.3, 0.3, 0.05);
			level.sendParticles(ParticleTypes.CRIT,
					p.x, p.y, p.z, 14, 0.3, 0.3, 0.3, 0.2);
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

	private static void autoPull(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 origin = player.position();
		AABB box = new AABB(origin, origin).inflate(PULL_RADIUS);
		List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != player && e.isAlive() && !e.isSpectator() && (e instanceof Enemy || e instanceof Mob));
		LivingEntity nearest = null;
		double bestDist = Double.MAX_VALUE;
		for (LivingEntity e : candidates) {
			double d = e.distanceToSqr(player);
			if (d < bestDist) {
				bestDist = d;
				nearest = e;
			}
		}
		if (nearest == null) return;
		if (nearest.distanceTo(player) < 1.6) return;
		Vec3 toPlayer = player.position().subtract(nearest.position()).normalize().scale(PULL_STRENGTH);
		nearest.push(toPlayer.x, Math.max(0.05, toPlayer.y * 0.5), toPlayer.z);
		nearest.hurtMarked = true;
		nearest.fallDistance = 0f;
		Entity target = nearest;
		level.sendParticles(ParticleTypes.SMOKE,
				target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
				4, 0.2, 0.2, 0.2, 0.0);
	}
}
