package com.example.superheroes.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Невидимый компаньон, который рендерится моделью Iron Golem с Hulkbuster-текстурой
 * поверх позиции игрока. Спавнится при активации {@code iron_man_hulkbuster} toggle,
 * убирается при выключении / смене героя / disconnect / death.
 *
 * <p>Расширяет {@link IronGolem}, чтобы переиспользовать ванильный рендерер
 * (через сабкласс с подменой текстуры). AI отключён, никакой физики, неуязвим.
 */
public class HulkbusterCloakEntity extends IronGolem {
	private static final EntityDataAccessor<Boolean> WALKING = SynchedEntityData.defineId(
			HulkbusterCloakEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> ATTACKING_DATA = SynchedEntityData.defineId(
			HulkbusterCloakEntity.class, EntityDataSerializers.BOOLEAN);

	private UUID ownerUuid;

	public HulkbusterCloakEntity(EntityType<? extends IronGolem> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.noCulling = true;
		this.setNoAi(true);
		this.setSilent(true);
		this.setInvulnerable(true);
		this.setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(WALKING, false);
		builder.define(ATTACKING_DATA, false);
	}

	public void setOwner(Player owner) {
		this.ownerUuid = owner.getUUID();
	}

	public UUID getOwnerUuid() {
		return ownerUuid;
	}

	public void setWalking(boolean v) {
		this.entityData.set(WALKING, v);
	}

	public boolean isWalking() {
		return this.entityData.get(WALKING);
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	public void aiStep() {
		// без AI; обновляем только walkAnimation для рендера
		if (this.isWalking()) {
			this.walkAnimation.update(1.0f, 0.4f);
		} else {
			this.walkAnimation.update(0.0f, 0.4f);
		}
	}

	@Override
	protected void customServerAiStep() {
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double dist) {
		return true;
	}

	@Override
	public boolean shouldRender(double x, double y, double z) {
		return true;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("Owner")) {
			this.ownerUuid = tag.getUUID("Owner");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.ownerUuid != null) {
			tag.putUUID("Owner", this.ownerUuid);
		}
	}
}
