package com.example.superheroes.ability;

import com.example.superheroes.entity.ModEntities;
import com.example.superheroes.entity.RegulusProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class ObjectProjectileAbility implements Ability {
	private static final double RAY_RANGE = 8.0;
	private static final double PROJECTILE_SPEED = 3.0;

	private static final Set<net.minecraft.world.level.block.Block> ALLOWED = Set.of(
			Blocks.SAND,
			Blocks.RED_SAND,
			Blocks.GRAVEL,
			Blocks.DIRT,
			Blocks.COARSE_DIRT,
			Blocks.ROOTED_DIRT,
			Blocks.GRASS_BLOCK,
			Blocks.STONE,
			Blocks.COBBLESTONE,
			Blocks.MOSSY_COBBLESTONE,
			Blocks.DEEPSLATE,
			Blocks.COBBLED_DEEPSLATE,
			Blocks.ANDESITE,
			Blocks.DIORITE,
			Blocks.GRANITE
	);

	@Override
	public ResourceLocation getId() {
		return AbilityIds.OBJECT_PROJECTILE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 50f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1f);
		Vec3 end = eye.add(dir.scale(RAY_RANGE));
		BlockHitResult hit = level.clip(new ClipContext(eye, end,
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!ALLOWED.contains(state.getBlock())) {
			return false;
		}

		RegulusProjectileEntity projectile = new RegulusProjectileEntity(ModEntities.REGULUS_PROJECTILE, level);
		projectile.setOwner(player);
		projectile.setItem(new net.minecraft.world.item.ItemStack(state.getBlock()));
		Vec3 spawnPos = eye.add(dir.scale(0.8));
		projectile.setPos(spawnPos.x, spawnPos.y - 0.1, spawnPos.z);
		projectile.setDeltaMovement(dir.scale(PROJECTILE_SPEED));
		level.addFreshEntity(projectile);

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.4f);
		return true;
	}
}
