package com.bcrq.legit.client.setting;

import com.bcrq.legit.client.module.Module;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BooleanSetting extends Setting<Boolean> {
	public BooleanSetting(Module module, String name, String description, boolean defaultValue) {
		super(module, name, description, defaultValue);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			setValue(element.getAsBoolean());
		}
	}
}
