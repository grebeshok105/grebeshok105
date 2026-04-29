package com.example.superheroes.item;

import com.example.superheroes.entity.HomelanderBossEntity;
import com.example.superheroes.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class VoughtSignalItem extends Item {
	public VoughtSignalItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel().isClientSide) {
			return InteractionResult.SUCCESS;
		}
		ServerLevel level = (ServerLevel) context.getLevel();
		BlockPos pos = context.getClickedPos().above();
		HomelanderBossEntity boss = ModEntities.HOMELANDER_BOSS.create(level);
		if (boss == null) {
			return InteractionResult.FAIL;
		}
		boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				context.getHorizontalDirection().toYRot(), 0f);
		level.addFreshEntity(boss);

		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
		if (bolt != null) {
			bolt.moveTo(boss.position());
			bolt.setVisualOnly(true);
			level.addFreshEntity(bolt);
		}

		level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.4f, 0.7f);

		if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}
		return InteractionResult.CONSUME;
	}
}
