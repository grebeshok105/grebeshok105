package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientLokiMindManipulationState;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MindManipulationCameraMixin {
	@Shadow
	@Final
	private Quaternionf rotation;

	@Inject(method = "setRotation", at = @At("TAIL"))
	private void superheroes$mindManipulationFlipCamera(float yaw, float pitch, CallbackInfo ci) {
		if (ClientLokiMindManipulationState.active()) {
			this.rotation.rotateZ((float) Math.PI);
		}
	}
}
