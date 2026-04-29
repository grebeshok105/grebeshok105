package com.example.superheroes.entity;

import com.example.superheroes.ModId;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final EntityType<HomelanderBossEntity> HOMELANDER_BOSS = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			ModId.of("homelander_boss"),
			EntityType.Builder.of(HomelanderBossEntity::new, MobCategory.MONSTER)
					.sized(0.6f, 1.95f)
					.clientTrackingRange(10)
					.build("homelander_boss")
	);

	private ModEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(HOMELANDER_BOSS, HomelanderBossEntity.createAttributes());
	}
}
