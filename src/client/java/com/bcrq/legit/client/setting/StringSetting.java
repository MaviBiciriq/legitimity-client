package com.bcrq.legit.client.setting;

import com.bcrq.legit.client.module.Module;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class StringSetting extends Setting<String> {
	private final int maxLength;

	public StringSetting(Module module, String name, String description, String defaultValue, int maxLength) {
		super(module, name, description, defaultValue);
		this.maxLength = maxLength;
	}

	@Override
	public void setValue(String value) {
		String sanitized = value == null ? "" : value;
		if (sanitized.length() > maxLength) {
			sanitized = sanitized.substring(0, maxLength);
		}

		super.setValue(sanitized);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			setValue(element.getAsString());
		}
	}

	public int getMaxLength() {
		return maxLength;
	}
}
