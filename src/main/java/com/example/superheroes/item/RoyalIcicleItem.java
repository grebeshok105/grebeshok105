package com.example.superheroes.item;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.ReinhardState;
import com.example.superheroes.effect.ReinhardWorthyOpponent;
import com.example.superheroes.hero.ReinhardHero;
import com.example.superheroes.transform.HeroData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;

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
		super(Tiers.NETHERITE, properties.attributes(SwordItem.createAttributes(Tiers.NETHERITE, 5, -2.4f)));
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof ServerPlayer player) {
			HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
			if (ReinhardHero.ID.equals(data.heroId())) {
				ReinhardState state = player.getAttachedOrCreate(ModAttachments.REINHARD_STATE);
				if (state.swordDrawn() && ReinhardWorthyOpponent.isWorthy(target)) {
					float bonus = 4.0f + state.phase() * 1.5f;
					target.hurt(player.serverLevel().damageSources().playerAttack(player), bonus);
					target.invulnerableTime = 0;
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
