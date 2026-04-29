package com.example.superheroes.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Shadow Soldier — теневой солдат Сон Джи Ву. Летает, не наносит fall damage,
 * имеет хозяина и фокус-цель. 3 вариант текстуры (рандомно при создании).
 *
 * Урон, который должен получить хозяин, перенаправляется на солдат через
 * {@link com.example.superheroes.effect.SungJinwooController} (без радиуса).
 */
public class ShadowSoldierEntity extends Monster {
	public static final int VARIANT_COUNT = 3;
	public static final double FOLLOW_RADIUS = 12.0;

	private static final EntityDataAccessor<Byte> DATA_VARIANT =
			SynchedEntityData.defineId(ShadowSoldierEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
			SynchedEntityData.defineId(ShadowSoldierEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_FOCUS =
			SynchedEntityData.defineId(ShadowSoldierEntity.class, EntityDataSerializers.OPTIONAL_UUID);

	public ShadowSoldierEntity(EntityType<? extends ShadowSoldierEntity> type, Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl(this, 30, true);
		this.setNoGravity(true);
		this.xpReward = 0;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanOpenDoors(false);
		nav.setCanFloat(true);
		nav.setCanPassDoors(true);
		return nav;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.ARMOR, 5.0)
				.add(Attributes.MOVEMENT_SPEED, 0.30)
				.add(Attributes.FLYING_SPEED, 0.40)
				.add(Attributes.ATTACK_DAMAGE, 6.0)
				.add(Attributes.FOLLOW_RANGE, 64.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_VARIANT, (byte) 0);
		builder.define(DATA_OWNER, Optional.empty());
		builder.define(DATA_FOCUS, Optional.empty());
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	public int getVariant() {
		return this.entityData.get(DATA_VARIANT) & 0xFF;
	}

	public void setVariant(int variant) {
		this.entityData.set(DATA_VARIANT, (byte) (variant % VARIANT_COUNT));
	}

	@Nullable
	public UUID getOwnerId() {
		return this.entityData.get(DATA_OWNER).orElse(null);
	}

	public void setOwnerId(@Nullable UUID id) {
		this.entityData.set(DATA_OWNER, Optional.ofNullable(id));
	}

	@Nullable
	public UUID getFocusId() {
		return this.entityData.get(DATA_FOCUS).orElse(null);
	}

	public void setFocusId(@Nullable UUID id) {
		this.entityData.set(DATA_FOCUS, Optional.ofNullable(id));
	}

	@Nullable
	public Player getOwner() {
		UUID id = getOwnerId();
		if (id == null) return null;
		Player p = this.level().getPlayerByUUID(id);
		return p != null && p.isAlive() ? p : null;
	}

	@Nullable
	public LivingEntity getFocus() {
		UUID id = getFocusId();
		if (id == null) return null;
		if (!(this.level() instanceof ServerLevel sl)) return null;
		Entity e = sl.getEntity(id);
		if (e instanceof LivingEntity le && le.isAlive()) return le;
		return null;
	}

	@Override
	public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.MobSpawnType spawnReason) {
		// Не спавнятся натурально — только через Arise
		return false;
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.25f;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide()) {
			if (this.tickCount % 4 == 0) {
				double x = this.getX() + (this.random.nextDouble() - 0.5) * 0.3;
				double y = this.getY() + this.random.nextDouble() * 0.5;
				double z = this.getZ() + (this.random.nextDouble() - 0.5) * 0.3;
				this.level().addParticle(ParticleTypes.WARPED_SPORE, x, y, z, 0.0, 0.02, 0.0);
			}
			return;
		}

		Player owner = getOwner();
		LivingEntity focus = getFocus();

		// Цель: focus → последний damager хозяина → null
		LivingEntity target = (focus != null) ? focus : null;
		if (target == null && owner != null) {
			LivingEntity lastDamager = owner.getLastHurtByMob();
			if (lastDamager != null && lastDamager.isAlive() && lastDamager != owner) {
				target = lastDamager;
			}
		}
		if (target != this.getTarget()) {
			this.setTarget(target);
		}

		// Если цели нет — следовать за хозяином в радиусе
		if (target == null && owner != null) {
			double d2 = this.distanceToSqr(owner);
			if (d2 > FOLLOW_RADIUS * FOLLOW_RADIUS) {
				Vec3 dir = owner.position().add(0, 1.5, 0).subtract(this.position()).normalize().scale(0.5);
				this.move(MoverType.SELF, dir);
			}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// Не получать урон от хозяина
		Entity ent = source.getEntity();
		if (ent != null && ent.getUUID().equals(getOwnerId())) {
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (target.getUUID().equals(getOwnerId())) return false;
		return super.canAttack(target);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putByte("Variant", (byte) getVariant());
		UUID owner = getOwnerId();
		if (owner != null) tag.putUUID("Owner", owner);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setVariant(tag.getByte("Variant"));
		if (tag.hasUUID("Owner")) {
			setOwnerId(tag.getUUID("Owner"));
		}
	}
}
