package com.example.superheroes.network;

import com.example.superheroes.ModId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FlightSpeedSyncS2CPayload(int percent) implements CustomPacketPayload {
	public static final Type<FlightSpeedSyncS2CPayload> TYPE = new Type<>(ModId.of("flight_speed_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightSpeedSyncS2CPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, FlightSpeedSyncS2CPayload::percent,
			FlightSpeedSyncS2CPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
