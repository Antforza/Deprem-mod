package com.depremmod.net;

import com.depremmod.DepremMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sunucudan oyuncuya: "deprem oldu" (her oyuncuya kendi mesafesiyle gonderilir). */
public record DepremStartPayload(
		float magnitude,
		float depthKm,
		float distanceKm,
		float felt,
		int durationTicks,
		String direction
) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DepremStartPayload> TYPE =
			new CustomPacketPayload.Type<>(DepremMod.id("start"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DepremStartPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, DepremStartPayload::magnitude,
			ByteBufCodecs.FLOAT, DepremStartPayload::depthKm,
			ByteBufCodecs.FLOAT, DepremStartPayload::distanceKm,
			ByteBufCodecs.FLOAT, DepremStartPayload::felt,
			ByteBufCodecs.VAR_INT, DepremStartPayload::durationTicks,
			ByteBufCodecs.STRING_UTF8, DepremStartPayload::direction,
			DepremStartPayload::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
