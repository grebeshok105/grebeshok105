package com.example.superheroes.client.render;

import com.example.superheroes.entity.RegulusProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class RegulusProjectileRenderer extends ThrownItemRenderer<RegulusProjectileEntity> {
	public RegulusProjectileRenderer(EntityRendererProvider.Context context) {
		super(context, 1.5f, false);
	}
}
