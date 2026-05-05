package com.example.superheroes.network;

import com.example.superheroes.ModId;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C: жертва получает 10-секундную "Манипуляцию разумом" Локи —
 * экран и HUD переворачиваются, управление инвертируется.
 *
 * @param durationMs длительность эффекта в миллисекундах (0 — отключить досрочно)
 */
public record LokiMindManipulationS2CPayload(int durationMs) implements CustomPacketPayload {
	public static final Type<LokiMindManipulationS2CPayload> TYPE =
			new Type<>(ModId.of("loki_mind_manipulation"));

	public static final StreamCodec<ByteBuf, LokiMindManipulationS2CPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, LokiMindManipulationS2CPayload::durationMs,
			LokiMindManipulationS2CPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
