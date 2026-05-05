package com.example.superheroes.client.mixin;

import com.example.superheroes.client.ClientLokiMindManipulationState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MindManipulationGuiMixin {
	@Unique
	private boolean superheroes$mindFlipped;

	@Inject(method = "render", at = @At("HEAD"))
	private void superheroes$mindFlipHead(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		if (ClientLokiMindManipulationState.active()) {
			PoseStack pose = graphics.pose();
			float w = graphics.guiWidth();
			float h = graphics.guiHeight();
			pose.pushPose();
			pose.translate(w / 2.0f, h / 2.0f, 0);
			pose.mulPose(Axis.ZP.rotation((float) Math.PI));
			pose.translate(-w / 2.0f, -h / 2.0f, 0);
			this.superheroes$mindFlipped = true;
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void superheroes$mindFlipReturn(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		if (this.superheroes$mindFlipped) {
			graphics.pose().popPose();
			this.superheroes$mindFlipped = false;
		}
	}
}
