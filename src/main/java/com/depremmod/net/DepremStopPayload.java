package com.depremmod.net;

import com.depremmod.DepremMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sunucudan oyuncuya: "depremi durdur" (/deprem stop). */
public record DepremStopPayload() implements CustomPacketPayload {
	public static final DepremStopPayload INSTANCE = new DepremStopPayload();

	public static final CustomPacketPayload.Type<DepremStopPayload> TYPE =
			new CustomPacketPayload.Type<>(DepremMod.id("stop"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DepremStopPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
