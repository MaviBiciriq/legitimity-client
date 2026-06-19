package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;

public final class HandStyleModule extends Module {
	private final NumberSetting offsetX = addSetting(new NumberSetting(this, "Offset X", "Horizontal hand offset.", 0.0D, -1.2D, 1.2D, 0.01D));
	private final NumberSetting offsetY = addSetting(new NumberSetting(this, "Offset Y", "Vertical hand offset.", 0.0D, -1.2D, 1.2D, 0.01D));
	private final NumberSetting offsetZ = addSetting(new NumberSetting(this, "Offset Z", "Depth hand offset.", 0.0D, -1.2D, 1.2D, 0.01D));

	public HandStyleModule() {
		super("Hand Style", "Lets you reposition the first-person hand transform.", ModuleCategory.RENDER);
	}

	public float getOffsetX() {
		return offsetX.getValue().floatValue();
	}

	public float getOffsetY() {
		return offsetY.getValue().floatValue();
	}

	public float getOffsetZ() {
		return offsetZ.getValue().floatValue();
	}
}
