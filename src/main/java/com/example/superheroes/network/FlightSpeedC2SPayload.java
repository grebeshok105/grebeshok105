package com.example.superheroes.network;

import com.example.superheroes.ModId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FlightSpeedC2SPayload(int delta) implements CustomPacketPayload {
	public static final Type<FlightSpeedC2SPayload> TYPE = new Type<>(ModId.of("flight_speed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightSpeedC2SPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, FlightSpeedC2SPayload::delta,
			FlightSpeedC2SPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
