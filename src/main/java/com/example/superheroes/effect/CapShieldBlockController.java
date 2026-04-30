package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.CaptainAmericaHero;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CapShieldBlockController {
	private static final Set<UUID> BLOCKING = new HashSet<>();

	private CapShieldBlockController() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			if (!isBlocking(player)) return true;
			HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
			if (!data.hasHero() || !CaptainAmericaHero.ID.equals(data.heroId())) return true;
			Entity attacker = source.getEntity();
			Vec3 toAttacker;
			if (attacker != null) {
				toAttacker = attacker.position().subtract(player.position()).normalize();
			} else if (source.getSourcePosition() != null) {
				toAttacker = source.getSourcePosition().subtract(player.position()).normalize();
			} else {
				return true;
			}
			Vec3 look = player.getLookAngle().normalize();
			double dot = look.x * toAttacker.x + look.z * toAttacker.z;
			if (dot <= 0.1) return true;
			return false;
		});
	}

	public static void setBlocking(ServerPlayer player, boolean blocking) {
		if (blocking) {
			BLOCKING.add(player.getUUID());
		} else {
			BLOCKING.remove(player.getUUID());
		}
	}

	public static boolean isBlocking(ServerPlayer player) {
		return BLOCKING.contains(player.getUUID());
	}
}
