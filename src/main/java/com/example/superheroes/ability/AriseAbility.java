package com.example.superheroes.ability;

import com.example.superheroes.effect.SungJinwooController;
import com.example.superheroes.entity.ShadowSoldierEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Arise — поднимает Теневого Солдата из ближайшего ослабленного врага в радиусе 8 блоков.
 *
 *  - Cтоимость: 20 ENERGY (Shadow Charges)
 *  - Cooldown: 40 тиков (2с)
 *  - Не работает если уже 10 живых теней (нужно сначала использовать Sacrifice / тень должна умереть).
 *  - Лечит цель в 0 HP (исполняет) и тут же спавнит Теневого Солдата на её месте.
 */
public final class AriseAbility implements Ability {
	private static final double RANGE = 8.0;
	private static final int COOLDOWN_TICKS = 40;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.ARISE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 20f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return SungJinwooController.aliveCount(player) < SungJinwooController.MAX_SHADOWS
				&& findCandidate(player) != null;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		LivingEntity target = findCandidate(player);
		if (target == null) return false;

		ServerLevel level = player.serverLevel();
		Vec3 pos = target.position();

		// Исполняем цель и спавним тень на её месте.
		target.kill();
		ShadowSoldierEntity shadow = SungJinwooController.spawnOneShadowAt(level, player, pos.add(0, 0.5, 0));
		if (shadow != null) {
			SungJinwooController.registerExtraShadow(player, shadow);
		}

		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + 1, pos.z, 40, 0.5, 1.0, 0.5, 0.05);
		level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1, pos.z, 60, 0.6, 1.0, 0.6, 0.6);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.7f, 1.6f);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.7f);

		AbilityCooldowns.setCooldownTicks(player, AbilityIds.ARISE, COOLDOWN_TICKS);
		return true;
	}

	private static LivingEntity findCandidate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		AABB box = player.getBoundingBox().inflate(RANGE);
		List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != player && e.isAlive() && !(e instanceof Player) && !(e instanceof ShadowSoldierEntity));
		// Самый «ослабленный» (самая малая доля HP) — раньше всех «умрёт» от приговора.
		return candidates.stream()
				.min(Comparator.comparingDouble(e -> e.getHealth() / e.getMaxHealth()))
				.orElse(null);
	}
}
