package com.example.superheroes.hero;

import com.example.superheroes.ModId;
import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.resource.ResourceKind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class SlendermanHero implements Hero {
	public static final ResourceLocation ID = ModId.of("slenderman");
	public static final ResourceLocation SKIN = ModId.of("textures/entity/hero/slenderman_skin.png");
	public static final ResourceLocation MODEL_TEXTURE = ModId.of("textures/entity/hero/slenderman.png");
	public static final ResourceLocation GEO = ModId.of("geo/entity/slenderman.geo.json");
	public static final ResourceLocation ANIMATION = ModId.of("animations/entity/slenderman.animation.json");

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
		return 0.6f;
	}

	@Override
	public float getManaMax() {
		return 100f;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return switch (pose) {
			case CROUCHING -> EntityDimensions.scalable(0.6f, 1.6f).withEyeHeight(1.35f);
			case SWIMMING, FALL_FLYING, SPIN_ATTACK -> EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f);
			default -> EntityDimensions.scalable(0.6f, 2.1f).withEyeHeight(1.95f);
		};
	}

	@Override
	public List<ResourceLocation> getAbilities() {
		return List.of(
				AbilityIds.SLENDER_BLINK,
				AbilityIds.SLENDER_TENDRILS,
				AbilityIds.SLENDER_PHASE_STALK,
				AbilityIds.SLENDER_STATIC_FIELD);
	}

	@Override
	public ResourceKind getDefaultBinding(ResourceLocation abilityId) {
		if (abilityId.equals(AbilityIds.SLENDER_STATIC_FIELD)) {
			return ResourceKind.MANA;
		}
		return ResourceKind.ENERGY;
	}

	@Override
	public void applyPassives(Player player) {
		HeroAttributes.SLENDERMAN.apply(player);
	}

	@Override
	public void removePassives(Player player) {
		HeroAttributes.SLENDERMAN.remove(player);
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
	public HeroTheme getTheme() {
		return HeroTheme.SLENDERMAN;
	}
}
