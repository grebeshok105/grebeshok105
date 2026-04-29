package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.DoomsdayHero;
import com.example.superheroes.sound.ModSounds;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DoomsdayFootstepsController {
	private static final int STEP_INTERVAL_TICKS = 12;
	private static final double MOVE_THRESHOLD = 0.04;

	private static final Map<UUID, Integer> NEXT_STEP = new HashMap<>();

	private DoomsdayFootstepsController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		UUID id = player.getUUID();
		if (!data.hasHero() || !DoomsdayHero.ID.equals(data.heroId())) {
			NEXT_STEP.remove(id);
			return;
		}
		if (!player.onGround()) {
			NEXT_STEP.remove(id);
			return;
		}
		Vec3 v = player.getDeltaMovement();
		double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
		if (horiz < MOVE_THRESHOLD) {
			NEXT_STEP.remove(id);
			return;
		}
		Integer next = NEXT_STEP.get(id);
		if (next != null && player.tickCount < next) {
			return;
		}
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.HOMELANDER_IRON_FISTS_CHARGE, SoundSource.PLAYERS, 1.0f, 0.65f);
		NEXT_STEP.put(id, player.tickCount + STEP_INTERVAL_TICKS);
	}
}
