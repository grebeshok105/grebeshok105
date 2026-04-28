package com.example.superheroes.hero;

import com.example.superheroes.ModId;
import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.resource.ResourceKind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class RegulusHero implements Hero {
	public static final ResourceLocation ID = ModId.of("regulus");
	public static final ResourceLocation SKIN = ModId.of("textures/entity/hero/regulus.png");

	public static final HeroTheme THEME = new HeroTheme(
			0xFFB8860B,
			0xFF503008,
			0xFFFFD700,
			0x44FFE680,
			0xFFFFD700,
			0xFFB8860B,
			0xFFFFD700,
			0x66FFD700,
			0xFFFFD700,
			0xFF503008,
			0xFFFFEFB0,
			0x66FFD27A,
			0xFF503008,
			0x66FFD27A,
			0xFFFFD700,
			0xFFFFD700,
			0xFFFFF7C8,
			0x66FFD700
	);

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public float getEnergyMax() {
		return 1000f;
	}

	@Override
	public float getEnergyRegenPerTick() {
		return 2.0f;
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
			default -> EntityDimensions.scalable(0.6f, 1.8f).withEyeHeight(1.62f);
		};
	}

	@Override
	public List<ResourceLocation> getAbilities() {
		return List.of(AbilityIds.LION_HEART, AbilityIds.OBJECT_PROJECTILE, AbilityIds.LION_ROAR);
	}

	@Override
	public ResourceKind getDefaultBinding(ResourceLocation abilityId) {
		return ResourceKind.ENERGY;
	}

	@Override
	public void applyPassives(Player player) {
		HeroAttributes.REGULUS.apply(player);
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 0, true, false, true));
	}

	@Override
	public void removePassives(Player player) {
		HeroAttributes.REGULUS.remove(player);
		player.removeEffect(MobEffects.REGENERATION);
	}

	@Override
	public boolean cancelsFallDamage(Player player) {
		return false;
	}

	@Override
	public ResourceLocation getSkinTexture() {
		return SKIN;
	}

	@Override
	public HeroTheme getTheme() {
		return THEME;
	}
}
