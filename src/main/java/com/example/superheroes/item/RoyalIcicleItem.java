package com.example.superheroes.item;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.damage.ModDamageTypes;
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
 * Reid — драконий меч Рейнхарда.
 *
 * При обнажённом мече каждый удар по ЛКМ случайно выбирает один из трёх стилей атаки —
 * у каждого свой DamageType (чтобы Думсдей не мог адаптироваться к одному типу за пару хитов):
 *  1. Стандартный — playerAttack
 *  2. Удар Небес — superheroes:reinhard_heavens_strike (разрез воздуха на скорости света)
 *  3. Дыхание Дракона — superheroes:reinhard_dragons_breath (дыхание самого Reid'a, burning)
 *
 * Все три варианта одинаковы по эффекту: 100 урона, AoE 5 блоков,
 * BLINDNESS на 10 блоков, time-slow на первом ударе. Различие только в типе урона
 * и цветовом фидбеке (партиклы/звук).
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
					ServerLevel level = player.serverLevel();
					int variant = level.random.nextInt(3);
					DamageSource swingSrc = pickDamageSource(level, player, variant);

					float bonus = 4.0f + state.phase() * 1.5f;
					target.hurt(swingSrc, bonus);
					target.invulnerableTime = 0;

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
						le.hurt(swingSrc, CLEAVE_DAMAGE);
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

					spawnVariantVfx(level, origin, variant);
				}
			}
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	private static DamageSource pickDamageSource(ServerLevel level, ServerPlayer player, int variant) {
		return switch (variant) {
			case 1 -> ModDamageTypes.reinhardHeavensStrike(level, player);
			case 2 -> ModDamageTypes.reinhardDragonsBreath(level, player);
			default -> level.damageSources().playerAttack(player);
		};
	}

	private static void spawnVariantVfx(ServerLevel level, Vec3 origin, int variant) {
		switch (variant) {
			case 1 -> {
				level.sendParticles(ParticleTypes.END_ROD,
						origin.x, origin.y, origin.z, 18, 0.6, 0.6, 0.6, 0.05);
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
						origin.x, origin.y, origin.z, 24, 0.6, 0.6, 0.6, 0.15);
				level.playSound(null, origin.x, origin.y, origin.z,
						SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.6f);
				level.playSound(null, origin.x, origin.y, origin.z,
						SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7f, 1.4f);
			}
			case 2 -> {
				level.sendParticles(ParticleTypes.DRAGON_BREATH,
						origin.x, origin.y, origin.z, 30, 0.7, 0.5, 0.7, 0.02);
				level.sendParticles(ParticleTypes.FLAME,
						origin.x, origin.y, origin.z, 18, 0.5, 0.5, 0.5, 0.05);
				level.playSound(null, origin.x, origin.y, origin.z,
						SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5f, 1.4f);
				level.playSound(null, origin.x, origin.y, origin.z,
						SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7f, 0.8f);
			}
			default -> {
				level.sendParticles(ParticleTypes.SWEEP_ATTACK,
						origin.x, origin.y, origin.z, 1, 0, 0, 0, 0);
				level.playSound(null, origin.x, origin.y, origin.z,
						SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.0f);
			}
		}
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
