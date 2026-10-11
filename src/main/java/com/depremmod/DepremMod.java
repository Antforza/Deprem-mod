package com.depremmod;

import com.depremmod.net.DepremStartPayload;
import com.depremmod.net.DepremStopPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepremMod implements ModInitializer {
	public static final String MOD_ID = "deprem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		DepremConfig.load();
		ModItems.initialize();

		// Sunucu -> istemci paketleri (hem sunucuda hem istemcide kayitli olmali)
		PayloadTypeRegistry.clientboundPlay().register(DepremStartPayload.TYPE, DepremStartPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(DepremStopPayload.TYPE, DepremStopPayload.CODEC);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				DepremCommand.register(dispatcher));

		// Kendiliginden deprem zamanlayicisi
		ServerTickEvents.END_SERVER_TICK.register(EarthquakeManager::tick);

		LOGGER.info("Deprem modu yuklendi.");
	}
}
