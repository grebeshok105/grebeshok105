package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.effect.ThanosGauntletStateController;
import com.example.superheroes.item.infinity.InfinityStoneType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public final class ThanosSnapAbility implements Ability {
	private static final int COOLDOWN_TICKS = 1200;
	private static final double RADIUS = 64.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.THANOS_SNAP;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 350f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		if (AbilityCooldowns.isOnCooldown(player, getId())) {
			return false;
		}
		EnumSet<InfinityStoneType> stones = ThanosGauntletStateController.getCurrentStones(player);
		if (stones.size() < InfinityStoneType.values().length) {
			player.displayClientMessage(
					Component.translatable("ability.superheroes.thanos_snap.gate_failed",
									stones.size(), InfinityStoneType.values().length)
							.withStyle(ChatFormatting.LIGHT_PURPLE),
					true);
			return false;
		}
		return true;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();

		AABB aoe = player.getBoundingBox().inflate(RADIUS, RADIUS, RADIUS);
		int killed = 0;
		for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aoe,
				e -> e != player && e.isAlive() && (e instanceof Enemy) && !(e instanceof Player))) {
			le.hurt(ModDamageTypes.thanosSnap(level, player), Float.MAX_VALUE);
			level.sendParticles(ParticleTypes.ASH,
					le.getX(), le.getY() + le.getBbHeight() / 2, le.getZ(),
					40, 0.5, 0.7, 0.5, 0.05);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					le.getX(), le.getY() + le.getBbHeight() / 2, le.getZ(),
					20, 0.4, 0.6, 0.4, 0.02);
			killed++;
		}

		for (Mob mob : level.getEntitiesOfClass(Mob.class, aoe,
				e -> e.isAlive() && !(e instanceof Enemy))) {
			if (level.random.nextBoolean()) {
				mob.hurt(ModDamageTypes.thanosSnap(level, player), Float.MAX_VALUE);
				level.sendParticles(ParticleTypes.ASH,
						mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
						30, 0.5, 0.7, 0.5, 0.05);
				killed++;
			}
		}

		level.sendParticles(ParticleTypes.FLASH,
				player.getX(), player.getY() + 2.0, player.getZ(), 20, 4.0, 4.0, 4.0, 0.0);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				player.getX(), player.getY() + 1.5, player.getZ(), 200, 5.0, 3.0, 5.0, 0.4);
		level.sendParticles(ParticleTypes.PORTAL,
				player.getX(), player.getY() + 1.5, player.getZ(), 400, 8.0, 4.0, 8.0, 0.6);

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.4f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 0.3f);

		player.displayClientMessage(
				Component.translatable("ability.superheroes.thanos_snap.snapped", killed)
						.withStyle(ChatFormatting.LIGHT_PURPLE),
				false);

		AbilityCooldowns.setCooldownTicks(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
