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

	public static final AttributeModifierSet HOMELANDER = AttributeModifierSet.builder()
			.add(Attributes.ARMOR, HOMELANDER_ARMOR, 50.0, AttributeModifier.Operation.ADD_VALUE)
			.add(Attributes.ARMOR_TOUGHNESS, HOMELANDER_TOUGHNESS, 6.0, AttributeModifier.Operation.ADD_VALUE)
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

	private HeroAttributes() {
	}
}
