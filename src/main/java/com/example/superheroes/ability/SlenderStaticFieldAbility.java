package com.example.superheroes.ability;

import com.example.superheroes.effect.SlendermanFieldController;
import com.example.superheroes.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

public final class SlenderStaticFieldAbility implements Ability {
	private static final int COOLDOWN_TICKS = 300;
	public static final int FIELD_TICKS = 240;
	public static final double FIELD_RADIUS = 18.0;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.SLENDER_STATIC_FIELD;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 90f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return !AbilityCooldowns.isOnCooldown(player, AbilityIds.SLENDER_STATIC_FIELD);
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		SlendermanFieldController.startField(player, FIELD_TICKS, FIELD_RADIUS);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.SLENDERMAN_JUMPSCARE, SoundSource.PLAYERS, 1.4f, 0.85f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.SLENDERMAN_ANGRY, SoundSource.PLAYERS, 1.0f, 1.0f);
		AbilityCooldowns.setCooldownTicks(player, AbilityIds.SLENDER_STATIC_FIELD, COOLDOWN_TICKS);
		return true;
	}
}
