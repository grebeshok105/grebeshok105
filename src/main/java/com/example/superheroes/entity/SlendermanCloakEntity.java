package com.example.superheroes.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * Invisible follower that renders the Slenderman geo + animations on top of
 * a transformed Slenderman player. Spawned when the player transforms,
 * despawned when they de-transform / leave / die.
 */
public class SlendermanCloakEntity extends Entity implements GeoEntity {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(
			SlendermanCloakEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SPRINTING = SynchedEntityData.defineId(
			SlendermanCloakEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> WALKING = SynchedEntityData.defineId(
			SlendermanCloakEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> BODY_YAW = SynchedEntityData.defineId(
			SlendermanCloakEntity.class, EntityDataSerializers.FLOAT);

	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.slenderman.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.slenderman.walk");
	private static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("animation.slenderman.sprint");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.slenderman.attack");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private UUID ownerUuid;
	private int attackTicks = 0;

	public SlendermanCloakEntity(EntityType<? extends SlendermanCloakEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ATTACKING, false);
		builder.define(SPRINTING, false);
		builder.define(WALKING, false);
		builder.define(BODY_YAW, 0f);
	}

	public void setOwner(Player owner) {
		this.ownerUuid = owner.getUUID();
	}

	public UUID getOwnerUuid() {
		return ownerUuid;
	}

	public void setAttacking(boolean v) {
		this.entityData.set(ATTACKING, v);
		if (v) attackTicks = 12;
	}

	public boolean isAttacking() {
		return this.entityData.get(ATTACKING);
	}

	public void setSprinting(boolean v) {
		this.entityData.set(SPRINTING, v);
	}

	@Override
	public boolean isSprinting() {
		return this.entityData.get(SPRINTING);
	}

	public void setWalking(boolean v) {
		this.entityData.set(WALKING, v);
	}

	public boolean isWalking() {
		return this.entityData.get(WALKING);
	}

	public void setBodyYaw(float yaw) {
		this.entityData.set(BODY_YAW, yaw);
	}

	public float getBodyYaw() {
		return this.entityData.get(BODY_YAW);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && attackTicks > 0) {
			attackTicks--;
			if (attackTicks == 0) {
				this.entityData.set(ATTACKING, false);
			}
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID("Owner")) {
			this.ownerUuid = tag.getUUID("Owner");
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (this.ownerUuid != null) {
			tag.putUUID("Owner", this.ownerUuid);
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isInvulnerable() {
		return true;
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
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "movement", 4, this::movementHandler));
		controllers.add(new AnimationController<>(this, "attack", 0, this::attackHandler)
				.triggerableAnim("attack", ATTACK));
	}

	private PlayState movementHandler(AnimationState<SlendermanCloakEntity> state) {
		if (isSprinting()) {
			return state.setAndContinue(SPRINT);
		}
		if (isWalking()) {
			return state.setAndContinue(WALK);
		}
		return state.setAndContinue(IDLE);
	}

	private PlayState attackHandler(AnimationState<SlendermanCloakEntity> state) {
		if (isAttacking()) {
			return state.setAndContinue(ATTACK);
		}
		state.getController().forceAnimationReset();
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
