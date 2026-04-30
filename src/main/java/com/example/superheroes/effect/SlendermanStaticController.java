package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.hero.SlendermanHero;
import com.example.superheroes.network.SlenderStaticS2CPayload;
import com.example.superheroes.sound.ModSounds;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slenderman "viewer effect": for any player who has line-of-sight on a
 * Slenderman and whose view vector points at him, accumulate "static stacks"
 * regardless of distance. Stacks decay when they look away. Applies status
 * effects + custom audio + per-target HUD overlay to the LOOKER, not to the
 * Slenderman himself.
 */
public final class SlendermanStaticController {
	// Looking-at threshold: dot >= this means viewer is roughly aiming at slender.
	private static final double LOOK_DOT = 0.985; // ~10° cone — needs to actually look
	private static final int SCAN_INTERVAL = 5;
	private static final int DECAY_INTERVAL = 20;
	private static final int MAX_STACKS = 20;

	// looker-uuid -> slender-uuid -> stacks
	private static final Map<UUID, Map<UUID, Integer>> LOOKER_STACKS = new ConcurrentHashMap<>();
	// looker-uuid -> next allowed audio tick (per looker, throttle)
	private static final Map<UUID, Integer> NEXT_AUDIO_TICK = new ConcurrentHashMap<>();

	private SlendermanStaticController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			java.util.List<ServerPlayer> all = server.getPlayerList().getPlayers();
			java.util.List<ServerPlayer> slendermen = new java.util.ArrayList<>();
			for (ServerPlayer p : all) {
				if (isSlenderman(p)) slendermen.add(p);
			}
			for (ServerPlayer looker : all) {
				if (isSlenderman(looker)) continue;
				if (!(looker.tickCount % SCAN_INTERVAL == 0 || looker.tickCount % DECAY_INTERVAL == 0)) {
					continue;
				}
				processLooker(looker, slendermen);
			}
			if (slendermen.isEmpty()) {
				LOOKER_STACKS.clear();
				NEXT_AUDIO_TICK.clear();
			}
		});
	}

	private static boolean isSlenderman(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && SlendermanHero.ID.equals(data.heroId());
	}

	private static void processLooker(ServerPlayer looker, java.util.List<ServerPlayer> slendermen) {
		Map<UUID, Integer> stacks = LOOKER_STACKS.computeIfAbsent(looker.getUUID(), k -> new HashMap<>());
		int max = 0;
		boolean scanning = looker.tickCount % SCAN_INTERVAL == 0;
		boolean decaying = looker.tickCount % DECAY_INTERVAL == 0;

		// Clean up entries for slendermen no longer present.
		java.util.Set<UUID> present = new java.util.HashSet<>();
		for (ServerPlayer sp : slendermen) present.add(sp.getUUID());
		stacks.keySet().removeIf(k -> !present.contains(k));

		for (ServerPlayer slender : slendermen) {
			if (slender.level() != looker.level()) {
				stacks.remove(slender.getUUID());
				continue;
			}
			boolean looking = isLookingAt(looker, slender);
			int s = stacks.getOrDefault(slender.getUUID(), 0);
			if (looking && scanning) {
				s = Math.min(MAX_STACKS, s + 1);
				stacks.put(slender.getUUID(), s);
				applyEffects(looker, s);
				if (s >= MAX_STACKS) {
					ServerLevel level = looker.serverLevel();
					looker.hurt(ModDamageTypes.slendermanStatic(level, slender), 8.0f);
					level.sendParticles(ParticleTypes.SQUID_INK, looker.getX(),
							looker.getY() + looker.getBbHeight() * 0.5, looker.getZ(),
							20, 0.4, 0.4, 0.4, 0.05);
					stacks.put(slender.getUUID(), 15);
					s = 15;
				}
			} else if (!looking && decaying) {
				s = s - 1;
				if (s <= 0) {
					stacks.remove(slender.getUUID());
					continue;
				}
				stacks.put(slender.getUUID(), s);
			}
			if (s > max) max = s;
		}

		if (looker.tickCount % 5 == 0) {
			float fade = Math.min(1f, max / (float) MAX_STACKS);
			ServerPlayNetworking.send(looker, new SlenderStaticS2CPayload(max, fade));
		}

		// Audio cues at the looker's location: heard by the looker AND nearby
		// players (so a 3rd-party witness also hears the dread). Throttled.
		if (max >= 5 && scanning) {
			int next = NEXT_AUDIO_TICK.getOrDefault(looker.getUUID(), 0);
			if (looker.tickCount >= next) {
				ServerLevel level = looker.serverLevel();
				if (max >= 15) {
					level.playSound(null, looker.getX(), looker.getY(), looker.getZ(),
							ModSounds.SLENDERMAN_STATIC, SoundSource.HOSTILE, 1.4f, 1.0f);
					level.playSound(null, looker.getX(), looker.getY(), looker.getZ(),
							ModSounds.SLENDERMAN_HUNT, SoundSource.HOSTILE, 1.0f, 1.0f);
					NEXT_AUDIO_TICK.put(looker.getUUID(), looker.tickCount + 50);
				} else if (max >= 10) {
					level.playSound(null, looker.getX(), looker.getY(), looker.getZ(),
							ModSounds.SLENDERMAN_STATIC, SoundSource.HOSTILE, 1.0f, 1.0f);
					level.playSound(null, looker.getX(), looker.getY(), looker.getZ(),
							ModSounds.SLENDERMAN_WARNING, SoundSource.HOSTILE, 0.9f, 1.0f);
					NEXT_AUDIO_TICK.put(looker.getUUID(), looker.tickCount + 80);
				} else {
					level.playSound(null, looker.getX(), looker.getY(), looker.getZ(),
							ModSounds.SLENDERMAN_STATIC, SoundSource.HOSTILE, 0.7f, 1.0f);
					NEXT_AUDIO_TICK.put(looker.getUUID(), looker.tickCount + 120);
				}
			}
		}
	}

	private static boolean isLookingAt(ServerPlayer looker, ServerPlayer slender) {
		if (looker.isSpectator()) return false;
		Vec3 lookerEye = looker.getEyePosition();
		Vec3 slenderTorso = slender.position().add(0, slender.getBbHeight() * 0.6, 0);
		Vec3 dir = slenderTorso.subtract(lookerEye);
		double len = dir.length();
		if (len < 0.001) return false;
		Vec3 view = looker.getViewVector(1f).normalize();
		double dot = dir.scale(1.0 / len).dot(view);
		if (dot < LOOK_DOT) return false;
		// No occlusion check at infinite distance: requirement is "infinite range".
		// We only require the looker's aim to be on the slender's torso direction.
		return true;
	}

	private static void applyEffects(LivingEntity target, int stacks) {
		if (stacks >= 5 && stacks < 10) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, true, false, false));
		} else if (stacks >= 10 && stacks < 15) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 1, true, false, false));
		} else if (stacks >= 15) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 2, true, false, false));
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, true, false, false));
		}
	}

	public static boolean isExposed(ServerPlayer slender) {
		return false;
	}

	public static void clear(UUID id) {
		LOOKER_STACKS.remove(id);
		NEXT_AUDIO_TICK.remove(id);
		// Also any entry referencing this id as slender
		for (Map<UUID, Integer> m : LOOKER_STACKS.values()) {
			m.remove(id);
		}
	}
}
