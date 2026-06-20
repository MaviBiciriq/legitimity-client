package com.bcrq.legit;

import com.bcrq.legit.util.LegitIds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LegitimityMod implements ModInitializer {
	public static final String MOD_ID = "bcrq.legit";
	public static final String MOD_NAME = "Legitimity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("{} is loading on Fabric 1.21.4", MOD_NAME);
		LOGGER.debug("Bootstrap identifier ready: {}", LegitIds.id("bootstrap"));
	}
}
