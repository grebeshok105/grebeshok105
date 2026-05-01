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
	public static final IronManHulkbusterAbility IRON_MAN_HULKBUSTER = new IronManHulkbusterAbility();

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
	public static final ChargeTackleAbility DOOMSDAY_CHARGE_TACKLE = new ChargeTackleAbility();
	public static final DoomGripAbility DOOMSDAY_DOOM_GRIP = new DoomGripAbility();

	public static final GokuKamehamehaAbility GOKU_KAMEHAMEHA = new GokuKamehamehaAbility();
	public static final GokuInstantTransmissionAbility GOKU_INSTANT_TRANSMISSION = new GokuInstantTransmissionAbility();
	public static final GokuKiChargeAbility GOKU_KI_CHARGE = new GokuKiChargeAbility();
	public static final GokuSolarFlareAbility GOKU_SOLAR_FLARE = new GokuSolarFlareAbility();
	public static final GokuSpiritBombAbility GOKU_SPIRIT_BOMB = new GokuSpiritBombAbility();
	public static final GokuSuperSaiyanAuraAbility GOKU_SUPER_SAIYAN_AURA = new GokuSuperSaiyanAuraAbility();

	public static final NarutoRasenganAbility NARUTO_RASENGAN = new NarutoRasenganAbility();
	public static final NarutoShadowClonesAbility NARUTO_SHADOW_CLONES = new NarutoShadowClonesAbility();
	public static final NarutoRasenshurikenAbility NARUTO_RASENSHURIKEN = new NarutoRasenshurikenAbility();
	public static final NarutoSageModeAbility NARUTO_SAGE_MODE = new NarutoSageModeAbility();
	public static final NarutoOodamaRasenganAbility NARUTO_OODAMA_RASENGAN = new NarutoOodamaRasenganAbility();

	public static final CapShieldThrowAbility CAP_SHIELD_THROW = new CapShieldThrowAbility();
	public static final CapShieldSlamAbility CAP_SHIELD_SLAM = new CapShieldSlamAbility();
	public static final CapShieldDashAbility CAP_SHIELD_DASH = new CapShieldDashAbility();
	public static final CapCounterStanceAbility CAP_COUNTER_STANCE = new CapCounterStanceAbility();

	public static final SlenderBlinkAbility SLENDER_BLINK = new SlenderBlinkAbility();
	public static final SlenderTendrilsAbility SLENDER_TENDRILS = new SlenderTendrilsAbility();
	public static final SlenderPhaseStalkAbility SLENDER_PHASE_STALK = new SlenderPhaseStalkAbility();
	public static final SlenderStaticFieldAbility SLENDER_STATIC_FIELD = new SlenderStaticFieldAbility();

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
		register(IRON_MAN_HULKBUSTER);
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
		register(DOOMSDAY_CHARGE_TACKLE);
		register(DOOMSDAY_DOOM_GRIP);
		register(GOKU_KAMEHAMEHA);
		register(GOKU_INSTANT_TRANSMISSION);
		register(GOKU_KI_CHARGE);
		register(GOKU_SOLAR_FLARE);
		register(GOKU_SPIRIT_BOMB);
		register(GOKU_SUPER_SAIYAN_AURA);
		register(NARUTO_RASENGAN);
		register(NARUTO_SHADOW_CLONES);
		register(NARUTO_RASENSHURIKEN);
		register(NARUTO_SAGE_MODE);
		register(NARUTO_OODAMA_RASENGAN);
		register(CAP_SHIELD_THROW);
		register(CAP_SHIELD_SLAM);
		register(CAP_SHIELD_DASH);
		register(CAP_COUNTER_STANCE);
		register(SLENDER_BLINK);
		register(SLENDER_TENDRILS);
		register(SLENDER_PHASE_STALK);
		register(SLENDER_STATIC_FIELD);
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
