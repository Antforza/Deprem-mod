package com.depremmod;

import com.depremmod.client.PhoneScreen;
import com.depremmod.client.QuakeHistory;
import com.depremmod.client.ShakeController;
import com.depremmod.net.DepremStartPayload;
import com.depremmod.net.DepremStopPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class DepremClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Telefon sag tiklaninca uygulama ekrani acilsin
		TelefonItem.openScreenHook = () -> Minecraft.getInstance().gui.setScreen(new PhoneScreen());

		ClientPlayNetworking.registerGlobalReceiver(DepremStartPayload.TYPE, (payload, context) -> {
			QuakeHistory.add(payload);
			ShakeController.start(payload);
		});

		ClientPlayNetworking.registerGlobalReceiver(DepremStopPayload.TYPE, (payload, context) ->
				ShakeController.stop());

		// Kamera sallanmasi her client tick'te uygulanir
		ClientTickEvents.END_CLIENT_TICK.register(client -> ShakeController.tick(client.player));
	}
}
