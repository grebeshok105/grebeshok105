package com.example.superheroes.client.render;

import com.example.superheroes.client.SlendermanCloakClientTracker;
import com.example.superheroes.entity.SlendermanCloakEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Рисует Slenderman-cloak модель прямо на координатах / yaw / walking-стейте
 * самого игрока. Использует server-spawned cloak-entity (хранит GeckoLib-кэш
 * и synced-стейты типа attacking) — но рендерит её НЕ на её собственной
 * позиции, а на позиции owner-player'а в текущий клиент-тик.
 */
public final class SlendermanMorphRenderer {
	private SlendermanMorphRenderer() {
	}

	/** Возвращает {@code true} если рендер выполнен (надо отменить vanilla). */
	public static boolean tryRender(AbstractClientPlayer player, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		SlendermanCloakEntity cloak = SlendermanCloakClientTracker.get(player.getUUID());
		if (cloak == null) return false;

		SlendermanCloakRenderer renderer = SlendermanCloakRenderer.INSTANCE;
		if (renderer == null) {
			// Лениво форсируем создание GeoEntityRenderer через dispatcher
			// (cloak-entity невидим, ленивый путь не срабатывает сам).
			try {
				net.minecraft.client.Minecraft.getInstance()
						.getEntityRenderDispatcher()
						.getRenderer(cloak);
			} catch (Throwable ignored) {
			}
			renderer = SlendermanCloakRenderer.INSTANCE;
			if (renderer == null) return false;
		}

		// Копируем transform с игрока в cloak: убирает сетевой лаг и кривое вращение.
		cloak.setPos(player.getX(), player.getY(), player.getZ());
		cloak.xo = player.xo;
		cloak.yo = player.yo;
		cloak.zo = player.zo;
		cloak.xOld = player.xOld;
		cloak.yOld = player.yOld;
		cloak.zOld = player.zOld;
		cloak.yRotO = player.yRotO;
		cloak.setYRot(player.getYRot());
		cloak.xRotO = player.xRotO;
		cloak.setXRot(player.getXRot());

		// entityYaw, который передаёт PlayerRenderer, это интерполированный
		// yBodyRot самого игрока — он и нужен. Пробрасываем как есть.
		renderer.renderProxy(cloak, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		return true;
	}
}
