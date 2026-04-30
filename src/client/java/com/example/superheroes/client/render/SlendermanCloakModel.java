package com.example.superheroes.client.render;

import com.example.superheroes.ModId;
import com.example.superheroes.entity.SlendermanCloakEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SlendermanCloakModel extends GeoModel<SlendermanCloakEntity> {
	private static final ResourceLocation MODEL = ModId.of("geo/entity/slenderman.geo.json");
	private static final ResourceLocation TEXTURE = ModId.of("textures/entity/hero/slenderman.png");
	private static final ResourceLocation ANIMATION = ModId.of("animations/entity/slenderman.animation.json");

	@Override
	public ResourceLocation getModelResource(SlendermanCloakEntity entity) {
		return MODEL;
	}

	@Override
	public ResourceLocation getTextureResource(SlendermanCloakEntity entity) {
		return TEXTURE;
	}

	@Override
	public ResourceLocation getAnimationResource(SlendermanCloakEntity entity) {
		return ANIMATION;
	}
}
