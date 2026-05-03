package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.HeroAttributes;
import com.example.superheroes.hero.Heroes;
import com.example.superheroes.hero.IronManHero;
import com.example.superheroes.transform.HeroData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class IronManMarkPromotion {
	private IronManMarkPromotion() {
	}

	public static boolean isIronMan(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && IronManHero.ID.equals(data.heroId());
	}

	public static int currentMark(ServerPlayer player) {
		return IronManHero.getMark(player);
	}

	/** Sets attachment to {@code targetMark} and reapplies tier-scaled attributes. */
	public static boolean promote(ServerPlayer player, int targetMark) {
		int target = Math.max(1, Math.min(3, targetMark));
		int current = currentMark(player);
		if (current >= target) {
			return false;
		}

		player.setAttached(ModAttachments.IRON_MAN_MARK, target);

		// Atomic re-apply: clear all possible tier sets, then apply the new one.
		HeroAttributes.IRON_MAN.remove(player);
		HeroAttributes.buildIronManTierSet(1).remove(player);
		HeroAttributes.buildIronManTierSet(2).remove(player);
		HeroAttributes.buildIronManTierSet(3).remove(player);
		HeroAttributes.buildIronManTierSet(target).apply(player);

		player.setHealth(player.getMaxHealth());

		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.2f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.7f, 1.4f);
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.0, player.getZ(),
				40, 0.5, 0.7, 0.5, 0.08);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
				player.getX(), player.getY() + 1.0, player.getZ(),
				30, 0.4, 0.6, 0.4, 0.1);

		String key = switch (target) {
			case 2 -> "ability.superheroes.iron_man.upgraded_mark_vii";
			case 3 -> "ability.superheroes.iron_man.upgraded_mark_l";
			default -> "ability.superheroes.iron_man.upgraded_mark_i";
		};
		player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GOLD), true);
		// also send to chat for permanence
		player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.GOLD));
		return true;
	}
}
