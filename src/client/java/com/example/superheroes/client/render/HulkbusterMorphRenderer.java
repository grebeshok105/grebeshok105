package com.example.superheroes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

/**
 * Рисует Hulkbuster-голема прямо на координатах игрока, копируя
 * position / xo,yo,zo / yaw / yBodyRot / yHeadRot / walkAnimation / attackAnim
 * с самого {@link AbstractClientPlayer}. Это устраняет визуальный сдвиг
 * и desync rotation, который был у follow-entity подхода.
 *
 * <p>Используется одно cached IronGolem-«пугало», создаётся лениво
 * на первый рендер. Не добавляется в мир.
 */
public final class HulkbusterMorphRenderer {
	private static IronGolem PROXY;

	private HulkbusterMorphRenderer() {
	}

	/** Возвращает {@code true} если рендер выполнен (надо отменить vanilla). */
	public static boolean tryRender(AbstractClientPlayer player, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		if (level == null) return false;

		HulkbusterCloakRenderer renderer = HulkbusterCloakRenderer.INSTANCE;
		if (renderer == null) {
			// Лениво форсируем создание рендерера через dispatcher: сам по себе
			// cloak-entity невидим, поэтому ванильный путь ленивой инициализации
			// не срабатывает — нужно поднять явно.
			com.example.superheroes.entity.HulkbusterCloakEntity probe =
					new com.example.superheroes.entity.HulkbusterCloakEntity(
							com.example.superheroes.entity.ModEntities.HULKBUSTER_CLOAK, level);
			try {
				mc.getEntityRenderDispatcher().getRenderer(probe);
			} catch (Throwable ignored) {
			}
			renderer = HulkbusterCloakRenderer.INSTANCE;
			if (renderer == null) return false;
		}

		IronGolem proxy = PROXY;
		if (proxy == null || proxy.level() != level) {
			proxy = new IronGolem(EntityType.IRON_GOLEM, level);
			PROXY = proxy;
		}

		copyTransform(player, proxy);
		renderer.renderProxy(proxy, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		return true;
	}

	private static void copyTransform(AbstractClientPlayer p, IronGolem g) {
		// position / interpolation refs
		g.setPos(p.getX(), p.getY(), p.getZ());
		g.xo = p.xo;
		g.yo = p.yo;
		g.zo = p.zo;
		g.xOld = p.xOld;
		g.yOld = p.yOld;
		g.zOld = p.zOld;

		// rotations (entity + body + head)
		g.setYRot(p.getYRot());
		g.yRotO = p.yRotO;
		g.setXRot(p.getXRot());
		g.xRotO = p.xRotO;
		g.yBodyRot = p.yBodyRot;
		g.yBodyRotO = p.yBodyRotO;
		g.yHeadRot = p.yHeadRot;
		g.yHeadRotO = p.yHeadRotO;

		// animation drivers
		g.walkAnimation.setSpeed(p.walkAnimation.speed());
		// position не имеет публичного сеттера — кладём руками через update с computed delta
		float wantPosition = p.walkAnimation.position(1.0f);
		float currentPosition = g.walkAnimation.position(1.0f);
		float delta = wantPosition - currentPosition;
		// walkAnimation.update(speed, scale): добавляет speed*scale к position и сглаживает speed
		// просто переинициализируем чтобы position попал в нужную точку
		if (Math.abs(delta) > 0.001f) {
			g.walkAnimation.update(delta, 1.0f);
		}

		g.tickCount = p.tickCount;
		g.hurtTime = p.hurtTime;
		g.hurtDuration = p.hurtDuration;
		g.deathTime = p.deathTime;
		g.attackAnim = p.attackAnim;
		g.oAttackAnim = p.oAttackAnim;
		g.setSwimming(p.isSwimming());
		g.setSprinting(p.isSprinting());
		if (p.isOnFire()) {
			g.setRemainingFireTicks(8);
		} else {
			g.setRemainingFireTicks(0);
		}
	}
}
