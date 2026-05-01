package com.example.superheroes.client.mixin;

import com.example.superheroes.client.FlightSpeedScrollHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerFlightSpeedMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void superheroes$flightSpeedScroll(long windowPointer, double horizontal, double vertical, CallbackInfo ci) {
		if (FlightSpeedScrollHandler.handle(vertical)) {
			ci.cancel();
		}
	}
}
