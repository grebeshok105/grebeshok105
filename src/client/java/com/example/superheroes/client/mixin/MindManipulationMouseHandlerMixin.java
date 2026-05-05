package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientLokiMindManipulationState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MindManipulationMouseHandlerMixin {
	@Inject(method = "turnPlayer", at = @At("HEAD"))
	private void superheroes$mindManipulationFlipMouse(double timeFactor, CallbackInfo ci) {
		if (!ClientLokiMindManipulationState.active()) return;
		MouseHandlerAccessor self = (MouseHandlerAccessor) this;
		self.superheroes$setAccumulatedDX(-self.superheroes$getAccumulatedDX());
		self.superheroes$setAccumulatedDY(-self.superheroes$getAccumulatedDY());
	}
}
