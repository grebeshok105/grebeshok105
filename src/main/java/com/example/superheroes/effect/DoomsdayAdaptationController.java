package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.DoomsdayHero;
import com.example.superheroes.hero.HeroAttributes;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DoomsdayAdaptationController {
	private static final float CUMULATIVE_THRESHOLD = 80f;
	private static final float ADAPT_DAMAGE_BONUS = 1.0f;

	private static final Map<UUID, Set<ResourceKey<DamageType>>> ADAPTED = new ConcurrentHashMap<>();
	private static final Map<UUID, Map<ResourceKey<DamageType>, Float>> CUMULATIVE = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> ADAPT_COUNT = new ConcurrentHashMap<>();

	private DoomsdayAdaptationController() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) {
				return true;
			}
			if (!isDoomsday(player)) {
				return true;
			}
			ResourceKey<DamageType> typeKey = source.typeHolder().unwrapKey().orElse(null);
			if (typeKey == null) {
				return true;
			}

			Set<ResourceKey<DamageType>> adapted = ADAPTED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
			if (adapted.contains(typeKey)) {
				return false;
			}

			float health = player.getHealth();
			boolean lethal = amount >= health;

			if (lethal) {
				registerAdaptation(player, typeKey, true);
				player.setHealth(Math.max(1.0f, health));
				player.invulnerableTime = 20;
				return false;
			}

			Map<ResourceKey<DamageType>, Float> cum = CUMULATIVE.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
			float total = cum.getOrDefault(typeKey, 0f) + amount;
			if (total >= CUMULATIVE_THRESHOLD) {
				cum.remove(typeKey);
				registerAdaptation(player, typeKey, false);
			} else {
				cum.put(typeKey, total);
			}
			return true;
		});
	}

	private static void registerAdaptation(ServerPlayer player, ResourceKey<DamageType> typeKey, boolean lethal) {
		Set<ResourceKey<DamageType>> adapted = ADAPTED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
		if (!adapted.add(typeKey)) {
			return;
		}
		int count = ADAPT_COUNT.merge(player.getUUID(), 1, Integer::sum);
		applyDamageBonus(player, count);

		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.4f, 0.5f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.7f, 0.6f);

		String typePath = typeKey.location().getPath();
		Component title = Component.translatable("hero.superheroes.doomsday.adapted",
				Component.literal(typePath).withStyle(ChatFormatting.RED))
				.withStyle(lethal ? ChatFormatting.DARK_RED : ChatFormatting.GOLD);
		player.displayClientMessage(title, true);
		player.sendSystemMessage(Component.translatable("hero.superheroes.doomsday.adapted.chat",
				Component.literal(typePath).withStyle(ChatFormatting.LIGHT_PURPLE),
				Component.literal(String.valueOf(count)).withStyle(ChatFormatting.RED))
				.withStyle(ChatFormatting.GRAY));
	}

	private static void applyDamageBonus(ServerPlayer player, int count) {
		AttributeInstance inst = player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (inst == null) return;
		double amount = ADAPT_DAMAGE_BONUS * count;
		inst.addOrReplacePermanentModifier(new AttributeModifier(
				HeroAttributes.DOOMSDAY_ADAPT_DAMAGE, amount, AttributeModifier.Operation.ADD_VALUE));
	}

	public static int getAdaptationCount(ServerPlayer player) {
		return ADAPT_COUNT.getOrDefault(player.getUUID(), 0);
	}

	public static boolean hasAdapted(ServerPlayer player, DamageSource source) {
		ResourceKey<DamageType> typeKey = source.typeHolder().unwrapKey().orElse(null);
		if (typeKey == null) return false;
		Set<ResourceKey<DamageType>> adapted = ADAPTED.get(player.getUUID());
		return adapted != null && adapted.contains(typeKey);
	}

	public static void clear(ServerPlayer player) {
		UUID id = player.getUUID();
		ADAPTED.remove(id);
		CUMULATIVE.remove(id);
		ADAPT_COUNT.remove(id);
		AttributeInstance inst = player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (inst != null) {
			inst.removeModifier(HeroAttributes.DOOMSDAY_ADAPT_DAMAGE);
		}
	}

	private static boolean isDoomsday(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && DoomsdayHero.ID.equals(data.heroId());
	}
}
