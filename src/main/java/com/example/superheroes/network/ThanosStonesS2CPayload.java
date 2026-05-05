package com.example.superheroes.network;

import com.example.superheroes.ModId;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ThanosStonesS2CPayload(UUID playerId, int bitmask, boolean broken) implements CustomPacketPayload {
	public static final Type<ThanosStonesS2CPayload> TYPE = new Type<>(ModId.of("thanos_stones"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ThanosStonesS2CPayload> STREAM_CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC, ThanosStonesS2CPayload::playerId,
					ByteBufCodecs.VAR_INT, ThanosStonesS2CPayload::bitmask,
					ByteBufCodecs.BOOL, ThanosStonesS2CPayload::broken,
					ThanosStonesS2CPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
