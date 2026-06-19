package com.bcrq.legit.client;

import com.bcrq.legit.LegitimityMod;
import net.fabricmc.api.ClientModInitializer;

public final class LegitimityModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LegitimityMod.LOGGER.info("Client bootstrap ready for {}", LegitimityMod.MOD_NAME);
		LegitimityClient.get().initialize();
	}
}
