package com.example.superheroes.client.render;

import com.example.superheroes.entity.SlendermanCloakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SlendermanCloakRenderer extends GeoEntityRenderer<SlendermanCloakEntity> {
	public SlendermanCloakRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new SlendermanCloakModel());
		this.shadowRadius = 0.5f;
		// The geo coordinates are in Bedrock units (~0.0625 per block); 1.0 maps
		// the model at default scale, which works out to roughly player height
		// for this geometry.
		this.withScale(1.0f);
	}
}
