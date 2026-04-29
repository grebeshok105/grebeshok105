package com.example.superheroes;

import com.example.superheroes.ability.AbilityRegistry;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.command.SuperheroesCommands;
import com.example.superheroes.effect.ModEffects;
import com.example.superheroes.hero.Heroes;
import com.example.superheroes.item.ModItemGroups;
import com.example.superheroes.item.ModItems;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.particle.ModParticles;
import com.example.superheroes.resource.ResourceController;
import com.example.superheroes.sound.ModSounds;
import com.example.superheroes.transform.HeroTransformService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperheroesMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ModId.MOD_ID);

	@Override
	public void onInitialize() {
		ModAttachments.init();
		ModEffects.init();
		Heroes.init();
		AbilityRegistry.init();
		com.example.superheroes.entity.ModEntities.init();
		ModItems.init();
		ModItemGroups.init();
		ModParticles.init();
		ModSounds.init();
		ModNetworking.init();
		ResourceController.init();
		com.example.superheroes.effect.MadnessFlightController.init();
		com.example.superheroes.effect.MadnessAftermathController.init();
		com.example.superheroes.effect.UnibeamController.init();
		com.example.superheroes.effect.HeroLandingTracker.init();
		com.example.superheroes.effect.HeroEquipmentLock.init();
		com.example.superheroes.effect.IronManReactorTracker.init();
		com.example.superheroes.effect.IronManAutoEjectController.init();
		com.example.superheroes.effect.RegulusTotemController.init();
		com.example.superheroes.effect.RegulusGreedController.init();
		com.example.superheroes.effect.RegulusMadnessController.init();
		com.example.superheroes.effect.SuperJumpController.init();
		com.example.superheroes.effect.AutoSaturationController.init();
		com.example.superheroes.effect.HomelanderRegenController.init();
		SuperheroesCommands.init();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			HeroTransformService.onPlayerJoin(handler.getPlayer());
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer serverPlayer) {
				HeroTransformService.forceUntransform(serverPlayer);
			}
		});

		EntityTrackingEvents.START_TRACKING.register((tracked, observer) -> {
			if (tracked instanceof ServerPlayer trackedPlayer) {
				ModNetworking.sendRemoteHeroSkinTo(observer, trackedPlayer);
			}
		});

		LOGGER.info("Superheroes mod initialized");
	}
}
