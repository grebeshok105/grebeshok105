package com.example.superheroes.item;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.effect.RaidenState;
import com.example.superheroes.hero.RaidenHero;
import com.example.superheroes.particle.ModParticles;
import com.example.superheroes.transform.HeroData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Musou no Hitotachi — Yamato в фиолетовом окрасе Райден Сёгун.
 * Базовый удар — около ванильного netherite. Бонусы стэкаются от состояния:
 *   • Глаз Грозного Суда (E активен) — каждый удар срабатывает Glaive of Judgment
 *     (доп. урон электро + AoE мини-сплеш).
 *   • Musou Shinsetsu (Q активен) — обычный удар бьёт +50% сильнее, всё в радиусе 4 блока тоже получает урон.
 */
public class MusouNoHitotachiItem extends SwordItem {
	public MusouNoHitotachiItem(Properties properties) {
		super(Tiers.NETHERITE, properties.attributes(SwordItem.createAttributes(Tiers.NETHERITE, 6, -2.4f)));
	}

	private static final float EYE_BONUS_PLAYER = 9.0f;
	private static final float EYE_BONUS_MOB = 4.0f;
	private static final double EYE_AOE_RADIUS = 3.0;
	private static final float EYE_AOE_DAMAGE = 4.0f;

	private static final float BURST_BONUS_PLAYER = 12.0f;
	private static final float BURST_BONUS_MOB = 6.0f;
	private static final double BURST_AOE_RADIUS = 4.0;
	private static final float BURST_AOE_DAMAGE = 8.0f;

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof ServerPlayer player) {
			HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
			if (RaidenHero.ID.equals(data.heroId())) {
				RaidenState state = player.getAttachedOrCreate(ModAttachments.RAIDEN_STATE);
				long now = player.serverLevel().getGameTime();
				ServerLevel level = player.serverLevel();
				boolean targetIsPlayer = target instanceof net.minecraft.world.entity.player.Player;

				if (state.eyeExpireTick() > now) {
					float bonus = targetIsPlayer ? EYE_BONUS_PLAYER : EYE_BONUS_MOB;
					target.invulnerableTime = 0;
					target.hurt(level.damageSources().playerAttack(player), bonus);

					level.sendParticles(ModParticles.SPARKS,
							target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
							24, 0.4, 0.6, 0.4, 0.18);
					level.sendParticles(ModParticles.PURPLE_FLAME,
							target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
							8, 0.3, 0.4, 0.3, 0.02);
					level.playSound(null, target.getX(), target.getY(), target.getZ(),
							SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.6f);

					double r2 = EYE_AOE_RADIUS * EYE_AOE_RADIUS;
					List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
							target.getBoundingBox().inflate(EYE_AOE_RADIUS),
							e -> e != player && e != target && e.isAlive() && !e.isSpectator()
									&& e.distanceToSqr(target) <= r2);
					for (LivingEntity e : nearby) {
						e.invulnerableTime = 0;
						e.hurt(level.damageSources().playerAttack(player), EYE_AOE_DAMAGE);
					}
				}

				if (state.burstExpireTick() > now) {
					float bonus = targetIsPlayer ? BURST_BONUS_PLAYER : BURST_BONUS_MOB;
					target.invulnerableTime = 0;
					target.hurt(level.damageSources().playerAttack(player), bonus);

					double r2 = BURST_AOE_RADIUS * BURST_AOE_RADIUS;
					List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
							target.getBoundingBox().inflate(BURST_AOE_RADIUS),
							e -> e != player && e != target && e.isAlive() && !e.isSpectator()
									&& e.distanceToSqr(target) <= r2);
					for (LivingEntity e : nearby) {
						e.invulnerableTime = 0;
						e.hurt(level.damageSources().playerAttack(player), BURST_AOE_DAMAGE);
					}

					level.sendParticles(ParticleTypes.SWEEP_ATTACK,
							target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
							1, 0, 0, 0, 0);
					level.sendParticles(ModParticles.SPARKS,
							target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
							30, 0.6, 0.8, 0.6, 0.25);
					level.sendParticles(ModParticles.SWORD_EXPLOSION,
							target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
							8, 0.4, 0.4, 0.4, 0.1);
					level.playSound(null, target.getX(), target.getY(), target.getZ(),
							SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9f, 0.9f);
				}
			}
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		TooltipFrame.openDivider(tooltip, ChatFormatting.LIGHT_PURPLE);
		tooltip.add(TooltipFrame.flavor("item.superheroes.musou_no_hitotachi.lore.line1", ChatFormatting.LIGHT_PURPLE));
		tooltip.add(TooltipFrame.flavor("item.superheroes.musou_no_hitotachi.lore.line2", ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());
		tooltip.add(TooltipFrame.bullet("item.superheroes.musou_no_hitotachi.lore.eye", ChatFormatting.AQUA));
		tooltip.add(TooltipFrame.bullet("item.superheroes.musou_no_hitotachi.lore.burst", ChatFormatting.YELLOW));
		TooltipFrame.closeDivider(tooltip, ChatFormatting.LIGHT_PURPLE);
	}
}
