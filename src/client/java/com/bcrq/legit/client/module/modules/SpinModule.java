package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.NumberSetting;

public final class SpinModule extends Module {
	private final NumberSetting speed = addSetting(
			new NumberSetting(this, "Speed", "Spin speed in degrees per second.", 720.0D, 90.0D, 3600.0D, 30.0D));
	private final BooleanSetting entities = addSetting(
			new BooleanSetting(this, "Entities", "Spin rendered players and mobs client-side.", true));
	private final BooleanSetting head = addSetting(
			new BooleanSetting(this, "Head", "Vertically spin biped heads client-side.", true));
	private final BooleanSetting hand = addSetting(
			new BooleanSetting(this, "Hand", "Spin held first-person items client-side.", true));

	public SpinModule() {
		super("Spin", "Troll visual module that spins entities, heads, and held items locally.", ModuleCategory.RENDER);
	}

	public float getAngle(float tickDelta) {
		long millis = System.currentTimeMillis();
		double seconds = millis / 1000.0D + tickDelta / 20.0D;
		return (float) (seconds * speed.getValue() % 360.0D);
	}

	public boolean shouldSpinEntities() {
		return isEnabled() && entities.getValue();
	}

	public boolean shouldSpinHead() {
		return isEnabled() && head.getValue();
	}

	public boolean shouldSpinHand() {
		return isEnabled() && hand.getValue();
	}
}
