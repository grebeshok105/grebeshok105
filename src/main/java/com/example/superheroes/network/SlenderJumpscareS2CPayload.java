package com.example.superheroes.network;

import com.example.superheroes.ModId;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlenderJumpscareS2CPayload(int durationTicks) implements CustomPacketPayload {
	public static final Type<SlenderJumpscareS2CPayload> TYPE = new Type<>(ModId.of("slender_jumpscare"));

	public static final StreamCodec<ByteBuf, SlenderJumpscareS2CPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SlenderJumpscareS2CPayload::durationTicks,
			SlenderJumpscareS2CPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
