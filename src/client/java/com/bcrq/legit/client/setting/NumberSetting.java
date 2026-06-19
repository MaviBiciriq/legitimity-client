package com.bcrq.legit.client.setting;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.util.AnimationUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumberSetting extends Setting<Double> {
	private final double min;
	private final double max;
	private final double step;

	public NumberSetting(Module module, String name, String description, double defaultValue, double min, double max, double step) {
		super(module, name, description, defaultValue);
		this.min = min;
		this.max = max;
		this.step = step;
		setValue(defaultValue);
	}

	@Override
	public void setValue(Double value) {
		double clamped = AnimationUtil.clamp(value, min, max);
		double snapped = Math.round(clamped / step) * step;
		super.setValue(AnimationUtil.clamp(snapped, min, max));
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			setValue(element.getAsDouble());
		}
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getStep() {
		return step;
	}
}
