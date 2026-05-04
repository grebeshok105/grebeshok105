package com.example.superheroes.item;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.ReinhardState;
import com.example.superheroes.effect.ReinhardWorthyOpponent;
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
	private static final float CLEAVE_DAMAGE = 12.0f;
	private static final float CLEAVE_DAMAGE_WORTHY = 22.0f;

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
					AABB box = new AABB(
							origin.x - CLEAVE_RADIUS, origin.y - CLEAVE_RADIUS, origin.z - CLEAVE_RADIUS,
							origin.x + CLEAVE_RADIUS, origin.y + CLEAVE_RADIUS, origin.z + CLEAVE_RADIUS);
					List<LivingEntity> cleaveTargets = level.getEntitiesOfClass(LivingEntity.class, box,
							e -> e != player && e != target && e.isAlive() && !e.isSpectator()
									&& !(e instanceof Player p && p.getUUID().equals(player.getUUID()))
									&& e.position().add(0, e.getBbHeight() * 0.5, 0).distanceToSqr(origin)
											<= CLEAVE_RADIUS * CLEAVE_RADIUS);
					for (LivingEntity le : cleaveTargets) {
						boolean worthy = ReinhardWorthyOpponent.isWorthy(le);
						float dmg = worthy ? CLEAVE_DAMAGE_WORTHY : CLEAVE_DAMAGE;
						le.hurt(cleaveSrc, dmg);
						le.invulnerableTime = 0;
						Vec3 push = le.position().subtract(origin);
						double horiz = Math.max(0.01, Math.sqrt(push.x * push.x + push.z * push.z));
						le.setDeltaMovement(push.x / horiz * 0.55, 0.18, push.z / horiz * 0.55);
						le.hurtMarked = true;
					}

					int rings = 3;
					for (int ring = 1; ring <= rings; ring++) {
						double r = (CLEAVE_RADIUS * ring) / rings;
						int count = 24 + ring * 12;
						for (int i = 0; i < count; i++) {
							double a = (Math.PI * 2 * i) / count;
							double px = origin.x + Math.cos(a) * r;
							double pz = origin.z + Math.sin(a) * r;
							level.sendParticles(ParticleTypes.SWEEP_ATTACK, px, origin.y, pz, 1, 0, 0, 0, 0);
							if (ring == rings) {
								level.sendParticles(ParticleTypes.END_ROD, px, origin.y + 0.4, pz, 1, 0, 0, 0, 0);
							}
						}
					}
					level.sendParticles(ParticleTypes.FLASH, origin.x, origin.y, origin.z, 1, 0, 0, 0, 0);
					level.playSound(null, origin.x, origin.y, origin.z,
							SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6f, 0.7f);
					level.playSound(null, origin.x, origin.y, origin.z,
							SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0f, 1.1f);
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
