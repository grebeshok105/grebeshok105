package com.example.superheroes.hero;

import com.example.superheroes.ModId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class HeroAttributes {
	public static final ResourceLocation HOMELANDER_ARMOR = ModId.of("modifiers/homelander/armor");
	public static final ResourceLocation HOMELANDER_TOUGHNESS = ModId.of("modifiers/homelander/toughness");
	public static final ResourceLocation HOMELANDER_DAMAGE = ModId.of("modifiers/homelander/damage");
	public static final ResourceLocation HOMELANDER_SPEED = ModId.of("modifiers/homelander/speed");
	public static final ResourceLocation HOMELANDER_HP = ModId.of("modifiers/homelander/max_health");
	public static final ResourceLocation HOMELANDER_KNOCKBACK = ModId.of("modifiers/homelander/knockback_resistance");

	public static final ResourceLocation IRON_MAN_ARMOR = ModId.of("modifiers/iron_man/armor");
	public static final ResourceLocation IRON_MAN_TOUGHNESS = ModId.of("modifiers/iron_man/toughness");
	public static final ResourceLocation IRON_MAN_DAMAGE = ModId.of("modifiers/iron_man/damage");
	public static final ResourceLocation IRON_MAN_SPEED = ModId.of("modifiers/iron_man/speed");
	public static final ResourceLocation IRON_MAN_KNOCKBACK = ModId.of("modifiers/iron_man/knockback_resistance");

	public static final ResourceLocation REGULUS_ARMOR = ModId.of("modifiers/regulus/armor");
	public static final ResourceLocation REGULUS_MADNESS_ARMOR = ModId.of("modifiers/regulus/madness_armor");
	public static final ResourceLocation REGULUS_MADNESS_HP = ModId.of("modifiers/regulus/madness_max_health");
	public static final ResourceLocation REGULUS_MADNESS_DAMAGE = ModId.of("modifiers/regulus/madness_damage");

	public static final ResourceLocation SUNG_ARMOR = ModId.of("modifiers/sung_jinwoo/armor");
	public static final ResourceLocation SUNG_TOUGHNESS = ModId.of("modifiers/sung_jinwoo/toughness");
	public static final ResourceLocation SUNG_DAMAGE = ModId.of("modifiers/sung_jinwoo/damage");
	public static final ResourceLocation SUNG_SPEED = ModId.of("modifiers/sung_jinwoo/speed");
	public static final ResourceLocation SUNG_KNOCKBACK = ModId.of("modifiers/sung_jinwoo/knockback_resistance");
	public static final ResourceLocation SUNG_ATTACK_SPEED = ModId.of("modifiers/sung_jinwoo/attack_speed");

	public static final ResourceLocation DOOMSDAY_ARMOR = ModId.of("modifiers/doomsday/armor");
	public static final ResourceLocation DOOMSDAY_TOUGHNESS = ModId.of("modifiers/doomsday/toughness");
	public static final ResourceLocation DOOMSDAY_DAMAGE = ModId.of("modifiers/doomsday/damage");
	public static final ResourceLocation DOOMSDAY_SPEED = ModId.of("modifiers/doomsday/speed");
	public static final ResourceLocation DOOMSDAY_HP = ModId.of("modifiers/doomsday/max_health");
	public static final ResourceLocation DOOMSDAY_KNOCKBACK = ModId.of("modifiers/doomsday/knockback_resistance");
	public static final ResourceLocation DOOMSDAY_SCALE = ModId.of("modifiers/doomsday/scale");
	public static final ResourceLocation DOOMSDAY_REACH = ModId.of("modifiers/doomsday/entity_reach");
	public static final ResourceLocation DOOMSDAY_BLOCK_REACH = ModId.of("modifiers/doomsday/block_reach");
	public static final ResourceLocation DOOMSDAY_STEP = ModId.of("modifiers/doomsday/step_height");
	public static final ResourceLocation DOOMSDAY_JUMP = ModId.of("modifiers/doomsday/jump_strength");
	public static final ResourceLocation DOOMSDAY_BERSERK_DAMAGE = ModId.of("modifiers/doomsday/berserk_damage");
	public static final ResourceLocation DOOMSDAY_BERSERK_ARMOR = ModId.of("modifiers/doomsday/berserk_armor");
	public static final ResourceLocation DOOMSDAY_BERSERK_SPEED = ModId.of("modifiers/doomsday/berserk_speed");
	public static final ResourceLocation DOOMSDAY_ADAPT_DAMAGE = ModId.of("modifiers/doomsday/adapt_damage");

	public static final ResourceLocation GOKU_ARMOR = ModId.of("modifiers/goku/armor");
	public static final ResourceLocation GOKU_TOUGHNESS = ModId.of("modifiers/goku/toughness");
	public static final ResourceLocation GOKU_DAMAGE = ModId.of("modifiers/goku/damage");
	public static final ResourceLocation GOKU_SPEED = ModId.of("modifiers/goku/speed");
	public static final ResourceLocation GOKU_HP = ModId.of("modifiers/goku/max_health");
	public static final ResourceLocation GOKU_KNOCKBACK = ModId.of("modifiers/goku/knockback_resistance");
	public static final ResourceLocation GOKU_ATTACK_SPEED = ModId.of("modifiers/goku/attack_speed");
	public static final ResourceLocation GOKU_JUMP = ModId.of("modifiers/goku/jump_strength");
	public static final ResourceLocation GOKU_STEP = ModId.of("modifiers/goku/step_height");
	public static final ResourceLocation GOKU_REACH = ModId.of("modifiers/goku/entity_reach");

	public static final ResourceLocation NARUTO_ARMOR = ModId.of("modifiers/naruto/armor");
	public static final ResourceLocation NARUTO_TOUGHNESS = ModId.of("modifiers/naruto/toughness");
	public static final ResourceLocation NARUTO_DAMAGE = ModId.of("modifiers/naruto/damage");
	public static final ResourceLocation NARUTO_SPEED = ModId.of("modifiers/naruto/speed");
	public static final ResourceLocation NARUTO_ATTACK_SPEED = ModId.of("modifiers/naruto/attack_speed");
	public static final ResourceLocation NARUTO_HP = ModId.of("modifiers/naruto/max_health");
	public static final ResourceLocation NARUTO_KNOCKBACK = ModId.of("modifiers/naruto/knockback_resistance");
	public static final ResourceLocation NARUTO_JUMP = ModId.of("modifiers/naruto/jump_strength");
	public static final ResourceLocation NARUTO_STEP = ModId.of("modifiers/naruto/step_height");

	public static final ResourceLocation CAP_ARMOR = ModId.of("modifiers/captain_america/armor");
	public static final ResourceLocation CAP_TOUGHNESS = ModId.of("modifiers/captain_america/toughness");
	public static final ResourceLocation CAP_DAMAGE = ModId.of("modifiers/captain_america/damage");
	public static final ResourceLocation CAP_HP = ModId.of("modifiers/captain_america/max_health");
	public static final ResourceLocation CAP_KNOCKBACK = ModId.of("modifiers/captain_america/knockback_resistance");

	public static final AttributeModifierSet HOMELANDER = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, HOMELANDER_ARMOR, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, HOMELANDER_TOUGHNESS, 8.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, HOMELANDER_DAMAGE, 6.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MOVEMENT_SPEED, HOMELANDER_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.MAX_HEALTH, HOMELANDER_HP, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.KNOCKBACK_RESISTANCE, HOMELANDER_KNOCKBACK, 1.0, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet IRON_MAN = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, IRON_MAN_ARMOR, 25.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, IRON_MAN_TOUGHNESS, 4.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, IRON_MAN_DAMAGE, 4.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MOVEMENT_SPEED, IRON_MAN_SPEED, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.KNOCKBACK_RESISTANCE, IRON_MAN_KNOCKBACK, 0.6, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet REGULUS = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, REGULUS_ARMOR, 15.0, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet REGULUS_MADNESS = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, REGULUS_MADNESS_ARMOR, 10.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MAX_HEALTH, REGULUS_MADNESS_HP, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.ATTACK_DAMAGE, REGULUS_MADNESS_DAMAGE, 0.40, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet SUNG_JINWOO = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, SUNG_ARMOR, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, SUNG_TOUGHNESS, 8.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, SUNG_DAMAGE, 4.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MOVEMENT_SPEED, SUNG_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.KNOCKBACK_RESISTANCE, SUNG_KNOCKBACK, 0.3, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_SPEED, SUNG_ATTACK_SPEED, 1.5, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet DOOMSDAY = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, DOOMSDAY_ARMOR, 30.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, DOOMSDAY_TOUGHNESS, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, DOOMSDAY_DAMAGE, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MOVEMENT_SPEED, DOOMSDAY_SPEED, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.MAX_HEALTH, DOOMSDAY_HP, 80.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.KNOCKBACK_RESISTANCE, DOOMSDAY_KNOCKBACK, 1.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.SCALE, DOOMSDAY_SCALE, 1.2, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ENTITY_INTERACTION_RANGE, DOOMSDAY_REACH, 1.5, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.BLOCK_INTERACTION_RANGE, DOOMSDAY_BLOCK_REACH, 1.5, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.STEP_HEIGHT, DOOMSDAY_STEP, 1.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.JUMP_STRENGTH, DOOMSDAY_JUMP, 0.6, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet GOKU = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, GOKU_ARMOR, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, GOKU_TOUGHNESS, 8.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, GOKU_DAMAGE, 12.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MOVEMENT_SPEED, GOKU_SPEED, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.MAX_HEALTH, GOKU_HP, 40.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.KNOCKBACK_RESISTANCE, GOKU_KNOCKBACK, 1.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_SPEED, GOKU_ATTACK_SPEED, 2.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.JUMP_STRENGTH, GOKU_JUMP, 0.5, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.STEP_HEIGHT, GOKU_STEP, 1.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ENTITY_INTERACTION_RANGE, GOKU_REACH, 1.0, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet NARUTO = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, NARUTO_ARMOR, 12.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, NARUTO_TOUGHNESS, 4.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, NARUTO_DAMAGE, 7.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MOVEMENT_SPEED, NARUTO_SPEED, 0.35, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.add(Attributes.ATTACK_SPEED, NARUTO_ATTACK_SPEED, 2.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MAX_HEALTH, NARUTO_HP, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.KNOCKBACK_RESISTANCE, NARUTO_KNOCKBACK, 0.5, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.JUMP_STRENGTH, NARUTO_JUMP, 0.4, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.STEP_HEIGHT, NARUTO_STEP, 0.5, AttributeModifier.Operation.ADD_VALUE)
			.build();

	public static final AttributeModifierSet CAPTAIN_AMERICA = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, CAP_ARMOR, 25.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, CAP_TOUGHNESS, 8.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ATTACK_DAMAGE, CAP_DAMAGE, 4.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.MAX_HEALTH, CAP_HP, 20.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.KNOCKBACK_RESISTANCE, CAP_KNOCKBACK, 0.6, AttributeModifier.Operation.ADD_VALUE)
			.build();

	private HeroAttributes() {
	}

	public static AttributeModifierSet buildDoomsdayTierSet(int tier) {
		int t = Math.max(1, Math.min(7, tier));
		double f = (t - 1) / 6.0;
		return AttributeModifierSet.builder()
				.add(Attributes.ARMOR, DOOMSDAY_ARMOR, lerp(0.0, 30.0, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.ARMOR_TOUGHNESS, DOOMSDAY_TOUGHNESS, lerp(0.0, 20.0, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.ATTACK_DAMAGE, DOOMSDAY_DAMAGE, lerp(0.0, 20.0, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.MOVEMENT_SPEED, DOOMSDAY_SPEED, lerp(0.0, 0.25, f), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
				.add(Attributes.MAX_HEALTH, DOOMSDAY_HP, lerp(0.0, 80.0, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.KNOCKBACK_RESISTANCE, DOOMSDAY_KNOCKBACK, lerp(0.0, 1.0, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.SCALE, DOOMSDAY_SCALE, lerp(0.0, 1.2, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.ENTITY_INTERACTION_RANGE, DOOMSDAY_REACH, lerp(0.0, 1.5, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.BLOCK_INTERACTION_RANGE, DOOMSDAY_BLOCK_REACH, lerp(0.0, 1.5, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.STEP_HEIGHT, DOOMSDAY_STEP, lerp(0.0, 1.0, f), AttributeModifier.Operation.ADD_VALUE)
				.add(Attributes.JUMP_STRENGTH, DOOMSDAY_JUMP, lerp(0.0, 0.6, f), AttributeModifier.Operation.ADD_VALUE)
				.build();
	}

	private static double lerp(double a, double b, double f) {
		return a + (b - a) * f;
	}
}
