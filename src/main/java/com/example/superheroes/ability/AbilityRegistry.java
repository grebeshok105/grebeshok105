package com.example.superheroes.ability;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityRegistry {
	private static final Map<ResourceLocation, Ability> REGISTRY = new LinkedHashMap<>();

	public static final FlightAbility FLIGHT = new FlightAbility();
	public static final EyeLasersAbility EYE_LASERS = new EyeLasersAbility();
	public static final XRayAbility X_RAY = new XRayAbility();
	public static final IronManFlightAbility IRON_MAN_FLIGHT = new IronManFlightAbility();
	public static final SupersonicAbility SUPERSONIC = new SupersonicAbility();
	public static final RepulsorAbility REPULSOR = new RepulsorAbility();
	public static final BoxEspAbility BOX_ESP = new BoxEspAbility();
	public static final UnibeamAbility UNIBEAM = new UnibeamAbility();

	public static final LionHeartAbility LION_HEART = new LionHeartAbility();
	public static final ManiaOfGreedAbility MANIA_OF_GREED = new ManiaOfGreedAbility();
	public static final LionRoarAbility LION_ROAR = new LionRoarAbility();
	public static final CounterStrikeAbility COUNTER_STRIKE = new CounterStrikeAbility();
	public static final SuperJumpAbility SUPER_JUMP = new SuperJumpAbility();

	private AbilityRegistry() {
	}

	public static void init() {
		register(FLIGHT);
		register(EYE_LASERS);
		register(X_RAY);
		register(IRON_MAN_FLIGHT);
		register(SUPERSONIC);
		register(REPULSOR);
		register(BOX_ESP);
		register(UNIBEAM);
		register(LION_HEART);
		register(MANIA_OF_GREED);
		register(LION_ROAR);
		register(COUNTER_STRIKE);
		register(SUPER_JUMP);
	}

	public static void register(Ability ability) {
		REGISTRY.put(ability.getId(), ability);
	}

	@Nullable
	public static Ability get(@Nullable ResourceLocation id) {
		return id == null ? null : REGISTRY.get(id);
	}

	public static Map<ResourceLocation, Ability> all() {
		return Collections.unmodifiableMap(REGISTRY);
	}
}
