package com.example.superheroes.sound;

import com.example.superheroes.ModId;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	public static final SoundEvent LIGHTNING_THUNDER_ANIME = register("lightning.thunder.anime");
	public static final SoundEvent LIGHTNING_THUNDER_LOUD = register("lightning.thunder.loud");
	public static final SoundEvent UNIBEAM_CHARGE = register("unibeam.charge");
	public static final SoundEvent UNIBEAM_BEAM = register("unibeam.beam");
	public static final SoundEvent UNIBEAM_BLAST = register("unibeam.blast");
	public static final SoundEvent HOMELANDER_ROAR = register("homelander.roar");
	public static final SoundEvent HOMELANDER_ROAR_DEEP = register("homelander.roar.deep");
	public static final SoundEvent HOMELANDER_HAND_CLAP = register("homelander.hand_clap");
	public static final SoundEvent HOMELANDER_IRON_FISTS_IMPACT = register("homelander.iron_fists.impact");
	public static final SoundEvent HOMELANDER_IRON_FISTS_CHARGE = register("homelander.iron_fists.charge");
	public static final SoundEvent DOOMSDAY_ROAR = register("doomsday.roar");

	private ModSounds() {
	}

	private static SoundEvent register(String path) {
		ResourceLocation id = ModId.of(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {
	}
}
