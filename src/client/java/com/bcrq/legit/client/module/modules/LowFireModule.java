package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;

public final class LowFireModule extends Module {
	private final NumberSetting scalePercent = addSetting(new NumberSetting(this, "Yuzde", "Scales the fire overlay height.", 50.0D, 10.0D, 100.0D, 5.0D));

	public LowFireModule() {
		super("Low Fire", "Reduces the burning fire overlay by roughly fifty percent.", ModuleCategory.RENDER);
	}

	public float getScalePercent() {
		return scalePercent.getValue().floatValue() / 100.0f;
	}
}
