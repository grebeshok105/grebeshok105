package com.example.superheroes.client.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Accessor("accumulatedDX")
	double superheroes$getAccumulatedDX();

	@Accessor("accumulatedDX")
	void superheroes$setAccumulatedDX(double v);

	@Accessor("accumulatedDY")
	double superheroes$getAccumulatedDY();

	@Accessor("accumulatedDY")
	void superheroes$setAccumulatedDY(double v);
}
