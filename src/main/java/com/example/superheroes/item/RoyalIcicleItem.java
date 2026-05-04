package com.example.superheroes.item;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.ReinhardState;
import com.example.superheroes.hero.ReinhardHero;
import com.example.superheroes.transform.HeroData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Reid — драконий меч Рейнхарда. Обычная атака — около ванильного netherite-меча.
 * Sword abilities (air slash, teleport, jump) идут через ability system.
 *
 * При попадании в "достойного соперника" наносит +50% бонусного урона.
 * При попадании в обычного моба урона нет (нанесённый урон обнуляется).
 *
 * Проверка достойности — на стороне сервера через ServerLivingEntityEvents.
 * Сам Item ничего не блокирует — только подсказывает в hurtEnemy().
 */
public class RoyalIcicleItem extends SwordItem {
	public RoyalIcicleItem(Properties properties) {
		super(Tiers.NETHERITE, properties.attributes(SwordItem.createAttributes(Tiers.NETHERITE, 100, -2.4f)));
	}

	private static final double CLEAVE_RADIUS = 5.0;
	private static final float CLEAVE_DAMAGE = 100.0f;
	private static final double DARKNESS_RADIUS = 10.0;
	private static final int DARKNESS_DURATION_TICKS = 80;

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof ServerPlayer player) {
			HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
			if (ReinhardHero.ID.equals(data.heroId())) {
				ReinhardState state = player.getAttachedOrCreate(ModAttachments.REINHARD_STATE);
				if (state.swordDrawn()) {
					float bonus = 4.0f + state.phase() * 1.5f;
					target.hurt(player.serverLevel().damageSources().playerAttack(player), bonus);
					target.invulnerableTime = 0;

					ServerLevel level = player.serverLevel();
					DamageSource cleaveSrc = level.damageSources().playerAttack(player);
					Vec3 origin = target.position().add(0, target.getBbHeight() * 0.5, 0);
					AABB cleaveBox = new AABB(
							origin.x - CLEAVE_RADIUS, origin.y - CLEAVE_RADIUS, origin.z - CLEAVE_RADIUS,
							origin.x + CLEAVE_RADIUS, origin.y + CLEAVE_RADIUS, origin.z + CLEAVE_RADIUS);
					double cleaveR2 = CLEAVE_RADIUS * CLEAVE_RADIUS;
					List<LivingEntity> cleaveTargets = level.getEntitiesOfClass(LivingEntity.class, cleaveBox,
							e -> e != player && e != target && e.isAlive() && !e.isSpectator()
									&& !(e instanceof Player p && p.getUUID().equals(player.getUUID()))
									&& e.position().add(0, e.getBbHeight() * 0.5, 0).distanceToSqr(origin) <= cleaveR2);
					for (LivingEntity le : cleaveTargets) {
						le.hurt(cleaveSrc, CLEAVE_DAMAGE);
						le.invulnerableTime = 0;
						Vec3 push = le.position().subtract(origin);
						double horiz = Math.max(0.01, Math.sqrt(push.x * push.x + push.z * push.z));
						le.setDeltaMovement(push.x / horiz * 0.55, 0.18, push.z / horiz * 0.55);
						le.hurtMarked = true;
					}

					double darkR2 = DARKNESS_RADIUS * DARKNESS_RADIUS;
					AABB darkBox = new AABB(
							origin.x - DARKNESS_RADIUS, origin.y - DARKNESS_RADIUS, origin.z - DARKNESS_RADIUS,
							origin.x + DARKNESS_RADIUS, origin.y + DARKNESS_RADIUS, origin.z + DARKNESS_RADIUS);
					List<LivingEntity> nearbyForDark = level.getEntitiesOfClass(LivingEntity.class, darkBox,
							e -> e.isAlive() && !e.isSpectator()
									&& e.position().add(0, e.getBbHeight() * 0.5, 0).distanceToSqr(origin) <= darkR2);
					for (LivingEntity le : nearbyForDark) {
						le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DARKNESS_DURATION_TICKS, 0, false, false, false));
					}

					level.sendParticles(ParticleTypes.SWEEP_ATTACK, origin.x, origin.y, origin.z, 1, 0, 0, 0, 0);
					level.playSound(null, origin.x, origin.y, origin.z,
							SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.0f);
				}
			}
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		TooltipFrame.openDivider(tooltip, ChatFormatting.GOLD);
		tooltip.add(TooltipFrame.flavor("item.superheroes.royal_icicle.lore.line1", ChatFormatting.GOLD));
		tooltip.add(TooltipFrame.flavor("item.superheroes.royal_icicle.lore.line2", ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());
		tooltip.add(TooltipFrame.bullet("item.superheroes.royal_icicle.lore.worthy", ChatFormatting.YELLOW));
		tooltip.add(TooltipFrame.bullet("item.superheroes.royal_icicle.lore.bound", ChatFormatting.AQUA));
		TooltipFrame.closeDivider(tooltip, ChatFormatting.GOLD);
	}
}
