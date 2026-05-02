package com.example.superheroes.ability;

import com.example.superheroes.hero.HeroAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class KratosSpartanRageAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return AbilityIds.KRATOS_SPARTAN_RAGE;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 30f;
	}

	@Override
	public float costPerTick() {
		return 0.5f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		HeroAttributes.KRATOS_RAGE.apply(player);
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.6f, 0.6f);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		if (player.tickCount % 4 == 0) {
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					player.getX(), player.getY() + 1.0, player.getZ(),
					6, 0.4, 0.6, 0.4, 0.02);
			level.sendParticles(ParticleTypes.FLAME,
					player.getX(), player.getY() + 1.0, player.getZ(),
					3, 0.4, 0.5, 0.4, 0.02);
		}
		if (player.tickCount % 40 == 0) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, false, true));
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		HeroAttributes.KRATOS_RAGE.remove(player);
	}

	public static boolean isActive(ServerPlayer player) {
		return player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null
				&& player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).hasModifier(HeroAttributes.KRATOS_RAGE_DAMAGE);
	}
}
