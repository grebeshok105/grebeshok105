package com.example.superheroes.entity;

import com.example.superheroes.entity.ai.HomelanderEyeLaserGoal;
import com.example.superheroes.entity.ai.HomelanderFlightGoal;
import com.example.superheroes.entity.ai.HomelanderShockwaveDiveGoal;
import com.example.superheroes.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class HomelanderBossEntity extends Monster {
	private final ServerBossEvent bossEvent = new ServerBossEvent(
			Component.translatable("entity.superheroes.homelander_boss"),
			BossEvent.BossBarColor.RED,
			BossEvent.BossBarOverlay.NOTCHED_10);

	private int laserCooldown;
	private int shockwaveCooldown;

	public HomelanderBossEntity(EntityType<? extends HomelanderBossEntity> type, Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.setNoGravity(true);
		this.xpReward = 50;
		this.shockwaveCooldown = 200;
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
				.add(Attributes.MAX_HEALTH, 500.0)
				.add(Attributes.ARMOR, 100.0)
				.add(Attributes.ARMOR_TOUGHNESS, 12.0)
				.add(Attributes.ATTACK_DAMAGE, 12.0)
				.add(Attributes.ATTACK_KNOCKBACK, 1.5)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.FLYING_SPEED, 0.6)
				.add(Attributes.FOLLOW_RANGE, 64.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new HomelanderShockwaveDiveGoal(this));
		this.goalSelector.addGoal(2, new HomelanderEyeLaserGoal(this));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
		this.goalSelector.addGoal(4, new HomelanderFlightGoal(this));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 24f));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (laserCooldown > 0) {
			laserCooldown--;
		}
		if (shockwaveCooldown > 0) {
			shockwaveCooldown--;
		}
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossEvent.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
		super.dropCustomDeathLoot(level, damageSource, wasRecentlyHit);
		this.spawnAtLocation(new ItemStack(ModItems.HOMELANDER_SUIT));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENDER_DRAGON_GROWL;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
		return super.canBeAffected(effect);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	public int getLaserCooldown() {
		return laserCooldown;
	}

	public void setLaserCooldown(int ticks) {
		this.laserCooldown = ticks;
	}

	public int getShockwaveCooldown() {
		return shockwaveCooldown;
	}

	public void setShockwaveCooldown(int ticks) {
		this.shockwaveCooldown = ticks;
	}
}
