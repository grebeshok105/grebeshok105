package com.example.superheroes.network;

import com.example.superheroes.ModId;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlenderFieldS2CPayload(boolean inside, int remainingTicks) implements CustomPacketPayload {
	public static final Type<SlenderFieldS2CPayload> TYPE = new Type<>(ModId.of("slender_field"));

	public static final StreamCodec<ByteBuf, SlenderFieldS2CPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, SlenderFieldS2CPayload::inside,
			ByteBufCodecs.VAR_INT, SlenderFieldS2CPayload::remainingTicks,
			SlenderFieldS2CPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
