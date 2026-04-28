package com.example.superheroes.attachment;

import com.example.superheroes.ModId;
import com.example.superheroes.effect.RegulusMadnessState;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	public static final AttachmentType<HeroData> HERO_DATA = AttachmentRegistry.<HeroData>builder()
			.initializer(() -> HeroData.EMPTY)
			.persistent(HeroData.CODEC)
			.copyOnDeath()
			.buildAndRegister(ModId.of("hero_data"));

	public static final AttachmentType<RegulusMadnessState> REGULUS_MADNESS = AttachmentRegistry.<RegulusMadnessState>builder()
			.initializer(() -> RegulusMadnessState.EMPTY)
			.persistent(RegulusMadnessState.CODEC)
			.copyOnDeath()
			.buildAndRegister(ModId.of("regulus_madness"));

	private ModAttachments() {
	}

	public static void init() {
	}
}
