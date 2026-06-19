package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;

public final class RapidEquipModule extends Module {
	private final NumberSetting speed = addSetting(new NumberSetting(this, "Speed", "Extra equip animation speed.", 1.8D, 1.0D, 3.5D, 0.1D));

	public RapidEquipModule() {
		super("Rapid Equip", "Speeds up held-item animation transitions.", ModuleCategory.RENDER);
	}

	public float getSpeedMultiplier() {
		return speed.getValue().floatValue();
	}
}
