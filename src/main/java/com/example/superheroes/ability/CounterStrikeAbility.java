package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.RegulusMadnessController;
import com.example.superheroes.effect.RegulusMadnessState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CounterStrikeAbility implements Ability {
	private static final double RANGE = 16.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.COUNTER_STRIKE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 80f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
		if (!state.madness()) {
			player.displayClientMessage(Component.translatable("ability.superheroes.counter_strike.unavailable"), true);
			return false;
		}
		LivingEntity target = findTarget(player);
		if (target == null) {
			player.displayClientMessage(Component.translatable("ability.superheroes.counter_strike.no_target"), true);
			return false;
		}
		RegulusMadnessController.executeCounter(player, target);
		return true;
	}

	private static LivingEntity findTarget(ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1.0f);
		AABB box = new AABB(eye, eye.add(dir.scale(RANGE))).inflate(3.0);
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, box, le -> le != player && le.isAlive())) {
			Vec3 toE = e.getEyePosition().subtract(eye);
			if (toE.lengthSqr() > RANGE * RANGE) continue;
			double dot = toE.normalize().dot(dir);
			if (dot < 0.4) continue;
			double d = e.distanceToSqr(player);
			if (d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		return best;
	}
}
