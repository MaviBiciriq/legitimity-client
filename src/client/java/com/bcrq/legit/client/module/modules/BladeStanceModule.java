package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;

public final class BladeStanceModule extends Module {
	private final NumberSetting strength = addSetting(new NumberSetting(this, "Strength", "How dramatic the sword block pose should be.", 1.0D, 0.6D, 1.6D, 0.05D));

	public BladeStanceModule() {
		super("Blade Stance", "Applies a 1.8-style right-click sword pose.", ModuleCategory.RENDER);
	}

	public float getStrength() {
		return strength.getValue().floatValue();
	}

	public boolean shouldRenderPose() {
		return net.minecraft.client.MinecraftClient.getInstance().options.useKey.isPressed();
	}
}
