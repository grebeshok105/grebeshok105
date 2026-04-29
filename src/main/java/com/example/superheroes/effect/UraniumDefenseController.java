package com.example.superheroes.effect;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.HomelanderHero;
import com.example.superheroes.item.UraniumDaggerItem;
import com.example.superheroes.network.UraniumPressureS2CPayload;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UraniumDefenseController {
	private static final int SCAN_INTERVAL_TICKS = 20;
	private static int tickCounter = 0;
	private static Set<UUID> lastPressured = Collections.emptySet();

	private UraniumDefenseController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (++tickCounter < SCAN_INTERVAL_TICKS) return;
			tickCounter = 0;

			Set<UUID> pressured = new HashSet<>();
			List<ServerPlayer> players = server.getPlayerList().getPlayers();
			for (ServerPlayer homelander : players) {
				if (!isHomelander(homelander)) continue;
				ServerLevel level = homelander.serverLevel();
				for (ServerPlayer other : players) {
					if (other == homelander) continue;
					if (other.serverLevel() != level) continue;
					if (hasUraniumDagger(other)) {
						pressured.add(homelander.getUUID());
						break;
					}
				}
			}

			if (!pressured.equals(lastPressured)) {
				lastPressured = pressured;
				List<UUID> ids = new ArrayList<>(pressured);
				UraniumPressureS2CPayload payload = new UraniumPressureS2CPayload(ids);
				for (ServerPlayer p : players) {
					ServerPlayNetworking.send(p, payload);
				}
			}
		});
	}

	public static boolean isHomelander(Player player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		return data.hasHero() && HomelanderHero.ID.equals(data.heroId());
	}

	public static boolean hasUraniumDagger(Player player) {
		for (ItemStack stack : player.getInventory().items) {
			if (stack.getItem() instanceof UraniumDaggerItem) return true;
		}
		for (ItemStack stack : player.getInventory().offhand) {
			if (stack.getItem() instanceof UraniumDaggerItem) return true;
		}
		return false;
	}

	public static boolean isPlayerWithDagger(Player player) {
		return player instanceof Player && hasUraniumDagger(player);
	}

	public static float laserDamageMultiplier(Player target) {
		return hasUraniumDagger(target) ? 0.5f : 1.0f;
	}

	public static void sendCurrentTo(ServerPlayer player) {
		List<UUID> ids = new ArrayList<>(lastPressured);
		ServerPlayNetworking.send(player, new UraniumPressureS2CPayload(ids));
	}
}
