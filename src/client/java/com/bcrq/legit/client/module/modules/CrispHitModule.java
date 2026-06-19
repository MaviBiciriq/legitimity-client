package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

public final class CrispHitModule extends Module {
	private final NumberSetting aimAssistMargin = addSetting(new NumberSetting(this, "Margin", "Extra local hit selection margin.", 0.12D, 0.02D, 0.40D, 0.01D));

	public CrispHitModule() {
		super("Crisp Hit", "Makes hits feel snappier by reducing client attack delay and slightly widening local target checks.", ModuleCategory.COMBAT);
	}

	@Override
	public void onTick(MinecraftClient client) {
		client.attackCooldown = 0;
	}

	public float getAimAssistMargin() {
		return aimAssistMargin.getValue().floatValue();
	}
}
