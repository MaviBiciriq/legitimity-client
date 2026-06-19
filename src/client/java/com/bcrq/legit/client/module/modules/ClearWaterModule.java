package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;

public final class ClearWaterModule extends Module {
	private final BooleanSetting waterFog = addSetting(new BooleanSetting(this, "Water Fog Kapali", "Disables water fog for cleaner underwater vision.", true));
	private final BooleanSetting lavaFog = addSetting(new BooleanSetting(this, "Lava Fog Kapali", "Disables lava fog.", false));
	private final BooleanSetting powderSnowFog = addSetting(new BooleanSetting(this, "Snow Fog Kapali", "Disables powder snow fog.", false));
	private final BooleanSetting terrainFog = addSetting(new BooleanSetting(this, "Terrain Fog Kapali", "Disables regular terrain fog.", false));
	private final BooleanSetting skyFog = addSetting(new BooleanSetting(this, "Sky Fog Kapali", "Disables sky fog.", false));

	public ClearWaterModule() {
		super("Clear Fog", "Removes water fog and optionally clears other fog types too.", ModuleCategory.RENDER);
	}

	public boolean isWaterFogDisabled() {
		return waterFog.getValue();
	}

	public boolean isLavaFogDisabled() {
		return lavaFog.getValue();
	}

	public boolean isPowderSnowFogDisabled() {
		return powderSnowFog.getValue();
	}

	public boolean isTerrainFogDisabled() {
		return terrainFog.getValue();
	}

	public boolean isSkyFogDisabled() {
		return skyFog.getValue();
	}
}
