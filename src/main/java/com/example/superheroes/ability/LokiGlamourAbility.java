package com.example.superheroes.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public final class LokiGlamourAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return AbilityIds.LOKI_GLAMOUR;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 0f;
	}

	@Override
	public float costPerTick() {
		return 0.4f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false, true));
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		Vec3 motion = player.getDeltaMovement();
		double speedSq = motion.x * motion.x + motion.z * motion.z;
		if (speedSq < 0.001) {
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false, true));
		} else {
			player.removeEffect(MobEffects.INVISIBILITY);
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		player.removeEffect(MobEffects.INVISIBILITY);
	}
}
