package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.RaidenState;
import com.example.superheroes.particle.ModParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Plunging Strike — активная. Прыжок-импульс ввысь + 3 секунды «armed»: следующее
 * приземление наносит удар грома по AoE 5 блоков (10 dmg игрокам, 5 мобам).
 * Стоимость 200 энергии, КД 8 секунд.
 */
public final class RaidenPlungingStrikeAbility implements Ability {
	private static final float COST = 200f;
	private static final int COOLDOWN_TICKS = 8 * 20;
	private static final int ARMED_TICKS = 3 * 20;
	private static final double LAUNCH_VY = 1.4;
	private static final double LAUNCH_FORWARD = 0.4;
	private static final double SLAM_RADIUS = 5.0;
	private static final float SLAM_DAMAGE_PLAYER = 10f;
	private static final float SLAM_DAMAGE_MOB = 5f;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.RAIDEN_PLUNGING_STRIKE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return COST;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		if (AbilityCooldowns.isOnCooldown(player, getId())) {
			player.displayClientMessage(
					Component.translatable("ability.superheroes.raiden_plunging_strike.cooldown"), true);
			return false;
		}
		return true;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		long now = player.serverLevel().getGameTime();
		RaidenState state = player.getAttachedOrCreate(ModAttachments.RAIDEN_STATE);
		player.setAttached(ModAttachments.RAIDEN_STATE,
				state.withPlungingArmedUntilTick(now + ARMED_TICKS));

		Vec3 look = player.getLookAngle().normalize();
		Vec3 boost = new Vec3(look.x * LAUNCH_FORWARD, LAUNCH_VY, look.z * LAUNCH_FORWARD);
		player.setDeltaMovement(player.getDeltaMovement().x * 0.2 + boost.x,
				boost.y, player.getDeltaMovement().z * 0.2 + boost.z);
		player.hurtMarked = true;
		player.fallDistance = 0f;

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);

		ServerLevel level = player.serverLevel();
		level.sendParticles(ModParticles.JIWALD_EFFECT,
				player.getX(), player.getY() + 0.2, player.getZ(),
				40, 0.5, 0.1, 0.5, 0.4);
		level.sendParticles(ModParticles.BLUE_FLAME,
				player.getX(), player.getY() + 0.5, player.getZ(),
				18, 0.4, 0.4, 0.4, 0.02);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.7f, 1.5f);
		return true;
	}

	public static void onLanding(ServerPlayer player) {
		long now = player.serverLevel().getGameTime();
		RaidenState state = player.getAttachedOrCreate(ModAttachments.RAIDEN_STATE);
		if (state.plungingArmedUntilTick() <= now) return;
		player.setAttached(ModAttachments.RAIDEN_STATE, state.withPlungingArmedUntilTick(0L));

		ServerLevel level = player.serverLevel();
		Vec3 origin = player.position();
		double r2 = SLAM_RADIUS * SLAM_RADIUS;
		AABB box = new AABB(
				origin.x - SLAM_RADIUS, origin.y - 1, origin.z - SLAM_RADIUS,
				origin.x + SLAM_RADIUS, origin.y + 3, origin.z + SLAM_RADIUS);
		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != player && e.isAlive() && !e.isSpectator()
						&& !(e instanceof Player p && p.getUUID().equals(player.getUUID()))
						&& e.position().distanceToSqr(origin) <= r2);
		for (LivingEntity le : targets) {
			float dmg = (le instanceof Player) ? SLAM_DAMAGE_PLAYER : SLAM_DAMAGE_MOB;
			le.invulnerableTime = 0;
			le.hurt(level.damageSources().playerAttack(player), dmg);
			Vec3 push = le.position().subtract(origin);
			double horiz = Math.max(0.01, Math.sqrt(push.x * push.x + push.z * push.z));
			le.setDeltaMovement(push.x / horiz * 0.5, 0.45, push.z / horiz * 0.5);
			le.hurtMarked = true;
		}

		level.sendParticles(ModParticles.WHITE_BOOM,
				origin.x, origin.y + 0.2, origin.z,
				1, 0, 0, 0, 0);
		level.sendParticles(ModParticles.SWORD_EXPLOSION,
				origin.x, origin.y + 0.5, origin.z,
				36, 1.2, 0.4, 1.2, 0.2);
		level.sendParticles(ModParticles.JIWALD_EFFECT,
				origin.x, origin.y + 0.5, origin.z,
				120, 1.5, 0.4, 1.5, 0.5);
		level.sendParticles(ModParticles.MOONVEIL,
				origin.x, origin.y + 1.0, origin.z,
				14, 0.8, 0.4, 0.8, 0.05);
		level.playSound(null, origin.x, origin.y, origin.z,
				SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.4f, 0.7f);
	}
}
