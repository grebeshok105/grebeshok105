package com.example.superheroes.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeys {
	public static final String CATEGORY = "key.categories.superheroes";

	public static KeyMapping RADIAL;
	public static KeyMapping BINDINGS;
	public static KeyMapping TOGGLE_TOOLTIPS;
	public static KeyMapping[] ABILITY_SLOTS;

	private static final int[] DEFAULT_SLOT_KEYS = {
			GLFW.GLFW_KEY_Z,
			GLFW.GLFW_KEY_X,
			GLFW.GLFW_KEY_C,
			GLFW.GLFW_KEY_V
	};

	private ModKeys() {
	}

	public static void init() {
		RADIAL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.superheroes.radial",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				CATEGORY));
		BINDINGS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.superheroes.bindings",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				CATEGORY));
		TOGGLE_TOOLTIPS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.superheroes.toggle_tooltips",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_H,
				CATEGORY));
		ABILITY_SLOTS = new KeyMapping[DEFAULT_SLOT_KEYS.length];
		for (int i = 0; i < DEFAULT_SLOT_KEYS.length; i++) {
			ABILITY_SLOTS[i] = KeyBindingHelper.registerKeyBinding(new KeyMapping(
					"key.superheroes.ability_" + (i + 1),
					InputConstants.Type.KEYSYM,
					DEFAULT_SLOT_KEYS[i],
					CATEGORY));
		}
	}
}
