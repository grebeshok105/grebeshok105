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
	public static final IronFistsAbility IRON_FISTS = new IronFistsAbility();
	public static final HandClapAbility HAND_CLAP = new HandClapAbility();
	public static final StunningRoarAbility STUNNING_ROAR = new StunningRoarAbility();
	public static final IronManFlightAbility IRON_MAN_FLIGHT = new IronManFlightAbility();
	public static final SupersonicAbility SUPERSONIC = new SupersonicAbility();
	public static final RepulsorAbility REPULSOR = new RepulsorAbility();
	public static final BoxEspAbility BOX_ESP = new BoxEspAbility();
	public static final UnibeamAbility UNIBEAM = new UnibeamAbility();

	public static final LionHeartAbility LION_HEART = new LionHeartAbility();
	public static final ManiaOfGreedAbility MANIA_OF_GREED = new ManiaOfGreedAbility();
	public static final LionRoarAbility LION_ROAR = new LionRoarAbility();
	public static final CounterStrikeAbility COUNTER_STRIKE = new CounterStrikeAbility();

	public static final AriseAbility ARISE = new AriseAbility();
	public static final ShadowExchangeAbility SHADOW_EXCHANGE = new ShadowExchangeAbility();
	public static final SacrificeAbility SACRIFICE = new SacrificeAbility();
	public static final RulersAuthorityAbility RULERS_AUTHORITY = new RulersAuthorityAbility();
	public static final ShadowExtractionAbility SHADOW_EXTRACTION = new ShadowExtractionAbility();
	public static final MonarchsDomainAbility MONARCHS_DOMAIN = new MonarchsDomainAbility();

	public static final DoomsdaySmashAbility DOOMSDAY_SMASH = new DoomsdaySmashAbility();
	public static final DoomsdayRoarAbility DOOMSDAY_ROAR = new DoomsdayRoarAbility();
	public static final DoomsdayBerserkAbility DOOMSDAY_BERSERK = new DoomsdayBerserkAbility();
	public static final DoomsdayBoneSpikeAbility DOOMSDAY_BONE_SPIKE = new DoomsdayBoneSpikeAbility();

	private AbilityRegistry() {
	}

	public static void init() {
		register(FLIGHT);
		register(EYE_LASERS);
		register(X_RAY);
		register(IRON_FISTS);
		register(HAND_CLAP);
		register(STUNNING_ROAR);
		register(IRON_MAN_FLIGHT);
		register(SUPERSONIC);
		register(REPULSOR);
		register(BOX_ESP);
		register(UNIBEAM);
		register(LION_HEART);
		register(MANIA_OF_GREED);
		register(LION_ROAR);
		register(COUNTER_STRIKE);
		register(ARISE);
		register(SHADOW_EXCHANGE);
		register(SACRIFICE);
		register(RULERS_AUTHORITY);
		register(SHADOW_EXTRACTION);
		register(MONARCHS_DOMAIN);
		register(DOOMSDAY_SMASH);
		register(DOOMSDAY_ROAR);
		register(DOOMSDAY_BERSERK);
		register(DOOMSDAY_BONE_SPIKE);
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
