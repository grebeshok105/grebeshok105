package com.example.superheroes.network;

import com.example.superheroes.ModId;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlenderStaticS2CPayload(int stacks, float fadeAlpha) implements CustomPacketPayload {
	public static final Type<SlenderStaticS2CPayload> TYPE = new Type<>(ModId.of("slender_static"));

	public static final StreamCodec<ByteBuf, SlenderStaticS2CPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SlenderStaticS2CPayload::stacks,
			ByteBufCodecs.FLOAT, SlenderStaticS2CPayload::fadeAlpha,
			SlenderStaticS2CPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
