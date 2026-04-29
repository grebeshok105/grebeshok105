package com.example.superheroes.hero;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Heroes {
	private static final Map<ResourceLocation, Hero> REGISTRY = new LinkedHashMap<>();
	public static final HomelanderHero HOMELANDER = new HomelanderHero();
	public static final IronManHero IRON_MAN = new IronManHero();
	public static final RegulusHero REGULUS = new RegulusHero();
	public static final SungJinwooHero SUNG_JINWOO = new SungJinwooHero();
	public static final DoomsdayHero DOOMSDAY = new DoomsdayHero();

	private Heroes() {
	}

	public static void init() {
		register(HOMELANDER);
		register(IRON_MAN);
		register(REGULUS);
		register(SUNG_JINWOO);
		register(DOOMSDAY);
	}

	public static void register(Hero hero) {
		REGISTRY.put(hero.getId(), hero);
	}

	@Nullable
	public static Hero get(@Nullable ResourceLocation id) {
		return id == null ? null : REGISTRY.get(id);
	}

	public static Map<ResourceLocation, Hero> all() {
		return Collections.unmodifiableMap(REGISTRY);
	}
}
