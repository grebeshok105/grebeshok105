package com.example.superheroes.client.render;

import com.example.superheroes.item.VibraniumShieldItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VibraniumShieldGeoModel extends GeoModel<VibraniumShieldItem> {
	private static final ResourceLocation MODEL =
			ResourceLocation.fromNamespaceAndPath("superheroes", "geo/vibranium_shield.geo.json");
	private static final ResourceLocation TEXTURE =
			ResourceLocation.fromNamespaceAndPath("superheroes", "textures/item/vibranium_shield.png");
	private static final ResourceLocation ANIMATION =
			ResourceLocation.fromNamespaceAndPath("superheroes", "animations/vibranium_shield.animation.json");

	@Override
	public ResourceLocation getModelResource(VibraniumShieldItem item) {
		return MODEL;
	}

	@Override
	public ResourceLocation getTextureResource(VibraniumShieldItem item) {
		return TEXTURE;
	}

	@Override
	public ResourceLocation getAnimationResource(VibraniumShieldItem item) {
		return ANIMATION;
	}
}
