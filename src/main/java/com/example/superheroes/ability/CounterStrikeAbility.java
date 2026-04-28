package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.RegulusMadnessController;
import com.example.superheroes.effect.RegulusMadnessState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class CounterStrikeAbility implements Ability {
	private static final double SEARCH_RANGE = 24.0;

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
		return 200f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		RegulusMadnessState state = player.getAttachedOrCreate(ModAttachments.REGULUS_MADNESS);
		if (!state.madness()) {
			return false;
		}
		LivingEntity target = findTarget(player);
		if (target == null) {
			return false;
		}
		RegulusMadnessController.triggerCounter(player, target);
		return true;
	}

	private static LivingEntity findTarget(ServerPlayer player) {
		LivingEntity last = player.getLastHurtByMob();
		if (last != null && last.isAlive() && last.distanceTo(player) <= SEARCH_RANGE) {
			return last;
		}
		ServerLevel level = player.serverLevel();
		Vec3 eye = player.getEyePosition(1f);
		AABB box = new AABB(eye, eye).inflate(SEARCH_RANGE);
		LivingEntity best = null;
		double bestDist = SEARCH_RANGE * SEARCH_RANGE;
		for (Entity e : level.getEntities(player, box, ent -> ent != player && ent.isAlive()
				&& !ent.isSpectator() && ent instanceof LivingEntity && !(ent instanceof Player))) {
			double d = e.distanceToSqr(player);
			if (d < bestDist) {
				bestDist = d;
				best = (LivingEntity) e;
			}
		}
		return best;
	}
}
