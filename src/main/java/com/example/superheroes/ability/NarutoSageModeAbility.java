package com.example.superheroes.ability;

import com.example.superheroes.ModId;
import com.example.superheroes.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class NarutoSageModeAbility implements Ability {
	public static final ResourceLocation DAMAGE_MODIFIER_ID = ModId.of("modifiers/naruto/sage_mode_damage");
	public static final ResourceLocation SPEED_MODIFIER_ID = ModId.of("modifiers/naruto/sage_mode_speed");
	public static final ResourceLocation ARMOR_MODIFIER_ID = ModId.of("modifiers/naruto/sage_mode_armor");

	private static final double DAMAGE_BONUS = 8.0;
	private static final double SPEED_BONUS = 0.20;
	private static final double ARMOR_BONUS = 8.0;

	private static final Set<UUID> ACTIVE = new HashSet<>();

	@Override
	public ResourceLocation getId() {
		return AbilityIds.NARUTO_SAGE_MODE;
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
		return 0.4f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ACTIVE.add(player.getUUID());
		applyModifiers(player);
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4f, 0.7f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 0.8f);
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.0, player.getZ(),
				40, 0.6, 1.0, 0.6, 0.05);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		ACTIVE.add(player.getUUID());
		ServerLevel level = player.serverLevel();
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 1, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25, 0, true, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.JUMP, 25, 1, true, false, false));

		if (player.tickCount % 3 == 0) {
			level.sendParticles(ModParticles.NARUTO_RASENGAN_SWIRL,
					player.getX(), player.getY() + 1.0, player.getZ(),
					3, 0.4, 0.6, 0.4, 0.02);
		}
		if (player.tickCount % 8 == 0) {
			level.sendParticles(ParticleTypes.END_ROD,
					player.getX(), player.getY() + 0.4, player.getZ(),
					2, 0.5, 0.4, 0.5, 0.01);
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		ACTIVE.remove(player.getUUID());
		removeModifiers(player);
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9f, 0.9f);
	}

	public static boolean isActive(ServerPlayer player) {
		return ACTIVE.contains(player.getUUID());
	}

	public static float damageMultiplier(ServerPlayer player) {
		return isActive(player) ? 1.5f : 1.0f;
	}

	private static void applyModifiers(ServerPlayer player) {
		addOrReplace(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER_ID, DAMAGE_BONUS);
		addOrReplace(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_MODIFIER_ID, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		addOrReplace(player.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID, ARMOR_BONUS);
	}

	private static void removeModifiers(ServerPlayer player) {
		AttributeInstance dmg = player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (dmg != null) dmg.removeModifier(DAMAGE_MODIFIER_ID);
		AttributeInstance spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (spd != null) spd.removeModifier(SPEED_MODIFIER_ID);
		AttributeInstance arm = player.getAttribute(Attributes.ARMOR);
		if (arm != null) arm.removeModifier(ARMOR_MODIFIER_ID);
	}

	private static void addOrReplace(AttributeInstance instance, ResourceLocation id, double amount) {
		addOrReplace(instance, id, amount, AttributeModifier.Operation.ADD_VALUE);
	}

	private static void addOrReplace(AttributeInstance instance, ResourceLocation id, double amount, AttributeModifier.Operation op) {
		if (instance == null) return;
		instance.removeModifier(id);
		instance.addPermanentModifier(new AttributeModifier(id, amount, op));
	}
}
