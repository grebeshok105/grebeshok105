package com.example.superheroes.hero;

import com.example.superheroes.ModId;
import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.physics.ShockwaveUtil;
import com.example.superheroes.resource.ResourceKind;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class CaptainAmericaHero implements Hero {
	public static final ResourceLocation ID = ModId.of("captain_america");
	public static final ResourceLocation SKIN = ModId.of("textures/entity/hero/captain_america.png");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public float getEnergyMax() {
		return 200f;
	}

	@Override
	public float getEnergyRegenPerTick() {
		return 1.4f;
	}

	@Override
	public float getManaMax() {
		return 0f;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return switch (pose) {
			case CROUCHING -> EntityDimensions.scalable(0.6f, 1.5f).withEyeHeight(1.27f);
			case SWIMMING, FALL_FLYING, SPIN_ATTACK -> EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f);
			default -> EntityDimensions.scalable(0.6f, 1.85f).withEyeHeight(1.65f);
		};
	}

	@Override
	public List<ResourceLocation> getAbilities() {
		return List.of(
				AbilityIds.CAP_SHIELD_THROW,
				AbilityIds.CAP_SHIELD_SLAM,
				AbilityIds.CAP_SHIELD_BLOCK
		);
	}

	@Override
	public ResourceKind getDefaultBinding(ResourceLocation abilityId) {
		return ResourceKind.ENERGY;
	}

	@Override
	public void applyPassives(Player player) {
		HeroAttributes.CAPTAIN_AMERICA.apply(player);
	}

	@Override
	public void removePassives(Player player) {
		HeroAttributes.CAPTAIN_AMERICA.remove(player);
	}

	@Override
	public boolean cancelsFallDamage(Player player) {
		return true;
	}

	@Override
	public ResourceLocation getSkinTexture() {
		return SKIN;
	}

	@Override
	public void onLanded(ServerPlayer player, LandingImpact impact) {
		float intensity = impact.intensity();
		if (intensity < 0.15f) return;
		double radius = 2.0 + intensity * 4.0;
		float damage = 1.5f + intensity * 5.0f;
		ShockwaveUtil.detonate(player, player.position(), radius, damage, false);

		ServerLevel level = player.serverLevel();
		double cx = player.getX();
		double cy = player.getY();
		double cz = player.getZ();
		level.playSound(null, cx, cy, cz, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 1.2f, 0.9f);
		level.playSound(null, cx, cy, cz, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.7f, 1.2f);
		level.sendParticles(ParticleTypes.SWEEP_ATTACK, cx, cy + 0.3, cz, 4, radius * 0.3, 0.1, radius * 0.3, 0);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 0.1, cz, 24, radius * 0.4, 0.2, radius * 0.4, 0.05);
	}
}
