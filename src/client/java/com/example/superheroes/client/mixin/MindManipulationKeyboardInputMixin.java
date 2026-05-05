package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientLokiMindManipulationState;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MindManipulationKeyboardInputMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void superheroes$mindManipulationInvert(boolean isInWater, float scale, CallbackInfo ci) {
		if (!ClientLokiMindManipulationState.active()) return;
		Input self = (Input) (Object) this;
		boolean u = self.up, d = self.down, l = self.left, r = self.right;
		self.up = d;
		self.down = u;
		self.left = r;
		self.right = l;
		boolean j = self.jumping, sh = self.shiftKeyDown;
		self.jumping = sh;
		self.shiftKeyDown = j;
		self.forwardImpulse = -self.forwardImpulse;
		self.leftImpulse = -self.leftImpulse;
	}
}
